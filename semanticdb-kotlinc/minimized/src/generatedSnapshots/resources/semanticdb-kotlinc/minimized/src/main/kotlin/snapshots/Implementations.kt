package snapshots
//      ^^^^^^^^^ reference semanticdb maven . . snapshots/

class Overrides : AutoCloseable {
//    ^^^^^^^^^ definition semanticdb maven . . snapshots/Overrides#
//              documentation ```kotlin\npublic final class Overrides : java.lang.AutoCloseable\n```
//              relationship is_reference is_implementation semanticdb maven jdk 8 java/lang/AutoCloseable#
//    ^^^^^^^^^ definition semanticdb maven . . snapshots/Overrides#`<init>`().
//              documentation ```kotlin\npublic constructor Overrides()\n```
//                ^^^^^^^^^^^^^ reference semanticdb maven jdk 8 java/lang/AutoCloseable#
    override fun close() {
//               ^^^^^ definition semanticdb maven . . snapshots/Overrides#close().
//                     documentation ```kotlin\npublic open fun close()\n```
//                     relationship is_reference is_implementation semanticdb maven jdk 8 java/lang/AutoCloseable#close().
        TODO("Not yet implemented")
//      ^^^^ reference semanticdb maven . . kotlin/StandardKt#TODO(+1).
    }
}

interface Animal {
//        ^^^^^^ definition semanticdb maven . . snapshots/Animal#
//               documentation ```kotlin\npublic interface Animal\n```
    val favoriteNumber: Int
//      ^^^^^^^^^^^^^^ definition semanticdb maven . . snapshots/Animal#favoriteNumber.
//                     documentation ```kotlin\npublic abstract val favoriteNumber: kotlin.Int\n```
//      ^^^^^^^^^^^^^^ definition semanticdb maven . . snapshots/Animal#getFavoriteNumber().
//                     documentation ```kotlin\npublic abstract val favoriteNumber: kotlin.Int\n```
//                      ^^^ reference semanticdb maven . . kotlin/Int#
    fun sound(): String
//      ^^^^^ definition semanticdb maven . . snapshots/Animal#sound().
//            documentation ```kotlin\npublic abstract fun sound(): kotlin.String\n```
//               ^^^^^^ reference semanticdb maven . . kotlin/String#
}
open class Bird : Animal {
//         ^^^^ definition semanticdb maven . . snapshots/Bird#
//              documentation ```kotlin\npublic open class Bird : snapshots.Animal\n```
//              relationship is_reference is_implementation semanticdb maven . . snapshots/Animal#
//         ^^^^ definition semanticdb maven . . snapshots/Bird#`<init>`().
//              documentation ```kotlin\npublic constructor Bird()\n```
//                ^^^^^^ reference semanticdb maven . . snapshots/Animal#
    override val favoriteNumber: Int
//               ^^^^^^^^^^^^^^ definition semanticdb maven . . snapshots/Bird#favoriteNumber.
//                              documentation ```kotlin\npublic open val favoriteNumber: kotlin.Int\n```
//                               ^^^ reference semanticdb maven . . kotlin/Int#
        get() = 42
//      ^^^ definition semanticdb maven . . snapshots/Bird#getFavoriteNumber().
//          documentation ```kotlin\npublic open fun `<get-favoriteNumber>`(): kotlin.Int\n```

    override fun sound(): String {
//               ^^^^^ definition semanticdb maven . . snapshots/Bird#sound().
//                     documentation ```kotlin\npublic open fun sound(): kotlin.String\n```
//                     relationship is_reference is_implementation semanticdb maven . . snapshots/Animal#sound().
//                        ^^^^^^ reference semanticdb maven . . kotlin/String#
        return "tweet"
    }
}
class Seagull : Bird() {
//    ^^^^^^^ definition semanticdb maven . . snapshots/Seagull#
//            documentation ```kotlin\npublic final class Seagull : snapshots.Bird\n```
//            relationship is_reference is_implementation semanticdb maven . . snapshots/Animal#
//            relationship is_reference is_implementation semanticdb maven . . snapshots/Bird#
//    ^^^^^^^ definition semanticdb maven . . snapshots/Seagull#`<init>`().
//            documentation ```kotlin\npublic constructor Seagull()\n```
//              ^^^^ reference semanticdb maven . . snapshots/Bird#`<init>`().
    override val favoriteNumber: Int
//               ^^^^^^^^^^^^^^ definition semanticdb maven . . snapshots/Seagull#favoriteNumber.
//                              documentation ```kotlin\npublic open val favoriteNumber: kotlin.Int\n```
//                               ^^^ reference semanticdb maven . . kotlin/Int#
        get() = 1337
//      ^^^ definition semanticdb maven . . snapshots/Seagull#getFavoriteNumber().
//          documentation ```kotlin\npublic open fun `<get-favoriteNumber>`(): kotlin.Int\n```
    override fun sound(): String {
//               ^^^^^ definition semanticdb maven . . snapshots/Seagull#sound().
//                     documentation ```kotlin\npublic open fun sound(): kotlin.String\n```
//                     relationship is_reference is_implementation semanticdb maven . . snapshots/Animal#sound().
//                     relationship is_reference is_implementation semanticdb maven . . snapshots/Bird#sound().
//                        ^^^^^^ reference semanticdb maven . . kotlin/String#
        return "squawk"
    }
}
