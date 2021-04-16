package com.sourcegraph.semanticdb_kotlinc

import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.descriptors.impl.LocalVariableDescriptor
import org.jetbrains.kotlin.protobuf.Descriptors
import org.jetbrains.kotlin.psi.KtNamedDeclaration
import org.jetbrains.kotlin.psi.KtParameter
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList

class GlobalSymbolsCache {
    private val globals = IdentityHashMap<DeclarationDescriptor, Symbol>()

    fun semanticdbSymbol(descriptor: DeclarationDescriptor/*, decl: KtNamedDeclaration*/, locals: LocalSymbolsCache): Symbol {
        globals[descriptor]?.let { return it }
        locals[descriptor]?.let { return it }
        return uncachedSemanticdbSymbol(descriptor, locals).also {
            if (it.isGlobal()) globals[descriptor] = it
        }
    }

    private fun isLocalVariable(desc: DeclarationDescriptor): Boolean = desc is LocalVariableDescriptor

    private fun isAnonymousClass(desc: DeclarationDescriptor): Boolean = desc is ClassDescriptor /*&& desc.nam*/

    private fun uncachedSemanticdbSymbol(descriptor: DeclarationDescriptor, locals: LocalSymbolsCache): Symbol {
        val ownerDesc = descriptor.containingDeclaration ?: return NONE
        val owner = semanticdbSymbol(ownerDesc, locals)
        if (owner == NONE) return ROOT_PACKAGE
        else if (owner.isLocal() || isLocalVariable(descriptor)) return locals + ownerDesc
        val semanticdbDescriptor = semanticdbDescriptor(descriptor)
        return createGlobal(owner, semanticdbDescriptor)
    }

    private fun semanticdbDescriptor(desc: DeclarationDescriptor): Descriptor {
        return when(desc) {
            is ClassDescriptor -> Descriptor(Descriptor.Kind.TYPE, desc.name.toString())
            is FunctionDescriptor -> Descriptor(Descriptor.Kind.METHOD, desc.name.toString(), methodDisambiguator(desc))
            //is PackageDesc
            is TypeParameterDescriptor -> Descriptor(Descriptor.Kind.TYPE_PARAMETER, desc.name.toString())
            is VariableDescriptor -> Descriptor(Descriptor.Kind.TERM, desc.name.toString())
            else -> Descriptor.NONE
        }
    }

    private fun methodDisambiguator(desc: FunctionDescriptor): String {
        val methods =
            (desc.containingDeclaration as ClassDescriptorWithResolutionScopes).declaredCallableMembers.filter { it.name == desc.name }
            as ArrayList<CallableMemberDescriptor>

        methods.sortWith { m1, m2 -> compareValues(m1.dispatchReceiverParameter == null, m2.dispatchReceiverParameter == null) }

        val index = methods.indexOf(desc)
        return when(index) {
            0 -> "()"
            else -> "(+$index)"
        }
    }
}

class LocalSymbolsCache {
    private val symbols = IdentityHashMap<DeclarationDescriptor, Symbol>()
    private var localsCounter = -1

    operator fun get(desc: DeclarationDescriptor): Symbol? = symbols[desc]

    operator fun plus(desc: DeclarationDescriptor): Symbol {
        localsCounter++
        val result = createLocal(localsCounter)
        symbols[desc] = result
        return result
    }
}