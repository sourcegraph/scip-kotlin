package com.sourcegraph.semanticdb_kotlinc

import org.jetbrains.kotlin.psi.KtNamedDeclaration
import org.jetbrains.kotlin.psi.KtTypeReference
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.BindingTrace

class DescriptorResolver(private val bindingTrace: BindingTrace) {
    fun fromDeclaration(declaration: KtNamedDeclaration) = bindingTrace[BindingContext.DECLARATION_TO_DESCRIPTOR, declaration]

    fun fromTypeReference(reference: KtTypeReference) = bindingTrace[BindingContext.TYPE, reference]
        ?: bindingTrace[BindingContext.ABBREVIATED_TYPE, reference]!!
}