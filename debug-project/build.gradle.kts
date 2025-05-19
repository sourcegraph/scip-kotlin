import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
}

group = "org.example"

repositories {
    mavenCentral()
}

val semanticdbJar: Configuration by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

dependencies {
    implementation(kotlin("stdlib"))
    semanticdbJar(project(
        path = ":${projects.semanticdbKotlinc.name}",
        configuration = "semanticdbJar"
    ))
}

tasks.withType<KotlinCompile> {
    dependsOn(":${projects.semanticdbKotlinc.name}:shadowJar")
}

kotlin {
    val targetroot = File(project.buildDir, "semanticdb-targetroot")
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
        freeCompilerArgs.addAll(
            "-Xplugin=${semanticdbJar.first()}",
            "-P",
            "plugin:semanticdb-kotlinc:sourceroot=${projectDir.path}",
            "-P",
            "plugin:semanticdb-kotlinc:targetroot=${targetroot}"
        )
    }
}
