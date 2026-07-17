plugins {
    `java-library`
    id("org.owasp.dependencycheck") version "10.0.4"
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.11.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("net.jqwik:jqwik:1.9.1")
    testImplementation("org.yaml:snakeyaml:2.3")
}

dependencyCheck {
    failBuildOnCVSS = 7.0f
    suppressionFile = "dependency-check-suppressions.xml"
}
