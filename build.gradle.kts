import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.72"
}

group = "blaarkies"
version = "0.0.0"

repositories {
    mavenCentral()
}

val kotlinVersion = "1.3.72"
val lwjglVersion = "3.2.3"
val lwjglNatives = "natives-windows"

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.3")
    implementation("io.reactivex.rxjava2:rxkotlin:2.4.0")
    implementation("org.jbox2d:jbox2d-library:2.2.1.1")
    implementation("org.joml:joml:1.9.25")

    testImplementation("org.junit.jupiter:junit-jupiter:5.5.2")
    testImplementation("io.mockk:mockk:1.10.0")

    implementation(platform("org.lwjgl:lwjgl-bom:$lwjglVersion"))
    listOf(
        "lwjgl", "1",
        //        "lwjgl-assimp", "1",
        //        "lwjgl-bgfx", "1",
        //        "lwjgl-cuda", "0",
        //        "lwjgl-egl", "0",
        "lwjgl-glfw", "1",
        "lwjgl-jawt", "0",
        "lwjgl-jemalloc", "1",
        //        "lwjgl-libdivide", "1",
        //        "lwjgl-llvm", "1",
        //        "lwjgl-lmdb", "1",
        //        "lwjgl-lz4", "1",
        //        "lwjgl-meow", "1",
        //        "lwjgl-nanovg", "1",
        //        "lwjgl-nfd", "1",
        //        "lwjgl-nuklear", "1",
        //        "lwjgl-odbc", "0",
        //        "lwjgl-openal", "1",
        //        "lwjgl-opencl", "0",
        "lwjgl-opengl", "1",
        //        "lwjgl-opengles", "1",
        //        "lwjgl-openvr", "1",
        //        "lwjgl-opus", "1",
        //        "lwjgl-ovr", "1",
        //        "lwjgl-par", "1",
        //        "lwjgl-remotery", "1",
        //        "lwjgl-rpmalloc", "1",
        //        "lwjgl-shaderc", "1",
        //        "lwjgl-sse", "1",
        "lwjgl-stb", "1"
        //        "lwjgl-tinyexr", "1",
        //        "lwjgl-tinyfd", "1",
        //        "lwjgl-tootle", "1",
        //        "lwjgl-vma", "1",
        //        "lwjgl-vulkan", "0",
        //        "lwjgl-xxhash", "1",
        //        "lwjgl-yoga", "1",
        //        "lwjgl-zstd", "1)
    )
        .chunked(2)
        .forEach {
            implementation("org.lwjgl", it[0])
            if (it[1] == "1") {
                runtimeOnly("org.lwjgl", it[0], classifier = lwjglNatives)
            }
        }
}

sourceSets {
    main {
        resources {
            exclude("textures", "fonts")
        }
    }
}

tasks {

    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }

    register<Zip>("zipFolder") {
        archiveFileName.set("volynov.zip")
        destinationDirectory.set(File("$buildDir/"))
        from("$buildDir/Volynov")
    }

    task("renameFolder") {
        mustRunAfter("createBat")
        doLast {
            file("$buildDir/libs").renameTo(file("$buildDir/Volynov"))
        }
    }

    task("createBat") {
        mustRunAfter("uberJar")
        doLast {
            File("$buildDir/libs/run.bat")
                .writeText("""|java -Dorg.lwjgl.util.Debug=true -jar .\volynov-$version-uber.jar
                              |pause """.trimMargin())
            File("$buildDir/libs/config.txt")
                .writeText("""|isDebugMode=0
                        """.trimMargin())
        }
    }

    register<Copy>("copyTextures") {
        from("$projectDir/src/main/resources")
        exclude("shaders")
        into("$buildDir/libs")
    }

    register<Jar>("uberJar") {
        dependsOn(configurations.runtimeClasspath)

        archiveClassifier.set("uber")
        manifest { attributes["Main-Class"] = "MainKt" }

        from(sourceSets.main.get().output)
        from({
            configurations.runtimeClasspath.get()
                .map { zipTree(it) }
        })
    }

    task("package") {
        group = "build"
        dependsOn("clean", "uberJar", "copyTextures", "createBat", "renameFolder", "zipFolder")
    }
}

tasks.test {
    group = "build"
    useJUnitPlatform()
    testLogging { events("PASSED", "SKIPPED", "FAILED") }
}
