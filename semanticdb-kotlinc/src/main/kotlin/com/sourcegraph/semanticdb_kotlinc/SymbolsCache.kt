package com.sourcegraph.semanticdb_kotlinc

import com.sourcegraph.semanticdb_kotlinc.SemanticdbSymbolDescriptor.Kind
import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.descriptors.impl.AnonymousFunctionDescriptor
import org.jetbrains.kotlin.load.java.descriptors.JavaClassDescriptor
import org.jetbrains.kotlin.load.kotlin.JvmPackagePartSource
import org.jetbrains.kotlin.load.kotlin.toSourceElement
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.resolve.descriptorUtil.module
import org.jetbrains.kotlin.resolve.scopes.DescriptorKindFilter
import org.jetbrains.kotlin.resolve.scopes.getDescriptorsFiltered
import org.jetbrains.kotlin.resolve.source.getPsi
import org.jetbrains.kotlin.serialization.deserialization.descriptors.DescriptorWithContainerSource
import org.jetbrains.kotlin.serialization.deserialization.descriptors.DeserializedSimpleFunctionDescriptor
import java.lang.System.err
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@ExperimentalContracts
class GlobalSymbolsCache(testing: Boolean = false): Iterable<Symbol> {
    private val globals =
        if (testing) LinkedHashMap<DeclarationDescriptor, Symbol>()
        else HashMap<DeclarationDescriptor, Symbol>()
    lateinit var resolver: DescriptorResolver

    operator fun get(descriptor: DeclarationDescriptor, locals: LocalSymbolsCache): Symbol {
        globals[descriptor]?.let { return it }
        locals[descriptor]?.let { return it }
        return uncachedSemanticdbSymbol(descriptor, locals).also {
            if (it.isGlobal()) globals[descriptor] = it
        }
    }

    private fun skip(desc: DeclarationDescriptor?): Boolean {
        contract { returns(false) implies (desc != null) }
        return desc == null || desc is ModuleDescriptor || desc is AnonymousFunctionDescriptor
    }

    private fun uncachedSemanticdbSymbol(descriptor: DeclarationDescriptor?, locals: LocalSymbolsCache): Symbol {
        if (skip(descriptor)) return Symbol.NONE
        val ownerDesc = getParentDescriptor(descriptor) ?: return Symbol.ROOT_PACKAGE

        var owner = this[ownerDesc, locals]
        if (ownerDesc.isObjectDeclaration() || owner.isLocal() || ownerDesc.isLocalVariable() || ownerDesc is AnonymousFunctionDescriptor || descriptor.isLocalVariable())
            return locals + ownerDesc

        // if is a top-level function or variable, Kotlin creates a wrapping class
        if ((descriptor is FunctionDescriptor || descriptor is VariableDescriptor) && ownerDesc is PackageFragmentDescriptor) {
            owner = Symbol.createGlobal(owner, SemanticdbSymbolDescriptor(Kind.TYPE, sourceFileToClassSymbol(descriptor.toSourceElement.containingFile, descriptor)))
        }

        val semanticdbDescriptor = semanticdbDescriptor(descriptor)
        return Symbol.createGlobal(owner, semanticdbDescriptor)
    }

    /**
     * Returns the parent DeclarationDescriptor for a given DeclarationDescriptor.
     * For most descriptor types, this simply returns the 'containing' descriptor.
     * For Module- or PackageFragmentDescriptors, it returns the descriptor for the parent fqName of the current
     * descriptors fqName e.g. for the fqName `test.sample.main`, the parent fqName would be `test.sample`.
     */
    private fun getParentDescriptor(descriptor: DeclarationDescriptor): DeclarationDescriptor? = when(descriptor) {
        is ModuleDescriptor -> {
            val pkg = descriptor.getPackage(descriptor.fqNameSafe).fragments[0]
            descriptor.getPackage(pkg.fqName.parent()).fragments[0]
        }
        is PackageFragmentDescriptor -> {
            if (descriptor.fqNameSafe.isRoot) null
            else descriptor.module.getPackage(descriptor.fqNameSafe.parent())
        }
        else -> descriptor.containingDeclaration
    }

    /**
     * generates the synthetic class name from the source file
     * https://kotlinlang.org/docs/java-to-kotlin-interop.html#package-level-functions
     */
    private fun sourceFileToClassSymbol(file: SourceFile, descriptor: DeclarationDescriptor): String = when(val name = file.name) {
        null -> {
            // TODO: see how its referenced on Java side
            if (KotlinBuiltIns.isBuiltIn(descriptor)) "LibraryKt"
            else {
                val jvmPackagePartSource =
                    (descriptor as DescriptorWithContainerSource).containerSource as JvmPackagePartSource
            jvmPackagePartSource.facadeClassName?.fqNameForClassNameWithoutDollars?.shortName()?.asString()
                ?: jvmPackagePartSource.simpleName.asString()
        }
        }
        else -> name.replace(".kt", "Kt")
    }

