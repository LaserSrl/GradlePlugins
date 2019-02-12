package com.laser.gradle.apkpublisher.core

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.NamedDomainObjectFactory
import org.gradle.api.Project

/**
 * extension that will be created for each {@link PublishModule}
 */
final class PublishModuleExtension {
    String name

    /**
     * list of {@link PublishTarget} of the type specified from the {@link PublishModule}
     */
    NamedDomainObjectContainer<PublishTarget> targets

    PublishModuleExtension(String name, Project project, Class clazz, List<PublishTarget> initialVariants) {
        this.name = name
        targets = project.container(PublishTarget, new Factory(clazz, project, initialVariants))

        if (initialVariants != null)
            targets.addAll(initialVariants)
    }

    def targets(Closure closure) {
        this.targets.configure(closure)
    }

    class Factory implements NamedDomainObjectFactory<PublishTarget> {
        Class clazz
        Project project
        List<PublishTarget> initialVariants

        Factory(Class clazz, Project project, List<PublishTarget> initialVariants) {
            this.clazz = clazz
            this.project = project
            this.initialVariants = initialVariants
        }

        @Override
        PublishTarget create(String s) {
            initialVariants?.forEach {
                if (it.name == s) {
                    return it
                }
            }
            return (PublishTarget)clazz.getConstructor(String, Project).newInstance(s, project)
        }
    }
}
