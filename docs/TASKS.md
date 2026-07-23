AI Development Orchestrator (ADO)

Implementation Task Backlog

Document: TASKS.md
Version: 0.1
Status: Active

⸻

1. Purpose

This document defines the atomic implementation tasks required to build the AI Development Orchestrator MVP.

Tasks are designed for autonomous execution by AI coding agents.

Each task must:

* implement one focused change
* satisfy all dependencies
* keep the repository buildable
* include required tests
* follow project documentation
* avoid future-task implementation
* produce one focused commit

⸻

2. Task Status

Supported statuses:

todo
in_progress
blocked
done

Only one task may be in_progress.

⸻

3. Task Selection Rules

The next executable task is the first task where:

1. status is todo
2. every task in depends_on is done
3. no earlier executable task exists

Tasks must be executed in documented order when multiple tasks are executable.

⸻

4. Task Completion Rules

A task may be marked done only when:

* acceptance criteria are satisfied
* required tests exist
* tests pass
* project builds successfully
* no unrelated code is modified
* no future task is implemented

⸻

Epic 1 — Repository Bootstrap

ADO-001 — Initialize Gradle Kotlin Project

Status: done

Depends On: None

Description

Initialize the ADO repository as a Gradle Kotlin DSL project.

Files

settings.gradle.kts
build.gradle.kts
gradle.properties

Acceptance Criteria

* Kotlin project is configured.
* Java 21 is configured.
* Gradle Kotlin DSL is used.
* Project builds successfully.

Tests

Run:

./gradlew build

⸻

ADO-002 — Configure Kotlin Dependencies

Status: done

Depends On: ADO-001

Description

Configure the dependencies required by the approved MVP technology stack.

Required Dependencies

* Clikt
* kotlinx.serialization
* Kotlin Logging
* SLF4J
* JUnit 5
* MockK

Acceptance Criteria

* Required dependencies are configured.
* Dependency resolution succeeds.
* Project builds.

⸻

ADO-003 — Configure JUnit 5

Status: done

Depends On: ADO-002

Description

Configure Gradle to execute tests using JUnit 5.

Acceptance Criteria

* JUnit Platform is enabled.
* Test task executes successfully.

⸻

ADO-004 — Create Production Source Structure

Status: done

Depends On: ADO-001

Description

Create the production source structure defined in docs/04-repository-structure.md.

Required Packages

src/main/kotlin/
cli/
core/planner/
core/context/
core/agent/
core/execution/
core/build/
core/repair/
core/validation/
core/review/
core/git/
core/progress/
core/configuration/
core/common/
adapters/claude/
adapters/codex/
adapters/gemini/
adapters/devin/
models/
utils/

Acceptance Criteria

* Repository structure matches the documented architecture.
* No business logic is added.

⸻

ADO-005 — Create Test Source Structure

Status: done

Depends On: ADO-003

Description

Create the documented test directory structure.

Required Structure

src/test/kotlin/
unit/
integration/

Create test packages matching production capabilities as required.

Acceptance Criteria

* Test source set is recognized by Gradle.
* Tests execute successfully.

⸻

ADO-006 — Create Runtime Directory Structure

Status: done

Depends On: ADO-001

Description

Create the .ado runtime structure.

Structure

.ado/
config.yaml
progress.yaml
runtime/
runtime/logs/
runtime/cache/
runtime/sessions/

Acceptance Criteria

* Required directories exist.
* Configuration files contain minimal valid placeholders.
* No runtime logic is implemented.

⸻

ADO-007 — Configure Git Ignore Rules

Status: done

Depends On: ADO-006

Description

Create .gitignore.

Acceptance Criteria

Ignore:

* Gradle build output
* IDE metadata
* runtime logs
* runtime cache
* runtime sessions

Do not ignore project documentation.

⸻

ADO-008 — Create Root CLI Command

Status: done

Depends On: ADO-002, ADO-004

Description

Create the root ado CLI command using Clikt.

Files

src/main/kotlin/cli/AdoCommand.kt

Acceptance Criteria

* Root command executes.
* Clikt is used.
* No orchestration logic exists.

Tests

Add a root command test.

⸻

ADO-009 — Create Version Command

Status: done

Depends On: ADO-008

Description

Implement the CLI version command.

Expected Behavior

ado version

prints the ADO version.

Acceptance Criteria

* Version command executes.
* Output contains the current version.
* Unit test exists.

⸻

ADO-010 — Create Run Command

Status: done

Depends On: ADO-008

Description

Create the initial run command.

Expected Output

AI Development Orchestrator MVP

Acceptance Criteria

* ado run executes.
* Exact MVP message is printed.
* No orchestration logic is implemented.
* Unit test exists.

⸻

ADO-011 — Configure Executable Application

Status: done

Depends On: ADO-008, ADO-009, ADO-010

Description

Configure Gradle application execution.

Acceptance Criteria

The application can be executed through Gradle.

The root CLI and subcommands are available.

⸻

ADO-012 — Configure Logging

Status: done

Depends On: ADO-002

Description

Configure Kotlin Logging and SLF4J.

Acceptance Criteria

* Logging initializes successfully.
* No custom logging abstraction is implemented.
* Application starts without logging errors.

⸻

ADO-013 — Create README

Status: done

Depends On: ADO-011

Description

Create the project README.

Required Sections

* Project Overview
* Build Instructions
* Run Instructions
* Repository Layout

