  package snapshots;
  
  public class ClassConsumer {
//             ^^^^^^^^^^^^^ definition semanticdb maven . . snapshots/ClassConsumer#
//                           display_name ClassConsumer
//                           signature_documentation java public class ClassConsumer
//                           kind Class
//             ^^^^^^^^^^^^^ definition semanticdb maven . . snapshots/ClassConsumer#`<init>`().
//                           display_name <init>
//                           signature_documentation java public ClassConsumer()
//                           kind Constructor
      public static void run() {
//                       ^^^ definition semanticdb maven . . snapshots/ClassConsumer#run().
//                           display_name run
//                           signature_documentation java public static void run()
//                           kind StaticMethod
          System.out.println(new Class().getAsdf());
//        ^^^^^^ reference semanticdb maven jdk 8 java/lang/System#
//               ^^^ reference semanticdb maven jdk 8 java/lang/System#out.
//                   ^^^^^^^ reference semanticdb maven jdk 8 java/io/PrintStream#println(+9).
//                               ^^^^^ reference semanticdb maven . . snapshots/Class#`<init>`(+1).
//                                       ^^^^^^^ reference semanticdb maven . . snapshots/Class#getAsdf().
      }
  }
