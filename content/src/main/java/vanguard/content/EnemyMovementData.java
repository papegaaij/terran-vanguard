package vanguard.content;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.List;
import java.util.Optional;
import vanguard.content.ColossusBlock.Breach;
import vanguard.content.EnemyHookData.CoilChange;
import vanguard.content.EnemyHookData.LungeChange;
import vanguard.content.EnemyHookData.PullChange;
import vanguard.content.EnemyHookData.SlowChange;
import vanguard.content.EnemyHookData.VortexChange;
import vanguard.sim.PlayField;

/**
 * An enemy's movement keys in its stat block ({@link EnemyData}): the movement patterns ({@code movement}) and
 * the parts they carry, the unit-level behaviours that move or shape it (cloak, submerge, latch, roll, vortex, pull,
 * turret, rear plate, pursuit, aura, the segment chain, under water).
 *
 * <p>It only holds these types: {@link EnemyData} implements it, so callers keep naming them {@code EnemyData.X}
 * (Java inherits member types from superinterfaces, JLS 8.5) while the record's file stays small. New code may name
 * the interface directly; an import must (the canonical name: {@code import vanguard.content.EnemyMovementData.X}).
 */
public sealed interface EnemyMovementData permits EnemyData {
    /**
     * M7 part D, the curl-roll (design/enemies/ground/shellback): once, when its body (its vital part, else the unit)
     * falls to {@code below} of its HP, it curls over {@code curl} s (already armoured), rolls {@code seconds} s along
     * its walk path at {@code speed} px/s as a ball of {@code ball} px (its hit box; fully armoured: shots spark off;
     * it crushes the ground objects in its path), uncurls over {@code uncurl} s and walks on; for {@code
     * belly_seconds} s after it uncurls its body takes {@code belly_factor} × the damage (the belly). No attack while
     * curled. Round 40 R.e: with {@code belly_row} (px above the bottom edge), a roll drifting down the screen ends
     * early so its belly shows at that row or higher.
     */
    record Roll(
            double below,
            double curl,
            double seconds,
            double speed,
            double uncurl,
            double bellySeconds,
            double bellyFactor,
            Size ball,
            Optional<Double> bellyRow) {
        public Roll {
            Check.that(below > 0 && below < 1, "below: a share of its HP between 0 and 1, was " + below);
            Check.positive("curl", curl);
            Check.positive("seconds", seconds);
            Check.positive("speed", speed);
            Check.positive("uncurl", uncurl);
            Check.notNegative("belly_seconds", bellySeconds);
            Check.that(bellyFactor >= 1, "belly_factor: 1 or more, was " + bellyFactor);
            bellyRow.ifPresent(row -> Check.that(
                    row > 0 && row < PlayField.HEIGHT,
                    "belly_row: px above the bottom edge, on the play field, was " + row));
        }

        /** The px it rolls: {@code seconds × speed}. */
        public double length() {
            return seconds * speed;
        }
    }

    /**
     * M7 part E, the spin cycle (design/enemies/ground/dust-devil): a cycle of {@code closed} s closed on {@code
     * ground} (the vanes {@code armoured}: every shot glances off, a blast excepted), {@code rise} s rising (the
     * tell), {@code open} s open on {@code low-air} (the core hittable, its {@code while: open} attack firing), then
     * it sinks back at the next cycle's start; unit i of a wave runs its cycle {@code i × offset} s later. It wanders
     * at {@code wander_speed} px/s up to {@code wander_amplitude} px either side of its wave's path (seeded), scrolling
     * with the ground, never closer than {@code keep_away} px to the ship's centre.
     */
    record Vortex(
            double closed,
            double rise,
            double open,
            double offset,
            double wanderSpeed,
            double wanderAmplitude,
            double keepAway) {
        public Vortex {
            Check.positive("closed", closed);
            Check.positive("rise", rise);
            Check.positive("open", open);
            Check.notNegative("offset", offset);
            Check.positive("wander_speed", wanderSpeed);
            Check.notNegative("wander_amplitude", wanderAmplitude);
            Check.notNegative("keep_away", keepAway);
        }

        /** One cycle, s: closed, rise and open. */
        public double cycle() {
            return closed + rise + open;
        }

        /** The cycle with another open window, s (a difficulty's {@link VortexChange}). */
        public Vortex withOpen(double seconds) {
            return new Vortex(closed, rise, seconds, offset, wanderSpeed, wanderAmplitude, keepAway);
        }
    }

    /**
     * M7 part E, the radial pull (design/enemies/ground/dust-devil): the ship within {@code ship_radius} px of its
     * centre drifts toward it at {@code ship_speed} px/s at the centre, fading to 0 at the edge (never a trap against
     * the ship's speed); a loose pickup within {@code pickup_radius} px is drawn in at {@code pickup_speed} px/s (the
     * Magnet's inverse; with a fitted Magnet the stronger pull wins) and held until the unit dies (released then) or
     * leaves the screen (carried off).
     */
    record Pull(double shipRadius, double shipSpeed, double pickupRadius, double pickupSpeed) {
        public Pull {
            Check.positive("ship_radius", shipRadius);
            Check.positive("ship_speed", shipSpeed);
            Check.positive("pickup_radius", pickupRadius);
            Check.positive("pickup_speed", pickupSpeed);
        }

        /** The pull with another speed on the ship, px/s (a difficulty's {@link PullChange}). */
        public Pull withShipSpeed(double speed) {
            return new Pull(shipRadius, speed, pickupRadius, pickupSpeed);
        }
    }

    /**
     * M7 part F, an independent turret (design/enemies/ground/warden-tank): it turns at {@code turn_rate} °/s toward
     * the ship all round, independent of the hull; once its gun is ready and its aim is within {@code within}° of the
     * ship it stops for {@code lock} s (the tell) and fires its burst along its aim.
     */
    record Turret(double turnRate, double lock, double within) {
        public Turret {
            Check.positive("turn_rate", turnRate);
            Check.notNegative("lock", lock);
            Check.notNegative("within", within);
        }
    }

    /**
     * M7 part F, a thin rear plate (design/enemies/ground/warden-tank): a direct shot arriving within {@code arc}° of
     * its rear (flying along its facing) does {@code multiplier} × its damage to the body (the Scuttler's front arc
     * reversed).
     */
    record RearPlate(double arc, double multiplier) {
        public RearPlate {
            Check.that(arc > 0 && arc <= 180, "arc: degrees each side of the rear, 0-180, was " + arc);
            Check.positive("multiplier", multiplier);
        }
    }

