dependencies {
    api(project(":api:api-common"))

    compileOnly(libs.crowdin)
    compileOnly(libs.toml4j)
}
