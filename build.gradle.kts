import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.21"
}

group = "blaarkies"
version = "0.0.0"

repositories {
    mavenCentral()
}

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
        "lwjgl", "runtime",
        //        "lwjgl-assimp", "runtime",
        //        "lwjgl-bgfx", "runtime",
        //        "lwjgl-cuda", "",
        //        "lwjgl-egl", "",
        "lwjgl-glfw", "runtime",
        "lwjgl-jawt", "",
        "lwjgl-jemalloc", "runtime",
        //        "lwjgl-libdivide", "runtime",
        //        "lwjgl-llvm", "runtime",
        //        "lwjgl-lmdb", "runtime",
        //        "lwjgl-lz4", "runtime",
        //        "lwjgl-meow", "runtime",
        //        "lwjgl-nanovg", "runtime",
        //        "lwjgl-nfd", "runtime",
        //        "lwjgl-nuklear", "runtime",
        //        "lwjgl-odbc", "",
        //        "lwjgl-openal", "runtime",
        //        "lwjgl-opencl", "",
        "lwjgl-opengl", "runtime",
        //        "lwjgl-opengles", "runtime",
        //        "lwjgl-openvr", "runtime",
        //        "lwjgl-opus", "runtime",
        //        "lwjgl-ovr", "runtime",
        //        "lwjgl-par", "runtime",
        //        "lwjgl-remotery", "runtime",
        //        "lwjgl-rpmalloc", "runtime",
        //        "lwjgl-shaderc", "runtime",
        //        "lwjgl-sse", "runtime",
        "lwjgl-stb", "runtime"
        //        "lwjgl-tinyexr", "runtime",
        //        "lwjgl-tinyfd", "runtime",
        //        "lwjgl-tootle", "runtime",
        //        "lwjgl-vma", "runtime",
        //        "lwjgl-vulkan", "",
        //        "lwjgl-xxhash", "runtime",
        //        "lwjgl-yoga", "runtime",
        //        "lwjgl-zstd", "1)
    )
        .chunked(2)
        .forEach {
            implementation("org.lwjgl", it[0])
            if (it[1] == "runtime") {
                runtimeOnly("org.lwjgl", it[0], classifier = lwjglNatives)
            }
        }
}

sourceSets {
    main {
        resources {
            exclude("textures", "fonts", "models")
        }
    }
}

tasks {

    val javaVersion = "11" // "1.8"

    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = javaVersion
    }

    register<Zip>("zipFolder") {
        archiveFileName.set("volynov.zip")
        destinationDirectory.set(File("$buildDir/"))
        from("$buildDir/Volynov")
    }

    task("renameFolder") {
        mustRunAfter("createBat")
        doLast {
            File("$buildDir/libs").renameTo(File("$buildDir/Volynov"))
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
