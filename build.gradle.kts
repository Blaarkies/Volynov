import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.41"
}

group = "blaarkies"
version = "0.0.0"

repositories {
    mavenCentral()
}

val kotlinVersion = "1.3.10"
val lwjglVersion = "3.2.3"
val lwjglNatives = "natives-windows"

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.3")
    implementation("io.reactivex.rxjava2:rxkotlin:2.4.0")
    implementation("org.jbox2d:jbox2d-library:2.2.1.1")

    testImplementation("org.junit.jupiter:junit-jupiter:5.5.2")

    implementation(platform("org.lwjgl:lwjgl-bom:$lwjglVersion"))
    val lwjglList = listOf(
        arrayOf("lwjgl", true),
        //        arrayOf("lwjgl-assimp", true),
        //        arrayOf("lwjgl-bgfx", true),
        //        arrayOf("lwjgl-cuda", false),
        //        arrayOf("lwjgl-egl", false),
        arrayOf("lwjgl-glfw", true),
        arrayOf("lwjgl-jawt", false),
        //        arrayOf("lwjgl-jemalloc", true),
        //        arrayOf("lwjgl-libdivide", true),
        //        arrayOf("lwjgl-llvm", true),
        //        arrayOf("lwjgl-lmdb", true),
        //        arrayOf("lwjgl-lz4", true),
        //        arrayOf("lwjgl-meow", true),
        //        arrayOf("lwjgl-nanovg", true),
        //        arrayOf("lwjgl-nfd", true),
        //        arrayOf("lwjgl-nuklear", true),
        //        arrayOf("lwjgl-odbc", false),
        //        arrayOf("lwjgl-openal", true),
        //        arrayOf("lwjgl-opencl", false),
        arrayOf("lwjgl-opengl", true),
        //        arrayOf("lwjgl-opengles", true),
        //        arrayOf("lwjgl-openvr", true),
        //        arrayOf("lwjgl-opus", true),
        //        arrayOf("lwjgl-ovr", true),
        //        arrayOf("lwjgl-par", true),
        //        arrayOf("lwjgl-remotery", true),
        //        arrayOf("lwjgl-rpmalloc", true),
        //        arrayOf("lwjgl-shaderc", true),
        //        arrayOf("lwjgl-sse", true),
        arrayOf("lwjgl-stb", true)
        //        arrayOf("lwjgl-tinyexr", true),
        //        arrayOf("lwjgl-tinyfd", true),
        //        arrayOf("lwjgl-tootle", true),
        //        arrayOf("lwjgl-vma", true),
        //        arrayOf("lwjgl-vulkan", false),
        //        arrayOf("lwjgl-xxhash", true),
        //        arrayOf("lwjgl-yoga", true),
        //        arrayOf("lwjgl-zstd", true)
    )
    lwjglList.forEach { implementation("org.lwjgl", it[0].toString()) }
    lwjglList.filter { it[1] == true }
        .forEach { runtimeOnly("org.lwjgl", it[0].toString(), classifier = lwjglNatives) }
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
        kotlinOptions.jvmTarget = "11"
    }

    task("renameFolder") {
        mustRunAfter("createBat")
        doLast {
            file("$buildDir/libs").renameTo(file("$buildDir/Volynov-$version"))
        }
    }

    task("createBat") {
        mustRunAfter("uberJar")
        doLast {
            File("$buildDir/libs/run.bat")
                .writeText("""|chcp 65001
                        |java -Dfile.encoding=UTF-8 -jar ./volynov-$version-uber.jar
                        |pause
                        """.trimMargin())
        }
    }

    register<Jar>("uberJar") {
        dependsOn(configurations.runtimeClasspath)

        group = "build"
        archiveClassifier.set("uber")
        manifest { attributes["Main-Class"] = "MainKt" }

        from(sourceSets.main.get().output)
        from({
            configurations.runtimeClasspath.get()
                .filter { it.name.endsWith("jar") }
                .map { zipTree(it) }
        })
    }

    task("package") {
        group = "build"
        dependsOn("clean", "uberJar", "copyTextures", "createBat", "renameFolder")
    }
}

tasks.test {
    group = "build"
    useJUnitPlatform()
    testLogging { events("PASSED", "SKIPPED", "FAILED") }
}

tasks.register<Copy>("copyTextures") {
    from("$projectDir/src/main/resources")
    exclude("shaders")
    into("$buildDir/libs")
}
