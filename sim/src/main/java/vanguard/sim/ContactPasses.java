package vanguard.sim;

/**
 * What stops shots and bullets in a {@link Sortie}'s step, and the touches of rock: the running sled,
 * the cranes' arms and clamps, the tows' cables, the debris chunks, rock against enemy bullets (M7 part
 * C), and the ship's and Rook's wall contacts with their push and cooldown. A clamp or cable shot loose
 * releases its secret's crate.
 */
final class ContactPasses {
    private final Sortie s;
    private final Ship ship;
    private final PlayerFire fire;
    private final EnemyForce force;
    private final SimEvents events;
    private final Radio radio;
    private final Objectives objectives;
    private final Rules rules;
    private final HitPasses hitPasses;
    private final Crane[] cranes;
    private final Tow[] tows;
    /** The mass-driver sleds; null without them. */
    private final Sled sled;

    private final Pool<Debris> debris;
    private final Pool<Pickup> pickups;
    /** A secret's crate's life, steps. */
    private final int crateTicks;
    /** M5 part A: the wingman in the escort slot (Rook); null without one. */
    private final Wingman wingman;
    /**
     * M7 part C: the level's rock and what touching it does (Level 16); null (and the contacts null)
     * without terrain.
     */
    private final GroundSpec.TerrainSpec terrainSpec;
    /** M7 part C: the rock with the scroll now; null without terrain. */
    private final Canyon canyon;

    private final WallContact shipContact;
    private final WallContact rookContact;
    /** M7 part C: a contact's normal and depth ({@link Terrain#contact}). */
    private final double[] touch = new double[3];
    /** M7 part C: whether the ship touched rock in this attempt (the radio's {@code first-wall-contact}). */
    private boolean wallContactSeen;

    ContactPasses(
            Sortie s,
            GroundSpec.TerrainSpec terrainSpec,
            Canyon canyon,
            Ship ship,
            PlayerFire fire,
            EnemyForce force,
            SimEvents events,
            Radio radio,
            Objectives objectives,
            Rules rules,
            HitPasses hitPasses,
            Crane[] cranes,
            Tow[] tows,
            Sled sled,
            Pool<Debris> debris,
            Pool<Pickup> pickups,
            int crateTicks,
            Wingman wingman) {
        this.s = s;
        this.terrainSpec = terrainSpec;
        this.canyon = canyon;
        shipContact = terrainSpec == null ? null : new WallContact(terrainSpec);
        rookContact = terrainSpec == null ? null : new WallContact(terrainSpec);
        this.ship = ship;
        this.fire = fire;
        this.force = force;
        this.events = events;
        this.radio = radio;
        this.objectives = objectives;
        this.rules = rules;
        this.hitPasses = hitPasses;
        this.cranes = cranes;
        this.tows = tows;
        this.sled = sled;
        this.debris = debris;
        this.pickups = pickups;
        this.crateTicks = crateTicks;
        this.wingman = wingman;
    }

    /** M7 part C: no push or cooldown carries over (a new attempt, a boss checkpoint in a level with terrain). */
    void resetContacts() {
        if (shipContact != null) {
            shipContact.reset();
            rookContact.reset();
        }
    }

    /** A new attempt: no wall contact pushes, cools down or was seen. */
    void reset() {
        wallContactSeen = false;
        resetContacts();
    }

    /** M7 part C: only in a level with terrain, so the other levels hash as before. */
    void addTo(StateHash hash) {
        if (shipContact != null) {
            shipContact.addTo(hash);
            rookContact.addTo(hash);
            hash.add(wallContactSeen ? 1 : 0);
        }
    }

    /** M7 part C: whether the ship's wall contact cooldown runs (the hit flash may follow it); false without terrain. */
    boolean wallCooling() {
        return shipContact != null && shipContact.cooling();
    }

