package vanguard.sim;

/**
 * M5 part G, the target-the-objective hook's modes (design/enemies, Target-the-objective hook;
 * design/campaign Level 13, What hurts the relay; user decision G1 = a). Only an attack aimed at the
 * objective hurts it; its bullets still hurt the ship they cross.
 */
public enum HookMode {
    /** Not hooked: every attack goes at the ship. */
    NONE,
    /** Level 04's: each aimed single shot goes at the ship or the nearest objective unit, whichever is closer. */
    NEAREST,
    /**
     * The unit counts its attacks (a burst is one); every second one goes at the objective while it
     * is closer than the ship, the others at the ship (the Skimmer).
     */
    ALTERNATE,
    /**
     * A walker's fan is centred on the objective, aimed at it, while the objective lies within its
     * armour's arc of its facing; otherwise along its facing. Its spit stays on the ship (the Scuttler).
     */
    IN_ARC,
    /**
     * Every attack goes at the objective while it is on the screen: a diver dives at its centre (its
     * crossing deals the ally's {@link AllySpec#dives()} once) and fires its fan at it, a mortar lobs at
     * it (the direct hit on it, the ring only at the ship), an aimed gun or fan fires at it (the
     * Stinger, the Polyp Mortar).
     */
    ALWAYS
}
