# AI Development Orchestrator (ADO)

# Component Design

**Document:** 05-component-design.md  
**Version:** 0.1  
**Status:** Draft  
**Product:** AI Development Orchestrator (ADO)

---

# 1. Purpose

This document defines the implementation design of every major component in the AI Development Orchestrator (ADO).

Each component is described by:

- responsibility
- public interface
- inputs
- outputs
- dependencies
- workflow
- error handling
- extension points

The goal is to provide sufficient implementation detail while remaining independent of any programming language.

---

# 2. Component Overview

The ADO MVP consists of the following core components.

```
CLI

↓

Execution Engine

├── Story Planner
├── Context Builder
├── Agent Adapter
├── Build Executor
├── Failure Analyzer
├── Repair Loop
├── Quality Gate Engine
├── Code Review Agent
├── Git Manager
└── Progress Tracker
```

The Execution Engine orchestrates all components.

The Task Generator (§15) is not part of this chain: it is invoked directly via the CLI and runs
once, before a backlog exists, rather than on every story.

---

# 3. Component Interaction

```
Story Planner

↓

Context Builder

↓

Agent Adapter

↓

Build Executor

↓

Failure Analyzer

↓

Repair Loop

↓

Quality Gates

↓

Code Review

↓

Git Manager

↓

Progress Tracker
```

Each component performs exactly one responsibility.

---

# 4. Story Planner

## Responsibility

Determine the next executable story.

---

## Inputs

- TASKS.md
- Progress State
- Story Dependencies

---

## Outputs

StorySelection

---

## Public Interface

```
selectNextStory()
```

Returns

```
StorySelection
```

---

## Workflow

1. Load tasks
2. Remove completed stories
3. Check dependencies
4. Ignore blocked stories
5. Select first executable story

---

## Dependencies

- Progress Tracker

---

## Error Handling

Returns

```
NoExecutableStory
```

when no work remains.

---

# 5. Context Builder

## Responsibility

Build the minimal implementation context.

---

## Inputs

- Story
- PRD
- Architecture
- ADRs
- Coding Standards
- Repository

---

## Outputs

```
ContextPackage
```

---

## Public Interface

```
buildContext(story)
```

---

## Workflow

1. Load story
2. Discover impacted modules
3. Load architecture
4. Load relevant documentation
5. Load related source files
6. Assemble context package

---

## Dependencies

- Documentation Loader
- Repository Scanner

---

## Error Handling

Missing documentation produces

```
ContextBuildException
```

---

# 6. Agent Adapter

## Responsibility

Provide a common interface for AI providers.

---

## Public Interface

```
generate(context)
```

---

## Input

```
ContextPackage
```

---

## Output

```
GenerationResult
```

---

## Generation Result

Contains

- modified files
- summary
- implementation notes

---

## Supported Adapters

- Claude Code
- Codex
- Gemini
- Devin

---

## Extension Point

New providers implement the same interface.

---

# 7. Build Executor

## Responsibility

Execute project builds.

---

## Public Interface

```
executeBuild()
```

---

## Output

```
BuildResult
```

---

## Build Result

Contains

- status
- stdout
- stderr
- warnings
- duration

---

## Supported Commands

- Gradle
- Maven
- npm
- pnpm

---

## Dependencies

System Process Executor

---

# 8. Failure Analyzer

## Responsibility

Interpret build failures.

---

## Public Interface

```
analyze(buildResult)
```

---

## Output

```
FailureAnalysis
```

---

## Supported Categories

- Compilation
- Testing
- Dependency
- Formatting
- Architecture

---

## Workflow

1. Read build output
2. Detect category
3. Extract relevant failures
4. Produce structured report

---

# 9. Repair Loop

## Responsibility

Automatically repair failed implementations.

---

## Public Interface

```
repair(failureAnalysis)
```

---

## Workflow

```
Failure

↓

Generate Repair Context

↓

Invoke Agent

↓

Apply Changes

↓

Rebuild

↓

Repeat
```

---

## Retry Policy

Default

```
5 attempts
```

---

## Exit Conditions

- Build succeeds
- Retry limit exceeded

---

# 10. Quality Gate Engine

## Responsibility

Validate implementation quality.

---

## Public Interface

```
runQualityGates()
```

---

## Quality Gates

- Build
- Formatting
- Static Analysis
- Unit Tests
- Integration Tests
- Architecture Validation

---

## Output

```
QualityGateResult
```

---

# 11. Code Review Agent

## Responsibility

Perform AI review.

---

