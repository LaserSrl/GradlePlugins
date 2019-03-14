package com.laser.gradle.apkpublisher.action.store

import com.laser.gradle.apkpublisher.core.PublishModule
import com.laser.gradle.apkpublisher.core.PublishTarget
import org.gradle.api.Project

/**
 * {@link PublishModule} for the publication on the PlayStore.
 */
class PlayStoreModule implements PublishModule {
    private static final def PUBLISH_FILE_PROP_NAME = "PUBLISH_JSON_FILE"

    @Override
    Class<PublishTarget> targetClass() {
        return StoreVariant
    }

    @Override
    String extensionName() {
        return "playPublish"
    }

    @Override
    List<PublishTarget> defaultTargets(Project project) {
        /*
        * By default:
        * - if the PUBLISH_FILE_PROP_NAME is configured in client, then set the keyFilePath (the json for the publication)
        * with its value.
        * - create 3 publishTarget: alpha, beta, production
        */

        def keyFilePath = ""
        if (project.hasProperty(PUBLISH_FILE_PROP_NAME)) {
            keyFilePath = project.property(PUBLISH_FILE_PROP_NAME)
        }

        List<PublishTarget> initialVariants = new ArrayList<>()

        StoreVariant alpha = new StoreVariant("alpha", project)
        alpha.track = VersionTrack.ALPHA
        alpha.keyFilePath = keyFilePath

        StoreVariant beta = new StoreVariant("beta", project)
        beta.track = VersionTrack.BETA
        beta.keyFilePath = keyFilePath

        StoreVariant production = new StoreVariant("production", project)
        production.track = VersionTrack.PRODUCTION
        production.keyFilePath = keyFilePath

        initialVariants.addAll(alpha, beta, production)
        return initialVariants
    }
}