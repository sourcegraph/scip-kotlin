package com.sourcegraph.semanticdb_kotlinc

import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.com.intellij.navigation.NavigationItem
import org.jetbrains.kotlin.fir.declarations.FirFile
import org.jetbrains.kotlin.psi.KtPropertyAccessor
import org.jetbrains.kotlin.text

/** Maps between an element and its identifier positions */
class LineMap(private val file: FirFile) {
    private fun offsetToLineAndCol(offset: Int): Pair<Int, Int>? =
        file.sourceFileLinesMapping?.getLineAndColumnByOffset(offset)

    /** Returns the non-0-based start character */
    fun startCharacter(element: KtSourceElement): Int =
        offsetToLineAndCol(element.startOffset)?.second ?: 0

    /** Returns the non-0-based end character */
    fun endCharacter(element: KtSourceElement): Int =
        startCharacter(element) + nameForOffset(element).length

    /** Returns the non-0-based line number */
    fun lineNumber(element: KtSourceElement): Int =
        file.sourceFileLinesMapping?.getLineByOffset(element.startOffset)?.let { it + 1 } ?: 0

    companion object {
        fun nameForOffset(element: KtSourceElement): String =
            when (element) {
                is KtPropertyAccessor -> element.namePlaceholder.text
                is NavigationItem -> element.name ?: element.text?.toString() ?: ""
                else -> element.text?.toString() ?: ""
            }
    }
}
