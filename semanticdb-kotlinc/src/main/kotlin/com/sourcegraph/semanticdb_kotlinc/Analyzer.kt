package com.sourcegraph.semanticdb_kotlinc

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.contracts.ExperimentalContracts
import org.jetbrains.kotlin.analyzer.AnalysisResult
import org.jetbrains.kotlin.com.intellij.openapi.project.Project
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.resolve.BindingTrace
import org.jetbrains.kotlin.resolve.jvm.extensions.AnalysisHandlerExtension

@ExperimentalContracts
class Analyzer(
    val sourceroot: Path,
    val targetroot: Path,
    val callback: (Semanticdb.TextDocument) -> Unit
) : AnalysisHandlerExtension {
    private val globals = GlobalSymbolsCache()

    override fun analysisCompleted(
        project: Project,
        module: ModuleDescriptor,
        bindingTrace: BindingTrace,
        files: Collection<KtFile>
    ): AnalysisResult? {
        val resolver = DescriptorResolver(bindingTrace).also { globals.resolver = it }
        for (file in files) {
            val lineMap = LineMap(project, file)
            val document = SemanticdbVisitor(sourceroot, resolver, file, lineMap, globals).build()
            semanticdbOutPathForFile(file)?.apply {
                Files.write(this, TextDocuments { addDocuments(document) }.toByteArray())
            }
            callback(document)
        }

        return super.analysisCompleted(project, module, bindingTrace, files)
    }

    private fun semanticdbOutPathForFile(file: KtFile): Path? {
        val normalizedPath = Paths.get(file.virtualFilePath).normalize()
        if (normalizedPath.startsWith(sourceroot)) {
            val relative = sourceroot.relativize(normalizedPath)
            val filename = relative.fileName.toString() + ".semanticdb"
            val semanticdbOutPath =
                targetroot
                    .resolve("META-INF")
                    .resolve("semanticdb")
                    .resolve(relative)
                    .resolveSibling(filename)

            Files.createDirectories(semanticdbOutPath.parent)
            return semanticdbOutPath
        }
        System.err.println(
            "given file is not under the sourceroot.\n\tSourceroot: $sourceroot\n\tFile path: ${file.virtualFilePath}\n\tNormalized file path: $normalizedPath")
        return null
    }
}
