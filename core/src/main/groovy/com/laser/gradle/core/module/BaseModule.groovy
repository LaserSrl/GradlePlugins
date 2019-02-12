package com.laser.gradle.core.module

import com.laser.gradle.core.extension.ExtensionConfigurator
import org.gradle.api.Project

/**
 * Base implementation of {@link Module} and {@link ExtensionComponent} that
 * can be used with a {@link Project}.
 *
 * @param <ExtType> type of the extension.
 */
abstract class BaseModule<ExtType> implements Module, ExtensionComponent<ExtType> {

    private ExtType extension
    private Project project

    /**
     * Creates an instance of {@link BaseModule} for the given {@link Project}.
     *
     * @param project {@link Project} used inside the module.
     */
    BaseModule(Project project) {
        this.project = project
    }

    @Override
    final void attach() {
        attach(project)
    }

    @Override
    final ExtensionConfigurator getExtensionConfigurator() {
        return getExtensionConfigurator(project)
    }

    @Override
    ExtType getExtension() {
        extension
    }

    @Override
    void setExtension(ExtType extension) {
        this.extension = extension
    }

    /**
     * Called when the module must initialize its components.
     * This method must be called only once.
     *
     * @param project the {@link Project} used in the constructor.
     */
    abstract void attach(Project project)

    /**
     * @param project the {@link Project} used in the constructor.
     * @return the instance of a configurator for an extension of type <ExtType>.
     */
    abstract ExtensionConfigurator getExtensionConfigurator(Project project)
}