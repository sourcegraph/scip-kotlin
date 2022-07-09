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
    val sourceroot: Path? = null,
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
            val sourceRootPath = sourceroot ?: inferBazelSourceRoot(file)
            val document = SemanticdbVisitor(sourceRootPath, resolver, file, lineMap, globals).build()
            semanticdbOutPathForFile(file, sourceRootPath)?.apply {
                Files.write(this, TextDocuments { addDocuments(document) }.toByteArray())
            }
            callback(document)
        }

        return super.analysisCompleted(project, module, bindingTrace, files)
    }

    private fun inferBazelSourceRoot(file: KtFile): Path {
        val pathElements = file.virtualFilePath.split("/")
        return Paths.get(pathElements.take(pathElements.indexOf("execroot") + 2).joinToString("/"))
    }

    private fun semanticdbOutPathForFile(file: KtFile, sourceRootPath: Path): Path? {
        val normalizedPath = Paths.get(file.virtualFilePath).normalize()
        if (normalizedPath.startsWith(sourceRootPath)) {
            val relative = sourceRootPath.relativize(normalizedPath)
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
