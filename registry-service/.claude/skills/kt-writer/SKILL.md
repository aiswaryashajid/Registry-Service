---
name: kt-writer
description: Generates a complete professional Knowledge Transfer (KT) document in Markdown format for a Spring Boot Maven project. Use after graphify, cartographer, and acquire-codebase-knowledge have completed their analysis. Reads the graphify knowledge graph (GRAPH_REPORT.md, graph.json), CODEBASE_MAP.md, and all 7 docs from docs/codebase/ and combines everything into a 14 section KT document saved to the project docs folder. Designed specifically for large Spring Boot projects being handed over to a new development team.
---

# KT Writer Skill

Generate a complete professional KT document by reading all
analysis files produced by previous agents.
Only write facts found in the input files.
Never invent anything not already in the reports.

## Pre-condition Check and Loop

Before writing anything check if all input files exist.

Required from graphify:
- graphify-out/GRAPH_REPORT.md   (the bounded index — read this)
- graphify-out/graph.json        (must EXIST, but do NOT load it wholesale;
  query it with `graphify query/path/explain` only for a specific lookup)

Required from cartographer:
- docs/CODEBASE_MAP.md

Required from acquire-codebase-knowledge:
- docs/codebase/STACK.md
- docs/codebase/STRUCTURE.md
- docs/codebase/ARCHITECTURE.md
- docs/codebase/CONVENTIONS.md
- docs/codebase/INTEGRATIONS.md
- docs/codebase/TESTING.md
- docs/codebase/CONCERNS.md

If the graphify files are missing, do NOT block — they enrich the
document but are not strictly required. Write the document from the
cartographer and acquire-codebase-knowledge files and note the gap
in Section 14.

## Missing File Loop

If any file is missing do NOT stop.
Follow this loop instead:

### Round 1
- Report exactly which files are missing
- Ask acquire-codebase-knowledge to regenerate
  only the missing files
- Wait for regeneration to complete
- Check all files again

### Round 2
- If still missing after round 1
- Ask acquire-codebase-knowledge to retry
  with focus only on the missing sections
- Wait for regeneration to complete
- Check all files again

### Round 3
- If still missing after round 2
- Ask acquire-codebase-knowledge for final retry
- Wait for regeneration to complete
- Check all files again

### After 3 Rounds — Always Move Forward
If any file still missing after 3 rounds:
- Do NOT stop the pipeline
- Skip that section in KT document
- Write in that section:
  "Section not available. acquire-codebase-knowledge
  could not generate this data after 3 attempts.
  Needs human review — added to Section 14."
- Add the missing section to Section 14 Flagged Items
- Continue writing all remaining sections with available files
- Always make forward progress — never stop completely

## Exit Criteria — Two Valid Ways to Proceed

EXIT 1 — Clean:
All 7 files exist and CODEBASE_MAP.md exists.
Proceed to write full KT document.

EXIT 2 — After 3 retries:
Some files still missing after 3 rounds.
Proceed to write KT document with available files.
Flag missing sections in Section 14.

## Input Files — Read ALL Available Files First

### Priority 1 — Most Important
- docs/CODEBASE_MAP.md
  This has the complete feature flows, architecture diagram,
  module guide, navigation guide and gotchas.
  Use this as the primary source for business logic sections.

### Priority 1b — Structural Graph (graphify)
- graphify-out/GRAPH_REPORT.md
  Use to enrich and cross-check, NOT as the primary narrative:
  - God Nodes      → confirm which classes matter most (Section 5)
  - Communities    → cross-check structure / grouping (Sections 3, 5)
  - Hyperedges     → confirm end-to-end flows (Section 11)
  - Surprising Connections → candidate items for Section 14
- graphify-out/graph.json
  Do NOT load this file wholesale — it is large. To verify a single class/edge
  exists when cross-checking, query it instead of dumping it:
    - `graphify query "<question>"`
    - `graphify path "<A>" "<B>"`
    - `graphify explain "<node>"`
  Treat the graph as evidence, never as a substitute for the
  business rules in CODEBASE_MAP.md.

### Priority 2 — Structured Analysis
- docs/codebase/STACK.md       → tech stack and dependencies
- docs/codebase/STRUCTURE.md   → folder layout and entry points
- docs/codebase/ARCHITECTURE.md → layers, patterns, data flow
- docs/codebase/CONVENTIONS.md → naming, formatting, patterns
- docs/codebase/INTEGRATIONS.md → external APIs, database, auth
- docs/codebase/TESTING.md     → test frameworks and strategy
- docs/codebase/CONCERNS.md    → tech debt, risks, gotchas

