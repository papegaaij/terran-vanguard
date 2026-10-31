package vanguard.game.audio;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import vanguard.sim.Enemy;
import vanguard.sim.EnemySpec;
import vanguard.sim.PlayField;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M10 part B, step B4: Level 36's own sounds for {@link FlightSounds} (design/audio/sfx, Act 6 sounds; step AUD-B's
 * round 74 proposals, their a variants until the picks, levelled in their files and played as they are): the fleet's
 * drop craft diving past (one pass a pair, as the first of it shows), one scoop drone for the nearest helium-3 harvester
 * on the screen while any flies, a harvester's break over its rung and a Chimera's death (the Talon's crack over a dry
 * tear) over its rung. The Chimera's turn-in whine and twin crack are the Talon's ({@link CeresCues}) until round 74's
 * QB-2 pick; its healing creaks as every regenerating unit's ({@link JovianCues}).
 */
final class CloudDiverCues extends SoundFamily {
    static final String HARVESTER = "harvester";
    static final String DROP_CRAFT = "fleet-stormhawk";
    static final String CHIMERA = "chimera";

    /** The cues are levelled in their files (step AUD-B) and play as they are. */
    static final float CUE_LEVEL = 1f;
    /** A pair of drop craft is one pass: a second pass waits this long, steps. */
    static final int PASS_GAP_STEPS = SimStep.ticks(1.5);

    private static final int SLOTS = 16;

    private boolean[] harvesters;
    private boolean[] dropCraft;
    private boolean[] chimeras;
    private float scoopLevel;
    private long lastPass = Long.MIN_VALUE / 2;
    private long now;
    /** The drop craft seen (serials, a ring): a new one diving in passes. */
    private final int[] craftSeen = new int[SLOTS];

    private int nextCraft;

    CloudDiverCues(SoundMix mix) {
        super(mix);
        Arrays.fill(craftSeen, -1);
    }

    @Override
    Set<SimEvents.Type> types() {
        return EnumSet.noneOf(SimEvents.Type.class);
    }

    @Override
    void play(SimEvents events, int i, float pan) {}

    /** Notes the level's kinds: which are the harvester, the drop craft and the Chimera. */
    void noteKinds(List<EnemySpec> kinds) {
        harvesters = new boolean[kinds.size()];
        dropCraft = new boolean[kinds.size()];
        chimeras = new boolean[kinds.size()];
        for (int k = 0; k < kinds.size(); k++) {
            String slug = kinds.get(k).slug();
            harvesters[k] = slug.equals(HARVESTER);
            dropCraft[k] = slug.equals(DROP_CRAFT);
            chimeras[k] = slug.equals(CHIMERA);
        }
    }

    private static boolean is(boolean[] kinds, int kind) {
        return kinds != null && kind >= 0 && kind < kinds.length && kinds[kind];
    }

    /** Whether any of the level's kinds has a sound here. */
    private boolean any() {
        for (int k = 0; k < harvesters.length; k++) {
            if (harvesters[k] || dropCraft[k] || chimeras[k]) {
                return true;
            }
        }
        return false;
    }

    /**
     * Once a step: a drop craft pair diving in; the scoop for the nearest harvester on the screen; the deaths' sounds of
     * the step's harvesters and Chimeras (over the rung {@link UnitCues} plays for them).
     */
    void watch(Sortie sortie) {
        if (harvesters == null) {
            noteKinds(sortie.enemyKinds());
        }
        if (!any()) {
            return;
        }
        long tick = sortie.tick();
        if (tick < now) {
            // A retry or a checkpoint: the passes come again.
            Arrays.fill(craftSeen, -1);
            lastPass = Long.MIN_VALUE / 2;
        }
        now = tick;
        SimEvents events = sortie.events();
        for (int i = 0; i < events.size(); i++) {
            if (events.type(i) != SimEvents.Type.ENEMY_DESTROYED) {
                continue;
            }
            int kind = events.value(i);
            float pan = pan(events.x(i));
            if (is(harvesters, kind)) {
                bank.play(Sfx.HARVESTER_BREAK, CUE_LEVEL, pitch(0.03), pan);
            } else if (is(chimeras, kind)) {
                bank.play(Sfx.CHIMERA_DEATH, CUE_LEVEL, pitch(0.05), pan);
            }
        }
        Enemy nearest = null;
        double best = Double.POSITIVE_INFINITY;
        double shipX = sortie.ship().renderX(1);
        double shipY = sortie.ship().renderY(1);
        boolean flying = sortie.flying() && !sortie.complete();
        for (int e = 0; e < sortie.enemyCount() && flying; e++) {
            Enemy enemy = sortie.enemy(e);
            if (is(dropCraft, enemy.kind()) && !seen(enemy.serial())) {
                craftSeen[nextCraft] = enemy.serial();
                nextCraft = (nextCraft + 1) % SLOTS;
                if (tick - lastPass >= PASS_GAP_STEPS) {
                    bank.play(Sfx.DROP_CRAFT_PASS, CUE_LEVEL, pitch(0.03), pan(enemy.renderX(1)));
                    lastPass = tick;
                }
            }
            if (is(harvesters, enemy.kind()) && onScreen(enemy)) {
                double d = Math.hypot(enemy.renderX(1) - shipX, enemy.renderY(1) - shipY);
                if (d < best) {
                    best = d;
                    nearest = enemy;
                }
            }
        }
        scoopLevel = fadeLoop(
                Sfx.HARVESTER_SCOOP,
                scoopLevel,
                nearest == null ? 0 : FlightSounds.UNIT_LOOP_LEVEL,
                nearest == null ? 0 : pan(nearest.renderX(1)));
    }

    /** Whether a unit's centre is on the play field. */
    static boolean onScreen(Enemy enemy) {
        double y = enemy.renderY(1);
        double x = enemy.renderX(1);
        return y >= 0 && y <= PlayField.HEIGHT && x >= 0 && x <= PlayField.WIDTH;
    }

    private boolean seen(int serial) {
        for (int s = 0; s < SLOTS; s++) {
            if (craftSeen[s] == serial) {
                return true;
            }
        }
        return false;
    }

    /** The flight closes: the scoop loop is stopped ({@link FlightSounds#stopLoops}). */
    void silence(Sfx loop) {
        if (loop == Sfx.HARVESTER_SCOOP) {
            scoopLevel = 0;
        }
    }
}
