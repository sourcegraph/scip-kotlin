package com.sourcegraph.semanticdb_kotlinc.test

import com.sourcegraph.semanticdb_kotlinc.*
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import io.kotest.assertions.assertSoftly
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.matchers.collections.shouldContainInOrder
import io.kotest.matchers.shouldBe
import org.jetbrains.kotlin.analyzer.AnalysisResult
import org.jetbrains.kotlin.com.intellij.mock.MockProject
import org.jetbrains.kotlin.com.intellij.openapi.project.Project
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.resolve.BindingTrace
import org.jetbrains.kotlin.resolve.jvm.extensions.AnalysisHandlerExtension
import org.junit.jupiter.api.DynamicTest.*
import org.junit.jupiter.api.TestFactory
import kotlin.contracts.ExperimentalContracts

@ExperimentalContracts
class SymbolsCacheTest {
    data class ExpectedSymbols(
        val testName: String,
        val source: SourceFile,
        val expectedGlobals: List<Symbol>,
        val localsCount: Int? = null
    )

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
                listOf("Test#sample().".symbol(), "Test#sample(+1).".symbol())
            ),
            ExpectedSymbols(
                "Inline class constructor",
                SourceFile.testKt("""
                    class Test(val x: Int)
                """),
                listOf("Test#`<init>`().(x)".symbol())
            ),
            ExpectedSymbols(
                "Inline + secondary class constructors",
                SourceFile.testKt("""
                    class Test(val x: Int) {
                        constructor(y: Long): this(y.toInt())
                        constructor(z: String): this(z.toInt())
                    }
                """),
                listOf(
                    "Test#`<init>`(+2).(x)".symbol(),
                    "Test#`<init>`().(y)".symbol(),
                    "Test#`<init>`(+1).(z)".symbol()
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
                listOf("Test#test().".symbol(), "Test#test(+1).".symbol())
            ),
            ExpectedSymbols(
                "Top level overloaded functions",
                SourceFile.testKt("""
                    fun test() {}
                    fun test(x: Int) {}
                """),
                listOf("TestKt#test().".symbol(), "TestKt#test(+1).(x)".symbol())
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
                listOf("main/Test#".symbol()), 0
            ),
            ExpectedSymbols(
                "multi component package name",
                SourceFile.testKt("""
                    package test.sample.main
                    
                    class Test
                """),
                listOf("test/sample/main/Test#".symbol()), 0
            ),
            ExpectedSymbols(
                "no package name",
                SourceFile.testKt("""
                    class Test
                """),
                listOf("Test#".symbol()), 0
            )
        ).mapCheckExpectedSymbols()

    @TestFactory
    fun `builtin symbols`() =
        listOf(
            ExpectedSymbols(
                "builtin types",
                SourceFile.testKt("""
                    var x: Int = 1
                    lateinit var y: Unit
                    lateinit var z: Any
                    lateinit var w: Nothing
                """),
                listOf("kotlin/Int#".symbol(), "kotlin/Unit#".symbol(), "kotlin/Any#".symbol(), "kotlin/Nothing#".symbol())
            ),
            ExpectedSymbols(
                "builtin functions",
                SourceFile.testKt(
                    """
                    val x = mapOf<Void, Void>()
                    fun main() {
                        println()
                    }
                """
                ),
                listOf("kotlin/collections/MapsKt#mapOf(+1).".symbol(), "kotlin/io/ConsoleKt#println().".symbol())
            )
        ).mapCheckExpectedSymbols()


    companion object {
        fun checkContainsExpectedSymbols(source: SourceFile, expectedGlobals: List<Symbol>, localsCount: Int? = null) {
            val globals = GlobalSymbolsCache(testing = true)
            val locals = LocalSymbolsCache()
            val analyzer = symbolVisitorAnalyzer(globals, locals)
            val compilation = KotlinCompilation().apply {
                sources = listOf(source)
                compilerPlugins = listOf(analyzer)
                verbose = false
            }
            val result = shouldNotThrowAny { compilation.compile() }
            result.exitCode shouldBe KotlinCompilation.ExitCode.OK
            assertSoftly(globals) {
                this.iterator().asSequence().toList().shouldContainInOrder(expectedGlobals)
            }
            localsCount?.also { locals.size shouldBe it }
        }
    }
}

@ExperimentalContracts
fun symbolVisitorAnalyzer(globals: GlobalSymbolsCache, locals: LocalSymbolsCache): ComponentRegistrar {
    return object: ComponentRegistrar {
        override fun registerProjectComponents(project: MockProject, configuration: CompilerConfiguration) {
            AnalysisHandlerExtension.registerExtension(project, object: AnalysisHandlerExtension {
                override fun analysisCompleted(
                    project: Project,
                    module: ModuleDescriptor,
                    bindingTrace: BindingTrace,
                    files: Collection<KtFile>
                ): AnalysisResult? {
                    val resolver = DescriptorResolver(bindingTrace).also { globals.resolver = it }
                    SymbolGenVisitor(resolver, globals, locals).visitKtFile(files.first(), Unit)
                    return super.analysisCompleted(project, module, bindingTrace, files)
                }
            })
        }
    }
}
