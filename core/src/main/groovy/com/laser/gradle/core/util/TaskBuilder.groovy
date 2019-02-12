package com.laser.gradle.core.util

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.internal.TaskInternal

/**
 * Used to create a task with a builder approach.
 *
 * @param Builder the type of the builder that extends this one.
 * The recursive generic is needed to avoid the cast in subclasses.
 */
class TaskBuilder<Builder extends TaskBuilder<Builder>> {

    private static final String DEFAULT_GROUP = "laser"

    private Project project
    private String name
    private String description
    private String group
    private Object type = TaskInternal
    private Closure action

    /**
     * Creates an instance of this builder.
     *
     * @param project {@link Project} in which the task must be created.
     */
    TaskBuilder(Project project) {
        this.project = project
    }

    /**
     * Sets the description of this task.
     *
     * @param description the description of the task.
     * @return this instance of {@link TaskBuilder}.
     */
    Builder description(String description) {
        this.description = description
        return this
    }

    /**
     * Sets the name of this task.
     * The name must be unique.
     * A task with the same name of another existing task will be ignored.
     *
     * @param name the name of the task.
     * @return this instance of {@link TaskBuilder}.
     */
    Builder name(String name) {
        this.name = name
        return this
    }

    /**
     * Sets the group of this task.
     * The default group is {@link #DEFAULT_GROUP}
     *
     * @param group the group of the task.
     * @return this instance of {@link TaskBuilder}.
     */
    Builder group(String group) {
        this.group = group
        return this
    }

    /**
     * Sets the type of this task.
     * If the type is not set, it will be a base task.
     *
     * @param type the type of the task (e.g. Zip, Exec, etc..)
     * @return this instance of {@link TaskBuilder}.
     */
    Builder type(type) {
        this.type = type
        return this
    }

    /**
     * Sets the action of this task.
     * In this action, the task can be configured or can specify other actions
     * like {@link Task#doFirst}, {@link Task#doLast}, etc..
     *
     * @param action the action of the task.
     * @return this instance of {@link TaskBuilder}.
     */
    Builder action(Closure action) {
        this.action = action
        return this
    }

    /**
     * Builds the task with the configurations specified in this builder.
     * If the task wasn't created before, it will be created and attached to the project.
     * Otherwise, it won't be created and the previously created task will be returned.
     *
     * @return the built task.
     */
    Task build() {
        def tasks = project.tasks
        // Check if the task was created before.
        def task = tasks.findByName(name)
        if (task == null) {
            // Create the task.
            if (action == null)
                task = project.tasks.create(name, type)
            else
                task = project.tasks.create(name, type, action)

            if (group == null) {
                // The default group is "laser".
                group = DEFAULT_GROUP
            }
            task.setGroup(group)
            task.setDescription(description)
        }
        return task
    }
}