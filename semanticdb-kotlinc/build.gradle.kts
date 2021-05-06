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
    implementation(projects.semanticdbKotlin)
}

tasks.withType<KotlinCompile> {
    dependsOn(":${projects.semanticdbKotlin.name}:build")
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

val semanticdbJar: Configuration by configurations.creating {
    isCanBeConsumed = true
    isCanBeResolved = false
    outgoing.artifact(tasks.shadowJar.get().outputs.files.first())
}

artifacts {
    add("semanticdbJar", tasks.shadowJar.get().outputs.files.first()) {
       builtBy(tasks.shadowJar)
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

tasks.named<ShadowJar>("shadowJar").configure {
    configurations.add(project.configurations.compileOnly.get())
    dependencies {
        exclude("org.jetbrains.kotlin:kotlin-stdlib")
        exclude("org.jetbrains.kotlin:kotlin-compiler")
    }
    relocate("com.intellij", "org.jetbrains.kotlin.com.intellij")
    minimize()
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

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    freeCompilerArgs = listOf("-Xinline-classes")
}