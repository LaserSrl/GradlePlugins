package com.laser.gradle.apkpublisher.core

import org.gradle.api.Project

/**
 * a {@link PublishModule} must specify a class that extends from this one.
 * this is the generic target for each module that has the generic params that
 * a user can specify, for example the file in which there are the changelogs.
 */
abstract class PublishTarget {
    String name

    Project project

    /**
     * publish also the changelog in the release notes of the apk
     */
    boolean publishChangeLog = true

    /**
     * The file in which there are all the notes for the supported languages for the release
     */
    String versionChangeLogPath

    PublishTarget(String name, Project project) {
        this.name = name
        this.project = project
    }

    /**
     * Called from the {@link ApkPublisherModule} when is time to check the params.
     * a target must use this method for check the validity of its params or for check
     * connection to a server or other..
     * @return
     */
    abstract boolean canPublish(PublishParams params)

    /**
     * Called from the {@link ApkPublisherModule} when is time to publish.
     * Write all the logic of the publication here.
     * @param params
     */
    abstract void publish(PublishParams params)
}
