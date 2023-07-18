package snapshots;

import kotlin.collections.CollectionsKt;
//     ^^^^^^ reference semanticdb maven . . kotlin/
//            ^^^^^^^^^^^ reference semanticdb maven . . kotlin/collections/
//                        ^^^^^^^^^^^^^ reference semanticdb maven . . kotlin/collections/CollectionsKt#
import kotlin.text.StringsKt;
//     ^^^^^^ reference semanticdb maven . . kotlin/
//            ^^^^ reference semanticdb maven . . kotlin/text/
//                 ^^^^^^^^^ reference semanticdb maven . . kotlin/text/StringsKt#


public class KotlinLambdas {
//           ^^^^^^^^^^^^^ definition semanticdb maven . . snapshots/KotlinLambdas#
//                         documentation ```java\npublic class KotlinLambdas\n```
//           ^^^^^^^^^^^^^ definition semanticdb maven . . snapshots/KotlinLambdas#`<init>`().
//                         documentation ```java\npublic KotlinLambdas()\n```
    public void test() {
//              ^^^^ definition semanticdb maven . . snapshots/KotlinLambdas#test().
//                   documentation ```java\npublic void test()\n```
        LambdasKt.getX();// TODO figure out emit getX on kotlin side
//      ^^^^^^^^^ reference semanticdb maven . . snapshots/LambdasKt#
//                ^^^^ reference semanticdb maven . . snapshots/LambdasKt#getX().

        kotlin.collections.CollectionsKt.listOf();
//      ^^^^^^ reference semanticdb maven . . kotlin/
//             ^^^^^^^^^^^ reference semanticdb maven . . kotlin/collections/
//                         ^^^^^^^^^^^^^ reference semanticdb maven . . kotlin/collections/CollectionsKt#
//                                       ^^^^^^ reference semanticdb maven . . kotlin/collections/CollectionsKt__CollectionsKt#listOf().
        FunctionsKt.sampleText("");
//      ^^^^^^^^^^^ reference semanticdb maven . . snapshots/FunctionsKt#
//                  ^^^^^^^^^^ reference semanticdb maven . . snapshots/FunctionsKt#sampleText().
    }
}
