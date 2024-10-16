package com.sourcegraph.semanticdb_kotlinc

import com.sourcegraph.semanticdb_kotlinc.SemanticdbSymbolDescriptor.Kind
import java.lang.System.err
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.isLocalMember
import org.jetbrains.kotlin.fir.analysis.checkers.getContainingClassSymbol
import org.jetbrains.kotlin.fir.analysis.checkers.getContainingSymbol
import org.jetbrains.kotlin.fir.declarations.FirDeclaration
import org.jetbrains.kotlin.fir.declarations.FirDeclarationOrigin
import org.jetbrains.kotlin.fir.declarations.FirFunction
import org.jetbrains.kotlin.fir.declarations.FirRegularClass
import org.jetbrains.kotlin.fir.declarations.utils.nameOrSpecialName
import org.jetbrains.kotlin.fir.resolve.providers.firProvider
import org.jetbrains.kotlin.fir.resolve.providers.getContainingFile
import org.jetbrains.kotlin.fir.resolve.providers.symbolProvider
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.fir.symbols.SymbolInternals
import org.jetbrains.kotlin.fir.symbols.impl.FirAnonymousFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirCallableSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirClassLikeSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirConstructorSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirFileSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirPropertyAccessorSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirPropertySymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirTypeAliasSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirTypeParameterSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirValueParameterSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirVariableSymbol
import org.jetbrains.kotlin.fir.types.ConeClassLikeType
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.fir.types.classId
import org.jetbrains.kotlin.fir.types.coneType
import org.jetbrains.kotlin.fir.types.coneTypeSafe
import org.jetbrains.kotlin.load.kotlin.toSourceElement
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.resolve.scopes.DescriptorKindFilter
import org.jetbrains.kotlin.resolve.scopes.getDescriptorsFiltered
import org.jetbrains.kotlin.resolve.source.getPsi
import org.jetbrains.kotlin.types.TypeUtils
import org.jetbrains.kotlin.util.capitalizeDecapitalize.capitalizeAsciiOnly

@ExperimentalContracts
class GlobalSymbolsCache(testing: Boolean = false) : Iterable<Symbol> {
    private val globals =
        if (testing) LinkedHashMap<FirBasedSymbol<*>, Symbol>()
        else HashMap<FirBasedSymbol<*>, Symbol>()
    lateinit var resolver: DescriptorResolver

    operator fun get(symbol: FirBasedSymbol<*>, locals: LocalSymbolsCache): Sequence<Symbol> =
        sequence {
        emitSymbols(symbol, locals)
    }

    /**
     * called whenever a new symbol should be yielded in the sequence e.g. for properties we also
     * want to yield for every implicit getter/setter, but wouldn't want to yield for e.g. the
     * package symbol parts that a class symbol is composed of.
     */
    @OptIn(SymbolInternals::class)
    private suspend fun SequenceScope<Symbol>.emitSymbols(
        symbol: FirBasedSymbol<*>,
        locals: LocalSymbolsCache
    ) {
        yield(getSymbol(symbol, locals))
        if (symbol is FirPropertySymbol) {
            if (symbol.fir.getter?.origin is FirDeclarationOrigin.Synthetic)
                emitSymbols(symbol.fir.getter!!.symbol, locals)
            if (symbol.fir.setter?.origin is FirDeclarationOrigin.Synthetic)
                emitSymbols(symbol.fir.setter!!.symbol, locals)
        }
    }

    /**
     * Entrypoint for building or looking-up a symbol without yielding a value in the sequence.
     * Called recursively for every part of a symbol, unless a cached result short circuits.
     */
    private fun getSymbol(symbol: FirBasedSymbol<*>, locals: LocalSymbolsCache): Symbol {
        globals[symbol]?.let {
            return it
        }
        locals[symbol]?.let {
            return it
        }
        return uncachedSemanticdbSymbol(symbol, locals).also {
            if (it.isGlobal()) globals[symbol] = it
        }
    }

