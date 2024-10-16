package com.sourcegraph.semanticdb_kotlinc.test

import com.sourcegraph.semanticdb_kotlinc.*
import com.sourcegraph.semanticdb_kotlinc.Semanticdb.Documentation.Format
import com.sourcegraph.semanticdb_kotlinc.Semanticdb.Language
import com.sourcegraph.semanticdb_kotlinc.Semanticdb.SymbolOccurrence.Role
import com.sourcegraph.semanticdb_kotlinc.test.ExpectedSymbols.SemanticdbData
import com.sourcegraph.semanticdb_kotlinc.test.ExpectedSymbols.SymbolCacheData
import com.tschuchort.compiletesting.SourceFile
import kotlin.contracts.ExperimentalContracts
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

@ExperimentalCompilerApi
@ExperimentalContracts
class SemanticdbSymbolsTest {
    @TestFactory
    fun `method disambiguator`() =
        listOf<ExpectedSymbols>(
                ExpectedSymbols(
                    "Basic two methods",
                    SourceFile.testKt(
                        """
                        |class Test {
                        |    fun sample() {}
                        |    fun sample(x: Int) {}
                        |}
                        |""".trimMargin()),
                    symbolsCacheData =
                        SymbolCacheData(
                            listOf(
                                "Test#sample.sample(): kotlin/Unit.".symbol(),
                                "Test#sample.sample(kotlin/Int): kotlin/Unit.".symbol()),
                        )),
                ExpectedSymbols(
                    "Inline class constructor",
                    SourceFile.testKt(
                        """
                        |class Test(val x: Int)
                        |""".trimMargin()),
                    symbolsCacheData =
                        SymbolCacheData(listOf("Test#Test.Test(kotlin/Int): Test.".symbol()))),
                ExpectedSymbols(
                    "Inline + secondary class constructors",
                    SourceFile.testKt(
                        """
                        |class Test(val x: Int) {
                        |    constructor(y: Long): this(y.toInt())
                        |    constructor(z: String): this(z.toInt())
                        |}
                        |""".trimMargin()),
                    symbolsCacheData =
                        SymbolCacheData(
                            listOf(
                                "Test#Test.Test(kotlin/Int): Test.".symbol(),
                                "Test#Test.Test(kotlin/Long): Test.".symbol(),
                                "Test#Test.Test(kotlin/String): Test.".symbol()))),
                ExpectedSymbols(
                    "Disambiguator number is not affected by different named methods",
                    SourceFile.testKt(
                        """
                        |class Test {
                        |    fun sample() {}
                        |    fun test() {}
                        |    fun test(x: Int) {}
                        |}
                        |""".trimMargin()),
                    symbolsCacheData =
                        SymbolCacheData(
                            listOf(
                                "Test#sample.sample(): kotlin/Unit.".symbol(),
                                "Test#test.test(): kotlin/Unit.".symbol(),
                                "Test#test.test(kotlin/Int): kotlin/Unit.".symbol()))),
                ExpectedSymbols(
                    "Top level overloaded functions",
                    SourceFile.testKt(
                        """
                        |fun test() {}
                        |fun test(x: Int) {}
                        |""".trimMargin()),
                    symbolsCacheData =
                        SymbolCacheData(
                            listOf(
                                "`Test.kt.test`#test.test(): kotlin/Unit.".symbol(),
                                "`Test.kt.test`#test.test(kotlin/Int): kotlin/Unit.".symbol()))),
                ExpectedSymbols(
                    "Annotations incl annotation type alias",
                    SourceFile.testKt(
                        """
                        |import kotlin.contracts.ExperimentalContracts
                        |import kotlin.test.Test
                        |
                        |@ExperimentalContracts
                        |class Banaan {
                        |    @Test
                        |    fun test() {}
                        |}
                        |""".trimMargin()),
                    symbolsCacheData =
                        SymbolCacheData(
                            listOf(
                                "`kotlin/contracts/ExperimentalContracts`#".symbol(),
                                "`kotlin/test/Test`#".symbol()))),
                // https://kotlinlang.slack.com/archives/C7L3JB43G/p1624995376114900
                /*ExpectedSymbols(
                    "Method call with type parameters",
                    SourceFile.testKt("""
                        import org.junit.jupiter.api.io.TempDir
                        val burger = LinkedHashMap<String, TempDir>()
                    """),
                    symbolsCacheData = SymbolCacheData(
                        listOf("kotlin/collection/TypeAliasesKt#LinkedHashMap#`<init>`().".symbol())
                    )
                )*/
                )
            .mapCheckExpectedSymbols()