    /**
     * M7 part G, the pursuit formation's defaults (design/enemies/air/mote-swarm; design/campaign Level 20, The pursuit
     * swarm): the flock's leader point holds a {@code band} {@code [top, bottom]} px below the top edge (behind the ship
     * once the scroll is reversed), following the ship's x at {@code lag} px/s; every {@code lunge_every} s a {@code
     * tell} of that many s (the flock contracts into a {@code ball} px across), then the leader dives down the ship's
     * lane at {@code dive} px/s to {@code overshoot} px below where the ship was at the tell's start (no homing) and
     * climbs back to the band at {@code climb} px/s; {@code refill} motes join from the top edge every {@code
     * refill_every} s while the flock has room; at its wave's break-off the leader climbs off the top edge at {@code
     * break_speed} px/s and the members follow, alive. A wave's {@code pursuit} block may change any of them.
     */
    record Pursuit(
            Span band,
            double lag,
            double lungeEvery,
            double tell,
            double ball,
            double dive,
            double overshoot,
            double climb,
            int refill,
            double refillEvery,
            double breakSpeed) {
        public Pursuit {
            Check.that(band.min() >= 0 && band.max() > band.min(), "band: [top, bottom] px below the top edge");
            Check.that(band.max() < PlayField.HEIGHT, "band: inside the play field");
            Check.positive("lag", lag);
            Check.positive("lunge_every", lungeEvery);
            Check.positive("tell", tell);
            Check.that(tell < lungeEvery, "tell: shorter than the time between lunges");
            Check.positive("ball", ball);
            Check.positive("dive", dive);
            Check.notNegative("overshoot", overshoot);
            Check.positive("climb", climb);
            Check.notNegative("refill", refill);
            Check.positive("refill_every", refillEvery);
            Check.positive("break_speed", breakSpeed);
        }
    }

    /**
     * M7 part D, the {@code aura} attack (design/enemies/air/choir-herald; M8 = a): every other enemy within {@code
     * radius} px of its centre (never a boss or mid-boss, never another unit with an aura) carries a shield ring that
     * absorbs damage before its HP: at most {@code share} × that unit's HP at the level, capped at {@code cap}. A unit
     * entering the radius gets its full ring over {@code fill} s, one leaving it keeps the ring {@code keep} s; while
     * the aura's unit lives a ring untouched for {@code refill_delay} s refills at {@code refill} of its maximum a
     * second; at its death every ring it fed collapses over {@code collapse} s. Its escorts ({@code carrier +
     * escorts}) orbit it at {@code escort_radius} × the radius, so they stay inside it. The EMP Burst strips the rings.
     */
    record Aura(
            double radius,
            double share,
            double cap,
            double refill,
            double refillDelay,
            double fill,
            double keep,
            double collapse,
            double escortRadius) {
        public Aura {
            Check.positive("radius", radius);
            Check.positive("share", share);
            Check.positive("cap", cap);
            Check.that(refill > 0 && refill <= 1, "refill: a share of the ring a second, above 0 and at most 1");
            Check.notNegative("refill_delay", refillDelay);
            Check.positive("fill", fill);
            Check.positive("keep", keep);
            Check.positive("collapse", collapse);
            Check.that(
                    escortRadius > 0 && escortRadius < 1,
                    "escort_radius: a share of the radius between 0 and 1, was " + escortRadius);
        }

        /** The ring a unit of {@code hp} carries: {@code share × hp}, at most {@code cap}. */
        public double ring(double hp) {
            return Math.min(cap, share * hp);
        }
    }

    /**
     * @param ambush M5 part D: the path of a {@code rear ambush} wave's unit (the Wraith)
     * @param flock M5 part D: how a {@code swarm} wave's members steer (the Mote Swarm)
     * @param anchored M5 part E: an arena boss's anchored arrival (the Harbour Kraken)
     * @param chase M5 part F: it homes on the ship from its entry, then flies straight on (the Lamprey)
     * @param burrow M7 part B: it travels under the sand and surfaces near the ship (the Burrower)
     * @param crawl M7 part C: its head crawls a wave's ground path (the Threadcrawler; the wave's {@code crawl})
     * @param laneStrafe M7 part E: it drops to a lane and strafes across the screen (the Ghost Drone)
     * @param stride M7 part F: a walking boss's passes across its halted arena (the Revenant Walker)
     * @param breach M7 part H: a breaching boss's cycle under its crawling arena (the Dust Colossus)
     * @param rise M8 part D: it rises out of the deep water below the play plane, glides across and sinks away (the
     *     Abyss Ray)
     * @param coil M8 part E: it orbits the ship in a closed, armoured shell, spiralling in, then uncoils and fires (the
     *     Spiral Nautilus)
     * @param lunge M8 part F: it hangs drifting by its lure and lunges at the ship that comes close, rings and retreats
     *     (the Glow Angler)
     */
    record Movement(
            Optional<Snake> snake,
            Optional<Swoop> swoop,
            Optional<Straight> straight,
            Optional<Hover> hover,
            Optional<Orbit> orbit,
            Optional<Dive> dive,
            Optional<Terrain> terrain,
            Optional<Strafe> strafe,
            Optional<SpiralOut> spiralOut,
            Optional<Drift> drift,
            Optional<Sine> sine,
            Optional<Walk> walk,
            Optional<PathMove> path,
            Optional<Ambush> ambush,
            Optional<Flock> flock,
            Optional<Anchored> anchored,
            Optional<Chase> chase,
            Optional<Burrow> burrow,
            Optional<Crawl> crawl,
            Optional<LaneStrafe> laneStrafe,
            Optional<Stride> stride,
            Optional<Breach> breach,
            Optional<Rise> rise,
            Optional<Coil> coil,
            Optional<Lunge> lunge) {}

    /**
     * M8 part F, the lunge from a lure (design/enemies/naval/glow-angler; m8f-gaps.md §1.1, §5 F2): it hangs drifting
     * with the water at its speed (a field's current, else its path's way); when the ship's centre comes within {@code
     * trigger} px of its centre (its lure, at most 48 px from the body, lies inside that: touching the lure needs no test
     * of its own) it dives at {@code speed} px/s at where the ship was then, stopping {@code stop_short} px short of that
     * point or after {@code seconds}; there its {@code ring} attack fires once and it retreats away from the ship at
     * {@code retreat_speed} px/s for {@code retreat_seconds}, then drifts again; it re-arms the ring attack's {@code
     * interval} after the ring. The ring is the lunge's, not a gun.
     */
    record Lunge(
            double trigger,
            double speed,
            double seconds,
            double stopShort,
            double retreatSpeed,
            double retreatSeconds) {
        public Lunge {
            Check.notNegative("stop_short", stopShort);
            Check.that(trigger > stopShort, "trigger: beyond stop_short, was " + trigger);
            Check.positive("speed", speed);
            Check.positive("seconds", seconds);
            Check.notNegative("retreat_speed", retreatSpeed);
            Check.notNegative("retreat_seconds", retreatSeconds);
        }

        /** The lunge with another trigger distance, px (a difficulty's {@link LungeChange}). */
        public Lunge withTrigger(double px) {
            return new Lunge(px, speed, seconds, stopShort, retreatSpeed, retreatSeconds);
        }
    }

