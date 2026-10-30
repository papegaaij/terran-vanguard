package vanguard.sim;

/**
 * What hits the ship, Rook and the drones in a {@link Sortie}'s step: enemy bullets, lobs' direct hits,
 * sweeping beams, armed spore mines, rammers and latching units, the cranes' arms, the sled, debris, set
 * pieces' bodies and flared tendrils; and what follows a hit on the ship ({@link #damaged}). The sortie
 * runs the passes in its step's order; this class holds no state of its own.
 */
final class HitPasses {
    private final Sortie s;
    private final Ship ship;
    private final PlayerFire fire;
    private final EnemyForce force;
    private final SimEvents events;
    private final Tally tally;
    private final SetPiece[] setPieces;
    private final Crane[] cranes;
    private final Wingman wingman;
    private final Latches latches;
    /** The mass-driver sleds; null without them. */
    private final Sled sled;

    private final Pool<Debris> debris;
    /** M7 part C: the level's tendril gates; null without. */
    private final Links links;

    HitPasses(
            Sortie s,
            Ship ship,
            PlayerFire fire,
            EnemyForce force,
            SimEvents events,
            Tally tally,
            SetPiece[] setPieces,
            Crane[] cranes,
            Wingman wingman,
            Latches latches,
            Sled sled,
            Pool<Debris> debris,
            Links links) {
        this.s = s;
        this.ship = ship;
        this.fire = fire;
        this.force = force;
        this.events = events;
        this.tally = tally;
        this.setPieces = setPieces;
        this.cranes = cranes;
        this.wingman = wingman;
        this.latches = latches;
        this.sled = sled;
        this.debris = debris;
        this.links = links;
    }

    /**
     * The wingman takes what crosses his path (design/player/wingmen): enemy bullets, armed spore
     * mines, and the contact of air enemies, which take his ram damage (a small one is destroyed, as
     * when it rams the player; what his ram destroys pays like the player's kills). Enemies never
     * aim at him.
     */
    void hitWingman() {
        if (wingman.out()) {
            return;
        }
        bossBeamsHitWingman();
        if (wingman.out()) {
            return;
        }
        Hull hull = wingman.hull();
        double x = wingman.x();
        double y = wingman.y();
        Pool<EnemyBullet> bullets = force.bullets();
        for (int i = bullets.size() - 1; i >= 0; i--) {
            EnemyBullet bullet = bullets.get(i);
            if (hull.overlaps(x, y, EnemyGun.BULLET, bullet.x(), bullet.y())) {
                bullets.free(i);
                if (wingman.hit(bullet.damage(), ship.x(), ship.y(), events)) {
                    return;
                }
            }
        }
        Pool<Mine> mines = force.mines();
        for (int i = mines.size() - 1; i >= 0; i--) {
            Mine mine = mines.get(i);
            if (mine.armed() && hull.overlaps(x, y, EnemyGun.MineSpec.BOX, mine.x(), mine.y())) {
                double damage = mine.gun().damage();
                events.add(SimEvents.Type.MINE_BURST, mine.x(), mine.y());
                if (mine.line()) {
                    // M9 part C: a line mine touched bursts with its ring.
                    force.lineMineBurst(mine, ship, false);
                }
                mines.free(i);
                if (wingman.hit(damage, ship.x(), ship.y(), events)) {
                    return;
                }
            }
        }
        // M9 part C: a line mine's blast.
        double blast = force.blastDamage(hull, x, y);
        if (blast > 0 && wingman.hit(blast, ship.x(), ship.y(), events)) {
            return;
        }
        missilesAndConesHitWingman(hull, x, y);
        if (wingman.out()) {
            return;
        }
        if (s.vents.grates() > 0 && ventsHitWingman(hull, x, y)) {
            return;
        }
        Pool<Enemy> enemies = force.enemies();
        double ram = wingman.spec().craft().ramDamage();
        wingman.beginContacts();
        for (int j = enemies.size() - 1; j >= 0; j--) {
            Enemy enemy = enemies.get(j);
            EnemySpec spec = enemy.spec();
            // A pounce (M5 part C) touches him once per leap, in its air window.
            // M5 part F: a Lamprey latched onto the player's hull does not touch him.
            if (!enemy.bodyTouches()
                    || Chain.doomed(enemy)
                    || enemy.latched()
                    || !hull.overlaps(x, y, enemy.hitbox(), enemy.x(), enemy.y())
                    || !wingman.touch(enemy.serial())
                    || (enemy.leaping() && !enemy.pounceTouch(Enemy.TOUCHED_WINGMAN))) {
                continue;
            }
            boolean out = wingman.hit(spec.contactDamage(), ship.x(), ship.y(), events);
            if (enemy.damage(ram) || spec.destroyedByRamming()) {
                s.objectivePasses.destroy(j);
            }
            if (out) {
                return;
            }
        }
        wingman.endContacts();
    }

