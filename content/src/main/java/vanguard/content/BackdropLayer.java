package vanguard.content;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Locale;

/**
 * The parallax layers a backdrop draws on (design/art-direction, Parallax layer model), back to
 * front; the play plane ({@code air}) lies between {@link #LOW_AIR} and {@link #HIGH_AIR}. M7 part C: {@link
 * #PLATEAU} (optional, a canyon level's) lies between the ground and the low-air layer.
 */
public enum BackdropLayer {
    @JsonProperty("deep")
    DEEP,
    @JsonProperty("far")
    FAR,
    @JsonProperty("ground")
    GROUND,
    /**
     * M7 part C (design/campaign Level 16, Terrain: MA.1 = b): the plateau beyond a canyon's walls, at about 1.25,
     * drawn only beyond the projected walls' outer edge; a level without terrain leaves it out.
     */
    @JsonProperty("plateau")
    PLATEAU,
    @JsonProperty("low-air")
    LOW_AIR,
    @JsonProperty("high-air")
    HIGH_AIR;

    /** The name in the documents and data files. */
    public String key() {
        return name().toLowerCase(Locale.ROOT).replace('_', '-');
    }
}
