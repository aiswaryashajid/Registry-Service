# KT Document Orchestrator — Agent README

> An autonomous, multi-skill agent that turns a Spring Boot Maven codebase into a
> professional, **hallucination-checked** Knowledge Transfer (KT) document — with a
> knowledge graph, a codebase map, structured analysis docs, and a 12-test validation gate.

This folder (`.claude/`) contains everything that defines the agent: the orchestrator
contract (`../CLAUDE.md`), six skills, two slash-commands, and local settings. This README
explains **what the agent is, how it is wired, what it produces, and the rules that keep it
accurate and cheap to run.**

---

## 1. What this agent does

Given any Spring Boot Maven project, the agent produces a complete onboarding/handover
package so a brand-new development team can understand the system without reading the code
first:

- A **knowledge graph** of the code (classes, methods, edges, communities, "god nodes").
- A **codebase map** with end-to-end feature flows (HTTP → Controller → Service → Repository → DB).
- **Seven structured knowledge documents** (stack, structure, architecture, conventions, integrations, testing, concerns).
- A **14-section KT document** (`docs/KT_DOCUMENT.md`) — the final deliverable.
- A **validation report** proving every claim in the KT doc is traceable to real source.

The guiding principle is **"never write anything not found in actual code."** Every phase
is followed by an independent verification gate, and the final document is validated by 12
explicit tests before it is marked APPROVED.

---

## 2. How to run it

Type any of these to start the full pipeline (defined in `../CLAUDE.md`):

| Trigger | Where defined |
|---|---|
| `/kt` | `.claude/commands/kt.md` |
| `/start-kt` | `.claude/commands/start-kt.md` |
| `Start` / `Generate KT` | `../CLAUDE.md` → Trigger section |

On trigger, the orchestrator prints a start banner, classifies the repo size, and runs
Phases 0 → 4 without further input. It keeps the user informed at every phase boundary and
retry.

---

## 3. Architecture at a glance

```
                       ┌──────────────────────────────────────────┐
   /kt  ──────────────▶│   Orchestrator  (../CLAUDE.md)            │
                       │   "coordinate skills, never read code"     │
                       └───────┬───────────┬───────────┬──────────┘
                               │            │           │
   Phase 0   graphify ─────────┘            │           │   builds the graph
   Phase 1   cartographer ──────────────────┘           │   maps features (parallel subagents)
   Phase 2   acquire-codebase-knowledge ─────────────────┘   writes 7 structured docs
   Phase 3   kt-writer                                       assembles 14-section KT doc
   Phase 4   kt-validator                                    12 tests, loops back to kt-writer

   After Phases 0/1/2:  doublecheck  (run INSIDE a subagent)  →  PASS / FAIL / CONDITIONAL
```

- **Orchestrator** = `../CLAUDE.md`. It only *coordinates*; it does not analyze code itself.
- **Skills** = the six capability modules under `.claude/skills/`. Each is invoked in strict order.
- **Validation gates** = the `doublecheck` skill run after each build phase, plus `kt-validator` at the end.

---

## 4. The pipeline — phase by phase

Each phase has a **retry loop (×3)** and, where applicable, a **validation gate**. The pipeline
**never stops** — if something fails after 3 rounds it is flagged INCOMPLETE / CONDITIONALLY
APPROVED and the pipeline moves on (always make forward progress).

### Phase 0 — Build the knowledge graph (`graphify`)
- Runs `graphify` on the project (code-only, per `.graphifyignore`).
- Re-runs use `graphify <path> --update` so only changed files are re-extracted (cache re-runs).
- **Produces:** `graphify-out/graph.json`, `graphify-out/GRAPH_REPORT.md`, `graphify-out/graph.html`.
- **Gate:** `doublecheck` validates the graph against source → `graphify-out/DOUBLECHECK_GRAPHIFY.md`.
- This is also where **repo-size triage** happens (SMALL / MEDIUM / LARGE) — see §6.

### Phase 1 — Map the codebase (`cartographer`)
- **First reads the graph** (`GRAPH_REPORT.md`) and reuses graphify's **communities** as the
  groups for its parallel subagents (no guessing from controller names).
- Subagents read method bodies to extract complete business-logic flows.
- **Produces:** `docs/CODEBASE_MAP.md`.
- **Gate:** `doublecheck` → `docs/DOUBLECHECK_CARTOGRAPHER.md`.

