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
    kotlinOptions.jvmTarget = "1.8"
    dependsOn(":${projects.semanticdbKotlinc.name}:shadowJar")
    val targetroot = File(project.buildDir, "semanticdb-targetroot")
    kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs = freeCompilerArgs + listOf(
            "-Xplugin=${semanticdbJar.first()}",
            "-P",
            "plugin:semanticdb-kotlinc:sourceroot=${projectDir.path}",
            "-P",
            "plugin:semanticdb-kotlinc:targetroot=${targetroot}"
        )
    }
}
