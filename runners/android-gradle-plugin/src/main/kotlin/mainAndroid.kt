package org.jetbrains.dokka.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.jetbrains.dokka.DokkaConfiguration.ExternalDocumentationLink.Builder
import org.jetbrains.dokka.DokkaConfiguration.SourceRoot
import java.io.File

internal const val CONFIGURATION_EXTENSION_NAME = "configuration"
internal const val MULTIPLATFORM_EXTENSION_NAME = "multiplatform"

open class DokkaAndroidPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        DokkaVersion.loadFrom(javaClass.getResourceAsStream("/META-INF/gradle-plugins/org.jetbrains.dokka-android.properties"))
        project.tasks.create("dokka", DokkaAndroidTask::class.java).apply {
            dokkaRuntime = project.configurations.create("dokkaRuntime")
            moduleName = project.name
            outputDirectory = File(project.buildDir, "dokka").absolutePath
        }
        project.tasks.withType(DokkaTask::class.java) { task ->
            val passConfiguration = project.container(GradlePassConfigurationImpl::class.java)
            task.extensions.add(MULTIPLATFORM_EXTENSION_NAME, passConfiguration)
            task.extensions.create(CONFIGURATION_EXTENSION_NAME, GradlePassConfigurationImpl::class.java, "")
        }
    }
}

private val ANDROID_REFERENCE_URL = Builder("https://developer.android.com/reference/").build()

open class DokkaAndroidTask : DokkaTask() {

    @Input var noAndroidSdkLink: Boolean = false

    override fun collectSuppressedFiles(sourceRoots: List<SourceRoot>): List<String> {
        val generatedRoot = project.buildDir.resolve("generated").absoluteFile
        return sourceRoots
            .map { File(it.path) }
            .filter { it.startsWith(generatedRoot) }
            .flatMap { it.walk().toList() }
            .map { it.absolutePath }
    }

    init {
        project.afterEvaluate {
            if (!noAndroidSdkLink) externalDocumentationLinks.add(ANDROID_REFERENCE_URL)
        }
    }
}