    /**
     * M8 part E, the coiling orbit (design/enemies/naval/spiral-nautilus; m8e-gaps.md §1.3, §5 E7–E8): closed in its
     * armoured shell it spirals in round the ship from {@code outer} to {@code inner} px over {@code closing} s at its
     * speed (never closer than {@code inner}); then it uncoils: {@code opening} s opening (still armoured, its crown's
     * tell the last {@code tell} s of it) and {@code open} s open (hittable, its spiral firing) at {@code open_speed}
     * px/s; then it closes, breaks outward and spirals in again. A wave's units orbit in turn the other way round, each
     * cycle {@code offset} s after the one before; they leave after their wave's hold.
     */
    record Coil(
            double outer,
            double inner,
            double closing,
            double opening,
            double tell,
            double open,
            double openSpeed,
            double offset) {
        public Coil {
            Check.positive("inner", inner);
            Check.that(outer >= inner, "outer: at least inner, was " + outer);
            Check.positive("closing", closing);
            Check.notNegative("opening", opening);
            Check.that(tell >= 0 && tell <= opening, "tell: within the opening, was " + tell);
            Check.positive("open", open);
            Check.positive("open_speed", openSpeed);
            Check.notNegative("offset", offset);
        }

        /** One cycle, s: spiralling in, opening and open. */
        public double cycle() {
            return closing + opening + open;
        }

        /** The cycle with another open window, s (a difficulty's {@link CoilChange}). */
        public Coil withOpen(double seconds) {
            return new Coil(outer, inner, closing, opening, tell, seconds, openSpeed, offset);
        }
    }

    /**
     * M8 part E, a ring that slows (design/enemies/naval/siren; m8e-gaps.md §1.2, §5 E2, E4): a {@code ring} attack with
     * this block sends a ring of sound, not bullets: it expands from the sac at the attack's {@code speed} (not the
     * water's × 0.7) to {@code radius} px, its band {@code band} px wide behind its edge; it deals no damage. A hull it
     * reaches with a clear line from the sac is slowed to {@code factor} of its speed and acceleration for {@code
     * seconds}, then immune to slows for {@code immunity} s. The attack's {@code tell} is the sac's glow before each
     * ring, its {@code first_shot_delay} the first ring's time after the unit enters, its {@code interval} the time
     * between two rings.
     */
    record Slow(double radius, double band, double factor, double seconds, double immunity) {
        public Slow {
            Check.positive("radius", radius);
            Check.positive("band", band);
            Check.that(factor > 0 && factor <= 1, "factor: a share of the speed above 0 and at most 1, was " + factor);
            Check.positive("seconds", seconds);
            Check.notNegative("immunity", immunity);
        }

        /** The slow with a difficulty's {@link SlowChange} applied. */
        public Slow with(SlowChange change) {
            return new Slow(
                    radius,
                    band,
                    change.factor().orElse(factor),
                    change.seconds().orElse(seconds),
                    change.immunity().orElse(immunity));
        }
    }

    /**
     * M8 part D, a rise (design/enemies/naval/abyss-ray, Behaviour; gap check m8d-gaps.md §5 D2): from its entry it is a
     * shadow on {@code sub} for {@code shadow} s, growing in the ship's lane (the ship's x at its entry, clamped) {@code
     * ahead} px above the ship (the tell: only {@code anti-sub} fire and the Smart Bomb reach it, no contact); it rises
     * over {@code rise} s, its layer flipping to {@code air} at the rise's middle (no contact damage until the rise
     * ends); it glides {@code glide} s at {@code speed} px/s toward the far side, its attack firing from the rise's end;
     * then it sinks over {@code sink} s (back to {@code sub} at the sink's middle) and is gone (escaped). A {@code carrier
     * + escorts} wave's escorts stay unspawned until the rise's middle and fan out from it (gap check D3).
     */
    record Rise(double shadow, double rise, double glide, double speed, double sink, double ahead) {
        public Rise {
            Check.positive("shadow", shadow);
            Check.positive("rise", rise);
            Check.positive("glide", glide);
            Check.positive("speed", speed);
            Check.positive("sink", sink);
            Check.notNegative("ahead", ahead);
        }

        /** The whole visit from the shadow's start to the sink's end, s. */
        public double visit() {
            return shadow + rise + glide + sink;
        }
    }

    /**
     * M7 part F, a walking boss's passes (design/enemies/bosses/revenant-walker, Passes): it crosses its halted arena
     * in passes from edge to edge, entering from the {@code edges} in turn ({@code top}, {@code bottom}, {@code left},
     * {@code right}; the first {@code top}, its intro), a vertical pass on one of {@code lanes_x} (px from the left
     * edge), a horizontal one on one of {@code lanes_y} (px below the top edge); {@code off_screen} s off the screen between
     * two passes under the next edge's warning; a turn round takes {@code turn} s; it walks at {@code speeds} px/s by
     * phase (the last for later phases). {@code beside_lane}: in its first phase it takes the lane beside the ship's;
     * {@code turn_in_place}: in its last phase it turns round on the spot at a pass's end instead of leaving.
     */
    record Stride(
            List<Double> lanesX,
            List<Double> lanesY,
            List<String> edges,
            double offScreen,
            double turn,
            List<Double> speeds,
            boolean besideLane,
            boolean turnInPlace) {
        /** The entry edges a stride knows. */
        public static final List<String> EDGES = List.of("top", "bottom", "left", "right");

        public Stride {
            lanesX = List.copyOf(lanesX);
            lanesY = List.copyOf(lanesY);
            edges = List.copyOf(edges);
            speeds = List.copyOf(speeds);
            Check.notEmpty("lanes_x", lanesX);
            Check.notEmpty("lanes_y", lanesY);
            Check.notEmpty("edges", edges);
            Check.notEmpty("speeds", speeds);
            Check.that(edges.getFirst().equals("top"), "edges: the first pass enters from the top (its intro)");
            edges.forEach(
                    edge -> Check.that(EDGES.contains(edge), "edges: top, bottom, left or right, was '" + edge + "'"));
            lanesX.forEach(x -> Check.that(x > 0 && x < PlayField.WIDTH, "lanes_x: on the play field, was " + x));
            lanesY.forEach(y -> Check.that(y > 0 && y < PlayField.HEIGHT, "lanes_y: on the play field, was " + y));
            speeds.forEach(v -> Check.positive("speeds", v));
            Check.notNegative("off_screen", offScreen);
            Check.positive("turn", turn);
        }
    }

