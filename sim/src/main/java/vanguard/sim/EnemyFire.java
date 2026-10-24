package vanguard.sim;

/**
 * The fire of an {@link EnemyForce}'s units and of the bosses' parts: aimed shots and fans, a Wraith's
 * upward burst, rings and death bursts, the mine layers' spores and the mortars' lobs, within the
 * difficulty's bullet budget and never too close to the ship (design/enemies, bullet readability rules).
 * An attack the {@link ObjectiveHook} aims at a convoy unit goes there instead of at the ship.
 */
final class EnemyFire {
    /** No enemy bullet spawns this close to the ship (design/enemies, bullet readability rules). */
    private static final double NO_FIRE_DISTANCE = 72;

    private static final int MINE_CAPACITY = 96;
    private static final int LOB_CAPACITY = 32;
    /** How far outside a lob's impact circle its ring starts, px. */
    private static final double RING_MARGIN = 12;

    private final SplitMix64 rng;
    private final Rules rules;
    private final SimEvents events;
    private final ObjectiveHook hook;

    private final Pool<EnemyBullet> bullets =
            new Pool<>(EnemyForce.BULLET_CAPACITY, EnemyBullet::new, EnemyBullet[]::new);
    private final Pool<Mine> mines = new Pool<>(MINE_CAPACITY, Mine::new, Mine[]::new);
    private final Pool<Lob> lobs = new Pool<>(LOB_CAPACITY, Lob::new, Lob[]::new);
    /** M7 part F: the bosses' homing missiles (the Revenant Walker's pods). */
    private final Missiles missiles;
    /** M8 part A: the Bulwark that draws aimed volleys at the ship to itself; null without one. */
    private Bulwark lure;
    /** M5 part G: the direct hits of the lobs aimed at the objective that landed on it in the last {@link #moveLobs}. */
    private double objectiveDamage;

    EnemyFire(SplitMix64 rng, Rules rules, SimEvents events, ObjectiveHook hook) {
        this.rng = rng;
        this.rules = rules;
        this.events = events;
        this.hook = hook;
        missiles = new Missiles(events);
    }

    /**
     * M8 part A (design/world/europa, Under water rules): the level's medium: every bullet and homing missile is fired
     * at its bullet speed × the water's.
     */
    void medium(Medium water) {
        for (int i = 0; i < bullets.capacity(); i++) {
            bullets.get(i).medium(water);
        }
        missiles.medium(water);
    }

    /**
     * M8 part A (design/player/wingmen, Heavy drone "Bulwark"; D11): the Bulwark in the escort slot, which draws a
     * share of the aimed volleys at the ship to itself while it is near the ship (its own generator decides).
     */
    void lure(Bulwark bulwark) {
        lure = bulwark;
    }

    /** Back to the level start: no bullets, spores or lobs. */
    void reset() {
        Pools.clear(bullets);
        Pools.clear(mines);
        Pools.clear(lobs);
        missiles.reset();
    }

    /** M7 part F: the homing missiles in flight. */
    Missiles missiles() {
        return missiles;
    }

    Pool<EnemyBullet> bullets() {
        return bullets;
    }

    /**
     * M7 part E, a vortex's step after its move (design/enemies/ground/dust-devil): its rise's, opening's and
     * closing's events, and while it is open its spiral's volleys when due (while {@code firing}, on the play
     * field; none while the ship is closer than bullets may spawn): a bullet per arm, the arms evenly round,
     * one {@link SimEvents.Type#ENEMY_FIRED} per volley (a fan counts once).
     */
    void vortex(Enemy enemy, Ship ship, boolean firing) {
        VortexCycle cycle = enemy.vortexCycle();
        int now = cycle.now();
        if (now == 0) {
            return;
        }
        if ((now & VortexCycle.NOW_CLOSED) != 0) {
            events.add(SimEvents.Type.VORTEX_CLOSED, enemy.x(), enemy.y(), enemy.kind());
        }
        if ((now & VortexCycle.NOW_RISING) != 0) {
            events.add(SimEvents.Type.VORTEX_RISING, enemy.x(), enemy.y(), enemy.kind());
        }
        if ((now & VortexCycle.NOW_OPENED) != 0) {
            events.add(SimEvents.Type.VORTEX_OPENED, enemy.x(), enemy.y(), enemy.kind());
        }
        if ((now & VortexCycle.NOW_SPIRAL) != 0 && firing) {
            spiral(enemy, ship, cycle.spiralAngle());
        }
    }

