import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.tasks.testing.logging.TestExceptionFormat

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
    compileOnly(kotlin("compiler-embeddable"))
    implementation("com.google.protobuf", "protobuf-java", "3.15.7")
    implementation(projects.semanticdbKotlin)

    testImplementation(kotlin("compiler-embeddable"))
    testImplementation(kotlin("test"))
    testImplementation("io.kotest", "kotest-core", "4.2.0.RC2")
    testImplementation("io.kotest", "kotest-assertions-core", "4.5.0")
    testImplementation("io.kotest", "kotest-framework-datatest", "4.6.0")
    testImplementation("io.kotest", "kotest-runner-junit5-jvm", "4.6.0")
    testImplementation("com.github.tschuchortdev", "kotlin-compile-testing", "1.4.0")
    testImplementation("org.junit.jupiter", "junit-jupiter-params", "5.7.2")
    testImplementation("org.jetbrains.kotlin", "kotlin-stdlib-jdk8", "1.5.0") {
        version {
            strictly("1.5.0")
        }
    }.because("transitive dependencies introduce 1.4.31 to the classpath which conflicts, can't use testRuntimeOnly")
    testImplementation(kotlin("reflect"))
    testImplementation(kotlin("script-runtime", "1.5.0"))
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

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    freeCompilerArgs = listOf("-Xinline-classes")
}