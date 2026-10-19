package vanguard.game.render;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

/**
 * The play field's pass order: the sequence of draw calls (every call handed the batch, and the
 * blend switches) in {@link LevelRenderer}'s {@code draw}, read from its source. The tests have no GL
 * context to record a real batch, so this pins the order the frame is drawn in: a change to the
 * stack (a pass moved, added or dropped) has to be made here as well, on purpose.
 */
class PassOrderTest {
    private static final Path SOURCE = Path.of("src/main/java/vanguard/game/render/LevelRenderer.java");
    private static final Pattern CALL = Pattern.compile("([A-Za-z_]\\w*(?:\\(sortie\\))?(?:\\.\\w+)*)\\(\\s*batch\\b");
    private static final Pattern BLEND = Pattern.compile("batch\\.setBlendFunction\\(([^)]*)\\)");

    private static final String ORDER = """
            backdrop.drawBehind(batch, scroll, clock)
            water.begin(batch)
            drawSub(batch, sortie, alpha, seconds, wrecks, scroll)
            water.end(batch, sortie.realSeconds() - lag)
            shadows.clear(batch)
            shadows.beginGround(batch)
            backdrop.drawGroundTiles(batch, scroll, clock)
            convoy.drawRoad(batch, Math.round(scroll))
            backdrop.drawGroundPieces(batch, scroll, clock)
            shadows.endGround(batch)
            weather.drawShaft(batch, seconds)
            kraken.drawForeshadow(batch, seconds, sortie.tick())
            player.drawTorpedoBacks(batch, sortie, alpha)
            bubbles.draw(batch, scroll)
            setPieces.drawKraken(batch, sortie, wrecks, alpha, scroll, true)
            rootBursts.drawGround(batch, sortie, seconds, scroll)
            setPieces.drawSpire(batch, sortie, wrecks, alpha, scroll, SpirePass.GROUND)
            luna.drawSled(batch, sortie, scroll, alpha)
            luna.drawMarkers(batch, sortie, alpha)
            ground.drawGround(batch, sortie, alpha)
            convoy.drawConvoy(batch, sortie, alpha, play.whiteFlash)
            relay.draw(batch, sortie, alpha, play.whiteFlash, seconds, clock.real())
            domes.draw(batch, sortie, alpha, play.whiteFlash, clock.real())
            shuttles.drawLow(batch, sortie, alpha, play.whiteFlash)
            shadows.beginGround(batch)
            backdrop.drawOverhead(batch, scroll, clock)
            shadows.endGround(batch)
            units.drawCreep(batch, sortie, alpha)
            arsenal.drawGround(batch, scroll)
            arsenal.drawGroundGlow(batch, scroll, sortie.tick())
            debris.draw(batch, scroll)
            burrowers(sortie).drawGround(batch, sortie.tick(), alpha, scroll)
            surfaceWater.draw(batch, scroll)
            crawlers(sortie).drawHusks(batch, scroll)
            units.drawEnemies(batch, sortie, alpha, Depth.GROUND, Launched.ANY)
            crawlers(sortie).drawChains(batch, sortie, alpha, looks, units.unitFlash)
            crawlers(sortie).drawPores(batch, sortie, alpha, scroll)
            shellbacks(sortie).drawOver(batch, sortie, alpha)
            auras.drawRings(batch, sortie, alpha, true)
            rootBursts.drawDust(batch, sortie, seconds, scroll)
            setPieces.drawSpire(batch, sortie, wrecks, alpha, scroll, SpirePass.SHOCKWAVE)
            ground.drawGlints(batch, sortie, alpha)
            setPieces.drawKraken(batch, sortie, wrecks, alpha, scroll, false)
            farside.darken(batch, sortie, alpha, scroll, seconds)
            farside.drawGlows(batch, sortie, looks, alpha, seconds)
            ground.drawGroundGlows(batch, sortie, alpha)
            player.drawBomberShadows(batch, sortie, alpha)
            drawShadows(batch, sortie, alpha)
            relay.drawColumn(batch, sortie, alpha, clock.real())
            drones.draw(batch, sortie, relay, alpha, seconds)
            units.drawEnemies(batch, sortie, alpha, Depth.LEAP, Launched.ANY)
            units.drawEnemies(batch, sortie, alpha, Depth.LOW_AIR, Launched.ANY)
            blasts.draw(batch, scroll)
            lowAir.draw(batch, scroll)
            collapse.drawOver(batch)
            drawLowAir(batch, scroll)
            weather.drawOverBanks(batch, seconds, play.whiteFlash)
            drawRevealed(batch, sortie, alpha)
            drawHiddenGlows(batch, sortie, alpha)
            setPieces.drawBossShadows(batch, sortie, alpha)
            units.drawUnitGlows(batch, sortie, alpha)
            loss.drawGlow(batch, sortie, alpha, seconds)
            tows.draw(batch, sortie, alpha, seconds)
            shuttles.drawAir(batch, sortie, alpha, play.whiteFlash)
            tendrils(sortie).draw(batch, sortie, alpha)
            auras.drawAuras(batch, sortie, alpha, clock.real())
            units.drawEnemies(batch, sortie, alpha, Depth.AIR, overHull ? Launched.NOT : Launched.ANY)
            auras.drawCrests(batch, sortie, alpha)
            auras.drawRings(batch, sortie, alpha, false)
            units.drawChains(batch, sortie, alpha)
            farside.drawSweeps(batch, sortie, alpha)
            luna.drawBlobs(batch, sortie, alpha)
            player.drawAirstrike(batch, sortie, alpha)
            setPieces.drawSetPieces(batch, sortie, alpha, seconds, false)
            setPieces.drawSpire(batch, sortie, wrecks, alpha, scroll, SpirePass.AIR)
            setPieces.drawWrecks(batch, sortie, wrecks, alpha, seconds)
            pieces.draw(batch, scroll)
            props.drawDebris(batch, sortie, alpha)
            props.drawCranes(batch, sortie, alpha)
            secretGlints.draw(batch, sortie, alpha)
            props.drawSondes(batch, sortie, alpha)
            props.drawPickups(batch, sortie, alpha)
            player.drawShots(batch, sortie, alpha, false)
            wingmanLooks.draw(batch, rook, sortie.tick(), alpha, play.whiteFlash)
            player.drawShip(batch, sortie, alpha, shieldShimmer)
            units.drawLatched(batch, sortie, alpha)
            blend(SRC_ALPHA, ONE)
            player.drawShots(batch, sortie, alpha, true)
            player.drawMuzzles(batch, sortie, alpha, true)
            arsenal.drawOverShip(batch, sortie, alpha)
            arsenal.drawStunned(batch, sortie, alpha)
            wingmanLooks.drawMuzzle(batch, rook, weapons, sortie.wingmanMount(), alpha, true)
            blend(SRC_ALPHA, ONE_MINUS_SRC_ALPHA)
            player.drawMuzzles(batch, sortie, alpha, false)
            wingmanLooks.drawMuzzle(batch, rook, weapons, sortie.wingmanMount(), alpha, false)
            effects.draw(batch, scroll)
            canyonLooks.draw(batch, scroll)
            loss.drawLance(batch, sortie, alpha)
            wingmanLooks.drawOver(batch, rook, sortie.tick(), alpha)
            farside.drawSmartBomb(batch, sortie, alpha, play.whiteFlash)
            blend(SRC_ALPHA, ONE)
            arsenal.drawSpecials(batch, sortie, alpha)
            blend(SRC_ALPHA, ONE_MINUS_SRC_ALPHA)
            setPieces.drawSetPieces(batch, sortie, alpha, seconds, true)
            units.drawEnemies(batch, sortie, alpha, Depth.AIR, Launched.ONLY)
            units.drawEnemies(batch, sortie, alpha, Depth.HIGH_AIR, Launched.ANY)
            backdrop.drawFront(batch, scroll, clock)
            spire.drawDaylight(batch, canopyDeath, backdrop.highAirScroll(scroll))
            snow.draw(batch, seconds, clock.real())
            weather.drawFront(batch, seconds, clock.real(), sortie.wind(), SimStep.ticks(sortie.levelSeconds()), play.whiteFlash)
            targeting.draw(batch, sortie, alpha)
            props.drawMines(batch, sortie, alpha)
            setPieces.drawSpire(batch, sortie, wrecks, alpha, scroll, SpirePass.SWEEP)
            props.drawBullets(batch, sortie, alpha)
            screenFlash.draw(batch, sprites.pixel, alpha, play.whiteFlash)
            warnings.draw(batch, sortie.tick(), alpha)
            credits.draw(batch, font)
            setPieces.drawBossBar(batch, sortie)
            """;