    /** The cranes' arms stop every shot that touches them; the player's hits on a swinging arm's clamp count. */
    void blockShots() {
        Pool<Shot> shots = fire.shots();
        Pool<EnemyBullet> bullets = force.bullets();
        if (sled != null && sled.running()) {
            // A running sled stops every shot and bullet that crosses the rail.
            for (int i = shots.size() - 1; i >= 0; i--) {
                Shot shot = shots.get(i);
                if (!shot.weapon().delivery().passesUnder()
                        && sled.blocks(shot.x(), shot.y(), shot.weapon().size())) {
                    events.add(SimEvents.Type.SHOT_GLANCED, shot.x(), shot.y(), shot.mount());
                    shots.free(i);
                }
            }
            for (int i = bullets.size() - 1; i >= 0; i--) {
                EnemyBullet bullet = bullets.get(i);
                if (sled.blocks(bullet.x(), bullet.y(), EnemyGun.BULLET)) {
                    bullets.free(i);
                }
            }
        }
        for (Crane crane : cranes) {
            if (!crane.present()) {
                continue;
            }
            for (int i = shots.size() - 1; i >= 0; i--) {
                Shot shot = shots.get(i);
                Hitbox size = shot.weapon().size();
                if (shot.weapon().delivery().passesUnder()) {
                    continue;
                }
                if (crane.clampHit(shot.x(), shot.y(), size)) {
                    events.add(SimEvents.Type.CLAMP_HIT, shot.x(), shot.y(), shot.mount());
                    shots.free(i);
                    if (crane.countClampHit()) {
                        releaseCrate(crane);
                    }
                } else if (crane.touches(shot.x(), shot.y(), size.width() / 2, size.height() / 2)) {
                    events.add(SimEvents.Type.SHOT_GLANCED, shot.x(), shot.y(), shot.mount());
                    shots.free(i);
                }
            }
            for (int i = bullets.size() - 1; i >= 0; i--) {
                EnemyBullet bullet = bullets.get(i);
                if (crane.touches(bullet.x(), bullet.y(), EnemyGun.BULLET.width() / 2, EnemyGun.BULLET.height() / 2)) {
                    bullets.free(i);
                }
            }
        }
        hitTows(shots);
        blockByDebris(shots, bullets);
    }

    /**
     * Part G: the tows' cables take the player's shots that touch them while they hold their pods;
     * the last hit cuts one and its pod falls loose with its secret's crate. Every other shot and
     * bullet passes the boats and the pods.
     */
    private void hitTows(Pool<Shot> shots) {
        for (Tow tow : tows) {
            if (!tow.present() || !tow.holding()) {
                continue;
            }
            for (int i = shots.size() - 1; i >= 0; i--) {
                Shot shot = shots.get(i);
                if (shot.weapon().delivery().passesUnder()
                        || !tow.cableHit(shot.x(), shot.y(), shot.weapon().size())) {
                    continue;
                }
                events.add(SimEvents.Type.CLAMP_HIT, shot.x(), shot.y(), shot.mount());
                shots.free(i);
                if (tow.countHit(s.levelTick)) {
                    cutTow(tow);
                    break;
                }
            }
        }
    }

    /**
     * A tow's cable was cut: the secret is found and its pod falls loose ({@link Tow}); the crate
     * falls out of it at once if the pod is already down at the ship.
     */
    private void cutTow(Tow tow) {
        objectives.secretFound();
        events.add(SimEvents.Type.SECRET_FOUND, towPodX(tow), towPodY(tow));
        radio.cue(CueTrigger.SECRET, tow.spec().secret());
        if (tow.releaseCrate()) {
            dropTowCrate(tow);
        }
    }

    /** The crate falls out of a tow's loose pod, an ordinary hidden crate from here on. */
    void dropTowCrate(Tow tow) {
        Pickup crate = pickups.obtain();
        if (crate != null) {
            crate.drop(PickupType.HIDDEN_CRATE, tow.spec().crateCredits(), towPodX(tow), towPodY(tow), crateTicks);
        }
    }

    /** A tow's pod's centre kept a bullet's width inside the play field. */
    private static double towPodX(Tow tow) {
        double half = EnemyGun.BULLET.width();
        return Math.clamp(tow.podX(), half, PlayField.WIDTH - half);
    }

    private static double towPodY(Tow tow) {
        double half = EnemyGun.BULLET.width();
        return Math.clamp(tow.podY(), half, PlayField.HEIGHT - half);
    }

    /**
     * The debris chunks stop every shot and enemy bullet that touches them: shots glance off a
     * large chunk and damage a small one, which breaks after its HP.
     */
    private void blockByDebris(Pool<Shot> shots, Pool<EnemyBullet> bullets) {
        for (int d = debris.size() - 1; d >= 0; d--) {
            Debris chunk = debris.get(d);
            boolean broken = false;
            for (int i = shots.size() - 1; i >= 0; i--) {
                Shot shot = shots.get(i);
                if (shot.weapon().delivery().passesUnder()
                        || !chunk.touches(shot.x(), shot.y(), shot.weapon().size())) {
                    continue;
                }
                events.add(
                        chunk.large() ? SimEvents.Type.SHOT_GLANCED : SimEvents.Type.DEBRIS_HIT,
                        shot.x(),
                        shot.y(),
                        shot.mount());
                broken = chunk.damage(shot.damage());
                shots.free(i);
                if (broken) {
                    events.add(SimEvents.Type.DEBRIS_DESTROYED, chunk.x(), chunk.y());
                    if (s.rides != null) {
                        // M9 part B: a wave's rock broke: its riders tumble off.
                        s.rides.broken(chunk);
                    }
                    debris.free(d);
                    break;
                }
            }
            if (broken || chunk.pulled()) {
                // M7 part H (the user's round 43 answer H.7c): a breaching boss's rocks stop no enemy bullets.
                continue;
            }
            for (int i = bullets.size() - 1; i >= 0; i--) {
                EnemyBullet bullet = bullets.get(i);
                if (chunk.touches(bullet.x(), bullet.y(), EnemyGun.BULLET)) {
                    bullets.free(i);
                }
            }
        }
    }

