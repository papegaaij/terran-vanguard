package vanguard.sim;

import static vanguard.sim.PlayerFire.BEAM_REACH;
import static vanguard.sim.PlayerFire.BEAM_TRIGGER_TICKS;
import static vanguard.sim.PlayerFire.PART_SERIAL;
import static vanguard.sim.PlayerFire.columnContact;
import static vanguard.sim.PlayerFire.groundFactor;
import static vanguard.sim.PlayerFire.onField;

/**
 * M7 part A (design/player/weapons/ion-beam, Beam rules): the {@link PlayerFire}'s beam mounts. A {@link
 * WeaponSpec.Delivery#BEAM} mount fires no shots; while fire is held its {@link Beam} runs from the muzzle
 * straight up and {@link #burn} damages the first target in its column every step. It holds the beams
 * and the cast's scratch fields (no allocation per step).
 */
final class Beams {
    private final PlayerFire fire;
    private final Ship ship;
    private final Armament armament;
    private final SimEvents events;
    private final PlayerFire.Hits hits;
    private final SetPiece[] setPieces;
    /** Per mount its beam (a {@link WeaponSpec.Delivery#BEAM} mount's), null for others. */
    private final Beam[] beams;

    private final boolean anyBeam;

    Beams(PlayerFire fire, Ship ship, Armament armament, SimEvents events, PlayerFire.Hits hits, SetPiece[] setPieces) {
        this.fire = fire;
        this.ship = ship;
        this.armament = armament;
        this.events = events;
        this.hits = hits;
        this.setPieces = setPieces;
        beams = new Beam[armament.size()];
        boolean beam = false;
        for (int m = 0; m < armament.size(); m++) {
            if (armament.mount(m).fires() && armament.mount(m).weapon().delivery() == WeaponSpec.Delivery.BEAM) {
                beams[m] = new Beam();
                beam = true;
            }
        }
        anyBeam = beam;
    }

    /** Back to the level start: every beam off. */
    void reset() {
        for (int m = 0; m < beams.length; m++) {
            if (beams[m] != null) {
                beams[m].reset();
            }
        }
    }

    /** Whether mount {@code m} is a beam mount. */
    boolean fitted(int m) {
        return beams[m] != null;
    }

    /** Mount {@code m}'s beam, null unless it is a {@link WeaponSpec.Delivery#BEAM} mount. */
    Beam beam(int m) {
        return beams[m];
    }

    /** Only with a beam fitted, so other sorties hash as before. */
    void addTo(StateHash hash) {
        if (anyBeam) {
            for (Beam beam : beams) {
                if (beam != null) {
                    beam.addTo(hash);
                }
            }
        }
    }

    /**
     * M7 part A: beam mount {@code m} this step: on while fire is held ({@link SimEvents.Type#BEAM_ON}
     * as it comes on), off otherwise ({@link SimEvents.Type#BEAM_OFF}); its muzzle glows while it runs.
     */
    boolean step(int m, WeaponSpec weapon, boolean held) {
        Beam beam = beams[m];
        WeaponSpec.Muzzle muzzle = weapon.muzzles().getFirst();
        beam.x = ship.x() + muzzle.dx();
        beam.fromY = ship.y() + muzzle.dy();
        beam.width = weapon.size().width();
        if (!held) {
            ceaseBeam(m);
            return false;
        }
        if (!beam.on) {
            beam.on = true;
            beam.ticksOn = 0;
            events.add(SimEvents.Type.BEAM_ON, beam.x, beam.fromY, m);
        }
        beam.ticksOn++;
        return true;
    }

    /** M7 part A: every beam goes off (the ship does not fly: its launch, its wreck, the level's end). */
    void ceaseBeams() {
        if (!anyBeam) {
            return;
        }
        for (int m = 0; m < beams.length; m++) {
            if (beams[m] != null) {
                ceaseBeam(m);
            }
        }
    }

    private void ceaseBeam(int m) {
        Beam beam = beams[m];
        if (beam.on) {
            events.add(SimEvents.Type.BEAM_OFF, beam.x, beam.fromY, m);
        }
        beam.reset();
    }

    /** M7 part A: what the beam cast found nearest along the column (see {@link #burn}). */
    private static final int CAST_NONE = 0;

    private static final int CAST_ENEMY = 1;
    private static final int CAST_PART = 2;
    private static final int CAST_SPORE = 3;
    private static final int CAST_GROUND = 4;
    private static final int CAST_BLOCKED = 5;
    /**
     * Round 37 R.4 (c): a set piece's armour (a part that takes no damage now, its body or neck): it stops
     * the beam on every layer, the ground's included (a boss is not hardened ground); stored as {@link
     * #CAST_BLOCKED}.
     */
    private static final int CAST_ARMOUR = 6;
    /** M7 part F: an enemy's homing missile. */
    private static final int CAST_MISSILE = 7;