    /**
     * M7 part F, a walking boss's front plate (design/enemies/bosses/revenant-walker): a box of {@code hitbox} (its
     * width across the heading, its height along it) {@code ahead} px in front of {@code part}'s centre along its
     * heading; every shot meeting it glances off and it stops the Ion Beam while the part lives (no HP, not in the
     * bar).
     */
    record Plate(String part, Size hitbox, double ahead) {
        public Plate {
            Check.notNegative("ahead", ahead);
        }
    }

    /**
     * M7 part F, a walking boss's rear flame cone (design/enemies/bosses/revenant-walker, Attacks): from its flamer
     * {@code behind} px behind its centre, {@code length} px long, {@code half_angle}° each side of its axis
     * (against its heading); it starts when the ship or Rook is behind it within {@code length + reach_behind} px,
     * at most once every {@code cooldown} s: a {@code tell} s tell (from its second phase on swivelling up to {@code
     * swivel}° toward the ship), then a {@code burn} s burn hitting for the {@code bullet} class's damage once per
     * burn; after the burn its vents (its spots {@code while: open}) open for {@code vents} s. Round 43 (K.a of round 42):
     * from its second phase on a burn keeps turning toward the ship at up to {@code track} °/s within its swivel (none
     * without it).
     */
    record Cone(
            double length,
            double halfAngle,
            double reachBehind,
            double cooldown,
            double tell,
            double burn,
            double vents,
            double swivel,
            String bullet,
            double behind,
            Optional<Double> track) {
        public Cone {
            track.ifPresent(t -> Check.notNegative("track", t));
            Check.positive("length", length);
            Check.that(halfAngle > 0 && halfAngle < 90, "half_angle: degrees, 0-90, was " + halfAngle);
            Check.notNegative("reach_behind", reachBehind);
            Check.notNegative("cooldown", cooldown);
            Check.notNegative("tell", tell);
            Check.positive("burn", burn);
            Check.notNegative("vents", vents);
            Check.notNegative("swivel", swivel);
        }
    }

    /**
     * Round 44 (the user in round 43, W.a; design/enemies/bosses/revenant-walker, Attacks): a walking boss's
     * back-mounted rail lance. It wakes once every part of {@code after} is destroyed, or from phase {@code
     * from_phase} (a phase's name) on; then, while it walks on the screen, it charges for {@code charge} s (the line from
     * its lance, {@code behind} px behind its centre, through where the ship was) and fires along that line for {@code
     * fire} s, {@code width} px wide, hitting for the {@code bullet} class's damage once a shot; a charge every {@code
     * interval} s ({@code last_interval} in its last phase).
     */
    record Lance(
            List<String> after,
            Optional<String> fromPhase,
            double charge,
            double fire,
            double interval,
            double lastInterval,
            double width,
            String bullet,
            double behind) {
        public Lance {
            after = List.copyOf(after);
            Check.that(!after.isEmpty() || fromPhase.isPresent(), "a rail lance wakes after parts or from a phase");
            Check.positive("charge", charge);
            Check.positive("fire", fire);
            Check.that(interval >= charge + fire, "interval: holds the charge and the shot, was " + interval);
            Check.that(
                    lastInterval >= charge + fire, "last_interval: holds the charge and the shot, was " + lastInterval);
            Check.positive("width", width);
        }
    }

    /**
     * Round 44 (the user in round 43, W.e): a walking boss's stomp shockwave: the ship or Rook within {@code reach} px of
     * its centre starts a {@code tell} s tell (at most once every {@code cooldown} s), then a ring expands to {@code
     * radius} px over {@code expand} s, hitting for the {@code bullet} class's damage once a stomp.
     */
    record Stomp(double reach, double radius, double tell, double expand, double cooldown, String bullet) {
        public Stomp {
            Check.positive("reach", reach);
            Check.that(radius >= reach, "radius: at least its reach, was " + radius);
            Check.notNegative("tell", tell);
            Check.positive("expand", expand);
            Check.that(cooldown >= tell + expand, "cooldown: holds the tell and the ring, was " + cooldown);
        }
    }

    /**
     * M7 part F, homing missiles (design/enemies/bosses/revenant-walker, Attacks): an aimed attack's shots fly at its
     * speed turning toward the ship at up to {@code turn_rate} °/s, fizzle after {@code life} s, have {@code hp} (every
     * shot reaching the air hits them), at most {@code most} alive at once; the firing part's hatch opens {@code
     * hatch} s before each salvo (the tell).
     */
    record Homing(double turnRate, double life, double hp, int most, double hatch) {
        public Homing {
            Check.notNegative("turn_rate", turnRate);
            Check.positive("life", life);
            Check.positive("hp", hp);
            Check.positive("most", most);
            Check.notNegative("hatch", hatch);
        }
    }

    /**
     * M7 part E, the {@code lane_strafe} movement (design/enemies/air/ghost-drone): a front entry drops straight down
     * at {@code descent_speed} px/s to its lane ({@code lane_depth} px below the top edge, a wave's lanes {@code
     * lane_spacing} px apart), banks 90° and strafes across at {@code strafe_speed} px/s, leaving through the far
     * side edge; a line abreast splits half left, half right; a side entry (a pincer, warned at its edge) strafes in
     * at its lane. The hook {@code second_pass} (hard): a line abreast's survivors turn at the edge and strafe back
     * once.
     */
    record LaneStrafe(double descentSpeed, double strafeSpeed, Span laneDepth, double laneSpacing) {
        public LaneStrafe {
            Check.positive("descent_speed", descentSpeed);
            Check.positive("strafe_speed", strafeSpeed);
            Check.that(
                    laneDepth.min() > 0 && laneDepth.max() < PlayField.HEIGHT,
                    "lane_depth: px below the top edge, on the play field");
            Check.positive("lane_spacing", laneSpacing);
        }
    }

    /**
     * M7 part C, the {@code crawl} movement (design/enemies/ground/threadcrawler): a ground chain's head crawls its
     * wave's ground path ({@code crawl} points in the walls' rows) at {@code speed} px/s along the ground, on top of
     * the scroll; the body follows by its segment chain.
     */
    record Crawl(double speed) {
        public Crawl {
            Check.positive("speed", speed);
        }
    }

