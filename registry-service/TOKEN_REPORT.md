# KT Pipeline — Token Usage (Chronological)

_Scope: a **single** KT run. This is not a total across all your runs — 7 KT run(s) exist; see `--list`._

- **Session:** `b1176569-a8f7-415d-ba86-e88f39f042f4`
- **Run window:** 08:22:20 → 09:00:44 (local time)
- **Transcript:** `C:\Users\2494517\.claude\projects\c--Workspace-demo-registry-service\b1176569-a8f7-415d-ba86-e88f39f042f4.jsonl`

Every token component is reported **separately**: fresh **input**, generated **output**, **cache-creation** (first-time writes to cache) and **cache-read** (cheap cached re-reads). The **Total** column sums all four for each phase; the bottom row totals each column.

| # | Phase (skill) | Start | End | Input | Output | Cache-create | Cache-read | Total |
|---|---------------|-------|-----|------:|-------:|-------------:|-----------:|------:|
| — | orchestration (pre-graphify) | 08:22:17 | 08:22:20 | 7,652 | 950 | 22,770 | 42,256 | 73,628 |
| 1 | graphify | 08:22:20 | 08:31:53 | 4,930 | 37,184 | 131,435 | 3,912,238 | 4,085,787 |
| 2 | doublecheck | 08:31:53 | 08:34:05 | 786 | 24,833 | 77,313 | 2,473,807 | 2,576,739 |
| 3 | cartographer | 08:34:05 | 08:40:25 | 48,926 | 66,437 | 378,438 | 3,415,634 | 3,909,435 |
| 4 | doublecheck | 08:40:25 | 08:42:07 | 408 | 22,179 | 74,191 | 3,567,792 | 3,664,570 |
| 5 | acquire-codebase-knowledge | 08:42:07 | 08:46:25 | 806 | 31,952 | 77,359 | 7,175,403 | 7,285,520 |
| 6 | doublecheck | 08:46:25 | 08:48:10 | 140 | 15,207 | 35,171 | 1,943,289 | 1,993,807 |
| 7 | kt-writer | 08:48:10 | 08:52:23 | 258 | 37,572 | 41,452 | 1,653,772 | 1,733,054 |
| 8 | kt-validator | 08:52:23 | 09:00:45 | 310 | 20,515 | 60,204 | 8,921,203 | 9,002,232 |
| | **GRAND TOTAL** | | | **64,216** | **256,829** | **898,333** | **33,105,394** | **34,324,772** |

> Notes
> - Phase boundaries are the `Skill` tool calls in the session transcript; tokens are bucketed by timestamp, so parallel subagent tokens are included in the phase that spawned them.
> - A run ends after 30 min of idle; later activity in a resumed session is excluded.
