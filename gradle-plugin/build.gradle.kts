import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    `java-gradle-plugin`
    id("com.gradle.plugin-publish") version "0.10.1"
    id("com.github.gmazzo.buildconfig") version "3.0.0"
}

group = "com.sourcegraph"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(gradleApi())
    implementation(kotlin("stdlib"))
    implementation(kotlin("gradle-plugin-api"))

    println(projects.semanticdbKotlinc.targetConfiguration)
    compileOnly(project(":" + projects.semanticdbKotlinc.name, configuration = "embeddable"))

    runtimeOnly(kotlin("compiler-embeddable"))
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

/*buildConfig {
    val project = projects.semanticdbKotlinc
    packageName(project.group.toString())
    buildConfigField("String", "KOTLIN_PLUGIN_ID", "\"${rootProject.extra["kotlin_plugin_id"]}\"")
    buildConfigField("String", "KOTLIN_PLUGIN_GROUP", "\"${project.group}\"")
    buildConfigField("String", "KOTLIN_PLUGIN_NAME", "\"${project.name}\"")
    buildConfigField("String", "KOTLIN_PLUGIN_VERSION", "\"${project.version}\"")
}*/

tasks.jar {
    // Embed compiler plugin in jar
    from(zipTree(configurations.compileClasspath.get().first { it.name.startsWith(projects.semanticdbKotlinc.name) }))

    manifest {
        attributes["Specification-Title"] = project.name
        attributes["Specification-Version"] = project.version
        attributes["Implementation-Title"] = "com.sourcegraph.lsif_kotlin.gradle"
        attributes["Implementation-Version"] = project.version
    }
}


gradlePlugin {
    plugins {
        create("lsif-kotlin") {
            id = "com.sourcegraph.lsif_kotlin.gradle"
            implementationClass = "com.sourcegraph.lsif_kotlin.gradle.LsifKotlinGradlePlugin"
        }
    }
}
