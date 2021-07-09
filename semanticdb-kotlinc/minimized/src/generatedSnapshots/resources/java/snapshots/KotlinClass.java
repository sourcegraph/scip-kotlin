package snapshots;

public class KotlinClass {
//           ^^^^^^^^^^^ definition snapshots/KotlinClass# public class KotlinClass
    KotlinClass() throws Class {
//  ^^^^^^^^^^^ definition snapshots/KotlinClass#`<init>`(). KotlinClass() throws Class
//                       ^^^^^ reference snapshots/Class#
        throw new Class();
//                ^^^^^ reference snapshots/Class#`<init>`(+1).
    }

    void test() throws Class {
//       ^^^^ definition snapshots/KotlinClass#test(). void test() throws Class
//                     ^^^^^ reference snapshots/Class#
        throw new Class(1, "");
//                ^^^^^ reference snapshots/Class#`<init>`().
    }

    void other() throws Class {
//       ^^^^^ definition snapshots/KotlinClass#other(). void other() throws Class
//                      ^^^^^ reference snapshots/Class#
        throw new Class(1);
//                ^^^^^ reference snapshots/Class#`<init>`(+2).
    }
}
