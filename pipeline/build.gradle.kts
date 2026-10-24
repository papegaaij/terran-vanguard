// Build-time asset tools. Kept apart because gdx-tools pulls in the old LWJGL 2 backend.
plugins {
    id("vanguard.java-conventions")
}

dependencies {
    implementation(libs.gdx.tools)
    implementation(project(":content"))
}

val design = rootProject.layout.projectDirectory.dir("design")
val assets = rootProject.layout.projectDirectory.dir("assets")

/**
 * Lists every spoken line with its key and settings for the offline voice renderer
 * (tools/art/voice.py, design/audio/voice) in build/voice/lines.json.
 */
tasks.register<JavaExec>("voiceLines") {
    description = "Writes the spoken lines and their keys for tools/art/voice.py."
    group = "assets"
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass = "vanguard.pipeline.VoiceLineList"
    args(layout.buildDirectory.file("voice/lines.json").get().asFile.absolutePath)
}

/** Cuts the placeholder sprites out of the chosen concept sheets (crop rectangles in PlaceholderSprites). */
val cutPlaceholderSprites = tasks.register<JavaExec>("cutPlaceholderSprites") {
    description = "Cuts placeholder sprite frames from chosen concept sheets into assets/."
    group = "assets"
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass = "vanguard.pipeline.PlaceholderSprites"
    args(design.asFile.absolutePath, assets.asFile.absolutePath)
    jvmArgs("-Djava.awt.headless=true")
}

/**
 * Copies the chosen concept sound effects the game plays into assets/sfx; they keep their concept
 * names, so CREDITS.md rows match at a glance. Final sounds (tools/art/sfx_originals.py, with a
 * SOURCE comment) are left alone.
 */
