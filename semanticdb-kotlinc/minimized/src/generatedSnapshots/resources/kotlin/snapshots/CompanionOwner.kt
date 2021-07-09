package snapshots
//      ^^^^^^^^^ reference snapshots/

class CompanionOwner {
//    ^^^^^^^^^^^^^^ definition snapshots/CompanionOwner# CompanionOwner
//    ^^^^^^^^^^^^^^ definition snapshots/CompanionOwner#`<init>`(). CompanionOwner
    companion object {
        fun create(): CompanionOwner = CompanionOwner()
//          ^^^^^^ definition snapshots/CompanionOwner#Companion#create(). create
//                    ^^^^^^^^^^^^^^ reference snapshots/CompanionOwner#
//                                     ^^^^^^^^^^^^^^ reference snapshots/CompanionOwner#`<init>`().
    }
}