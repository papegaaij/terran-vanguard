// LWJGL3 desktop launcher, settings file and Construo packaging.
import io.github.fourlastor.construo.Target

plugins {
    id("vanguard.java-conventions")
    application
    alias(libs.plugins.construo)
}

/** The texture atlases packed by the pipeline from assets/sprites and assets/backdrop. */
val atlases by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

dependencies {
    atlases(project(":pipeline", "atlases"))
    implementation(project(":game"))
    implementation(libs.gdx.backend.lwjgl3)
    implementation(variantOf(libs.gdx.platform) { classifier("natives-desktop") })
    implementation(libs.gdx.controllers.desktop)
}

/** Generational ZGC: pauses stayed below 0.02 ms in the spike, G1 reached 2.3 ms (gate 2). */
val zgc = listOf("-XX:+UseZGC", "-XX:+ZGenerational")

application {
    mainClass = "vanguard.desktop.DesktopLauncher"
    applicationName = "terran-vanguard"
    applicationDefaultJvmArgs = zgc
}

/** The version for --version and the game (vanguard/version.properties on the class path). */
val gameVersion = project.version.toString()
val versionResource = tasks.register<WriteProperties>("versionResource") {
    destinationFile = layout.buildDirectory.file("generated/version/vanguard/version.properties")
    property("version", gameVersion)
}

tasks.processResources {
    from(versionResource) {
        into("vanguard")
    }
    // The sprite frames reach the game packed into atlases, not one by one.
    from(rootProject.layout.projectDirectory.dir("assets")) {
        exclude("sprites/**", "backdrop/**")
    }
    from(atlases) {
        into("atlas")
    }
}

tasks.test {
    systemProperty("vanguard.expectedVersion", gameVersion)
}

tasks.named<JavaExec>("run") {
    workingDir = rootProject.projectDir
    // GLFW must run on the process's first thread on macOS; the Construo launcher does this itself.
    if (System.getProperty("os.name").startsWith("Mac")) {
        jvmArgs("-XstartOnFirstThread")
    }
}

/** One jar with all dependencies: Construo bundles it with the trimmed runtime. */
val fatJar = tasks.register<Jar>("fatJar") {
    archiveClassifier = "all"
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest {
        attributes(
            "Main-Class" to application.mainClass,
            "Implementation-Title" to "Terran Vanguard",
            "Implementation-Version" to gameVersion,
        )
    }
    from(sourceSets.main.map { it.output })
    from(configurations.runtimeClasspath.map { jars -> jars.map { zipTree(it) } })
    // the mapped provider above drops the jar tasks' dependencies: a clean build needs them
    dependsOn(configurations.runtimeClasspath)
    exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA", "META-INF/INDEX.LIST", "module-info.class")
}

val temurin = "https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.12.1%2B1/OpenJDK21U-jdk"

/**
 * The player's README.txt of the bundles (desktop/bundle), with the version filled in; the bundles
 * also carry LICENSE and CREDITS.md (design/tech/release).
 */
val bundleReadme = tasks.register<Copy>("bundleReadme") {
    from("bundle/README.txt")
    into(layout.buildDirectory.dir("bundle"))
    filter<org.apache.tools.ant.filters.ReplaceTokens>("tokens" to mapOf("version" to gameVersion))
}
val rootDir = rootProject.layout.projectDirectory
val bundleDocs: Map<String, Provider<RegularFile>> = mapOf(
    "README.txt" to layout.buildDirectory.file("bundle/README.txt"),
    "LICENSE.txt" to provider { rootDir.file("LICENSE") },
    "CREDITS.md" to provider { rootDir.file("CREDITS.md") },
)
val zipFolderName = "terran-vanguard-$gameVersion"

/** terran-vanguard-<version>-<target>.zip (design/tech/release). */
fun distZipName(target: String) = "terran-vanguard-$gameVersion-$target.zip"

/** Where a macOS zip waits for its docs (see the package<Target>Docs tasks). */
fun stagedZip(target: String) = layout.buildDirectory.file("construo/stage/${distZipName(target)}")