val copyPlaceholderSounds = tasks.register<JavaExec>("copyPlaceholderSounds") {
    description = "Copies chosen concept sound effects into assets/sfx, except final ones."
    group = "assets"
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass = "vanguard.pipeline.PlaceholderSounds"
    args(
        design.dir("audio/sfx/concept").asFile.absolutePath,
        assets.dir("sfx").asFile.absolutePath,
        "player-shot-r02-a.ogg",
        "hit-organic-r08-a.ogg",
        "hit-organic-r08-b.ogg",
        "explosion-tiny-r03-a.ogg",
        "explosion-tiny-r03-b.ogg",
        "player-shield-hit-r08-a.ogg",
        "player-shield-break-r08-a.ogg",
        "player-armour-hit-r08-a.ogg",
        "player-destroyed-r08-a.ogg",
        "enemy-shot-small-r08-a.ogg",
        "enemy-shot-small-r08-b.ogg",
        "explosion-r02-a.ogg",
        "explosion-small-r03-a.ogg",
        "hit-metal-r08-a.ogg",
        "hit-metal-r08-b.ogg",
        "pickup-salvage-small-r08-a.ogg",
        "pickup-salvage-large-r08-a.ogg",
        "pickup-shield-cell-r08-a.ogg",
        "pickup-armour-patch-r08-a.ogg",
        "ui-radio-open-r08-a.ogg",
        "ui-radio-close-r08-a.ogg",
        "ui-typewriter-r08-a.ogg",
        "ui-tally-tick-r08-a.ogg",
        "ui-tally-total-r08-a.ogg",
        "ui-grade-stamp-r08-a.ogg",
        "ui-menu-move-r08-a.ogg",
        "ui-menu-confirm-r08-a.ogg",
        "ui-menu-back-r08-a.ogg",
        "ambience-orbit-r08-a.ogg",
        "ui-edge-warning-r11-b.ogg",
        "enemy-mortar-lob-r21-a.ogg",
        "enemy-mortar-impact-r21-a.ogg",
        "hazard-sled-whine-r21-a.ogg",
        "hazard-sled-pass-r21-b.ogg",
        "enemy-mantis-telegraph-r23-a.ogg",
        "enemy-mantis-sweep-r23-b.ogg",
        "hazard-flare-launch-r23-a.ogg",
        "hazard-flare-burn-r23-a.ogg",
        "enemy-coilwyrm-regrow-r23-b.ogg",
        "enemy-coilwyrm-burst-r24-b.ogg",
        "enemy-coilwyrm-head-burst-r24-c.ogg",
        "enemy-carrier-roar-r25-b.ogg",
        "enemy-carrier-sac-open-r25-b.ogg",
        "enemy-carrier-sac-close-r25-b.ogg",
        "enemy-carrier-launch-r25-b.ogg",
        "enemy-carrier-sac-burst-r25-a.ogg",
        "enemy-carrier-iris-r25-b.ogg",
        "secret-cable-snap-r25-a.ogg",
        // M4 part H's SFX pass: the synthesized pickups and hangar sounds.
        "pickup-r01-a.ogg",
        "pickup-r01-b.ogg",
        "pickup-r01-c.ogg",
        "pickup-special-charge-r08-a.ogg",
        "ui-shop-buy-r08-a.ogg",
        "ui-shop-sell-r08-a.ogg",
        "ui-shop-denied-r08-a.ogg",
        "ui-equip-r08-a.ogg",
        "ui-upgrade-r08-a.ogg",
        // Concept round 27's choices: the save sound (synthesized) and the Coilwyrm's chain-cut
        // tear (its final file from tools/art/sfx_originals.py).
        "ui-save-r27-b.ogg",
        "enemy-coilwyrm-cut-r27-a.ogg",
        // Concept round 28 (open): the proximity mine's arming beep, option a until the choice.
        "weapon-mine-arm-r28-a.ogg",
        // Concept round 34's choices (user, 2026-10-12; M5 parts F-H, rounds 34-36's sounds): Level 12's
        // thunder and gust b, the latch, shake-off and the rain-only storm bed (F6 = a) a, beside round 08's
        // chosen storm ambience, which stays (the drain and chime: round 46's picks, below).
        "ambience-storm-rain-r34-a.ogg",
        "thunder-1-r34-b.ogg",
        "thunder-2-r34-b.ogg",
        "thunder-3-r34-b.ogg",
        "gust-r34-b.ogg",
        "lamprey-latch-r34-a.ogg",
        "lamprey-shake-r34-a.ogg",
        // Level 13's: grid online b, the relay hit and the integrity alarm a and the relay destroyed (the
        // drone launch: round 46's pick, below).
        "relay-hit-r35-a.ogg",
        "integrity-alarm-r35-a.ogg",
        "relay-destroyed-r35-a.ogg",
        "grid-online-r35-b.ogg",
        // Level 14's: the tear-free roar and the canopy withering b, the root burst a and the root growth
        // (the canopy hum: round 46's pick, below).
        "root-burst-r36-a.ogg",
        "root-growth-r36-a.ogg",
        "tear-free-roar-r36-b.ogg",
        "canopy-withering-r36-b.ogg",
        // M7 part A, the Act 3 arsenal: the Ion Beam's chosen loop (round 36 a) and round 37's picks
        // (user, 2026-10-13: the rock impact, the deflector's return and the lance's rumble b, the rest a;
        // the lance's loop c and the lock beep are already there; the beam's stop and impact, the drone
        // rebuild, the EMP, the overcharge and the dash: round 46's picks, below).
        "ion-beam-loop-r36-a.ogg",
        "ion-beam-start-r37-a.ogg",
        "beam-impact-rock-r37-b.ogg",
        "deflector-tick-r37-a.ogg",
        "deflector-return-r37-b.ogg",
        "drone-break-r37-a.ogg",
        "lance-rumble-r37-b.ogg",
        "cooldown-ready-r37-a.ogg",
        // M7 part B, Level 15: the Burrower's eruption chosen in round 36 (S.2 = a; it opens with the 1.0 s
        // bulge tell, so it starts at the bulge) and round 38's picks (2026-10-14): the bulge's crack layer b
        // over the eruption, the entry roar a and the Mars dust wind a (the dive: round 46's pick, below).
        "burrow-eruption-r36-a.ogg",
        "burrow-bulge-r38-b.ogg",
        "entry-roar-r38-a.ogg",
        "mars-dust-r38-a.ogg",
        // M7 part C, Level 16: the canyon wall scrape chosen in round 36 (S.3 = b) and the tendril's hum
        // (round 39 a, synthesized, kept by the user in round 46; the rest of Level 16: round 46's picks).
        "canyon-scrape-r36-b.ogg",
        "tendril-hum-r39-a.ogg",
        // M7 part D, Level 17: round 40's proposal still standing, option a (the Choir Herald's rings' collapse;
        // synthesized; the domes' hit and the song: rounds 49 and 47's picks, the rest: round 46's picks, below).
        // The dome break: round 40's redo c, chosen in round 41 (S.h, 2026-10-19).
        "dome-break-r40-c.ogg",
        "aura-collapse-r40-a.ogg",
        // M7 part E, Level 18: the `mars-storm` bed chosen in round 36 (S.5 = a, "recorded front": a sandstorm over
        // a furious low howl, CC0 + CC-BY, seamless 16 s loop).
        "dust-storm-bed-r36-a.ogg",
        // M7 part F, Level 19: round 42's belt loop, option a (synthesized; the stomp, the tank's death and the
        // `mars-foundry` ambience: round 46's picks, below).
        "belt-rumble-r42-a.ogg",
        // Round 43's picks (the user, 2026-10-20; built from CC0/CC-BY Freesound originals, CREDITS.md): the walker's
        // hatch b, flamer tell a, flame loop b, vent hit a, death a and wreck settle a; the furnace vents' tell a and
        // burst a; the Warden Tank's tracks a, turret a and burst a. The walker's pod missile plays the chosen enemy
        // missile launch (r08 a, final). Level 20 (M7 part G): the basin rumble a, the pursuit swarm's hum a and lunge a.
        "walker-hatch-r43-b.ogg",
        "walker-flame-tell-r43-a.ogg",
        "walker-flame-r43-b.ogg",
        "walker-vent-hit-r43-a.ogg",
        "walker-death-r43-a.ogg",
        "walker-wreck-settle-r43-a.ogg",
        "vent-tell-r43-a.ogg",
        "vent-burst-r43-a.ogg",
        "tank-tracks-r43-a.ogg",
        "tank-turret-r43-a.ogg",
        "tank-burst-r43-a.ogg",
        "basin-rumble-r43-a.ogg",
        "swarm-hum-r43-a.ogg",
        "swarm-lunge-r43-a.ogg",
        // Round 44's picks (the user, 2026-10-20; CC0/CC-BY Freesound originals, CREDITS.md): the Dust Colossus's
        // bulge a, eruption a, suction loop a, bite a and death a (Level 21) and the Revenant Walker's rail lance
        // charge b, shot a and stomp shockwave a. The ridge pass is a too, but its game copy is not copied from here:
        // a's file pans left to right and Level 21's ridge runs from the right edge to the left, so
        // `tools/concept/audio/sfx_r44.py --mirror colossus-ridge-r44-a` writes assets/sfx/colossus-ridge-r44-a.ogg
        // with its channels swapped (same name, so its CREDITS.md rows match).
        "colossus-bulge-r44-a.ogg",
        "colossus-eruption-r44-a.ogg",
        "colossus-suction-r44-a.ogg",
        "colossus-bite-r44-a.ogg",
        "colossus-death-r44-a.ogg",
        "walker-lance-charge-r44-b.ogg",
        "walker-lance-shot-r44-a.ogg",
        "walker-shockwave-r44-a.ogg",
        // Round 46's picks (the user, 2026-10-21; CC0/CC-BY Freesound originals, CREDITS.md), replacing the synthesized
        // and earlier files: the Ghost Drone, Dust Devil, Shellback, Threadcrawler, tendril burn and break, Burrower dive,
        // Herald aura and absorb, walker stomp, tank death, the canopy, canyon, colony and foundry beds and the Act 2/3
        // sounds (Driftjelly pulse, Lamprey drain, sonde, drone launch, dash, overcharge, EMP, beam stop and impact,
        // drone rebuild). The tendril hum stays round 39's a; the integrity alarm keeps its old file (round 47); the
        // herald song, Leviathan cry, dome hit and launch rail: rounds 47 and 49's picks, below.
        "ghost-drone-drop-r46-b.ogg",
        "ghost-drone-glint-r46-b.ogg",
        "ghost-drone-burst-r46-a.ogg",
        "ghost-drone-death-r46-b.ogg",
        "devil-spin-r46-a.ogg",
        "devil-open-r46-a.ogg",
        "devil-spiral-r46-a.ogg",
        "shellback-curl-r46-b.ogg",
        "shellback-uncurl-r46-b.ogg",
        "shellback-roll-r46-b.ogg",
        "shellback-lob-r46-b.ogg",
        "shellback-walk-r46-b.ogg",
        "crawler-walk-r46-a.ogg",
        "tendril-burn-r46-a.ogg",
        "tendril-break-r46-b.ogg",
        "burrow-dive-r46-a.ogg",
        "herald-aura-r46-b.ogg",
        "aura-absorb-r46-b.ogg",
        "walker-stomp-r46-a.ogg",
        "tank-death-r46-a.ogg",
        "canopy-hum-r46-a.ogg",
        "mars-canyon-r46-a.ogg",
        "mars-colony-r46-b.ogg",
        "mars-foundry-r46-b.ogg",
        "driftjelly-pulse-r46-b.ogg",
        "lamprey-drain-r46-b.ogg",
        "sonde-chime-r46-a.ogg",
        "drone-launch-r46-a.ogg",
        "dash-r46-b.ogg",
        "overcharge-whoosh-r46-b.ogg",
        "overcharge-end-r46-b.ogg",
        "emp-ring-r46-b.ogg",
        "emp-powerdown-r46-a.ogg",
        "ion-beam-stop-r46-a.ogg",
        "beam-impact-r46-a.ogg",
        "drone-rebuild-r46-a.ogg",
        // Round 47's and 49's picks (the user, 2026-10-22; CC0/CC-BY Freesound sounds, CREDITS.md), cut from the
        // Freesound previews (the download limit blocked the originals) and to be rebuilt from the originals by
        // tools/concept/audio/sfx_r47l.py and sfx_r49m.py: the Herald's song a, the Leviathan's cry b, the launch
        // rail b and the domes' hit b (round 49).
        "herald-song-r47-a.ogg",
        "enemy-leviathan-cry-r47-b.ogg",
        "launch-rail-r47-b.ogg",
        "dome-hit-r49-b.ogg",
        // M8 part A: round 50's picks for the Act 4 arsenal (the user, 2026-10-22; CC0/CC-BY Freesound HQ previews,
        // CREDITS.md; the limit chime synthesized), to be rebuilt from the originals by tools/concept/audio/sfx_r50a.py:
        // the Harpoon Torpedoes' launch b, run loop a and hit a, the Depth Charges' roll-out b and burst a, the special
        // row's limit chime b. The Sonar Pulse and the Decoy Flares play their chosen files (r04 a, r08 a, already final
        // in assets/sfx); the Torpedo Pod keeps its chosen r03 a (round 50, S.6: both de-bubbled launches rejected).
        "shot-harpoon-r50-b.ogg",
        "harpoon-run-r50-a.ogg",
        "harpoon-hit-r50-a.ogg",
        "shot-depth-charge-r50-b.ogg",
        "depth-charge-burst-r50-a.ogg",
        "special-limit-r50-b.ogg",
        // M8 part B: round 51's picks for Level 22 (user 2026-10-22; CC0/CC-BY Freesound HQ previews, CREDITS rows from
        // step AUD-B): the ice geyser's tell b and burst a, the Drill Rig's collar loop, shard ring and core death b, the
        // Europa ice bed a and Conamara's bed a.
        "geyser-tell-r51-b.ogg",
        "geyser-burst-r51-a.ogg",
        "rig-collar-r51-b.ogg",
        "rig-ring-r51-b.ogg",
        "rig-death-r51-b.ogg",
        "europa-ice-r51-a.ogg",
        "conamara-station-r51-a.ogg",
        // M8 part C: round 52's picks for Level 23 (the user, 2026-10-22; CC0/CC-BY Freesound HQ previews, CREDITS rows
        // from step AUD-C): the waterline crossing a, the hydro-kit's refit a, the wall scrape under water a, the eel's
        // pulse b and segment b, the Glassfin's shatter b, the siphon pump's hum b and break b, the bed under the ice a;
        // the under-water explosion ladder (tiny to large) is still open (S.10): family a until the pick.
        "waterline-cross-r52-a.ogg",
        "hydro-refit-r52-a.ogg",
        "scrape-under-r52-a.ogg",
        "eel-pulse-r52-b.ogg",
        "eel-segment-r52-b.ogg",
        "glassfin-shatter-r52-b.ogg",
        "pump-hum-r52-b.ogg",
        "pump-break-r52-b.ogg",
        "europa-under-r52-a.ogg",
        "explosion-underwater-tiny-r52-a.ogg",
        "explosion-underwater-small-r52-a.ogg",
        "explosion-underwater-medium-r52-a.ogg",
        "explosion-underwater-large-r52-a.ogg",
        // M8 part D: round 53's picks for Level 24 (the user, 2026-10-23; CC0/CC-BY Freesound HQ previews, CREDITS rows
        // from step AUD-D): the Abyss Ray's rise b, glide a, fan b and death a, the Barbfin's needle b, the spawning
        // reef's iris a and death b, the relay beacons' pulse a, the Meridian's hatch b.
        "ray-rise-r53-b.ogg",
        "ray-glide-r53-a.ogg",
        "ray-fan-r53-b.ogg",
        "ray-death-r53-a.ogg",
        "barbfin-needle-r53-b.ogg",
        "reef-iris-r53-a.ogg",
        "reef-death-r53-b.ogg",
        "beacon-pulse-r53-a.ogg",
        "wreck-hatch-r53-b.ogg",
        // M8 part E: round 54's proposals for Level 25 (CC0/CC-BY Freesound HQ previews, CREDITS rows from step AUD-E),
        // their a variants until the user's pick, which may swap the file: the Siren's ring, slow and death, the Spiral
        // Nautilus's spin, uncoil and death, the trench's bed.
        "siren-ring-r54-a.ogg",
        "siren-slow-r54-b.ogg",
        "siren-death-r54-a.ogg",
        "nautilus-spin-r54-a.ogg",
        "nautilus-uncoil-r54-a.ogg",
        "nautilus-death-r54-b.ogg",
        "europa-trench-r54-a.ogg",
        // M8 part F: round 55's proposals for Level 26 (CC0/CC-BY Freesound HQ previews, CREDITS rows from step AUD-F),
        // their a variants until the user's pick, which may swap the file: the Glow Angler's lure hum and lunge, the
        // Nautilus's crown hum in the dark, Rook's loss (the trails, the hits, the spin), the headlight click, the Choir's
        // static lead, the black water's bed.
        "lure-hum-r55-a.ogg",
        "angler-lunge-r55-a.ogg",
        "crown-hum-r55-a.ogg",
        "torpedo-trails-r55-a.ogg",
        "rook-hit-r55-a.ogg",
        "rook-spin-r55-a.ogg",
        "headlight-click-r55-a.ogg",
        "choir-static-r55-a.ogg",
        "europa-black-r55-a.ogg",
    )
}

