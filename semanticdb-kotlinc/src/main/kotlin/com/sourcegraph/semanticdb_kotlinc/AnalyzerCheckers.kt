package com.sourcegraph.semanticdb_kotlinc

import java.nio.file.Path
import kotlin.contracts.ExperimentalContracts
import org.jetbrains.kotlin.*
import org.jetbrains.kotlin.com.intellij.lang.LighterASTNode
import org.jetbrains.kotlin.com.intellij.util.diff.FlyweightCapableTreeStructure
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.collectDescendantsOfType
import org.jetbrains.kotlin.diagnostics.findChildByType
import org.jetbrains.kotlin.diagnostics.findLastDescendant
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.*
import org.jetbrains.kotlin.fir.analysis.checkers.expression.ExpressionCheckers
import org.jetbrains.kotlin.fir.analysis.checkers.expression.FirQualifiedAccessExpressionChecker
import org.jetbrains.kotlin.fir.analysis.checkers.getContainingClassSymbol
import org.jetbrains.kotlin.fir.analysis.checkers.toClassLikeSymbol
import org.jetbrains.kotlin.fir.analysis.extensions.FirAdditionalCheckersExtension
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.expressions.FirQualifiedAccessExpression
import org.jetbrains.kotlin.fir.references.FirResolvedNamedReference
import org.jetbrains.kotlin.fir.resolve.calls.FirSyntheticFunctionSymbol
import org.jetbrains.kotlin.fir.resolve.providers.symbolProvider
import org.jetbrains.kotlin.fir.resolve.toClassLikeSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirAnonymousObjectSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirPropertySymbol
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName

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
        override val classLikeCheckers: Set<FirClassLikeChecker> =
            setOf(SemanticClassLikeChecker())
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
            val visitor = visitors[ktFile]

            val eachFqNameElement =
                {
                fqName: FqName,
                tree: FlyweightCapableTreeStructure<LighterASTNode>,
                names: LighterASTNode,
                callback: (FqName, KtLightSourceElement) -> Unit ->
                val nameList =
                    if (names.tokenType == KtNodeTypes.REFERENCE_EXPRESSION) listOf(names)
                    else tree.collectDescendantsOfType(names, KtNodeTypes.REFERENCE_EXPRESSION)

                var ancestor = fqName
                var depth = 0
                while (ancestor != FqName.ROOT) {
                    val nameNode = nameList[nameList.lastIndex - depth]
                    val nameSource = nameNode.toKtLightSourceElement(tree)

                    callback(ancestor, nameSource)

                    ancestor = ancestor.parent()
                    depth++
                }
            }

            val packageDirective = declaration.packageDirective
            val fqName = packageDirective.packageFqName
            val source = packageDirective.source
            if (source != null) {
                val names = source.treeStructure.findLastDescendant(source.lighterASTNode) { true }
                if (names != null) {
                    eachFqNameElement(fqName, source.treeStructure, names) { fqName, name ->
                        visitor?.visitPackage(fqName, name, context)
                    }
                }
            }

            declaration.imports.forEach { import ->
                val source = import.source ?: return@forEach
                val fqName = import.importedFqName ?: return@forEach

                val names = source.treeStructure.findLastDescendant(source.lighterASTNode) { true }
                if (names != null) {
                    eachFqNameElement(fqName, source.treeStructure, names) { fqName, name ->
                        val symbolProvider = context.session.symbolProvider

                        val klass =
                            symbolProvider.getClassLikeSymbolByClassId(ClassId.topLevel(fqName))
                        val callables =
                            symbolProvider.getTopLevelCallableSymbols(
                                fqName.parent(), fqName.shortName())

                        if (klass != null) {
                            visitor?.visitClassReference(klass, name, context)
                        } else if (callables.isNotEmpty()) {
                            for (callable in callables) {
                                visitor?.visitCallableReference(callable, name, context)
                            }
                        } else {
                            visitor?.visitPackage(fqName, name, context)
                        }
                    }
                }
            }
        }
    }

    private class SemanticClassLikeChecker : FirClassLikeChecker(MppCheckerKind.Common) {
        @OptIn(ExperimentalContracts::class)
        override fun check(
            declaration: FirClassLikeDeclaration,
            context: CheckerContext,
            reporter: DiagnosticReporter
        ) {
            val source = declaration.source ?: return
            val ktFile = context.containingFile?.sourceFile ?: return
            val visitor = visitors[ktFile]
            val objectKeyword = if (declaration is FirAnonymousObject) {
                source
                    .treeStructure
                    .findChildByType(source.lighterASTNode, KtTokens.OBJECT_KEYWORD)
                    ?.toKtLightSourceElement(source.treeStructure)
            } else {
                null
            }
            visitor?.visitClassOrObject(declaration, objectKeyword ?: getIdentifier(source), context)

            if (declaration is FirClass) {
                for (superType in declaration.superTypeRefs) {
                    val superSymbol = superType.toClassLikeSymbol(context.session)
                    val superSource = superType.source
                    if (superSymbol != null && superSource != null) {
                        visitor?.visitClassReference(superSymbol, superSource, context)
                    }
                }
            }
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

                val objectKeyword = if (klass is FirAnonymousObjectSymbol) {
                    source
                        .treeStructure
                        .findChildByType(source.lighterASTNode, KtTokens.OBJECT_KEYWORD)
                        ?.toKtLightSourceElement(source.treeStructure)
                } else {
                    null
                }

                visitor?.visitPrimaryConstructor(declaration, constructorKeyboard ?: objectKeyword ?: getIdentifier(klassSource), context)
            } else {
                visitor?.visitSecondaryConstructor(declaration, getIdentifier(source), context)
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
            visitor?.visitNamedFunction(declaration, getIdentifier(source), context)
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
            visitor?.visitNamedFunction(declaration, source, context)
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
            visitor?.visitProperty(declaration, getIdentifier(source), context)

            val klass = declaration.returnTypeRef.toClassLikeSymbol(context.session)
            val klassSource = declaration.returnTypeRef.source
            if (klass != null && klassSource != null) {
                visitor?.visitClassReference(klass, getIdentifier(klassSource), context)
            }
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
            visitor?.visitParameter(declaration, getIdentifier(source), context)
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
            visitor?.visitTypeParameter(declaration, getIdentifier(source), context)
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
            visitor?.visitTypeAlias(declaration, getIdentifier(source), context)
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

            visitor?.visitPropertyAccessor(declaration, identifierSource, context)
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
            visitor?.visitSimpleNameExpression(calleeReference, getIdentifier(calleeReference.source ?: source), context)

            val resolvedSymbol = calleeReference.resolvedSymbol
            if (resolvedSymbol.origin == FirDeclarationOrigin.SamConstructor && resolvedSymbol is FirSyntheticFunctionSymbol) {
                val referencedKlass = resolvedSymbol.resolvedReturnType.toClassLikeSymbol(context.session)
                if (referencedKlass != null) {
                    visitor?.visitClassReference(referencedKlass, getIdentifier(calleeReference.source ?: source), context)
                }
            }

            // When encountering a reference to a property symbol, emit both getter and setter symbols
            if (resolvedSymbol is FirPropertySymbol) {
                resolvedSymbol.getterSymbol?.let {
                    visitor?.visitCallableReference(it, getIdentifier(calleeReference.source ?: source), context)
                }
                resolvedSymbol.setterSymbol?.let {
                    visitor?.visitCallableReference(it, getIdentifier(calleeReference.source ?: source), context)
                }
            }
        }
    }
}
