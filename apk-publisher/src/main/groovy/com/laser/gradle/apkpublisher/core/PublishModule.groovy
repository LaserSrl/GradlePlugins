package com.laser.gradle.apkpublisher.core

import org.gradle.api.Project

/**
 * Module for the publication.
 * A module must specify a target class, that will be used from the user for specify the params for the publication.
 * This can specify a default targets that will be inserted always
 * the extension name that will be used as name for the extension of the {@link PublishModuleExtension}
 */
interface PublishModule {

    /**
     * will be used from the user for specify the params for the publication.
     * @return
     */
    Class<PublishTarget> targetClass()

    /**
     * default targets that will be always inserted in the module extension
     * @param project
     * @return
     */
    List<PublishTarget> defaultTargets(Project project)

    /**
     * extension name used as name for the extension of the {@link PublishModuleExtension}
     * @return
     */
    String extensionName()
}