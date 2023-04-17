rootProject.name = "lsif-kotlin"

include(
    "semanticdb-kotlin",
    "semanticdb-kotlinc",
    "semanticdb-kotlinc:minimized",
    "debug-project"
)

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