    /**
     * M5 part F, the {@code chase} movement (design/enemies/air/lamprey): from its entry it steers
     * toward the ship's centre at {@code speed} px/s, turning at most {@code turn_rate} °/s, for
     * {@code seconds}; then it flies straight on along its heading at its speed.
     */
    record Chase(double speed, double turnRate, double seconds) {
        public Chase {
            Check.positive("speed", speed);
            Check.positive("turn_rate", turnRate);
            Check.positive("seconds", seconds);
        }
    }

    /**
     * M5 part F, the {@code latch} block (design/enemies/air/lamprey; user decisions F3 and F4 = a):
     * {@code slots} units may be latched at once (the hook {@code latch_slots} instead); the hull
     * {@code spots} it sits at; the {@code damage_factor} on the damage it takes while latched; the
     * {@code shake} that throws every latched unit off; the {@code stun} s and the {@code throw} px/s
     * of its way off the hull, after which it flees to the nearer side edge.
     */
    record Latch(
            int slots,
            Spots spots,
            double damageFactor,
            Shake shake,
            double stun,

            @com.fasterxml.jackson.annotation.JsonProperty("throw")
            double throwSpeed) {
        public Latch {
            Check.positive("slots", slots);
            Check.positive("damage_factor", damageFactor);
            Check.positive("stun", stun);
            Check.notNegative("throw", throwSpeed);
        }
    }

    /**
     * M5 part F: a latch's hull spots, each {@code [dx, dy]} px from the ship's centre with y up (as the
     * weapons' muzzles): {@code left} and {@code right} for a touch from the side, {@code tail} for one
     * within 45° of straight down.
     */
    record Spots(Point left, Point right, Point tail) {}

    /** M5 part F: {@code reversals} reversals of the commanded horizontal direction within {@code within} s. */
    record Shake(int reversals, double within) {
        public Shake {
            Check.that(reversals >= 2, "reversals: at least 2, was " + reversals);
            Check.positive("within", within);
        }
    }

    /**
     * M5 part E, an arena boss's arrival (design/enemies/bosses/harbour-kraken): no descent and no
     * hover; its centre stops {@code y} px below the top edge when the scroll halts in its arena. It
     * lies on the ground layer that far up the scroll from the halt, scrolls in with the ground
     * (invulnerable, without its bar) and engages at the halt. Round 40 (the reworked Harbour Kraken): the
     * centre may lie above the top edge (a negative {@code y}; Platform Tiamat at -46), as long as the boss's
     * hit box reaches onto the play field.
     */
    record Anchored(double y) {
        public Anchored {
            Check.that(
                    Double.isFinite(y) && y < vanguard.sim.PlayField.HEIGHT, "y: on or above the play field, was " + y);
        }
    }

    /**
     * M5 part E, surfacing and submerging (design/enemies/naval/driftjelly; the stated defaults of
     * 2026-10-08): every {@code every} {@code [min, max]} s, drawn per unit from its own seed on the
     * simulation's real steps, it swaps between the surface ({@code ground}) and {@code sub} over
     * {@code swap} s, its layer flipping at the swap's middle; a {@code field} wave starts the share
     * {@code start} of its units submerged.
     */
    record Submerge(Span every, double swap, double start) {
        public Submerge {
            Check.positive("every", every.min());
            Check.positive("swap", swap);
            Check.share("start", start);
            Check.that(swap < every.min(), "swap: shorter than the least time between swaps");
        }
    }

    /**
     * M5 part D, a cloak (design/enemies/air/wraith): the stat block's {@code layer} is the cloaked
     * one; from its decloak at its hold point on it is on {@code layer}, the {@code flash} (s) of
     * its decloak starting there, its gun silent until the flash ends.
     */
    record Cloak(String layer, double flash) {
        public Cloak {
            Layers.of(layer);
            Check.notNegative("flash", flash);
        }
    }

    /**
     * M5 part D, the path of a {@code rear ambush} wave's unit (design/enemies/air/wraith): cloaked
     * straight down its lane at the stat block's {@code speed} past the ship and off the bottom
     * edge, {@code gap} s below it, back up to its {@code hover.y} at that speed, the decloak and its
     * {@code hover.seconds} hold, then out up the nearer side lane ({@code lane} px from that edge, a
     * tie to the left) at its {@code straight.speed}.
     */
    record Ambush(double gap, double lane) {
        public Ambush {
            Check.notNegative("gap", gap);
            Check.notNegative("lane", lane);
        }
    }

    /**
     * M5 part D, a flock (design/enemies/air/mote-swarm, user decision D8 = a): a {@code swarm}
     * wave's members keep {@code separation} px apart, steer by {@code alignment} and {@code
     * cohesion} among the members within {@code radius} px and toward the wave's leader point by
     * {@code leader} (the weights), at {@code speed} px/s ({@code dive_speed} after a loop-back),
     * turning at most {@code turn_rate} °/s; at most {@code max} members (24). M8 part C (the Glassfin): a {@code
     * sway} across the leader's route (the ribbon) and a {@code ram} (members breaking off to dive at the ship's lane).
     */
    record Flock(
            double separation,
            double radius,
            double alignment,
            double cohesion,
            double leader,
            double speed,
            double diveSpeed,
            double turnRate,
            int max,
            Optional<Sway> sway,
            Optional<Ram> ram) {
        public Flock {
            Check.positive("separation", separation);
            Check.that(radius >= separation, "radius: the neighbourhood reaches at least the separation");
            Check.notNegative("alignment", alignment);
            Check.notNegative("cohesion", cohesion);
            Check.positive("leader", leader);
            Check.positive("speed", speed);
            Check.positive("dive_speed", diveSpeed);
            Check.positive("turn_rate", turnRate);
            Check.that(max >= 1 && max <= 24, "max: 1 to 24 members");
        }
    }

    /**
     * Part F: the head of a segment chain flies the wave's authored path at {@code speed} px/s. M8 part C (the Eel
     * Swarm's {@code snake}): with a {@code sway}, its point swings across the path (a sine about it).
     */
    record PathMove(double speed, Optional<Sway> sway) {
        /** Without M8 part C's sway. */
        public PathMove(double speed) {
            this(speed, Optional.empty());
        }

        public PathMove {
            Check.positive("speed", speed);
        }
    }

    /**
     * M8 part C (design/enemies/naval/eel-swarm, glassfin): a sine of {@code amplitude} px across the route at {@code
     * frequency} Hz, about the authored path (the Eel Swarm's head) or a flock's leader route (the Glassfin's ribbon).
     */
    record Sway(double amplitude, double frequency) {
        public Sway {
            Check.positive("amplitude", amplitude);
            Check.positive("frequency", frequency);
        }
    }

