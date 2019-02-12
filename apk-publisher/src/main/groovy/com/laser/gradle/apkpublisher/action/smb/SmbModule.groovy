package com.laser.gradle.apkpublisher.action.smb

import com.laser.gradle.apkpublisher.core.PublishModule
import com.laser.gradle.apkpublisher.core.PublishTarget
import org.gradle.api.Project

/**
 * {@link PublishModule} used for the publication with the protocol smb.
 */
class SmbModule implements PublishModule {
    @Override
    Class<PublishTarget> targetClass() {
        return SmbVariant
    }

    @Override
    List<PublishTarget> defaultTargets(Project project) {
        return null
    }

    @Override
    String extensionName() {
        return "smb"
    }
}