    /**
     * M8 part E, a coil's step after its move (design/enemies/naval/spiral-nautilus): while it is open its spiral's
     * volleys when due (while {@code firing}), as a vortex's ({@link #spiral}); its events come from its {@link
     * Coiling}.
     */
    void coil(Enemy enemy, Ship ship, boolean firing) {
        if ((enemy.coiling.now() & Coiling.NOW_SPIRAL) != 0 && firing) {
            spiral(enemy, ship, enemy.coiling.spiralAngle());
        }
    }

    /**
     * M8 part F, a lunging unit's step after its move (design/enemies/naval/glow-angler; {@link Lunging}): its ring once
     * due (while {@code firing}, on the play field), as a proximity ring's burst; while the ship is closer than bullets
     * may spawn it waits (its {@link Lunging} drops it at its retreat's end). One {@link SimEvents.Type#ENEMY_FIRED} and
     * one {@link SimEvents.Type#LURE_RING} (value: its kind) per ring; its re-arm then starts.
     */
    void lure(Enemy enemy, Ship ship, boolean firing) {
        Lunging lunging = enemy.lunging;
        if (!firing || !lunging.ringDue() || !PlayField.overlaps(enemy.x(), enemy.y(), enemy.hitbox())) {
            return;
        }
        double dx = ship.x() - enemy.x();
        double dy = ship.y() - enemy.y();
        if (dx * dx + dy * dy < NO_FIRE_DISTANCE * NO_FIRE_DISTANCE) {
            return;
        }
        EnemySpec.Lunge lunge = enemy.spec().lunge().orElseThrow();
        burst(enemy.x(), enemy.y(), lunge.ring(), lunge.ringSpeed(), lunge.ringDamage(), ship);
        lunging.ringFired();
        events.add(SimEvents.Type.ENEMY_FIRED, enemy.x(), enemy.y(), (int) lunge.ringDamage());
        events.add(SimEvents.Type.LURE_RING, enemy.x(), enemy.y(), enemy.kind());
    }

    /**
     * A spiral gun's volley (M7 part E, the vortex's; M8 part E, the coil's) with its first arm at {@code firstArm}: on
     * the play field and not while the ship is closer than bullets may spawn, a bullet per arm, the arms evenly round,
     * one {@link SimEvents.Type#ENEMY_FIRED} per volley (a fan counts once).
     */
    private void spiral(Enemy enemy, Ship ship, double firstArm) {
        if (!PlayField.overlaps(enemy.x(), enemy.y(), enemy.hitbox())) {
            return;
        }
        double dx = ship.x() - enemy.x();
        double dy = ship.y() - enemy.y();
        if (dx * dx + dy * dy < NO_FIRE_DISTANCE * NO_FIRE_DISTANCE || bullets.size() >= rules.bulletBudget()) {
            return;
        }
        EnemyGun gun = enemy.spec().gun().orElseThrow();
        int arms = gun.spiral().orElseThrow().arms();
        // M7 part G: the spiral turns in the vortex's frame; its arms come out mirrored for a flipped one.
        TravelFrame frame = enemy.frame();
        for (int k = 0; k < arms && bullets.size() < rules.bulletBudget(); k++) {
            double angle = firstArm + 2 * StrictMath.PI * k / arms;
            bullets.obtain()
                    .fire(
                            enemy.x(),
                            enemy.y(),
                            Trig.cos(angle) * gun.bulletSpeed(),
                            frame.vy(Trig.sin(angle) * gun.bulletSpeed()),
                            gun.damage());
        }
        events.add(SimEvents.Type.ENEMY_FIRED, enemy.x(), enemy.y(), (int) gun.damage());
    }