    /** M7 part A: the beam cast's nearest find: its kind, height, layer, index (and part) so far. */
    private int castKind;

    private double castY;
    private Layer castLayer;
    private int castIndex;
    private int castPart;
    /** M7 part A: the nearest glance crossing the cast passed over. */
    private double castGlance;

    /**
     * M7 part A (design/player/weapons/ion-beam, Beam rules): every beam that is on casts its column
     * (its width, from its muzzle straight up to its reach, which grows over {@link #Beam.GROW_TICKS}
     * after it comes on) across every hit path at once, without allocating: enemies (segment chains
     * and swarm members among them), set pieces' parts (an armoured body or neck stops it unless a
     * living part of that piece is in the column: it burns over the back to it), armed spore mines and
     * ground objects. The nearest touch wins, a tie the higher layer. What it can damage takes the
     * weapon's damage for this step (a part times its weak-point multiplier; a trigger counts a hit
     * every {@link PlayerFire#BEAM_TRIGGER_TICKS} steps) and its kill pays as a shot's. On the ground layer what
     * it cannot damage (hardened ground objects and units without {@code anti-ground}, a walker's
     * frontal armour, a growth still growing) does not stop it: it passes over with a glance ({@link
     * Beam#glanceY()}); above the ground such a thing stops it without damage ({@link
     * Beam.Contact#BLOCKED}). Round 37 R.4 (c): a set piece's armour stops it on every layer, the ground's
     * included (the Siege Spire's trunk, its maw and core before their phases), and so does a high-air
     * boss's body (the Brood Carrier's hull in its first phase): the beam reaches high air, so it is not
     * beneath the hull as a bolt is. Units under the water, a Lamprey latched onto the
     * hull (front shots pass it), allies, pickups and enemy bullets do not stop it.
     *
     * <p>M7 part C: a canyon wall or rock island in the column ends it there ({@link #wallAhead}, {@link
     * Beam.Contact#ROCK}); a gate's tendril it crosses short of its end burns, and it goes on past it ({@link
     * Links#burn}; M9 = a). M7 part D: a Herald's aura ring absorbs its damage in {@link Enemy#damage}.
     */
    void burn(Pool<Enemy> enemies, Pool<GroundObject> ground, Pool<Mine> spores) {
        if (!anyBeam) {
            return;
        }
        for (int m = 0; m < beams.length; m++) {
            Beam beam = beams[m];
            if (beam == null || !beam.on) {
                continue;
            }
            WeaponSpec weapon = fire.overdriveTicks() > 0
                    ? armament.mount(m).overdrive()
                    : armament.mount(m).weapon();
            double half = weapon.size().width() / 2;
            Medium medium = fire.medium();
            // M8 part A: under water a reach-limited beam reaches its share of the field's height from the nose.
            double length = medium.wet() ? BEAM_REACH * medium.reach(weapon) : BEAM_REACH;
            double reach = beam.fromY + length * Math.min(1, (double) beam.ticksOn / Beam.GROW_TICKS);
            double limit = Math.min(reach, wallAhead(beam.x, half, beam.fromY));
            cast(m, weapon, beam.x, half, beam.fromY, limit, enemies, ground, spores);
            // M8 part A: under water a weapon without anti-sub does the water's share of its damage, every step.
            double water = medium.wet() ? medium.damage(weapon) : 1;
            if (fire.links() != null) {
                // M7 part C (M9 = a): the tendrils it crosses short of its end burn, and it goes on past them.
                fire.links()
                        .burn(beam.x, half, beam.fromY, castKind == CAST_NONE ? limit : castY, weapon.damage() * water);
            }
            beam.glanceY = castGlance < castY ? castGlance : Double.NaN;
            beam.target = -1;
            if (castKind == CAST_NONE) {
                // M7 part C: ended by rock short of its reach: the rock-scorch impact.
                beam.contact = limit < reach ? Beam.Contact.ROCK : Beam.Contact.NONE;
                beam.endY = limit;
                beam.trigger = -1;
                beam.triggerTicks = 0;
                continue;
            }
            beam.endY = castY + Beam.BORE;
            beam.contact = castKind == CAST_BLOCKED ? Beam.Contact.BLOCKED : Beam.Contact.BURN;
            int trigger = -1;
            double damage = weapon.damage() * groundFactor(weapon, castLayer) * water;
            switch (castKind) {
                case CAST_ENEMY -> {
                    Enemy enemy = enemies.get(castIndex);
                    beam.target = enemy.serial();
                    fire.hitBy(enemy.serial());
                    if (castPart >= 0) {
                        // M7 part D: its unit part lay nearer in the column (the Shellback's vent).
                        if (enemy.damagePart(castPart, damage, false)) {
                            hits.unitPartDestroyed(castIndex, castPart, m);
                        }
                    } else if (enemy.damage(damage * enemy.rearFactor(0, 1))) {
                        // M7 part F: the beam goes up the screen: from behind a rear plate facing up it does more.
                        hits.enemyDestroyed(castIndex, m);
                    }
                }
                case CAST_PART -> {
                    SetPiece piece = setPieces[castIndex];
                    beam.target = PART_SERIAL + castIndex * SetPieceSpec.MAX_PARTS + castPart;
                    if (piece.damagePartAt(castPart, damage, beam.x, castY, 0, 1)) {
                        hits.partDestroyed(castIndex, castPart, m);
                    }
                }
                case CAST_SPORE -> {
                    if (spores.get(castIndex).damage(damage)) {
                        hits.mineDestroyed(castIndex);
                    }
                }
                case CAST_MISSILE -> {
                    Missiles missiles = fire.missiles();
                    if (missiles.get(castIndex).damage(damage)) {
                        missiles.destroy(castIndex, Missiles.SHOT_DOWN);
                    }
                }
                case CAST_GROUND -> {
                    GroundObject object = ground.get(castIndex);
                    if (object.spec().trigger()) {
                        trigger = object.serial();
                        beam.triggerTicks = beam.trigger == trigger ? beam.triggerTicks + 1 : 0;
                        if (beam.triggerTicks % BEAM_TRIGGER_TICKS == 0) {
                            fire.strike(castIndex, object, damage);
                        }
                    } else {
                        fire.strike(castIndex, object, damage);
                    }
                }
                default -> {
                    // CAST_BLOCKED: it stops at the edge without damage.
                }
            }
            beam.trigger = trigger;
            if (trigger < 0) {
                beam.triggerTicks = 0;
            }
        }
    }

