package com.laser.gradle.apknamegenerator

import com.laser.gradle.core.extension.ExtensionConfigurator
import com.laser.gradle.core.module.VariantModule
import com.laser.gradle.core.util.TaskBuilder
import org.gradle.api.Project
import org.gradle.api.Task

/**
 * Module that depends on variants used to generate the name of the apk.
 * The configurations of this module can be changed using {@link ApkNameGeneratorExtension}.
 */
class ApkNameGeneratorModule extends VariantModule<ApkNameGeneratorExtension> {

    private static final String DEFAULT_BASE_NAME = "app"

    ApkNameGeneratorModule(Project project) {
        super(project)
    }

    @Override
    ExtensionConfigurator getExtensionConfigurator(Project project) {
        new ExtensionConfigurator("apkNameGenerator", ApkNameGeneratorExtension)
    }

    @Override
    void handle(Project project, Task dependency, Object variant) {
        def variantName = variant.name

        // The dependency task is "assemble$variant"
        def expectingTaskName = "assemble" + variantName.capitalize()
        if (expectingTaskName != dependency.name)
            return

        def apkTask = new TaskBuilder(project)
                .name("generateApkName${variantName.capitalize()}")
                .description("Change the apk name based on variant.")
                .action {
            variant.outputs.all { output ->

                def newName = project.archivesBaseName
                if (newName == null)
                    newName = DEFAULT_BASE_NAME

                if (extension.includeVariantName)
                    newName += "-$variantName"

                if (extension.includeVersionName)
                    newName += "-${variant.versionName}"

                if (extension.includeVersionCode)
                    newName += "-${variant.versionCode}"

                newName += ".apk"

                // Change the apk file name with the new name.
                outputFileName = newName
            }
        }.build()

        // The apk task will run after the "assemble$variant" task.
        dependency.finalizedBy apkTask
    }
}