package com.laser.gradle.core

import com.laser.gradle.core.module.ExtensionComponent
import com.laser.gradle.core.module.Module
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionContainer

/**
 * Gradle plugin used to attach other {@link Module}s to the main plugin.
 * The module approach can be used to manage the {@link Module}'s extensions inside the plugin.
 */
abstract class ModulePlugin extends BasePlugin {

    @Override
    void execute(Project project) {

        project.with {
            def extensionContainer = extensionContainer(it)

            def modules = modules(project)
            // Create the extensions of the modules.
            modules.findAll { module ->
                // The extensions can be created only for modules that implement ExtensionComponent.
                module instanceof ExtensionComponent
            }.each { module ->
                def configurator = module.getExtensionConfigurator()
                def extName = configurator.name
                // Create the extension in the container of the module extensions.
                extensionContainer.create(extName, configurator.type, configurator.args)
                // Inject the created extension in the module.
                module.setExtension(extensionContainer."$extName")
            }

            afterEvaluate {
                modules.findAll { module ->
                    // Check if the module should be attached.
                    shouldAttachModule(project, module)
                }.each { module ->
                    // Attach the module.
                    module.attach()
                }
            }
        }
    }

    /**
     * Get all modules that are related to this plugin.
     *
     * @param project {@link Project} in which the plugin is applied.
     * @return the list of all modules related to this plugin.
     */
    protected abstract Module[] modules(Project project)

    /**
     * Get the {@link ExtensionContainer} of the modules' extensions.
     * By default the extensions' container is the project, but it can be changed to have
     * a group of extensions inside a common extension.
     *
     * @param project {@link Project} in which the plugin is applied.
     * @return the {@link ExtensionContainer} of all modules' extensions.
     */
    protected ExtensionContainer extensionContainer(Project project) {
        // The default extension container is the root project.
        return project.extensions
    }

    /**
     * Check if a module should be attached to the plugin.
     *
     * @param project {@link Project} in which the plugin is applied.
     * @param module {@link Module} that isn't attached yet.
     * @return true if the module should be attached.
     */
    boolean shouldAttachModule(Project project, Module module) {
        // By default, the module can be attached.
        return true
    }
}