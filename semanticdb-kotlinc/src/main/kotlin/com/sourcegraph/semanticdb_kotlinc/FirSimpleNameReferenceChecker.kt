package com.sourcegraph.semanticdb_kotlinc

import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.expression.FirQualifiedAccessExpressionChecker
import org.jetbrains.kotlin.fir.expressions.FirQualifiedAccessExpression
import org.jetbrains.kotlin.fir.references.impl.FirSimpleNamedReference

object FirSimpleNameReferenceChecker : FirQualifiedAccessExpressionChecker(MppCheckerKind.Common) {
    override fun check(
        expression: FirQualifiedAccessExpression,
        context: CheckerContext,
        reporter: DiagnosticReporter
    ) {
        val callee = expression.calleeReference as? FirSimpleNamedReference ?: return

        // Traverse the declaration's body, looking for references
        //        declaration.accept(object : FirVisitorVoid() {
        //            override fun visitSimpleNameReference(simpleNameReference:
        // FirSimpleNameReference) {
        //                super.visitSimpleNameReference(simpleNameReference)
        //                // Add custom logic for simple name references
        //                // For example, you could log or report errors on unresolved references
        //                if (simpleNameReference is FirErrorNamedReference) {
        //                    reporter.reportOn(simpleNameReference.source,
        // FirErrors.UNRESOLVED_REFERENCE, context)
        //                }
        //            }
        //        })
    }
}
