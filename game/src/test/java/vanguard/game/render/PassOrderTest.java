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
 * blend switches) in {@link DrawPasses}' {@code draw}, its named passes expanded in the order it calls them, read
 * from its source. The tests have no GL context to record a real batch, so this pins the order the frame is drawn
 * in: a change to the stack (a pass moved, added or dropped) has to be made here as well, on purpose.
 */
class PassOrderTest {
    private static final Path SOURCE = Path.of("src/main/java/vanguard/game/render/DrawPasses.java");
    private static final Pattern CALL = Pattern.compile("([A-Za-z_]\\w*(?:\\(sortie\\))?(?:\\.\\w+)*)\\(\\s*batch\\b");
    /** RF6: a named pass's call in {@code draw}, {@code groundPass(batch, f);}. */
    private static final Pattern PASS = Pattern.compile("(\\w+Pass)\\(batch, f\\);");

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
            ridge.draw(batch, seconds, scroll)
            foundry.drawGround(batch, sortie, scroll, seconds, clock.real())
            plumes.drawGround(batch, sortie, scroll)
            shadows.endGround(batch)
            weather.drawShaft(batch, seconds)
            kraken.drawForeshadow(batch, seconds, sortie.tick())
            player.drawTorpedoBacks(batch, sortie, alpha)
            bubbles.draw(batch, scroll)
            setPieces.drawKraken(batch, sortie, wrecks, alpha, scroll, true)
            rootBursts.drawGround(batch, sortie, seconds, scroll)
            setPieces.drawSpire(batch, sortie, wrecks, alpha, scroll, SpirePass.GROUND)
            walker.drawGround(batch, sortie, alpha, scroll, play)
            colossus.drawGround(batch, sortie, alpha, scroll, clock.real())
            luna.drawSled(batch, sortie, scroll, alpha)
            luna.drawMarkers(batch, sortie, alpha)
            crawlerDrops.draw(batch, seconds)
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
            tanks(sortie).drawWrecks(batch, scroll)
            rigs(sortie).drawGround(batch, sortie, alpha, scroll)
            burrowers(sortie).drawGround(batch, sortie.tick(), alpha, scroll)
            surfaceWater.draw(batch, scroll)
            crawlers(sortie).drawHusks(batch, scroll)
            vortices(sortie).drawSwirls(batch, sortie, alpha)
            pumps(sortie).drawGround(batch, sortie, alpha)
            plain.draw(batch, scroll)
            units.drawEnemies(batch, sortie, alpha, Depth.GROUND, Launched.ANY)
            crawlers(sortie).drawChains(batch, sortie, alpha, looks, units.unitFlash)
            crawlers(sortie).drawPores(batch, sortie, alpha, scroll)
            shellbacks(sortie).drawOver(batch, sortie, alpha)
            tanks(sortie).drawOver(batch, sortie, alpha)
            rigs(sortie).drawOver(batch, sortie, looks, alpha, scroll)
            auras.drawRings(batch, sortie, alpha, true)
            rootBursts.drawDust(batch, sortie, seconds, scroll)
            setPieces.drawSpire(batch, sortie, wrecks, alpha, scroll, SpirePass.SHOCKWAVE)
            walker.drawCone(batch, sortie, alpha, clock.real())
            colossus.drawStreaks(batch, sortie, alpha, scroll, clock.real())
            foundry.drawFire(batch, sortie, scroll)
            plumes.drawOver(batch, sortie, scroll)
            maw.draw(batch, sortie, alpha, play.whiteFlash)
            ground.drawGlints(batch, sortie, alpha)
            setPieces.drawKraken(batch, sortie, wrecks, alpha, scroll, false)
            farside.darken(batch, sortie, alpha, scroll, seconds)
            farside.drawGlows(batch, sortie, looks, alpha, seconds)
            ground.drawGroundGlows(batch, sortie, alpha)
            cruisers.drawHoles(batch, sortie, scroll)
            units.drawRisers(batch, sortie, alpha)
            player.drawBomberShadows(batch, sortie, alpha)
            drawShadows(batch, sortie, alpha)
            relay.drawColumn(batch, sortie, alpha, clock.real())
            drones.draw(batch, sortie, relay, alpha, seconds)
            units.drawEnemies(batch, sortie, alpha, Depth.LEAP, Launched.ANY)
            units.drawEnemies(batch, sortie, alpha, Depth.LOW_AIR, Launched.ANY)
            cruisers.draw(batch, sortie)
            blasts.draw(batch, scroll)
            lowAir.draw(batch, scroll)
            flakFlashes.draw(batch, softGlow, sortie.tick(), alpha)
            collapse.drawOver(batch)
            drawLowAir(batch, scroll)
            weather.drawOverBanks(batch, seconds, play.whiteFlash)
            drawRevealed(batch, sortie, alpha)
            drawHiddenGlows(batch, sortie, alpha)
            storm.drawVeil(batch, sortie, alpha, seconds, clock.real())
            storm.drawHiddenGlows(batch, sortie, alpha, clock.real())
            luna.drawMarkers(batch, sortie, alpha)
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
            tenderDrops.drawBodies(batch, seconds)
            buoys.drawBodies(batch, seconds, scroll)
            storm.drawSilhouettes(batch, sortie, alpha, units)
            units.drawAirGlows(batch, sortie, alpha)
            farside.drawSweeps(batch, sortie, alpha)
            luna.drawBlobs(batch, sortie, alpha)
            player.drawAirstrike(batch, sortie, alpha)
            setPieces.drawSetPieces(batch, sortie, alpha, seconds, false)
            setPieces.drawSpire(batch, sortie, wrecks, alpha, scroll, SpirePass.AIR)
            setPieces.drawWrecks(batch, sortie, wrecks, alpha, seconds)
            pieces.draw(batch, scroll)
            props.drawDebris(batch, sortie, alpha)
            colossus.drawRocks(batch, sortie, alpha, scroll)
            props.drawCranes(batch, sortie, alpha)
            secretGlints.draw(batch, sortie, alpha)
            props.drawSondes(batch, sortie, alpha)
            props.drawPickups(batch, sortie, alpha)
            storm.drawPickupGlints(batch, sortie, alpha)
            deepWater.drawUnderShots(batch, sortie, alpha)
            player.drawShots(batch, sortie, alpha, false)
            deepWater.drawBulwark(batch, sortie, alpha)
            wingmanLooks.draw(batch, rook, sortie.tick(), alpha, play.whiteFlash)
            rookLoss.drawSpin(batch, rook, alpha)
            storm.drawWingman(batch, rook, alpha, wingmanLooks)
            player.drawShip(batch, sortie, alpha, shieldShimmer)
            units.drawLatched(batch, sortie, alpha)
            deepWater.drawTossed(batch, sortie, alpha)
            drawUnderGrade(batch, sortie, alpha, scroll, seconds, rook)
            anglers(sortie).drawGlows(batch, sortie, alpha)
            dark.drawHalos(batch, sortie, alpha, clock.real())
            blend(SRC_ALPHA, ONE)
            player.drawShots(batch, sortie, alpha, true)
            deepWater.drawOverShots(batch, sortie, alpha)
            player.drawMuzzles(batch, sortie, alpha, true)
            arsenal.drawOverShip(batch, sortie, alpha)
            arsenal.drawStunned(batch, sortie, alpha)
            wingmanLooks.drawMuzzle(batch, rook, weapons, sortie.wingmanMount(), alpha, true)
            blend(SRC_ALPHA, ONE_MINUS_SRC_ALPHA)
            player.drawMuzzles(batch, sortie, alpha, false)
            wingmanLooks.drawMuzzle(batch, rook, weapons, sortie.wingmanMount(), alpha, false)
            effects.draw(batch, scroll)
            deepWater.drawEffects(batch, scroll)
            canyonLooks.draw(batch, scroll)
            loss.drawLance(batch, sortie, alpha)
            rookLoss.drawOver(batch, sortie, alpha)
            wingmanLooks.drawOver(batch, rook, sortie.tick(), alpha)
            farside.drawSmartBomb(batch, sortie, alpha, play.whiteFlash)
            blend(SRC_ALPHA, ONE)
            arsenal.drawSpecials(batch, sortie, alpha)
            deepWater.drawSonar(batch, sortie, alpha)
            blend(SRC_ALPHA, ONE_MINUS_SRC_ALPHA)
            setPieces.drawSetPieces(batch, sortie, alpha, seconds, true)
            units.drawEnemies(batch, sortie, alpha, Depth.AIR, Launched.ONLY)
            units.drawEnemies(batch, sortie, alpha, Depth.HIGH_AIR, Launched.ANY)
            backdrop.drawFront(batch, scroll, clock)
            grade.drawSnow(batch, scroll, clock.real())
            breakout.draw(batch, sortie, alpha, scroll, seconds)
            spire.drawDaylight(batch, canopyDeath, backdrop.highAirScroll(scroll))
            snow.draw(batch, seconds, clock.real())
            weather.drawFront(batch, seconds, clock.real(), sortie.wind(), SimStep.ticks(sortie.levelSeconds()), play.whiteFlash)
            targeting.draw(batch, sortie, alpha)
            props.drawMines(batch, sortie, alpha)
            setPieces.drawSpire(batch, sortie, wrecks, alpha, scroll, SpirePass.SWEEP)
            walker.drawMissiles(batch, sortie, alpha)
            hunters(sortie).drawTorpedoes(batch, sortie, alpha)
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
        assertEquals(
                String.join("\n", expected),
                String.join("\n", passes(drawBody(Files.readString(SOURCE).replace("\r\n", "\n")))));
    }

    /** RF6: {@code draw} calls its named passes, each once and nothing else handed the batch. */
    @Test
    void drawIsItsNamedPassesInOrder() throws IOException {
        String source = Files.readString(SOURCE).replace("\r\n", "\n");
        String draw = body(source, "    void draw(\n");
        List<String> named = new ArrayList<>();
        Matcher pass = PASS.matcher(draw);
        while (pass.find()) {
            named.add(pass.group(1));
        }
        assertEquals(
                List.of(
                        "behindPass",
                        "groundPass",
                        "groundUnitsPass",
                        "shadowPass",
                        "lowAirPass",
                        "planeUnitsPass",
                        "setPiecePass",
                        "shotPass",
                        "highAirPass",
                        "weatherPass",
                        "overlayPass"),
                named);
        assertEquals(named.size(), CALL.matcher(draw).results().count(), "draw hands the batch to its passes only");
    }

    /** The body of {@code draw(...)} with its named passes' bodies in their calls' place, without line comments. */
    private static String drawBody(String source) {
        Matcher pass = PASS.matcher(body(source, "    void draw(\n"));
        StringBuilder expanded = new StringBuilder();
        while (pass.find()) {
            pass.appendReplacement(
                    expanded,
                    Matcher.quoteReplacement(
                            body(source, "    private void " + pass.group(1) + "(SpriteBatch batch, Frame f) {")));
        }
        return pass.appendTail(expanded).toString();
    }

    /** The body of the method whose declaration starts with {@code declaration}, without its line comments. */
    private static String body(String source, String declaration) {
        int at = source.indexOf(declaration);
        if (at < 0) {
            throw new IllegalStateException("no " + declaration.strip() + " in " + SOURCE);
        }
        int start = source.indexOf(") {", at) + 2;
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