    /**
     * M8 part A (design/player/wingmen, Heavy drone "Bulwark"): the enemy bullets, armed mines and homing missiles
     * that touch the Bulwark burst on it, and an {@code air} enemy touching it hurts it once per contact by its contact
     * damage (a rammer destroyed by ramming is destroyed, paying as the player's kills). Default pending the user's
     * review: the bosses' beams, cones, vents and strikes pass it. M8 part G (design/campaign Level 27; §5 G7): a heat
     * plume's burning disc burns it once per burst ({@link SimEvents.Type#VENT_HIT} value 2); the square grates and the
     * geysers still pass it.
     */
    void hitBulwark(Bulwark bulwark) {
        if (bulwark.out()) {
            return;
        }
        Hull hull = bulwark.hull();
        double x = bulwark.x();
        double y = bulwark.y();
        Pool<EnemyBullet> bullets = force.bullets();
        for (int i = bullets.size() - 1; i >= 0; i--) {
            EnemyBullet bullet = bullets.get(i);
            if (hull.overlaps(x, y, EnemyGun.BULLET, bullet.x(), bullet.y())) {
                bullets.free(i);
                if (bulwark.hit(bullet.damage(), events)) {
                    return;
                }
            }
        }
        Pool<Mine> mines = force.mines();
        for (int i = mines.size() - 1; i >= 0; i--) {
            Mine mine = mines.get(i);
            if (mine.armed() && hull.overlaps(x, y, EnemyGun.MineSpec.BOX, mine.x(), mine.y())) {
                double damage = mine.gun().damage();
                events.add(SimEvents.Type.MINE_BURST, mine.x(), mine.y());
                if (mine.line()) {
                    // M9 part C: a line mine touched bursts with its ring.
                    force.lineMineBurst(mine, ship, false);
                }
                mines.free(i);
                if (bulwark.hit(damage, events)) {
                    return;
                }
            }
        }
        // M9 part C: a line mine's blast.
        double blast = force.blastDamage(hull, x, y);
        if (blast > 0 && bulwark.hit(blast, events)) {
            return;
        }
        Missiles missiles = force.missiles();
        for (int i = missiles.size() - 1; i >= 0; i--) {
            Missile missile = missiles.get(i);
            if (hull.overlaps(x, y, missile.box(), missile.x(), missile.y())) {
                double damage = missile.damage();
                missiles.destroy(i, Missiles.STRUCK);
                if (bulwark.hit(damage, events)) {
                    return;
                }
            }
        }
        if (s.plumes != null) {
            FurnaceVents vents = s.vents;
            for (int g = vents.strike(hull, x, y, FurnaceVents.BULWARK, 0);
                    g >= 0;
                    g = vents.strike(hull, x, y, FurnaceVents.BULWARK, g + 1)) {
                events.add(SimEvents.Type.VENT_HIT, x, y, 2);
                if (bulwark.hit(vents.damage(g), events)) {
                    return;
                }
            }
        }
        Pool<Enemy> enemies = force.enemies();
        bulwark.beginContacts();
        for (int j = enemies.size() - 1; j >= 0; j--) {
            Enemy enemy = enemies.get(j);
            EnemySpec spec = enemy.spec();
            if (!enemy.bodyTouches()
                    || Chain.doomed(enemy)
                    || enemy.latched()
                    || !hull.overlaps(x, y, enemy.hitbox(), enemy.x(), enemy.y())
                    || !bulwark.touch(enemy.serial())
                    || (enemy.leaping() && !enemy.pounceTouch(Enemy.TOUCHED_WINGMAN))) {
                continue;
            }
            boolean out = bulwark.hit(spec.contactDamage(), events);
            if (spec.destroyedByRamming()) {
                s.objectivePasses.destroy(j);
            }
            if (out) {
                return;
            }
        }
        bulwark.endContacts();
    }

