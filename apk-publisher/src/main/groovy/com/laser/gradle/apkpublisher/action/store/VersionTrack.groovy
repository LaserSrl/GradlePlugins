package com.laser.gradle.apkpublisher.action.store

import org.gradle.api.GradleException

/**
 * enum with the possible tracks for the publication
 */
enum VersionTrack {
    ALPHA,
    BETA,
    PRODUCTION

    String convertToApiTrackName() {
        switch (this) {
            case ALPHA:
                return "alpha"
                break
            case BETA:
                return "beta"
                break
            case PRODUCTION:
                return "production"
                break
            default:
                throw GradleException("trackVersion must be ALPHA, BETA or PRODUCTION")
        }
    }
}