package vanguard.sim;

/**
 * What a {@link Sortie} does when its fire destroys something ({@link PlayerFire.Hits}), when a unit leaves the
 * screen alive or a spawner bursts or releases ({@link EnemyForce.Escapes}), and when a tendril ends ({@link
 * Links.Outcome}): each is handed to its pass (split out of {@link Sortie}).
 */
final class SortieOutcomes implements PlayerFire.Hits, EnemyForce.Escapes, Links.Outcome {
    private final Sortie s;

    SortieOutcomes(Sortie sortie) {
        this.s = sortie;
    }

    @Override
    public void enemyDestroyed(int index, int mount) {
        s.objectivePasses.escortKill(mount);
        s.objectivePasses.destroy(index);
    }

    @Override
    public void groundDestroyed(int index) {
        s.groundPasses.demolish(index);
    }

    @Override
    public void triggerReleased(int index) {
        GroundObject trigger = s.ground.get(index);
        s.events.add(SimEvents.Type.TRIGGER_SPENT, trigger.x(), trigger.y(), trigger.serial());
        s.groundPasses.released(trigger);
    }

    @Override
    public void mineDestroyed(int index) {
        s.objectivePasses.shootMine(index);
    }

    @Override
    public void partDestroyed(int piece, int hit, int mount) {
        // M8 part H: a hit on a coiled boss's implant destroys the head whose HP it took.
        int part = s.setPieces[piece].hpPart(hit);
        if (s.setPieces[piece].spec().parts().get(part).vital()) {
            s.objectivePasses.escortKill(mount);
        }
        s.objectivePasses.wreck(piece, part);
    }

    @Override
    public void unitPartDestroyed(int index, int part, int mount) {
        s.objectivePasses.partOff(index, part);
    }

    @Override
    public void escaped(Enemy enemy) {
        s.objectivePasses.escaped(enemy);
    }

    @Override
    public void burst(Enemy enemy) {
        s.objectivePasses.selfBurst(enemy);
    }

    @Override
    public void released(Enemy spawner, int units) {
        // M5 part C: a periodic spawner's units count among the enemies as released.
        s.streamReleased += units;
    }

    @Override
    public void broken(Tendril tendril) {
        s.objectivePasses.tendrilBurnt(tendril);
        s.objectivePasses.tendrilBroken();
    }

    @Override
    public void lost(Tendril tendril) {
        s.objectivePasses.tendrilLost();
    }
}