    private fun skip(symbol: FirBasedSymbol<*>?): Boolean {
        contract { returns(false) implies (symbol != null) }
        return symbol == null || symbol is FirAnonymousFunctionSymbol
    }

    @OptIn(SymbolInternals::class)
    private fun uncachedSemanticdbSymbol(
        symbol: FirBasedSymbol<*>?,
        locals: LocalSymbolsCache
    ): Symbol {
        if (skip(symbol)) return Symbol.NONE
        val ownerSymbol = getParentSymbol(symbol)
        var owner = ownerSymbol?.let { this.getSymbol(it, locals) } ?: Symbol.ROOT_PACKAGE
        if ((ownerSymbol?.fir as? FirRegularClass)?.classKind == ClassKind.OBJECT ||
            owner.isLocal() ||
            (ownerSymbol as? FirBasedSymbol<FirDeclaration>)?.fir?.isLocalMember == true ||
            ownerSymbol is FirAnonymousFunctionSymbol ||
            (symbol as? FirBasedSymbol<FirDeclaration>)?.fir?.isLocalMember == true)
            return locals + symbol

        // if is a top-level function or variable, Kotlin creates a wrapping class
        if (ownerSymbol !is FirClassSymbol &&
            (symbol is FirFunctionSymbol || symbol is FirPropertySymbol)) {
            owner =
                Symbol.createGlobal(
                    owner, SemanticdbSymbolDescriptor(Kind.TYPE, sourceFileToClassSymbol(symbol)))
        }

        val semanticdbDescriptor = semanticdbDescriptor(symbol)
        return Symbol.createGlobal(owner, semanticdbDescriptor)
    }

    /**
     * Returns the parent DeclarationDescriptor for a given DeclarationDescriptor. For most
     * descriptor types, this simply returns the 'containing' descriptor. For Module- or
     * PackageFragmentDescriptors, it returns the descriptor for the parent fqName of the current
     * descriptors fqName e.g. for the fqName `test.sample.main`, the parent fqName would be
     * `test.sample`.
     */
    @OptIn(SymbolInternals::class)
    private fun getParentSymbol(symbol: FirBasedSymbol<*>): FirBasedSymbol<*>? {
        val session = symbol.fir.moduleData.session
        return symbol.getContainingClassSymbol(session)
            ?: (symbol as? FirBasedSymbol<*>)?.let {
                try {
                    session.firProvider.getContainingFile(it)?.symbol
                } catch (ex: IllegalStateException) {
                    null
                }
            }
    }

    /**
     * generates the synthetic class name from the source file
     * https://kotlinlang.org/docs/java-to-kotlin-interop.html#package-level-functions
     */
    @OptIn(SymbolInternals::class)
    private fun sourceFileToClassSymbol(symbol: FirBasedSymbol<*>): String {
        val callableSymbol = (symbol as? FirCallableSymbol<*>) ?: return ""
        val packageName =
            (callableSymbol.getContainingSymbol(symbol.moduleData.session) as? FirFileSymbol)
                ?.fir
                ?.name
                ?: symbol.callableId.packageName.asString()
        return "${packageName}.${callableSymbol.callableId.callableName.asString()}"
    }