    /**
     * M8 part C (design/enemies/naval/glassfin, the ram): after {@code after} s on the screen each member of a flock
     * draws, from its own seed, whether it breaks off; {@code share} of a school does. A member that does glints
     * {@code tell} s (the tell), dives {@code delay} s after the glint (step C3: the README's "0.5 s after a 0.3 s
     * glint tell", the simulation's wait after the tell's end), straight down the ship's lane at {@code speed} px/s with
     * no homing, and leaves at the bottom edge (none loops back).
     */
    record Ram(double after, double share, double tell, double delay, double speed) {
        public Ram {
            Check.notNegative("after", after);
            Check.that(share >= 0 && share <= 1, "share: 0 to 1, was " + share);
            Check.positive("tell", tell);
            Check.notNegative("delay", delay);
            Check.positive("speed", speed);
        }
    }

    /**
     * M8 part C (design/enemies/naval, Returning units under water; gap check D2): a unit's changes in a wave or a nest
     * that enters under water, applied when the level's script is built (by the entry time's section). {@code layer}
     * the layer it flies on instead (the Driftjelly's {@code air}, the play plane); {@code submerge: false} drops its
     * surface/submerge swap; {@code drift} its terrain drift instead, px/s (the Reef Spitter's 0: no raft); {@code look}
     * how the game draws it ({@code floor}: on a rock-and-kelp base instead of its raft; presentation only).
     */
    record Underwater(
            Optional<String> layer, Optional<Boolean> submerge, Optional<Double> drift, Optional<String> look) {
        /** The Reef Spitter's base on the sea floor, instead of its raft. */
        public static final String LOOK_FLOOR = "floor";

        public Underwater {
            layer.ifPresent(Layers::of);
            submerge.ifPresent(s -> Check.that(!s, "submerge: only false (the swap dropped under water)"));
            drift.ifPresent(d -> Check.notNegative("drift", d));
            look.ifPresent(l -> Check.that(LOOK_FLOOR.equals(l), "look: only 'floor', was '" + l + "'"));
        }
    }

    /**
     * Part F: the body of a segment chain (the Coilwyrm) between its head and its tail, following
     * the head's path history: {@code segments} segments whose sprites taper from {@code size[0]}
     * to {@code size[1]} px, hit boxes {@code hitboxShare} of the sprite, {@code spacing} × the
     * segment length apart, each with {@code hp} and paying {@code bounty}; {@code contact} the
     * contact class of the segments and the tail; {@code regrow} the rear part's new head after a
     * cut (once per chain); {@code popInterval} s per segment of the chained death. M7 part C (the Threadcrawler):
     * {@code width} the segments' sprite across the path, px (their hit box {@code width × share} by {@code length
     * × share}; square if not given), and {@code cut} instead of {@code regrow}: a cut chain's rear part dies.
     */
    record SegmentChain(
            int segments,
            List<Double> size,
            Optional<Double> width,
            double hitboxShare,
            double spacing,
            double hp,
            int bounty,
            Tier contact,
            Optional<Regrow> regrow,
            Optional<Cut> cut,
            double popInterval) {
        public SegmentChain {
            Check.that(
                    regrow.isPresent() != cut.isPresent(), "a chain's rear part regrows a head (regrow) or dies (cut)");
            width.ifPresent(w -> Check.positive("width", w));
            Check.positive("segments", segments);
            Check.that(size.size() == 2, "size is [first, last]");
            Check.positive("hitbox_share", hitboxShare);
            Check.positive("spacing", spacing);
            Check.positive("hp", hp);
            Check.notNegative("bounty", bounty);
            Check.positive("pop_interval", popInterval);
        }
    }

    /**
     * M7 part C (the Threadcrawler): a cut chain's rear part stops, writhes and dies {@code delay} s after the cut, from
     * the cut backwards at the chain's pop interval (no new head; the front part crawls on). M8 part C (the Eel Swarm):
     * or it scatters: it keeps its heading at {@code scatter} px/s for {@code seconds} s, firing nothing, then dies from
     * the cut backwards (no regrowth); a cut gives {@code delay} or {@code scatter} with {@code seconds}.
     */
    record Cut(Optional<Double> delay, Optional<Double> scatter, Optional<Double> seconds) {
        /** M7 part C's cut: the rear part dies {@code delay} s after it. */
        public Cut(double delay) {
            this(Optional.of(delay), Optional.empty(), Optional.empty());
        }

        public Cut {
            Check.that(
                    delay.isPresent() != (scatter.isPresent() || seconds.isPresent()),
                    "a cut's rear part dies after a delay, or scatters (scatter and seconds)");
            Check.that(scatter.isPresent() == seconds.isPresent(), "a scattering cut has its scatter and its seconds");
            delay.ifPresent(d -> Check.positive("delay", d));
            scatter.ifPresent(v -> Check.positive("scatter", v));
            seconds.ifPresent(v -> Check.positive("seconds", v));
        }

        /** M8 part C: whether the cut's rear part scatters (the Eel Swarm's) instead of dying after a delay. */
        public boolean scatters() {
            return scatter.isPresent();
        }
    }

    /** A cut chain's rear part grows a new head over {@code seconds}, flying at {@code speed} px/s. */
    record Regrow(double seconds, double speed, double hp, int bounty) {
        public Regrow {
            Check.positive("seconds", seconds);
            Check.positive("speed", speed);
            Check.positive("hp", hp);
            Check.notNegative("bounty", bounty);
        }
    }

    /**
     * Part F: a {@code laser-sweep}'s beam, {@code length} × {@code width} px from the eye, sweeping
     * {@code arc} ° over {@code duration} s after a {@code telegraph} of s. The eye is at {@code
     * origin} {@code [in, down]} px from the unit's centre: toward the field (mirrored on the right
     * edge) and down the screen; the centre when it is left out. The beam, its hit test and its
     * telegraph start there. Round 41 (a boss's sweep only, the Siege Spire's; the user's playtest 2026-10-19:
     * "you are forced to keep moving"): with {@code lead} ° the arc starts that far beside the ship's bearing on the
     * side the beam comes from (the telegraph's line) and sweeps through the ship; left out, centred on the bearing.
     */
    record Sweep(
            double arc,
            double duration,
            double telegraph,
            double length,
            double width,
            Optional<Point> origin,
            Optional<Double> lead) {
        public Sweep {
            Check.positive("arc", arc);
            Check.positive("duration", duration);
            Check.notNegative("telegraph", telegraph);
            Check.positive("length", length);
            Check.positive("width", width);
            lead.ifPresent(degrees -> Check.that(degrees >= 0 && degrees <= arc, "lead: 0 to the arc's °"));
        }
    }