    @TestFactory
    fun `check package symbols`() =
        listOf(
                ExpectedSymbols(
                    "single component package name",
                    SourceFile.testKt(
                        """
                        |package main
                        |
                        |class Test
                        |""".trimMargin()),
                    symbolsCacheData = SymbolCacheData(listOf("`main/Test`#".symbol()), 0)),
                ExpectedSymbols(
                    "multi component package name",
                    SourceFile.testKt(
                        """
                        |package test.sample.main
                        |
                        |class Test
                        |""".trimMargin()),
                    symbolsCacheData =
                        SymbolCacheData(listOf("`test/sample/main/Test`#".symbol()), 0)),
                ExpectedSymbols(
                    "no package name",
                    SourceFile.testKt(
                        """
                        |class Test
                        |""".trimMargin()),
                    symbolsCacheData = SymbolCacheData(listOf("Test#".symbol()), 0)))
            .mapCheckExpectedSymbols()

    @TestFactory
    fun `check locals counts`() =
        listOf(
                ExpectedSymbols(
                    "simple variables",
                    SourceFile.testKt(
                        """
                        |fun test() {
                        |    val x = "hello"
                        |    println(x)
                        |}
                        |""".trimMargin()),
                    symbolsCacheData = SymbolCacheData(localsCount = 1)))
            .mapCheckExpectedSymbols()

    @Test
    fun `builtin symbols`() =
        listOf(
                ExpectedSymbols(
                    "types",
                    SourceFile.testKt(
                        """
                        |var x: Int = 1
                        |lateinit var y: Unit
                        |lateinit var z: Any
                        |lateinit var w: Nothing
                        |""".trimMargin()),
                    symbolsCacheData =
                        SymbolCacheData(
                            listOf(
                                "`Test.kt.accessor`#accessorspecial.accessor(): kotlin/Int.".symbol(),
                                "`Test.kt.accessor`#accessorspecial.accessor(): kotlin/Unit.".symbol(),
                                "`Test.kt.accessor`#accessorspecial.accessor(): kotlin/Any.".symbol(),
                                "`Test.kt.accessor`#accessorspecial.accessor(): kotlin/Nothing.".symbol()))),
                ExpectedSymbols(
                    "functions",
                    SourceFile.testKt(
                        """
                        |val x = mapOf<Void, Void>()
                        |fun main() {
                        |    println()
                        |}
                        |""".trimMargin()),
                    symbolsCacheData =
                        SymbolCacheData(
                            listOf(
                                "`kotlin.collections.mapOf`#mapOfkotlin.collections.mapOf(): kotlin/collections/Map<K, V>.".symbol(),
                                "`kotlin.io.println`#printlnkotlin.io.println(): kotlin/Unit.".symbol()))))
            .mapCheckExpectedSymbols()

    @TestFactory
    fun `reference expressions`() =
        listOf(
                ExpectedSymbols(
                    "dot qualified expression",
                    SourceFile.testKt(
                        """
                        |import java.lang.System
                        |
                        |fun main() {
                        |    System.err
                        |}
                        |""".trimMargin()),
                    symbolsCacheData = SymbolCacheData(listOf("`java/lang/System`#err.".symbol()))))
            .mapCheckExpectedSymbols()

