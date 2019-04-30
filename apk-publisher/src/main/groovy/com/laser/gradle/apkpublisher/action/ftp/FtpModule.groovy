package com.laser.gradle.apkpublisher.action.ftp

import com.laser.gradle.apkpublisher.core.PublishModule
import com.laser.gradle.apkpublisher.core.PublishTarget
import org.gradle.api.Project

/**
 * {@link PublishModule} used for the publication with the protocol ftp.
 */
class FtpModule implements PublishModule {
    @Override
    Class<PublishTarget> targetClass() {
        return FtpVariant
    }

    @Override
    List<PublishTarget> defaultTargets(Project project) {
        return null
    }

    @Override
    String extensionName() {
        return "ftp"
    }
}