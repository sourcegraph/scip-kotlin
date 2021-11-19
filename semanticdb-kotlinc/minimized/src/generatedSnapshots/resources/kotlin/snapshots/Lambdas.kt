package snapshots
//      ^^^^^^^^^ reference snapshots/

val x = arrayListOf<String>().forEachIndexed { i, s -> println("$i $s") }
//  ^ definition snapshots/LambdasKt#x. val x: kotlin.Unit
//  ^ definition snapshots/LambdasKt#getX(). val x: kotlin.Unit
//      ^^^^^^^^^^^ reference kotlin/collections/CollectionsKt#arrayListOf().
//                  ^^^^^^ reference kotlin/String#
//                            ^^^^^^^^^^^^^^ reference kotlin/collections/CollectionsKt#forEachIndexed(+9).
//                                             ^ definition local0 value-parameter i: kotlin.Int
//                                                ^ definition local1 value-parameter s: kotlin.String
//                                                     ^^^^^^^ reference kotlin/io/ConsoleKt#println(+1).
//                                                               ^ reference local0
//                                                                  ^ reference local1

val y = "fdsa".run { this.toByteArray() }
//  ^ definition snapshots/LambdasKt#y. val y: kotlin.ByteArray
//  ^ definition snapshots/LambdasKt#getY(). val y: kotlin.ByteArray
//             ^^^ reference kotlin/StandardKt#run(+1).
//                   ^^^^ reference
//                        ^^^^^^^^^^^ reference kotlin/text/StringsKt#toByteArray().

val z = y.let { it.size }
//  ^ definition snapshots/LambdasKt#z. val z: kotlin.Int
//  ^ definition snapshots/LambdasKt#getZ(). val z: kotlin.Int
//      ^ reference snapshots/LambdasKt#y.
//      ^ reference snapshots/LambdasKt#getY().
//        ^^^ reference kotlin/StandardKt#let().
//              ^^ reference local2
//                 ^^^^ reference kotlin/ByteArray#size.
//                 ^^^^ reference kotlin/ByteArray#getSize().