    /**
     * M7 part A (design/player/wingmen, Light drone): every whole drone absorbs the enemy bullets that
     * touch it (one hit each), a laser touching it (a sweeping beam, its laser cost once per touch; the
     * laser goes on) and the {@code air} enemies that touch it (its contact cost, a {@code tiny} or
     * {@code small} rammer's less; the enemy takes its contact damage and a small rammer is destroyed,
     * paying as the player's kills). Enemies never aim at it.
     */
    void hitDrones() {
        Drones drones = fire.drones();
        if (!drones.any()) {
            return;
        }
        Pool<EnemyBullet> bullets = force.bullets();
        Pool<Enemy> enemies = force.enemies();
        for (int m = 0; m < fire.armament().size(); m++) {
            if (!drones.whole(m)) {
                continue;
            }
            DroneSpec spec = drones.spec(m);
            double x = drones.x(m);
            double y = drones.y(m);
            boolean broke = false;
            for (int i = bullets.size() - 1; i >= 0 && !broke; i--) {
                EnemyBullet bullet = bullets.get(i);
                if (spec.box().overlaps(x, y, EnemyGun.BULLET, bullet.x(), bullet.y())) {
                    bullets.free(i);
                    broke = drones.hit(m, 1, events);
                }
            }
            if (broke) {
                continue;
            }
            if (drones.laser(m, lasersTouch(drones.hull(m), x, y)) && drones.hit(m, spec.laserHits(), events)) {
                continue;
            }
            drones.beginContacts(m);
            for (int j = enemies.size() - 1; j >= 0 && !broke; j--) {
                Enemy enemy = enemies.get(j);
                EnemySpec enemySpec = enemy.spec();
                if (!enemy.bodyTouches()
                        || Chain.doomed(enemy)
                        || enemy.latched()
                        || !spec.box().overlaps(x, y, enemy.hitbox(), enemy.x(), enemy.y())
                        || !drones.touch(m, enemy.serial())) {
                    continue;
                }
                int cost = enemySpec.destroyedByRamming() ? spec.smallContactHits() : spec.contactHits();
                if (enemy.damage(spec.contactDamage()) || enemySpec.destroyedByRamming()) {
                    s.objectivePasses.destroy(j);
                }
                broke = drones.hit(m, cost, events);
            }
            if (!broke) {
                drones.endContacts(m);
            }
        }
    }

