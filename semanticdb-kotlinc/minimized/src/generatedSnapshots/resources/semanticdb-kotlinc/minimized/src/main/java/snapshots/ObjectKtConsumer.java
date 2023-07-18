package snapshots;

public class ObjectKtConsumer {
//           ^^^^^^^^^^^^^^^^ definition semanticdb maven . . snapshots/ObjectKtConsumer#
//                            documentation ```java\npublic class ObjectKtConsumer\n```
//           ^^^^^^^^^^^^^^^^ definition semanticdb maven . . snapshots/ObjectKtConsumer#`<init>`().
//                            documentation ```java\npublic ObjectKtConsumer()\n```
    public static void run() {
//                     ^^^ definition semanticdb maven . . snapshots/ObjectKtConsumer#run().
//                         documentation ```java\npublic static void run()\n```
        ObjectKt.INSTANCE.fail("boom");
//      ^^^^^^^^ reference semanticdb maven . . snapshots/ObjectKt#
//               ^^^^^^^^ reference semanticdb maven . . snapshots/ObjectKt#INSTANCE.
//                        ^^^^ reference semanticdb maven . . snapshots/ObjectKt#fail().
    }
}
