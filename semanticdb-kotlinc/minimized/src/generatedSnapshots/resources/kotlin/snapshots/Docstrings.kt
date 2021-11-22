package snapshots
//      ^^^^^^^^^ reference snapshots/

import java.io.Serializable
//     ^^^^ reference java/
//          ^^ reference java/io/
//             ^^^^^^^^^^^^ reference java/io/Serializable#

abstract class DocstringSuperclass
//             ^^^^^^^^^^^^^^^^^^^ definition snapshots/DocstringSuperclass# public abstract class DocstringSuperclass
//             ^^^^^^^^^^^^^^^^^^^ definition snapshots/DocstringSuperclass#`<init>`(). public constructor DocstringSuperclass()
/** Example class docstring. */
class Docstrings :  DocstringSuperclass(), Serializable {
//    ^^^^^^^^^^ definition snapshots/Docstrings# public final class Docstrings : snapshots.DocstringSuperclass, java.io.Serializable
//    ^^^^^^^^^^ definition snapshots/Docstrings#`<init>`(). public constructor Docstrings()
//                  ^^^^^^^^^^^^^^^^^^^ reference snapshots/DocstringSuperclass#`<init>`().
//                                         ^^^^^^^^^^^^ reference java/io/Serializable#
}

/** Example method docstring. */
fun docstrings() { }
//  ^^^^^^^^^^ definition snapshots/DocstringsKt#docstrings(). public fun docstrings()