    /**
     * M7 part E, a lane strafer's gun (design/enemies/air/ghost-drone): its pass trigger's step; while {@code
     * firing} its eye's {@link SimEvents.Type#EYE_GLINT} and, on the play field, its burst's shots, each an aimed
     * shot at the ship (or its hooked target) as {@link #fire} fires it, one {@link SimEvents.Type#ENEMY_FIRED} each;
     * a burst that is not re-aimed follows its first shot's line.
     */
    void passTrigger(Enemy enemy, Ship ship, boolean firing) {
        if (enemy.spec().gun().isEmpty()) {
            return;
        }
        EnemyGun gun = enemy.spec().gun().get();
        LaneStrafing strafe = enemy.laneStrafing();
        boolean onField = PlayField.overlaps(enemy.x(), enemy.y(), enemy.hitbox());
        int now = strafe.fire(gun, ship.x(), onField);
        if (now == 0 || !firing) {
            return;
        }
        if ((now & LaneStrafing.NOW_GLINT) != 0) {
            events.add(SimEvents.Type.EYE_GLINT, enemy.x(), enemy.y(), enemy.kind());
        }
        if ((now & LaneStrafing.NOW_SHOT) == 0 || !onField) {
            return;
        }
        int target = hook.target(enemy, ship);
        if (gun.reaim() || strafe.firstShot()) {
            double aimX = target < 0 ? ship.x() : hook.convoy().get(target).x();
            double aimY = target < 0 ? ship.y() : hook.convoy().get(target).y();
            // Radians clockwise from straight down, as a barrel's aim.
            strafe.lockAim(StrictMath.atan2(enemy.x() - aimX, enemy.y() - aimY));
            fire(enemy.x(), enemy.y(), gun, false, 0, ship, target, false, enemy.frame());
        } else {
            fire(enemy.x(), enemy.y(), gun, false, strafe.lockedAim(), ship, target, true, enemy.frame());
        }
    }

    /**
     * M7 part C (design/enemies/ground/threadcrawler, Travelling wave): the pore due in {@code chain}'s wave
     * fires its slow spore at the ship, from a segment on the play field that is neither destroyed nor doomed,
     * within the bullet budget and not too close to the ship ({@link SimEvents.Type#PORE_FIRED}).
     */
    void firePore(Chain chain, Ship ship) {
        int pore = chain.poreFiring();
        Enemy member = chain.member(pore);
        if (member == null || !PlayerFire.onField(member) || member.empStunned()) {
            return;
        }
        EnemySpec.PoreWave wave = chain.crawl().pores();
        double dx = ship.x() - member.x();
        double dy = ship.y() - member.y();
        double distance = Math.sqrt(dx * dx + dy * dy);
        if (distance < NO_FIRE_DISTANCE || bullets.size() >= rules.bulletBudget()) {
            return;
        }
        bullets.obtain()
                .fire(
                        member.x(),
                        member.y(),
                        dx / distance * wave.bulletSpeed(),
                        dy / distance * wave.bulletSpeed(),
                        wave.damage());
        events.add(SimEvents.Type.PORE_FIRED, member.x(), member.y(), pore);
    }

    /**
     * M5 part E, a proximity ring (design/enemies/naval/driftjelly, user decision E3 = b): a unit on
     * the screen whose ring is ready fires it when the ship's centre is within its reach, surfaced or
     * submerged (the bullets are on the player's plane either way); its cooldown then starts. A ring
     * due while the ship is closer than the bullets may spawn waits until it is not.
     */
    void pulse(Enemy enemy, Ship ship, boolean firing) {
        EnemySpec.ProximityRing ring = enemy.spec().ring().get();
        if (!firing || !enemy.ringReady() || !PlayField.overlaps(enemy.x(), enemy.y(), enemy.hitbox())) {
            return;
        }
        double dx = ship.x() - enemy.x();
        double dy = ship.y() - enemy.y();
        double distance = dx * dx + dy * dy;
        if (distance > ring.within() * ring.within() || distance < NO_FIRE_DISTANCE * NO_FIRE_DISTANCE) {
            return;
        }
        burst(enemy.x(), enemy.y(), ring.count(), ring.bulletSpeed(), ring.damage(), ship);
        enemy.ringFired();
        events.add(SimEvents.Type.ENEMY_FIRED, enemy.x(), enemy.y(), (int) ring.damage());
    }

    /**
     * M5 part H (design/enemies, Boss attacks and root bursts; decision H5 = a): a boss part at (x, y)
     * lobs {@code gun}'s mortar at where the ship is now (the marker does not follow it).
     */
    void bossLob(double x, double y, EnemyGun gun, Ship ship) {
        Lob lob = lobs.obtain();
        if (lob != null) {
            lob.launch(gun, x, y, ship.x(), ship.y());
            events.add(SimEvents.Type.MORTAR_LOBBED, x, y);
        }
    }

    /**
     * A shot at the ship (or where it is going for a unit that leads the target), a turret's along
     * its barrel, or a fan centred on that line; turned by a random angle within the difficulty's
     * spread.
     */
    void fireAt(Enemy enemy, Ship ship, int target) {
        fire(
                enemy.x(),
                enemy.y(),
                enemy.spec().gun().orElseThrow(),
                enemy.leadsTarget(),
                enemy.aim(),
                ship,
                target,
                false,
                enemy.frame());
    }

