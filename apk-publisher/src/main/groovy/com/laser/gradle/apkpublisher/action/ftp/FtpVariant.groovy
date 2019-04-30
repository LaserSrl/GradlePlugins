package com.laser.gradle.apkpublisher.action.ftp

import com.laser.gradle.apkpublisher.core.PublishParams
import com.laser.gradle.apkpublisher.core.PublishTarget
import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPReply
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.apache.commons.net.ftp.FTPClient
import java.nio.charset.StandardCharsets


/**
 * {@link PublishTarget} of the {@link FtpModule}
 */
class FtpVariant extends PublishTarget {

    FtpVariant(String name, Project project) {
        super(name, project)
    }

    /**
     * server used for connection
     */
    String server

    /**
     * port used for connection
     */
    int port = 21

    /**
     * username used for authentication
     */
    String username

    /**
     * password used for authentication
     */
    String password

    /**
     * destination in which the apk will be copied:
     * for example: test/publishFolder
     */
    String destinationPath

    /**
     * name of the apk copied in the destination, if is null
     * then will be used the apk original name
     */
    String apkName

    /**
     * possibility to create the copy of the existent apk
     * in the same folder with bck_ as prefix
     */
    Boolean backupFile = false

    /**
     * the json format is: {"v":versionCode,"u":downloadApkUrl}
     * path in which the json for the version will be created
     * @default destinationPath
     */
    String jsonFilePath

    /**
     * name of the json
     */
    String jsonFileName = "version.json"

    /**
     * url that will be set in the json
     */
    String downloadApkUrl

    @Override
    boolean canPublish(PublishParams params) {
        if (destinationPath == null)
            throw new GradleException("destinationPath must not be null.")

        if (server == null || server.trim().isEmpty())
            throw new GradleException("server must not be null.")
        if (port == null)
            throw new GradleException("port must not be null.")
        if (username == null || username.trim().isEmpty())
            throw new GradleException("username must not be null.")
        if (password == null || password.trim().isEmpty())
            throw new GradleException("password must not be null.")

        //test ftp connection
        FTPClient ftp = new FTPClient()
        try {
            ftp.connect(server, port)
            ftp.login(username, password)

            // After connection attempt, you should check the reply code to verify
            // success.
            int reply = ftp.getReplyCode()

            if(!FTPReply.isPositiveCompletion(reply)) {
                throw new Exception("Error on ftp connect with reply code: $reply, reply string: ${ftp.getReplyString()}")
            }

        } catch(Exception e) {
            throw new Exception("Error during test ftp connection -> $e")
        } finally {
            if(ftp.isConnected()) {
                try {
                    ftp.disconnect()
                } catch(IOException ex) {
                    // do nothing
                }
            }
        }

        return true
    }

    /**
     * publication with ftp protocol.
     */
    @Override
    void publish(PublishParams params) {
        File apk = params.file.publishFile

        FTPClient ftp = new FTPClient()

        try {
            ftp.connect(server, port)
            ftp.login(username, password)

            // After connection attempt, you should check the reply code to verify
            // success.
            int reply = ftp.getReplyCode()

            if(!FTPReply.isPositiveCompletion(reply)) {
                ftp.disconnect()
                throw new Exception("Error on ftp connect with reply code: $reply")
            }

            ftp.setFileType(FTP.BINARY_FILE_TYPE)

            //create backup
            if (backupFile)
                backupFile(ftp, apk)

            def fileName = createFilePath(createFileName(apk))
            def stream = new FileInputStream(apk)

            if (!ftp.storeFile(fileName, stream))
                throw new Exception("Error during the file upload")

            //publish the json for this apk
            publishJson(ftp, params)

            ftp.logout()

        } catch(Exception e) {
            throw e
        } finally {
            if(ftp.isConnected()) {
                try {
                    ftp.disconnect()
                } catch(IOException ex) {
                    // do nothing
                }
            }
        }
    }

    protected def backupFile(FTPClient client, File apk) {
        def path = createFilePath(createFileName(apk))
        def files = client.listFiles(path)

        if (files.length > 0) {
            client.rename(path, createFilePath("bck_${createFileName(apk)}"))
        }
    }

    protected def createFilePath(String fileName) {
        return "$destinationPath/$fileName"
    }

    protected def createFileName(File apk) {
        return (apkName == null) ? apk.name : apkName
    }

    protected def createJson(PublishParams params) {
        "{\"v\":${params.variant.versionCode},\"u\":\"${downloadApkUrl}\"}"
    }

    protected def publishJson(FTPClient client, PublishParams params) {
        def json = createJson(params)

        if (jsonFilePath == null)
            jsonFilePath = destinationPath

        def fileName = "$jsonFilePath/$jsonFileName"
        def stream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8))

        client.storeFile(fileName, stream)
    }
}