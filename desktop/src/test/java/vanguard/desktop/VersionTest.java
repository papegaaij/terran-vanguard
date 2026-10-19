package vanguard.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class VersionTest {
    @Test
    void theBuildsVersionIsOnTheClassPath() {
        String expected = System.getProperty("vanguard.expectedVersion");
        assertEquals(expected, Version.current());
        assertEquals("Terran Vanguard " + expected, Version.line());
    }

    @Test
    void versionIsAnOptionOfItsOwn() {
        assertEquals(true, DesktopLauncher.printsVersion(new String[] {"--version"}));
        assertEquals(false, DesktopLauncher.printsVersion(new String[] {"--bench", "3"}));
    }
}