    /**
     * M5 part D (design/enemies/air/wraith, user decision of 2026-10-08): one shot of a burst fired
     * straight up the screen as a fixed fan, the burst's shots in turn from the fan's left edge to
     * its right; aimed at nobody, without the difficulty's aim spread, and fired however close the
     * ship is (within the bullet budget).
     */
    void fireUp(Enemy enemy, EnemyGun gun) {
        if (bullets.size() >= rules.bulletBudget()) {
            return;
        }
        int shot = enemy.burstShot();
        double angle = StrictMath.PI / 2
                + (gun.burst() == 1 ? 0 : gun.spreadRadians() / 2 - shot * gun.spreadRadians() / (gun.burst() - 1));
        bullets.obtain()
                .fire(
                        enemy.x(),
                        enemy.y(),
                        Trig.cos(angle) * gun.bulletSpeed(),
                        // M7 part G: up its frame: down the screen for a flipped unit (a Wraith from the top).
                        enemy.frame().vy(Trig.sin(angle) * gun.bulletSpeed()),
                        gun.damage());
        events.add(SimEvents.Type.ENEMY_FIRED, enemy.x(), enemy.y(), (int) gun.damage());
    }

    /**
     * One bullet from (x, y) at {@code angle} radians (0 = right, y up), within the bullet budget;
     * none when the ship is closer than the bullets may spawn.
     */
    void fireAngle(double x, double y, double angle, double speed, double damage, Ship ship) {
        double dx = ship.x() - x;
        double dy = ship.y() - y;
        if (dx * dx + dy * dy < NO_FIRE_DISTANCE * NO_FIRE_DISTANCE || bullets.size() >= rules.bulletBudget()) {
            return;
        }
        bullets.obtain().fire(x, y, Trig.cos(angle) * speed, Trig.sin(angle) * speed, damage);
    }

    /** A set piece's part fires its gun from (x, y) at the ship, as a unit that aims at once. */
    void fireFrom(double x, double y, EnemyGun gun, Ship ship) {
        fire(x, y, gun, false, 0, ship, -1, false);
    }

    /** Round 34, P2: {@code gun}'s volley from (x, y) straight down the screen, as an aimed one otherwise. */
    void fireDown(double x, double y, EnemyGun gun, Ship ship) {
        fire(x, y, gun, false, 0, ship, -1, true);
    }

    /**
     * @param target the convoy unit the shot is aimed at; -1 for the ship
     * @param alongAim whether the volley goes along {@code aim} (a walker's facing fan) whatever the gun
     */
    void fire(double x, double y, EnemyGun gun, boolean leads, double aim, Ship ship, int target, boolean alongAim) {
        fire(x, y, gun, leads, aim, ship, target, alongAim, TravelFrame.SCREEN);
    }

