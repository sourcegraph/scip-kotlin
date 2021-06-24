package com.sourcegraph.semanticdb_kotlinc.test

import com.tschuchort.compiletesting.SourceFile
import org.intellij.lang.annotations.Language

fun SourceFile.Companion.testKt(@Language("kotlin") contents: String): SourceFile =
    kotlin("Test.kt", contents)