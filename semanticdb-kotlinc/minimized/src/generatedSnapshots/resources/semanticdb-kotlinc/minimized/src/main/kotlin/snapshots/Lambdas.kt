  package snapshots
//        ^^^^^^^^^ reference semanticdb maven . . snapshots/
  
  val x = arrayListOf<String>().forEachIndexed { i, s -> println("$i $s") }
//    ^ definition semanticdb maven . . snapshots/getX().
//      display_name x
//      documentation ```kotlin\npublic get(): Unit\n```
//    ^ definition semanticdb maven . . snapshots/x.
//      display_name x
//      documentation ```kotlin\npublic final val x: Unit\n```
//        ^^^^^^^^^^^ reference semanticdb maven . . kotlin/collections/arrayListOf().
//                              ^^^^^^^^^^^^^^ reference semanticdb maven . . kotlin/collections/forEachIndexed(+9).
//                                               ^ definition local 0
//                                                 display_name i
//                                                 documentation ```kotlin\ni: Int\n```
//                                                  ^ definition local 1
//                                                    display_name s
//                                                    documentation ```kotlin\ns: String\n```
//                                                       ^^^^^^^ reference semanticdb maven . . kotlin/io/println().
//                                                                 ^ reference local 0
//                                                                    ^ reference local 1
  
  val y = "fdsa".run { this.toByteArray() }
//    ^ definition semanticdb maven . . snapshots/getY().
//      display_name y
//      documentation ```kotlin\npublic get(): ByteArray\n```
//    ^ definition semanticdb maven . . snapshots/y.
//      display_name y
//      documentation ```kotlin\npublic final val y: ByteArray\n```
//               ^^^ reference semanticdb maven . . kotlin/run(+1).
//                          ^^^^^^^^^^^ reference semanticdb maven . . kotlin/text/toByteArray().
  
  val z = y.let { it.size }
//    ^ definition semanticdb maven . . snapshots/getZ().
//      display_name z
//      documentation ```kotlin\npublic get(): Int\n```
//    ^ definition semanticdb maven . . snapshots/z.
//      display_name z
//      documentation ```kotlin\npublic final val z: Int\n```
//        ^ reference semanticdb maven . . snapshots/getY().
//        ^ reference semanticdb maven . . snapshots/y.
//          ^^^ reference semanticdb maven . . kotlin/let().
//              ^^^^^^^^^^^ definition local 2
//                          display_name it
//                          documentation ```kotlin\nit: ByteArray\n```
//                ^^ reference local 2
//                   ^^^^ reference semanticdb maven . . kotlin/ByteArray#getSize().
//                   ^^^^ reference semanticdb maven . . kotlin/ByteArray#size.
