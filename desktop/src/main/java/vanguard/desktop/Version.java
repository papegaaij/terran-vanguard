package vanguard.desktop;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Properties;

/**
 * The game's version (design/tech/release): the build writes Gradle's {@code version} into {@code
 * vanguard/version.properties} on the class path; {@code --version} prints it.
 */
final class Version {
    /** The class-path resource the build generates (desktop/build.gradle.kts, versionResource). */
    static final String RESOURCE = "/vanguard/version.properties";

    private Version() {}

    /** The version, e.g. {@code 0.1.0}; {@code dev} when the resource is missing (an IDE run without the build). */
    static String current() {
        try (InputStream in = Version.class.getResourceAsStream(RESOURCE)) {
            if (in == null) {
                return "dev";
            }
            var properties = new Properties();
            properties.load(in);
            return properties.getProperty("version", "dev");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** The line {@code --version} prints and every start logs. */
    static String line() {
        return "Terran Vanguard " + current();
    }
}
