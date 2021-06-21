package com.sourcegraph.semanticdb_kotlinc.test

import com.sourcegraph.semanticdb_kotlinc.*
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.matchers.shouldBe
import org.jetbrains.kotlin.analyzer.AnalysisResult
import org.jetbrains.kotlin.com.intellij.mock.MockProject
import org.jetbrains.kotlin.com.intellij.openapi.project.Project
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.KtTreeVisitorVoid
import org.jetbrains.kotlin.resolve.BindingTrace
import org.jetbrains.kotlin.resolve.jvm.extensions.AnalysisHandlerExtension
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import kotlin.contracts.ExperimentalContracts
import kotlin.test.Test

@ExperimentalContracts
class SymbolsCacheTest {
    @Test
    fun `method disambiguator`() {
        val globals = GlobalSymbolsCache()
        val locals = LocalSymbolsCache()

        val analyzer = object: ComponentRegistrar {
            override fun registerProjectComponents(project: MockProject, configuration: CompilerConfiguration) {
                AnalysisHandlerExtension.registerExtension(project, object : AnalysisHandlerExtension {
                    override fun analysisCompleted(project: Project, module: ModuleDescriptor, bindingTrace: BindingTrace, files: Collection<KtFile>): AnalysisResult? {
                        val resolver = DescriptorResolver(bindingTrace).also { globals.resolver = it }
                        object : KtTreeVisitorVoid() {
                            override fun visitNamedFunction(function: KtNamedFunction) {
                                val desc = resolver.fromDeclaration(function)!!
                                globals[desc, locals]
                                super.visitNamedFunction(function)
                            }

                            override fun visitParameter(parameter: KtParameter) {
                                val desc = resolver.fromDeclaration(parameter)!!
                                globals[desc, locals]
                                super.visitParameter(parameter)
                            }
                        }.visitKtFile(files.first())

                        return super.analysisCompleted(project, module, bindingTrace, files)
                    }
                })
            }
        }

        val source = SourceFile.kotlin("Banana.kt", """
            @file:Suppress("UNUSED_VARIABLE", "UNUSED_PARAMETER")
            package test

            import kotlin.collections.Map

            class Test {
                fun sample() {}

                fun sample(s: String) {}
            }
    
            fun sampletext() {}
            
            val searchQueryGenerators = mapOf<String, (options: List<String>) -> String>(
                "search" to { it[0] },
                "raw-search" to {
                    it.fold("") { acc, next ->
                        acc + next
                    }
                }
            )
        """.trimIndent())

        val compilation = KotlinCompilation().apply {
            sources = listOf(source)
            compilerPlugins = listOf(analyzer)
            verbose = false
        }

        val result = shouldNotThrowAny { compilation.compile() }
        result.exitCode shouldBe KotlinCompilation.ExitCode.OK
        println()
        locals.iterator.forEach {
            println("${it.key.javaClass} ${it.key} ${it.value}")
        }
        locals.size shouldBe 3
    }

    @ParameterizedTest
    @MethodSource("semanticdbPairs")
    fun `semanticdb symbols check`(source: String, syms: Array<Symbol>) {

    }

    companion object {
        @JvmStatic
        fun semanticdbPairs() = listOf(
            Arguments.of("""
                
            """.trimIndent(), arrayOf(Symbol("")))
        )

    }
}