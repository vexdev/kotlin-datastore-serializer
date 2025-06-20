import com.vanniktech.maven.publish.SonatypeHost

plugins {
    id("com.vanniktech.maven.publish") version "0.32.0"
    id("org.jetbrains.kotlin.jvm") version "2.1.21"
    kotlin("plugin.serialization") version "2.1.21"
    `java-library`
}

repositories {
    mavenCentral()
    maven { url = uri("https://maven.pkg.jetbrains.space/public/p/ktor/eap") }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")
    implementation(platform("com.google.cloud:libraries-bom:26.62.0"))
    implementation("com.google.cloud:google-cloud-datastore")
    testImplementation("io.kotest:kotest-runner-junit5:5.9.1")
    testImplementation("io.kotest:kotest-assertions-core:5.9.1")
    testImplementation("io.kotest:kotest-framework-datatest:5.9.1")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}

version = "xxx.xxx.xxx" // Replace with the correct version
group = "com.vexdev"

object Meta {
    const val desc = "Kotlin Serializer for the Google Cloud Datastore"
    const val license = "Apache-2.0"
    const val githubRepo = "vexdev/kotlin-datastore-serializer"
}
mavenPublishing {
    publishToMavenCentral(SonatypeHost.DEFAULT)
    signAllPublications()

    coordinates(
        groupId = group as String,
        artifactId = name,
        version = version as String
    )

    pom {
        name.set(project.name)
        description.set(Meta.desc)
        url.set("https://github.com/${Meta.githubRepo}")
        licenses {
            license {
                name.set(Meta.license)
                url.set("https://opensource.org/licenses/Apache-2.0")
            }
        }
        developers {
            developer {
                id.set("vexdev")
                name.set("Luca Vitucci")
            }
        }
        scm {
            url.set(
                "https://github.com/${Meta.githubRepo}.git"
            )
            connection.set(
                "scm:git:git://github.com/${Meta.githubRepo}.git"
            )
            developerConnection.set(
                "scm:git:git://github.com/${Meta.githubRepo}.git"
            )
        }
        issueManagement {
            url.set("https://github.com/${Meta.githubRepo}/issues")
        }
    }
}