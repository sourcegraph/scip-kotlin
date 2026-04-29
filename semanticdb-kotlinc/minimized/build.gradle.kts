import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    compileOnly("com.sourcegraph", "semanticdb-javac", "0.8.23")
}

val semanticdbJar: Configuration by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

dependencies {
    semanticdbJar(project(mapOf(
        "path" to ":semanticdb-kotlinc",
        "configuration" to "semanticdbJar"
    )))
}

val sourceroot = rootDir.path
val targetroot = project.buildDir.resolve("semanticdb-targetroot")

tasks.withType<KotlinCompile> {
    dependsOn(":semanticdb-kotlinc:shadowJar")
    outputs.upToDateWhen { false }
}

kotlin {
    jvmToolchain(8)
    val pluginJar = semanticdbJar.incoming.artifacts.artifactFiles.first().path
    compilerOptions {
        jvmTarget = JvmTarget.JVM_1_8
        freeCompilerArgs.addAll(
            "-Xplugin=$pluginJar",
            "-P",
            "plugin:semanticdb-kotlinc:sourceroot=${sourceroot}",
            "-P",
            "plugin:semanticdb-kotlinc:targetroot=${targetroot}",
        )
    }
}

tasks.withType<JavaCompile> {
    dependsOn(":semanticdb-kotlinc:shadowJar")
    outputs.upToDateWhen { false }
    sourceCompatibility = "1.8"
    options.compilerArgs = options.compilerArgs + listOf(
        "-Xplugin:semanticdb -sourceroot:$sourceroot -targetroot:$targetroot"
    )
}

// create a sourceset in which to output the generated snapshots.
// we may choose to not use sourcesets down the line
val generatedSnapshots: SourceSet by sourceSets.creating {
    resources.srcDir("generatedSnapshots")
}

// JavaExec task that invokes the snapshot creating main class
task<JavaExec>("snapshots") {
    javaLauncher.set(javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(8))
    })
    dependsOn(
        project.tasks.compileKotlin.get().path,
        project.tasks.compileJava.get().path
    )
    outputs.upToDateWhen { false }
    mainClass.set("com.sourcegraph.scip_kotlin.SnapshotKt")
    // this is required as the main class SnapshotKt is in this classpath
    classpath = project(":semanticdb-kotlinc")
        .the<SourceSetContainer>()["snapshots"].runtimeClasspath
    args = listOf(
        kotlin.sourceSets.main.get().kotlin.srcDirs.first().canonicalPath,
        sourceSets.main.get().java.srcDirs.first().canonicalPath
    )
    systemProperties = mapOf(
        "sourceroot" to sourceroot,
        "targetroot" to project.buildDir.resolve("semanticdb-targetroot"),
        "snapshotDir" to generatedSnapshots.resources.srcDirs.first())
}
