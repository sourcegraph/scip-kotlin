import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.publish.maven.MavenPublication

plugins {
    kotlin("jvm")
    id("com.github.johnrengelman.shadow")
    id("maven-publish")
}

group = "com.sourcegraph"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

// create a new sourceset for the subproject JavaExec tasks to consume as a runtime classpath
// maybe we should move snapshot to its own subproject?
val snapshots: SourceSet by sourceSets.creating {
    java.srcDirs("src/snapshots/kotlin")
}

// create a new configuration independent from the one consumed by the shadowJar task
val snapshotsImplementation: Configuration by configurations.getting {
    extendsFrom(configurations.testImplementation.get())
}

dependencies {
    implementation(kotlin("stdlib"))
    compileOnly(kotlin("compiler-embeddable"))
    implementation("com.google.protobuf", "protobuf-java", "3.15.7")
    implementation(projects.semanticdbKotlin)

    testImplementation(kotlin("compiler-embeddable"))
    testImplementation(kotlin("test"))
    testImplementation("io.kotest", "kotest-assertions-core", "4.5.0")
    testImplementation("com.github.tschuchortdev", "kotlin-compile-testing", "1.4.0")
    testImplementation("org.junit.jupiter", "junit-jupiter-params", "5.7.2")
    testImplementation("org.jetbrains.kotlin", "kotlin-stdlib-jdk8", "1.5.0") {
        version {
            strictly("1.5.0")
        }
    }.because("transitive dependencies introduce 1.4.31 to the classpath which conflicts, can't use testRuntimeOnly")
    testImplementation(kotlin("reflect"))
    testImplementation(kotlin("script-runtime", "1.5.0"))

    snapshotsImplementation("com.sourcegraph", "lsif-java_2.13", "0.5.6")
}

tasks.withType<KotlinCompile> {
    dependsOn(":${projects.semanticdbKotlin.name}:build")
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = freeCompilerArgs + listOf("-Xinline-classes")
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

publishing {
    publications {
        create<MavenPublication>("maven") {
            shadow.component(this)
        }
    }
    repositories {
        mavenLocal()
    }
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        showStandardStreams = true
        exceptionFormat = TestExceptionFormat.FULL
        events("passed", "skipped", "failed")
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
    relocate("com.intellij", "org.jetbrains.kotlin.com.intellij")
    minimize()
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")

    repositories {
        mavenCentral()
    }

    dependencies {
        implementation(kotlin("stdlib"))
        implementation("com.sourcegraph", "semanticdb-javac", "0.5.6")
    }

    afterEvaluate {
        val semanticdbJar: Configuration by configurations.creating {
            isCanBeConsumed = false
            isCanBeResolved = true
        }

        dependencies {
            semanticdbJar(project(mapOf(
                "path" to ":" + this@afterEvaluate.projects.semanticdbKotlinc.name,
                "configuration" to "semanticdbJar"
            )))
        }

        val sourceroot = rootDir.path
        val targetroot = this@afterEvaluate.project.buildDir.resolve( "semanticdb-targetroot")

        tasks.withType<KotlinCompile> {
            dependsOn(":${this@afterEvaluate.projects.semanticdbKotlinc.name}:shadowJar")
            outputs.upToDateWhen { false }
            val pluginJar = semanticdbJar.incoming.artifacts.artifactFiles.first().path
            kotlinOptions {
                jvmTarget = "1.8"
                freeCompilerArgs = freeCompilerArgs + listOf(
                    "-Xplugin=$pluginJar",
                    "-P",
                    "plugin:com.sourcegraph.lsif-kotlin:sourceroot=${sourceroot}",
                    "-P",
                    "plugin:com.sourcegraph.lsif-kotlin:targetroot=${targetroot}"
                )
            }
        }

        tasks.withType<JavaCompile> {
            dependsOn(":${this@afterEvaluate.projects.semanticdbKotlinc.name}:shadowJar")
            outputs.upToDateWhen { false }
            options.compilerArgs = options.compilerArgs + listOf(
                "-Xplugin:semanticdb -sourceroot:$sourceroot -targetroot:$targetroot"
            )
        }

        // create a sourceset in which to output the generated snapshots.
        // we may choose to not use sourcesets down the line
        val generatedSnapshots: SourceSet by sourceSets.creating {
            resources.srcDir("generatedSnapshots")
        }

        // for each subproject e.g. 'minimized', create a JavaExec task that invokes the snapshot creating main class
        task("snapshots", JavaExec::class) {
            dependsOn(
                project.getTasksByName("compileKotlin", false).first().path,
                project.getTasksByName("compileJava", false).first().path
            )
            outputs.upToDateWhen { false }
            main = "com.sourcegraph.lsif_kotlin.SnapshotKt"
            // this is required as the main class SnapshotKt is in this classpath
            classpath = snapshots.runtimeClasspath
            args = listOf(
                kotlin.sourceSets.main.get().kotlin.srcDirs.first().canonicalPath,
                sourceSets.main.get().java.srcDirs.first().canonicalPath
            )
            systemProperties = mapOf(
                "sourceroot" to sourceroot,
                "targetroot" to this@afterEvaluate.project.buildDir.resolve("semanticdb-targetroot"),
                "snapshotDir" to generatedSnapshots.resources.srcDirs.first())
        }
    }
}