construo {
    name = "terran-vanguard"
    humanName = "Terran Vanguard"
    jarTask = fatJar.name
    // the zips unpack into a folder of their own
    zipFolder = zipFolderName
    prePackageTasks.add(bundleReadme.name)
    roast {
        // Roast starts the JVM with ZGC and, on macOS, on the first thread (both by default);
        // on Java 21 ZGC is generational only with this flag.
        vmArgs.add("-XX:+ZGenerational")
    }
    targets {
        create<Target.Linux>("linuxX64") {
            architecture = Target.Architecture.X86_64
            jdkUrl = "${temurin}_x64_linux_hotspot_21.0.12.1_1.tar.gz"
            jdkSha256 = "ce79869e1307ed8ee1e2baa86a412b1eb5b75d10a01006d788a6f968bcfaee94"
            archiveFile = layout.buildDirectory.file("construo/dist/${distZipName(name)}")
            bundleDocs.forEach { (doc, file) -> packageFiles.put(doc, file) }
        }
        create<Target.Windows>("winX64") {
            architecture = Target.Architecture.X86_64
            jdkUrl = "${temurin}_x64_windows_hotspot_21.0.12.1_1.zip"
            jdkSha256 = "f9d6e191ab098c0d416e7d588a24420a8621cd2f4720dab2459b8b7b2d2d8b4e"
            archiveFile = layout.buildDirectory.file("construo/dist/${distZipName(name)}")
            icon = layout.projectDirectory.file("icons/terran-vanguard.ico")
            bundleDocs.forEach { (doc, file) -> packageFiles.put(doc, file) }
        }
        create<Target.MacOs>("macX64") {
            architecture = Target.Architecture.X86_64
            jdkUrl = "${temurin}_x64_mac_hotspot_21.0.12.1_1.tar.gz"
            jdkSha256 = "44db0f08196daf19a47f90d13388b0c943b67663cb537f998fe29e836fa842ce"
            identifier = "nl.terranvanguard.game"
            archiveFile = stagedZip(name)
            versionNumber = gameVersion
            buildNumber = gameVersion
            macIcon = layout.projectDirectory.file("icons/terran-vanguard.icns")
        }
        create<Target.MacOs>("macM1") {
            architecture = Target.Architecture.AARCH64
            jdkUrl = "${temurin}_aarch64_mac_hotspot_21.0.12.1_1.tar.gz"
            jdkSha256 = "3623232f33a9c3baadf304480b2535f9a3cba8a58d42ecbb438ba267315d9998"
            identifier = "nl.terranvanguard.game"
            archiveFile = stagedZip(name)
            versionNumber = gameVersion
            buildNumber = gameVersion
            macIcon = layout.projectDirectory.file("icons/terran-vanguard.icns")
        }
    }
}

/**
 * Construo puts a target's package files next to the executable, which on macOS is inside the app
 * (Contents/MacOS), out of the player's sight: the macOS zips are staged and copied into dist with
 * the files beside the app instead.
 */
for (target in listOf("macX64", "macM1")) {
    val packageTask = "package${target.replaceFirstChar { it.uppercase() }}"
    val withDocs = tasks.register<Zip>("${packageTask}Docs") {
        description = "Adds README.txt, LICENSE.txt and CREDITS.md beside the app in the $target zip."
        dependsOn(packageTask)
        from(zipTree(stagedZip(target)))
        // zipTree drops the Unix modes: the launcher and the JVM's spawn helper must stay executable
        filesMatching(listOf("**/Contents/MacOS/terran-vanguard", "**/runtime/lib/jspawnhelper")) {
            permissions { unix("rwxr-xr-x") }
        }
        for ((doc, file) in bundleDocs) {
            from(file) {
                into(zipFolderName)
                rename { doc }
            }
        }
        destinationDirectory = layout.buildDirectory.dir("construo/dist")
        archiveFileName = distZipName(target)
    }
    tasks.named(packageTask) { finalizedBy(withDocs) }
}
