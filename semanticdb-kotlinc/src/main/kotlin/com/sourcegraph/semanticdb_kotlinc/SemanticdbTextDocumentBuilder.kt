package com.sourcegraph.semanticdb_kotlinc

import com.sourcegraph.semanticdb_kotlinc.Semanticdb.SymbolOccurrence.Role
import java.lang.IllegalArgumentException
import java.lang.StringBuilder
import java.nio.file.Path
import java.nio.file.Paths
import java.security.MessageDigest
import kotlin.contracts.ExperimentalContracts
import org.jetbrains.kotlin.KtLightSourceElement
import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.KtSourceFile
import org.jetbrains.kotlin.com.intellij.lang.LighterASTNode
import org.jetbrains.kotlin.com.intellij.lang.java.JavaLanguage
import org.jetbrains.kotlin.com.intellij.openapi.util.Ref
import org.jetbrains.kotlin.com.intellij.util.diff.FlyweightCapableTreeStructure
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.containingClassLookupTag
import org.jetbrains.kotlin.fir.declarations.FirRegularClass
import org.jetbrains.kotlin.fir.declarations.FirSimpleFunction
import org.jetbrains.kotlin.fir.declarations.utils.isOverride
import org.jetbrains.kotlin.fir.psi
import org.jetbrains.kotlin.fir.render
import org.jetbrains.kotlin.fir.resolve.toSymbol
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.fir.symbols.SymbolInternals
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirPropertyAccessorSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirPropertySymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirVariableSymbol
import org.jetbrains.kotlin.fir.types.ConeClassLikeType
import org.jetbrains.kotlin.fir.types.coneType
import org.jetbrains.kotlin.fir.types.coneTypeOrNull
import org.jetbrains.kotlin.fir.types.coneTypeSafe
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi
import org.jetbrains.kotlin.text

