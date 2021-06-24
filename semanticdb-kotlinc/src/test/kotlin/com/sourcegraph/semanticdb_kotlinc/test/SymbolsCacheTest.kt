package com.sourcegraph.semanticdb_kotlinc.test

import com.sourcegraph.semanticdb_kotlinc.*
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import io.kotest.assertions.asClue
import io.kotest.assertions.assertSoftly
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.matchers.collections.shouldContainAll
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
import org.jetbrains.kotlin.resolve.DescriptorUtils
import org.jetbrains.kotlin.resolve.jvm.extensions.AnalysisHandlerExtension
import org.jetbrains.kotlin.types.TypeUtils
import org.jetbrains.kotlin.types.isNullable
import org.jetbrains.kotlin.types.typeUtil.isTypeParameter
import org.jetbrains.kotlin.types.typeUtil.makeNotNullable
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.*
import org.junit.jupiter.api.TestFactory
import kotlin.contracts.ExperimentalContracts
import kotlin.test.Test

@ExperimentalContracts
class SymbolsCacheTest {
    data class ExpectedSymbols(val source: String, val globalChecks: Array<Symbol>, val localsCount: Int? = null)
    
    @TestFactory
    fun `method disambiguator`() =
        listOf(
            ExpectedSymbols("""
                class Test {
                    fun sample() {}
                    fun sample(x: Int) {}
                }
                """.trimIndent(),
                arrayOf("Test#sample().".symbol(), "Test#sample(+1).".symbol())),
            ExpectedSymbols("""
                class Test(val x: Int) {}
                """.trimIndent(),
                arrayOf("Test#`<init>`().(x)".symbol())),
            ExpectedSymbols("""
                class Test(val x: Int) {
                    constructor(y: Long): this(y.toInt())
                    constructor(z: String): this(z.toInt())
                }
                """.trimIndent(),
                arrayOf("Test#`<init>`(+2).(x)".symbol(), "Test#`<init>`().(y)".symbol(), "Test#`<init>`(+1).(z)".symbol())),
            ExpectedSymbols("""
                class Test {
                    fun sample() {}
                    fun test() {}
                    fun test(x: Int) {}
                }
                """.trimIndent(),
                arrayOf("Test#test().".symbol(), "Test#test(+1).".symbol()))
        ).mapIndexed { index, (source, globalChecks) ->
            dynamicTest("File number ${index + 1}: ${source.lines().first()}") {
                checkContainsExpectedSymbols(source, globalChecks)
            }
        }

    @TestFactory
    fun `check package symbols`() =
        listOf(
            ExpectedSymbols("""
                package main
                
                class Test {}
                """.trimIndent(),
                arrayOf("main/Test#".symbol()), 0
            ),
            ExpectedSymbols("""
                package test.sample.main
                
                class Test {}
                """.trimIndent(),
                arrayOf("test/sample/main/Test#".symbol()), 0
            ),
            ExpectedSymbols("""
                class Test {}
                """.trimIndent(),
                arrayOf("Test#".symbol()), 0
            )
        ).mapIndexed { index, (source, globalChecks, localsCount) ->
            dynamicTest("File number ${index + 1}: ${source.lines().first()}") {
                checkContainsExpectedSymbols(source, globalChecks, localsCount)
            }
        }

    private fun checkContainsExpectedSymbols(source: String, globalChecks: Array<Symbol>, localsCount: Int? = null) {
        val source = SourceFile.kotlin("Test.kt", source)
        val globals = GlobalSymbolsCache(testing = true)
        val locals = LocalSymbolsCache()
        val analyzer = allVisitorAnalyzer(globals, locals)
        val compilation = KotlinCompilation().apply {
            sources = listOf(source)
            compilerPlugins = listOf(analyzer)
            verbose = false
        }
        val result = shouldNotThrowAny { compilation.compile() }
        result.exitCode shouldBe KotlinCompilation.ExitCode.OK
        assertSoftly(globals) {
            this.iterator().asSequence().toList().shouldContainInOrder(*globalChecks)
        }
        localsCount?.also { locals.size shouldBe it }
    }
}

@ExperimentalContracts
fun allVisitorAnalyzer(globals: GlobalSymbolsCache, locals: LocalSymbolsCache): ComponentRegistrar {
    return object: ComponentRegistrar {
        override fun registerProjectComponents(project: MockProject, configuration: CompilerConfiguration) {
            AnalysisHandlerExtension.registerExtension(project, object : AnalysisHandlerExtension {
                override fun analysisCompleted(project: Project, module: ModuleDescriptor, bindingTrace: BindingTrace, files: Collection<KtFile>): AnalysisResult? {
                    val resolver = DescriptorResolver(bindingTrace).also { globals.resolver = it }
                    object : KtTreeVisitorVoid() {
                        override fun visitClass(klass: KtClass) {
                            val desc = resolver.fromDeclaration(klass)!!
                            globals[desc, locals]
                            super.visitClass(klass)
                        }

                        override fun visitNamedFunction(function: KtNamedFunction) {
                            val desc = resolver.fromDeclaration(function)!!
                            globals[desc, locals]
                            super.visitNamedFunction(function)
                        }

                        override fun visitProperty(property: KtProperty) {
                            val desc = resolver.fromDeclaration(property)!!
                            globals[desc, locals]
                            super.visitProperty(property)
                        }

                        override fun visitParameter(parameter: KtParameter) {
                            val desc = resolver.fromDeclaration(parameter)!!
                            globals[desc, locals]
                            super.visitParameter(parameter)
                        }

                        override fun visitTypeParameter(parameter: KtTypeParameter) {
                            val desc = resolver.fromDeclaration(parameter)!!
                            globals[desc, locals]
                            super.visitTypeParameter(parameter)
                        }

                        override fun visitTypeAlias(typeAlias: KtTypeAlias) {
                            val desc = resolver.fromDeclaration(typeAlias)!!
                            globals[desc, locals]
                            super.visitTypeAlias(typeAlias)
                        }

                        override fun visitTypeReference(typeReference: KtTypeReference) {
                            val type = resolver.fromTypeReference(typeReference).let {
                                if (it.isNullable()) return@let it.makeNotNullable()
                                else return@let it
                            }
                            val desc = if (!type.isTypeParameter()) {
                                DescriptorUtils.getClassDescriptorForType(type)
                            } else {
                                TypeUtils.getTypeParameterDescriptorOrNull(type)!!
                            }
                            globals[desc, locals]
                            super.visitTypeReference(typeReference)
                        }
                    }.visitKtFile(files.first())

                    return super.analysisCompleted(project, module, bindingTrace, files)
                }
            })
        }
    }
}
