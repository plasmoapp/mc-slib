dependencies {
    compileOnly(libs.spigot)
    compileOnly(libs.folia)

    api(project(":common"))
    api(project(":api:api-server"))
}
