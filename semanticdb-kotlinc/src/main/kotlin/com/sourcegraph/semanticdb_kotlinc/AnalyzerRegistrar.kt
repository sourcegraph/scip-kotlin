package com.sourcegraph.semanticdb_kotlinc

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import kotlin.contracts.ExperimentalContracts
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrarAdapter

@OptIn(ExperimentalCompilerApi::class)
@ExperimentalContracts
class AnalyzerRegistrar(private val callback: (Semanticdb.TextDocument) -> Unit = {}) :
    CompilerPluginRegistrar() {
    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        println("Ernald - AnalyzerRegistrar")
        //        AnalysisHandlerExtension.registerExtension(
        //            Analyzer(
        //                sourceroot = configuration[KEY_SOURCES]
        //                        ?: throw IllegalArgumentException("configuration key $KEY_SOURCES
        // missing"),
        //                targetroot = configuration[KEY_TARGET]
        //                        ?: throw IllegalArgumentException("configuration key $KEY_TARGET
        // missing"),
        //                callback = callback))

        FirExtensionRegistrarAdapter.registerExtension(
            AnalyzerFirExtensionRegistrar(
                sourceroot = configuration[KEY_SOURCES]!!,
                targetroot = configuration[KEY_TARGET]!!,
                callback = callback))
        IrGenerationExtension.registerExtension(PostAnalysisExtension(
            sourceRoot = configuration[KEY_SOURCES]!!,
            targetRoot = configuration[KEY_TARGET]!!,
            callback = callback))
    }

    override val supportsK2: Boolean
        get() = true
}
