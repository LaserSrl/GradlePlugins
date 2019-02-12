package com.laser.gradle.krake.term

import com.laser.gradle.core.extension.ExtensionConfigurator
import com.laser.gradle.core.module.BaseModule
import com.laser.gradle.core.util.TaskBuilder
import groovy.json.JsonSlurper
import org.gradle.api.InvalidUserDataException
import org.gradle.api.Project
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

/**
 * This plugin will download all taxonomies' icons from project's WS.
 * It downloads a json with terms' ids and, with a thread executor, all images are downloaded into drawable folder.
 * Two tasks are added to the project:
 * <ul>
 * <li>runIconDownload: runs automatically in prebuild phase caring to the cache</li>
 * <li>forceIconDownload: ignores the cache and forces the download</li>
 * </ul>
 */
class TermIconDownloaderModule extends BaseModule<TermIconDownloaderExtension> {

    /**
     * Holds relative paths based on Project dir
     */
    private def pathHolder

    TermIconDownloaderModule(Project project) {
        super(project)
        pathHolder = PathHolder.withProject(project)
    }
/**
 * Convenience method to map most commons image content types to extensions
 * @param contentType contentType of connection
 * @return png , jpeg or bmp if the extension can be mapped, null instead
 */
    final static def mapContentTypeToExtension(contentType) {
        final def imageContent = "image"

        def extension = null
        if (contentType == "$imageContent/png") {
            extension = "png"
        } else if (contentType == "$imageContent/jpeg") {
            extension = "jpg"
        } else if (contentType == "$imageContent/bmp") {
            extension = "bmp"
        }
        extension
    }

    final def downloadTaskFunction(project, forced) {
        // get icon name from project
        String partialIconName = extension.partialIconName
        // json parser
        def slurper = new JsonSlurper()

        def cacheController = CacheController.getInstance(pathHolder, slurper, partialIconName)
        if (forced || !cacheController.isCacheValid()) {
            // get base url from the project
            String baseUrl = extension.baseUrl
            println("Base url $baseUrl")
            if (baseUrl == null) {
                throw new InvalidUserDataException('You have to set the property "baseUrl" of the extension "termIconDownloader')
            }
            // url of icons
            def iconIdsUrl = "${baseUrl}/Terms/GetIconsIds"
            println("Trying to connect to $iconIdsUrl")
            try {
                // term ids list
                def termIconIds = new ArrayList<Integer>()
                def conn = (HttpURLConnection) new URL(iconIdsUrl).openConnection()
                // connection timeout set to 10 seconds
                conn.setConnectTimeout(10000 /* 10 seconds */)
                // if the result is 200 the process will start
                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    println("Connected successfully")
                    def br = new BufferedReader(new InputStreamReader((conn.getInputStream())))
                    def sb = new StringBuilder()
                    def output
                    while ((output = br.readLine()) != null) {
                        sb.append(output)
                    }
                    // json downloaded from url
                    def downloadedJson = sb.toString()
                    println("Downloaded json: " + downloadedJson)

                    if (downloadedJson == null || downloadedJson.length() == 0) {
                        project.logger.warn("Json null or empty, check the url: $iconIdsUrl")
                        return
                    }

                    // parse json into a string and maps it
                    def jsonData = slurper.parseText(downloadedJson)

                    if (!(jsonData instanceof ArrayList)) {
                        project.logger.warn("Json not an array, check the url: $iconIdsUrl")
                        return
                    }

                    jsonData.each {
                        termIconIds.add(it)
                    }
                    // delete old drawables
                    cacheController.deleteOldDrawables()
                    // pool executor to download images with 4 parallel threads
                    def executor = Executors.newFixedThreadPool(4)
                    def futureIcons = new ArrayList<Future<CacheIcon>>()
                    termIconIds.each {
                        def imageUrl = new URL("$baseUrl/MediaExtensions/ImageUrl/?Path=$it&Width=100&Height=100&Mode=pan")

                        def callable = new GroovyCallable<CacheIcon>() {
                            @Override
                            CacheIcon call() throws Exception {
                                println("Downloading image with id: $it")

                                // open the connection with this url
                                def imageConn = imageUrl.openConnection()
                                // set timeout to 10 seconds
                                imageConn.setConnectTimeout(10000 /* 10 seconds */)
                                // get content type
                                def contentType = imageConn.getContentType()
                                // get extension from content type
                                def extension = mapContentTypeToExtension(contentType)
                                // initialize a new CacheIcon
                                def cacheIcon = null
                                // if extension is null, the file mustn't be created
                                if (extension != null) {
                                    // get content in bytes
                                    def inputStream = imageConn.getInputStream()
                                    // create a destination stream
                                    def outStream = new ByteArrayOutputStream()

                                    def n
                                    // write the content of the inputstream to a new outstream
                                    while (-1 != (n = inputStream.read())) {
                                        outStream.write(n)
                                    }
                                    outStream.close()
                                    inputStream.close()
                                    def response = outStream.toByteArray()

                                    def imagePath = "${pathHolder.DRAWABLE}/${partialIconName}${it}.$extension"
                                    println("Generating image with path: $imagePath")
                                    def fos = new FileOutputStream(imagePath)
                                    fos.write(response)
                                    fos.close()
                                    // create a new CacheIcon with id and the current time in millis
                                    cacheIcon = new CacheIcon(it, System.currentTimeMillis())
                                }
                                cacheIcon
                            }
                        }
                        // add the thread to the pool executor
                        def futureIcon = executor.submit(callable)
                        // if the icon is null, the file wasn't created
                        futureIcons.add(futureIcon)
                    }

                    def icons = new ArrayList<CacheIcon>()

                    for (Future<CacheIcon> future : futureIcons) {
                        def icon = null
                        try {
                            icon = future.get()
                        } catch (InterruptedException | ExecutionException e) {
                            println(e)
                        }
                        // if the result icon is null, it will not be written on local json file
                        if (icon != null) {
                            icons.add(icon)
                        }
                    }
                    // the executor is closed
                    executor.shutdown()
                    // the executor will wait till all icons are downloaded
                    executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
                    // write the icons specs on the json file
                    cacheController.writeToJson(icons)
                }
            } catch (SocketTimeoutException ignored) {
                project.logger.warn("Connection timeout with url: $iconIdsUrl")
            }
        } else {
            println("The cache is valid")
        }
    }

    @Override
    ExtensionConfigurator getExtensionConfigurator(Project project) {
        new ExtensionConfigurator("termIconDownloader", TermIconDownloaderExtension)
    }

    @Override
    void attach(Project project) {

        def autoDownloadTask = new TaskBuilder(project)
                .name("downloadTermIconsAutomatically")
                .action {
            doFirst {
                downloadTaskFunction(project, false)
            }
        }.build()

        new TaskBuilder(project)
                .name("downloadTermIconsForced")
                .action {
            doFirst {
                downloadTaskFunction(project, true)
            }
        }.build()

        project.tasks.getByName("preBuild").dependsOn autoDownloadTask
    }
}