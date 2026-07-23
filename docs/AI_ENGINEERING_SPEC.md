# AI Engineering Specification
## Autonomous AI Coding — Reusable Engineering Methodology

**Version:** 1.0  
**Derived from:** AI Development Orchestrator (ADO) MVP — 157 tasks, 308 tests, 1 human intervention  
**Applies to:** Any project using Claude Code or a compatible AI coding agent for autonomous implementation

---

## 1. Overview

This specification defines a reusable engineering methodology for running AI coding agents autonomously on real software projects.

The methodology is not prompt engineering. It is project infrastructure — a set of documents, conventions, and contracts that give an AI agent enough structure to implement a complete project backlog with minimal human involvement.

**The core principle:** Treat the AI coding agent as a worker, not a chatbot. A worker needs a task assignment, a scope boundary, a definition of done, and a way to validate the output. This specification defines how to provide all of those in software.

**What this methodology produces:**
- A clean, reviewable git history with one commit per task
- Architecture that matches your documentation
- Tests co-committed with every production change
- A resumable execution state that survives session interruptions
- A complete audit trail of what the agent did and why

**What this methodology requires from you (the human):**
- Write specifications before writing code
- Decompose the backlog into atomic tasks
- Define engineering guardrails in `CLAUDE.md`
- Trigger autonomous execution
- Resume after context compaction (press "continue")

---

## 2. Mental Model

### The Agent Is a Worker

A chatbot responds to prompts. A worker executes tasks within a managed workflow.

| Chatbot model | Worker model |
|---------------|-------------|
| What prompt gets the best output? | What is the agent's current task? |
| How do I describe what I want? | What context does it need for this specific task? |
| How do I re-prompt after a bad result? | What does "done" mean for this task? |
| — | How do we validate the output? |
| — | How does it know what to do next? |

These are engineering management questions. This specification answers them in software.

### The Documentation Is the Engineering Contract

Documentation kept in the repository is accessible to an AI agent operating on the local file system. Documentation in external systems (Confluence, Notion, Google Docs) is not.

Every specification document in this methodology is committed to the repository in a `docs/` directory. The agent reads them directly before every implementation.

### Constraints Beat Aspirations

"The CLI should be thin" is vague. "The CLI must not contain orchestration logic" is not.

Throughout this specification, architecture rules are written as **"must not"** constraints. Negative constraints are more actionable for AI agents than positive descriptions.

---

## 3. Repository Structure

Before writing any application code, set up this structure:

```
your-project/
├── CLAUDE.md                        ← Engineering contract (read every session)
├── TASKS.md                         ← Atomic task backlog (the execution queue)
├── START_AUTONOMOUS_EXECUTION.md    ← Execution trigger (re-runnable)
├── docs/
│   ├── 01-vision.md                 ← Why this project exists
│   ├── 02-product-requirements.md   ← What the MVP must do and must not do
│   ├── 03-system-architecture.md    ← Components, boundaries, data flow
│   ├── 04-repository-structure.md   ← Package layout and directory conventions
│   ├── 05-component-design.md       ← Per-component contracts
│   └── (additional as needed)
└── .ado/
    └── progress.yaml                ← Execution state (auto-managed)
```

**Rule:** Commit all specification documents before writing application code. The agent needs to read them. If they don't exist yet, the agent will invent requirements to fill the gaps.

---

## 4. Specification Pipeline

Write these documents in order. Each one is a dependency for the next.

### 4.1 Vision (`docs/01-vision.md`)

One to two pages. Answer:
- What problem does this project solve?
- Who has the problem?
- What does the world look like after this project exists?
- What is explicitly out of scope?

**Why it matters:** The agent will make scope decisions throughout implementation. If it doesn't know what the project is *for*, it will expand scope toward what seems logical.

### 4.2 Product Requirements (`docs/02-product-requirements.md`)

