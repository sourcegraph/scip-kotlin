package com.sourcegraph.semanticdb_kotlinc

import java.nio.file.Path
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.extensions.FirExtensionSessionComponent

open class AnalyzerParamsProvider(
    session: FirSession,
    sourceroot: Path,
    targetroot: Path,
    callback: (Semanticdb.TextDocument) -> Unit
) : FirExtensionSessionComponent(session) {
    companion object {
        fun getFactory(
            sourceroot: Path,
            targetroot: Path,
            callback: (Semanticdb.TextDocument) -> Unit
        ): Factory {
            return Factory { AnalyzerParamsProvider(it, sourceroot, targetroot, callback) }
        }
    }

    val sourceroot: Path = sourceroot
    val targetroot: Path = targetroot
    val callback: (Semanticdb.TextDocument) -> Unit = callback
}

val FirSession.analyzerParamsProvider: AnalyzerParamsProvider by FirSession
    .sessionComponentAccessor()
