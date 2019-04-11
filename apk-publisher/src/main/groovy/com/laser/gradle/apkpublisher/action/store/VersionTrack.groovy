package com.laser.gradle.apkpublisher.action.store

/**
 * enum with the possible tracks for the publication
 */
enum VersionTrack {
    INTERNAL,
    ALPHA,
    BETA,
    PRODUCTION

    String convertToApiTrackName() {
        switch (this) {
            case INTERNAL:
                return "internal"
                break
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