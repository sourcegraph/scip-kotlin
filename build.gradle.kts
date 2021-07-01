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

// allprojects {
//     tasks.withType<KotlinCompile> {
//         val targetroot = File(this@allprojects.buildDir, "semanticdb-targetroot")
//         kotlinOptions {
//             freeCompilerArgs = freeCompilerArgs + listOf(
//                 "-Xplugin=/home/noah/Sourcegraph/lsif-kotlin/semanticdb-kotlinc-1.0-SNAPSHOT-all.jar",
//                 "-P",
//                 "plugin:com.sourcegraph.lsif-kotlin:sourceroot=${rootDir.path}",
//                 "-P",
//                 "plugin:com.sourcegraph.lsif-kotlin:targetroot=${targetroot}"
//             )
//         }
//     }
// }