package snapshots
//      ^^^^^^^^^ reference snapshots/

class Class(private var banana: Int, apple: String): Throwable(banana.toString()) {
//    ^^^^^ definition snapshots/Class# Class
//                      ^^^^^^ definition snapshots/Class#banana. banana
//                      ^^^^^^ definition snapshots/Class#getBanana(). banana
//                      ^^^^^^ definition snapshots/Class#setBanana(). banana
//                      ^^^^^^ definition snapshots/Class#`<init>`().(banana) banana
//                              ^^^ reference kotlin/Int#
//                                   ^^^^^ definition snapshots/Class#`<init>`().(apple) apple
//                                          ^^^^^^ reference kotlin/String#
//                                                   ^^^^^^^^^ reference kotlin/Throwable#`<init>`().
//                                                             ^^^^^^ reference snapshots/Class#`<init>`().(banana)
//                                                                    ^^^^^^^^ reference kotlin/Int#toString().
    fun run() {
//      ^^^ definition snapshots/Class#run(). run
        println(Class::class)
//      ^^^^^^^ reference kotlin/io/ConsoleKt#println(+1).
//              ^^^^^ reference snapshots/Class#
        println("I eat $banana for lunch")
//      ^^^^^^^ reference kotlin/io/ConsoleKt#println(+1).
//                      ^^^^^^ reference snapshots/Class#banana.
//                      ^^^^^^ reference snapshots/Class#getBanana().
//                      ^^^^^^ reference snapshots/Class#setBanana().
    }
}