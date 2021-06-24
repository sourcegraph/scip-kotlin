package com.sourcegraph.semanticdb_kotlinc.test

import com.sourcegraph.semanticdb_kotlinc.test.SymbolsCacheTest.Companion.checkContainsExpectedSymbols
import com.tschuchort.compiletesting.SourceFile
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.DynamicTest
import kotlin.contracts.ExperimentalContracts

fun SourceFile.Companion.testKt(@Language("kotlin") contents: String): SourceFile =
    kotlin("Test.kt", contents)

@ExperimentalContracts
fun List<SymbolsCacheTest.ExpectedSymbols>.mapCheckExpectedSymbols() = this.map { (testName, source, expectedGlobals, localsCount) ->
    DynamicTest.dynamicTest(testName) {
        checkContainsExpectedSymbols(source, expectedGlobals, localsCount)
    }
}