# Kotlin LSIF support

This codebase implements a Kotlin compiler plugin that can be used together with
[lsif-java](https://sourcegraph.github.io/lsif-java) to emit
[LSIF](https://lsif.dev) indexes for Kotlin projects.

## Getting started

This project must be used together with lsif-java. Visit
[https://sourcegraph.github.io/lsif-java/] for instructions on how to index
Kotlin projects with lsif-java. Note that lsif-java indexes Kotlin sources even
if you have no Java code.