Constraints

Do not document future capabilities as implemented features.

⸻

ADO-014 — Validate Repository Bootstrap

Status: done

Depends On: ADO-001 through ADO-013

Description

Validate the complete repository bootstrap.

Acceptance Criteria

./gradlew clean build

passes.

All tests pass.

CLI executes.

Repository structure matches documentation.

No ADO business logic exists.

⸻

Epic 2 — Foundation

ADO-015 — Create StoryStatus Model

Status: done

Depends On: ADO-014

Description

Create the story status model.

Statuses

todo
ready
in_progress
review
retrying
blocked
failed
passed
done

Acceptance Criteria

* All documented states exist.
* Model is immutable.
* Unit tests exist.

⸻

ADO-016 — Create Story Model

Status: done

Depends On: ADO-015

Description

Create the immutable Story domain model.

Required Data

* ID
* title
* description
* status
* dependencies
* acceptance criteria

Acceptance Criteria

* Story is immutable.
* Dependencies can be represented.
* Acceptance criteria can be represented.
* Unit tests exist.

⸻

ADO-017 — Create StorySelection Model

Status: done

Depends On: ADO-016

Description

Create the StorySelection model.

Acceptance Criteria

* Selected Story can be represented.
* No planner logic exists.
* Unit tests exist.

⸻

ADO-018 — Create ContextPackage Model

Status: done

Depends On: ADO-016

Description

Create the ContextPackage model based on docs/07-context-engine.md.

Required Data

* story
* acceptance criteria
* PRD excerpts
* architecture rules
* ADR references
* coding standards
* impacted source files
* related tests
* existing implementation
* expected deliverables

Acceptance Criteria

* Model is immutable.
* All documented context sections are represented.
* Unit tests exist.

⸻

ADO-019 — Create GenerationResult Model

Status: done

Depends On: ADO-014

Description

Create the GenerationResult model.

Required Data

* modified files
* summary
* implementation notes

Acceptance Criteria

* Model is immutable.
* Unit tests exist.

⸻

ADO-020 — Create BuildResult Model

Status: done

Depends On: ADO-014

Description

Create the BuildResult model.

Required Data

* status
* stdout
* stderr
* warnings
* duration

Acceptance Criteria

* Successful and failed build results can be represented.
* Unit tests exist.

⸻

ADO-021 — Create FailureAnalysis Model

Status: done

Depends On: ADO-020

Description

Create the FailureAnalysis model.

Failure Categories

* compilation
* testing
* dependency
* formatting
* architecture

Acceptance Criteria

* Failure category is represented.
* Relevant failure details are represented.
* Unit tests exist.

⸻

ADO-022 — Create QualityGateResult Model

Status: done

Depends On: ADO-014

Description

Create the QualityGateResult model.

Acceptance Criteria

* Gate status is represented.
* Failure details can be represented.
* Unit tests exist.

⸻

ADO-023 — Create ReviewResult Model

Status: done

Depends On: ADO-014

Description

Create the ReviewResult model.

Required Data

* blocking issues
* recommendations

Acceptance Criteria

* Blocking review issues can be represented.
* Recommendations can be represented.
* Unit tests exist.

⸻

ADO-024 — Create CommitResult Model

Status: done

Depends On: ADO-014

Description

Create the CommitResult model.

Acceptance Criteria

* Commit result can represent success.
* Commit identifier can be represented.
* Failure information can be represented.
* Unit tests exist.

⸻

ADO-025 — Create ProgressState Model

Status: done

Depends On: ADO-015, ADO-016

Description

Create the ProgressState model.

Acceptance Criteria

* Story statuses can be stored.
* State is immutable.
* Unit tests exist.

⸻

ADO-026 — Create Configuration Model

Status: done

Depends On: ADO-014

Description

Create the configuration model matching the documented YAML configuration.

Required Sections

agent
build
test
review
repair

Acceptance Criteria

* Documented configuration can be represented.
* Repair retry configuration is supported.
* Unit tests exist.

⸻

ADO-027 — Create Exception Hierarchy

Status: done

Depends On: ADO-014

Description

Create documented ADO exceptions.

Exceptions

ConfigurationException
BuildFailureException
ContextBuildException
AgentInvocationException
QualityGateException
ReviewFailureException
GitOperationException

Acceptance Criteria

* Exceptions exist.
* Exceptions provide meaningful messages.
* No component logic is added.

⸻

ADO-028 — Create File Utility

Status: done

Depends On: ADO-014

Description

Create minimal reusable file operations required by ADO.

Required Operations

* read file
* write file
* check file existence

Acceptance Criteria

* Operations use explicit paths.
* Errors are surfaced.
* Unit tests exist.

⸻

ADO-029 — Create Process Execution Model

Status: done

Depends On: ADO-014

Description

Create a model representing external process execution results.

Required Data

* exit code
* stdout
* stderr
* duration

Acceptance Criteria

* Model is immutable.
* Unit tests exist.

⸻

ADO-030 — Create Process Executor

Status: done

Depends On: ADO-029

Description

Create the process execution abstraction using Kotlin process APIs.

Required Behavior

* execute command
* capture stdout
* capture stderr
* capture exit code
* capture duration

Acceptance Criteria

* Successful commands are supported.
* Failed commands are supported.
* Unit tests exist.

⸻

ADO-031 — Implement Configuration Loader

Status: done

Depends On: ADO-026, ADO-027, ADO-028

