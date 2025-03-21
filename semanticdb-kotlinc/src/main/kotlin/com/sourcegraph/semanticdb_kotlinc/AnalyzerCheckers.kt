package com.sourcegraph.semanticdb_kotlinc

import java.nio.file.Path
import kotlin.contracts.ExperimentalContracts
import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.KtSourceFile
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.findChildByType
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.*
import org.jetbrains.kotlin.fir.analysis.checkers.expression.ExpressionCheckers
import org.jetbrains.kotlin.fir.analysis.checkers.expression.FirQualifiedAccessExpressionChecker
import org.jetbrains.kotlin.fir.analysis.checkers.getContainingClassSymbol
import org.jetbrains.kotlin.fir.analysis.extensions.FirAdditionalCheckersExtension
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.expressions.FirQualifiedAccessExpression
import org.jetbrains.kotlin.fir.references.FirResolvedNamedReference
import org.jetbrains.kotlin.fir.resolve.providers.symbolProvider
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.toKtLightSourceElement

open class AnalyzerCheckers(session: FirSession) : FirAdditionalCheckersExtension(session) {
    companion object {
        @OptIn(ExperimentalContracts::class)
        val visitors: MutableMap<KtSourceFile, SemanticdbVisitor> = mutableMapOf()

        private fun getIdentifier(element: KtSourceElement): KtSourceElement =
            element
                .treeStructure
                .findChildByType(element.lighterASTNode, KtTokens.IDENTIFIER)
                ?.toKtLightSourceElement(element.treeStructure) ?: element
    }
    override val declarationCheckers: DeclarationCheckers
        get() = AnalyzerDeclarationCheckers(session.analyzerParamsProvider.sourceroot)

    override val expressionCheckers: ExpressionCheckers
        get() =
            object : ExpressionCheckers() {
                override val qualifiedAccessExpressionCheckers:
                    Set<FirQualifiedAccessExpressionChecker> =
                    setOf(SemanticQualifiedAccessExpressionChecker())
            }

    open class AnalyzerDeclarationCheckers(sourceroot: Path) : DeclarationCheckers() {
        override val fileCheckers: Set<FirFileChecker> =
            setOf(SemanticFileChecker(sourceroot), SemanticImportsChecker())
        override val regularClassCheckers: Set<FirRegularClassChecker> =
            setOf(SemanticRegularClassChecker())
        override val constructorCheckers: Set<FirConstructorChecker> =
            setOf(SemanticConstructorChecker())
        override val simpleFunctionCheckers: Set<FirSimpleFunctionChecker> =
            setOf(SemanticSimpleFunctionChecker())
        override val anonymousFunctionCheckers: Set<FirAnonymousFunctionChecker> =
            setOf(SemanticAnonymousFunctionChecker())
        override val propertyCheckers: Set<FirPropertyChecker> = setOf(SemanticPropertyChecker())
        override val valueParameterCheckers: Set<FirValueParameterChecker> =
            setOf(SemanticValueParameterChecker())
        override val typeParameterCheckers: Set<FirTypeParameterChecker> =
            setOf(SemanticTypeParameterChecker())
        override val typeAliasCheckers: Set<FirTypeAliasChecker> = setOf(SemanticTypeAliasChecker())
        override val propertyAccessorCheckers: Set<FirPropertyAccessorChecker> =
            setOf(SemanticPropertyAccessorChecker())
    }

    private class SemanticFileChecker(private val sourceroot: Path) :
        FirFileChecker(MppCheckerKind.Common) {
        companion object {
            @OptIn(ExperimentalContracts::class) val globals = GlobalSymbolsCache()
        }

        @OptIn(ExperimentalContracts::class)
        override fun check(
            declaration: FirFile,
            context: CheckerContext,
            reporter: DiagnosticReporter
        ) {
            val ktFile = declaration.sourceFile ?: return
            val lineMap = LineMap(declaration)
            val visitor = SemanticdbVisitor(sourceroot, ktFile, lineMap, globals)
            visitors[ktFile] = visitor
        }
    }

