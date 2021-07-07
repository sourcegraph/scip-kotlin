package snapshots

class Class(private var banana: Int, apple: String): Throwable(banana.toString()) {
    fun run() {
        println(Class::class)
        println("I eat $banana for lunch")
    }
}