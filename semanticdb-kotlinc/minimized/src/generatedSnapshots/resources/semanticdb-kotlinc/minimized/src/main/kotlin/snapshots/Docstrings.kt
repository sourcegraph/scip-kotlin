package snapshots
//      ^^^^^^^^^ reference semanticdb maven . . snapshots/

import java.io.Serializable
//     ^^^^ reference semanticdb maven . . java/
//          ^^ reference semanticdb maven . . java/io/
//             ^^^^^^^^^^^^ reference semanticdb maven jdk 8 java/io/Serializable#

abstract class DocstringSuperclass
//             ^^^^^^^^^^^^^^^^^^^ definition semanticdb maven . . snapshots/DocstringSuperclass#
//                                 documentation ```kotlin\npublic abstract class DocstringSuperclass\n```
//             ^^^^^^^^^^^^^^^^^^^ definition semanticdb maven . . snapshots/DocstringSuperclass#`<init>`().
//                                 documentation ```kotlin\npublic constructor DocstringSuperclass()\n```
/** Example class docstring. */
class Docstrings :  DocstringSuperclass(), Serializable {
//    ^^^^^^^^^^ definition semanticdb maven . . snapshots/Docstrings#
//               documentation ```kotlin\npublic final class Docstrings : snapshots.DocstringSuperclass, java.io.Serializable\n```\n\n----\n\n Example class docstring.
//               relationship is_reference is_implementation semanticdb maven . . snapshots/DocstringSuperclass#
//    ^^^^^^^^^^ definition semanticdb maven . . snapshots/Docstrings#`<init>`().
//               documentation ```kotlin\npublic constructor Docstrings()\n```\n\n----\n\n Example class docstring.
//                  ^^^^^^^^^^^^^^^^^^^ reference semanticdb maven . . snapshots/DocstringSuperclass#`<init>`().
//                                         ^^^^^^^^^^^^ reference semanticdb maven jdk 8 java/io/Serializable#
}

/** Example method docstring. */
fun docstrings() { }
//  ^^^^^^^^^^ definition semanticdb maven . . snapshots/DocstringsKt#docstrings().
//             documentation ```kotlin\npublic fun docstrings()\n```\n\n----\n\n Example method docstring.
