package com.sourcegraph.semanticdb_kotlinc.test

import com.sourcegraph.semanticdb_kotlinc.*
import com.sourcegraph.semanticdb_kotlinc.test.ExpectedSymbols.SemanticdbData
import com.sourcegraph.semanticdb_kotlinc.test.ExpectedSymbols.SymbolCacheData
import com.tschuchort.compiletesting.SourceFile
import org.junit.jupiter.api.TestFactory
import kotlin.contracts.ExperimentalContracts

@ExperimentalContracts
class SemanticdbSymbolsTest {
    @TestFactory
    fun `method disambiguator`() =
        listOf(
            ExpectedSymbols(
                "Basic two methods",
                SourceFile.testKt("""
                    class Test {
                        fun sample() {}
                        fun sample(x: Int) {}
                    }
                """),
                SymbolCacheData(
                    listOf("Test#sample().".symbol(), "Test#sample(+1).".symbol()),
                )
            ),
            ExpectedSymbols(
                "Inline class constructor",
                SourceFile.testKt("""
                    class Test(val x: Int)
                """),
                SymbolCacheData(
                    listOf("Test#`<init>`().(x)".symbol())
                )
            ),
            ExpectedSymbols(
                "Inline + secondary class constructors",
                SourceFile.testKt("""
                    class Test(val x: Int) {
                        constructor(y: Long): this(y.toInt())
                        constructor(z: String): this(z.toInt())
                    }
                """),
                SymbolCacheData(
                    listOf(
                        "Test#`<init>`(+2).(x)".symbol(),
                        "Test#`<init>`().(y)".symbol(),
                        "Test#`<init>`(+1).(z)".symbol()
                    )
                )
            ),
            ExpectedSymbols(
                "Disambiguator number is not affected by different named methods",
                SourceFile.testKt("""
                    class Test {
                        fun sample() {}
                        fun test() {}
                        fun test(x: Int) {}
                    }
                """),
                SymbolCacheData(
                    listOf("Test#test().".symbol(), "Test#test(+1).".symbol())
                )
            ),
            ExpectedSymbols(
                "Top level overloaded functions",
                SourceFile.testKt("""
                    fun test() {}
                    fun test(x: Int) {}
                """),
                SymbolCacheData(
                    listOf("TestKt#test().".symbol(), "TestKt#test(+1).(x)".symbol())
                )
            ),
            ExpectedSymbols(
                "Annotations incl annotation type alias",
                SourceFile.testKt("""
                    import kotlin.contracts.ExperimentalContracts
                    import kotlin.test.Test

                    @ExperimentalContracts   
                    class Banaan {
                        @Test
                        fun test() {}                   
                   } 
                """),
                SymbolCacheData(
                    listOf("kotlin/contracts/ExperimentalContracts#".symbol(), "kotlin/test/Test#".symbol())
                )
            ),
            ExpectedSymbols(
                "Method call with type parameters",
                SourceFile.testKt("""
                    import org.junit.jupiter.api.io.TempDir
                    val burger = LinkedHashMap<String, TempDir>() 
                """),
                SymbolCacheData(
                    listOf("kotlin/collection/TypeAliasesKt#LinkedHashMap#`<init>`().".symbol())
                )
            )
        ).mapCheckExpectedSymbols()

    @TestFactory
    fun `check package symbols`() =
        listOf(
            ExpectedSymbols(
                "single component package name",
                SourceFile.testKt("""
                    package main
                    
                    class Test
                """),
                SymbolCacheData(
                    listOf("main/Test#".symbol()), 0
                )
            ),
            ExpectedSymbols(
                "multi component package name",
                SourceFile.testKt("""
                    package test.sample.main
                    
                    class Test
                """),
                SymbolCacheData(
                    listOf("test/sample/main/Test#".symbol()), 0
                )
            ),
            ExpectedSymbols(
                "no package name",
                SourceFile.testKt("""
                    class Test
                """),
                SymbolCacheData(
                    listOf("Test#".symbol()), 0
                )
            )
        ).mapCheckExpectedSymbols()

    @TestFactory
    fun `builtin symbols`() =
        listOf(
            ExpectedSymbols(
                "types",
                SourceFile.testKt("""
                    var x: Int = 1
                    lateinit var y: Unit
                    lateinit var z: Any
                    lateinit var w: Nothing
                """),
                SymbolCacheData(
                    listOf("kotlin/Int#".symbol(), "kotlin/Unit#".symbol(), "kotlin/Any#".symbol(), "kotlin/Nothing#".symbol())
                )
            ),
            ExpectedSymbols(
                "functions",
                SourceFile.testKt(
                    """
                    val x = mapOf<Void, Void>()
                    fun main() {
                        println()
                    }
                """),
                SymbolCacheData(
                    listOf("kotlin/collections/MapsKt#mapOf(+1).".symbol(), "kotlin/io/ConsoleKt#println().".symbol())
                )
            )
        ).mapCheckExpectedSymbols()

    @TestFactory
    fun `reference expressions`() =
        listOf(
            ExpectedSymbols(
                "dot qualified expression",
                SourceFile.testKt("""
                    import java.lang.System

                    fun main() {
                        System.err
                    }
                """
                ),
                SymbolCacheData(
                    listOf("java/lang/System#err.".symbol())
                )
            )
        ).mapCheckExpectedSymbols()

}
