package com.sourcegraph.semanticdb_kotlinc

import java.nio.file.Path
import java.nio.file.Paths
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey

const val VAL_SOURCES = "sourceroot"
val KEY_SOURCES = CompilerConfigurationKey<Path>(VAL_SOURCES)

const val VAL_TARGET = "targetroot"
val KEY_TARGET = CompilerConfigurationKey<Path>(VAL_TARGET)

const val VAL_BUILD_TOOL = "buildtool"
val KEY_BUILD_TOOL = CompilerConfigurationKey<String>(VAL_BUILD_TOOL)

class AnalyzerCommandLineProcessor : CommandLineProcessor {
    override val pluginId: String = "semanticdb-kotlinc"
    override val pluginOptions: Collection<AbstractCliOption> =
        listOf(
            CliOption(
                VAL_SOURCES,
                "<path>",
                "the absolute path to the root of the Kotlin sources",
                required = false),
            CliOption(
                VAL_TARGET,
                "<path>",
                "the absolute path to the directory where to generate SemanticDB files.",
                required = false),
            CliOption(
                    VAL_BUILD_TOOL,
                    "<build-tool>",
                    "the build tool used, for example bazel, sbt, gradle.",
                    required = false),
        )

    override fun processOption(
        option: AbstractCliOption,
        value: String,
        configuration: CompilerConfiguration
    ) {
        when (option.optionName) {
            VAL_SOURCES -> configuration.put(KEY_SOURCES, Paths.get(value))
            VAL_TARGET -> configuration.put(KEY_TARGET, Paths.get(value))
            VAL_BUILD_TOOL -> configuration.put(KEY_BUILD_TOOL, value)
        }
    }
}
