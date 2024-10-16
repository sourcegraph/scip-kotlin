package com.sourcegraph.semanticdb_kotlinc

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.contracts.ExperimentalContracts
import org.jetbrains.kotlin.KtSourceFile
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment

class PostAnalysisExtension(
    private val sourceRoot: Path,
    private val targetRoot: Path,
    private val callback: (Semanticdb.TextDocument) -> Unit
) : IrGenerationExtension {
    @OptIn(ExperimentalContracts::class)
    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        for ((ktSourceFile, visitor) in AnalyzerCheckers.visitors) {
            val document = visitor.build()
            semanticdbOutPathForFile(ktSourceFile)?.apply {
                Files.write(this, TextDocuments { addDocuments(document) }.toByteArray())
            }
            callback(document)
        }
    }

    private fun semanticdbOutPathForFile(file: KtSourceFile): Path? {
        val normalizedPath = Paths.get(file.path).normalize()
        if (normalizedPath.startsWith(sourceRoot)) {
            val relative = sourceRoot.relativize(normalizedPath)
            val filename = relative.fileName.toString() + ".semanticdb"
            val semanticdbOutPath =
                targetRoot
                    .resolve("META-INF")
                    .resolve("semanticdb")
                    .resolve(relative)
                    .resolveSibling(filename)

            Files.createDirectories(semanticdbOutPath.parent)
            return semanticdbOutPath
        }
        System.err.println(
            "given file is not under the sourceroot.\n\tSourceroot: $sourceRoot\n\tFile path: ${file.path}\n\tNormalized file path: $normalizedPath")
        return null
    }
}
