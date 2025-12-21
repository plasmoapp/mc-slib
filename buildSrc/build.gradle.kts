plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(libs.shadow)
    implementation(libs.asm)

    implementation(libs.semver4j)
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}
