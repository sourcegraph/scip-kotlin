package snapshots;

import kotlin.collections.CollectionsKt;
//     ^^^^^^ reference kotlin/
//            ^^^^^^^^^^^ reference kotlin/collections/
//                        ^^^^^^^^^^^^^ reference kotlin/collections/CollectionsKt#
import kotlin.text.StringsKt;
//     ^^^^^^ reference kotlin/
//            ^^^^ reference kotlin/text/
//                 ^^^^^^^^^ reference kotlin/text/StringsKt#


public class KotlinLambdas {
//           ^^^^^^^^^^^^^ definition snapshots/KotlinLambdas# public class KotlinLambdas
//           ^^^^^^^^^^^^^ definition snapshots/KotlinLambdas#`<init>`(). public KotlinLambdas()
    public void test() {
//              ^^^^ definition snapshots/KotlinLambdas#test(). public void test()
        LambdasKt.getX();// TODO figure out emit getX on kotlin side
//      ^^^^^^^^^ reference snapshots/LambdasKt#
//                ^^^^ reference snapshots/LambdasKt#getX().
    }
}
