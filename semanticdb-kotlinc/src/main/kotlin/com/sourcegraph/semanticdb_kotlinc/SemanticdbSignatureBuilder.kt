package com.sourcegraph.semanticdb_kotlinc

import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.TypeParameterDescriptor
import kotlin.contracts.ExperimentalContracts

@ExperimentalContracts
class SemanticdbSignatureBuilder(private val cache: SymbolsCache) {
    fun build(descriptor: DeclarationDescriptor): Semanticdb.Signature =
        when (descriptor) {
            is ClassDescriptor -> Signature { classSignature = generateClassSignature(descriptor) }
            else -> Signature {}
        }

    private fun generateClassSignature(desc: ClassDescriptor) = ClassSignature {
        typeParameters = generateScope(desc.declaredTypeParameters)
    }

    private fun generateScope(params: List<TypeParameterDescriptor>) = Scope {
        this.addAllSymlinks(params.flatMap { cache[it] }.toMutableList().map { it.toString() })
    }
}
