package com.sourcegraph.semanticdb_kotlinc

import com.intellij.openapi.project.Project
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
import org.jetbrains.kotlin.types.KotlinType
import kotlin.contracts.ExperimentalContracts

@ExperimentalContracts
class Analyzer: AnalysisHandlerExtension {
    private val globals = GlobalSymbolsCache()

    override fun analysisCompleted(project: Project,
    module: ModuleDescriptor,
    bindingTrace: BindingTrace,
    files: Collection<KtFile>
    ): AnalysisResult? {
        val x = Visitor(bindingTrace)

        for (file in files) {
            file.classes
            x.visitKtFile(file, null)
        }

        return super.analysisCompleted(project, module, bindingTrace, files)
    }

    private inner class Visitor(val bindingTrace: BindingTrace): KtTreeVisitorVoid() {
        private val locals = LocalSymbolsCache()

        override fun visitClass(klass: KtClass) {
            val desc = bindingTrace[BindingContext.DECLARATION_TO_DESCRIPTOR, klass]!!
            val symbol = globals.semanticdbSymbol(desc, locals)
            println("NAMED TYPE $klass ${klass.javaClass.simpleName} ${desc.javaClass.simpleName} ${desc.name}  $symbol")
            super.visitClass(klass)
        }

        override fun visitNamedFunction(function: KtNamedFunction) {
            val desc = bindingTrace[BindingContext.DECLARATION_TO_DESCRIPTOR, function]!!
            val symbol = globals.semanticdbSymbol(desc, locals)
            println("NAMED FUN $function ${function.javaClass.simpleName} ${desc.javaClass.simpleName} ${desc.name}  $symbol")
            super.visitNamedFunction(function)
        }

        override fun visitProperty(property: KtProperty) {
            val desc = bindingTrace[BindingContext.DECLARATION_TO_DESCRIPTOR, property]!!
            val symbol = globals.semanticdbSymbol(desc, locals)
            println("NAMED PROP $property ${property.javaClass.simpleName} ${desc.javaClass.simpleName} ${desc.name}  $symbol")
            super.visitProperty(property)
        }

        override fun visitParameter(parameter: KtParameter) {
            val desc = bindingTrace[BindingContext.DECLARATION_TO_DESCRIPTOR, parameter]!!
            val symbol = globals.semanticdbSymbol(desc, locals)
            println("NAMED PARAM $parameter ${parameter.javaClass.simpleName} ${desc.javaClass.simpleName} ${desc.name}  $symbol")
            super.visitParameter(parameter)
        }

        override fun visitTypeParameter(parameter: KtTypeParameter) {
            val desc = bindingTrace[BindingContext.DECLARATION_TO_DESCRIPTOR, parameter]!!
            val symbol = globals.semanticdbSymbol(desc, locals)
            println("NAMED TYPE-PARAM $parameter ${parameter.javaClass.simpleName} ${desc.javaClass.simpleName} ${desc.name}  $symbol")
            super.visitTypeParameter(parameter)
        }

        override fun visitTypeAlias(typeAlias: KtTypeAlias) {
            val desc = bindingTrace[BindingContext.DECLARATION_TO_DESCRIPTOR, typeAlias]!!
            val symbol = globals.semanticdbSymbol(desc, locals)
            println("NAMED TYPE-ALIAS $typeAlias ${typeAlias.javaClass.simpleName} ${desc.javaClass.simpleName} ${desc.name}  $symbol")
            super.visitTypeAlias(typeAlias)
        }

    }
}