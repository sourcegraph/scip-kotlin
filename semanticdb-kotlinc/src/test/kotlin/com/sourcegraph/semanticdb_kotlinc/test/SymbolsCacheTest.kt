package com.sourcegraph.semanticdb_kotlinc.test
/*

import com.sourcegraph.semanticdb_kotlinc.*
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import io.kotest.assertions.asClue
import io.kotest.assertions.assertSoftly
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldContainAnyOf
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
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import kotlin.contracts.ExperimentalContracts
import kotlin.test.Test

@ExperimentalContracts
class SymbolsCacheTest {
    @ParameterizedTest
    @MethodSource("methodDisambiguatorTestPairs")
    fun `method disambiguator`() {
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
    }

    @ParameterizedTest
    @MethodSource("packageTestPairs")
    fun `check package symbols`(source: String, globalChecks: Array<Symbol>, localsCount: Int) = checkContainsExpectedSymbols(source, globalChecks, localsCount)

    private fun checkContainsExpectedSymbols(source: String, globalChecks: Array<Symbol>, localsCount: Int? = null) {
        val source = SourceFile.kotlin("Test.kt", source)
        val globals = GlobalSymbolsCache()
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
            (this.iterator().asSequence().toList()).asClue {
                it.shouldContainAll(*globalChecks)
            }
            // globalChecks.forEach {
            //     this shouldContain it
            // }
        }
        localsCount?.also { locals.size shouldBe it }
    }

    companion object {
        @JvmStatic
        fun packageTestPairs() = listOf(
            Arguments.of("""
                package main
                
                class Test {}
                """.trimIndent(),
                arrayOf("main/Test#".symbol()), 0
            ),
            Arguments.of("""
                package test.sample.main
                
                class Test {}
                """.trimIndent(),
                arrayOf("tet/sample/main/Test#".symbol()), 0
            ),
            Arguments.of("""
                class Test {}
                """.trimIndent(),
                arrayOf("Test#".symbol()), 0
            )
        )

        @JvmStatic
        fun methodDisambiguatorTestPairs() = listOf(
            Arguments.of("""
                
            """.trimIndent())
        )
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
                            val symbol = globals[desc, locals]
                            println("NAMED TYPE $klass ${desc.name} $symbol")
                            super.visitClass(klass)
                        }

                        override fun visitNamedFunction(function: KtNamedFunction) {
                            val desc = resolver.fromDeclaration(function)!!
                            val symbol = globals[desc, locals]
                            println("NAMED FUN $function ${desc.name} $symbol")
                            super.visitNamedFunction(function)
                        }

                        override fun visitProperty(property: KtProperty) {
                            val desc = resolver.fromDeclaration(property)!!
                            val symbol = globals[desc, locals]
                            println("NAMED PROP $property ${desc.name} $symbol")
                            super.visitProperty(property)
                        }

                        override fun visitParameter(parameter: KtParameter) {
                            val desc = resolver.fromDeclaration(parameter)!!
                            val symbol = globals[desc, locals]
                            println("NAMED PARAM $parameter ${desc.name} $symbol")
                            super.visitParameter(parameter)
                        }

                        override fun visitTypeParameter(parameter: KtTypeParameter) {
                            val desc = resolver.fromDeclaration(parameter)!!
                            val symbol = globals[desc, locals]
                            println("NAMED TYPE-PARAM $parameter ${desc.name} $symbol")
                            super.visitTypeParameter(parameter)
                        }

                        override fun visitTypeAlias(typeAlias: KtTypeAlias) {
                            val desc = resolver.fromDeclaration(typeAlias)!!
                            val symbol = globals[desc, locals]
                            println("NAMED TYPE-ALIAS $typeAlias ${desc.name} $symbol")
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
                            val symbol = globals[desc, locals]
                            println("TYPE REFERENCE $typeReference $type ${desc.name} $symbol")
                            super.visitTypeReference(typeReference)
                        }
                    }.visitKtFile(files.first())

                    return super.analysisCompleted(project, module, bindingTrace, files)
                }
            })
        }
    }
}*/