Description

Load .ado/config.yaml.

Acceptance Criteria

* Valid configuration loads.
* Missing configuration produces ConfigurationException.
* Invalid configuration produces ConfigurationException.
* Unit tests exist.

⸻

ADO-032 — Validate Foundation

Status: done

Depends On: ADO-015 through ADO-031

Description

Validate foundation implementation.

Acceptance Criteria

* All foundation tests pass.
* Build passes.
* Models match documented contracts.
* No orchestration logic exists.

⸻

Epic 3 — Story Management

ADO-033 — Define Story Loader Interface

Status: done

Depends On: ADO-032

Description

Define the contract for loading stories from TASKS.md.

Acceptance Criteria

* Interface returns stories.
* Interface contains no parsing implementation.
* Interface is independently testable.

⸻

ADO-034 — Implement TASKS.md File Loader

Status: done

Depends On: ADO-028, ADO-033

Description

Load raw TASKS.md content.

Acceptance Criteria

* Explicit repository path is supported.
* Missing file is reported.
* File content is returned.
* Unit tests exist.

⸻

ADO-035 — Define Story Parser Interface

Status: done

Depends On: ADO-016

Description

Define the Story Parser contract.

Acceptance Criteria

* Parser accepts task document content.
* Parser returns Story models.
* No parsing implementation exists.

⸻

ADO-036 — Parse Story ID

Status: done

Depends On: ADO-035

Description

Implement Story ID extraction from task sections.

Acceptance Criteria

* ADO task IDs are extracted.
* Invalid task sections are rejected.
* Unit tests exist.

⸻

ADO-037 — Parse Story Title

Status: done

Depends On: ADO-036

Description

Extract the task title.

Acceptance Criteria

* Title is parsed correctly.
* Missing title is rejected.
* Unit tests exist.

⸻

ADO-038 — Parse Story Status

Status: done

Depends On: ADO-036, ADO-015

Description

Extract story status.

Acceptance Criteria

* Supported statuses parse correctly.
* Unsupported statuses are rejected.
* Unit tests exist.

⸻

ADO-039 — Parse Story Dependencies

Status: done

Depends On: ADO-036

Description

Extract Depends On values.

Acceptance Criteria

* Single dependencies are parsed.
* Multiple dependencies are parsed.
* None produces no dependencies.
* Unit tests exist.

⸻

ADO-040 — Parse Story Description

Status: done

Depends On: ADO-036

Description

Extract story description.

Acceptance Criteria

* Description content is preserved.
* Unit tests exist.

⸻

ADO-041 — Parse Acceptance Criteria

Status: done

Depends On: ADO-036

Description

Extract acceptance criteria.

Acceptance Criteria

* Multiple criteria are preserved.
* Criteria order is preserved.
* Unit tests exist.

⸻

ADO-042 — Assemble Story Model

Status: done

Depends On: ADO-037, ADO-038, ADO-039, ADO-040, ADO-041

Description

Assemble parsed task data into Story models.

Acceptance Criteria

* Complete Story model is produced.
* Invalid required data is rejected.
* Unit tests exist.

⸻

ADO-043 — Implement Story Parser

Status: done

Depends On: ADO-042

Description

Implement complete TASKS.md story parsing.

Acceptance Criteria

* Multiple stories are parsed.
* Story order is preserved.
* Unit tests use representative task fixtures.

⸻

ADO-044 — Define Progress Tracker Interface

Status: done

Depends On: ADO-025

Description

Define the Progress Tracker contract.

Operations

loadProgress
updateStatus
saveProgress

Acceptance Criteria

* Interface matches component design.
* No persistence implementation exists.

⸻

ADO-045 — Implement Progress YAML Loading

Status: done

Depends On: ADO-025, ADO-028, ADO-044

Description

Load .ado/progress.yaml.

Acceptance Criteria

* Story progress loads.
* Empty progress is supported.
* Invalid progress data is rejected.
* Unit tests exist.

⸻

ADO-046 — Implement Story Status Update

Status: done

Depends On: ADO-045

Description

Update a story status in ProgressState.

Acceptance Criteria

* Existing story status can be updated.
* Updated state is returned.
* Unit tests exist.

⸻

ADO-047 — Implement Progress YAML Persistence

Status: done

Depends On: ADO-046

Description

Persist ProgressState to .ado/progress.yaml.

Acceptance Criteria

* State is persisted.
* Persisted state can be loaded.
* Unit tests exist.

⸻

ADO-048 — Define Dependency Resolver Interface

Status: done

Depends On: ADO-016, ADO-025

Description

Define the story dependency resolution contract.

Acceptance Criteria

* Contract determines whether dependencies are satisfied.
* No dependency logic exists.

⸻

ADO-049 — Implement Dependency Resolution

Status: done

Depends On: ADO-048

Description

Determine whether all story dependencies are complete.

Acceptance Criteria

* Stories without dependencies are executable.
* done dependencies are satisfied.
* Incomplete dependencies prevent execution.
* Unit tests exist.

⸻

ADO-050 — Detect Missing Story Dependencies

Status: done

Depends On: ADO-049

Description

Detect references to story IDs that do not exist.

Acceptance Criteria

* Missing dependency IDs are detected.
* Invalid dependency graphs are rejected.
* Unit tests exist.

⸻

ADO-051 — Define Story Planner Interface

Status: done

Depends On: ADO-017

Description

