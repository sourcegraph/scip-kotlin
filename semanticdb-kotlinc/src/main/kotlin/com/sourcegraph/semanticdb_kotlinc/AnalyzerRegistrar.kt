package com.sourcegraph.semanticdb_kotlinc

//import org.jetbrains.kotlin.com.intellij.mock.MockProject
import com.intellij.mock.MockProject
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.resolve.jvm.extensions.AnalysisHandlerExtension
import java.lang.IllegalArgumentException
import kotlin.contracts.ExperimentalContracts

@ExperimentalContracts
class AnalyzerRegistrar: ComponentRegistrar {
    override fun registerProjectComponents(project: MockProject, configuration: CompilerConfiguration) {
        AnalysisHandlerExtension.registerExtension(
            project, Analyzer(
                sourceroot = configuration[KEY_SOURCES] ?: throw IllegalArgumentException("configuration key $KEY_SOURCES missing"),
                targetroot = configuration[KEY_TARGET] ?: throw IllegalArgumentException("configuration key $KEY_TARGET missing"),
            )
        )
    }
}