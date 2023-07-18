package snapshots
//      ^^^^^^^^^ reference semanticdb maven . . snapshots/

class Overrides : AutoCloseable {
//    ^^^^^^^^^ definition semanticdb maven . . snapshots/Overrides#
//              documentation ```kt\npublic final class Overrides : java.lang.AutoCloseable\n```
//    ^^^^^^^^^ definition semanticdb maven . . snapshots/Overrides#`<init>`().
//              documentation ```kt\npublic constructor Overrides()\n```
//                ^^^^^^^^^^^^^ reference semanticdb maven jdk 8 java/lang/AutoCloseable#
    override fun close() {
//               ^^^^^ definition semanticdb maven . . snapshots/Overrides#close().
//                     documentation ```kt\npublic open fun close()\n```
        TODO("Not yet implemented")
//      ^^^^ reference semanticdb maven . . kotlin/StandardKt#TODO(+1).
    }
}

interface Animal {
//        ^^^^^^ definition semanticdb maven . . snapshots/Animal#
//               documentation ```kt\npublic interface Animal\n```
    val favoriteNumber: Int
//      ^^^^^^^^^^^^^^ definition semanticdb maven . . snapshots/Animal#favoriteNumber.
//                     documentation ```kt\npublic abstract val favoriteNumber: kotlin.Int\n```
//      ^^^^^^^^^^^^^^ definition semanticdb maven . . snapshots/Animal#getFavoriteNumber().
//                     documentation ```kt\npublic abstract val favoriteNumber: kotlin.Int\n```
//                      ^^^ reference semanticdb maven . . kotlin/Int#
    fun sound(): String
//      ^^^^^ definition semanticdb maven . . snapshots/Animal#sound().
//            documentation ```kt\npublic abstract fun sound(): kotlin.String\n```
//               ^^^^^^ reference semanticdb maven . . kotlin/String#
}
open class Bird : Animal {
//         ^^^^ definition semanticdb maven . . snapshots/Bird#
//              documentation ```kt\npublic open class Bird : snapshots.Animal\n```
//         ^^^^ definition semanticdb maven . . snapshots/Bird#`<init>`().
//              documentation ```kt\npublic constructor Bird()\n```
//                ^^^^^^ reference semanticdb maven . . snapshots/Animal#
    override val favoriteNumber: Int
//               ^^^^^^^^^^^^^^ definition semanticdb maven . . snapshots/Bird#favoriteNumber.
//                              documentation ```kt\npublic open val favoriteNumber: kotlin.Int\n```
//                               ^^^ reference semanticdb maven . . kotlin/Int#
        get() = 42
//      ^^^ definition semanticdb maven . . snapshots/Bird#getFavoriteNumber().
//          documentation ```kt\npublic open fun <get-favoriteNumber>(): kotlin.Int\n```

    override fun sound(): String {
//               ^^^^^ definition semanticdb maven . . snapshots/Bird#sound().
//                     documentation ```kt\npublic open fun sound(): kotlin.String\n```
//                        ^^^^^^ reference semanticdb maven . . kotlin/String#
        return "tweet"
    }
}
class Seagull : Bird() {
//    ^^^^^^^ definition semanticdb maven . . snapshots/Seagull#
//            documentation ```kt\npublic final class Seagull : snapshots.Bird\n```
//    ^^^^^^^ definition semanticdb maven . . snapshots/Seagull#`<init>`().
//            documentation ```kt\npublic constructor Seagull()\n```
//              ^^^^ reference semanticdb maven . . snapshots/Bird#`<init>`().
    override val favoriteNumber: Int
//               ^^^^^^^^^^^^^^ definition semanticdb maven . . snapshots/Seagull#favoriteNumber.
//                              documentation ```kt\npublic open val favoriteNumber: kotlin.Int\n```
//                               ^^^ reference semanticdb maven . . kotlin/Int#
        get() = 1337
//      ^^^ definition semanticdb maven . . snapshots/Seagull#getFavoriteNumber().
//          documentation ```kt\npublic open fun <get-favoriteNumber>(): kotlin.Int\n```
    override fun sound(): String {
//               ^^^^^ definition semanticdb maven . . snapshots/Seagull#sound().
//                     documentation ```kt\npublic open fun sound(): kotlin.String\n```
//                        ^^^^^^ reference semanticdb maven . . kotlin/String#
        return "squawk"
    }
}