    /**
     * M7 part A: whether a laser touches {@code hull} at (x, y) now: a unit's sweeping beam
     * (design/enemies/air/mantis) or a boss's sweep, while it sweeps.
     */
    boolean lasersTouch(Hull hull, double x, double y) {
        Pool<Enemy> enemies = force.enemies();
        for (int j = 0; j < enemies.size(); j++) {
            Enemy enemy = enemies.get(j);
            if (enemy.spec().sweep().isEmpty() || !enemy.sweeping()) {
                continue;
            }
            EnemySpec.Sweep sweep = enemy.spec().sweep().get();
            double heading = enemy.beam(0);
            double fromX = enemy.x() + enemy.beamOffsetX();
            double fromY = enemy.y() + enemy.beamOffsetY();
            double endX = fromX - Trig.sin(heading) * sweep.length();
            double endY = fromY - Trig.cos(heading) * sweep.length();
            if (hull.touchesSegment(x, y, fromX, fromY, endX, endY, sweep.width() / 2)) {
                return true;
            }
        }
        for (int j = 0; j < enemies.size(); j++) {
            Enemy enemy = enemies.get(j);
            // M9 part A: a live laser line's band too.
            if (enemy.spec().laserLine().isPresent()
                    && enemy.lineBeam.touches(enemy.spec().laserLine().get(), hull, x, y)) {
                return true;
            }
        }
        for (SetPiece piece : setPieces) {
            if (!piece.present() || piece.boss().isEmpty()) {
                continue;
            }
            for (int a = 0; a < piece.attackCount(); a++) {
                if (piece.sweepBeaming(a) && beamTouches(piece, a, hull, x, y)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * M7 part A (design/player/weapons/deflector-pod): the Deflector Pods catch the enemy bullets that
     * enter their fields while they hold charges; a caught bullet is gone (no score, no shield hit).
     */
    void deflect() {
        Deflectors deflectors = fire.deflectors();
        if (!deflectors.any()) {
            return;
        }
        Pool<EnemyBullet> bullets = force.bullets();
        for (int i = bullets.size() - 1; i >= 0; i--) {
            EnemyBullet bullet = bullets.get(i);
            if (deflectors.catches(bullet.x(), bullet.y(), events)) {
                bullets.free(i);
            }
        }
    }

    /** A running sled hits the ship whatever its layer, once per sled. */
    void hitBySled() {
        if (sled != null && sled.strikes(ship.spec().hull(), ship.x(), ship.y())) {
            events.add(SimEvents.Type.SLED_HIT, ship.x(), ship.y());
            double lost = ship.defences().armourLost();
            damaged(lost, ship.defences().takeCollision(sled.spec().damage(), events, ship.x(), ship.y()));
        }
    }

    /** M7 part F: a burning furnace vent hits the ship whatever its layer, once per burst (a collision's damage). */
    void hitByVents() {
        FurnaceVents vents = s.vents;
        Hull hull = ship.spec().hull();
        for (int g = vents.strike(hull, ship.x(), ship.y(), FurnaceVents.SHIP, 0);
                g >= 0;
                g = vents.strike(hull, ship.x(), ship.y(), FurnaceVents.SHIP, g + 1)) {
            events.add(SimEvents.Type.VENT_HIT, ship.x(), ship.y(), 0);
            double lost = ship.defences().armourLost();
            if (damaged(lost, ship.defences().takeCollision(vents.damage(g), events, ship.x(), ship.y()))) {
                return;
            }
        }
    }

    /** A large debris chunk touching the hull deals its contact damage, at most once per its interval. */
    void hitByDebris() {
        Hull hull = ship.spec().hull();
        for (int d = 0; d < debris.size(); d++) {
            Debris chunk = debris.get(d);
            HazardSpec.DebrisSpec spec = chunk.spec();
            if (spec.damage() > 0
                    && hull.overlaps(ship.x(), ship.y(), spec.size(), chunk.x(), chunk.y())
                    && chunk.strike()) {
                double lost = ship.defences().armourLost();
                boolean thrown = chunk.thrown() || chunk.pulled();
                if (thrown) {
                    // A thrown rock (M7 part H: or a pulled one) breaks on the hull.
                    events.add(SimEvents.Type.DEBRIS_DESTROYED, chunk.x(), chunk.y());
                    debris.free(d--);
                }
                if (damaged(lost, ship.defences().takeCollision(spec.damage(), events, ship.x(), ship.y()))) {
                    return;
                }
            }
        }
    }

    /** A set piece's body on the player's layer touching the hull deals its contact damage, at most once per its interval. */
    void hitBySetPieces() {
        Hull hull = ship.spec().hull();
        for (SetPiece piece : setPieces) {
            SetPieceSpec spec = piece.spec();
            if (piece.present()
                    && piece.onPlane()
                    && hull.overlaps(ship.x(), ship.y(), piece.body(), piece.x(), piece.y())
                    && piece.strike()) {
                double lost = ship.defences().armourLost();
                if (damaged(lost, ship.defences().takeCollision(spec.contactDamage(), events, ship.x(), ship.y()))) {
                    return;
                }
            }
        }
    }

    /** A crane's arm touching the hull deals its damage, shield first, at most once per its interval. */
    void hitByCranes() {
        Hull hull = ship.spec().hull();
        for (Crane crane : cranes) {
            if (!crane.present()) {
                continue;
            }
            for (int p = 0; p < hull.parts().size(); p++) {
                Hull.Part part = hull.parts().get(p);
                if (crane.touches(
                                ship.x() + part.dx(),
                                ship.y() + part.dy(),
                                part.box().width() / 2,
                                part.box().height() / 2)
                        && crane.strike()) {
                    double lost = ship.defences().armourLost();
                    if (damaged(lost, ship.defences().takeShot(crane.spec().damage(), events, ship.x(), ship.y()))) {
                        return;
                    }
                    break;
                }
            }
        }
    }

    void hitShip() {
        if (s.directHits > 0) {
            double lost = ship.defences().armourLost();
            if (damaged(lost, ship.defences().takeShot(s.directHits, events, ship.x(), ship.y()))) {
                return;
            }
        }
        Pool<EnemyBullet> bullets = force.bullets();
        Hull hull = ship.spec().hull();
        for (int i = bullets.size() - 1; i >= 0; i--) {
            EnemyBullet bullet = bullets.get(i);
            if (hull.overlaps(ship.x(), ship.y(), EnemyGun.BULLET, bullet.x(), bullet.y())) {
                bullets.free(i);
                double lost = ship.defences().armourLost();
                if (damaged(lost, ship.defences().takeShot(bullet.damage(), events, ship.x(), ship.y()))) {
                    return;
                }
            }
        }
        if (beamsHit(hull)
                || bossBeamsHit(hull)
                || linesHit(hull)
                || bossLinesHit(hull)
                || volleyHit(hull)
                || relayHit(hull)) {
            return;
        }
        // An armed spore bursts on contact with the hull, dealing its layer's attack damage.
        Pool<Mine> mines = force.mines();
        for (int i = mines.size() - 1; i >= 0; i--) {
            Mine mine = mines.get(i);
            if (mine.armed() && hull.overlaps(ship.x(), ship.y(), EnemyGun.MineSpec.BOX, mine.x(), mine.y())) {
                double damage = mine.gun().damage();
                events.add(SimEvents.Type.MINE_BURST, mine.x(), mine.y());
                if (mine.line()) {
                    // M9 part C: a line mine touched bursts with its ring (none this close to the ship).
                    force.lineMineBurst(mine, ship, false);
                }
                mines.free(i);
                double lost = ship.defences().armourLost();
                if (damaged(lost, ship.defences().takeShot(damage, events, ship.x(), ship.y()))) {
                    return;
                }
            }
        }
        // M9 part C (Level 30, C2 a): a proximity burst's blast where it touches the hull, once.
        double blast = force.blastDamage(hull, ship.x(), ship.y());
        if (blast > 0) {
            events.add(SimEvents.Type.MINE_BLAST_HIT, ship.x(), ship.y(), (int) Math.ceil(blast));
            double lost = ship.defences().armourLost();
            if (damaged(lost, ship.defences().takeShot(blast, events, ship.x(), ship.y()))) {
                return;
            }
        }
        if (missilesHit(hull)) {
            return;
        }
        if (coneBurnsShip(hull)) {
            return;
        }
        strikesHitShip(hull);
    }

    /** M7 part F: a homing missile bursts on the hull, dealing its damage; returns whether it wrecked the ship. */
    private boolean missilesHit(Hull hull) {
        Missiles missiles = force.missiles();
        for (int i = missiles.size() - 1; i >= 0; i--) {
            Missile missile = missiles.get(i);
            if (hull.overlaps(ship.x(), ship.y(), missile.box(), missile.x(), missile.y())) {
                double damage = missile.damage();
                missiles.destroy(i, Missiles.STRUCK);
                double lost = ship.defences().armourLost();
                if (damaged(lost, ship.defences().takeShot(damage, events, ship.x(), ship.y()))) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Round 44: a walking boss's firing rail lance touching the hull, or its stomp's ring reaching the ship, deals its
     * damage, once a shot or a stomp each.
     */
    private void strikesHitShip(Hull hull) {
        for (SetPiece piece : setPieces) {
            StrideArena stride = piece.stride().orElse(null);
            if (stride == null || !piece.present() || piece.destroyed()) {
                continue;
            }
            StrideStrikes strikes = stride.strikes();
            if (!strikes.lanceHit(StrideStrikes.SHIP) && strikes.lanceTouches(hull, ship.x(), ship.y())) {
                strikes.markLanceHit(StrideStrikes.SHIP);
                events.add(SimEvents.Type.LANCE_HIT, ship.x(), ship.y(), StrideStrikes.SHIP);
                double lost = ship.defences().armourLost();
                if (damaged(lost, ship.defences().takeShot(strikes.lanceDamage(), events, ship.x(), ship.y()))) {
                    return;
                }
            }
            if (!strikes.stompHit(StrideStrikes.SHIP) && strikes.stompReaches(ship.x(), ship.y())) {
                strikes.markStompHit(StrideStrikes.SHIP);
                events.add(SimEvents.Type.STOMP_HIT, ship.x(), ship.y(), StrideStrikes.SHIP);
                double lost = ship.defences().armourLost();
                if (damaged(lost, ship.defences().takeShot(strikes.stompDamage(), events, ship.x(), ship.y()))) {
                    return;
                }
            }
        }
    }

    /**
     * M7 part F: a walking boss's burning flame cone touching the hull deals its damage, once per burn; returns whether
     * it wrecked the ship.
     */
    private boolean coneBurnsShip(Hull hull) {
        for (SetPiece piece : setPieces) {
            StrideArena stride = piece.stride().orElse(null);
            if (stride == null
                    || !piece.present()
                    || piece.destroyed()
                    || stride.coneHit(StrideArena.CONE_SHIP)
                    || !stride.coneTouches(hull, ship.x(), ship.y())) {
                continue;
            }
            stride.markConeHit(StrideArena.CONE_SHIP);
            double lost = ship.defences().armourLost();
            double damage = piece.boss().orElseThrow().cone().orElseThrow().damage();
            if (damaged(lost, ship.defences().takeShot(damage, events, ship.x(), ship.y()))) {
                return true;
            }
        }
        return false;
    }

    /** M7 part F: a burning furnace vent hits Rook whatever his layer, once per burst; returns whether he ejected. */
    private boolean ventsHitWingman(Hull hull, double x, double y) {
        FurnaceVents vents = s.vents;
        for (int g = vents.strike(hull, x, y, FurnaceVents.ROOK, 0);
                g >= 0;
                g = vents.strike(hull, x, y, FurnaceVents.ROOK, g + 1)) {
            events.add(SimEvents.Type.VENT_HIT, x, y, 1);
            if (wingman.hit(vents.damage(g), ship.x(), ship.y(), events)) {
                return true;
            }
        }
        return false;
    }

    /** M7 part F: homing missiles burst on Rook's hull and a burning flame cone burns him once per burn. */
    private void missilesAndConesHitWingman(Hull hull, double x, double y) {
        Missiles missiles = force.missiles();
        for (int i = missiles.size() - 1; i >= 0; i--) {
            Missile missile = missiles.get(i);
            if (hull.overlaps(x, y, missile.box(), missile.x(), missile.y())) {
                double damage = missile.damage();
                missiles.destroy(i, Missiles.STRUCK);
                if (wingman.hit(damage, ship.x(), ship.y(), events)) {
                    return;
                }
            }
        }
        for (SetPiece piece : setPieces) {
            StrideArena stride = piece.stride().orElse(null);
            if (stride == null
                    || !piece.present()
                    || piece.destroyed()
                    || stride.coneHit(StrideArena.CONE_WINGMAN)
                    || !stride.coneTouches(hull, x, y)) {
                continue;
            }
            stride.markConeHit(StrideArena.CONE_WINGMAN);
            double damage = piece.boss().orElseThrow().cone().orElseThrow().damage();
            if (wingman.hit(damage, ship.x(), ship.y(), events)) {
                return;
            }
        }
        for (SetPiece piece : setPieces) {
            StrideArena stride = piece.stride().orElse(null);
            if (stride == null || !piece.present() || piece.destroyed()) {
                continue;
            }
            // Round 44: the rail lance and the stomp hit him as they hit the ship.
            StrideStrikes strikes = stride.strikes();
            if (!strikes.lanceHit(StrideStrikes.WINGMAN) && strikes.lanceTouches(hull, x, y)) {
                strikes.markLanceHit(StrideStrikes.WINGMAN);
                events.add(SimEvents.Type.LANCE_HIT, x, y, StrideStrikes.WINGMAN);
                if (wingman.hit(strikes.lanceDamage(), ship.x(), ship.y(), events)) {
                    return;
                }
            }
            if (!strikes.stompHit(StrideStrikes.WINGMAN) && strikes.stompReaches(x, y)) {
                strikes.markStompHit(StrideStrikes.WINGMAN);
                events.add(SimEvents.Type.STOMP_HIT, x, y, StrideStrikes.WINGMAN);
                if (wingman.hit(strikes.stompDamage(), ship.x(), ship.y(), events)) {
                    return;
                }
            }
        }
    }

    /**
     * A sweeping beam (design/enemies/air/mantis) touching the hull deals its damage, at most once
     * per sweep; returns whether it wrecked the ship.
     */
    private boolean beamsHit(Hull hull) {
        Pool<Enemy> enemies = force.enemies();
        for (int j = 0; j < enemies.size(); j++) {
            Enemy enemy = enemies.get(j);
            if (enemy.spec().sweep().isEmpty() || !enemy.sweeping() || enemy.sweepHit()) {
                continue;
            }
            EnemySpec.Sweep sweep = enemy.spec().sweep().get();
            double heading = enemy.beam(0);
            double fromX = enemy.x() + enemy.beamOffsetX();
            double fromY = enemy.y() + enemy.beamOffsetY();
            double endX = fromX - Trig.sin(heading) * sweep.length();
            double endY = fromY - Trig.cos(heading) * sweep.length();
            if (hull.touchesSegment(ship.x(), ship.y(), fromX, fromY, endX, endY, sweep.width() / 2)) {
                enemy.markSweepHit();
                events.add(SimEvents.Type.SWEEP_HIT, ship.x(), ship.y());
                double lost = ship.defences().armourLost();
                if (damaged(lost, ship.defences().takeShot(sweep.damage(), events, ship.x(), ship.y()))) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * M9 part A (design/enemies/ground/rail-bunker, {@code laser-line}): a live line whose band touches the hull deals
     * its damage, once per line; returns whether it wrecked the ship.
     */
    private boolean linesHit(Hull hull) {
        Pool<Enemy> enemies = force.enemies();
        for (int j = 0; j < enemies.size(); j++) {
            Enemy enemy = enemies.get(j);
            if (enemy.spec().laserLine().isEmpty()) {
                continue;
            }
            EnemySpec.LaserLine line = enemy.spec().laserLine().get();
            if (enemy.lineBeam.strikes(line, hull, ship.x(), ship.y())) {
                enemy.lineBeam.markHit();
                events.add(SimEvents.Type.LINE_HIT, ship.x(), ship.y(), enemy.kind());
                double lost = ship.defences().armourLost();
                if (damaged(lost, ship.defences().takeShot(line.damage(), events, ship.x(), ship.y()))) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Step BOSS70 ({@link CoreVolley}): a ringed boss's core volley whose live band touches the hull deals its damage,
     * once per line; returns whether it wrecked the ship.
     */
    private boolean volleyHit(Hull hull) {
        for (int k = 0; k < setPieces.length; k++) {
            RingArena ring = setPieces[k].ring;
            if (ring == null || !setPieces[k].present()) {
                continue;
            }
            RingCore core = ring.core().orElse(null);
            CoreVolley volley = core == null ? null : core.volley().orElse(null);
            int line = volley == null ? -1 : volley.strikes(hull, ship.x(), ship.y());
            if (line < 0) {
                continue;
            }
            volley.markHit(line);
            events.add(SimEvents.Type.CORE_VOLLEY_HIT, ship.x(), ship.y(), line);
            double lost = ship.defences().armourLost();
            if (damaged(lost, ship.defences().takeShot(volley.damage(), events, ship.x(), ship.y()))) {
                return true;
            }
        }
        return false;
    }

    /**
     * M9 part H, step H1b ({@link BossLines}): a boss's live rail line whose band touches the hull deals its damage, once
     * per line; returns whether it wrecked the ship.
     */
    private boolean bossLinesHit(Hull hull) {
        for (int k = 0; k < setPieces.length; k++) {
            BossLines lines = setPieces[k].lines;
            if (lines == null || !setPieces[k].present() || !lines.strikes(hull, ship.x(), ship.y())) {
                continue;
            }
            lines.markHit();
            events.add(SimEvents.Type.BOSS_LINE_HIT, ship.x(), ship.y(), lines.part());
            double lost = ship.defences().armourLost();
            if (damaged(lost, ship.defences().takeShot(lines.damage(), events, ship.x(), ship.y()))) {
                return true;
            }
        }
        return false;
    }

    /**
     * M9 part H, step H1b ({@link RelayFlash}, design/campaign Level 35): the mass relay's live flash whose band touches
     * the hull deals its damage, once; returns whether it wrecked the ship.
     */
    private boolean relayHit(Hull hull) {
        RelayFlash relay = s.relay;
        if (relay == null || !relay.strikes(hull, ship.x(), ship.y())) {
            return false;
        }
        relay.markHit();
        events.add(SimEvents.Type.RELAY_HIT, ship.x(), ship.y(), 0);
        double lost = ship.defences().armourLost();
        return damaged(lost, ship.defences().takeShot(relay.spec().damage(), events, ship.x(), ship.y()));
    }

    /**
     * M5 part H (design/enemies, Boss attacks and root bursts; decision H5 = a): a boss's sweeping beam
     * touching the hull deals its damage, at most once per sweep; returns whether it wrecked the ship.
     */
    private boolean bossBeamsHit(Hull hull) {
        for (int k = 0; k < setPieces.length; k++) {
            SetPiece piece = setPieces[k];
            if (!piece.present() || piece.boss().isEmpty()) {
                continue;
            }
            for (int a = 0; a < piece.attackCount(); a++) {
                if (!piece.sweepBeaming(a)
                        || piece.sweepHit(a, SetPiece.SWEPT_SHIP)
                        || !beamTouches(piece, a, hull, ship.x(), ship.y())) {
                    continue;
                }
                piece.markSweepHit(a, SetPiece.SWEPT_SHIP);
                events.add(SimEvents.Type.SWEEP_HIT, ship.x(), ship.y());
                double lost = ship.defences().armourLost();
                double damage = piece.boss()
                        .get()
                        .attacks()
                        .get(a)
                        .sweep()
                        .orElseThrow()
                        .damage();
                if (damaged(lost, ship.defences().takeShot(damage, events, ship.x(), ship.y()))) {
                    return true;
                }
            }
        }
        return false;
    }

    /** M5 part H: a boss's sweeping beam touching Rook's hull deals its damage to him, at most once per sweep. */
    private void bossBeamsHitWingman() {
        for (int k = 0; k < setPieces.length; k++) {
            SetPiece piece = setPieces[k];
            if (!piece.present() || piece.boss().isEmpty()) {
                continue;
            }
            for (int a = 0; a < piece.attackCount(); a++) {
                if (!piece.sweepBeaming(a)
                        || piece.sweepHit(a, SetPiece.SWEPT_WINGMAN)
                        || !beamTouches(piece, a, wingman.hull(), wingman.x(), wingman.y())) {
                    continue;
                }
                piece.markSweepHit(a, SetPiece.SWEPT_WINGMAN);
                double damage = piece.boss()
                        .get()
                        .attacks()
                        .get(a)
                        .sweep()
                        .orElseThrow()
                        .damage();
                if (wingman.hit(damage, ship.x(), ship.y(), events)) {
                    return;
                }
            }
        }
    }

    /** M5 part H: whether attack {@code a}'s beam of {@code piece} touches {@code hull} at (x, y) now. */
    static boolean beamTouches(SetPiece piece, int a, Hull hull, double x, double y) {
        EnemySpec.Sweep sweep =
                piece.boss().orElseThrow().attacks().get(a).sweep().orElseThrow();
        double heading = piece.sweepBeam(a, 0);
        double fromX = piece.sweepOriginX(a);
        double fromY = piece.sweepOriginY(a);
        double endX = fromX - Trig.sin(heading) * sweep.length();
        double endY = fromY - Trig.cos(heading) * sweep.length();
        return hull.touchesSegment(x, y, fromX, fromY, endX, endY, sweep.width() / 2);
    }

    /**
     * A rammer on the player's layer deals its contact damage; a small one is destroyed by the
     * impact. A pounce (M5 part C) touches the ship once per leap, in its air window.
     */
    void ramShip() {
        Pool<Enemy> enemies = force.enemies();
        Hull hull = ship.spec().hull();
        if (latches.present()) {
            latches.countLatched();
        }
        for (int j = enemies.size() - 1; j >= 0; j--) {
            Enemy enemy = enemies.get(j);
            EnemySpec spec = enemy.spec();
            // M5 part F: a latched, stunned or fleeing Lamprey does not collide with the ship.
            if (spec.latch().isPresent() && !enemy.latchable()) {
                continue;
            }
            if (spec.latch().isPresent()
                    && latches.count() < spec.latch().get().slots()
                    && !enemy.empStunned()
                    && hull.overlaps(ship.x(), ship.y(), enemy.hitbox(), enemy.x(), enemy.y())) {
                latches.latchOn(enemy);
                continue;
            }
            if (enemy.bodyTouches()
                    && !Chain.doomed(enemy)
                    && hull.overlaps(ship.x(), ship.y(), enemy.hitbox(), enemy.x(), enemy.y())
                    && (!enemy.leaping() || enemy.pounceTouch(Enemy.TOUCHED_SHIP))) {
                double lost = ship.defences().armourLost();
                boolean wrecked = ship.defences().takeCollision(spec.contactDamage(), events, ship.x(), ship.y());
                if (spec.destroyedByRamming()) {
                    s.objectivePasses.destroy(j);
                }
                if (damaged(lost, wrecked)) {
                    return;
                }
            }
        }
    }

    /**
     * M7 part C (design/enemies/ground/tendril-anchor, Touch): a flared tendril touching the ship deals its
     * {@code medium} damage, shield first (not pushed; its own cooldown).
     */
    /**
     * M9 part D (design/enemies, Air links): a lit air link touching the ship's hull hurts it by its touch (the ship's
     * cooldown; the ship is not pushed). Rook is never touched.
     */
    void touchAirLinks() {
        double damage = force.airLinks().touch(ship.spec().hull(), ship.x(), ship.y());
        if (damage > 0) {
            double lost = ship.defences().armourLost();
            damaged(lost, ship.defences().takeShot(damage, events, ship.x(), ship.y()));
        }
    }

    void touchTendrils() {
        double damage = links.touch(ship.spec().hull(), ship.x(), ship.y(), false);
        if (damage > 0) {
            double lost = ship.defences().armourLost();
            damaged(lost, ship.defences().takeShot(damage, events, ship.x(), ship.y()));
        }
    }

    /**
     * After a hit: armour damage (more armour lost than {@code lostBefore}) ends the chain, and a
     * hit that took the last armour wrecks the ship. Returns whether it did.
     */
    boolean damaged(double lostBefore, boolean destroyed) {
        if (ship.defences().armourLost() > lostBefore) {
            tally.breakChain();
        }
        if (destroyed) {
            s.wrecked = true;
            events.add(SimEvents.Type.SHIP_DESTROYED, ship.x(), ship.y());
            // M7 part A: the drones break with the ship.
            fire.drones().breakAll(events);
        }
        return destroyed;
    }
}
