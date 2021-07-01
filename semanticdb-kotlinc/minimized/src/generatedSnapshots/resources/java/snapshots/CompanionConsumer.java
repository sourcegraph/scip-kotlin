package snapshots;

public class CompanionConsumer {
//           ^^^^^^^^^^^^^^^^^ definition snapshots/CompanionConsumer# public class CompanionConsumer
    CompanionConsumer() {
//  ^^^^^^^^^^^^^^^^^ definition snapshots/CompanionConsumer#`<init>`(). CompanionConsumer()
        CompanionOwner.Companion.create();
//      ^^^^^^^^^^^^^^ reference snapshots/CompanionOwner#
//                     ^^^^^^^^^ reference snapshots/CompanionOwner#Companion.
//                               ^^^^^^ reference snapshots/CompanionOwner#Companion#create().
    }
}