Define the Story Planner contract.

Operation

selectNextStory

Acceptance Criteria

* Contract returns StorySelection or no executable story.
* No selection logic exists.

⸻

ADO-052 — Filter Completed Stories

Status: done

Depends On: ADO-051, ADO-025

Description

Exclude completed stories from selection.

Acceptance Criteria

* done stories are excluded.
* Unit tests exist.

⸻

ADO-053 — Filter Blocked Stories

Status: done

Depends On: ADO-051

Description

Exclude blocked stories.

Acceptance Criteria

* blocked stories are excluded.
* Unit tests exist.

⸻

ADO-054 — Filter Stories With Incomplete Dependencies

Status: done

Depends On: ADO-049, ADO-051

Description

Exclude stories whose dependencies are incomplete.

Acceptance Criteria

* Dependency resolver is used.
* Unit tests exist.

⸻

ADO-055 — Select First Executable Story

Status: done

Depends On: ADO-052, ADO-053, ADO-054

Description

Select the first executable story in documented order.

Acceptance Criteria

* Selection is deterministic.
* Story order is preserved.
* Exactly one story is selected.
* Unit tests exist.

⸻

ADO-056 — Handle No Executable Story

Status: done

Depends On: ADO-055

Description

Represent the condition where no executable story exists.

Acceptance Criteria

* No executable story is handled without selecting invalid work.
* Unit tests exist.

⸻

ADO-057 — Implement Story Planner

Status: done

Depends On: ADO-055, ADO-056

Description

Complete the Story Planner implementation.

Workflow

1. Load stories.
2. Remove completed stories.
3. Check dependencies.
4. Ignore blocked stories.
5. Select the first executable story.

Acceptance Criteria

* Workflow matches component design.
* Selection is deterministic.
* Unit tests exist.

⸻

ADO-058 — Add Story Planner Integration Test

Status: done

Depends On: ADO-034, ADO-043, ADO-047, ADO-057

Description

Test story selection using real task and progress fixtures.

Acceptance Criteria

* TASKS fixture is loaded.
* Progress fixture is loaded.
* Correct executable story is selected.

⸻

ADO-059 — Validate Story Management

Status: done

Depends On: ADO-033 through ADO-058

Description

Validate the complete Story Management phase.

Acceptance Criteria

* TASKS.md can be loaded.
* Stories can be parsed.
* Progress can be loaded and saved.
* Dependencies are evaluated.
* The next executable story is selected.
* Build passes.
* Tests pass.

⸻

Epic 4 — Context Engine

ADO-060 — Define Documentation Loader Interface

Status: done

Depends On: ADO-059

Description

Define the contract for loading project documentation.

Acceptance Criteria

* Documentation can be requested by path.
* No loading implementation exists.

⸻

ADO-061 — Implement Documentation Loader

Status: done

Depends On: ADO-028, ADO-060

Description

Load documentation from the repository.

Acceptance Criteria

* Existing documentation loads.
* Missing required documentation is reported.
* Unit tests exist.

⸻

ADO-062 — Define Repository Scanner Interface

Status: done

Depends On: ADO-059

Description

Define the repository scanning contract.

Acceptance Criteria

* Repository files can be discovered.
* No scanning implementation exists.

⸻

ADO-063 — Implement Repository File Discovery

Status: done

Depends On: ADO-062

Description

Discover files within an explicit repository path.

Acceptance Criteria

* Repository files are discovered.
* Runtime cache and build output are excluded.
* Unit tests exist.

⸻

ADO-064 — Discover Source Files

Status: done

Depends On: ADO-063

Description

Identify production source files.

Acceptance Criteria

* Production source files are returned.
* Test files are excluded.
* Unit tests exist.

⸻

ADO-065 — Discover Test Files

Status: done

Depends On: ADO-063

Description

Identify test source files.

Acceptance Criteria

* Test files are returned.
* Production files are excluded.
* Unit tests exist.

⸻

ADO-066 — Discover Project Documentation

Status: done

Depends On: ADO-063

Description

Identify project documentation.

Acceptance Criteria

* Documents under docs/ are discovered.
* TASKS.md is discoverable.
* Unit tests exist.

⸻

ADO-067 — Define Source Selector Interface

Status: done

Depends On: ADO-016

Description

Define the source selection contract.

Acceptance Criteria

* Story and repository files are accepted.
* Relevant file paths are returned.
* No selection implementation exists.

⸻

ADO-068 — Select Files Referenced by Story

Status: done

Depends On: ADO-067

Description

Select source files explicitly referenced by the active story.

Acceptance Criteria

* Explicitly referenced files are selected.
* Duplicate paths are removed.
* Unit tests exist.

⸻

ADO-069 — Select Impacted Module Files

Status: done

Depends On: ADO-067

Description

Select files from modules identified by the active story.

Acceptance Criteria

* Selection is limited to identified modules.
* Unrelated modules are excluded.
* Unit tests exist.

⸻

ADO-070 — Select Related Tests

Status: done

Depends On: ADO-065, ADO-067

Description

Select tests related to selected source files.

Acceptance Criteria

* Related tests are selected.
* Unrelated tests are excluded.
* Unit tests exist.

⸻

ADO-071 — Load Product Requirements Context

Status: done

Depends On: ADO-061

Description

Load relevant product requirements context.

Acceptance Criteria

* Product requirements document can be loaded.
* Content is represented for context assembly.
* Unit tests exist.

