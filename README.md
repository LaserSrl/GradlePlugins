# GradlePlugins
Laser gradle plugins

# Gradle #

The developed features are used across all the Android applications, both for Krake apps and ad-hoc apps, but some of them are useful only for Krake projects.

## Components ##

- [Krake](#Krake): used in all Krake projects
- [TermIconDownloader](#TermIconDownloader): downloads all the icons of the TermPart contents returned by the WS
- [AutoOrientation](#AutoOrientation): adds a common value to the orientation in the manifest
- [ApkNameGenerator](#ApkNameGenerator): generates a name of the APK that is related to variants
- [ProguardZipGenerator](#ProguardZipGenerator): generates a zip containing the output files of Proguard
- [ApkPublisher](#ApkPublisher): publishes an APK in the Play Store

## Krake ##

This component is used to manage the modules used in a Krake project.
It's only a manager that will attach external modules to the project.

It can be used as a standalone plugin with the following configurations:
- *classpath*: **com.github.LaserSrl.GradlePlugins:krake:TAG**
- *name*: **krake**

It can be configured using the extension named **krake**.
The default values are:

```
krake {
    // Specifies the aliases of the modules that must be attached to the project.
    // t can be also used as vararg, so, for example: "apkNameGenerator", "apkPublisher"
    modules []
}
```

The supported modules are:
- [TermIconDownloader](#TermIconDownloader): downloads all the icons of the TermPart contents returned by the WS
- [AutoOrientation](#AutoOrientation): adds a common value to the orientation in the manifest
- [ApkNameGenerator](#ApkNameGenerator): generates a name of the APK that is related to variants
- [ProguardZipGenerator](#ProguardZipGenerator): generates a zip containing the output files of Proguard
- [ApkPublisher](#ApkPublisher): publishes an APK in the Play Store


The extensions of the modules must be configured inside the **krake** extension.


## TermIconDownloader ##

This component is used to download all the icons related to TermPart items returned by the WS.

**It can be used only as a module of the plugin krake, not as a standalone plugin**

It can be configured using the extension named **termIconDownloader**.
The default values are:

```
termIconDownloader {
    // Specifies the base url used to download the icons of the TermPart items.
    baseUrl null
    // Specifies the base name of the icons of the TermPart items.
    partialIconName "termicon_"
}
```

The property **baseUrl** must be set, otherwise, an exception will be thrown.

Considering these placeholders:
- *baseName*: the name taken from the value of **partialIconName**
- *id*: the id of the MediaPart that represents the icon of the TermPart
- *extension*: the extension of the icon file mapped from the mime-type returned by the WS (the supported mime-types are image/png, image/jpeg and image/bmp

The icons will be downloaded in //$projectDir/src/main/res/drawable// folder with a name that follows this pattern:
```
$baseName$id.$extension
```

The download will start automatically in the pre build phase. To avoid unnecessary requests to the WS, the icons are cached for 3 days. The cache is managed through a json file with the path //$projectDir/termIconDownloader/term-icon-downloader.json//.

The developer can force a request to the WS (invalidating the cache) using the task **downloadTermIconsForced**.


## AutoOrientation ##

This component is used to add the value **@integer/activity_orientation** to the attribute named **screenOrientation** in all Activity nodes contained in the **AndroidManifest.xml** file.

**It can be used only as a module of the plugin krake, not as a standalone plugin**

It can be configured using the extension named **autoOrientation**.
The default values are:

```
autoOrientation {
    // Specifies the names of the activities that must be excluded from this task.
    // It can be also used as vararg, so, for example: ".FirstActivity", ".example.SecondActivity"
    exclude []
}
```

The value **@integer/activity_orientation** won't be set in two cases:
- The name of the Activity is specified in *excludedActivities*
- The value of **screenOrientation** is defined.


## ApkNameGenerator ##

This component is used to generate the name of the APK related to variants.

It can be used as a standalone plugin with the following configurations:
  * *classpath*: **com.github.LaserSrl.GradlePlugins:apk-name-generator:TAG**
  * *name*: **apk-name-generator**

It can be configured using the extension named **apkNameGenerator**.
The default values are:

```
apkNameGenerator {
    // Specifies if the name of the APK must include the name of the variant.
    includeVariantName true
    // Specifies if the name of the APK must include the version code.
    includeVersionCode false
    // Specifies if the name of the APK must include the version name.
    includeVersionName true
}
```

The base name of the apk is taken from the Android property **archivesBaseName**. If the value related to this property is null, the default name is *app*.

Considering these placeholders:
- *baseName*: the base name of the apk
- *variantName*: the name of the variant (only if included in the extension)
- *versionName*: the name of the version (only if included in the extension)
- *versionCode*: the version code (only if included in the extension)

The name will be generated following this pattern:
```
$baseName-$variantName-$versionName-$versionCode.apk
```


## ProguardZipGenerator ##

This component is used to generate the zip file containing the Proguard output files.

It can be used as a standalone plugin with the following configurations:
- *classpath*: **com.github.LaserSrl.GradlePlugins:proguard-zip-generator:TAG**
- *name*: **proguard-zip-generator**

It can be configured using the extension named **proguardZipGenerator**.
The default values are:

```
proguardZipGenerator {
    // Specifies the destination path in which the zip will be created.
    destinationPath "$projectDir/proguardZip"
    // Specifies if the zip filename must contain the version code.
    saveVersionCode false
    // Specifies if the zip must contain the dump.txt file.
    includeDump false
    // Specifies if the zip must contain the mapping.txt file.
    includeMapping true
    // Specifies if the zip must contain the resources.txt file.
    includeResources true
    // Specifies if the zip must contain the seeds.txt file.
    includeSeeds true
    // Specifies if the zip must contain the usage.txt file.
    includeUsage true
}
```

Considering these placeholders:
- *variantName*: the name of the variant
- *versionName*: the name of the version
- *versionCode*: the version code (only if included in the extension)

The name of the zip file will be generated following this pattern:
```
proguard-$variantName-$versionName-$versionCode.zip
```


## ApkPublisher ##
This component is used to publish an Apk file directly in the store.

It can be used as a standalone plugin with the following configurations:
- *classpath*: **com.github.LaserSrl.GradlePlugins:apk-publisher:TAG**
- *name*: **apk-publisher**

It can be configured using the extension named **apkPublisher**.

The steps are:
- check if the version can be published with the comparison of the infos in the file in the path versionsFilePath generated in the last publish
- if the check passes, publish the apk based on the publish module.
- upload the changeLogs only if the flag **publishChangeLog** is true: the script take the strings in the file specified in the folder: 'publishApk/changeLogs/'$PATH where $PATH is configurable with the param **versionChangeLogPath** in the target.
- if the publish passes, write in a file in versionsFilePath the versionName and versionCode used for this publish

Publish Module supported:
- [PlayStore Module](#PlayStoreModule): PlayStoreModule
- [Smb Module](#SmbModule): SmbModule
- [Custom publish module](#CustomPublishModule): ..or you can create a custom module

The task generated will be in this format: publishApk + FlavorName + BuildType
for example: 
- publishApkProdRelease 
- publishApkStoreRelease

the task will be generated only for the targets that have the propriety publishTarget configured
and only for the buildType 'release'.

The default values are:
```
apkPublisher {
	//specifies the path of the file in which the plugin will write the versionName and versionCode when 
    //the publish is terminated
    versionsFilePath "config.json"

	configs {
		playPublish {
			//targets that a flavour can specify in order to customize the publish
		    targets {
		        //will perform the publish in alpha target
		        //the changeLogs are taken from 'publishApk/changeLogs/alpha'
		        alpha {
		            //specifies the local path of the json file used from the plugin for the upload with the google api 
    			    keyFilePath PUBLISH_JSON_FILE
		            version = 'ALPHA'
		            publishChangeLog = true
		            versionChangeLogPath = 'alpha'
		        }
		        //will perform the publish in beta target and un track the last alpha version if exist
		        //the changeLogs are taken from 'publishApk/changeLogs/beta'
		        beta {
		            //specifies the local path of the json file used from the plugin for the upload with the google api 
    			    keyFilePath PUBLISH_JSON_FILE
		            version = 'BETA'
		            unTrackOld = true
		            publishChangeLog = true
		            versionChangeLogPath = 'beta'
		        }
		        //will perform the publish in production target and un track the last alpha or beta version if 
		        //exist
		        //the changeLogs are taken from 'publishApk/changeLogs/production'
		        production {
		            //specifies the local path of the json file used from the plugin for the upload with the google api 
    			    keyFilePath PUBLISH_JSON_FILE
		            version = 'PRODUCTION'
		            unTrackOld = true
		            publishChangeLog = true
		            versionChangeLogPath = 'production'
		        }
		    }
		}
	}
}
```

The developer must specify for each target the version that will use, for example:
```
productFlavors {
    prod {
       ext.publishTarget = apkPublisher.configs.apkPublisher.targets.beta
    }
    store {
       ext.publishTarget = apkPublisher.configs.apkPublisher.targets.production
    }
}
```

So, if there aren't custom configurations, the only thing that a developer must do are:
- specify the publishTarget for each flavor.
- ask to an administrator the json file for the publish and set the **ABSOLUTE** path in a global property named 'PUBLISH_JSON_FILE'

**Format of the changelog.json**
```
{
    "default":"test default", 
    "it-IT":"test it changelog"
}
```

the script download the languages of the application configured in the store and then apply the default value only to languages that aren't specified.

NOTE:
- for each publish the user must set a different versionName and a greater versionCode from the last used.

### Publish Modules ###

#### PlayStoreModule ####
Publish module for play store.

extension name: **playPublish**

Target params:
```
keyFilePath
type: String
path of the json key file for the publication of the apk with the Google publish api
```
```
track
type: VersionTrack
Version Track that will be used for set the correct track for the publication possible values: ALPHA, BETA, PRODUCTION
```
```
unTrackOld
type: Boolean
The Google Play Developer API does not allow us to publish a beta version if there is an alpha version with a lower version code. If you want to publish to higher track and automatically disable conflicting APKs from a lower track, this can be specified by setting this property to true
```

default targets:
```
//will perform the publish in alpha target the changeLogs are taken from 'publishApk/changeLogs/alpha'
alpha {
    keyFilePath PUBLISH_JSON_FILE
    version = 'ALPHA'
    publishChangeLog = true
    versionChangeLogPath = 'alpha'
}

//will perform the publish in beta target and un track the last alpha version if exist
//the changeLogs are taken from 'publishApk/changeLogs/beta'
beta {
    keyFilePath PUBLISH_JSON_FILE
    version = 'BETA'
    unTrackOld = true
    publishChangeLog = true
    versionChangeLogPath = 'beta'
}

//will perform the publish in production target and un track the last alpha or beta version if 
//exist
//the changeLogs are taken from 'publishApk/changeLogs/production'
production {
    keyFilePath PUBLISH_JSON_FILE
    version = 'PRODUCTION'
    unTrackOld = true
    publishChangeLog = true
    versionChangeLogPath = 'production'
}
```

#### SmbModule ####
Publish module compatible with smb protocol.

extension name: **smb**

Target params:

```
domain
type: String
Domain used for authentication

username 
type: String 
Username used for authentication

password 
type: String 
Password used for authentication

destinationPath 
type: String 
destination in which the apk will be copied:
for example: 127.0.0.1/d$/test/publishFolder

apkName 
type: String 
Name of the apk copied in the destination, if is null then will used the apk original name.

apkName 
type: String 
Name of the apk copied in the destination, if is null then will used the apk original name.

backupFile 
type: Boolean 
possibility to create the copy of the existent apk in the same folder with bck_ as prefix

jsonFilePath 
type: String 
the json format is: {"v":versionCode,"u":downloadApkUrl} path in which the json for the version will be created @default destinationPath.

jsonFileName 
type: String 
Name of the json file. @default "version.json".

downloadApkUrl 
type: String 
Url that will be set in the json.
```

default targets: **nothing**

Steps:
- Create a backup of the file if exists and if the BackupFile flag is true.
- Copy the new file in the destinationPath with the apkName specified (if null the original name)
- Create the json with the format: {"v":versionCode,"u":downloadApkUrl}

example of target
```
server1 {
    destinationPath = "127.0.0.1/D\$/www/testPublish"
    username = testUsername
    password = testPassword
    backupFile = true
    apkName = "testapk.apk"
    jsonFileName = "test-apk-json.json"
    downloadApkUrl = "https://testurl.com/testPublish/testapk.apk"
}
```

#### CustomPublishModule ####
you can create your own custom publish module and the use it with the apk publisher plugin.
you can override from an existing one for modify the logic or create a new one that implements
PublishModule.

You have to create a folder named buildSrc, see https://docs.gradle.org/current/userguide/custom_plugins.html

TestModule code example
```
class SmbModule implements PublishModule {
    @Override
    Class<PublishTarget> targetClass() {
        return TestVariant
    }

    @Override
    List<PublishTarget> defaultTargets(Project project) {
        return null
    }

    @Override
    String extensionName() {
        return "testMod"
    }
}
```

TestTarget code example
```
class TestTarget extends PublishTarget {

    TestTarget(String name, Project project) {
        super(name, project)
    }

    String testParam1 = ""

    Boolean testBool

    @Override
    boolean canPublish() {
        return true
    }

    /**
     * publication with smb protocol.
     */
    @Override
    void publish(PublishParams params) {
        //TODO
    }
}
```

declaration in build.gradle
```
apkPublisher {
    modules = "TestModule"

    configs {
        testMod {
            targets {
                testTarget {
                    testParam1 = "pippo"
                    testBool = true
                }
                testTarget2 {
                    testParam1 = "pluto"
                    testBool = false
                }
            }
        }
    }
} 
```