    @OptIn(SymbolInternals::class)
    private fun semanticdbDescriptor(symbol: FirBasedSymbol<*>): SemanticdbSymbolDescriptor {
        return when {
            symbol is FirClassLikeSymbol ->
                SemanticdbSymbolDescriptor(Kind.TYPE, symbol.classId.asString())
            symbol is FirPropertyAccessorSymbol &&
                symbol.fir.nameOrSpecialName.asStringStripSpecialMarkers().startsWith("set") ->
                SemanticdbSymbolDescriptor(
                    Kind.METHOD,
                    "set" + symbol.propertySymbol.fir.name.toString().capitalizeAsciiOnly())
            symbol is FirPropertyAccessorSymbol &&
                symbol.fir.nameOrSpecialName.asStringStripSpecialMarkers().startsWith("get") ->
                SemanticdbSymbolDescriptor(
                    Kind.METHOD,
                    "get" + symbol.propertySymbol.fir.name.toString().capitalizeAsciiOnly())
            symbol is FirFunctionSymbol ->
                SemanticdbSymbolDescriptor(
                    Kind.METHOD, symbol.name.toString(), methodDisambiguator(symbol))
            symbol is FirTypeParameterSymbol ->
                SemanticdbSymbolDescriptor(Kind.TYPE_PARAMETER, symbol.name.toString())
            symbol is FirValueParameterSymbol ->
                SemanticdbSymbolDescriptor(Kind.PARAMETER, symbol.name.toString())
            symbol is FirVariableSymbol ->
                SemanticdbSymbolDescriptor(Kind.TERM, symbol.name.toString())
            symbol is FirTypeAliasSymbol ->
                SemanticdbSymbolDescriptor(Kind.TYPE, symbol.name.toString())
            else -> {
                err.println("unknown symbol kind ${symbol.javaClass.simpleName}")
                SemanticdbSymbolDescriptor.NONE
            }
        }
    }

    @OptIn(SymbolInternals::class)
    fun disambiguateCallableSymbol(callableSymbol: FirCallableSymbol<*>): String {
        val callableId = callableSymbol.callableId
        val callableName = callableId.callableName.asString()
        val fqName = callableId.packageName.asString()

        // Get the FIR element associated with the callable symbol
        val firFunction = callableSymbol.fir as? FirFunction ?: return "$fqName.$callableName"

        // Get parameter types from the function's value parameters
        val parameterTypes =
            firFunction.valueParameters.joinToString(separator = ", ") {
                it.returnTypeRef.coneType.render()
            }

        // Get the return type (for functions and properties)
        val returnType = firFunction.returnTypeRef.coneType.render()

        // Create a string representing the fully qualified name + signature
        return "$fqName.$callableName($parameterTypes): $returnType"
    }

    // Extension function to render a ConeKotlinType to a string
    fun ConeKotlinType?.render(): String = this?.toString() ?: "Unit"

    private fun disambiguateClassSymbol(classSymbol: FirClassSymbol<*>): String {
        val classId = classSymbol.classId
        val fqName = classId.asString()
        // You can also add additional details like visibility or modifiers if needed
        return "class $fqName"
    }

    @OptIn(SymbolInternals::class)
    private fun disambiguatePropertySymbol(propertySymbol: FirPropertySymbol): String {
        val propertyId = propertySymbol.callableId
        val fqName = propertyId.packageName.asString()
        val propertyName = propertyId.callableName.asString()
        val returnType = propertySymbol.fir.returnTypeRef.coneType.render()
        return "$fqName.$propertyName: $returnType"
    }

    private fun methodDisambiguator(symbol: FirBasedSymbol<*>): String =
        when (symbol) {
            is FirCallableSymbol<*> -> disambiguateCallableSymbol(symbol)
            is FirClassSymbol<*> -> disambiguateClassSymbol(symbol)
            is FirPropertySymbol -> disambiguatePropertySymbol(symbol)
            else -> "()"
        }

    @OptIn(SymbolInternals::class)
    private fun FirConstructorSymbol.isFromTypeAlias(): Boolean {
        val session = moduleData.session
        val classId =
            resolvedReturnTypeRef.coneTypeSafe<ConeClassLikeType>()?.classId ?: return false
        val classSymbol = session.symbolProvider.getClassLikeSymbolByClassId(classId)

        if (classSymbol is FirTypeAliasSymbol) {
            val expandedClassId = classSymbol.fir.expandedTypeRef.coneType.classId
            return expandedClassId == classId
        }
        return false
    }