    /**
     * As {@link #fire(double, double, EnemyGun, boolean, double, Ship, int, boolean)} from a unit living in {@code
     * frame}; M7 part G: a flipped unit's spread and fan turn the other way, so its volley is its frame's mirrored.
     */
    void fire(
            double x,
            double y,
            EnemyGun gun,
            boolean leads,
            double aim,
            Ship ship,
            int target,
            boolean alongAim,
            TravelFrame frame) {
        double dx = ship.x() - x;
        double dy = ship.y() - y;
        double distance = Math.sqrt(dx * dx + dy * dy);
        if (distance < NO_FIRE_DISTANCE || bullets.size() >= rules.bulletBudget()) {
            return;
        }
        boolean drawn = false;
        if (target >= 0) {
            dx = hook.convoy().get(target).x() - x;
            dy = hook.convoy().get(target).y() - y;
            distance = Math.sqrt(dx * dx + dy * dy);
            if (distance == 0) {
                return;
            }
        } else if (lure != null && !alongAim && !Double.isFinite(gun.turnRate()) && lure.draws(ship.x(), ship.y())) {
            // M8 part A: the Bulwark draws this aimed volley to itself (its own generator decided; no lead).
            dx = lure.x() - x;
            dy = lure.y() - y;
            distance = Math.sqrt(dx * dx + dy * dy);
            if (distance == 0) {
                return;
            }
            drawn = true;
        }
        if (alongAim || Double.isFinite(gun.turnRate())) {
            // Along the barrel: radians clockwise from straight down.
            dx = -Trig.sin(aim);
            dy = -Trig.cos(aim);
            distance = 1;
        } else if (leads && target < 0 && !drawn) {
            double flight = distance / gun.bulletSpeed();
            dx += ship.vx() * flight;
            dy += ship.vy() * flight;
            distance = Math.sqrt(dx * dx + dy * dy);
        }
        dx /= distance;
        dy /= distance;
        double turn = rules.aimedSpread() > 0 ? rng.range(-rules.aimedSpread(), rules.aimedSpread()) : 0;
        for (int k = 0; k < gun.fan(); k++) {
            if (bullets.size() >= rules.bulletBudget()) {
                break;
            }
            double angle =
                    turn + (gun.fan() == 1 ? 0 : -gun.spreadRadians() / 2 + k * gun.spreadRadians() / (gun.fan() - 1));
            double cos = Trig.cos(angle);
            double sin = frame.vy(Trig.sin(angle));
            EnemyBullet bullet = bullets.obtain();
            bullet.fire(
                    x,
                    y,
                    (dx * cos - dy * sin) * gun.bulletSpeed(),
                    (dx * sin + dy * cos) * gun.bulletSpeed(),
                    gun.damage(),
                    target);
        }
        for (int k = 0; k < gun.aimedShards() && bullets.size() < rules.bulletBudget(); k++) {
            // M8 part B (design/enemies/ground/drill-rig, hard): an aimed shard straight along the volley's aim.
            double cos = Trig.cos(turn);
            double sin = frame.vy(Trig.sin(turn));
            bullets.obtain()
                    .fire(
                            x,
                            y,
                            (dx * cos - dy * sin) * gun.bulletSpeed(),
                            (dx * sin + dy * cos) * gun.bulletSpeed(),
                            gun.damage(),
                            target);
        }
        events.add(SimEvents.Type.ENEMY_FIRED, x, y, (int) gun.damage());
    }

    /** A mine layer drops a spore under it, drifting in a random direction. */
    void dropMine(Enemy enemy, EnemyGun gun) {
        dropMine(gun, enemy.x(), enemy.y(), rng.range(0, 2 * StrictMath.PI));
    }

    /** A spore of {@code gun}'s mine at (x, y), drifting at {@code angle} radians (0 = right, y up). */
    void dropMine(EnemyGun gun, double x, double y, double angle) {
        Mine mine = mines.obtain();
        if (mine != null) {
            mine.drop(gun, x, y, angle);
            events.add(SimEvents.Type.MINE_DROPPED, x, y);
        }
    }

    /**
     * Drifts the spores; one whose life ran out bursts into its ring (unless it never bursts),
     * away from the ship (no bullet spawns close to it).
     */
    void moveMines(Ship ship) {
        for (int i = mines.size() - 1; i >= 0; i--) {
            Mine mine = mines.get(i);
            if (!mine.move()) {
                EnemyGun.MineSpec spec = mine.spec();
                if (mine.expired() && spec.bursts()) {
                    burst(mine.x(), mine.y(), spec.ring(), mine.gun().bulletSpeed(), spec.ringDamage(), ship);
                    events.add(SimEvents.Type.MINE_BURST, mine.x(), mine.y());
                }
                mines.free(i);
            }
        }
    }

    /**
     * A mortar lobs a blob at where the ship is now (the marker does not follow it); M5 part G: under
     * the always hook at the objective while it can be aimed at.
     */
    void lob(Enemy enemy, EnemyGun gun, Ship ship) {
        Lob lob = lobs.obtain();
        if (lob != null) {
            HookMode mode = hook.mode(enemy.kind());
            int target = mode == HookMode.ALWAYS
                    ? hook.convoy().objective(enemy.x(), enemy.y())
                    : mode == HookMode.ALTERNATE ? hook.alternate(enemy, ship) : -1;
            if (target >= 0 && hook.groundHook()) {
                // M7 part D: the marker on the dome, in ground space: it scrolls with the dome.
                lob.launchOnGround(
                        gun,
                        enemy.x(),
                        enemy.y(),
                        hook.convoy().get(target).x(),
                        hook.convoy().get(target).y(),
                        target);
            } else if (target >= 0) {
                lob.launch(
                        gun,
                        enemy.x(),
                        enemy.y(),
                        hook.convoy().get(target).x(),
                        hook.convoy().get(target).y(),
                        target);
            } else {
                lob.launch(gun, enemy.x(), enemy.y(), ship.x(), ship.y());
            }
            events.add(SimEvents.Type.MORTAR_LOBBED, enemy.x(), enemy.y());
        }
    }

