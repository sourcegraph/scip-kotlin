package snapshots
//      ^^^^^^^^^ reference snapshots/

import java.io.Serializable
//     ^^^^ reference java/
//          ^^ reference java/io/
//             ^^^^^^^^^^^^ reference java/io/Serializable#

abstract class DocstringSuperclass
//             ^^^^^^^^^^^^^^^^^^^ definition snapshots/DocstringSuperclass# DocstringSuperclass
//             ^^^^^^^^^^^^^^^^^^^ definition snapshots/DocstringSuperclass#`<init>`(). DocstringSuperclass
/** Example class docstring. */
class Docstrings :  DocstringSuperclass(), Serializable {
//    ^^^^^^^^^^ definition snapshots/Docstrings# Docstrings
//    ^^^^^^^^^^ definition snapshots/Docstrings#`<init>`(). Docstrings
//                  ^^^^^^^^^^^^^^^^^^^ reference snapshots/DocstringSuperclass#`<init>`().
//                                         ^^^^^^^^^^^^ reference java/io/Serializable#
}

/** Example method docstring. */
fun docstrings() { }
//  ^^^^^^^^^^ definition snapshots/DocstringsKt#docstrings(). docstrings