    /** Planned (part D): straight down the screen at {@code speed} px/s, with no intent. Read, not flown yet. */
    record Drift(double speed) {
        public Drift {
            Check.positive("speed", speed);
        }
    }

    /** Planned (part D): a side-to-side offset of {@code amplitude} px, {@code period} s per swing. Read, not flown yet. */
    record Sine(double amplitude, double period) {
        public Sine {
            Check.notNegative("amplitude", amplitude);
            Check.positive("period", period);
        }
    }

    /**
     * Planned (part D): a walker on a ground path at {@code speed} px/s, turning at {@code turn_rate}
     * °/s, {@code stride} px of ground per walk cycle. Read, not flown yet.
     */
    record Walk(double speed, double turnRate, double stride) {
        public Walk {
            Check.positive("speed", speed);
            Check.positive("turn_rate", turnRate);
            Check.positive("stride", stride);
        }
    }

    /** A convoy unit turns across the screen at a height in {@code y}, px from the top. */
    record Strafe(Span y) {}

    /**
     * Spirals out from its release point for {@code seconds} ({@code turns} per second, the radius
     * growing {@code growth} px/s, the release point drifting down {@code drift} px/s), then flies on
     * along its outward angle turned downward, bouncing off the side edges up to {@code ricochets} times.
     */
    record SpiralOut(double seconds, double turns, double growth, double drift, int ricochets) {
        public SpiralOut {
            Check.positive("seconds", seconds);
            Check.notNegative("ricochets", ricochets);
        }
    }

    /**
     * Enters to a height of {@code y} px below the top of the play field, pauses for {@code pause}
     * seconds (its telegraph), then dives at {@code speed} px/s towards where the player was when
     * the pause ended, and flies on off the screen.
     *
     * @param fireAfter seconds into the dive at which it fires, unless it passes the player's
     *     height first
     */
    record Dive(Span y, double pause, double speed, double fireAfter) {
        public Dive {
            Check.notNegative("pause", pause);
            Check.positive("speed", speed);
            Check.positive("fire_after", fireAfter);
        }
    }

    /**
     * Fixed to the ground layer, moving only with the scroll; M5 part E: with a {@code drift}, px/s
     * along its nest's {@code current} on top of the scroll (a raft on the water, the Reef Spitter).
     */
    record Terrain(Optional<Double> drift) {
        public Terrain {
            drift.ifPresent(d -> Check.positive("drift", d));
        }
    }

    /** Along an authored path, {@code spacing} seconds between two units. */
    record Snake(double spacing) {
        public Snake {
            Check.positive("spacing", spacing);
        }
    }

    /**
     * An entry curve of {@code radius} px, reaching {@code topSpeed} px/s. M7 part B (design/enemies/air/dust-kite):
     * with a {@code trigger} it is the Dust Kite's final swoop instead, started once the unit is within that many px
     * above the ship's row: one shallow curve across the ship's lane, aimed at the ship's x when it starts (no
     * homing), at {@code topSpeed} (the hook {@code swoop_speed} instead); then it glides on off the bottom or a
     * side. Mapped for the simulation by {@link MarsRules#kite}.
     */
    record Swoop(Optional<Span> radius, Optional<Double> topSpeed, Optional<Double> trigger) {
        public Swoop {
            topSpeed.ifPresent(speed -> Check.positive("top_speed", speed));
            trigger.ifPresent(px -> Check.positive("trigger", px));
            Check.that(trigger.isEmpty() || topSpeed.isPresent(), "a triggered swoop has its top_speed");
        }
    }

    /**
     * M7 part B, the {@code burrow} movement (design/enemies/ground/burrower; M7 = a and the user's Q1 = a of
     * 2026-10-14): it travels under the sand on the {@code buried} layer at {@code swim} px/s on the screen (screen-fixed
     * while it travels, bulges and is surfaced: it works against the scroll) to a spot picked relative to the ship
     * when the bulge starts ({@code spot}), bulges there for {@code bulge} s, is a {@code ground} unit for {@code
     * surfaced} s (its attack's rings at the {@code rings}' times after it breaks out), dives over {@code dive} s
     * ({@code buried} from the dive's middle) and travels {@code travel} s (seeded) to its next spot; after {@code
     * surfacings} surfacings it travels off the bottom edge under the sand (an escape, no bounty). A wave's units
     * surface at least {@code stagger} s apart and at most {@code max_up} at once (a unit that would break it waits
     * buried). Mapped for the simulation by {@link MarsRules#burrower}.
     */
    record Burrow(
            double swim,
            Span travel,
            Spot spot,
            double bulge,
            double surfaced,
            double dive,
            int surfacings,
            List<Ring> rings,
            double stagger,
            int maxUp) {
        public Burrow {
            Check.positive("swim", swim);
            Check.positive("travel", travel.min());
            Check.positive("bulge", bulge);
            Check.positive("surfaced", surfaced);
            Check.positive("dive", dive);
            Check.positive("surfacings", surfacings);
            Check.notEmpty("rings", rings);
            rings = List.copyOf(rings);
            for (Ring ring : rings) {
                Check.that(ring.at() < surfaced, "rings: each fires while it is surfaced, before " + surfaced + " s");
            }
            Check.notNegative("stagger", stagger);
            Check.positive("max_up", maxUp);
        }
    }

    /**
     * M7 part B: where a Burrower surfaces, picked when the bulge starts: {@code ahead} {@code [min, max]} px above the
     * ship's centre, within {@code side} px either side of its x, {@code edge} px inside the play field's side edges and
     * at least {@code clear} px from the ship's centre.
     */
    record Spot(Span ahead, double side, double edge, double clear) {
        public Spot {
            Check.positive("ahead", ahead.min());
            Check.notNegative("side", side);
            Check.notNegative("edge", edge);
            Check.positive("clear", clear);
        }
    }

    /**
     * M7 part B: a Burrower's ring {@code at} s after it breaks out, of {@code count} bullets (its attack's count, or
     * the hook's {@code attacks.<name>.count}, if not given).
     */
    record Ring(double at, Optional<Integer> count) {
        public Ring {
            Check.notNegative("at", at);
            count.ifPresent(n -> Check.positive("count", n));
        }
    }

    /** Straight at {@code speed} px/s. */
    record Straight(double speed) {
        public Straight {
            Check.positive("speed", speed);
        }
    }