⸻

ADO-072 — Load Architecture Context

Status: done

Depends On: ADO-061

Description

Load relevant system architecture context.

Acceptance Criteria

* Architecture document can be loaded.
* Content is represented for context assembly.
* Unit tests exist.

⸻

ADO-073 — Load Component Design Context

Status: done

Depends On: ADO-061

Description

Load component design context.

Acceptance Criteria

* Component design can be loaded.
* Content is represented for context assembly.
* Unit tests exist.

⸻

ADO-074 — Define Context Builder Interface

Status: done

Depends On: ADO-018

Description

Define the Context Builder contract.

Operation

buildContext(story)

Acceptance Criteria

* Story input is accepted.
* ContextPackage is returned.
* No context assembly logic exists.

⸻

ADO-075 — Assemble Story Context

Status: done

Depends On: ADO-074

Description

Add the active story and acceptance criteria to ContextPackage assembly.

Acceptance Criteria

* Current story is included.
* Acceptance criteria are included.
* Future stories are not included.
* Unit tests exist.

⸻

ADO-076 — Assemble Documentation Context

Status: done

Depends On: ADO-071, ADO-072, ADO-073, ADO-075

Description

Add relevant documentation to context assembly.

Acceptance Criteria

* PRD context is included.
* Architecture rules are included.
* Relevant component design is included.
* Unit tests exist.

⸻

ADO-077 — Assemble Source Context

Status: done

Depends On: ADO-068, ADO-069, ADO-076

Description

Add selected source files and existing implementation to the context package.

Acceptance Criteria

* Selected source content is included.
* Duplicate source content is excluded.
* Unit tests exist.

⸻

ADO-078 — Assemble Test Context

Status: done

Depends On: ADO-070, ADO-077

Description

Add related tests to the context package.

Acceptance Criteria

* Related tests are included.
* Unrelated tests are excluded.
* Unit tests exist.

⸻

ADO-079 — Enforce Context Source Priority

Status: done

Depends On: ADO-078

Description

Apply documented context source priority.

Priority

1. Story Specification
2. Product Requirements
3. System Architecture
4. ADRs
5. Coding Standards
6. Existing Source Code
7. Related Tests
8. Repository Metadata

Acceptance Criteria

* Context sections follow documented priority.
* Unit tests exist.

⸻

ADO-080 — Remove Duplicate Context Content

Status: done

Depends On: ADO-079

Description

Remove duplicate context content during assembly.

Acceptance Criteria

* Duplicate files are excluded.
* Duplicate context entries are excluded.
* Unit tests exist.

⸻

ADO-081 — Implement Context Builder

Status: done

Depends On: ADO-075 through ADO-080

Description

Complete the deterministic Context Builder implementation.

Acceptance Criteria

* One story produces one ContextPackage.
* Only relevant documentation is included.
* Only selected source files are included.
* Related tests are included.
* Future stories are excluded.
* Unit tests exist.

⸻

ADO-082 — Add Context Builder Integration Test

Status: done

Depends On: ADO-061, ADO-063, ADO-081

Description

Build a ContextPackage from a repository fixture.

Acceptance Criteria

* Story is loaded.
* Documentation is loaded.
* Source files are discovered.
* Relevant files are selected.
* ContextPackage is produced.

⸻

ADO-083 — Validate Context Engine

Status: done

Depends On: ADO-060 through ADO-082

Description

Validate the complete Context Engine phase.

Acceptance Criteria

* ContextPackage can be built for one story.
* Context selection is deterministic.
* Unrelated source files are excluded.
* Future stories are excluded.
* Build passes.
* Tests pass.

⸻

Epic 5 — Agent Integration

ADO-084 — Define Agent Adapter Interface

Status: done

Depends On: ADO-083

Description

Define the vendor-neutral Agent Adapter contract.

Operation

generate(context)

Acceptance Criteria

* ContextPackage is accepted.
* GenerationResult is returned.
* No vendor dependency exists in the interface.

⸻

ADO-085 — Implement Prompt Builder

Status: done

Depends On: ADO-018, ADO-084

Description

Build an agent prompt from ContextPackage.

Agent Rules

* Implement exactly one story.
* Do not implement future stories.
* Do not refactor unrelated code.
* Follow architecture documents.
* Write tests with production code.
* Keep changes focused.

Acceptance Criteria

* Prompt contains current story.
* Prompt contains acceptance criteria.
* Prompt contains context.
* Agent rules are included.
* Unit tests exist.

⸻

ADO-086 — Define Claude Code Process Invocation

Status: done

Depends On: ADO-030, ADO-084

Description

Define Claude Code invocation through the process execution abstraction.

Acceptance Criteria

* Invocation uses Process Executor.
* Claude-specific behavior remains inside the Claude adapter.
* No Execution Engine logic exists.

⸻

ADO-087 — Implement Claude Code Adapter

Status: done

Depends On: ADO-085, ADO-086

Description

Implement the Claude Code Agent Adapter.

Acceptance Criteria

* ContextPackage is converted to a prompt.
* Claude Code is invoked.
* Invocation failures produce AgentInvocationException.
* Unit tests exist using process abstraction test doubles.

⸻

ADO-088 — Collect Claude Code Result

Status: done

Depends On: ADO-087

Description

Collect Claude Code execution output.

Acceptance Criteria

* Agent output is captured.
* Invocation status is captured.
* Unit tests exist.

