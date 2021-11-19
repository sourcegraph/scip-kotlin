package snapshots
//      ^^^^^^^^^ reference snapshots/

class CompanionOwner {
//    ^^^^^^^^^^^^^^ definition snapshots/CompanionOwner# class CompanionOwner
//    ^^^^^^^^^^^^^^ definition snapshots/CompanionOwner#`<init>`(). constructor CompanionOwner()
  companion object {
//          ^^^^^^^^^ definition snapshots/CompanionOwner#Companion# companion object
    fun create(): CompanionOwner = CompanionOwner()
//      ^^^^^^ definition snapshots/CompanionOwner#Companion#create(). fun create(): snapshots.CompanionOwner
//                ^^^^^^^^^^^^^^ reference snapshots/CompanionOwner#
//                                 ^^^^^^^^^^^^^^ reference snapshots/CompanionOwner#`<init>`().
  }
  fun create(): Int = CompanionOwner.create().hashCode()
//    ^^^^^^ definition snapshots/CompanionOwner#create(). fun create(): kotlin.Int
//              ^^^ reference kotlin/Int#
//                    ^^^^^^^^^^^^^^ reference snapshots/CompanionOwner#Companion#
//                                   ^^^^^^ reference snapshots/CompanionOwner#Companion#create().
//                                            ^^^^^^^^ reference snapshots/CompanionOwner#hashCode(+-1).
}