/**
 * Copies the voice part's stage sounds into assets/voice/<voice>/ under their concept names: the
 * sound the speaker table's `stage` names (design/audio/voice/data.yaml), played for a radio line
 * that is only a stage direction. The Choir's sung sting is round 29's option b (user, 2026-10-06),
 * the sung "ah" F3 to E3.
 */
val copyPlaceholderStageSounds = tasks.register<JavaExec>("copyPlaceholderStageSounds") {
    description = "Copies the voices' stage sounds (the Choir sings) into assets/voice."
    group = "assets"
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass = "vanguard.pipeline.PlaceholderSounds"
    args(
        design.dir("audio/voice/concept").asFile.absolutePath,
        assets.dir("voice/choir").asFile.absolutePath,
        "voice-choir-sings-r29-b.ogg",
    )
}

/**
 * Copies the chosen concept music the game plays into assets/music under the names it loads. Final
 * themes (tools/art/themes.py, with a SOURCE comment) are left alone.
 */
val copyPlaceholderMusic = tasks.register<JavaExec>("copyPlaceholderMusic") {
    description = "Copies chosen concept music into assets/music, except final ones."
    group = "assets"
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass = "vanguard.pipeline.PlaceholderSounds"
    args(
        design.dir("audio/music/concept").asFile.absolutePath,
        assets.dir("music").asFile.absolutePath,
        "title-theme-full-r08-a.ogg=title-theme.ogg",
        "coalition-rising-full-r08-a.ogg=coalition-rising.ogg",
        "coalition-rising-base-r11-a.ogg=coalition-rising-base.ogg",
        "afterburner-full-r08-a.ogg=afterburner.ogg",
        "afterburner-base-r15-a.ogg=afterburner-base.ogg",
        "briefing-theme-r08-a.ogg=briefing-theme.ogg",
        "hangar-theme-full-r08-a.ogg=hangar-theme.ogg",
        "game-over-r08-a.ogg=game-over.ogg",
        "mission-failed-r08-a.ogg=mission-failed.ogg",
        "miniboss-sting-r08-a.ogg=miniboss-sting.ogg",
        "mission-complete-r08-a.ogg=mission-complete.ogg",
        "boss-warning-r08-a.ogg=boss-warning.ogg",
        "choir-descends-full-r08-a.ogg=choir-descends.ogg",
        "act-complete-r08-a.ogg=act-complete.ogg",
        "debrief-theme-r40-b.ogg=debrief-theme.ogg",
        // M7 part E: track 9 "Grey Signal" (round 41 a, proposed) and its base and motif stems, one render,
        // sample-aligned (LOOPSTART 349272, LOOPLENGTH 7451136).
        "grey-signal-r41-a.ogg=grey-signal.ogg",
        "grey-signal-base-r41-a.ogg=grey-signal-base.ogg",
        "grey-signal-motif-r41-a.ogg=grey-signal-motif.ogg",
        // M8 part F: track 10's dark stem (round 51 a, the stem tools/art/themes.py's thera-deep{,-base} were copied
        // from, same grid) and its warm pad (round 55 a until the user's pick), Level 26's `stem_times`.
        "thera-deep-dark-r51-a.ogg=thera-deep-dark.ogg",
        "thera-deep-warm-r55-a.ogg=thera-deep-warm.ogg",
    )
}

