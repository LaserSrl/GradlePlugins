package com.laser.gradle.krake.orientation

/**
 * Extension used by {@link AutoOrientationModule} to specify the configurations for the
 * task used to add a common value to the orientation in the AndroidManifest.xml file.
 */
class AutoOrientationExtension {

    /**
     * Specifies the activities that must be excluded from this task.
     */
    def excludedActivities = []

    /**
     * Used to specify the activities that must be excluded from this task.
     *
     * @param activities the array of the name of the activities that must be excluded.
     */
    void exclude(String... excludedActivities) {
        this.excludedActivities = excludedActivities
    }

}