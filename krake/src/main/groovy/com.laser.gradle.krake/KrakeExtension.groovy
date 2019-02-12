package com.laser.gradle.krake

/**
 * Extension used by {@link KrakePlugin} to specify the configurations for a Krake project.
 */
class KrakeExtension {

    /**
     * Specifies the aliases of the modules that must be attached to the project.
     */
    def importedModules = []

    /**
     * @param importedModules the aliases of the modules that must be attached to the project.
     */
    void modules(String... importedModules) {
        this.importedModules = importedModules
    }
}