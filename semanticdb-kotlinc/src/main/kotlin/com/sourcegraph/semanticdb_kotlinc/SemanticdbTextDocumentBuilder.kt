package com.sourcegraph.semanticdb_kotlinc

import com.sourcegraph.semanticdb_kotlinc.Semanticdb.SymbolOccurrence.Role
import java.lang.IllegalArgumentException
import java.nio.file.Path
import java.nio.file.Paths
import java.security.MessageDigest
import kotlin.contracts.ExperimentalContracts
import kotlin.text.Charsets.UTF_8
import org.jetbrains.kotlin.asJava.namedUnwrappedElement
import org.jetbrains.kotlin.com.intellij.lang.java.JavaLanguage
import org.jetbrains.kotlin.com.intellij.navigation.NavigationItem
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.psi.KtConstructor
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtPropertyAccessor

@ExperimentalContracts
class SemanticdbTextDocumentBuilder(
    private val sourceroot: Path,
    private val file: KtFile,
    private val lineMap: LineMap,
    private val cache: SymbolsCache
) {
    private val occurrences = mutableListOf<Semanticdb.SymbolOccurrence>()
    private val symbols = mutableListOf<Semanticdb.SymbolInformation>()

    fun build() = TextDocument {
        this.text = file.text
        this.uri = semanticdbURI()
        this.md5 = semanticdbMD5()
        this.schema = Semanticdb.Schema.SEMANTICDB4
        this.language = Semanticdb.Language.KOTLIN
        this.addAllOccurrences(occurrences)
        this.addAllSymbols(symbols)
    }

    fun emitSemanticdbData(
        symbol: Symbol,
        descriptor: DeclarationDescriptor,
        element: PsiElement,
        role: Role
    ) {
        symbolOccurrence(symbol, element, role)?.let(occurrences::add)
        if (role == Role.DEFINITION) symbols.add(symbolInformation(symbol, descriptor, element))
    }

    private fun symbolInformation(
        symbol: Symbol,
        descriptor: DeclarationDescriptor,
        element: PsiElement
    ): Semanticdb.SymbolInformation {
        return SymbolInformation {
            this.symbol = symbol.toString()
            this.displayName = displayName(element)
            this.language =
                when (element.language) {
                    is KotlinLanguage -> Semanticdb.Language.KOTLIN
                    is JavaLanguage -> Semanticdb.Language.JAVA
                    else ->
                        throw IllegalArgumentException("unexpected language ${element.language}")
                }
        }
    }

    private fun symbolOccurrence(
        symbol: Symbol,
        element: PsiElement,
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

    private fun semanticdbRange(element: PsiElement): Semanticdb.Range {
        return Range {
            startCharacter = lineMap.startCharacter(element) - 1
            startLine = lineMap.lineNumber(element) - 1
            endCharacter = lineMap.endCharacter(element) - 1
            endLine = lineMap.lineNumber(element) - 1
        }
    }

    private fun semanticdbURI(): String {
        // TODO: unix-style only
        val relative = sourceroot.relativize(Paths.get(file.virtualFilePath))
        return relative.toString()
    }

    private fun semanticdbMD5(): String =
        MessageDigest.getInstance("MD5").digest(file.text.toByteArray(UTF_8)).joinToString("") {
            "%02X".format(it)
        }

    companion object {
        private fun displayName(element: PsiElement): String =
            when (element) {
                is KtPropertyAccessor -> element.namePlaceholder.text
                is NavigationItem ->
                    when (element.namedUnwrappedElement) {
                        is KtConstructor<*> ->
                            (element.namedUnwrappedElement as KtConstructor<*>).name!!
                        else -> element.name ?: element.text
                    }
                else -> element.text
            }
    }
}
