package com.laser.gradle.krake

import com.laser.gradle.core.module.Module

/**
 * Used to retrieve the {@link Module} classes supported by {@link KrakePlugin}.
 */
interface ModuleRetriever {

    /**
     * @return all {@link Module} classes supported by {@link KrakePlugin}.
     */
    Class<? extends Module>[] allModuleClasses()

    /**
     * Get a {@link Module} class by a name or an alias.
     *
     * @param name the alias of the {@link Module}.
     * @return the class of the {@link Module} retrieved by name.
     */
    Class<? extends Module> moduleClassByName(String name)
}