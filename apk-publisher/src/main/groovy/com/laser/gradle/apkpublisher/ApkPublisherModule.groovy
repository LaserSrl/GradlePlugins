package com.laser.gradle.apkpublisher

import com.laser.gradle.apkpublisher.core.ApkPublisherExtension
import com.laser.gradle.apkpublisher.core.PublishParams
import com.laser.gradle.apkpublisher.core.PublishTarget
import com.laser.gradle.core.extension.ExtensionConfigurator
import com.laser.gradle.core.module.VariantModule
import com.laser.gradle.core.util.TaskBuilder
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.Task

/**
 * module for the publication of an apk for each variant in release Build Type
 * that has a property 'publishTarget' with the {@link } that this module
 * will use for the publication
 */
class ApkPublisherModule extends VariantModule<ApkPublisherExtension> {

    private static final def PUBLISH_DEFAULT_DIR = "publishApk"
    private static final def PUBLISH_VERSION_NOTES_DIR = "$PUBLISH_DEFAULT_DIR/changeLogs"

    ApkPublisherModule(Project project) {
        super(project)
    }

    @Override
    ExtensionConfigurator getExtensionConfigurator(Project project) {
        //specify the default publish modules and the versionsFilePath
        String versionsFilePath = "config.json"

        List<String> modules = new ArrayList<>()
        modules.add("com.laser.gradle.apkpublisher.action.store.PlayStoreModule")
        modules.add("com.laser.gradle.apkpublisher.action.smb.SmbModule")

        return new ExtensionConfigurator("apkPublisher", ApkPublisherExtension, project, modules, versionsFilePath)
    }

    /**
     * creation of tasks for the publication for each variant in Release Build Type.
     * @param dependency
     * @param variant
     */
    @Override
    void handle(Project project, Task dependency, Object variant) {
        def variantName = variant.name.capitalize()
        def currentFlavor = variant.productFlavors.get(0)

        def publishTaskName = "publishApk${variantName}"

        if (variantName.contains("Release") &&
                currentFlavor.ext.has("publishTarget") &&
                variant.isSigningReady() &&
                project.tasks.findByName(publishTaskName) == null) {

            def publishApkExtension = getExtension()

            def versionKey = variantName
            def versionName = variant.versionName
            def versionCode = variant.versionCode

            PublishTarget currentPublishTarget = currentFlavor.ext.publishTarget

            //create the default directory
            def apkPublishDirectory = project.file(PUBLISH_DEFAULT_DIR)
            if (!apkPublishDirectory.exists())
                apkPublishDirectory.mkdir()

            //file for versions
            def versionsFile = project.file("$PUBLISH_DEFAULT_DIR/${publishApkExtension.versionsFilePath}")

            def json = prepareChangeLogs(project, currentPublishTarget)
            def params = new PublishParams()
            params.variant = variant
            params.changeLogs = json

            //create the task for the publication
            def publishApkTask = new TaskBuilder(project)
                    .name(publishTaskName)
                    .action {
                        doLast {
                            currentPublishTarget.publish(params)
                        }
                    }.build()

            publishApkTask.dependsOn variant.assemble

            def preTaskName = "checkBeforePublish${variantName}"
            def preTask = new TaskBuilder(project)
                    .name(preTaskName)
                    .action {

                doLast {
                    checkIfApkCanBePublished(versionsFile, versionKey, versionName, versionCode)

                    //check if target can publish
                    if (!currentPublishTarget.canPublish(params))
                        throw new GradleException("unable to publish.")
                }
            }.build()

            publishApkTask.dependsOn preTask

            //create the task for the validation post publication
            //run only if the publish not generate errors
            def postTask = new TaskBuilder(project)
                    .name("validateAfterPublish${variantName}")
                    .action {
                doLast {
                    saveCurrentVersion(versionsFile, versionKey, versionName, versionCode)
                }

                onlyIf {
                    publishApkTask.state.failure == null
                }
            }.build()

            publishApkTask.finalizedBy postTask
        }
    }

    static private Map<String, String> prepareChangeLogs(Project project, PublishTarget currentPublishTarget) {
        //create the versionChangeLogs directory
        def versionNotesDirectory = project.file(PUBLISH_VERSION_NOTES_DIR)
        if (!versionNotesDirectory.exists())
            versionNotesDirectory.mkdir()

        //check if the versionChangeLogs File is present for this variant, if not create the default file
        def json
        if (currentPublishTarget.publishChangeLog) {
            if (currentPublishTarget.versionChangeLogPath == null)
                currentPublishTarget.versionChangeLogPath = "${currentPublishTarget.name}.json"

            def variantVersionNotesFile = project.file("$PUBLISH_VERSION_NOTES_DIR/${currentPublishTarget.versionChangeLogPath}")

            if (!variantVersionNotesFile.exists()) {
                variantVersionNotesFile.createNewFile()
                json = new HashMap()
                json.put("default", "")
                variantVersionNotesFile.text = JsonOutput.toJson(json)
            } else {
                json = new JsonSlurper().parseText(variantVersionNotesFile.text) as Map
            }
        }
        return json
    }

    /**
     * check if apk can be published.
     * this open the json file with all the versions also published
     * and check if the current version name is not equal to the last used
     * and if the current version code is not equal to the last used.
     * If the file not exist, then skip this check
     */
    static private void checkIfApkCanBePublished(versionsFile, versionKey, versionName, versionCode) {
        if (versionsFile.exists()) {
            def json = new JsonSlurper().parseText(versionsFile.text) as Map
            if (json.containsKey(versionKey)) {
                def objSaved = json.get(versionKey) as Tuple2<String, Integer>
                def lastVersionName = objSaved.getFirst()
                def lastVersionCode = objSaved.getSecond()
                if (lastVersionName == versionName) {
                    throw new GradleException("version name is the same as the last version name used: $lastVersionName")
                }
                if (versionCode <= lastVersionCode) {
                    throw new GradleException("versionCode is less or equal to last version code used for the publish: $lastVersionCode")
                }
            }
        }
    }

    /**
     * the publish is ok, so save the current version code and version name
     * in a file with the path provided from the configuration of the user,
     * if this file not exist then create it.
     */
    static private void saveCurrentVersion(versionsFile, versionKey, versionName, versionCode) {
        def json
        if (versionsFile.exists()) {
            json = new JsonSlurper().parseText(versionsFile.text) as Map
        } else {
            versionsFile.createNewFile()
            json = new HashMap()
        }
        def objToSave = new Tuple2(versionName, versionCode)
        json.put(versionKey, objToSave)
        versionsFile.text = JsonOutput.toJson(json)
    }
}