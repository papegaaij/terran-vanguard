package vanguard.sim;

import java.util.List;
import java.util.Optional;

/**
 * A mutable copy of an {@link EnemySpec}'s components, for its {@code withX} builders: each sets the
 * components it changes and {@link #build()}s the new stat block through the canonical constructor (so
 * its checks run). Used while the specs are built, never in a step.
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
                coil);
    }
}
