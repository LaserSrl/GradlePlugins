package com.laser.gradle.core.module

import com.laser.gradle.core.extension.ExtensionConfigurator

/**
 * Used to define a component that interacts with a Gradle extension.
 * After its creation, the extension must be injected from outside using {@link ExtensionComponent#setExtension}.
 *
 * @param <ExtType> type of the extension.
 */
interface ExtensionComponent<ExtType> {

    /**
     * @return the instance of a configurator for an extension of type <ExtType>.
     */
    ExtensionConfigurator getExtensionConfigurator()

    /**
     * Get the extension of type <ExtType>.
     * The extension must be created before with an {@link ExtensionConfigurator}.
     *
     * @return extension of type <ExtType>.
     */
    ExtType getExtension()

    /**
     * Inject an extension of type <ExtType> into this component.
     *
     * @param extension the extension that must be set.
     */
    void setExtension(ExtType extension)
}