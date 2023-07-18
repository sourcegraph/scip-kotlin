package snapshots
//      ^^^^^^^^^ reference semanticdb maven . . snapshots/

class Class constructor(private var banana: Int, apple: String) :
//    ^^^^^ definition semanticdb maven . . snapshots/Class#
//          documentation ```kt\npublic final class Class : kotlin.Throwable\n```
//          relationship is_reference is_implementation semanticdb maven . . kotlin/Throwable#
//          ^^^^^^^^^^^ definition semanticdb maven . . snapshots/Class#`<init>`().
//                      documentation ```kt\npublic constructor Class(banana: kotlin.Int, apple: kotlin.String)\n```
//                                  ^^^^^^ definition semanticdb maven . . snapshots/Class#`<init>`().(banana)
//                                         documentation ```kt\nvalue-parameter banana: kotlin.Int\n```
//                                  ^^^^^^ definition semanticdb maven . . snapshots/Class#banana.
//                                         documentation ```kt\nprivate final var banana: kotlin.Int\n```
//                                  ^^^^^^ definition semanticdb maven . . snapshots/Class#getBanana().
//                                         documentation ```kt\nprivate final var banana: kotlin.Int\n```
//                                  ^^^^^^ definition semanticdb maven . . snapshots/Class#setBanana().
//                                         documentation ```kt\nprivate final var banana: kotlin.Int\n```
//                                          ^^^ reference semanticdb maven . . kotlin/Int#
//                                               ^^^^^ definition semanticdb maven . . snapshots/Class#`<init>`().(apple)
//                                                     documentation ```kt\nvalue-parameter apple: kotlin.String\n```
//                                                      ^^^^^^ reference semanticdb maven . . kotlin/String#
    Throwable(banana.toString() + apple) {
//  ^^^^^^^^^ reference semanticdb maven . . kotlin/Throwable#`<init>`().
//            ^^^^^^ reference semanticdb maven . . snapshots/Class#`<init>`().(banana)
//                   ^^^^^^^^ reference semanticdb maven . . kotlin/Int#toString().
//                              ^ reference semanticdb maven . . kotlin/String#plus().
//                                ^^^^^ reference semanticdb maven . . snapshots/Class#`<init>`().(apple)
  init {
    println("")
//  ^^^^^^^ reference semanticdb maven . . kotlin/io/ConsoleKt#println(+1).
  }

  val asdf =
//    ^^^^ definition semanticdb maven . . snapshots/Class#asdf.
//         documentation ```kt\npublic final val asdf: kotlin.Any\n```
//    ^^^^ definition semanticdb maven . . snapshots/Class#getAsdf().
//         documentation ```kt\npublic final val asdf: kotlin.Any\n```
      object {
        fun doStuff() = Unit
//          ^^^^^^^ definition local 0
//                  documentation ```kt\npublic final fun doStuff()\n```
//                      ^^^^ reference semanticdb maven . . kotlin/Unit#
      }

  constructor() : this(1, "")
//^^^^^^^^^^^ definition semanticdb maven . . snapshots/Class#`<init>`(+1).
//            documentation ```kt\npublic constructor Class()\n```

  constructor(banana: Int) : this(banana, "")
//^^^^^^^^^^^ definition semanticdb maven . . snapshots/Class#`<init>`(+2).
//            documentation ```kt\npublic constructor Class(banana: kotlin.Int)\n```
//            ^^^^^^ definition semanticdb maven . . snapshots/Class#`<init>`(+2).(banana)
//                   documentation ```kt\nvalue-parameter banana: kotlin.Int\n```
//                    ^^^ reference semanticdb maven . . kotlin/Int#
//                                ^^^^^^ reference semanticdb maven . . snapshots/Class#`<init>`(+2).(banana)

  fun run() {
//    ^^^ definition semanticdb maven . . snapshots/Class#run().
//        documentation ```kt\npublic final fun run()\n```
    println(Class::class)
//  ^^^^^^^ reference semanticdb maven . . kotlin/io/ConsoleKt#println(+1).
//          ^^^^^ reference semanticdb maven . . snapshots/Class#
    println("I eat $banana for lunch")
//  ^^^^^^^ reference semanticdb maven . . kotlin/io/ConsoleKt#println(+1).
//                  ^^^^^^ reference semanticdb maven . . snapshots/Class#banana.
//                  ^^^^^^ reference semanticdb maven . . snapshots/Class#getBanana().
//                  ^^^^^^ reference semanticdb maven . . snapshots/Class#setBanana().
    banana = 42
//  ^^^^^^ reference semanticdb maven . . snapshots/Class#banana.
//  ^^^^^^ reference semanticdb maven . . snapshots/Class#getBanana().
//  ^^^^^^ reference semanticdb maven . . snapshots/Class#setBanana().
  }
}
