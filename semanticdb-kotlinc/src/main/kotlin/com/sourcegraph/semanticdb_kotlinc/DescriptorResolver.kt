package com.sourcegraph.semanticdb_kotlinc

import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtNamedDeclaration
import org.jetbrains.kotlin.psi.KtReferenceExpression
import org.jetbrains.kotlin.psi.KtTypeReference
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.BindingTrace

class DescriptorResolver(private val bindingTrace: BindingTrace) {
    fun fromDeclaration(declaration: KtDeclaration) = bindingTrace[BindingContext.DECLARATION_TO_DESCRIPTOR, declaration]

    fun fromReference(reference: KtReferenceExpression) = bindingTrace[BindingContext.REFERENCE_TARGET, reference]

    fun fromTypeReference(reference: KtTypeReference) = bindingTrace[BindingContext.TYPE, reference]
        ?: bindingTrace[BindingContext.ABBREVIATED_TYPE, reference]!!
}