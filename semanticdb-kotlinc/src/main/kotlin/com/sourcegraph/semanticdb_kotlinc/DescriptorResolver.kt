package com.sourcegraph.semanticdb_kotlinc

import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.ValueParameterDescriptor
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtNamedDeclaration
import org.jetbrains.kotlin.psi.KtReferenceExpression
import org.jetbrains.kotlin.psi.KtTypeReference
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.BindingTrace

class DescriptorResolver(private val bindingTrace: BindingTrace) {
    fun fromDeclaration(declaration: KtDeclaration): Sequence<DeclarationDescriptor> = sequence {
        val descriptor = bindingTrace[BindingContext.DECLARATION_TO_DESCRIPTOR, declaration]
        if (descriptor is ValueParameterDescriptor) {
            bindingTrace[BindingContext.VALUE_PARAMETER_AS_PROPERTY, descriptor]?.let {
                yield(it)
            }
        }
        descriptor?.let { yield(it) }
    }

    fun fromReference(reference: KtReferenceExpression) = bindingTrace[BindingContext.REFERENCE_TARGET, reference]

    fun fromTypeReference(reference: KtTypeReference) = bindingTrace[BindingContext.TYPE, reference]
        ?: bindingTrace[BindingContext.ABBREVIATED_TYPE, reference]!!
}