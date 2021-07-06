package com.sourcegraph.semanticdb_kotlinc

import com.sourcegraph.semanticdb_kotlinc.Semanticdb.SymbolOccurrence.Role
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.psi.*
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

    private fun Sequence<Symbol>?.emitAll(element: KtElement, role: Role): List<Symbol>? = this?.onEach {
        emitter.emitSemanticdbData(it, element, role)
    }?.toList()

    override fun visitClass(klass: KtClass) {
        val desc = resolver.fromDeclaration(klass)!!
        val symbols = globals[desc, locals].emitAll(klass, Role.DEFINITION)
        println("NAMED TYPE $klass ${desc.name} $symbols")
        super.visitClass(klass)
    }

    override fun visitNamedFunction(function: KtNamedFunction) {
        val desc = resolver.fromDeclaration(function)!!
        val symbols = globals[desc, locals].emitAll(function, Role.DEFINITION)
        println("NAMED FUN $function ${desc.name} $symbols")
        super.visitNamedFunction(function)
    }

    override fun visitProperty(property: KtProperty) {
        val desc = resolver.fromDeclaration(property)!!
        val symbols = globals[desc, locals].emitAll(property, Role.DEFINITION)
        println("NAMED PROP $property ${desc.name} $symbols")
        super.visitProperty(property)
    }

    override fun visitParameter(parameter: KtParameter) {
        val desc = resolver.fromDeclaration(parameter)!!
        val symbols = globals[desc, locals].emitAll(parameter, Role.DEFINITION)
        println("NAMED PARAM $parameter ${desc.name} $symbols")
        super.visitParameter(parameter)
    }

    override fun visitTypeParameter(parameter: KtTypeParameter) {
        val desc = resolver.fromDeclaration(parameter)!!
        val symbols = globals[desc, locals].emitAll(parameter, Role.DEFINITION)
        println("NAMED TYPE-PARAM $parameter ${desc.name} $symbols")
        super.visitTypeParameter(parameter)
    }

    override fun visitTypeAlias(typeAlias: KtTypeAlias) {
        val desc = resolver.fromDeclaration(typeAlias)!!
        val symbols = globals[desc, locals].emitAll(typeAlias, Role.DEFINITION)
        println("NAMED TYPE-ALIAS $typeAlias ${desc.name} $symbols")
        super.visitTypeAlias(typeAlias)
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