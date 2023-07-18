package snapshots
//      ^^^^^^^^^ reference semanticdb maven . . snapshots/

val x = arrayListOf<String>().forEachIndexed { i, s -> println("$i $s") }
//  ^ definition semanticdb maven . . snapshots/LambdasKt#getX().
//    documentation ```kt\npublic val x: kotlin.Unit\n```
//  ^ definition semanticdb maven . . snapshots/LambdasKt#x.
//    documentation ```kt\npublic val x: kotlin.Unit\n```
//      ^^^^^^^^^^^ reference semanticdb maven . . kotlin/collections/CollectionsKt#arrayListOf().
//                  ^^^^^^ reference semanticdb maven . . kotlin/String#
//                            ^^^^^^^^^^^^^^ reference semanticdb maven . . kotlin/collections/CollectionsKt#forEachIndexed(+9).
//                                             ^ definition local 0
//                                               documentation ```kt\nvalue-parameter i: kotlin.Int\n```
//                                                ^ definition local 1
//                                                  documentation ```kt\nvalue-parameter s: kotlin.String\n```
//                                                     ^^^^^^^ reference semanticdb maven . . kotlin/io/ConsoleKt#println(+1).
//                                                               ^ reference local 0
//                                                                  ^ reference local 1

val y = "fdsa".run { this.toByteArray() }
//  ^ definition semanticdb maven . . snapshots/LambdasKt#getY().
//    documentation ```kt\npublic val y: kotlin.ByteArray\n```
//  ^ definition semanticdb maven . . snapshots/LambdasKt#y.
//    documentation ```kt\npublic val y: kotlin.ByteArray\n```
//             ^^^ reference semanticdb maven . . kotlin/StandardKt#run(+1).
//                        ^^^^^^^^^^^ reference semanticdb maven . . kotlin/text/StringsKt#toByteArray().

val z = y.let { it.size }
//  ^ definition semanticdb maven . . snapshots/LambdasKt#getZ().
//    documentation ```kt\npublic val z: kotlin.Int\n```
//  ^ definition semanticdb maven . . snapshots/LambdasKt#z.
//    documentation ```kt\npublic val z: kotlin.Int\n```
//      ^ reference semanticdb maven . . snapshots/LambdasKt#getY().
//      ^ reference semanticdb maven . . snapshots/LambdasKt#y.
//        ^^^ reference semanticdb maven . . kotlin/StandardKt#let().
//              ^^ reference local 2
//                 ^^^^ reference semanticdb maven . . kotlin/ByteArray#getSize().
//                 ^^^^ reference semanticdb maven . . kotlin/ByteArray#size.
