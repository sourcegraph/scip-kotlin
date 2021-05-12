package com.sourcegraph.lsif_kotlin.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

/**
 * Project level plugin
 */
class LsifKotlinGradlePlugin: Plugin<Project> {
    override fun apply(project: Project) {}
}

/**
 * Compilation level plugin
 */
class LsifKotlinCompileGradlePlugin: KotlinCompilerPluginSupportPlugin {
    override fun applyToCompilation(kotlinCompilation: KotlinCompilation<*>): Provider<List<SubpluginOption>> {
        //kotlinCompilation.
        TODO()
    }

    override fun getCompilerPluginId(): String {
        TODO("Not yet implemented")
    }

    override fun getPluginArtifact(): SubpluginArtifact {
        TODO("Not yet implemented")
    }

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean = true

}