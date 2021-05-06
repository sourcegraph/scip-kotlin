package com.sourcegraph.semanticdb_kotlinc

import com.sourcegraph.semanticdb_kotlinc.Semanticdb.SymbolOccurrence.Role
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtFile
import java.nio.file.Path
import java.security.MessageDigest
import kotlin.contracts.ExperimentalContracts
import kotlin.text.Charsets.UTF_8

@ExperimentalContracts
class SemanticdbEmitter(
    private val globals: GlobalSymbolsCache,
    private val locals: LocalSymbolsCache,
    private val sourceroot: Path,
    private val file: KtFile,
    private val lineMap: LineMap
) {
    private val occurrences = mutableListOf<Semanticdb.SymbolOccurrence>()
    private val symbols = mutableListOf<Semanticdb.SymbolInformation>()

    fun buildSemanticdbTextDocument() = TextDocument {
        this.text = file.text
        this.uri = semanticdbURI()
        this.md5 = semanticdbMD5()
        this.schema = Semanticdb.Schema.SEMANTICDB4
        this.language = Semanticdb.Language.KOTLIN
        this.addAllOccurrences(occurrences)
        this.addAllSymbols(symbols)
    }

    fun emitSemanticdbData(descriptor: DeclarationDescriptor, element: KtElement, role: Role) {
        symbolOccurrence(descriptor, element, role)?.let(occurrences::add)
        if (role == Role.DEFINITION) symbolInformation(descriptor, element).let(symbols::add)
    }

    private fun symbolInformation(descriptor: DeclarationDescriptor, element: KtElement): Semanticdb.SymbolInformation {
        return SymbolInformation {
            this.symbol = globals.semanticdbSymbol(descriptor, locals).symbol
        }
    }

    private fun symbolOccurrence(descriptor: DeclarationDescriptor, element: KtElement, role: Role): Semanticdb.SymbolOccurrence? {
        /*val symbol = when(val s = globals.semanticdbSymbol(descriptor, locals)) {
            Symbol.NONE -> return null
            else -> s
        }.symbol*/

        return SymbolOccurrence {
            this.symbol = globals.semanticdbSymbol(descriptor, locals).symbol
            this.role = role
            this.range = semanticdbRange(element)
        }
    }

    private fun semanticdbRange(element: KtElement): Semanticdb.Range {
        return Range {
            startCharacter = lineMap.startCharacter(element) - 1
            startLine = lineMap.lineNumber(element) - 1
            endCharacter = lineMap.endCharacter(element) - 1
            endLine = lineMap.lineNumber(element) - 1
        }
    }

    private fun semanticdbURI(): String {
        // TODO: unix-style only
        val relative = sourceroot.relativize(Path.of(file.virtualFilePath))
        return relative.toString()
    }

    private fun semanticdbMD5(): String = MessageDigest.getInstance("MD5").digest(file.text.toByteArray(UTF_8)).joinToString("") { "%02X".format(it) }
}

