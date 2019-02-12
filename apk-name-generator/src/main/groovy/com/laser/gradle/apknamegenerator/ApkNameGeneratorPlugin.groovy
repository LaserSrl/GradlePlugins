package com.laser.gradle.apknamegenerator

import com.laser.gradle.core.ModulePlugin
import com.laser.gradle.core.module.Module
import org.gradle.api.Project

/**
 * Standalone plugin used to attach the {@link ApkNameGeneratorModule}.
 * The extensions of {@link ApkNameGeneratorModule} will be attached to the project extensions.
 */
class ApkNameGeneratorPlugin extends ModulePlugin {

    @Override
    protected Module[] modules(Project project) {
        // The only used module is ApkGeneratorModule.
        return [new ApkNameGeneratorModule(project)]
    }
}