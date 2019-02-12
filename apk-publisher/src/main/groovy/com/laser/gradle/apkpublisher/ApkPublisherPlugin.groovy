package com.laser.gradle.apkpublisher

import com.laser.gradle.core.ModulePlugin
import com.laser.gradle.core.module.Module
import org.gradle.api.Project

/**
 * Standalone plugin used to attach the {@link ApkPublisherModule}.
 * The extensions of {@link ApkPublisherModule} will be attached to the project extensions.
 */
class ApkPublisherPlugin extends ModulePlugin {

    @Override
    protected Module[] modules(Project project) {
        // The only used module is ApkPublisherModule.
        return [new ApkPublisherModule(project)]
    }
}