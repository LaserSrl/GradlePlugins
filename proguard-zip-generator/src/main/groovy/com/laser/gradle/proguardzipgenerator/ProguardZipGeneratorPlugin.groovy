package com.laser.gradle.proguardzipgenerator

import com.laser.gradle.core.ModulePlugin
import com.laser.gradle.core.module.Module
import org.gradle.api.Project

/**
 * Standalone plugin used to attach the {@link ProguardZipGeneratorModule}.
 * The extensions of {@link ProguardZipGeneratorModule} will be attached to the project extensions.
 */
class ProguardZipGeneratorPlugin extends ModulePlugin {

    @Override
    protected Module[] modules(Project project) {
        // The only used module is ProguardZipGeneratorModule.
        return [new ProguardZipGeneratorModule(project)]
    }
}