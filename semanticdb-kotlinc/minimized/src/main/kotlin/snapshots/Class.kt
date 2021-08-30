package snapshots

class Class constructor(private var banana: Int, apple: String): Throwable(banana.toString()) {
    init {
        println("")
    }

    val asdf = object {
        fun doStuff() = Unit
    }

    constructor(): this(1, "")

    constructor(banana: Int): this(banana, "")

    fun run() {
        println(Class::class)
        println("I eat $banana for lunch")
    }
}