    private fun semanticdbDescriptor(desc: DeclarationDescriptor): SemanticdbSymbolDescriptor {
        return when(desc) {
            is ClassDescriptor -> SemanticdbSymbolDescriptor(Kind.TYPE, desc.name.toString())
            is FunctionDescriptor -> SemanticdbSymbolDescriptor(Kind.METHOD, desc.name.toString(), methodDisambiguator(desc))
            is TypeParameterDescriptor -> SemanticdbSymbolDescriptor(Kind.TYPE_PARAMETER, desc.name.toString())
            is ValueParameterDescriptor -> SemanticdbSymbolDescriptor(Kind.PARAMETER, desc.name.toString())
            is VariableDescriptor -> SemanticdbSymbolDescriptor(Kind.TERM, desc.name.toString())
            is TypeAliasDescriptor -> SemanticdbSymbolDescriptor(Kind.TYPE, desc.name.toString())
            is PackageFragmentDescriptor, is PackageViewDescriptor -> SemanticdbSymbolDescriptor(Kind.PACKAGE, desc.name.toString())
            else -> {
                err.println("unknown descriptor kind ${desc.javaClass.simpleName}")
                SemanticdbSymbolDescriptor.NONE
            }
        }
    }

    private fun methodDisambiguator(desc: FunctionDescriptor): String {
        val ownerDecl = desc.containingDeclaration
        val methods = when(ownerDecl) {
            is PackageFragmentDescriptor ->
                ownerDecl.getMemberScope().getDescriptorsFiltered(DescriptorKindFilter.FUNCTIONS).map { it as CallableMemberDescriptor }
            is ClassDescriptorWithResolutionScopes -> {
                when (desc) {
                    is ClassConstructorDescriptor -> {
                        val constructors = (desc.containingDeclaration as ClassDescriptorWithResolutionScopes).constructors as ArrayList
                        // primary constructor always seems to be last, so move it to the start. TODO is this correct? in what order does Java see them?
                        // if (constructors.last().isPrimary) constructors.add(0, constructors.removeLast())
                        constructors
                    }
                    else -> ownerDecl.declaredCallableMembers
                }
            }
            is FunctionDescriptor ->
                ownerDecl.toSourceElement.getPsi()!!.
                    children.first { it is KtBlockExpression }.
                    children.filterIsInstance<KtNamedFunction>().
                    map { resolver.fromDeclaration(it)!! as CallableMemberDescriptor }
            is ClassDescriptor -> {
                val methods = ownerDecl.unsubstitutedMemberScope.getContributedDescriptors().filterIsInstance<FunctionDescriptor>()
                val staticMethods = ownerDecl.staticScope.getContributedDescriptors().filterIsInstance<FunctionDescriptor>()
                val ctors = ownerDecl.constructors.toList()
                val allFuncs = ArrayList<FunctionDescriptor>(methods.size + ctors.size + staticMethods.size)
                allFuncs.addAll(ctors)
                allFuncs.addAll(methods)
                allFuncs.addAll(staticMethods)
                allFuncs
            }
            else -> throw IllegalStateException("unexpected owner decl type '${ownerDecl.javaClass}':\n\t\tMethod: ${desc}\n\t\tParent: $ownerDecl")
        }.filter { it.name == desc.name } as ArrayList<CallableMemberDescriptor>

        methods.sortWith { m1, m2 -> compareValues(m1.dispatchReceiverParameter == null, m2.dispatchReceiverParameter == null) }

        // need to get original to get method without type projections
        return when(val index = methods.indexOf(desc.original)) {
            0 -> "()"
            -1 -> throw IllegalStateException("failed to find method in parent:\n\t\tMethod: ${desc}\n\t\tParent: ${ownerDecl.name}\n\t\tMethods: ${methods.joinToString("\n\t\t\t ")}")
            else -> "(+$index)"
        }
    }

    override fun iterator(): Iterator<Symbol> = globals.values.iterator()
}

class LocalSymbolsCache: Iterable<Symbol> {
    private val symbols = HashMap<DeclarationDescriptor, Symbol>()
    private var localsCounter = 0

    val iterator: Iterable<Map.Entry<DeclarationDescriptor, Symbol>> get() = symbols.asIterable()

    val size: Int get() = symbols.size

    operator fun get(desc: DeclarationDescriptor): Symbol? = symbols[desc]

    operator fun plus(desc: DeclarationDescriptor): Symbol {
        val result = Symbol.createLocal(localsCounter++)
        symbols[desc] = result
        return result
    }

    override fun iterator(): Iterator<Symbol> = symbols.values.iterator()
}