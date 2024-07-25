package snapshots
//      ^^^^^^^^^ reference semanticdb maven . . snapshots/

fun sampleText(x: String = "") {
//  ^^^^^^^^^^ definition semanticdb maven . . snapshots/FunctionsKt#sampleText().
//             documentation ```kotlin\npublic fun sampleText(x: kotlin.String = ...)\n```
//             ^ definition semanticdb maven . . snapshots/FunctionsKt#sampleText().(x)
//               documentation ```kotlin\nvalue-parameter x: kotlin.String = ...\n```
//                ^^^^^^ reference semanticdb maven . . kotlin/String#
  println(x)
//^^^^^^^ reference semanticdb maven . . kotlin/io/ConsoleKt#println(+1).
//        ^ reference semanticdb maven . . snapshots/FunctionsKt#sampleText().(x)
}
