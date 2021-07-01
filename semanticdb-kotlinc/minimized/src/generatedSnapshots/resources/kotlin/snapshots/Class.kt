package snapshots
//      ^^^^^^^^^ reference snapshots/

class Class {
//    ^^^^^ definition snapshots/Class# Class
    fun run() {
//      ^^^ definition snapshots/Class#run(). run
        println(Class::class)
//      ^^^^^^^ reference kotlin/io/ConsoleKt#println(+1).
//              ^^^^^ reference snapshots/Class#
    }
}