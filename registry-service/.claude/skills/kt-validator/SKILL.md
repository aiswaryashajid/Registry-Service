---
name: kt-validator
description: Validates a generated KT document against actual Spring Boot source code to prevent hallucination and ensure accuracy. Runs 12 specific test criteria including business logic validation. Use after kt-writer has generated KT_DOCUMENT.md in the project docs folder. 100% pass rate required before document is approved. Sends failures back to kt-writer for fixes with maximum 3 retry rounds. Never stops the pipeline — always makes forward progress.
---

# KT Validator Skill

Cross check every single claim in KT_DOCUMENT.md against
the actual source code files in src/main/java.
Trust the code — not the document.
This is the anti-hallucination agent.

## Pre-condition Check and Loop

Before validating check KT_DOCUMENT.md exists at:
docs/KT_DOCUMENT.md

If file does not exist:

Round 1: Ask kt-writer to generate the document and wait
Round 2: If still missing ask kt-writer to retry
Round 3: If still missing ask kt-writer for final attempt

After 3 rounds if still missing:
- Report the issue clearly
- Stop the pipeline — cannot validate what does not exist
- This is the only valid reason to stop completely

## Read These Files Before Running Any Test

1. docs/KT_DOCUMENT.md
   The document to validate — read every section

2. src/main/java/com/vehicle/registry_service/ — all Java files
   The ground truth — what the code actually says

3. pom.xml
   For dependency and version validation

4. src/main/resources/application.properties or application.yml
   For configuration validation

5. docs/CODEBASE_MAP.md
   For cross reference with cartographer analysis

6. graphify-out/GRAPH_REPORT.md (read this as the index) and graphify-out/graph.json
   Use ONLY as an index to scope your reads — see the rule below. Do NOT load
   graph.json wholesale; query it (`graphify query/path/explain`) for a specific
   location when the report doesn't already point you to the file.

## Graph-as-Index, Source-as-Truth (Token Efficiency Rule)

The graphify outputs exist to make validation CHEAPER, not to replace it.

- USE the graph as a MAP: read graph.json / GRAPH_REPORT.md first to find
  WHICH file (and ideally which line range) each class, method, endpoint,
  entity, dependency, or god node lives in. This lets you open the exact
  slice you need instead of scanning all of src/main/java.
- ASSERT against SOURCE only: every PASS/FAIL verdict in every one of the
  12 tests must be proven by reading the actual .java file, pom.xml, or
  application.properties — never by trusting what the graph or the KT
  document says. The graph tells you where to look; the code decides the result.
- WHY: kt-writer generated KT_DOCUMENT.md FROM the graph. If you validate the
  document against the same graph, you only confirm the document copied the
  graph faithfully — a hallucination already baked into the graph would pass.
  Independence is the entire point of this anti-hallucination agent, so the
  source of truth must stay the raw code, not any upstream artifact.
- If the graph is missing, incomplete, or contradicts the source, ignore it
  and fall back to reading source directly. Never let the graph override the code.

## Run All 12 Tests

Run every single test in order.
Never skip a test.
Report PASS or FAIL with evidence for each test.

---

### Test 1 — Class Existence Test

For every class name mentioned anywhere in KT_DOCUMENT.md:
- Search src/main/java for that exact .java filename
- Open the file and confirm the class name matches
- PASS: file found with exact matching class name
- FAIL: file not found → flag as hallucinated class name

Evidence required: exact file path where class was found or not found.

---

### Test 2 — Package Accuracy Test

For every class mentioned with a package path in KT_DOCUMENT.md:
- Open the actual .java file
- Read the package declaration at the very top of the file
- Compare package in document with package in file
- PASS: package in document matches package declaration in file
- FAIL: package is wrong → flag with correct package from file

Evidence required: actual package declaration line from the file.

---

### Test 3 — Method Existence Test

