// The data model and the loader for the design tree's data files: no libGDX.
plugins {
    id("vanguard.java-conventions")
    `java-library`
}

dependencies {
    api(project(":sim"))
    api(platform(libs.jackson.bom))
    // The data records carry Jackson's annotations, so their users compile against them too.
    api(libs.jackson.annotations)
    implementation(libs.jackson.yaml)
    testImplementation(testFixtures(project(":sim")))
}

/**
 * The game reads its numbers from design/**/data.yaml at run time: they are copied with their paths
 * under design/ into the resources, with design/data-files.txt listing them for the loader.
 */
val designData = tasks.register<DesignData>("designData") {
    source.from(rootProject.layout.projectDirectory.dir("design").asFileTree.matching { include("**/data.yaml") })
    designDir = rootProject.layout.projectDirectory.dir("design")
    outputDir = layout.buildDirectory.dir("generated/design-data")
}

sourceSets.main {
    resources.srcDir(designData)
}

abstract class DesignData : DefaultTask() {
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val source: ConfigurableFileCollection

    @get:Internal
    abstract val designDir: DirectoryProperty

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun copy() {
        val root = designDir.get().asFile
        val target = outputDir.get().asFile.resolve("design")
        target.deleteRecursively()
        val paths = source.files.map { it.relativeTo(root).invariantSeparatorsPath }.sorted()
        for (path in paths) {
            root.resolve(path).copyTo(target.resolve(path))
        }
        target.resolve("data-files.txt").writeText(paths.joinToString("\n", postfix = "\n"))
    }
}

tasks.test {
    // The loader test reads the data files straight from the design tree as well.
    systemProperty("vanguard.designDir", rootProject.layout.projectDirectory.dir("design").asFile.absolutePath)
    // Re-recording the replay (see ReplayTest): -Dvanguard.recordDir=<dir>, relative to the root.
    providers.systemProperty("vanguard.recordDir").orNull?.let {
        systemProperty("vanguard.recordDir", rootProject.layout.projectDirectory.dir(it).asFile.absolutePath)
    }
}

tasks.test {
    // VoiceFilesTest checks that every spoken line has its rendered file in assets/voice.
    systemProperty("vanguard.assetsDir", rootProject.layout.projectDirectory.dir("assets").asFile.absolutePath)
    inputs.files(rootProject.fileTree("assets/voice")).withPropertyName("voice")
}

/**
 * A level's numbers for tuning (LevelReport, CLAUDE.md Tools): ./gradlew :content:levelReport -Plevel=19
 * [-Pdifficulty=easy,medium,hard] [-Pseeds=3] [-Pfit=test|plan|user] [-Pparts=pacing,floor,haul,boss,secondary,hp,pursuit,colossus]
 * [-Pjson]. It runs on the test classes and reads the design data at run time: a data.yaml edit needs no recompile.
 */
tasks.register<JavaExec>("levelReport") {
    group = "verification"
    description = "Prints a level's pacing, floor, haul, boss, beam-era, pursuit and colossus numbers (-Plevel=<n>)."
    classpath = sourceSets.test.get().runtimeClasspath
    mainClass = "vanguard.content.LevelReport"
    maxHeapSize = "2g"
    systemProperty("vanguard.designDir", rootProject.layout.projectDirectory.dir("design").asFile.absolutePath)
    val options = listOf("level", "difficulty", "seeds", "fit", "parts").mapNotNull { name ->
        providers.gradleProperty(name).orNull?.let { listOf("--$name", it) }
    }.flatten()
    args(options + if (providers.gradleProperty("json").isPresent) listOf("--json") else listOf())
}

/**
 * The user's playthrough save (M7 part I, the user's I.3 of round 44): ./gradlew :content:playthroughSave -Plevel=15
 * [-Pdifficulty=medium] -Pout=<settings dir> flies the playthrough test's campaign on the balance plan to that level
 * and writes it into <out>/saves/ (the autosave and slot 1); the game reads it with --settings <out>/settings.properties.
 */
tasks.register<JavaExec>("playthroughSave") {
    group = "application"
    description = "Writes the balance plan's campaign before a level into a settings dir's saves (-Plevel -Pout)."
    classpath = sourceSets.test.get().runtimeClasspath
    mainClass = "vanguard.content.campaign.PlaythroughSave"
    maxHeapSize = "2g"
    systemProperty("vanguard.designDir", rootProject.layout.projectDirectory.dir("design").asFile.absolutePath)
    val options = listOf("level", "difficulty").mapNotNull { name ->
        providers.gradleProperty(name).orNull?.let { listOf("--$name", it) }
    }.flatten()
    // A relative -Pout is taken from the directory Gradle was started in, not the module's.
    val start = gradle.startParameter.currentDir
    val out = providers.gradleProperty("out").map { path ->
        listOf("--out", File(path).let { if (it.isAbsolute) it else start.resolve(path) }.absolutePath)
    }
    args(options + out.getOrElse(listOf()))
}

/**
 * The zero-allocation tests (Allocations) once more on C1 code only (-XX:TieredStopAtLevel=1): C1 has no escape
 * analysis, so an allocation that only C2 removes (HazardPlay.channelSpan's double[2], CI on the 3-core macOS runner,
 * 2026-10-20) fails here on every machine instead of on a slow one only. Its own forks, next to `test` in `check`.
 */
val allocationTest = tasks.register<Test>("allocationTest") {
    group = "verification"
    description = "Runs the zero-allocation tests on the C1 compiler only (no escape analysis)."
    testClassesDirs = sourceSets.test.get().output.classesDirs
    classpath = sourceSets.test.get().runtimeClasspath
    filter {
        includeTestsMatching("*.steppingAWholeLevelDoesNotAllocate")
        includeTestsMatching("*.theFightAllocatesNothingPerStep")
    }
    jvmArgs("-XX:TieredStopAtLevel=1")
    systemProperty("vanguard.designDir", rootProject.layout.projectDirectory.dir("design").asFile.absolutePath)
}

tasks.check {
    dependsOn(allocationTest)
}
