package snapshots;

public class ObjectKtConsumer {
//           ^^^^^^^^^^^^^^^^ definition snapshots/ObjectKtConsumer# public class ObjectKtConsumer
//           ^^^^^^^^^^^^^^^^ definition snapshots/ObjectKtConsumer#`<init>`(). public ObjectKtConsumer()
    public static void run() {
//                     ^^^ definition snapshots/ObjectKtConsumer#run(). public static void run()
        ObjectKt.INSTANCE.fail("boom");
//      ^^^^^^^^ reference snapshots/ObjectKt#
//               ^^^^^^^^ reference snapshots/ObjectKt#INSTANCE.
//                        ^^^^ reference snapshots/ObjectKt#fail().
    }
}
