package com.laser.gradle.apkpublisher.core

/**
 * class that will be passed to the {@link PublishTarget} when the publication
 * must start
 */
class PublishParams {

    /**
     * android variant
     */
    Object variant

    /**
     * map of changelogs, with the language as key
     */
    Map<String, String> changeLogs

    /**
     * file to be published
     */
    PublishFile file
}