    @TestFactory
    fun `properties with getters-setters`() =
        listOf(
                ExpectedSymbols(
                    "top level properties - implicit",
                    SourceFile.testKt(
                        """
                        |var x: Int = 5
                        |""".trimMargin()),
                    semanticdb =
                        SemanticdbData(
                            expectedOccurrences =
                                listOf(
                                    SymbolOccurrence {
                                        role = Role.DEFINITION
                                        symbol = "`Test.kt.x`#x."
                                        range {
                                            startLine = 0
                                            startCharacter = 0
                                            endLine = 0
                                            endCharacter = 13
                                        }
                                    },
                                    SymbolOccurrence {
                                        role = Role.DEFINITION
                                        symbol =
                                            "`Test.kt.accessor`#accessorspecial.accessor(): kotlin/Int."
                                        range {
                                            startLine = 0
                                            startCharacter = 0
                                            endLine = 0
                                            endCharacter = 13
                                        }
                                    },
                                    SymbolOccurrence {
                                        role = Role.DEFINITION
                                        symbol =
                                            "`Test.kt.accessor`#accessorspecial.accessor(kotlin/Int): kotlin/Unit."
                                        range {
                                            startLine = 0
                                            startCharacter = 0
                                            endLine = 0
                                            endCharacter = 13
                                        }
                                    })),
                ),
                ExpectedSymbols(
                    "top level properties - explicit getter",
                    SourceFile.testKt(
                        """
                        |var x: Int = 5
                        |    get() = field + 10
                        |""".trimMargin()),
                    semanticdb =
                        SemanticdbData(
                            expectedOccurrences =
                                listOf(
                                    SymbolOccurrence {
                                        role = Role.DEFINITION
                                        symbol = "`Test.kt.x`#x."
                                        range {
                                            startLine = 0
                                            startCharacter = 0
                                            endLine = 0
                                            endCharacter = 36
                                        }
                                    },
                                    SymbolOccurrence {
                                        role = Role.DEFINITION
                                        symbol =
                                            "`Test.kt.accessor`#accessorspecial.accessor(): kotlin/Int."
                                        range {
                                            startLine = 1
                                            startCharacter = 4
                                            endLine = 1
                                            endCharacter = 21
                                        }
                                    },
                                    SymbolOccurrence {
                                        role = Role.DEFINITION
                                        symbol =
                                            "`Test.kt.accessor`#accessorspecial.accessor(kotlin/Int): kotlin/Unit."
                                        range {
                                            startLine = 0
                                            startCharacter = 0
                                            endLine = 0
                                            endCharacter = 36
                                        }
                                    },
                                )),
                ),
                ExpectedSymbols(
                    "top level properties - explicit setter",
                    SourceFile.testKt(
                        """
                        |var x: Int = 5
                        |    set(value) { field = value + 5 }
                        |""".trimMargin()),
                    semanticdb =
                        SemanticdbData(
                            expectedOccurrences =
                                listOf(
                                    SymbolOccurrence {
                                        role = Role.DEFINITION
                                        symbol = "`Test.kt.x`#x."
                                        range {
                                            startLine = 0
                                            startCharacter = 0
                                            endLine = 0
                                            endCharacter = 50
                                        }
                                    },
                                    SymbolOccurrence {
                                        role = Role.DEFINITION
                                        symbol =
                                            "`Test.kt.accessor`#accessorspecial.accessor(): kotlin/Int."
                                        range {
                                            startLine = 0
                                            startCharacter = 0
                                            endLine = 0
                                            endCharacter = 50
                                        }
                                    },
                                    SymbolOccurrence {
                                        role = Role.DEFINITION
                                        symbol =
                                            "`Test.kt.accessor`#accessorspecial.accessor(kotlin/Int): kotlin/Unit."
                                        range {
                                            startLine = 1
                                            startCharacter = 4
                                            endLine = 1
                                            endCharacter = 35
                                        }
                                    })),
                ),
                ExpectedSymbols(
                    "top level properties - explicit getter & setter",
                    SourceFile.testKt(
                        """
                        |var x: Int = 5
                        |    get() = field + 10
                        |    set(value) { field = value + 10 }
                        |""".trimMargin()),
                    semanticdb =
                        SemanticdbData(
                            expectedOccurrences =
                                listOf(
                                    SymbolOccurrence {
                                        role = Role.DEFINITION
                                        symbol = "`Test.kt.x`#x."
                                        range {
                                            startLine = 0
                                            startCharacter = 0
                                            endLine = 0
                                            endCharacter = 74
                                        }
                                    },
                                    SymbolOccurrence {
                                        role = Role.DEFINITION
                                        symbol =
                                            "`Test.kt.accessor`#accessorspecial.accessor(): kotlin/Int."
                                        range {
                                            startLine = 1
                                            startCharacter = 4
                                            endLine = 1
                                            endCharacter = 21
                                        }
                                    },
                                    SymbolOccurrence {
                                        role = Role.DEFINITION
                                        symbol =
                                            "`Test.kt.accessor`#accessorspecial.accessor(kotlin/Int): kotlin/Unit."
                                        range {
                                            startLine = 2
                                            startCharacter = 4
                                            endLine = 2
                                            endCharacter = 36
                                        }
                                    })),
                ),
                ExpectedSymbols(
                    "class constructor properties",
                    SourceFile.testKt(
                        """
                        |class Test(var sample: Int, text: String): Throwable(sample.toString()) {
                        |    fun test() {
                        |        println(sample)
                        |    }
                        |}
                        |""".trimMargin()),
                    semanticdb =
                        SemanticdbData(
                            expectedOccurrences =
                                listOf(
                                    SymbolOccurrence {
                                        role = Role.DEFINITION
                                        symbol = "Test#Test.Test(kotlin/Int, kotlin/String): Test."
                                        range {
                                            startLine = 0
                                            startCharacter = 10
                                            endLine = 0
                                            endCharacter = 40
                                        }
                                    },
                                    SymbolOccurrence {
                                        role = Role.REFERENCE
                                        symbol = "(sample)"
                                        range {
                                            startLine = 0
                                            startCharacter = 53
                                            endLine = 0
                                            endCharacter = 58
                                        }
                                    },
                                    SymbolOccurrence {
                                        role = Role.DEFINITION
                                        symbol = "Test#sample."
                                        range {
                                            startLine = 0
                                            startCharacter = 11
                                            endLine = 0
                                            endCharacter = 25
                                        }
                                    },
                                    SymbolOccurrence {
                                        role = Role.REFERENCE
                                        symbol = "(sample)"
                                        range {
                                            startLine = 0
                                            startCharacter = 11
                                            endLine = 0
                                            endCharacter = 25
                                        }
                                    },
                                    SymbolOccurrence {
                                        role = Role.DEFINITION
                                        symbol = "Test#accessorspecial.accessor(): kotlin/Int."
                                        range {
                                            startLine = 0
                                            startCharacter = 11
                                            endLine = 0
                                            endCharacter = 25
                                        }
                                    },
                                    SymbolOccurrence {
                                        role = Role.DEFINITION
                                        symbol =
                                            "Test#accessorspecial.accessor(kotlin/Int): kotlin/Unit."
                                        range {
                                            startLine = 0
                                            startCharacter = 11
                                            endLine = 0
                                            endCharacter = 25
                                        }
                                    },
                                ))))
            .mapCheckExpectedSymbols()

