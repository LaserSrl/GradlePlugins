package com.laser.gradle.apkpublisher.action.store

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.HttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.androidpublisher.AndroidPublisher
import com.google.api.services.androidpublisher.AndroidPublisherScopes

import java.nio.file.Files
import java.nio.file.Paths
import java.security.GeneralSecurityException

/**
 * Helper for the creation of a new {@link com.google.api.services.androidpublisher.AndroidPublisher}
 * for now is only supported the creation of this service with the credentials with the json key file
 */
class AndroidPublisherHelper {

    private static HttpTransport HTTP_TRANSPORT

    private static JSON_FACTORY = JacksonFactory.getDefaultInstance();

    private static final APPLICATION_NAME = 'krake-gradle-apk-publisher'

    /**
     * Create a new instance of AndroidPublisher.
     * @param jsonKeyFile File in json format with the info for the creation of the credentials
     */
    static AndroidPublisher providePublisher(File jsonKeyFile) throws IOException, GeneralSecurityException {
        // Authorization.
        newTrustedTransport()
        Credential credential = authorizeWithJsonFile(jsonKeyFile);

        // Set up and return API client.
        return new AndroidPublisher.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build()
    }

    private static Credential authorizeWithJsonFile(File jsonFile) throws IOException {
        def path = Paths.get(jsonFile.absolutePath)
        def serviceAccountStream = new ByteArrayInputStream(Files.readAllBytes(path))
        def credential = GoogleCredential.fromStream(serviceAccountStream, HTTP_TRANSPORT, JSON_FACTORY)
        return credential.createScoped(Collections.singleton(AndroidPublisherScopes.ANDROIDPUBLISHER))
    }

    private static void newTrustedTransport() throws GeneralSecurityException, IOException {
        if (HTTP_TRANSPORT == null) {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport()
        }
    }
}