⸻

ADO-089 — Produce GenerationResult

Status: done

Depends On: ADO-019, ADO-088

Description

Convert Claude Code invocation output into GenerationResult.

Acceptance Criteria

* Modified files are represented.
* Summary is represented.
* Implementation notes are represented.
* Unit tests exist.

⸻

ADO-090 — Validate Claude Adapter

Status: done

Depends On: ADO-084 through ADO-089

Description

Validate the Claude Code adapter.

Acceptance Criteria

* Adapter follows vendor-neutral interface.
* ContextPackage can be submitted.
* GenerationResult is produced.
* Build passes.
* Tests pass.

⸻

Epic 6 — Build Execution

ADO-091 — Define Build Executor Interface

Status: done

Depends On: ADO-090

Description

Define the Build Executor contract.

Operation

executeBuild()

Acceptance Criteria

* BuildResult is returned.
* No build interpretation exists.

⸻

ADO-092 — Load Build Command From Configuration

Status: done

Depends On: ADO-031, ADO-091

Description

Read the configured build command.

Acceptance Criteria

* Build command comes from configuration.
* Missing command is rejected.
* Unit tests exist.

⸻

ADO-093 — Execute Build Command

Status: done

Depends On: ADO-030, ADO-092

Description

Execute the configured project build command.

Acceptance Criteria

* Process Executor is used.
* Exit code is captured.
* stdout is captured.
* stderr is captured.
* Unit tests exist.

⸻

ADO-094 — Produce BuildResult

Status: done

Depends On: ADO-020, ADO-093

Description

Convert process execution output into BuildResult.

Acceptance Criteria

* Build status is represented.
* Output is preserved.
* Errors are preserved.
* Duration is preserved.
* Unit tests exist.

⸻

ADO-095 — Implement Build Executor

Status: done

Depends On: ADO-091 through ADO-094

Description

Complete the Build Executor.

Acceptance Criteria

* Configured builds execute.
* BuildResult is returned.
* Build failures are not interpreted.
* Unit tests exist.

⸻

ADO-096 — Add Build Executor Integration Test

Status: done

Depends On: ADO-095

Description

Execute a build against a project fixture.

Acceptance Criteria

* Successful build is represented.
* Failed build is represented.

⸻

Epic 7 — Failure Analysis and Repair

ADO-097 — Define Failure Analyzer Interface

Status: done

Depends On: ADO-096

Description

Define the Failure Analyzer contract.

Operation

analyze(buildResult)

⸻

ADO-098 — Detect Compilation Failure

Status: done

Depends On: ADO-097

Description

Categorize compilation failures.

Acceptance Criteria

* Compilation failures are identified.
* Relevant output is preserved.
* Unit tests exist.

⸻

ADO-099 — Detect Test Failure

Status: done

Depends On: ADO-097

Description

Categorize test failures.

Acceptance Criteria

* Test failures are identified.
* Relevant output is preserved.
* Unit tests exist.

⸻

ADO-100 — Detect Dependency Failure

Status: done

Depends On: ADO-097

Description

Categorize missing dependency failures.

Acceptance Criteria

* Dependency failures are identified.
* Unit tests exist.

⸻

ADO-101 — Detect Formatting Failure

Status: done

Depends On: ADO-097

Description

Categorize formatting failures.

Acceptance Criteria

* Formatting failures are identified.
* Unit tests exist.

⸻

ADO-102 — Detect Architecture Failure

Status: done

Depends On: ADO-097

Description

Categorize architecture validation failures.

Acceptance Criteria

* Architecture failures are identified.
* Unit tests exist.

⸻

ADO-103 — Implement Failure Analyzer

Status: done

Depends On: ADO-098 through ADO-102

Description

Produce structured FailureAnalysis from BuildResult.

Acceptance Criteria

* Documented failure categories are supported.
* Structured failure details are returned.
* Unit tests exist.

⸻

ADO-104 — Define Repair Loop Interface

Status: done

Depends On: ADO-103

Description

Define the Repair Loop contract.

⸻

ADO-105 — Build Repair Context

Status: done

Depends On: ADO-021, ADO-104

Description

Create focused repair context from FailureAnalysis.

Acceptance Criteria

* Failure category is included.
* Relevant failure details are included.
* Current story remains identified.
* Unit tests exist.

⸻

ADO-106 — Invoke Agent for Repair

Status: done

Depends On: ADO-087, ADO-105

Description

Invoke the configured Agent Adapter with repair context.

Acceptance Criteria

* Agent Adapter is used.
* Repair invocation is isolated from initial generation.
* Unit tests exist.

⸻

ADO-107 — Rebuild After Repair

Status: done

Depends On: ADO-095, ADO-106

Description

Execute the build after a repair attempt.

Acceptance Criteria

* Build Executor is used.
* New BuildResult is returned.
* Unit tests exist.

⸻

ADO-108 — Enforce Repair Retry Limit

Status: done

Depends On: ADO-026, ADO-107

Description

Enforce the configured repair retry limit.

Acceptance Criteria

* Retry count starts deterministically.
* Configured limit is respected.
* Unit tests exist.

⸻

ADO-109 — Implement Repair Loop

Status: done

Depends On: ADO-103, ADO-105 through ADO-108

Description

Implement the documented repair workflow.

Workflow

Build Failure
↓
Analyze
↓
Build Repair Context
↓
Invoke Agent
↓
Rebuild
↓
Repeat

