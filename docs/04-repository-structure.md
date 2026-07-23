# AI Development Orchestrator (ADO)
# Repository Structure

**Document:** 04-repository-structure.md  
**Version:** 0.1  
**Status:** Draft  
**Product:** AI Development Orchestrator (ADO)

---

# 1. Purpose

This document defines the standard repository structure for the AI Development Orchestrator (ADO).

The repository is organized around business capabilities rather than technical layers. Each major component of the orchestration engine owns its implementation, tests, and supporting models.

The goals are:

- predictable project organization
- high cohesion
- low coupling
- clear ownership
- AI-friendly navigation
- incremental extensibility

---

# 2. Repository Layout

```
ado/

├── docs/
│
├── .ado/
│   ├── config.yaml
│   ├── progress.yaml
│   └── runtime/
│
├── src/
│
│   ├── cli/
│   │
│   ├── core/
│   │
│   │   ├── planner/
│   │   ├── context/
│   │   ├── agent/
│   │   ├── execution/
│   │   ├── build/
│   │   ├── repair/
│   │   ├── validation/
│   │   ├── review/
│   │   ├── git/
│   │   ├── progress/
│   │   ├── configuration/
│   │   └── common/
│   │
│   ├── adapters/
│   │
│   │   ├── claude/
│   │   ├── codex/
│   │   ├── gemini/
│   │   └── devin/
│   │
│   ├── models/
│   │
│   └── utils/
│
├── tests/
│
├── scripts/
│
├── examples/
│
├── build/
│
└── README.md
```

---

# 3. Documentation

All project documentation resides in the `docs/` directory.

```
docs/

01-vision.md

02-product-requirements.md

03-system-architecture.md

04-repository-structure.md

05-component-design.md

06-execution-engine.md

07-context-engine.md

08-agent-contract.md

09-build-validation.md

10-cli-specification.md

11-mvp-roadmap.md

12-task-backlog.md
```

Documentation is the primary source of truth for the orchestrator.

---

# 4. Runtime Directory

ADO maintains runtime state separately from source code.

```
.ado/

config.yaml

progress.yaml

runtime/

    logs/

    cache/

    sessions/
```

Purpose:

- execution progress
- temporary files
- logs
- runtime metadata

These files are generated during execution.

---

# 5. Source Code Organization

All production code resides under `src/`.

```
src/

cli/

core/

adapters/

models/

utils/
```

Each directory has a single responsibility.

---

# 6. CLI Module

```
src/cli/
```

Responsibilities:

- command parsing
- command execution
- terminal output
- argument validation

The CLI contains no orchestration logic.

---

# 7. Core Module

```
src/core/
```

Contains the orchestration engine.

Submodules:

```
planner/

context/

agent/

execution/

build/

repair/

validation/

review/

git/

progress/

configuration/

common/
```

All business logic belongs here.

---

# 8. Planner Module

```
core/planner/
```

Responsibilities:

- load stories
- evaluate dependencies
- determine next executable story

Produces:

- Story Selection

---

# 9. Context Module

```
core/context/
```

Responsibilities:

- documentation loading
- source code discovery
- dependency lookup
- context assembly

Produces:

- Story Context Package

---

# 10. Agent Module

```
core/agent/
```

Responsibilities:

- invoke AI agent
- prompt construction
- response parsing
- execution coordination

Contains only vendor-independent logic.

---

# 11. Execution Module

```
core/execution/
```

Responsibilities:

- workflow orchestration
- execution lifecycle
- state transitions
- retry coordination

This module acts as the heart of ADO.

---

# 12. Build Module

```
core/build/
```

Responsibilities:

- execute builds
- execute tests
- collect outputs
- build result generation

No build interpretation occurs here.

---

# 13. Repair Module

```
core/repair/
```

Responsibilities:

- failure repair
- retry management
- repair request generation

Coordinates the repair loop.

---

# 14. Validation Module

```
core/validation/
```

Responsibilities:

- formatting
- static analysis
- architecture validation
- quality gates

Only validates implementations.

---

# 15. Review Module

```
core/review/
```

Responsibilities:

- AI code review
- review result generation
- blocking issue detection

---

# 16. Git Module

```
core/git/
```

Responsibilities:

- commits
- branch operations
- repository status

---

# 17. Progress Module

```
core/progress/
```

Responsibilities:

- story status
- execution persistence
- progress summaries

---

# 18. Configuration Module

```
core/configuration/
```

Responsibilities:

- load YAML configuration
- validate configuration
- expose configuration model

---

# 19. Common Module

```
core/common/
```

Shared functionality:

- interfaces
- constants
- shared models
- exceptions

Business logic should not accumulate here.

---

# 20. Adapter Module

```
src/adapters/
```

One adapter per supported AI provider.

```
claude/

codex/

gemini/

devin/
```

Each adapter implements the same interface.

No adapter depends on another adapter.

---

# 21. Models

```
src/models/
```

Contains shared domain models.

Examples:

- Story
- BuildResult
- ContextPackage
- ReviewResult
- ProgressState
- Configuration

Models should remain immutable whenever possible.

---

# 22. Utilities

```
src/utils/
```

Contains reusable helper functions.

Examples:

- file utilities
- process utilities
- YAML helpers
- logging

Utilities must not contain orchestration logic.

---

# 23. Tests

```
tests/

unit/

integration/

fixtures/
```

Test organization mirrors production code.

```
tests/

unit/planner/

unit/context/

unit/build/

integration/
```

---

# 24. Scripts

```
scripts/
```

Contains development scripts.

Examples:

- build
- release
- formatting
- documentation generation

Scripts are not part of runtime execution.

---

# 25. Examples

```
examples/
```

Contains sample projects.

Examples:

- sample configuration
- sample documentation
- demo repositories

Useful for testing and onboarding.

---

# 26. Dependency Rules

Dependencies flow inward.

```
CLI

↓

Core

↓

Adapters

↓

External Systems
```

Rules:

- CLI may call Core
- Core may call Adapters
- Adapters may call external tools
- Core never depends on CLI
- Core never depends on specific AI vendors
- Utilities may not depend on business modules

---

# 27. Naming Conventions

Directories use lowercase.

```
planner/

context/

review/
```

Classes use PascalCase.

```
StoryPlanner

BuildExecutor

RepairLoop
```

Interfaces begin with "I" only if required by the implementation language.

Configuration files use kebab-case.

```
config.yaml

progress.yaml
```

---

# 28. Generated Files

The following files are generated during execution:

```
.ado/progress.yaml

.ado/runtime/

logs/

cache/

sessions/
```

Generated files should never contain source code.

---

# 29. Repository Principles

The repository follows these principles:

- documentation first
- business capability organization
- minimal coupling
- explicit dependencies
- deterministic layout
- AI-friendly navigation
- vendor neutrality
- incremental extensibility

Every source file should have a clearly defined responsibility.

---

# 30. Summary

The ADO repository is organized around orchestration capabilities rather than technical layers. Each module owns a single business responsibility, allowing AI coding agents to navigate, modify, and extend the project predictably.

This structure provides a stable foundation for autonomous development while remaining intentionally minimal for the MVP.