/**
 * Writes the credits roll (design/ui/credits) from CREDITS.md into assets/ui/credits.txt: the
 * attributions of every shipped CC-BY asset and font, the CC0 authors' thanks. Run it after
 * CREDITS.md or the shipped assets change and commit the result; CreditsListTest fails while the
 * committed roll differs from what CREDITS.md gives.
 */
val credits = tasks.register<JavaExec>("credits") {
    description = "Writes the credits roll from CREDITS.md into assets/ui/credits.txt."
    group = "assets"
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass = "vanguard.pipeline.CreditsList"
    mustRunAfter(cutPlaceholderSprites, copyPlaceholderSounds, copyPlaceholderStageSounds, copyPlaceholderMusic)
    args(rootProject.projectDir.absolutePath, assets.file("ui/credits.txt").asFile.absolutePath)
}

tasks.test {
    // CreditsListTest compares the committed roll with CREDITS.md and the shipped assets.
    systemProperty("vanguard.rootDir", rootProject.projectDir.absolutePath)
    inputs.file(rootProject.layout.projectDirectory.file("CREDITS.md")).withPropertyName("credits")
    inputs.file(assets.file("ui/credits.txt")).withPropertyName("creditsRoll")
    inputs.files(assets.dir("sfx"), assets.dir("music"), assets.dir("voice"), assets.dir("fonts"))
        .withPropertyName("creditedAssets")
}

