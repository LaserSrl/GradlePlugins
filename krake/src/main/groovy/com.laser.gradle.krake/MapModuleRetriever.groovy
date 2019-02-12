package com.laser.gradle.krake

import com.laser.gradle.apknamegenerator.ApkNameGeneratorModule
import com.laser.gradle.apkpublisher.ApkPublisherModule
import com.laser.gradle.core.module.Module
import com.laser.gradle.krake.orientation.AutoOrientationModule
import com.laser.gradle.krake.term.TermIconDownloaderModule
import com.laser.gradle.proguardzipgenerator.ProguardZipGeneratorModule

/**
 * Implementation of {@link ModuleRetriever} that uses an in-memory map to store the module classes.
 */
final class MapModuleRetriever implements ModuleRetriever {

    private Map<String, Class<? extends Module>> moduleMap = [:]

    MapModuleRetriever() {
        moduleMap << ["apkNameGenerator": ApkNameGeneratorModule.class]
        moduleMap << ["apkPublisher": ApkPublisherModule.class]
        moduleMap << ["autoOrientation": AutoOrientationModule.class]
        moduleMap << ["proguardZipGenerator": ProguardZipGeneratorModule.class]
        moduleMap << ["termIconDownloader": TermIconDownloaderModule.class]
    }

    @Override
    Class<? extends Module>[] allModuleClasses() {
        return moduleMap.values()
    }

    @Override
    Class<? extends Module> moduleClassByName(String name) {
        return moduleMap[name]
    }
}