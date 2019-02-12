package com.laser.gradle.core.module

/**
 * Used to specify routines and behavior of a part of an application.
 */
interface Module {

    /**
     * Called when the module must initialize its components.
     * This method must be called only once.
     */
    void attach()
}