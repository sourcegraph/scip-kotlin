package com.sourcegraph.semanticdb_kotlinc

import org.jetbrains.kotlin.KtSourceFile
import java.nio.file.Path
import kotlin.contracts.ExperimentalContracts
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.DeclarationCheckers
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirConstructorChecker
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirFileChecker
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirFunctionChecker
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirPropertyAccessorChecker
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirPropertyChecker
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirRegularClassChecker
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirTypeAliasChecker
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirTypeParameterChecker
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirValueParameterChecker
import org.jetbrains.kotlin.fir.analysis.checkers.expression.ExpressionCheckers
import org.jetbrains.kotlin.fir.analysis.checkers.expression.FirQualifiedAccessExpressionChecker
import org.jetbrains.kotlin.fir.analysis.extensions.FirAdditionalCheckersExtension
import org.jetbrains.kotlin.fir.declarations.FirConstructor
import org.jetbrains.kotlin.fir.declarations.FirFile
import org.jetbrains.kotlin.fir.declarations.FirFunction
import org.jetbrains.kotlin.fir.declarations.FirProperty
import org.jetbrains.kotlin.fir.declarations.FirPropertyAccessor
import org.jetbrains.kotlin.fir.declarations.FirRegularClass
import org.jetbrains.kotlin.fir.declarations.FirTypeAlias
import org.jetbrains.kotlin.fir.declarations.FirTypeParameter
import org.jetbrains.kotlin.fir.declarations.FirValueParameter
import org.jetbrains.kotlin.fir.expressions.FirQualifiedAccessExpression
import org.jetbrains.kotlin.fir.references.FirResolvedNamedReference
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.resolve.checkers.DeclarationChecker
import org.jetbrains.kotlin.resolve.checkers.DeclarationCheckerContext
import java.nio.file.Files
import java.nio.file.Paths

class AnalyzerCheckers(session: FirSession) : FirAdditionalCheckersExtension(session) {
    companion object {
        @OptIn(ExperimentalContracts::class)
        val visitors: MutableMap<KtSourceFile, SemanticdbVisitor> = mutableMapOf()
    }

    //    override val declarationCheckers: DeclarationCheckers
    //        get() = object : DeclarationCheckers() {
    //            override val fileCheckers: Set<FirFileChecker> = setOf()
    //            override val regularClassCheckers: Set<FirRegularClassChecker> = emptySet()
    //            override val functionCheckers: Set<FirFunctionChecker> = emptySet()
    //            override val propertyCheckers: Set<FirPropertyChecker> = emptySet()
    //            override val constructorCheckers: Set<FirConstructorChecker> = emptySet()
    //            override val typeParameterCheckers: Set<FirTypeParameterChecker> = emptySet()
    //            override val valueParameterCheckers: Set<FirValueParameterChecker> = emptySet()
    //            override val typeAliasCheckers: Set<FirTypeAliasChecker> = emptySet()
    //            override val propertyAccessorCheckers: Set<FirPropertyAccessorChecker> =
    // emptySet()
    //
    //        }
    override val declarationCheckers: DeclarationCheckers
        get() =
            object : DeclarationCheckers() {
                override val fileCheckers: Set<FirFileChecker> =
                    setOf(
                        SemanticFileChecker(
                            session.diTransformerService.sourceroot,
                            session.diTransformerService.callback))
                override val regularClassCheckers: Set<FirRegularClassChecker> =
                    setOf(SemanticRegularClassChecker())
                override val constructorCheckers: Set<FirConstructorChecker> =
                    setOf(SemanticConstructorChecker())
                override val functionCheckers: Set<FirFunctionChecker> =
                    setOf(SemanticFunctionChecker())
                override val propertyCheckers: Set<FirPropertyChecker> =
                    setOf(SemanticPropertyChecker())
                override val valueParameterCheckers: Set<FirValueParameterChecker> =
                    setOf(SemanticValueParameterChecker())
                override val typeParameterCheckers: Set<FirTypeParameterChecker> =
                    setOf(SemanticTypeParameterChecker())
                override val typeAliasCheckers: Set<FirTypeAliasChecker> =
                    setOf(SemanticTypeAliasChecker())
                override val propertyAccessorCheckers: Set<FirPropertyAccessorChecker> =
                    setOf(SemanticPropertAccessorChecker())
            }

