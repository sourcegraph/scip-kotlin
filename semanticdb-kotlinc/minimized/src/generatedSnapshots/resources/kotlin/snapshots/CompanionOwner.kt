package snapshots
//      ^^^^^^^^^ reference snapshots/

class CompanionOwner {
//    ^^^^^^^^^^^^^^ definition snapshots/CompanionOwner# CompanionOwner
//    ^^^^^^^^^^^^^^ definition snapshots/CompanionOwner#`<init>`(). CompanionOwner
  companion object {
//          ^^^^^^^^^ definition snapshots/CompanionOwner#Companion# Companion
    fun create(): CompanionOwner = CompanionOwner()
//      ^^^^^^ definition snapshots/CompanionOwner#Companion#create(). create
//                ^^^^^^^^^^^^^^ reference snapshots/CompanionOwner#
//                                 ^^^^^^^^^^^^^^ reference snapshots/CompanionOwner#`<init>`().
  }
  fun create(): Int = CompanionOwner.create().hashCode()
//    ^^^^^^ definition snapshots/CompanionOwner#create(). create
//              ^^^ reference kotlin/Int#
//                    ^^^^^^^^^^^^^^ reference snapshots/CompanionOwner#Companion#
//                                   ^^^^^^ reference snapshots/CompanionOwner#Companion#create().
//                                            ^^^^^^^^ reference snapshots/CompanionOwner#hashCode(+-1).
}
