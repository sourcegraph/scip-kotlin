import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.nio.file.Files
import java.nio.file.Paths
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

tasks.withType<KotlinCompile> {
    dependsOn(":${projects.semanticdbKotlinc.name}:shadowJar")
    outputs.cacheIf { false }
    val pluginJar = semanticdbJar.incoming.artifacts.artifactFiles.first().path
    val targetroot = File(rootProject.buildDir, "semanticdb-targetroot")
    kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs = freeCompilerArgs + listOf(
            "-Xplugin=$pluginJar",
            "-P",
            "plugin:com.sourcegraph.lsif-kotlin:sourceroot=${projectDir.path}",
            "-P",
            "plugin:com.sourcegraph.lsif-kotlin:targetroot=${targetroot}"
        )
    }
}
