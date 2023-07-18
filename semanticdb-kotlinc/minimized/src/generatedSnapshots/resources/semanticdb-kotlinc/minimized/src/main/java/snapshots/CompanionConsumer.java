package snapshots;

public class CompanionConsumer {
//           ^^^^^^^^^^^^^^^^^ definition semanticdb maven . . snapshots/CompanionConsumer#
//                             documentation ```java\npublic class CompanionConsumer\n```
    CompanionConsumer() {
//  ^^^^^^^^^^^^^^^^^ definition semanticdb maven . . snapshots/CompanionConsumer#`<init>`().
//                    documentation ```java\nCompanionConsumer()\n```
        CompanionOwner.Companion.create();
//      ^^^^^^^^^^^^^^ reference semanticdb maven . . snapshots/CompanionOwner#
//                     ^^^^^^^^^ reference semanticdb maven . . snapshots/CompanionOwner#Companion.
//                               ^^^^^^ reference semanticdb maven . . snapshots/CompanionOwner#Companion#create().
        new CompanionOwner().create();
//          ^^^^^^^^^^^^^^ reference semanticdb maven . . snapshots/CompanionOwner#`<init>`().
//                           ^^^^^^ reference semanticdb maven . . snapshots/CompanionOwner#create().
    }
}
