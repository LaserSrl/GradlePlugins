package com.laser.gradle.apkpublisher.core

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.NamedDomainObjectFactory
import org.gradle.api.Project

/**
 * Extension that an user must implement in the build.gradle file
 * for the publication of an application
 */
class ApkPublisherExtension {

    /**
     * path of the file that will be used for the check of the publication of the apk
     * based on the last versions published
     */
    String versionsFilePath

    /**
     * configs that contains all the {@link PublishModuleExtension} for each {@link PublishModule}
     */
    NamedDomainObjectContainer<PublishModuleExtension> configs

    private Project project

    private Factory factory

    ApkPublisherExtension(Project project, List<String> modules, String versionsFilePath) {
        this.versionsFilePath = versionsFilePath
        this.project = project
        this.factory = new Factory(project)
        setModules(modules)

        this.configs = project.container(PublishModuleExtension, factory)

        factory.modules.entrySet().forEach {
            configs.add(new PublishModuleExtension(it.key, project, it.value.first, it.value.second))
        }
    }

    def modules(String... importedModules) {
        setModules(Arrays.asList(importedModules))
    }

    def setModules(List<String> modules) {
        def map = new HashMap()
        final def p = project
        modules.forEach {
            PublishModule instance = Class.forName(it).newInstance()
            map.put(instance.extensionName(), new Tuple2(instance.targetClass(), instance.defaultTargets(p)))
        }

        factory.modules.putAll(map)
    }

    def configs(Closure config) {
        this.configs.configure(config)
    }

    class Factory implements NamedDomainObjectFactory<PublishModuleExtension> {
        final Project project
        private Map<String, Tuple2<Class, List<Object>>> modules = new HashMap<>()

        Factory(Project project) {
            this.project = project
        }

        @Override
        PublishModuleExtension create(String s) {
            def module = modules.get(s)
            return new PublishModuleExtension(s, project, module.first, module.second)
        }
    }
}