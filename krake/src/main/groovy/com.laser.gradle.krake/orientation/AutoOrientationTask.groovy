package com.laser.gradle.krake.orientation

import groovy.io.FileType
import groovy.xml.XmlUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.TaskAction

/**
 * Gradle task used to specify a common value for the attribute {@code android:screenOrientation}
 * contained in the AndroidManifest.xml file.
 */
class AutoOrientationTask extends DefaultTask {

    /**
     * Specifies the source directory in which all manifests are contained.
     */
    @InputDirectory
    File srcDir

    /**
     * Specifies the common value of the screen orientation that will be added to all activities' nodes.
     */
    @Input
    String orientation

    /**
     * Specifies the activities that must be excluded from this task.
     */
    def excludedActivities = []

    @SuppressWarnings("GroovyUnusedDeclaration")
    @TaskAction
    public void run() {
        // Create the XML slurper that won't validate the XML after writing it.
        XmlSlurper xmlSlurper = new XmlSlurper(false, false)

        def manifestList = []

        def manifestFileName = "AndroidManifest.xml"

        // Get all manifests inside the src folder.
        srcDir.eachFileRecurse(FileType.FILES) { file ->
            if (file.name == manifestFileName) {
                manifestList << file
            }
        }

        manifestList.each {
            // Parse the manifest.
            def manifest = xmlSlurper.parse(it)

            // Get all activities' nodes that will be changed.
            def activities = manifest.application.activity.findAll { activity ->
                // Get the activity name for each node.
                def activityName = activity.@"android:name"
                // Check if the activity is excluded or not.
                !excludedActivities.contains(activityName)
            }

            def screenOrientationAttr = "android:screenOrientation"
            def modifiedCount = 0
            activities.findAll { activity ->
                // Get only the activities that haven't a specified "screenOrientation" attribute.
                activity.attributes().get(screenOrientationAttr) == null
            }.each { activity ->
                // Change the value of the "screenOrientation" attribute.
                activity.@"$screenOrientationAttr" = orientation
                modifiedCount++
            }

            if (modifiedCount > 0) {
                // If at least one node was modified, the XML must be serialized.
                def serializedManifest = XmlUtil.serialize(manifest)

                // It will be rewritten at the same path.
                def writer = new FileWriter(it)
                writer.write(serializedManifest)
                writer.close()
            }
        }
    }

    /**
     * Used to specify the activities that must be excluded from this task.
     *
     * @param activities the array of the name of the activities that must be excluded.
     */
    public void excludedActivities(String... activities) {
        excludedActivities = activities
    }
}