Acceptance Criteria

* Loop stops when build succeeds.
* Loop stops when retry limit is reached.
* Unit tests exist.

⸻

Epic 8 — Quality Gates and Review

ADO-110 — Define Quality Gate Contract

Status: done

Depends On: ADO-109

Description

Define a common quality gate contract.

Acceptance Criteria

* Quality gate can execute.
* QualityGateResult is returned.

⸻

ADO-111 — Implement Build Quality Gate

Status: done

Depends On: ADO-095, ADO-110

Description

Implement the build quality gate.

⸻

ADO-112 — Implement Formatting Quality Gate

Status: done

Depends On: ADO-110

Description

Execute the configured formatting validation command.

⸻

ADO-113 — Implement Static Analysis Quality Gate

Status: done

Depends On: ADO-110

Description

Execute the configured static analysis command.

⸻

ADO-114 — Implement Unit Test Quality Gate

Status: done

Depends On: ADO-110

Description

Execute the configured unit test command.

⸻

ADO-115 — Implement Integration Test Quality Gate

Status: done

Depends On: ADO-110

Description

Execute the configured integration test command.

⸻

ADO-116 — Implement Architecture Validation Gate

Status: done

Depends On: ADO-110

Description

Execute the configured architecture validation command.

⸻

ADO-117 — Implement Quality Gate Engine

Status: done

Depends On: ADO-111 through ADO-116

Description

Execute all mandatory quality gates sequentially.

Acceptance Criteria

* Build runs.
* Formatting runs.
* Static analysis runs.
* Unit tests run.
* Integration tests run.
* Architecture validation runs.
* Failed gates prevent successful completion.

⸻

ADO-118 — Define Code Review Agent Interface

Status: done

Depends On: ADO-023

Description

Define the AI code review contract.

⸻

ADO-119 — Build Code Review Prompt

Status: done

Depends On: ADO-118

Description

Create the independent AI review prompt.

Checks

* Architecture compliance
* SOLID principles
* Naming
* Readability
* Performance
* Thread safety
* Test coverage

⸻

ADO-120 — Implement Code Review Agent

Status: done

Depends On: ADO-087, ADO-119

Description

Invoke a separate agent session for code review.

Acceptance Criteria

* Separate invocation is used.
* ReviewResult is produced.
* Blocking issues are represented.

⸻

Epic 9 — Git Management

ADO-121 — Define Git Manager Interface

Status: done

Depends On: ADO-120

Description

Define the Git Manager contract.

⸻

ADO-122 — Detect Repository Status

Status: done

Depends On: ADO-030, ADO-121

Description

Read Git repository status.

⸻

ADO-123 — Generate Commit Message

Status: done

Depends On: ADO-016

Description

Generate a focused commit message for the current story.

Format

type(scope): short description

⸻

ADO-124 — Create Git Commit

Status: done

Depends On: ADO-122, ADO-123

Description

Commit successful story changes.

Acceptance Criteria

* Git command execution uses Process Executor.
* CommitResult is produced.
* Git failures produce GitOperationException.

⸻

ADO-125 — Validate Git Manager

Status: done

Depends On: ADO-121 through ADO-124

Description

Validate Git operations using a temporary Git repository fixture.

⸻

Epic 10 — Execution Engine

ADO-126 — Create ExecutionContext Model

Status: done

Depends On: ADO-125

Description

Create the lightweight execution context.

Required Data

* current story
* retry count
* configuration
* progress state
* repository path
* active context package

⸻

ADO-127 — Define Execution Engine Interface

Status: done

Depends On: ADO-126

Description

Define the Execution Engine contract.

Operation

execute()

⸻

ADO-128 — Implement Execution Initialization

Status: done

Depends On: ADO-031, ADO-044, ADO-127

Description

Load configuration and progress state.

⸻

ADO-129 — Implement Story Selection Stage

Status: done

Depends On: ADO-057, ADO-128

Description

Select the next executable story.

⸻

ADO-130 — Mark Story In Progress

Status: done

Depends On: ADO-047, ADO-129

Description

Persist in_progress before implementation begins.

⸻

ADO-131 — Implement Context Build Stage

Status: done

Depends On: ADO-081, ADO-130

Description

Build ContextPackage for the active story.

⸻

ADO-132 — Implement Agent Generation Stage

Status: done

Depends On: ADO-090, ADO-131

Description

Invoke the configured coding agent.

⸻

ADO-133 — Implement Build Stage

Status: done

Depends On: ADO-095, ADO-132

Description

Execute the project build.

⸻

ADO-134 — Implement Repair Stage

Status: done

Depends On: ADO-109, ADO-133

Description

Invoke repair when the build fails.

⸻

ADO-135 — Implement Quality Gate Stage

Status: done

Depends On: ADO-117, ADO-134

Description

Execute mandatory quality gates.

⸻

ADO-136 — Implement Review Stage

Status: done

Depends On: ADO-120, ADO-135

Description

Run independent AI code review.

⸻

ADO-137 — Implement Commit Stage

Status: done

Depends On: ADO-125, ADO-136

Description

Commit successful implementation changes.

⸻

ADO-138 — Mark Story Done

Status: done

Depends On: ADO-047, ADO-137

Description

Persist done after successful commit.

⸻

ADO-139 — Implement Blocked Story Handling

Status: done

Depends On: ADO-047, ADO-134, ADO-135, ADO-136

