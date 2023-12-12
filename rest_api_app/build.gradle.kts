plugins {
    id("bisq.java-library")
    application
}

application {
    mainClass.set("bisq.rest_api.RestApiApp")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":persistence"))
    implementation(project(":i18n"))
    implementation(project(":security"))
    implementation(project(":identity"))
    implementation(project(":account"))
    implementation(project(":offer"))
    implementation(project(":contract"))
    implementation(project(":trade"))
    implementation(project(":bonded_roles"))
    implementation(project(":settings"))
    implementation(project(":user"))
    implementation(project(":chat"))
    implementation(project(":support"))
    implementation(project(":presentation"))
    implementation(project(":bisq_easy"))
    implementation(project(":application"))

    implementation("network:network")
    implementation("wallets:electrum")
    implementation("wallets:bitcoind")

    implementation(libs.typesafe.config)
    implementation(libs.bundles.glassfish.jersey)
    implementation(libs.bundles.jackson)

    implementation(libs.swagger.jaxrs2.jakarta)
}

tasks {
    distZip {
        enabled = false
    }

    distTar {
        enabled = false
    }
}