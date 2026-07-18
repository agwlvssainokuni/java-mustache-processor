plugins {
    application
    id("com.gradleup.shadow") version "9.6.0"
    id("org.owasp.dependencycheck") version "10.0.4"
}

dependencies {
    implementation(project(":cherry-mustache-core"))
    implementation("org.slf4j:slf4j-api:2.0.16")
    // cliはライブラリではなくエンドユーザー向け実行可能ツールのため、coreと異なりSLF4Jバインディングを
    // 自身のfat jarに同梱する（呼び出し側にバインディング選定を委ねる必要が無いため）
    implementation("org.slf4j:slf4j-simple:2.0.16")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.22.1")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.22.1")

    testImplementation(platform("org.junit:junit-bom:5.11.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

application {
    mainClass.set("cherry.mustache.cli.Main")
}

dependencyCheck {
    failBuildOnCVSS = 7.0f
    suppressionFile = "dependency-check-suppressions.xml"
}
