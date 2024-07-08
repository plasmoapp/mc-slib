plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(libs.shadow)
    implementation(libs.asm)
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}
