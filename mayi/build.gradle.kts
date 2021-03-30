import java.util.*

plugins {
    id(GradlePluginId.ANDROID_LIBRARY)
    id(GradlePluginId.KOTLIN_ANDROID)
    // Documentation for our code
    id(GradlePluginId.DOKKA) version GradlePluginVersion.DOKKA_VERSION
    // Maven publication
    `maven-publish`
    signing
}
val credentialsMap: Map<String, String> = LinkedHashMap<String, String>().apply {
    val propertiesFile = project.rootProject.file("local.properties")
    if (propertiesFile.exists() && propertiesFile.canRead()) {
        val properties = Properties()
        properties.load(propertiesFile.inputStream())
        this["signing.keyId"] = properties.getProperty("signing.keyId")
        this["signing.password"] = properties.getProperty("signing.password")
        this["signing.secretKeyRingFile"] = properties.getProperty("signing.secretKeyRingFile")
        this["ossrhUsername"] = properties.getProperty("ossrhUsername")
        this["ossrhPassword"] = properties.getProperty("ossrhPassword")
        this["sonatypeStagingProfileId"] = properties.getProperty("sonatypeStagingProfileId")
    } else {
        this["signing.keyId"] = System.getenv("signing.keyId")
        this["signing.password"] = System.getenv("signing.password")
        this["signing.secretKeyRingFile"] = System.getenv("signing.secretKeyRingFile")
        this["ossrhUsername"] = System.getenv("ossrhUsername")
        this["ossrhPassword"] = System.getenv("ossrhPassword")
        this["sonatypeStagingProfileId"] = System.getenv("sonatypeStagingProfileId")
        //val keystoreFile = project.rootProject.file(rootDir.path + File.separator + System.getenv("keystore_name"))
    }
}

