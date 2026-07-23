# AI Development Orchestrator (ADO)

# Context Engine Design

**Document:** 07-context-engine.md  
**Version:** 0.1  
**Status:** Draft  
**Product:** AI Development Orchestrator (ADO)

---

# 1. Purpose

The Context Engine is responsible for assembling the minimal, complete, and deterministic context required for implementing a single story.

Instead of sending the entire repository to an AI coding agent, the Context Engine identifies only the documentation, source code, and project artifacts that are relevant to the selected story.

Its primary objectives are:

- Minimize token usage
- Preserve implementation correctness
- Reduce irrelevant information
- Produce deterministic context packages
- Remain independent of AI providers

---

# 2. Design Principles

The Context Engine follows these principles:

1. Only include information required for the current story.
2. Documentation is the primary source of truth.
3. Prefer precision over completeness.
4. Context must be reproducible.
5. Source code supplements documentation.
6. Entire repositories are never included unless explicitly required.

---

# 3. High-Level Architecture

```
Story Selection
       │
       ▼
Context Engine
       │
 ┌─────┼────────────────────────────┐
 ▼     ▼            ▼              ▼
Documentation   Source Code   Dependencies   Standards
       │
       ▼
Context Package
       │
       ▼
Agent Adapter
```

---

# 4. Responsibilities

The Context Engine is responsible for:

- Loading project documentation
- Discovering impacted modules
- Identifying related source files
- Collecting architecture constraints
- Gathering coding standards
- Resolving story dependencies
- Building the Context Package

The Context Engine never invokes AI agents directly.

---

# 5. Context Sources

The Context Engine assembles information from the following sources in priority order:

1. Story Specification
2. Product Requirements
3. System Architecture
4. Architecture Decision Records (ADRs)
5. Coding Standards
6. Existing Source Code
7. Related Tests
8. Repository Metadata

Higher-priority sources override lower-priority information.

---

# 6. Context Assembly Workflow

```
Load Story
      │
      ▼
Load Documentation
      │
      ▼
Identify Impacted Modules
      │
      ▼
Locate Related Source Files
      │
      ▼
Collect Architecture Rules
      │
      ▼
Collect Related Tests
      │
      ▼
Assemble Context Package
```

---

# 7. Story Analysis

For each selected story, the Context Engine determines:

- Functional requirements
- Acceptance criteria
- Impacted modules
- Related documentation
- Existing implementation
- Required tests
- Architectural constraints

This analysis drives context selection.

---

# 8. Repository Analysis

The Context Engine scans the repository to identify:

- Impacted directories
- Existing implementations
- Interfaces
- Models
- Configuration files
- Build files
- Test suites

Repository scanning is limited to files relevant to the active story.

---

# 9. Documentation Selection

The following documents may be included:

- PRD
- System Architecture
- ADRs
- Component Design
- Coding Standards
- API Specifications
- Story Specification

Only documents referenced by the current story are selected.

---

# 10. Source Code Selection

The Context Engine identifies:

- Existing classes
- Interfaces
- Related services
- Models
- Configuration
- Utility classes

Selection is based on dependency and impact analysis.

---

# 11. Test Selection

Relevant tests include:

- Existing unit tests
- Integration tests
- Test fixtures
- Acceptance tests

Tests unrelated to the current story are excluded.

---

# 12. Context Package Structure

```
Context Package

├── Story
├── Acceptance Criteria
├── PRD Excerpts
├── Architecture Rules
├── ADR References
├── Coding Standards
├── Impacted Source Files
├── Related Tests
├── Existing Implementation
└── Expected Deliverables
```

The package is self-contained and sufficient for implementing one story.

---

# 13. Context Prioritization

When the context exceeds practical limits, information is prioritized as follows:

1. Story Specification
2. Acceptance Criteria
3. Architecture Constraints
4. Existing Implementation
5. Related Tests
6. Coding Standards
7. Supporting Documentation

Lower-priority information may be omitted if necessary.

---

# 14. Context Reduction Strategy

The Context Engine reduces unnecessary information by:

- Excluding unrelated modules
- Removing completed stories
- Ignoring unused documentation
- Limiting source files to impacted areas
- Avoiding duplicate content

The goal is to minimize tokens without losing implementation accuracy.

---

# 15. Context Package Contract

Every Context Package contains:

- Current story only
- Current acceptance criteria
- Relevant documentation only
- Relevant source code only
- Required architecture constraints
- Required coding standards
- Required tests

Future stories must never be included.

---

# 16. Dependency Resolution

The Context Engine resolves:

- Story dependencies
- Module dependencies
- Source code references
- Test dependencies

Only resolved dependencies required for implementation are included.

---

# 17. Context Caching

To improve performance, the Context Engine may cache:

- Parsed documentation
- Repository metadata
- Source file indexes
- Dependency graphs

Caches are invalidated when project files change.

---

# 18. Error Handling

The Context Engine reports structured errors such as:

- Missing story
- Missing documentation
- Missing architecture
- Invalid repository
- Circular dependency
- Context assembly failure

Errors are returned to the Execution Engine.

---

# 19. Extension Points

Future enhancements may include:

- Semantic code search
- Dependency graph analysis
- Repository indexing
- Symbol-level impact analysis
- AI-assisted context ranking
- Multi-repository context
- Incremental context updates

These capabilities are outside the MVP scope.

---

# 20. Summary

The Context Engine is responsible for transforming project knowledge into an implementation-ready Context Package for a single story.

By selecting only the relevant documentation, source code, architecture constraints, and tests, it minimizes token usage while preserving correctness. This focused context enables AI coding agents to produce deterministic, reviewable, and high-quality implementations without requiring the entire repository.