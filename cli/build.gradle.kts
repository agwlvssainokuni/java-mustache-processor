plugins {
    application
}

dependencies {
    implementation(project(":core"))

    testImplementation(platform("org.junit:junit-bom:5.11.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

application {
    // Unit 2 (CLI Tool) の Functional Design / Code Generation で確定する
    mainClass.set("cherry.mustache.cli.Main")
}
