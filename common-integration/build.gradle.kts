dependencies {
    api(project(":api:api-common"))

    compileOnly(project(":common", "shadow"))

    compileOnly(libs.adventure.api)
    compileOnly(libs.adventure.gson)
    compileOnly(libs.adventure.legacy)
    compileOnly(libs.adventure.minimessage)
}