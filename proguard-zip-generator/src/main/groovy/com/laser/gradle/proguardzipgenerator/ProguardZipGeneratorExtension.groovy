package com.laser.gradle.proguardzipgenerator

/**
 * Extension used by {@link ProguardZipGeneratorModule} to specify the configurations for the
 * generation of the zip containing Proguard files.
 */
class ProguardZipGeneratorExtension {

    /**
     * Specifies the destination path in which the zip will be created.
     */
    String destinationPath

    /**
     * Specifies if the zip filename must contain the version code.
     */
    boolean saveVersionCode = false

    /**
     * Specifies if the zip must contain the dump.txt file.
     */
    boolean includeDump = false

    /**
     * Specifies if the zip must contain the mapping.txt file.
     */
    boolean includeMapping = true

    /**
     * Specifies if the zip must contain the resources.txt file.
     */
    boolean includeResources = true

    /**
     * Specifies if the zip must contain the seeds.txt file.
     */
    boolean includeSeeds = true

    /**
     * Specifies if the zip must contain the usage.txt file.
     */
    boolean includeUsage = true

    /**
     * Creates a new instance of this extensions with given destination path.
     *
     * @param destinationPath the path in which the zip will be created.
     */
    ProguardZipGeneratorExtension(String destinationPath) {
        this.destinationPath = destinationPath
    }
}