## Smart Writing Strategy

Use from docs/CODEBASE_MAP.md directly:
- System Overview Mermaid diagram → Section 4 Architecture
- Feature Flows → Section 11 Business Logic Summary
- Module Guide → Section 5 Key Classes
- Navigation Guide → Section 13 New Developer Checklist
- Gotchas → Section 14 Flagged Items

Use from docs/codebase/ files directly:
- STACK.md → Section 2 Tech Stack
- STRUCTURE.md → Section 3 Project Structure
- ARCHITECTURE.md → Section 4 Architecture enhance
- INTEGRATIONS.md → Section 7 Auth and Security + Section 8 Database
- CONVENTIONS.md → Section 12 Architecture Decisions
- CONCERNS.md → Section 14 Flagged Items

Use from graphify-out/ files to enrich (cross-check only):
- God Nodes → Section 5 Key Classes (rank the most central classes)
- Communities → Sections 3 and 5 (confirm grouping)
- Hyperedges → Section 11 Business Logic (confirm the flow skeleton)
- Surprising Connections → Section 14 Flagged Items
- graph.html → Section 4 Architecture (link it as the interactive map)

Generate by combining all sources:
- Section 1 Project Overview
- Section 6 API Endpoints from CODEBASE_MAP.md feature flows
- Section 9 Configuration from STACK.md and INTEGRATIONS.md
- Section 10 How to Run from STRUCTURE.md entry points

## Output Location
Save the final document to the project docs folder:
docs/KT_DOCUMENT.md

This path is relative to the project root (the registry-service
folder), alongside the other output files like docs/CODEBASE_MAP.md.
Create the docs folder if it does not already exist.

## Section Writing Loop

For each of the 14 sections follow this loop:

Step 1 — Write the section from available input files
Step 2 — Reread the section
Step 3 — Check every class name and endpoint exists in input files
Step 4 — Remove anything not traceable to an input file
Step 5 — Ask: would a new developer understand this?
Step 6 — If not clear enough rewrite and repeat from Step 2
Step 7 — Maximum 3 self review passes per section
Step 8 — Move to next section

## Complete Document Structure

Write exactly these 14 sections in this exact order.
Do not skip any section.
If data is not available for a section write:
"Not found in codebase analysis — added to Section 14."

---

# {Project Name} — Knowledge Transfer Document

> Generated by KT Agent on {current date}
> This document was auto-generated from actual source code analysis.
> Every claim is traceable to source files.

---

## 🎯 1. Project Overview

Write 4 to 6 sentences covering:
- What this project does in simple terms
- Who uses it
- What business problem it solves
- How it fits into the larger system if evident from code
- Technology approach used

Source: Combine README if found plus STRUCTURE.md project intent
plus CODEBASE_MAP.md system overview section.

---

## 🛠️ 2. Tech Stack

Use STACK.md directly for this section.

| Technology | Version | Purpose |
|---|---|---|

Include:
- Java version
- Spring Boot version
- Database and ORM
- All Spring starters and what they enable
- Any Lombok MapStruct or other code gen tools
- Build tool and version

---

## 📁 3. Project Structure

Use STRUCTURE.md directly for this section.

Show the complete package layout with purpose of each package:

src/main/java/com/vehicle/registry_service/
  controller/      → REST API layer
  service/         → Business logic layer
  Repository/      → Data access layer
  entity/          → Database entities
  dto/             → Data transfer objects
  client/          → External API clients
  configuration/   → Spring configuration classes
  constants/       → Constant values and enums
  exception/       → Custom exception classes
  filters/         → Request and response filters

---

## 🏗️ 4. Architecture

REQUIRED — this section MUST include a Mermaid diagram.
Always render the architecture as a Mermaid diagram (do not describe the
architecture in prose only). Reuse the Mermaid diagram from
CODEBASE_MAP.md System Overview directly; if CODEBASE_MAP.md has no
Mermaid diagram, build one from the ARCHITECTURE.md layer description and
the graphify graph. Then enhance with the layer description from
ARCHITECTURE.md.