    override val expressionCheckers: ExpressionCheckers
        get() =
            object : ExpressionCheckers() {
                override val qualifiedAccessExpressionCheckers:
                    Set<FirQualifiedAccessExpressionChecker> =
                    setOf(SemanticQualifiedAccessExpressionChecker())
            }

    private class SemanticFileChecker(
        private val sourceroot: Path,
        private val callback: (Semanticdb.TextDocument) -> Unit
    ) : FirFileChecker(MppCheckerKind.Common) {
        companion object {
            @OptIn(ExperimentalContracts::class) val globals = GlobalSymbolsCache()
        }

        @OptIn(ExperimentalContracts::class)
        override fun check(
            declaration: FirFile,
            context: CheckerContext,
            reporter: DiagnosticReporter
        ) {
            println("Ernald - FirFile1")
            val ktFile = declaration.sourceFile ?: return
            val source = declaration.source ?: return
            println("Ernald - FirFile2")
            val lineMap = LineMap(declaration)
            val visitor = SemanticdbVisitor(sourceroot, ktFile, lineMap, globals)
            visitors[ktFile] = visitor
        }

        private fun semanticdbOutPathForFile(session: FirSession, file: KtSourceFile): Path? {
            val sourceRoot = session.diTransformerService.sourceroot
            val normalizedPath = Paths.get(file.path).normalize()
            if (normalizedPath.startsWith(sourceRoot)) {
                val relative = sourceRoot.relativize(normalizedPath)
                val filename = relative.fileName.toString() + ".semanticdb"
                val semanticdbOutPath =
                    session.diTransformerService.targetroot
                        .resolve("META-INF")
                        .resolve("semanticdb")
                        .resolve(relative)
                        .resolveSibling(filename)

                Files.createDirectories(semanticdbOutPath.parent)
                return semanticdbOutPath
            }
            System.err.println(
                "given file is not under the sourceroot.\n\tSourceroot: $sourceRoot\n\tFile path: ${file.path}\n\tNormalized file path: $normalizedPath")
            return null
        }
    }

    private class SemanticRegularClassChecker : FirRegularClassChecker(MppCheckerKind.Common) {
        @OptIn(ExperimentalContracts::class)
        override fun check(
            declaration: FirRegularClass,
            context: CheckerContext,
            reporter: DiagnosticReporter
        ) {
            println("Ernald - FirRegularClass1")
            val source = declaration.source ?: return
            println("Ernald - FirRegularClass2")
            val ktFile = context.containingFile?.sourceFile ?: return
            println("Ernald - FirRegularClass3")
            val visitor = visitors[ktFile]
            visitor?.visitClassOrObject(declaration, source)
        }
    }

    private class SemanticConstructorChecker : FirConstructorChecker(MppCheckerKind.Common) {
        @OptIn(ExperimentalContracts::class)
        override fun check(
            declaration: FirConstructor,
            context: CheckerContext,
            reporter: DiagnosticReporter
        ) {
            val source = declaration.source ?: return
            val ktFile = context.containingFile?.sourceFile ?: return
            val visitor = visitors[ktFile]

            if (declaration.isPrimary) {
                visitor?.visitPrimaryConstructor(declaration, source)
            } else {
                visitor?.visitSecondaryConstructor(declaration, source)
            }
        }
    }

    private class SemanticFunctionChecker : FirFunctionChecker(MppCheckerKind.Common) {
        @OptIn(ExperimentalContracts::class)
        override fun check(
            declaration: FirFunction,
            context: CheckerContext,
            reporter: DiagnosticReporter
        ) {
            val source = declaration.source ?: return
            val ktFile = context.containingFile?.sourceFile ?: return
            val visitor = visitors[ktFile]
            visitor?.visitNamedFunction(declaration, source)
        }
    }

