package com.sourcegraph.semanticdb_kotlinc

import org.jetbrains.kotlin.KtSourceFile
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.contracts.ExperimentalContracts

class PostAnalysisExtension(
    private val sourceRoot: Path,
    private val targetRoot: Path,
    private val callback: (Semanticdb.TextDocument) -> Unit): IrGenerationExtension {
    @OptIn(ExperimentalContracts::class)
    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        println("Ernald - generate - count:${AnalyzerCheckers.visitors.size}")
        for ((ktSourceFile, visitor) in AnalyzerCheckers.visitors) {
            println("Ernald - callback1")
            val document = visitor.build()
            println("Ernald - callback2")
            semanticdbOutPathForFile(ktSourceFile)?.apply {
                println("Ernald - callback3")
                Files.write(this, TextDocuments { addDocuments(document) }.toByteArray())
                println("Ernald - callback4")
            }
            println("Ernald - callback5")
            callback(document)
            println("Ernald - callback6")
        }
//        AnalyzerCheckers.visitors.forEach { (ktSourceFile, visitor) ->  {
//                println("Ernald - callback1")
//                val document = visitor.build()
//                println("Ernald - callback2")
//                semanticdbOutPathForFile(ktSourceFile)?.apply {
//                    println("Ernald - callback3")
//                    Files.write(this, TextDocuments { addDocuments(document) }.toByteArray())
//                    println("Ernald - callback4")
//                }
//                println("Ernald - callback5")
//                callback(document)
//                println("Ernald - callback6")
//            }
//        }
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