    @TestFactory
    fun `class constructors`() =
        listOf(
                ExpectedSymbols(
                    "implicit primary constructor",
                    SourceFile.testKt(
                        """
                        |class Banana
                        |""".trimMargin()),
                    semanticdb =
                        SemanticdbData(
                            expectedOccurrences =
                                listOf(
                                    SymbolOccurrence {
                                        role = Role.DEFINITION
                                        symbol = "Banana#"
                                        range {
                                            startLine = 0
                                            startCharacter = 0
                                            endLine = 0
                                            endCharacter = 11
                                        }
                                    },
                                    SymbolOccurrence {
                                        role = Role.DEFINITION
                                        symbol = "Banana#Banana.Banana(): Banana."
                                        range {
                                            startLine = 0
                                            startCharacter = 0
                                            endLine = 0
                                            endCharacter = 11
                                        }
                                    },
                                ))),
                ExpectedSymbols(
                    "explicit primary constructor without keyword",
                    SourceFile.testKt(
                        """
                        |class Banana(size: Int)
                        |""".trimMargin()),
                    semanticdb =
                        SemanticdbData(
                            expectedOccurrences =
                                listOf(
                                    SymbolOccurrence {
                                        role = Role.DEFINITION
                                        symbol = "Banana#"
                                        range {
                                            startLine = 0
                                            startCharacter = 0
                                            endLine = 0
                                            endCharacter = 22
                                        }
                                    },
                                    SymbolOccurrence {
                                        role = Role.DEFINITION
                                        symbol = "Banana#Banana.Banana(kotlin/Int): Banana."
                                        range {
                                            startLine = 0
                                            startCharacter = 12
                                            endLine = 0
                                            endCharacter = 22
                                        }
                                    },
                                ))),
                ExpectedSymbols(
                    "explicit primary constructor with keyword",
                    SourceFile.testKt(
                        """
                        |class Banana constructor(size: Int)
                        |""".trimMargin()),
                    semanticdb =
                        SemanticdbData(
                            expectedOccurrences =
                                listOf(
                                    SymbolOccurrence {
                                        role = Role.DEFINITION
                                        symbol = "Banana#"
                                        range {
                                            startLine = 0
                                            startCharacter = 0
                                            endLine = 0
                                            endCharacter = 34
                                        }
                                    },
                                    SymbolOccurrence {
                                        role = Role.DEFINITION
                                        symbol = "Banana#Banana.Banana(kotlin/Int): Banana."
                                        range {
                                            startLine = 0
                                            startCharacter = 13
                                            endLine = 0
                                            endCharacter = 34
                                        }
                                    },
                                ))))
            .mapCheckExpectedSymbols()

