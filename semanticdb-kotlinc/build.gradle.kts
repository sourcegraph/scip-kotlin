import java.net.URI
import com.google.protobuf.gradle.*
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.publish.maven.MavenPublication

plugins {
    kotlin("jvm")
    id("com.github.johnrengelman.shadow")
    id("com.google.protobuf") version "0.9.4"
    id("maven-publish")
    signing
}

repositories {
    mavenCentral()
}

// create a new sourceset for the subproject JavaExec tasks to consume as a runtime classpath
// maybe we should move snapshot to its own subproject?
val snapshots: SourceSet by sourceSets.creating {
    java.srcDirs("src/snapshots/kotlin")
}

// create a new configuration independent of the one consumed by the shadowJar task
val snapshotsImplementation: Configuration by configurations.getting {
    extendsFrom(configurations.testImplementation.get())
}

dependencies {
    implementation(kotlin("stdlib"))
    compileOnly(kotlin("compiler-embeddable"))
    implementation("com.google.protobuf", "protobuf-java", "3.17.3")

    testImplementation(kotlin("compiler-embeddable"))
    testImplementation(kotlin("test"))
    testImplementation("io.kotest", "kotest-assertions-core", "4.6.3")

    // Unable to use com.github.tschuchortdev:kotlin-compile-testing until 1.9.x support is fixed
    //   https://github.com/tschuchortdev/kotlin-compile-testing/issues/390
    // Until then, we use the fork from https://github.com/ZacSweers/kotlin-compile-testing instead.
    // testImplementation("com.github.tschuchortdev", "kotlin-compile-testing", "1.5.0")
    testImplementation("dev.zacsweers.kctfork", "core", "0.7.1")

    testImplementation(kotlin("reflect"))

    snapshotsImplementation("com.sourcegraph", "scip-java_2.13", "0.12.0")
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.17.3"
    }

    plugins {
        kotlin { }
    }
}

afterEvaluate {
    tasks.processResources {
        dependsOn(tasks.getByName("generateProto"))
    }

    tasks.compileKotlin {
        dependsOn(tasks.getByName("generateProto"))
    }
}

kotlin {
    jvmToolchain(8)
    compilerOptions {
        jvmTarget = JvmTarget.JVM_1_8
        freeCompilerArgs.addAll(
            "-Xinline-classes",
            "-Xcontext-parameters",
        )
    }
}

tasks.withType<JavaCompile> {
    sourceCompatibility = "1.8"
}

val semanticdbJar: Configuration by configurations.creating {
    isCanBeConsumed = true
    isCanBeResolved = false
}

artifacts {
    add(semanticdbJar.name, tasks.shadowJar)
}

tasks.jar {
    archiveClassifier.set("slim")
    manifest {
        attributes["Specification-Title"] = project.name
        attributes["Specification-Version"] = project.version
        attributes["Implementation-Title"] = "semanticdb-kotlinc"
        attributes["Implementation-Version"] = project.version
    }
}

tasks.shadowJar {
    archiveClassifier.set("")
    relocate("com.intellij", "org.jetbrains.kotlin.com.intellij")
    minimize()
}

val sourceJar = task<Jar>("sourceJar") {
    dependsOn(tasks.classes)
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

val javadocJar = task<Jar>("javadocJar") {
    dependsOn(tasks.javadoc)
    archiveClassifier.set("javadoc")
    from(tasks.javadoc.get().destinationDir)
}

publishing {
    publications {
        create<MavenPublication>("shadow") {
            this.apply {
                pom {
                    name.set("semanticdb-kotlinc")
                    description.set("A kotlinc plugin to emit SemanticDB information")
                    url.set("https://github.com/sourcegraph/scip-kotlin")
                    developers {
                        developer {
                            id.set("strum355")
                            name.set("Noah Santschi-Cooney")
                            email.set("noah@sourcegraph.com")
                        }
                        developer {
                            id.set("olafurpg")
                            name.set("Ólafur Páll Geirsson")
                            email.set("olafurpg@sourcegraph.com")
                        }
                    }
                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }
                    scm {
                        url.set("https://github.com/sourcegraph/scip-kotlin")
                    }
                }
                shadow.component(this)
                artifact(sourceJar)
                artifact(javadocJar)
            }
        }
    }
    repositories {
        maven {
            name = "sonatype"
            url =
                if (!(version as String).endsWith("-SNAPSHOT"))
                    URI("https://ossrh-staging-api.central.sonatype.com/service/local/staging/deploy/maven2/")
                else
                    URI("https://central.sonatype.com/repository/maven-snapshots/")
            credentials {
                username = System.getenv("SONATYPE_USERNAME")
                password = System.getenv("SONATYPE_PASSWORD")
            }
        }
    }
}

signing {
    val signingKey: String? by project
    val signingPassword: String? by project
    useInMemoryPgpKeys(signingKey, signingPassword)
    sign(publishing.publications["shadow"])
}

tasks.withType<Sign>().configureEach {
    onlyIf { !(project.version as String).endsWith("SNAPSHOT") }
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        showStandardStreams = true
        exceptionFormat = TestExceptionFormat.FULL
        events("passed", "failed")
    }
    maxHeapSize = "2g"
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")

    repositories {
        mavenCentral()
    }

    dependencies {
        implementation(kotlin("stdlib"))
    }

    afterEvaluate {
        val semanticdbJar: Configuration by configurations.creating {
            isCanBeConsumed = false
            isCanBeResolved = true
        }

        dependencies {
            semanticdbJar(project(mapOf(
                "path" to projects.semanticdbKotlinc.dependencyProject.path,
                "configuration" to "semanticdbJar"
            )))
        }

        val sourceroot = rootDir.path
        val targetroot = project.buildDir.resolve( "semanticdb-targetroot")

        tasks.withType<KotlinCompile> {
            dependsOn(projects.semanticdbKotlinc.dependencyProject.tasks.shadowJar.get().path)
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
            dependsOn(projects.semanticdbKotlinc.dependencyProject.tasks.shadowJar.get().path)
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

        // for each subproject e.g. 'minimized', create a JavaExec task that invokes the snapshot creating main class
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
            classpath = snapshots.runtimeClasspath
            args = listOf(
                kotlin.sourceSets.main.get().kotlin.srcDirs.first().canonicalPath,
                sourceSets.main.get().java.srcDirs.first().canonicalPath
            )
            systemProperties = mapOf(
                "sourceroot" to sourceroot,
                "targetroot" to project.buildDir.resolve("semanticdb-targetroot"),
                "snapshotDir" to generatedSnapshots.resources.srcDirs.first())
        }
    }
}