Mermaid diagram rules:
- Wrap the diagram in a ```mermaid fenced code block.
- Use `flowchart TD` (or `graph TD`) showing the full layer flow:
  HTTP Request → Filters → Controllers → Services → Repositories → Database.
- Every node must be a real class/component found in CODEBASE_MAP.md or
  graph.json — never invent a node.
- Branch out to external API calls (client/ package), the security layer
  (configuration/ + filters/), and the exception-handling flow
  (exception/ package) when they exist in the inputs.
- If no Mermaid diagram can be produced from the inputs, write a plain-text
  layer flow instead AND flag the missing diagram in Section 14.

Show the complete Spring Boot layer flow:

HTTP Request
  → Filters
  → Controllers
  → Services
  → Repositories
  → Database

Also document:
- Any external API calls from client/ package
- Security layer if found in configuration/
- Exception handling flow from exception/ package

---

## 🔑 5. Key Classes

Use Module Guide from CODEBASE_MAP.md for this section.

| Class | Package | Layer | Purpose |
|---|---|---|---|

Group by layer:
- Controllers
- Services
- Repositories
- Entities
- Clients
- Configuration
- Filters
- Exceptions

---

## 🌐 6. API Endpoints

Compile complete endpoint table from CODEBASE_MAP.md Feature Flows.
List EVERY endpoint found — do not skip any.

| Method | Path | Controller | Handler Method | Purpose | Possible Error Codes |
|---|---|---|---|---|---|

For the **Possible Error Codes** column, list the HTTP status codes
each endpoint can realistically return on failure, with a short reason.
Derive these ONLY from actual code — never invent them. Look for:
- Exceptions thrown in the handler/service and their mapped status
  (e.g. `@ResponseStatus`, `@ExceptionHandler`, `ResponseStatusException`,
  `ControllerAdvice` classes from the Exceptions community)
- Validation annotations (`@Valid`, `@NotNull`, etc.) → `400 Bad Request`
- Missing/duplicate resource logic → `404 Not Found` / `409 Conflict`
- Security constraints on the endpoint → `401 Unauthorized` / `403 Forbidden`
- Any explicit `ResponseEntity` status codes returned in the handler

If an endpoint's failure paths cannot be verified from the source,
write `Not determinable from code` rather than guessing.

After the table, add an **Error Handling Summary** describing the
global exception handlers (`@ControllerAdvice` / `@RestControllerAdvice`)
found and the standard error response shape they produce, if any.

| Exception | Mapped HTTP Status | Handled By | Meaning |
|---|---|---|---|

---

## 🔐 7. Authentication and Security

Use INTEGRATIONS.md auth and security section directly.

Document:
- Authentication mechanism found
- How authentication is implemented
- Which endpoints are protected
- Which endpoints are public
- Security configuration class if found
- Any security related filters

If no security configuration found write:
"No security configuration found in codebase analysis.
Needs human verification — added to Section 14."

---

## 🗄️ 8. Database Design

Use INTEGRATIONS.md database section and CODEBASE_MAP.md module guide.

### Database Overview
- Database type: from INTEGRATIONS.md
- ORM used: from STACK.md
- Connection: from INTEGRATIONS.md mask password

### Entities
For each entity class found document:

#### {EntityName}
- Database Table: from @Table annotation
- Fields:
| Java Field | DB Column | Type | Constraints |
|---|---|---|---|
- Relationships:
| Annotation | Target Entity | Cascade | Fetch Type |
|---|---|---|---|

---

## ⚙️ 9. Configuration

Use STACK.md environment config section and INTEGRATIONS.md.

| Property | Value | Purpose |
|---|---|---|

Include:
- Server port
- Database connection properties
- Any profile specific configs
- Any feature flags
- Any timeout or retry configs

---

## 🚀 10. How to Run

Use STRUCTURE.md entry points section and STACK.md.

### Prerequisites
- Java version from pom.xml
- Maven
- Database type and version from INTEGRATIONS.md

### Setup Steps
1. Clone the repository
2. Configure src/main/resources/application.properties
   - Set spring.datasource.url to your local database
   - Set spring.datasource.username and password
3. Run: mvn clean install
4. Verify: BUILD SUCCESS message appears
5. Run: mvn spring-boot:run
6. Verify: Started application on correct port
7. Test: call the health endpoint if actuator is present

---

## 💡 11. Business Logic Summary

This is the most important section for a new developer.
Use Feature Flows from CODEBASE_MAP.md directly.
Do not summarize — show the full detail of every flow.

For each feature document the COMPLETE flow:

### Feature: {Feature Name}

#### {HTTP Method} {Path} — {Purpose}

First, a per-feature graph as a Mermaid flowchart.
Build it PRIMARILY from the CODEBASE_MAP.md feature flow (which already has the
filter → controller → service → repository → database chain). Use the graphify
graph only to confirm or fill a specific edge — via `graphify path "<A>" "<B>"`
or `graphify explain "<node>"`, NOT by loading graph.json wholesale. Draw only
the nodes and edges for THIS feature (its community plus the endpoint's call
chain) that actually exist in those sources, and branch out to any exception
thrown on a business-rule failure.

```mermaid
flowchart LR
  {Filter} --> {Controller}
  {Controller} --> {Service}
  {Service} -->|{business rule / condition}| {Exception}
  {Service} --> {Repository}
  {Repository} --> {Database}
