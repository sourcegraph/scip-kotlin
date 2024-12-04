package com.sourcegraph.semanticdb_kotlinc

import java.lang.IllegalArgumentException
import kotlin.contracts.ExperimentalContracts
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.resolve.jvm.extensions.AnalysisHandlerExtension

@OptIn(ExperimentalCompilerApi::class)
@ExperimentalContracts
class AnalyzerRegistrar(private val callback: (Semanticdb.TextDocument) -> Unit = {}) :
    CompilerPluginRegistrar() {
    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        AnalysisHandlerExtension.registerExtension(
            Analyzer(
                sourceroot = configuration[KEY_SOURCES]
                        ?: throw IllegalArgumentException("configuration key $KEY_SOURCES missing"),
                targetroot = configuration[KEY_TARGET]
                        ?: throw IllegalArgumentException("configuration key $KEY_TARGET missing"),
                callback = callback))
    }

    override val supportsK2: Boolean
        get() = false
}
