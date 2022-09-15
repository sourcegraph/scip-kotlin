package com.sourcegraph.semanticdb_kotlinc

import java.lang.IllegalArgumentException
import kotlin.contracts.ExperimentalContracts
import org.jetbrains.kotlin.com.intellij.mock.MockProject
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.resolve.jvm.extensions.AnalysisHandlerExtension

@ExperimentalContracts
class AnalyzerRegistrar(private val callback: (Semanticdb.TextDocument) -> Unit = {}) :
    ComponentRegistrar {
    override fun registerProjectComponents(
        project: MockProject,
        configuration: CompilerConfiguration
    ) {
        val targetRoot = configuration[KEY_TARGET]
            ?: throw IllegalArgumentException("configuration key $KEY_TARGET missing")
        val analyzer = if (configuration[KEY_BUILD_TOOL] ?: "" == "bazel") {
            Analyzer(targetroot = targetRoot.parent, callback = callback)
        } else {
            Analyzer(
                    sourceroot = configuration[KEY_SOURCES]
                            ?: throw IllegalArgumentException("configuration key $KEY_SOURCES missing"),
                    targetroot = targetRoot,
                    callback = callback)
        }
        AnalysisHandlerExtension.registerExtension(project, analyzer)
    }
}
