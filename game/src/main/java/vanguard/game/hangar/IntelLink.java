package vanguard.game.hangar;

import vanguard.game.ui.Box;

/**
 * Step DB2 (design/ui/databank, The ways in): an intel picture a click opens the databank at: its box and the unit's slug
 * (an enemy's portrait, the boss's or a contact's silhouette; a livery's portrait names its livery).
 */
public record IntelLink(Box box, String slug) {}