    private class SemanticPropertyChecker : FirPropertyChecker(MppCheckerKind.Common) {
        @OptIn(ExperimentalContracts::class)
        override fun check(
            declaration: FirProperty,
            context: CheckerContext,
            reporter: DiagnosticReporter
        ) {
            val source = declaration.source ?: return
            val ktFile = context.containingFile?.sourceFile ?: return
            val visitor = visitors[ktFile]
            visitor?.visitProperty(declaration, source)
        }
    }

    private class SemanticValueParameterChecker : FirValueParameterChecker(MppCheckerKind.Common) {
        @OptIn(ExperimentalContracts::class)
        override fun check(
            declaration: FirValueParameter,
            context: CheckerContext,
            reporter: DiagnosticReporter
        ) {
            val source = declaration.source ?: return
            val ktFile = context.containingFile?.sourceFile ?: return
            val visitor = visitors[ktFile]
            visitor?.visitParameter(declaration, source)
        }
    }

    private class SemanticTypeParameterChecker : FirTypeParameterChecker(MppCheckerKind.Common) {
        @OptIn(ExperimentalContracts::class)
        override fun check(
            declaration: FirTypeParameter,
            context: CheckerContext,
            reporter: DiagnosticReporter
        ) {
            val source = declaration.source ?: return
            val ktFile = context.containingFile?.sourceFile ?: return
            val visitor = visitors[ktFile]
            visitor?.visitTypeParameter(declaration, source)
        }
    }

    private class SemanticTypeAliasChecker : FirTypeAliasChecker(MppCheckerKind.Common) {
        @OptIn(ExperimentalContracts::class)
        override fun check(
            declaration: FirTypeAlias,
            context: CheckerContext,
            reporter: DiagnosticReporter
        ) {
            val source = declaration.source ?: return
            val ktFile = context.containingFile?.sourceFile ?: return
            val visitor = visitors[ktFile]
            visitor?.visitTypeAlias(declaration, source)
        }
    }

    private class SemanticPropertAccessorChecker :
        FirPropertyAccessorChecker(MppCheckerKind.Common) {
        @OptIn(ExperimentalContracts::class)
        override fun check(
            declaration: FirPropertyAccessor,
            context: CheckerContext,
            reporter: DiagnosticReporter
        ) {
            val source = declaration.source ?: return
            val ktFile = context.containingFile?.sourceFile ?: return
            val visitor = visitors[ktFile]
            visitor?.visitPropertyAccessor(declaration, source)
        }
    }

    private class SemanticQualifiedAccessExpressionChecker :
        FirQualifiedAccessExpressionChecker(MppCheckerKind.Common) {
        @OptIn(ExperimentalContracts::class)
        override fun check(
            declaration: FirQualifiedAccessExpression,
            context: CheckerContext,
            reporter: DiagnosticReporter
        ) {
            val source = declaration.source ?: return
            val calleeReference = declaration.calleeReference
            if ((calleeReference as? FirResolvedNamedReference) == null) {
                return
            }

            val ktFile = context.containingFile?.sourceFile ?: return
            val visitor = visitors[ktFile]
            visitor?.visitSimpleNameExpression(calleeReference, source)
        }
    }
}

//private fun FirElement.findKtFile(): KtSourceFile? = source?.psi?.containingFile as? KtSourceFile

/*
   KtObjectDeclaration -> FirRegularClassChecker
   KTClass -> FirRegularClassChecker
   KtPrimaryConstructor -> FirConstructorChecker
   KtSecondaryConstructor -> FirConstructorChecker
   KtNamedFunction -> FirFunctionChecker
   KtProperty -> FirPropertyChecker
   KtParameter -> FirValueParameterChecker
   KtTypeParameter -> FirTypeParameterChecker
   KtTypeAlias -> FirTypeAliasChecker
   KtPropertyAccessor -> FirPropertyAccessorChecker
   KtSimpleNameExpression -> TBD (using FirQualifiedAccessExpression)
*/

class AnalyzerDeclarationChecker : DeclarationChecker {
    override fun check(
        declaration: KtDeclaration,
        descriptor: DeclarationDescriptor,
        context: DeclarationCheckerContext
    ) = Unit
}
