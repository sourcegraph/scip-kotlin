package com.sourcegraph.semanticdb_kotlinc

import org.jetbrains.kotlin.com.intellij.openapi.editor.Document
import org.jetbrains.kotlin.com.intellij.openapi.project.Project
import org.jetbrains.kotlin.com.intellij.psi.PsiDocumentManager
import org.jetbrains.kotlin.diagnostics.PsiDiagnosticUtils
import org.jetbrains.kotlin.diagnostics.PsiDiagnosticUtils.LineAndColumn
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtPropertyAccessor

/**
 * Maps between an element and its identifier positions
 */
class LineMap(project: Project, file: KtFile) {
    private val document: Document = PsiDocumentManager.getInstance(project).getDocument(file)!!

    private fun offsetToLineAndCol(offset: Int): LineAndColumn = PsiDiagnosticUtils.offsetToLineAndColumn(document, offset)

    /**
     * Returns the non-0-based start character
     */
    fun startCharacter(element: KtElement): Int = offsetToLineAndCol(element.textOffset).column

    /**
     * Returns the non-0-based end character
     */
    fun endCharacter(element: KtElement): Int = startCharacter(element) +
            (element.name ?: when(element) {
                is KtPropertyAccessor -> element.namePlaceholder.text
                else -> element.text
            }).removeSuffix("?").length

    /**
     * Returns the non-0-based line number
     */
    fun lineNumber(element: KtElement): Int = document.getLineNumber(element.textOffset) + 1
}