    /**
     * Flies the lobs, their start scrolling with the ground by {@code groundScroll}; one that lands
     * bursts into its ring (whether the ship is near or not: the ring starts just outside the impact
     * circle) and hits a ship within its impact circle. Returns the damage of the direct hits in this
     * step, which the ship takes when it can be hit.
     */
    double moveLobs(Ship ship, double groundScroll) {
        return moveLobs(ship, groundScroll, null);
    }

    /**
     * As {@link #moveLobs(Ship, double)}; M7 part A: a direct hit whose blob lands in a Deflector Pod's
     * field while it holds a charge is caught instead (its ring still forms). {@code deflectors}: null
     * while nothing can catch (the ship does not fly).
     */
    double moveLobs(Ship ship, double groundScroll, Deflectors deflectors) {
        double direct = 0;
        objectiveDamage = 0;
        for (int i = lobs.size() - 1; i >= 0; i--) {
            Lob lob = lobs.get(i);
            lob.scroll(groundScroll);
            if (!lob.fly()) {
                continue;
            }
            EnemyGun gun = lob.gun();
            EnemyGun.MortarSpec mortar = gun.mortar().orElseThrow();
            double x = lob.targetX();
            double y = lob.targetY();
            // The ring starts just outside the impact circle, so a ship hit directly is not also
            // hit by the ring as it forms.
            double start = mortar.impactRadius() + RING_MARGIN;
            for (int k = 0; k < mortar.ring() && bullets.size() < rules.bulletBudget(); k++) {
                double angle = 2 * StrictMath.PI * k / mortar.ring();
                double cos = Trig.cos(angle);
                double sin = Trig.sin(angle);
                bullets.obtain()
                        .fire(
                                x + cos * start,
                                y + sin * start,
                                cos * gun.bulletSpeed(),
                                sin * gun.bulletSpeed(),
                                mortar.ringDamage());
            }
            double dx = ship.x() - x;
            double dy = ship.y() - y;
            boolean hit = dx * dx + dy * dy <= mortar.impactRadius() * mortar.impactRadius();
            if (hit && deflectors != null && deflectors.catches(x, y, events)) {
                hit = false;
            }
            if (hit) {
                direct += gun.damage();
            }
            // M5 part G: a lob aimed at the objective deals its direct hit to it when it lands on it.
            if (lob.target() >= 0 && hook.convoy().touches(lob.target(), x, y, EnemyGun.BULLET)) {
                objectiveDamage += gun.damage();
                // M7 part D: on a ground ally, one hit (the ally's spec says whether lobs hurt it).
                if (hook.convoy().spec(lob.target()).lobs()) {
                    hook.hitAlly(lob.target(), 1);
                }
            }
            events.add(SimEvents.Type.MORTAR_IMPACT, x, y, hit ? 1 : 0);
            lobs.free(i);
        }
        return direct;
    }

    Pool<Lob> lobs() {
        return lobs;
    }

    /** M5 part G: the direct hits the lobs aimed at the objective dealt it in the last {@link #moveLobs}. */
    double objectiveLobDamage() {
        return objectiveDamage;
    }

    /** A destroyed unit with a death burst pops into its puff of bullets. */
    void deathBurst(Enemy enemy, Ship ship) {
        EnemySpec.DeathBurst puff = enemy.spec().deathBurst().orElseThrow();
        burst(enemy.x(), enemy.y(), puff.count(), puff.speed(), puff.damage(), ship);
    }

    /**
     * A ring of {@code count} bullets from (x, y), within the bullet budget; none when the ship is
     * closer than the bullets may spawn (design/enemies, bullet readability rules).
     */
    void burst(double x, double y, int count, double speed, double damage, Ship ship) {
        double dx = ship.x() - x;
        double dy = ship.y() - y;
        if (dx * dx + dy * dy < NO_FIRE_DISTANCE * NO_FIRE_DISTANCE) {
            return;
        }
        for (int k = 0; k < count && bullets.size() < rules.bulletBudget(); k++) {
            double angle = 2 * StrictMath.PI * k / count;
            bullets.obtain().fire(x, y, Trig.cos(angle) * speed, Trig.sin(angle) * speed, damage);
        }
    }

    Pool<Mine> mines() {
        return mines;
    }

    void moveBullets() {
        for (int i = bullets.size() - 1; i >= 0; i--) {
            EnemyBullet bullet = bullets.get(i);
            bullet.move();
            if (!PlayField.overlaps(bullet.x(), bullet.y(), EnemyGun.BULLET)) {
                bullets.free(i);
            }
        }
    }
}
