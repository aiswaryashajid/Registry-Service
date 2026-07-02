# Registry Service - Graphify Setup

> **TL;DR:** Run `graphify .` once, then enjoy 40-60% token savings on all future Jira ticket analyses

## Setup (5 minutes)

### Step 1: Install Graphify
```powershell
# If you have uv installed (recommended):
uv tool install graphifyy

# Otherwise use pipx:
pipx install graphifyy

# Verify:
graphify --version
```

### Step 2: Generate the Knowledge Graph
```powershell
# From the registry-service directory
cd "C:\Users\2510172\Documents\Projects\registry-service 7"

# Generate the graph (takes ~2 minutes)
graphify .
```

### Step 3: Explore the Graph
```powershell
# Open the interactive visualization
start graphify-out/graph.html

# Or read the architecture report
Get-Content graphify-out/GRAPH_REPORT.md | less
```

## What You Get

```
graphify-out/
├── graph.html       ← Open in browser for interactive visualization
├── GRAPH_REPORT.md  ← Architectural insights and class relationships
└── graph.json       ← Cached knowledge graph (automatically reused by Jira analyser)
```

## Using with Jira Analyser

When you run: `run jira-ticket-analyser PROJ-123`

The analyser will:
1. ✓ Detect the cached graph in `graphify-out/graph.json`
2. ✓ Query it for relevant classes (instant, no file re-reading)
3. ✓ Trace call chains through the graph
4. ✓ Identify DTOs, entities, and relationships
5. ✓ **Use ~40-60% fewer tokens** compared to reading all files

## Example: Analyzing a Vehicle API Bug

**Without Graphify:**
- Re-reads 50+ Java files → 60k tokens
- Greps for keywords → slow
- Rebuilds understanding each time

**With Graphify:**
- Loads cached graph.json → 20k tokens
- Queries for VehicleController, VehicleService, DTOs → instant
- Reuses relationships → context preserved

**Result: 67% token savings! 💰**

## When to Regenerate

Regenerate the graph after:
- Adding new controllers or services
- Changing class dependencies
- Major refactoring or feature addition
- Merging large PRs

```powershell
# Quick regenerate
cd "C:\Users\2510172\Documents\Projects\registry-service 7"
graphify .
```

## Inspect the Graph

### See all Controllers
```powershell
$graph = Get-Content graphify-out/graph.json | ConvertFrom-Json
$graph.nodes | Where-Object { $_.tags -contains "RestController" } | Select-Object name
```

### Find Vehicle-related Classes
```powershell
$graph.nodes | Where-Object { $_.name -like "*Vehicle*" } | Select-Object name, tags
```

### Trace Service Dependencies
```powershell
$graph.edges | Where-Object { $_.from -eq "VehicleService" }
```

## File Reference

| File | Purpose | Update |
|------|---------|--------|
| `graph.html` | Interactive visualization | Regen after code changes |
| `graph.json` | Cached knowledge graph | Regen after code changes |
| `GRAPH_REPORT.md` | Architecture insights | Regen to see updates |

## Optional: Version Control

If sharing the project with others:

```powershell
# Commit the graph
git add graphify-out/graph.json
git commit -m "chore: update knowledge graph"

# (Exclude HTML as it's generated)
# echo "graphify-out/graph.html" >> .gitignore
```

## Troubleshooting

**"graphify command not found"**
```powershell
uv tool update-shell
# Restart PowerShell and try again
```

**"graph.json not found"**
```powershell
# Generate it
graphify .
```

**"Graph seems old"**
```powershell
# Regenerate
graphify .
```

## Next Steps

1. ✅ Run `graphify .` in this directory
2. ✅ Open `graphify-out/graph.html` to explore
3. ✅ Use Jira analyser on a ticket — it'll automatically use the cached graph
4. ✅ Regenerate graph after major code changes

---

**Questions?** See [GRAPHIFY_SETUP.md](../Jira%20Ticket%20Analyser%20Agent/GRAPHIFY_SETUP.md) in the Jira Analyser directory for detailed documentation.