    @TestFactory
    fun `Single Abstract Method interface`() =
        listOf(
                ExpectedSymbols(
                    "basic java.lang.Runnable",
                    SourceFile.testKt(
                        """
                        |val x = Runnable { }.run()
                        |""".trimMargin()),
                    semanticdb =
                        SemanticdbData(
                            expectedOccurrences =
                                listOf(
                                    SymbolOccurrence {
                                        role = Role.REFERENCE
                                        symbol =
                                            "`java/lang/Runnable`#runjava.lang.run(): kotlin/Unit."
                                        range {
                                            startLine = 0
                                            startCharacter = 8
                                            endLine = 0
                                            endCharacter = 25
                                        }
                                    },
                                    SymbolOccurrence {
                                        role = Role.REFERENCE
                                        symbol =
                                            "`java.lang.Runnable`#Runnablejava.lang.Runnable(kotlin/Function0<kotlin/Unit>): java/lang/Runnable."
                                        range {
                                            startLine = 0
                                            startCharacter = 8
                                            endLine = 0
                                            endCharacter = 19
                                        }
                                    }))))
            .mapCheckExpectedSymbols()

    @Test
    fun kdoc() =
        listOf(
                ExpectedSymbols(
                    "empty kdoc line",
                    SourceFile.testKt(
                        """
                    |/**
                    |
                    |hello world
                    |* test content
                    |*/
                    |val x = ""
                    |""".trimMargin()),
                    semanticdb =
                        SemanticdbData(
                            expectedSymbols =
                                listOf(
                                    SymbolInformation {
                                        symbol = "`Test.kt.x`#x."
                                        displayName = "x"
                                        language = Language.KOTLIN
                                        documentation {
                                            message =
                                                "```\npublic final val x: R|kotlin/String| = String()\n    public get(): R|kotlin/String|\n\n```\n\n\n----\n\n\nhello world\n test content\n"
                                            format = Format.MARKDOWN
                                        }
                                    },
                                    SymbolInformation {
                                        symbol =
                                            "`Test.kt.accessor`#accessorspecial.accessor(): kotlin/String."
                                        displayName = "x"
                                        language = Language.KOTLIN
                                        documentation {
                                            message =
                                                "```\npublic get(): R|kotlin/String|\n```\n\n\n----\n\n\nhello world\n test content\n"
                                            format = Format.MARKDOWN
                                        }
                                    }))))
            .mapCheckExpectedSymbols()
}
