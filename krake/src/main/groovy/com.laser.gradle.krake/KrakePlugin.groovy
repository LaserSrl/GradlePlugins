package com.laser.gradle.krake

import com.laser.gradle.core.ModulePlugin
import com.laser.gradle.core.module.Module
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionContainer

/**
 * Gradle plugin used inside Krake projects.
 * This plugin permits to toggle the modules used usually in Krake projects.
 * The configurations of this module can be changed using {@link KrakeExtension}.
 */
final class KrakePlugin extends ModulePlugin {

    private ModuleRetriever moduleRetriever = new MapModuleRetriever()

    @Override
    protected ExtensionContainer extensionContainer(Project project) {
        // The container of all submodules extensions will be "krake".
        project.extensions.create("krake", KrakeExtension)
        return project.krake.extensions
    }

    @Override
    protected Module[] modules(Project project) {
        def modules = []
        moduleRetriever.allModuleClasses().each { moduleClass ->
            Module module
            // Get the constructor with the Project instance.
            def moduleConstructor = moduleClass.getConstructor(Project.class)
            if (moduleConstructor != null) {
                // Create the module through reflection.
                module = moduleConstructor.newInstance(project)
            } else {
                // Get the empty constructor.
                moduleConstructor = moduleClass.getConstructor()
                if (moduleConstructor != null) {
                    // Create the module through reflection.
                    module = moduleConstructor.newInstance()
                } else {
                    // The only supported constructors are the empty constructor and
                    // a constructor with only one parameter of type Project.
                    // The other constructors are unsupported because they cannot be
                    // instantiated through reflection.
                    throw IllegalArgumentException("Unsupported ${Module.class.name} constructor.")
                }
            }
            modules << module
        }
        return modules
    }

    @Override
    boolean shouldAttachModule(Project project, Module module) {
        return project.krake.importedModules.find { moduleName ->
            // Get the class by name if possible.
            def foundClass = moduleRetriever.moduleClassByName(moduleName)
            if (foundClass == null)
                throw new RuntimeException("The krake module with name \"$moduleName\" doesn't exist.")

            module.class == foundClass
        } != null
    }
}