```

Then the same flow in detail:

{Controller}.{method}()
  → {Service}.{method}()
    → Business rule: {condition found in service}
    → Business rule: {condition found in service}
    → {Repository}.{query}()
      → Database: {what query runs}
    → {transformation applied}
  → Returns {type}

Repeat for EVERY endpoint found in CODEBASE_MAP.md —
each feature gets BOTH its Mermaid graph and its detailed flow.

### Feature Graph Rules
- One Mermaid flowchart per feature, placed above its detailed flow.
- Every node in the diagram must be a real class/component found in
  CODEBASE_MAP.md or confirmed via a `graphify query/path/explain` lookup —
  never invent a node.
- Keep node names = actual class names (no spaces; use the class name).
- Label edges with the real business rule/condition when one applies.
- CODEBASE_MAP.md is the primary source for the chain; the graph is only a
  cross-check. If the map lacks a flow, confirm it with a graphify query and
  note any gap in Section 14.

### Business Logic Loop
After writing each feature flow:
- Reread the flow
- Check every method name exists in CODEBASE_MAP.md
- Check every business rule came from actual service code
- If any item cannot be traced remove it
- Maximum 3 review passes per feature flow

---

## 🏛️ 12. Architecture Decisions

Use CONVENTIONS.md directly for this section.
Add any patterns found in ARCHITECTURE.md.

Document:
- Why Spring Boot layered architecture was used
- DTO pattern usage
- Exception handling approach
- Transaction management strategy
- Naming conventions used
- Any notable design patterns found
- Package organization rationale

If reason for decision is not in code write:
"[ASK TEAM] — reason not evident from code analysis"

---

## ✅ 13. New Developer Checklist

Use Navigation Guide from CODEBASE_MAP.md directly.

### Setup Checklist
- [ ] Clone the repository
- [ ] Install Java version from pom.xml
- [ ] Install Maven
- [ ] Set up local database from INTEGRATIONS.md
- [ ] Configure application.properties with local values
- [ ] Run mvn clean install and verify BUILD SUCCESS
- [ ] Start application and verify correct port
- [ ] Test a GET endpoint to confirm working

### Understanding Checklist
- [ ] Read this entire KT document
- [ ] Open docs/CODEBASE_MAP.md for full architecture map
- [ ] Trace one complete feature flow in the actual source code
- [ ] Understand the database schema from Section 8
- [ ] Review all API endpoints from Section 6
- [ ] Check security configuration from Section 7

### Adding New Features
From Navigation Guide in CODEBASE_MAP.md:
- [ ] To add a new endpoint: {files to touch}
- [ ] To add a new entity: {files to touch}
- [ ] To add a new service: {files to touch}
- [ ] To add an external API client: {files to touch}

---

## ⚠️ 14. Flagged Items — Needs Human Review

Combine from all sources:

From CODEBASE_MAP.md Gotchas section:
[List all gotchas found by cartographer]

From docs/codebase/CONCERNS.md:
[List all tech debt risks and concerns]

From acquire-codebase-knowledge TODO markers:
[List everything marked as TODO in the 7 docs]

From acquire-codebase-knowledge ASK USER markers:
[List everything marked as ASK USER in the 7 docs]

From missing file loop:
[List any sections that could not be generated]

From this document:
[List any sections where data was insufficient]

| Item | Source | Why Flagged | Action Needed |
|---|---|---|---|

---

## Writing Rules
- Simple clear English — a junior developer must understand
- Every class method and endpoint must come from input files
- If unsure about anything add it to Section 14
- Use tables for all structured data
- Use code blocks for all code snippets
- Never invent information not found in input files
- Think: would a developer who never saw this code understand it?

## Anti-Hallucination Rule
Only write facts from the input files.
Never invent class names, method names, or endpoints.
Never guess at business logic not found in code.
If a section has no data write: Not found in codebase analysis.