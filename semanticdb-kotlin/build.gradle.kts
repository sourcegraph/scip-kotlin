import com.google.protobuf.gradle.*

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
        mavenCentral()
    }

    dependencies {
        classpath("com.google.protobuf:protobuf-java:3.15.7")
    }
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.google.protobuf:protobuf-java:3.15.7")
    implementation("com.sourcegraph", "semanticdb-javac", "0.5.6")
}


afterEvaluate {
    tasks.processResources {
        dependsOn(tasks.getByName("generateProto"))
    }

    tasks.compileKotlin {
        dependsOn(tasks.getByName("generateProto"))
    }

    tasks.withType<JavaCompile> {
        val sourceroot = rootDir.path
        val targetroot = this.project.buildDir.resolve( "semanticdb-targetroot")
        options.compilerArgs = options.compilerArgs + listOf(
            "-Xplugin:semanticdb -sourceroot:$sourceroot -targetroot:$targetroot"
        )
    }
}

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

    generatedFilesBaseDir = kotlin.sourceSets.main.get().kotlin.srcDirs.first().path.split(":")[0].removeSuffix("main/kotlin")

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
                    option("ConfigPath=${krotoConfig}")
                }
            }
        }
    }
}