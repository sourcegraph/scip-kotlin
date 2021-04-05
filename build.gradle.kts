import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "1.4.32"
    id("com.github.johnrengelman.shadow") version "6.1.0"
}

group = "com.sourcegraph"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    compileOnly(kotlin("compiler"))

    testImplementation(kotlin("test-junit5"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.6.0")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile>() {
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

tasks.named<ShadowJar>("shadowJar").configure {
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
