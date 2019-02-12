package com.laser.gradle.core

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * The base Gradle plugin to use in a project.
 * It provides some common features that are useful developing a custom plugin for a project.
 */
@SuppressWarnings("GroovyUnusedDeclaration")
abstract class BasePlugin implements Plugin<Project> {
    Project project

    @Override
    final void apply(Project project) {
        this.project = project

        execute(project)
    }

    abstract void execute(Project project)

    /**
     * Load the properties from descending files' tree.
     * This method will search a file with the name passed as parameter in the project dir and in the root dir.
     * The output properties will be the merge of the two files giving a major priority to a property
     * found in the project directory.
     *
     * @param fileName name of the properties file without extension
     * @return instance of {@code Properties}
     */
    Properties loadProps(String fileName) {
        File[] files = new File[2]
        files[0] = new File(project.rootDir.path + File.separator + "${fileName}.properties")
        files[1] = new File(project.projectDir.path + File.separator + "${fileName}.properties")
        return loadProps(files)
    }

    /**
     * Load multiple files in the same {@code Properties} instance.
     *
     * @param files varargs of files to load
     * @return instance of {@code Properties}
     */
    static Properties loadProps(File... files) {
        Properties props = new Properties()
        files.each {
            if (it.exists()) {
                props.load(new FileInputStream(it))
            }
        }
        return props
    }

    /**
     * Get the value of a project property (gradle.properties file).
     *
     * @param propName name of the property
     * @return value of the property in String format or empty String if not found
     */
    String prop(String propName) {
        project.hasProperty(propName) ? project.property(propName) : ""
    }

    /**
     * Get the value of a property.
     *
     * @param properties instance of {@code Properties} after a File, or an XML is loaded into it.
     * @param propName name of the property
     * @return value of the property in String format or empty String if not found
     */
    static String prop(Properties properties, String propName) {
        properties.getProperty(propName, "")
    }

    /**
     * Apply a plugin to the project only if it wasn't applied before.
     *
     * @param pluginName name of the plugin to apply
     */
    void applyPlugin(String pluginName) {
        if (!project.plugins.hasPlugin(pluginName)) {
            project.apply plugin: pluginName
        }
    }
}