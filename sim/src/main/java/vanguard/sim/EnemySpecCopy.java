package vanguard.sim;

import java.util.List;
import java.util.Optional;

/**
 * A mutable copy of an {@link EnemySpec}'s components, for its {@code withX} builders: each sets the
 * components it changes and {@link #build()}s the new stat block through the canonical constructor (so
 * its checks run), and {@link EnemySpec#of}'s base stats. Used while the specs are built, never in a step.
 */
final class EnemySpecCopy {
    String slug;
    double hp;
    Hitbox hitbox;
    Layer layer;
    double contactDamage;
    boolean destroyedByRamming;
    int bounty;
    double speed;
    Optional<EnemySpec.Snake> snake;
    Optional<Double> streamSpeed;
    Optional<EnemySpec.Hover> hover;
    Optional<EnemySpec.Orbit> orbit;
    Optional<EnemyGun> gun;
    Optional<EnemySpec.Drop> drop;
    Optional<EnemySpec.Dive> dive;
    boolean terrain;
    Optional<EnemySpec.Spiral> spiral;
    Optional<Range> strafe;
    Optional<EnemySpec.DeathBurst> deathBurst;
    Optional<EnemySpec.Sine> sine;
    Optional<EnemySpec.Brood> brood;
    Optional<EnemySpec.Walker> walker;
    Optional<EnemySpec.SideHover> sideHover;
    Optional<EnemySpec.Sweep> sweep;
    Optional<EnemySpec.ChainSpec> chain;
    boolean hardened;
    Optional<EnemySpec.Spawner> spawner;
    Optional<EnemySpec.Pounce> pounce;
    Optional<EnemySpec.Cloak> cloak;
    Optional<EnemySpec.Ambush> ambush;
    Optional<EnemySpec.FlockSpec> flock;
    Optional<EnemySpec.Submerge> submerge;
    Optional<EnemySpec.ProximityRing> ring;
    double drift;
    Optional<EnemySpec.Chase> chase;
    Optional<EnemySpec.Latch> latch;
    boolean machine;
    Optional<EnemySpec.Burrow> burrow;
    Optional<EnemySpec.Swoop> swoop;
    Optional<EnemySpec.Link> link;
    Optional<EnemySpec.Crawl> crawl;
    Optional<EnemySpec.Aura> aura;
    List<EnemySpec.UnitPart> parts;
    Optional<EnemySpec.Roll> roll;
    Optional<EnemySpec.Vortex> vortex;
    Optional<EnemySpec.Pull> pull;
    Optional<EnemySpec.LaneStrafe> laneStrafe;
    Optional<EnemySpec.Turret> turret;
    Optional<EnemySpec.RearPlate> rearPlate;
    boolean seekersIgnore;
    Optional<EnemySpec.Rise> rise;
    Optional<EnemySpec.SlowRing> slowRing;
    Optional<EnemySpec.Coil> coil;
    Optional<EnemySpec.Lunge> lunge;
    Optional<EnemySpec.Shield> shield;
    Optional<EnemySpec.LaserLine> laserLine;
    Optional<EnemySpec.SquadScatter> scatter;
    Optional<EnemySpec.Ride> ride;
    boolean neutral;
    Optional<EnemySpec.SwoopPath> swoopPath;
    Optional<EnemySpec.MineLayer> mineLayer;
    Optional<EnemySpec.FacingArmour> facingArmour;
    Optional<EnemySpec.Launcher> launcher;
    Optional<EnemySpec.Dash> dash;
    Optional<EnemySpec.AirLink> airLink;
    Optional<Hitbox> core;
    Optional<EnemySpec.GroundSweep> groundSweep;
    Optional<EnemySpec.Topple> topple;
    EnemySpec.PreyRole preyRole;
    Optional<EnemySpec.Mirror> mirror;
    Optional<EnemySpec.Regeneration> regeneration;
    Optional<EnemySpec.Regrowth> regrowth;
    Optional<EnemySpec.Dome> dome;
    boolean spoofable;