### Phase 2 — Acquire codebase knowledge (`acquire-codebase-knowledge`)
- Seeds **seven documents** from the graph + the Phase 1 map; opens source only to fill gaps.
- **Produces (in `docs/codebase/`):** `STACK.md`, `STRUCTURE.md`, `ARCHITECTURE.md`,
  `CONVENTIONS.md`, `INTEGRATIONS.md`, `TESTING.md`, `CONCERNS.md`.
- **Gate:** `doublecheck` → `docs/codebase/DOUBLECHECK_ACQUIRE.md`.

### Phase 3 — Write the KT document (`kt-writer`)
- **Anchors on** the graph + Phase 1 + Phase 2 outputs; does **not** re-read the codebase.
- **Produces:** `docs/KT_DOCUMENT.md` — 14 sections (overview, stack, structure, architecture,
  key classes, API endpoints, auth/security, database, configuration, how-to-run, business-logic
  flows, architecture decisions, new-dev checklist, flagged items).

### Phase 4 — Validate and fix loop (`kt-validator`)
- Runs **12 tests** against real source (class/package/method existence, endpoint paths + HTTP
  methods, entity/table/field checks, dependency + version checks, and **business-logic
  completeness + accuracy**).
- **Outer loop ×3:** failures go back to `kt-writer`, which fixes *only* the flagged items, then
  validation re-runs.
- **Produces:** `VALIDATION_REPORT.md` (project root) with `APPROVED` or `CONDITIONALLY APPROVED`.

---

## 5. The skills

All live under `.claude/skills/<name>/SKILL.md` (plus optional `references/`, `scripts/`, `assets/`).

| Skill | Role in the pipeline | Key output |
|---|---|---|
| **graphify** | Deterministic AST + semantic graph; community detection; god-node analysis; `query`/`path`/`explain` tools | `graphify-out/` |
| **cartographer** | Maps the codebase via parallel subagents grouped by graph communities; extracts feature flows | `docs/CODEBASE_MAP.md` |
| **acquire-codebase-knowledge** | Produces the 7 structured knowledge docs, seeded from the graph + map | `docs/codebase/*.md` |
| **kt-writer** | Assembles the final 14-section KT document from all prior outputs | `docs/KT_DOCUMENT.md` |
| **kt-validator** | 12-test anti-hallucination validation; loops back to kt-writer | `VALIDATION_REPORT.md` |
| **doublecheck** | Per-phase verification gate (Codebase Artifact Mode); emits `RESULT: PASS/FAIL/CONDITIONALLY APPROVED` | `DOUBLECHECK_*.md` |

> `graphify` and `doublecheck` are general-purpose; the other four are KT-specific. `doublecheck`
> has a special **Codebase Artifact Mode** that validates generated docs against *source* (not the web).

---

## 6. Repo-size triage (decided once, at Phase 0)

The orchestrator classifies the repo and applies matching rules for the rest of the run:

| Mode | Size | Behavior |
|---|---|---|
| **SMALL** | < ~50 source files (fits one context) | graphify optional; full per-phase doublecheck |
| **MEDIUM** | ~50–500 source files | full pipeline, full per-phase doublecheck |
| **LARGE** | > ~500 source files | graph-as-index, `--update` caching, community-bounded subagents, **sampled** per-phase gates + one final full validation |

It announces: `Repo-size mode: SMALL | MEDIUM | LARGE — applying the matching rules.`

---

## 7. Token-efficiency rules (why it stays cheap)

The pipeline is engineered to minimize tokens while maximizing coverage:

1. **Graph-first / reuse, never re-derive.** Every phase builds on the previous one's output
   instead of re-reading the codebase. The graph is the single source of truth for structure.
2. **Graph-as-index, never wholesale.** Read the bounded `GRAPH_REPORT.md`; never load the large
   `graph.json` into context — use `graphify query/path/explain` for targeted lookups.
3. **Targeted source reads only.** When source is needed, open the exact file/line the graph points
   to; never scan a whole package or `src/` tree.
4. **`--update` caching.** Re-running graphify on an unchanged/lightly-changed repo re-extracts only
   changed files.
5. **Community-bounded subagents.** Each fan-out subagent is capped to one community's files.
6. **VALIDATE IN SUBAGENTS — never read source inline.** Every `doublecheck` gate runs **inside a
   subagent** that reads source, writes its report, and returns **only the RESULT verdict + flagged
   items**. This keeps validated source files out of the main transcript so they are not re-billed as
   cache-read on every later phase's turn — the single largest avoidable cost.

> **Why cache-read dominates the bill:** the API re-sends the whole conversation prefix every turn;
> the unchanged part is billed at the cheap cache-read rate but still scales with *context size ×
> number of turns*. Rules 2–6 exist to keep that prefix small.

