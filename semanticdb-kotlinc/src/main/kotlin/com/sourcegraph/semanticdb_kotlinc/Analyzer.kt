package com.sourcegraph.semanticdb_kotlinc

import com.sourcegraph.semanticdb_kotlinc.Semanticdb.SymbolOccurrence.Role
import org.jetbrains.kotlin.analyzer.AnalysisResult
import org.jetbrains.kotlin.com.intellij.openapi.project.Project
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.resolve.BindingTrace
import org.jetbrains.kotlin.resolve.DescriptorUtils
import org.jetbrains.kotlin.resolve.jvm.extensions.AnalysisHandlerExtension
import org.jetbrains.kotlin.types.TypeUtils
import org.jetbrains.kotlin.types.isNullable
import org.jetbrains.kotlin.types.typeUtil.isTypeParameter
import org.jetbrains.kotlin.types.typeUtil.makeNotNullable
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
            val document = Visitor(resolver, file, lineMap).build()
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

    private inner class Visitor(val resolver: DescriptorResolver, val file: KtFile, lineMap: LineMap): KtTreeVisitorVoid() {
        private val locals = LocalSymbolsCache()
        private val emitter = SemanticdbEmitter(globals, locals, sourceroot, file, lineMap)

        fun build(): Semanticdb.TextDocument {
            super.visitKtFile(file)
            return emitter.buildSemanticdbTextDocument()
        }

        override fun visitClass(klass: KtClass) {
            val desc = resolver.fromDeclaration(klass)!!
            emitter.emitSemanticdbData(desc, klass, Role.DEFINITION)
            val symbol = globals[desc, locals]
            println("NAMED TYPE $klass ${desc.name} $symbol")
            super.visitClass(klass)
        }

        override fun visitNamedFunction(function: KtNamedFunction) {
            val desc = resolver.fromDeclaration(function)!!
            emitter.emitSemanticdbData(desc, function, Role.DEFINITION)
            val symbol = globals[desc, locals]
            println("NAMED FUN $function ${desc.name} $symbol")
            super.visitNamedFunction(function)
        }

        override fun visitProperty(property: KtProperty) {
            val desc = resolver.fromDeclaration(property)!!
            emitter.emitSemanticdbData(desc, property, Role.DEFINITION)
            val symbol = globals[desc, locals]
            println("NAMED PROP $property ${desc.name} $symbol")
            super.visitProperty(property)
        }

        override fun visitParameter(parameter: KtParameter) {
            val desc = resolver.fromDeclaration(parameter)!!
            emitter.emitSemanticdbData(desc, parameter, Role.DEFINITION)
            val symbol = globals[desc, locals]
            println("NAMED PARAM $parameter ${desc.name} $symbol")
            super.visitParameter(parameter)
        }

        override fun visitTypeParameter(parameter: KtTypeParameter) {
            val desc = resolver.fromDeclaration(parameter)!!
            emitter.emitSemanticdbData(desc, parameter, Role.DEFINITION)
            val symbol = globals[desc, locals]
            println("NAMED TYPE-PARAM $parameter ${desc.name} $symbol")
            super.visitTypeParameter(parameter)
        }

        override fun visitTypeAlias(typeAlias: KtTypeAlias) {
            val desc = resolver.fromDeclaration(typeAlias)!!
            emitter.emitSemanticdbData(desc, typeAlias, Role.DEFINITION)
            val symbol = globals[desc, locals]
            println("NAMED TYPE-ALIAS $typeAlias ${desc.name} $symbol")
            super.visitTypeAlias(typeAlias)
        }

        override fun visitTypeReference(typeReference: KtTypeReference) {
            val type = resolver.fromTypeReference(typeReference).let {
                    if (it.isNullable()) return@let it.makeNotNullable()
                    else return@let it
            }
            val desc = if (!type.isTypeParameter()) {
                DescriptorUtils.getClassDescriptorForType(type)
            } else {
                TypeUtils.getTypeParameterDescriptorOrNull(type)!!
            }
            emitter.emitSemanticdbData(desc, typeReference, Role.REFERENCE)
            val symbol = globals[desc, locals]
            println("TYPE REFERENCE $typeReference $type ${desc.name} $symbol")
            super.visitTypeReference(typeReference)
        }
    }
}