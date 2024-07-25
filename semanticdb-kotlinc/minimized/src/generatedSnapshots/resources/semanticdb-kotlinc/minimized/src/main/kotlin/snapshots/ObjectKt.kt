package snapshots
//      ^^^^^^^^^ reference semanticdb maven . . snapshots/

import java.lang.RuntimeException
//     ^^^^ reference semanticdb maven . . java/
//          ^^^^ reference semanticdb maven . . java/lang/
//               ^^^^^^^^^^^^^^^^ reference semanticdb maven jdk 8 java/lang/RuntimeException#

object ObjectKt {
//     ^^^^^^^^ definition semanticdb maven . . snapshots/ObjectKt#
//              documentation ```kotlin\npublic object ObjectKt\n```
  fun fail(message: String?): Nothing {
//    ^^^^ definition semanticdb maven . . snapshots/ObjectKt#fail().
//         documentation ```kotlin\npublic final fun fail(message: kotlin.String?): kotlin.Nothing\n```
//         ^^^^^^^ definition semanticdb maven . . snapshots/ObjectKt#fail().(message)
//                 documentation ```kotlin\nvalue-parameter message: kotlin.String?\n```
//                  ^^^^^^ reference semanticdb maven . . kotlin/String#
//                            ^^^^^^^ reference semanticdb maven . . kotlin/Nothing#
    throw RuntimeException(message)
//        ^^^^^^^^^^^^^^^^ reference semanticdb maven jdk 8 java/lang/RuntimeException#`<init>`(+1).
//                         ^^^^^^^ reference semanticdb maven . . snapshots/ObjectKt#fail().(message)
  }
}
