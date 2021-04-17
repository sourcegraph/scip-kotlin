package com.sourcegraph.semanticdb_kotlinc

import org.jetbrains.kotlin.descriptors.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@ExperimentalContracts
class GlobalSymbolsCache {
    private val globals = IdentityHashMap<DeclarationDescriptor, Symbol>()

    fun semanticdbSymbol(descriptor: DeclarationDescriptor, locals: LocalSymbolsCache): Symbol {
        globals[descriptor]?.let { return it }
        locals[descriptor]?.let { return it }
        return uncachedSemanticdbSymbol(descriptor, locals).also {
            if (it.isGlobal()) globals[descriptor] = it
        }
    }

    private fun skip(desc: DeclarationDescriptor?): Boolean {
        contract { returns(false) implies (desc != null) }
        return desc == null || desc is ModuleDescriptor
    }

    private fun uncachedSemanticdbSymbol(descriptor: DeclarationDescriptor?, locals: LocalSymbolsCache): Symbol {
        if (skip(descriptor)) return Symbol.NONE
        val ownerDesc = descriptor.containingDeclaration ?: return Symbol.ROOT_PACKAGE
        val owner = semanticdbSymbol(ownerDesc, locals)
        if (ownerDesc.isObjectDeclaration() || descriptor.isLocalVariable())
            return locals + ownerDesc

        val semanticdbDescriptor = semanticdbDescriptor(descriptor)
        return Symbol.createGlobal(owner, semanticdbDescriptor)
    }

    private fun semanticdbDescriptor(desc: DeclarationDescriptor): Descriptor {
        return when(desc) {
            is ClassDescriptor -> Descriptor(Descriptor.Kind.TYPE, desc.name.toString())
            is FunctionDescriptor -> Descriptor(Descriptor.Kind.METHOD, desc.name.toString(), methodDisambiguator(desc))
            is TypeParameterDescriptor -> Descriptor(Descriptor.Kind.TYPE_PARAMETER, desc.name.toString())
            is ValueParameterDescriptor -> Descriptor(Descriptor.Kind.PARAMETER, desc.name.toString())
            is VariableDescriptor -> Descriptor(Descriptor.Kind.TERM, desc.name.toString())
            is TypeAliasDescriptor -> Descriptor(Descriptor.Kind.TYPE, desc.name.toString())
            is PackageFragmentDescriptor -> Descriptor(Descriptor.Kind.PACKAGE, desc.name.toString())
            else -> {
                println("unknown descriptor kind ${desc.javaClass.simpleName}")
                Descriptor.NONE
            }
        }
    }

    private fun methodDisambiguator(desc: FunctionDescriptor): String {
        val methods =
            (desc.containingDeclaration as ClassDescriptorWithResolutionScopes).declaredCallableMembers.filter { it.name == desc.name }
            as ArrayList<CallableMemberDescriptor>

        methods.sortWith { m1, m2 -> compareValues(m1.dispatchReceiverParameter == null, m2.dispatchReceiverParameter == null) }

        return when(val index = methods.indexOf(desc)) {
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
        val result = Symbol.createLocal(localsCounter)
        symbols[desc] = result
        return result
    }
}