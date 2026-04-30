  package snapshots;
//        ^^^^^^^^^ reference semanticdb maven . . snapshots/
  
  public class KotlinClass {
//             ^^^^^^^^^^^ definition semanticdb maven . . snapshots/KotlinClass#
//                         display_name KotlinClass
//                         signature_documentation java public class KotlinClass
//                         kind Class
      KotlinClass() throws Class {
//    ^^^^^^^^^^^ definition semanticdb maven . . snapshots/KotlinClass#`<init>`().
//                display_name <init>
//                signature_documentation java KotlinClass() throws Class
//                kind Constructor
//                         ^^^^^ reference semanticdb maven . . snapshots/Class#
          throw new Class();
//                  ^^^^^ reference semanticdb maven . . snapshots/Class#`<init>`(+1).
      }
  
      void test() throws Class {
//         ^^^^ definition semanticdb maven . . snapshots/KotlinClass#test().
//              display_name test
//              signature_documentation java void test() throws Class
//              kind Method
//                       ^^^^^ reference semanticdb maven . . snapshots/Class#
          throw new Class(1, "");
//                  ^^^^^ reference semanticdb maven . . snapshots/Class#`<init>`().
      }
  
      void other() throws Class {
//         ^^^^^ definition semanticdb maven . . snapshots/KotlinClass#other().
//               display_name other
//               signature_documentation java void other() throws Class
//               kind Method
//                        ^^^^^ reference semanticdb maven . . snapshots/Class#
          throw new Class(1);
//                  ^^^^^ reference semanticdb maven . . snapshots/Class#`<init>`(+2).
      }
  }
