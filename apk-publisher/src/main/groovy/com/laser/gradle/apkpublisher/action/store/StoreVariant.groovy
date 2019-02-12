package com.laser.gradle.apkpublisher.action.store

import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.client.http.FileContent
import com.google.api.services.androidpublisher.AndroidPublisher
import com.google.api.services.androidpublisher.AndroidPublisher.Edits
import com.google.api.services.androidpublisher.model.LocalizedText
import com.google.api.services.androidpublisher.model.Track
import com.google.api.services.androidpublisher.model.TrackRelease
import com.laser.gradle.apkpublisher.core.PublishParams
import com.laser.gradle.apkpublisher.core.PublishTarget
import org.gradle.api.GradleException
import org.gradle.api.Project

/**
 * {@link PublishTarget} of the {@link PlayStoreModule}
 */
class StoreVariant extends PublishTarget {
    private static MIME_TYPE_APK = 'application/vnd.android.package-archive'
    private static CHANGE_LOGS_LENGTH_LIMIT = 500
    private AndroidPublisher service
    private Object variant
    private Map<String, String> versionChangeLogs

    /**
     * path of the json key file for the publication of the apk with
     * the Google publish api
     */
    String keyFilePath

    /**
     * {@link VersionTrack} that will be used for
     * set the correct track for the publication
     */
    VersionTrack track

    /**
     * The Google Play Developer API does not allow us to publish a beta version
     * if there is an alpha version with a lower version code. If you want to publish
     * to higher track and automatically disable conflicting APKs from a lower track,
     * this can be specified by setting this property to true
     */
    boolean unTrackOld = false

    StoreVariant(String name, Project project) {
        super(name, project)
    }

    @Override
    boolean canPublish(PublishParams params) {
        //check if the keyFilePath is not null
        if (keyFilePath == null || keyFilePath.trim().isEmpty())
            throw new GradleException("keyFilePath must be specified, probably the property $PUBLISH_FILE_PROP_NAME is not configured in the gradle.properties, please ask to administrators the json file for the publication and set the path in this property.")

        if (track == null)
            throw new GradleException("track must be specified.")

        //check change logs
        if (publishChangeLog) {
            params.changeLogs.each {
                if (it.value == null || it.value.trim().isEmpty())
                    throw GradleException("the changeLog file must contain only valid string, the locale '$locale' is not valid.")

                if (it.value.length() > 500)
                    throw new GradleException("for the language ${it.key} the changelog must be lower than $CHANGE_LOGS_LENGTH_LIMIT chars")
            }
        }

        return true
    }

    /**
     * publication of the apk for this variant.
     * - open a new edit
     * - publish apk
     * - push the track
     * - commit all changes
     */
    @Override
    void publish(PublishParams params) {
        this.variant = params.variant
        this.versionChangeLogs = params.changeLogs

        //create an instance of the AndroidPublisher
        if (service == null) {
            service = AndroidPublisherHelper.providePublisher(project.file(keyFilePath))
        }

        def edits = service.edits()

        //open an edit for publish the apk
        String editId = openEdit(edits)

        //publish the apk
        variant.outputs
                .each { variantOutput ->
            publishApk(edits, editId, new FileContent(MIME_TYPE_APK, variantOutput.outputFile))
        }

        //finally commit the edit
        edits.commit(variant.applicationId, editId)
                .execute()
    }

    /**
     * publication of an apk for this variant.
     * - upload the apk
     * - upload proguard file if available
     * @param edits {@link com.google.api.services.androidpublisher.AndroidPublisher.Edits} used for the upload
     * @param editId edit key in String format used for upload this apk
     * @param apkFile apk file to upload
     */
    private def publishApk(Edits edits, editId, apkFile) {
        //upload the apk
        def apk = edits.apks()
                .upload(variant.applicationId, editId, apkFile)
                .execute()

        //if the configuration has unTrackOld then untrack the old versions of the minor tracks
        if (unTrackOld && track != VersionTrack.ALPHA) {
            def untrackChannels = track == VersionTrack.BETA ? [VersionTrack.ALPHA] : [VersionTrack.ALPHA, VersionTrack.BETA]
            untrackChannels.each { channel ->
                try {
                    def trackName = channel.convertToApiTrackName()
                    def track = edits.tracks().get(variant.applicationId, editId, trackName).execute()
                    track.setVersionCodes(track.getVersionCodes().findAll {
                        it > apk.getVersionCode()
                    })

                    edits.tracks().update(variant.applicationId, editId, trackName, track).execute()
                } catch (GoogleJsonResponseException e) {
                    // Just skip if there is no version in track
                    if (e.details.getCode() != 404) {
                        throw e
                    }
                }
            }
        }

        //upload the track for the the apk uploaded with the track specified by the user
        //and set change logs
        def uploadTrack = new Track()
        uploadTrack.setTrack(track.convertToApiTrackName())
        def trackRelease = new TrackRelease()
                .setStatus("completed")
                .setVersionCodes(Collections.singletonList(apk.getVersionCode()))

        if (publishChangeLog) {
            //set the changelog for the apk
            def languagesList = edits.listings().list(variant.applicationId, editId).execute()

            def defaultText = versionChangeLogs.get("default", "").trim()

            def localizedTextList = new ArrayList<LocalizedText>()
            languagesList.getListings().each { listing ->
                def locale = listing.getLanguage()
                def newNotes = versionChangeLogs.get(locale, defaultText)

                def localizedText = new LocalizedText()
                localizedText.setLanguage(locale)
                localizedText.setText(newNotes)
                localizedTextList.add(localizedText)
            }
            trackRelease.setReleaseNotes(localizedTextList)
        }

        uploadTrack.setReleases(Collections.singletonList(trackRelease))
        edits.tracks().update(variant.applicationId, editId, track.convertToApiTrackName(), uploadTrack).execute()

        // upload proguard file if available
        if (variant.mappingFile?.exists()) {
            def fileStream = new FileContent('application/octet-stream', variant.mappingFile)
            edits.deobfuscationfiles().upload(variant.applicationId, editId, apk.getVersionCode(), 'proguard', fileStream).execute()
        }

        return apk
    }

    /**
     * open a new edit for the publication
     * @param edits {@link com.google.api.services.androidpublisher.AndroidPublisher.Edits} used for open the edit
     * @return editId key in String format
     */
    private String openEdit(edits) {
        // Create a new edit to make changes to your listing.
        def editRequest = edits.insert(variant.applicationId, null /* no content yet */)

        try {
            def edit = editRequest.execute()
            return edit.getId()
        } catch (GoogleJsonResponseException e) {

            // The very first release has to be uploaded via the web interface.
            // We add a little explanation to Google's exception.
            if (e.message != null && e.message.contains('applicationNotFound')) {
                throw new IllegalArgumentException("No application was found for the package name ${variant.applicationId}. Is this the first release for this app? The first version has to be uploaded via the web interface.", e)
            }

            // Just rethrow everything else.
            throw e
        }
    }
}