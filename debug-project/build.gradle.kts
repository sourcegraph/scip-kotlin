import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import kotlin.collections.mapOf

plugins {
    kotlin("jvm")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val semanticdbJar: Configuration by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

dependencies {
    implementation(kotlin("stdlib"))
    semanticdbJar(project(mapOf(
        "path" to ":" + projects.semanticdbKotlinc.name,
        "configuration" to "semanticdbJar"
    )))
}
