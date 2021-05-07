package com.sourcegraph.semanticdb_kotlinc

import com.intellij.openapi.project.Project
import com.sourcegraph.semanticdb_kotlinc.Semanticdb.SymbolOccurrence.Role
import org.jetbrains.kotlin.analyzer.AnalysisResult
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.descriptors.buildPossiblyInnerType
import org.jetbrains.kotlin.js.descriptorUtils.getJetTypeFqName
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.BindingTrace
import org.jetbrains.kotlin.resolve.DescriptorUtils
import org.jetbrains.kotlin.resolve.calls.callUtil.getResolvedCall
import org.jetbrains.kotlin.resolve.calls.callUtil.getType
import org.jetbrains.kotlin.resolve.jvm.extensions.AnalysisHandlerExtension
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
        for (file in files) {
            val lineMap = LineMap(project, file)
            val document = Visitor(bindingTrace, file, lineMap).build()
            Files.write(semanticdbOutPathForFile(file)!!, document.toByteArray())
            callback(document)
        }

        return super.analysisCompleted(project, module, bindingTrace, files)
    }

    private fun semanticdbOutPathForFile(file: KtFile): Path? {
        val normalizedPath = Path.of(file.virtualFilePath).normalize()
        if (normalizedPath.startsWith(sourceroot)) {
            val relative = sourceroot.relativize(normalizedPath)
            val filename = relative.fileName.toString() + ".semanticdb"
            val semanticdbOutPath = targetroot.resolve("META-INF").resolve("semanticdb").resolve(relative).resolveSibling(filename)

            Files.createDirectories(semanticdbOutPath.parent)
            return semanticdbOutPath
        }
        return null
    }

    private inner class Visitor(val bindingTrace: BindingTrace, val file: KtFile, lineMap: LineMap): KtTreeVisitorVoid() {
        private val locals = LocalSymbolsCache()
        private val emitter = SemanticdbEmitter(globals, locals, sourceroot, file, lineMap)

        fun build(): Semanticdb.TextDocument {
            super.visitKtFile(file)
            return emitter.buildSemanticdbTextDocument()
        }

        override fun visitClass(klass: KtClass) {
            val desc = bindingTrace[BindingContext.DECLARATION_TO_DESCRIPTOR, klass]!!
            emitter.emitSemanticdbData(desc, klass, Role.DEFINITION)
            val symbol = globals.semanticdbSymbol(desc, locals)
            println("NAMED TYPE $klass ${desc.name} $symbol")
            super.visitClass(klass)
        }

        override fun visitNamedFunction(function: KtNamedFunction) {
            val desc = bindingTrace[BindingContext.DECLARATION_TO_DESCRIPTOR, function]!!
            emitter.emitSemanticdbData(desc, function, Role.DEFINITION)
            val symbol = globals.semanticdbSymbol(desc, locals)
            println("NAMED FUN $function ${desc.name} $symbol")
            super.visitNamedFunction(function)
        }

        override fun visitProperty(property: KtProperty) {
            val desc = bindingTrace[BindingContext.DECLARATION_TO_DESCRIPTOR, property]!!
            emitter.emitSemanticdbData(desc, property, Role.DEFINITION)
            val symbol = globals.semanticdbSymbol(desc, locals)
            println("NAMED PROP $property ${desc.name} $symbol")
            super.visitProperty(property)
        }

        override fun visitParameter(parameter: KtParameter) {
            val desc = bindingTrace[BindingContext.DECLARATION_TO_DESCRIPTOR, parameter]!!
            emitter.emitSemanticdbData(desc, parameter, Role.DEFINITION)
            val symbol = globals.semanticdbSymbol(desc, locals)
            println("NAMED PARAM $parameter ${desc.name} $symbol")
            super.visitParameter(parameter)
        }

        override fun visitTypeParameter(parameter: KtTypeParameter) {
            val desc = bindingTrace[BindingContext.DECLARATION_TO_DESCRIPTOR, parameter]!!
            emitter.emitSemanticdbData(desc, parameter, Role.DEFINITION)
            val symbol = globals.semanticdbSymbol(desc, locals)
            println("NAMED TYPE-PARAM $parameter ${desc.name} $symbol")
            super.visitTypeParameter(parameter)
        }

        override fun visitTypeAlias(typeAlias: KtTypeAlias) {
            val desc = bindingTrace[BindingContext.DECLARATION_TO_DESCRIPTOR, typeAlias]!!
            emitter.emitSemanticdbData(desc, typeAlias, Role.DEFINITION)
            val symbol = globals.semanticdbSymbol(desc, locals)
            println("NAMED TYPE-ALIAS $typeAlias ${desc.name} $symbol")
            super.visitTypeAlias(typeAlias)
        }

    }
}