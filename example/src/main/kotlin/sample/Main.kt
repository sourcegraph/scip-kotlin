package sample

class Main<T> {
    private val test: String = ""

    /**
     * Some comment
     */
    fun method(something: Any): Array<T?>? {
        var i = 1
        val j = 2
        println("$i $j")
        return null
    }

    fun method(burger: String) {}

    val helloWorld =
        object {
            val hello = "Hello"
            val world = "World"

            override fun toString() = "$hello $world"
        }
}

val bananas = 1

fun test() = Unit

typealias Stringer = String