    /**
     * Hovers for {@code seconds} at a height of {@code y} px from the top of the play field; without
     * {@code seconds} it holds until it is killed (a boss). Part F: a unit entering from a side
     * edge hovers {@code edgeX} px from that edge, and {@code exit} {@code back} leaves through it. M8 part D
     * (design/enemies/naval/barbfin): with {@code above_ship} instead of {@code y} it stops a seeded {@code [min, max]}
     * px above the ship's y at the moment it stops (the Barbfin's 90–200), drifting {@code drift} px/s sideways while it
     * holds (the Barbfin's 6; 0 if not given) and turning toward the ship.
     */
    record Hover(
            Optional<Span> seconds,
            Optional<Span> y,
            Optional<Double> edgeX,
            Optional<String> exit,
            Optional<Span> aboveShip,
            Optional<Double> drift) {
        public Hover {
            drift.ifPresent(speed -> Check.notNegative("drift", speed));
            // M8 part G: a hover at its y drifts too (the Depth Hunter's, in its lower band).
            Check.that(
                    drift.isEmpty() || aboveShip.isPresent() || y.isPresent(),
                    "a hover drifts at its y or above the ship");
            Check.that(y.isPresent() != aboveShip.isPresent(), "a hover holds at a y or above_ship, one of the two");
            aboveShip.ifPresent(above -> {
                Check.notNegative("above_ship", above.min());
                Check.that(above.max() >= above.min(), "above_ship: [min, max]");
            });
            Check.that(aboveShip.isEmpty() || edgeX.isEmpty(), "a hover above the ship enters from the top, no edge_x");
            edgeX.ifPresent(x -> Check.notNegative("edge_x", x));
            exit.ifPresent(e -> Check.that(e.equals("back"), "exit must be back, was '" + e + "'"));
        }

        /** {@code y}, {@code seconds} and {@code above_ship} written as {@code [min, max]} or as one value. */
        @JsonCreator
        static Hover of(
                @com.fasterxml.jackson.annotation.JsonProperty("seconds")
                        Optional<tools.jackson.databind.JsonNode> seconds,
                @com.fasterxml.jackson.annotation.JsonProperty("y") Optional<tools.jackson.databind.JsonNode> y,
                @com.fasterxml.jackson.annotation.JsonProperty("edge_x") Optional<Double> edgeX,
                @com.fasterxml.jackson.annotation.JsonProperty("exit") Optional<String> exit,
                @com.fasterxml.jackson.annotation.JsonProperty("above_ship")
                        Optional<tools.jackson.databind.JsonNode> aboveShip,
                @com.fasterxml.jackson.annotation.JsonProperty("drift") Optional<Double> drift) {
            Optional<tools.jackson.databind.JsonNode> height = given(y == null ? Optional.empty() : y);
            Optional<tools.jackson.databind.JsonNode> above = given(aboveShip == null ? Optional.empty() : aboveShip);
            Check.that(height.isEmpty() || span(height.get()) != null, "y is a height or [min, max]");
            Check.that(above.isEmpty() || span(above.get()) != null, "above_ship is a height or [min, max]");
            Optional<tools.jackson.databind.JsonNode> time = given(seconds == null ? Optional.empty() : seconds);
            Check.that(time.isEmpty() || span(time.get()) != null, "seconds is a time or [min, max]");
            return new Hover(
                    time.map(Hover::span),
                    height.map(Hover::span),
                    edgeX == null ? Optional.empty() : edgeX,
                    exit == null ? Optional.empty() : exit,
                    above.map(Hover::span),
                    drift == null ? Optional.empty() : drift);
        }

        /** The hold's height from the top of the play field; a hover above the ship has none (M8 part D). */
        public Span heightFromTop() {
            return y.orElseThrow(() -> new IllegalStateException("a hover above the ship holds at no fixed y"));
        }

        private static Optional<tools.jackson.databind.JsonNode> given(Optional<tools.jackson.databind.JsonNode> node) {
            return node.filter(n -> !n.isNull() && !n.isMissingNode());
        }

        private static Span span(tools.jackson.databind.JsonNode node) {
            if (node.isNumber()) {
                return new Span(node.doubleValue(), node.doubleValue());
            }
            if (node.isArray() && node.size() == 2) {
                return new Span(node.get(0).doubleValue(), node.get(1).doubleValue());
            }
            return null;
        }
    }

    /** Orbits a point at {@code radius} px and {@code turnRate} °/s. */
    record Orbit(double radius, double turnRate) {
        public Orbit {
            Check.positive("radius", radius);
            Check.positive("turn_rate", turnRate);
        }
    }

    /**
     * M7 part C, a {@code link}'s tendril (design/enemies/ground/tendril-anchor; M9 = a): {@code hp} (damaged only by
     * a {@code beam}, which goes on past it), {@code dormant} s of a harmless flickering tell after the pair scrolls
     * onto the screen, then a touch in its {@code band} px contact band costs the attack's bullet class with a {@code
     * cooldown} s of its own (the ship and the escort; no push); {@code width} its strip's width, px; {@code
     * burnt_bounty} the credits for burning it (Act 1 terms). Either anchor's death breaks it for good.
     */
    record Link(double hp, double dormant, double band, double width, double cooldown, int burntBounty) {
        public Link {
            Check.positive("hp", hp);
            Check.notNegative("dormant", dormant);
            Check.positive("band", band);
            Check.positive("width", width);
            Check.positive("cooldown", cooldown);
            Check.notNegative("burnt_bounty", burntBounty);
        }
    }

    /**
     * M7 part C, a chain's pore wave (design/enemies/ground/threadcrawler): after {@code rest} s, each segment's pore
     * fires in turn head to tail (the attack's {@code interval} s apart), each glowing {@code glow} s before it fires
     * (the tell); with {@code every_other} only every other pore fires (easy).
     */
    record Pores(double rest, double glow, Optional<Boolean> everyOther) {
        public Pores {
            Check.positive("rest", rest);
            Check.positive("glow", glow);
        }

        /** Whether only every other pore fires. */
        public boolean alternate() {
            return everyOther.orElse(false);
        }
    }

    /**
     * M5 part C, a pounce's leap (design/enemies/ground/ravager): within {@code range} px of the
     * ship (centre to centre, on the screen) it leaps at the ship's position over {@code leap} s;
     * for the middle {@code air} s it is on the {@code air} layer; drawn {@code scale} times its size
     * at the apex.
     */
    record Pounce(double range, double leap, double air, double scale) {
        public Pounce {
            Check.positive("range", range);
            Check.positive("leap", leap);
            Check.notNegative("air", air);
            Check.that(air <= leap, "the air window lies inside the leap");
            Check.positive("scale", scale);
        }
    }
}
