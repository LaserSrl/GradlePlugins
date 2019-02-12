package com.laser.gradle.apkpublisher.action.smb

import com.laser.gradle.apkpublisher.core.PublishParams
import com.laser.gradle.apkpublisher.core.PublishTarget
import jcifs.smb.NtlmPasswordAuthentication
import jcifs.smb.SmbFile
import jcifs.smb.SmbFileOutputStream
import org.gradle.api.GradleException
import org.gradle.api.Project

/**
 * {@link PublishTarget} of the {@link SmbModule}
 */
class SmbVariant extends PublishTarget {

    SmbVariant(String name, Project project) {
        super(name, project)
    }

    /**
     * domain used for authentication
     */
    String domain = ""

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
     * for example: 127.0.0.1/d$/test/publishFolder
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

        if (username == null || username.trim().isEmpty())
            throw new GradleException("username must not be null.")
        if (password == null || password.trim().isEmpty())
            throw new GradleException("password must not be null.")

        return true
    }

    /**
     * publication with smb protocol.
     */
    @Override
    void publish(PublishParams params) {
        File apk = (File)params.variant.outputs[0].outputFile

        NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(domain, username, password)

        //create backup
        if (backupFile)
            backupFile(params, apk, auth)

        def newFile = new SmbFile("smb://" + destinationPath + "/" + createFilePathName(params, apk), auth)

        SmbFileOutputStream s = new SmbFileOutputStream(newFile)
        try {
            s.write(apk.bytes)
        } catch (Exception ex) {
            throw ex
        }
        finally {
            s.close()
        }

        //publish the json for this apk
        publishJson(params, auth)
    }

    protected def backupFile(PublishParams params, File apk, NtlmPasswordAuthentication auth) {
        def existentFile = new SmbFile("smb://" + destinationPath + "/" + createFilePathName(params, apk), auth)
        if (existentFile.exists()) {
            def newFile = new SmbFile("smb://" + destinationPath + "/bck_" + createFilePathName(params, apk), auth)
            existentFile.copyTo(newFile)
        }
    }

    protected def createFilePathName(PublishParams params, File apk) {
        return (apkName == null) ? apk.name : apkName
    }

    protected def createJson(PublishParams params) {
        "{\"v\":${params.variant.versionCode},\"u\":\"${downloadApkUrl}\"}"
    }

    protected def publishJson(PublishParams params, NtlmPasswordAuthentication auth) {
        def json = createJson(params)

        if (jsonFilePath == null)
            jsonFilePath = destinationPath

        def newFile = new SmbFile("smb://" + jsonFilePath + "/" + jsonFileName, auth)

        SmbFileOutputStream s = new SmbFileOutputStream(newFile)
        try {
            s.write(json.bytes)
        } finally {
            s.close()
        }
    }
}