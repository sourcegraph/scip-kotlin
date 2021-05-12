
rootProject.name = "lsif-kotlin"

include(
    "semanticdb-kotlin",
    "semanticdb-kotlinc",
    "semanticdb-kotlinc:minimized",
    "debug-project",
    "gradle-plugin"
)

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")