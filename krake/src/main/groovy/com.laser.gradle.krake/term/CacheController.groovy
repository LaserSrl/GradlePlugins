package com.laser.gradle.krake.term

import groovy.json.JsonOutput
import groovy.json.JsonSlurper

/**
 * Manager of the cache.
 * The cache is handled with a json.
 * The json is read and write for the validation
 */
class CacheController {
    static def cacheController
    PathHolder pathHolder
    JsonSlurper slurper
    def partialIconName

    /**
     * Singleton instance of this class
     * @param pathHolder PathHolder to retrieve paths
     * @param slurper json parser
     * @param partialIconName icons' name
     * @return the instance of the CacheController
     */
    static CacheController getInstance(pathHolder, slurper, partialIconName) {
        if (cacheController == null) {
            cacheController = new CacheController(pathHolder, slurper, partialIconName)
        }
        cacheController
    }

    /**
     * Default private constructor for CacheController
     * @param pathHolder PathHolder to retrieve paths
     * @param slurper json parser
     * @param partialIconName icons' name
     */
    private CacheController(pathHolder, slurper, partialIconName) {
        this.pathHolder = pathHolder
        this.slurper = slurper
        this.partialIconName = partialIconName
    }

    /**
     * Validates cache time, if the time passed is major than 3 days, or the icons were deleted
     * @return true if the cache is valid, false instead
     */
    def isCacheValid() {
        File file = new File(pathHolder.JSON_PATH)
        if (file.exists() && file.getBytes().length > 0) {
            def drawables = getDrawables()
            def jsonData = slurper.parse(file)

            if (drawables.length == 0 || jsonData.size == 0 || drawables.length != jsonData.size()) {
                return false
            }

            for (def icon : jsonData) {
                for (def drawable : getDrawables()) {
                    // remove everything handle last dot
                    def trimmedName = drawable.name.replaceAll("\\.[^.]*\$", "")
                    if (trimmedName == "$partialIconName${icon.getAt("id")}".toString()) {
                        def lastDate = icon.getAt("lastDate")
                        if (System.currentTimeMillis() > (lastDate as long) + 259200000 /* 3 days */) {
                            return false
                        }
                    } else {
                        return false
                    }
                }
            }
        } else {
            return false
        }
        true
    }

    /**
     * Writes icons specs on a json file
     * @param icons icons array with {id, lastDate}
     */
    def writeToJson(icons) {
        // creates json destination dir if not exists
        def jsonDir = new File(pathHolder.JSON_DIR)
        if (!jsonDir.exists()) {
            jsonDir.mkdir()
        }
        // creates json file if not exists
        def jsonFile = new File(pathHolder.JSON_PATH)
        if (!jsonFile.exists()) {
            jsonFile.createNewFile()
        }

        def fw = new FileWriter(jsonFile)
        fw.write(JsonOutput.toJson(icons))
        fw.flush()
        fw.close()
    }

    /**
     * Creates an array of files extracting them from drawable folder
     * @return array of files
     */
    def getDrawables() {
        def drawableDir = new File(pathHolder.DRAWABLE)
        if (!drawableDir.exists()) {
            drawableDir.mkdir()
        }
        def icons = drawableDir.listFiles(new FilenameFilter() {
            @Override
            boolean accept(File dir, String name) {
                name.contains(partialIconName as String)
            }
        })
        icons
    }

    /**
     * Delete old drawables if cache is invalid
     */
    def deleteOldDrawables() {
        getDrawables().each {
            it.delete()
        }
    }
}