    /**
     * M7 part A, a hook for part C (canyon walls and rock islands, M2 = a): the height at which a wall
     * in the column from {@code fromY} up stops the beam; none yet.
     */
    private double wallAhead(double x, double halfWidth, double fromY) {
        // M7 part C: the first rock in the column (a wall's or an island's), +∞ without terrain.
        Canyon canyon = fire.canyon();
        return canyon == null ? Double.POSITIVE_INFINITY : canyon.terrain.ceiling(canyon.scroll(), x, halfWidth, fromY);
    }

    /** The beam cast for mount {@code m}: sets the {@code cast*} fields to the nearest find below {@code limit}. */
    private void cast(
            int m,
            WeaponSpec weapon,
            double x,
            double half,
            double fromY,
            double limit,
            Pool<Enemy> enemies,
            Pool<GroundObject> ground,
            Pool<Mine> spores) {
        castKind = CAST_NONE;
        castY = limit;
        castLayer = null;
        castIndex = -1;
        castPart = -1;
        castGlance = Double.POSITIVE_INFINITY;
        Medium medium = fire.medium();
        for (int j = 0; j < enemies.size(); j++) {
            Enemy enemy = enemies.get(j);
            Layer layer = enemy.layer();
            if (!medium.reaches(weapon, layer) || !onField(enemy) || (enemy.latched() && !fire.hitsLatched(m))) {
                continue;
            }
            double y = columnContact(x, half, fromY, enemy.x(), enemy.y(), enemy.hitbox());
            boolean damageable =
                    (weapon.antiGround() || !enemy.spec().hardened()) && !enemy.glances(0, 1) && !enemy.growing();
            // M7 part D: a unit part first, so a tie goes to it (the Shellback's vent); round 40 R.a: a column
            // through the part burns over the body to it (the vent on the back of the shell is seen from above). M8
            // part B: of several parts in the column (a Drill Rig's pumps), the nearest.
            boolean onPart = false;
            for (int p = 0; p < enemy.partCount(); p++) {
                if (!enemy.partAlive(p)) {
                    continue;
                }
                double partY = columnContact(x, half, fromY, enemy.partX(p), enemy.partY(p), enemy.partBox(p));
                if (partY < limit) {
                    consider(damageable ? CAST_ENEMY : CAST_BLOCKED, partY, layer, j, p);
                    onPart = true;
                }
            }
            if (onPart) {
                continue;
            }
            consider(damageable ? CAST_ENEMY : CAST_BLOCKED, y, layer, j, -1);
        }
        for (int k = 0; k < setPieces.length; k++) {
            SetPiece piece = setPieces[k];
            boolean anchored = piece.arena().isPresent();
            if (!piece.present() || (!anchored && !medium.reaches(weapon, piece.layer()))) {
                continue;
            }
            if (!anchored || medium.reaches(weapon, piece.layer())) {
                // M7 part F: a walking boss's front plate stops it, first so that a tie with its hull goes to it.
                consider(CAST_ARMOUR, piece.plateColumn(x, half, fromY), piece.layer(), k, -1);
            }
            boolean partInColumn = false;
            for (int p = 0; p < piece.partCount(); p++) {
                Layer partLayer = piece.partLayer(p);
                if (piece.partWrecked(p) || !piece.partOnField(p) || (anchored && !medium.reaches(weapon, partLayer))) {
                    continue;
                }
                double y = piece.partColumn(p, x, half, fromY);
                if (y < Double.POSITIVE_INFINITY) {
                    partInColumn = true;
                }
                consider(piece.partShielded(p) ? CAST_ARMOUR : CAST_PART, y, partLayer, k, p);
            }
            if (anchored || partInColumn) {
                // An arena boss's platform is scenery; over the armoured back (a high-air boss's hull too,
                // round 37 R.4 c) the beam burns on to a part in its column.
                continue;
            }
            if (PlayField.overlaps(piece.x(), piece.y(), piece.body())) {
                consider(
                        CAST_ARMOUR,
                        columnContact(x, half, fromY, piece.x(), piece.y(), piece.body()),
                        piece.layer(),
                        k,
                        -1);
            }
            consider(CAST_ARMOUR, piece.neckColumn(x, half, fromY), piece.layer(), k, -1);
        }
        CruiserRun cruisers = fire.cruisers();
        if (cruisers != null && medium.reaches(weapon, Layer.LOW_AIR)) {
            // M8 part H, step H2: a cruiser's hull stops it without damage.
            consider(CAST_ARMOUR, cruisers.column(x, half, fromY), Layer.LOW_AIR, -1, -1);
        }
        if (medium.reaches(weapon, Layer.AIR)) {
            for (int j = 0; j < spores.size(); j++) {
                Mine spore = spores.get(j);
                if (spore.armed()) {
                    consider(
                            CAST_SPORE,
                            columnContact(x, half, fromY, spore.x(), spore.y(), EnemyGun.MineSpec.BOX),
                            Layer.AIR,
                            j,
                            -1);
                }
            }
        }
        Missiles missiles = fire.missiles();
        if (missiles != null && medium.reaches(weapon, Layer.AIR)) {
            // M7 part F: the enemies' homing missiles.
            for (int j = 0; j < missiles.size(); j++) {
                Missile missile = missiles.get(j);
                consider(
                        CAST_MISSILE,
                        columnContact(x, half, fromY, missile.x(), missile.y(), Missile.BOX),
                        Layer.AIR,
                        j,
                        -1);
            }
        }
        for (int j = 0; j < ground.size(); j++) {
            GroundObject object = ground.get(j);
            GroundObjectSpec spec = object.spec();
            if (!object.hittable() || (spec.submerged() && !medium.reaches(weapon, Layer.SUB))) {
                continue;
            }
            double y = columnContact(x, half, fromY, object.x(), object.y(), spec.size());
            boolean damageable = weapon.antiGround() || !spec.hardened();
            consider(damageable ? CAST_GROUND : CAST_BLOCKED, y, spec.lowAir() ? Layer.LOW_AIR : Layer.GROUND, j, -1);
        }
    }

    /**
     * M7 part A: a find of {@code kind} touched at height {@code y} on {@code layer}: nearer than the
     * cast's find so far (a tie on a higher layer) it becomes the find; something it cannot damage on
     * the ground layer is passed over instead (a glance crossing), a set piece's armour ({@link
     * #CAST_ARMOUR}) excepted.
     */
    private void consider(int kind, double y, Layer layer, int index, int part) {
        boolean nearer = y < castY;
        boolean tie = y == castY && castKind != CAST_NONE && layer.ordinal() > castLayer.ordinal();
        if (!nearer && !tie) {
            return;
        }
        if (kind == CAST_BLOCKED && layer == Layer.GROUND) {
            castGlance = Math.min(castGlance, y);
            return;
        }
        castKind = kind == CAST_ARMOUR ? CAST_BLOCKED : kind;
        castY = y;
        castLayer = layer;
        castIndex = index;
        castPart = part;
    }
}
