dependencies {
    api(project(":api:api-common"))

    compileOnly(libs.adventure.api)
    compileOnly(libs.adventure.gson)
    compileOnly(libs.adventure.legacy)
}