package snapshots
//      ^^^^^^^^^ reference snapshots/

fun sampleText(x: String = "") {
//  ^^^^^^^^^^ definition snapshots/FunctionsKt#sampleText(). sampleText
//             ^ definition snapshots/FunctionsKt#sampleText().(x) x
//                ^^^^^^ reference kotlin/String#
    println(x)
//  ^^^^^^^ reference kotlin/io/ConsoleKt#println(+1).
//          ^ reference snapshots/FunctionsKt#sampleText().(x)
}