package snapshots
//      ^^^^^^^^^ reference semanticdb maven . . snapshots/

import java.io.Serializable
//     ^^^^ reference semanticdb maven . . java/
//          ^^ reference semanticdb maven . . java/io/
//             ^^^^^^^^^^^^ reference semanticdb maven jdk 8 java/io/Serializable#

abstract class DocstringSuperclass
//             ^^^^^^^^^^^^^^^^^^^ definition semanticdb maven . . snapshots/DocstringSuperclass#
//                                 documentation ```kt\npublic abstract class DocstringSuperclass\n```
//             ^^^^^^^^^^^^^^^^^^^ definition semanticdb maven . . snapshots/DocstringSuperclass#`<init>`().
//                                 documentation ```kt\npublic constructor DocstringSuperclass()\n```
/** Example class docstring. */
class Docstrings :  DocstringSuperclass(), Serializable {
//    ^^^^^^^^^^ definition semanticdb maven . . snapshots/Docstrings#
//               documentation ```kt\npublic final class Docstrings : snapshots.DocstringSuperclass, java.io.Serializable\n```\n\n----\n\n Example class docstring.
//               relationship is_reference is_implementation semanticdb maven . . snapshots/DocstringSuperclass#
//    ^^^^^^^^^^ definition semanticdb maven . . snapshots/Docstrings#`<init>`().
//               documentation ```kt\npublic constructor Docstrings()\n```\n\n----\n\n Example class docstring.
//                  ^^^^^^^^^^^^^^^^^^^ reference semanticdb maven . . snapshots/DocstringSuperclass#`<init>`().
//                                         ^^^^^^^^^^^^ reference semanticdb maven jdk 8 java/io/Serializable#
}

/** Example method docstring. */
fun docstrings() { }
//  ^^^^^^^^^^ definition semanticdb maven . . snapshots/DocstringsKt#docstrings().
//             documentation ```kt\npublic fun docstrings()\n```\n\n----\n\n Example method docstring.
