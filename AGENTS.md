# AGENTS.md

Guidance for coding agents working in this repository.

## What this is

A Kotlin compiler plugin (`com.sourcegraph:semanticdb-kotlinc`) that emits
[SemanticDB](https://scalameta.org/docs/semanticdb/specification.html) data for
Kotlin sources, used with [scip-java](https://sourcegraph.github.io/scip-java)
to produce [SCIP](https://github.com/sourcegraph/scip) indexes. See
`README.md` for usage.

## Layout

- `build.sbt` — sbt build defining three projects: `kotlinc` (the plugin),
  `minimized` (compile fixtures + snapshot generation), and `snapshotsRunner`
  (runs scip-java).
- `semanticdb-kotlinc/src/main/kotlin/com/sourcegraph/semanticdb_kotlinc/` —
  main plugin sources (analyzer, registrars, SemanticDB builder/visitor).
- `semanticdb-kotlinc/src/main/proto/` — protobuf definitions (Java codegen).
- `semanticdb-kotlinc/src/test/kotlin/.../semanticdb_kotlinc/test/` — tests.
- `semanticdb-kotlinc/minimized/` — fixtures and golden snapshots under
  `minimized/src/generatedSnapshots/resources/`.
- `project/` — sbt version (`build.properties`) and plugins (`plugins.sbt`).

## Setup

- Build tool: sbt (`sbt.version` pinned in `project/build.properties`).
- JDK: 11 (used by CI).
- Kotlin version is pinned in `build.sbt` (`V.kotlin`); each release supports a
  single major Kotlin version (see the compatibility table in `README.md`).

## Common commands

Run from the repository root:

```sh
# Run the plugin's test suite
sbt kotlinc/test

# Regenerate the golden SemanticDB/SCIP snapshots
sbt minimized/snapshots

# Build the shaded fat-jar / publish locally
sbt kotlinc/assembly
sbt kotlinc/publishLocal
```

## Conventions

- After changing plugin behavior, regenerate snapshots with
  `sbt minimized/snapshots`. CI fails on snapshot drift via:

  ```sh
  git diff --exit-code semanticdb-kotlinc/minimized/src/generatedSnapshots
  ```

- Formatting for `.sbt`/Scala build files is handled by sbt-scalafmt
  (`sbt scalafmtAll`).
- `kotlin-stdlib` and `kotlin-compiler-embeddable` are `Provided` — they are
  supplied by `kotlinc` at runtime and must not be bundled into the fat-jar.

## CI

`.github/workflows/ci.yml` runs `sbt kotlinc/test`, `sbt minimized/snapshots`,
and the snapshot-drift check. `.github/workflows/release.yml` publishes via
`sbt ci-release` on pushes to `main` and `v*` tags.
