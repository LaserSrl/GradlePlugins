package com.laser.gradle.krake.term

import org.gradle.api.Project

/**
 * Holds the project's path to retrieve relative paths like destination paths or res path
 */
class PathHolder {
    private static def pathHolder

    private Project project

    private final def basePath = project.projectDir.path
    final def RES = "$basePath/src/main/res"
    final def DRAWABLE = "$RES/drawable"
    final def JSON_DIR = "$basePath/termIconDownloader"
    final def JSON_PATH = "$JSON_DIR/term-icon-downloader.json"

    /**
     * Default private constructor that initialize PathHolder with a Project
     * @param project Project you want to use
     */
    private PathHolder(Project project) {
        this.project = project
    }

    /**
     * Singleton instance of this class
     * @param project project you want to use
     * @return PathHolder with the project passed as parameter
     */
    static PathHolder withProject(Project project) {
        if (pathHolder == null) {
            pathHolder = new PathHolder(project)
        }
        pathHolder
    }
}