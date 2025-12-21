plugins {
    `java-test-fixtures`
}

dependencies {
    testFixturesImplementation(kotlin("stdlib-jdk8"))

    testFixturesImplementation(project(":api:api-common"))
    testFixturesImplementation(project(":api:api-server"))
}
