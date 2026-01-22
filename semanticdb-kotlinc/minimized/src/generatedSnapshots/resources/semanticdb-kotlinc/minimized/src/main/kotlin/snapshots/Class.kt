package snapshots
//      ^^^^^^^^^ reference semanticdb maven . . snapshots/

class Class constructor(private var banana: Int, apple: String) :
//    ^^^^^ definition semanticdb maven . . snapshots/Class#
//          documentation ```kotlin\npublic final class Class : Throwable\n```
//          relationship is_reference is_implementation semanticdb maven . . kotlin/Throwable#
//          ^^^^^^^^^^^ definition semanticdb maven . . snapshots/Class#`<init>`().
//                      documentation ```kotlin\npublic constructor(banana: Int, apple: String): Class\n```
//                                  ^^^^^^ definition semanticdb maven . . snapshots/Class#`<init>`().(banana)
//                                         documentation ```kotlin\nbanana: Int\n```
//                                  ^^^^^^ reference semanticdb maven . . snapshots/Class#`<init>`().(banana)
//                                  ^^^^^^ definition semanticdb maven . . snapshots/Class#banana.
//                                         documentation ```kotlin\nprivate final var banana: Int\n```
//                                  ^^^^^^ definition semanticdb maven . . snapshots/Class#getBanana().
//                                         documentation ```kotlin\nprivate get(): Int\n```
//                                  ^^^^^^ definition semanticdb maven . . snapshots/Class#setBanana().
//                                         documentation ```kotlin\nprivate set(value: Int): Unit\n```
//                                  ^^^^^^ definition semanticdb maven . . snapshots/Class#setBanana().(value)
//                                         documentation ```kotlin\nvalue: Int\n```
//                                          ^^^ reference semanticdb maven . . kotlin/Int#
//                                               ^^^^^ definition semanticdb maven . . snapshots/Class#`<init>`().(apple)
//                                                     documentation ```kotlin\napple: String\n```
//                                                      ^^^^^^ reference semanticdb maven . . kotlin/String#
    Throwable(banana.toString() + apple) {
//  ^^^^^^^^^ reference semanticdb maven . . kotlin/Throwable#
//            ^^^^^^ reference semanticdb maven . . snapshots/Class#`<init>`().(banana)
//                   ^^^^^^^^ reference semanticdb maven . . kotlin/Int#toString().
//                              ^ reference semanticdb maven . . kotlin/String#plus().
//                                ^^^^^ reference semanticdb maven . . snapshots/Class#`<init>`().(apple)
  init {
    println("")
//  ^^^^^^^ reference semanticdb maven . . kotlin/io/println().
  }

  val asdf =
//    ^^^^ definition semanticdb maven . . snapshots/Class#asdf.
//         documentation ```kotlin\npublic final val asdf: Any\n```
//    ^^^^ definition semanticdb maven . . snapshots/Class#getAsdf().
//         documentation ```kotlin\npublic get(): Any\n```
      object {
//    ^^^^^^ definition semanticdb maven . . snapshots/`<anonymous object at 177>`#
//           documentation ```kotlin\nobject : Any\n```
//    ^^^^^^ definition semanticdb maven . . snapshots/`<anonymous object at 177>`#`<init>`().
//           documentation ```kotlin\nprivate constructor(): <anonymous>\n```
        fun doStuff() = Unit
//          ^^^^^^^ definition semanticdb maven . . snapshots/`<anonymous object at 177>`#doStuff().
//                  documentation ```kotlin\npublic final fun doStuff(): Unit\n```
      }

  constructor() : this(1, "")
//^^^^^^^^^^^^^^^^^^^^^^^^^^^ definition semanticdb maven . . snapshots/Class#`<init>`(+1).
//                            documentation ```kotlin\npublic constructor(): Class\n```

  constructor(banana: Int) : this(banana, "")
//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ definition semanticdb maven . . snapshots/Class#`<init>`(+2).
//                                            documentation ```kotlin\npublic constructor(banana: Int): Class\n```
//            ^^^^^^ definition semanticdb maven . . snapshots/Class#`<init>`(+2).(banana)
//                   documentation ```kotlin\nbanana: Int\n```
//                    ^^^ reference semanticdb maven . . kotlin/Int#
//                                ^^^^^^ reference semanticdb maven . . snapshots/Class#`<init>`(+2).(banana)

  fun run() {
//    ^^^ definition semanticdb maven . . snapshots/Class#run().
//        documentation ```kotlin\npublic final fun run(): Unit\n```
    println(Class::class)
//  ^^^^^^^ reference semanticdb maven . . kotlin/io/println().
    println("I eat $banana for lunch")
//  ^^^^^^^ reference semanticdb maven . . kotlin/io/println().
//                  ^^^^^^ reference semanticdb maven . . snapshots/Class#banana.
//                  ^^^^^^ reference semanticdb maven . . snapshots/Class#getBanana().
//                  ^^^^^^ reference semanticdb maven . . snapshots/Class#setBanana().
    banana = 42
//  ^^^^^^ reference semanticdb maven . . snapshots/Class#banana.
//  ^^^^^^ reference semanticdb maven . . snapshots/Class#getBanana().
//  ^^^^^^ reference semanticdb maven . . snapshots/Class#setBanana().
  }
}