## Public Interface

```
review()
```

---

## Checks

- SOLID
- Naming
- Readability
- Maintainability
- Performance
- Thread Safety
- Test Coverage

---

## Output

```
ReviewResult
```

---

## Blocking Reviews

Any blocking issue prevents completion.

---

# 12. Git Manager

## Responsibility

Persist successful implementation.

---

## Public Interface

```
commit()
```

---

## Responsibilities

- Commit
- Generate message
- Update repository

---

## Output

```
CommitResult
```

---

# 13. Progress Tracker

## Responsibility

Persist execution state.

---

## Public Interface

```
updateStatus()

loadProgress()

saveProgress()
```

---

## State Machine

```
todo

↓

ready

↓

in_progress

↓

review

↓

passed

↓

done
```

Failure states

```
retrying

blocked

failed
```

---

# 14. Execution Engine

## Responsibility

Coordinate all components.

The Execution Engine owns the workflow.

---

## Public Interface

```
execute()
```

---

## Workflow

```
Load Project

↓

Select Story

↓

Build Context

↓

Generate Code

↓

Build

↓

Repair

↓

Validate

↓

Review

↓

Commit

↓

Update Progress
```

---

## Dependencies

All orchestration components.

---

## Rules

- One story at a time
- Deterministic execution
- Sequential workflow
- No component bypass

---

# 15. Task Generator

## Responsibility

Generate `docs/TASKS.md` from a repository's planning documents.

Invoked directly via the CLI (`ado tasks generate`), independently of the Execution Engine — it
runs once, before a backlog exists, rather than as part of the per-story workflow.

---

## Inputs

- AI Engineering Spec
- Product Requirements
- System Architecture

---

## Outputs

```
docs/TASKS.md
```

---

## Public Interface

```
generate(repositoryPath, force, regenerate)
```

---

## Workflow

```
Load Planning Documents

↓

Hash Documents

↓

Cache Hit?

┌───────────────┐
│               │
▼               ▼

Yes             No
│               │
│        Invoke Agent (read-only)
│               │
│               ▼
│        Validate Against Backlog Grammar
│               │
│               ▼
│        Save To Cache
│               │
└───────────────┘

↓

Write docs/TASKS.md (unless content already matches what's on disk)
```

---

## Caching

Generation is keyed by a hash of the three input documents.

Unchanged documents reproduce the exact same output without invoking the agent again; only a
document change or an explicit regenerate request triggers a fresh invocation.

---

## Dependencies

- Documentation Loader

---

## Error Handling

Throws

```
TaskGenerationException
```

when a required document is missing, the agent cannot be invoked, the generated backlog fails to
parse, or an existing backlog with different content would be overwritten without an explicit
force flag.

---

# 16. Shared Models

Core models include

```
Story

StorySelection

ContextPackage

GenerationResult

BuildResult

FailureAnalysis

QualityGateResult

ReviewResult

CommitResult

ProgressState

Configuration

TaskGenerationResult

TaskGenerationCache
```

These models are immutable wherever possible.

---

# 17. Error Model

Each component returns structured errors.

Examples

```
ConfigurationException

BuildFailureException

ContextBuildException

AgentInvocationException

QualityGateException

ReviewFailureException

GitOperationException

TaskGenerationException
```

Components never terminate the workflow directly.

Errors are returned to the Execution Engine.

---

# 18. Dependency Graph

```
CLI

↓

Execution Engine

↓

Story Planner

↓

Context Builder

↓

Agent Adapter

↓

Build Executor

↓

Failure Analyzer

↓

Repair Loop

↓

Quality Gates

↓

Code Review

↓

Git Manager

↓

Progress Tracker
```

Dependencies always flow downward.

The Task Generator sits outside this chain: it depends only on the Documentation Loader and is
invoked directly by the CLI.

---

# 19. Extension Strategy

The architecture supports future extensions through interfaces.

Future components may include:

- Security Scanner
- Multi-Agent Coordinator
- Metrics Engine
- Portfolio Planner
- AIReady Integration

These extensions should not require changes to the Execution Engine.

---

# 20. Design Principles

Every component shall:

- own a single responsibility
- expose a small public interface
- hide implementation details
- communicate using shared models
- remain independently testable
- avoid vendor-specific dependencies

---

# 21. Summary

The component design decomposes ADO into small, focused, and independently testable modules coordinated by the Execution Engine.

This design ensures that the MVP remains deterministic, maintainable, and vendor-neutral while providing a clear implementation blueprint for AI coding agents.