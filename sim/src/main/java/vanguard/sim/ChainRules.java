package vanguard.sim;

import java.util.List;

/**
 * The segment chains of an {@link EnemyForce} (design/enemies/air/coilwyrm; M7 part C: the ground's, the
 * Threadcrawler): a wave's chain entering strung out behind its path's start, the chains flying, regrowing a
 * cut-off rear part's head and popping their dying members, and a member's death cutting its chain; M8 part C (design/
 * enemies/naval/eel-swarm): a water chain's cut part scattering and its head firing its own attacks.
 */
final class ChainRules {
    /** Segment chains in flight: the waves' and their regrown rear parts. */
    private static final int CHAIN_CAPACITY = 12;

    private final EnemyForce force;
    private final WaveSchedule waves;
    private final Pool<Enemy> enemies;
    private final SimEvents events;
    private final EnemyForce.Escapes escapes;
    private final EnemyFire guns;
    private final Pool<Chain> chains = new Pool<>(CHAIN_CAPACITY, Chain::new, Chain[]::new);
    /** Chains started in this attempt, for their serials. */
    private int chainsSpawned;
    /** Per enemy kind, its chain's segment, tail and regrown head kinds; -1 for others. */
    private final int[] segmentKinds;

    private final int[] tailKinds;
    private final int[] regrownKinds;

    ChainRules(
            EnemyForce force,
            List<EnemySpec> kinds,
            WaveSchedule waves,
            Pool<Enemy> enemies,
            SimEvents events,
            EnemyForce.Escapes escapes,
            EnemyFire guns) {
        this.force = force;
        this.waves = waves;
        this.enemies = enemies;
        this.events = events;
        this.escapes = escapes;
        this.guns = guns;
        segmentKinds = new int[kinds.size()];
        tailKinds = new int[kinds.size()];
        regrownKinds = new int[kinds.size()];
        for (int k = 0; k < kinds.size(); k++) {
            var chain = kinds.get(k).chain();
            segmentKinds[k] = chain.isPresent() ? kinds.indexOf(chain.get().segment()) : -1;
            tailKinds[k] = chain.isPresent() ? kinds.indexOf(chain.get().tail()) : -1;
            regrownKinds[k] = chain.isPresent() && chain.get().regrown() != null
                    ? kinds.indexOf(chain.get().regrown())
                    : -1;
        }
    }

    /** Back to the level start: no chains. */
    void reset() {
        Pools.clear(chains);
        chainsSpawned = 0;
    }

    Pool<Chain> chains() {
        return chains;
    }

    /**
     * A wave's segment chain enters: its head (the unit that counts as the kill, carrying the
     * wave's pickup), its segments and its tail, strung out behind the path's start; members the
     * pool has no room for are left out.
     */
    void spawnChain(Spawn spawn) {
        Chain chain = chains.obtain();
        if (chain == null) {
            force.nextSerial();
            return;
        }
        EnemySpec head = spawn.enemy();
        EnemySpec.ChainSpec spec = head.chain().orElseThrow();
        if (head.crawl().isPresent()) {
            // M7 part C: a ground chain crawls its path in ground space (the Threadcrawler).
            chain.startGround(spec, chainsSpawned++, head.crawl().get(), force.groundScroll(), waves.lastEntry());
        } else {
            chain.start(spec, chainsSpawned++, spawn, regrownKinds[spawn.kind()], waves.lastEntry());
        }
        // M8 part C: its wave's pickup may drop only with a head-first kill.
        chain.headFirstDrop(waves.lastHeadFirst());
        int members = spec.members();
        for (int i = 0; i < members; i++) {
            Enemy member = enemies.obtain();
            if (member == null) {
                break;
            }
            boolean tail = i == members - 1;
            EnemySpec part = i == 0 ? head : tail ? spec.tail() : spec.segment();
            int kind = i == 0 ? spawn.kind() : tail ? tailKinds[spawn.kind()] : segmentKinds[spawn.kind()];
            Hitbox box = i == 0 || tail ? part.hitbox() : spec.segmentBoxes().get(i - 1);
            member.link(chain, i, part, kind, box, force.nextSerial(), 0, 0, 0);
            if (i == 0) {
                member.carry(spawn.carried());
            }
            chain.member(i, member);
        }
        chain.settle();
    }

    /**
     * The chains fly a step: a rear part whose new head has grown gets it (and lunges at the ship),
     * the dying members pop one by one, and a chain that has flown its course leaves with its
     * members (its wave's head counting as escaped).
     */
    void moveChains(Ship ship, boolean firing) {
        for (int c = chains.size() - 1; c >= 0; c--) {
            Chain chain = chains.get(c);
            if (chain.regrowDue()) {
                Enemy head = null;
                if (chain.alive()) {
                    head = enemies.obtain();
                    if (head != null) {
                        int kind = chain.regrownKind();
                        EnemySpec regrown = chain.spec().regrown();
                        head.link(
                                chain,
                                0,
                                regrown,
                                kind,
                                regrown.hitbox(),
                                force.nextSerial(),
                                chain.headX(),
                                chain.headY(),
                                chain.headFacing());
                        events.add(SimEvents.Type.CHAIN_REGROWN, chain.headX(), chain.headY(), kind);
                    }
                }
                chain.grown(head, ship.x(), ship.y());
            }
            Enemy popped = chain.popDue();
            if (popped != null) {
                events.add(
                        SimEvents.Type.CHAIN_POP,
                        popped.x(),
                        popped.y(),
                        SimEvents.chainPopValue(popped.kind(), popped.hitbox().width()));
                force.free(popped);
            }
            if (!chain.advance(force.groundScroll())) {
                for (int i = 0; i < chain.size(); i++) {
                    Enemy member = chain.member(i);
                    if (member != null) {
                        if (i == 0 && chain.original()) {
                            escapes.escaped(member);
                        }
                        force.free(member);
                    }
                }
                chains.free(c);
            } else if (!chain.alive() && !chain.regrowing()) {
                chains.free(c);
            } else {
                if (chain.poreFiring() >= 0 && firing) {
                    guns.firePore(chain, ship);
                }
                // M8 part C: a water chain's head fires its own attacks (design/enemies/naval/eel-swarm).
                chain.headAttacks().step(chain, ship, firing, guns, events);
            }
        }
    }

    /**
     * A chain member was destroyed (before it is freed): the chain is cut there; a split-off rear
     * part becomes a chain of its own that grows a new head.
     */
    void memberDestroyed(Enemy enemy) {
        Chain chain = enemy.chain();
        Chain rear = chains.obtain();
        boolean split = chain.destroyed(enemy.link(), rear, chainsSpawned);
        if (split) {
            chainsSpawned++;
            events.add(SimEvents.Type.CHAIN_CUT, enemy.x(), enemy.y(), enemy.kind());
        } else if (rear != null) {
            chains.free(chains.size() - 1);
        }
        if (chain.takeCut()) {
            // M7 part C: a ground chain's cut stops its rear part.
            events.add(SimEvents.Type.CHAIN_CUT, enemy.x(), enemy.y(), enemy.kind());
        }
    }
}