    /** A crane's clamp let go: its secret's crate drops from the hook. */
    private void releaseCrate(Crane crane) {
        objectives.secretFound();
        HazardSpec.CraneSpec spec = crane.spec();
        events.add(SimEvents.Type.SECRET_FOUND, crane.tipX(), crane.tipY());
        Pickup crate = pickups.obtain();
        if (crate != null) {
            crate.drop(PickupType.HIDDEN_CRATE, spec.crateCredits(), crane.tipX(), crane.tipY(), crateTicks);
        }
        radio.cue(CueTrigger.SECRET, spec.secret());
    }

    /** M7 part C (design/campaign Level 16, What walls stop): enemy bullets stop at rock with a spark. */
    void stopBulletsAtRock() {
        Pool<EnemyBullet> bullets = force.bullets();
        double half = EnemyGun.BULLET.width() / 2;
        double halfHeight = EnemyGun.BULLET.height() / 2;
        for (int i = bullets.size() - 1; i >= 0; i--) {
            EnemyBullet bullet = bullets.get(i);
            if (canyon.rock(bullet.x(), bullet.y(), half, halfHeight)) {
                events.add(SimEvents.Type.ROCK_SPARK, bullet.x(), bullet.y(), -1);
                bullets.free(i);
            }
        }
    }

    /**
     * M7 part C (design/campaign Level 16, Wall contact; M1 = a): the ship's and Rook's touches of rock. A
     * push under way moves the hull on; a hull over rock is clamped out of it along the rock's normal; a
     * touch outside the cooldown deals the contact's damage (the ship: shield first, the overflow to armour,
     * with the mercy time; Rook: his armour), starts the push and the cooldown and raises {@link
     * SimEvents.Type#WALL_CONTACT}, the ship's first one the radio's {@code first-wall-contact}. In a dash
     * (default pending the user's review), after the level's end or with an invulnerable ship it clamps and
     * pushes without damage.
     */
    void scrape() {
        if (s.flying() && !s.launching()) {
            if (shipContact.step()) {
                ship.pushOut(shipContact.nx(), shipContact.ny(), shipContact.pushStep());
            }
            Hull hull = ship.spec().hull();
            if (terrainSpec.terrain().contact(canyon.scroll(), hull, ship.x(), ship.y(), touch)) {
                double nx = touch[0];
                double ny = touch[1];
                ship.pushOut(nx, ny, touch[2]);
                if (shipContact.ready()) {
                    shipContact.touch(nx, ny);
                    int side = nx > 0 ? -1 : 1;
                    events.add(SimEvents.Type.WALL_CONTACT, ship.x() - nx * 18, ship.y() - ny * 18, side);
                    if (!wallContactSeen) {
                        wallContactSeen = true;
                        radio.cue(CueTrigger.FIRST_WALL_CONTACT, "");
                    }
                    if (!s.complete && !s.failed && !rules.invulnerableShip() && !ship.evading()) {
                        double lost = ship.defences().armourLost();
                        hitPasses.damaged(
                                lost, ship.defences().takeShot(terrainSpec.damage(), events, ship.x(), ship.y()));
                    }
                }
            }
        }
        if (wingman == null || wingman.out() || s.launching()) {
            return;
        }
        if (rookContact.step()) {
            wingman.pushOut(rookContact.nx(), rookContact.ny(), rookContact.pushStep());
        }
        if (terrainSpec.terrain().contact(canyon.scroll(), wingman.hull(), wingman.x(), wingman.y(), touch)) {
            wingman.pushOut(touch[0], touch[1], touch[2]);
            if (rookContact.ready()) {
                rookContact.touch(touch[0], touch[1]);
                events.add(SimEvents.Type.WALL_CONTACT, wingman.x(), wingman.y(), 2);
                if (s.flying() && !s.complete && !s.failed && !rules.invulnerableShip()) {
                    wingman.hit(terrainSpec.damage(), ship.x(), ship.y(), events);
                }
            }
        }
    }
}
