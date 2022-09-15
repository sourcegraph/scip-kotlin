# Kotlin LSIF support

This codebase implements a Kotlin compiler plugin that can be used together with
[lsif-java](https://sourcegraph.github.io/lsif-java) to emit
[LSIF](https://lsif.dev) indexes for Kotlin projects.

## Getting started

This project must be used together with lsif-java. Visit
[https://sourcegraph.github.io/lsif-java/] for instructions on how to index
Kotlin projects with lsif-java. Note that lsif-java indexes Kotlin sources even
if you have no Java code.

# Build with bazel plugin

Important: Make sure that we only run the plugin in `compile_phase` and **not** in `stubs_phase`. See rule def here:

```starlark
kt_compiler_plugin(
    name = "kotlin_semanticdb_plugin",
    compile_phase = True,
    id = "semanticdb-kotlinc",
    options = {
        "sourceroot": "/private/var/tmp/_bazel_jkvarnefalk/ad23c1b0c3ae269eb3abff3b8750adb6/execroot/scip_kt_tests/",
        "targetroot": "/Users/jkvarnefalk/dev/sp/android/lsif-kotlin",
    },
    stubs_phase = False,
    target_embedded_compiler = True,
    deps = [
        ":kotlin_semanticdbx",
        "//semanticdb-kotlin:semanticdb-java",
        "//semanticdb-kotlin/src/main/proto/com.sourcegraph.semanticdb_kotlin:semanticdb_java_proto",
    ],
)
```

At the moment, we haven't implemented any smart sourceroot and targetroot mechanism to work around the sandbox features
in Bazel. It works by hardcoding the tmp dir, but we want to have a better resolver here like in [scip-java](https://github.com/sourcegraph/scip-java/blob/53db85a92162a56ffcc831af60d63b5afa3601b8/semanticdb-javac/src/main/java/com/sourcegraph/semanticdb_javac/SemanticdbTaskListener.java#L133)

# Running plugin built with Bazel with kotlinc

To build the plugin with bazel, run:

```bash
bazel build //semanticdb-kotlinc:kotlin_semanticdb_plugin
```

That command usually outputs a list of jars, we have put this in a convience file called `pack.sh`.
This file unpacks all of the jars built by bazel and puts them into a combined jar `combined.jar`.
The `combined.jar` is the jar we will pass as the plugin to kotlinc command:

```bash
kotlinc \
   -Xplugin=combined.jar \
   -P plugin:semanticdb-kotlinc:sourceroot=/Users/jkvarnefalk/dev/sp/android/lsif-kotlin \
   -P plugin:semanticdb-kotlinc:targetroot=/Users/jkvarnefalk/dev/sp/android/lsif-kotlin \
   example/src/main/kotlin/sample/Hello.kt
```

This command will output a file called `META-INF/semanticdb/example/src/main/kotlin/sample/Hello.kt.semanticdb`