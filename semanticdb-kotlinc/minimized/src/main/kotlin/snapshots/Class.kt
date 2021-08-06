package snapshots

class Class constructor(var banana: Int, apple: String): Throwable(banana.toString()) {
   init {
       println("")
   }

   constructor(): this(1, "")

   constructor(banana: Int): this(banana, "")

    fun run() {
       println(Class::class)
        println("I eat $banana for lunch")
    }
}