package com.sourcegraph.semanticdb_kotlinc

import java.nio.file.Path
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.extensions.FirExtensionSessionComponent
import org.jetbrains.kotlin.fir.extensions.FirExtensionSessionComponent.Factory

open class AnalyzerParamsProvider(
    session: FirSession,
    sourceroot: Path,
    targetroot: Path,
) : FirExtensionSessionComponent(session) {
    companion object {
        fun getFactory(
            sourceroot: Path,
            targetroot: Path,
        ): Factory {
            return Factory { AnalyzerParamsProvider(it, sourceroot, targetroot) }
        }
    }

    val sourceroot: Path = sourceroot
    val targetroot: Path = targetroot
}

val FirSession.analyzerParamsProvider: AnalyzerParamsProvider by FirSession
    .sessionComponentAccessor()
