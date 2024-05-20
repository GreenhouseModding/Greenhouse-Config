import dev.greenhouseteam.greenhouseconfig.gradle.Properties
import gradle.kotlin.dsl.accessors._8347e3f88f0262002bff02c22a2fad22.sourceSets
import gradle.kotlin.dsl.accessors._8347e3f88f0262002bff02c22a2fad22.testCompileOnly

plugins {
    id("greenhouseconfig.common")
}

configurations {
    register("commonJava") {
        isCanBeResolved = true
    }
    register("commonTestJava") {
        isCanBeResolved = true
    }
    register("commonResources") {
        isCanBeResolved = true
    }
    register("commonTestResources") {
        isCanBeResolved = true
    }
}

dependencies {
    compileOnly(project(":common")) {
        capabilities {
            requireCapability("$group:${Properties.MOD_ID}")
        }
    }
    testCompileOnly(project(":common", "commonJava"))
    testCompileOnly(project(":common", "commonTestJava"))
    testCompileOnly(project.sourceSets["main"].output)
    "commonJava"(project(":common", "commonJava"))
    "commonTestJava"(project(":common", "commonTestJava"))
    "commonResources"(project(":common", "commonResources"))
    "commonTestResources"(project(":common", "commonTestResources"))
}

tasks {
    named<JavaCompile>("compileJava").configure {
        dependsOn(configurations.getByName("commonJava"))
        source(configurations.getByName("commonJava"))
    }
    named<JavaCompile>("compileTestJava").configure {
        dependsOn(configurations.getByName("commonTestJava"))
        source(configurations.getByName("commonTestJava"))
    }
    named<ProcessResources>("processResources").configure {
        dependsOn(configurations.getByName("commonResources"))
        from(configurations.getByName("commonResources"))
        from(configurations.getByName("commonResources"))
    }
    named<ProcessResources>("processTestResources").configure {
        dependsOn(configurations.getByName("commonTestResources"))
        from(configurations.getByName("commonTestResources"))
        from(configurations.getByName("commonTestResources"))
    }
    named<Javadoc>("javadoc").configure {
        dependsOn(configurations.getByName("commonJava"))
        source(configurations.getByName("commonJava"))
    }
    named<Jar>("sourcesJar").configure {
        dependsOn(configurations.getByName("commonJava"))
        from(configurations.getByName("commonJava"))
        dependsOn(configurations.getByName("commonResources"))
        from(configurations.getByName("commonResources"))
    }
}