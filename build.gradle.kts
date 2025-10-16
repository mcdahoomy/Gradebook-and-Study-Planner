plugins {
    id("application") // enables 'run' task and application config
}

// Coordinates (useful if you ever publish artifacts)
group = "com.dahoomy"
version = "0.1.0"

repositories {
    // Where Gradle should download libraries from
    mavenCentral()
}

java {
    // Ensure we compile and target Java 21
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

application {
    // Fully-qualified name of your main() class
    mainClass.set("com.dahoomy.gradebook.Main")
}

dependencies {
    // JUnit 5 BOM (keeps JUnit modules on the same version)
    testImplementation(platform("org.junit:junit-bom:5.10.2"))
    // JUnit 5 Jupiter API + engine
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    // Tell Gradle to use JUnit 5 platform
    useJUnitPlatform()
}
