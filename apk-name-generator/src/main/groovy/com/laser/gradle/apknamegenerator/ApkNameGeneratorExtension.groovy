package com.laser.gradle.apknamegenerator

/**
 * Extension used by {@link ApkNameGeneratorModule} to specify the configurations for the
 * generation of the name of the apk.
 */
class ApkNameGeneratorExtension {

    /**
     * Specifies if the name of the apk must include the name of the variant.
     */
    boolean includeVariantName = true

    /**
     * Specifies if the name of the apk must include the version code.
     */
    boolean includeVersionCode = false

    /**
     * Specifies if the name of the apk must include the version name.
     */
    boolean includeVersionName = true
}