---

## 8. `.graphifyignore` (corpus hygiene)

At the project root, `.graphifyignore` keeps graphify's corpus **code-only** so it stays on the
free AST path and never fires the LLM semantic extractor for non-source files. It excludes:
`.claude/`, `docs/`, `graphify-out/`, `*.md`, `target/`, the generated reports, and local tooling
scripts. This also prevents pipeline-generated docs from being graphed (which would be circular).

---

## 9. Output files map

```
Project root
  CLAUDE.md                              ← the orchestrator contract (the "brain")
  .graphifyignore                        ← graphify corpus exclusions
  VALIDATION_REPORT.md                   ← Phase 4 result (APPROVED / CONDITIONAL)
  TOKEN_REPORT.md                        ← optional: per-phase token usage (see §11)

graphify-out/
  graph.json                             ← raw knowledge graph
  GRAPH_REPORT.md                        ← bounded graph index (communities, god nodes)
  graph.html                             ← interactive graph viewer
  DOUBLECHECK_GRAPHIFY.md                ← Phase 0 validation

docs/
  CODEBASE_MAP.md                        ← Phase 1 feature flows + module guide
  DOUBLECHECK_CARTOGRAPHER.md            ← Phase 1 validation
  KT_DOCUMENT.md                         ← ★ final 14-section KT deliverable
  codebase/
    STACK.md  STRUCTURE.md  ARCHITECTURE.md  CONVENTIONS.md
    INTEGRATIONS.md  TESTING.md  CONCERNS.md     ← Phase 2 (7 docs)
    DOUBLECHECK_ACQUIRE.md                       ← Phase 2 validation

.claude/
  README.md                              ← this file
  commands/  kt.md  start-kt.md          ← slash-command triggers
  settings.local.json                    ← local harness settings
  skills/<six skills>/SKILL.md           ← skill definitions
```

---

## 10. Anti-hallucination guarantees

The whole design exists to stop the agent inventing facts:

- **Two independent verification layers:** a `doublecheck` gate after each build phase, and a
  final 12-test `kt-validator` pass. Both assert against **real `file:line` source**, not against
  the upstream artifact (which would be circular).
- **Source is ground truth.** If the graph or any doc contradicts the code, the code wins.
- **Honest flagging.** Anything that cannot be verified is written into **Section 14** of the KT
  document and into the validation reports, marked for human review — never silently dropped or
  guessed.
- **Faithful identifiers.** Real source names (including typos like `VehicleServiveConstants`,
  `updateVechicleDetails`, the capitalized `Repository` package) are preserved verbatim, because a
  new developer must use the *actual* identifiers.

---

## 11. `kt-token-report.py` (cost/usage utility)

A read-only helper at the project root that parses the Claude Code session transcript and reports
how many tokens each KT phase consumed, in pipeline order.

```bash
python kt-token-report.py            # report the MOST RECENT KT run
python kt-token-report.py --list     # list all detected KT runs (most-recent first)
python kt-token-report.py --session <id>   # report a specific run
python kt-token-report.py --gap 30   # minutes of idle that ends a run
```

It writes `TOKEN_REPORT.md` with a per-phase table (input / output / cache-creation / cache-read /
total). It reads only the transcript and `graphify-out/cost.json`; it never touches source.

---

## 12. Absolute rules (cannot be overridden)

From `../CLAUDE.md`:

- **Do NOT** edit, modify, or delete any existing Java source or configuration files.
- **Do NOT** create new Java/code files, or modify `pom.xml` / `application.properties`.
- Read all source files in **READ-ONLY** mode.
- Only **CREATE** new report files, the `graphify-out/` outputs, and `KT_DOCUMENT.md`.

---

## 13. Where to look first (for maintainers)

| You want to… | Open |
|---|---|
| Understand the orchestration logic | `../CLAUDE.md` |
| Change how a phase behaves | the relevant `.claude/skills/<name>/SKILL.md` |
| Change what's excluded from the graph | `../.graphifyignore` |
| Change run triggers | `.claude/commands/kt.md`, `start-kt.md` |
| See the final deliverable | `docs/KT_DOCUMENT.md` |
| Audit accuracy | `VALIDATION_REPORT.md` + the `DOUBLECHECK_*.md` files |
| Inspect token cost | `python kt-token-report.py` → `TOKEN_REPORT.md` |

---

_This agent is project-local. The orchestrator coordinates; the skills do the work; every build
phase is independently verified against source before the next phase reuses it._
