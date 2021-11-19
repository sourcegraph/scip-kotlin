package snapshots
//      ^^^^^^^^^ reference snapshots/

class Class constructor(private var banana: Int, apple: String) :
//    ^^^^^ definition snapshots/Class# class Class : kotlin.Throwable
//          ^^^^^^^^^^^ definition snapshots/Class#`<init>`(). constructor Class(banana: kotlin.Int, apple: kotlin.String)
//                                  ^^^^^^ definition snapshots/Class#banana. var banana: kotlin.Int
//                                  ^^^^^^ definition snapshots/Class#getBanana(). var banana: kotlin.Int
//                                  ^^^^^^ definition snapshots/Class#setBanana(). var banana: kotlin.Int
//                                  ^^^^^^ definition snapshots/Class#`<init>`().(banana) value-parameter banana: kotlin.Int
//                                          ^^^ reference kotlin/Int#
//                                               ^^^^^ definition snapshots/Class#`<init>`().(apple) value-parameter apple: kotlin.String
//                                                      ^^^^^^ reference kotlin/String#
    Throwable(banana.toString() + apple) {
//  ^^^^^^^^^ reference kotlin/Throwable#`<init>`().
//            ^^^^^^ reference snapshots/Class#`<init>`().(banana)
//                   ^^^^^^^^ reference kotlin/Int#toString().
//                              ^ reference kotlin/String#plus().
//                                ^^^^^ reference snapshots/Class#`<init>`().(apple)
  init {
    println("")
//  ^^^^^^^ reference kotlin/io/ConsoleKt#println(+1).
  }

  val asdf =
//    ^^^^ definition snapshots/Class#asdf. val asdf: kotlin.Any
//    ^^^^ definition snapshots/Class#getAsdf(). val asdf: kotlin.Any
      object {
        fun doStuff() = Unit
//          ^^^^^^^ definition local0 fun doStuff(): kotlin.Unit
//                      ^^^^ reference kotlin/Unit#
      }

  constructor() : this(1, "")
//^^^^^^^^^^^ definition snapshots/Class#`<init>`(+1). constructor Class()

  constructor(banana: Int) : this(banana, "")
//^^^^^^^^^^^ definition snapshots/Class#`<init>`(+2). constructor Class(banana: kotlin.Int)
//            ^^^^^^ definition snapshots/Class#`<init>`(+2).(banana) value-parameter banana: kotlin.Int
//                    ^^^ reference kotlin/Int#
//                                ^^^^^^ reference snapshots/Class#`<init>`(+2).(banana)

  fun run() {
//    ^^^ definition snapshots/Class#run(). fun run(): kotlin.Unit
    println(Class::class)
//  ^^^^^^^ reference kotlin/io/ConsoleKt#println(+1).
//          ^^^^^ reference snapshots/Class#
    println("I eat $banana for lunch")
//  ^^^^^^^ reference kotlin/io/ConsoleKt#println(+1).
//                  ^^^^^^ reference snapshots/Class#banana.
//                  ^^^^^^ reference snapshots/Class#getBanana().
//                  ^^^^^^ reference snapshots/Class#setBanana().
    banana = 42
//  ^^^^^^ reference snapshots/Class#banana.
//  ^^^^^^ reference snapshots/Class#getBanana().
//  ^^^^^^ reference snapshots/Class#setBanana().
  }
}