android {
    compileSdkVersion(AndroidConfig.COMPILE_SDK_VERSION)
    //buildToolsVersion("29.0.3")

    defaultConfig {
        minSdkVersion(AndroidConfig.MIN_SDK_VERSION)
        targetSdkVersion(AndroidConfig.TARGET_SDK_VERSION)
        versionCode = Artifact.VERSION_CODE
        versionName = Artifact.VERSION_NAME
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {

        getByName(BuildType.DEBUG) {
            isMinifyEnabled = BuildTypeDebug.isMinifyEnabled
            isDebuggable = BuildTypeDebug.isDebuggable
        }

        getByName(BuildType.RELEASE) {
            isMinifyEnabled = BuildTypeRelease.isMinifyEnabled
            isDebuggable = BuildTypeRelease.isDebuggable
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    addLibModuleDependencies()
    addTestDependencies()
}

val dokkaTask by tasks.creating(org.jetbrains.dokka.gradle.DokkaTask::class) {
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    description = "Assembles Kotlin docs with Dokka"
    outputDirectory.set(File("$buildDir/dokka"))
    //documentationFileName.set("README.md")
}

val dokkaJar by tasks.creating(Jar::class) {
    archiveClassifier.set("dokka")
    from("$buildDir/dokka")
    dependsOn(dokkaTask)
}

val sourcesJar by tasks.creating(Jar::class) {
    archiveClassifier.set("sources")
    if (project.plugins.findPlugin("com.android.library") != null) {
        from(android.sourceSets.getByName("main").java.srcDirs)
    } else {
        from(sourceSets.getByName("main").java.srcDirs)
    }
}

artifacts {
    archives(sourcesJar)
    archives(dokkaJar)
}

publishing {
    publications {
        create<MavenPublication>(Artifact.ARTIFACT_NAME) {
            groupId = Artifact.ARTIFACT_GROUP
            artifactId = Artifact.ARTIFACT_NAME
            version = Artifact.VERSION_NAME

            if (project.plugins.findPlugin("com.android.library") != null) {
                artifact("$buildDir/outputs/aar/${project.name}-release.aar")
            } else {
                from(components["java"])
                //artifact("$buildDir/libs/${project.getName()}-${version}.jar")
            }
            artifacts {
                artifact(sourcesJar)
                artifact(dokkaJar)
            }

            pom {
                name.set(Artifact.LIBRARY_NAME)
                description.set(Artifact.POM_DESC)
                url.set(Artifact.POM_URL)
                licenses {
                    license {
                        name.set(Artifact.POM_LICENSE_NAME)
                        url.set(Artifact.POM_LICENSE_URL)
                        distribution.set(Artifact.POM_URL)
                    }
                }
                developers {
                    developer {
                        id.set(Artifact.POM_DEVELOPER_ID)
                        name.set(Artifact.POM_DEVELOPER_NAME)
                        email.set(Artifact.DEVELOPER_EMAIL)
                    }
                }
                scm {
                    connection.set(Artifact.POM_SCM_CONNECTION)
                    developerConnection.set(Artifact.POM_SCM_DEV_CONNECTION)
                    url.set(Artifact.POM_SCM_URL)
                }
                repositories {
                    maven {
                        // change URLs to point to your repos, e.g. http://my.org/repo
                        val releasesRepoUrl = uri(Artifact.RELEASE_REPO_URL)
                        val snapshotsRepoUrl = uri(Artifact.SNAPSHOT_REPO_URL)
                        url = if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
                        credentials {
                            username = credentialsMap["ossrhUsername"]
                            password = credentialsMap["ossrhPassword"]
                        }
                    }
                }

                //  hack if you wanna include any transitive dependencies. I'm a hackur indeed
                /*            withXml {
                                asNode().apply {
                                    appendNode("description", Artifact.POM_DESC)
                                    appendNode("name", Artifact.LIBRARY_NAME)
                                    appendNode("url", Artifact.POM_URL)
                                    appendNode("licenses").appendNode("license").apply {
                                        appendNode("name", Artifact.POM_LICENSE_NAME)
                                        appendNode("url", Artifact.POM_LICENSE_URL)
                                        appendNode("distribution", Artifact.POM_LICENSE_DIST)
                                    }
                                    appendNode("developers").appendNode("developer").apply {
                                        appendNode("id", Artifact.POM_DEVELOPER_ID)
                                        appendNode("name", Artifact.POM_DEVELOPER_NAME)
                                    }
                                    appendNode("scm").apply {
                                        appendNode("url", Artifact.POM_SCM_URL)
                                    }
                                }
                            }*/
            }
        }
    }
}

signing {
    sign(publishing.publications[Artifact.ARTIFACT_NAME])
}


/*
bintray {
    val properties = Properties()
    properties.load(project.rootProject.file("local.properties").inputStream())

    // Getting bintray user and key from properties file or command line
    user =
        if (properties.hasProperty("bintrayUser")) properties.getProperty("bintrayUser") as String else "thanosfisherman"
    key =
        if (properties.hasProperty("bintrayKey")) properties.getProperty("bintrayKey") as String else ""

    // Automatic publication enabled
    publish = true
    dryRun = false

    // Set maven publication onto bintray plugin
    setPublications(Artifact.ARTIFACT_NAME)

    // Configure package
    pkg.apply {
        repo = "maven"
        name = Artifact.BINTRAY_NAME
        setLicenses("Apache-2.0")
        setLabels("Kotlin", "android", "permissions", "runtime")
        vcsUrl = Artifact.POM_SCM_URL
        websiteUrl = Artifact.POM_URL
        issueTrackerUrl = Artifact.POM_ISSUE_URL
        githubRepo = Artifact.GITHUB_REPO
        githubReleaseNotesFile = Artifact.GITHUB_README

        // Configure version
        version.apply {
            name = Artifact.VERSION_NAME
            desc = Artifact.POM_DESC
            released = Date().toString()
            vcsTag = Artifact.VERSION_NAME
        }
    }
}*/
