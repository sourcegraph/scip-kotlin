package snapshots;

public class KotlinClass {
//           ^^^^^^^^^^^ definition snapshots/KotlinClass# public class KotlinClass
    KotlinClass() throws Class {
//  ^^^^^^^^^^^ definition snapshots/KotlinClass#`<init>`(). KotlinClass() throws Class
//                       ^^^^^ reference snapshots/Class#
       throw new Class();
//               ^^^^^ reference snapshots/Class#`<init>`(+1).
    }

    void test() throws Class {
//       ^^^^ definition snapshots/KotlinClass#test(). void test() throws Class
//                     ^^^^^ reference snapshots/Class#
        Class c = new Class(1, "");
//      ^^^^^ reference snapshots/Class#
//            ^ definition local0 Class c
//                    ^^^^^ reference snapshots/Class#`<init>`().
        System.out.println(c.getBanana());
//      ^^^^^^ reference java/lang/System#
//             ^^^ reference java/lang/System#out.
//                 ^^^^^^^ reference java/io/PrintStream#println(+3).
//                         ^ reference local0
//                           ^^^^^^^^^ reference snapshots/Class#getBanana().
        c.setBanana(5);
//      ^ reference local0
//        ^^^^^^^^^ reference snapshots/Class#setBanana().
        throw c;
//            ^ reference local0
    }

    void other() throws Class {
//       ^^^^^ definition snapshots/KotlinClass#other(). void other() throws Class
//                      ^^^^^ reference snapshots/Class#
       throw new Class(1);
//               ^^^^^ reference snapshots/Class#`<init>`(+2).
    }
}
