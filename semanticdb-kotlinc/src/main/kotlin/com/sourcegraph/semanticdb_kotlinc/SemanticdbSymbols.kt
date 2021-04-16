package com.sourcegraph.semanticdb_kotlinc

inline class Symbol(val symbol: String) {
    fun isGlobal() = !isLocal()

    fun isLocal() = symbol.startsWith("local")
}

val NONE = Symbol("");
val ROOT_PACKAGE = Symbol("_root_/")

fun createGlobal(owner: Symbol, desc: Descriptor): Symbol = when {
    desc == Descriptor.NONE -> NONE
    owner != ROOT_PACKAGE -> Symbol(owner.symbol + desc.encode().symbol)
    else -> desc.encode()
}

fun createLocal(i: Int) = Symbol("local$i")

data class Descriptor(val kind: Kind, val name: String, val disambiguator: String = "") {
    companion object {
        val NONE = Descriptor(Kind.NONE, "")

        private fun encodeName(name: String): String {
            if (name.isEmpty()) return "``"
            val isStartOk = Character.isJavaIdentifierStart(name[0])
            var isPartsOk = true
            var i = 1
            while (isPartsOk && i < name.length) {
                isPartsOk = Character.isJavaIdentifierPart(name[i])
                i++
            }
            return if (isStartOk && isPartsOk) name else "`$name`"
        }
    }

    enum class Kind {
        NONE, TERM, METHOD, TYPE, PACKAGE, PARAMETER, TYPE_PARAMETER
    }

    fun encode() = Symbol(when(kind) {
        Kind.NONE -> ""
        Kind.TERM -> "${encodeName(name)}."
        Kind.METHOD -> "${encodeName(name)}${disambiguator}."
        Kind.TYPE -> "${encodeName(name)}#"
        Kind.PACKAGE -> "${encodeName(name)}/"
        Kind.PARAMETER -> "(${encodeName(name)})"
        Kind.TYPE_PARAMETER -> "[${encodeName(name)}]"
    })
}