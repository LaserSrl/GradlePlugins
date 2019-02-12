package com.laser.gradle.core.module

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.platform.base.Variant

/**
 * Implementation of {@link BaseModule} that depends on variants.
 * This module is an helper class used to specify a behavior related to an Android variant.
 *
 * @param ExtType type of the extension.
 */
abstract class VariantModule<ExtType> extends BaseModule<ExtType> {

    /**
     * Creates an instance of {@link VariantModule} for the given {@link Project}.
     *
     * @param project {@link Project} used inside the module.
     */
    VariantModule(Project project) {
        super(project)
    }

    /**
     * Called when the module must initialize its components.
     * This method must be called only once.
     *
     * @param project the {@link Project} used in the constructor.
     */
    @Override
    void attach(Project project) {
        project.with {
            def variantList = android.applicationVariants

            def handleTask = { Task task ->
                // Loop on the of variants.
                variantList.all { variant ->
                    // Manage the configurations for each variant.
                    handle(project, task, variant)
                }
            }

            // Get all previously created tasks.
            tasks.all { handleTask(it) }

            // Be notified when a new task is added.
            tasks.whenTaskAdded { handleTask(it) }
        }
    }

    /**
     * Used to specify a behavior related to a task and a variant.
     * This method is invoked only once for every combination of task - variant.
     *
     * @param project the {@link Project} used in the constructor.
     * @param dependency current added {@link Task} of the {@link Project}.
     * @param variant current variant.
     */
    abstract void handle(Project project, Task dependency, variant)
}