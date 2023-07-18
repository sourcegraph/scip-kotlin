package snapshots;

public class KotlinClass {
//           ^^^^^^^^^^^ definition semanticdb maven . . snapshots/KotlinClass#
//                       documentation ```java\npublic class KotlinClass\n```
    KotlinClass() throws Class {
//  ^^^^^^^^^^^ definition semanticdb maven . . snapshots/KotlinClass#`<init>`().
//              documentation ```java\nKotlinClass() throws Class\n```
//                       ^^^^^ reference semanticdb maven . . snapshots/Class#
        throw new Class();
//                ^^^^^ reference semanticdb maven . . snapshots/Class#`<init>`(+1).
    }

    void test() throws Class {
//       ^^^^ definition semanticdb maven . . snapshots/KotlinClass#test().
//            documentation ```java\nvoid test() throws Class\n```
//                     ^^^^^ reference semanticdb maven . . snapshots/Class#
        throw new Class(1, "");
//                ^^^^^ reference semanticdb maven . . snapshots/Class#`<init>`().
    }

    void other() throws Class {
//       ^^^^^ definition semanticdb maven . . snapshots/KotlinClass#other().
//             documentation ```java\nvoid other() throws Class\n```
//                      ^^^^^ reference semanticdb maven . . snapshots/Class#
        throw new Class(1);
//                ^^^^^ reference semanticdb maven . . snapshots/Class#`<init>`(+2).
    }
}
