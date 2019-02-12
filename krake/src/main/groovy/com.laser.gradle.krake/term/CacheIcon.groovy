package com.laser.gradle.krake.term

/**
 * Model that handles icons saved on term-icon-downloader.json
 */
class CacheIcon {
    def id
    def lastDate

    /**
     * Default constructor
     * @param id if of the icon in json file
     * @param lastDate last modified date
     * @return a new CacheIcon
     */
    def CacheIcon(id, lastDate) {
        this.id = id
        this.lastDate = lastDate
    }
}