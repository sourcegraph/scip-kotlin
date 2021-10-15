package snapshots
//      ^^^^^^^^^ reference snapshots/

val x = arrayListOf<String>().forEachIndexed { i, s -> println("$i $s") }
//  ^ definition snapshots/LambdasKt#x. x
//  ^ definition snapshots/LambdasKt#getX(). x
//      ^^^^^^^^^^^ reference kotlin/collections/CollectionsKt#arrayListOf().
//                  ^^^^^^ reference kotlin/String#
//                            ^^^^^^^^^^^^^^ reference kotlin/collections/CollectionsKt#forEachIndexed(+9).
//                                             ^ definition local0 i
//                                                ^ definition local1 s
//                                                     ^^^^^^^ reference kotlin/io/ConsoleKt#println(+1).
//                                                               ^ reference local0
//                                                                  ^ reference local1

val y = "fdsa".run { this.toByteArray() }
//  ^ definition snapshots/LambdasKt#y. y
//  ^ definition snapshots/LambdasKt#getY(). y
//             ^^^ reference kotlin/StandardKt#run(+1).
//                   ^^^^ reference 
//                        ^^^^^^^^^^^ reference kotlin/text/StringsKt#toByteArray().

val z = y.let { it.size }
//  ^ definition snapshots/LambdasKt#z. z
//  ^ definition snapshots/LambdasKt#getZ(). z
//      ^ reference snapshots/LambdasKt#y.
//      ^ reference snapshots/LambdasKt#getY().
//        ^^^ reference kotlin/StandardKt#let().
//              ^^ reference local2
//                 ^^^^ reference kotlin/ByteArray#size.
//                 ^^^^ reference kotlin/ByteArray#getSize().
