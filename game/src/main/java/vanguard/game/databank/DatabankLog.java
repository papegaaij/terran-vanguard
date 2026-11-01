package vanguard.game.databank;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import vanguard.content.campaign.Campaign;
import vanguard.content.databank.Databank;
import vanguard.sim.Enemy;
import vanguard.sim.EnemySpec;
import vanguard.sim.PlayField;
import vanguard.sim.SetPiece;
import vanguard.sim.SimEvents;
import vanguard.sim.Sortie;

/**
 * Step DB2 (design/ui/databank, The record in the save): an attempt's databank record while a level is flown: the units
 * that were on the screen (any part of one inside the play field for a frame; a boss or set piece once it is present,
 * its bar showing) and the kills per unit ({@code ENEMY_DESTROYED}'s kind, Rook's kills included; a set piece's kill its
 * {@code SET_PIECE_DESTROYED}). Only units the databank has an entry for are kept. Allocation-free per step: a flag and
 * a count per enemy kind and per set piece. {@link #mergeInto} hands the attempt to the campaign at its end (won,
 * failed, restarted or aborted, before the autosave that follows) and starts the next attempt empty.
 */
public final class DatabankLog {
    /** Per enemy kind ({@link Sortie#enemyKinds()}): the databank's unit slug, null for a kind without an entry. */
    private final String[] kindSlugs;

    private final double[] kindHalfWidths;
    private final double[] kindHalfHeights;
    private final boolean[] kindSeen;
    private final int[] kindKills;
    /** Per set piece: its slug, null without an entry. */
    private final String[] pieceSlugs;

    private final boolean[] pieceSeen;
    private final int[] pieceKills;

    private DatabankLog(Sortie sortie, Databank databank) {
        List<EnemySpec> kinds = sortie.enemyKinds();
        kindSlugs = new String[kinds.size()];
        kindHalfWidths = new double[kinds.size()];
        kindHalfHeights = new double[kinds.size()];
        kindSeen = new boolean[kinds.size()];
        kindKills = new int[kinds.size()];
        for (int k = 0; k < kinds.size(); k++) {
            EnemySpec kind = kinds.get(k);
            kindSlugs[k] = databank.unit(kind.slug()).isPresent() ? kind.slug() : null;
            kindHalfWidths[k] = kind.hitbox().width() / 2;
            kindHalfHeights[k] = kind.hitbox().height() / 2;
        }
        pieceSlugs = new String[sortie.setPieceCount()];
        pieceSeen = new boolean[pieceSlugs.length];
        pieceKills = new int[pieceSlugs.length];
        for (int p = 0; p < pieceSlugs.length; p++) {
            String slug = sortie.setPiece(p).slug();
            pieceSlugs[p] = databank.unit(slug).isPresent() ? slug : null;
        }
    }

    /** The log of a sortie's attempts. */
    public static DatabankLog of(Sortie sortie, Databank databank) {
        return new DatabankLog(sortie, databank);
    }

    /** After a simulation step: marks the units on the screen. */
    public void watch(Sortie sortie) {
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            int kind = enemy.kind();
            if (kindSeen[kind] || kindSlugs[kind] == null) {
                continue;
            }
            double x = enemy.renderX(1);
            double y = enemy.renderY(1);
            double halfWidth = kindHalfWidths[kind];
            double halfHeight = kindHalfHeights[kind];
            if (x + halfWidth >= 0
                    && x - halfWidth <= PlayField.WIDTH
                    && y + halfHeight >= 0
                    && y - halfHeight <= PlayField.HEIGHT) {
                kindSeen[kind] = true;
            }
        }
        for (int p = 0; p < pieceSlugs.length; p++) {
            if (!pieceSeen[p] && pieceSlugs[p] != null) {
                SetPiece piece = sortie.setPiece(p);
                pieceSeen[p] = piece.present();
            }
        }
    }

    /** Event {@code i} of a step: counts a kill. */
    public void react(SimEvents events, int i) {
        switch (events.type(i)) {
            case ENEMY_DESTROYED -> {
                int kind = events.value(i);
                if (kind >= 0 && kind < kindKills.length) {
                    kindKills[kind]++;
                    kindSeen[kind] = true;
                }
            }
            case SET_PIECE_DESTROYED -> {
                int piece = events.value(i);
                if (piece >= 0 && piece < pieceKills.length) {
                    pieceKills[piece]++;
                    pieceSeen[piece] = true;
                }
            }
            default -> {}
        }
    }

    /** The units seen in this attempt so far, by slug. */
    public List<String> seen() {
        List<String> seen = new ArrayList<>();
        for (int k = 0; k < kindSlugs.length; k++) {
            if (kindSeen[k] && kindSlugs[k] != null && !seen.contains(kindSlugs[k])) {
                seen.add(kindSlugs[k]);
            }
        }
        for (int p = 0; p < pieceSlugs.length; p++) {
            if (pieceSeen[p] && pieceSlugs[p] != null && !seen.contains(pieceSlugs[p])) {
                seen.add(pieceSlugs[p]);
            }
        }
        return seen;
    }

    /** The kills in this attempt so far, by slug. */
    public Map<String, Integer> kills() {
        Map<String, Integer> kills = new LinkedHashMap<>();
        for (int k = 0; k < kindSlugs.length; k++) {
            if (kindKills[k] > 0 && kindSlugs[k] != null) {
                kills.merge(kindSlugs[k], kindKills[k], Integer::sum);
            }
        }
        for (int p = 0; p < pieceSlugs.length; p++) {
            if (pieceKills[p] > 0 && pieceSlugs[p] != null) {
                kills.merge(pieceSlugs[p], pieceKills[p], Integer::sum);
            }
        }
        return kills;
    }

    /**
     * The attempt's end: its units and kills go into {@code campaign}'s databank as met at Level {@code level}, and the
     * next attempt starts empty. An attempt that met nothing changes nothing.
     */
    public void mergeInto(Campaign campaign, int level) {
        List<String> seen = seen();
        Map<String, Integer> kills = kills();
        if (!seen.isEmpty() || !kills.isEmpty()) {
            campaign.recordAttempt(level, seen, kills);
        }
        java.util.Arrays.fill(kindSeen, false);
        java.util.Arrays.fill(kindKills, 0);
        java.util.Arrays.fill(pieceSeen, false);
        java.util.Arrays.fill(pieceKills, 0);
    }
}
