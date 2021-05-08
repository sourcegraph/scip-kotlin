package com.sourcegraph.semanticdb_kotlinc

import com.sourcegraph.semanticdb_kotlinc.SemanticdbSymbolDescriptor.Kind
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.load.kotlin.toSourceElement
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.resolve.scopes.DescriptorKindFilter
import org.jetbrains.kotlin.resolve.scopes.getDescriptorsFiltered
import org.jetbrains.kotlin.resolve.source.getPsi
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@ExperimentalContracts
class GlobalSymbolsCache {
    private val globals = HashMap<DeclarationDescriptor, Symbol>()
    lateinit var resolver: DescriptorResolver

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

        var owner = semanticdbSymbol(ownerDesc, locals)
        if (ownerDesc.isObjectDeclaration() || descriptor.isLocalVariable())
            return locals + ownerDesc

        if ((descriptor is FunctionDescriptor || descriptor is VariableDescriptor) && ownerDesc is PackageFragmentDescriptor) {
            owner = Symbol.createGlobal(owner, SemanticdbSymbolDescriptor(Kind.TYPE, sourceFileToClassSymbol(descriptor.toSourceElement.containingFile)))
        }

        val semanticdbDescriptor = semanticdbDescriptor(descriptor)
        return Symbol.createGlobal(owner, semanticdbDescriptor)
    }

    // generates the synthetic class name from the source file
    // https://kotlinlang.org/docs/java-to-kotlin-interop.html#package-level-functions
    private fun sourceFileToClassSymbol(file: SourceFile) = file.name!!.replace(".kt", "Kt")

    private fun semanticdbDescriptor(desc: DeclarationDescriptor): SemanticdbSymbolDescriptor {
        return when(desc) {
            is ClassDescriptor -> SemanticdbSymbolDescriptor(Kind.TYPE, desc.name.toString())
            is FunctionDescriptor -> SemanticdbSymbolDescriptor(Kind.METHOD, desc.name.toString(), methodDisambiguator(desc))
            is TypeParameterDescriptor -> SemanticdbSymbolDescriptor(Kind.TYPE_PARAMETER, desc.name.toString())
            is ValueParameterDescriptor -> SemanticdbSymbolDescriptor(Kind.PARAMETER, desc.name.toString())
            is VariableDescriptor -> SemanticdbSymbolDescriptor(Kind.TERM, desc.name.toString())
            is TypeAliasDescriptor -> SemanticdbSymbolDescriptor(Kind.TYPE, desc.name.toString())
            is PackageFragmentDescriptor -> SemanticdbSymbolDescriptor(Kind.PACKAGE, desc.name.toString())
            else -> {
                println("unknown descriptor kind ${desc.javaClass.simpleName}")
                SemanticdbSymbolDescriptor.NONE
            }
        }
    }

    private fun methodDisambiguator(desc: FunctionDescriptor): String {
        val methods = when(val ownerDecl = desc.containingDeclaration) {
            is PackageFragmentDescriptor ->
                ownerDecl.getMemberScope().getDescriptorsFiltered(DescriptorKindFilter.FUNCTIONS).map { it as CallableMemberDescriptor }
            is ClassDescriptorWithResolutionScopes ->
                ownerDecl.declaredCallableMembers.filter { it.name == desc.name }
            is FunctionDescriptor ->
                ownerDecl.toSourceElement.getPsi()!!.
                    children.first { it is KtBlockExpression }.
                    children.filterIsInstance<KtNamedFunction>().
                    map { resolver.fromDeclaration(it)!! as CallableMemberDescriptor }
            else -> throw IllegalStateException("unexpected owner decl type ${ownerDecl.javaClass}")
        } as ArrayList<CallableMemberDescriptor>

        methods.sortWith { m1, m2 -> compareValues(m1.dispatchReceiverParameter == null, m2.dispatchReceiverParameter == null) }

        return when(val index = methods.indexOf(desc)) {
            0 -> "()"
            else -> "(+$index)"
        }
    }
}

class LocalSymbolsCache {
    private val symbols = HashMap<DeclarationDescriptor, Symbol>()
    private var localsCounter = 0

    operator fun get(desc: DeclarationDescriptor): Symbol? = symbols[desc]

    operator fun plus(desc: DeclarationDescriptor): Symbol {
        val result = Symbol.createLocal(localsCounter++)
        symbols[desc] = result
        return result
    }
}