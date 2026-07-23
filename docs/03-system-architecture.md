# AI Development Orchestrator (ADO)
# System Architecture

**Document:** 03-system-architecture.md  
**Version:** 0.1  
**Status:** Draft  
**Product:** AI Development Orchestrator (ADO)

---

# 1. Purpose

This document defines the architecture of the AI Development Orchestrator (ADO) MVP.

It describes the major architectural components, their responsibilities, interactions, execution flow, and extension points required to autonomously execute software development work.

This architecture intentionally follows the MVP defined in the Vision and Product Requirements documents and avoids introducing additional product capabilities.

---

# 2. Architecture Goals

The architecture is designed to achieve the following goals:

- Autonomous engineering execution
- Deterministic workflow execution
- Vendor-neutral AI agent integration
- Minimal context delivery
- Incremental story implementation
- Automated validation and repair
- High cohesion and low coupling
- Extensible component interfaces

---

# 3. Architectural Principles

The architecture follows these principles:

1. Documentation is the source of truth.
2. Components have a single responsibility.
3. AI agents are interchangeable workers.
4. The orchestrator owns all workflow decisions.
5. Context is assembled dynamically.
6. Stories are executed sequentially.
7. Every story must pass quality gates.
8. Components communicate through well-defined interfaces.
9. Architecture should remain vendor neutral.

---

# 4. System Context

```
                     Human
                       │
                       ▼
          Product Documentation
(PRD, Architecture, ADRs, TASKS, Standards)
                       │
                       ▼
         AI Development Orchestrator
                       │
       ┌───────────────┼────────────────┐
       ▼               ▼                ▼
 Story Planner   Context Builder   Progress Tracker
                       │
                       ▼
               Agent Adapter
                       │
                       ▼
                AI Coding Agent
      (Claude / Codex / Gemini / Devin)
                       │
                       ▼
               Generated Changes
                       │
                       ▼
               Build Executor
                       │
              Build Successful?
               │              │
              Yes            No
               │              ▼
               │      Failure Analyzer
               │              │
               └──── Repair Loop ─────┐
                                      │
                                      ▼
                              Agent Adapter
                                      │
                                      ▼
                            Successful Build
                                      │
                                      ▼
                            Quality Gate Engine
                                      │
                                      ▼
                            AI Code Reviewer
                                      │
                                      ▼
                                Git Manager
                                      │
                                      ▼
                            Progress Tracker
                                      │
                                      ▼
                           Next Story Selection
```

---

# 5. High-Level Architecture

The ADO architecture consists of three logical layers.

```
+------------------------------------------------------+
|                 Documentation Layer                  |
|------------------------------------------------------|
| PRD | Architecture | ADR | TASKS | Standards | Code |
+------------------------------------------------------+

                     │

+------------------------------------------------------+
|              Orchestration Layer                     |
|------------------------------------------------------|
| Story Planner                                        |
| Context Builder                                      |
| Agent Adapter                                        |
| Build Executor                                       |
| Failure Analyzer                                     |
| Repair Loop                                          |
| Quality Gate Engine                                  |
| Code Review Agent                                    |
| Git Manager                                          |
| Progress Tracker                                     |
+------------------------------------------------------+

                     │

+------------------------------------------------------+
|               External Systems Layer                 |
|------------------------------------------------------|
| Claude Code                                          |
| Codex                                                |
| Gemini CLI                                           |
| Devin                                                |
| Git                                                  |
| Build Tools                                          |
+------------------------------------------------------+
```

---

# 6. Component Architecture

## 6.1 Story Planner

### Responsibility

Determines the next executable story.

### Inputs

- TASKS.md
- Story dependencies
- Progress state

### Outputs

- Selected story

The Story Planner guarantees that only one story is active at a time.

---

## 6.2 Context Builder

### Responsibility

Constructs the minimal context required for implementation.

### Inputs

- PRD
- Architecture
- ADRs
- Story specification
- Existing source code
- Coding standards

### Outputs

A Story Context Package containing only the information necessary for the current implementation.

The Context Builder is responsible for minimizing token usage while preserving correctness.

---

## 6.3 Agent Adapter

### Responsibility

Provides a common interface for all supported AI coding agents.

Supported implementations include:

- Claude Code
- Codex
- Gemini CLI
- Devin

The orchestrator never communicates directly with a specific AI provider.

---

## 6.4 Build Executor

### Responsibility

Executes project build and test commands.

Supported build systems include:

- Gradle
- Maven
- npm
- pnpm

The Build Executor captures:

- exit status
- stdout
- stderr
- warnings
- execution duration

---

## 6.5 Failure Analyzer

### Responsibility

Interprets build failures and produces structured feedback.

Supported failure categories include:

- Compilation errors
- Test failures
- Missing dependencies
- Formatting violations
- Architecture violations

The Failure Analyzer does not modify source code.

---

## 6.6 Repair Loop

### Responsibility

Automatically repairs implementation failures.

Workflow:

