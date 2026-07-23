# AI Development Orchestrator (ADO)

# Execution Engine Design

**Document:** 06-execution-engine.md  
**Version:** 0.1  
**Status:** Draft  
**Product:** AI Development Orchestrator (ADO)

---

# 1. Purpose

The Execution Engine is the central coordinator of the AI Development Orchestrator (ADO).

It is responsible for managing the complete lifecycle of story execution by coordinating all system components.

The Execution Engine never generates code itself. Instead, it orchestrates specialized components that perform planning, context assembly, AI invocation, validation, repair, review, and progress tracking.

---

# 2. Responsibilities

The Execution Engine is responsible for:

- Loading project configuration
- Loading execution progress
- Selecting executable stories
- Coordinating component execution
- Managing workflow state
- Handling failures
- Coordinating repair attempts
- Executing quality gates
- Updating execution progress
- Determining when execution is complete

---

# 3. Architecture

```
                    CLI

                     │

                     ▼

             Execution Engine

                     │

     ┌───────────────┼────────────────┐

     ▼               ▼                ▼

Story Planner   Context Builder   Progress Tracker

                     │

                     ▼

              Agent Adapter

                     │

                     ▼

             Build Executor

                     │

                     ▼

           Failure Analyzer

                     │

                     ▼

              Repair Loop

                     │

                     ▼

          Quality Gate Engine

                     │

                     ▼

           Code Review Agent

                     │

                     ▼

               Git Manager
```

The Execution Engine owns the orchestration workflow.

---

# 4. Execution Lifecycle

Every execution follows the same lifecycle.

```
Initialize

↓

Load Project

↓

Select Story

↓

Build Context

↓

Generate Code

↓

Apply Changes

↓

Build

↓

Repair (if needed)

↓

Quality Gates

↓

Code Review

↓

Commit

↓

Update Progress

↓

Next Story

↓

Complete
```

---

# 5. Story Lifecycle

A story transitions through the following states.

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

Failure states:

```
retrying

blocked

failed
```

Only one story may be in the `in_progress` state at any time.

---

# 6. Execution State Machine

```
                Start
                  │
                  ▼
          Load Configuration
                  │
                  ▼
          Load Progress State
                  │
                  ▼
       Select Executable Story
                  │
         ┌────────┴────────┐
         │                 │
         ▼                 ▼
      Story Found      No Story
         │                 │
         ▼                 ▼
   Execute Story        Complete
         │
         ▼
   Build Successful?
     │           │
    Yes         No
     │           ▼
     │     Repair Loop
     │           │
     └───────────┘
         │
         ▼
 Quality Gates Pass?
     │           │
    Yes         No
     │           ▼
     │       Block Story
     │
     ▼
 AI Review Pass?
     │           │
    Yes         No
     │           ▼
     │       Block Story
     │
     ▼
 Commit Changes
     │
     ▼
 Update Progress
     │
     ▼
 Select Next Story
```

---

# 7. Execution Algorithm

High-level execution flow:

```
initialize()

while executable stories exist

    select next story

    build context

    invoke coding agent

    apply generated changes

    execute build

    repair until successful

    execute quality gates

    perform AI review

    commit changes

    update progress

end
```

Execution terminates only when:

- all stories are complete
- no executable stories remain
- a blocking failure occurs

---

# 8. Story Execution Workflow

For each story, the Execution Engine performs:

### Step 1

Select story

↓

### Step 2

Update state

```
in_progress
```

↓

### Step 3

Build context package

↓

### Step 4

Invoke AI agent

↓

### Step 5

Apply generated changes

↓

### Step 6

Execute build

↓

### Step 7

Repair if required

↓

### Step 8

Execute quality gates

↓

### Step 9

Run AI review

↓

### Step 10

Commit implementation

↓

### Step 11

Update status

```
done
```

---

# 9. Repair Workflow

The Repair Loop is controlled entirely by the Execution Engine.

```
Build

↓

Failure

↓

Analyze

↓

Generate Repair Context

↓

Invoke Agent

↓

Apply Fix

↓

Build Again
```

The loop repeats until:

- build succeeds
- retry limit reached

---

# 10. Retry Policy

Default configuration:

```yaml
repair:

  retries: 5
```

After the maximum number of retries:

```
status = blocked
```

Execution stops.

Human intervention is required.

---

# 11. Component Sequence

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

Components execute sequentially.

No parallel execution occurs in the MVP.

---

# 12. Progress Updates

Progress is updated after every significant transition.

Examples:

```
todo

↓

in_progress
```

```
in_progress

↓

retrying
```

```
retrying

↓

review
```

```
review

↓

done
```

Progress updates are persisted immediately.

---

# 13. Error Handling

The Execution Engine coordinates all error handling.

Recoverable errors:

- compilation failure
- test failure
- formatting issue
- dependency issue

Non-recoverable errors:

- configuration error
- corrupted repository
- missing documentation
- unsupported build system

Recoverable errors invoke the Repair Loop.

Non-recoverable errors terminate execution.

---

# 14. Resume Capability

Execution can resume after interruption.

On startup:

1. Load progress state
2. Detect in-progress story
3. Resume execution from that story

No completed stories are reprocessed.

---

# 15. Cancellation

Execution may be cancelled safely between workflow stages.

Cancellation points:

- after build
- after repair
- after review
- after commit

Cancellation never leaves progress in an inconsistent state.

---

# 16. Logging

The Execution Engine records:

- execution start
- selected story
- build duration
- repair attempts
- validation results
- review results
- commit information
- execution summary

Logs are written to:

```
.ado/runtime/logs/
```

---

# 17. Execution Context

The Execution Engine maintains a lightweight execution context.

Contents include:

- current story
- retry count
- configuration
- progress state
- repository path
- active context package

This context exists only during execution.

---

# 18. Design Constraints

The Execution Engine shall:

- execute one story at a time
- remain deterministic
- never bypass quality gates
- never bypass code review
- never modify workflow order
- remain independent of AI vendors

All implementation decisions remain delegated to other components.

---

# 19. Extension Points

Future versions may extend the Execution Engine with:

- parallel story execution
- multiple AI agents
- distributed execution
- workflow plugins
- event bus
- scheduling policies

The MVP intentionally excludes these capabilities.

---

# 20. Summary

The Execution Engine is the orchestration core of ADO. It coordinates the complete software delivery lifecycle by executing stories sequentially, enforcing quality gates, managing repair loops, and maintaining execution progress.

Its deterministic workflow, strict state management, and component-based orchestration provide the foundation for autonomous, production-quality software development while preserving a minimal and extensible MVP architecture.