    @Test
    void thePlayFieldIsDrawnInItsPassOrder() throws IOException {
        List<String> expected =
                ORDER.lines().map(String::strip).filter(line -> !line.isEmpty()).toList();
        assertEquals(String.join("\n", expected), String.join("\n", passes(drawBody(Files.readString(SOURCE)))));
    }

    /** The body of {@code draw(...)}, without its line comments. */
    private static String drawBody(String source) {
        int start = source.indexOf(") {", source.indexOf("    public void draw(\n")) + 2;
        int depth = 0;
        int end = start;
        for (; ; end++) {
            char c = source.charAt(end);
            if (c == '{') {
                depth++;
            } else if (c == '}' && --depth == 0) {
                break;
            }
        }
        return source.substring(start, end).replaceAll("//[^\n]*", "");
    }

    /** The calls handed the batch, each with its arguments on one line, and the blend switches, in order. */
    private static List<String> passes(String body) {
        TreeMap<Integer, String> found = new TreeMap<>();
        Matcher call = CALL.matcher(body);
        while (call.find()) {
            int open = call.end(1);
            int depth = 0;
            int close = open;
            for (; ; close++) {
                char c = body.charAt(close);
                if (c == '(') {
                    depth++;
                } else if (c == ')' && --depth == 0) {
                    break;
                }
            }
            String args = body.substring(open, close + 1)
                    .replaceAll("\\s+", " ")
                    .replace("( ", "(")
                    .replace(" )", ")");
            found.put(call.start(), call.group(1) + args);
        }
        Matcher blend = BLEND.matcher(body);
        while (blend.find()) {
            found.put(blend.start(), "blend(" + blend.group(1).replace("GL20.GL_", "") + ")");
        }
        return new ArrayList<>(found.values());
    }
}