1. Receive failure analysis
2. Generate repair request
3. Invoke coding agent
4. Apply changes
5. Rebuild
6. Repeat until success or retry limit

The Repair Loop terminates when:

- build succeeds
- retry limit reached

---

## 6.7 Quality Gate Engine

### Responsibility

Validates completed implementations.

Mandatory quality gates:

- Build
- Formatting
- Static analysis
- Unit tests
- Integration tests
- Architecture validation

A story cannot complete until every gate passes.

---

## 6.8 Code Review Agent

### Responsibility

Performs an independent AI review.

Review criteria include:

- Architecture compliance
- SOLID principles
- Readability
- Naming
- Maintainability
- Performance
- Thread safety
- Test coverage

Blocking issues prevent story completion.

---

## 6.9 Git Manager

### Responsibility

Persists successful work.

Responsibilities include:

- Create commit
- Generate commit message
- Update story status

Branch creation and pull request preparation remain optional in the MVP.

---

## 6.10 Progress Tracker

### Responsibility

Maintains execution state.

Supported story states:

- todo
- ready
- in_progress
- review
- retrying
- blocked
- failed
- passed
- done

The Progress Tracker persists state between executions.

---

# 7. Execution Flow

```
Load Project

        │

        ▼

Select Story

        │

        ▼

Build Context

        │

        ▼

Invoke AI Agent

        │

        ▼

Apply Changes

        │

        ▼

Execute Build

        │

        ▼

Build Successful?

 ┌───────────────┐
 │               │
 ▼               ▼

Yes             No
 │               │
 │        Analyze Failure
 │               │
 │               ▼
 │         Repair Loop
 │               │
 └───────────────┘

        ▼

Run Quality Gates

        ▼

AI Code Review

        ▼

Git Commit

        ▼

Update Progress

        ▼

Next Story
```

---

# 8. Story Context Package

The Context Builder assembles a Story Context Package.

```
Story Context

├── Story Specification
├── Acceptance Criteria
├── Architecture Constraints
├── Coding Standards
├── Relevant Documentation
├── Impacted Source Files
├── Related Tests
└── Current Implementation
```

Only the required information is included.

Entire repositories are never sent unless explicitly required.

---

# 9. Agent Interaction Model

All coding agents implement the same logical contract.

```
Context Package

        │

        ▼

Generate Implementation

        │

        ▼

Response

├── Modified Files
├── Summary
└── Implementation Notes
```

The orchestrator remains independent of the underlying AI provider.

---

# 10. Build and Validation Architecture

```
Generated Changes

        │

        ▼

Build Executor

        │

        ▼

Build Result

├── Status
├── Output
├── Errors
├── Warnings
└── Duration

        │

        ▼

Failure Analyzer
```

The Build Executor never interprets failures.

Interpretation is delegated to the Failure Analyzer.

---

# 11. State Management

ADO maintains execution state independently of source code.

Example:

```yaml
stories:

  AI-001:
    status: done

  AI-002:
    status: done

  AI-003:
    status: in_progress

  AI-004:
    status: blocked
```

Only one story may be active at a time.

---

# 12. Configuration Architecture

The orchestrator is configured through YAML.

Example:

```yaml
agent:

  provider: claude-code

build:

  command: ./gradlew build

test:

  command: ./gradlew test

review:

  enabled: true

repair:

  retries: 5
```

Configuration is externalized to avoid changes to orchestration logic.

---

# 13. Extension Points

The architecture intentionally defines extension points for future capabilities.

Future extensions may include:

- Additional AI agent adapters
- Additional build systems
- Additional quality gates
- Additional review engines
- Security scanners
- Multi-agent orchestration

The MVP does not implement these capabilities.

---

# 14. Deployment Model

The MVP executes entirely on a developer workstation.

```
Developer Machine

├── Git Repository
├── Project Documentation
├── AI Development Orchestrator
├── AI Coding Agent
├── Build Tools
└── Git
```

No cloud services or databases are required.

---

# 15. Architecture Constraints

The following constraints apply to all implementations.

## Sequential Execution

Only one story may be executed at a time.

---

## Deterministic Workflow

The orchestrator controls every execution step.

AI agents never determine workflow.

---

## Minimal Context

Only the context required for the active story may be provided.

---

## Incremental Development

Each execution implements exactly one story.

Future stories must never be implemented.

---

## Documentation Driven

All implementation decisions originate from project documentation.

---

## Vendor Neutrality

All AI providers integrate through the Agent Adapter interface.

No orchestration component shall depend on vendor-specific APIs.

---

## Quality Enforcement

No story is considered complete until all mandatory quality gates succeed.

---

# 16. Summary

The ADO architecture establishes a deterministic, documentation-driven orchestration engine that manages AI coding agents as specialized workers.

By separating orchestration, context assembly, code generation, validation, and quality enforcement into independent components, the architecture provides a modular foundation for autonomous software development while maintaining a deliberately minimal MVP scope. Future capabilities can be introduced by extending existing interfaces without altering the core orchestration workflow.