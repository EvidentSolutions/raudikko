import java.net.URI

plugins {
    `java-library`
    `maven-publish`
    signing
}

group = "fi.evident.raudikko"
version = "0.1.4"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.jetbrains:annotations:20.1.0")

    testCompileOnly("org.jetbrains:annotations:20.1.0")
    testImplementation("org.puimula.voikko:libvoikko:4.1.1")
    testImplementation(platform("org.junit:junit-bom:5.7.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

java {
    withJavadocJar()
    withSourcesJar()

    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
        vendor.set(JvmVendorSpec.ADOPTOPENJDK)
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<Javadoc> {
    options.encoding = "UTF-8"
}

tasks.test {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("raudikko") {
            from(components["java"])

            pom {
                name.set("Raudikko")
                description.set("Morphological analysis for Finnish")
                url.set("https://github.com/evidentsolutions/raudikko")
                licenses {
                    license {
                        name.set("GNU General Public License, version 3")
                        url.set("https://opensource.org/licenses/GPL-3.0")
                    }
                }
                developers {
                    developer {
                        id.set("komu")
                        name.set("Juha Komulainen")
                    }
                }
                scm {
                    connection.set("scm:git:git@github.com:evidentsolutions/raudikko.git")
                    developerConnection.set("scm:git:git@github.com:evidentsolutions/raudikko.git")
                    url.set("https://github.com/evidentsolutions/raudikko")
                }
            }
        }
    }

    if (hasProperty("sonatypeUsername")) {
        repositories {
            maven {
                name = "sonatype"

                url = if (version.toString().endsWith("-SNAPSHOT"))
                    URI("https://oss.sonatype.org/content/repositories/snapshots/")
                else
                    URI("https://oss.sonatype.org/service/local/staging/deploy/maven2/")

                credentials {
                    username = property("sonatypeUsername") as String
                    password = property("sonatypePassword") as String
                }
            }
        }
    }
}

signing {
    sign(publishing.publications["raudikko"])
}
