dependencies {
    api(project(":api:api-common"))
    compileOnly(libs.adventure.api)
    compileOnly(libs.adventure.gson)

    compileOnly(rootProject.libs.slf4j)
}
