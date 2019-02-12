package com.laser.gradle.core.extension

/**
 * Defines the properties of a Gradle extension.
 */
class ExtensionConfigurator {
    String name
    Object type
    Object[] args

    /**
     * Creates a new instance of an {@link ExtensionConfigurator} defining the properties of a Gradle extension.
     *
     * @param name the name of the extension when it's used.
     * @param type the type of the extension.
     * @param args the optional arguments passed in the extension's constructor.
     */
    ExtensionConfigurator(String name, Object type, Object... args) {
        this.name = name
        this.type = type
        this.args = args
    }
}