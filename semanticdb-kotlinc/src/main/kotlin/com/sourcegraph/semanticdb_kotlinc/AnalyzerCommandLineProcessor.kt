package com.sourcegraph.semanticdb_kotlinc

import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.config.CompilerConfiguration

class AnalyzerCommandLineProcessor: CommandLineProcessor {
    override val pluginId: String
        get() = "com.sourcegraph.lsifjava"
    override val pluginOptions: Collection<AbstractCliOption>
        get() = listOf()

    override fun processOption(option: AbstractCliOption, value: String, configuration: CompilerConfiguration) {
        println("asdf")
        //super.processOption(option, value, configuration)
    }
}