Answer:
- What must the MVP do? (explicit feature list)
- What must the MVP not do? (explicit out-of-scope list — write this, don't skip it)
- What are the quality bars? (performance, reliability, security)
- What is the acceptance criteria for MVP completion?

**Rule:** Write an explicit out-of-scope list. Without it, the agent will treat adjacent capabilities as in-scope.

### 4.3 System Architecture (`docs/03-system-architecture.md`)

Define:
- All major components
- What each component is responsible for (and what it is *not* responsible for)
- How components communicate
- Data flow for the primary use case

Write each component with a "must not" constraint:

```
## Component: CLI
Responsibilities: parse arguments, invoke application, display results
The CLI must not contain orchestration logic.
The CLI must not own business state.
```

### 4.4 Repository Structure (`docs/04-repository-structure.md`)

Define:
- Package/directory layout (include empty directories if needed)
- File naming conventions
- Where production code lives vs test code

**Why explicit package structure matters:** When the agent creates a new class, it needs to know where to put it. Ambiguous structure produces scattered code that violates intended boundaries.

### 4.5 Component Design (`docs/05-component-design.md` and beyond)

For each major component:
- Interface definition (method signatures and types)
- Behavioral contract (what it does, what invariants it maintains)
- Integration contract (what it calls, what calls it)
- Explicit constraints (what it must not do)

Include Architectural Decision Records (ADRs) for non-obvious choices.

### 4.6 Documentation Authority Order

Define a priority order for your specification documents. When two documents conflict, the higher-priority document wins. Document this in `CLAUDE.md`.

Example:
```
1. docs/01-vision.md
2. docs/02-product-requirements.md
3. docs/03-system-architecture.md
4. docs/04-repository-structure.md
5. docs/05-component-design.md
6. TASKS.md
```

**Rule:** The agent must stop and report a conflict rather than invent a resolution. Make this explicit in `CLAUDE.md`.

---

## 5. Task Backlog Design

The task backlog (`TASKS.md`) is the most important engineering artifact in this methodology. Its quality directly determines the quality of autonomous execution.

### 5.1 Task Structure

Every task must include:

```markdown
### PROJ-NNN — Task Title

**Status:** todo
**Dependencies:** PROJ-NNN, PROJ-NNN (or "none")

**Description:**
One paragraph describing what this task implements and why.

**Expected files:**
- path/to/new/file.kt (new)
- path/to/existing/file.kt (modified)

**Acceptance criteria:**
- Specific, verifiable condition 1
- Specific, verifiable condition 2
- Specific, verifiable condition 3

**Test requirement:**
- What to run (e.g., `./gradlew test`)
- What test cases must exist
```

### 5.2 Task Sizing

**Target:** One task should produce one focused git commit.

If implementing a task requires changing more than 3–5 files in unrelated ways, the task is too large. Split it.

If implementing a task requires less than a meaningful function, the task is too small. Merge it with a related task.

**Good size indicators:**
- One interface definition
- One implementation of a defined interface
- One parser for a specific domain concept
- One test suite for a defined component
- One configuration change with a specific purpose

**Oversized task indicators:**
- "Implement the [Engine/Service/Manager]" (entire component)
- "Add all [parsers/validators/converters]" (whole class of objects)
- "Wire everything together" (integration without specific scope)

### 5.3 Dependency Graph Design

Dependencies define execution order. Design them so:
- Foundation tasks have no dependencies and can run first
- Tasks within an epic depend only on earlier tasks in the same epic
- Integration tasks depend on the components they integrate
- Milestone validation tasks depend on all tasks in the milestone

**Rule:** The task selection algorithm is: first `todo` task where all dependencies are `done`. Make your dependency graph acyclic and progressive.

### 5.4 Epic Structure

Group tasks into epics that map to components or phases:

```
Epic 1 — Bootstrap (project setup, build config, package structure)
Epic 2 — Domain Models (data structures, no logic)
Epic 3 — Component A (interface → implementation → tests)
Epic 4 — Component B (interface → implementation → tests)
...
Epic N — Integration & E2E Tests
Epic N+1 — Final Validation
```

Include explicit validation tasks at the end of each epic:

```markdown
### PROJ-014 — Validate Epic 1 Build

**Status:** todo
**Dependencies:** PROJ-001, PROJ-002, ..., PROJ-013

**Description:**
Run `./gradlew clean build` and confirm the build passes with all tests green.
This task adds no source code.

**Expected files:** (none)

**Acceptance criteria:**
- `./gradlew clean build` exits with code 0
- All tests pass
- Zero test failures

**Test requirement:**
- `./gradlew clean build`
```

These zero-code tasks create committed proof of clean-build state at each milestone.

### 5.5 Cross-Cutting Infrastructure Tasks

Add explicit validation tasks for infrastructure assumptions that could become latent defects:

```markdown
### PROJ-015 — Validate .gitignore Does Not Exclude Source Packages

**Status:** todo
**Dependencies:** PROJ-001

**Description:**
Verify that no .gitignore pattern excludes files that should be tracked.
Check that package names do not collide with ignored directory names.

**Acceptance criteria:**
- Run `git status` and verify no source files are untracked due to .gitignore
- Verify `build/` exclusion is anchored to the root (`/build/`)
- Verify no source package name collides with ignored directories
```

This prevents the class of failure where infrastructure configured in task 7 becomes a silent defect discovered in task 91.

---

## 6. Engineering Contract (`CLAUDE.md`)

`CLAUDE.md` is a repository-level instructions file that Claude Code reads at the start of every session. It is permanent infrastructure, not a one-time prompt. Its rules apply to every task in the backlog without being re-stated.

### 6.1 Required Sections

#### Project Identity
```
[Project Name] is [one-sentence description].
[Agent role sentence].
The product must remain focused on the approved MVP.
Do not expand product scope.
```

#### Source of Truth
```
Before implementing code, read the relevant project documentation.

Documentation authority order:
1. docs/01-vision.md
2. docs/02-product-requirements.md
[...]
N. TASKS.md

A lower-priority document must not override a higher-priority document.
If documents contain incompatible requirements, stop and report the conflict.
Do not invent requirements to resolve documentation conflicts.
```

#### Product Scope
List what the MVP does — and explicitly list what is out of scope.

#### Architecture Rules
One section per major component. Each section must contain at least one "must not" constraint.

```
## [Component Name]
[Component] is responsible for [specific responsibility].
[Component] must not [boundary constraint 1].
[Component] must not [boundary constraint 2].
```

#### Coding Contract
```
Every implementation session must follow these rules:
1. Implement exactly one task at a time.
2. Do not implement future tasks.
3. Do not refactor unrelated code.
4. Follow project documentation.
5. Write required tests with production code.
6. Keep changes small and focused.
7. Run required validation.
8. Repair failures caused by the current task.
9. Update only the current task status.
10. Create one focused commit per completed task.
```

#### Task Execution Protocol
```
When autonomously executing TASKS.md:
1. Read TASKS.md.
2. Resume an existing in_progress task if one exists.
3. Otherwise select the first executable todo task.
4. Validate dependencies.
5. Mark the task in_progress.
6. Implement the minimal required change.
7. Add required tests.
8. Run tests and build.
9. Repair failures caused by the current task.
10. Inspect the change set for scope expansion.
11. Mark the task done.
12. Create one focused Git commit.
13. Reload TASKS.md.
14. Continue with the next executable task.
```

#### Testing Rules
```
Tests must validate observable behavior.
Prefer focused unit tests.
Use integration tests where component interaction must be validated.

For components with durable state, always include a round-trip test:
save → reload with fresh instance → verify state is present.

Do not:
- disable failing tests
- weaken assertions to force success
- delete tests to pass the build
- bypass validation
```

#### Validation
```
Run [your build command] after each task.
Run [your clean build command] before milestone completion.
A task cannot be marked done while required validation is failing.
```

#### Scope Protection
```
Before completing every task, inspect the change set.
Verify:
- only current-task functionality was implemented
- unrelated files were not changed without necessity
- future tasks were not implemented
- architecture boundaries remain intact
Remove accidental scope expansion before committing.
```

#### Git Rules
```
Create exactly one focused commit for each completed task.
The commit must contain: implementation + tests + TASKS.md status update.
Do not combine multiple tasks in one commit.
Do not commit unrelated pre-existing changes.
Do not force push.
Commit format: type(scope): short description
```

#### Blocking Conditions
```
Normal build failures and test failures are not blocking conditions.
Attempt to repair failures caused by the current task.

Stop autonomous execution only when safe progress requires:
- inventing a product requirement
- contradicting project documentation
- changing documented architecture
- implementing an undocumented capability
- unavailable required credentials or tooling
- destructive repository operations
- choosing between conflicting source-of-truth documents

When blocked:
1. Stop implementation.
2. Do not mark the task done.
3. Preserve the safest repository state.
4. Report the task ID and title.
5. Report the exact blocking condition.
6. Identify the relevant source-of-truth document.
7. State the exact human decision required.
Do not continue to later tasks.
```

#### Completion Criteria
```
The project is complete only when:
- every task in TASKS.md is done
- [clean build command] succeeds
- all tests pass
- no task remains todo, in_progress, or blocked
- architecture boundaries match project documentation

At completion, produce a final execution summary.
Do not add additional features after completion.
```

---

## 7. Execution Trigger (`START_AUTONOMOUS_EXECUTION.md`)

The execution trigger is a separate document that starts the autonomous loop. It must be re-runnable without risk of duplicating already-completed work.

```markdown
# Start Autonomous Execution

You are operating as the autonomous implementation engineer for [Project Name].

Follow all repository instructions defined in CLAUDE.md.

## Objective

Autonomously execute the implementation backlog until:
- every task in TASKS.md is done, or
- a genuine blocking condition defined in CLAUDE.md occurs.

Do not stop after completing a single task.
Do not ask for permission before continuing to the next executable task.

## Startup Sequence

1. Read CLAUDE.md.
2. Read all source-of-truth documentation in docs/.
3. Read TASKS.md.
4. Inspect the current repository and git state.
5. Resume the existing in_progress task if one exists.
6. Otherwise select the first executable todo task.

## Per-Task Loop

1. Validate task dependencies.
2. Mark only the selected task in_progress.
3. Inspect the existing implementation.
4. Implement the minimum change required by the task.
5. Add required tests.
6. Run task-specific validation.
7. Run the project build.
8. Repair failures caused by the current task.
9. Inspect the final change set for scope expansion.
10. Mark the task done only after validation succeeds.
11. Create exactly one focused git commit for the task.
12. Reload TASKS.md.
13. Select the next executable task.
14. Continue autonomous execution.

## Completion

When every task is done:
1. Run the complete clean build.
2. Confirm all tests pass.
3. Inspect git status.
4. Verify no task remains todo, in_progress, or blocked.
5. Produce the final execution summary.

Start now. Continue until complete or genuinely blocked.
```

**To start execution:** Open Claude Code in the repository and say:
```
Read START_AUTONOMOUS_EXECUTION.md and execute it.
```

---

## 8. Progress Tracking

Progress state is the resume anchor after context compaction. It must be reliable.

### 8.1 State File Format

Store progress in `.ado/progress.yaml` (or `.progress.yaml` for simpler projects):

```yaml
version: 1
tasks:
  - id: PROJ-001
    status: done
  - id: PROJ-002
    status: done
  - id: PROJ-003
    status: in_progress
  - id: PROJ-004
    status: todo
```

### 8.2 State Machine

```
todo → in_progress → done
              ↓
           blocked
              ↓
           failed
```

Rules:
- Only one task may be `in_progress` at any time.
- A task transitions to `done` only after a successful commit.
- A task transitions to `blocked` only on a genuine blocking condition.

### 8.3 Resume Rule

The execution startup sequence checks:
1. Is there an `in_progress` task? → Resume it.
2. Otherwise → Select the first `todo` task where all dependencies are `done`.

This two-rule algorithm survives session resets without human guidance.

### 8.4 Persistence Testing (Critical)

For any component that manages persistent state, write a round-trip test:

```kotlin
// Save state
tracker.updateStatus("PROJ-003", Status.DONE)

// Create a new instance pointing at the same file
val freshTracker = FileProgressTracker(samePath)
val reloaded = freshTracker.loadProgress()

// Assert state survived the reload
assertEquals(Status.DONE, reloaded.getStatus("PROJ-003"))
```

Without this test pattern, the bug where `updateStatus()` updates memory but not disk will not be caught until the session ends and the agent tries to resume. (This exact failure occurred in the ADO MVP as FAILURE-003.)

---

## 9. Git Discipline

### 9.1 Commit Format

Use Conventional Commits:
```
type(scope): short description

[optional body with task IDs]

Co-Authored-By: Claude Sonnet 4.6 <noreply@anthropic.com>
```

Types: `feat`, `fix`, `chore`, `test`, `docs`, `refactor`  
Scope: the component or epic being changed (e.g., `planner`, `context`, `engine`)

### 9.2 One Commit per Task

Each commit must contain exactly:
- Production code for the current task
- Tests for the current task
- Updated task status in TASKS.md (status: `done`)

Nothing else. No "while I was in here" changes.

### 9.3 AI Authorship Record

Include `Co-Authored-By` with the agent name in every commit. This creates an honest, auditable record of AI involvement visible in the git history without requiring external tracking.

### 9.4 Why This Matters

A one-task-per-commit history is navigable. When something breaks, you can `git bisect` directly to the task that introduced it. When you review the agent's work, you can `git show <hash>` any commit and understand exactly what was implemented and why.

The ADO MVP's git history shows this in practice: every repair commit (`298d583`, `c4502c8`) is traceable to a specific task, and the root cause is visible in the diff.

---

## 10. Quality Gates

Quality gates are non-negotiable. They must not be bypassed to complete a task.

### Mandatory gates per task

| Gate | Command | When |
|------|---------|------|
| Compilation | build command | After every implementation |
| Unit tests | test command | After every implementation |
| Formatting | lint/format command | After every implementation |
| Integration tests | integration test command | After task-specific integration |

### Mandatory gates per milestone

| Gate | Command | When |
|------|---------|------|
| Full clean build | `clean build` command | At every milestone validation task |
| Static analysis | lint/analysis command | At every milestone validation task |
| Architecture validation | boundary check | At every milestone validation task |

**Rule:** A task may not be marked `done` while any required gate is failing. If a gate is failing due to a pre-existing issue unrelated to the current task, stop and report it — do not bypass the gate.

---

## 11. Failure and Repair

### 11.1 What Is Not a Blocking Condition

- Compilation errors caused by the current task
- Test failures caused by the current task
- Build failures caused by the current task

These are expected. Repair them and continue.

### 11.2 Repair Protocol

When a failure occurs:
1. Categorize the failure (compilation, test, dependency, formatting, architecture)
2. Read the full error output
3. Build a repair context: what was changed, what failed, what the error says
4. Implement the minimal fix
5. Re-run the failing command
6. Repeat up to the configured retry limit (default: 3)

### 11.3 What to Watch For

Based on the ADO MVP failure analysis, these failure patterns are common in autonomous AI coding:

**Hardcoded prefix assumptions** — parsers, validators, and pattern matchers written for your specific project's naming convention. Add test cases with alternative naming (e.g., if your tasks are `PROJ-001`, add a test case for `OTHER-001`).

**Anchoring issues in configuration** — `.gitignore`, `.editorconfig`, and similar files with patterns that are not anchored to the root (`build/` vs `/build/`).

**Memory-not-disk state** — any component that updates in-memory state must also persist it. Add round-trip tests for every component with durable state.

**Stale test assertions** — tests written for placeholder implementations that become incorrect when the real implementation replaces the placeholder. Update tests in the same commit as the implementation change.

### 11.4 Genuine Blocking Conditions

Stop and report to the human only when:
- Proceeding requires inventing a requirement not in any specification document
- Two specification documents directly contradict each other
- Required credentials or tooling are unavailable with no documented alternative
- The next step is a destructive git operation not sanctioned by any document

---

## 12. Context Compaction Resilience

### What Happens

At approximately 15–25 minutes of active execution, the AI agent's context window fills. Claude Code automatically compacts prior context into a summary and continues. For sessions longer than one context window, execution suspends and a human must re-trigger it.

This is not a failure — it is an operational characteristic of current AI coding systems.

### Design Principles

**Make the task backlog the authoritative state.** Every task's status is in `TASKS.md`. On session restart, the agent reads `TASKS.md` and knows exactly where it is. No progress is lost.

**Make the execution trigger re-runnable.** `START_AUTONOMOUS_EXECUTION.md` must be safe to execute at any time. The startup sequence handles the "is there an in-progress task?" case. Duplicate execution is impossible if the task selection rule is followed.

**Keep progress state reliable.** The progress file is the resume anchor. Test it with round-trip persistence tests before autonomous execution begins.

**Estimate sessions needed.** The ADO MVP produced approximately 19 commits per context window of active execution (~12 minutes). Larger models may handle more. For a 50-task project, expect 2–3 sessions; for a 150-task project, expect 6–10 sessions.

### Human Action Required

When a session ends due to context compaction, re-trigger execution with:
```
Read START_AUTONOMOUS_EXECUTION.md and execute it.
```

The agent will resume from the correct next task without any additional direction.

---

## 13. Testing Methodology

### 13.1 Test What You Can Observe

Tests must validate the externally observable behavior of a component, not its internal implementation:

- Return values from public methods
- State persisted to disk and reloaded
- Exceptions thrown under error conditions
- Side effects (files created, network calls made) via test doubles

Do not test private implementation details.

### 13.2 Test Double Strategy

At process boundaries (file system, network, external APIs), use test doubles in unit tests. At component integration boundaries, use real implementations.

Unit tests must not:
- Invoke real AI agents
- Require network access
- Depend on external credentials
- Depend on system-specific paths

### 13.3 Parser Testing Pattern

For any parser or matcher, always include inputs that test the boundaries of the pattern:

```kotlin
// Test with expected prefix
assertEquals("PROJ-001", parser.parse("PROJ-001 — Task Title"))

// Test with alternative prefix — catches hardcoded assumptions
assertEquals("OTHER-001", parser.parse("OTHER-001 — Task Title"))

// Test with edge cases
assertNull(parser.parse("not a valid task line"))
```

### 13.4 Round-Trip Testing for Persistent State

```kotlin
// Phase 1: Write state
val writer = FileStateStore(path)
writer.save(StateSnapshot(tasks = listOf(Task("T-001", Status.DONE))))

// Phase 2: Read state with a fresh instance (no shared memory)
val reader = FileStateStore(path)
val loaded = reader.load()

// Phase 3: Assert persistence survived
assertEquals(Status.DONE, loaded.getStatus("T-001"))
```

---

## 14. Scope Protection

Autonomous agents have a tendency to implement adjacent functionality they "see coming." This is scope creep. It must be detected and reversed before each commit.

### Pre-Commit Checklist

Before marking any task `done` and before creating the commit:

- [ ] Only the files listed in the task's "Expected files" section were changed (or a superset with documented reason)
- [ ] No future tasks were implemented speculatively
- [ ] No unrelated refactoring was introduced
- [ ] No new dependencies were added that the specification does not require
- [ ] Architecture boundaries are intact

### How to Remove Scope Creep

```bash
# See what changed
git diff

# If an unrelated file was changed, unstage it
git restore path/to/unrelated/file.kt

# If a future feature was implemented, revert those specific changes
git diff path/to/file.kt  # identify the future lines
# then manually remove them via editor
```

The goal: each commit is a focused, reviewable unit of work. "While I was in there" changes belong in a separate task.

---

## 15. Completion Criteria

A project is complete only when all of the following are true:

| Criterion | Check |
|-----------|-------|
| Every task in TASKS.md has status `done` | `grep "todo\|in_progress\|blocked" TASKS.md` returns empty |
| Clean build passes | `[clean build command]` exits 0 |
| All tests pass | Zero test failures in build output |
| No task is `todo` | — |
| No task is `in_progress` | — |
| No task is `blocked` | — |
| Architecture boundaries match docs | Code review confirms boundaries |
| No future capability was added | Scope protection audit |

At completion, produce a final execution summary:
- Total tasks completed
- Build status
- Test count and pass/fail
- Git status (clean working tree)
- List of known limitations
- Any blocking conditions encountered

---

## 16. Quick-Start Checklist

Use this checklist when starting a new project with this methodology.

### Phase 1 — Specification (before any code)

- [ ] Write `docs/01-vision.md` — problem, audience, out-of-scope list
- [ ] Write `docs/02-product-requirements.md` — feature list, quality bar, acceptance criteria
- [ ] Write `docs/03-system-architecture.md` — components, boundaries, must-not constraints
- [ ] Write `docs/04-repository-structure.md` — package layout, naming conventions
- [ ] Write `docs/05-component-design.md` — per-component interfaces and contracts
- [ ] Commit all specification documents

### Phase 2 — Task Decomposition (before execution trigger)

- [ ] Decompose backlog into atomic tasks in `TASKS.md`
- [ ] Each task has: ID, status, dependencies, description, expected files, acceptance criteria, test requirement
- [ ] All tasks start with status `todo`
- [ ] Add milestone validation tasks every 15–30 tasks
- [ ] Add cross-cutting infrastructure validation tasks in Epic 1
- [ ] Add parser generalization tests to any task that creates a pattern matcher
- [ ] Review: can each task be implemented in one focused commit?

### Phase 3 — Engineering Contract

- [ ] Write `CLAUDE.md` with all required sections (Section 6 of this spec)
- [ ] Documentation authority order is defined
- [ ] Every major component has a "must not" constraint
- [ ] Blocking conditions are explicitly listed
- [ ] Completion criteria are defined

### Phase 4 — Execution Infrastructure

- [ ] Write `START_AUTONOMOUS_EXECUTION.md` (re-runnable)
- [ ] Verify `.gitignore` uses anchored patterns (`/build/`, not `build/`)
- [ ] Commit everything
- [ ] Verify: `git status` is clean

### Phase 5 — Trigger Execution

- [ ] Open Claude Code in the repository
- [ ] Send: `Read START_AUTONOMOUS_EXECUTION.md and execute it.`
- [ ] Monitor: check in periodically; resume with the same command after context compaction

### Phase 6 — Completion

- [ ] All tasks done in TASKS.md
- [ ] Clean build passes
- [ ] All tests pass
- [ ] Git working tree is clean
- [ ] Produce final execution summary

---

## 17. Known Limitations

These limitations are observed from the ADO MVP execution and represent the current state of this methodology.

**Context compaction requires human intervention.** For projects larger than ~20 tasks, expect multiple sessions. The only human action required is re-triggering execution after each session ends.

**Latent defects are possible.** The agent validates each task against its own acceptance criteria. Cross-cutting assumptions made in early tasks (`.gitignore` patterns, regex prefixes) may not be discovered until a later task exercises them. Mitigate with explicit cross-cutting validation tasks.

**Batch commits appear in later epics.** As the codebase grows more complex, the agent may combine multiple closely related tasks into fewer commits. The one-task-one-commit discipline is most reliable in early epics. Compensate with finer task decomposition in later epics.

**Round-trip persistence failures go undetected.** State components that update memory but not disk pass naive unit tests. Mitigate with explicit round-trip test requirements in `CLAUDE.md`.

**Intermediate build state is not visible.** The git history captures committed state, not the intermediate build/repair cycles within a session. The number of repair attempts for a given failure is unknown from git history alone.

---

## 18. Evidence Basis

This specification is derived from the AI Development Orchestrator (ADO) MVP, implemented July 11, 2026.

| Metric | Value |
|--------|-------|
| Tasks | 157 completed |
| Commits | 87 implementation commits |
| Tests | 308, zero failures |
| Confirmed defects | 4 (all repaired autonomously) |
| Human interventions | 1 (session resumption) |
| Active execution time | ~107 minutes across 2 sessions |

Full engineering retrospective: [`journey/LESSONS_LEARNED.md`](journey/LESSONS_LEARNED.md)  
Full failure analysis: [`journey/FAILURE_ANALYSIS.md`](journey/FAILURE_ANALYSIS.md)  
Full metrics: [`journey/EXECUTION_METRICS.md`](journey/EXECUTION_METRICS.md)
