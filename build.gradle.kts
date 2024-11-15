plugins {
    id("org.jetbrains.kotlin.jvm") version "2.0.21"
    kotlin("plugin.serialization") version "2.0.21"
    `java-library`
    `maven-publish`
    id("io.github.gradle-nexus.publish-plugin") version "2.0.0"
    signing
}

repositories {
    mavenCentral()
    maven { url = uri("https://maven.pkg.jetbrains.space/public/p/ktor/eap") }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    implementation(platform("com.google.cloud:libraries-bom:26.50.0"))
    implementation("com.google.cloud:google-cloud-datastore")
    testImplementation("io.kotest:kotest-runner-junit5:5.9.1")
    testImplementation("io.kotest:kotest-assertions-core:5.9.1")
    testImplementation("io.kotest:kotest-framework-datatest:5.9.1")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(11)
}

version = "0.2.0"
group = "com.vexdev"

java {
    withSourcesJar()
    withJavadocJar()
}

signing {
    val signingKey = providers
        .environmentVariable("GPG_SIGNING_KEY")
    val signingPassphrase = providers
        .environmentVariable("GPG_SIGNING_PASSPHRASE")
    if (signingKey.isPresent && signingPassphrase.isPresent) {
        useInMemoryPgpKeys(signingKey.get(), signingPassphrase.get())
        val extension = extensions
            .getByName("publishing") as PublishingExtension
        sign(extension.publications)
    }
}

object Meta {
    const val desc = "Kotlin Serializer for the Google Cloud Datastore"
    const val license = "Apache-2.0"
    const val githubRepo = "vexdev/kotlin-datastore-serializer"
}
publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()
            from(components["kotlin"])
            artifact(tasks["sourcesJar"])
            artifact(tasks["javadocJar"])
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
    }
}

nexusPublishing {
    repositories {
        sonatype()
    }
}