    class SemanticImportsChecker : FirFileChecker(MppCheckerKind.Common) {
        @OptIn(ExperimentalContracts::class)
        override fun check(
            declaration: FirFile,
            context: CheckerContext,
            reporter: DiagnosticReporter
        ) {
            val ktFile = declaration.sourceFile ?: return
            declaration.imports.forEach { import ->
                val source = import.source ?: return@forEach
                val visitor = visitors[ktFile]
                val fqName = import.importedFqName ?: return@forEach
                val importedClassSymbol =
                    context.session.symbolProvider.getClassLikeSymbolByClassId(
                        ClassId.topLevel(fqName))
                        ?: return@forEach
                visitor?.visitImport(importedClassSymbol, source)
            }
        }
    }

    private class SemanticRegularClassChecker : FirRegularClassChecker(MppCheckerKind.Common) {
        @OptIn(ExperimentalContracts::class)
        override fun check(
            declaration: FirRegularClass,
            context: CheckerContext,
            reporter: DiagnosticReporter
        ) {
            val source = declaration.source ?: return
            val ktFile = context.containingFile?.sourceFile ?: return
            val visitor = visitors[ktFile]
            visitor?.visitClassOrObject(declaration, getIdentifier(source))
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
                // if the constructor is not denoted by the 'constructor' keyword, we want to link it to the
                // class identifier
                val klass = declaration.symbol.getContainingClassSymbol()
                val klassSource = klass?.source ?: source
                val constructorKeyboard =
                    source
                        .treeStructure
                        .findChildByType(source.lighterASTNode, KtTokens.CONSTRUCTOR_KEYWORD)
                        ?.toKtLightSourceElement(source.treeStructure)

                visitor?.visitPrimaryConstructor(declaration, constructorKeyboard ?: getIdentifier(klassSource))
            } else {
                visitor?.visitSecondaryConstructor(declaration, getIdentifier(source))
            }
        }
    }

    private class SemanticSimpleFunctionChecker : FirSimpleFunctionChecker(MppCheckerKind.Common) {
        @OptIn(ExperimentalContracts::class)
        override fun check(
            declaration: FirSimpleFunction,
            context: CheckerContext,
            reporter: DiagnosticReporter
        ) {
            val source = declaration.source ?: return
            val ktFile = context.containingFile?.sourceFile ?: return
            val visitor = visitors[ktFile]
            visitor?.visitNamedFunction(declaration, getIdentifier(source))
        }
    }

    private class SemanticAnonymousFunctionChecker :
        FirAnonymousFunctionChecker(MppCheckerKind.Common) {
        @OptIn(ExperimentalContracts::class)
        override fun check(
            declaration: FirAnonymousFunction,
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
            visitor?.visitProperty(declaration, getIdentifier(source))
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
            visitor?.visitParameter(declaration, getIdentifier(source))
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
            visitor?.visitTypeParameter(declaration, getIdentifier(source))
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
            visitor?.visitTypeAlias(declaration, getIdentifier(source))
        }
    }

    private class SemanticPropertyAccessorChecker :
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
            val identifierSource =
                if (declaration.isGetter) {
                    source
                        .treeStructure
                        .findChildByType(source.lighterASTNode, KtTokens.GET_KEYWORD)
                        ?.toKtLightSourceElement(source.treeStructure)
                        ?: getIdentifier(source)
                } else if (declaration.isSetter) {
                    source
                        .treeStructure
                        .findChildByType(source.lighterASTNode, KtTokens.SET_KEYWORD)
                        ?.toKtLightSourceElement(source.treeStructure)
                        ?: getIdentifier(source)
                } else {
                    getIdentifier(source)
                }

            visitor?.visitPropertyAccessor(declaration, identifierSource)
        }
    }

    private class SemanticQualifiedAccessExpressionChecker :
        FirQualifiedAccessExpressionChecker(MppCheckerKind.Common) {
        @OptIn(ExperimentalContracts::class)
        override fun check(
            expression: FirQualifiedAccessExpression,
            context: CheckerContext,
            reporter: DiagnosticReporter
        ) {
            val source = expression.source ?: return
            val calleeReference = expression.calleeReference
            if ((calleeReference as? FirResolvedNamedReference) == null) {
                return
            }

            val ktFile = context.containingFile?.sourceFile ?: return
            val visitor = visitors[ktFile]
            visitor?.visitSimpleNameExpression(calleeReference, getIdentifier(calleeReference.source ?: source))
        }
    }
}
