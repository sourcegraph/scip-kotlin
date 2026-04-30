package com.sourcegraph.scip_kotlin

import com.sourcegraph.scip_java.ScipJava
import kotlin.io.path.Path

fun main(args: Array<String>) {
    val sourceroot = Path(args[0])
    val targetroot = Path(args[1])
    val snapshotDir = Path(args[2])

    ScipJava.main(
        arrayOf(
            "index-semanticdb",
            "--no-emit-inverse-relationships",
            "--cwd",
            sourceroot.toString(),
            "--output",
            targetroot.resolve("index.scip").toString(),
            targetroot.toString()))
    ScipJava.main(
        arrayOf(
            "snapshot",
            "--cwd",
            sourceroot.toString(),
            "--output",
            snapshotDir.toString(),
            targetroot.toString()))
}