/**
 * Copies the chosen concept art and audio that stand in for production assets into assets/ and
 * cuts the sprite frames. Run it after a concept choice changes; its output is committed (Git
 * LFS), the build only reads assets/. The title scene, the logo and the bitmap fonts are production
 * art (tools/art/ui_scenes.py, tools/art/fonts.py), and so are the act titles' lettering
 * (tools/art/act_titles.py). It rewrites the credits roll last.
 */
tasks.register("importPlaceholders") {
    description = "Copies chosen concept art and audio into assets/ as placeholders."
    group = "assets"
    dependsOn(cutPlaceholderSprites, copyPlaceholderSounds, copyPlaceholderStageSounds, copyPlaceholderMusic, credits)
}

/**
 * A tool's file arguments as inputs and outputs of the task: Gradle hashes them by their paths
 * relative to their roots, so the task's build cache entry is shared by every checkout and build
 * directory (absolute paths in plain `args` would make each one a miss).
 */
abstract class AtlasArgs : CommandLineArgumentProvider {
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sprites: DirectoryProperty

    @get:Optional
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val backdrop: DirectoryProperty

    @get:Optional
    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val claims: RegularFileProperty

    @get:Optional
    @get:OutputFile
    abstract val claimsOutput: RegularFileProperty

    @get:Optional
    @get:OutputDirectory
    abstract val atlasOutput: DirectoryProperty

