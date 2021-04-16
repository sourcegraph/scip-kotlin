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
        //return AnalysisResult.EMPTY
        //return AnalysisResult.Companion.success(BindingContext.EMPTY, module, false)
        return super.analysisCompleted(project, module, bindingTrace, files)
    }

    private inner class Visitor(val bindingTrace: BindingTrace): KtTreeVisitorVoid() {
        private val locals = LocalSymbolsCache()

        /*override fun visitClass(klass: KtClass) {
            println("CLASS " + klass.fqName)
            super.visitClass(klass)
        }*/

        /*override fun visitParameter(parameter: KtParameter) {
            //val resolvedCall = parameter.getResolvedCall(bindingTrace.bindingContext)
            val desc = bindingTrace[BindingContext.VALUE_PARAMETER, parameter]!!
            val descStr = desc.toString()
            println("PARAM ${DescriptorUtils.getFqName(desc.type.buildPossiblyInnerType()?.classifierDescriptor?.typeConstructor?.declarationDescriptor as ClassDescriptor)}")
            when(desc.type.constructor) {
                is ClassDescriptor -> println("PARAM ${DescriptorUtils.getFqName(desc.type.constructor as ClassDescriptor)}")
                else -> println("PARAM2 ${desc.type}")
            }

            super.visitParameter(parameter)
        }*/

        override fun visitPackageDirective(directive: KtPackageDirective) {
            super.visitPackageDirective(directive)
        }

        override fun visitNamedDeclaration(declaration: KtNamedDeclaration) {
            val desc = bindingTrace[BindingContext.DECLARATION_TO_DESCRIPTOR, declaration]!!.original
            val symbol = globals.semanticdbSymbol(desc, locals)
            println("NAMED DECL $declaration ${declaration.javaClass.simpleName} ${desc.javaClass.simpleName} ${desc.name}  $symbol")
            super.visitNamedDeclaration(declaration)
        }

        override fun visitNamedFunction(function: KtNamedFunction) {
            val desc = bindingTrace[BindingContext.DECLARATION_TO_DESCRIPTOR, function]
            super.visitNamedFunction(function)
        }

        override fun visitProperty(property: KtProperty) {
            val desc = bindingTrace[BindingContext.DECLARATION_TO_DESCRIPTOR, property]!!.original
            super.visitProperty(property)
        }

        override fun visitParameter(parameter: KtParameter) {
            super.visitParameter(parameter)
        }

        override fun visitTypeParameter(parameter: KtTypeParameter) {
            super.visitTypeParameter(parameter)
        }

        override fun visitObjectDeclaration(declaration: KtObjectDeclaration) {
            val desc = bindingTrace[BindingContext.DECLARATION_TO_DESCRIPTOR, declaration]
            super.visitObjectDeclaration(declaration)
        }

        /*override fun visitCallExpression(expression: KtCallExpression) {
            *//*println("CALL " + expression.getResolvedCall(ctx)*//**//*?.call?.dispatchReceiver*//**//*
            + expression.valueArgumentList)*//*
            //println(expression.valueArguments.map { it.name })
            super.visitCallExpression(expression)
        }*/

        /*override fun visitQualifiedExpression(expression: KtQualifiedExpression) {
            super.visitQualifiedExpression(expression)
        }

        override fun visitObjectLiteralExpression(expression: KtObjectLiteralExpression) {
            val desc = bindingTrace[BindingContext.EXPRESSION_TYPE_INFO, expression]!!
            super.visitObjectLiteralExpression(expression)
        }*/
    }
}