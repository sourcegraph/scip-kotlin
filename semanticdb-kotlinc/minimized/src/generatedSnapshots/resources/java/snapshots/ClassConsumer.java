package snapshots;

public class ClassConsumer {
//           ^^^^^^^^^^^^^ definition snapshots/ClassConsumer# public class ClassConsumer
//           ^^^^^^^^^^^^^ definition snapshots/ClassConsumer#`<init>`(). public ClassConsumer()
    public static void run() {
//                     ^^^ definition snapshots/ClassConsumer#run(). public static void run()
        System.out.println(new Class().getAsdf());
//      ^^^^^^ reference java/lang/System#
//             ^^^ reference java/lang/System#out.
//                 ^^^^^^^ reference java/io/PrintStream#println(+9).
//                             ^^^^^ reference snapshots/Class#`<init>`(+1).
//                                     ^^^^^^^ reference snapshots/Class#getAsdf().
    }
}
