package com.sourcegraph.semanticdb_kotlinc.test

import com.sourcegraph.semanticdb_kotlinc.AnalyzerCommandLineProcessor
import com.sourcegraph.semanticdb_kotlinc.AnalyzerRegistrar
import com.sourcegraph.semanticdb_kotlinc.Semanticdb.TextDocument
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.PluginOption
import com.tschuchort.compiletesting.SourceFile
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path
import kotlin.contracts.ExperimentalContracts
import kotlin.test.Test

@ExperimentalContracts
class AnalyzerTest {

    @Test
    fun `test standard`(@TempDir path: Path) {
        val buildPath = File(path.resolve("build").toString()).apply { mkdir() }

        val source = SourceFile.kotlin("Banana.kt", """
            package sample
            class Banana {
                fun foo() { }
            }
        """.trimIndent())

        lateinit var document: TextDocument
        val callback = { it: TextDocument -> document = it }

        val result = KotlinCompilation().apply {
            sources = listOf(source)
            compilerPlugins = listOf(AnalyzerRegistrar(callback))
            verbose = false
            messageOutputStream = System.err
            pluginOptions = listOf(
                PluginOption("com.sourcegraph.lsif-kotlin", "sourceroot", path.toString()),
                PluginOption("com.sourcegraph.lsif-kotlin", "targetroot", buildPath.toString())
            )
            commandLineProcessors = listOf(AnalyzerCommandLineProcessor())
            workingDir = path.toFile()
        }.compile()

        result.exitCode shouldBe KotlinCompilation.ExitCode.OK
        document.occurrencesList.map { it.symbol } shouldBe listOf("sample/Banana#", "sample/Banana#foo().")
    }
}