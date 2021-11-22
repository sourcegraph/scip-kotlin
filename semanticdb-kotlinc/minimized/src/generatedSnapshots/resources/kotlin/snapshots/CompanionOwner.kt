package snapshots
//      ^^^^^^^^^ reference snapshots/

class CompanionOwner {
//    ^^^^^^^^^^^^^^ definition snapshots/CompanionOwner# public final class CompanionOwner
//    ^^^^^^^^^^^^^^ definition snapshots/CompanionOwner#`<init>`(). public constructor CompanionOwner()
  companion object {
//          ^^^^^^^^^ definition snapshots/CompanionOwner#Companion# public companion object
    fun create(): CompanionOwner = CompanionOwner()
//      ^^^^^^ definition snapshots/CompanionOwner#Companion#create(). public final fun create(): snapshots.CompanionOwner
//                ^^^^^^^^^^^^^^ reference snapshots/CompanionOwner#
//                                 ^^^^^^^^^^^^^^ reference snapshots/CompanionOwner#`<init>`().
  }
  fun create(): Int = CompanionOwner.create().hashCode()
//    ^^^^^^ definition snapshots/CompanionOwner#create(). public final fun create(): kotlin.Int
//              ^^^ reference kotlin/Int#
//                    ^^^^^^^^^^^^^^ reference snapshots/CompanionOwner#Companion#
//                                   ^^^^^^ reference snapshots/CompanionOwner#Companion#create().
//                                            ^^^^^^^^ reference snapshots/CompanionOwner#hashCode(+-1).
}
