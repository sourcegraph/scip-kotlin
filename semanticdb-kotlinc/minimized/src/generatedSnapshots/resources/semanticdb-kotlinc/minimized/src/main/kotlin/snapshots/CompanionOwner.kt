package snapshots
//      ^^^^^^^^^ reference semanticdb maven . . snapshots/

class CompanionOwner {
//    ^^^^^^^^^^^^^^ definition semanticdb maven . . snapshots/CompanionOwner#
//                   documentation ```kotlin\npublic final class CompanionOwner : Any\n```
//    ^^^^^^^^^^^^^^ definition semanticdb maven . . snapshots/CompanionOwner#`<init>`().
//                   documentation ```kotlin\npublic constructor(): CompanionOwner\n```
  companion object {
//^^^^^^^^^^^^^^^^^^ definition semanticdb maven . . snapshots/CompanionOwner#Companion# 2:3
//                   documentation ```kotlin\npublic final companion object Companion : Any\n```
//^^^^^^^^^^^^^^^^^^ definition semanticdb maven . . snapshots/CompanionOwner#Companion#`<init>`(). 2:3
//                   documentation ```kotlin\nprivate constructor(): CompanionOwner.Companion\n```
    fun create(): CompanionOwner = CompanionOwner()
//      ^^^^^^ definition semanticdb maven . . snapshots/CompanionOwner#Companion#create().
//             documentation ```kotlin\npublic final fun create(): CompanionOwner\n```
//                ^^^^^^^^^^^^^^ reference semanticdb maven . . snapshots/CompanionOwner#
//                                 ^^^^^^^^^^^^^^ reference semanticdb maven . . snapshots/CompanionOwner#`<init>`().
  }
  fun create(): Int = CompanionOwner.create().hashCode()
//    ^^^^^^ definition semanticdb maven . . snapshots/CompanionOwner#create().
//           documentation ```kotlin\npublic final fun create(): Int\n```
//              ^^^ reference semanticdb maven . . kotlin/Int#
//                                   ^^^^^^ reference semanticdb maven . . snapshots/CompanionOwner#Companion#create().
//                                            ^^^^^^^^ reference semanticdb maven . . kotlin/Any#hashCode().
}
