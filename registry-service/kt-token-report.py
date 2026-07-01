#!/usr/bin/env python3
"""
KT Pipeline — Chronological Token Report (read-only)

Parses the Claude Code session transcript for this project and reports how many
tokens each KT phase (graphify, cartographer, acquire, kt-writer, kt-validator,
doublecheck ...) consumed, in the order the pipeline actually ran.

It does NOT touch any Java/config/source file. It only reads:
  - the session transcript JSONL  (~/.claude/projects/<slug>/<session>.jsonl)
  - that session's subagent transcripts (<session>/subagents/agent-*.jsonl)
  - graphify-out/cost.json        (graphify's own internal LLM token counts)

and writes a single report file: TOKEN_REPORT.md

Usage:
  python kt-token-report.py                 # report the MOST RECENT KT run
  python kt-token-report.py --session <id>  # force a specific session id/file
  python kt-token-report.py --gap 30        # minutes of idle that ends a run
  python kt-token-report.py --out TOKEN_REPORT.md
"""

import argparse
import json
from datetime import datetime, timedelta, timezone
from pathlib import Path

# Project transcript folder is derived from the working dir name, the same way
# Claude Code slugifies it: drive/colon/backslash/slash -> '-'.
PROJECT_DIR = Path(__file__).resolve().parent
PROJECT_SLUG = "c--Workspace-demo-registry-service"
TRANSCRIPT_ROOT = Path.home() / ".claude" / "projects" / PROJECT_SLUG

KT_SKILLS = {
    "graphify",
    "cartographer",
    "acquire-codebase-knowledge",
    "kt-writer",
    "kt-validator",
    "doublecheck",
}


def parse_ts(s):
    """Parse an ISO-8601 timestamp (with trailing Z) into an aware datetime."""
    if not s:
        return None
    try:
        return datetime.fromisoformat(s.replace("Z", "+00:00"))
    except ValueError:
        return None


# The four token components we track separately. Order = column order in report.
COMPONENTS = ("input", "output", "cache_creation", "cache_read")


def token_components(usage):
    """Break a usage block into its four token components.

    Returns a dict with keys input / output / cache_creation / cache_read.
    Nothing is excluded here — every component is reported on its own; the
    report sums them into per-row and per-column totals.
    """
    if not isinstance(usage, dict):
        return {k: 0 for k in COMPONENTS}
    return {
        "input": usage.get("input_tokens", 0),
        "output": usage.get("output_tokens", 0),
        "cache_creation": usage.get("cache_creation_input_tokens", 0),
        "cache_read": usage.get("cache_read_input_tokens", 0),
    }


def comp_total(comp):
    """Sum all four components of a component dict."""
    return sum(comp[k] for k in COMPONENTS)


def _records(path):
    """Yield parsed JSON objects from a JSONL file, skipping bad lines."""
    try:
        with open(path, encoding="utf-8") as f:
            for line in f:
                line = line.strip()
                if not line:
                    continue
                try:
                    yield json.loads(line)
                except json.JSONDecodeError:
                    continue
    except OSError:
        return


def usage_entries(path):
    """Yield (datetime, component_dict) for every assistant message with usage."""
    for obj in _records(path):
        ts = parse_ts(obj.get("timestamp"))
        msg = obj.get("message")
        usage = msg.get("usage") if isinstance(msg, dict) else None
        if usage is None:
            usage = obj.get("usage")
        if ts and usage:
            comp = token_components(usage)
            if comp_total(comp):
                yield ts, comp


def skill_calls(path):
    """Yield (datetime, skill_name) for every Skill tool_use in the main file."""
    for obj in _records(path):
        ts = parse_ts(obj.get("timestamp"))
        msg = obj.get("message")
        if not (ts and isinstance(msg, dict)):
            continue
        content = msg.get("content")
        if not isinstance(content, list):
            continue
        for block in content:
            if (
                isinstance(block, dict)
                and block.get("type") == "tool_use"
                and block.get("name") == "Skill"
            ):
                skill = (block.get("input") or {}).get("skill", "?")
                yield ts, skill


def list_runs():
    """Return [(kt_count, session_id, first_dt, last_dt)] for every KT session,
    MOST RECENT first (so the top row is the run the default reports). One
    session ≈ one KT run in practice."""
    rows = []
    for p in TRANSCRIPT_ROOT.glob("*.jsonl"):
        times = [ts for ts, s in skill_calls(p) if s in KT_SKILLS]
        if times:
            rows.append((len(times), p.stem, min(times), max(times)))
    return sorted(rows, key=lambda r: r[3], reverse=True)


