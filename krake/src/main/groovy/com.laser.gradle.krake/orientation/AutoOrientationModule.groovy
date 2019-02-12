package com.laser.gradle.krake.orientation

import com.laser.gradle.core.extension.ExtensionConfigurator
import com.laser.gradle.core.module.BaseModule
import com.laser.gradle.core.util.TaskBuilder
import org.gradle.api.Project

/**
 * Module used to add a common value for the attribute {@code android:screenOrientation}
 * contained in the AndroidManifest.xml file.
 * The configurations of this module can be changed using {@link AutoOrientationExtension}.
 */
class AutoOrientationModule extends BaseModule<AutoOrientationExtension> {

    AutoOrientationModule(Project project) {
        super(project)
    }

    @Override
    ExtensionConfigurator getExtensionConfigurator(Project project) {
        new ExtensionConfigurator("autoOrientation", AutoOrientationExtension)
    }

    @Override
    void attach(Project project) {
        def task = new TaskBuilder(project)
                .name("manageAutoOrientation")
                .type(AutoOrientationTask)
                .action {
            // Executed during configuration phase.
            srcDir = new File("$project.projectDir/src")
            orientation = "@integer/activity_orientation"
            // Executed during execution phase.
            doFirst {
                excludedActivities = extension.excludedActivities
            }
        }.build()

        // This task must run before the "preBuild".
        project.tasks.getByName("preBuild").dependsOn task
    }
}