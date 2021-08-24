package com.sourcegraph.semanticdb_kotlinc.test

import com.sourcegraph.semanticdb_kotlinc.*
import com.sourcegraph.semanticdb_kotlinc.Semanticdb.SymbolOccurrence.Role
import com.sourcegraph.semanticdb_kotlinc.test.ExpectedSymbols.SemanticdbData
import com.sourcegraph.semanticdb_kotlinc.test.ExpectedSymbols.SymbolCacheData
import com.tschuchort.compiletesting.SourceFile
import org.junit.jupiter.api.TestFactory
import kotlin.contracts.ExperimentalContracts

@ExperimentalContracts
class SemanticdbSymbolsTest {
    @TestFactory
    fun `method disambiguator`() = listOf(
        ExpectedSymbols(
            "Basic two methods",
            SourceFile.testKt("""
                class Test {
                    fun sample() {}
                    fun sample(x: Int) {}
                }
            """),
            symbolsCacheData = SymbolCacheData(
                listOf("Test#sample().".symbol(), "Test#sample(+1).".symbol()),
            )
        ),
        ExpectedSymbols(
            "Inline class constructor",
            SourceFile.testKt("""
                class Test(val x: Int)
            """),
            symbolsCacheData = SymbolCacheData(
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
            symbolsCacheData = SymbolCacheData(
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
            symbolsCacheData = SymbolCacheData(
                listOf("Test#test().".symbol(), "Test#test(+1).".symbol())
            )
        ),
        ExpectedSymbols(
            "Top level overloaded functions",
            SourceFile.testKt("""
                fun test() {}
                fun test(x: Int) {}
            """),
            symbolsCacheData = SymbolCacheData(
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
            symbolsCacheData = SymbolCacheData(
                listOf("kotlin/contracts/ExperimentalContracts#".symbol(), "kotlin/test/Test#".symbol())
            )
        ),
        ExpectedSymbols(
            "Method call with type parameters",
            SourceFile.testKt("""
                import org.junit.jupiter.api.io.TempDir
                val burger = LinkedHashMap<String, TempDir>() 
            """),
            symbolsCacheData = SymbolCacheData(
                listOf("kotlin/collection/TypeAliasesKt#LinkedHashMap#`<init>`().".symbol())
            )
        )
    ).mapCheckExpectedSymbols()

    @TestFactory
    fun `check package symbols`() = listOf(
        ExpectedSymbols(
            "single component package name",
            SourceFile.testKt("""
                package main
                
                class Test
            """),
            symbolsCacheData = SymbolCacheData(
                listOf("main/Test#".symbol()), 0
            )
        ),
        ExpectedSymbols(
            "multi component package name",
            SourceFile.testKt("""
                package test.sample.main
                
                class Test
            """),
            symbolsCacheData = SymbolCacheData(
                listOf("test/sample/main/Test#".symbol()), 0
            )
        ),
        ExpectedSymbols(
            "no package name",
            SourceFile.testKt("""
                class Test
            """),
            symbolsCacheData = SymbolCacheData(
                listOf("Test#".symbol()), 0
            )
        )
    ).mapCheckExpectedSymbols()

    @TestFactory
    fun `check locals counts`() = listOf(
        ExpectedSymbols(
            "simple variables",
            SourceFile.testKt(
                """
                    fun test() {
                        val x = "hello"
                        println(x)
                    }
                """
            ),
            symbolsCacheData = SymbolCacheData(localsCount = 1)
        )
    ).mapCheckExpectedSymbols()

    @TestFactory
    fun `builtin symbols`() = listOf(
        ExpectedSymbols(
            "types",
            SourceFile.testKt("""
                var x: Int = 1
                lateinit var y: Unit
                lateinit var z: Any
                lateinit var w: Nothing
            """),
            symbolsCacheData = SymbolCacheData(
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
            symbolsCacheData = SymbolCacheData(
                listOf("kotlin/collections/MapsKt#mapOf(+1).".symbol(), "kotlin/io/ConsoleKt#println().".symbol())
            )
        )
    ).mapCheckExpectedSymbols()

    @TestFactory
    fun `reference expressions`() = listOf(
        ExpectedSymbols(
            "dot qualified expression",
            SourceFile.testKt("""
                import java.lang.System

                fun main() {
                    System.err
                }
            """
            ),
            symbolsCacheData = SymbolCacheData(
                listOf("java/lang/System#err.".symbol())
            )
        )
    ).mapCheckExpectedSymbols()

    @TestFactory
    fun `properties with getters-setters`() = listOf(
        ExpectedSymbols(
            "top level properties - implicit",
            SourceFile.testKt(
                """
                var x: Int = 5
            """),
            semanticdb = SemanticdbData(
                expectedOccurrences = listOf(
                    SymbolOccurrence { role = Role.DEFINITION; symbol = "TestKt#x."; range { startLine = 0; startCharacter = 4; endLine = 0; endCharacter = 5; } },
                    SymbolOccurrence { role = Role.DEFINITION; symbol = "TestKt#getX()."; range { startLine = 0; startCharacter = 4; endLine = 0; endCharacter = 5; } },
                    SymbolOccurrence { role = Role.DEFINITION; symbol = "TestKt#setX()."; range { startLine = 0; startCharacter = 4; endLine = 0; endCharacter = 5; } }
                )
            ),
        ),
        ExpectedSymbols(
            "top level properties - explicit getter",
            SourceFile.testKt(
                """
                var x: Int = 5
                    get() = field + 10
            """),
            semanticdb = SemanticdbData(
                expectedOccurrences = listOf(
                    SymbolOccurrence { role = Role.DEFINITION; symbol = "TestKt#x."; range { startLine = 0; startCharacter = 4; endLine = 0; endCharacter = 5; } },
                    SymbolOccurrence { role = Role.DEFINITION; symbol = "TestKt#setX()."; range { startLine = 0; startCharacter = 4; endLine = 0; endCharacter = 5; } },
                    SymbolOccurrence { role = Role.DEFINITION; symbol = "TestKt#getX()."; range { startLine = 1; startCharacter = 4; endLine = 1; endCharacter = 7; } }
                )
            ),
        ),
        ExpectedSymbols(
            "top level properties - explicit setter",
            SourceFile.testKt(
                """
                var x: Int = 5
                    set(value) { field = value + 5 }
            """),
            semanticdb = SemanticdbData(
                expectedOccurrences = listOf(
                    SymbolOccurrence { role = Role.DEFINITION; symbol = "TestKt#x."; range { startLine = 0; startCharacter = 4; endLine = 0; endCharacter = 5; } },
                    SymbolOccurrence { role = Role.DEFINITION; symbol = "TestKt#getX()."; range { startLine = 0; startCharacter = 4; endLine = 0; endCharacter = 5; } },
                    SymbolOccurrence { role = Role.DEFINITION; symbol = "TestKt#setX()."; range { startLine = 1; startCharacter = 4; endLine = 1; endCharacter = 7; } }
                )
            ),
        ),
        ExpectedSymbols(
            "top level properties - explicit getter & setter",
            SourceFile.testKt(
                """
                var x: Int = 5
                    get() = field + 10
                    set(value) { field = value + 10 }
            """),
            semanticdb = SemanticdbData(
                expectedOccurrences = listOf(
                    SymbolOccurrence { role = Role.DEFINITION; symbol = "TestKt#x."; range { startLine = 0; startCharacter = 4; endLine = 0; endCharacter = 5; } },
                    SymbolOccurrence { role = Role.DEFINITION; symbol = "TestKt#getX()."; range { startLine = 1; startCharacter = 4; endLine = 1; endCharacter = 7; } },
                    SymbolOccurrence { role = Role.DEFINITION; symbol = "TestKt#setX()."; range { startLine = 2; startCharacter = 4; endLine = 2; endCharacter = 7; } }
                )
            ),
        ),
        ExpectedSymbols(
            "class constructor properties",
            SourceFile.testKt(
                """
                class Test(var sample: Int, text: String): Throwable(sample.toString()) {
                    fun test() {
                        println(sample)
                    }
                }        
            """),
            semanticdb = SemanticdbData(
                expectedOccurrences = listOf(
                    SymbolOccurrence { role = Role.DEFINITION; symbol = "Test#sample."; range { startLine = 0; startCharacter = 15; endLine = 0; endCharacter = 21; } },
                    SymbolOccurrence { role = Role.DEFINITION; symbol = "Test#getSample()."; range { startLine = 0; startCharacter = 15; endLine = 0; endCharacter = 21; } },
                    SymbolOccurrence { role = Role.DEFINITION; symbol = "Test#setSample()."; range { startLine = 0; startCharacter = 15; endLine = 0; endCharacter = 21; } },
                    SymbolOccurrence { role = Role.DEFINITION; symbol = "Test#`<init>`().(sample)"; range { startLine = 0; startCharacter = 15; endLine = 0; endCharacter = 21; } },
                    SymbolOccurrence { role = Role.REFERENCE; symbol = "Test#`<init>`().(sample)"; range { startLine = 0; startCharacter = 53; endLine = 0; endCharacter = 59; } },
                    SymbolOccurrence { role = Role.REFERENCE; symbol = "Test#sample."; range { startLine = 2; startCharacter = 16; endLine = 2; endCharacter = 22; } },
                    SymbolOccurrence { role = Role.REFERENCE; symbol = "Test#getSample()."; range { startLine = 2; startCharacter = 16; endLine = 2; endCharacter = 22; } },
                )
            )
        )
    ).mapCheckExpectedSymbols()

    @TestFactory
    fun `class constructors`() = listOf(
        ExpectedSymbols(
            "implicit primary constructor",
            SourceFile.testKt(
                """
                class Banana
            """),
            semanticdb = SemanticdbData(
                expectedOccurrences = listOf(
                    SymbolOccurrence { role = Role.DEFINITION; symbol = "Banana#"; range { startLine = 0; startCharacter = 6; endLine = 0; endCharacter = 12; } },
                    SymbolOccurrence { role = Role.DEFINITION; symbol = "Banana#`<init>`()."; range { startLine = 0; startCharacter = 6; endLine = 0; endCharacter = 12; } },
                )
            )
        ),
        ExpectedSymbols(
            "explicit primary constructor without keyword",
            SourceFile.testKt("""
               class Banana(size: Int)
            """),
            semanticdb = SemanticdbData(
                expectedOccurrences = listOf(
                    SymbolOccurrence { role = Role.DEFINITION; symbol = "Banana#"; range { startLine = 0; startCharacter = 6; endLine = 0; endCharacter = 12; } },
                    SymbolOccurrence { role = Role.DEFINITION; symbol = "Banana#`<init>`()."; range { startLine = 0; startCharacter = 6; endLine = 0; endCharacter = 12; } },
                )
            )
        ),
        ExpectedSymbols(
            "explicit primary constructor with keyword",
            SourceFile.testKt("""
               class Banana constructor(size: Int) 
            """),
            semanticdb = SemanticdbData(
                expectedOccurrences = listOf(
                    SymbolOccurrence { role = Role.DEFINITION; symbol = "Banana#"; range { startLine = 0; startCharacter = 6; endLine = 0; endCharacter = 12; } },
                    SymbolOccurrence { role = Role.DEFINITION; symbol = "Banana#`<init>`()."; range { startLine = 0; startCharacter = 13; endLine = 0; endCharacter = 24; } },
                )
            )
        )
    ).mapCheckExpectedSymbols()

    @TestFactory
    fun `Single Abstract Method interface`() = listOf(
        ExpectedSymbols(
            "basic java.lang.Runnable",
            SourceFile.testKt(
                """
                val x = Runnable { }.run()
            """
            ),
            semanticdb = SemanticdbData(
                expectedOccurrences = listOf(
                    SymbolOccurrence { role = Role.REFERENCE; symbol = "java/lang/Runnable#"; range { startLine = 0; startCharacter = 8; endLine = 0; endCharacter = 16; } },
                    SymbolOccurrence { role = Role.REFERENCE; symbol = "java/lang/Runnable#run()."; range { startLine = 0; startCharacter = 21; endLine = 0; endCharacter = 24; } }
                )
            )
        )
    ).mapCheckExpectedSymbols()
}
