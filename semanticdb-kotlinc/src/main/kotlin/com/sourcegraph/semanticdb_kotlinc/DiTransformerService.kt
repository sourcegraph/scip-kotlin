package com.sourcegraph.semanticdb_kotlinc

import java.nio.file.Path
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.extensions.FirExtensionSessionComponent

class DiTransformerService(
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
            return Factory { DiTransformerService(it, sourceroot, targetroot, callback) }
        }
    }

    val sourceroot: Path = sourceroot
    val targetroot: Path = targetroot
    val callback: (Semanticdb.TextDocument) -> Unit = callback
}

val FirSession.diTransformerService: DiTransformerService by FirSession.sessionComponentAccessor()
