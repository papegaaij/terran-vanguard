// Shared Java setup for every module: Java 21, warnings as errors, JUnit 5, enforced formatting.
plugins {
    java
    id("com.diffplug.spotless")
}

val libs = the<VersionCatalogsExtension>().named("libs")

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

dependencies {
    testImplementation(platform(libs.findLibrary("junit-bom").get()))
    testImplementation(libs.findLibrary("junit-jupiter").get())
    testRuntimeOnly(libs.findLibrary("junit-launcher").get())
}

tasks.withType<JavaCompile>().configureEach {
    options.release = 21
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf("-Xlint:all", "-Werror"))
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    // The test classes spread over forked JVMs, half the cores (step BT1, 2026-10-18: the full check went from 50 to
    // 12 minutes on 20 cores). Forks rather than JUnit's in-JVM threads: concurrent flights change what the JIT
    // compiles, and the allocation tests (Allocations) then see allocations that escape analysis removes otherwise.
    maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).coerceAtLeast(1)
    // Each fork holds the loaded content and its flights; Gradle's default is 512 MB.
    maxHeapSize = "1g"
    testLogging {
        events("failed", "skipped")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}

spotless {
    java {
        // palantir-java-format wraps at 120 columns.
        palantirJavaFormat(libs.findVersion("palantir-java-format").get().requiredVersion)
        formatAnnotations()
    }
}