For every method name mentioned in KT_DOCUMENT.md:
- Open the class file that should contain this method
- Search for the exact method signature in the file
- PASS: method found in file
- FAIL: method not found → flag as hallucinated method name

Evidence required: the actual method signature found or confirmation not found.

---

### Test 4 — Endpoint Path Test

For every API endpoint listed in Section 6 of KT_DOCUMENT.md:
- Find the controller class for that endpoint
- Find the @GetMapping @PostMapping @PutMapping @DeleteMapping @PatchMapping annotation
- Read the exact path value from the annotation
- PASS: URL path in document matches annotation value exactly
- FAIL: path is different or annotation not found → flag it

Evidence required: exact annotation value from the controller file.

---

### Test 5 — HTTP Method Test

For every API endpoint listed in Section 6 of KT_DOCUMENT.md:
- Find the controller method handling this endpoint
- Check which annotation it uses
  @GetMapping = GET
  @PostMapping = POST
  @PutMapping = PUT
  @DeleteMapping = DELETE
  @PatchMapping = PATCH
- PASS: HTTP method in document matches annotation type
- FAIL: wrong HTTP method documented → flag with correct method

Evidence required: the actual annotation found on the method.

---

### Test 6 — Entity Existence Test

For every database entity mentioned in Section 8 of KT_DOCUMENT.md:
- Search src/main/java for a class with @Entity annotation
  matching the entity name
- PASS: entity class with @Entity annotation exists
- FAIL: entity not found → flag as hallucinated entity

Evidence required: file path where @Entity class was found or not found.

---

### Test 7 — Table Name Test

For every table name mentioned in Section 8 of KT_DOCUMENT.md:
- Find the @Entity class for this table
- Look for @Table(name="...") annotation
- If no @Table annotation the table name equals the class name in lowercase
- PASS: table name in document matches @Table annotation or class name rule
- FAIL: wrong table name → flag with correct table name from annotation

Evidence required: exact @Table annotation value or class name used.

---

### Test 8 — Field Existence Test

For every field mentioned in the database design tables in Section 8:
- Open the @Entity class
- Search for that exact field name as a Java field declaration
- PASS: field exists as a declared field in the entity class
- FAIL: field not found → flag as hallucinated field name

Evidence required: the actual field declaration line from the entity file.

---

### Test 9 — Dependency Accuracy Test

For every dependency mentioned in Section 2 Tech Stack of KT_DOCUMENT.md:
- Open pom.xml
- Search for that exact artifactId in the dependencies section
- PASS: dependency found in pom.xml
- FAIL: dependency not in pom.xml → flag as hallucinated dependency

Evidence required: the actual dependency entry from pom.xml or confirmation missing.

---

### Test 10 — Version Accuracy Test

For every version number mentioned in Section 2 Tech Stack of KT_DOCUMENT.md:
- Open pom.xml
- Find the dependency and read its version tag
- Check properties section for version variables
- PASS: version in document matches pom.xml exactly
- FAIL: wrong version → flag with correct version from pom.xml

Evidence required: exact version value found in pom.xml.

---

### Test 11 — Business Logic Completeness Test

For every service method documented in Section 11 Business Logic Summary:
- Open the actual @Service class file
- Read the complete method body line by line
- List every condition found:
  Every if statement and what it checks
  Every filter applied and the condition
  Every validation performed
  Every exception thrown and when
- Compare this list with what KT_DOCUMENT.md says about this method
- PASS: all key conditions are documented in KT document
- FAIL: important conditions are missing → list exactly what is missing

Example check:
Code has these conditions in getAllVehicles():
  filter(v -> v.getStatus() == ACTIVE)
  filter(v -> !v.isUnderMaintenance())
  filter(v -> v.getInsuranceExpiry().isAfter(LocalDate.now()))

KT document must mention all three conditions.
If any condition is missing → FAIL with list of missing conditions.

Evidence required: actual code conditions found vs what document states.

---

### Test 12 — Business Logic Accuracy Test

