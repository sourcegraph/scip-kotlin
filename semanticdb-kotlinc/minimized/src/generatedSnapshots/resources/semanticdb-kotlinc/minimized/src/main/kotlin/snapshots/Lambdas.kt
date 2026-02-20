package snapshots
//      ^^^^^^^^^ reference semanticdb maven . . snapshots/

val x = arrayListOf<String>().forEachIndexed { i, s -> println("$i $s") }
//  ^ definition semanticdb maven . . snapshots/getX().
//    documentation ```kotlin\npublic get(): Unit\n```
//  ^ definition semanticdb maven . . snapshots/x.
//    documentation ```kotlin\npublic final val x: Unit\n```
//      ^^^^^^^^^^^ reference semanticdb maven . . kotlin/collections/arrayListOf().
//                            ^^^^^^^^^^^^^^ reference semanticdb maven . . kotlin/collections/forEachIndexed(+9).
//                                             ^ definition local 0
//                                               documentation ```kotlin\ni: Int\n```
//                                                ^ definition local 1
//                                                  documentation ```kotlin\ns: String\n```
//                                                     ^^^^^^^ reference semanticdb maven . . kotlin/io/println().
//                                                               ^ reference local 0
//                                                                  ^ reference local 1

val y = "fdsa".run { this.toByteArray() }
//  ^ definition semanticdb maven . . snapshots/getY().
//    documentation ```kotlin\npublic get(): ByteArray\n```
//  ^ definition semanticdb maven . . snapshots/y.
//    documentation ```kotlin\npublic final val y: ByteArray\n```
//             ^^^ reference semanticdb maven . . kotlin/run(+1).
//                        ^^^^^^^^^^^ reference semanticdb maven . . kotlin/text/toByteArray().

val z = y.let { it.size }
//  ^ definition semanticdb maven . . snapshots/getZ().
//    documentation ```kotlin\npublic get(): Int\n```
//  ^ definition semanticdb maven . . snapshots/z.
//    documentation ```kotlin\npublic final val z: Int\n```
//      ^ reference semanticdb maven . . snapshots/getY().
//      ^ reference semanticdb maven . . snapshots/y.
//        ^^^ reference semanticdb maven . . kotlin/let().
//            ^^^^^^^^^^^ definition local 2
//                        documentation ```kotlin\nit: ByteArray\n```
//              ^^ reference local 2
//                 ^^^^ reference semanticdb maven . . kotlin/ByteArray#getSize().
//                 ^^^^ reference semanticdb maven . . kotlin/ByteArray#size.
