import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "1.5.0"
    id("com.github.johnrengelman.shadow") version "6.1.0"
}

group = "com.sourcegraph"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

allprojects {
    afterEvaluate {
        tasks.withType<KotlinCompile> {
                kotlinOptions.jvmTarget = "1.8"
        }
            
        tasks.withType<JavaCompile> {
            sourceCompatibility = "1.8"
        }
    }
}