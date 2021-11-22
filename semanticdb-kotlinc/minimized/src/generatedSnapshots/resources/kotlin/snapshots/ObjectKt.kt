package snapshots
//      ^^^^^^^^^ reference snapshots/

import java.lang.RuntimeException
//     ^^^^ reference java/
//          ^^^^ reference java/lang/
//               ^^^^^^^^^^^^^^^^ reference java/lang/RuntimeException#

object ObjectKt {
//     ^^^^^^^^ definition snapshots/ObjectKt# public object ObjectKt
  fun fail(message: String?): Nothing {
//    ^^^^ definition snapshots/ObjectKt#fail(). public final fun fail(message: kotlin.String?): kotlin.Nothing
//         ^^^^^^^ definition snapshots/ObjectKt#fail().(message) value-parameter message: kotlin.String?
//                  ^^^^^^ reference kotlin/String#
//                            ^^^^^^^ reference kotlin/Nothing#
    throw RuntimeException(message)
//        ^^^^^^^^^^^^^^^^ reference java/lang/RuntimeException#`<init>`(+1).
//                         ^^^^^^^ reference snapshots/ObjectKt#fail().(message)
  }
}
