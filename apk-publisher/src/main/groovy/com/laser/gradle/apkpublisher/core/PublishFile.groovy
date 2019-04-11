package com.laser.gradle.apkpublisher.core

/**
 * class that represent the file to be published
 */
class PublishFile {

    /**
     * File to upload, can be an Apk or an AppBundle
     */
    File publishFile

    /**
     * File Type
     */
    FileType fileType

    enum FileType {
        APK,
        APP_BUNDLE
    }
}