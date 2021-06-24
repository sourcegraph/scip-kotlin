package com.sourcegraph.semanticdb_kotlinc

import org.jetbrains.kotlin.analyzer.AnalysisResult
import org.jetbrains.kotlin.com.intellij.openapi.project.Project
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.resolve.BindingTrace
import org.jetbrains.kotlin.resolve.jvm.extensions.AnalysisHandlerExtension
import java.lang.IllegalStateException
import java.nio.file.Files
import java.nio.file.Path
import kotlin.contracts.ExperimentalContracts

@ExperimentalContracts
class Analyzer(val sourceroot: Path, val targetroot: Path, val callback: (Semanticdb.TextDocument) -> Unit): AnalysisHandlerExtension {
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
            val document = TextDocumentBuildingVisitor(sourceroot, resolver, file, lineMap, globals).build()
            Files.write(semanticdbOutPathForFile(file), TextDocuments { addDocuments(document) }.toByteArray())
            callback(document)
        }

        return super.analysisCompleted(project, module, bindingTrace, files)
    }

    private fun semanticdbOutPathForFile(file: KtFile): Path {
        val normalizedPath = Path.of(file.virtualFilePath).normalize()
        if (normalizedPath.startsWith(sourceroot)) {
            val relative = sourceroot.relativize(normalizedPath)
            val filename = relative.fileName.toString() + ".semanticdb"
            val semanticdbOutPath =
                targetroot.resolve("META-INF").resolve("semanticdb").resolve(relative).resolveSibling(filename)

            Files.createDirectories(semanticdbOutPath.parent)
            return semanticdbOutPath
        }
        throw IllegalStateException("given file is not under the sourceroot.\n\t\tSourceroot: $sourceroot\n\t\tFile path: ${file.virtualFilePath}\n\t\tNormalized file path: $normalizedPath")
    }
}