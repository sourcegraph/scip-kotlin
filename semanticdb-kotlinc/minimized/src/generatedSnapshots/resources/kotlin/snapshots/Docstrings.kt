package snapshots
//      ^^^^^^^^^ reference snapshots/

import java.io.Serializable
//     ^^^^ reference java/
//          ^^ reference java/io/
//             ^^^^^^^^^^^^ reference java/io/Serializable#

abstract class DocstringSuperclass
//             ^^^^^^^^^^^^^^^^^^^ definition snapshots/DocstringSuperclass# class DocstringSuperclass
//             ^^^^^^^^^^^^^^^^^^^ definition snapshots/DocstringSuperclass#`<init>`(). constructor DocstringSuperclass()
/** Example class docstring. */
class Docstrings :  DocstringSuperclass(), Serializable {
//    ^^^^^^^^^^ definition snapshots/Docstrings# class Docstrings : snapshots.DocstringSuperclass, java.io.Serializable
//    ^^^^^^^^^^ definition snapshots/Docstrings#`<init>`(). constructor Docstrings()
//                  ^^^^^^^^^^^^^^^^^^^ reference snapshots/DocstringSuperclass#`<init>`().
//                                         ^^^^^^^^^^^^ reference java/io/Serializable#
}

/** Example method docstring. */
fun docstrings() { }
//  ^^^^^^^^^^ definition snapshots/DocstringsKt#docstrings(). fun docstrings(): kotlin.Unit