    private fun FirConstructorSymbol.getTypeAliasSymbol(): FirTypeAliasSymbol? {
        val session = moduleData.session
        val classId =
            resolvedReturnTypeRef.coneTypeSafe<ConeClassLikeType>()?.classId ?: return null
        val classSymbol = session.symbolProvider.getClassLikeSymbolByClassId(classId)
        return classSymbol as? FirTypeAliasSymbol
    }

    private fun getAllMethods(
        desc: FunctionDescriptor,
        ownerDecl: DeclarationDescriptor
    ): Collection<CallableMemberDescriptor> =
        when (ownerDecl) {
            is PackageFragmentDescriptor ->
                ownerDecl
                    .getMemberScope()
                    .getDescriptorsFiltered(DescriptorKindFilter.FUNCTIONS)
                    .map { it as CallableMemberDescriptor }
            is ClassDescriptorWithResolutionScopes -> {
                when (desc) {
                    is ClassConstructorDescriptor -> {
                        val constructors =
                            (desc.containingDeclaration as ClassDescriptorWithResolutionScopes)
                                .constructors as
                                ArrayList
                        // primary constructor always seems to be last, so move it to the start.
                        if (constructors.last().isPrimary)
                            constructors.add(0, constructors.removeLast())
                        constructors
                    }
                    else -> ownerDecl.declaredCallableMembers
                }
            }
            is FunctionDescriptor ->
                ownerDecl.toSourceElement.getPsi()!!
                    .children
                    .first { it is KtBlockExpression }
                    .children
                    .filterIsInstance<KtNamedFunction>()
                    .map { resolver.fromDeclaration(it).single() as CallableMemberDescriptor }
            is ClassDescriptor -> {
                // Do we have to go recursively?
                // https://sourcegraph.com/github.com/JetBrains/kotlin/-/blob/idea/src/org/jetbrains/kotlin/idea/actions/generate/utils.kt?L32:5
                val methods =
                    ownerDecl
                        .unsubstitutedMemberScope
                        .getContributedDescriptors()
                        .filterIsInstance<FunctionDescriptor>()
                val staticMethods =
                    ownerDecl
                        .staticScope
                        .getContributedDescriptors()
                        .filterIsInstance<FunctionDescriptor>()
                val ctors = ownerDecl.constructors.toList()
                val allFuncs =
                    ArrayList<FunctionDescriptor>(methods.size + ctors.size + staticMethods.size)
                allFuncs.addAll(ctors)
                allFuncs.addAll(methods)
                allFuncs.addAll(staticMethods)
                allFuncs
            }
            is TypeAliasDescriptor -> {
                // We get the underlying class descriptor and restart the process recursively
                getAllMethods(desc, TypeUtils.getClassDescriptor(ownerDecl.underlyingType)!!)
            }
            else ->
                throw IllegalStateException(
                    "unexpected owner decl type '${ownerDecl.javaClass}':\n\t\tMethod: ${desc}\n\t\tParent: $ownerDecl")
        }

    override fun iterator(): Iterator<Symbol> = globals.values.iterator()
}

class LocalSymbolsCache : Iterable<Symbol> {
    private val symbols = HashMap<FirBasedSymbol<*>, Symbol>()
    private var localsCounter = 0

    val iterator: Iterable<Map.Entry<FirBasedSymbol<*>, Symbol>>
        get() = symbols.asIterable()

    val size: Int
        get() = symbols.size

    operator fun get(symbol: FirBasedSymbol<*>): Symbol? = symbols[symbol]

    operator fun plus(symbol: FirBasedSymbol<*>): Symbol {
        val result = Symbol.createLocal(localsCounter++)
        symbols.put(symbol, result)
        return result
    }

    override fun iterator(): Iterator<Symbol> = symbols.values.iterator()
}

@ExperimentalContracts
class SymbolsCache(private val globals: GlobalSymbolsCache, private val locals: LocalSymbolsCache) {
    operator fun get(symbol: FirBasedSymbol<*>) = globals[symbol, locals]
}
