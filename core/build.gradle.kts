import org.jetbrains.dokka.gradle.DokkaTask
import java.util.*

plugins {
    id("com.android.library")
    kotlin("android")
    id("org.jetbrains.dokka")
    id("maven-publish")
    id("signing")
}

object Maven {
    const val groupId = "io.dyte.mediasoup"
    const val artifactId = "libmediasoup-android"
    const val name = "libmediasoup-android"
    const val desc = "mediasoup client side library for Android"
    const val version = "0.0.1"
    const val siteUrl = "https://github.com/dyte-in/libmediasoup-android"
    const val gitUrl = "https://github.com/dyte-in/libmediasoup-android.git"
    const val githubRepo = "dyte-in/libmediasoup-android"
    const val licenseName = "The Apache Software License, Version 2.0"
    const val licenseUrl = "http://www.apache.org/licenses/LICENSE-2.0.txt"
    const val licenseDist = "repo"
}

group = Maven.groupId
version = Maven.version

android {
    buildToolsVersion = "32.0.0"
    compileSdk = 32

    defaultConfig {
        minSdk = 21
        consumerProguardFiles("consumer-proguard-rules.pro")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        externalNativeBuild {
            cmake {
                arguments += listOf(
                    "-DLIBWEBRTC_INCLUDE_PATH=${projectDir}/deps/webrtc/include",
                    "-DLIBWEBRTC_BINARY_ANDROID_PATH=${projectDir}/deps/webrtc/lib",
                    "-DLIBMEDIASOUPCLIENT_ROOT_PATH=${projectDir}/deps/libmediasoupclient",
                    "-DMEDIASOUPCLIENT_BUILD_TESTS=OFF"
                )
            }
        }

        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a", "x86_64")
        }
    }

    lint {
        textReport = true
        checkDependencies = true
    }

    libraryVariants.all {
        generateBuildConfigProvider?.configure {
            enabled = false
        }
    }

    buildTypes {
        debug {
            isJniDebuggable = true
            externalNativeBuild {
                cmake {
                    arguments += listOf(
                        "-DMEDIASOUPCLIENT_LOG_TRACE=ON",
                        "-DMEDIASOUPCLIENT_LOG_DEV=ON"
                    )
                }
            }
        }
        release {
            isJniDebuggable = false
            externalNativeBuild {
                cmake {
                    arguments += listOf(
                        "-DMEDIASOUPCLIENT_LOG_TRACE=OFF",
                        "-DMEDIASOUPCLIENT_LOG_DEV=OFF"
                    )
                }
            }
        }
    }

    compileOptions {
        sourceCompatibility(JavaVersion.VERSION_11)
        targetCompatibility(JavaVersion.VERSION_11)
    }

    kotlin {
        kotlinOptions {
            freeCompilerArgs = listOf("-module-name", "libmediasoup-android")
            jvmTarget = "11"
            apiVersion = "1.6"
            languageVersion = "1.6"
        }
    }

    externalNativeBuild {
        cmake {
            version = "3.18.1"
            path = file("${projectDir}/CMakeLists.txt")
        }
    }
    ndkVersion = "23.1.7779620"
}

dependencies {
    api(Kotlin.stdlib)
    implementation(fileTree(mapOf("dir" to "${projectDir}/deps/webrtc/lib", "include" to arrayOf("*.jar"))))

    testImplementation(Testing.junit4)
    testImplementation("com.willowtreeapps.assertk:assertk-jvm:_")
    androidTestImplementation(Testing.junit4)
    androidTestImplementation(AndroidX.test.ext.junitKtx)
    androidTestImplementation(AndroidX.test.espresso.core)
    androidTestImplementation("com.willowtreeapps.assertk:assertk-jvm:_")
}

val sourcesJar by tasks.creating(Jar::class) {
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    description = "Assembles sources JAR"
    archiveClassifier.set("sources")
    from(sourceSets.create("main").allSource)
}

val customDokkaTask by tasks.creating(DokkaTask::class) {
    dokkaSourceSets.getByName("main") {
        noAndroidSdkLink.set(false)
    }
    dependencies {
        plugins("org.jetbrains.dokka:javadoc-plugin:_")
    }
    inputs.dir("src/main/java")
    outputDirectory.set(buildDir.resolve("javadoc"))
}

val javadocJar by tasks.creating(Jar::class) {
    dependsOn(customDokkaTask)
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    description = "Assembles JavaDoc JAR"
    archiveClassifier.set("javadoc")
    from(customDokkaTask.outputDirectory)
}

// Grabbing secrets from local.properties file or from environment variables, which could be used on CI
val secretPropsFile = project.rootProject.file("local.properties")
if (secretPropsFile.exists()) {
    secretPropsFile.reader().use {
        Properties().apply {
            load(it)
        }
    }.onEach { (name, value) ->
        ext[name.toString()] = value
    }
} else {
    ext["signing.keyId"] = System.getenv("SIGNING_KEY_ID")
    ext["signing.password"] = System.getenv("SIGNING_PASSWORD")
    ext["signing.secretKeyRingFile"] = System.getenv("SIGNING_SECRET_KEY_RING_FILE")
    ext["ossrhUsername"] = System.getenv("OSSRH_USERNAME")
    ext["ossrhPassword"] = System.getenv("OSSRH_PASSWORD")
}

fun getExtraString(name: String) = ext[name]?.toString()

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("maven") {
                from(components.getByName("release"))

                groupId = Maven.groupId
                artifactId = Maven.artifactId

                println("""
                    |Creating maven publication
                    |    Group: $groupId
                    |    Artifact: $artifactId
                    |    Version: $version
                """.trimMargin())

                artifact(sourcesJar)
                artifact(javadocJar)

                pom {
                    name.set(Maven.name)
                    description.set(Maven.desc)
                    url.set(Maven.siteUrl)

                    scm {
                        val scmUrl = "scm:git:${Maven.gitUrl}"
                        connection.set(scmUrl)
                        developerConnection.set(scmUrl)
                        url.set(Maven.gitUrl)
                        tag.set("HEAD")
                    }

                    developers {
                        developer {
                            id.set("DyteINC")
                            name.set("DYTE INC")
                            email.set("dev@dyte.io")
                            roles.set(listOf("Project-Administrator", "Developer"))
                        }
                    }

                    licenses {
                        license {
                            name.set(Maven.licenseName)
                            url.set(Maven.licenseUrl)
                            distribution.set(Maven.licenseDist)
                        }
                    }
                }
            }
        }
        repositories {
            maven {
                name = "sonatype"
                setUrl("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
                credentials {
                    username = getExtraString("ossrhUsername")
                    password = getExtraString("ossrhPassword")
                }
            }
        }
    }

    signing {
        sign(publishing.publications)
    }
}

tasks {
    withType<Test> {
        useJUnitPlatform()
        testLogging {
            showStandardStreams = true
            events("passed", "skipped", "failed")
        }
    }
}
