# Kotlin SCIP support
<img src="https://img.shields.io/maven-central/v/com.sourcegraph/semanticdb-kotlinc?style=flat-square" />

This codebase implements a Kotlin compiler plugin that can be used together with
[scip-java](https://sourcegraph.github.io/scip-java) to emit
[SCIP](https://github.com/sourcegraph/scip) indexes for Kotlin projects.

## Getting started

This project must be used together with scip-java. Visit
[scip-java](https://sourcegraph.github.io/scip-java/) for instructions on how to
index Kotlin projects with scip-java. Note that scip-java indexes Kotlin sources
even if you have no Java code.

## SemanticDB support

This project is implemented as a
[SemanticDB](https://scalameta.org/docs/semanticdb/specification.html) compiler
plugin. To generate SCIP, you first compile the Kotlin sources with the
SemanticDB compiler plugin and then convert SemanticDB files into SCIP using
`scip-java index-semanticdb`. See [Low-level usage](#low-level-usage) for more
details on how to generate SemanticDB files and convert SemanticDB into SCIP.

## Low-level usage


First, fetch the jar file of the SemanticDB compiler plugin:
[`com.sourcegraph:semanticdb-kotlinc:VERSION`](https://mvnrepository.com/artifact/com.sourcegraph/semanticdb-kotlinc).
For example, you can use 
[Coursier](https://get-coursier.io) to download the jar file.

```sh
curl -fLo coursier https://github.com/coursier/launchers/raw/master/coursier && chmod +x ./coursier
export SEMANTICDB_KOTLIN_VERSION="latest.release" # or replace with a particular version
export SEMANTICDB_KOTLIN_JAR=$(./coursier fetch com.sourcegraph:semanticdb-kotlinc:$SEMANTICDB_KOTLIN_VERSION)
```

Once you have the jar file, you need to determine two compiler options:

- `sourceroot`: the absolute path to the root directory of your codebase. All
  source files that you want to index should be under this directory. For Gradle
  codebases, this is typically the toplevel `build.gradle` file. For Maven
  codebases, this is typically the toplevel `pom.xml` file.
- `targetroot`: the absolute path to the directory where you want the compiler
plugin to write SemanticDB files. This can be any directory on your computer.

Now you have all the necessary parameters to invoke the Kotlin compiler with
the SemanticDB compiler plugin.

```sh
kotlinc -Xplugin=${SEMANTICDB_KOTLIN_JAR} \
  -P plugin:semanticdb-kotlinc:sourceroot=SOURCEROOT_DIRECTORY \
  -P plugin:semanticdb-kotlinc:targetroot=TARGETROOT_DIRECTORY
```

Once the compilation is complete, the targetroot should contain `*.semanticdb`
files in the `META-INF/semanticdb` sub-directory.

To convert the SemanticDB files into SCIP, run `scip-java index TARGETROOT_DIRECTORY`.
If you have Coursier installed, you can run scip-java directly like this

```sh
cd $SOURCEROOT_DIRECTORY
./coursier launch --contrib scip-java -- index-semanticdb TARGETROOT_DIRECTORY
```