For every business rule stated in Section 11 of KT_DOCUMENT.md:
- Find the corresponding code in the actual @Service class
- Read the actual implementation
- Verify the rule correctly describes what the code does
- PASS: rule accurately and completely describes the code behaviour
- FAIL: rule is wrong misleading or incomplete → flag with what code actually does

Example check:
KT document says: Returns vehicles with ACTIVE status only
Code actually does: filters by ACTIVE status AND not under maintenance
                   AND insurance not expired
KT document is INCOMPLETE → FAIL

Evidence required: actual code behaviour vs what document claims.

---

## Business Logic Validation Limitation

Add this note to VALIDATION_REPORT.md:

Business logic validation covers code level rules only.
Conditions filters and validations visible in source code are checked.
The following require human review and cannot be validated automatically:
- Business intent — why decisions were made
- Domain specific knowledge not written in code
- Implicit team agreements and conventions
- Edge cases not yet handled in current code
These items should be in Section 14 of KT_DOCUMENT.md.

---

## Validation Loop

After running all 12 tests check the results.

### If All 12 Tests Pass
Mark document as APPROVED.
Save final VALIDATION_REPORT.md.
Proceed to completion.

### If Any Test Fails — Loop Back to kt-writer

Round 1:
- Create VALIDATION_REPORT.md with complete failure list
- Send report to kt-writer with exact list of what failed
- kt-writer fixes ONLY the flagged items — not the whole document
- kt-writer saves updated KT_DOCUMENT.md to docs folder
- Run all 12 tests again from the beginning

Round 2:
- If failures remain after round 1
- Update VALIDATION_REPORT.md with remaining failures
- Send updated report to kt-writer for second fix attempt
- kt-writer fixes remaining flagged items only
- Run all 12 tests again from the beginning

Round 3:
- If failures remain after round 2
- Update VALIDATION_REPORT.md with final remaining failures
- Send updated report to kt-writer for final fix attempt
- kt-writer fixes remaining flagged items only
- Run all 12 tests one final time

### After 3 Rounds — Always Move Forward
If any test still failing after 3 complete rounds:
- Do NOT stop the pipeline
- Add all remaining failures to Section 14 of KT_DOCUMENT.md
  as NEEDS HUMAN REVIEW
- Mark document as CONDITIONALLY APPROVED
- Save final VALIDATION_REPORT.md
- Always make forward progress — never stop completely

## Exit Criteria — Two Valid Ways to Complete

EXIT 1 — Clean Approval:
All 12 tests pass in any round
Mark as APPROVED
Document is ready for use

EXIT 2 — Conditional Approval After 3 Rounds:
3 rounds completed
Remaining failures added to Section 14
Mark as CONDITIONALLY APPROVED
Document is ready with honest flags for human review

## Output — Save VALIDATION_REPORT.md to Project Root

# Validation Report

## Summary
- Total Tests Run: 12
- Passed: X
- Failed: X
- Pass Rate: X%
- Validation Round: N of 3
- Overall Result: APPROVED / CONDITIONALLY APPROVED

## Failed Items
| Test | Test Name | Item | In Document | In Code | File to Check |
|---|---|---|---|---|---|

## Passed Items
| Test | Test Name | Item | Verified In File |
|---|---|---|---|

## Business Logic Limitation Note
Business logic validation covers code level rules only.
Domain knowledge and business intent require human review.
These items have been added to Section 14 of KT_DOCUMENT.md.

## Next Action
APPROVED: Document ready. Find it at docs/KT_DOCUMENT.md
CONDITIONALLY APPROVED: Review Section 14 before sharing document.

---

## Anti-Hallucination is the Only Job Here

Be strict. Be precise. Read the actual file fresh for each test.
Do not rely on memory of previous reads.
Open each file as if reading it for the first time.

A wrong class name caught here saves hours of confusion
for the new developer joining this project.

The code is always right. The document must match the code.