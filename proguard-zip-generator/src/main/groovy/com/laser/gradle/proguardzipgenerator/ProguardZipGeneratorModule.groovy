package com.laser.gradle.proguardzipgenerator

import com.laser.gradle.core.extension.ExtensionConfigurator
import com.laser.gradle.core.module.VariantModule
import com.laser.gradle.core.util.TaskBuilder
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.bundling.Zip

/**
 * Module that depends on variants used to generate a zip file containing the Proguard output files.
 * The configurations of this module can be changed using {@link ProguardZipGeneratorExtension}.
 */
class ProguardZipGeneratorModule extends VariantModule<ProguardZipGeneratorExtension> {

    private static final String ZIP_BASE_NAME = "proguard"

    ProguardZipGeneratorModule(Project project) {
        super(project)
    }

    @Override
    ExtensionConfigurator getExtensionConfigurator(Project project) {
        def destinationPath = "$project.projectDir/proguardZip".toString()
        new ExtensionConfigurator("proguardZipGenerator", ProguardZipGeneratorExtension, destinationPath)
    }

    @Override
    void handle(Project project, Task dependency, Object variant) {
        def variantName = variant.name

        // The dependency task is "package$variant"
        def expectingTaskName = "package" + variantName.capitalize()
        if (expectingTaskName != dependency.name || !variant.buildType.isMinifyEnabled())
            return

        // Get the base Proguard output file path.
        def mappingPath = "${project.projectDir}/build/outputs/mapping/"
        // Get the current product flavor.
        def flavor = variant.productFlavors.find {
            variantName.contains(it.name)
        }
        if (flavor != null) {
            // Add the relative path related to the flavor.
            mappingPath += "$flavor.name/"
        }
        // Add the relative path related to the build type.
        mappingPath += "$variant.buildType.name/"

        def versionAppendix = variant.versionName
        if (extension.saveVersionCode)
            versionAppendix += "-$variant.versionCode"

        def destinationPath = extension.destinationPath
        def includeMapping = extension.includeMapping
        def includeDump = extension.includeDump
        def includeResources = extension.includeResources
        def includeSeeds = extension.includeSeeds
        def includeUsage = extension.includeUsage

        def proguardTask = new TaskBuilder(project)
                .name("generateProguardZip${variantName.capitalize()}")
                .description("Generate proguard zip of the current source files")
                .type(Zip)
                .action {

            baseName ZIP_BASE_NAME
            appendix variantName
            version versionAppendix
            from mappingPath

            destinationDir = new File(destinationPath)

            if (!includeMapping)
                exclude "mapping.txt"

            if (!includeDump)
                exclude "dump.txt"

            if (!includeResources)
                exclude "resources.txt"

            if (!includeSeeds)
                exclude "seeds.txt"

            if (!includeUsage)
                exclude "usage.txt"
        }.build()

        // The proguard task will run after the "package$variant" task.
        dependency.finalizedBy proguardTask
    }
}