    /** The base stats with every other component left out ({@link EnemySpec#of}). */
    EnemySpecCopy(
            String slug,
            double hp,
            Hitbox hitbox,
            Layer layer,
            double contactDamage,
            boolean destroyedByRamming,
            int bounty,
            double speed) {
        this.slug = slug;
        this.hp = hp;
        this.hitbox = hitbox;
        this.layer = layer;
        this.contactDamage = contactDamage;
        this.destroyedByRamming = destroyedByRamming;
        this.bounty = bounty;
        this.speed = speed;
        snake = Optional.empty();
        streamSpeed = Optional.empty();
        hover = Optional.empty();
        orbit = Optional.empty();
        gun = Optional.empty();
        drop = Optional.empty();
        dive = Optional.empty();
        terrain = false;
        spiral = Optional.empty();
        strafe = Optional.empty();
        deathBurst = Optional.empty();
        sine = Optional.empty();
        brood = Optional.empty();
        walker = Optional.empty();
        sideHover = Optional.empty();
        sweep = Optional.empty();
        chain = Optional.empty();
        hardened = false;
        spawner = Optional.empty();
        pounce = Optional.empty();
        cloak = Optional.empty();
        ambush = Optional.empty();
        flock = Optional.empty();
        submerge = Optional.empty();
        ring = Optional.empty();
        drift = 0;
        chase = Optional.empty();
        latch = Optional.empty();
        machine = false;
        burrow = Optional.empty();
        swoop = Optional.empty();
        link = Optional.empty();
        crawl = Optional.empty();
        aura = Optional.empty();
        parts = List.of();
        roll = Optional.empty();
        vortex = Optional.empty();
        pull = Optional.empty();
        laneStrafe = Optional.empty();
        turret = Optional.empty();
        rearPlate = Optional.empty();
        seekersIgnore = false;
        rise = Optional.empty();
        slowRing = Optional.empty();
        coil = Optional.empty();
        lunge = Optional.empty();
        shield = Optional.empty();
        laserLine = Optional.empty();
        scatter = Optional.empty();
        ride = Optional.empty();
        neutral = false;
        swoopPath = Optional.empty();
        mineLayer = Optional.empty();
        facingArmour = Optional.empty();
        launcher = Optional.empty();
        dash = Optional.empty();
        airLink = Optional.empty();
        core = Optional.empty();
        groundSweep = Optional.empty();
        topple = Optional.empty();
        preyRole = EnemySpec.PreyRole.NONE;
        mirror = Optional.empty();
        regeneration = Optional.empty();
        regrowth = Optional.empty();
        dome = Optional.empty();
        spoofable = false;
    }

    EnemySpecCopy(EnemySpec spec) {
        slug = spec.slug();
        hp = spec.hp();
        hitbox = spec.hitbox();
        layer = spec.layer();
        contactDamage = spec.contactDamage();
        destroyedByRamming = spec.destroyedByRamming();
        bounty = spec.bounty();
        speed = spec.speed();
        snake = spec.snake();
        streamSpeed = spec.streamSpeed();
        hover = spec.hover();
        orbit = spec.orbit();
        gun = spec.gun();
        drop = spec.drop();
        dive = spec.dive();
        terrain = spec.terrain();
        spiral = spec.spiral();
        strafe = spec.strafe();
        deathBurst = spec.deathBurst();
        sine = spec.sine();
        brood = spec.brood();
        walker = spec.walker();
        sideHover = spec.sideHover();
        sweep = spec.sweep();
        chain = spec.chain();
        hardened = spec.hardened();
        spawner = spec.spawner();
        pounce = spec.pounce();
        cloak = spec.cloak();
        ambush = spec.ambush();
        flock = spec.flock();
        submerge = spec.submerge();
        ring = spec.ring();
        drift = spec.drift();
        chase = spec.chase();
        latch = spec.latch();
        machine = spec.machine();
        burrow = spec.burrow();
        swoop = spec.swoop();
        link = spec.link();
        crawl = spec.crawl();
        aura = spec.aura();
        parts = spec.parts();
        roll = spec.roll();
        vortex = spec.vortex();
        pull = spec.pull();
        laneStrafe = spec.laneStrafe();
        turret = spec.turret();
        rearPlate = spec.rearPlate();
        seekersIgnore = spec.seekersIgnore();
        rise = spec.rise();
        slowRing = spec.slowRing();
        coil = spec.coil();
        lunge = spec.lunge();
        shield = spec.shield();
        laserLine = spec.laserLine();
        scatter = spec.scatter();
        ride = spec.ride();
        neutral = spec.neutral();
        swoopPath = spec.swoopPath();
        mineLayer = spec.mineLayer();
        facingArmour = spec.facingArmour();
        launcher = spec.launcher();
        dash = spec.dash();
        airLink = spec.airLink();
        core = spec.core();
        groundSweep = spec.groundSweep();
        topple = spec.topple();
        preyRole = spec.preyRole();
        mirror = spec.mirror();
        regeneration = spec.regeneration();
        regrowth = spec.regrowth();
        dome = spec.dome();
        spoofable = spec.spoofable();
    }

    EnemySpec build() {
        return new EnemySpec(
                slug,
                hp,
                hitbox,
                layer,
                contactDamage,
                destroyedByRamming,
                bounty,
                speed,
                snake,
                streamSpeed,
                hover,
                orbit,
                gun,
                drop,
                dive,
                terrain,
                spiral,
                strafe,
                deathBurst,
                sine,
                brood,
                walker,
                sideHover,
                sweep,
                chain,
                hardened,
                spawner,
                pounce,
                cloak,
                ambush,
                flock,
                submerge,
                ring,
                drift,
                chase,
                latch,
                machine,
                burrow,
                swoop,
                link,
                crawl,
                aura,
                parts,
                roll,
                vortex,
                pull,
                laneStrafe,
                turret,
                rearPlate,
                seekersIgnore,
                rise,
                slowRing,
                coil,
                lunge,
                shield,
                laserLine,
                scatter,
                ride,
                neutral,
                swoopPath,
                mineLayer,
                facingArmour,
                launcher,
                dash,
                airLink,
                core,
                groundSweep,
                topple,
                preyRole,
                mirror,
                regeneration,
                regrowth,
                dome,
                spoofable);
    }
}
