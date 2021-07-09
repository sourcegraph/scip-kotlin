package com.sourcegraph.semanticdb_kotlinc

import com.sourcegraph.semanticdb_kotlinc.Semanticdb.SymbolOccurrence.Role
import org.jetbrains.kotlin.com.intellij.psi.NavigatablePsiElement
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.containingClass
import java.nio.file.Path
import kotlin.contracts.ExperimentalContracts

@ExperimentalContracts
class SemanticdbVisitor(
    sourceroot: Path,
    private val resolver: DescriptorResolver,
    private val file: KtFile,
    lineMap: LineMap,
    private val globals: GlobalSymbolsCache,
    private val locals: LocalSymbolsCache = LocalSymbolsCache()
): KtTreeVisitorVoid() {
    private val emitter = SemanticdbTextDocumentEmitter(sourceroot, file, lineMap)

    fun build(): Semanticdb.TextDocument {
        super.visitKtFile(file)
        return emitter.buildSemanticdbTextDocument()
    }

    private fun Sequence<Symbol>?.emitAll(element: PsiElement, role: Role): List<Symbol>? = this?.onEach {
        emitter.emitSemanticdbData(it, element, role)
    }?.toList()

    override fun visitClass(klass: KtClass) {
        val desc = resolver.fromDeclaration(klass).single()
        var symbols = globals[desc, locals].emitAll(klass, Role.DEFINITION)
        println("CLASS $klass ${desc.name} $symbols")
        if (!klass.hasExplicitPrimaryConstructor()) {
            resolver.syntheticConstructor(klass)?.apply {
                symbols = globals[this, locals].emitAll(klass, Role.DEFINITION)
                println("SYNTHETIC CONSTRUCTOR $klass ${this.name} $symbols")
            }
        }
        super.visitClass(klass)
    }

    override fun visitPrimaryConstructor(constructor: KtPrimaryConstructor) {
        val desc = resolver.fromDeclaration(constructor).single()
        // if the constructor is not denoted by the 'constructor' keyword, we want to link it to the class ident
        val symbols = if (!constructor.hasConstructorKeyword()) {
            globals[desc, locals].emitAll(constructor.containingClass()!!, Role.DEFINITION)
        } else {
            globals[desc, locals].emitAll(constructor.getConstructorKeyword()!!, Role.DEFINITION)
        }
        println("PRIMARY CONSTRUCTOR ${constructor.identifyingElement?.parent ?: constructor.containingClass()} ${desc.name} $symbols")
        super.visitPrimaryConstructor(constructor)
    }

    override fun visitSecondaryConstructor(constructor: KtSecondaryConstructor) {
        val desc = resolver.fromDeclaration(constructor).single()
        val symbols = globals[desc, locals].emitAll(constructor.getConstructorKeyword(), Role.DEFINITION)
        println("SECONDARY COSNTRUCTOR ${constructor.parent} ${desc.name} $symbols")
        super.visitSecondaryConstructor(constructor)
    }

    override fun visitNamedFunction(function: KtNamedFunction) {
        val desc = resolver.fromDeclaration(function).single()
        val symbols = globals[desc, locals].emitAll(function, Role.DEFINITION)
        println("NAMED FUN $function ${desc.name} $symbols")
        super.visitNamedFunction(function)
    }

    override fun visitProperty(property: KtProperty) {
        val desc = resolver.fromDeclaration(property).single()
        val symbols = globals[desc, locals].emitAll(property, Role.DEFINITION)
        println("NAMED PROP $property ${desc.name} $symbols")
        super.visitProperty(property)
    }

    override fun visitParameter(parameter: KtParameter) {
        val symbols = resolver.fromDeclaration(parameter).flatMap { desc ->
            globals[desc, locals]
        }.emitAll(parameter, Role.DEFINITION)
        println("NAMED PARAM $parameter $symbols")
        super.visitParameter(parameter)
    }

    override fun visitTypeParameter(parameter: KtTypeParameter) {
        val desc = resolver.fromDeclaration(parameter).single()
        val symbols = globals[desc, locals].emitAll(parameter, Role.DEFINITION)
        println("NAMED TYPE-PARAM $parameter ${desc.name} $symbols")
        super.visitTypeParameter(parameter)
    }

    override fun visitTypeAlias(typeAlias: KtTypeAlias) {
        val desc = resolver.fromDeclaration(typeAlias).single()
        val symbols = globals[desc, locals].emitAll(typeAlias, Role.DEFINITION)
        println("NAMED TYPE-ALIAS $typeAlias ${desc.name} $symbols")
        super.visitTypeAlias(typeAlias)
    }

    override fun visitPropertyAccessor(accessor: KtPropertyAccessor) {
        val desc = resolver.fromDeclaration(accessor).single()
        val symbols = globals[desc, locals].emitAll(accessor, Role.DEFINITION)
        println("PROPERTY ACCESSOR $accessor ${desc.name} $symbols")
        super.visitPropertyAccessor(accessor)
    }

    override fun visitSimpleNameExpression(expression: KtSimpleNameExpression) {
        val desc = resolver.fromReference(expression) ?: run {
            println("NULL DESCRIPTOR FROM NAME EXPRESSION $expression ${expression.javaClass}")
            super.visitSimpleNameExpression(expression)
            return
        }
        val symbols = globals[desc, locals].emitAll(expression, Role.REFERENCE)
        println("NAME EXPRESSION $expression ${expression.javaClass} ${desc.name} $symbols")
        super.visitSimpleNameExpression(expression)
    }
}