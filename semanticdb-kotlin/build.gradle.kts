import com.google.protobuf.gradle.*
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

plugins {
    kotlin("jvm")
    id("com.google.protobuf") version "0.8.15"
    id("com.github.marcoferrer.kroto-plus") version "0.6.1"
}

repositories {
    mavenCentral()
}

buildscript {
    repositories {
        jcenter()
    }

    dependencies {
        classpath("com.google.protobuf:protobuf-java:3.15.7")
    }
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.google.protobuf:protobuf-java:3.15.7")
}

val SourceSet.kotlin: SourceDirectorySet get() = this.withConvention(KotlinSourceSet::class) { kotlin }

krotoPlus {
    config {
        create("main") {
            builder.protoBuilders {
                useDslMarkers = true
                unwrapBuilders = true
            }
        }
    }
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.15.7"
    }

    generatedFilesBaseDir = sourceSets.main.get().kotlin.sourceDirectories.asPath.split(":")[0].removeSuffix("main/kotlin")

    plugins {
        id("kroto") {
            artifact = "com.github.marcoferrer.krotoplus:protoc-gen-kroto-plus:0.6.1"
        }
    }

    generateProtoTasks {
        val krotoConfig = file("${projectDir}/krotoconfig.json")
        all().forEach { task ->
            task.inputs.files(krotoConfig)

            task.plugins {
                id("kroto") {
                    outputSubDir = "java"
                    //option(krotoPlus.config["main"].asOption())
                    //option(krotoPlus.config.findByName("main")!!.asOption())
                    option("ConfigPath=${krotoConfig}")
                    //option(compConfigBuilder.build().toString())
                }
            }
        }
    }
}