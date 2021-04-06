import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm")
    id("com.github.johnrengelman.shadow")
    }

group = "com.sourcegraph"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    compileOnly(kotlin("compiler"))
    implementation("com.google.protobuf:protobuf-java:3.15.7")
    implementation(project(":semanticdb-kotlin"))
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions{
        jvmTarget = "1.8"
        //freeCompilerArgs = freeCompilerArgs + "-Xplugin=${project.rootDir}/build/libs/lsif-kotlin-1.0-SNAPSHOT-all.jar"
    }
}

tasks.jar {
    manifest {
        attributes["Specification-Title"] = project.name
        attributes["Specification-Version"] = project.version
        attributes["Implementation-Title"] = "com.sourcegraph.lsif-kotlin"
        attributes["Implementation-Version"] = project.version
    }
}

tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar").configure {
    configurations.add(project.configurations.compileOnly.get())
    dependencies {
        exclude("org.jetbrains.kotlin:kotlin-stdlib")
        exclude("org.jetbrains.kotlin:kotlin-compiler")
    }
    relocate("com.intellij", "org.jetbrains.kotlin.com.intellij")
}

/*tasks.shadowJar {
    configurations = listOf()
    archiveClassifier.set("embeddable")
    relocate("com.intellij", "org.jetbrains.kotlin.com.intellij")
}

// Create embeddable configuration
configurations.create("embeddable") {
    extendsFrom(configurations.shadow.get())
}*/
