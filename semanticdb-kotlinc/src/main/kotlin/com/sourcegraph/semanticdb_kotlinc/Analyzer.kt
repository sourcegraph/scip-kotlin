package com.sourcegraph.semanticdb_kotlinc

import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.analyzer.AnalysisResult
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.descriptors.buildPossiblyInnerType
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.BindingTrace
import org.jetbrains.kotlin.resolve.DescriptorUtils
import org.jetbrains.kotlin.resolve.jvm.extensions.AnalysisHandlerExtension

class Analyzer: AnalysisHandlerExtension {
    override fun analysisCompleted(
        project: Project,
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
        override fun visitClass(klass: KtClass) {
            println("CLASS " + klass.fqName)
            super.visitClass(klass)
        }

        override fun visitParameter(parameter: KtParameter) {
            val desc = bindingTrace[BindingContext.VALUE_PARAMETER, parameter]!!
            val descStr = desc.toString()
            println("PARAM ${DescriptorUtils.getFqName(desc.type.buildPossiblyInnerType()?.classifierDescriptor?.typeConstructor?.declarationDescriptor as ClassDescriptor)}")
            /*when(desc.type.constructor) {
                is ClassDescriptor -> println("PARAM ${DescriptorUtils.getFqName(desc.type.constructor as ClassDescriptor)}")
                else -> println("PARAM2 ${desc.type}")
            }*/

            super.visitParameter(parameter)
        }

        override fun visitCallExpression(expression: KtCallExpression) {
            /*println("CALL " + expression.getResolvedCall(ctx)*//*?.call?.dispatchReceiver*//*
            + expression.valueArgumentList)*/
            //println(expression.valueArguments.map { it.name })
            super.visitCallExpression(expression)
        }
    }
}