@ExperimentalContracts
class SemanticdbTextDocumentBuilder(
    private val sourceroot: Path,
    private val file: KtSourceFile,
    private val lineMap: LineMap,
    private val cache: SymbolsCache
) {
    private val occurrences = mutableListOf<Semanticdb.SymbolOccurrence>()
    private val symbols = mutableListOf<Semanticdb.SymbolInformation>()
    private val fileText = file.getContentsAsStream().reader().readText()
    private val semanticMd5 = semanticdbMD5()

    fun build() = TextDocument {
        this.text = fileText
        this.uri = semanticdbURI()
        this.md5 = semanticMd5
        this.schema = Semanticdb.Schema.SEMANTICDB4
        this.language = Semanticdb.Language.KOTLIN
        this.addAllOccurrences(occurrences)
        this.addAllSymbols(symbols)
    }

    fun emitSemanticdbData(
        firBasedSymbol: FirBasedSymbol<*>,
        symbol: Symbol,
        element: KtSourceElement,
        role: Role
    ) {
        symbolOccurrence(symbol, element, role)?.let {
            if (!occurrences.contains(it)) {
                occurrences.add(it)
            }
        }
        val symbolInformation = symbolInformation(firBasedSymbol, symbol, element)
        if (role == Role.DEFINITION && !symbols.contains(symbolInformation))
            symbols.add(symbolInformation)
    }

    private val isIgnoredSuperClass =
        setOf("kotlin.Any", "java.lang.Object", "java.io.Serializable")

    @OptIn(SymbolInternals::class)
    private fun functionDescriptorOverrides(
        functionSymbol: FirFunctionSymbol<*>
    ): Iterable<String> {
        val containingClassSymbol =
            functionSymbol
                .containingClassLookupTag()
                ?.toSymbol(functionSymbol.fir.moduleData.session) as?
                FirRegularClass
        if (containingClassSymbol != null) {
            // Get the super types of the class
            val superTypes: List<ConeClassLikeType> =
                containingClassSymbol.superTypeRefs.mapNotNull { it.coneTypeSafe() }

            val overriddenFunctions = mutableListOf<FirFunctionSymbol<*>>()

            // Traverse the super types to find functions with the same signature
            superTypes.forEach { superType ->
                val superClassSymbol =
                    superType.lookupTag.toSymbol(functionSymbol.fir.moduleData.session) as?
                        FirRegularClass
                superClassSymbol?.declarations?.forEach { declaration ->
                    if (declaration is FirSimpleFunction &&
                        declaration.isOverride &&
                        declaration.isOverride(functionSymbol)) {
                        overriddenFunctions.add(declaration.symbol)
                    }
                }
            }
            return overriddenFunctions.map { it.toString() }
        }
        return emptyList()
    }

    @OptIn(SymbolInternals::class)
    private fun FirSimpleFunction.isOverride(otherFunctionSymbol: FirFunctionSymbol<*>): Boolean {
        // Compare names
        if (this.symbol.callableId.callableName != otherFunctionSymbol.callableId.callableName)
            return false

        // Compare return types
        if (this.returnTypeRef.coneType != otherFunctionSymbol.fir.returnTypeRef.coneType)
            return false

        // Compare value parameters
        val thisParams = this.valueParameters
        val otherParams = otherFunctionSymbol.fir.valueParameters
        if (thisParams.size != otherParams.size) return false

        for ((thisParam, otherParam) in thisParams.zip(otherParams)) {
            if (thisParam.returnTypeRef.coneType != otherParam.returnTypeRef.coneType) return false
        }

        return true
    }

    @OptIn(SymbolInternals::class)
    private fun symbolInformation(
        firBasedSymbol: FirBasedSymbol<*>,
        symbol: Symbol,
        element: KtSourceElement
    ): Semanticdb.SymbolInformation {
        val supers =
            when (firBasedSymbol) {
                is FirClassSymbol ->
                    firBasedSymbol
                        .resolvedSuperTypeRefs
                        .filter {
                            (it.coneTypeOrNull as? ConeClassLikeType)?.toString() !in
                                isIgnoredSuperClass
                        }
                        .map { it.toString() }
                        .asIterable()
                is FirFunctionSymbol -> functionDescriptorOverrides(firBasedSymbol)
                else -> emptyList<String>().asIterable()
            }
        return SymbolInformation {
            this.symbol = symbol.toString()
            this.displayName = displayName(firBasedSymbol)
            this.documentation = semanticdbDocumentation(firBasedSymbol.fir, element)
            this.addAllOverriddenSymbols(supers)
            this.language =
                when (element.psi?.language ?: KotlinLanguage.INSTANCE) {
                    is KotlinLanguage -> Semanticdb.Language.KOTLIN
                    is JavaLanguage -> Semanticdb.Language.JAVA
                    else -> throw IllegalArgumentException("unexpected language")
                }
        }
    }

    private fun symbolOccurrence(
        symbol: Symbol,
        element: KtSourceElement,
        role: Role
    ): Semanticdb.SymbolOccurrence? {
        /*val symbol = when(val s = globals[descriptor, locals]) {
            Symbol.NONE -> return null
            else -> s
        }.symbol*/

        return SymbolOccurrence {
            this.symbol = symbol.toString()
            this.role = role
            this.range = semanticdbRange(element)
        }
    }

    private fun semanticdbRange(element: KtSourceElement): Semanticdb.Range {
        return Range {
            startCharacter = lineMap.startCharacter(element)
            startLine = lineMap.lineNumber(element) - 1
            endCharacter = lineMap.endCharacter(element) - 1
            endLine = lineMap.lineNumber(element) - 1
        }
    }

    private fun semanticdbURI(): String {
        // TODO: unix-style only
        val relative = sourceroot.relativize(Paths.get(file.path))
        return relative.toString()
    }

    private fun semanticdbMD5(): String =
        MessageDigest.getInstance("MD5")
            .digest(file.getContentsAsStream().readBytes())
            .joinToString("") { "%02X".format(it) }

    private fun semanticdbDocumentation(
        element: FirElement,
        source: KtSourceElement
    ): Semanticdb.Documentation = Documentation {
        format = Semanticdb.Documentation.Format.MARKDOWN
        val renderOutput = element.render()
        val kdoc = getKDocFromKtLightSourceElement(source as? KtLightSourceElement) ?: ""
        message = "```\n$renderOutput\n```\n${stripKDocAsterisks(kdoc)}"
    }

    private fun getKDocFromKtLightSourceElement(
        lightSourceElement: KtLightSourceElement?
    ): String? {
        if (lightSourceElement == null) return null
        val tree = lightSourceElement.treeStructure // FlyweightCapableTreeStructure<LighterASTNode>
        val node =
            lightSourceElement.lighterASTNode // LighterASTNode, the root of the element's structure
        return findKDoc(tree, node)
    }

    // Helper function to find the KDoc node in the AST
    private fun findKDoc(
        tree: FlyweightCapableTreeStructure<LighterASTNode>,
        node: LighterASTNode
    ): String? {
        // Recursively traverse the light tree to find a DOC_COMMENT node
        val kidsRef = Ref<Array<LighterASTNode?>>()
        tree.getChildren(node, kidsRef)
        return kidsRef.get().singleOrNull { it?.tokenType == KtTokens.DOC_COMMENT }?.toString()
    }

    // Returns the kdoc string with all leading and trailing "/*" tokens removed. Naive
    // implementation that can
    // be replaced with a utility method from the compiler in the future, if one exists.
    private fun stripKDocAsterisks(kdoc: String): String {
        if (kdoc.isEmpty()) return kdoc
        val out = StringBuilder().append("\n\n").append("----").append("\n")
        kdoc.lineSequence().forEach { line ->
            if (line.isEmpty()) return@forEach
            var start = 0
            while (start < line.length && line[start].isWhitespace()) {
                start++
            }
            if (start < line.length && line[start] == '/') {
                start++
            }
            while (start < line.length && line[start] == '*') {
                start++
            }
            var end = line.length - 1
            if (end > start && line[end] == '/') {
                end--
            }
            while (end > start && line[end] == '*') {
                end--
            }
            while (end > start && line[end].isWhitespace()) {
                end--
            }
            start = minOf(start, line.length - 1)
            if (end > start) {
                end++
            }
            out.append("\n").append(line, start, end)
        }
        return out.toString()
    }

    companion object {
        @OptIn(SymbolInternals::class)
        private fun displayName(firBasedSymbol: FirBasedSymbol<*>): String =
            when (firBasedSymbol) {
                is FirClassSymbol -> firBasedSymbol.classId.asSingleFqName().asString()
                is FirPropertyAccessorSymbol -> firBasedSymbol.fir.propertySymbol.name.asString()
                is FirFunctionSymbol -> firBasedSymbol.callableId.callableName.asString()
                is FirPropertySymbol -> firBasedSymbol.callableId.callableName.asString()
                is FirVariableSymbol -> firBasedSymbol.name.asString()
                else -> firBasedSymbol.toString()
            }
    }
}