    override fun asArguments(): List<String> = listOfNotNull(
        sprites.get().asFile.absolutePath,
        backdrop.orNull?.asFile?.absolutePath,
        claims.orNull?.asFile?.absolutePath,
        claimsOutput.orNull?.asFile?.absolutePath,
        atlasOutput.orNull?.asFile?.absolutePath,
    )
}

/**
 * The atlases each sprite goes into, from the levels' data (SpriteUse). It reads the whole content,
 * so it runs again after any content change; packAtlases only sees its small output and packs again
 * only when a sprite moves to other atlases.
 */
val spriteClaimsFile = layout.buildDirectory.file("atlas-claims.txt")
val spriteClaims = tasks.register<JavaExec>("spriteClaims") {
    description = "Writes the atlases each sprite goes into, from the levels' data."
    group = "assets"
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass = "vanguard.pipeline.SpriteClaims"
    argumentProviders.add(objects.newInstance<AtlasArgs>().apply {
        sprites = assets.dir("sprites")
        claimsOutput = spriteClaimsFile
    })
    outputs.cacheIf { true }
}

/**
 * Packs assets/sprites and assets/backdrop into texture atlases; build output, never committed. The
 * atlases are packed side by side, and the task is cacheable: its inputs are the frames, the claims
 * and the packer (the pipeline's own classes and its external libraries, not the content), so an
 * unchanged tree takes the atlases from the build cache, in every build directory.
 */
val packAtlases = tasks.register<JavaExec>("packAtlases") {
    description = "Packs the sprite frames in assets/ into texture atlases."
    group = "assets"
    val externalRuntime = configurations.runtimeClasspath.get().incoming.artifactView {
        componentFilter { it !is ProjectComponentIdentifier }
    }.files
    classpath = files(sourceSets.main.get().output, externalRuntime)
    mainClass = "vanguard.pipeline.AtlasPacker"
    argumentProviders.add(objects.newInstance<AtlasArgs>().apply {
        sprites = assets.dir("sprites")
        backdrop = assets.dir("backdrop")
        claims = spriteClaimsFile
        atlasOutput = layout.buildDirectory.dir("atlases")
    })
    dependsOn(spriteClaims)
    jvmArgs("-Djava.awt.headless=true")
    outputs.cacheIf { true }
}

/** The packed atlases, for the desktop build's resources. */
val atlases by configurations.creating {
    isCanBeConsumed = true
    isCanBeResolved = false
}

artifacts {
    add(atlases.name, layout.buildDirectory.dir("atlases")) {
        builtBy(packAtlases)
    }
}
