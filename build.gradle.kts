import com.palantir.gradle.gitversion.VersionDetails
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import groovy.lang.Closure
import org.gradle.jvm.toolchain.internal.CurrentJvmToolchainSpec

plugins {
    kotlin("jvm") version "2.2.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("com.palantir.git-version") version "3.4.0"
    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
    id("com.diffplug.spotless") version "5.17.1"
}

val versionDetails: Closure<VersionDetails> by extra

allprojects {
    if (name !in setOf("minimized", "semanticdb-kotlin")) {
        apply(plugin = "com.diffplug.spotless")
        spotless {
            kotlin {
                ktfmt().dropboxStyle()
            }
        }
    }

    group = "com.sourcegraph"
    version = (project.properties["version"] as String).let {
        if (it != "unspecified" && !it.startsWith("refs"))
            return@let it.removePrefix("v")
        val lastTag = versionDetails().lastTag
        val tag =
            if (lastTag.startsWith("v")) lastTag.removePrefix("v")
            else "0.0.0"
        val lastNum = tag.split(".").last().toInt() + 1
        "${tag.split(".").subList(0, 2).joinToString(".")}.$lastNum-SNAPSHOT"
    }

}

repositories {
    mavenCentral()
}

nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://ossrh-staging-api.central.sonatype.com/service/local/"))
            snapshotRepositoryUrl.set(uri("https://central.sonatype.com/repository/maven-snapshots/"))
            username.set(System.getenv("SONATYPE_USERNAME"))
            password.set(System.getenv("SONATYPE_PASSWORD"))
        }
    }
}

subprojects {
    tasks.withType<PublishToMavenRepository> {
        doFirst {
            println("Publishing ${publication.groupId}:${publication.artifactId}:${publication.version} to ${repository.url}")
        }
        if (!(version as String).endsWith("SNAPSHOT")) {
            finalizedBy(rootProject.tasks.closeAndReleaseStagingRepository)
        }
    }
}

allprojects {
    afterEvaluate {
        kotlin {
            compilerOptions {
                jvmTarget = JvmTarget.JVM_1_8
            }
            jvmToolchain {
                (this as JavaToolchainSpec).languageVersion.set(JavaLanguageVersion.of(8))
            }
        }

        tasks.withType<JavaCompile> {
            sourceCompatibility = "1.8"
        }
    }
}
