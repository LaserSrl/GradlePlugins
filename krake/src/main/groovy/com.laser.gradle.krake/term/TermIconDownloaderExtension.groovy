package com.laser.gradle.krake.term

/**
 * Extension named "termIconDownloader" that defines plugin's configurations
 */
class TermIconDownloaderExtension {
    String baseUrl
    String partialIconName

    /**
     * Constructor that initializes default values
     */
    TermIconDownloaderExtension() {
        partialIconName = "termicon_"
    }
}
