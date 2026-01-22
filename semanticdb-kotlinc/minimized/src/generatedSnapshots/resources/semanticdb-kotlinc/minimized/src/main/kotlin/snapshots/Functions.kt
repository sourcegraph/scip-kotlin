package snapshots
//      ^^^^^^^^^ reference semanticdb maven . . snapshots/

fun sampleText(x: String = "") {
//  ^^^^^^^^^^ definition semanticdb maven . . snapshots/sampleText().
//             documentation ```kotlin\npublic final fun sampleText(x: String = ...): Unit\n```
//             ^ definition semanticdb maven . . snapshots/sampleText().(x)
//               documentation ```kotlin\nx: String = ...\n```
//                ^^^^^^ reference semanticdb maven . . kotlin/String#
  println(x)
//^^^^^^^ reference semanticdb maven . . kotlin/io/println().
//        ^ reference semanticdb maven . . snapshots/sampleText().(x)
}
