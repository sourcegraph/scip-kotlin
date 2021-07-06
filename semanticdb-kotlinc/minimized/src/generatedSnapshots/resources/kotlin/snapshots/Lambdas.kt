package snapshots
//      ^^^^^^^^^ reference snapshots/

val x = arrayListOf<String>().forEachIndexed { i, s ->
//  ^ definition snapshots/LambdasKt#x. x
//  ^ definition snapshots/LambdasKt#getX(). x
//      ^^^^^^^^^^^ reference kotlin/collections/CollectionsKt#arrayListOf().
//                  ^^^^^^ reference kotlin/String#
//                            ^^^^^^^^^^^^^^ reference kotlin/collections/CollectionsKt#forEachIndexed(+9).
//                                             ^ definition local0 i
//                                                ^ definition local1 s
    println("$i $s")
//  ^^^^^^^ reference kotlin/io/ConsoleKt#println(+1).
//            ^ reference local2
//               ^ reference local3
}

val y = "fdsa".run {
//  ^ definition snapshots/LambdasKt#y. y
//  ^ definition snapshots/LambdasKt#getY(). y
//             ^^^ reference kotlin/StandardKt#run(+1).
    this.toByteArray()
//  ^^^^ reference 
//       ^^^^^^^^^^^ reference kotlin/text/StringsKt#toByteArray().
}

val z = y.let {
//  ^ definition snapshots/LambdasKt#z. z
//  ^ definition snapshots/LambdasKt#getZ(). z
//      ^ reference snapshots/LambdasKt#y.
//      ^ reference snapshots/LambdasKt#getY().
//        ^^^ reference kotlin/StandardKt#let().
    it.size
//  ^^ reference local4
//     ^^^^ reference kotlin/ByteArray#size.
//     ^^^^ reference kotlin/ByteArray#getSize().
}