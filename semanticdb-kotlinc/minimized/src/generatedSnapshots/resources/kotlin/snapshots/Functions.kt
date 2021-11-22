package snapshots
//      ^^^^^^^^^ reference snapshots/

fun sampleText(x: String = "") {
//  ^^^^^^^^^^ definition snapshots/FunctionsKt#sampleText(). public fun sampleText(x: kotlin.String = ...)
//             ^ definition snapshots/FunctionsKt#sampleText().(x) value-parameter x: kotlin.String = ...
//                ^^^^^^ reference kotlin/String#
  println(x)
//^^^^^^^ reference kotlin/io/ConsoleKt#println(+1).
//        ^ reference snapshots/FunctionsKt#sampleText().(x)
}