Description

Mark the current story blocked when documented blocking conditions occur.

⸻

ADO-140 — Implement Execution Loop

Status: done

Depends On: ADO-128 through ADO-139

Description

Implement sequential story execution.

Workflow

Select Story
↓
Build Context
↓
Generate
↓
Build
↓
Repair
↓
Quality Gates
↓
Review
↓
Commit
↓
Update Progress
↓
Next Story

Acceptance Criteria

* One story executes at a time.
* Workflow order is deterministic.
* Quality gates cannot be bypassed.
* Review cannot be bypassed.
* Execution stops on blocking failure.

⸻

ADO-141 — Implement Resume Detection

Status: done

Depends On: ADO-140

Description

Detect an existing in_progress story on startup.

⸻

ADO-142 — Implement Story Resume

Status: done

Depends On: ADO-141

Description

Resume execution for the existing active story.

Acceptance Criteria

* Completed stories are not reprocessed.
* Active story remains the current story.

⸻

ADO-143 — Wire Execution Engine to Run Command

Status: done

Depends On: ADO-010, ADO-142

Description

Replace the bootstrap run command behavior with Execution Engine invocation.

Acceptance Criteria

* ado run invokes Execution Engine.
* CLI contains no orchestration logic.

⸻

Epic 11 — End-to-End MVP

ADO-144 — Create End-to-End Project Fixture

Status: done

Depends On: ADO-143

Description

Create a minimal sample project for ADO execution testing.

Acceptance Criteria

* Project has documentation.
* Project has TASKS.md.
* Project has configuration.
* Project has build command.

⸻

ADO-145 — Test Story Selection End to End

Status: done

Depends On: ADO-144

Description

Validate story loading, progress loading, dependency resolution, and selection.

⸻

ADO-146 — Test Context Assembly End to End

Status: done

Depends On: ADO-145

Description

Validate ContextPackage generation for the selected story.

⸻

ADO-147 — Test Agent Invocation End to End

Status: done

Depends On: ADO-146

Description

Validate ContextPackage submission through the configured Claude adapter.

⸻

ADO-148 — Test Build and Repair End to End

Status: done

Depends On: ADO-147

Description

Validate build failure analysis and repair workflow.

⸻

ADO-149 — Test Quality Gates End to End

Status: done

Depends On: ADO-148

Description

Validate all mandatory quality gates.

⸻

ADO-150 — Test Code Review End to End

Status: done

Depends On: ADO-149

Description

Validate independent AI code review.

⸻

ADO-151 — Test Commit and Progress Update End to End

Status: done

Depends On: ADO-150

Description

Validate Git commit and story completion persistence.

⸻

ADO-152 — Test Sequential Story Execution

Status: done

Depends On: ADO-151

Description

Execute multiple dependency-aware stories sequentially.

Acceptance Criteria

* Stories execute in deterministic order.
* One story executes at a time.
* Completed stories are not reprocessed.

⸻

ADO-153 — Test Blocking Failure

Status: done

Depends On: ADO-152

Description

Validate execution behavior after retry exhaustion or blocking validation failure.

Acceptance Criteria

* Story becomes blocked.
* Progress is persisted.
* Execution stops.

⸻

ADO-154 — Validate CLI MVP

Status: done

Depends On: ADO-153

Description

Validate the complete CLI workflow.

Acceptance Criteria

ado version

works.

ado run

executes the orchestration workflow.

⸻

ADO-155 — Run Complete Build Validation

Status: done

Depends On: ADO-154

Description

Execute complete project validation.

Required Validation

./gradlew clean build

Acceptance Criteria

* Compilation succeeds.
* Unit tests pass.
* Integration tests pass.
* No broken functionality remains.

⸻

ADO-156 — Validate MVP Architecture Compliance

Status: done

Depends On: ADO-155

Description

Validate implementation against approved architecture documents.

Acceptance Criteria

* CLI contains no orchestration logic.
* Execution Engine owns workflow.
* Agent integration is vendor neutral.
* Build Executor does not analyze failures.
* Failure Analyzer does not modify source code.
* Context Builder does not invoke agents.
* Only one story executes at a time.

⸻

ADO-157 — Complete ADO MVP

Status: done

Depends On: ADO-156

Description

Perform final MVP completion validation.

MVP Success Criteria

ADO can:

1. Load project documentation.
2. Load TASKS.md.
3. Select the next executable story.
4. Build a focused ContextPackage.
5. Invoke Claude Code.
6. Execute the project build.
7. Analyze build failures.
8. Run the repair loop.
9. Execute mandatory quality gates.
10. Run independent AI code review.
11. Commit successful changes.
12. Update story progress.
13. Select the next executable story.
14. Continue sequential execution.

The ADO MVP is complete when all criteria pass.

⸻

5. AI Agent Execution Contract

For each execution session:

Read project documentation.
Read TASKS.md.
Find the first task where:
status = todo
AND
all depends_on tasks = done.
Implement exactly that task.
Do not implement future tasks.
Do not refactor unrelated code.
Run required tests.
Run the project build.
If validation fails, repair the current task.
When validation succeeds:
Update the task status to done.
Create one focused Git commit.
Stop.

⸻

6. Final Rule

TASKS.md is the implementation execution backlog.

Project documentation remains the architectural source of truth.

If a task conflicts with project documentation, the AI coding agent must stop execution and report the conflict.
