package snapshots;

public class ClassConsumer {
//           ^^^^^^^^^^^^^ definition semanticdb maven . . snapshots/ClassConsumer#
//                         documentation ```java\npublic class ClassConsumer\n```
//           ^^^^^^^^^^^^^ definition semanticdb maven . . snapshots/ClassConsumer#`<init>`().
//                         documentation ```java\npublic ClassConsumer()\n```
    public static void run() {
//                     ^^^ definition semanticdb maven . . snapshots/ClassConsumer#run().
//                         documentation ```java\npublic static void run()\n```
        System.out.println(new Class().getAsdf());
//      ^^^^^^ reference semanticdb maven jdk 8 java/lang/System#
//             ^^^ reference semanticdb maven jdk 8 java/lang/System#out.
//                 ^^^^^^^ reference semanticdb maven jdk 8 java/io/PrintStream#println(+9).
//                             ^^^^^ reference semanticdb maven . . snapshots/Class#`<init>`(+1).
//                                     ^^^^^^^ reference semanticdb maven . . snapshots/Class#getAsdf().
    }
}