def find_session(session_arg):
    """Resolve which transcript file to use."""
    if session_arg:
        p = Path(session_arg)
        if p.is_file():
            return p
        cand = TRANSCRIPT_ROOT / f"{session_arg}.jsonl"
        if cand.is_file():
            return cand
        raise SystemExit(f"Session not found: {session_arg}")

    # Auto-detect: the MOST RECENT KT run — the transcript whose latest KT skill
    # call is newest. This guarantees the freshest run is always reported, even
    # if an older run happened to log more skill calls (e.g. a validation
    # fix-loop). File mtime is only a tie-breaker for the rare case of equal
    # timestamps.
    best = None  # ((last_kt_ts, mtime), path)
    for p in TRANSCRIPT_ROOT.glob("*.jsonl"):
        kt_times = [ts for ts, s in skill_calls(p) if s in KT_SKILLS]
        if not kt_times:
            continue
        key = (max(kt_times), p.stat().st_mtime)
        if best is None or key > best[0]:
            best = (key, p)
    if best is None:
        raise SystemExit(
            f"No KT run found in {TRANSCRIPT_ROOT}. Pass --session <id> explicitly."
        )
    return best[1]


def fmt(n):
    return f"{n:,}"


def main():
    ap = argparse.ArgumentParser(description="Chronological KT token report")
    ap.add_argument("--session", help="session id or path to a .jsonl transcript")
    ap.add_argument(
        "--gap",
        type=float,
        default=30.0,
        help="idle minutes that mark the end of a run (default 30)",
    )
    ap.add_argument(
        "--out",
        default=str(PROJECT_DIR / "TOKEN_REPORT.md"),
        help="output report path (default: TOKEN_REPORT.md)",
    )
    ap.add_argument(
        "--list",
        action="store_true",
        help="list every detected KT run and exit (no report written)",
    )
    args = ap.parse_args()

    runs = list_runs()
    if args.list:
        print("Detected KT runs (target any with --session <id>):\n")
        print(f"  {'session':40} {'#calls':>6}  {'first':14} {'last':14}")
        for n, sid, a, b in runs:
            print(f"  {sid:40} {n:>6}  "
                  f"{a.astimezone():%m-%d %H:%M}   {b.astimezone():%m-%d %H:%M}")
        return

    main_file = find_session(args.session)
    session_id = main_file.stem

    # All Skill calls in chronological order; keep only KT-pipeline ones.
    calls = sorted(skill_calls(main_file), key=lambda x: x[0])
    kt_calls = [(ts, s) for ts, s in calls if s in KT_SKILLS]
    if not kt_calls:
        raise SystemExit(f"No KT skill calls in {main_file}")

    # Gather every token-bearing entry: main transcript + this session's subagents.
    entries = list(usage_entries(main_file))
    sub_dir = TRANSCRIPT_ROOT / session_id / "subagents"
    for sub in sub_dir.glob("agent-*.jsonl"):
        entries.extend(usage_entries(sub))
    entries.sort(key=lambda x: x[0])

    # The run spans from the first KT skill call to the last. Idle pauses BETWEEN
    # phases (you reviewing output, etc.) must NOT split the run, so the gap rule
    # is applied only AFTER the final skill call — to trim trailing chatter from
    # a resumed session.
    run_start = kt_calls[0][0]
    last_skill_ts = kt_calls[-1][0]
    gap = timedelta(minutes=args.gap)
    run_end = last_skill_ts
    for ts, _comp in entries:
        if ts < last_skill_ts:
            continue
        if ts <= run_end + gap:
            run_end = max(run_end, ts)
        else:
            break

    # Build phase windows. Each KT skill call opens a phase that runs until the
    # next KT skill call (last one runs to run_end). Tokens spent before the
    # first skill call (orchestrator setup) get their own leading bucket.
    boundaries = [ts for ts, _ in kt_calls]
    phases = []  # (label, start, end)
    if entries and entries[0][0] < run_start:
        phases.append(("orchestration (pre-graphify)", entries[0][0], run_start))
    for i, (ts, skill) in enumerate(kt_calls):
        end = boundaries[i + 1] if i + 1 < len(boundaries) else run_end + timedelta(seconds=1)
        phases.append((f"{skill}", ts, end))

    # Bucket tokens into phases by timestamp. Each phase keeps a per-component
    # dict so the report can show input / output / cache columns separately.
    totals = [{k: 0 for k in COMPONENTS} for _ in phases]
    for ts, comp in entries:
        if ts < phases[0][1] or ts > run_end:
            continue
        for i, (_, start, end) in enumerate(phases):
            if start <= ts < end:
                for k in COMPONENTS:
                    totals[i][k] += comp[k]
                break

    # graphify's own internal LLM calls are external to the transcript; fold in
    # its cost.json. cost.json keeps a per-run history AND a cumulative total —
    # use the latest non-empty run so this stays a per-run figure (not all-time).
    # graphify reports only raw input/output (no cache fields).
    graphify_internal = {k: 0 for k in COMPONENTS}
    cost_path = PROJECT_DIR / "graphify-out" / "cost.json"
    if cost_path.is_file():
        try:
            cost = json.loads(cost_path.read_text(encoding="utf-8"))
            for r in cost.get("runs", []):
                gi = r.get("input_tokens", 0)
                go = r.get("output_tokens", 0)
                if gi + go > 0:
                    graphify_internal = {  # keep last non-empty run
                        "input": gi, "output": go,
                        "cache_creation": 0, "cache_read": 0,
                    }
        except (OSError, json.JSONDecodeError):
            pass

    def local(dt):
        return dt.astimezone().strftime("%H:%M:%S")

    # Number only the actual pipeline phases (skip the setup bucket in numbering).
    lines = []
    lines.append("# KT Pipeline — Token Usage (Chronological)")
    lines.append("")
    lines.append("_Scope: a **single** KT run. This is not a total across all your "
                 f"runs — {len(runs)} KT run(s) exist; see `--list`._")
    lines.append("")
    lines.append(f"- **Session:** `{session_id}`")
    lines.append(f"- **Run window:** {local(run_start)} → {local(run_end)} (local time)")
    lines.append(f"- **Transcript:** `{main_file}`")
    lines.append("")
    lines.append("Every token component is reported **separately**: fresh "
                 "**input**, generated **output**, **cache-creation** (first-time "
                 "writes to cache) and **cache-read** (cheap cached re-reads). The "
                 "**Total** column sums all four for each phase; the bottom row "
                 "totals each column.")
    lines.append("")
    lines.append("| # | Phase (skill) | Start | End | Input | Output | "
                 "Cache-create | Cache-read | Total |")
    lines.append("|---|---------------|-------|-----|------:|-------:|"
                 "-------------:|-----------:|------:|")

    grand = {k: 0 for k in COMPONENTS}
    step = 0
    for (label, start, end), comp in zip(phases, totals):
        shown = dict(comp)
        note = ""
        if label == "graphify" and comp_total(graphify_internal):
            for k in COMPONENTS:
                shown[k] += graphify_internal[k]
            note = f" *(incl. {fmt(comp_total(graphify_internal))} graphify-internal)*"
        for k in COMPONENTS:
            grand[k] += shown[k]
        row_total = comp_total(shown)
        if label.startswith("orchestration"):
            num = "—"
        else:
            step += 1
            num = str(step)
        end_show = local(end) if end <= run_end + timedelta(seconds=2) else local(run_end)
        lines.append(
            f"| {num} | {label}{note} | {local(start)} | {end_show} | "
            f"{fmt(shown['input'])} | {fmt(shown['output'])} | "
            f"{fmt(shown['cache_creation'])} | {fmt(shown['cache_read'])} | "
            f"{fmt(row_total)} |"
        )

    lines.append(
        f"| | **GRAND TOTAL** | | | **{fmt(grand['input'])}** | "
        f"**{fmt(grand['output'])}** | **{fmt(grand['cache_creation'])}** | "
        f"**{fmt(grand['cache_read'])}** | **{fmt(comp_total(grand))}** |"
    )
    lines.append("")
    lines.append("> Notes")
    lines.append("> - Phase boundaries are the `Skill` tool calls in the session "
                 "transcript; tokens are bucketed by timestamp, so parallel "
                 "subagent tokens are included in the phase that spawned them.")
    if comp_total(graphify_internal):
        lines.append("> - The graphify row adds graphify's own LLM tokens from "
                     "`graphify-out/cost.json` (its work happens outside the "
                     "Claude transcript); graphify reports only input/output, "
                     "so it adds nothing to the cache columns.")
    lines.append(f"> - A run ends after {args.gap:g} min of idle; later activity "
                 "in a resumed session is excluded.")
    lines.append("")

    out_path = Path(args.out)
    out_path.write_text("\n".join(lines), encoding="utf-8")

    # Console summary.
    print(f"Reporting the LATEST of {len(runs)} detected KT run(s) (use --list to"
          " see all, --session <id> to pick another).\n")
    print(f"Session : {session_id}")
    print(f"Run     : {local(run_start)} -> {local(run_end)}")
    print(f"Phases  : {step}")
    print(f"Input         : {fmt(grand['input'])}")
    print(f"Output        : {fmt(grand['output'])}")
    print(f"Cache-create  : {fmt(grand['cache_creation'])}")
    print(f"Cache-read    : {fmt(grand['cache_read'])}")
    print(f"TOTAL (all)   : {fmt(comp_total(grand))} tokens")
    print(f"Report  : {out_path}")


if __name__ == "__main__":
    main()
