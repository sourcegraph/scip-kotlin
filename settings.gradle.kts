rootProject.name = "lsif-kotlin"

include(
    "semanticdb-kotlin",
    "semanticdb-kotlinc",
    "semanticdb-kotlinc:minimized"
)

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
