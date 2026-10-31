# Sound effect briefs and prompts — concept round 01

All round-01 SFX are code-generated mockups, reproducible with:

```
python3 tools/concept/audio/sfx.py            # writes all nine files into this directory
python3 tools/concept/audio/analyze.py design/audio/sfx/concept/*.ogg   # objective checks
```

Generator: `tools/concept/audio/sfx.py` (synth library `tools/concept/audio/synth.py`).
Format: OGG Vorbis q6, 44.1 kHz stereo, fixed seeds (byte-identical on re-run).

**Level rules** (apply to every future SFX, not just these):
player shots peak at −10 dBFS (they repeat several times per second and must never fatigue),
pickups peak at −4 dBFS (sustained ones −7), explosions peak at −1.5 dBFS. Every file starts
and ends at silence (0.5 ms / 5 ms fades), no DC offset, sample peak ≤ −1 dBFS after encoding.

---

## player-shot

The sound heard most in the game, several times per second for minutes on end. It must be
short (< 0.25 s), punchy at the start and gone immediately, with no harsh 2–4 kHz content that
tires the ear. It shouldn't mask enemy shots or the music's lead.

### player-shot-r01-a — classic laser "pew" (0.14 s)
Brief: square wave sweeping down 1900 → 380 Hz with the pulse width narrowing, through a
closing low-pass. Pure late-80s/90s arcade laser.
AI prompt (ElevenLabs SFX style): *"Retro arcade spaceship laser shot, short descending
square-wave 'pew', 0.15 seconds, clean, no reverb, 1990s DOS shoot'em up, dry, punchy start,
fast decay."*

### player-shot-r01-b — pulse cannon (0.12 s)
Brief: sine thump 240 → 70 Hz plus a band-passed noise snap and a short triangle tick. It
sounds more like a projectile cannon than a laser. Heavy, but the least tiring of the three.
AI prompt: *"Short sci-fi pulse cannon shot, compact low thump with a crisp mechanical snap,
0.12 seconds, dry, suitable for rapid fire, 1990s top-down shooter."*

### player-shot-r01-c — plasma bolt "zwip" (0.23 s)
Brief: two saws a fifth apart (1 : 1.5) that rise quickly and then droop, with fast vibrato, a
resonant filter sweep and a tiny ping-pong echo. The most sci-fi and "energy weapon"-like;
also the most distinctive, which suits a signature weapon better than the default gun.
AI prompt: *"Sci-fi plasma blaster shot, resonant filter 'zwip', rising then falling pitch,
slight stereo echo, 0.2 seconds, energetic but soft attack, 90s pre-rendered space shooter."*

---

## explosion

Enemy destruction. Size variants (small / large / boss) will be derived later from the chosen
style. Must feel rewarding and cut through the music without masking the next shot.

### explosion-r01-a — small, crunchy 8-bit pop (0.62 s)
Brief: noise with a low-pass sweeping down 7 kHz → 250 Hz, a short sine thump, saturated,
then bit-crushed to 6 bits at ¼ sample rate. The crush makes the end gate abruptly, like
tracker-era samples. Best for small drones and swarms, where many play at once.
AI prompt: *"Small retro explosion, crunchy bit-crushed 8-bit noise burst, 0.6 seconds, lo-fi,
Amiga / DOS game sample, punchy, no long tail."*

### explosion-r01-b — big boomy blast with debris (1.45 s)
Brief: a sub sine dropping 95 → 28 Hz, a brown-noise body behind a closing low-pass, a
high-passed crack at the start, 38 random filtered debris crackles thinning out over time, a
Haas stereo spread and a short dark room reverb. The "cinematic" option, for large ships and
ground structures.
AI prompt: *"Large spaceship explosion, deep sub-bass boom, rumbling body, crackling metal
debris falling away, 1.5 seconds, wide stereo, punchy transient, 90s sci-fi game."*

### explosion-r01-c — sci-fi plasma detonation (1.25 s)
Brief: noise through a resonant (Q 6) filter that opens and then closes ("whoom"), ring-modulated
falling sine tones, a tremolo saw shimmer, a sub thump, a slow left-right swirl and an echo.
Alien and energetic; a candidate for Vrell biomechanical enemies.
AI prompt: *"Sci-fi plasma explosion, resonant whooshing energy burst, ring-modulated falling
tones, swirling stereo, shimmering tail, 1.2 seconds, alien technology, 1990s space shooter."*

---

## pickup

Collecting credits and power-ups. Bright and positive, clearly different from anything
hostile, and short enough to chain when several pickups are collected in a row.

### pickup-r01-a — square arpeggio power-up (0.57 s)
Brief: a C major arpeggio C6-E6-G6-C7 in 25 % pulse waves, 55 ms per note, the last note held,
with a subtle echo. The iconic arcade "power-up".
AI prompt: *"Classic arcade power-up pickup sound, fast rising major arpeggio, square wave
chiptune, bright and cheerful, 0.5 seconds, light echo."*

### pickup-r01-b — bell chime pair (0.75 s)
Brief: two inharmonic bell tones (partials 1, 2.76, 5.4, 8.93) a fifth apart (E6 then B6),
70 ms apart, with a stereo echo. Cleaner and more "premium"; could suit rare pickups (weapon
upgrades) rather than common credits.
AI prompt: *"Bright crystal bell chime pickup, two quick ascending tones, shimmering stereo
echo, 0.7 seconds, clean digital FM bell, sci-fi UI."*

### pickup-r01-c — rising PWM sweep with sparkles (0.5 s)
Brief: a pulse wave sweeping two octaves up (330 → 1320 Hz) with pulse-width modulation,
vibrato and an opening filter, plus nine random sine sparkles. The "credits collected" candidate.
AI prompt: *"Sci-fi credits pickup, rising synth sweep with pulse-width modulation, sparkling
glitter on top, 0.5 seconds, bright, rewarding, 1990s arcade shooter."*

# Concept round 02 — recorded sound effects (third-party)

The round 01 synthesized shots and explosions were rejected. Round 02 uses recorded sounds from
[Freesound](https://freesound.org), CC0 or CC-BY only (see [CREDITS.md](../../../../CREDITS.md)).
All files are imported by `python3 tools/concept/audio/import_sfx.py`, which downloads the public
HQ previews (~192 kbps OGG) into a cache outside the repository, trims the leading silence, cuts,
fades, peak-normalises (shots −10 dBFS, explosions −1.5 dBFS) and encodes to OGG Vorbis.
**For the production asset, download the original file from the source page (Freesound login)
and rebuild with the same settings.** Selection was based on download counts, descriptions,
licence and measured envelope/spectrum (the files cannot be auditioned by the generator).

### player-shot-r02-a — "Projectile Shoot" by unfa
Source: <https://freesound.org/people/unfa/sounds/193427/> — CC0 1.0. Suggested use: Pulse Cannon (starter front gun).
Why: Sharp broadband transient with a short low thump; very fast attack and decay, so it stays clear under rapid fire. Well used (4k+ downloads).
Edit: 0.25 s, 0.12 s fade-out.

### player-shot-r02-b — "Sci Fi Gun Shot" by Bird_man
Source: <https://freesound.org/people/Bird_man/sounds/317136/> — CC0 1.0. Suggested use: heavy front gun / Hammer Mortar.
Why: Punchy sci-fi gun with a lot of low-end weight (~70% of energy below 200 Hz); reads as a heavy weapon.
Edit: 0.28 s, 0.14 s fade-out.

### player-shot-r02-c — "laser3" by nsstudios
Source: <https://freesound.org/people/nsstudios/sounds/344276/> — CC-BY 4.0. Suggested use: Lance Laser / light laser weapons.
Why: Clean descending laser sweep, naturally 0.3 s long; the most "energy weapon" of the set without sounding 8-bit.
Edit: 0.27 s, 0.10 s fade-out.

### player-shot-r02-d — "Machine Gun 001 - single shot" by pgi
Source: <https://freesound.org/people/pgi/sounds/212601/> — CC0 1.0. Suggested use: Autocannon Pod / ballistic guns.
Why: Real recorded single machine-gun shot: realistic crack and body; 10k+ downloads.
Edit: 0.28 s, 0.16 s fade-out.

### player-shot-r02-e — "Autocannon Three Shot Burst" by qubodup
Source: <https://freesound.org/people/qubodup/sounds/854186/> — CC0 1.0. Suggested use: Scatter Vulcan / heavy ballistic front gun.
Why: Recorded autocannon; the first shot of the three-shot burst is cut out. Deep, mechanical, weighty.
Edit: first shot of the burst, 0.26 s, 0.12 s fade-out.

### explosion-r02-a — "small explosion" by bevibeldesign
Source: <https://freesound.org/people/bevibeldesign/sounds/315826/> — CC0 1.0. Suggested use: small enemy destroyed (fighters, drones).
Why: Short, natural decay within 0.4 s, not bass-heavy; fits frequent small kills without masking the mix.
Edit: 0.70 s, 0.30 s fade-out.

### explosion-r02-b — "Explosion 1" by magnuswaker
Source: <https://freesound.org/people/magnuswaker/sounds/523089/> — CC0 1.0. Suggested use: small-to-medium enemy (heavy fighters, turrets).
Why: Dense, bass-heavy (85% below 200 Hz) short blast; a heavier alternative for small kills.
Edit: 0.90 s, 0.40 s fade-out.

### explosion-r02-c — "Explosion" by qubodup
Source: <https://freesound.org/people/qubodup/sounds/182429/> — CC0 1.0. Suggested use: medium enemy (gunships, ground vehicles, buildings).
Why: Well-known game explosion (14k downloads): deep boom with crackle, clean decay by 1.6 s.
Edit: 1.70 s, 0.50 s fade-out.

### explosion-r02-d — "Nearby explosion with debris" by juskiddink
Source: <https://freesound.org/people/juskiddink/sounds/108641/> — CC-BY 4.0. Suggested use: large enemy / mid-boss / building collapse.
Why: Real recorded nearby explosion with falling debris: the most realistic of the set.
Edit: 2.80 s, 0.90 s fade-out.

### explosion-r02-e — "explosion_big_01" by derplayer
Source: <https://freesound.org/people/derplayer/sounds/587194/> — CC0 1.0. Suggested use: boss destroyed / capital ship.
Why: Big, dense blast with rolling secondary crackle and debris over ~2.8 s.
Edit: 2.86 s, 0.80 s fade-out.

# Concept round 03 — weapon sound families and the explosion ladder (third-party)

The weapon → family mapping and the size ladder are in the [README](../README.md). All files are
imported by `python3 tools/concept/audio/import_sfx.py` from Freesound HQ previews (production:
rebuild from the original files with the same settings). Beam files are seamless loops
(`loop=` in the importer: equal-power cross-fade of the loop end into its start, no fades).

### shot-vulcan-r03-b — "minigun.wav" by pgi
Source: <https://freesound.org/people/pgi/sounds/98331/> — CC0 1.0. Use: `vulcan` b, rotary chatter.
Why: Recorded minigun: continuous rotary chatter, so any 0.3 s slice works as one volley; gives the vulcan a second, rougher texture next to r02-e.
Edit: leading silence trimmed, cut to 0.30 s, 0.12 s fade-out; peak -10.0 dBFS.

### shot-laser-r03-b — "Laser shot.wav" by michael_grinnell
Source: <https://freesound.org/people/michael_grinnell/sounds/512469/> — CC0 1.0. Use: `laser` b.
Why: Clean, bright laser with a fast attack (4.6 kHz centroid); a second laser flavour for Rear Lance vs Lance Laser.
Edit: leading silence trimmed, cut to 0.30 s, 0.14 s fade-out; peak -10.0 dBFS.

### shot-beam-r03-a — "heavy beam weapon" by deleted Freesound user (deleted_user_1941307)
Source: <https://freesound.org/people/deleted_user_1941307/sounds/152322/> — CC0 1.0. Use: `beam` a, 1.4 s seamless loop (Ion Beam).
Why: Steady for its first 1.7 s (flat envelope), ideal for a seamless loop; 8.5k downloads.
Edit: seamless 1.40 s loop from 0.20 s with 0.10 s cross-fade; peak -12.0 dBFS.

### shot-beam-r03-b — "SFX Oscilating Laser Beam" by bolkmar
Source: <https://freesound.org/people/bolkmar/sounds/420364/> — CC-BY 4.0. Use: `beam` b, 2.62 s seamless loop (Orbital Lance).
Why: Oscillating beam with a clean 1.31 s period; the loop covers exactly two periods.
Edit: seamless 2.62 s loop from 0.40 s with 0.08 s cross-fade; peak -12.0 dBFS.

### shot-missile-r03-a — "Rocket Launch" by Jarusca
Source: <https://freesound.org/people/Jarusca/sounds/521377/> — CC0 1.0. Use: `missile` (Hornet Launcher).
Why: Short rocket launch with a strong ignition transient and ~0.6 s whoosh; 5.6k downloads.
Edit: leading silence trimmed, cut to 0.90 s, 0.40 s fade-out; peak -8.0 dBFS.

### shot-micromissile-r03-a — "Rocket Shots" by Audionautics
Source: <https://freesound.org/people/Audionautics/sounds/171655/> — CC-BY 3.0. Use: `micromissile`, first shot.
Why: Isolated small rocket shots in a longer take; the first one is cut out.
Edit: leading silence trimmed, cut to 0.45 s, 0.20 s fade-out; peak -10.0 dBFS.

### shot-mortar-r03-a — "Mortar Shots.flac" by qubodup
Source: <https://freesound.org/people/qubodup/sounds/184382/> — CC0 1.0. Use: `mortar`, first shot.
Why: Recorded mortar shots; the first thump decays cleanly within 1 s.
Edit: leading silence trimmed, cut to 1.00 s, 0.50 s fade-out; peak -8.0 dBFS.

### shot-bomb-r03-a — "Falling Bomb.wav" by Daleonfire
Source: <https://freesound.org/people/Daleonfire/sounds/506313/> — CC0 1.0. Use: `bomb`, whistle cut to 1.2 s.
Why: Classic falling-bomb whistle (2.9 s); cut short for the frequent Bomb Rack, full length for the Airstrike special.
Edit: leading silence trimmed, cut to 1.20 s, 0.60 s fade-out; peak -10.0 dBFS.

### shot-torpedo-r03-a — "Torpedo launch underwater.wav" by jobro
Source: <https://freesound.org/people/jobro/sounds/35530/> — CC-BY 3.0. Use: `torpedo`.
Why: Real underwater launch with bubbles; the only good torpedo recording with a usable licence.
Edit: leading silence trimmed, cut to 1.00 s, 0.50 s fade-out; peak -8.0 dBFS.

### shot-mine-r03-a — "small metal object fall " by nicktermer
Source: <https://freesound.org/people/nicktermer/sounds/259553/> — CC0 1.0. Use: `mine` drop-and-bounce.
Why: Metallic object dropping and bouncing: two clunks read as 'dropped and landed'.
Edit: leading silence trimmed, cut to 0.90 s, 0.30 s fade-out; peak -8.0 dBFS.

### shot-tesla-r03-a — "Electric zap.wav" by michael_grinnell
Source: <https://freesound.org/people/michael_grinnell/sounds/512471/> — CC0 1.0. Use: `tesla`.
Why: Tight 0.22 s electric zap, 12.6k downloads; short enough to chain for Plasma Arc.
Edit: leading silence trimmed, cut to 0.22 s, 0.06 s fade-out; peak -10.0 dBFS.

### shot-resonator-r03-a — "sci-fi cannon" by humanoide9000
Source: <https://freesound.org/people/humanoide9000/sounds/422440/> — CC-BY 4.0. Use: `resonator` (Choir Resonator).
Why: Heavy sci-fi cannon with a sustained energy body; alien enough for captured Vrell tech.
Edit: leading silence trimmed, cut to 0.60 s, 0.30 s fade-out; peak -8.0 dBFS.

### explosion-tiny-r03-a — "Small Explosion" by Cyberios
Source: <https://freesound.org/people/Cyberios/sounds/145788/> — CC0 1.0. Use: `tiny` a.
Why: Very short, bass-heavy pop that decays within 0.45 s.
Edit: leading silence trimmed, cut to 0.45 s, 0.20 s fade-out; peak -4.0 dBFS.

### explosion-tiny-r03-b — "Small explosion" by dinodilopho
Source: <https://freesound.org/people/dinodilopho/sounds/328833/> — CC0 1.0. Use: `tiny` b.
Why: Single small blast with quick decay; thinner than a, so the two alternate well.
Edit: leading silence trimmed, cut to 0.60 s, 0.30 s fade-out; peak -4.0 dBFS.

### explosion-tiny-r03-c — "Firecracker Explosion" by unfa
Source: <https://freesound.org/people/unfa/sounds/609588/> — CC0 1.0. Use: `tiny` c, sharp crack.
Why: Firecracker crack: the sharpest transient of the set, decays in ~0.35 s.
Edit: leading silence trimmed, cut to 0.60 s, 0.30 s fade-out; peak -4.0 dBFS.

### explosion-small-r03-a — "Small Explosion" by lorenzgillner
Source: <https://freesound.org/people/lorenzgillner/sounds/271979/> — CC0 1.0. Use: `small` c.
Why: Small explosion with some crackle, a bit longer than the r02 small ones.
Edit: leading silence trimmed, cut to 1.10 s, 0.40 s fade-out; peak -1.5 dBFS.

### explosion-medium-r03-a — "Air Explosion.wav" by 1histori
Source: <https://freesound.org/people/1histori/sounds/401609/> — CC0 1.0. Use: `medium` b.
Why: Sharp airburst with a clean tail; lighter than r02-c.
Edit: leading silence trimmed, cut to 1.60 s, 0.60 s fade-out; peak -1.5 dBFS.

### explosion-medium-r03-b — "Explode001" by mitchelk
Source: <https://freesound.org/people/mitchelk/sounds/136765/> — CC0 1.0. Use: `medium` c.
Why: Dense, rumbling medium blast (10k downloads), cut to 2 s.
Edit: leading silence trimmed, cut to 2.00 s, 0.80 s fade-out; peak -1.5 dBFS.

### explosion-large-r03-a — "explosion_big_02.ogg" by derplayer
Source: <https://freesound.org/people/derplayer/sounds/587193/> — CC0 1.0. Use: `large` c.
Why: Sister file of r02-e: big blast with secondary crackle and debris.
Edit: leading silence trimmed, cut to 3.10 s, 1.00 s fade-out; peak -1.5 dBFS.

### explosion-huge-r03-a — "Explosion_01.wav" by tommccann
Source: <https://freesound.org/people/tommccann/sounds/235968/> — CC0 1.0. Use: `huge` a, 5 s.
Why: Most-downloaded CC0 explosion on Freesound (138k); long natural decay, cut to 5 s.
Edit: leading silence trimmed, offset 0.36 s, cut to 5.00 s, 2.00 s fade-out; peak -1.0 dBFS.

### explosion-huge-r03-b — "Big Boom" by unfa
Source: <https://freesound.org/people/unfa/sounds/189779/> — CC0 1.0. Use: `huge` b, 6 s, deep sub-bass.
Why: Deep cinematic boom dominated by sub-bass (94% of energy below 200 Hz), cut to 6 s.
Edit: leading silence trimmed, cut to 6.00 s, 2.50 s fade-out; peak -1.0 dBFS.

### explosion-underwater-r03-a — "underwater explosion.wav" by cubix
Source: <https://freesound.org/people/cubix/sounds/124544/> — CC0 1.0. Use: `underwater` a, recorded.
Why: Recorded underwater explosion: slow, muffled 'whump' with a long rumble.
Edit: leading silence trimmed, cut to 3.00 s, 1.20 s fade-out; peak -1.5 dBFS.

### explosion-underwater-r03-b — "Explosion" by qubodup
Source: <https://freesound.org/people/qubodup/sounds/182429/> — CC0 1.0. Use: `underwater` b, derived.
Why: Shows the derivation recipe: any explosion through a 4-pole 500 Hz low-pass (import_sfx.py `lowpass=500`).
Edit: leading silence trimmed, cut to 1.70 s, 0.60 s fade-out, 4-pole 500 Hz low-pass; peak -1.5 dBFS.

### explosion-water-r03-a — "Water Explosion" by Sheyvan
Source: <https://freesound.org/people/Sheyvan/sounds/519008/> — CC0 1.0. Use: `water`, surface naval kills.
Why: Explosion with water spray, sharp attack; for kills on the water surface.
Edit: leading silence trimmed, cut to 1.90 s, 0.70 s fade-out; peak -1.5 dBFS.

# Concept round 04 — audible beams, Sonar Pulse, extra huge and under-water explosions

The round 03 beam loops were rejected ("I can't even hear B"): both had ~100% of their energy
below 200 Hz, so they were loud on a meter but inaudible on small speakers. Round 04 candidates
were measured for their 200 Hz–5 kHz share, and beams (plus start/stop) are normalised on that
band's RMS (−30 dB, `band_rms=` in `tools/concept/audio/import_sfx.py`), the level the chosen
shots reach. Loop seams use a linear cross-fade when the overlapping parts correlate (> 0.5),
equal-power otherwise (`loop=(…, "auto")`).

### shot-beam-r04-a — "laser beam" by peepholecircus
Source: <https://freesound.org/people/peepholecircus/sounds/169991/> — CC0 1.0. Use: `beam` loop a, 2.6 s, smooth ~850 Hz hum (Ion Beam).
Why: 100% of its energy in 200 Hz–5 kHz (median 845 Hz); a steady 2.6 s stretch between its slow swells gives a smooth loop. 6k downloads.
Edit: seamless 2.60 s loop from 5.60 s with 0.12 s cross-fade; 200 Hz–5 kHz band RMS normalised to -30.0 dB (peak ceiling -3.0 dBFS).

### shot-beam-r04-b — "Weapons Beam Loop" by unfa
Source: <https://freesound.org/people/unfa/sounds/584191/> — CC0 1.0. Use: `beam` loop b, 1.9 s, gritty pulsing texture.
Why: Made as a beam loop by unfa; 72% mid-band with some grit on top, the most 'weapon-like' texture.
Edit: seamless 1.90 s loop from 0.00 s with 0.10 s cross-fade; 200 Hz–5 kHz band RMS normalised to -30.0 dB (peak ceiling -3.0 dBFS).

### shot-beam-r04-c — "SonicDeathRay_1.2KHzNoCrackle.wav" by zimbot
Source: <https://freesound.org/people/zimbot/sounds/177100/> — CC-BY 4.0. Use: `beam` loop c, 2.0 s, piercing ray (Orbital Lance).
Why: Very steady ray tone (~1.65 kHz, 100% mid-band): the most clearly audible option, suited to the Orbital Lance.
Edit: seamless 2.00 s loop from 1.00 s with 0.10 s cross-fade; 200 Hz–5 kHz band RMS normalised to -30.0 dB (peak ceiling -3.0 dBFS).

### shot-beam-start-r04-a — "Machine Charge.wav" by Glitchedtones
Source: <https://freesound.org/people/Glitchedtones/sounds/375925/> — CC0 1.0. Use: `beam` start, 0.9 s rising charge.
Why: Rising machine charge, 71% mid-band; the last 0.9 s of the rise leads into the loop.
Edit: leading silence trimmed, offset 1.10 s, cut to 0.90 s, 0.15 s fade-out; 200 Hz–5 kHz band RMS normalised to -30.0 dB (peak ceiling -3.0 dBFS).

### shot-beam-stop-r04-a — "Power Down" by noirenex
Source: <https://freesound.org/people/noirenex/sounds/159399/> — CC0 1.0. Use: `beam` stop, 1.3 s power-down.
Why: Short power-down whine, 72% mid-band (the peepholecircus 'Power Down' was 100% sub-bass, so rejected).
Edit: leading silence trimmed, cut to 1.30 s, 0.60 s fade-out; 200 Hz–5 kHz band RMS normalised to -30.0 dB (peak ceiling -3.0 dBFS).

### special-sonar-r04-a — "Sonar Ping" by SamsterBirdies
Source: <https://freesound.org/people/SamsterBirdies/sounds/539957/> — CC0 1.0. Use: Sonar Pulse, clean single ping.
Why: Classic single submarine ping, all energy in the mid band; decays within ~2.5 s.
Edit: leading silence trimmed, cut to 3.00 s, 1.00 s fade-out; peak-normalised to -4.0 dBFS.

### special-sonar-r04-b — "Ping!" by unfa
Source: <https://freesound.org/people/unfa/sounds/215415/> — CC0 1.0. Use: Sonar Pulse, long ringing ping.
Why: unfa's ping (13k downloads): longer, ringing tail for a more dramatic reveal.
Edit: leading silence trimmed, cut to 3.50 s, 1.50 s fade-out; peak-normalised to -4.0 dBFS.

### explosion-huge-r04-a — "Explosion with debris - authentic. 4kg TNT" by sidohzen
Source: <https://freesound.org/people/sidohzen/sounds/165808/> — CC0 1.0. Use: `huge` b, 6 s real blast.
Why: Authentic recorded 4 kg TNT blast with debris; 65% of its energy in the mid band, so it complements the sub-heavy huge a instead of doubling it.
Edit: leading silence trimmed, offset 1.63 s, cut to 6.00 s, 2.50 s fade-out; peak-normalised to -1.0 dBFS.

### explosion-underwater-r04-a — "underwater explosion" by mokasza
Source: <https://freesound.org/people/mokasza/sounds/810765/> — CC-BY 4.0. Use: `underwater` b.
Why: Muffled under-water blast with rumble; second recorded under-water variant for Act 4.
Edit: leading silence trimmed, cut to 3.50 s, 1.20 s fade-out; peak-normalised to -1.5 dBFS.
**Rejected** (round 52, Q.1, 2026-10-22: replace): its Freesound page now says it was generated by an AI model (ElevenLabs), against the real-recordings rule; removed from `assets/` and the credits roll, the file is in `rejected/`.

## Round 08 — remaining Acts 1–2 sounds

Sourced with the same rules as rounds 02–04 (CC0/CC-BY, licence checked on each page, Freesound HQ previews for concept; rebuild from the originals for production). One-shots are levelled on the 200 Hz–5 kHz band (`band_rms`) so sub-heavy sources stay audible; candidates with more than ~85 % of their energy below 200 Hz were rejected (Movie Trailer Boom, PlasmaCannon, Heavy Blast, Big Sci-Fi Cinematic Explosion, ATP2 power-down). Ambience files are seamless loops. Regenerate: `python3 tools/concept/audio/import_sfx.py <name ...>`.

Pickups and UI blips stay **synthesized** (see Sourcing in the README), extending the family of the three chosen round-01 pickups: `python3 tools/concept/audio/sfx_r08.py`.

### hit-metal-r08-a — "HeavyBulletPing.mp3" by wilhellboy
Source: <https://freesound.org/people/wilhellboy/sounds/351371/> — CC0 1.0. Use: Hit: metal — bullet ping on Ascendancy hulls and machines.
Why: Short heavy bullet ping, 77% mid-band; reads as 'shot hits armour' at rapid fire.
Edit: leading silence trimmed, cut to 0.30 s, 0.12 s fade-out; 200 Hz–5 kHz band RMS normalised to -30.0 dB (peak ceiling -8.0 dBFS).

### hit-metal-r08-b — "Bullet Hit Metal" by coolguy244e
Source: <https://freesound.org/people/coolguy244e/sounds/267893/> — CC0 1.0. Use: Hit: metal, variant.
Why: Clean metallic bullet impact, 86% mid-band; alternates with a.
Edit: leading silence trimmed, cut to 0.45 s, 0.20 s fade-out; 200 Hz–5 kHz band RMS normalised to -30.0 dB (peak ceiling -8.0 dBFS).

### hit-organic-r08-a — "splat.ogg" by gprosser
Source: <https://freesound.org/people/gprosser/sounds/360942/> — CC0 1.0. Use: Hit: organic — wet hit on Vrell chitin.
Why: Short wet splat; gives the Vrell a fleshy hit distinct from metal.
Edit: leading silence trimmed, cut to 0.30 s, 0.12 s fade-out; 200 Hz–5 kHz band RMS normalised to -30.0 dB (peak ceiling -8.0 dBFS).

### hit-organic-r08-b — "cracking.wav" by smidoid
Source: <https://freesound.org/people/smidoid/sounds/49139/> — CC-BY 4.0. Use: Hit: organic, chitin crunch.
Why: Dry cracking crunch (64% above 5 kHz) for armoured Vrell plates.
Edit: leading silence trimmed, cut to 0.40 s, 0.15 s fade-out; 200 Hz–5 kHz band RMS normalised to -30.0 dB (peak ceiling -8.0 dBFS).

### hit-crumble-r08-a — "building_collapse02_close.wav" by onteca
Source: <https://freesound.org/people/onteca/sounds/197772/> — CC-BY 3.0. Use: Ground target destroyed — crumbling structure.
Why: Close building collapse with debris clatter; 2.4 s.
Edit: leading silence trimmed, cut to 2.40 s, 0.80 s fade-out; peak-normalised to -1.5 dBFS.

### hit-crumble-r08-b — "Bricks/Stones/Rocks/Gravel Falling" by iwanPlays
Source: <https://freesound.org/people/iwanPlays/sounds/567249/> — CC0 1.0 (a mix of three CC0 rock sounds). Use: Ground target destroyed, small (rubble burst).
Why: Dense, crunchy rubble burst that starts at full strength, 65% mid-band. Replaces "Rock Smash" by NeoSpica (512243): 77% below 200 Hz with one sharp spike, so even high-passed it stayed peak-limited ~8 dB below a on the 200 Hz–5 kHz band.
Edit: leading silence trimmed, cut to 1.60 s, 0.60 s fade-out; 200 Hz–5 kHz band RMS normalised to -25.0 dB (peak ceiling -1.5 dBFS). The band level matches crumble a (-25.3 dB).

### player-shield-hit-r08-a — "ELECTRIC_ZAP_001.wav" by JoelAudio
Source: <https://freesound.org/people/JoelAudio/sounds/136542/> — CC0 1.0. Use: Shield hit — electric fizz.
Why: 37k-download electric zap, mostly above 5 kHz: a crisp fizz that never masks the music's low end.
Edit: leading silence trimmed, cut to 0.50 s, 0.20 s fade-out; 4-pole low-pass at 7 kHz (83% of the energy was above 5 kHz, harsh for a sound that plays often; now 56%, the fizz character kept); 200 Hz–5 kHz band RMS normalised to -24.0 dB (peak ceiling -2.0 dBFS).

### player-shield-hit-r08-b — "Sci-Fi Force Field Impact 15.wav" by StormwaveAudio
Source: <https://freesound.org/people/StormwaveAudio/sounds/330629/> — CC-BY 4.0. Use: Shield hit, force-field variant.
Why: Sci-fi force-field impact with a glassy tail.
Edit: leading silence trimmed, cut to 0.90 s, 0.35 s fade-out; 200 Hz–5 kHz band RMS normalised to -24.0 dB (peak ceiling -2.0 dBFS).

### player-shield-break-r08-a — "Synthesized_Pitch-Down_Zap" by joe_bou_khalil
Source: <https://freesound.org/people/joe_bou_khalil/sounds/861848/> — CC-BY 4.0. Use: Shield break — descending zap.
Why: Synthesized pitch-down zap, 100% mid-band; reads as 'shield collapsed'.
Edit: leading silence trimmed, cut to 1.40 s, 0.50 s fade-out; 200 Hz–5 kHz band RMS normalised to -24.0 dB (peak ceiling -2.0 dBFS).

### player-shield-restore-r08-a — "Power Up Charge [Remix of LegoLunatic's Charged laser 151243]" by qubodup
Source: <https://freesound.org/people/qubodup/sounds/172631/> — CC0 1.0. Use: Shield restored — rising charge.
Why: Short rising power-up charge (remix of LegoLunatic's charged laser).
Edit: leading silence trimmed, cut to 0.80 s, 0.25 s fade-out; 200 Hz–5 kHz band RMS normalised to -24.0 dB (peak ceiling -2.0 dBFS).

### player-shield-restore-r08-b — "Shield recharging" by Bychop
Source: <https://freesound.org/people/Bychop/sounds/136881/> — CC0 1.0. Use: Shield restored, longer recharge.
Why: Shield recharge hum rising over 1.6 s; for a full restore.
Edit: leading silence trimmed, cut to 1.60 s, 0.60 s fade-out; 200 Hz–5 kHz band RMS normalised to -24.0 dB (peak ceiling -2.0 dBFS).

### player-armour-hit-r08-a — "Impact on metal" by JoMungus
Source: <https://freesound.org/people/JoMungus/sounds/726486/> — CC0 1.0. Use: Armour hit — metallic crunch.
Why: Heavy impact on metal; distinct from the shield fizz so the player hears armour damage.
Edit: leading silence trimmed, cut to 0.60 s, 0.30 s fade-out; 200 Hz–5 kHz band RMS normalised to -24.0 dB (peak ceiling -2.0 dBFS).

### player-low-armour-r08-a — "Bleeper 1" by magnuswaker
Source: <https://freesound.org/people/magnuswaker/sounds/522162/> — CC0 1.0. Use: Low armour warning — one beep.
Why: Single clean bleep (88% mid-band); the game repeats it slowly at 30 % and fast at 15 % armour.
Edit: leading silence trimmed, cut to 0.40 s, 0.08 s fade-out; 200 Hz–5 kHz band RMS normalised to -24.0 dB (peak ceiling -2.0 dBFS).

### player-destroyed-r08-a — "spaceship explosion9.WAV" by phantastonia
Source: <https://freesound.org/people/phantastonia/sounds/270616/> — CC-BY 4.0. Use: Ship destroyed.
Why: Spaceship explosion with a long rumble; the music sting follows it.
Edit: leading silence trimmed, cut to 3.50 s, 4-pole high-pass at 20 Hz (sub-sonic drift left a 0.008 DC offset after the fade-out), 1.50 s fade-out; peak-normalised to -1.0 dBFS.

### overdrive-start-r08-a — "Spacey 1up/Power up" by GameAudio
Source: <https://freesound.org/people/GameAudio/sounds/220173/> — CC0 1.0. Use: Overdrive start.
Why: Spacey 1-up / power-up sweep, 99% mid-band.
Edit: leading silence trimmed, cut to 0.60 s, 0.15 s fade-out; 200 Hz–5 kHz band RMS normalised to -24.0 dB (peak ceiling -2.0 dBFS).

### overdrive-end-r08-a — "Teleport Error" by Jerimee
Source: <https://freesound.org/people/Jerimee/sounds/521776/> — CC0 1.0. Use: Overdrive end.
Why: Falling 'teleport error' wind-down; the ATP2 power-down candidate was 89% sub-bass, so rejected.
Edit: leading silence trimmed, cut to 1.10 s, 0.40 s fade-out; 200 Hz–5 kHz band RMS normalised to -24.0 dB (peak ceiling -2.0 dBFS).

### enemy-shot-small-r08-a — "Sci-fi gun shot x6" by humanoide9000
Source: <https://freesound.org/people/humanoide9000/sounds/330293/> — CC0 1.0. Use: Enemy shot, small.
Why: First shot of a six-shot sci-fi gun take; light and short.
Edit: leading silence trimmed, cut to 0.45 s, 0.20 s fade-out; 200 Hz–5 kHz band RMS normalised to -30.0 dB (peak ceiling -8.0 dBFS).

### enemy-shot-small-r08-b — "retro shot blaster" by JavierZumer
Source: <https://freesound.org/people/JavierZumer/sounds/257232/> — CC-BY 4.0. Use: Enemy shot, small (retro blaster).
Why: Retro blaster zap; alternates with a.
Edit: leading silence trimmed, cut to 0.32 s, 0.12 s fade-out; 200 Hz–5 kHz band RMS normalised to -30.0 dB (peak ceiling -8.0 dBFS).

### enemy-shot-heavy-r08-a — "ScifiHeavyBlasterShot.wav" by SuperPhat
Source: <https://freesound.org/people/SuperPhat/sounds/531861/> — CC0 1.0. Use: Enemy shot, heavy.
Why: Heavy sci-fi blaster shot, 47% mid-band (PlasmaCannon and Heavy Blast candidates were >85% sub-bass, rejected).
Edit: leading silence trimmed, cut to 0.75 s, 0.30 s fade-out; 200 Hz–5 kHz band RMS normalised to -30.0 dB (peak ceiling -8.0 dBFS).

### enemy-shot-heavy-r08-b — "Heavy blaster shot 05" by xkeril
Source: <https://freesound.org/people/xkeril/sounds/702000/> — CC0 1.0. Use: Enemy shot, heavy b.
Why: Bassy blaster made from a recorded hit, with a falling sweep; 50% mid-band, 40% sub, so it reads on small speakers and differs from a.
Edit: leading silence trimmed, cut to 0.90 s, 0.40 s fade-out; 200 Hz–5 kHz band RMS normalised to -30.0 dB (peak ceiling -8.0 dBFS).

### enemy-laser-warning-r08-a — "Laser Charging" by plasterbrain
Source: <https://freesound.org/people/plasterbrain/sounds/351807/> — CC0 1.0. Use: Enemy laser charge warning.
Why: First 1.2 s of a laser charge-up: the telegraph before a laser sweep or rail shot.
Edit: leading silence trimmed, cut to 1.20 s, 0.30 s fade-out; 200 Hz–5 kHz band RMS normalised to -30.0 dB (peak ceiling -8.0 dBFS).

### enemy-missile-r08-a — "Missile firing fl.mp3" by NHMWretched
Source: <https://freesound.org/people/NHMWretched/sounds/151858/> — CC0 1.0. Use: Enemy missile launch.
Why: Missile firing with a short exhaust tail (Jarusca's rocket is the player missile, so not reused).
Edit: leading silence trimmed, cut to 1.60 s, 0.60 s fade-out; 200 Hz–5 kHz band RMS normalised to -30.0 dB (peak ceiling -8.0 dBFS).

### enemy-screech-r08-a — "Monster screech" by Khrinx
Source: <https://freesound.org/people/Khrinx/sounds/565024/> — CC0 1.0. Use: Vrell screech (spawn/attack cue) a.
Why: Monster screech, 90% mid-band.
Edit: leading silence trimmed, cut to 2.20 s, 0.50 s fade-out; 200 Hz–5 kHz band RMS normalised to -30.0 dB (peak ceiling -8.0 dBFS).

### enemy-screech-r08-b — "inhuman screech.wav" by Wolfsinger
Source: <https://freesound.org/people/Wolfsinger/sounds/25713/> — CC-BY 4.0. Use: Vrell screech b.
Why: Inhuman screech, cut to 1.6 s.
Edit: leading silence trimmed, cut to 1.60 s, 0.50 s fade-out; 200 Hz–5 kHz band RMS normalised to -30.0 dB (peak ceiling -8.0 dBFS).

### enemy-screech-r08-c — "alien4.wav" by AlienXXX
Source: <https://freesound.org/people/AlienXXX/sounds/78539/> — CC-BY 4.0. Use: Vrell screech c.
Why: Alien call, cut to 1.6 s.
Edit: leading silence trimmed, cut to 1.60 s, 0.50 s fade-out; 200 Hz–5 kHz band RMS normalised to -30.0 dB (peak ceiling -8.0 dBFS).

### enemy-screech-r08-d — "alien screech.wav" by jvmyka@gmail.com
Source: <https://freesound.org/people/jvmyka@gmail.com/sounds/556535/> — CC0 1.0. Use: Vrell screech d.
Why: Tonal screech made from reversed, processed horse sounds (no human voice); first of two takes, 74% mid-band.
Edit: leading silence trimmed, cut to 2.00 s, 0.50 s fade-out; 200 Hz–5 kHz band RMS normalised to -30.0 dB (peak ceiling -8.0 dBFS).

### enemy-spawn-r08-a — "DCA Alien Spawning Birth.aif" by darcyadam
Source: <https://freesound.org/people/darcyadam/sounds/651487/> — CC0 1.0. Use: Vrell spawn a (Hive Node and Brood Carrier spawns, portal warp-in).
Why: Wet creature swell built from sponge, glassware, pans and cats; first of three takes.
Edit: leading silence trimmed, cut to 1.60 s, 0.60 s fade-out; 200 Hz–5 kHz band RMS normalised to -30.0 dB (peak ceiling -6.0 dBFS).

### enemy-spawn-r08-b — "goreSplat.wav" by ThefitzyG
Source: <https://freesound.org/people/ThefitzyG/sounds/414296/> — CC0 1.0. Use: Vrell spawn b (Brood Pod bursting).
Why: "Something exploded into tiny fleshy pieces": a wet burst with splatter. (Artninja's "morphing burst" was rejected: its description says it uses Warner Bros and Zapsplat library sounds, which cannot be relicensed as CC-BY.)
Edit: leading silence trimmed, cut to 1.70 s, 0.50 s fade-out; 200 Hz–5 kHz band RMS normalised to -30.0 dB (peak ceiling -6.0 dBFS).

### enemy-lock-r08-a — "lock on" by SamsterBirdies
Source: <https://freesound.org/people/SamsterBirdies/sounds/467881/> — CC0 1.0. Use: Turret lock-on beep.
Why: Rising lock-on beep sequence, first 1.06 s.
Edit: leading silence trimmed, cut to 1.06 s, 0.08 s fade-out; 200 Hz–5 kHz band RMS normalised to -30.0 dB (peak ceiling -8.0 dBFS).

### special-airstrike-jets-r08-a — "Jet Plane Flyby.flac" by qubodup
Source: <https://freesound.org/people/qubodup/sounds/189446/> — CC0 1.0. Use: Airstrike: jets flyby.
Why: Jet plane flyby, cut around its loudest pass (4.5 s with 0.6 s fade-in).
Edit: leading silence trimmed, offset 3.40 s, cut to 4.50 s, 0.60 s fade-in, 1.50 s fade-out; 200 Hz–5 kHz band RMS normalised to -24.0 dB (peak ceiling -1.5 dBFS).

### special-airstrike-bombs-r08-a — "R11-55-Large Blasts.wav" by craigsmith
Source: <https://freesound.org/people/craigsmith/sounds/483284/> — CC0 1.0. Use: Airstrike: bomb carpet.
Why: A series of large blasts recorded in sequence: a natural carpet-bombing run.
Edit: leading silence trimmed, offset 0.95 s, cut to 5.00 s, 1.50 s fade-out; peak-normalised to -1.5 dBFS.

### special-smartbomb-r08-a — "Energy Blast" by Kinoton
Source: <https://freesound.org/people/Kinoton/sounds/369516/> — CC0 1.0. Use: Smart bomb: charge-up + white-out boom.
Why: Energy blast with build and long tail, 62% mid-band (the Movie Trailer Boom candidate was 100% sub-bass, rejected).
Edit: leading silence trimmed, cut to 4.20 s, 1.50 s fade-out; peak-normalised to -1.5 dBFS.

### special-flares-r08-a — "Guns & Explosions Album - Flare gun 5-2.wav" by OGsoundFX
Source: <https://freesound.org/people/OGsoundFX/sounds/423109/> — CC-BY 4.0. Use: Decoy flares.
Why: Flare-gun launch with fizz.
Edit: leading silence trimmed, cut to 1.40 s, 0.60 s fade-out; 200 Hz–5 kHz band RMS normalised to -24.0 dB (peak ceiling -1.5 dBFS).

### special-denied-r08-a — "acess denied buzz" by Jacco18
Source: <https://freesound.org/people/Jacco18/sounds/419023/> — CC0 1.0. Use: Special unavailable (denied buzz).
Why: Short access-denied buzz.
Edit: leading silence trimmed, cut to 0.31 s, 0.05 s fade-out; 200 Hz–5 kHz band RMS normalised to -27.0 dB (peak ceiling -4.0 dBFS).

### ui-radio-open-r08-a — "Power On.wav" by JustinBW
Source: <https://freesound.org/people/JustinBW/sounds/70107/> — CC-BY 4.0. Use: Radio squelch open.
Why: Walkie-talkie power-on click before a radio line.
Edit: leading silence trimmed, cut to 0.30 s, 0.05 s fade-out; 200 Hz–5 kHz band RMS normalised to -27.0 dB (peak ceiling -4.0 dBFS).

### ui-radio-close-r08-a — "Radio Sign Off / Squelch" by JovianSounds
Source: <https://freesound.org/people/JovianSounds/sounds/524205/> — CC0 1.0. Use: Radio squelch close.
Why: Radio sign-off squelch after a radio line.
Edit: leading silence trimmed, cut to 0.50 s, 0.20 s fade-out; 200 Hz–5 kHz band RMS normalised to -27.0 dB (peak ceiling -4.0 dBFS).

### ui-klaxon-r08-a — "Sci-Fi Alarm" by noirenex
Source: <https://freesound.org/people/noirenex/sounds/159453/> — CC0 1.0. Use: Warning klaxon (boss, rear attack) — loop.
Why: Sci-fi alarm cut to two cycles (2 × 2.38 s) as a seamless loop.
Edit: leading silence trimmed, seamless 4.76 s loop from 0.00 s with a 0.08 s cross-fade; 200 Hz–5 kHz band RMS normalised to -27.0 dB (peak ceiling -4.0 dBFS).

### ui-klaxon-r08-b — "RedAlert_Klaxon_STTOS_recreated.wav" by zimbot
Source: <https://freesound.org/people/zimbot/sounds/178032/> — CC-BY 4.0. Use: Warning klaxon, single blast.
Why: Red-alert klaxon recreation, one blast.
Edit: leading silence trimmed, cut to 0.95 s, 0.10 s fade-out; 200 Hz–5 kHz band RMS normalised to -27.0 dB (peak ceiling -4.0 dBFS).

### ambience-orbit-r08-a — "spacedrone3.wav" by Elektrocell
Source: <https://freesound.org/people/Elektrocell/sounds/20705/> — CC0 1.0. Use: Ambience: Earth orbit (space hum).
Why: Slow evolving space drone; 16 s loop.
Edit: leading silence trimmed, seamless 16.00 s loop from 20.00 s with a 1.50 s cross-fade; 200 Hz–5 kHz band RMS normalised to -36.0 dB (peak ceiling -10.0 dBFS).

### ambience-luna-r08-a — "drone Space wind scifi.wav" by ztitchez
Source: <https://freesound.org/people/ztitchez/sounds/370754/> — CC-BY 4.0. Use: Ambience: Luna.
Why: Desolate sci-fi space-wind drone; 16 s loop.
Edit: leading silence trimmed, seamless 16.00 s loop from 10.00 s with a 1.50 s cross-fade; 200 Hz–5 kHz band RMS normalised to -36.0 dB (peak ceiling -10.0 dBFS).

### ambience-city-r08-a — "201110 Distant sirens, urban, night, quiet, roof 11pm.flac" by TRP
Source: <https://freesound.org/people/TRP/sounds/568975/> — CC0 1.0. Use: Ambience: megacity.
Why: Night city from a rooftop with distant sirens; 20 s loop.
Edit: leading silence trimmed, seamless 20.00 s loop from 30.00 s with a 2.00 s cross-fade; 200 Hz–5 kHz band RMS normalised to -36.0 dB (peak ceiling -10.0 dBFS).

### ambience-ocean-r08-a — "Ocean waves hitting bow of moving boat." by byjoshberry
Source: <https://freesound.org/people/byjoshberry/sounds/435668/> — CC-BY 4.0. Use: Ambience: ocean.
Why: Waves against a moving bow: open sea at speed; 16 s loop.
Edit: leading silence trimmed, seamless 16.00 s loop from 2.00 s with a 2.00 s cross-fade; 200 Hz–5 kHz band RMS normalised to -36.0 dB (peak ceiling -10.0 dBFS).

### ambience-storm-r08-a — "Rain and Thunder 4" by FlatHill
Source: <https://freesound.org/people/FlatHill/sounds/237729/> — CC0 1.0. Use: Ambience: ocean storm (rain + thunder).
Why: Rain and thunder (132k downloads); 24 s loop with one thunder roll near the start.
Edit: leading silence trimmed, seamless 24.00 s loop from 1.00 s with a 2.00 s cross-fade; 200 Hz–5 kHz band RMS normalised to -36.0 dB (peak ceiling -10.0 dBFS).

### ambience-arctic-r08-a — "Wind__Artic__Cold.wav" by cobratronik
Source: <https://freesound.org/people/cobratronik/sounds/117136/> — CC0 1.0. Use: Ambience: arctic wind.
Why: Cold arctic wind; 16 s loop.
Edit: leading silence trimmed, seamless 16.00 s loop from 30.00 s with a 2.00 s cross-fade; 200 Hz–5 kHz band RMS normalised to -36.0 dB (peak ceiling -10.0 dBFS).

### pickup-salvage-small-r08-a (synthesized)
Salvage small — short, high PWM blip with one sparkle (pickup-r01-c family, which itself is salvage medium). Generator: `tools/concept/audio/sfx_r08.py`.
AI prompt: "late-90s game UI/pickup sound, short, high PWM blip with one sparkle (pickup-r01-c family, which itself is salvage medium), clean synthesized chiptune-meets-FM timbre, short, no reverb wash, no voice"

### pickup-salvage-large-r08-a (synthesized)
Salvage large — long PWM sweep two octaves up with an octave layer and a sparkle shower. Generator: `tools/concept/audio/sfx_r08.py`.
AI prompt: "late-90s game UI/pickup sound, long PWM sweep two octaves up with an octave layer and a sparkle shower, clean synthesized chiptune-meets-FM timbre, short, no reverb wash, no voice"

### pickup-shield-cell-r08-a (synthesized)
Shield cell — cool rising triangle arpeggio (G major) with a chorus shimmer. Generator: `tools/concept/audio/sfx_r08.py`.
AI prompt: "late-90s game UI/pickup sound, cool rising triangle arpeggio (G major) with a chorus shimmer, clean synthesized chiptune-meets-FM timbre, short, no reverb wash, no voice"

### pickup-armour-patch-r08-a (synthesized)
Armour patch — low square 'clunk' plus a metallic ding: a plate bolted on. Generator: `tools/concept/audio/sfx_r08.py`.
The ding rings out over 0.7 s (it was cut at 0.45 s).
AI prompt: "late-90s game UI/pickup sound, low square 'clunk' plus a metallic ding: a plate bolted on, clean synthesized chiptune-meets-FM timbre, short, no reverb wash, no voice"

### pickup-special-charge-r08-a (synthesized)
Special charge — three rising square notes ending in a bell. Generator: `tools/concept/audio/sfx_r08.py`.
AI prompt: "late-90s game UI/pickup sound, three rising square notes ending in a bell, clean synthesized chiptune-meets-FM timbre, short, no reverb wash, no voice"

### ui-menu-move-r08-a (synthesized)
Menu move — soft 50 ms triangle blip. Generator: `tools/concept/audio/sfx_r08.py`.
AI prompt: "late-90s game UI/pickup sound, soft 50 ms triangle blip, clean synthesized chiptune-meets-FM timbre, short, no reverb wash, no voice"

### ui-menu-confirm-r08-a (synthesized)
Menu confirm — two rising square notes (E6–B6). Generator: `tools/concept/audio/sfx_r08.py`.
AI prompt: "late-90s game UI/pickup sound, two rising square notes (E6–B6), clean synthesized chiptune-meets-FM timbre, short, no reverb wash, no voice"

### ui-menu-back-r08-a (synthesized)
Menu back — two falling square notes (B5–E5). Generator: `tools/concept/audio/sfx_r08.py`.
AI prompt: "late-90s game UI/pickup sound, two falling square notes (B5–E5), clean synthesized chiptune-meets-FM timbre, short, no reverb wash, no voice"

### ui-shop-buy-r08-a (synthesized)
Shop buy — blip plus coin sparkle ('ka-ching'). Generator: `tools/concept/audio/sfx_r08.py`.
AI prompt: "late-90s game UI/pickup sound, blip plus coin sparkle ('ka-ching'), clean synthesized chiptune-meets-FM timbre, short, no reverb wash, no voice"

### ui-shop-sell-r08-a (synthesized)
Shop sell — three descending coin blips. Generator: `tools/concept/audio/sfx_r08.py`.
AI prompt: "late-90s game UI/pickup sound, three descending coin blips, clean synthesized chiptune-meets-FM timbre, short, no reverb wash, no voice"

### ui-shop-denied-r08-a (synthesized)
Shop denied (can't afford / won't fit) — low beating square buzz. Generator: `tools/concept/audio/sfx_r08.py`.
AI prompt: "late-90s game UI/pickup sound, low beating square buzz, clean synthesized chiptune-meets-FM timbre, short, no reverb wash, no voice"

### ui-typewriter-r08-a (synthesized)
Typewriter blip — 30 ms click-blip, played per character of briefing text. Generator: `tools/concept/audio/sfx_r08.py`.
AI prompt: "late-90s game UI/pickup sound, 30 ms click-blip, played per character of briefing text, clean synthesized chiptune-meets-FM timbre, short, no reverb wash, no voice"

### ui-tally-tick-r08-a (synthesized)
Debrief tally tick — 30 ms high sine tick. Generator: `tools/concept/audio/sfx_r08.py`.
AI prompt: "late-90s game UI/pickup sound, 30 ms high sine tick, clean synthesized chiptune-meets-FM timbre, short, no reverb wash, no voice"

### ui-tally-total-r08-a (synthesized)
Debrief total — C-major bell chord stinger. Generator: `tools/concept/audio/sfx_r08.py`.
AI prompt: "late-90s game UI/pickup sound, C-major bell chord stinger, clean synthesized chiptune-meets-FM timbre, short, no reverb wash, no voice"

### ui-equip-r08-a (synthesized)
Hangar equip — square clunk (220 → 110 Hz), latch click and a quiet rising two-note blip (G5–D6, the ui-menu-confirm shape). Generator: `tools/concept/audio/sfx_r08.py`.
AI prompt: "late-90s game UI sound, hangar equip: a short low square clunk and a latch click, then a quiet rising two-note blip, clean synthesized chiptune-meets-FM timbre, short, no reverb wash, no voice"

### ui-upgrade-r08-a (synthesized)
Hangar upgrade — short PWM rise (pickup-r01-c family) landing on a G6 bell with a D7 overtone and a few sparkles. Generator: `tools/concept/audio/sfx_r08.py`.
AI prompt: "late-90s game UI sound, hangar upgrade: a quick rising synth sweep that lands on a bright bell, a few sparkles, clean synthesized chiptune-meets-FM timbre, short, no reverb wash, no voice"

### ui-grade-stamp-r08-a (synthesized)
Debrief grade stamp — punchy thump with a 300 → 140 Hz square body (audible on small speakers), a band-passed paper slap and a low C5 bell. Generator: `tools/concept/audio/sfx_r08.py`.
AI prompt: "late-90s game UI sound, a grade being stamped onto a debrief report: a punchy rubber-stamp thump with a paper slap and a short low bell, clean synthesized timbre, short, no reverb wash, no voice"

## Round 11 (synthesized, `tools/concept/audio/sfx_r11.py`)

Levelled on the 200 Hz–5 kHz band RMS like the recorded sounds; transients above the ceiling go
through the look-ahead limiter instead of turning the whole sound down. Launch rail −27 dB /
−4 dBFS; warning tones −24 dB / −2 dBFS.

### launch-rail-r11-a (synthesized)
Launch rail "mag-lev" — 4.9 s: a saw/pulse linear-motor hum (62 → 362 Hz, low-pass opening) and a
whine at its 6th harmonic rising with the ship's speed, coil ticks at the rail segments (5 → 46
per second, an even acceleration), the motor letting go at 3.6 s with a low clunk, a metal ring
and a latch click, then the engine whoosh into open space. Generator: `tools/concept/audio/sfx_r11.py`.
AI prompt: "late-90s sci-fi game sound, a fighter launched down an electromagnetic rail on an orbital station: rising linear-motor hum and whine, accelerating clicks of rail segments, a heavy release clunk with a metallic ring, then a short engine whoosh, about 5 seconds, clean, no voice, no music"

### launch-rail-r11-b (synthesized)
Launch rail "catapult" — 4.9 s: a pressure hiss building (band-pass opening 1.2 → 4.2 kHz), the
shuttle rumbling over the rail joints (3 → 30 per second) with a metal rattle, a faint pressure
whistle, a heavy two-stage buffer clunk at 3.6 s with a ring, then the steam vent dying away.
Generator: `tools/concept/audio/sfx_r11.py`.
AI prompt: "late-90s sci-fi game sound, an aircraft-carrier style steam catapult on a space station: building pressure hiss, rumbling shuttle over rail joints with rattles, a heavy buffer clunk, venting steam, about 5 seconds, no voice, no music"

### ui-edge-warning-r11-a (synthesized)
Edge warning tone "triple chirp" — three 75 ms square blips (30 % pulse width, low-passed at
3.5 kHz) on A5, A5, E6, 0.267 s apart (the warning's flash cycle: 8 game frames on, 8 off), each
with a quiet octave edge. Generator: `tools/concept/audio/sfx_r11.py`.
AI prompt: "late-90s arcade shooter HUD alert, three short soft square-wave chirps, the last one higher, crisp and attention-grabbing but not harsh, under one second, no voice"

### ui-edge-warning-r11-b (synthesized)
Edge warning tone "contact ping" — an upward chirp (700 Hz → 1.6 kHz in ~60 ms) into a ringing
ping that decays over ~0.35 s, a quieter repeat one flash cycle later, a little room.
Generator: `tools/concept/audio/sfx_r11.py`.
AI prompt: "late-90s sci-fi game radar contact alert, a short rising chirp into a ringing ping with one quieter echo, clean synthesized, under one second, no voice"

## Round 16 (synthesized, `tools/concept/audio/sfx_r16.py`)

Levelled on the 200 Hz–5 kHz band RMS like the recorded enemy sounds (−30 dB, the Vrell screech
and spawn level), transients limited at −6 dBFS, so it sits under the large explosion it plays
with (those are at −21 to −24 dB band RMS, −1.6 dBFS peak, −11 to −14.5 LUFS).

### enemy-leviathan-cry-r16-a (synthesized)
The Leviathan's whale-song cry, played when it dies (design/enemies/space/leviathan) — 2.4 s of
call and a reverb tail, 3.0 s in all: a throat-like pulse train (a smooth glottal pulse mixed
with a band-limited saw, slight jitter) whose fundamental rises 92 → 128 Hz in the first half
second, then falls in a long moan to 52 Hz with one upward yodel break at 1.05 s and a slow
vibrato that widens; three moving formants sweep "oo" → "aah" → "oo" (about 320/780/1900 Hz at
the open vowel) so it reads as a throat; a second, quieter voice a tritone and a few cents above
enters at 0.3 s and glides more slowly, beating against the first; a faint inharmonic whistle
(7.3×) falls with it; a breathy band-passed rasp (the blowhole) opens the call; a long, dark
2.6 s reverb gives it its size. High-passed at 70 Hz so the weight is in the chest, not in
sub-bass a small speaker drops (about 80 % of its energy is in 200 Hz–2 kHz, in the explosion's
gaps rather than its low boom, and 20 % in the 60–200 Hz chest). −22.5 LUFS, peak −11.3 dBFS (the Vrell screeches: −22 LUFS).
Generator: `tools/concept/audio/sfx_r16.py`.
AI prompt: "late-90s sci-fi game sound, a huge alien space whale dying: one deep, mournful whale-song call, a moaning glide that rises and then falls slowly with a yodel break, two voices beating against each other, breathy, with a long dark reverb, about 3 seconds, no explosion, no music"

## Round 21 — Level 05's sled and Polyp Mortar (M4 part E, user decision D8)

Recorded third-party sounds (enemy and hazard sounds are recorded CC0/CC-BY, the 2026-10-01 rule), imported by `tools/concept/audio/import_sfx.py` from the Freesound HQ previews; one a/b pair per sound. The chosen four (whine a, pass b, lob a, impact a) were rebuilt from the Freesound originals with the same settings by `tools/art/sfx_originals.py` (2026-10-05; the pass's original clipped at full scale first) into `assets/sfx/` under their concept names; the files here stay the record of the choice.

### hazard-sled-whine-r21-a — "Railgun_ChargeLoop" by BaggoNotes
Source: <https://freesound.org/people/BaggoNotes/sounds/785400/> — CC0 1.0. Use: sled whine a, a loop played while the rail lights chase (1.5 s).
Why: A rail-gun charge hum: a steady electric drone that loops cleanly; mechanical and close to the rail's own machinery.
Edit: leading silence trimmed, a seamless 0.78 s loop from 0.00 s (0.04 s cross-fade); 200 Hz–5 kHz band RMS normalised to -30.0 dB (peak ceiling -8.0 dBFS).

### hazard-sled-whine-r21-b — "charging power" by JavierZumer
Source: <https://freesound.org/people/JavierZumer/sounds/257229/> — CC-BY 4.0. Use: sled whine b, played once over the 1.5 s chase.
Why: An electrical machine charging up: the last 1.6 s of a rising whine that ends at its peak as the sled fires.
Edit: leading silence trimmed, cut from 2.00 s, cut to 1.60 s, 0.30 s fade-in, 0.10 s fade-out; 200 Hz–5 kHz band RMS normalised to -30.0 dB (peak ceiling -8.0 dBFS).

### hazard-sled-pass-r21-a — "shipboard_railgun.mp3" by deleted_user_1941307
Source: <https://freesound.org/people/deleted_user_1941307/sounds/155790/> — CC0 1.0. Use: sled pass a, the 0.4 s run.
Why: A heavy shipboard rail-gun crack with a long decaying tail (the uploader's own mix of Freesound sounds, released as CC0; the account has since been deleted).
Edit: leading silence trimmed, cut to 1.80 s, 0.70 s fade-out; 200 Hz–5 kHz band RMS normalised to -24.0 dB (peak ceiling -3.0 dBFS).

### hazard-sled-pass-r21-b — "Planetary Flyby faster.aif" by mattpavone
Source: <https://freesound.org/people/mattpavone/sounds/76175/> — CC0 1.0. Use: sled pass b, the 0.4 s run.
Why: A large object rushing past with a low rumble: cut around its loudest second, so it reads as the sled racing by.
Edit: leading silence trimmed, cut from 0.30 s, cut to 1.60 s, 0.15 s fade-in, 0.70 s fade-out; 200 Hz–5 kHz band RMS normalised to -24.0 dB (peak ceiling -3.0 dBFS).

### enemy-mortar-lob-r21-a — "Mortar" by Mozfoo
Source: <https://freesound.org/people/Mozfoo/sounds/529239/> — CC0 1.0. Use: Polyp Mortar lob a.
Why: A real mortar being fired (from a military training recording, with reverb): a deep, hollow thump.
Edit: leading silence trimmed, cut to 1.20 s, 0.60 s fade-out; 200 Hz–5 kHz band RMS normalised to -27.0 dB (peak ceiling -6.0 dBFS).

### enemy-mortar-lob-r21-b — "NPX Throat Gathering Spit 3.wav" by noahpardo
Source: <https://freesound.org/people/noahpardo/sounds/352404/> — CC0 1.0. Use: Polyp Mortar lob b.
Why: An organic spit: the polyp hawking its acid glob, wetter and more creature-like than a gun.
Edit: leading silence trimmed, cut to 0.60 s, 0.15 s fade-out; 200 Hz–5 kHz band RMS normalised to -27.0 dB (peak ceiling -6.0 dBFS).

### enemy-mortar-impact-r21-a — "Wet Splat" by JustInvoke
Source: <https://freesound.org/people/JustInvoke/sounds/446115/> — CC-BY 4.0. Use: Polyp Mortar impact a, the blob landing.
Why: A moist splat: short, so the 8-bullet ring stays readable.
Edit: leading silence trimmed, cut to 0.69 s, 0.30 s fade-out; 200 Hz–5 kHz band RMS normalised to -27.0 dB (peak ceiling -4.0 dBFS).

### enemy-mortar-impact-r21-b — "Acid Bubbling.wav" by spookymodem
Source: <https://freesound.org/people/spookymodem/sounds/202094/> — CC0 1.0. Use: Polyp Mortar impact b, the blob landing.
Why: Acid bubbling up: a hissing sizzle rather than a splat.
Edit: leading silence trimmed, cut to 1.60 s, 0.60 s fade-out; 200 Hz–5 kHz band RMS normalised to -30.0 dB (peak ceiling -8.0 dBFS).

## Round 23 — Level 06's Mantis, flares and Coilwyrm (M4 part F, user decision D8)

Recorded third-party sounds (enemy and hazard sounds are recorded CC0/CC-BY, the 2026-10-01 rule), imported by `tools/concept/audio/import_sfx.py` from the Freesound HQ previews; one a/b pair per sound. The chosen five (telegraph a, sweep b, flare launch a, flare burn a, regrowth b) were rebuilt from the Freesound originals with the same settings by `tools/art/sfx_originals.py` (2026-10-05; the burn stays a seamless 3.00 s loop) into `assets/sfx/` under their concept names; the files here stay the record of the choice. Not in this round: the data core pickup (already mapped to the chosen `pickup-r01-b`, round 08), the Smart Bomb (chosen `special-smartbomb-r08-a`), the Coilwyrm's fan and pops (the enemy shot and explosion ladder) and the round-08 `enemy-laser-warning-r08-a` (chosen for enemy laser charges in general; it stays an option for the Mantis telegraph).

Outcome (user, 2026-10-05): telegraph **a**, sweep **b** (CC-BY), flare launch **a** (CC-BY), flare burn **a**, Coilwyrm regrowth **b**; the other five moved to `concept/rejected/` (the headings below keep the files' names).

### enemy-mantis-telegraph-r23-a — "Laser Charge Up" by magnuswaker
Source: <https://freesound.org/people/magnuswaker/sounds/588242/> — CC0 1.0. Use: Mantis telegraph a, the 0.6 s crimson arc before a sweep.
Why: A bright, rising laser charge-up cut in its rise, so it ends as the beam fires.
Edit: leading silence trimmed, cut from 0.10 s, cut to 0.60 s, 0.06 s fade-out; 200 Hz–5 kHz band RMS normalised to -30.0 dB (peak ceiling -8.0 dBFS).

### enemy-mantis-telegraph-r23-b — "laser-charge.wav" by StavSounds
Source: <https://freesound.org/people/StavSounds/sounds/701702/> — CC0 1.0. Use: Mantis telegraph b, the 0.6 s crimson arc before a sweep.
Why: A lower, buzzing charge: its loudest 0.6 s, more menace than sparkle.
Edit: leading silence trimmed, cut from 0.60 s, cut to 0.60 s, 0.05 s fade-in, 0.06 s fade-out; 200 Hz–5 kHz band RMS normalised to -30.0 dB (peak ceiling -8.0 dBFS).

### enemy-mantis-sweep-r23-a — "Game - Style Laser Beam" by Jofae
Source: <https://freesound.org/people/Jofae/sounds/352852/> — CC0 1.0. Use: Mantis sweep a, the 1.2 s beam.
Why: A steady, game-style laser beam: a bright continuous tone that reads over the music.
Edit: leading silence trimmed, cut from 0.20 s, cut to 1.30 s, 0.02 s fade-in, 0.25 s fade-out; 200 Hz–5 kHz band RMS normalised to -27.0 dB (peak ceiling -6.0 dBFS).

### enemy-mantis-sweep-r23-b — "SonicDeathRay_1.2KHzWithCrackle.wav" by zimbot
Source: <https://freesound.org/people/zimbot/sounds/177099/> — CC-BY 4.0. Use: Mantis sweep b, the 1.2 s beam.
Why: A 1.2 kHz death-ray tone with crackle: harsher and more organic-electric.
Edit: leading silence trimmed, cut from 1.00 s, cut to 1.30 s, 0.02 s fade-in, 0.25 s fade-out; 200 Hz–5 kHz band RMS normalised to -27.0 dB (peak ceiling -6.0 dBFS).

### hazard-flare-launch-r23-a — "FlareGun_Shot01.wav" by marb7e
Source: <https://freesound.org/people/marb7e/sounds/674375/> — CC-BY 4.0. Use: flare launch a, the perimeter beacon firing a flare shell (t=70, 112, 150).
Why: A real flare pistol shot: a sharp pop with a short hiss.
Edit: leading silence trimmed, cut to 0.80 s, 0.40 s fade-out; 200 Hz–5 kHz band RMS normalised to -27.0 dB (peak ceiling -4.0 dBFS).

### hazard-flare-launch-r23-b — "firework_rocket_ignition.ogg" by derplayer
Source: <https://freesound.org/people/derplayer/sounds/587173/> — CC0 1.0. Use: flare launch b, the perimeter beacon firing a flare shell (t=70, 112, 150).
Why: A firework rocket igniting and climbing away: a longer rushing hiss, the shell going up.
Edit: leading silence trimmed, cut from 0.40 s, cut to 1.60 s, 0.05 s fade-in, 0.70 s fade-out; 200 Hz–5 kHz band RMS normalised to -37.0 dB (peak ceiling -8.0 dBFS).

### hazard-flare-burn-r23-a — "ROAD FLARE.wav" by frankelmedico
Source: <https://freesound.org/people/frankelmedico/sounds/348766/> — CC0 1.0. Use: flare burn a, a quiet loop while a flare falls (8 s, 12 s on easy).
Why: A road flare's steady sputter, cut as a seamless loop.
Edit: leading silence trimmed, a seamless 3.00 s loop from 4.00 s (0.50 s cross-fade); 200 Hz–5 kHz band RMS normalised to -34.0 dB (peak ceiling -10.0 dBFS).

### hazard-flare-burn-r23-b — "Road Flare1" by theshaggyfreak
Source: <https://freesound.org/people/theshaggyfreak/sounds/317834/> — CC-BY 4.0. Use: flare burn b, a quiet loop while a flare falls (8 s, 12 s on easy).
Why: A road flare burning close by: a brighter, hissier loop.
Edit: leading silence trimmed, a seamless 3.00 s loop from 3.00 s (0.50 s cross-fade); 200 Hz–5 kHz band RMS normalised to -34.0 dB (peak ceiling -10.0 dBFS).

### enemy-coilwyrm-regrow-r23-a — "Slime 3.wav" by Archos
Source: <https://freesound.org/people/Archos/sounds/433826/> — CC0 1.0. Use: Coilwyrm regrowth a, the 0.6 s a cut chain's new head grows.
Why: Wet slime stretching: flesh knitting itself into a head.
Edit: leading silence trimmed, cut to 0.60 s, 0.15 s fade-out; 200 Hz–5 kHz band RMS normalised to -27.0 dB (peak ceiling -6.0 dBFS).

### enemy-coilwyrm-regrow-r23-b — "Insect Animal Growl Chitter" by SecureSubset
Source: <https://freesound.org/people/SecureSubset/sounds/800277/> — CC0 1.0. Use: Coilwyrm regrowth b, the 0.6 s a cut chain's new head grows.
Why: An insect growl and chitter: the new head waking, more creature than flesh.
Edit: leading silence trimmed, cut from 0.10 s, cut to 0.60 s, 0.03 s fade-in, 0.15 s fade-out; 200 Hz–5 kHz band RMS normalised to -27.0 dB (peak ceiling -6.0 dBFS).

## Round 24 — the Coilwyrm's death bursts

Brief: the Coilwyrm's chained death as a ripple of bursts from the head to the tail, one every
0.05 s (13 at medium): wet organic bursts with some crunch, a pressurised bio-creature popping, not
a fireball. Each burst must read on its own when it repeats every 50 ms (a sharp onset, most of its
energy in the first 100–150 ms, no long tail), so the sequence sounds like a ripple rather than a
smear; the head's burst is deeper and longer and starts it. All files come from
`tools/concept/audio/sfx_r24.py` (a–c: the public HQ previews, fetched and cut with
`import_sfx.py`'s functions; d synthesized), deterministic. Levels are matched on the loudest
100 ms of the 200 Hz–5 kHz band: a segment burst at -16 dB (the tiny explosions measure -18 and
-14), ceiling -1.5 dBFS; the head's burst at -14 dB (explosion-r02-a -15), ceiling -1.5 dBFS. The
head burst of every option is its source cut longer, played 18 % slower, over a sub thump (85 →
38 Hz). The `enemy-coilwyrm-death-r24-<v>` previews are for the review only: the head's burst
at the explosion level, then 13 bursts 0.05 s apart, 4 dB lower, pitch rising 1.8 % a burst from
0.94 (±2 % random), the oldest stopped beyond six instances, as `FlightSounds` played them in the
round.

**Outcome (2026-10-05):** the segment burst is **b** and the head burst **c**; the other files are
in `rejected/`. The ripple is slowed to one burst every 0.25 s (user: "the effects too fast"), so
the bursts no longer overlap and play at the explosion level, each pitched by the member's hit box
width (0.94 at the first segment up to 1.2 at the last, the tail in between). The production
files `assets/sfx/enemy-coilwyrm-burst-r24-b.ogg` and `assets/sfx/enemy-coilwyrm-head-burst-r24-c.ogg`
are rebuilt from the Freesound originals by `tools/art/sfx_originals.py` with sfx_r24.py's
treatment (its `PRODUCTION`).

### enemy-coilwyrm-death-final-r24-a — the chosen pair at the game's rhythm
Source: `tools/concept/audio/sfx_r24.py final`, from the Freesound originals of b and c (the HQ previews when they are not cached). Use: review only.
Why: To hear the chained death as the game plays it: the head burst c at 0 s, then the 12 segments and the tail with burst b, 0.25 s apart (about 3.25 s), each at the explosion level and pitched by its width down the taper with ±2 % random.

### enemy-coilwyrm-burst-r24-a — "Gib sound" by RoozyDB (rejected)
Source: <https://freesound.org/people/RoozyDB/sounds/504629/> — CC0 1.0. Use: Coilwyrm segment and tail burst a (and its head burst).
Why: A sharp wet crack with gooey bits after it; the cleanest onset of the four, so the ripple's bursts stay distinct.
Edit: leading silence trimmed, cut to 0.30 s, 90 Hz high-pass, 0.16 s fade-out; levelled as the brief says (the crack meets the ceiling, so its band level ends near -20 dB). Head: cut to 0.45 s, 50 Hz high-pass, 0.20 s fade-out, slowed, with the thump.

### enemy-coilwyrm-burst-r24-b — "Burst Flesh" by magnuswaker (segment burst chosen)
Source: <https://freesound.org/people/magnuswaker/sounds/581092/> — CC0 1.0. Use: Coilwyrm segment and tail burst b (and its head burst).
Why: A dense fleshy burst with a low body: the biggest of the four.
Edit: leading silence trimmed, cut from 0.015 s to 0.24 s, 90 Hz high-pass, 0.14 s fade-out; levelled as the brief says. Head: cut to 0.62 s, 50 Hz high-pass, 0.30 s fade-out, slowed, with the thump.

### enemy-coilwyrm-burst-r24-c — "Messy Splat 3" by FoolBoyMedia (head burst chosen)
Source: <https://freesound.org/people/FoolBoyMedia/sounds/237927/> — CC-BY 4.0. Use: Coilwyrm segment and tail burst c (and its head burst).
Why: A bright, messy splatter (most of it at 600 Hz–2 kHz): splashier and less punchy.
Edit: leading silence trimmed, cut to 0.22 s, 90 Hz high-pass, 0.14 s fade-out; levelled as the brief says. Head: cut to 0.62 s, 50 Hz high-pass, 0.30 s fade-out, slowed, with the thump.

### enemy-coilwyrm-burst-r24-d — synthesized pressurised pop (rejected)
Source: `tools/concept/audio/sfx_r24.py` (`splat`), seeds 2401 (segment) and 2402 (head). Use: Coilwyrm segment and tail burst d (and its head burst).
Why: A burst made for the ripple: a band-noise snap (the membrane rupturing), ten hard chitin clicks in the first 50 ms, a saturated gas "bloop" falling 230 → 70 Hz with a low thud, twelve wet bubble chirps (0.9–2.6 kHz, rising) scattered over 200 ms and a splash hiss; 90 Hz high-pass, 0.26 s. The head: a lower bloop (170 → 45 Hz), twenty drops over 350 ms, 0.6 s, slowed, with the thump.

## Round 25 — Level 07's Brood Carrier and lifeboat tow (M4 part G)

Brief: the Act 1 boss is a teal-black living carrier about one screen long (design/enemies/bosses/
brood-carrier): it roars as it comes in over the ship and as it turns broadside; its eight lime bay
sacs open in windows (wet membranes parting, then sucking shut) and spit out Skitters and Needlers;
a destroyed sac bursts with ichor; its core is under a plate iris of chitin that opens over a second
in the last phase. Organic and wet, Vrell family (the round-08 screeches and spawns), never
mechanical gunfire. The lifeboat tow (Level 07's secret) needs a cable snapping as the pod falls
free: a taut tow line, metallic, a reward cue. Recorded only (CC0 / CC-BY, Freesound), one a/b pair
per sound, found with the Freesound API's text search filtered to CC0 and CC-BY. All files come from
`tools/concept/audio/sfx_r25.py`, deterministic, cut from the **Freesound originals** cached by
`tools/concept/audio/freesound_fetch.py --download` (clipped at full scale first where an original
decodes beyond it, as `tools/art/sfx_originals.py` does), so a chosen file is already the production
file. Levels on the loudest 100 ms of the 200 Hz–5 kHz band (sfx_r24.py's `level`): roar -19 dB
(the Vrell screeches and the Leviathan's cry -24 to -27, the klaxon -23), sac opening -24 dB
(the Vrell spawns -23 / -27), closing -28 dB, launch -24 dB, sac burst -15 dB (explosion-r02-a
-15.2), iris -20 dB, cable snap -17 dB (the large salvage -17.7). A sharp transient meets its
ceiling first: sac opening a reaches -25.6, the sac bursts -16.3 and -17.3, the cable snaps -20.0.
Not listened to. Closed 2026-10-05 (user): roar b, sac opening b, closing b, launch b, sac burst a,
iris b, cable snap a; the other variants are in `concept/rejected/`. The game's files in
`assets/sfx/` are written by `tools/art/sfx_originals.py` with sfx_r25.py's `PRODUCTION` (the same
cut, with a `SOURCE` comment).

### enemy-carrier-roar-r25-a — "Deep Roar Echo 2.wav" by noahpardo
Source: <https://freesound.org/people/noahpardo/sounds/345735/> — CC0 1.0. Use: the Brood Carrier's roar as it arrives (with the klaxon) and, at 0.85 pitch, as it turns broadside.
Why: A huge creature's roar with a long echo: the swell, the roar and its tail make the carrier sound a screen long. Darker and more animal than b.
Edit: leading silence trimmed, cut to 4.00 s, 30 Hz high-pass, 1.40 s fade-out, levelled to -19 dB, ceiling -3.0 dBFS.

### enemy-carrier-roar-r25-b — "Didgeridoo Monster Roar" by Noxdl
Source: <https://freesound.org/people/Noxdl/sounds/204912/> — CC0 1.0. Use: the Brood Carrier's roar as it arrives (with the klaxon) and, at 0.85 pitch, as it turns broadside.
Why: A bear's roar mixed with a didgeridoo (made for a giant kaiju): a buzzing drone body that reads as a living ship, more whale than beast.
Edit: leading silence trimmed, cut to 3.00 s, 30 Hz high-pass, 0.80 s fade-out, levelled to -19 dB, ceiling -3.0 dBFS.

### enemy-carrier-sac-open-r25-a — "GOREFlsh_Flesh Manipulation 01_KVV AUDIO_FREE" by KVV_Audio
Source: <https://freesound.org/people/KVV_Audio/sounds/796506/> — CC-BY 4.0. Use: a bay sac's membrane parting as its window opens (one sound for a pair).
Why: Flesh pulled apart close to the mic: a wet, layered parting with small tears, the first of the recording's three gestures.
Edit: leading silence trimmed, cut to 0.85 s, 80 Hz high-pass, 0.25 s fade-out, levelled to -24 dB, ceiling -4.0 dBFS.

### enemy-carrier-sac-open-r25-b — "Squelch.mp3" by LucasDuff
Source: <https://freesound.org/people/LucasDuff/sounds/516643/> — CC0 1.0. Use: a bay sac's membrane parting as its window opens (one sound for a pair).
Why: Something ripped out of a sucking mud: a longer, smoother wet tear; its spectrum has a few narrow squeaky tones.
Edit: leading silence trimmed, cut from 0.25 s, cut to 0.90 s, 80 Hz high-pass, 0.05 s fade-in, 0.30 s fade-out, levelled to -24 dB, ceiling -4.0 dBFS.

### enemy-carrier-sac-close-r25-a — "GOREFlsh_Flesh Manipulation 01_KVV AUDIO_FREE" by KVV_Audio
Source: <https://freesound.org/people/KVV_Audio/sounds/796506/> — CC-BY 4.0. Use: a bay sac sucking shut as its window closes.
Why: The same recording's third gesture, shorter and 4 dB under the opening: the membrane folding back.
Edit: leading silence trimmed, cut from 4.40 s, cut to 0.60 s, 80 Hz high-pass, 0.03 s fade-in, 0.25 s fade-out, levelled to -28 dB, ceiling -8.0 dBFS.

### enemy-carrier-sac-close-r25-b — "Squelch.mp3" by LucasDuff
Source: <https://freesound.org/people/LucasDuff/sounds/516643/> — CC0 1.0. Use: a bay sac sucking shut as its window closes.
Why: Opening b's tear played backwards: a wet suck that ends shut.
Edit: leading silence trimmed, cut from 0.45 s, cut to 0.60 s, 80 Hz high-pass, 0.20 s fade-in, 0.05 s fade-out, reversed, levelled to -28 dB, ceiling -8.0 dBFS.

### enemy-carrier-launch-r25-a — "Spit 1 - The Ridge - Spanker" by bananplyte
Source: <https://freesound.org/people/bananplyte/sounds/452169/> — CC0 1.0. Use: a Skitter or Needler spat out of an open sac (up to three after an opening).
Why: A throaty creature spit made with a voice and a glass of water for a student game's boss: a short, mid-heavy 'ptoo' with a tonal throat.
Edit: leading silence trimmed, cut to 0.50 s, 80 Hz high-pass, 0.25 s fade-out, levelled to -24 dB, ceiling -6.0 dBFS.

### enemy-carrier-launch-r25-b — "Slime Attack 1" by qubodup
Source: <https://freesound.org/people/qubodup/sounds/751338/> — CC0 1.0. Use: a Skitter or Needler spat out of an open sac (up to three after an opening).
Why: A slime monster's lunge (a layered slime and meat slap): brighter and shorter, more splat than spit.
Edit: leading silence trimmed, cut to 0.40 s, 80 Hz high-pass, 0.20 s fade-out, levelled to -24 dB, ceiling -6.0 dBFS.

### enemy-carrier-sac-burst-r25-a — "Headshot 2" by SilverIllusionist
Source: <https://freesound.org/people/SilverIllusionist/sounds/470586/> — CC-BY 4.0. Use: a bay sac destroyed, and each sac in the tail-to-head death chain.
Why: "An exceptionally wet and fleshy explosion": a big burst with a splattering tail, played 10 % slower for a boss-size sac.
Edit: leading silence trimmed, cut to 1.40 s, 50 Hz high-pass, 0.60 s fade-out, played 10 % slower, levelled to -15 dB, ceiling -1.5 dBFS.

### enemy-carrier-sac-burst-r25-b — "Gutsy Spillage 1" by magnuswaker
Source: <https://freesound.org/people/magnuswaker/sounds/522159/> — CC0 1.0. Use: a bay sac destroyed, and each sac in the tail-to-head death chain.
Why: Ripped cardboard and poured water: a visceral tear with a wet tail, a little lower in the body than a, also 10 % slower.
Edit: leading silence trimmed, cut to 1.10 s, 50 Hz high-pass, 0.50 s fade-out, played 10 % slower, levelled to -15 dB, ceiling -1.5 dBFS.

### enemy-carrier-iris-r25-a — "SFX Door Open.wav" by Paul368
Source: <https://freesound.org/people/Paul368/sounds/264061/> — CC0 1.0. Use: the plate iris opening over the core (1 s) as phase 3 starts.
Why: An alien hatch morphing open with a pressure release up front: the plates sliding apart, mechanical-organic. Much of its energy is below 200 Hz (high-passed at 80 Hz).
Edit: leading silence trimmed, cut to 1.60 s, 80 Hz high-pass, 0.60 s fade-out, levelled to -20 dB, ceiling -3.0 dBFS.

### enemy-carrier-iris-r25-b — "Simple Mutate (Monster)" by Division4884
Source: <https://freesound.org/people/Division4884/sounds/342336/> — CC0 1.0. Use: the plate iris opening over the core (1 s) as phase 3 starts.
Why: A monster's body morphing: a grinding, churning organic texture, steadier than a, without a hatch's clunk.
Edit: leading silence trimmed, cut from 0.10 s, cut to 1.30 s, 40 Hz high-pass, 0.05 s fade-in, 0.40 s fade-out, levelled to -20 dB, ceiling -3.0 dBFS.

### secret-cable-snap-r25-a — "snapping-chain" by CosmicEmbers
Source: <https://freesound.org/people/CosmicEmbers/sounds/161650/> — CC-BY 3.0. Use: the lifeboat's amber tow cable snapping on its third hit, the cargo pod falling free.
Why: A chain snapping: a sharp crack and the loose chain rattling after it; very bright (most of its energy above 5 kHz), so it meets its ceiling 3 dB under the level.
Edit: leading silence trimmed, cut to 1.20 s, 60 Hz high-pass, 0.50 s fade-out, levelled to -17 dB, ceiling -1.5 dBFS.

### secret-cable-snap-r25-b — "Guitar string snaps.wav" by juskiddink
Source: <https://freesound.org/people/juskiddink/sounds/58491/> — CC-BY 4.0. Use: the lifeboat's amber tow cable snapping on its third hit, the cargo pod falling free.
Why: A guitar's E string snapping, 25 % slower: a heavy steel cable's twang with a ringing tail; the guitar body's boom is high-passed out.
Edit: leading silence trimmed, cut from 0.48 s, cut to 1.20 s, 200 Hz high-pass, 0.02 s fade-in, 0.70 s fade-out, played 25 % slower, levelled to -17 dB, ceiling -1.5 dBFS.

## Round 27 — the save-done sound and the Coilwyrm's chain-cut tear

Brief (user decision of 2026-10-06 on round 26's open questions): a **save-done** sound, played when
the hangar's autosave or a save to a slot is written: a short, positive interface confirmation,
clearly not the menu confirm (two rising square notes) nor the shop's purchase (two notes and a coin
sparkle), synthesized in the round-08 UI family (`sfx_r08.py`'s notes, bells and sparkles; peak
-8 dBFS like the other UI blips). And the **Coilwyrm's chain-cut tear**, played when a shot cuts the
chain through (the simulation's `CHAIN_CUT`, over the cut segment's own burst): a wet, organic
tear or rip that reads as the body being severed, distinct from the segment bursts (round 24's
fleshy burst); recorded only (CC0 / CC-BY, Freesound), found with the Freesound API's text search
("flesh tear", "gore rip wet", "limb tear") filtered to CC0 and CC-BY. One a/b pair each, all from
`tools/concept/audio/sfx_r27.py`, deterministic; the tears cut from the **Freesound originals**
cached by `tools/concept/audio/freesound_fetch.py --download`, so a chosen tear is already the
production file. The tears are levelled on the loudest 100 ms of the 200 Hz–5 kHz band to -20 dB
(ceiling -3 dBFS), between the segment burst (-17.3) and the regrowth (-27.4); the Brood Pod burst
they replace measures -23.3. The save sounds reach -21.1 (a) and -20.1 dB (b) on that measure, the
menu confirm -17.5, the purchase -22.6, the upgrade -23.3. Not listened to: checked by envelopes,
band balance and levels only.

Outcome (user, 2026-10-06): save done **b**, chain-cut tear **a** (CC0); save a and tear b
(CC-BY) moved to `concept/rejected/` (the headings below keep the files' names). The save sound is
copied into `assets/sfx/` by `copyPlaceholderSounds`; the tear's file there is written by
`tools/art/sfx_originals.py` with sfx_r27.py's `PRODUCTION` (the same cut, with a `SOURCE`
comment).

### ui-save-r27-a — synthesized, "write and seal"
Use: save done (the hangar's autosave as it opens, a save to a slot).
Why: Four quick rising pulse ticks (E6 G♯6 B6 E7, 32 ms apart) read as data being written, then a bright triangle fifth (E6 + B6) held 0.2 s over a bell an octave up seals it; a short echo. Brighter and longer than the menu confirm, no coin sparkle.
Edit: 0.70 s, peak -8 dBFS, loudest 100 ms of the band -21.1 dB; 97 % of the energy in 200 Hz–5 kHz.

### ui-save-r27-b — synthesized, "calm chime"
Use: save done (the hangar's autosave as it opens, a save to a slot).
Why: A soft latch click, then two warm bells a fourth apart (A5, then D6 80 ms later) over a triangle pad (D5 + A5) that swells in over 60 ms and fades: calmer and rounder than a, a "your progress is safe" chime under the debrief's total.
Edit: 0.92 s, peak -8 dBFS, loudest 100 ms of the band -20.1 dB.

### enemy-coilwyrm-cut-r27-a — "rip_tear FLESH!!!!.wav" by aust_paul
Source: <https://freesound.org/people/aust_paul/sounds/30928/> — CC0 1.0. Use: a Coilwyrm's chain cut through (`CHAIN_CUT`), over the cut segment's burst.
Why: "Some sort of monster or animal tearing at flesh", organic Foley (4.1 stars from 110 ratings): a run of short wet rips over half a second. The original is bright (most of its energy at 1.6–6.4 kHz) and thin below 800 Hz, so it plays 15 % slower for a bigger body.
Edit: leading silence trimmed, cut to 0.90 s, 80 Hz high-pass, 11 kHz low-pass, 0.35 s fade-out, played 15 % slower (1.06 s), levelled to -20 dB, ceiling -3.0 dBFS (peak -4.5 dBFS).

### enemy-coilwyrm-cut-r27-b — "Tearing Flesh" by dereklieu
Source: <https://freesound.org/people/dereklieu/sounds/241822/> — CC-BY 3.0. Use: a Coilwyrm's chain cut through (`CHAIN_CUT`), over the cut segment's burst.
Why: "A juicy and gross" limb being torn off, made from stale bread torn apart with the breaks layered: one big rip with a heavy low body, shorter and punchier than a. Half the original's energy is below 80 Hz, so it is high-passed at 90 Hz; 28 % stays below 200 Hz.
Edit: leading silence trimmed, cut to 0.70 s, 90 Hz high-pass, 0.30 s fade-out (0.72 s), levelled to -20 dB, ceiling -3.0 dBFS (peak -5.6 dBFS).

## Round 28 — the proximity mine's arming beep

Brief (M5 part A): the `mine` family's **arming beep**, played as one of the Proximity Mines' mines
arms 0.4 s after its drop (the simulation's `PROXIMITY_MINE_ARMED`): a short, quiet electronic
"armed" chirp that does not clutter when several mines arm (a mount drops up to two a second and
keeps up to six alive). Synthesized in the round-08 UI family (`sfx_r08.py`'s notes), dry (no echo
or reverb tail to pile up), high and narrow-band so it sits above the shots' and explosions' body,
peak -12 dBFS (the tick level of the tally tick and the typewriter, below the UI blips' -8); the game
plays it at -10 dB in the flight mix (the hits at -8, the shots at -14) and two at a time at most.
One a/b pair from `tools/concept/audio/sfx_r28.py`, deterministic. Not listened to: checked by
envelopes, band balance and levels only. Round 28 closed (user, 2026-10-06): **a**, the armed chirp,
chosen; b moved to `concept/rejected/` (the script's `CHOSEN` writes it there). The game plays a as a
concept copy (`copyPlaceholderSounds` puts it in `assets/sfx/`, `Sfx.MINE_ARM`).

### weapon-mine-arm-r28-a — synthesized, "armed chirp" (chosen)
Use: a proximity mine arming (`PROXIMITY_MINE_ARMED`).
Why: Two soft rising pulse blips a fifth apart (A6, then E7 45 ms later), 28 ms each, a 12.5 % pulse low-passed at 5.5 kHz over a sine for body: an electronic, friendly "ready" that is over before the next mine drops.
Edit: 0.10 s, peak -12 dBFS, loudest 100 ms of the band -24.6 dB; 93 % of the energy in 200 Hz–5 kHz, nothing below 200 Hz.

### weapon-mine-arm-r28-b — synthesized, "sensor ping" (rejected)
Use: a proximity mine arming (`PROXIMITY_MINE_ARMED`).
Why: One sine ping that glides up a fifth (1.6 to 2.4 kHz) in 15 ms and rings out (35 ms decay) on a faint metallic overtone at 2.76× (the round-08 bells' ratio), a tiny band-passed latch click under its start: a single sensor "lock" rather than a two-note signal, a little longer but softer at its tail.
Edit: 0.15 s, peak -12 dBFS, loudest 100 ms of the band -23.9 dB; 99 % of the energy in 200 Hz–5 kHz.

## Round 31 — the arcology's collapse and the Ravager's pounce

Brief (M5 part C, user decision D9 = a): Level 09's two new sounds. The **arcology collapse**, played
on the simulation's collapse event as Node C1 and C2 both die: a low rumble that swells under the
tower's 1.5 s shadow (the warning), then a long crash from 1.5 s as the tower falls across the
ground layer over 3 s, and a debris tail (about 5.5 s); the level's set piece, so as big as the
biggest explosions but long, and distinct from `explosion-r02-d` ("building collapse", a 2.8 s
blast with debris) and `hit-crumble-r08-a` (a ground target crumbling). The **Ravager pounce**,
played as a Ravager takes off on its 0.75 s leap at the ship: a short snarl and lunge, an animal
attack rather than a screech (the Vrell screech is the flyers' entry cue; the pounce is the
Ravager's only cue and the one ground attack that hurts); packs of 3–5 can pounce close together,
so it is short and dry. Recorded (CC0 / CC-BY, Freesound), found with the Freesound API's text
search ("building collapse", "collapse rumble", "earthquake rumble", "demolition collapse"; "dog
snarl attack", "creature attack snarl", "growl short creature") filtered to CC0 and CC-BY, cut from
the **Freesound originals** cached by `tools/concept/audio/freesound_fetch.py --download`; each
pounce has a synthesized whoosh under its snarl (band-passed noise sweeping up and back down, panned
across). One a/b pair each, all from `tools/concept/audio/sfx_r31.py`, deterministic. The collapse is
levelled on its integrated loudness, -15 LUFS, ceiling -1.5 dBFS (`explosion-huge-r04-a` -15.1 LUFS,
`explosion-r02-d` -14.4): its sub-heavy crash makes the round-24 band measure unfair between a and b
(at the same band level b would play 5 LU louder). A pounce is levelled on the loudest 100 ms of the
200 Hz–5 kHz band to -22 dB, ceiling -6 dBFS (the Vrell screeches -24 / -27, the carrier launch
-25). Not listened to: checked by envelopes, band balance and levels only.

Outcome (user, 2026-10-07): collapse **a** (CC-BY, YleArkisto on the in-game credits roll), and
**both** pounces kept, the game picking one at random per pounce; collapse b moved to
`concept/rejected/` (the headings below keep the files' names). The three files in `assets/sfx/` are
written by `tools/art/sfx_originals.py` with sfx_r31.py's `PRODUCTION` (the same cuts, with a
`SOURCE` comment).

### collapse-r31-a — "Cracking Earthquake" by uagadugu + "Big crash, a house tumbling down" by YleArkisto
Source: <https://freesound.org/people/uagadugu/sounds/222521/> — CC0 1.0 (the rumble);
<https://freesound.org/people/YleArkisto/sounds/342891/> — CC-BY 4.0 (the crash). Use: the arcology
collapse (Level 09's collapse event).
Why: The rumble is an earthquake cracking open soil and stone (4.8 stars from 398 ratings): a deep
rumble with stony cracks in it, the frame starting to give. It swells from -24 dB to full over the
1.5 s warning, then fades out under the crash. The crash is a big crash of a house tumbling down from
the Finnish broadcaster Yle's effects archive (1980s tape, a mix): a sustained, tumbling crash of
heavy masonry, read as the tower toppling across; its first 4 s, then a fade.
Edit: rumble cut to 3.00 s from 0.50 s, 25 Hz high-pass, mixed 6 dB under the crash; crash cut to
4.00 s, 25 Hz high-pass, 1.50 s fade-out, entering at 1.5 s. 5.50 s, -15.3 LUFS, peak -1.5 dBFS,
loudest 100 ms of the band -15.2 dB; 64 % of the energy below 200 Hz. The warning rises from about
-47 to -17 dBFS (100 ms RMS), the crash peaks at -11 and holds -17 to -20 for 2.5 s.

### collapse-r31-b — "Radiator Metal Rumbling" by RutgerMuller + "Explosion or Collapse" by tec_studio
Source: <https://freesound.org/people/RutgerMuller/sounds/51097/> — CC0 1.0 (the rumble);
<https://freesound.org/people/tec_studio/sounds/703070/> — CC0 1.0 (the crash). Use: the arcology
collapse (Level 09's collapse event).
Why: The rumble is a radiator shaken for earthquake sounds (4.5 stars from 39 ratings), played 20 %
slower: a steel frame rattling and groaning, the megastructure's skeleton rather than stone. It
swells over the warning like a. The crash is "maybe a tunnel explosion or collapse": a deep, rolling
roar that holds level for 3 s and decays into a long dust tail, read as the tower pancaking into a
wave of dust; deeper and steadier than a's tumbling crash.
Edit: rumble cut to 3.00 s from 0.30 s, 40 Hz high-pass, 20 % slower, mixed 4 dB under the crash;
crash cut to 4.20 s, 45 Hz high-pass, 1.80 s fade-out, entering at 1.5 s. 5.70 s, -15.0 LUFS, peak
-1.9 dBFS, loudest 100 ms of the band -18.5 dB; 71 % of the energy below 200 Hz. The warning rises
from about -57 to -33 dBFS, the crash holds -13 to -17 for 3 s.

### ravager-pounce-r31-a — "Goblin Snarl" by qubodup
Source: <https://freesound.org/people/qubodup/sounds/442815/> — CC0 1.0 (a remix of strongbot's CC0
dog snarl). Use: a Ravager taking off on its pounce.
Why: A monster's attacking snarl made for a game (4.7 stars from 36 ratings): a rasping, dog-like
snarl that reads as a hound going for the throat; its first lunge, 10 % slower for a bigger body,
over a quick swish (the whoosh's centre 500 → 2 600 → 700 Hz, 9 dB under the snarl).
Edit: leading silence trimmed, cut to 0.65 s, 90 Hz high-pass, 0.30 s fade-out, 10 % slower; 0.72 s,
levelled to -22 dB, ceiling -6 dBFS (peak -10.4 dBFS, -24.1 LUFS); 88 % of the energy in
200 Hz–5 kHz; loudest at once, then falling off over 0.5 s.

### ravager-pounce-r31-b — "Dragon: Snarl, Roar + Attack" by Breviceps
Source: <https://freesound.org/people/Breviceps/sounds/466830/> — CC0 1.0. Use: a Ravager taking off
on its pounce.
Why: A huge monster snarling and roaring (4.5 stars from 38 ratings); its second gesture, the attack,
played 20 % faster so it is a hound-sized beast rather than a dragon: a snapping roar with a short
swell into it, over a lower, heavier whoosh (250 → 1 500 → 350 Hz, 7 dB under the snarl).
Edit: leading silence trimmed, cut to 0.85 s from 1.55 s, 90 Hz high-pass, 0.35 s fade-out, 20 %
faster; 0.71 s, levelled to -22 dB, ceiling -6 dBFS (peak -11.7 dBFS, -20.9 LUFS); 93 % of the energy
in 200 Hz–5 kHz; a 0.1 s swell, loudest at 0.3 s.

## Round 32 — the Wraith's decloak, the Mote Swarm's whoosh and the lance

Brief (M5 part D, user decision D10 = a): Level 10's three new sounds, played from the simulation's
events. The **Wraith decloak**, as its 0.4 s violet flash starts (behind the ship, low on the
screen): a short violet shimmer-crack, the Wraith's only cue (no screech); a rear ambush decloaks
2–4 of them close together, so it is short and sits low. The **Mote Swarm whoosh**, a one-shot
played as a swarm enters and again as it loops back from the bottom edge (not a loop: the swarm's
20 motes make their own noise only when they die, on the `tiny` rung): a rushing flutter of many
small wings, "a flock, not a formation". The **lance**, the scripted loss at t=118: a thin
descending whine into a sharp impact as a thorn lance drops from the cloud deck onto Lifeline
Three; the shuttle's loss itself reuses the ally loss sound and the music ducks −6 dB for 3 s under
it. The impact sits at 1.2 s (`LEAD` in the script), so the game starts the sound 1.2 s before the
lance hits, inside the glow (t 116–118). Recorded (CC0 / CC-BY, Freesound), found with the Freesound
API's text search ("cloak", "shimmer", "ice crack", "glass crack", "magic reveal", "teleport";
"starling murmuration", "starlings", "pigeons flock", "flock flying", "wings flapping", "insect
swarm"; "falling bomb whistle", "incoming whistle", "dive whine", "thunder crack close", "impact
pierce", "metal crunch impact") filtered to CC0 and CC-BY, cut from the **Freesound originals**
cached by `tools/concept/audio/freesound_fetch.py --download`, mixed with synthesized layers. One
a/b pair each, all from `tools/concept/audio/sfx_r32.py`, deterministic. Levels as the loudest
100 ms of the 200 Hz–5 kHz band (round 24's measure): the decloak −22 dB, ceiling −6 dBFS (as the
Ravager's pounce); the swarm −24 dB, ceiling −6 dBFS (as the carrier launch); the lance −17 dB,
ceiling −1.5 dBFS (as the secret's cable snap: the level's one story beat). Rejected sources: Daleonfire's
"Falling Bomb" (the player's Bomb Rack already falls on it, `shot-bomb-r03-a`); Artninja's metal
impact (its licence asks to credit a second person). Not listened to: checked by envelopes, band
balance and levels only.

Outcome (user, 2026-10-08): decloak **b** (its cloak layer CC-BY 4.0, F.M.Audio on the in-game
credits roll), swarm **b** and lance **a**; decloak a, swarm a and lance b moved to
`concept/rejected/` (the headings below keep the files' names). The three files in `assets/sfx/` are
written by `tools/art/sfx_originals.py` with sfx_r32.py's `PRODUCTION` (the same cuts, with a
`SOURCE` comment); the game still starts the lance at t 116.8, its impact 1.2 s in.

### wraith-decloak-r32-a — "ICEBrk_Break04" by InMotionAudio + a synthesized shimmer
Source: <https://freesound.org/people/InMotionAudio/sounds/719973/> — CC0 1.0. Use: a Wraith
decloaking (its decloak event, the flash's start).
Why: An ice sheet broken (4.9 stars from 10 ratings): two sharp, glassy cracks 0.2 s apart, read as
the cloak's refraction shattering. Under it a synthesized violet shimmer: seven high partials
(1.8–5.2 kHz, jittered) each fluttering at 22–38 Hz like light through a moving membrane, gliding
down 15 %, swelling over 0.18 s into the crack and ringing out (0.14 s decay), spread across.
Edit: crack cut to 0.50 s, 150 Hz high-pass, 0.20 s fade-out, entering at 0.18 s; the shimmer 6 dB
under it. 0.68 s, levelled to −22 dB, ceiling −6 dBFS (peak −6.0 dBFS, band −22.8 dB, −20.2 LUFS);
78 % of the energy in 200 Hz–5 kHz, none below 200 Hz. The shimmer rises from −53 to −30 dBFS
(50 ms RMS), the cracks at −21 and −25, gone by 0.6 s.

### wraith-decloak-r32-b — "Spacey Teleport Rip" by GameAudio + "Swirling Velvet Cloak 2" by F.M.Audio
Source: <https://freesound.org/people/GameAudio/sounds/220164/> — CC0 1.0 (the rip);
<https://freesound.org/people/F.M.Audio/sounds/556759/> — CC-BY 4.0 (the cloak). Use: a Wraith
decloaking.
Why: A quick sci-fi rip with a rough metallic texture (5 stars from 4 ratings), the cloak tearing
open; under it a velvet cloak swirled (fabric on fabric), its swish played 30 % faster: the manta's
membrane snapping open. Fully recorded, more sci-fi and less glassy than a.
Edit: rip cut to 0.60 s, 120 Hz high-pass, 0.25 s fade-out; cloak cut to 0.60 s from 0.35 s, 200 Hz
high-pass, 0.25 s fade-out, 30 % faster, 5 dB under the rip. 0.60 s, levelled to −22 dB, ceiling
−6 dBFS (peak −8.5 dBFS, band −22.1 dB, −19.7 LUFS); 66 % of the energy in 200 Hz–5 kHz; loudest at
0.15–0.2 s, gone by 0.45 s.

### mote-swarm-r32-a — "Pigeon flock fly away, wing flaps, Toronto" by TRP + a synthesized whoosh
Source: <https://freesound.org/people/TRP/sounds/616623/> — CC0 1.0. Use: a Mote Swarm entering, and
again on its loop-back.
Why: A flock of pigeons taking off in the city (4.7 stars from 59 ratings): a swelling rush of many
wing flaps, played 40 % faster so the wings are small and the flutter higher; birdlike ("they steer
like starlings"). Under it a whoosh sweeping 600 → 2 400 → 900 Hz, panned left to right.
Edit: cut to 2.40 s from 0.80 s, 180 Hz high-pass, 0.15 s fade-in, 0.90 s fade-out, 40 % faster;
the whoosh 9 dB under it. 1.71 s, levelled to −24 dB, ceiling −6 dBFS (peak −6.1 dBFS, band
−25.0 dB, −19.9 LUFS); 73 % of the energy in 200 Hz–5 kHz. Rises over 0.4 s, holds −21 to −26 dBFS
for 0.8 s, fades by 1.5 s.

### mote-swarm-r32-b — "Insect Superfast Wing Flap" by kalhan + a synthesized whoosh
Source: <https://freesound.org/people/kalhan/sounds/482733/> — CC0 1.0. Use: a Mote Swarm entering,
and again on its loop-back.
Why: A cicada flapping its wings, recorded very close (4.9 stars from 18 ratings): a fast, dry,
buzzing flutter. Five copies from different points of the recording, played 0.85–1.3× and panned
across, make an insect swarm's chitter that swells and fades; under it a lower whoosh (400 → 1 800
→ 600 Hz) panned right to left. Alien and insect-like where a is birdlike.
Edit: 250 Hz high-pass, 9 kHz low-pass; copies from 2, 5.5, 9, 12.5 and 16 s, 1.7 s each, swelling
over 0.35 s and fading over 1.1 s; the whoosh 8 dB under it. 1.70 s, levelled to −24 dB, ceiling
−6 dBFS (peak −8.1 dBFS, band −24.4 dB, −19.4 LUFS); 50 % of the energy in 200 Hz–5 kHz, the rest
above (a bright buzz). Loudest at 0.3–0.5 s, fading by 1.3 s.

### lance-r32-a — a synthesized whine + "Closeup Thunder Strike 01" by loganzsound
Source: <https://freesound.org/people/loganzsound/sounds/840628/> — CC0 1.0. Use: the lance striking
Lifeline Three (Level 10's scripted loss).
Why: A synthesized thin whine, a sine falling 3.4 kHz → 700 Hz over 1.2 s with a dissonant partial
at 1.41× and an 11 Hz vibrato and a breath of band noise, swelling from −20 dB (something alien
falling closer, energy rather than ordnance), into a close thunder strike (5 stars from 40
ratings): a sharp crack and a long roll, the lance as a bolt from the cloud deck.
Edit: whine 1.20 s, 7 dB under the strike; strike cut to 2.60 s from 0.47 s, 40 Hz high-pass,
1.40 s fade-out, entering at 1.2 s. 3.80 s, levelled to −17 dB, ceiling −1.5 dBFS (peak −3.3 dBFS,
band −17.0 dB, −19.7 LUFS); 85 % of the energy in 200 Hz–5 kHz, 15 % below. The whine rises from
−44 to −23 dBFS, the crack −14 at 1.25 s, the roll −15 to −35 over 1.5 s.

### lance-r32-b — "Whistling Firework" by magnuswaker + "Piercing impact / Stabbing" by Breviceps
Source: <https://freesound.org/people/magnuswaker/sounds/555998/> — CC0 1.0 (the whistle);
<https://freesound.org/people/Breviceps/sounds/464839/> — CC0 1.0 (the impact). Use: the lance
striking Lifeline Three.
Why: A firework's whistler (4.8 stars from 9 ratings), a steady thin whistle falling 2.6 → 1.9 kHz,
played 25 % faster (about 3.3 → 2.4 kHz, a projectile coming down) and swelling from −14 dB, into an
anime-style piercing stab (4.8 stars from 88 ratings; an arrow or dart hitting) over a synthesized
sub thump (95 → 38 Hz): a thorn spike punching through a hull. Shorter and drier than a.
Edit: whistle cut to 1.50 s from 0.30 s, 400 Hz high-pass, 0.60 s fade-in, 25 % faster (1.20 s), 7 dB
under the stab; stab cut to 0.65 s from 1.62 s (its third take), 90 Hz high-pass, 0.30 s fade-out,
entering at 1.2 s; thump 0.6 s, 4 dB under the stab. 1.84 s, levelled to −17 dB, ceiling −1.5 dBFS
(peak −3.0 dBFS, band −17.2 dB, −15.5 LUFS); 57 % of the energy in 200 Hz–5 kHz, 34 % below. The
whistle rises from −47 to −19 dBFS, the impact −14 at 1.4 s, gone by 1.75 s.

## Round 33 — Level 11's Kraken, convoy ships, flak and jelly

Brief (M5 part E, step E3d; one proposal each, no a/b): the sounds of Level 11 "Atlantic Convoy"
that the chosen torpedo launch (`shot-torpedo-r03-a`), the water explosion (`explosion-water-r03-a`)
and the ocean ambience (`ambience-ocean-r08-a`, from t = 0) do not cover. All from
`tools/concept/audio/sfx_r33.py`, deterministic; recorded layers are cut from the **Freesound
originals** (cached by `tools/concept/audio/freesound_fetch.py --download`) and every one is CC0, so
nothing needs the credits roll. Found with the Freesound API's text search ("big splash", "water
slam", "tentacle", "wave crash", "ship sinking", "hull creak", "bubbles", "distant explosion",
"underwater rumble", "rising water", "metal impact", "sea monster roar") filtered to CC0 and CC-BY,
sorted by rating, judged by name, rating and the envelope of the HQ preview. Rejected sources: the
CC-BY "Underwater Rumble" (EminYILDIRIM) and "UnderWater_ExplosionFar" (Akkaittou), replaced by
synthesized swells and thumps to keep every layer CC0. Levels as the loudest 100 ms of the
200 Hz–5 kHz band against the existing set (the script's header). Not listened to by Claude: checked
by envelopes, band balance and levels only. The offset is the event's moment inside the file.

Outcome (user, 2026-10-09): all eight accepted: the slam, surfacing, ship hit and jelly pulse as first
made; the churn, the death and the sinking reworked without bubbles (the churn and the sinking now
with byjoshberry's CC-BY waves, already on the credits roll) and the flak raised twice (to −16 dB)
after listening. The seven with a recorded layer are written into `assets/sfx/` by
`tools/art/sfx_originals.py` with sfx_r33.py's `PRODUCTION` (the same cuts, with a `SOURCE`
comment); the synthesized pulse is copied as it is.

### kraken-slam-r33-a — "Big robot footstep 002" by AudioPapkin + "Big Splash" by Bird_man + synthesis
Source: <https://freesound.org/people/AudioPapkin/sounds/813053/> and
<https://freesound.org/people/Bird_man/sounds/316744/> — CC0 1.0. Use: a slam arm hitting its lane.
Why: A heavy robot step (5 stars, 11 ratings) is a thud with a metallic body, the arm's mass on the water,
slowed 20 %; a big splash (4.8 stars from 37 ratings) lands with it; under them a synthesized sub
thump (85 → 32 Hz) and, before, a rising rush (bandpassed noise 300 → 2200 Hz) for the arm rising
out of the water; after, a synthesized spray tail falling 3.2 → 0.7 kHz over 1.5 s.
Edit: **impact at 0.5 s** (`SLAM_IMPACT`, the arm's rise as a first value; start the file when the arm
starts to rise). 2.20 s, levelled to −15 dB, ceiling −1.5 dBFS (peak −1.6 dBFS, band −15.4 dB,
−14.2 LUFS); 56 % of the energy below 200 Hz. Loudest 0.5–1.1 s, gone by 1.9 s.

### kraken-churn-r33-a — "Ocean waves hitting bow of moving boat" by byjoshberry + a synthesized rumble
Source: <https://freesound.org/people/byjoshberry/sounds/435668/> — CC-BY 4.0 (already on the credits
roll). Use: the lane telegraph's churn under the red dashes. Reworked after the user's listening
("what's with the soap bubbles?"): no bubbles, no water jet.
Why: Waves breaking against a bow are a rushing, surging lane of water; one take swelled from −14 dB
to full over the telegraph, a second, slower wash (15 % slower, low-passed at 2.5 kHz, 3 dB under)
adds weight, and a low synthesized rumble fills it.
Edit: 1.00 s, ends at its peak so the slam's rush takes over (**hard: the telegraph is 0.8 s, so
skip the first 0.2 s**); levelled to −24 dB, ceiling −6 dBFS (peak −9.9 dBFS, band −24.1 dB,
−28.8 LUFS); 19 % below 200 Hz; the envelope climbs from −40 to −22 dBFS over the second.

### kraken-surface-r33-a — "Coming up from underwater" by adviseme333 + synthesized swell
Source: <https://freesound.org/people/adviseme333/sounds/679410/> — CC0 1.0. Use: the head surfacing
(2.0 s, with each `SURFACE`).
Why: A body rising out of the water (4.8 stars from 18 ratings): runs and pours, entering at 0.85 s
as the crown nears the surface (the layer flips at the midpoint, 1.0 s). Under it a synthesized deep
swell (noise low-passed 90 → 420 Hz under a 48 Hz sub rising a third) peaking at 1.1 s, and a whoosh
for the foam.
Edit: 2.75 s (the tail runs 0.75 s past the 2.0 s surfacing), levelled to −21 dB, ceiling −4 dBFS
(peak −6.7 dBFS, band −21.0 dB, −23.6 LUFS); 74 % below 200 Hz. Rises from −45 to −17 dBFS at 1.1 s,
then falls to −37 at the end (cut by the 20 ms fade).

### ship-hit-r33-a — "Metal Shovel Impact" by Sadiquecat + "slightmetallicthud" by profoundsounds + "Splash_002" by jamesabels
Source: <https://freesound.org/people/Sadiquecat/sounds/718749/>,
<https://freesound.org/people/profoundsounds/sounds/686282/>,
<https://freesound.org/people/jamesabels/sounds/166966/> — CC0 1.0. Use: a cargo ship taking a slam
(the `ALLY_HIT` cue, at the impact).
Why: Steel struck and ringing (5 stars, 21 ratings), slowed 15 % to hull size, a dull metallic thud
(5 stars, 13 ratings) slowed 25 % for its body, a splash 60 ms later as the water rushes in, and a
short synthesized sub thump (78 → 40 Hz).
Edit: impact at 0 s; 0.94 s, levelled to −18 dB, ceiling −3 dBFS (peak −3.1 dBFS, band −18.3 dB,
−20.7 LUFS); 39 % below 200 Hz. Starts at −16 dBFS (an impact), gone by 0.7 s.

### ship-sink-r33-a — "…ship hull list2" by kyles + "Ocean waves hitting bow of moving boat" by byjoshberry + synthesis
Source: <https://freesound.org/people/kyles/sounds/455757/> (CC0 1.0) and
<https://freesound.org/people/byjoshberry/sounds/435668/> (CC-BY 4.0, already on the credits roll).
Use: a ship sinking (its sinking steps, from the first). Reworked after the user's listening (water
from wave sounds, no bubbles).
Why: Old wood and metal groaning as a hull lists (4.7 stars), its loudest 3 s slowed 10 %; a
synthesized muffled boom (70 → 28 Hz) 0.15 s in as the hull gives; the water from waves: a breaking
swell (2.4 s, 10 % slower, from 0.7 s) and the wash rolling over the deck (1.6 s, 20 % slower, from
1.8 s, 5 dB under the groan); a low synthesized rumble under all.
Edit: 3.80 s, levelled to −20 dB, ceiling −3 dBFS (peak −4.9 dBFS, band −20.0 dB, −23.2 LUFS); 35 %
below 200 Hz. The creaks are irregular (−18 to −40 dBFS).

### frigate-flak-r33-a — "Distant Shot 4" and "Distant Shot 2" by HenKonen
Source: <https://freesound.org/people/HenKonen/sounds/682122/> and
<https://freesound.org/people/HenKonen/sounds/682120/> — CC0 1.0. Use: the frigate's flak burst (an
`ALLY_FLAK` event every 2 s while it is on screen), an ambience layer.
Why: Gunshots from far away: only a dull thump and a long roll-off reach the listener; low-passed at
4 kHz, the first slowed 8 %, the second 10 % faster, 3 dB under it, panned apart; two pops 0.55 s
apart, a battery firing, not a cue.
Edit: 1.64 s, levelled to −16 dB, ceiling −2 dBFS (peak −2.1 dBFS, band −18.1 dB, −22.2 LUFS; the user found
the first two versions, −33 and −24 dB, too quiet) — well above the ocean ambience (−28 dB band), as loud
as a ship hit; 6 % below 200 Hz.

### driftjelly-pulse-r33-a — synthesized
Source: none (synthesized). Use: a Driftjelly's proximity ring as it is released (72–96 px), if
the user wants one (the default reuses the enemy shot's small sound).
Why: A soft underwater bloop: a sine falling 520 → 170 Hz with a quiet octave partial, a second, lower
echo bloop 0.24 s later and a ripple of band noise (1.8 → 0.6 kHz): a pressure pulse, not a shot, so
it does not read as a threat of its own; up to six rings can sound at once, hence the low level.
Edit: 0.90 s, levelled to −24 dB, ceiling −8 dBFS (peak −18.3 dBFS, band −24.0 dB, −29.7 LUFS);
97 % in 200 Hz–5 kHz. A pure tone is quieter than its band level suggests (−30 LUFS against the spawn
sound's −24), hence −24 dB against the spawn's −27.

### kraken-death-r33-a — "Sea Creature Roar" by Bikkit99 + "CO2 Water Jet" by unfa + a synthesized rumble
Source: <https://freesound.org/people/Bikkit99/sounds/837799/>,
<https://freesound.org/people/unfa/sounds/532492/> — CC0 1.0. Use: the Harbour Kraken's
death, started with the `huge` water burst (which plays on top of it).
Why: A sea creature's roar (4.7 stars from 60 ratings), its loudest stretch slowed 20 % into a huge
animal's long groan; churning water (the jet, 6 dB under) around the sinking head (no bubbles, after the
user's listening); a low synthesized rumble holding it together.
Edit: 5.20 s, levelled to −17 dB, ceiling −3 dBFS (peak −3.4 dBFS, band −17.1 dB, −16.1 LUFS); 48 %
below 200 Hz. A plateau at −16 dBFS from 0.5 to 3 s, then falling to −36 at 4.8 s.

## Round 34 — Level 12's storm and the Lamprey

Brief (M5 part F, step F2; user decisions F6 = a and F8 = a): the storm's thunder as one-shots
synced with the lightning (2–3 variants per proposal; the game plays variant *flash index mod 3*
0.3–1.5 s after each flash), the gust that rises with each gust's 1.0 s telegraph (wind from the
left), the Lamprey's latch and its drain loop a/b; the shake-off and the sonde chime one proposal each;
and a rain-only bed cut from the chosen storm bed's own source so the thunder can follow the flashes
(the chosen `ambience-storm-r08-a` kept). All from `tools/concept/audio/sfx_r34.py`, deterministic,
from the **Freesound originals** already cached for earlier rounds (every source is in CREDITS.md
already; the CC-BY ones are on the credits roll) plus synthesized layers. No bubble or popping
recordings (the user's round-33 note). Levels as the loudest 100 ms of the 200 Hz–5 kHz band against
the existing set (the script's header); not listened to by Claude: checked by envelopes, band balance,
levels and, for the loops, the seam.

### thunder-{1,2,3}-r34-a — "Rain and Thunder 4" by FlatHill + a synthesized sub rumble
Source: <https://freesound.org/people/FlatHill/sounds/237729/> — CC0 1.0 (the chosen storm bed's
source). Use: thunder after a lightning flash, look a "the storm's own thunder".
Why: The recording's one big clap and roll (2.3–8.8 s) and two later distant rolls (12.0 and 17.6 s)
are the thunder the player already hears in the chosen bed, so the synced one-shots match the rain bed
cut from the same take. Low-passed at 1.8 kHz (the rain's hiss stays in the bed), a soft synthesized
sub rumble 9 dB under for weight. No crack: a heavy, distant roll.
Edit: 1 6.5 s at −14 dB (−14.4 LUFS); 2 3.6 s at −16 dB, panned right (−20.4 LUFS); 3 3.6 s at −19 dB
(its roll is nearly all below 200 Hz: −16.3 LUFS); ceiling −1.5 dBFS.

### thunder-{1,2,3}-r34-b — "Closeup Thunder Strike 01" by loganzsound + a synthesized sub rumble
Source: <https://freesound.org/people/loganzsound/sounds/840628/> — CC0 1.0 (the lance's crack in
round 32). Use: thunder after a lightning flash, look b "close strikes".
Why: A close strike's sharp crack and long roll reads as lightning right over the convoy lane; three
speeds (1.0, 0.86, 0.74) move it from near to far, with a pan each and a sub rumble 8 dB under.
Edit: 1 7.0 s at −14 dB; 2 6.0 s at −16 dB; 3 5.5 s at −17 dB; ceiling −1.5 dBFS. The crack sets the
level, so the roll after it is quieter than look a's (−22 to −23 LUFS against −14 to −20).

### gust-r34-a — synthesized
Source: none. Use: a wind gust (`GUST` telegraph; it starts with the 1.0 s telegraph).
Why: Two bands of noise whose centres wander with a slow random flutter, a hollow howl (a narrow band
near 600 Hz gliding up with the gust), rising over the telegraph, holding 2.0 s, falling; panned from
the left (upwind) to the right.
Edit: 3.6 s, −19 dB, ceiling −4 dBFS (peak −4.2, −15.6 LUFS); no energy below 200 Hz.

### gust-r34-b — "Wind__Artic__Cold.wav" by cobratronik + a synthesized rumble
Source: <https://freesound.org/people/cobratronik/sounds/117136/> — CC0 1.0 (the arctic ambience's
source). Use: as gust a.
Why: A real wind's buffeting from its loudest stretch (14.6 s), in gust a's envelope and pan, a low
rumble 10 dB under.
Edit: 3.6 s, −19 dB, ceiling −4 dBFS (peak −5.1, −19.0 LUFS).

### lamprey-latch-r34-a — "Slime Attack 1" by qubodup + "rip_tear FLESH!!!!.wav" by aust_paul + synthesis
Source: <https://freesound.org/people/qubodup/sounds/751338/> and
<https://freesound.org/people/aust_paul/sounds/30928/> — CC0 1.0. Use: `LAMPREY_LATCHED`.
Why: "Suck and bite": a synthesized inhaling rush (band noise 3.2 → 0.7 kHz, swelling and cut off by
the bite, no pop), the wet slime attack slowed 15 % as the bite at 0.16 s, the flesh tear 7 dB under
as teeth grinding in, a small thump.
Edit: 0.67 s, −16.7 dB, ceiling −2 dBFS (−15.0 LUFS). The bite is 0.16 s into the file: start it that
much before the latch for a tight sync, or at the latch (the rush then trails the touch).

### lamprey-latch-r34-b — "Wet Splat" by JustInvoke + "slightmetallicthud.mp3" by profoundsounds + "rip_tear FLESH!!!!.wav" by aust_paul + synthesis
Source: <https://freesound.org/people/JustInvoke/sounds/446115/> (CC-BY 4.0, on the credits roll),
<https://freesound.org/people/profoundsounds/sounds/686282/> and
<https://freesound.org/people/aust_paul/sounds/30928/> (CC0 1.0). Use: `LAMPREY_LATCHED`.
Why: "Clamp on the hull": the bite lands at once — a wet splat on a metallic thud (the hull), the tear
6 dB under as teeth gnawing in, a thump.
Edit: 0.68 s, −17.5 dB, ceiling −2 dBFS (−13.9 LUFS); the impact at 0 s.

### lamprey-drain-r34-a — synthesized
Source: none. Use: the drain loop while any Lamprey drains (stops at the break or the shake-off).
Why: A 55 Hz electric hum (saw partials) through a low-pass that opens with each 0.4 s gulp (latch
look a's swallow at 10 fps), sparse electric crackle and a faint 1.1 kHz whine. Every frequency has a
whole number of cycles in the loop; built over three periods, the middle kept.
Edit: seamless 2.4 s loop (`LOOPSTART`/`LOOPLENGTH` tags), −22 dB, ceiling −6 dBFS (−16.9 LUFS);
76 % of the energy below 200 Hz; the seam's jump 0.7 × the median sample step.

### lamprey-drain-r34-b — "SonicDeathRay_1.2KHzWithCrackle.wav" by zimbot + a synthesized sub
Source: <https://freesound.org/people/zimbot/sounds/177099/> — CC-BY 4.0 (on the credits roll, the
Mantis sweep's source). Use: as drain a.
Why: The ray's crackling tone slowed to 0.45 (≈ 540 Hz) and band-limited (150 Hz–3.2 kHz) is a
siphoning whine; a 50 Hz sub swelling twice a loop under it.
Edit: seamless 2.4 s loop (a 0.3 s cross-fade), −22 dB, ceiling −6 dBFS (−18.0 LUFS); the seam's jump
1.8 × the median step.

### lamprey-shake-r34-a — "Wet Splat" by JustInvoke + "inhuman screech.wav" by Wolfsinger + a synthesized whoosh
Source: <https://freesound.org/people/JustInvoke/sounds/446115/> and
<https://freesound.org/people/Wolfsinger/sounds/25713/> — CC-BY 4.0 (both on the credits roll). Use:
`LAMPREY_SHAKEN`.
Why: A wet slap (10 % faster) as it is torn off, the screech sped up 35 % and cut to 0.6 s as its
squeal, panned away to the left, and a whoosh falling 2.6 → 0.6 kHz as it is thrown.
Edit: 0.82 s, −13.5 dB, ceiling −2 dBFS (−12.1 LUFS; −17 dB and −16.1 LUFS until 2026-10-12, raised
3.5 dB because it was marginal in Level 12's busy mix: step G8, pending the user's review).

### sonde-chime-r34-a — synthesized
Source: none. Use: `SONDE_COLLECTED`.
Why: A weather sonde's telemetry: three soft square blips rising (2.4, 2.8, 3.2 kHz, 28 ms, 40 ms
apart), then two bright bell tones (E6 at 0.13 s, B6 at 0.22 s; inharmonic partials 1, 2.76, 5.4,
8.93 decaying fast to slow).
Edit: 0.95 s, −16 dB, ceiling −4 dBFS (−13.4 LUFS), like the pickups (−15 to −17).

### ambience-storm-rain-r34-a — "Rain and Thunder 4" by FlatHill
Source: <https://freesound.org/people/FlatHill/sounds/237729/> — CC0 1.0 (the chosen bed's source).
Use: the `earth-storm` bed of Level 12 (sections 1–4 and 6), the thunder now played as the one-shots
above.
Why: The take's last 11.9 s (from 21.0 s) are rain after its thunder has rolled away; a 90 Hz
high-pass removes the faint far rumble left in it.
Edit: seamless 10.0 s loop from 21.0 s with a 1.5 s equal-power cross-fade (import_sfx.py's
`make_loop`), normalised as round 08's ambiences (200 Hz–5 kHz band RMS −36 dB, peak ceiling −10 dBFS:
the ceiling holds it at −38.8 dB); −32.1 LUFS; the seam's jump 0.2 × the median step. The same rain
is at −51 to −55 dB inside the chosen bed (whose normalisation its thunder set): this bed is the rain
~14 dB louder, at the other ambiences' level (the ocean's).

## Round 35 — Level 13's relay and the supply drone

Brief (M5 part G, step G2; decision G9 = a, pending the user's review): a/b for the relay hit (every
relay-aimed hit, so short), the integrity alarm (once at 50 %, once at 25 %, under Nansen's or Okafor's
line) and grid online (t=220, the lights coming on in sequence, after the mid-level jingle); one
proposal each for the supply drone's launch and the relay destroyed (a power-down; the mission fails on
it). All from `tools/concept/audio/sfx_r35.py`, deterministic, from **Freesound originals** already
cached and credited for earlier rounds (all CC0) plus synthesized layers. No bubble or popping
recordings. Levels as the loudest 100 ms of the 200 Hz–5 kHz band against the existing set (ship hit
−18.3, edge warning −16, low-armour beep −20.3, overdrive −20); not listened to by Claude: checked by
envelopes, band balance and levels.

### relay-hit-r35-a — "Impact on metal" by JoMungus + "ELECTRIC_ZAP_001" by JoelAudio + a synthesized thump
Source: <https://freesound.org/people/JoMungus/sounds/726486/> and
<https://freesound.org/people/JoelAudio/sounds/136542/> — CC0 1.0. Use: a relay-aimed hit on the relay.
Why: Struck plating: the impact cut to 0.55 s, a spark from the zap 8 dB under (high-passed at 900 Hz),
a sub thump.
Edit: 0.57 s, −19.1 dB, ceiling −3 dBFS (peak −4.4, −15.7 LUFS).

### relay-hit-r35-b — "slightmetallicthud.mp3" by profoundsounds + synthesized glitch and thump
Source: <https://freesound.org/people/profoundsounds/sounds/686282/> — CC0 1.0. Use: as a.
Why: A dull thud with the relay's link glitching: a 0.9–2.1 kHz data chirp stepping every 30 ms,
stuttered, bit-crushed to 5 bits and decaying.
Edit: 0.45 s, −19.0 dB, ceiling −3 dBFS (peak −4.3, −16.9 LUFS).

### integrity-alarm-r35-a — synthesized
Source: none. Use: `ally-below` 0.5 and 0.25.
Why: A station alert: a soft square alternating 988 / 740 Hz, 0.22 s a tone, three times, through a
small speaker's band (450 Hz–3.5 kHz).
Edit: 1.5 s, −16.9 dB, ceiling −3 dBFS (peak −12.2, −14.6 LUFS).

### integrity-alarm-r35-b — synthesized
Source: none. Use: as a.
Why: Three rising whoops (a buzzy saw with an octave under it, 420 → 1050 Hz), 0.45 s apart.
Edit: 1.5 s, −17.0 dB, ceiling −3 dBFS (peak −13.5, −15.5 LUFS).

### grid-online-r35-a — synthesized
Source: none. Use: grid online (t=220), with the lights sequence.
Why: A breaker chain: five heavy breakers closing 0.2 s apart (a clunk and a click each, rising, panned
left to right), a 50 Hz mains hum swelling up under them, an A-major chord (A4 C♯5 E5 A5) blooming at
1.05 s.
Edit: 2.6 s, −17.3 dB, ceiling −2 dBFS (peak −2.2, −18.7 LUFS).

### grid-online-r35-b — "Machine Charge.wav" by Glitchedtones + synthesized clunk, whine and chord
Source: <https://freesound.org/people/Glitchedtones/sounds/375925/> — CC0 1.0. Use: as a.
Why: A spin-up: the recorded charge (from 0.25 s, 1.25 s) with a thin whine rising 300 → 1400 Hz under
it, cut by a big contact clunk at 1.2 s, the chord 5 dB under.
Edit: 2.6 s, −17.1 dB, ceiling −2 dBFS (peak −4.6, −17.8 LUFS).

### drone-launch-r35-a — synthesized
Source: none. Use: a supply drone lifting off the relay's helipad (t=160, t=200).
Why: Four ducted rotors (soft pulses at the blade rate, slightly detuned so they beat) spinning up from
40 to ~190 Hz in 0.5 s, a breath of downwash; then climbing away duller and quieter, panned centre →
right → left over its arc.
Edit: 2.2 s, −20.5 dB, ceiling −4 dBFS (peak −4.4, −19.4 LUFS).

### relay-destroyed-r35-a — "Big Boom" by unfa + "Sounds For Earthquakes - Radiator Metal Rumbling.wav" by RutgerMuller + synthesized power-down and thump
Source: <https://freesound.org/people/unfa/sounds/189779/> and
<https://freesound.org/people/RutgerMuller/sounds/51097/> — CC0 1.0. Use: the relay destroyed
(`mission-failed`), before Okafor's "Nansen is gone".
Why: A power-down (a 60 Hz hum and a 1.3 kHz whine slowing to nothing over 2.4 s) under the blast at
0.12 s, the radiator recording's loud metal crash at 0.55 s, a heavy thump.
Edit: 3.7 s, −15.2 dB, ceiling −1.5 dBFS (peak −1.6, −15.0 LUFS; 75 % of the energy below 200 Hz).

## Round 36 — Level 14's root field, the Siege Spire's tear-free and the canopy

Brief (M5 part H, step H2; H8 = a, pending the user's review): a/b for the root burst (the 1 s dust telegraph and the root breaking out), the tear-free roar (the Spire tearing out of the ground at phase 3 and lifting over ≈ 3 s, the shockwave) and the canopy withering (after the kill, at the start of the 12 s daylight section, no music); one proposal each for the root growth (a Spine Turret and a Polyp Mortar growing out of the root over 1.5 s) and the canopy hum (a loop that replaces the megacity ambience's sirens and cuts out when the Spire dies). All from `tools/concept/audio/sfx_r36.py`, deterministic, from **Freesound originals** already cached and credited for earlier rounds plus synthesized layers. No bubble or popping recordings. Levels as the loudest 100 ms of the 200 Hz–5 kHz band against the existing set (edge warning −16.2, Kraken slam −15.4, large explosion −11.2, ship sinking −20.0); the hum by band RMS as the ambience loops (−36); not listened to by Claude: checked by envelopes, spectrograms, band balance and levels.

### root-burst-r36-a — "Cracking Earthquake" by uagadugu + "cracking.wav" by smidoid + "building_collapse02_close.wav" by onteca + "Gravel Falling" by iwanPlays + synthesized cracks and thump
Source: <https://freesound.org/people/uagadugu/sounds/222521/>, <https://freesound.org/people/iwanPlays/sounds/567249/> — CC0 1.0; <https://freesound.org/people/smidoid/sounds/49139/> — CC-BY 4.0; <https://freesound.org/people/onteca/sounds/197772/> — CC-BY 3.0. Use: a root burst (t≈64, 74, 84, 92), from the telegraph's start.
Why: Stone gives way: a ground rumble swelling 14 dB over the 1 s telegraph, 26 synthesized cracks coming faster and a slowed crack at 0.62 s; at 1.0 s the root breaks out: a masonry crunch, a sub thump, stones raining down.
Edit: 2.07 s, −17.1 dB, ceiling −2 dBFS (peak −2.0, −17.2 LUFS).

### root-burst-r36-b — "Squelch.mp3" by LucasDuff + "…ship hull list2" by kyles + "Tearing Flesh" by dereklieu + "rip_tear FLESH!!!!.wav" by aust_paul + synthesized slosh, creak and thump
Source: <https://freesound.org/people/LucasDuff/sounds/516643/>, <https://freesound.org/people/kyles/sounds/455757/>, <https://freesound.org/people/aust_paul/sounds/30928/> — CC0 1.0; <https://freesound.org/people/dereklieu/sounds/241822/> — CC-BY 3.0. Use: as a.
Why: The creep surges: a slow slurry swell (a slowly drifting band, nothing bubbly) with the squelch slowed to 0.8, a stick-slip creak rising 45 → 140 Hz and the recorded creak slowed; at 1.0 s the root tears up wet (Tearing Flesh at 0.75, the rip at 0.85) over a softer thump; little stone.
Edit: 1.97 s, −17.2 dB, ceiling −2 dBFS (peak −2.3, −15.5 LUFS).

### root-growth-r36-a — "…ship hull list2" by kyles + synthesized swell, creak and knock
Source: <https://freesound.org/people/kyles/sounds/455757/> — CC0 1.0. Use: the growths growing out of a burst's root (1.5 s).
Why: A stretching creak (the recording slowed to 0.82, 5 kHz low-pass, plus a synthesized creak rising 40 → 110 Hz) over a low tone gliding 55 → 95 Hz and a breath of noise opening; a dull knock and a dry click at 1.45 s, the growth set; no bubbles, no pops.
Edit: 1.75 s, −20.2 dB, ceiling −4 dBFS (peak −4.2, −18.9 LUFS).

### tear-free-roar-r36-a — "Cracking Earthquake" by uagadugu + "building_collapse02_close.wav" by onteca + "cracking.wav" by smidoid + "ICEBrk_Break04" by InMotionAudio + "Big Boom" by unfa + "Deep Roar Echo 2.wav" by noahpardo + "Gravel Falling" by iwanPlays + synthesized thump and lift
Source: <https://freesound.org/people/uagadugu/sounds/222521/>, <https://freesound.org/people/InMotionAudio/sounds/719973/>, <https://freesound.org/people/unfa/sounds/189779/>, <https://freesound.org/people/noahpardo/sounds/345735/>, <https://freesound.org/people/iwanPlays/sounds/567249/> — CC0 1.0; <https://freesound.org/people/smidoid/sounds/49139/> — CC-BY 4.0; <https://freesound.org/people/onteca/sounds/197772/> — CC-BY 3.0. Use: the Siege Spire's phase-3 tear-free, from the lift's start.
Why: A tectonic rip: the earth and stone tearing (slowed to 0.85 / 0.8), roots snapping (the crack at 0.6, the ice break at 0.55), the shockwave at 0.35 s (Big Boom at 0.85 and a sub thump), a deep roar slowed to 0.78 under it, a rumble rising 60 → 220 Hz with the lift, debris from 1.5 s.
Edit: 3.41 s, −15.0 dB (dense, held by the ceiling), ceiling −1.5 dBFS (peak −1.6, −11.5 LUFS; 61 % of the energy below 200 Hz).

### tear-free-roar-r36-b — "Sea Creature Roar" by Bikkit99 + "Dragon: Snarl, Roar + Attack" by Breviceps + "Tearing Flesh" by dereklieu + "rip_tear FLESH!!!!.wav" by aust_paul + synthesized shockwave and thump
Source: <https://freesound.org/people/Bikkit99/sounds/837799/>, <https://freesound.org/people/Breviceps/sounds/466830/>, <https://freesound.org/people/aust_paul/sounds/30928/> — CC0 1.0; <https://freesound.org/people/dereklieu/sounds/241822/> — CC-BY 3.0. Use: as a.
Why: The creature bellows: the roar's speed gliding 0.7 → 1.0 so it rises with the lift, the dragon's roar at 0.6 under it, the roots tearing wet (Tearing Flesh at 0.62 and 0.5, the rip at 0.7), an air shockwave at 0.3 s (a noise blast closing from 2.5 kHz to 120 Hz and a sub thump); no stone.
Edit: 3.36 s, −14.3 dB, ceiling −1.5 dBFS (peak −1.6, −12.2 LUFS).

### canopy-withering-r36-a — "rip_tear FLESH!!!!.wav" by aust_paul + "Tearing Flesh" by dereklieu + "Wind__Artic__Cold.wav" by cobratronik + synthesized crackle
Source: <https://freesound.org/people/aust_paul/sounds/30928/>, <https://freesound.org/people/cobratronik/sounds/117136/> — CC0 1.0; <https://freesound.org/people/dereklieu/sounds/241822/> — CC-BY 3.0. Use: the canopy withering after the Spire's death, at the start of the daylight section.
Why: A dry shrivel: a synthesized crackle of leaves and paper drying (short trains of 1–4 ms bursts across the stereo field, densest at 1–3 s, gone by ≈ 5 s), the membrane tearing slowly far above (the rip at half speed, Tearing Flesh at 0.45, low-passed), settling into a soft low-passed wind.
Edit: 7.00 s, −19.5 dB, ceiling −2 dBFS (peak −2.1, −16.6 LUFS).

### canopy-withering-r36-b — "Simple Mutate (Monster)" by Division4884 + "Wind__Artic__Cold.wav" by cobratronik + synthesized sigh and groan
Source: <https://freesound.org/people/Division4884/sounds/342336/>, <https://freesound.org/people/cobratronik/sounds/117136/> — CC0 1.0. Use: as a.
Why: A dying exhale: a deep groan descending (the monster's speed gliding 0.62 → 0.4, 2.5 kHz low-pass), a vast sigh (breath noise through formants falling 650 → 330 and 1150 → 700 Hz), a sub sinking 62 → 31 Hz, fading into the wind.
Edit: 7.50 s, −19.1 dB, ceiling −2 dBFS (peak −3.0, −18.1 LUFS).

### canopy-hum-r36-a — synthesized
Source: none. Use: Level 14's ambience layer in place of the sirens, cut when the Spire dies; a loop.
Why: A low organic hum: two harmonic voices on 46.00 / 46.25 Hz (beating slowly; harmonics peaking around 200 Hz so small speakers carry it), a throb six times a loop (0.75 Hz), a 4 s breath swelling the upper harmonics, an organic noise band (90–500 Hz); every frequency a multiple of 1/8 Hz and the noise circular, so it loops with no crossfade.
Edit: seamless 8.00 s loop, band RMS −39.0 dB (3 dB under the ambience loops' −36; loudest 100 ms −33.7), peak −20.3 dBFS, −31.0 LUFS; the seam's step +2.4 dB against its neighbours after encoding (no click).

## Round 36 (Act 3) — the Burrower's eruption, the canyon wall scrape, the Ion Beam loop and the dust-storm bed

Brief (M7 "Act 3 – Red Dust" design phase, step A-audio; pending the user's review in round 36): a/b for the Burrower's eruption (Level 15 on: the 1.0 s bulge tell with its low rumble, then the break-out; its ring follows 0.4 s later), the canyon wall scrape (Level 16: the ship touching rock, 10 damage and a 28 px push over 0.12 s, then a 0.5 s cooldown), the Ion Beam loop (Act 3's front beam, heard for as long as fire is held, between the round-04 start and stop sounds) and the dust-storm bed (Level 18's `mars-storm` ambience: a low roar with grit hiss). All from `tools/concept/audio/sfx_r36b.py` (round 36's Level 14 sounds use `sfx_r36.py`; different subjects), deterministic, from **Freesound originals** (new in this round: Elements-Library, nicoproson, lolamadeus, a deleted user's metal pipe, Halleck, craigsmith, dobroide; reused: uagadugu, noahpardo, peepholecircus; all CC0 but Halleck's and dobroide's CC-BY 4.0) plus synthesized layers. No bubble, popping or squelch layers. Levels as the loudest 100 ms of the 200 Hz–5 kHz band against the existing set (root burst −17, edge warning −16.2, ship sinking −20, armour hit −20.5, ship hit −18); the loops by band RMS (the round-04 beam loops −30, the ambience loops −36), one gain and no limiter so the seams stay intact. Not listened to by Claude: checked by envelopes, spectrograms, band balance, levels and the loop seams.

### burrow-eruption-r36-a — "Cracking Earthquake" by uagadugu + "Hard Impact on Sand" by Elements-Library + "SAND POUR.wav" by nicoproson + synthesized pressure, grains and thump

**Chosen** (round 36, 2026-10-13).
Source: <https://freesound.org/people/uagadugu/sounds/222521/>, <https://freesound.org/people/Elements-Library/sounds/683785/>, <https://freesound.org/people/nicoproson/sounds/627070/> — CC0 1.0. Use: a Burrower surfacing, from the bulge tell's start (the break-out at 1.0 s).
Why: A sand burst: the ground rumble swelling 14 dB over the 1.0 s tell (low-passed at 2.5 kHz) under a pressure tone rising 38 → 64 Hz and sand hissing out of the cracks (dense 2–6 ms grains, 2–7 kHz, thickening into a grainy hiss, never single clicks); at 1.0 s a heavy impact on sand, a sub thump and the sand raining back down.
Edit: 2.11 s, −19.6 dB (its dense lows held by the ceiling), ceiling −2 dBFS (peak −2.0, −16.3 LUFS; 67 % of the energy below 200 Hz).

### burrow-eruption-r36-b — "Deep Roar Echo 2.wav" by noahpardo + "Gravel Impacts and Falls.wav" by lolamadeus + synthesized churn, spray and thump

**Rejected** (round 36, 2026-10-13: a picked); the files are in `rejected/`.
Source: <https://freesound.org/people/noahpardo/sounds/345735/>, <https://freesound.org/people/lolamadeus/sounds/179341/> — CC0 1.0. Use: as a.
Why: The swimmer breaks out: a low churn of sand under the ground (noise through a band wandering 70–320 Hz, slowly; nothing bubbly) swelling over the tell, a muffled growl just before the break (the roar at 0.7 speed, 1.5 kHz low-pass); at 1.0 s gravel and rock thrown up and falling over a wide spray (a noise burst whose high-pass rises 300 → 2500 Hz as it thins) and a sub thump.
Edit: 2.10 s, −20.3 dB (held by the ceiling), ceiling −2 dBFS (peak −2.2, −14.9 LUFS).

### canyon-scrape-r36-a — "Metal pipe along concrete 2.wav" by a deleted Freesound user + "Hard Impact on Sand" by Elements-Library + "Gravel Impacts and Falls.wav" by lolamadeus + synthesized thump

**Rejected** (round 36, 2026-10-13: b picked); the files are in `rejected/`.
Source: <https://freesound.org/people/deleted_user_2104797/sounds/164673/>, <https://freesound.org/people/Elements-Library/sounds/683785/>, <https://freesound.org/people/lolamadeus/sounds/179341/> — CC0 1.0. Use: the ship touching a canyon wall or rock island (with the rock spark and hit flash).
Why: A rock grind: the hull dragged along stone (the pipe on concrete, 0.62 s), a dull hit under it (the sand impact high-passed at 90 Hz), grit falling (gravel, 250 Hz high-pass) and a short thump; dull and heavy, no ring.
Edit: 0.63 s, −18.0 dB, ceiling −3 dBFS (peak −3.9, −18.6 LUFS).

### canyon-scrape-r36-b — "big metal side impact 3 scraping.wav" by Halleck + synthesized sparks and thump

**Chosen** (round 36, 2026-10-13).
Source: <https://freesound.org/people/Halleck/sounds/121623/> — CC-BY 4.0. Use: as a.
Why: A metal screech: a car-chase foley side impact scraping along metal (0.6 s), a spark spray off the hull (fine dense noise, 3–9 kHz, a short comb for a metallic sheen, dying in 0.25 s; no clicks) and a short thump; brighter and more alarming than a.
Edit: 0.60 s, −18.1 dB, ceiling −3 dBFS (peak −5.8, −15.8 LUFS).

### ion-beam-loop-r36-a — "laser beam" by peepholecircus + synthesized body and hiss

**Chosen** (round 36, 2026-10-13).
Source: <https://freesound.org/people/peepholecircus/sounds/169991/> — CC0 1.0. Use: the Ion Beam's loop (between the round-04 start and stop sounds).
Why: The chosen round-04 hum (`shot-beam-r04-a`: the same source and loop window, 2.60 s from 5.60 s, 0.12 s cross-fade), made heavier for a main weapon: an ion body under it (two harmonic voices on 110.00 / 110.38 Hz beating once a loop, harmonics peaking at 220–440 Hz, 5 dB under the hum) and a faint hiss (2–6 kHz, 16 dB under); every synthesized frequency a whole number of cycles in the loop.
Edit: seamless 2.60 s loop, band RMS −30.0 dB (the round-04 loops' level), peak −19.6 dBFS, −26.7 LUFS; seam step +1.8 dB after encoding (no click).

### ion-beam-loop-r36-b — synthesized

**Rejected** (round 36, 2026-10-13: a picked); the files are in `rejected/`.
Source: none. Use: as a.
Why: An ion stream: a buzzing core (two saw-like voices on 165.0 / 165.5 Hz, harmonics 1–14 with a formant at 1.2 kHz, a slow beat once a loop and a 10 % swell twice a loop), a shimmer band round 1.8 kHz fluttering 16 times a second (a smooth sine flutter, not pulses; the channels a quarter cycle apart) and a hiss (3–8 kHz); circular noise and whole-cycle frequencies, so it loops with no cross-fade.
Edit: seamless 2.00 s loop, band RMS −30.2 dB, peak −13.5 dBFS, −23.1 LUFS (39 % of the energy below 200 Hz: heavier than a); seam step +1.4 dB after encoding.

### dust-storm-bed-r36-a — "G56-22-Sandstorm.wav" by craigsmith + "20101121.wind.howl.furious.01.wav" by dobroide

**Chosen** (round 36, 2026-10-13).
Source: <https://freesound.org/people/craigsmith/sounds/438877/> — CC0 1.0; <https://freesound.org/people/dobroide/sounds/109371/> — CC-BY 4.0. Use: Level 18's storm bed (`mars-storm`), from t=25, swelling in the storm walls, dropping to a light wind in the eye (the game sets its gain).
Why: A recorded front: the sandstorm's fast, high, steady wind (80 Hz high-pass) over a furious low wind howl (900 Hz low-pass, 4 dB under), each cut to 16 s and looped with a 1.5 s equal-power cross-fade (filtered before the cut, so the seam stays continuous).
Edit: seamless 16.00 s loop, band RMS −36.1 dB (the ambience loops' level), peak −21.9 dBFS, −32.7 LUFS; seam step +4.8 dB after encoding (a cross-faded noise join, no click).

### dust-storm-bed-r36-b — synthesized

**Rejected** (round 36, 2026-10-13: a picked); the files are in `rejected/`.
Source: none. Use: as a.
Why: A synthesized front: a low roar (noise peaking at 120 Hz), a grit hiss (2.5–9 kHz, rising harder with the gusts) and a howl (noise through a band-pass wandering 450–900 Hz), all riding gusts (sinusoids of 1, 3 and 5 cycles a loop, the channels apart); circular noise, the howl's filter run over three loop lengths and the middle one kept, so the loop joins with no cross-fade.
Edit: seamless 16.00 s loop, band RMS −36.1 dB, peak −13.3 dBFS, −27.0 LUFS (40 % of the energy below 200 Hz: a heavier roar than a); seam step +3.3 dB after encoding.

## Round 37 (M7 part A) — the Act 3 equipment: beam start, stop and impact, Deflector Pod, EMP Burst, Orbital Lance, Shield Overcharge, ready chime, dash, light drones

Brief (M7 part A "Act 3 systems and arsenal", step AUD-A; pending the user's review in round 37): a/b for the equipment sounds of [*Act 3 sounds*](../README.md#act-3-sounds-approved-round-36) that part A builds: the Ion Beam's start and stop around its chosen loop (round 36, S.4 = a; the beam is at full length in 0.05 s and fades in 0.08 s, so the round-04 start's 0.9 s charge and the stop's 1.3 s power-down are too long for it), the beam's contact sizzle (a loop while it touches; plain and on rock), the Deflector Pod's tick (a bullet caught) and charge return, the EMP Burst's ring and a stunned machine's power-down, the deep rumble under the Orbital Lance's column (the column itself plays the chosen loop [c](shot-beam-r04-c.ogg), its targeting line the chosen lock beep), the Shield Overcharge's activation whoosh and end shimmer, the cooldown special's ready chime, the Evasive thrusters' dash and the light drones' break and rebuild. All from `tools/concept/audio/sfx_r37.py`, deterministic, from **Freesound originals** (new in this round: jokallset, Maxx222, JSK_Productions; reused: Glitchedtones, noirenex, JoelAudio, michael_grinnell, uagadugu, qubodup, wilhellboy, Cyberios, dinodilopho, JoMungus; all CC0 1.0) plus synthesized layers. No bubbly, popping or squelching layers (user rule): no short pitch-swept sine blips, no low clicks, buzzes built with random harmonic phases (a band-passed saw at 50 Hz turns into a click train), grains Hann-windowed and dense enough to fuse; every file scanned for isolated 5 ms peaks 12 dB over their surroundings (none). Levels as the loudest 100 ms of the 200 Hz–5 kHz band against the existing set; the loops by band RMS, one gain and no limiter so the seams stay intact. Not listened to by Claude: checked by envelopes, spectrograms, band balance, levels and the loop seams.

The chosen loop itself ([ion-beam-loop-r36-a](ion-beam-loop-r36-a.ogg)) stays as it is: `sfx_r37.py --check-loop` measures it as the game imports it: 114 660 samples (2.600 s), band RMS −30.0 dB, −26.7 LUFS, peak −19.6 dBFS, seam step +1.8 dB decoded. The start is a layer played **with** the loop's first pass (the loop starts at the press too), the stop at the release while the loop fades out in 0.08 s.

### ion-beam-start-r37-a — "Machine Charge.wav" by Glitchedtones + synthesized ignition and ion body

Proposed (round 37).
Source: <https://freesound.org/people/Glitchedtones/sounds/375925/> — CC0 1.0 (the round-04 start's source). Use: the Ion Beam's start, with the loop.
Why: A charge snap: the charge's rise (0.72 s from 0.12 s, 120 Hz high-pass) played 1.8× fast, an ignition hiss (2–7 kHz noise, 2 ms attack, no low end so no pop) and the loop's own ion body (harmonics 1–8 of its 110 Hz voice, peak at 330 Hz) rising 55 → 110 Hz in 0.12 s onto the loop's pitch, then fading under it.
Edit: 0.40 s, −24.1 dB, ceiling −6 dBFS (peak −9.2).

### ion-beam-start-r37-b — synthesized

Proposed (round 37).
Source: none. Use: as a.
Why: An ion strike: a bright arc crack (a dense rain of 1–3 ms grains, 3–8 kHz, fused into one rough burst), the ion body falling 220 → 110 Hz in 0.2 s onto the loop's pitch (a settling, not a blip: eight harmonics, smoothstep) and a 2–6 kHz hiss swelling and fading.
Edit: 0.45 s, −24.4 dB, ceiling −6 dBFS (peak −6.1, −20.1 LUFS).

### ion-beam-stop-r37-a — "Power Down" by noirenex + synthesized ion body

Proposed (round 37).
Source: <https://freesound.org/people/noirenex/sounds/159399/> — CC0 1.0 (the round-04 stop's source). Use: the Ion Beam's stop, at the release.
Why: The power-down, short: the source's first fall (0.5 s, 0.3 s fade-out) over the ion body sliding 110 → 45 Hz as it dies.
Edit: 0.55 s, −25.0 dB, ceiling −6 dBFS (peak −11.7, −25.1 LUFS).

### ion-beam-stop-r37-b — synthesized

Proposed (round 37).
Source: none. Use: as a.
Why: An ion collapse: the ion body sliding 110 → 35 Hz (exponential) as it fades over 0.5 s, its hiss band falling 4 → 1 kHz with it.
Edit: 0.62 s, −25.0 dB, ceiling −6 dBFS (peak −11.6, −23.4 LUFS; 41 % of the energy below 200 Hz).

### beam-impact-r37-a — synthesized

Proposed (round 37).
Source: none. Use: the beam's contact on a target (a layer under the beam loop while it touches; the game starts and stops it with the contact).
Why: An arc scorch: a dense dry sizzle (circular noise 2–7 kHz whose level flutters fast and randomly, a control signal low-passed at 60 Hz: a flutter, not pulses) over a lower burning body (300–1200 Hz, breathing slowly); circular noise, so it loops with no cross-fade.
Edit: seamless 1.50 s loop, band RMS −38.3 dB (most of its energy lies above 5 kHz: at the beam loops' −30 it would be 4 LU louder than the loop it sits under), −27.7 LUFS, peak −17.7 dBFS; seam step +5.4 dB after encoding (a noise join, no click).

### beam-impact-r37-b — "Plasma cutter gas.wav" by jokallset + synthesized sizzle

Proposed (round 37).
Source: <https://freesound.org/people/jokallset/sounds/395889/> — CC0 1.0 (a steady plasma-cutter gas roar, no spatter). Use: as a.
Why: A cutting torch: the cutter's gas roar (800 Hz–7 kHz, filtered before the cut; 1.5 s from 2.0 s with a 0.3 s cross-fade) with a's sizzle 6 dB under it; smoother and more "industrial" than a.
Edit: seamless 1.50 s loop, band RMS −38.4 dB, −28.8 LUFS, peak −21.6 dBFS; seam step −4.2 dB after encoding.

### beam-impact-rock-r37-a — synthesized

Proposed (round 37).
Source: none. Use: the beam stopped by a canyon wall or rock island (Level 16 on), in place of the plain impact.
Why: a with a grit crackle (2–5 ms Hann-windowed grains, 1.5–5 kHz, about 150 a second, placed round the loop so it joins) 5 dB under and a low stone roar (circular noise 150–600 Hz) 10 dB under.
Edit: seamless 1.50 s loop, band RMS −38.1 dB, −28.2 LUFS, peak −18.0 dBFS; seam step −4.2 dB after encoding.

### beam-impact-rock-r37-b — "Plasma cutter gas.wav" by jokallset + synthesized sizzle, grit and stone

Proposed (round 37).
Source: <https://freesound.org/people/jokallset/sounds/395889/> — CC0 1.0. Use: as the rock a, if b is picked for the plain impact.
Why: b with the same grit crackle and stone roar.
Edit: seamless 1.50 s loop, band RMS −38.2 dB, −29.1 LUFS, peak −18.0 dBFS; seam step +5.1 dB after encoding.

### deflector-tick-r37-a — "ELECTRIC_ZAP_001.wav" by JoelAudio + synthesized ping

Proposed (round 37).
Source: <https://freesound.org/people/JoelAudio/sounds/136542/> — CC0 1.0 (the shield hit's source). Use: the Deflector Pod catching a bullet (it may come several times a second).
Why: The shield hit pitched up and cut short, as the weapon's document asks: its first 0.2 s (300 Hz–9 kHz) played 1.6× fast, with a glassy tick on top (2.6 / 4.1 kHz partials, 1 ms attack, tau 25 ms).
Edit: 0.12 s, −25.5 dB (its energy is mostly above 5 kHz; held by the ceiling), ceiling −5 dBFS (peak −5.2).

### deflector-tick-r37-b — synthesized

Proposed (round 37).
Source: none. Use: as a.
Why: A dry deflect: a fused 2–9 kHz noise snap (30 ms) and three inharmonic metal partials (1.9 / 3.3 / 5.2 kHz) ringing 35 ms and drooping 3 %; no low end.
Edit: 0.12 s, −24.5 dB, ceiling −5 dBFS (peak −5.2).

### deflector-return-r37-a — synthesized

Proposed (round 37).
Source: none. Use: a Deflector Pod charge returning.
Why: A soft rise: two soft voices 0.2 % apart (a sine with a quiet 2nd and 3rd harmonic) easing up a fifth, 520 → 780 Hz, on a smoothstep over 0.3 s (slow at both ends, never a fast sweep), 60 ms attack, with an airy 3–8 kHz breath.
Edit: 0.45 s, −26.9 dB, ceiling −10 dBFS (peak −20.2, −26.6 LUFS).

### deflector-return-r37-b — synthesized

Proposed (round 37).
Source: none. Use: as a.
Why: A charge settling: a held E5–B5 fifth fading in over 0.18 s above a rising air whoosh (band 800 → 3000 Hz); no pitch glide at all.
Edit: 0.45 s, −27.0 dB, ceiling −10 dBFS (peak −17.7, −25.9 LUFS).

### emp-ring-r37-a — "Electric zap.wav" by michael_grinnell + synthesized thump, arcs and buzz

Proposed (round 37).
Source: <https://freesound.org/people/michael_grinnell/sounds/512471/> — CC0 1.0 (the `tesla` shot's source). Use: the EMP Burst's ring (0.4 s outward).
Why: The tesla shot layered and longer, as the brief says: four copies (80 Hz–6 kHz) at 1.0 / 0.8 / 0.65 / 0.5× speed entering at 0 / 0.08 / 0.2 / 0.35 s, centre, left, right, centre; an electric thump (70 → 30 Hz); crackling arcs (fused 1–4 ms grains, 1.5–8 kHz, 900 → 150 a second) running out to both sides as the ring grows; a fading 100 Hz buzz.
Edit: 1.35 s, −18.6 dB at the Smart Bomb's ceiling −1.5 dBFS (peak −1.6, −14.1 LUFS; the Smart Bomb −14.4 dB, −13.6 LUFS).

### emp-ring-r37-b — synthesized

Proposed (round 37).
Source: none. Use: as a.
Why: An arc ring: a mains-like buzz (saw-spectrum harmonics of 60 → 45 Hz with random phases, 400 Hz–3 kHz) dying over 0.9 s, a discharge crack (1–10 kHz grains) 5 dB under it, the arcs running out to both sides and a soft thump.
Edit: 1.34 s, −18.0 dB, ceiling −1.5 dBFS (peak −2.8, −19.2 LUFS: less dense than a).

### emp-powerdown-r37-a — synthesized

Proposed (round 37).
Source: none. Use: a machine stunned by the EMP, as its running lights go out (several can play at once).
Why: A spin-down whine: a relay clunk (300–1500 Hz noise and a 900 Hz body, tau 20 ms, no sub) and a whine running down 1400 → 180 Hz with its 2nd harmonic and a breath of band noise, fading as it falls.
Edit: 0.92 s, −24.1 dB, ceiling −8 dBFS (peak −11.0, −26.5 LUFS).

### emp-powerdown-r37-b — synthesized

Proposed (round 37).
Source: none. Use: as a.
Why: The buzz dies: a 100 Hz mains buzz (random-phase harmonics up to 3 kHz) flickering (a smooth 9 Hz wobble deepening from 10 % to 80 %) and sagging to 70 Hz, a faint whine falling 900 → 250 Hz under it.
Edit: 0.90 s, −24.0 dB, ceiling −8 dBFS (peak −10.9, −21.9 LUFS; 69 % of the energy below 200 Hz).

### lance-rumble-r37-a — synthesized

Proposed (round 37).
Source: none. Use: the deep rumble under the Orbital Lance's 3 s column (with the column's loop c; looped, so a longer column works too).
Why: An orbital roar: circular noise peaking at 60 Hz (with a 120–400 Hz band so small speakers carry it) swelling 15 % once a loop, over two low harmonic voices on 41.0 / 41.3 Hz (harmonics 1–10, a bump at 200 Hz) beating once a loop.
Edit: seamless 3.00 s loop, band RMS −36.0 dB (the ambience loops' level), −24.9 LUFS (the column's loop c −22.5), peak −13.7 dBFS, 87 % of the energy below 200 Hz; seam step +3.1 dB after encoding.

### lance-rumble-r37-b — "Cracking Earthquake" by uagadugu + synthesized roar

Proposed (round 37).
Source: <https://freesound.org/people/uagadugu/sounds/222521/> — CC0 1.0 (the root burst's rumble). Use: as a.
Why: The ground answers: the earthquake (30 Hz–1.2 kHz, filtered before the cut; 3.0 s from 2.0 s, a 0.6 s cross-fade) with its cracking, over a's roar 4 dB under.
Edit: seamless 3.00 s loop, band RMS −36.1 dB, −23.1 LUFS, peak −11.4 dBFS, 94 % below 200 Hz; seam step −3.3 dB after encoding.

### overcharge-whoosh-r37-a — "Power Up Charge" by qubodup + synthesized whoosh and shimmer

Proposed (round 37).
Source: <https://freesound.org/people/qubodup/sounds/172631/> — CC0 1.0 (the shield-restore chime's source). Use: the Shield Overcharge's activation.
Why: The restore, charged: the restore's source (0.8 s) at 0.9× speed, a rising whoosh (band 400 → 4000 Hz over 0.7 s) and a white shimmer bloom at the top (nine fluttering partials 2.5–6.5 kHz gliding up 8 %).
Edit: 1.00 s, −19.4 dB, ceiling −3 dBFS (peak −3.1, −14.2 LUFS: the shield restore's −14.2).

### overcharge-whoosh-r37-b — synthesized

Proposed (round 37).
Source: none. Use: as a.
Why: A double shell: a soft supersaw pad rising an octave (220 → 440 Hz, smoothstep over 0.75 s) as its low-pass opens 500 → 5000 Hz, the same whoosh, and a brighter bloom (eleven partials 3–8 kHz).
Edit: 1.00 s, −19.0 dB, ceiling −3 dBFS (peak −7.0, −19.8 LUFS; nothing below 200 Hz: lighter than a).

### overcharge-end-r37-a — synthesized

Proposed (round 37).
Source: none. Use: the end of the overcharge's 8 s (the second shell fading).
Why: The shimmer fades: ten high partials (2–7 kHz) each fluttering at its own rate, gliding down to 0.7× and ringing out.
Edit: 0.90 s, −23.9 dB, ceiling −8 dBFS (peak −11.4, −24.3 LUFS).

### overcharge-end-r37-b — "Power Up Charge" by qubodup + synthesized shimmer

Proposed (round 37).
Source: <https://freesound.org/people/qubodup/sounds/172631/> — CC0 1.0. Use: as a.
Why: The charge drains: the restore's source (its first 0.6 s, 200 Hz–4 kHz) played backwards, so its rise becomes a fall, and faded out over its length, under a falling shimmer (eight partials 2.5–6 kHz gliding to 0.75×).
Edit: 0.90 s, −24.2 dB, ceiling −8 dBFS (peak −9.2, −20.6 LUFS).

### cooldown-ready-r37-a — synthesized

Proposed (round 37).
Source: none. Use: a cooldown special's ring full (the HUD's white flash).
Why: Two soft bells: A5 then E6, 70 ms apart, each a sine with a quiet 2.76× partial decaying three times faster (a small bell), 4 ms attack, tau 0.18 s; a different pair from the sonde chime's E6 / B6.
Edit: 0.50 s, −22.0 dB (UI: the equip sound −24.7, the sonde chime −15.9), ceiling −8 dBFS (peak −13.6, −21.6 LUFS).

### cooldown-ready-r37-b — synthesized

Proposed (round 37).
Source: none. Use: as a.
Why: A glass fifth: one soft C6–G6 chord (8 ms attack) with a quiet shimmering partial at 4.1× the root, tau 0.25 s.
Edit: 0.50 s, −22.0 dB, ceiling −8 dBFS (peak −14.2, −21.6 LUFS).

### dash-r37-a — synthesized

Proposed (round 37).
Source: none. Use: the Evasive thrusters' dash (0.12 s, 72 px).
Why: A thruster puff: noise through a band-pass sweeping 600 → 2500 Hz in 0.04 s and back to 900 Hz over the rest (6 ms attack, tau 0.09 s) over a short low roar (noise under 400 Hz, tau 0.1 s); the game has no engine loop, so "the engine's family" is a jet's.
Edit: 0.35 s, −20.6 dB (the overdrive start −19.9), ceiling −4 dBFS (peak −4.2).

### dash-r37-b — "RocketThrustMaxx.wav" by Maxx222 + "Afterburner (Close, Screamer, Quick Onset)" by JSK_Productions

Proposed (round 37).
Source: <https://freesound.org/people/Maxx222/sounds/446764/>, <https://freesound.org/people/JSK_Productions/sounds/829068/> — CC0 1.0. Use: as a.
Why: A rocket kick: 0.35 s of a steady rocket thrust (from 2.0 s, 8 ms fade-in, 0.25 s fade-out) with the afterburner's scream at its loudest (from 2.2 s, 300 Hz high-pass) 4 dB under; a heavier, rawer burst than a.
Edit: 0.35 s, −20.1 dB, ceiling −4 dBFS (peak −4.5; 47 % of the energy below 200 Hz).

### drone-break-r37-a — "HeavyBulletPing.mp3" by wilhellboy + "Small Explosion" by Cyberios + synthesized sparks

Proposed (round 37).
Source: <https://freesound.org/people/wilhellboy/sounds/351371/>, <https://freesound.org/people/Cyberios/sounds/145788/> — CC0 1.0 (the metal hit's and the `tiny` blast a's sources). Use: a light drone breaking.
Why: As the brief says, the metal hit and a `tiny` blast: the ping (0.3 s), the blast (0.45 s, 90 Hz high-pass) 20 ms later and a spark spray (3–9 kHz with a metallic comb, dying in 0.25 s).
Edit: 0.49 s, −20.2 dB (the `tiny` blasts −18.1 / −14.4), ceiling −4 dBFS (peak −7.0, −17.8 LUFS).

### drone-break-r37-b — "Impact on metal" by JoMungus + "Small explosion" by dinodilopho + synthesized sparks

Proposed (round 37).
Source: <https://freesound.org/people/JoMungus/sounds/726486/>, <https://freesound.org/people/dinodilopho/sounds/328833/> — CC0 1.0 (the armour hit's and the `tiny` blast b's sources). Use: as a.
Why: A crunch: struck plating (its first 0.4 s, 120 Hz high-pass), the blast (0.6 s) 30 ms later and the sparks; duller and heavier than a.
Edit: 0.63 s, −20.3 dB, ceiling −4 dBFS (peak −7.3, −19.5 LUFS).

### drone-rebuild-r37-a — synthesized

Proposed (round 37).
Source: none. Use: a light drone rebuilt (its 0.3 s fade-in).
Why: Materialise: a soft filtered saw pad (two voices 0.4 % apart) easing up a fourth, 330 → 440 Hz, as its low-pass opens 600 → 3000 Hz, a 0.15 s attack and release.
Edit: 0.60 s, −24.9 dB, ceiling −10 dBFS (peak −19.8, −23.9 LUFS).

### drone-rebuild-r37-b — synthesized

Proposed (round 37).
Source: none. Use: as a.
Why: Three bells: E5, A5, E6 sine bells (as the ready chime a's) 60 ms apart over a faint 4–9 kHz sparkle fading in; no glide.
Edit: 0.57 s, −25.0 dB, ceiling −10 dBFS (peak −15.5, −24.9 LUFS).

## Round 38 (M7 part B) — Level 15: the Burrower's bulge and dive, the entry roar, the Mars dust wind

Brief (M7 part B "Level 15 Olympus Descent", step AUD-B; round 38 closed 2026-10-14: bulge **b**, dive **b**, entry roar **a**, `mars-dust` **a**; the unchosen dive a, entry roar b and `mars-dust` b in `rejected/`, where `sfx_r38.py` now writes them): a/b for the Level 15 sounds of [*Act 3 sounds*](../README.md#act-3-sounds-approved-round-36) and the `mars-dust` ambience: the Burrower's 1.0 s bulge tell and its 0.4 s dive, the high thin entry roar of sections 1–2 and the Mars dust wind from section 3 (and Levels 20–21). All from `tools/concept/audio/sfx_r38.py`, deterministic, from **cached Freesound originals credited in earlier rounds** (smidoid, nicoproson, iwanPlays, dobroide, craigsmith, cobratronik; CC0 1.0, smidoid and dobroide CC-BY 4.0) plus synthesized layers. No bubbly, popping or squelching layers (user rule): grains Hann-windowed and dense enough to fuse, no pitch-swept blips, no low thump under the dive; files scanned for isolated 10 ms frames 12 dB over their surroundings (only the bulge layer's sparse early cracks, by design, masked in its mix with the eruption). Levels: one-shots on the loudest 100 ms of the 200 Hz–5 kHz band, the loops by band RMS (−36 dB like every ambience), one gain and no limiter. Not listened to by Claude: checked by envelopes, spectrograms, band balance, levels and the loop seams.

The chosen eruption ([burrow-eruption-r36-a](burrow-eruption-r36-a.ogg), S.2 = a) already contains the bulge: its first 1.0 s is the ground rumble swelling 14 dB with a pressure tone and a sand hiss, the impact lands at 1.0 s. A separate rumble would double it, so the bulge's **a is no file**: the eruption a is started at the bulge's start (the game's default until the pick); b adds a crack layer over it.

### burrow-bulge-r38-b — "cracking.wav" by smidoid + synthesized cracks and trickle

Proposed (round 38).
Source: <https://freesound.org/people/smidoid/sounds/49139/> — CC-BY 4.0. Use: the Burrower's bulge (`burrow`), a layer started with the eruption a at the bulge's start.
Why: Sand and crust cracking: 30 synthesized cracks (4–18 ms band noise, 1.2–6 kHz, with a duller tick) bunching and growing towards the break at 1.0 s, smidoid's cracking at 0.8× speed from 0.5 s (250 Hz high-pass, panned left), and a gritty trickle (2.5–7 kHz, fluttering) swelling under them; no rumble of its own.
Edit: 1.27 s, −25.0 dB, ceiling −6 dBFS (peak −6.1, −20.9 LUFS; 0 % below 200 Hz). Mixed with the eruption ([burrow-bulge-mix-r38-b](burrow-bulge-mix-r38-b.ogg)): peak −2.2 dBFS, −15.8 LUFS, the loudest 100 ms band −19.1 dB (the eruption's −19.3 alone).

### burrow-dive-r38-a — "SAND POUR.wav" by nicoproson + "Bricks/Stones/Rocks/Gravel Falling" by iwanPlays + synthesized slump

Proposed (round 38).
Sources: <https://freesound.org/people/nicoproson/sounds/627070/>, <https://freesound.org/people/iwanPlays/sounds/567249/> — CC0 1.0. Use: the Burrower diving (0.4 s).
Why: A falling sand hiss: the pour (0.6 s from 11.6 s, 350 Hz high-pass) under a low-pass falling 9 → 1.6 kHz, a few stones dropping in 7 dB under (300 Hz high-pass, panned right) and a soft sand slump (a 70–250 Hz noise band) 9 dB under.
Edit: 0.62 s, −22.2 dB, ceiling −4 dBFS (peak −10.3, −22.0 LUFS; 90 % of the energy in 200 Hz–5 kHz).

### burrow-dive-r38-b — synthesized

Proposed (round 38).
Source: none. Use: as a.
Why: A grain slide: 2–6 ms noise grains band-passed round a centre falling 5 → 1 kHz, coming 900 → 150 a second (a hiss sliding down, never single clicks), fading out, over the same slump; set 4 dB under a's band level because its energy lies higher (at −22 dB it measured 5 LU louder than a).
Edit: 0.55 s, −26.4 dB, ceiling −4 dBFS (peak −5.1, −21.4 LUFS), a 20 ms fade-in.

### entry-roar-r38-a — "20101121.wind.howl.furious.01.wav" by dobroide + synthesized buffet

Proposed (round 38).
Source: <https://freesound.org/people/dobroide/sounds/109371/> — CC-BY 4.0 (the storm bed's howl, another stretch). Use: Level 15's entry roar in sections 1–2 (a loop; the game raises its level as the air thickens and crossfades it into `mars-dust` at section 3).
Why: A high thin wind roar over the hull: the howl high-passed at 650 Hz before the cut (its furious body gone, the thin roar left; 12 s from 150 s, 1.5 s cross-fade) over a synthesized hull buffet (circular noise 30–180 Hz riding 2, 5 and 9 gust cycles a loop) 10 dB under.
Edit: seamless 12.00 s loop, band RMS −36.2 dB, −29.0 LUFS, peak −17.1 dBFS; seam step +6.7 dB after encoding (a noise join, no click).

### entry-roar-r38-b — synthesized

Proposed (round 38).
Source: none. Use: as a.
Why: Thin air tearing past the hull: a roar band (circular noise 0.7–6 kHz, peaking at 2 kHz) and a resonant whistle band-passed round a centre wandering 1.6–2.6 kHz (q 6; the filter run over three loop lengths and the middle kept), both riding gusts of 1, 3 and 7 cycles a loop, over a's buffet.
Edit: seamless 12.00 s loop, band RMS −36.3 dB, −27.9 LUFS, peak −17.3 dBFS; seam step +1.9 dB after encoding.

### mars-dust-r38-a — "G56-22-Sandstorm.wav" by craigsmith + "Wind__Artic__Cold.wav" by cobratronik

Proposed (round 38).
Sources: <https://freesound.org/people/craigsmith/sounds/438877/>, <https://freesound.org/people/cobratronik/sounds/117136/> — CC0 1.0. Use: `mars-dust`, the Martian dust wind (Level 15 from section 3, Levels 20 and 21; Level 18's eye crossfades the storm bed into it).
Why: A recorded dust wind, the storm bed's calmer cousin: the sandstorm's steady grit (16 s from 44 s, 0.3–6 kHz, so not the storm bed's stretch) riding slow periodic gusts (1 and 3 cycles a loop, ±25 % and ±12 %), over the low moan of a cold wind (16 s from 30 s, 650 Hz low-pass) 6 dB under; no furious howl.
Edit: seamless 16.00 s loop, band RMS −36.1 dB, −32.7 LUFS, peak −21.2 dBFS; seam step −13.0 dB after encoding.

### mars-dust-r38-b — synthesized

Proposed (round 38).
Source: none. Use: as a.
Why: A thin Martian wind: pinkish noise (circular, 150 Hz–3 kHz, peaking at 500 Hz), a faint whistle band-passed round a centre wandering 0.9–1.4 kHz (q 5) and a grit hiss (3–8 kHz) rising with the gusts (1, 2 and 5 cycles a loop), the two channels apart.
Edit: seamless 16.00 s loop, band RMS −36.1 dB, −29.5 LUFS, peak −17.6 dBFS; seam step +2.9 dB after encoding.

## Round 39 (M7 part C) — Level 16: the tendril's hum, burn and break, the Threadcrawler's walk, the canyon ambience

Brief (M7 part C "Level 16 Valles Canyon Run", step AUD-C; pending the user's review in round 39): one proposal (a) each for the Level 16 sounds of [*Act 3 sounds*](../README.md#act-3-sounds-approved-round-36) and the `mars-canyon` ambience; the canyon wall scrape is the chosen [canyon-scrape-r36-b](canyon-scrape-r36-b.ogg) (Halleck's CC-BY scrape, [CREDITS.md](../../../../CREDITS.md)), copied into the assets unchanged. All from `tools/concept/audio/sfx_r39.py`, deterministic and **fully synthesized** (no third-party source, nothing new for CREDITS.md). No bubbly, popping or squelching layers (user rule): transients are band-noise grains with a fast decay, tones are steady or glide with a sawtooth's harmonics and fade out before a low tone, never a falling sine. Levels: the break on the loudest 100 ms of the 200 Hz–5 kHz band (−17 dB), the loops by band RMS, one gain and no limiter. Not listened to by Claude: checked by envelopes, band balance, levels and the loop seams.

### tendril-hum-r39-a — synthesized

Chosen (round 39, S.1, user 2026-10-18). Source: none. Use: a tendril barrier live (a loop; the nearest one plays; the game raises its gain over the 1.0 s dormant tell into the flare).
Why: A taut electric-organic drone: 120 Hz and its harmonics up to 5 kHz (whole cycles in the loop, odd partials stronger, the third a little reedy, fixed random phases per channel) over a faint steady fizz (circular noise 2.5–6 kHz). No wobble: no vibrato, no beating pair, no amplitude modulation, so it cannot read as bubbling.
Edit: seamless 6.00 s loop, band RMS −38.0 dB, peak −20.7 dBFS; 77 % of the energy lies below 200 Hz (the fundamental), seam step +3.5 dB after encoding (a steady tone, no click).

### tendril-burn-r39-a — synthesized

Chosen (round 39, S.2, user 2026-10-18). Source: none. Use: a `beam` on a tendril (a loop while it burns, over the beam's own loop and the rock-or-target impact layer).
Why: A searing crackle: 3–9 ms noise grains (band 2–8 kHz, fast exponential decay, ~220 a second at random strengths, two channels apart, placed circularly) over a dry sizzle bed (3–9 kHz, gusts of 2 and 7 cycles a loop).
Edit: seamless 2.00 s loop, band RMS −34.1 dB, peak −5.3 dBFS; seam step −1.4 dB after encoding.

### tendril-break-r39-a — synthesized

Chosen (round 39, S.3, user 2026-10-18). Source: none. Use: the tendril or an anchor dies (one-shot).
Why: A whip-crack (a 2 ms broadband snap, then a 30 ms noise tail above 1.8 kHz) and the line going slack: six sawtooth partials and a band-noise component gliding 2.4 → 0.55 kHz over 0.7 s from 12 ms in, fading out before it settles into a low tone.
Edit: 0.92 s, −17.3 dB loudest 100 ms band, ceiling −4 dBFS (peak −4.1, −14.3 LUFS); 0 % below 200 Hz.

### crawler-walk-r39-a — synthesized

Chosen (round 39, S.4, user 2026-10-18). Source: none. Use: the Threadcrawler's walk (a loop while any of it is on screen); the pores use the enemy small shot, the death the Coilwyrm's bursts.
Why: A dry many-legged skitter on rock: twelve legs in two staggered rows, each a 2–6 ms noise tick (1.8–7 kHz, fast decay) once per step, 25 steps in the loop (8.4 a second), a little timing jitter and the odd skipped tick so it never marches, spread across the field by leg; under a faint rock rustle (0.9–2.8 kHz).
Edit: seamless 3.00 s loop, band RMS −36.1 dB, peak −6.7 dBFS; seam step −27 dB after encoding.

### mars-canyon-r39-a — synthesized

Chosen (round 39, S.5, user 2026-10-18). Source: none. Use: ambience `mars-canyon` (Level 16).
Why: The Martian dust wind with a canyon howl: pinkish wind (circular noise 120 Hz–3 kHz, peaking at 380 Hz) and a grit hiss over a resonant moan (band-passed noise, q 9, wandering 260–420 Hz) with a second voice an octave up (q 7, 600–760 Hz), both breathing with slow gusts (1, 2 and 5 cycles a loop); lower and steadier than `mars-dust`.
Edit: seamless 16.00 s loop, band RMS −36.0 dB, peak −16.8 dBFS; seam step +6.4 dB after encoding (a noise join, no click).

## Concept round 40 (M7 part D, Level 17 "Dome Siege")

All twelve are synthesized by `tools/concept/audio/sfx_r40.py` (no third-party layers, nothing for CREDITS.md);
no bubbly, popping or squelching layers. Proposed (round 40), option a each, pending the user's review.
Levels: one-shots on the loudest 100 ms of the 200 Hz–5 kHz band, loops by the whole loop's band RMS.

### dome-hit-r40-a — synthesized
Use: a dome of Level 17 takes a hit (`ALLY_HIT`). Brief: thick glass under stress: a heavy knock, a creak running round the dome and a fine crackle of stress lines; no shatter. Why: a 70–190 Hz noise thud plus a 1.2–3 kHz tick, a band-noise creak (resonance wandering 700 → 1400 Hz, panned across the field) and 60 grains of 2–9 kHz. Edit: 1.2 s, −18 dB loudest 100 ms band, ceiling −4 dBFS.

**Rejected** (round 49, M.1, 2026-10-22): replaced by dome-hit-r49-b.

### dome-break-r40-a — synthesized
Use: a dome breaks (`ALLY_LOST`). Brief: a long glass cave-in: a crack, panes going in a cascade, air rushing out in a falling hiss, a steel frame groan. Why: a snap, ~400 glass grains (3–10 ms, thinning out), a hiss falling 6 → 1.2 kHz and a rough sawtooth set sinking 150 → 85 Hz. Edit: 3.6 s, −16 dB, ceiling −4 dBFS.

### dome-break-r40-b — Freesound mix (redo after the user's verdict on a)
Use: a dome breaks (`ALLY_LOST`). Brief: a cave-in / collapse: a low rumble, the structure coming down, glass and frame breaking; no bubbly or popping layer. Why: the user on a: "This doesn't sound as a cave in. It needs a rumble." Sources: [collapse.wav](https://freesound.org/people/metalfortress/sounds/611801/) (metalfortress, CC0) 0–2.4 s, 30 Hz high-pass; [Glass Shatter 2](https://freesound.org/people/Greg_Surr/sounds/554564/) (Greg_Surr, CC0) at 0.15 s; a synthesized 55 → 38 Hz sub thump. Edit: tools/concept/audio/sfx_r40_dome_break.py, 2.4 s, −16 dB, ceiling −4 dBFS.

### dome-break-r40-c — Freesound mix (redo after the user's verdict on a)
Use: as b. Brief: a heavier rubble crash over a deep ground rumble, panes smashing in it. Sources: [Building Collapse](https://freesound.org/people/craigsmith/sounds/845932/) (craigsmith, CC0) 0–2.4 s, low-passed at 2.2 kHz; [Earth's Crust Depressing](https://freesound.org/people/bevibeldesign/sounds/366089/) (bevibeldesign, CC0) 1.0–3.4 s, 180 Hz low-pass; [Glass Smash](https://freesound.org/people/dwswords/sounds/848312/) (dwswords, CC0) at 0.5 s. Edit: same script, 2.4 s, −16 dB, ceiling −4 dBFS.

### herald-song-r40-a — synthesized
Use: the Choir Herald's song (with its `ring`). Brief: a short sung phrase in the Choir's colour, not words. Why: three sawtooth voices (unison, a fifth, an octave) through 800/1150/2900 Hz formants, F3 → A3 → E3 with 0.12 s slides and a slow vibrato. Edit: 1.9 s, −20 dB.

### herald-aura-r40-a — synthesized
Use: the aura, a loop while any ring is up. Brief: a soft high shimmer. Why: six steady sine partials (1.9–4.6 kHz, whole cycles in the loop) each with a slow amplitude swell, over faint 5–9 kHz air; no vibrato or beating pair. Edit: seamless 4.0 s loop, −40 dB band RMS.

### aura-absorb-r40-a — synthesized
Use: a ring absorbs a hit. Brief: a glassy ping (the enemy-shield hit). Why: four steady inharmonic partials (2.1, 3.3, 4.7, 6.1 kHz), 3 ms attack, exponential decay, never a pitch glide. Edit: 0.4 s, −22 dB.

### aura-collapse-r40-a — synthesized
Use: the rings collapse at the Herald's death. Brief: a falling chord. Why: five sustained voices (A4 minor-ish stack) sliding a fourth down together over 1.6 s, with a fading sheet of ring-glass pings. Edit: 2.0 s, −18 dB.

### shellback-walk-r40-a — synthesized
Use: the Shellback's walk (a loop while on screen). Brief: heavy padded footfalls with shell plates clacking. Why: four footfalls (60–160 Hz thud and a 300–900 Hz pad), left and right, each followed by four 1–3.6 kHz plate ticks. Edit: seamless 3.2 s loop, −34 dB band RMS.

### shellback-lob-r40-a — synthesized
Use: the Shellback's mortar lob (the impact is the Polyp Mortar's chosen impact). Brief: the mortar's lob, lower. Why: an 80–260 Hz cough thump with a short rise-and-fall pressure hiss (300–1800 Hz). Edit: 0.6 s, −17 dB.

### shellback-curl-r40-a — synthesized
Use: the Shellback curls up. Brief: plates grinding shut. Why: eleven staggered plate scrapes (band noise sweeping down from 0.6–2.5 kHz) ending in a heavy clack. Edit: 0.8 s, −20 dB.

### shellback-roll-r40-a — synthesized
Use: the Shellback's roll (a loop). Brief: a rumbling grind that crushes. Why: a 40–140 Hz rumble and a gritty 0.7–4.5 kHz crush noise swelling on 2.5 turns in the loop. Edit: seamless 2.5 s loop, −30 dB band RMS.

### shellback-uncurl-r40-a — synthesized
Use: the Shellback uncurls. Brief: the plates opening. Why: the curl's scrapes in reverse order (rising), a looser clack and a short exhale of grit. Edit: 0.7 s, −22 dB.

### mars-colony-r40-a — synthesized
Use: ambience `mars-colony` (Level 17). Brief: the plains wind, the domes' air-plant hum, a distant rail clank. Why: a thinner wind than `mars-dust` (circular noise 100 Hz–2.4 kHz) with slow gusts, a steady 60 Hz hum with partials 2–6, and three soft 900 Hz metal tick pairs per loop, far off. Edit: seamless 16.0 s loop, −36 dB band RMS.

## Concept round 41 (M7 part E, Level 18 "Dust Storm")

All seven are synthesized by `tools/concept/audio/sfx_r41.py` (nothing for CREDITS.md); no bubbly, popping or squelching layers. Proposed (round 41), option a each, pending the user's review. Levels as round 40.

### devil-spin-r41-a — synthesized
Use: the nearest Dust Devil's spin (a loop). Brief: a whirling wind roar with grit. Why: a band-noise roar whose resonance turns 3 times a loop (channels half a turn apart), a 50–130 Hz rumble, 3–8 kHz grit; 3.0 s seamless loop, −32 dB band RMS. Edit: ceiling −4 dBFS (loops −1 dBFS).

### devil-open-r41-a — synthesized
Use: the vanes parting (0.4 s rise and opening). Brief: a gust and a low hollow tone. Why: a rising band-noise gust, five 1–3 kHz vane scrapes, a 110/165/220 Hz hollow tone with a slow attack; 0.7 s, −20 dB. Edit: ceiling −4 dBFS (loops −1 dBFS).

### devil-spiral-r41-a — synthesized
Use: the grit spiral (enemy small shot). Brief: a grit hiss. Why: a 2–7 kHz grit hiss with a resonance wandering 3.5 → 1.8 kHz, panned across, 14 sand ticks; 0.45 s, −22 dB. Edit: ceiling −4 dBFS (loops −1 dBFS).

### ghost-drone-drop-r41-a — synthesized
Use: the Ghost Drone dropping into the lane. Brief: a clean turbine whine descending; built, not grown. Why: a harmonic set gliding 1100 → 520 Hz, smooth attack, faint air hiss; 0.9 s, −24 dB. Edit: ceiling −4 dBFS (loops −1 dBFS).

### ghost-drone-glint-r41-a — synthesized
Use: the 0.3 s eye glint. Brief: a short electronic lock tick. Why: two inharmonic pings (2.6 and 3.9 kHz, 2 ms attack, 60 ms decay); 0.15 s, −24 dB. Edit: ceiling −4 dBFS (loops −1 dBFS).

### ghost-drone-burst-r41-a — synthesized
Use: the Ghost Drone's burst. Brief: three dry precise shots (human guns, not Vrell). Why: a snap, a 1.5–4 kHz crack and a 150–400 Hz body each, 65 ms apart, no tail; 0.35 s, −21 dB (the −4 dBFS ceiling limits the level). Edit: ceiling −4 dBFS (loops −1 dBFS).

### ghost-drone-death-r41-a — synthesized
Use: the Ghost Drone's death, `small` explosion rung. Brief: a mechanical break-up, sparks and metal. Why: a dry 100–300 Hz thud and snap, nine inharmonic metal pieces, 70 spark grains of 2.5–9 kHz, no fireball rumble; 0.8 s, −19 dB. Edit: ceiling −4 dBFS (loops −1 dBFS).

## Concept round 42 (M7 part F, Level 19 "Foundry of Tharsis")

All thirteen are synthesized by `tools/concept/audio/sfx_r42.py` (nothing for CREDITS.md); no bubbly, popping or squelching layers, every blast with a ramped attack. Proposed (round 42), option a each, pending the user's review. Levels as round 41.

### walker-stomp-r42-a — synthesized
Use: the Revenant Walker's footfall (each stride). Brief: a dull heavy stomp with a hydraulic hiss, no pop. Why: a 30–120 Hz thud and a 120–400 Hz body (band noise, 6 ms attack), a low leg clank (inharmonic partials round 260 Hz, fast decay) and a 2–6 kHz hydraulic hiss easing off; 1.0 s, −19 dB (the −4 dBFS ceiling limits the level). Edit: ceiling −4 dBFS (loops −1 dBFS).

### walker-hatch-r42-a — synthesized
Use: a pod's hatch flipping open, the 0.4 s tell before a salvo. Brief: a mechanical clunk. Why: a latch tick, a short 1.8–6 kHz pneumatic hiss and a hinge clack (inharmonic partials round 900 Hz with a low knock) at 0.11 s; 0.45 s, −22 dB. Edit: ceiling −4 dBFS (loops −1 dBFS).

### walker-missile-r42-a — synthesized
Use: one pod missile leaving (an alternative to the planned enemy missile launch r08 a). Brief: an ignition and a rocket whoosh. Why: a dry ignition crack, a band-noise whoosh rising 0.5 → 2.6 kHz (20 ms attack) that slides off to one side and a thin 3–8 kHz exhaust hiss; 0.8 s, −19 dB. Edit: ceiling −4 dBFS (loops −1 dBFS).

### walker-flame-tell-r42-a — synthesized
Use: the flamer's 0.6 s tell. Brief: a gas hiss rising. Why: band noise rising 0.8 → 3.2 kHz and swelling over a low 80–400 Hz gas rush, cut at the end for the flame; 0.7 s, −22 dB. Edit: ceiling −4 dBFS (loops −1 dBFS).

### walker-flame-r42-a — synthesized
Use: the flame cone (a loop while it burns). Brief: a roaring flame. Why: an 80–1400 Hz roar through a wandering resonance, flickering at 3, 7 and 11 cycles a loop (channels apart), a 40–120 Hz body and a soft 3–7 kHz gas hiss, no crackle grains; 2.0 s seamless loop, −28 dB band RMS. Edit: ceiling −4 dBFS (loops −1 dBFS).

### vent-tell-r42-a — synthesized
Use: a furnace vent's 1.0 s tell (the glow). Brief: a rising gas roar. Why: a resonance rising 250 → 1400 Hz and swelling over a deepening 40–160 Hz rumble, ending at its peak for the burst; 1.05 s, −22 dB. Edit: ceiling −4 dBFS (loops −1 dBFS).

### vent-burst-r42-a — synthesized
Use: the vent's burst. Brief: a fire blast roaring out and dying, no pop at the start. Why: a 40 ms attack, a roar whose resonance falls 1.4 → 0.3 kHz, a broad 60–3000 Hz body and a low rush, decaying over 1.5 s; −16 dB. Edit: ceiling −4 dBFS (loops −1 dBFS).

### tank-tracks-r42-a — synthesized
Use: the Warden Tank's tracks (a loop, the nearest tank). Brief: grinding treads and a diesel-turbine hum. Why: a 400–1600 Hz tread grind, track-link ticks 14 a second (0.7–2.6 kHz grains, both tracks), a 41 Hz diesel harmonic set and a faint 1.9 kHz turbine whine; 2.0 s seamless loop, −32 dB band RMS. Edit: ceiling −4 dBFS (loops −1 dBFS).

### tank-turret-r42-a — synthesized
Use: the turret slew. Brief: a servo whine. Why: a harmonic set gliding 420 → 640 Hz then holding, an 80 ms attack and a 150 ms release, eight faint gear ticks; 0.6 s, −24 dB. Edit: ceiling −4 dBFS (loops −1 dBFS).

### tank-burst-r42-a — synthesized
Use: the burst after the 0.3 s hold. Brief: a heavy autocannon. Why: four rounds 0.12 s apart, each a snap, a 0.8–3 kHz crack and a 50–250 Hz body with a short low echo; 0.75 s, −18 dB (the −4 dBFS ceiling limits the level). Edit: ceiling −4 dBFS (loops −1 dBFS).

### tank-death-r42-a — synthesized
Use: the Warden Tank's death. Brief: a hull explosion with metal debris. Why: a deep 30–200 Hz boom, a fire roar falling 1.5 → 0.3 kHz, a heavy metal clang (inharmonic partials round 420 Hz), eight pieces of debris clattering down and 50 spark grains; 1.8 s, −17 dB. Edit: ceiling −4 dBFS (loops −1 dBFS).

### belt-rumble-r42-a — synthesized
Use: the conveyor belts (a loop while belts are on screen; the plan folds them into the bed, offered all the same). Brief: a belt rumble. Why: a 40–160 Hz roller rumble, a 200–1200 Hz belt grind, plate seams knocking 6 a second (a 250–900 Hz knock and a 1.2–2.5 kHz rattle) and a 50 Hz motor hum; 2.4 s seamless loop, −34 dB band RMS. Edit: ceiling −4 dBFS (loops −1 dBFS).

### mars-foundry-r42-a — synthesized
Use: ambience `mars-foundry` (Level 19). Brief: conveyor rumble, a far furnace roar, a slow hammer, the plateau wind. Why: a 35–180 Hz conveyor rumble, an 80–900 Hz furnace roar (peaking at 240 Hz) breathing slowly, a distant hammer every 4.0 s (a low-passed thud and ring with two hall echoes) and a plains wind on slow gusts; 16.0 s seamless loop, −36 dB band RMS like every ambience loop. Edit: ceiling −4 dBFS (loops −1 dBFS).

## Concept round 43 (M7 part F, Level 19: the walker's and the tank's sounds from the database)

The user (round 42): "You should really stop trying to make these sounds yourself. Use the database." Every file is cut from **Freesound originals** (CC0 or CC-BY, each in [CREDITS.md](../../../../CREDITS.md)) by `tools/concept/audio/sfx_r43.py`: cut, slowed, layered, faded and levelled, nothing synthesized; big and heavy sources, nothing bubbly or popping, every blast with a ramped attack. An a/b pair each, proposed (round 43), pending the user's review; the game keeps the round-42 files until the pick. Levels as round 42: one-shots on the loudest 100 ms of the 200 Hz–5 kHz band under a −4 dBFS ceiling (the death −1.5 dBFS), loops by the whole loop's band RMS. The walker's pod missile stays the chosen [enemy-missile-r08-a](enemy-missile-r08-a.ogg) (nothing new).

### walker-hatch-r43-a — "AIRBrst-Samsung Galaxy Smartphone, CU_Truck, Air Brakes, Released_Nicholas Judy_TDC" by designerschoice + "Closing large metal door-001" by AudioPapkin

Proposed (round 43).
Sources: <https://freesound.org/people/designerschoice/sounds/803052/> (AIRBrst-Samsung Galaxy Smartphone, CU_Truck, Air Brakes, Released_Nicholas Judy_TDC by designerschoice, CC-BY 4.0), <https://freesound.org/people/AudioPapkin/sounds/814976/> (Closing large metal door-001 by AudioPapkin, CC0 1.0). Use: a pod's hatch flipping open, 0.4 s before its salvo (the tell).
Brief: round 42: "That's not a hatch, that's a pop. Isn't this supposed to be a heavy thing?" A heavy armoured hatch: a hydraulic latch and a metal lid. Why: air brakes releasing and a large metal door slamming.
Edit: 1.05 s, the loudest 100 ms band −20.1 dB, peak −4.1 dBFS, −16.2 LUFS.

### walker-hatch-r43-b — "Air (or steam) pressure release" by brunoboselli + "G45-30-Submarine Hatch Opening.wav" by craigsmith + "Big Structure Collision Heard from Inside" by Osvoldon

Proposed (round 43).
Sources: <https://freesound.org/people/brunoboselli/sounds/457294/> (Air (or steam) pressure release by brunoboselli, CC0 1.0), <https://freesound.org/people/craigsmith/sounds/438732/> (G45-30-Submarine Hatch Opening.wav by craigsmith, CC0 1.0), <https://freesound.org/people/Osvoldon/sounds/653294/> (Big Structure Collision Heard from Inside by Osvoldon, CC0 1.0). Use: a pod's hatch flipping open, 0.4 s before its salvo (the tell).
Brief: round 42: "That's not a hatch, that's a pop. Isn't this supposed to be a heavy thing?" A heavy armoured hatch: a hydraulic latch and a metal lid. Why: a submarine hatch's metal clank and creak over a steam pressure release, a deep impact under the clank.
Edit: 1.01 s, the loudest 100 ms band −20.1 dB, peak −8.4 dBFS, −21.0 LUFS.

### walker-flame-tell-r43-a — "Venting.wav" by Sea Fury

Proposed (round 43).
Source: <https://freesound.org/people/Sea%20Fury/sounds/48710/> (Venting.wav by Sea Fury, CC-BY 3.0). Use: the flamer's 0.6 s tell.
Brief: round 42: "way too small. Search in the database for large venting.". Why: a huge gas vent opening.
Edit: 0.87 s, the loudest 100 ms band −20.6 dB, peak −4.3 dBFS, −15.7 LUFS.

### walker-flame-tell-r43-b — "steam_safety_valve.wav" by Engineer_815

Proposed (round 43).
Source: <https://freesound.org/people/Engineer_815/sounds/493558/> (steam_safety_valve.wav by Engineer_815, CC0 1.0). Use: the flamer's 0.6 s tell.
Brief: round 42: "way too small. Search in the database for large venting.". Why: a 1000 hp boiler's safety valve opening, slowed to 0.85.
Edit: 0.84 s, the loudest 100 ms band −20.1 dB, peak −6.2 dBFS, −15.8 LUFS.

### walker-flame-r43-a — "Flamethrower_Weapon_Long_Bursts.wav" by AslakHostaker

Proposed (round 43).
Source: <https://freesound.org/people/AslakHostaker/sounds/395039/> (Flamethrower_Weapon_Long_Bursts.wav by AslakHostaker, CC0 1.0). Use: the flame cone, a loop while it burns.
Brief: round 42: "Also too small. Search for large gas burns or large flamethrower". Why: a flamethrower's long burst, slowed to 0.85.
Edit: seamless 4 s LOOP, band RMS −28.1 dB, peak −11.8 dBFS, −22.7 LUFS.

### walker-flame-r43-b — "Flamethrower" by pugaeme

Proposed (round 43).
Source: <https://freesound.org/people/pugaeme/sounds/396890/> (Flamethrower by pugaeme, CC-BY 3.0). Use: the flame cone, a loop while it burns.
Brief: round 42: "Also too small. Search for large gas burns or large flamethrower". Why: a flamethrower's deep roar.
Edit: seamless 4 s LOOP, band RMS −28.0 dB, peak −8.8 dBFS, −21.1 LUFS.

### vent-tell-r43-a — "gas vuur dakwerken gasbrander luchtballon.wav" by NickPeeters

Proposed (round 43).
Source: <https://freesound.org/people/NickPeeters/sounds/243763/> (gas vuur dakwerken gasbrander luchtballon.wav by NickPeeters, CC0 1.0). Use: a furnace vent's 1.0 s tell (the glow).
Brief: round 42: "Same thing": a large furnace or gas vent. Why: a balloon burner's roar swelling in.
Edit: 1.05 s, the loudest 100 ms band −20.3 dB, peak −4.1 dBFS, −17.7 LUFS.

### vent-tell-r43-b — "Hot air balloon burner" by YannSauvin

Proposed (round 43).
Source: <https://freesound.org/people/YannSauvin/sounds/778358/> (Hot air balloon burner by YannSauvin, CC0 1.0). Use: a furnace vent's 1.0 s tell (the glow).
Brief: round 42: "Same thing": a large furnace or gas vent. Why: a brighter balloon burner's roar swelling in.
Edit: 1.05 s, the loudest 100 ms band −20.1 dB, peak −7.6 dBFS, −19.0 LUFS.

### vent-burst-r43-a — "Flame Burst" by magnuswaker + "gas vuur dakwerken gasbrander luchtballon.wav" by NickPeeters

Proposed (round 43).
Sources: <https://freesound.org/people/magnuswaker/sounds/592572/> (Flame Burst by magnuswaker, CC0 1.0), <https://freesound.org/people/NickPeeters/sounds/243763/> (gas vuur dakwerken gasbrander luchtballon.wav by NickPeeters, CC0 1.0). Use: the vent's burst.
Brief: round 42: "Same thing": a big fire blast. Why: a deep flame burst with the end of a gas burner's burn, dying away.
Edit: 1.60 s, the loudest 100 ms band −19.3 dB, peak −4.4 dBFS, −13.2 LUFS.

### vent-burst-r43-b — "Fire - Effects - Swooshes - Bursts, evolving" by GregorQuendel

Proposed (round 43).
Source: <https://freesound.org/people/GregorQuendel/sounds/421879/> (Fire - Effects - Swooshes - Bursts, evolving by GregorQuendel, CC-BY 4.0). Use: the vent's burst.
Brief: round 42: "Same thing": a big fire blast. Why: a fire burst roaring out.
Edit: 1.60 s, the loudest 100 ms band −19.8 dB, peak −4.2 dBFS, −11.3 LUFS.

### tank-tracks-r43-a — "t26_moving_01.wav" by greatmganga

Proposed (round 43).
Source: <https://freesound.org/people/greatmganga/sounds/329800/> (t26_moving_01.wav by greatmganga, CC0 1.0). Use: the Warden Tank's tracks, a loop (the nearest tank).
Brief: round 42: "Search for sounds of a real tank driving". Why: a T-26 tank driving, tracks and engine.
Edit: seamless 4 s LOOP, band RMS −32.1 dB, peak −13.3 dBFS, −24.6 LUFS.

### tank-tracks-r43-b — "tank bulldozer tracked vehicle sound effect atmo" by Garuda1982

Proposed (round 43).
Source: <https://freesound.org/people/Garuda1982/sounds/541240/> (tank bulldozer tracked vehicle sound effect atmo by Garuda1982, CC-BY 4.0). Use: the Warden Tank's tracks, a loop (the nearest tank).
Brief: round 42: "Search for sounds of a real tank driving". Why: a tracked bulldozer driving.
Edit: seamless 4 s LOOP, band RMS −32.1 dB, peak −13.8 dBFS, −26.2 LUFS.

### tank-turret-r43-a — "Tank or Tower Turret Moving and Turning" by qubodup

Proposed (round 43).
Source: <https://freesound.org/people/qubodup/sounds/171657/> (Tank or Tower Turret Moving and Turning by qubodup, CC0 1.0). Use: the Warden Tank's turret slew.
Brief: a real heavy turret traverse. Why: a real artillery turret re-aligning.
Edit: 0.80 s, the loudest 100 ms band −23.0 dB, peak −12.2 dBFS, −21.6 LUFS.

### tank-turret-r43-b — "Factory_Crane_Roll.wav" by mickyman5000

Proposed (round 43).
Source: <https://freesound.org/people/mickyman5000/sounds/340634/> (Factory_Crane_Roll.wav by mickyman5000, CC0 1.0). Use: the Warden Tank's turret slew.
Brief: a real heavy turret traverse. Why: a factory overhead crane rolling.
Edit: 0.90 s, the loudest 100 ms band −23.1 dB, peak −9.8 dBFS, −19.5 LUFS.

### tank-burst-r43-a — "Tank Firing" by qubodup

Proposed (round 43).
Source: <https://freesound.org/people/qubodup/sounds/168707/> (Tank Firing by qubodup, CC0 1.0). Use: the Warden Tank's burst after the 0.3 s hold (3 shots 0.15 s apart: the medium burst).
Brief: round 42: "Not a heavy cannon. Not even a small cannon.". Why: a tank cannon firing three shots 0.15 s apart.
Edit: 1.18 s, the loudest 100 ms band −17.0 dB, peak −4.1 dBFS, −13.3 LUFS.

### tank-burst-r43-b — "Guns & Explosions Album - Autocannon - Shot 1.wav" by OGsoundFX

Proposed (round 43).
Source: <https://freesound.org/people/OGsoundFX/sounds/423108/> (Guns & Explosions Album - Autocannon - Shot 1.wav by OGsoundFX, CC-BY 4.0). Use: the Warden Tank's burst after the 0.3 s hold (3 shots 0.15 s apart: the medium burst).
Brief: round 42: "Not a heavy cannon. Not even a small cannon.". Why: a heavy artillery shot, three shots 0.15 s apart.
Edit: 1.38 s, the loudest 100 ms band −17.9 dB, peak −4.1 dBFS, −12.7 LUFS.

### walker-vent-hit-r43-a — "Big stereo metal hit" by LPA134

Proposed (round 43).
Source: <https://freesound.org/people/LPA134/sounds/329515/> (Big stereo metal hit by LPA134, CC0 1.0). Use: a hit on the Revenant Walker's open reactor vents (new).
Brief: a heavy hit on hot metal. Why: a big hit on a lift door.
Edit: 0.55 s, the loudest 100 ms band −20.5 dB, peak −4.1 dBFS, −13.2 LUFS.

### walker-vent-hit-r43-b — "Old_Truck_Metal_Squeaks-Creaks-Rattle-Impact_Hollow_Impact_Soft_Squeak" by dilsun + "steam_burst.wav" by pengo_au

Proposed (round 43).
Sources: <https://freesound.org/people/dilsun/sounds/508272/> (Old_Truck_Metal_Squeaks-Creaks-Rattle-Impact_Hollow_Impact_Soft_Squeak by dilsun, CC0 1.0), <https://freesound.org/people/pengo_au/sounds/90143/> (steam_burst.wav by pengo_au, CC0 1.0). Use: a hit on the Revenant Walker's open reactor vents (new).
Brief: a heavy hit on hot metal. Why: a hollow metal impact with a burst of steam.
Edit: 0.52 s, the loudest 100 ms band −19.2 dB, peak −4.1 dBFS, −19.6 LUFS.

### walker-death-r43-a — "S17-28 Large explosion with debris.wav" by craigsmith + "S10-13 Large hard metal crash; optical.wav" by craigsmith + "Big Structure Collision Heard from Inside" by Osvoldon

Proposed (round 43).
Sources: <https://freesound.org/people/craigsmith/sounds/674899/> (S17-28 Large explosion with debris.wav by craigsmith, CC0 1.0), <https://freesound.org/people/craigsmith/sounds/675462/> (S10-13 Large hard metal crash; optical.wav by craigsmith, CC0 1.0), <https://freesound.org/people/Osvoldon/sounds/653294/> (Big Structure Collision Heard from Inside by Osvoldon, CC0 1.0). Use: the Revenant Walker's death, the `huge` rung (5–6 s): it topples over 2 s with chained explosions and the hull lands (new).
Brief: a giant machine collapsing: explosion and metal. Why: a large explosion with debris, the hull landing with a massive impact and a metal crash.
Edit: 5.60 s, the loudest 100 ms band −13.5 dB, peak −1.6 dBFS, −14.8 LUFS.

### walker-death-r43-b — "Huge Explosion" by florianreichelt + "massive explosion" by Hugofski + "Car Crash (with Glass)" by magnuswaker + "Sounds For Earthquakes - Radiator Metal Rumbling.wav" by RutgerMuller

Proposed (round 43).
Sources: <https://freesound.org/people/florianreichelt/sounds/459973/> (Huge Explosion by florianreichelt, CC0 1.0), <https://freesound.org/people/Hugofski/sounds/177556/> (massive explosion by Hugofski, CC0 1.0), <https://freesound.org/people/magnuswaker/sounds/592388/> (Car Crash (with Glass) by magnuswaker, CC0 1.0), <https://freesound.org/people/RutgerMuller/sounds/51097/> (Sounds For Earthquakes - Radiator Metal Rumbling.wav by RutgerMuller, CC0 1.0). Use: the Revenant Walker's death, the `huge` rung (5–6 s): it topples over 2 s with chained explosions and the hull lands (new).
Brief: a giant machine collapsing: explosion and metal. Why: two chained blasts, the final boom with a car crash's metal and a radiator rumbling.
Edit: 5.60 s, the loudest 100 ms band −14.9 dB, peak −1.8 dBFS, −13.3 LUFS.

### walker-wreck-settle-r43-a — "Metallic Groan" by hinchinbrook + "Clicking Engine Cooldown" by CmdRobot

Proposed (round 43).
Sources: <https://freesound.org/people/hinchinbrook/sounds/496836/> (Metallic Groan by hinchinbrook, CC0 1.0), <https://freesound.org/people/CmdRobot/sounds/539514/> (Clicking Engine Cooldown by CmdRobot, CC0 1.0). Use: the wreck settling under the crawl after the death (new).
Brief: a huge metal hulk settling: creaks, and ticking as it cools. Why: a bunker door's low metallic groan, slowed to 0.85, over an engine cooling.
Edit: 5.00 s, the loudest 100 ms band −23.9 dB, peak −15.4 dBFS, −24.4 LUFS.

### walker-wreck-settle-r43-b — "Creaking Metal - Slow" by EagleStealthTeam + "Clicking Engine Cooldown" by CmdRobot

Proposed (round 43).
Sources: <https://freesound.org/people/EagleStealthTeam/sounds/172411/> (Creaking Metal - Slow by EagleStealthTeam, CC0 1.0), <https://freesound.org/people/CmdRobot/sounds/539514/> (Clicking Engine Cooldown by CmdRobot, CC0 1.0). Use: the wreck settling under the crawl after the death (new).
Brief: a huge metal hulk settling: creaks, and ticking as it cools. Why: a giant metal structure creaking, over an engine cooling.
Edit: 5.00 s, the loudest 100 ms band −23.9 dB, peak −16.9 dBFS, −25.4 LUFS.

## Concept round 43, Level 20 (M7 part G: the basin rumble and the pursuing swarm, from the database)

The same rule as Level 19's round-43 sounds above: every file is cut from **Freesound originals** (CC0 or CC-BY, each in [CREDITS.md](../../../../CREDITS.md)) by `tools/concept/audio/sfx_r43g.py` (cut, slowed or gliding, layered, filtered, faded and levelled; nothing synthesized), big and heavy sources, nothing bubbly or popping. An a/b pair each, proposed (round 43), pending the user's review. Levels: the basin rumble at the loudness of the ambience bed under it (`mars-foundry`, −28 LUFS: nearly all of a rumble's energy is below 200 Hz, so the beds' −36 dB band RMS would put it 6–9 LU over the bed), the swarm's hum as a unit's loop (the tank's tracks, −32 dB band RMS), the lunge as a tell (the loudest 100 ms of the band at −20 dB under −4 dBFS). The TOP warning reuses `ui-edge-warning` and the rim flak the chosen [frigate-flak-r33-a](frigate-flak-r33-a.ogg): nothing new.

### basin-rumble-r43-a — "earth rumble.wav" by Reitanna

Proposed (round 43).
Source: <https://freesound.org/people/Reitanna/sounds/217657/> (earth rumble.wav by Reitanna, CC0 1.0). Use: the Hellas basin under Level 20 from the ridge (t=12) and under Level 21 from the ridge pass (t≈64) into the fight; a loop that can swell.
Brief: a deep earth groan with grinding under it, a loop that can swell (§1.8: a low earth groan). Why: an earthquake's earth rumble, deep, with rock grinding in it.
Edit: seamless 16.0 s LOOP, −28.0 LUFS, band RMS −42.2 dB, peak −12.9 dBFS.

### basin-rumble-r43-b — "Earthquake_Rumble_Creaks_Groans_Dust.wav" by Cell31_Sound_Productions + "EARTHQUAKE OR DISTANT SPACE SHUTTLE RUMBLE.WAV" by metrostock99

Proposed (round 43).
Sources: <https://freesound.org/people/Cell31_Sound_Productions/sounds/263461/> (Earthquake_Rumble_Creaks_Groans_Dust.wav by Cell31_Sound_Productions, CC-BY 4.0), <https://freesound.org/people/metrostock99/sounds/203281/> (EARTHQUAKE OR DISTANT SPACE SHUTTLE RUMBLE.WAV by metrostock99, CC0 1.0). Use: the Hellas basin under Level 20 from the ridge (t=12) and under Level 21 from the ridge pass (t≈64) into the fight; a loop that can swell.
Brief: a deep earth groan with grinding under it, a loop that can swell (§1.8: a low earth groan). Why: an earthquake's rumble with creaks and groans, over a deep distant rumble.
Edit: seamless 12.0 s LOOP, −28.1 LUFS, band RMS −34.3 dB, peak −10.6 dBFS.

### swarm-hum-r43-a — "Swarm Drone.wav" by deerlord + "In the bee box2- swarm of bees_stereo" by Nordliecht

Proposed (round 43).
Sources: <https://freesound.org/people/deerlord/sounds/516517/> (Swarm Drone.wav by deerlord, CC0 1.0), <https://freesound.org/people/Nordliecht/sounds/817927/> (In the bee box2- swarm of bees_stereo by Nordliecht, CC0 1.0). Use: the pursuing Mote Swarm's band on Level 20, a loop while the pursuit is on.
Brief: the chosen swarm whoosh's insect chitter ([mote-swarm-r32-b](mote-swarm-r32-b.ogg)) stretched into a hum; big and heavy, nothing bubbly. Why: a designed swarm drone slowed to 0.85, over a bee box's hive hum.
Edit: seamless 6.0 s LOOP, band RMS −32.2 dB, peak −13.0 dBFS, −23.9 LUFS.

### swarm-hum-r43-b — "Large Swarm of Buzzing Flies.wav" by jakobthiesen + "Blowflies!" by kangaroovindaloo

Proposed (round 43).
Sources: <https://freesound.org/people/jakobthiesen/sounds/207685/> (Large Swarm of Buzzing Flies.wav by jakobthiesen, CC-BY 3.0), <https://freesound.org/people/kangaroovindaloo/sounds/324590/> (Blowflies! by kangaroovindaloo, CC0 1.0). Use: the pursuing Mote Swarm's band on Level 20, a loop while the pursuit is on.
Brief: the chosen swarm whoosh's insect chitter ([mote-swarm-r32-b](mote-swarm-r32-b.ogg)) stretched into a hum; big and heavy, nothing bubbly. Why: a large swarm of buzzing flies slowed to 0.8, over blowflies slowed to 0.9.
Edit: seamless 6.0 s LOOP, band RMS −32.1 dB, peak −9.0 dBFS, −24.8 LUFS.

### swarm-lunge-r43-a — "Swarm Drone.wav" by deerlord + "Powerfull Whooshes 2.wav" by Nic3_one

Proposed (round 43).
Sources: <https://freesound.org/people/deerlord/sounds/516517/> (Swarm Drone.wav by deerlord, CC0 1.0), <https://freesound.org/people/Nic3_one/sounds/434869/> (Powerfull Whooshes 2.wav by Nic3_one, CC0 1.0). Use: a lunge: the swarm's 0.6 s tell (the ball contracts and brightens) and its dive.
Brief: a rising hum and a rush, 0.6 s, no pop. Why: the swarm drone of hum a swelling in and gliding up, a powerful whoosh rushing in at its end.
Edit: 0.80 s, the loudest 100 ms band −20.2 dB, peak −4.6 dBFS, −16.3 LUFS.

### swarm-lunge-r43-b — "Large Swarm of Buzzing Flies.wav" by jakobthiesen + "Big Swoosh" by Electroviolence

Proposed (round 43).
Sources: <https://freesound.org/people/jakobthiesen/sounds/207685/> (Large Swarm of Buzzing Flies.wav by jakobthiesen, CC-BY 3.0), <https://freesound.org/people/Electroviolence/sounds/234547/> (Big Swoosh by Electroviolence, CC0 1.0). Use: a lunge: the swarm's 0.6 s tell (the ball contracts and brightens) and its dive.
Brief: a rising hum and a rush, 0.6 s, no pop. Why: the fly swarm of hum b swelling in and gliding up, a big swoosh rushing in at its end.
Edit: 0.80 s, the loudest 100 ms band −20.9 dB, peak −4.1 dBFS, −16.5 LUFS.

## Concept round 44 (M7 part H, Level 21: the Dust Colossus, and the walker's rail lance and stomp shockwave, from the database)

The user (round 42): "You should really stop trying to make these sounds yourself. Use the database." Every file is cut from **Freesound originals** (CC0 or CC-BY, each in [CREDITS.md](../../../../CREDITS.md)) by `tools/concept/audio/sfx_r44.py` (cut, slowed or gliding, layered, filtered, faded, panned and levelled; nothing synthesized), big and heavy sources, nothing bubbly or popping, every blast with a ramped attack. An a/b pair each, proposed (round 44), pending the user's review; nothing is in the game until the pick (the walker's lance and shockwave play WR2's placeholders). Levels as round 43: one-shots on the loudest 100 ms of the 200 Hz–5 kHz band under a −4 dBFS ceiling (the Colossus's death −1.5 dBFS, the `huge` rung), but at most the accepted round-43 sounds' loudness (the tells −16 LUFS, the rest −14 LUFS: the rumbles are nearly all below 200 Hz and would otherwise reach the band level only through the limiter); the suction loop by its band RMS (−28 dB, the walker's flame loop); the ridge pass by its loudness (−20 LUFS, 8 LU over the `basin-rumble` bed it plays over, which stays the basin's bed on Level 21: the ridge pass is a separate one-shot on top).

### colossus-bulge-r44-a — "Cracking Earthquake (cracking soil, cracking stone)" by uagadugu + "sand pour on sand FF662.aif" by martinimeniscus

Proposed (round 44).
Sources: <https://freesound.org/people/uagadugu/sounds/222521/> (Cracking Earthquake (cracking soil, cracking stone) by uagadugu, CC0 1.0), <https://freesound.org/people/martinimeniscus/sounds/199332/> (sand pour on sand FF662.aif by martinimeniscus, CC0 1.0). Use: a breach's 2.0 s tell: the sand bulges and cracks at the breach point (each breach, and phase 3's marker under the ship).
Brief: the Burrower's rumble, far bigger and longer; a low rumble, no popping (§1.7 breach bulge). Why: an earthquake's cracking soil and stone swelling in, sand pouring off the bulge.
Edit: 2.10 s, the loudest 100 ms band −24.2 dB, peak −4.0 dBFS, −16.0 LUFS.

### colossus-bulge-r44-b — "Large, Low Rumble.wav" by duckduckpony + "Guterton - Heavy Stone Grind" by guterton.wav + "Flowing Sand" by SteveMeli

Proposed (round 44).
Sources: <https://freesound.org/people/duckduckpony/sounds/130972/> (Large, Low Rumble.wav by duckduckpony, CC-BY 4.0), <https://freesound.org/people/guterton.wav/sounds/760672/> (Guterton - Heavy Stone Grind by guterton.wav, CC-BY 4.0), <https://freesound.org/people/SteveMeli/sounds/215006/> (Flowing Sand by SteveMeli, CC-BY 3.0). Use: a breach's 2.0 s tell: the sand bulges and cracks at the breach point (each breach, and phase 3's marker under the ship).
Brief: the Burrower's rumble, far bigger and longer; a low rumble, no popping (§1.7 breach bulge). Why: a large low rumble's crescendo with a heavy stone grind and sand flowing.
Edit: 2.10 s, the loudest 100 ms band −22.4 dB, peak −4.1 dBFS, −16.3 LUFS.

### colossus-eruption-r44-a — "superhero_earth_impact_hit_07062025" by Artninja + "Deep Roar Echo 2.wav" by noahpardo + "avl04.wav" by blaukreuz

Proposed (round 44).
Sources: <https://freesound.org/people/Artninja/sounds/815336/> (superhero_earth_impact_hit_07062025 by Artninja, CC-BY 4.0), <https://freesound.org/people/noahpardo/sounds/345735/> (Deep Roar Echo 2.wav by noahpardo, CC0 1.0), <https://freesound.org/people/blaukreuz/sounds/162668/> (avl04.wav by blaukreuz, CC0 1.0). Use: the mouth breaching (the 1.5 s rise) and phase 3's eruption under the ship.
Brief: a mountain of sand erupting with a roar (§1.7 eruption). Why: an earth impact's boom, a deep creature roar rising out of it, rock and sand pouring back.
Edit: 2.60 s, the loudest 100 ms band −17.4 dB, peak −4.1 dBFS, −15.9 LUFS.

### colossus-eruption-r44-b — "massive explosion" by Hugofski + "Deep Monster Roar 2.wav" by MisterSegev + "Landslide.wav" by bikesnbassboi + "avl02.wav" by blaukreuz + "sand pour on sand FF662.aif" by martinimeniscus

Proposed (round 44).
Sources: <https://freesound.org/people/Hugofski/sounds/177556/> (massive explosion by Hugofski, CC0 1.0), <https://freesound.org/people/MisterSegev/sounds/207746/> (Deep Monster Roar 2.wav by MisterSegev, CC-BY 4.0), <https://freesound.org/people/bikesnbassboi/sounds/543837/> (Landslide.wav by bikesnbassboi, CC-BY 4.0), <https://freesound.org/people/blaukreuz/sounds/162670/> (avl02.wav by blaukreuz, CC0 1.0), <https://freesound.org/people/martinimeniscus/sounds/199332/> (sand pour on sand FF662.aif by martinimeniscus, CC0 1.0). Use: the mouth breaching (the 1.5 s rise) and phase 3's eruption under the ship.
Brief: a mountain of sand erupting with a roar (§1.7 eruption). Why: a massive explosion's boom, a deep monster roar and a landslide's roar, sand pouring back.
Edit: 2.60 s, the loudest 100 ms band −19.9 dB, peak −4.1 dBFS, −14.4 LUFS.

### colossus-suction-r44-a — "fireplace air rush into chimney steady wind current hum.flac" by kyles + "G56-22-Sandstorm.wav" by craigsmith

Proposed (round 44).
Sources: <https://freesound.org/people/kyles/sounds/454020/> (fireplace air rush into chimney steady wind current hum.flac by kyles, CC0 1.0), <https://freesound.org/people/craigsmith/sounds/438877/> (G56-22-Sandstorm.wav by craigsmith, CC0 1.0). Use: the suction, a loop while a mouth is open and pulling.
Brief: a deep inhaling roar with sand streaming, a seamless loop (§1.7 suction draw). Why: air rushing into a chimney (a deep steady draw) under a sandstorm.
Edit: seamless 4.0 s LOOP, band RMS −28.1 dB, peak −9.9 dBFS, −22.5 LUFS.

### colossus-suction-r44-b — "motor industrial turbine large fan blades spin sorta.flac" by kyles + "Monster Inhale.wav" by ecfike + "Flowing Sand" by SteveMeli

Proposed (round 44).
Sources: <https://freesound.org/people/kyles/sounds/453476/> (motor industrial turbine large fan blades spin sorta.flac by kyles, CC0 1.0), <https://freesound.org/people/ecfike/sounds/132865/> (Monster Inhale.wav by ecfike, CC0 1.0), <https://freesound.org/people/SteveMeli/sounds/215006/> (Flowing Sand by SteveMeli, CC-BY 3.0). Use: the suction, a loop while a mouth is open and pulling.
Brief: a deep inhaling roar with sand streaming, a seamless loop (§1.7 suction draw). Why: a large industrial turbine's fan slowed to 0.8, a monster's inhale under it, sand flowing.
Edit: seamless 4.0 s LOOP, band RMS −28.1 dB, peak −9.1 dBFS, −22.2 LUFS.

### colossus-bite-r44-a — "Big Structure Collision Heard from Inside" by Osvoldon + "Horror / Zombie - Biting on bones" by Breviceps + "Scraping and Clunking Large Stone 1.wav" by F.M.Audio

Proposed (round 44).
Sources: <https://freesound.org/people/Osvoldon/sounds/653294/> (Big Structure Collision Heard from Inside by Osvoldon, CC0 1.0), <https://freesound.org/people/Breviceps/sounds/445987/> (Horror / Zombie - Biting on bones by Breviceps, CC0 1.0), <https://freesound.org/people/F.M.Audio/sounds/572754/> (Scraping and Clunking Large Stone 1.wav by F.M.Audio, CC-BY 4.0). Use: the bite's snap after its 0.8 s tell.
Brief: a heavy jaw snap with grinding chitin (§1.7 bite snap). Why: a massive structure's deep impact with bones crunching, slowed to 0.6, and a large stone scraping.
Edit: 0.90 s, the loudest 100 ms band −18.9 dB, peak −4.3 dBFS, −17.0 LUFS.

### colossus-bite-r44-b — "superhero_earth_impact_hit_07062025" by Artninja + "Bone Crunch/Crack 2 (Mixed)" by EvanBoyerman + "Guterton - Heavy Stone Grind" by guterton.wav

Proposed (round 44).
Sources: <https://freesound.org/people/Artninja/sounds/815336/> (superhero_earth_impact_hit_07062025 by Artninja, CC-BY 4.0), <https://freesound.org/people/EvanBoyerman/sounds/369601/> (Bone Crunch/Crack 2 (Mixed) by EvanBoyerman, CC-BY 3.0), <https://freesound.org/people/guterton.wav/sounds/760672/> (Guterton - Heavy Stone Grind by guterton.wav, CC-BY 4.0). Use: the bite's snap after its 0.8 s tell.
Brief: a heavy jaw snap with grinding chitin (§1.7 bite snap). Why: an earth impact's boom with a bone crack slowed to 0.55 and a heavy stone grind after it.
Edit: 1.00 s, the loudest 100 ms band −20.3 dB, peak −4.2 dBFS, −14.0 LUFS.

### colossus-death-r44-a — "NPX Deep Roar Echo 1.wav" by noahpardo + "S17-28 Large explosion with debris.wav" by craigsmith + "superhero_earth_impact_hit_07062025" by Artninja + "massive explosion" by Hugofski + "rock_avalanche_w_rumbling_sound_01092026" by Artninja

Proposed (round 44).
Sources: <https://freesound.org/people/noahpardo/sounds/345736/> (NPX Deep Roar Echo 1.wav by noahpardo, CC0 1.0), <https://freesound.org/people/craigsmith/sounds/674899/> (S17-28 Large explosion with debris.wav by craigsmith, CC0 1.0), <https://freesound.org/people/Artninja/sounds/815336/> (superhero_earth_impact_hit_07062025 by Artninja, CC-BY 4.0), <https://freesound.org/people/Hugofski/sounds/177556/> (massive explosion by Hugofski, CC0 1.0), <https://freesound.org/people/Artninja/sounds/841540/> (rock_avalanche_w_rumbling_sound_01092026 by Artninja, CC-BY 4.0). Use: the Dust Colossus's death, the `huge` rung of the act boss: chained eruptions over 3 s and the crater collapsing.
Brief: a collapse into the sand, a long subsiding roar under the `huge` rung, dry throughout (§1.7 death collapse). Why: a deep roar shuddering, chained eruptions, then a rock avalanche's rumble subsiding.
Edit: 6.10 s, the loudest 100 ms band −13.3 dB, peak −1.7 dBFS, −14.2 LUFS.

### colossus-death-r44-b — "Deep Monster Roar 2.wav" by MisterSegev + "Deep Boom" by Za-Games + "building_collapse04_enclosed_space.wav" by onteca + "Landslide.wav" by bikesnbassboi + "avl02.wav" by blaukreuz

Proposed (round 44).
Sources: <https://freesound.org/people/MisterSegev/sounds/207746/> (Deep Monster Roar 2.wav by MisterSegev, CC-BY 4.0), <https://freesound.org/people/Za-Games/sounds/539968/> (Deep Boom by Za-Games, CC0 1.0), <https://freesound.org/people/onteca/sounds/197770/> (building_collapse04_enclosed_space.wav by onteca, CC-BY 3.0), <https://freesound.org/people/bikesnbassboi/sounds/543837/> (Landslide.wav by bikesnbassboi, CC-BY 4.0), <https://freesound.org/people/blaukreuz/sounds/162670/> (avl02.wav by blaukreuz, CC0 1.0). Use: the Dust Colossus's death, the `huge` rung of the act boss: chained eruptions over 3 s and the crater collapsing.
Brief: a collapse into the sand, a long subsiding roar under the `huge` rung, dry throughout (§1.7 death collapse). Why: a deep monster roar, a building collapsing and two deep booms over a landslide, rock pouring in.
Edit: 6.00 s, the loudest 100 ms band −15.9 dB, peak −1.7 dBFS, −14.3 LUFS.

### colossus-ridge-r44-a — "Train, Subway, Pass By, Under Bridge Perspective" by Kinoton + "Flowing Sand" by SteveMeli

Proposed (round 44).
Sources: <https://freesound.org/people/Kinoton/sounds/368650/> (Train, Subway, Pass By, Under Bridge Perspective by Kinoton, CC0 1.0), <https://freesound.org/people/SteveMeli/sounds/215006/> (Flowing Sand by SteveMeli, CC-BY 3.0). Use: the ridge pass on Level 21 (t≈64–72): the Colossus's back moving under the sand past the ship, over the basin bed.
Brief: a travelling rumble panned across the field with sand hissing off the ridge, 6–8 s (§1.7 ridge rumble; the `basin-rumble` bed stays under it). Why: a subway train passing under a bridge, sand flowing off the ridge, panned left to right.
Edit: 7.00 s, −20.0 LUFS, the loudest 100 ms band −21.4 dB, peak −5.7 dBFS.

### colossus-ridge-r44-b — "Large, Low Rumble.wav" by duckduckpony + "Guterton - Heavy Stone Grind" by guterton.wav + "sand pour on sand FF662.aif" by martinimeniscus

Proposed (round 44).
Sources: <https://freesound.org/people/duckduckpony/sounds/130972/> (Large, Low Rumble.wav by duckduckpony, CC-BY 4.0), <https://freesound.org/people/guterton.wav/sounds/760672/> (Guterton - Heavy Stone Grind by guterton.wav, CC-BY 4.0), <https://freesound.org/people/martinimeniscus/sounds/199332/> (sand pour on sand FF662.aif by martinimeniscus, CC0 1.0). Use: the ridge pass on Level 21 (t≈64–72): the Colossus's back moving under the sand past the ship, over the basin bed.
Brief: a travelling rumble panned across the field with sand hissing off the ridge, 6–8 s (§1.7 ridge rumble; the `basin-rumble` bed stays under it). Why: a large low rumble passing, a heavy stone grind and sand pouring, panned right to left.
Edit: 7.00 s, −20.0 LUFS, the loudest 100 ms band −24.6 dB, peak −5.6 dBFS.

### walker-lance-charge-r44-a — "charging power" by JavierZumer + "S16-18 Tesla coil sparking and humming.wav" by craigsmith

Proposed (round 44).
Sources: <https://freesound.org/people/JavierZumer/sounds/257229/> (charging power by JavierZumer, CC-BY 4.0), <https://freesound.org/people/craigsmith/sounds/675760/> (S16-18 Tesla coil sparking and humming.wav by craigsmith, CC0 1.0). Use: the Revenant Walker's rail lance (step WR2): its 1 s charge tell, the glowing line across the ship's lane.
Brief: a heavy charge rising for 1 s, nothing synthesized (WR2: a placeholder plays until the pick). Why: a power charge rising, its pitch gliding up, a Tesla coil's sparking swelling under it.
Edit: 1.05 s, the loudest 100 ms band −20.0 dB, peak −10.5 dBFS, −21.2 LUFS.

### walker-lance-charge-r44-b — "high-voltage-discharge-2.wav" by follytowers + "S16-18 Tesla coil sparking and humming.wav" by craigsmith + "Large, Low Rumble.wav" by duckduckpony

Proposed (round 44).
Sources: <https://freesound.org/people/follytowers/sounds/415962/> (high-voltage-discharge-2.wav by follytowers, CC0 1.0), <https://freesound.org/people/craigsmith/sounds/675760/> (S16-18 Tesla coil sparking and humming.wav by craigsmith, CC0 1.0), <https://freesound.org/people/duckduckpony/sounds/130972/> (Large, Low Rumble.wav by duckduckpony, CC-BY 4.0). Use: the Revenant Walker's rail lance (step WR2): its 1 s charge tell, the glowing line across the ship's lane.
Brief: a heavy charge rising for 1 s, nothing synthesized (WR2: a placeholder plays until the pick). Why: a high-voltage discharge's buzz swelling and gliding up, a Tesla coil's sparks on top, a low rumble rising.
Edit: 1.05 s, the loudest 100 ms band −22.7 dB, peak −4.2 dBFS, −16.2 LUFS.

### walker-lance-shot-r44-a — "Railgun - Extremely LOUD Cannon" by qubodup

Proposed (round 44).
Source: <https://freesound.org/people/qubodup/sounds/162260/> (Railgun - Extremely LOUD Cannon by qubodup, CC0 1.0). Use: the rail lance's heavy line shot down the screen after the charge.
Brief: a heavy rail shot, big and heavy (WR2). Why: a real railgun (an extremely loud cannon).
Edit: 1.60 s, the loudest 100 ms band −16.2 dB, peak −6.4 dBFS, −15.8 LUFS.

### walker-lance-shot-r44-b — "shipboard_railgun.mp3" by deleted_user_1941307 + "Taser/High Voltage discharge" by The_Chemical_Workshop

Proposed (round 44).
Sources: <https://freesound.org/people/deleted_user_1941307/sounds/155790/> (shipboard_railgun.mp3 by deleted_user_1941307, CC0 1.0), <https://freesound.org/people/The_Chemical_Workshop/sounds/403252/> (Taser/High Voltage discharge by The_Chemical_Workshop, CC0 1.0). Use: the rail lance's heavy line shot down the screen after the charge.
Brief: a heavy rail shot, big and heavy (WR2). Why: a shipboard railgun's heavy boom with a high-voltage discharge's crack on its attack.
Edit: 1.60 s, the loudest 100 ms band −17.7 dB, peak −4.2 dBFS, −15.0 LUFS.

### walker-shockwave-r44-a — "Giant Walking.wav" by facklere + "Explosion and Shockwave 1" by qubodup

Proposed (round 44).
Sources: <https://freesound.org/people/facklere/sounds/458042/> (Giant Walking.wav by facklere, CC0 1.0), <https://freesound.org/people/qubodup/sounds/855245/> (Explosion and Shockwave 1 by qubodup, CC0 1.0). Use: the walker's stomp shockwave (step WR2): the stomp after its 0.5 s tell and the ring expanding out to 110 px.
Brief: a ground slam and a low boom (WR2). Why: a giant's footfall and an explosion's shockwave rolling out of it.
Edit: 1.60 s, the loudest 100 ms band −18.7 dB, peak −4.1 dBFS, −15.7 LUFS.

### walker-shockwave-r44-b — "Giant step" by DasWaff + "loudthump.wav" by 777hr1 + "Big Structure Collision Heard from Inside" by Osvoldon + "building_collapse04_enclosed_space.wav" by onteca

Proposed (round 44).
Sources: <https://freesound.org/people/DasWaff/sounds/721601/> (Giant step by DasWaff, CC0 1.0), <https://freesound.org/people/777hr1/sounds/663057/> (loudthump.wav by 777hr1, CC-BY 4.0), <https://freesound.org/people/Osvoldon/sounds/653294/> (Big Structure Collision Heard from Inside by Osvoldon, CC0 1.0), <https://freesound.org/people/onteca/sounds/197770/> (building_collapse04_enclosed_space.wav by onteca, CC-BY 3.0). Use: the walker's stomp shockwave (step WR2): the stomp after its 0.5 s tell and the ring expanding out to 110 px.
Brief: a ground slam and a low boom (WR2). Why: a giant's step, a loud thump and a massive structure's deep impact rolling out, rubble shaken loose.
Edit: 1.48 s, the loudest 100 ms band −20.9 dB, peak −4.2 dBFS, −14.3 LUFS.

## Concept round 46, step AUD-J (the Ghost Drone and the Act 3 enemy cues, from the database)

The user, Act 3 playthrough: "Ghost drone sounds are really bad. They sound like pling plong on a xylophone." Every file is cut from **Freesound originals** (CC0 or CC-BY, each in [CREDITS.md](../../../../CREDITS.md)) by `tools/concept/audio/sfx_r46j.py` (cut, slowed or gliding, layered, filtered, faded and levelled; nothing synthesized), big and heavy sources, nothing bubbly or popping, no pitched pings, every blast with a ramped attack. An a/b pair each, proposed (round 46), pending the user's review; the game keeps the old synthesized files until the pick. Levels: each at the level of the file it replaces: one-shots on the loudest 100 ms of the 200 Hz–5 kHz band under a −4 dBFS ceiling with at most 4 dB of limiting (a mix of sharper transients, the clicks and shots, lower), at most −14 LUFS (the tells −16, the Shellback's plates −16/−17, the stomp −15); loops by their band RMS, at most about the replaced loop's loudness (+1 LU) where a loop is heavy below 200 Hz.

### ghost-drone-drop-r46-a — "Jet Flyby 02" by mrspivey

Proposed (round 46).
Source: <https://freesound.org/people/mrspivey/sounds/805169/> (Jet Flyby 02 by mrspivey, CC0 1.0). Use: a Ghost Drone dropping into its lane (Levels 18, 19).
Brief: a turbine/jet whine descending, built not grown, 0.9 s. Why: a jet's turbine whine passing, its speed gliding down from 1.3 to 0.8.
Edit: 0.92 s, the loudest 100 ms band −24.1 dB, peak −11.5 dBFS, −23.8 LUFS.

### ghost-drone-drop-r46-b — "Eurofighter Typhoon Flyby 005 – Close Proximity" by TimoSchmied

Proposed (round 46).
Source: <https://freesound.org/people/TimoSchmied/sounds/640505/> (Eurofighter Typhoon Flyby 005 – Close Proximity by TimoSchmied, CC-BY 4.0). Use: a Ghost Drone dropping into its lane (Levels 18, 19).
Brief: a turbine/jet whine descending, built not grown, 0.9 s. Why: a Eurofighter's close flyby, its speed gliding down from 1.25 to 0.8.
Edit: 0.90 s, the loudest 100 ms band −24.0 dB, peak −8.1 dBFS, −25.3 LUFS.

### ghost-drone-glint-r46-a — "Relay 2" by JoontheFloof

Proposed (round 46).
Source: <https://freesound.org/people/JoontheFloof/sounds/740252/> (Relay 2 by JoontheFloof, CC0 1.0). Use: a Ghost Drone's 0.3 s eye glint before its burst (the tell).
Brief: a short mechanical/electronic lock click, not a pitched ping. Why: an electric relay latching (two dry clicks).
Edit: 0.23 s, the loudest 100 ms band −31.7 dB, peak −4.1 dBFS.

### ghost-drone-glint-r46-b — "Canon 550D Shutter" by iainmccurdy + "M&P15 Bolt Release [No Ring]" by smill.and.welson

Proposed (round 46).
Sources: <https://freesound.org/people/iainmccurdy/sounds/613884/> (Canon 550D Shutter by iainmccurdy, CC-BY 4.0), <https://freesound.org/people/smill.and.welson/sounds/858506/> (M&P15 Bolt Release [No Ring] by smill.and.welson, CC-BY 4.0). Use: a Ghost Drone's 0.3 s eye glint before its burst (the tell).
Brief: a short mechanical/electronic lock click, not a pitched ping. Why: a camera shutter's two clicks with a rifle's bolt release under them.
Edit: 0.30 s, the loudest 100 ms band −26.3 dB, peak −4.3 dBFS.

### ghost-drone-burst-r46-a — "M16 gun 3 round burst" by lensflare8642

Proposed (round 46).
Source: <https://freesound.org/people/lensflare8642/sounds/145206/> (M16 gun 3 round burst by lensflare8642, CC0 1.0). Use: a Ghost Drone's burst (shots 0.15 s apart), once at its first shot.
Brief: three dry precise human-gun shots. Why: an M16's three-round burst (shots 0.14 s apart).
Edit: 0.60 s, the loudest 100 ms band −21.0 dB, peak −5.5 dBFS, −18.7 LUFS.

### ghost-drone-burst-r46-b — "Rifle Burst" by qubodup

Proposed (round 46).
Source: <https://freesound.org/people/qubodup/sounds/482120/> (Rifle Burst by qubodup, CC0 1.0). Use: a Ghost Drone's burst (shots 0.15 s apart), once at its first shot.
Brief: three dry precise human-gun shots. Why: an automatic rifle's three-shot burst (shots 0.1 s apart).
Edit: 0.29 s, the loudest 100 ms band −23.6 dB, peak −4.3 dBFS.

### ghost-drone-death-r46-a — "Explosion 1 (Burning Car rec by Ñado)" by qubodup + "Metal_heavy_punch_with_crunch_and_glass_debris_sounds_08242024" by Artninja + "Electric Sparks" by mikiko850 + "Parts falling" by tlwmdbt

Proposed (round 46).
Sources: <https://freesound.org/people/qubodup/sounds/843247/> (Explosion 1 (Burning Car rec by Ñado) by qubodup, CC0 1.0), <https://freesound.org/people/Artninja/sounds/752072/> (Metal_heavy_punch_with_crunch_and_glass_debris_sounds_08242024 by Artninja, CC-BY 4.0), <https://freesound.org/people/mikiko850/sounds/849772/> (Electric Sparks by mikiko850, CC-BY 4.0), <https://freesound.org/people/tlwmdbt/sounds/165860/> (Parts falling by tlwmdbt, CC-BY 4.0). Use: a Ghost Drone's death, the `small` rung.
Brief: a small machine breaking up: crack, metal, sparks, no fireball. Why: a dry bang and a heavy metal crunch, a burst of sparks and parts falling.
Edit: 0.90 s, the loudest 100 ms band −19.1 dB, peak −5.7 dBFS, −20.8 LUFS.

### ghost-drone-death-r46-b — "Metal Crash Collision" by qubodup + "Quadcopter Drone Collision Hitting Wall and Falling" by qubodup + "QUICK_SMASH_DEBRIS_001.wav" by JoelAudio + "Electric Sparks" by mikiko850

Proposed (round 46).
Sources: <https://freesound.org/people/qubodup/sounds/67617/> (Metal Crash Collision by qubodup, CC0 1.0), <https://freesound.org/people/qubodup/sounds/854351/> (Quadcopter Drone Collision Hitting Wall and Falling by qubodup, CC0 1.0), <https://freesound.org/people/JoelAudio/sounds/135459/> (QUICK_SMASH_DEBRIS_001.wav by JoelAudio, CC-BY 4.0), <https://freesound.org/people/mikiko850/sounds/849772/> (Electric Sparks by mikiko850, CC-BY 4.0). Use: a Ghost Drone's death, the `small` rung.
Brief: a small machine breaking up: crack, metal, sparks, no fireball. Why: a metal crash, a real quadcopter hitting a wall and falling, smashed debris and sparks.
Edit: 0.87 s, the loudest 100 ms band −19.2 dB, peak −6.9 dBFS, −18.6 LUFS.

### devil-spin-r46-a — "Inside a Tornado.wav" by RogerBoyX69 + "G56-22-Sandstorm.wav" by craigsmith

Proposed (round 46).
Sources: <https://freesound.org/people/RogerBoyX69/sounds/338721/> (Inside a Tornado.wav by RogerBoyX69, CC-BY 4.0), <https://freesound.org/people/craigsmith/sounds/438877/> (G56-22-Sandstorm.wav by craigsmith, CC0 1.0). Use: the Dust Devil's spin, one loop for the nearest (Levels 18, 20, 21).
Brief: a whirling wind roar with grit. Why: inside a tornado (a steady whirling roar) with a sandstorm's grit.
Edit: a seamless 3 s loop, band RMS −32.1 dB, peak −16.1 dBFS, −26.7 LUFS.

### devil-spin-r46-b — "JM_NATURAL ELEMENTS_Wind 01 - Tornado - Inside a metalic squeaking structure" by Julien_Matthey + "Flowing Sand" by SteveMeli

Proposed (round 46).
Sources: <https://freesound.org/people/Julien_Matthey/sounds/557188/> (JM_NATURAL ELEMENTS_Wind 01 - Tornado - Inside a metalic squeaking structure by Julien_Matthey, CC0 1.0), <https://freesound.org/people/SteveMeli/sounds/215006/> (Flowing Sand by SteveMeli, CC-BY 3.0). Use: the Dust Devil's spin, one loop for the nearest (Levels 18, 20, 21).
Brief: a whirling wind roar with grit. Why: a tornado through a squeaking metal structure, sand flowing.
Edit: a seamless 3 s loop, band RMS −32.0 dB, peak −4.0 dBFS, −23.5 LUFS.

### devil-open-r46-a — "Instant Wind" by Raclure + "Heavy stone door opens" by PostProdDog

Proposed (round 46).
Sources: <https://freesound.org/people/Raclure/sounds/483599/> (Instant Wind by Raclure, CC0 1.0), <https://freesound.org/people/PostProdDog/sounds/578490/> (Heavy stone door opens by PostProdDog, CC0 1.0). Use: a Dust Devil's 0.4 s rise and opening (the tell).
Brief: the vanes parting with a gust and a low hollow tone. Why: a gust swelling in over a heavy stone door grinding open (its hollow resonance).
Edit: 0.70 s, the loudest 100 ms band −21.3 dB, peak −4.2 dBFS, −16.0 LUFS.

### devil-open-r46-b — "Powerfull Whooshes 2.wav" by Nic3_one + "stone_on_stone.aif" by thanvannispen

Proposed (round 46).
Sources: <https://freesound.org/people/Nic3_one/sounds/434869/> (Powerfull Whooshes 2.wav by Nic3_one, CC0 1.0), <https://freesound.org/people/thanvannispen/sounds/29986/> (stone_on_stone.aif by thanvannispen, CC-BY 4.0). Use: a Dust Devil's 0.4 s rise and opening (the tell).
Brief: the vanes parting with a gust and a low hollow tone. Why: a powerful whoosh with stone dragging on stone.
Edit: 0.70 s, the loudest 100 ms band −20.4 dB, peak −4.0 dBFS, −19.6 LUFS.

### devil-spiral-r46-a — "OsVaDuThDustThrowThud.wav" by The_Sean_ + "Sand slidding out of shovel slowly.wav" by XfiXy8

Proposed (round 46).
Sources: <https://freesound.org/people/The_Sean_/sounds/340307/> (OsVaDuThDustThrowThud.wav by The_Sean_, CC0 1.0), <https://freesound.org/people/XfiXy8/sounds/467301/> (Sand slidding out of shovel slowly.wav by XfiXy8, CC0 1.0). Use: a Dust Devil's grit spiral volleys.
Brief: a short turning grit hiss, 0.45 s. Why: a throw of dust with sand sliding off a shovel.
Edit: 0.47 s, the loudest 100 ms band −22.2 dB, peak −4.0 dBFS, −18.6 LUFS.

### devil-spiral-r46-b — "Big Swoosh" by Electroviolence + "sand pour on sand FF662.aif" by martinimeniscus

Proposed (round 46).
Sources: <https://freesound.org/people/Electroviolence/sounds/234547/> (Big Swoosh by Electroviolence, CC0 1.0), <https://freesound.org/people/martinimeniscus/sounds/199332/> (sand pour on sand FF662.aif by martinimeniscus, CC0 1.0). Use: a Dust Devil's grit spiral volleys.
Brief: a short turning grit hiss, 0.45 s. Why: a big swoosh with sand pouring.
Edit: 0.45 s, the loudest 100 ms band −22.2 dB, peak −8.1 dBFS, −20.1 LUFS.

### shellback-walk-r46-a — "Dinosaur Footsteps-01.wav" by jamesrodavidson + "stonehit1.wav" by aerror

Proposed (round 46).
Sources: <https://freesound.org/people/jamesrodavidson/sounds/192365/> (Dinosaur Footsteps-01.wav by jamesrodavidson, CC-BY 3.0), <https://freesound.org/people/aerror/sounds/350750/> (stonehit1.wav by aerror, CC0 1.0). Use: the Shellback's walk, one loop for the nearest (Levels 17, 19, 21).
Brief: heavy padded footfalls with shell plates clacking. Why: a big creature's footsteps (two to a loop) with stones clacking.
Edit: a seamless 3.26 s loop, band RMS −43.5 dB, peak −8.5 dBFS, −22.0 LUFS.

### shellback-walk-r46-b — "Monster Stomp Footsteps Sequence" by Vilkas_Sound + "stone_on_stone.aif" by thanvannispen

Proposed (round 46).
Sources: <https://freesound.org/people/Vilkas_Sound/sounds/753178/> (Monster Stomp Footsteps Sequence by Vilkas_Sound, CC-BY 4.0), <https://freesound.org/people/thanvannispen/sounds/29986/> (stone_on_stone.aif by thanvannispen, CC-BY 4.0). Use: the Shellback's walk, one loop for the nearest (Levels 17, 19, 21).
Brief: heavy padded footfalls with shell plates clacking. Why: a monster's heavy stomps (two to a loop) with stone knocking on stone.
Edit: a seamless 4.16 s loop, band RMS −34.2 dB, peak −6.2 dBFS, −22.0 LUFS.

### shellback-lob-r46-a — "M224 Mortar Firing Close 02" by Nanashi + "Pressure Hiss" by magnuswaker

Proposed (round 46).
Sources: <https://freesound.org/people/Nanashi/sounds/426162/> (M224 Mortar Firing Close 02 by Nanashi, CC-BY 4.0), <https://freesound.org/people/magnuswaker/sounds/581083/> (Pressure Hiss by magnuswaker, CC0 1.0). Use: a Shellback's mortar lob.
Brief: a dull cough thump and a pressure hiss. Why: a real M224 mortar firing, slowed to 0.85, with a pressure hiss.
Edit: 0.62 s, the loudest 100 ms band −17.2 dB, peak −5.8 dBFS, −17.0 LUFS.

### shellback-lob-r46-b — "120mm Mortar Shell Shot 2" by qubodup + "Pressure Hiss" by magnuswaker

Proposed (round 46).
Sources: <https://freesound.org/people/qubodup/sounds/854478/> (120mm Mortar Shell Shot 2 by qubodup, CC0 1.0), <https://freesound.org/people/magnuswaker/sounds/581083/> (Pressure Hiss by magnuswaker, CC0 1.0). Use: a Shellback's mortar lob.
Brief: a dull cough thump and a pressure hiss. Why: a 120 mm mortar shot, slowed to 0.8, with a pressure hiss.
Edit: 0.64 s, the loudest 100 ms band −20.8 dB, peak −6.1 dBFS, −16.2 LUFS.

### shellback-curl-r46-a — "stone_on_stone_dragging3.aif" by thanvannispen + "stonehit1.wav" by aerror

Proposed (round 46).
Sources: <https://freesound.org/people/thanvannispen/sounds/29989/> (stone_on_stone_dragging3.aif by thanvannispen, CC-BY 4.0), <https://freesound.org/people/aerror/sounds/350750/> (stonehit1.wav by aerror, CC0 1.0). Use: a Shellback curling up.
Brief: plates grinding shut, a heavy clack. Why: stone dragging on stone, closing on a heavy stone clack.
Edit: 0.80 s, the loudest 100 ms band −20.0 dB, peak −5.2 dBFS, −19.3 LUFS.

### shellback-curl-r46-b — "Guterton - Heavy Stone Grind" by guterton.wav + "stone_on_stone.aif" by thanvannispen

Proposed (round 46).
Sources: <https://freesound.org/people/guterton.wav/sounds/760672/> (Guterton - Heavy Stone Grind by guterton.wav, CC-BY 4.0), <https://freesound.org/people/thanvannispen/sounds/29986/> (stone_on_stone.aif by thanvannispen, CC-BY 4.0). Use: a Shellback curling up.
Brief: plates grinding shut, a heavy clack. Why: a heavy stone grind, closing on a stone-on-stone knock.
Edit: 0.85 s, the loudest 100 ms band −20.4 dB, peak −4.3 dBFS, −16.4 LUFS.

### shellback-roll-r46-a — "Rolling boulder" by gootie3333333 + "Gravel Impacts and Falls.wav" by lolamadeus

Proposed (round 46).
Sources: <https://freesound.org/people/gootie3333333/sounds/610739/> (Rolling boulder by gootie3333333, CC-BY 4.0), <https://freesound.org/people/lolamadeus/sounds/179341/> (Gravel Impacts and Falls.wav by lolamadeus, CC0 1.0). Use: a Shellback's roll, one loop for the nearest rolling one.
Brief: a rumbling grind that crushes. Why: a boulder rolling with gravel falling.
Edit: a seamless 2.5 s loop, band RMS −30.0 dB, peak −7.0 dBFS, −23.3 LUFS.

### shellback-roll-r46-b — "Large, Low Rumble.wav" by duckduckpony + "stone_on_stone_dragging3.aif" by thanvannispen

Proposed (round 46).
Sources: <https://freesound.org/people/duckduckpony/sounds/130972/> (Large, Low Rumble.wav by duckduckpony, CC-BY 4.0), <https://freesound.org/people/thanvannispen/sounds/29989/> (stone_on_stone_dragging3.aif by thanvannispen, CC-BY 4.0). Use: a Shellback's roll, one loop for the nearest rolling one.
Brief: a rumbling grind that crushes. Why: a large low rumble with stone dragging on stone.
Edit: a seamless 2.5 s loop, band RMS −31.1 dB, peak −5.7 dBFS, −19.0 LUFS.

### shellback-uncurl-r46-a — "stone_on_stone_dragging3.aif" by thanvannispen + "stonehit1.wav" by aerror + "sand pour on sand FF662.aif" by martinimeniscus

Proposed (round 46).
Sources: <https://freesound.org/people/thanvannispen/sounds/29989/> (stone_on_stone_dragging3.aif by thanvannispen, CC-BY 4.0), <https://freesound.org/people/aerror/sounds/350750/> (stonehit1.wav by aerror, CC0 1.0), <https://freesound.org/people/martinimeniscus/sounds/199332/> (sand pour on sand FF662.aif by martinimeniscus, CC0 1.0). Use: a Shellback uncurling.
Brief: the plates opening, a looser clack, a short exhale of grit. Why: stone dragging open, a loose stone clack and sand pouring off.
Edit: 0.70 s, the loudest 100 ms band −22.0 dB, peak −7.2 dBFS, −21.5 LUFS.

### shellback-uncurl-r46-b — "Heavy stone door opens" by PostProdDog + "stonehit1.wav" by aerror + "Sand slidding out of shovel slowly.wav" by XfiXy8

Proposed (round 46).
Sources: <https://freesound.org/people/PostProdDog/sounds/578490/> (Heavy stone door opens by PostProdDog, CC0 1.0), <https://freesound.org/people/aerror/sounds/350750/> (stonehit1.wav by aerror, CC0 1.0), <https://freesound.org/people/XfiXy8/sounds/467301/> (Sand slidding out of shovel slowly.wav by XfiXy8, CC0 1.0). Use: a Shellback uncurling.
Brief: the plates opening, a looser clack, a short exhale of grit. Why: a heavy stone door grinding open, a stone clack and sand sliding off.
Edit: 0.70 s, the loudest 100 ms band −24.4 dB, peak −7.0 dBFS, −16.5 LUFS.

### crawler-walk-r46-a — "bug legs tapping" by c64cosmin + "Gravel Impacts and Falls.wav" by lolamadeus

Proposed (round 46).
Sources: <https://freesound.org/people/c64cosmin/sounds/828623/> (bug legs tapping by c64cosmin, CC0 1.0), <https://freesound.org/people/lolamadeus/sounds/179341/> (Gravel Impacts and Falls.wav by lolamadeus, CC0 1.0). Use: the Threadcrawler's walk, one loop for the nearest (Level 16).
Brief: a dry many-legged skitter on rock. Why: fingers tapping like bug legs (a dense skitter) over gravel.
Edit: a seamless 3 s loop, band RMS −36.0 dB, peak −8.8 dBFS, −32.6 LUFS.

### crawler-walk-r46-b — "crab walking" by stuniverso + "bug legs tapping" by c64cosmin

Proposed (round 46).
Sources: <https://freesound.org/people/stuniverso/sounds/761559/> (crab walking by stuniverso, CC0 1.0), <https://freesound.org/people/c64cosmin/sounds/828623/> (bug legs tapping by c64cosmin, CC0 1.0). Use: the Threadcrawler's walk, one loop for the nearest (Level 16).
Brief: a dry many-legged skitter on rock. Why: a crab walking, slowed to 0.85, over the tapping legs.
Edit: a seamless 3 s loop, band RMS −36.1 dB, peak −9.3 dBFS, −30.5 LUFS.

### tendril-hum-r46-a — "hum electric transformer in large room1.flac" by kyles + "765 kilo-volt transmission lines buzzing and crackling" by johnaudiotech

Proposed (round 46).
Sources: <https://freesound.org/people/kyles/sounds/453444/> (hum electric transformer in large room1.flac by kyles, CC0 1.0), <https://freesound.org/people/johnaudiotech/sounds/347046/> (765 kilo-volt transmission lines buzzing and crackling by johnaudiotech, CC0 1.0). Use: a standing tendril barrier's hum, one loop for the nearest (L16, L06).
Brief: a taut electric-organic drone, no wobble. Why: an electric transformer's hum in a large room with high-voltage lines buzzing.
Edit: a seamless 6 s loop, band RMS −38.0 dB, peak −23.9 dBFS, −33.7 LUFS.

### tendril-hum-r46-b — "Electric_Pylon_Power Line-Buzz 01.wav" by omnisounddesign + "765 kilo-volt transmission lines buzzing and crackling" by johnaudiotech

Proposed (round 46).
Sources: <https://freesound.org/people/omnisounddesign/sounds/338074/> (Electric_Pylon_Power Line-Buzz 01.wav by omnisounddesign, CC-BY 4.0), <https://freesound.org/people/johnaudiotech/sounds/347046/> (765 kilo-volt transmission lines buzzing and crackling by johnaudiotech, CC0 1.0). Use: a standing tendril barrier's hum, one loop for the nearest (L16, L06).
Brief: a taut electric-organic drone, no wobble. Why: a power pylon's buzz with high-voltage lines crackling.
Edit: a seamless 6 s loop, band RMS −38.3 dB, peak −16.4 dBFS, −27.8 LUFS.

### tendril-burn-r46-a — "Welding_2_long_take.wav" by ivolipa

Proposed (round 46).
Source: <https://freesound.org/people/ivolipa/sounds/385992/> (Welding_2_long_take.wav by ivolipa, CC0 1.0). Use: a beam burning a tendril, a loop while it burns.
Brief: a searing crackle. Why: an electric welder's crackle.
Edit: a seamless 2 s loop, band RMS −34.2 dB, peak −3.7 dBFS, −22.6 LUFS.

### tendril-burn-r46-b — "Electric welding with tig - Full cycle" by ldezem + "Electric Sparks" by mikiko850

Proposed (round 46).
Sources: <https://freesound.org/people/ldezem/sounds/386386/> (Electric welding with tig - Full cycle by ldezem, CC0 1.0), <https://freesound.org/people/mikiko850/sounds/849772/> (Electric Sparks by mikiko850, CC-BY 4.0). Use: a beam burning a tendril, a loop while it burns.
Brief: a searing crackle. Why: a TIG welder's hiss with electric sparks.
Edit: a seamless 2 s loop, band RMS −34.1 dB, peak −14.0 dBFS, −27.3 LUFS.

### tendril-break-r46-a — "Whip Crack 01.wav" by CGEffex + "Wind-down 1.aif" by timgormly

Proposed (round 46).
Sources: <https://freesound.org/people/CGEffex/sounds/93100/> (Whip Crack 01.wav by CGEffex, CC-BY 4.0), <https://freesound.org/people/timgormly/sounds/151288/> (Wind-down 1.aif by timgormly, CC0 1.0). Use: a tendril breaking.
Brief: a whip-crack snap and a falling whine. Why: a whip crack and a motor winding down, its speed gliding from 1.4 to 0.6.
Edit: 0.90 s, the loudest 100 ms band −18.9 dB, peak −4.1 dBFS, −16.4 LUFS.

### tendril-break-r46-b — "24_SeRompeCable.wav" by Lextao + "Whip Crack.m4a" by SciFiSounds + "FAN 3 (Slow Stop).wav" by temawas

Proposed (round 46).
Sources: <https://freesound.org/people/Lextao/sounds/471828/> (24_SeRompeCable.wav by Lextao, CC-BY 4.0), <https://freesound.org/people/SciFiSounds/sounds/529925/> (Whip Crack.m4a by SciFiSounds, CC0 1.0), <https://freesound.org/people/temawas/sounds/179245/> (FAN 3 (Slow Stop).wav by temawas, CC-BY 4.0). Use: a tendril breaking.
Brief: a whip-crack snap and a falling whine. Why: a cable snapping with a whip crack, a fan slowing to a stop, its speed gliding from 2 to 0.8.
Edit: 0.92 s, the loudest 100 ms band −18.1 dB, peak −4.1 dBFS, −15.2 LUFS.

### burrow-dive-r46-a — "Sand slidding out of shovel slowly.wav" by XfiXy8

Proposed (round 46).
Source: <https://freesound.org/people/XfiXy8/sounds/467301/> (Sand slidding out of shovel slowly.wav by XfiXy8, CC0 1.0). Use: a Burrower's 0.4 s dive (Levels 15-17, 20, 21).
Brief: a falling sand hiss, short. Why: sand sliding off a shovel, its speed gliding down from 1 to 0.75.
Edit: 0.55 s, the loudest 100 ms band −26.2 dB, peak −13.9 dBFS, −22.0 LUFS.

### burrow-dive-r46-b — "sand slide on metal FF678.aif" by martinimeniscus + "sand pour on sand FF662.aif" by martinimeniscus

Proposed (round 46).
Sources: <https://freesound.org/people/martinimeniscus/sounds/199397/> (sand slide on metal FF678.aif by martinimeniscus, CC0 1.0), <https://freesound.org/people/martinimeniscus/sounds/199332/> (sand pour on sand FF662.aif by martinimeniscus, CC0 1.0). Use: a Burrower's 0.4 s dive (Levels 15-17, 20, 21).
Brief: a falling sand hiss, short. Why: sand sliding on metal, gliding down, with sand pouring.
Edit: 0.55 s, the loudest 100 ms band −26.3 dB, peak −6.7 dBFS, −18.9 LUFS.

### herald-song-r46-a — "Short Choir" by Breviceps

Proposed (round 46).
Source: <https://freesound.org/people/Breviceps/sounds/444491/> (Short Choir by Breviceps, CC0 1.0). Use: the Choir Herald's song with its ring of bullets (L17, L19, L21).
Brief: a short sung phrase in the Choir's colour (not words). Why: a short choir's 'aah', slowed to 0.85 (darker).
Edit: 1.90 s, the loudest 100 ms band −19.9 dB, peak −11.6 dBFS, −20.7 LUFS.

### herald-song-r46-b — "Choir.aif" by DiArchangeli + "Kargyraa Throat Chant/Singing (Lower Register Vocal Folds)" by Hjaarlmarch

Proposed (round 46).
Sources: <https://freesound.org/people/DiArchangeli/sounds/458144/> (Choir.aif by DiArchangeli, CC0 1.0), <https://freesound.org/people/Hjaarlmarch/sounds/732443/> (Kargyraa Throat Chant/Singing (Lower Register Vocal Folds) by Hjaarlmarch, CC0 1.0). Use: the Choir Herald's song with its ring of bullets (L17, L19, L21).
Brief: a short sung phrase in the Choir's colour (not words). Why: a choir's chord changing, a low throat chant under it.
Edit: 1.90 s, the loudest 100 ms band −20.0 dB, peak −6.8 dBFS, −16.4 LUFS.

### herald-aura-r46-a — "26-07-12 - Ethereal Choir Drone - ambient pad" by Valerie-Vivegnis

Proposed (round 46).
Source: <https://freesound.org/people/Valerie-Vivegnis/sounds/862321/> (26-07-12 - Ethereal Choir Drone - ambient pad by Valerie-Vivegnis, CC-BY 4.0). Use: the Herald's aura, one loop while any ring is up.
Brief: a soft high shimmer. Why: an ethereal choir drone, high-passed at 1.5 kHz.
Edit: a seamless 4 s loop, band RMS −40.2 dB, peak −21.8 dBFS, −31.8 LUFS.

### herald-aura-r46-b — "Choir.aif" by DiArchangeli

Proposed (round 46).
Source: <https://freesound.org/people/DiArchangeli/sounds/458144/> (Choir.aif by DiArchangeli, CC0 1.0). Use: the Herald's aura, one loop while any ring is up.
Brief: a soft high shimmer. Why: a choir's held chord, high-passed at 1.5 kHz.
Edit: a seamless 2.6 s loop, band RMS −40.1 dB, peak −24.1 dBFS, −33.0 LUFS. Re-cut after the pick (step HAF): the first cut (6.21 s for 4 s) ran into the chord's decay and dipped to about −59 dB at the seam every 4 s; the chord holds steady from 6.0 to about 9.25 s, so the loop now stays inside it (seam 100 ms RMS within 1 dB of the body, no click).

### aura-absorb-r46-a — "Electric Sparks" by mikiko850 + "Metal_heavy_punch_with_crunch_and_glass_debris_sounds_08242024" by Artninja

Proposed (round 46).
Sources: <https://freesound.org/people/mikiko850/sounds/849772/> (Electric Sparks by mikiko850, CC-BY 4.0), <https://freesound.org/people/Artninja/sounds/752072/> (Metal_heavy_punch_with_crunch_and_glass_debris_sounds_08242024 by Artninja, CC-BY 4.0). Use: a ring absorbing a hit, spaced out.
Brief: the enemy-shield hit, no glassy ping. Why: a burst of electric sparks over a muffled metal punch.
Edit: 0.35 s, the loudest 100 ms band −22.0 dB, peak −4.1 dBFS.

### aura-absorb-r46-b — "Force Field 01.wav" by LilMati + "Electric Sparks" by mikiko850 + "Metal_heavy_punch_with_crunch_and_glass_debris_sounds_08242024" by Artninja

Proposed (round 46).
Sources: <https://freesound.org/people/LilMati/sounds/702771/> (Force Field 01.wav by LilMati, CC0 1.0), <https://freesound.org/people/mikiko850/sounds/849772/> (Electric Sparks by mikiko850, CC-BY 4.0), <https://freesound.org/people/Artninja/sounds/752072/> (Metal_heavy_punch_with_crunch_and_glass_debris_sounds_08242024 by Artninja, CC-BY 4.0). Use: a ring absorbing a hit, spaced out.
Brief: the enemy-shield hit, no glassy ping. Why: a force field's hit with a crackle of sparks over a muffled punch.
Edit: 0.40 s, the loudest 100 ms band −22.1 dB, peak −6.7 dBFS.

### walker-stomp-r46-a — "Big robot footstep 002" by AudioPapkin + "Hydraulic Hiss, TTC bus door" by OttisG

Proposed (round 46).
Sources: <https://freesound.org/people/AudioPapkin/sounds/813053/> (Big robot footstep 002 by AudioPapkin, CC0 1.0), <https://freesound.org/people/OttisG/sounds/852991/> (Hydraulic Hiss, TTC bus door by OttisG, CC-BY 4.0). Use: a Revenant Walker's footfall (Level 19).
Brief: a dull heavy stomp, a leg clank and a hydraulic hiss. Why: a big robot's footstep with a bus door's hydraulic hiss.
Edit: 1.00 s, the loudest 100 ms band −19.6 dB, peak −4.1 dBFS, −14.7 LUFS.

### walker-stomp-r46-b — "mech_step_002.ogg" by pgi + "Pressure Hiss" by magnuswaker

Proposed (round 46).
Sources: <https://freesound.org/people/pgi/sounds/213041/> (mech_step_002.ogg by pgi, CC0 1.0), <https://freesound.org/people/magnuswaker/sounds/581083/> (Pressure Hiss by magnuswaker, CC0 1.0). Use: a Revenant Walker's footfall (Level 19).
Brief: a dull heavy stomp, a leg clank and a hydraulic hiss. Why: a mech's step and its servo, a pressure hiss.
Edit: 1.00 s, the loudest 100 ms band −22.5 dB, peak −4.2 dBFS, −15.6 LUFS.

### tank-death-r46-a — "Explosive 1 v2 [DOD 130303].flac" by qubodup + "R29-28-Large Metal Vehicle Crashes.wav" by craigsmith + "Flame Burst" by magnuswaker

Proposed (round 46).
Sources: <https://freesound.org/people/qubodup/sounds/182431/> (Explosive 1 v2 [DOD 130303].flac by qubodup, CC0 1.0), <https://freesound.org/people/craigsmith/sounds/487143/> (R29-28-Large Metal Vehicle Crashes.wav by craigsmith, CC0 1.0), <https://freesound.org/people/magnuswaker/sounds/592572/> (Flame Burst by magnuswaker, CC0 1.0). Use: a Warden Tank's death, the `medium` rung (Levels 08, 19).
Brief: a hull explosion with metal debris. Why: a real military explosive, a large metal vehicle crashing and a flame burst.
Edit: 1.80 s, the loudest 100 ms band −17.2 dB, peak −4.1 dBFS, −19.5 LUFS.

### tank-death-r46-b — "S17-28 Large explosion with debris.wav" by craigsmith + "Metal Crash Collision" by qubodup + "Fire - Effects - Swooshes - Bursts, evolving" by GregorQuendel + "R29-28-Large Metal Vehicle Crashes.wav" by craigsmith

Proposed (round 46).
Sources: <https://freesound.org/people/craigsmith/sounds/674899/> (S17-28 Large explosion with debris.wav by craigsmith, CC0 1.0), <https://freesound.org/people/qubodup/sounds/67617/> (Metal Crash Collision by qubodup, CC0 1.0), <https://freesound.org/people/GregorQuendel/sounds/421879/> (Fire - Effects - Swooshes - Bursts, evolving by GregorQuendel, CC-BY 4.0), <https://freesound.org/people/craigsmith/sounds/487143/> (R29-28-Large Metal Vehicle Crashes.wav by craigsmith, CC0 1.0). Use: a Warden Tank's death, the `medium` rung (Levels 08, 19).
Brief: a hull explosion with metal debris. Why: a large explosion with debris, a metal crash, fire roaring out, metal wreckage.
Edit: 1.80 s, the loudest 100 ms band −17.1 dB, peak −4.5 dBFS, −15.7 LUFS.

## Concept round 46, step AUD-K (the remaining synthesized beds, rare sounds and player gear, from the database)

The user (round 42): "You should really stop trying to make these sounds yourself. Use the database." Every file is cut from **Freesound originals** (CC0 or CC-BY, each in [CREDITS.md](../../../../CREDITS.md)) by `tools/concept/audio/sfx_r46k.py` (cut, slowed, sped or gliding, layered, filtered, faded and levelled; nothing synthesized), real recordings wherever the database has them, nothing bubbly or popping, no bell chimes. An a/b pair each, proposed (round 46), pending the user's review; the game keeps the old files until the pick. Levels: each sound like the one it replaces (its loudest 100 ms of the 200 Hz–5 kHz band under a −4 dBFS ceiling, at most a little over its loudness), the beds like the accepted beds (`mars-dust`'s −36 dB band RMS, at most `basin-rumble`'s −28 LUFS; the canopy 3 dB under them, as before), the loops (drain, beam impact) by their band RMS. Freesound's download limit (500 a day) ran out during the step, so the foundry, the sonde's b, the drone rebuild and the launch rail use originals already in the cache.

### canopy-hum-r46-a — "Dark Voice Drone 02 - Loop" by kkenny101 + "room tone large room church basement heavy deep ventilation or furnace hum quiet and clicking that picks up in middle.flac" by kyles

Proposed (round 46).
Sources: <https://freesound.org/people/kkenny101/sounds/872936/> (Dark Voice Drone 02 - Loop by kkenny101, CC0 1.0), <https://freesound.org/people/kyles/sounds/451923/> (room tone large room church basement heavy deep ventilation or furnace hum quiet and clicking that picks up in middle.flac by kyles, CC0 1.0). Use: Level 14 (`Sfx.AMBIENCE_CANOPY`): the canopy's bed over the megacity while the canopy lives.
Brief: replace the synthesized sound with a real recording, big and heavy, nothing bubbly. Why: a deep time-stretched voice drone (the living canopy), slowed to 0.85, over a large basement's deep furnace hum.
Edit: seamless 16.00 s loop, band RMS −47.6 dB, peak −20.7 dBFS, −31.0 LUFS.

### canopy-hum-r46-b — "Tuuli sähkölangoissa / Wind humming and howling in the electric wires, power line" by YleArkisto + "hum electric transformer in large room1.flac" by kyles

Proposed (round 46).
Sources: <https://freesound.org/people/YleArkisto/sounds/362914/> (Tuuli sähkölangoissa / Wind humming and howling in the electric wires, power line by YleArkisto, CC-BY 4.0), <https://freesound.org/people/kyles/sounds/453444/> (hum electric transformer in large room1.flac by kyles, CC0 1.0). Use: Level 14 (`Sfx.AMBIENCE_CANOPY`): the canopy's bed over the megacity while the canopy lives.
Brief: replace the synthesized sound with a real recording, big and heavy, nothing bubbly. Why: wind humming and howling in power lines, slowed to 0.7, over a transformer's hum in a large room.
Edit: seamless 16.00 s loop, band RMS −39.0 dB, peak −18.3 dBFS, −31.2 LUFS.

### mars-canyon-r46-a — "Low wind in a desert canyon 2" by fran_marenco + "G55-12-Moaning Whistle Wind.wav" by craigsmith

Proposed (round 46).
Sources: <https://freesound.org/people/fran_marenco/sounds/852881/> (Low wind in a desert canyon 2 by fran_marenco, CC0 1.0), <https://freesound.org/people/craigsmith/sounds/438866/> (G55-12-Moaning Whistle Wind.wav by craigsmith, CC0 1.0). Use: Level 16 (`mars-canyon`): the canyon wind and its howl.
Brief: replace the synthesized sound with a real recording, big and heavy, nothing bubbly. Why: a low wind in a desert canyon with a moaning whistling wind howling through it.
Edit: seamless 16.00 s loop, band RMS −36.1 dB, peak −13.7 dBFS, −29.1 LUFS.

### mars-canyon-r46-b — "howling wind.WAV" by inchadney + "G56-22-Sandstorm.wav" by craigsmith

Proposed (round 46).
Sources: <https://freesound.org/people/inchadney/sounds/157204/> (howling wind.WAV by inchadney, CC-BY 4.0), <https://freesound.org/people/craigsmith/sounds/438877/> (G56-22-Sandstorm.wav by craigsmith, CC0 1.0). Use: Level 16 (`mars-canyon`): the canyon wind and its howl.
Brief: replace the synthesized sound with a real recording, big and heavy, nothing bubbly. Why: a howling wind over a sandstorm's grit.
Edit: seamless 16.00 s loop, band RMS −36.2 dB, peak −19.3 dBFS, −30.8 LUFS.

### mars-colony-r46-a — "Pink Lakes Wind Atmos #2" by kangaroovindaloo + "Arena Underground Ventilation Hum and Rumble" by jamesdrake89 + "freight train distant short and clicky.flac" by kyles

Proposed (round 46).
Sources: <https://freesound.org/people/kangaroovindaloo/sounds/266656/> (Pink Lakes Wind Atmos #2 by kangaroovindaloo, CC0 1.0), <https://freesound.org/people/jamesdrake89/sounds/671456/> (Arena Underground Ventilation Hum and Rumble by jamesdrake89, CC0 1.0), <https://freesound.org/people/kyles/sounds/451145/> (freight train distant short and clicky.flac by kyles, CC0 1.0). Use: Level 17 (`mars-colony`): the plains wind, the domes' air plant, a distant rail.
Brief: replace the synthesized sound with a real recording, big and heavy, nothing bubbly. Why: an open plain's wind, an underground ventilation plant's hum and a distant freight train clicking by.
Edit: seamless 16.00 s loop, band RMS −36.0 dB, peak −21.1 dBFS, −32.0 LUFS.

### mars-colony-r46-b — "G56-22-Sandstorm.wav" by craigsmith + "Ambient Industrial Ventilation Soundscape" by amenhotepIV + "Shunting Atmos" by kangaroovindaloo

Proposed (round 46).
Sources: <https://freesound.org/people/craigsmith/sounds/438877/> (G56-22-Sandstorm.wav by craigsmith, CC0 1.0), <https://freesound.org/people/amenhotepIV/sounds/166014/> (Ambient Industrial Ventilation Soundscape by amenhotepIV, CC0 1.0), <https://freesound.org/people/kangaroovindaloo/sounds/414739/> (Shunting Atmos by kangaroovindaloo, CC-BY 4.0). Use: Level 17 (`mars-colony`): the plains wind, the domes' air plant, a distant rail.
Brief: replace the synthesized sound with a real recording, big and heavy, nothing bubbly. Why: a thin sand wind, an industrial ventilation hum and a distant freight train's steel clanking.
Edit: seamless 16.00 s loop, band RMS −36.1 dB, peak −19.6 dBFS, −32.2 LUFS.

### mars-foundry-r46-a — "Foundry Furnace.wav" by Walter_Odington + "G16-09-Factory-Overhead Crane.wav" by craigsmith + "Low wind in a desert canyon 2" by fran_marenco + "Big robot footstep 002" by AudioPapkin

Proposed (round 46).
Sources: <https://freesound.org/people/Walter_Odington/sounds/31566/> (Foundry Furnace.wav by Walter_Odington, CC0 1.0), <https://freesound.org/people/craigsmith/sounds/438133/> (G16-09-Factory-Overhead Crane.wav by craigsmith, CC0 1.0), <https://freesound.org/people/fran_marenco/sounds/852881/> (Low wind in a desert canyon 2 by fran_marenco, CC0 1.0), <https://freesound.org/people/AudioPapkin/sounds/813053/> (Big robot footstep 002 by AudioPapkin, CC0 1.0). Use: Level 19 (`mars-foundry`): the conveyors, the furnace, a slow hammer, the plateau wind.
Brief: replace the synthesized sound with a real recording, big and heavy, nothing bubbly. Why: a foundry furnace's roar, a factory's overhead crane rumbling (the conveyors), a far heavy hammer every 4 s and a low wind.
Edit: seamless 16.00 s loop, band RMS −37.6 dB, peak −16.6 dBFS, −28.0 LUFS.

### mars-foundry-r46-b — "Metalurgists At The Foundary.wav" by Walter_Odington + "MACHFan_big_industrial_fan_white_noise_Olympus_LS-P4.wav" by DBS_Sounds + "Steel mill low frequency drone" by sarana + "Low wind in a desert canyon 2" by fran_marenco + "Giant step" by DasWaff

Proposed (round 46).
Sources: <https://freesound.org/people/Walter_Odington/sounds/31567/> (Metalurgists At The Foundary.wav by Walter_Odington, CC0 1.0), <https://freesound.org/people/DBS_Sounds/sounds/613158/> (MACHFan_big_industrial_fan_white_noise_Olympus_LS-P4.wav by DBS_Sounds, CC0 1.0), <https://freesound.org/people/sarana/sounds/567148/> (Steel mill low frequency drone by sarana, CC0 1.0), <https://freesound.org/people/fran_marenco/sounds/852881/> (Low wind in a desert canyon 2 by fran_marenco, CC0 1.0), <https://freesound.org/people/DasWaff/sounds/721601/> (Giant step by DasWaff, CC0 1.0). Use: Level 19 (`mars-foundry`): the conveyors, the furnace, a slow hammer, the plateau wind.
Brief: replace the synthesized sound with a real recording, big and heavy, nothing bubbly. Why: a foundry's furnace, a big industrial fan (the blowers), a steel mill's deep drone, a low wind and a far giant hammer every 8 s.
Edit: seamless 16.00 s loop, band RMS −40.5 dB, peak −16.2 dBFS, −28.0 LUFS.

### enemy-leviathan-cry-r46-a — "Blue Whale B call @ 5X" by MBARI_MARS + "Underwater Rumble" by EminYILDIRIM

Proposed (round 46).
Sources: <https://freesound.org/people/MBARI_MARS/sounds/343682/> (Blue Whale B call @ 5X by MBARI_MARS, CC-BY 4.0), <https://freesound.org/people/EminYILDIRIM/sounds/572005/> (Underwater Rumble by EminYILDIRIM, CC-BY 4.0). Use: the Leviathan's death cry (Level 03), under its large burst.
Brief: replace the synthesized sound with a real recording, big and heavy, nothing bubbly. Why: a blue whale's B call (a deep falling moan), slowed to 0.9, over an underwater rumble.
Edit: 3.40 s, the loudest 100 ms band −23.9 dB, peak −16.0 dBFS, −23.8 LUFS.

### enemy-leviathan-cry-r46-b — "Underwater recording of Humpback whales and their calves" by alanmcki

Proposed (round 46).
Source: <https://freesound.org/people/alanmcki/sounds/399609/> (Underwater recording of Humpback whales and their calves by alanmcki, CC-BY 4.0). Use: the Leviathan's death cry (Level 03), under its large burst.
Brief: replace the synthesized sound with a real recording, big and heavy, nothing bubbly. Why: two humpback whale calls, slowed to 0.5 (deep and mournful).
Edit: 3.40 s, the loudest 100 ms band −24.0 dB, peak −16.1 dBFS, −26.5 LUFS.

### driftjelly-pulse-r46-a — "Underwater Impact" by deleted_user_7709760 + "Underwater Rumble" by EminYILDIRIM

Proposed (round 46).
Sources: <https://freesound.org/people/deleted_user_7709760/sounds/400793/> (Underwater Impact by deleted_user_7709760, CC-BY 3.0), <https://freesound.org/people/EminYILDIRIM/sounds/572005/> (Underwater Rumble by EminYILDIRIM, CC-BY 4.0). Use: the Driftjelly's ring pulse (Level 11).
Brief: replace the synthesized sound with a real recording, big and heavy, nothing bubbly. Why: a muffled underwater impact over an underwater rumble.
Edit: 0.90 s, the loudest 100 ms band −27.0 dB, peak −12.7 dBFS, −27.0 LUFS.

### driftjelly-pulse-r46-b — "Massive Structure Bend" by Kostas17 + "Underwater Impact" by deleted_user_7709760

Proposed (round 46).
Sources: <https://freesound.org/people/Kostas17/sounds/529620/> (Massive Structure Bend by Kostas17, CC-BY 3.0), <https://freesound.org/people/deleted_user_7709760/sounds/400793/> (Underwater Impact by deleted_user_7709760, CC-BY 3.0). Use: the Driftjelly's ring pulse (Level 11).
Brief: replace the synthesized sound with a real recording, big and heavy, nothing bubbly. Why: a massive structure bending (a deep groan), slowed to 0.8, with the underwater impact's tail.
Edit: 0.90 s, the loudest 100 ms band −30.2 dB, peak −15.3 dBFS, −26.9 LUFS.

### lamprey-drain-r46-a — "JacobsLadderLong1.flac" by Halleck + "hum electric transformer in large room1.flac" by kyles

Proposed (round 46).
Sources: <https://freesound.org/people/Halleck/sounds/19482/> (JacobsLadderLong1.flac by Halleck, CC-BY 4.0), <https://freesound.org/people/kyles/sounds/453444/> (hum electric transformer in large room1.flac by kyles, CC0 1.0). Use: the Lamprey's shield drain (Level 12) while it is latched on.
Brief: replace the synthesized sound with a real recording, big and heavy, nothing bubbly. Why: a Jacob's ladder's buzzing arc slowed to 0.6, over a transformer's hum.
Edit: seamless 2.40 s loop, band RMS −24.0 dB, peak −6.9 dBFS, −20.2 LUFS.

### lamprey-drain-r46-b — "S16-18 Tesla coil sparking and humming.wav" by craigsmith + "ELECMisc-INT_NOISES-Electrical Hums And Crackles Of A Light Fixture With A Loose Light Bulb_YMLT_NONE_08982261" by YouMightLikeThis

Proposed (round 46).
Sources: <https://freesound.org/people/craigsmith/sounds/675760/> (S16-18 Tesla coil sparking and humming.wav by craigsmith, CC0 1.0), <https://freesound.org/people/YouMightLikeThis/sounds/814061/> (ELECMisc-INT_NOISES-Electrical Hums And Crackles Of A Light Fixture With A Loose Light Bulb_YMLT_NONE_08982261 by YouMightLikeThis, CC0 1.0). Use: the Lamprey's shield drain (Level 12) while it is latched on.
Brief: replace the synthesized sound with a real recording, big and heavy, nothing bubbly. Why: a Tesla coil sparking and humming, slowed to 0.7, over a loose light fixture's hum and crackle.
Edit: seamless 2.40 s loop, band RMS −24.3 dB, peak −11.3 dBFS, −18.2 LUFS.

### sonde-chime-r46-a — "MechanicalClamp.wav" by Skullsmasha + "Radio Sign Off / Squelch" by JovianSounds

Proposed (round 46).
Sources: <https://freesound.org/people/Skullsmasha/sounds/123253/> (MechanicalClamp.wav by Skullsmasha, CC0 1.0), <https://freesound.org/people/JovianSounds/sounds/524205/> (Radio Sign Off / Squelch by JovianSounds, CC0 1.0). Use: a weather sonde collected (Level 12).
Brief: replace the synthesized sound with a real recording, big and heavy, nothing bubbly. Why: a heavy mechanical clamp slamming shut with a radio squelch (the telemetry handed over); no bell.
Edit: 1.00 s, the loudest 100 ms band −16.8 dB, peak −4.1 dBFS, −12.8 LUFS.

### sonde-chime-r46-b — "metal heavy dungeon prison door unlatch open close thud rattle and hatch open close slide squeak various int perspective on offmic.wav" by kyles + "Squelch.mp3" by LucasDuff

Proposed (round 46).
Sources: <https://freesound.org/people/kyles/sounds/407322/> (metal heavy dungeon prison door unlatch open close thud rattle and hatch open close slide squeak various int perspective on offmic.wav by kyles, CC0 1.0), <https://freesound.org/people/LucasDuff/sounds/516643/> (Squelch.mp3 by LucasDuff, CC0 1.0). Use: a weather sonde collected (Level 12).
Brief: replace the synthesized sound with a real recording, big and heavy, nothing bubbly. Why: a heavy dungeon door's latch clanking shut with a radio squelch; no bell.
Edit: 0.90 s, the loudest 100 ms band −16.5 dB, peak −4.3 dBFS, −17.3 LUFS.

### integrity-alarm-r46-a — "S29-08 Navy ship classic danger whistle siren.wav" by craigsmith

Proposed (round 46).
Source: <https://freesound.org/people/craigsmith/sounds/675782/> (S29-08 Navy ship classic danger whistle siren.wav by craigsmith, CC0 1.0). Use: the integrity alarm at 50 % and 25 % (Level 13's relay, Level 17's domes).
Brief: replace the synthesized sound with a real recording, big and heavy, nothing bubbly. Why: a navy ship's danger whistle siren (a real one).
Edit: 1.70 s, the loudest 100 ms band −17.0 dB, peak −8.9 dBFS, −14.5 LUFS.

### integrity-alarm-r46-b — "Submarie dive horn_better.wav" by CGEffex

Proposed (round 46).
Source: <https://freesound.org/people/CGEffex/sounds/104882/> (Submarie dive horn_better.wav by CGEffex, CC-BY 4.0). Use: the integrity alarm at 50 % and 25 % (Level 13's relay, Level 17's domes).
Brief: replace the synthesized sound with a real recording, big and heavy, nothing bubbly. Why: a submarine's dive horn, two blasts (a real one).
Edit: 1.85 s, the loudest 100 ms band −17.0 dB, peak −9.0 dBFS, −18.2 LUFS.

### drone-launch-r46-a — "Drone take off" by GeorgeHopkins

Proposed (round 46).
Source: <https://freesound.org/people/GeorgeHopkins/sounds/537241/> (Drone take off by GeorgeHopkins, CC-BY 4.0). Use: a drone launching (Level 13's relay, Level 17's colony).
Brief: replace the synthesized sound with a real recording, big and heavy, nothing bubbly. Why: a drone taking off, slowed to 0.75 (bigger rotors).
Edit: 2.20 s, the loudest 100 ms band −20.5 dB, peak −7.8 dBFS, −19.1 LUFS.

### drone-launch-r46-b — "Dji Mavic Mini 2 take-off UP-DOWN XY mic placement Zoom H5.wav" by Sadiquecat

Proposed (round 46).
Source: <https://freesound.org/people/Sadiquecat/sounds/683300/> (Dji Mavic Mini 2 take-off UP-DOWN XY mic placement Zoom H5.wav by Sadiquecat, CC0 1.0). Use: a drone launching (Level 13's relay, Level 17's colony).
Brief: replace the synthesized sound with a real recording, big and heavy, nothing bubbly. Why: a quadcopter's rotors starting and taking off, slowed to 0.75.
Edit: 2.20 s, the loudest 100 ms band −20.6 dB, peak −5.5 dBFS, −20.5 LUFS.

### dome-hit-r46-a — "BANGING ON THICK GLASS.wav" by trexlasso + "Thuds on Window" by zerolagtime + "Door Close Heavy Metal Glass Slow Creak Seal Theatre" by LampEight + "Ice Crack 3" by j_p_higgins

Proposed (round 46).
Sources: <https://freesound.org/people/trexlasso/sounds/629994/> (BANGING ON THICK GLASS.wav by trexlasso, CC0 1.0), <https://freesound.org/people/zerolagtime/sounds/245033/> (Thuds on Window by zerolagtime, CC-BY 4.0), <https://freesound.org/people/LampEight/sounds/402459/> (Door Close Heavy Metal Glass Slow Creak Seal Theatre by LampEight, CC0 1.0), <https://freesound.org/people/j_p_higgins/sounds/262633/> (Ice Crack 3 by j_p_higgins, CC0 1.0). Use: a Level 17 dome struck (`ALLY_HIT`).
Brief: replace the synthesized sound with a real recording, big and heavy, nothing bubbly. Why: a bang on thick glass with a heavy thud behind it, a glass door's creak and ice cracking (the stress lines); no shatter.
Edit: 1.20 s, the loudest 100 ms band −26.1 dB, peak −4.1 dBFS, −22.2 LUFS.

### dome-hit-r46-b — "Thuds on Window" by zerolagtime + "Door Close Heavy Metal Glass Medium Creak Seal Theatre" by LampEight + "Ice Crack 4" by j_p_higgins + "BANGING ON THICK GLASS.wav" by trexlasso

Proposed (round 46).
Sources: <https://freesound.org/people/zerolagtime/sounds/245033/> (Thuds on Window by zerolagtime, CC-BY 4.0), <https://freesound.org/people/LampEight/sounds/402460/> (Door Close Heavy Metal Glass Medium Creak Seal Theatre by LampEight, CC0 1.0), <https://freesound.org/people/j_p_higgins/sounds/262632/> (Ice Crack 4 by j_p_higgins, CC0 1.0), <https://freesound.org/people/trexlasso/sounds/629994/> (BANGING ON THICK GLASS.wav by trexlasso, CC0 1.0). Use: a Level 17 dome struck (`ALLY_HIT`).
Brief: replace the synthesized sound with a real recording, big and heavy, nothing bubbly. Why: a heavy thud on a window, a heavy glass door's creak and a frozen lake's fine cracking; no shatter.
Edit: 1.20 s, the loudest 100 ms band −27.2 dB, peak −4.1 dBFS, −23.0 LUFS.

### dash-r46-a — "Tire Bead Seater Air Blast" by j_soundeffects + "jet thruster bassy potential.aiff" by kyles

Proposed (round 46).
Sources: <https://freesound.org/people/j_soundeffects/sounds/863981/> (Tire Bead Seater Air Blast by j_soundeffects, CC-BY 4.0), <https://freesound.org/people/kyles/sounds/51808/> (jet thruster bassy potential.aiff by kyles, CC0 1.0). Use: the ship's dash.
Brief: replace the synthesized sound with a real recording, big and heavy, nothing bubbly. Why: a compressed-air blast with a jet thruster's bassy roar.
Edit: 0.40 s, the loudest 100 ms band −20.6 dB, peak −5.7 dBFS, −inf LUFS.

### dash-r46-b — "Afterburner (Close, Screamer, Quick Onset)" by JSK_Productions + "Gas Release" by adgawrhbshbffsfgvsrf

Proposed (round 46).
Sources: <https://freesound.org/people/JSK_Productions/sounds/829068/> (Afterburner (Close, Screamer, Quick Onset) by JSK_Productions, CC0 1.0), <https://freesound.org/people/adgawrhbshbffsfgvsrf/sounds/494044/> (Gas Release by adgawrhbshbffsfgvsrf, CC0 1.0). Use: the ship's dash.
Brief: replace the synthesized sound with a real recording, big and heavy, nothing bubbly. Why: an afterburner's roar, sped to 1.2, with a gas release's hiss.
Edit: 0.40 s, the loudest 100 ms band −21.0 dB, peak −6.9 dBFS, −inf LUFS.

### overcharge-whoosh-r46-a — "Power Charge" by crashoverride6 + "Powerfull Whooshes 2.wav" by Nic3_one

Proposed (round 46).
Sources: <https://freesound.org/people/crashoverride6/sounds/193610/> (Power Charge by crashoverride6, CC0 1.0), <https://freesound.org/people/Nic3_one/sounds/434869/> (Powerfull Whooshes 2.wav by Nic3_one, CC0 1.0). Use: Shield Overcharge's activation.
Brief: replace the synthesized sound with a real recording, big and heavy, nothing bubbly. Why: a camera flash charging, its whine gliding up, a powerful whoosh at its end.
Edit: 1.00 s, the loudest 100 ms band −20.2 dB, peak −7.3 dBFS, −18.6 LUFS.

### overcharge-whoosh-r46-b — "Electronic_Powerup.wav" by StephenSaldanha + "S16-18 Tesla coil sparking and humming.wav" by craigsmith

Proposed (round 46).
Sources: <https://freesound.org/people/StephenSaldanha/sounds/132560/> (Electronic_Powerup.wav by StephenSaldanha, CC-BY 4.0), <https://freesound.org/people/craigsmith/sounds/675760/> (S16-18 Tesla coil sparking and humming.wav by craigsmith, CC0 1.0). Use: Shield Overcharge's activation.
Brief: replace the synthesized sound with a real recording, big and heavy, nothing bubbly. Why: a phasing power-up gliding up with a Tesla coil's sparks swelling under it.
Edit: 1.00 s, the loudest 100 ms band −20.4 dB, peak −7.6 dBFS, −18.5 LUFS.

### overcharge-end-r46-a — "Power Charge" by crashoverride6

Proposed (round 46).
Source: <https://freesound.org/people/crashoverride6/sounds/193610/> (Power Charge by crashoverride6, CC0 1.0). Use: the end of Shield Overcharge's 8 s.
Brief: replace the synthesized sound with a real recording, big and heavy, nothing bubbly. Why: the camera flash's charge whine gliding down and fading.
Edit: 0.90 s, the loudest 100 ms band −24.0 dB, peak −16.4 dBFS, −22.4 LUFS.

### overcharge-end-r46-b — "Electronic_Powerup.wav" by StephenSaldanha + "S16-18 Tesla coil sparking and humming.wav" by craigsmith

Proposed (round 46).
Sources: <https://freesound.org/people/StephenSaldanha/sounds/132560/> (Electronic_Powerup.wav by StephenSaldanha, CC-BY 4.0), <https://freesound.org/people/craigsmith/sounds/675760/> (S16-18 Tesla coil sparking and humming.wav by craigsmith, CC0 1.0). Use: the end of Shield Overcharge's 8 s.
Brief: replace the synthesized sound with a real recording, big and heavy, nothing bubbly. Why: the phasing power-up gliding down with the Tesla coil's sparks dying.
Edit: 0.90 s, the loudest 100 ms band −25.3 dB, peak −12.2 dBFS, −21.5 LUFS.

### emp-ring-r46-a — "JacobsLadderLong1.flac" by Halleck + "S16-18 Tesla coil sparking and humming.wav" by craigsmith + "Big Structure Collision Heard from Inside" by Osvoldon

Proposed (round 46).
Sources: <https://freesound.org/people/Halleck/sounds/19482/> (JacobsLadderLong1.flac by Halleck, CC-BY 4.0), <https://freesound.org/people/craigsmith/sounds/675760/> (S16-18 Tesla coil sparking and humming.wav by craigsmith, CC0 1.0), <https://freesound.org/people/Osvoldon/sounds/653294/> (Big Structure Collision Heard from Inside by Osvoldon, CC0 1.0). Use: the EMP Burst's ring.
Brief: replace the synthesized sound with a real recording, big and heavy, nothing bubbly. Why: a Jacob's ladder's arc buzzing out, a Tesla coil's sparks and a massive structure's deep impact.
Edit: 1.35 s, the loudest 100 ms band −19.9 dB, peak −5.8 dBFS, −17.2 LUFS.

### emp-ring-r46-b — "high-voltage-discharge-2.wav" by follytowers + "Taser/High Voltage discharge" by The_Chemical_Workshop + "Deep Boom" by Za-Games

Proposed (round 46).
Sources: <https://freesound.org/people/follytowers/sounds/415962/> (high-voltage-discharge-2.wav by follytowers, CC0 1.0), <https://freesound.org/people/The_Chemical_Workshop/sounds/403252/> (Taser/High Voltage discharge by The_Chemical_Workshop, CC0 1.0), <https://freesound.org/people/Za-Games/sounds/539968/> (Deep Boom by Za-Games, CC0 1.0). Use: the EMP Burst's ring.
Brief: replace the synthesized sound with a real recording, big and heavy, nothing bubbly. Why: a high-voltage discharge with a taser's crack on its attack over a deep boom.
Edit: 1.35 s, the loudest 100 ms band −24.7 dB, peak −4.1 dBFS, −17.2 LUFS.

### emp-powerdown-r46-a — "Large machine running and powering down" by ryks88

Proposed (round 46).
Source: <https://freesound.org/people/ryks88/sounds/709258/> (Large machine running and powering down by ryks88, CC-BY 4.0). Use: a machine stunned by the EMP.
Brief: replace the synthesized sound with a real recording, big and heavy, nothing bubbly. Why: a large machine powering down, played fast and gliding down from 3x to 1x.
Edit: 0.90 s, the loudest 100 ms band −24.0 dB, peak −14.7 dBFS, −24.7 LUFS.

### emp-powerdown-r46-b — "hum electric transformer in large room1.flac" by kyles + "ELECMisc-INT_NOISES-Electrical Hums And Crackles Of A Light Fixture With A Loose Light Bulb_YMLT_NONE_08982261" by YouMightLikeThis

Proposed (round 46).
Sources: <https://freesound.org/people/kyles/sounds/453444/> (hum electric transformer in large room1.flac by kyles, CC0 1.0), <https://freesound.org/people/YouMightLikeThis/sounds/814061/> (ELECMisc-INT_NOISES-Electrical Hums And Crackles Of A Light Fixture With A Loose Light Bulb_YMLT_NONE_08982261 by YouMightLikeThis, CC0 1.0). Use: a machine stunned by the EMP.
Brief: replace the synthesized sound with a real recording, big and heavy, nothing bubbly. Why: a transformer's hum sagging (gliding 1.0 to 0.6) with a loose light fixture's flickering crackle.
Edit: 0.90 s, the loudest 100 ms band −24.0 dB, peak −10.8 dBFS, −20.5 LUFS.

### ion-beam-stop-r46-a — "S16-18 Tesla coil sparking and humming.wav" by craigsmith + "Large machine running and powering down" by ryks88

Proposed (round 46).
Sources: <https://freesound.org/people/craigsmith/sounds/675760/> (S16-18 Tesla coil sparking and humming.wav by craigsmith, CC0 1.0), <https://freesound.org/people/ryks88/sounds/709258/> (Large machine running and powering down by ryks88, CC-BY 4.0). Use: the Ion Beam's collapse when it stops.
Brief: replace the synthesized sound with a real recording, big and heavy, nothing bubbly. Why: a Tesla coil's hum and a large machine gliding down together (1.0 to 0.35).
Edit: 0.64 s, the loudest 100 ms band −25.5 dB, peak −15.6 dBFS, −21.3 LUFS.

### ion-beam-stop-r46-b — "JacobsLadderLong1.flac" by Halleck + "hum electric transformer in large room1.flac" by kyles

Proposed (round 46).
Sources: <https://freesound.org/people/Halleck/sounds/19482/> (JacobsLadderLong1.flac by Halleck, CC-BY 4.0), <https://freesound.org/people/kyles/sounds/453444/> (hum electric transformer in large room1.flac by kyles, CC0 1.0). Use: the Ion Beam's collapse when it stops.
Brief: replace the synthesized sound with a real recording, big and heavy, nothing bubbly. Why: a Jacob's ladder's arc buzz and a transformer's hum gliding down to nothing.
Edit: 0.62 s, the loudest 100 ms band −25.9 dB, peak −10.4 dBFS, −21.2 LUFS.

### beam-impact-r46-a — "Arc Welding.wav" by kev_durr

Proposed (round 46).
Source: <https://freesound.org/people/kev_durr/sounds/396511/> (Arc Welding.wav by kev_durr, CC-BY 4.0). Use: where any beam hits (a loop while it burns).
Brief: replace the synthesized sound with a real recording, big and heavy, nothing bubbly. Why: arc welding: a dry crackling sizzle over its 120 Hz buzz.
Edit: seamless 1.50 s loop, band RMS −40.1 dB, peak −5.1 dBFS, −26.2 LUFS.

### beam-impact-r46-b — "MIG Welding" by unfa + "JacobsLadderLong1.flac" by Halleck

Proposed (round 46).
Sources: <https://freesound.org/people/unfa/sounds/264827/> (MIG Welding by unfa, CC0 1.0), <https://freesound.org/people/Halleck/sounds/19482/> (JacobsLadderLong1.flac by Halleck, CC-BY 4.0). Use: where any beam hits (a loop while it burns).
Brief: replace the synthesized sound with a real recording, big and heavy, nothing bubbly. Why: MIG welding over a Jacob's ladder's arc buzz slowed to 0.6.
Edit: seamless 1.50 s loop, band RMS −38.0 dB, peak −10.5 dBFS, −32.5 LUFS.

### drone-rebuild-r46-a — "Rotating tank turret planetary electric motor" by lorefold + "M&P15 Bolt Release [No Ring]" by smill.and.welson

Proposed (round 46).
Sources: <https://freesound.org/people/lorefold/sounds/607310/> (Rotating tank turret planetary electric motor by lorefold, CC0 1.0), <https://freesound.org/people/smill.and.welson/sounds/858506/> (M&P15 Bolt Release [No Ring] by smill.and.welson, CC-BY 4.0). Use: the Combat Drone rebuilt.
Brief: replace the synthesized sound with a real recording, big and heavy, nothing bubbly. Why: an electric turret motor whirring up (gliding 0.8 to 1.2) into a rifle bolt's metal clack.
Edit: 0.60 s, the loudest 100 ms band −25.2 dB, peak −13.1 dBFS, −22.5 LUFS.

### drone-rebuild-r46-b — "Sci-Fi Mechanical Door" by CheatinSloth

Proposed (round 46).
Source: <https://freesound.org/people/CheatinSloth/sounds/739197/> (Sci-Fi Mechanical Door by CheatinSloth, CC0 1.0). Use: the Combat Drone rebuilt.
Brief: replace the synthesized sound with a real recording, big and heavy, nothing bubbly. Why: a sci-fi mechanical door's servo and clunk.
Edit: 0.60 s, the loudest 100 ms band −25.0 dB, peak −10.2 dBFS, −24.0 LUFS.

### launch-rail-r46-a — "Air (or steam) pressure release" by brunoboselli + "Eurofighter Typhoon Flyby 005 – Close Proximity" by TimoSchmied + "Closing large metal door-001" by AudioPapkin + "steam_safety_valve.wav" by Engineer_815

Proposed (round 46).
Sources: <https://freesound.org/people/brunoboselli/sounds/457294/> (Air (or steam) pressure release by brunoboselli, CC0 1.0), <https://freesound.org/people/TimoSchmied/sounds/640505/> (Eurofighter Typhoon Flyby 005 – Close Proximity by TimoSchmied, CC-BY 4.0), <https://freesound.org/people/AudioPapkin/sounds/814976/> (Closing large metal door-001 by AudioPapkin, CC0 1.0), <https://freesound.org/people/Engineer_815/sounds/493558/> (steam_safety_valve.wav by Engineer_815, CC0 1.0). Use: the catapult run at every level start (the buffer clunk at 3.6 s).
Brief: replace the synthesized sound with a real recording, big and heavy, nothing bubbly. Why: a pressure hiss building and a fighter jet's close pass (the shuttle's run) into a large metal door slamming (the buffer) and a steam safety valve venting.
Edit: 4.90 s, the loudest 100 ms band −20.9 dB, peak −6.2 dBFS, −20.9 LUFS.

### launch-rail-r46-b — "Hydraulic Hiss, TTC bus door" by OttisG + "Train, Subway, Pass By, Under Bridge Perspective" by Kinoton + "Heavy Gate Metal 01 (underground bunker of civil defense, Vsetin City, Club Vesmir)" by ALEFF-ATMOS + "MrM_Gas_Steam_Escape.wav" by mrmccormack

Proposed (round 46).
Sources: <https://freesound.org/people/OttisG/sounds/852991/> (Hydraulic Hiss, TTC bus door by OttisG, CC-BY 4.0), <https://freesound.org/people/Kinoton/sounds/368650/> (Train, Subway, Pass By, Under Bridge Perspective by Kinoton, CC0 1.0), <https://freesound.org/people/ALEFF-ATMOS/sounds/452780/> (Heavy Gate Metal 01 (underground bunker of civil defense, Vsetin City, Club Vesmir) by ALEFF-ATMOS, CC0 1.0), <https://freesound.org/people/mrmccormack/sounds/182359/> (MrM_Gas_Steam_Escape.wav by mrmccormack, CC0 1.0). Use: the catapult run at every level start (the buffer clunk at 3.6 s).
Brief: replace the synthesized sound with a real recording, big and heavy, nothing bubbly. Why: a hydraulic hiss, a subway train rushing past under a bridge (the shuttle's run), a bunker's heavy metal gate slamming (the buffer) and gas and steam escaping.
Edit: 4.90 s, the loudest 100 ms band −20.9 dB, peak −5.6 dBFS, −19.9 LUFS.

### herald-song-r47-a — "Vocal_crescendo.wav" by cellokratzer + "Throat Singing in a Cave" by ItsThatRoboGuy

Proposed (round 47), cut from the HQ previews by `tools/concept/audio/sfx_r47l.py`.
Sources: <https://freesound.org/people/cellokratzer/sounds/502946/> (Vocal_crescendo.wav by cellokratzer, CC0 1.0), <https://freesound.org/people/ItsThatRoboGuy/sounds/816640/> (Throat Singing in a Cave by ItsThatRoboGuy, CC0 1.0).
Use: the Choir Herald's song (`Sfx.HERALD_SONG`), a one-shot with each 10-bullet ring the Herald fires (every 4 s, 3.2 s on hard) while it lives, Levels 17, 19 and 21.
Brief: the Vrell Choir's voice: eerie and inhuman, several voices in a slow drifting chord; a real recording, big, nothing bubbly.
This one: a concert-hall choir's chord swelling in a crescendo while its voices slide upward, slowed to 0.7 (deeper and slower, the slide drags), with throat singing in a cave under it slowed to 0.85: its whistling overtones drift above the drone, which no human choir does.
Edit: 2.40 s, the loudest 100 ms band −20.0 dB, peak −10.8 dBFS, −20.6 LUFS.

**Chosen** (round 47, L.1: a picked; to be rebuilt from the originals, Freesound 502946 and 816640, when the download limit allows).

### herald-song-r47-b — "chamber_choir_chord_o.wav" by uair01

Proposed (round 47), cut from the HQ previews by `tools/concept/audio/sfx_r47l.py`.
Source: <https://freesound.org/people/uair01/sounds/65195/> (chamber_choir_chord_o.wav by uair01, CC-BY 3.0).
Use: the Choir Herald's song (`Sfx.HERALD_SONG`), a one-shot with each 10-bullet ring the Herald fires (every 4 s, 3.2 s on hard) while it lives, Levels 17, 19 and 21.
Brief: the Vrell Choir's voice: eerie and inhuman, several voices in a slow drifting chord; a real recording, big, nothing bubbly.
This one: a chamber choir's held 'oh' chord played twice at once, slowed to about 0.65 and a semitone apart, both sagging slowly in pitch: two choirs out of tune with each other, beating and smeared, a chord that slides downward and never settles.
Edit: 2.40 s, the loudest 100 ms band −20.0 dB, peak −9.3 dBFS, −19.4 LUFS.

**Rejected** (round 47, L.1, 2026-10-22: a picked); the file is in `rejected/`.

### enemy-leviathan-cry-r47-a — "humpback_whales_and_orcas_ambient_underwater_recording" by raiden04

Proposed (round 47), cut from the HQ previews by `tools/concept/audio/sfx_r47l.py`.
Source: <https://freesound.org/people/raiden04/sounds/797762/> (humpback_whales_and_orcas_ambient_underwater_recording by raiden04, CC-BY 4.0).
Use: the Leviathan's death cry (Level 03), under its large burst when it dies.
Brief: a whale's cry: deep, long and mournful; a real recording, big, nothing bubbly.
This one: a humpback whale's moan from a clean 3D hydrophone recording: a cry that falls and settles on a long low tone with its overtones, slowed to 0.85 (a bigger animal).
Edit: 4.00 s, the loudest 100 ms band −23.9 dB, peak −16.2 dBFS, −23.2 LUFS.

**Rejected** (round 47, L.2, 2026-10-22: b picked); the file is in `rejected/`.

### enemy-leviathan-cry-r47-b — "humpback_whales_and_orcas_ambient_underwater_recording" by raiden04

Proposed (round 47), cut from the HQ previews by `tools/concept/audio/sfx_r47l.py`.
Source: <https://freesound.org/people/raiden04/sounds/797762/> (humpback_whales_and_orcas_ambient_underwater_recording by raiden04, CC-BY 4.0).
Use: the Leviathan's death cry (Level 03), under its large burst when it dies.
Brief: a whale's cry: deep, long and mournful; a real recording, big, nothing bubbly.
This one: another humpback moan from the same recording: it starts high, sinks into a long deep groan and lifts a little at its end, slowed to 0.8 (deeper).
Edit: 4.00 s, the loudest 100 ms band −26.9 dB, peak −12.2 dBFS, −20.4 LUFS.

**Chosen** (round 47, L.2: b picked; to be rebuilt from the original, Freesound 797762, when the download limit allows).

### integrity-alarm-r47-a — "Submarine submersion alarm_mono_wave.wav" by Guialgarve

Proposed (round 47), cut from the HQ previews by `tools/concept/audio/sfx_r47l.py`.
Source: <https://freesound.org/people/Guialgarve/sounds/480898/> (Submarine submersion alarm_mono_wave.wav by Guialgarve, CC0 1.0).
Use: an ally or an object the player protects (Level 13's relay, Level 17's domes) dropping below 50 % and again below 25 % of its health: the cockpit's warning that it is in danger.
Brief: a short urgent alarm, two tones, about 1 s, repeating; a real recording, big, nothing bubbly.
This one: a real submarine's submersion alarm, a harsh buzzing horn, cut into a high-low call (the low a third under, by slowing it to 0.84), twice: 'BAAP-baap, BAAP-baap', 2 s.
Edit: 1.95 s, the loudest 100 ms band −17.1 dB, peak −6.6 dBFS, −14.1 LUFS.

**Rejected** (round 47, L.3, 2026-10-22: neither; "The old one is fine for that."); the file is in `rejected/`.

### integrity-alarm-r47-b — "Klaxon on axis Long.wav" by jameswrowles

Proposed (round 47), cut from the HQ previews by `tools/concept/audio/sfx_r47l.py`.
Source: <https://freesound.org/people/jameswrowles/sounds/514981/> (Klaxon on axis Long.wav by jameswrowles, CC0 1.0).
Use: an ally or an object the player protects (Level 13's relay, Level 17's domes) dropping below 50 % and again below 25 % of its health: the cockpit's warning that it is in danger.
Brief: a short urgent alarm, two tones, about 1 s, repeating; a real recording, big, nothing bubbly.
This one: a real mains-driven klaxon hooter (the old 'ah-oo-gah' horn's steady blare), cut into a high-low call (the low slowed to 0.8), twice: rougher and more nasal than a, 2 s.
Edit: 1.95 s, the loudest 100 ms band −17.3 dB, peak −6.6 dBFS, −13.1 LUFS.

**Rejected** (round 47, L.3, 2026-10-22: neither; "The old one is fine for that."); the file is in `rejected/`.

### dome-hit-r47-a — "Hard Glass Impact" by deleted_user_3656686 + "hit - glass - window 01.wav" by Anthousai + "Crack_glass.wav" by 13FPanska_Cerny_Jan

Proposed (round 47), cut from the HQ previews by `tools/concept/audio/sfx_r47l.py`.
Sources: <https://freesound.org/people/deleted_user_3656686/sounds/418194/> (Hard Glass Impact by deleted_user_3656686, CC0 1.0), <https://freesound.org/people/Anthousai/sounds/406261/> (hit - glass - window 01.wav by Anthousai, CC0 1.0), <https://freesound.org/people/13FPanska_Cerny_Jan/sounds/379008/> (Crack_glass.wav by 13FPanska_Cerny_Jan, CC0 1.0).
Use: an enemy shot hitting a Level 17 colony dome (`ALLY_HIT`), a greenhouse of thick glass.
Brief: glass being hit: sharp, a hard knock with a bright crack, maybe a faint stress creak; not a thud; a real recording, big, nothing bubbly.
This one: a hard glass impact (a metal object on glass, with cracks forming), the ring of a window pane under it and a sharp glass crack 0.1 s later: bright and hard, 0.9 s.
Edit: 0.90 s, the loudest 100 ms band −21.8 dB, peak −4.6 dBFS, −22.3 LUFS.

**Rejected** (round 47, L.4, 2026-10-22: redo; "These are too small. a is a bottle cracking, b is nothing like glass breaking. A dome is a very large structure."); the file is in `rejected/`.

### dome-hit-r47-b — "hit - glass - window 01.wav" by Anthousai + "Tapping on window - 3 types.WAV" by bpwebster + "glass_kappa_03.wav" by mugwood

Proposed (round 47), cut from the HQ previews by `tools/concept/audio/sfx_r47l.py`.
Sources: <https://freesound.org/people/Anthousai/sounds/406261/> (hit - glass - window 01.wav by Anthousai, CC0 1.0), <https://freesound.org/people/bpwebster/sounds/658607/> (Tapping on window - 3 types.WAV by bpwebster, CC0 1.0), <https://freesound.org/people/mugwood/sounds/121366/> (glass_kappa_03.wav by mugwood, CC-BY 4.0).
Use: an enemy shot hitting a Level 17 colony dome (`ALLY_HIT`), a greenhouse of thick glass.
Brief: glass being hit: sharp, a hard knock with a bright crack, maybe a faint stress creak; not a thud; a real recording, big, nothing bubbly.
This one: a window struck hard: the knock and the pane ringing, a second hard tap's ring on it, and a stress crack in the glass right after (a cooling glass cracking): a glassy 'tock' with a ring, 1 s.
Edit: 1.00 s, the loudest 100 ms band −20.6 dB, peak −4.2 dBFS, −23.6 LUFS.

**Rejected** (round 47, L.4, 2026-10-22: redo; "These are too small. a is a bottle cracking, b is nothing like glass breaking. A dome is a very large structure."); the file is in `rejected/`.

### launch-rail-r47-a — "Jet Fighters and Helicopter Launches and Recoveries (Landings) on Aircraft Carrier.flac" by qubodup + "Afterburner sound.wav" by StoneyJ + "Rail Cars Slam Hard and Couple Together Air Released" by WeenyBeany + "Air (or steam) pressure release" by brunoboselli

Proposed (round 47), cut from the HQ previews by `tools/concept/audio/sfx_r47l.py`.
Sources: <https://freesound.org/people/qubodup/sounds/162449/> (Jet Fighters and Helicopter Launches and Recoveries (Landings) on Aircraft Carrier.flac by qubodup, CC0 1.0), <https://freesound.org/people/StoneyJ/sounds/104883/> (Afterburner sound.wav by StoneyJ, CC0 1.0), <https://freesound.org/people/WeenyBeany/sounds/750751/> (Rail Cars Slam Hard and Couple Together Air Released by WeenyBeany, CC-BY 4.0), <https://freesound.org/people/brunoboselli/sounds/457294/> (Air (or steam) pressure release by brunoboselli, CC0 1.0).
Use: every level start (4.9 s): the ship is catapulted off the carrier; the shuttle stops at 3.6 s.
Brief: an aircraft-carrier catapult launch: the jet at full power, the run, the hard stop, steam; a real recording, big, nothing bubbly.
This one: a real carrier launch (USS George H. W. Bush) with a real F-4's afterburner at full power under its start, then the catapult fires at 1.2 s and the jet's roar tears past; at 3.55 s a rail car's hard steel slam (the shuttle hitting its stop) and a steam release hissing as the roar fades.
Edit: 4.90 s, the loudest 100 ms band −21.0 dB, peak −7.6 dBFS, −19.4 LUFS.

**Rejected** (round 47, L.5, 2026-10-22: b picked); the file is in `rejected/`.

### launch-rail-r47-b — "Jet Fighters and Helicopter Launches and Recoveries (Landings) on Aircraft Carrier.flac" by qubodup + "Afterburner sound.wav" by StoneyJ + "Train car coupling sounds" by toam + "Rail Cars Slam Hard and Couple Together Air Released" by WeenyBeany + "AIRHiss-Blue Snowball Microphone, CU_Steam Release, Acme Windmaster_Nicholas Judy_TDC" by designerschoice

Proposed (round 47), cut from the HQ previews by `tools/concept/audio/sfx_r47l.py`.
Sources: <https://freesound.org/people/qubodup/sounds/162449/> (Jet Fighters and Helicopter Launches and Recoveries (Landings) on Aircraft Carrier.flac by qubodup, CC0 1.0), <https://freesound.org/people/StoneyJ/sounds/104883/> (Afterburner sound.wav by StoneyJ, CC0 1.0), <https://freesound.org/people/toam/sounds/198605/> (Train car coupling sounds by toam, CC-BY 3.0), <https://freesound.org/people/WeenyBeany/sounds/750751/> (Rail Cars Slam Hard and Couple Together Air Released by WeenyBeany, CC-BY 4.0), <https://freesound.org/people/designerschoice/sounds/803061/> (AIRHiss-Blue Snowball Microphone, CU_Steam Release, Acme Windmaster_Nicholas Judy_TDC by designerschoice, CC-BY 4.0).
Use: every level start (4.9 s): the ship is catapulted off the carrier; the shuttle stops at 3.6 s.
Brief: an aircraft-carrier catapult launch: the jet at full power, the run, the hard stop, steam; a real recording, big, nothing bubbly.
This one: another real launch from the same carrier: an afterburner at full power, the catapult firing at 1.2 s and the jet's roar rising more slowly; at 3.55 s a train coupler's sharp steel clank over a rail car slam's low body (a brighter, more metallic stop than a), then a close steam release.
Edit: 4.95 s, the loudest 100 ms band −21.1 dB, peak −8.6 dBFS, −20.6 LUFS.

**Chosen** (round 47, L.5: b picked; to be rebuilt from the originals, Freesound 162449, 104883, 198605, 750751 and 803061, when the download limit allows).

## Concept round 49, step AUD-M (the dome hit, third try, from the database)

The generator is `tools/concept/audio/sfx_r49m.py` (Freesound HQ previews; variant b adds a real cathedral impulse response by convolution).

### dome-hit-r49-a — "IMPACT_GLASS_PANE_HIT_NO_SMASH_METAL_RING_LOUD_NTG3_H4N_20120513.wav" by samueljustice00 + "Ice - Lake fractures" by Vrymaa

Proposed (round 49), cut from the HQ previews by `tools/concept/audio/sfx_r49m.py`.
Sources: <https://freesound.org/people/samueljustice00/sounds/155619/> (IMPACT_GLASS_PANE_HIT_NO_SMASH_METAL_RING_LOUD_NTG3_H4N_20120513.wav by samueljustice00, CC0 1.0), <https://freesound.org/people/Vrymaa/sounds/737896/> (Ice - Lake fractures by Vrymaa, CC0 1.0).
Use: every enemy shot that hits one of Level 17's colony greenhouse domes (`Sfx.DOME_HIT`, `AllyCues`): often, several in a row.
Brief: a glass dome tens of metres across, the size of a stadium roof, being hit: both sharp and huge, a hard bright strike and then the big structure answering (a long low glassy resonance or boom, cracks running away across a large surface, like a frozen lake's ice), heard from a distance; at most 1.2 s, no shatter, not a thud, not a bottle; a real recording, big, nothing bubbly.
This one: a steel hammer striking a big glass pane on a wall (loud, bright, not breaking) and, right under it, a frozen lake's ice cracking: one deep crack and then a run of smaller cracks racing away across the sheet, each with the ice's falling 'zing', slowed to 0.8 so the sheet sounds bigger: sharp, glassy and wide, 1.2 s.
Edit: 1.22 s, the loudest 100 ms band −19.6 dB, peak −9.0 dBFS, −23.2 LUFS.

**Rejected** (round 49, M.1, 2026-10-22: the user picked b); the file is in `rejected/`.

### dome-hit-r49-b — "IMPACT_GLASS_PANE_HIT_NO_SMASH_METAL_RING_LOUD_NTG3_H4N_20120513.wav" by samueljustice00 + "R4_00328-8_GlassTable.wav" by kevp888 + "IR_Cathedral_5m_Stereo.wav" by Nox_Sound

Proposed (round 49), cut from the HQ previews by `tools/concept/audio/sfx_r49m.py`.
Sources: <https://freesound.org/people/samueljustice00/sounds/155619/> (IMPACT_GLASS_PANE_HIT_NO_SMASH_METAL_RING_LOUD_NTG3_H4N_20120513.wav by samueljustice00, CC0 1.0), <https://freesound.org/people/kevp888/sounds/636555/> (R4_00328-8_GlassTable.wav by kevp888, CC-BY 4.0), <https://freesound.org/people/Nox_Sound/sounds/648945/> (IR_Cathedral_5m_Stereo.wav by Nox_Sound, CC0 1.0).
Use: every enemy shot that hits one of Level 17's colony greenhouse domes (`Sfx.DOME_HIT`, `AllyCues`): often, several in a row.
Brief: a glass dome tens of metres across, the size of a stadium roof, being hit: both sharp and huge, a hard bright strike and then the big structure answering (a long low glassy resonance or boom, cracks running away across a large surface, like a frozen lake's ice), heard from a distance; at most 1.2 s, no shatter, not a thud, not a bottle; a real recording, big, nothing bubbly.
This one: the hammer on the glass pane slowed to 0.85 (heavier), over a glass table struck and ringing (a contact-mic recording) slowed to 0.5, an octave down, so a large glass body hums on after the hit; both played into a real cathedral (convolved with its recorded impulse response), so it rings out in a huge space as if heard from far below: a bright 'clank' with a long low glassy 'hmmm', 1.2 s.
Edit: 1.20 s, the loudest 100 ms band −19.9 dB, peak −9.0 dBFS, −22.1 LUFS.

**Chosen** (round 49, M.1: b picked; to be rebuilt from the originals, Freesound 155619, 636555 and the impulse response 648945, when the download limit allows).

### dome-hit-r49-c — "rock hitting icy lake" by ultraaxvii + "Ice - Lake fractures" by Vrymaa + "Door_Metal_Groans_Ext.wav" by Cell31_Sound_Productions

Proposed (round 49), cut from the HQ previews by `tools/concept/audio/sfx_r49m.py`.
Sources: <https://freesound.org/people/ultraaxvii/sounds/591152/> (rock hitting icy lake by ultraaxvii, CC0 1.0), <https://freesound.org/people/Vrymaa/sounds/737896/> (Ice - Lake fractures by Vrymaa, CC0 1.0), <https://freesound.org/people/Cell31_Sound_Productions/sounds/264014/> (Door_Metal_Groans_Ext.wav by Cell31_Sound_Productions, CC-BY 4.0).
Use: every enemy shot that hits one of Level 17's colony greenhouse domes (`Sfx.DOME_HIT`, `AllyCues`): often, several in a row.
Brief: a glass dome tens of metres across, the size of a stadium roof, being hit: both sharp and huge, a hard bright strike and then the big structure answering (a long low glassy resonance or boom, cracks running away across a large surface, like a frozen lake's ice), heard from a distance; at most 1.2 s, no shatter, not a thud, not a bottle; a real recording, big, nothing bubbly.
This one: a rock slammed onto a frozen lake (a hard icy crack with the ice's 'zing'), on a deep crack and boom of the lake's thick ice slowed to 0.85, and from 0.25 s a tall steel door's low groan under it, quiet, like the dome's frame taking the blow: harder and darker than a, no glass pane, 1.2 s.
Edit: 1.23 s, the loudest 100 ms band −24.7 dB, peak −4.3 dBFS, −22.0 LUFS.

**Rejected** (round 49, M.1, 2026-10-22: the user picked b); the file is in `rejected/`.

## Concept round 50, step AUD-A (the Act 4 arsenal: Harpoon Torpedoes, Depth Charges, the Torpedo Pod without bubbles, the Sonar Pulse and the limit chime)

The generator is `tools/concept/audio/sfx_r50a.py` (Freesound HQ previews; the harpoon's run is a seamless loop; the limit chime is synthesized). The Sonar Pulse's a is the chosen `special-sonar-r04-a` and the Decoy Flares keep the chosen special-flares-r08-a (no new file).

### shot-harpoon-r50-a — "Tire Bead Seater Air Blast" by j_soundeffects + "Cannon Fires, Take 1, Quarter Speed" by CHallSmith + "G45-20-Submarine Motor Underwater.wav" by craigsmith

Proposed (round 50), cut from the HQ previews by `tools/concept/audio/sfx_r50a.py`.
Sources: <https://freesound.org/people/j_soundeffects/sounds/863981/> (Tire Bead Seater Air Blast by j_soundeffects, CC-BY 4.0), <https://freesound.org/people/CHallSmith/sounds/870742/> (Cannon Fires, Take 1, Quarter Speed by CHallSmith, CC0 1.0), <https://freesound.org/people/craigsmith/sounds/438722/> (G45-20-Submarine Motor Underwater.wav by craigsmith, CC0 1.0).
Use: every Harpoon Torpedoes shot (the front `anti-sub` weapon, Act 4 from L23's waterline; 0.8-1 a second, two torpedoes at L5), mostly under water: the ship's own fire, so often.
Brief: the `torpedo` family made heavier and without bubbles: a pneumatic tube's thump as the torpedo is pushed out and its motor's whirr running off ahead; muffled and pressured, short, never tiring at one a second; a real recording, heavy, nothing bubbly.
This one: a tyre-bead seater's compressed-air blast (a pneumatic tube's hard 'thoomp'), muffled at 2.5 kHz, on a cannon's deep thump played at quarter speed, and from 0.12 s a submarine motor's whirr rising in pitch as it runs off and fades: a punchy pneumatic launch with a motor leaving, 0.82 s.
Edit: 0.82 s, the loudest 100 ms band −22.4 dB, peak −7.7 dBFS, −22.2 LUFS.

**Rejected** (round 50, S.1, 2026-10-22: the user picked b); the file is in `rejected/`.

### shot-harpoon-r50-b — "G45-13-Submarine Torpedo Fire.wav" by craigsmith + "Torpedo launch.wav" by jobro

Proposed (round 50), cut from the HQ previews by `tools/concept/audio/sfx_r50a.py`.
Sources: <https://freesound.org/people/craigsmith/sounds/438715/> (G45-13-Submarine Torpedo Fire.wav by craigsmith, CC0 1.0), <https://freesound.org/people/jobro/sounds/35531/> (Torpedo launch.wav by jobro, CC-BY 3.0).
Use: every Harpoon Torpedoes shot (the front `anti-sub` weapon, Act 4 from L23's waterline; 0.8-1 a second, two torpedoes at L5), mostly under water: the ship's own fire, so often.
Brief: the `torpedo` family made heavier and without bubbles: a pneumatic tube's thump as the torpedo is pushed out and its motor's whirr running off ahead; muffled and pressured, short, never tiring at one a second; a real recording, heavy, nothing bubbly.
This one: a vintage film recording of a submarine torpedo firing (a pressurised hiss with thumps and thunks in it), muffled at 3 kHz, over the Torpedo Pod's launch in its dry version slowed to 0.75 and muffled at 900 Hz: the pod's launch made bigger and lower, more hiss and mechanism than a, 0.9 s.
Edit: 0.90 s, the loudest 100 ms band −24.6 dB, peak −5.6 dBFS, −18.0 LUFS.

**Chosen** (round 50, S.1: b picked; to be rebuilt from the originals, Freesound 438715 and 35531, when the download limit allows).

### harpoon-run-r50-a — "G45-20-Submarine Motor Underwater.wav" by craigsmith + "Submarine Passing 1.wav" by Schmaddio

Proposed (round 50), cut from the HQ previews by `tools/concept/audio/sfx_r50a.py`.
Sources: <https://freesound.org/people/craigsmith/sounds/438722/> (G45-20-Submarine Motor Underwater.wav by craigsmith, CC0 1.0), <https://freesound.org/people/Schmaddio/sounds/432514/> (Submarine Passing 1.wav by Schmaddio, CC0 1.0).
Use: while a Harpoon Torpedo runs ahead of the ship (one loop for all of them, as long as any is on the screen: about 2.5 s a torpedo under water, so nearly all the time while the weapon fires).
Brief: a torpedo's motor and propeller running away through the water: a steady, muffled, heavy whirr or thrum, low in the mix under the launches and hits, no bubbles, no clicks, a seamless loop; a real recording, heavy, nothing bubbly.
This one: a submarine's motor under water (a steady, high, muffled whirr, a vintage film recording) over a deep submarine rumble: a machine's whine running on, the most 'motor' of the two.
Edit: 4.00 s, band RMS −34.0 dB, peak −18.7 dBFS, −30.4 LUFS.

**Chosen** (round 50, S.2: a picked; to be rebuilt from the originals, Freesound 438722 and 432514, when the download limit allows).

### harpoon-run-r50-b — "underwater ship propeller 3.45 PM 220920_0532.wav" by klankbeeld + "G45-20-Submarine Motor Underwater.wav" by craigsmith

Proposed (round 50), cut from the HQ previews by `tools/concept/audio/sfx_r50a.py`.
Sources: <https://freesound.org/people/klankbeeld/sounds/680037/> (underwater ship propeller 3.45 PM 220920_0532.wav by klankbeeld, CC-BY 4.0), <https://freesound.org/people/craigsmith/sounds/438722/> (G45-20-Submarine Motor Underwater.wav by craigsmith, CC0 1.0).
Use: while a Harpoon Torpedo runs ahead of the ship (one loop for all of them, as long as any is on the screen: about 2.5 s a torpedo under water, so nearly all the time while the weapon fires).
Brief: a torpedo's motor and propeller running away through the water: a steady, muffled, heavy whirr or thrum, low in the mix under the launches and hits, no bubbles, no clicks, a seamless loop; a real recording, heavy, nothing bubbly.
This one: a real ship's propeller recorded through a hydrophone (a low churning thrum, the blades' beat in it), muffled at 1.5 kHz, with the submarine motor slowed to 0.8 quietly under it: lower and rougher than a, the most 'propeller in water'.
Edit: 4.00 s, band RMS −34.0 dB, peak −11.9 dBFS, −30.4 LUFS.

**Rejected** (round 50, S.2, 2026-10-22: the user picked a); the file is in `rejected/`.

### harpoon-hit-r50-a — "Underwater Deep Impact" by Sheyvan + "DepthBomb_01.aif" by ZeSoundResearchInc.

Proposed (round 50), cut from the HQ previews by `tools/concept/audio/sfx_r50a.py`.
Sources: <https://freesound.org/people/Sheyvan/sounds/470084/> (Underwater Deep Impact by Sheyvan, CC0 1.0), <https://freesound.org/people/ZeSoundResearchInc./sounds/147876/> (DepthBomb_01.aif by ZeSoundResearchInc., CC0 1.0).
Use: a Harpoon Torpedo striking its target (with the 20 px splash), under water and at the surface of a `water: true` level: up to about one a second, more with two weapons' worth of targets dying around it; the target's own death plays its explosion rung over it.
Brief: a heavy thud on impact: a deep, pressured under-water blast with a muffled body and a short low tail, no splash and no bubble rattle; short enough to repeat; a real recording, heavy, nothing bubbly.
This one: an under-water deep impact (a massive dull hit with a churning body) on a depth bomb's sub-bass boom: a heavy 'thoom' with a low rolling tail, 1.2 s.
Edit: 1.20 s, the loudest 100 ms band −27.4 dB, peak −4.1 dBFS, −18.0 LUFS.

**Chosen** (round 50, S.3: a picked; to be rebuilt from the originals, Freesound 470084 and 147876, when the download limit allows).

### harpoon-hit-r50-b — "G45-14-Torpedo Explosion.wav" by craigsmith + "DepthBomb_04.aif" by ZeSoundResearchInc.

Proposed (round 50), cut from the HQ previews by `tools/concept/audio/sfx_r50a.py`.
Sources: <https://freesound.org/people/craigsmith/sounds/438716/> (G45-14-Torpedo Explosion.wav by craigsmith, CC0 1.0), <https://freesound.org/people/ZeSoundResearchInc./sounds/147873/> (DepthBomb_04.aif by ZeSoundResearchInc., CC0 1.0).
Use: a Harpoon Torpedo striking its target (with the 20 px splash), under water and at the surface of a `water: true` level: up to about one a second, more with two weapons' worth of targets dying around it; the target's own death plays its explosion rung over it.
Brief: a heavy thud on impact: a deep, pressured under-water blast with a muffled body and a short low tail, no splash and no bubble rattle; short enough to repeat; a real recording, heavy, nothing bubbly.
This one: a vintage film recording of a torpedo exploding (a big blast with rumble in it), muffled at 2 kHz, on a depth bomb's sub-bass boom: rougher and more 'explosion' than a, with more grit in it, 1.2 s.
Edit: 1.20 s, the loudest 100 ms band −23.4 dB, peak −8.4 dBFS, −18.8 LUFS.

**Rejected** (round 50, S.3, 2026-10-22: the user picked a); the file is in `rejected/`.

### shot-depth-charge-r50-a — "barrelhits.wav" by j1987 + "Oil Drum Soft Impact.wav" by DeVern

Proposed (round 50), cut from the HQ previews by `tools/concept/audio/sfx_r50a.py`.
Sources: <https://freesound.org/people/j1987/sounds/178838/> (barrelhits.wav by j1987, CC0 1.0), <https://freesound.org/people/DeVern/sounds/524817/> (Oil Drum Soft Impact.wav by DeVern, CC0 1.0).
Use: every Depth Charges volley (the rear `anti-sub` weapon, Act 4 from L23's waterline): a barrel rolling off the stern, 1 to 1.4 times a second (two barrels at L5); the ship's own fire, so often.
Brief: a heavy barrel clank as the charge rolls off its rail: steel on steel, weighty and short, slightly muffled; the burst comes later (its own sound); a real recording, heavy, nothing bubbly.
This one: a piece of a steel barrel slammed down (a hard metal clank and a smaller rebound 0.1 s later) slowed to 0.85, over an oil drum's soft hit slowed to 0.75 (its hollow boom ringing under the clank): a heavy barrel thrown off, 0.5 s.
Edit: 0.50 s, the loudest 100 ms band −27.3 dB, peak −11.6 dBFS, −24.6 LUFS.

**Rejected** (round 50, S.4, 2026-10-22: the user picked b); the file is in `rejected/`.

### shot-depth-charge-r50-b — "hatchclose.wav" by Mihacappy + "Big robot footstep 005" by AudioPapkin

Proposed (round 50), cut from the HQ previews by `tools/concept/audio/sfx_r50a.py`.
Sources: <https://freesound.org/people/Mihacappy/sounds/828127/> (hatchclose.wav by Mihacappy, CC0 1.0), <https://freesound.org/people/AudioPapkin/sounds/815039/> (Big robot footstep 005 by AudioPapkin, CC0 1.0).
Use: every Depth Charges volley (the rear `anti-sub` weapon, Act 4 from L23's waterline): a barrel rolling off the stern, 1 to 1.4 times a second (two barrels at L5); the ship's own fire, so often.
Brief: a heavy barrel clank as the charge rolls off its rail: steel on steel, weighty and short, slightly muffled; the burst comes later (its own sound); a real recording, heavy, nothing bubbly.
This one: a heavy metal hatch slamming shut slowed to 0.9, on a big robot's metal footstep (a deep steel stomp): a darker, more 'latch and weight' clunk than a, less ring, 0.55 s.
Edit: 0.50 s, the loudest 100 ms band −27.0 dB, peak −15.1 dBFS, −26.2 LUFS.

**Chosen** (round 50, S.4: b picked; to be rebuilt from the originals, Freesound 828127 and 815039, when the download limit allows).

### depth-charge-burst-r50-a — "DepthBomb_03.aif" by ZeSoundResearchInc. + "NổLớnDướiNước1" by SieuAmThanh

Proposed (round 50), cut from the HQ previews by `tools/concept/audio/sfx_r50a.py`.
Sources: <https://freesound.org/people/ZeSoundResearchInc./sounds/147874/> (DepthBomb_03.aif by ZeSoundResearchInc., CC0 1.0), <https://freesound.org/people/SieuAmThanh/sounds/546957/> (NổLớnDướiNước1 by SieuAmThanh, CC0 1.0).
Use: a depth charge bursting on its fuse (0.7-0.9 s after the roll-out, behind the ship), about once a second, two at L5; also on the `sub` layer above water.
Brief: a heavy, low under-water thud: a pressure wave rather than a fireball, deep and muffled with a short low tail; no splash, no bubbles; a real recording, heavy, nothing bubbly.
This one: a depth bomb's sub-bass boom with a real explosion low-passed at 500 Hz (its body only) over it: a deep, round pressure 'whumpf', the softest and lowest, 1.5 s.
Edit: 1.50 s, the loudest 100 ms band −23.4 dB, peak −5.0 dBFS, −18.1 LUFS.

**Chosen** (round 50, S.5: a picked; to be rebuilt from the originals, Freesound 147874 and 546957, when the download limit allows).

### depth-charge-burst-r50-b — "G45-14-Torpedo Explosion.wav" by craigsmith + "DepthBomb_02.aif" by ZeSoundResearchInc.

Proposed (round 50), cut from the HQ previews by `tools/concept/audio/sfx_r50a.py`.
Sources: <https://freesound.org/people/craigsmith/sounds/438716/> (G45-14-Torpedo Explosion.wav by craigsmith, CC0 1.0), <https://freesound.org/people/ZeSoundResearchInc./sounds/147875/> (DepthBomb_02.aif by ZeSoundResearchInc., CC0 1.0).
Use: a depth charge bursting on its fuse (0.7-0.9 s after the roll-out, behind the ship), about once a second, two at L5; also on the `sub` layer above water.
Brief: a heavy, low under-water thud: a pressure wave rather than a fireball, deep and muffled with a short low tail; no splash, no bubbles; a real recording, heavy, nothing bubbly.
This one: a vintage torpedo explosion slowed to 0.75 and muffled at 1.2 kHz, on a depth bomb's sub-bass boom: a deeper, rumbling 'thoom' with a rough edge, more weight than a, 1.5 s.
Edit: 1.50 s, the loudest 100 ms band −26.0 dB, peak −7.7 dBFS, −18.0 LUFS.

**Rejected** (round 50, S.5, 2026-10-22: the user picked a); the file is in `rejected/`.

### shot-torpedo-r50-a — "Torpedo launch.wav" by jobro

Proposed (round 50), cut from the HQ previews by `tools/concept/audio/sfx_r50a.py`.
Source: <https://freesound.org/people/jobro/sounds/35531/> (Torpedo launch.wav by jobro, CC-BY 3.0).
Use: every Torpedo Pod shot (wing mount, Levels 11 on and Act 4; Rook's torpedo gun too): its chosen launch (shot-torpedo-r03-a) today.
Brief: the chosen launch as it is, with its bubbly character cut (Q12): the source's 'water' is a phaser sweep over a low-passed launch, so the cut keeps the muffled launch and drops the phaser's gurgle; a real recording, heavy, nothing bubbly.
This one: the same launch without the phaser: its author's dry version ('Torpedo launch', from which the chosen one was made by filtering and a phaser) low-passed at 550 Hz like the chosen one: the same muffled rush and thump, smooth, no warble, 1.0 s.
Edit: 1.00 s, the loudest 100 ms band −22.5 dB, peak −8.5 dBFS, −19.8 LUFS.

**Rejected** (round 50, S.6, 2026-10-22: the user kept the chosen shot-torpedo-r03-a); the file is in `rejected/`.

### shot-torpedo-r50-b — "Torpedo launch underwater.wav" by jobro

Proposed (round 50), cut from the HQ previews by `tools/concept/audio/sfx_r50a.py`.
Source: <https://freesound.org/people/jobro/sounds/35530/> (Torpedo launch underwater.wav by jobro, CC-BY 3.0).
Use: every Torpedo Pod shot (wing mount, Levels 11 on and Act 4; Rook's torpedo gun too): its chosen launch (shot-torpedo-r03-a) today.
Brief: the chosen launch as it is, with its bubbly character cut (Q12): the source's 'water' is a phaser sweep over a low-passed launch, so the cut keeps the muffled launch and drops the phaser's gurgle; a real recording, heavy, nothing bubbly.
This one: the chosen launch itself with the phaser's fast sweep and gurgle smoothed out of its spectrum (each frequency's level held steady over 90 ms): closest to the chosen file, a little duller, 1.0 s.
Edit: 1.00 s, the loudest 100 ms band −22.5 dB, peak −8.2 dBFS, −19.0 LUFS.

**Rejected** (round 50, S.6, 2026-10-22: the user kept the chosen shot-torpedo-r03-a); the file is in `rejected/`.

### special-sonar-r50-b — "Sonar_pings.ogg" by Peter_Gross + "submarine sonar.mp3" by Link-Boy

Proposed (round 50), cut from the HQ previews by `tools/concept/audio/sfx_r50a.py`.
Sources: <https://freesound.org/people/Peter_Gross/sounds/12677/> (Sonar_pings.ogg by Peter_Gross, CC0 1.0), <https://freesound.org/people/Link-Boy/sounds/414831/> (submarine sonar.mp3 by Link-Boy, CC0 1.0).
Use: the Sonar Pulse call (the special, from L22's unlock; in water only, at most 4 charges, 1.5 s between pulses): with the teal ring sweeping the field in 0.5 s.
Brief: a soft, heavy, low ping that fills the water, no bubbles, no pops; a = the chosen special-sonar-r04-a (a clean single ping, kept), b only if clearly heavier; a real recording, heavy, nothing bubbly.
This one: a real 625 Hz active sonar ping (a long-range anti-submarine type) slowed to 0.85 (about 530 Hz, a lower and longer 'pinnng' than a's) over a heavy low sonar hit's thump and rumble: the same role as the chosen a, a deeper and weightier ping, 3.0 s.
Edit: 3.00 s, the loudest 100 ms band −13.4 dB, peak −4.1 dBFS, −15.3 LUFS.

**Rejected** (round 50, S.7, 2026-10-22: the user picked a, the chosen special-sonar-r04-a); the file is in `rejected/`.

### special-limit-r50-a — synthesized

Proposed (round 50), synthesized by `tools/concept/audio/sfx_r50a.py` (sfx_r37.py's bell and glass voices).
Use: the special row changing state at once as the ship crosses a waterline (greyed with `UNDER ICE` or `NO WATER`, or usable again; the row flashes once): a few times in Levels 23 and 28. A press on a greyed or empty special keeps the chosen denied buzz (special-denied-r08-a).
Brief: a short soft UI chime (synthesized, never bubbly), the cooldown-ready chime's family but clearly 'changed', not 'ready'.
This one: two soft bells falling, E6 then A5 80 ms later (the ready chime's two bells turned round): 'switched off', light, 0.53 s.
Edit: 0.55 s, the loudest 100 ms band −22.0 dB, peak −14.0 dBFS, −22.4 LUFS.

**Rejected** (round 50, S.8, 2026-10-22: the user picked b); the file is in `rejected/`.

### special-limit-r50-b — synthesized

Proposed (round 50), synthesized by `tools/concept/audio/sfx_r50a.py` (sfx_r37.py's bell and glass voices).
Use: the special row changing state at once as the ship crosses a waterline (greyed with `UNDER ICE` or `NO WATER`, or usable again; the row flashes once): a few times in Levels 23 and 28. A press on a greyed or empty special keeps the chosen denied buzz (special-denied-r08-a).
Brief: a short soft UI chime (synthesized, never bubbly), the cooldown-ready chime's family but clearly 'changed', not 'ready'.
This one: one soft glass fifth an octave lower, C5-G5, muted at 2.5 kHz: a darker, rounder 'tonk' than a, 0.6 s.
Edit: 0.60 s, the loudest 100 ms band −21.9 dB, peak −14.0 dBFS, −25.0 LUFS.

**Chosen** (round 50, S.8: b picked; synthesized, final as it is).

## Concept round 51, step AUD-B (Level 22: the ice geysers, the Drill Rig, the Europa surface beds)

The generator is `tools/concept/audio/sfx_r51b.py` (Freesound HQ previews; the rig's collar and the two beds are seamless loops; nothing synthesized). The station hum is not a sound of its own but the bed `conamara-station` (the ice bed with the hum), section 3's.

### geyser-tell-r51-a — "Ice - Lake fractures" by Vrymaa + "Air (or steam) pressure release" by brunoboselli

Proposed (round 51), cut from the HQ previews by `tools/concept/audio/sfx_r51b.py`.
Sources: <https://freesound.org/people/Vrymaa/sounds/737896/> (Ice - Lake fractures by Vrymaa, CC0 1.0), <https://freesound.org/people/brunoboselli/sounds/457294/> (Air (or steam) pressure release by brunoboselli, CC0 1.0).
Use: every Level 22 ice geyser's 1.2 s tell, as its crack darkens and glows lime-white before the steam column (8 rows of 3 cracks, a row rippling 0.4 s a crack, every 4 s; every 3.2 s in section 3), one per crack, so often.
Brief: a deep ice crack running and splitting under the ship as the pressure builds, with a hiss rising under it: heavy, 1.2 s, rising into the burst, no pop; a real recording, heavy, nothing bubbly or popping.
This one: a frozen lake's ice cracking (a field recording on a real lake) slowed to 0.85, quiet at first, the cracks gathering and growing heavier towards the end, over an air-pressure release's hiss rising with it: a deep, running, glassy crack building to the burst, 1.2 s.
Edit: 1.20 s, the loudest 100 ms band −20.4 dB, peak −6.3 dBFS, −25.7 LUFS.

**Rejected** (round 51, S.1, 2026-10-22: b picked); the file is in `rejected/`.

### geyser-tell-r51-b — "BreakingIce.wav" by iainmccurdy + "Safety relief valve.wav" by tosha73 + "Ice - Lake fractures" by Vrymaa

Proposed (round 51), cut from the HQ previews by `tools/concept/audio/sfx_r51b.py`.
Sources: <https://freesound.org/people/iainmccurdy/sounds/440858/> (BreakingIce.wav by iainmccurdy, CC-BY 4.0), <https://freesound.org/people/tosha73/sounds/544239/> (Safety relief valve.wav by tosha73, CC-BY 4.0), <https://freesound.org/people/Vrymaa/sounds/737896/> (Ice - Lake fractures by Vrymaa, CC0 1.0).
Use: every Level 22 ice geyser's 1.2 s tell, as its crack darkens and glows lime-white before the steam column (8 rows of 3 cracks, a row rippling 0.4 s a crack, every 4 s; every 3.2 s in section 3), one per crack, so often.
Brief: a deep ice crack running and splitting under the ship as the pressure builds, with a hiss rising under it: heavy, 1.2 s, rising into the burst, no pop; a real recording, heavy, nothing bubbly or popping.
This one: ice breaking (a slab cracking piece by piece) slowed to 0.7 and swelling, a steam safety valve's hiss rising under it, and at 0.85 s one deep lake-ice crack slowed to 0.8 as the crack gives: drier and more splintering than a, with a hard last crack, 1.2 s.
Edit: 1.20 s, the loudest 100 ms band −20.4 dB, peak −5.8 dBFS, −24.3 LUFS.

**Chosen** (round 51, S.1: b picked; to be rebuilt from the originals, Freesound 440858, 544239, 737896, when the download limit allows).

### geyser-burst-r51-a — "steam_safety_valve.wav" by Engineer_815 + "Spouting Horn2 Kauai.wav" by tec_studio + "Ice - Lake fractures" by Vrymaa

Proposed (round 51), cut from the HQ previews by `tools/concept/audio/sfx_r51b.py`.
Sources: <https://freesound.org/people/Engineer_815/sounds/493558/> (steam_safety_valve.wav by Engineer_815, CC0 1.0), <https://freesound.org/people/tec_studio/sounds/222641/> (Spouting Horn2 Kauai.wav by tec_studio, CC0 1.0), <https://freesound.org/people/Vrymaa/sounds/737896/> (Ice - Lake fractures by Vrymaa, CC0 1.0).
Use: every Level 22 ice geyser's burst, as its 40 px steam column stands for 0.8 s (one per crack, right after its tell).
Brief: a roaring column of steam and water slush tearing upward out of the ice: heavy, a soft onset (no hiss-pop at the start), 1.5 s with the roar dying away; a real recording, heavy, nothing bubbly or popping.
This one: a boiler's steam safety valve blowing (a loud roaring hiss) over a sea blowhole's spout of water (Spouting Horn, Kauai: air and spray roaring up through rock) and under both a lake-ice boom slowed to 0.8, muffled: a big wet steam roar with a deep thump in it, 1.5 s.
Edit: 1.50 s, the loudest 100 ms band −19.4 dB, peak −4.1 dBFS, −14.1 LUFS.

**Chosen** (round 51, S.2: a picked; to be rebuilt from the originals, Freesound 493558, 222641, 737896, when the download limit allows).

### geyser-burst-r51-b — "S34-24 Steam or fire extinguisher blasts.wav" by craigsmith + "Sea blowhole, breathe. Bufador de mar, respira." by SonoRec + "Digging.wav" by prozaciswack

Proposed (round 51), cut from the HQ previews by `tools/concept/audio/sfx_r51b.py`.
Sources: <https://freesound.org/people/craigsmith/sounds/676005/> (S34-24 Steam or fire extinguisher blasts.wav by craigsmith, CC0 1.0), <https://freesound.org/people/SonoRec/sounds/824087/> (Sea blowhole, breathe. Bufador de mar, respira. by SonoRec, CC0 1.0), <https://freesound.org/people/prozaciswack/sounds/82722/> (Digging.wav by prozaciswack, CC0 1.0).
Use: every Level 22 ice geyser's burst, as its 40 px steam column stands for 0.8 s (one per crack, right after its tell).
Brief: a roaring column of steam and water slush tearing upward out of the ice: heavy, a soft onset (no hiss-pop at the start), 1.5 s with the roar dying away; a real recording, heavy, nothing bubbly or popping.
This one: a steam (or fire extinguisher) blast, a long steady roar, over a sea blowhole breathing out (a surging rush of air and water) and a deep earth rumble under it: more air and surge than a, less steam hiss, 1.5 s.
Edit: 1.50 s, the loudest 100 ms band −19.6 dB, peak −7.1 dBFS, −14.7 LUFS.

**Rejected** (round 51, S.2, 2026-10-22: a picked); the file is in `rejected/`.

### rig-collar-r51-a — "Fracking.ND.DrillRig.Whine.mp3" by fkunze + "Digging.wav" by prozaciswack

Proposed (round 51), cut from the HQ previews by `tools/concept/audio/sfx_r51b.py`.
Sources: <https://freesound.org/people/fkunze/sounds/407454/> (Fracking.ND.DrillRig.Whine.mp3 by fkunze, CC0 1.0), <https://freesound.org/people/prozaciswack/sounds/82722/> (Digging.wav by prozaciswack, CC0 1.0).
Use: while a Drill Rig's collar bores on the screen (Level 22, four rigs; one quiet loop for all of them, as Level 19's belt loop, stopping with the last rig's core).
Brief: a low heavy drill whine with a grinding bite: a big machine boring into ice, steady, low in the mix under the fire, no clicks, a seamless loop; a real recording, heavy, nothing bubbly or popping.
This one: a real oil drill rig's whine (a fracking rig in North Dakota: a steady droning whine with a grind in it) over a deep digging rumble of earth: an industrial drill hard at work, 4.0 s loop.
Edit: 4.00 s, band RMS −34.0 dB, peak −19.7 dBFS, −30.2 LUFS.

**Rejected** (round 51, S.3, 2026-10-22: b picked); the file is in `rejected/`.

### rig-collar-r51-b — "Drilling to the earth's core.wav" by reacthor + "motor industrial turbine large fan blades spin sorta.flac" by kyles

Proposed (round 51), cut from the HQ previews by `tools/concept/audio/sfx_r51b.py`.
Sources: <https://freesound.org/people/reacthor/sounds/130438/> (Drilling to the earth's core.wav by reacthor, CC0 1.0), <https://freesound.org/people/kyles/sounds/453476/> (motor industrial turbine large fan blades spin sorta.flac by kyles, CC0 1.0).
Use: while a Drill Rig's collar bores on the screen (Level 22, four rigs; one quiet loop for all of them, as Level 19's belt loop, stopping with the last rig's core).
Brief: a low heavy drill whine with a grinding bite: a big machine boring into ice, steady, low in the mix under the fire, no clicks, a seamless loop; a real recording, heavy, nothing bubbly or popping.
This one: a deep boring drone (a sound designer's drill sinking towards the earth's core: low, heavy, grinding) with a big industrial turbine's fan blades turning in it: lower and darker than a, more machine-hall than oil field, 4.0 s loop.
Edit: 4.00 s, band RMS −35.0 dB, peak −14.7 dBFS, −27.1 LUFS.

**Chosen** (round 51, S.3: b picked; to be rebuilt from the originals, Freesound 130438, 453476, when the download limit allows).

### rig-ring-r51-a — "Wham n Shatter" by eben-frostey + "20061111AirHoseDisconnect1.wav" by daveincamas

Proposed (round 51), cut from the HQ previews by `tools/concept/audio/sfx_r51b.py`.
Sources: <https://freesound.org/people/eben-frostey/sounds/608685/> (Wham n Shatter by eben-frostey, CC0 1.0), <https://freesound.org/people/daveincamas/sounds/25578/> (20061111AirHoseDisconnect1.wav by daveincamas, CC-BY 4.0).
Use: each ring of 10 ice shards a Drill Rig's collar fires (every 3.5 s while its pumps live, slower as they die; Level 22, up to two rigs at once).
Brief: the collar spitting a ring of ice shards: a hard icy crack-and-shatter with a burst of pressure behind it, short and dry, not a gunshot, never bubbly; a real recording, heavy, nothing bubbly or popping.
This one: a hammer smashing a block of ice (the whack and the shards flying) on a compressed-air hose's burst: sharp, bright and icy, 0.7 s.
Edit: 0.70 s, the loudest 100 ms band −22.2 dB, peak −12.0 dBFS, −22.0 LUFS.

**Rejected** (round 51, S.4, 2026-10-22: b picked); the file is in `rejected/`.

### rig-ring-r51-b — "Chipping large Ice Block 1.wav" by kev_durr + "steam_burst.wav" by pengo_au

Proposed (round 51), cut from the HQ previews by `tools/concept/audio/sfx_r51b.py`.
Sources: <https://freesound.org/people/kev_durr/sounds/426205/> (Chipping large Ice Block 1.wav by kev_durr, CC-BY 4.0), <https://freesound.org/people/pengo_au/sounds/90143/> (steam_burst.wav by pengo_au, CC0 1.0).
Use: each ring of 10 ice shards a Drill Rig's collar fires (every 3.5 s while its pumps live, slower as they die; Level 22, up to two rigs at once).
Brief: the collar spitting a ring of ice shards: a hard icy crack-and-shatter with a burst of pressure behind it, short and dry, not a gunshot, never bubbly; a real recording, heavy, nothing bubbly or popping.
This one: a large block of ice chipped (one hard crack and the chips scattering) slowed to 0.8, on a steam piston's blast muffled at 3 kHz: heavier and lower than a, more crack than shatter, 0.8 s.
Edit: 0.80 s, the loudest 100 ms band −22.3 dB, peak −7.3 dBFS, −20.4 LUFS.

**Chosen** (round 51, S.4: b picked; to be rebuilt from the originals, Freesound 426205, 90143, when the download limit allows).

### rig-death-r51-a — "R29-28-Large Metal Vehicle Crashes.wav" by craigsmith + "Ice - Lake fractures" by Vrymaa + "Metal Groan_Steam release.wav" by Cell31_Sound_Productions

Proposed (round 51), cut from the HQ previews by `tools/concept/audio/sfx_r51b.py`.
Sources: <https://freesound.org/people/craigsmith/sounds/487143/> (R29-28-Large Metal Vehicle Crashes.wav by craigsmith, CC0 1.0), <https://freesound.org/people/Vrymaa/sounds/737896/> (Ice - Lake fractures by Vrymaa, CC0 1.0), <https://freesound.org/people/Cell31_Sound_Productions/sounds/390188/> (Metal Groan_Steam release.wav by Cell31_Sound_Productions, CC-BY 4.0).
Use: a Drill Rig's core destroyed (Level 22, four rigs; with the `large` ground explosion): the collar stops, the ice cracks and a steam plume rises.
Brief: a big structure failing: metal crashing and tearing, ice cracking under it and a steam plume rising, heavy, about 3 s, a few dB under the large explosion it plays with; a real recording, heavy, nothing bubbly or popping.
This one: a large metal vehicle crashing (a long tearing crash with debris, a vintage film recording) on a lake-ice boom slowed to 0.75 and muffled, then from 0.7 s a metal structure's groan ending in a steam release: the collar tearing apart and the plume escaping, 3.1 s.
Edit: 3.10 s, the loudest 100 ms band −17.1 dB, peak −5.8 dBFS, −15.1 LUFS.

**Rejected** (round 51, S.5, 2026-10-22: b picked); the file is in `rejected/`.

### rig-death-r51-b — "Big Structure Collision Heard from Inside" by Osvoldon + "BreakingIce.wav" by iainmccurdy + "Air (or steam) pressure release" by brunoboselli

Proposed (round 51), cut from the HQ previews by `tools/concept/audio/sfx_r51b.py`.
Sources: <https://freesound.org/people/Osvoldon/sounds/653294/> (Big Structure Collision Heard from Inside by Osvoldon, CC0 1.0), <https://freesound.org/people/iainmccurdy/sounds/440858/> (BreakingIce.wav by iainmccurdy, CC-BY 4.0), <https://freesound.org/people/brunoboselli/sounds/457294/> (Air (or steam) pressure release by brunoboselli, CC0 1.0).
Use: a Drill Rig's core destroyed (Level 22, four rigs; with the `large` ground explosion): the collar stops, the ice cracks and a steam plume rises.
Brief: a big structure failing: metal crashing and tearing, ice cracking under it and a steam plume rising, heavy, about 3 s, a few dB under the large explosion it plays with; a real recording, heavy, nothing bubbly or popping.
This one: a big structure's collision heard from inside (a vast deep boom and rumble), ice breaking up around it slowed to 0.8, and from 0.5 s a pressure release's hiss as the plume rises: deeper and less metallic than a, the ice in front, 3.0 s.
Edit: 3.00 s, the loudest 100 ms band −20.7 dB, peak −2.1 dBFS, −15.3 LUFS.

**Chosen** (round 51, S.5: b picked; to be rebuilt from the originals, Freesound 653294, 440858, 457294, when the download limit allows).

### europa-ice-r51-a — "Glacial Wind.wav" by Uzbazur + "Ice - Lake fractures" by Vrymaa

Proposed (round 51), cut from the HQ previews by `tools/concept/audio/sfx_r51b.py`.
Sources: <https://freesound.org/people/Uzbazur/sounds/432086/> (Glacial Wind.wav by Uzbazur, CC-BY 4.0), <https://freesound.org/people/Vrymaa/sounds/737896/> (Ice - Lake fractures by Vrymaa, CC0 1.0).
Use: Level 22 throughout (crossfading to `conamara-station` in section 3), Level 23's shaft mouth and Level 28 after the breakout: the ambience bed of Europa's surface.
Brief: a cold wind over open ice with deep ice groans and booms rolling under it far off; quiet, under the music, no hiss-pops, no bubbles, a seamless loop; a real recording, heavy, nothing bubbly or popping.
This one: a glacial wind (a steady, cold, hollow blow) over a frozen lake's fractures played an octave down and muffled, so the cracks become deep booms and groans rolling under it now and then: 16.0 s loop.
Edit: 16.00 s, band RMS −37.5 dB, peak −13.2 dBFS, −28.1 LUFS.

**Chosen** (round 51, S.6: a picked; to be rebuilt from the originals, Freesound 432086, 737896, when the download limit allows).

### europa-ice-r51-b — "Wind__Artic__Cold.wav" by cobratronik + "Ice 3.wav" by Benboncan

Proposed (round 51), cut from the HQ previews by `tools/concept/audio/sfx_r51b.py`.
Sources: <https://freesound.org/people/cobratronik/sounds/117136/> (Wind__Artic__Cold.wav by cobratronik, CC0 1.0), <https://freesound.org/people/Benboncan/sounds/112628/> (Ice 3.wav by Benboncan, CC-BY 4.0).
Use: Level 22 throughout (crossfading to `conamara-station` in section 3), Level 23's shaft mouth and Level 28 after the breakout: the ambience bed of Europa's surface.
Brief: a cold wind over open ice with deep ice groans and booms rolling under it far off; quiet, under the music, no hiss-pops, no bubbles, a seamless loop; a real recording, heavy, nothing bubbly or popping.
This one: a cold arctic wind (a low moan with gusts; the same recording's moan is under `mars-dust`) over ice creaking and groaning (a hydrophone recording in lake ice) slowed to 0.6 and muffled: more creak and groan than boom, closer than a, 16.0 s loop.
Edit: 16.00 s, band RMS −36.1 dB, peak −15.6 dBFS, −28.7 LUFS.

**Rejected** (round 51, S.6, 2026-10-22: a picked); the file is in `rejected/`.

### conamara-station-r51-a — "Glacial Wind.wav" by Uzbazur + "Ice - Lake fractures" by Vrymaa + "Arena Underground Ventilation Hum and Rumble" by jamesdrake89 + "hum electric transformer in large room1.flac" by kyles

Proposed (round 51), cut from the HQ previews by `tools/concept/audio/sfx_r51b.py`.
Sources: <https://freesound.org/people/Uzbazur/sounds/432086/> (Glacial Wind.wav by Uzbazur, CC-BY 4.0), <https://freesound.org/people/Vrymaa/sounds/737896/> (Ice - Lake fractures by Vrymaa, CC0 1.0), <https://freesound.org/people/jamesdrake89/sounds/671456/> (Arena Underground Ventilation Hum and Rumble by jamesdrake89, CC0 1.0), <https://freesound.org/people/kyles/sounds/453444/> (hum electric transformer in large room1.flac by kyles, CC0 1.0).
Use: Level 22's section 3 (t=104 on), the run-in to Conamara Station: the ice bed with the station's hum, crossfading in from `europa-ice` (one bed, no positional hum).
Brief: the ice bed with a big building's ventilation hum and a low transformer drone under the wind: inhabited, mechanical, steady; quiet, a seamless loop; a real recording, heavy, nothing bubbly or popping.
This one: europa-ice a (the glacial wind and the deep booms, 2 dB quieter) with an underground arena's ventilation hum and rumble and an electric transformer's hum in a large room under it: the wind over a building that breathes, 16.0 s loop.
Edit: 16.00 s, band RMS −36.1 dB, peak −16.5 dBFS, −28.4 LUFS.

**Chosen** (round 51, S.7: a picked; to be rebuilt from the originals, Freesound 432086, 737896, 671456, 453444, when the download limit allows).

### conamara-station-r51-b — "Wind__Artic__Cold.wav" by cobratronik + "Ice 3.wav" by Benboncan + "motor industrial turbine large fan blades spin sorta.flac" by kyles + "Large machine running and powering down" by ryks88

Proposed (round 51), cut from the HQ previews by `tools/concept/audio/sfx_r51b.py`.
Sources: <https://freesound.org/people/cobratronik/sounds/117136/> (Wind__Artic__Cold.wav by cobratronik, CC0 1.0), <https://freesound.org/people/Benboncan/sounds/112628/> (Ice 3.wav by Benboncan, CC-BY 4.0), <https://freesound.org/people/kyles/sounds/453476/> (motor industrial turbine large fan blades spin sorta.flac by kyles, CC0 1.0), <https://freesound.org/people/ryks88/sounds/709258/> (Large machine running and powering down by ryks88, CC-BY 4.0).
Use: Level 22's section 3 (t=104 on), the run-in to Conamara Station: the ice bed with the station's hum, crossfading in from `europa-ice` (one bed, no positional hum).
Brief: the ice bed with a big building's ventilation hum and a low transformer drone under the wind: inhabited, mechanical, steady; quiet, a seamless loop; a real recording, heavy, nothing bubbly or popping.
This one: europa-ice b (the arctic wind and the ice groans, 2 dB quieter) with a large industrial turbine fan turning and a big machine running under it, muffled: busier and more industrial than a, 16.0 s loop.
Edit: 16.00 s, band RMS −36.1 dB, peak −18.0 dBFS, −29.7 LUFS.

**Rejected** (round 51, S.7, 2026-10-22: a picked); the file is in `rejected/`.

## Concept round 52, step AUD-C (Level 23: the waterline, the eels, the Glassfin, the siphon pumps, the bed under the ice; the under-water explosions)

The generator is `tools/concept/audio/sfx_r52c.py` (Freesound HQ previews; the pump hum and the bed are seamless loops; nothing synthesized). The under-water explosion rungs are two families: a "the depth bomb" (a depth bomb's sub boom on a real under-water impact), b "the hydrophone" (a snapping shrimp slowed 32 times on a quarry blast heard from 800 m).

### waterline-cross-r52-a — "Eurofighter Typhoon Flyby 005 – Close Proximity" by TimoSchmied + "Underwater Impact" by deleted_user_7709760 + "DepthBomb_08.aif" by ZeSoundResearchInc. + "Deep Sea Ambience" by jhumbucker

Proposed (round 52), cut from the HQ previews by `tools/concept/audio/sfx_r52c.py`.
Sources: <https://freesound.org/people/TimoSchmied/sounds/640505/> (Eurofighter Typhoon Flyby 005 – Close Proximity by TimoSchmied, CC-BY 4.0), <https://freesound.org/people/deleted_user_7709760/sounds/400793/> (Underwater Impact by deleted_user_7709760, CC-BY 3.0), <https://freesound.org/people/ZeSoundResearchInc./sounds/147877/> (DepthBomb_08.aif by ZeSoundResearchInc., CC0 1.0), <https://freesound.org/people/jhumbucker/sounds/193822/> (Deep Sea Ambience by jhumbucker, CC0 1.0).
Use: Level 23 at t=25.5, once a run: the ship (and Rook) crossing the waterline at the foot of the dry shaft mouth into the water, as the screen's grade wipes to under water and the NO WATER rows flash back on.
Brief: a heavy submersion swell: the air's roar cut off into a deep, muffled whoomph, the world closing in; about 2 s, no splash, no plop, no bubbles; a real recording, heavy, nothing bubbly or popping.
This one: a fighter jet's roar (a close Eurofighter flyby) swelling for half a second and cut off dead, and in its place an under-water impact slowed to 0.8 and muffled at 1.5 kHz over a depth bomb's sub boom, with a deep-sea rumble settling in under it: the roar shut off by the water, a deep whoomph and the pressure, 2.0 s.
Edit: 2.03 s, the loudest 100 ms band −20.0 dB, peak −6.8 dBFS, −20.4 LUFS.
**Chosen** (round 52, S.1: a picked; to be rebuilt from the originals when the download limit allows).

### waterline-cross-r52-b — "Passenger jet fly-by.wav" by digifishmusic + "Snapping_Shrimp_Slowed_32X.mp3" by digifishmusic + "bolivar_waves_underwater.flac" by wjoojoo

Proposed (round 52), cut from the HQ previews by `tools/concept/audio/sfx_r52c.py`.
Sources: <https://freesound.org/people/digifishmusic/sounds/54967/> (Passenger jet fly-by.wav by digifishmusic, CC-BY 4.0), <https://freesound.org/people/digifishmusic/sounds/45439/> (Snapping_Shrimp_Slowed_32X.mp3 by digifishmusic, CC-BY 4.0), <https://freesound.org/people/wjoojoo/sounds/258376/> (bolivar_waves_underwater.flac by wjoojoo, CC-BY 4.0).
Use: Level 23 at t=25.5, once a run: the ship (and Rook) crossing the waterline at the foot of the dry shaft mouth into the water, as the screen's grade wipes to under water and the NO WATER rows flash back on.
Brief: a heavy submersion swell: the air's roar cut off into a deep, muffled whoomph, the world closing in; about 2 s, no splash, no plop, no bubbles; a real recording, heavy, nothing bubbly or popping.
This one: an airliner's roar at its loudest (a passenger jet passing over) swelling and cut off dead, and in its place a snapping shrimp's click slowed 32 times (a real hydrophone recording: a clean deep boom) with a wave surging over a hydrophone, slowed and muffled: rounder and wetter than a, the boom lower and cleaner, 1.88 s.
Edit: 1.88 s, the loudest 100 ms band −20.1 dB, peak −3.4 dBFS, −19.4 LUFS.
**Rejected** (round 52, S.1, 2026-10-22: a picked); the file is in `rejected/`.

### hydro-refit-r52-a — "deadbolt on bathroom wall castle dungeon heavy metal door pull in and out echo latch lock unlock squeak on offmic more hollow.wav" by kyles + "Heavy_mechanical_impact_sounds_various_pitch_sound" by Artninja + "Hydraulic Truck Brakes" by Filmscore

Proposed (round 52), cut from the HQ previews by `tools/concept/audio/sfx_r52c.py`.
Sources: <https://freesound.org/people/kyles/sounds/407293/> (deadbolt on bathroom wall castle dungeon heavy metal door pull in and out echo latch lock unlock squeak on offmic more hollow.wav by kyles, CC0 1.0), <https://freesound.org/people/Artninja/sounds/728170/> (Heavy_mechanical_impact_sounds_various_pitch_sound by Artninja, CC-BY 4.0), <https://freesound.org/people/Filmscore/sounds/823069/> (Hydraulic Truck Brakes by Filmscore, CC0 1.0).
Use: Level 23 at the waterline crossing, once a run, with the crossing: the Stormhawk's hydro-kit locking for water (the refit tell).
Brief: heavy clamps closing and a hydraulic seal: two hard metal locks and a short pressure hiss, 0.6 s, mechanical, no clicky toy sound; a real recording, heavy, nothing bubbly or popping.
This one: a heavy dungeon-door deadbolt slammed home, then a heavy mechanical clunk (a microwave door slammed, a big low impact) slowed to 0.85, and a truck's hydraulic brake hiss under it: clamp, clamp and the seal, 0.6 s.
Edit: 0.60 s, the loudest 100 ms band −21.1 dB, peak −5.2 dBFS, −19.2 LUFS.
**Chosen** (round 52, S.2: a picked; to be rebuilt from the originals when the download limit allows).

### hydro-refit-r52-b — "Open Vault" by Funkelfang + "Door Lock On, Off" by microman502 + "machine hydraulic metal cutter close crunch1.flac" by kyles

Proposed (round 52), cut from the HQ previews by `tools/concept/audio/sfx_r52c.py`.
Sources: <https://freesound.org/people/Funkelfang/sounds/844296/> (Open Vault by Funkelfang, CC-BY 4.0), <https://freesound.org/people/microman502/sounds/818290/> (Door Lock On, Off by microman502, CC0 1.0), <https://freesound.org/people/kyles/sounds/453303/> (machine hydraulic metal cutter close crunch1.flac by kyles, CC0 1.0).
Use: Level 23 at the waterline crossing, once a run, with the crossing: the Stormhawk's hydro-kit locking for water (the refit tell).
Brief: heavy clamps closing and a hydraulic seal: two hard metal locks and a short pressure hiss, 0.6 s, mechanical, no clicky toy sound; a real recording, heavy, nothing bubbly or popping.
This one: a vault's heavy bolts thrown, then a heavy door lock engaging, over a hydraulic metal cutter closing slowed to 0.8 (the press): more bolt-work and grind than a, less hiss, 0.6 s.
Edit: 0.60 s, the loudest 100 ms band −21.1 dB, peak −4.8 dBFS, −19.6 LUFS.
**Rejected** (round 52, S.2, 2026-10-22: a picked); the file is in `rejected/`.

### glassfin-shatter-r52-a — "Glass Break" by unfa

Proposed (round 52), cut from the HQ previews by `tools/concept/audio/sfx_r52c.py`.
Source: <https://freesound.org/people/unfa/sounds/221528/> (Glass Break by unfa, CC0 1.0).
Use: every Glassfin killed (Level 23 on; schools of ten or more tiny glassy fish, so often), with the under-water `tiny` burst.
Brief: a thin glass shatter: small, dry and bright, short, never a clink-and-tinkle tail that lingers; a real recording, heavy, nothing bubbly or popping.
This one: a small sheet of glass shattered with a rock: one sharp crack and the shards falling, 0.5 s.
Edit: 0.52 s, the loudest 100 ms band −21.3 dB, peak −4.3 dBFS, −20.7 LUFS.
**Rejected** (round 52, S.3, 2026-10-22: b picked); the file is in `rejected/`.

### glassfin-shatter-r52-b — "glass-shattering_04.ogg" by C_Rogers

Proposed (round 52), cut from the HQ previews by `tools/concept/audio/sfx_r52c.py`.
Source: <https://freesound.org/people/C_Rogers/sounds/203372/> (glass-shattering_04.ogg by C_Rogers, CC0 1.0).
Use: every Glassfin killed (Level 23 on; schools of ten or more tiny glassy fish, so often), with the under-water `tiny` burst.
Brief: a thin glass shatter: small, dry and bright, short, never a clink-and-tinkle tail that lingers; a real recording, heavy, nothing bubbly or popping.
This one: a glass shattering in a bucket (a dull, boxed-in break) slowed to 0.9: shorter and drier than a, less ring, 0.45 s.
Edit: 0.45 s, the loudest 100 ms band −29.1 dB, peak −10.0 dBFS, −19.6 LUFS.
**Chosen** (round 52, S.3: b picked; to be rebuilt from the originals when the download limit allows).

### eel-pulse-r52-a — "Deep Thumps.wav" by eardeer + "Underwater Impact" by deleted_user_7709760

Proposed (round 52), cut from the HQ previews by `tools/concept/audio/sfx_r52c.py`.
Sources: <https://freesound.org/people/eardeer/sounds/400997/> (Deep Thumps.wav by eardeer, CC0 1.0), <https://freesound.org/people/deleted_user_7709760/sounds/400793/> (Underwater Impact by deleted_user_7709760, CC-BY 3.0).
Use: each pulse shot an Eel Swarm's head fires (Level 23 on).
Brief: a dull pressure thump: short, heavy and soft-edged, a push of water rather than a gunshot; no squelch; a real recording, heavy, nothing bubbly or popping.
This one: a deep thump (a washing-machine door slammed, recorded close: a huge-gate-like boom) muffled at 1.5 kHz, over an under-water impact: a short round thud, 0.4 s.
Edit: 0.40 s, the loudest 100 ms band −23.0 dB, peak −8.3 dBFS, −inf LUFS.
**Rejected** (round 52, S.4, 2026-10-22: b picked); the file is in `rejected/`.

### eel-pulse-r52-b — "Underwater Impact" by deleted_user_7709760 + "Cannon Fires, Take 1, Quarter Speed" by CHallSmith

Proposed (round 52), cut from the HQ previews by `tools/concept/audio/sfx_r52c.py`.
Sources: <https://freesound.org/people/deleted_user_7709760/sounds/400793/> (Underwater Impact by deleted_user_7709760, CC-BY 3.0), <https://freesound.org/people/CHallSmith/sounds/870742/> (Cannon Fires, Take 1, Quarter Speed by CHallSmith, CC0 1.0).
Use: each pulse shot an Eel Swarm's head fires (Level 23 on).
Brief: a dull pressure thump: short, heavy and soft-edged, a push of water rather than a gunshot; no squelch; a real recording, heavy, nothing bubbly or popping.
This one: an under-water impact over an air cannon's blast at quarter speed, muffled at 1 kHz: a soft, pushing whump with more air in it than a, 0.45 s.
Edit: 0.45 s, the loudest 100 ms band −23.0 dB, peak −11.0 dBFS, −23.1 LUFS.
**Chosen** (round 52, S.4: b picked; to be rebuilt from the originals when the download limit allows).

### eel-segment-r52-a — "clothing rip tear fabric various long short or electric short circuit crackle zap or masking tape.flac" by kyles + "Underwater Impact" by deleted_user_7709760

Proposed (round 52), cut from the HQ previews by `tools/concept/audio/sfx_r52c.py`.
Sources: <https://freesound.org/people/kyles/sounds/455680/> (clothing rip tear fabric various long short or electric short circuit crackle zap or masking tape.flac by kyles, CC0 1.0), <https://freesound.org/people/deleted_user_7709760/sounds/400793/> (Underwater Impact by deleted_user_7709760, CC-BY 3.0).
Use: each Eel Swarm segment destroyed (Level 23 on; segments die in quick runs along the chain), with the under-water `tiny` or `small` burst.
Brief: a wet tearing slide, short: something tough tearing loose; no squelch, no slurp; a real recording, heavy, nothing bubbly or popping.
This one: a long rip of heavy fabric slowed to 0.8 and softened at 5 kHz, over an under-water impact's tail: a tearing slide with a damp body, 0.6 s.
Edit: 0.60 s, the loudest 100 ms band −21.2 dB, peak −6.3 dBFS, −18.0 LUFS.
**Rejected** (round 52, S.5, 2026-10-22: b picked); the file is in `rejected/`.

### eel-segment-r52-b — "Rubber Stretch Rip 1.wav" by mickdow

Proposed (round 52), cut from the HQ previews by `tools/concept/audio/sfx_r52c.py`.
Source: <https://freesound.org/people/mickdow/sounds/253327/> (Rubber Stretch Rip 1.wav by mickdow, CC0 1.0).
Use: each Eel Swarm segment destroyed (Level 23 on; segments die in quick runs along the chain), with the under-water `tiny` or `small` burst.
Brief: a wet tearing slide, short: something tough tearing loose; no squelch, no slurp; a real recording, heavy, nothing bubbly or popping.
This one: stretched rubber ripping (a live recording) slowed to 0.75 and softened at 4.5 kHz: rougher and more rubbery than a, a gristly tear, 0.6 s.
Edit: 0.60 s, the loudest 100 ms band −21.0 dB, peak −4.2 dBFS, −18.1 LUFS.
**Chosen** (round 52, S.5: b picked; to be rebuilt from the originals when the download limit allows).

### pump-hum-r52-a — "Basement Water Pump.flac" by qubodup + "Industrial Centrifugal Water Pump contactmic 130203_00.wav" by klankbeeld

Proposed (round 52), cut from the HQ previews by `tools/concept/audio/sfx_r52c.py`.
Sources: <https://freesound.org/people/qubodup/sounds/189314/> (Basement Water Pump.flac by qubodup, CC0 1.0), <https://freesound.org/people/klankbeeld/sounds/177195/> (Industrial Centrifugal Water Pump contactmic 130203_00.wav by klankbeeld, CC-BY 4.0).
Use: while a siphon pump lives on the screen (Level 23's section 3, five farm racks, each with a pump; one quiet loop for all of them, as the Drill Rig's collar, stopping with the last pump).
Brief: a deep pump throb: a big water pump labouring in a steady slow rhythm, muffled, low in the mix under the fire, no clicks, a seamless loop; a real recording, heavy, nothing bubbly or popping.
This one: a basement water pump's rhythmic throb slowed to 0.7 (a surge about every 2 s) over an industrial centrifugal water pump's hum (a contact-mic recording) slowed the same: a labouring pump, 4.0 s loop.
Edit: 4.00 s, band RMS −34.0 dB, peak −16.3 dBFS, −28.6 LUFS.
**Rejected** (round 52, S.6, 2026-10-22: b picked); the file is in `rejected/`.

### pump-hum-r52-b — "indutrial hydraulic pump LOOP 160531_0886.wav" by klankbeeld + "big_drone_lowered.wav" by decembered

Proposed (round 52), cut from the HQ previews by `tools/concept/audio/sfx_r52c.py`.
Sources: <https://freesound.org/people/klankbeeld/sounds/346815/> (indutrial hydraulic pump LOOP 160531_0886.wav by klankbeeld, CC-BY 4.0), <https://freesound.org/people/decembered/sounds/140217/> (big_drone_lowered.wav by decembered, CC0 1.0).
Use: while a siphon pump lives on the screen (Level 23's section 3, five farm racks, each with a pump; one quiet loop for all of them, as the Drill Rig's collar, stopping with the last pump).
Brief: a deep pump throb: a big water pump labouring in a steady slow rhythm, muffled, low in the mix under the fire, no clicks, a seamless loop; a real recording, heavy, nothing bubbly or popping.
This one: a large industrial hydraulic piston pump slowed to 0.6 (its whine dropped to a groan) over a water pump's drone pitched down: steadier and more of a machine-hall groan than a's throb, 4.0 s loop.
Edit: 4.00 s, band RMS −36.8 dB, peak −16.3 dBFS, −27.0 LUFS.
**Chosen** (round 52, S.6: b picked; to be rebuilt from the originals when the download limit allows).

### pump-break-r52-a — "Heavy_mechanical_impact_sounds_various_pitch_sound" by Artninja + "G45-18-Submarine Motor Stop.wav" by craigsmith + "Air (or steam) pressure release" by brunoboselli

Proposed (round 52), cut from the HQ previews by `tools/concept/audio/sfx_r52c.py`.
Sources: <https://freesound.org/people/Artninja/sounds/728170/> (Heavy_mechanical_impact_sounds_various_pitch_sound by Artninja, CC-BY 4.0), <https://freesound.org/people/craigsmith/sounds/438720/> (G45-18-Submarine Motor Stop.wav by craigsmith, CC0 1.0), <https://freesound.org/people/brunoboselli/sounds/457294/> (Air (or steam) pressure release by brunoboselli, CC0 1.0).
Use: a siphon pump destroyed (Level 23, five, the secondary; with its `medium` under-water burst): the pump stops and its line vents.
Brief: a heavy mechanical failure with a pressure release: a hard clunk, the machine dying and a rush of pressure; about 2 s, a few dB under the burst it plays with; a real recording, heavy, nothing bubbly or popping.
This one: a heavy mechanical clunk, then a submarine's motor running down to a stop with a rattle (a vintage film-library recording) slowed to 0.75, and a pressure release's hiss rising under it: the pump seizing and venting, 1.85 s.
Edit: 1.85 s, the loudest 100 ms band −18.1 dB, peak −4.2 dBFS, −18.4 LUFS.
**Rejected** (round 52, S.7, 2026-10-22: b picked); the file is in `rejected/`.

### pump-break-r52-b — "machine hydraulic metal cutter close crunch1.flac" by kyles + "Faucet or pipes strain" by jhumbucker + "Hydraulic Truck Brakes" by Filmscore

Proposed (round 52), cut from the HQ previews by `tools/concept/audio/sfx_r52c.py`.
Sources: <https://freesound.org/people/kyles/sounds/453303/> (machine hydraulic metal cutter close crunch1.flac by kyles, CC0 1.0), <https://freesound.org/people/jhumbucker/sounds/193820/> (Faucet or pipes strain by jhumbucker, CC0 1.0), <https://freesound.org/people/Filmscore/sounds/823069/> (Hydraulic Truck Brakes by Filmscore, CC0 1.0).
Use: a siphon pump destroyed (Level 23, five, the secondary; with its `medium` under-water burst): the pump stops and its line vents.
Brief: a heavy mechanical failure with a pressure release: a hard clunk, the machine dying and a rush of pressure; about 2 s, a few dB under the burst it plays with; a real recording, heavy, nothing bubbly or popping.
This one: a hydraulic metal cutter's crunch slowed to 0.8, a pipe straining and giving, and a truck's hydraulic brake hiss: more crunch and burst than a, less motor, 1.4 s.
Edit: 1.40 s, the loudest 100 ms band −18.6 dB, peak −2.4 dBFS, −15.5 LUFS.
**Chosen** (round 52, S.7: b picked; to be rebuilt from the originals when the download limit allows).

### scrape-under-r52-a — "big metal side impact 3 scraping.wav" by Halleck + "Underwater Impact" by deleted_user_7709760

Proposed (round 52), cut from the HQ previews by `tools/concept/audio/sfx_r52c.py`.
Sources: <https://freesound.org/people/Halleck/sounds/121623/> (big metal side impact 3 scraping.wav by Halleck, CC-BY 4.0), <https://freesound.org/people/deleted_user_7709760/sounds/400793/> (Underwater Impact by deleted_user_7709760, CC-BY 3.0).
Use: the ship (or Rook) touching the shaft or trench wall under water (Levels 23 and 25; the canyon scrape's rule: once per contact, a little under the armour or shield hit, panned to the touching side).
Brief: metal grinding on ice or rock, muffled by the water: a hard onset and a rough tail that reads as damage, 0.6 s; a real recording, heavy, nothing bubbly or popping.
This one: the chosen canyon scrape's recording (a big metal side impact scraping) slowed to 0.8 and muffled at 2.5 kHz, on an under-water impact: the screech gone dull and heavy, 0.6 s.
Edit: 0.60 s, the loudest 100 ms band −18.0 dB, peak −6.4 dBFS, −15.9 LUFS.
**Chosen** (round 52, S.8: a picked; to be rebuilt from the originals when the download limit allows).

### scrape-under-r52-b — "Freezer Ice Scrape Knife 22.wav" by Glitchedtones + "Deep Thumps.wav" by eardeer

Proposed (round 52), cut from the HQ previews by `tools/concept/audio/sfx_r52c.py`.
Sources: <https://freesound.org/people/Glitchedtones/sounds/380252/> (Freezer Ice Scrape Knife 22.wav by Glitchedtones, CC0 1.0), <https://freesound.org/people/eardeer/sounds/400997/> (Deep Thumps.wav by eardeer, CC0 1.0).
Use: the ship (or Rook) touching the shaft or trench wall under water (Levels 23 and 25; the canyon scrape's rule: once per contact, a little under the armour or shield hit, panned to the touching side).
Brief: metal grinding on ice or rock, muffled by the water: a hard onset and a rough tail that reads as damage, 0.6 s; a real recording, heavy, nothing bubbly or popping.
This one: a knife scraping freezer ice slowed to 0.6 and muffled at 3 kHz, on a deep thump: metal on ice, grittier and more juddering than a, 0.6 s.
Edit: 0.60 s, the loudest 100 ms band −18.1 dB, peak −4.3 dBFS, −19.3 LUFS.
**Rejected** (round 52, S.8, 2026-10-22: a picked); the file is in `rejected/`.

### europa-under-r52-a — "Deep Sea Ambience" by jhumbucker + "Stereotypical sinking submarine sound (NO REVERB).aif" by Ubehag + "humpback_whales_and_orcas_ambient_underwater_recording" by raiden04

Proposed (round 52), cut from the HQ previews by `tools/concept/audio/sfx_r52c.py`.
Sources: <https://freesound.org/people/jhumbucker/sounds/193822/> (Deep Sea Ambience by jhumbucker, CC0 1.0), <https://freesound.org/people/Ubehag/sounds/232021/> (Stereotypical sinking submarine sound (NO REVERB).aif by Ubehag, CC0 1.0), <https://freesound.org/people/raiden04/sounds/797762/> (humpback_whales_and_orcas_ambient_underwater_recording by raiden04, CC-BY 4.0).
Use: Level 23 from the waterline on (crossfading in from `europa-ice` at the crossing) and Level 24: the ambience bed under Europa's ice.
Brief: muffled deep water: a heavy pressure rumble, hull creaks now and then and far whale-like calls; quiet, under the music, no bubbles, no drips, a seamless loop; a real recording, heavy, nothing bubbly or popping.
This one: a deep-sea rumble (low, heavy, a steady pressure) with a sinking submarine's steel creaks slowed to 0.6 and muffled, and humpback whales calling far off (a real hydrophone recording) slowed to 0.8: 16.0 s loop.
Edit: 16.00 s, band RMS −38.7 dB, peak −11.1 dBFS, −28.0 LUFS.
**Chosen** (round 52, S.9: a picked; to be rebuilt from the originals when the download limit allows).

### europa-under-r52-b — "Large Vessel.wav" by duckduckpony + "wood creaks deep long door or ship hull1.flac" by kyles + "Blue Whale B call @ 5X" by MBARI_MARS

Proposed (round 52), cut from the HQ previews by `tools/concept/audio/sfx_r52c.py`.
Sources: <https://freesound.org/people/duckduckpony/sounds/130506/> (Large Vessel.wav by duckduckpony, CC-BY 4.0), <https://freesound.org/people/kyles/sounds/452107/> (wood creaks deep long door or ship hull1.flac by kyles, CC0 1.0), <https://freesound.org/people/MBARI_MARS/sounds/343682/> (Blue Whale B call @ 5X by MBARI_MARS, CC-BY 4.0).
Use: Level 23 from the waterline on (crossfading in from `europa-ice` at the crossing) and Level 24: the ambience bed under Europa's ice.
Brief: muffled deep water: a heavy pressure rumble, hull creaks now and then and far whale-like calls; quiet, under the music, no bubbles, no drips, a seamless loop; a real recording, heavy, nothing bubbly or popping.
This one: a large vessel's low rumble passing under water (deep, a little metallic) muffled at 1.5 kHz, a deep wooden hull creaking slowed to 0.5 and muffled, and a blue whale's B call (a real hydrophone recording, played at 3.5 times its speed) far off: darker and emptier than a, two long moans a loop, 16.0 s loop.
Edit: 16.00 s, band RMS −46.4 dB, peak −12.7 dBFS, −28.0 LUFS.
**Rejected** (round 52, S.9, 2026-10-22: a picked); the file is in `rejected/`.

### explosion-underwater-tiny-r52-a — "Underwater Impact" by deleted_user_7709760 + "DepthBomb_06.aif" by ZeSoundResearchInc.

Proposed (round 52), cut from the HQ previews by `tools/concept/audio/sfx_r52c.py`.
Sources: <https://freesound.org/people/deleted_user_7709760/sounds/400793/> (Underwater Impact by deleted_user_7709760, CC-BY 3.0), <https://freesound.org/people/ZeSoundResearchInc./sounds/147879/> (DepthBomb_06.aif by ZeSoundResearchInc., CC0 1.0).
Use: every kill of a `tiny` unit below the waterline (Levels 23-28, e.g. Glassfin, eel segments, shootable torpedoes and mines), in place of the air rung (the game picks a random variant per kill, ±4 % pitch).
Brief: a deep, heavy pressure boom heard under water: a muffled body and a long low tail, no splash, no bubble rattle, nothing that pops; at most 0.6 s; a real recording, heavy, nothing bubbly or popping.
This one: the depth bomb: an under-water impact muffled at 1.5 kHz over a depth bomb's sub boom, cut short: a small deep thud, 0.5 s.
Edit: 0.50 s, the loudest 100 ms band −17.1 dB, peak −3.9 dBFS, −13.7 LUFS.
**Chosen** (round 53, S.10: a picked; to be rebuilt from the originals when the download limit allows).

### explosion-underwater-tiny-r52-b — "Snapping_Shrimp_Slowed_32X.mp3" by digifishmusic + "Deep Thumps.wav" by eardeer

Proposed (round 52), cut from the HQ previews by `tools/concept/audio/sfx_r52c.py`.
Sources: <https://freesound.org/people/digifishmusic/sounds/45439/> (Snapping_Shrimp_Slowed_32X.mp3 by digifishmusic, CC-BY 4.0), <https://freesound.org/people/eardeer/sounds/400997/> (Deep Thumps.wav by eardeer, CC0 1.0).
Use: every kill of a `tiny` unit below the waterline (Levels 23-28, e.g. Glassfin, eel segments, shootable torpedoes and mines), in place of the air rung (the game picks a random variant per kill, ±4 % pitch).
Brief: a deep, heavy pressure boom heard under water: a muffled body and a long low tail, no splash, no bubble rattle, nothing that pops; at most 0.6 s; a real recording, heavy, nothing bubbly or popping.
This one: the hydrophone: a snapping shrimp's click slowed 32 times (a clean, deep boom) with a deep thump muffled at 1.5 kHz: tighter and lower than a, 0.5 s.
Edit: 0.50 s, the loudest 100 ms band −17.9 dB, peak −2.0 dBFS, −12.8 LUFS.
**Rejected** (round 53, S.10, 2026-10-23: a picked); the file is in `rejected/`.

### explosion-underwater-small-r52-a — "Underwater Impact" by deleted_user_7709760 + "DepthBomb_08.aif" by ZeSoundResearchInc.

Proposed (round 52), cut from the HQ previews by `tools/concept/audio/sfx_r52c.py`.
Sources: <https://freesound.org/people/deleted_user_7709760/sounds/400793/> (Underwater Impact by deleted_user_7709760, CC-BY 3.0), <https://freesound.org/people/ZeSoundResearchInc./sounds/147877/> (DepthBomb_08.aif by ZeSoundResearchInc., CC0 1.0).
Use: every kill of a `small` unit below the waterline (Levels 23-28, e.g. Driftjelly), in place of the air rung (the game picks a random variant per kill, ±4 % pitch).
Brief: a deep, heavy pressure boom heard under water: a muffled body and a long low tail, no splash, no bubble rattle, nothing that pops; 0.7-1.1 s; a real recording, heavy, nothing bubbly or popping.
This one: the depth bomb: an under-water impact slowed to 0.85 and muffled over a depth bomb's sub boom: a round deep thud with a short tail, 0.9 s.
Edit: 0.90 s, the loudest 100 ms band −16.6 dB, peak −3.0 dBFS, −13.8 LUFS.
**Chosen** (round 53, S.10: a picked; to be rebuilt from the originals when the download limit allows).

### explosion-underwater-small-r52-b — "Snapping_Shrimp_Slowed_32X.mp3" by digifishmusic + "Quarry blasting.wav" by juskiddink

Proposed (round 52), cut from the HQ previews by `tools/concept/audio/sfx_r52c.py`.
Sources: <https://freesound.org/people/digifishmusic/sounds/45439/> (Snapping_Shrimp_Slowed_32X.mp3 by digifishmusic, CC-BY 4.0), <https://freesound.org/people/juskiddink/sounds/82682/> (Quarry blasting.wav by juskiddink, CC-BY 4.0).
Use: every kill of a `small` unit below the waterline (Levels 23-28, e.g. Driftjelly), in place of the air rung (the game picks a random variant per kill, ±4 % pitch).
Brief: a deep, heavy pressure boom heard under water: a muffled body and a long low tail, no splash, no bubble rattle, nothing that pops; 0.7-1.1 s; a real recording, heavy, nothing bubbly or popping.
This one: the hydrophone: a snapping shrimp's click slowed 32 times (a deep boom) with a granite quarry blast heard from 800 m, slowed to 0.8 and muffled at 1.5 kHz, as its body: a boom with a rough rolling body, 0.9 s.
Edit: 0.92 s, the loudest 100 ms band −19.3 dB, peak −1.6 dBFS, −13.4 LUFS.
**Rejected** (round 53, S.10, 2026-10-23: a picked); the file is in `rejected/`.

### explosion-underwater-medium-r52-a — "DepthBomb_07.aif" by ZeSoundResearchInc. + "Underwater Impact" by deleted_user_7709760 + "Explosive works.wav" by tosha73

Proposed (round 52), cut from the HQ previews by `tools/concept/audio/sfx_r52c.py`.
Sources: <https://freesound.org/people/ZeSoundResearchInc./sounds/147878/> (DepthBomb_07.aif by ZeSoundResearchInc., CC0 1.0), <https://freesound.org/people/deleted_user_7709760/sounds/400793/> (Underwater Impact by deleted_user_7709760, CC-BY 3.0), <https://freesound.org/people/tosha73/sounds/584594/> (Explosive works.wav by tosha73, CC-BY 4.0).
Use: every kill of a `medium` unit below the waterline (Levels 23-28, e.g. Reef Spitter, Minelayer, the siphon pump), in place of the air rung (the game picks a random variant per kill, ±4 % pitch).
Brief: a deep, heavy pressure boom heard under water: a muffled body and a long low tail, no splash, no bubble rattle, nothing that pops; 1.6-2 s; a real recording, heavy, nothing bubbly or popping.
This one: the depth bomb: a depth bomb's sub boom with an under-water impact slowed to 0.7 and muffled at 1.2 kHz as its body and a quarry blast's roar slowed to 0.7 and muffled at 1 kHz low under it: a heavy pressure boom with a rolling tail, 1.8 s.
Edit: 1.80 s, the loudest 100 ms band −16.5 dB, peak −1.7 dBFS, −12.2 LUFS.
**Chosen** (round 53, S.10: a picked; to be rebuilt from the originals when the download limit allows).

### explosion-underwater-medium-r52-b — "Snapping_Shrimp_Slowed_32X.mp3" by digifishmusic + "Quarry blasting.wav" by juskiddink

Proposed (round 52), cut from the HQ previews by `tools/concept/audio/sfx_r52c.py`.
Sources: <https://freesound.org/people/digifishmusic/sounds/45439/> (Snapping_Shrimp_Slowed_32X.mp3 by digifishmusic, CC-BY 4.0), <https://freesound.org/people/juskiddink/sounds/82682/> (Quarry blasting.wav by juskiddink, CC-BY 4.0).
Use: every kill of a `medium` unit below the waterline (Levels 23-28, e.g. Reef Spitter, Minelayer, the siphon pump), in place of the air rung (the game picks a random variant per kill, ±4 % pitch).
Brief: a deep, heavy pressure boom heard under water: a muffled body and a long low tail, no splash, no bubble rattle, nothing that pops; 1.6-2 s; a real recording, heavy, nothing bubbly or popping.
This one: the hydrophone: a snapping shrimp's click slowed 32 times (a deep, clean boom) with a granite quarry blast heard from 800 m, slowed to 0.8 and muffled at 1.8 kHz: more crack and roll in the body than a, 1.8 s.
Edit: 1.80 s, the loudest 100 ms band −18.4 dB, peak −1.6 dBFS, −15.0 LUFS.
**Rejected** (round 53, S.10, 2026-10-23: a picked); the file is in `rejected/`.

### explosion-underwater-large-r52-a — "DepthBomb_05.aif" by ZeSoundResearchInc. + "Underwater Impact" by deleted_user_7709760 + "Explosive works.wav" by tosha73

Proposed (round 52), cut from the HQ previews by `tools/concept/audio/sfx_r52c.py`.
Sources: <https://freesound.org/people/ZeSoundResearchInc./sounds/147880/> (DepthBomb_05.aif by ZeSoundResearchInc., CC0 1.0), <https://freesound.org/people/deleted_user_7709760/sounds/400793/> (Underwater Impact by deleted_user_7709760, CC-BY 3.0), <https://freesound.org/people/tosha73/sounds/584594/> (Explosive works.wav by tosha73, CC-BY 4.0).
Use: every kill of a `large` unit below the waterline (Levels 23-28, e.g. Abyss Ray, mid-boss parts, boss phase ends), in place of the air rung (the game picks a random variant per kill, ±4 % pitch).
Brief: a deep, heavy pressure boom heard under water: a muffled body and a long low tail, no splash, no bubble rattle, nothing that pops; 2.8-3.1 s; a real recording, heavy, nothing bubbly or popping.
This one: the depth bomb: a long depth-bomb boom with an under-water impact slowed to 0.6 and a quarry blast's roar slowed to 0.6, both muffled under 1 kHz: a huge pressure boom and a long low rumble, 3.0 s.
Edit: 3.00 s, the loudest 100 ms band −15.1 dB, peak −1.6 dBFS, −12.5 LUFS.
**Chosen** (round 53, S.10: a picked; to be rebuilt from the originals when the download limit allows).

### explosion-underwater-large-r52-b — "Snapping_Shrimp_Slowed_32X.mp3" by digifishmusic + "Quarry blasting.wav" by juskiddink

Proposed (round 52), cut from the HQ previews by `tools/concept/audio/sfx_r52c.py`.
Sources: <https://freesound.org/people/digifishmusic/sounds/45439/> (Snapping_Shrimp_Slowed_32X.mp3 by digifishmusic, CC-BY 4.0), <https://freesound.org/people/juskiddink/sounds/82682/> (Quarry blasting.wav by juskiddink, CC-BY 4.0).
Use: every kill of a `large` unit below the waterline (Levels 23-28, e.g. Abyss Ray, mid-boss parts, boss phase ends), in place of the air rung (the game picks a random variant per kill, ±4 % pitch).
Brief: a deep, heavy pressure boom heard under water: a muffled body and a long low tail, no splash, no bubble rattle, nothing that pops; 2.8-3.1 s; a real recording, heavy, nothing bubbly or popping.
This one: the hydrophone: a snapping shrimp's click slowed 32 times (a deep boom and a second one 0.7 s after, rolling) with a granite quarry blast heard from 800 m slowed to 0.65 and muffled at 1.4 kHz: a double boom with a long rough rumble, 3.0 s.
Edit: 3.00 s, the loudest 100 ms band −17.2 dB, peak −1.7 dBFS, −14.3 LUFS.
**Rejected** (round 53, S.10, 2026-10-23: a picked); the file is in `rejected/`.

## Concept round 53, step AUD-D (Level 24: the Abyss Ray, the Barbfin, the spawning reef, the beacons, the *Meridian*)

The generator is `tools/concept/audio/sfx_r53d.py` (Freesound HQ previews; nothing synthesized; no source AI-made, every
description checked). Closed in [round 53](../../../concept-rounds/round-53/README.md) (the user, 2026-10-23): rise b, glide a, fan b, death a, needle b, iris a, razed b, pulse a, hatch b; the rejected files are in `rejected/`.

### ray-rise-r53-a — "Ship Horn - Fog Horn - Cruise Ship Grand Princess" by coalcon + "Deep Sea Ambience" by jhumbucker

Proposed (round 53), cut from the HQ previews by `tools/concept/audio/sfx_r53d.py`.
Sources: <https://freesound.org/people/coalcon/sounds/636075/> (Ship Horn - Fog Horn - Cruise Ship Grand Princess by coalcon, CC0 1.0), <https://freesound.org/people/jhumbucker/sounds/193822/> (Deep Sea Ambience by jhumbucker, CC0 1.0).
Use: Level 24, each Abyss Ray (three a run, the first at t≈57.6, the second over the beacon line at t=112; one on the screen at a time): from its entry, while its shadow grows 1.5 s under the ship's lane and through the 0.9 s rise (the audio tell before its fans; Varga's first-sight line comes with the first).
Brief: a low, heavy, swelling horn-like call from deep below: it grows over the shadow's 1.5 s, is loudest as the Ray breaks out (about 2.4 s) and lets go; huge, far down and muffled, no bubbles; a real recording, heavy, nothing bubbly or popping.
This one: a cruise ship's fog horn (a real one, recorded on deck) slowed to 0.7 (its 106 Hz note down to about 75 Hz) and muffled at 900 Hz, swelling up out of a deep-sea rumble and letting go: a vast mechanical 'hoooo' from the dark, 2.9 s.
Edit: 2.90 s, the loudest 100 ms band −21.0 dB, peak −7.7 dBFS, −19.1 LUFS.
**Rejected** (round 53, S.1, 2026-10-23: b picked); the file is in `rejected/`.

### ray-rise-r53-b — "Conch.wav" by RoofDog + "Earthquake  **audible only with appropriate speakers**" by MBARI_MARS

Proposed (round 53), cut from the HQ previews by `tools/concept/audio/sfx_r53d.py`.
Sources: <https://freesound.org/people/RoofDog/sounds/78974/> (Conch.wav by RoofDog, CC0 1.0), <https://freesound.org/people/MBARI_MARS/sounds/369485/> (Earthquake  **audible only with appropriate speakers** by MBARI_MARS, CC0 1.0).
Use: Level 24, each Abyss Ray (three a run, the first at t≈57.6, the second over the beacon line at t=112; one on the screen at a time): from its entry, while its shadow grows 1.5 s under the ship's lane and through the 0.9 s rise (the audio tell before its fans; Varga's first-sight line comes with the first).
Brief: a low, heavy, swelling horn-like call from deep below: it grows over the shadow's 1.5 s, is loudest as the Ray breaks out (about 2.4 s) and lets go; huge, far down and muffled, no bubbles; a real recording, heavy, nothing bubbly or popping.
This one: a conch shell blown on a rooftop, slowed to 0.55 and rising to 0.7 as it swells (about 160 to 220 Hz), over a real earthquake heard by a deep-sea hydrophone at 900 m, sped up 2.5 times into an audible rumble: an animal call, rounder and more alive than a, 2.9 s.
Edit: 2.90 s, the loudest 100 ms band −21.0 dB, peak −10.2 dBFS, −18.5 LUFS.
**Chosen** (round 53, S.1: b picked; to be rebuilt from the originals when the download limit allows).

### ray-glide-r53-a — "R24-18-Tent or Tarp Whoosh.wav" by craigsmith + "Deep Sea Ambience" by jhumbucker

Proposed (round 53), cut from the HQ previews by `tools/concept/audio/sfx_r53d.py`.
Sources: <https://freesound.org/people/craigsmith/sounds/481896/> (R24-18-Tent or Tarp Whoosh.wav by craigsmith, CC0 1.0), <https://freesound.org/people/jhumbucker/sounds/193822/> (Deep Sea Ambience by jhumbucker, CC0 1.0).
Use: Level 24, each Abyss Ray (three a run, the first at t≈57.6, the second over the beacon line at t=112; one on the screen at a time): once as it levels out after the rise, under its 5 s glide across the field at 70 px/s (its fans fire during it).
Brief: the huge wings pushing water: two or three slow, deep strokes, a soft rushing swell rather than a flap, about 3.5 s; no splash, no bubbles; a real recording, heavy, nothing bubbly or popping.
This one: two whooshes of a big tarp shaken through the air (a vintage film-library foley), slowed to half and muffled at 600 Hz, 1.5 s apart, over a faint deep-sea rumble: two deep soft 'whooom' strokes, 3.5 s.
Edit: 3.50 s, the loudest 100 ms band −24.0 dB, peak −11.0 dBFS, −25.1 LUFS.
**Chosen** (round 53, S.2: a picked; to be rebuilt from the originals when the download limit allows).

### ray-glide-r53-b — "Large Wings Flapping - Foley.wav" by tothrec2 + "bolivar_waves_underwater.flac" by wjoojoo

Proposed (round 53), cut from the HQ previews by `tools/concept/audio/sfx_r53d.py`.
Sources: <https://freesound.org/people/tothrec2/sounds/596541/> (Large Wings Flapping - Foley.wav by tothrec2, CC0 1.0), <https://freesound.org/people/wjoojoo/sounds/258376/> (bolivar_waves_underwater.flac by wjoojoo, CC-BY 4.0).
Use: Level 24, each Abyss Ray (three a run, the first at t≈57.6, the second over the beacon line at t=112; one on the screen at a time): once as it levels out after the rise, under its 5 s glide across the field at 70 px/s (its fans fire during it).
Brief: the huge wings pushing water: two or three slow, deep strokes, a soft rushing swell rather than a flap, about 3.5 s; no splash, no bubbles; a real recording, heavy, nothing bubbly or popping.
This one: a winged beast's large wing beats (foley) slowed to half and muffled at 900 Hz, three heavy beats 1.5 s apart, over a wave surging past a hydrophone slowed and muffled: more of a beat than a, 3.6 s.
Edit: 3.60 s, the loudest 100 ms band −24.5 dB, peak −6.2 dBFS, −24.7 LUFS.
**Rejected** (round 53, S.2, 2026-10-23: a picked); the file is in `rejected/`.

### ray-fan-r53-a — "Tire Bead Seater Air Blast" by j_soundeffects + "DepthBomb_05.aif" by ZeSoundResearchInc.

Proposed (round 53), cut from the HQ previews by `tools/concept/audio/sfx_r53d.py`.
Sources: <https://freesound.org/people/j_soundeffects/sounds/863981/> (Tire Bead Seater Air Blast by j_soundeffects, CC-BY 4.0), <https://freesound.org/people/ZeSoundResearchInc./sounds/147880/> (DepthBomb_05.aif by ZeSoundResearchInc., CC0 1.0).
Use: Level 24, each Abyss Ray (three a run, the first at t≈57.6, the second over the beacon line at t=112; one on the screen at a time): each fan it fires, three 1.2 s apart at the top of its glide (two on easy, four on hard), 7-11 slow bullets spreading 110 degrees from its mouth.
Brief: a heavy pressure push from the Ray's mouth: a deep, soft-edged whump with a short rushing exhale, the heavy enemy shot's weight, about 0.6 s; no gunshot crack, no pop; a real recording, heavy, nothing bubbly or popping.
This one: a tyre bead seater's compressed-air blast (an air tank with a bazooka nozzle) slowed to 0.7 and muffled at 1.4 kHz, over a depth bomb's sub boom: a round heavy 'whumf' with a short rush, 0.65 s.
Edit: 0.65 s, the loudest 100 ms band −23.1 dB, peak −9.9 dBFS, −22.7 LUFS.
**Rejected** (round 53, S.3, 2026-10-23: b picked); the file is in `rejected/`.

### ray-fan-r53-b — "HMNBrth-Blue Snowball Microphone, MCU_Thru Didgeridoo, Elephant Like_Nicholas Judy_TDC" by designerschoice + "DepthBomb_07.aif" by ZeSoundResearchInc.

Proposed (round 53), cut from the HQ previews by `tools/concept/audio/sfx_r53d.py`.
Sources: <https://freesound.org/people/designerschoice/sounds/824961/> (HMNBrth-Blue Snowball Microphone, MCU_Thru Didgeridoo, Elephant Like_Nicholas Judy_TDC by designerschoice, CC-BY 4.0), <https://freesound.org/people/ZeSoundResearchInc./sounds/147878/> (DepthBomb_07.aif by ZeSoundResearchInc., CC0 1.0).
Use: Level 24, each Abyss Ray (three a run, the first at t≈57.6, the second over the beacon line at t=112; one on the screen at a time): each fan it fires, three 1.2 s apart at the top of its glide (two on easy, four on hard), 7-11 slow bullets spreading 110 degrees from its mouth.
Brief: a heavy pressure push from the Ray's mouth: a deep, soft-edged whump with a short rushing exhale, the heavy enemy shot's weight, about 0.6 s; no gunshot crack, no pop; a real recording, heavy, nothing bubbly or popping.
This one: a forced breath blown through a didgeridoo (an elephant-like exhale) slowed to 0.75 and muffled, over a depth bomb's sub boom: breathier and more animal than a, a 'hhhff' with a thud under it, 0.65 s.
Edit: 0.65 s, the loudest 100 ms band −23.0 dB, peak −10.8 dBFS, −22.3 LUFS.
**Chosen** (round 53, S.3: b picked; to be rebuilt from the originals when the download limit allows).

### ray-death-r53-a — "S035_Camels_Mono.wav" by Pól + "DepthBomb_06.aif" by ZeSoundResearchInc.

Proposed (round 53), cut from the HQ previews by `tools/concept/audio/sfx_r53d.py`.
Sources: <https://freesound.org/people/Pól/sounds/385912/> (S035_Camels_Mono.wav by Pól, CC0 1.0), <https://freesound.org/people/ZeSoundResearchInc./sounds/147879/> (DepthBomb_06.aif by ZeSoundResearchInc., CC0 1.0).
Use: Level 24, each Abyss Ray (three a run, the first at t≈57.6, the second over the beacon line at t=112; one on the screen at a time): killed (210 HP; the first drops the Overdrive), with the under-water `large` burst, as its wings fold and the body sinks.
Brief: a huge creature's dying groan sinking away over a deep boom: a long low moan falling in pitch, heavy and muffled, about 2.5 s; no squeal, no bubbles; a real recording, heavy, nothing bubbly or popping.
This one: a camel's groan (a real one, recorded at a bazaar) slowed from 0.6 to 0.42 so it sinks as it goes and muffled at 1.2 kHz, over a depth bomb's sub boom: a deep dying moan with a body, 2.8 s.
Edit: 2.80 s, the loudest 100 ms band −19.3 dB, peak −4.6 dBFS, −15.0 LUFS.
**Chosen** (round 53, S.4: a picked; to be rebuilt from the originals when the download limit allows).

### ray-death-r53-b — "Conch.wav" by RoofDog + "Snapping_Shrimp_Slowed_32X.mp3" by digifishmusic

Proposed (round 53), cut from the HQ previews by `tools/concept/audio/sfx_r53d.py`.
Sources: <https://freesound.org/people/RoofDog/sounds/78974/> (Conch.wav by RoofDog, CC0 1.0), <https://freesound.org/people/digifishmusic/sounds/45439/> (Snapping_Shrimp_Slowed_32X.mp3 by digifishmusic, CC-BY 4.0).
Use: Level 24, each Abyss Ray (three a run, the first at t≈57.6, the second over the beacon line at t=112; one on the screen at a time): killed (210 HP; the first drops the Overdrive), with the under-water `large` burst, as its wings fold and the body sinks.
Brief: a huge creature's dying groan sinking away over a deep boom: a long low moan falling in pitch, heavy and muffled, about 2.5 s; no squeal, no bubbles; a real recording, heavy, nothing bubbly or popping.
This one: the conch call of rise b slowed from 0.65 to 0.4 so it falls away (about 210 down to 130 Hz), muffled, over a snapping shrimp's click slowed 32 times (a real hydrophone recording: a clean deep boom): the Ray's own call dying, more tonal than a, 2.8 s.
Edit: 2.80 s, the loudest 100 ms band −20.7 dB, peak −7.2 dBFS, −14.9 LUFS.
**Rejected** (round 53, S.4, 2026-10-23: a picked); the file is in `rejected/`.

### barbfin-needle-r53-a — "blowgun - pipeblow - dart shotting" by ramonmineiro

Proposed (round 53), cut from the HQ previews by `tools/concept/audio/sfx_r53d.py`.
Source: <https://freesound.org/people/ramonmineiro/sounds/531973/> (blowgun - pipeblow - dart shotting by ramonmineiro, CC0 1.0).
Use: Level 24 on, every needle a Barbfin fires: an aimed small violet needle every 2.4 s per Barbfin (four with each Ray, columns of three to five; hard: pairs 0.12 s apart), so often and many at once.
Brief: a short, sharp spit: a dart blown hard from a tube, tight and dry with a little body, 0.2-0.3 s; not a laser zap, not a pop, never tiring when many fire; a real recording, heavy, nothing bubbly or popping.
This one: a blowgun dart shot (a real blowpipe) slowed to 0.85, its breath plosive's low thump cut at 250 Hz: a quick 'fsst-tk', 0.3 s.
Edit: 0.30 s, the loudest 100 ms band −27.4 dB, peak −10.8 dBFS, −inf LUFS.
**Rejected** (round 53, S.5, 2026-10-23: b picked); the file is in `rejected/`.

### barbfin-needle-r53-b — "industrial_nailgun_1shot+ring" by gilly11

Proposed (round 53), cut from the HQ previews by `tools/concept/audio/sfx_r53d.py`.
Source: <https://freesound.org/people/gilly11/sounds/143615/> (industrial_nailgun_1shot+ring by gilly11, CC-BY 3.0).
Use: Level 24 on, every needle a Barbfin fires: an aimed small violet needle every 2.4 s per Barbfin (four with each Ray, columns of three to five; hard: pairs 0.12 s apart), so often and many at once.
Brief: a short, sharp spit: a dart blown hard from a tube, tight and dry with a little body, 0.2-0.3 s; not a laser zap, not a pop, never tiring when many fire; a real recording, heavy, nothing bubbly or popping.
This one: an industrial nail gun's shot slowed to 0.8 and muffled at 3.5 kHz: a harder pneumatic 'tchk' than a, more mechanical, 0.25 s.
Edit: 0.25 s, the loudest 100 ms band −27.2 dB, peak −8.2 dBFS, −inf LUFS.
**Chosen** (round 53, S.5: b picked; to be rebuilt from the originals when the download limit allows).

### reef-iris-r53-a — "Guterton - Heavy Stone Grind" by guterton.wav + "Jammed seashells" by MBPL

Proposed (round 53), cut from the HQ previews by `tools/concept/audio/sfx_r53d.py`.
Sources: <https://freesound.org/people/guterton.wav/sounds/760672/> (Guterton - Heavy Stone Grind by guterton.wav, CC-BY 4.0), <https://freesound.org/people/MBPL/sounds/667184/> (Jammed seashells by MBPL, CC0 1.0).
Use: Level 24, each spawning reef's 0.5 s telegraph before it releases its fry (four Glassfins, three on easy; every 3.5 s while a reef lives and the ship is not within 96 px of it): the iris of chitin plates grinding open, the fry darting out at its end.
Brief: a slow grinding of heavy chitin plates sliding apart: a rough stone-and-shell grind, dry and heavy, 0.6 s; nothing bubbly, no squelch; a real recording, heavy, nothing bubbly or popping.
This one: a heavy stone ground on stone, slowed to 0.75 and muffled at 3 kHz, with a handful of seashells jammed together under it (slowed, quiet): a gritty shell-plate grind, 0.6 s.
Edit: 0.60 s, the loudest 100 ms band −22.0 dB, peak −8.7 dBFS, −17.9 LUFS.
**Chosen** (round 53, S.6: a picked; to be rebuilt from the originals when the download limit allows).

### reef-iris-r53-b — "stone_on_stone.aif" by thanvannispen + "FOLYProp-Blue Snowball Microphone, CU_Seashell, Clatter 08_Nicholas Judy_TDC" by designerschoice

Proposed (round 53), cut from the HQ previews by `tools/concept/audio/sfx_r53d.py`.
Sources: <https://freesound.org/people/thanvannispen/sounds/29986/> (stone_on_stone.aif by thanvannispen, CC-BY 4.0), <https://freesound.org/people/designerschoice/sounds/807564/> (FOLYProp-Blue Snowball Microphone, CU_Seashell, Clatter 08_Nicholas Judy_TDC by designerschoice, CC-BY 4.0).
Use: Level 24, each spawning reef's 0.5 s telegraph before it releases its fry (four Glassfins, three on easy; every 3.5 s while a reef lives and the ship is not within 96 px of it): the iris of chitin plates grinding open, the fry darting out at its end.
Brief: a slow grinding of heavy chitin plates sliding apart: a rough stone-and-shell grind, dry and heavy, 0.6 s; nothing bubbly, no squelch; a real recording, heavy, nothing bubbly or popping.
This one: stone dragged on stone slowed to 0.6 and muffled, with a seashell clattering slowed under it: a deeper, smoother drag than a with a brittle shell rattle in it, 0.65 s.
Edit: 0.65 s, the loudest 100 ms band −22.6 dB, peak −5.0 dBFS, −22.9 LUFS.
**Rejected** (round 53, S.6, 2026-10-23: a picked); the file is in `rejected/`.

### reef-death-r53-a — "Rockslide 2.wav" by juskiddink + "DepthBomb_07.aif" by ZeSoundResearchInc.

Proposed (round 53), cut from the HQ previews by `tools/concept/audio/sfx_r53d.py`.
Sources: <https://freesound.org/people/juskiddink/sounds/79783/> (Rockslide 2.wav by juskiddink, CC-BY 4.0), <https://freesound.org/people/ZeSoundResearchInc./sounds/147878/> (DepthBomb_07.aif by ZeSoundResearchInc., CC0 1.0).
Use: Level 24, a spawning reef razed (200 HP, the secondary REEFS RAZED; three of four, two on easy), with the under-water `large` burst, as the coral tower falls to a stump and its iris shuts.
Brief: a heavy crumbling of coral-like rock: a big brittle break and rubble sliding down, muffled by the water, about 2 s; no splash, nothing that pops; a real recording, heavy, nothing bubbly or popping.
This one: a rockslide of large granite rocks slowed to 0.9 and muffled at 3.5 kHz, over a depth bomb's boom: a heavy rumbling collapse, rock on rock, 2.2 s.
Edit: 2.20 s, the loudest 100 ms band −21.9 dB, peak −3.1 dBFS, −15.1 LUFS.
**Rejected** (round 53, S.7, 2026-10-23: b picked); the file is in `rejected/`.

### reef-death-r53-b — "Bricks Falling 2.wav" by Benboncan + "S30-08 Wall falls down from fire damage.wav" by craigsmith

Proposed (round 53), cut from the HQ previews by `tools/concept/audio/sfx_r53d.py`.
Sources: <https://freesound.org/people/Benboncan/sounds/77086/> (Bricks Falling 2.wav by Benboncan, CC-BY 4.0), <https://freesound.org/people/craigsmith/sounds/675899/> (S30-08 Wall falls down from fire damage.wav by craigsmith, CC0 1.0).
Use: Level 24, a spawning reef razed (200 HP, the secondary REEFS RAZED; three of four, two on easy), with the under-water `large` burst, as the coral tower falls to a stump and its iris shuts.
Brief: a heavy crumbling of coral-like rock: a big brittle break and rubble sliding down, muffled by the water, about 2 s; no splash, nothing that pops; a real recording, heavy, nothing bubbly or popping.
This one: a load of kilned clay bricks falling (a brittle, chalky break) slowed to 0.8 and muffled, over a wall falling down (a vintage film-library recording) slowed and muffled: sharper and more brittle than a, more like breaking coral, 2.4 s.
Edit: 2.40 s, the loudest 100 ms band −19.1 dB, peak −5.9 dBFS, −18.9 LUFS.
**Chosen** (round 53, S.7: b picked; to be rebuilt from the originals when the download limit allows).

### beacon-pulse-r53-a — "Sonar_pings.ogg" by Peter_Gross

Proposed (round 53), cut from the HQ previews by `tools/concept/audio/sfx_r53d.py`.
Source: <https://freesound.org/people/Peter_Gross/sounds/12677/> (Sonar_pings.ogg by Peter_Gross, CC0 1.0).
Use: Level 24's section 3 (t≈95-135): each relay beacon's 2 s lamp pulse while a beacon is on the screen (three beacons, about 6 s each), at a low gain under the music (the 'sonar ping on the beat'; DeepWaterCues).
Brief: a deep, far, clean sonar ping: one round low tone with a long soft tail, heard from far off, about 1.9 s so pulses 2 s apart do not pile up; not a beep, not a pop; a real recording, heavy, nothing bubbly or popping.
This one: a real 625 Hz active sonar ping (a long-range anti-submarine type) slowed to 0.6 (about 400 Hz) and muffled at 1.5 kHz: a clean far 'pinnng' with its own water echo, 1.9 s.
Edit: 1.90 s, the loudest 100 ms band −15.9 dB, peak −7.7 dBFS, −21.9 LUFS.
**Chosen** (round 53, S.8: a picked; to be rebuilt from the originals when the download limit allows).

### beacon-pulse-r53-b — "Singing Bowl - Soft Stick Meditative Hit" by Rimych

Proposed (round 53), cut from the HQ previews by `tools/concept/audio/sfx_r53d.py`.
Source: <https://freesound.org/people/Rimych/sounds/616335/> (Singing Bowl - Soft Stick Meditative Hit by Rimych, CC0 1.0).
Use: Level 24's section 3 (t≈95-135): each relay beacon's 2 s lamp pulse while a beacon is on the screen (three beacons, about 6 s each), at a low gain under the music (the 'sonar ping on the beat'; DeepWaterCues).
Brief: a deep, far, clean sonar ping: one round low tone with a long soft tail, heard from far off, about 1.9 s so pulses 2 s apart do not pile up; not a beep, not a pop; a real recording, heavy, nothing bubbly or popping.
This one: a large singing bowl's soft-stick hit (a round 156 Hz tone, recorded close) sped to 1.25 (about 195 Hz) and muffled at 1.5 kHz: lower, softer and warmer than a, a deep 'bommm' rather than a ping, 1.9 s.
Edit: 1.90 s, the loudest 100 ms band −19.6 dB, peak −9.7 dBFS, −16.0 LUFS.
**Rejected** (round 53, S.8, 2026-10-23: a picked); the file is in `rejected/`.

### wreck-hatch-r53-a — "G45-30-Submarine Hatch Opening.wav" by craigsmith + "Underwater Impact" by deleted_user_7709760

Proposed (round 53), cut from the HQ previews by `tools/concept/audio/sfx_r53d.py`.
Sources: <https://freesound.org/people/craigsmith/sounds/438732/> (G45-30-Submarine Hatch Opening.wav by craigsmith, CC0 1.0), <https://freesound.org/people/deleted_user_7709760/sounds/400793/> (Underwater Impact by deleted_user_7709760, CC-BY 3.0).
Use: Level 24 at t≈105, once a run (the secret): the wrecked survey sub *Meridian* destroyed (20 HP) - its hatch lets go and the hidden crate floats up.
Brief: a heavy metal hatch giving way under water: a deep clunk of a breaking latch, then a slow creak as it swings open, about 1.5 s, muffled; built, not grown; a real recording, heavy, nothing bubbly or popping.
This one: a real submarine hatch opening (heavy metal clanks and a creak; a vintage film-library recording) slowed to 0.85 and muffled at 2.5 kHz, on a soft under-water impact: clank, clank and the hatch free, 1.6 s.
Edit: 1.60 s, the loudest 100 ms band −20.1 dB, peak −9.5 dBFS, −23.8 LUFS.
**Rejected** (round 53, S.9, 2026-10-23: b picked); the file is in `rejected/`.

### wreck-hatch-r53-b — "Metal_Hatch_Small_Close_01" by Rudmer_Rotteveel + "Metallic Groan" by hinchinbrook

Proposed (round 53), cut from the HQ previews by `tools/concept/audio/sfx_r53d.py`.
Sources: <https://freesound.org/people/Rudmer_Rotteveel/sounds/718440/> (Metal_Hatch_Small_Close_01 by Rudmer_Rotteveel, CC0 1.0), <https://freesound.org/people/hinchinbrook/sounds/496836/> (Metallic Groan by hinchinbrook, CC0 1.0).
Use: Level 24 at t≈105, once a run (the secret): the wrecked survey sub *Meridian* destroyed (20 HP) - its hatch lets go and the hidden crate floats up.
Brief: a heavy metal hatch giving way under water: a deep clunk of a breaking latch, then a slow creak as it swings open, about 1.5 s, muffled; built, not grown; a real recording, heavy, nothing bubbly or popping.
This one: a metal hatch slammed, slowed to 0.6 into a deep clunk, then a heavy bunker door's low hinge groan slowed to 0.8 and muffled: one big clunk and a long creak, 1.65 s.
Edit: 1.65 s, the loudest 100 ms band −20.5 dB, peak −7.3 dBFS, −17.0 LUFS.
**Chosen** (round 53, S.9: b picked; to be rebuilt from the originals when the download limit allows).

## Concept round 54, step AUD-E (Level 25: the Siren, the Spiral Nautilus, the trench bed)

The generator is `tools/concept/audio/sfx_r54e.py` (Freesound HQ previews; nothing synthesized). Pending the user's picks
in [round 54](../../../concept-rounds/round-54/README.md).

### siren-ring-r54-a — "alfonso_low_01.wav" by Bram + "Deep Sea Ambience" by jhumbucker

Proposed (round 54), cut from the HQ previews by `tools/concept/audio/sfx_r54e.py`.
Sources: <https://freesound.org/people/Bram/sounds/19517/> (alfonso_low_01.wav by Bram, CC-BY 4.0), <https://freesound.org/people/jhumbucker/sounds/193822/> (Deep Sea Ambience by jhumbucker, CC0 1.0).
Use: Level 25 (and Levels 26-27), each Siren nest in the trench walls: every 4.5 s a nest (6 s on easy, 3.6 s on hard), up to four nests on the screen, their rings at least 1.5 s apart: from the sac's 0.8 s teal tell through the ring spreading out (120 px/s to 230 px, about 1.9 s); shooting the sac during the tell ends it (the Sirens' 'song drone' of the music text is this sound).
Brief: a soft, heavy, low sweep, sung rather than pinged: a deep voice-like tone swelling over the tell, loudest as the ring leaves the sac, then spreading and sinking away; muffled, no bubbles, no sonar ping; a real recording, heavy, nothing bubbly or popping.
This one: Tuvan throat singing (kargyraa, a real singer's growling low drone with its overtones) slowed to 0.62 and sliding down to 0.5 and muffled at 1.2 kHz, swelling in over the 0.8 s tell and fading as the ring spreads, over a deep-sea rumble: a deep sung 'hmmmoooaa' from the wall, 2.8 s.
Edit: 2.80 s, the loudest 100 ms band −24.0 dB, peak −9.4 dBFS, −21.0 LUFS.
**Chosen** (round 54, S.1: a picked; to be rebuilt from the originals when the download limit allows).

### siren-ring-r54-b — "Tibetan bowl_rubbing rim.wav" by dersinnsspace + "Deep Sea Ambience" by jhumbucker

Proposed (round 54), cut from the HQ previews by `tools/concept/audio/sfx_r54e.py`.
Sources: <https://freesound.org/people/dersinnsspace/sounds/417115/> (Tibetan bowl_rubbing rim.wav by dersinnsspace, CC0 1.0), <https://freesound.org/people/jhumbucker/sounds/193822/> (Deep Sea Ambience by jhumbucker, CC0 1.0).
Use: Level 25 (and Levels 26-27), each Siren nest in the trench walls: every 4.5 s a nest (6 s on easy, 3.6 s on hard), up to four nests on the screen, their rings at least 1.5 s apart: from the sac's 0.8 s teal tell through the ring spreading out (120 px/s to 230 px, about 1.9 s); shooting the sac during the tell ends it (the Sirens' 'song drone' of the music text is this sound).
Brief: a soft, heavy, low sweep, sung rather than pinged: a deep voice-like tone swelling over the tell, loudest as the ring leaves the sac, then spreading and sinking away; muffled, no bubbles, no sonar ping; a real recording, heavy, nothing bubbly or popping.
This one: a Tibetan bowl's rubbed rim ring (a long sustained sung tone, 276 Hz) slowed to 0.8 and sliding down to 0.6 (about 220 to 165 Hz) and muffled at 2 kHz, swelling and fading the same over a deep-sea rumble: purer and rounder than a, a sung 'ooooom' rather than a voice, 2.8 s.
Edit: 2.80 s, the loudest 100 ms band −23.9 dB, peak −18.6 dBFS, −26.3 LUFS.
**Rejected** (round 54, S.1, 2026-10-23: the other picked); the file is in `rejected/`.

### siren-slow-r54-a — "Hand cranked siren.wav" by mikewest + "drone6_bowed_cymbal1.wav" by decembered

Proposed (round 54), cut from the HQ previews by `tools/concept/audio/sfx_r54e.py`.
Sources: <https://freesound.org/people/mikewest/sounds/239498/> (Hand cranked siren.wav by mikewest, CC0 1.0), <https://freesound.org/people/decembered/sounds/372202/> (drone6_bowed_cymbal1.wav by decembered, CC0 1.0).
Use: Level 25 on, each time a Siren's ring slows the ship (60 % speed and acceleration for 2 s, 1.5 s on easy; then 3 s immune): once, as the ring's band passes the ship, with the sonar shimmer on the hull.
Brief: the ship dragged down: a falling tone, as of the hull's engines losing power, with a metallic shimmer on the hull over it, about 2 s; muffled, nothing that pops; a real recording, heavy, nothing bubbly or popping.
This one: a rural fire brigade's hand-cranked siren at the top of its note, slowed to 0.5 and sliding down to 0.28 (its tone falling about an octave) and muffled at 1.5 kHz, with a bowed cymbal's high shimmer over it: a dropping 'wooouuuh' with a glassy ring, 2.0 s.
Edit: 2.00 s, the loudest 100 ms band −23.0 dB, peak −16.4 dBFS, −21.9 LUFS.
**Rejected** (round 54, S.2, 2026-10-23: the other picked); the file is in `rejected/`.

### siren-slow-r54-b — "drone8_bowed_cymbal2.wav" by decembered + "Real electro-mechanical fire siren" by AudioPapkin

Proposed (round 54), cut from the HQ previews by `tools/concept/audio/sfx_r54e.py`.
Sources: <https://freesound.org/people/decembered/sounds/372206/> (drone8_bowed_cymbal2.wav by decembered, CC0 1.0), <https://freesound.org/people/AudioPapkin/sounds/715561/> (Real electro-mechanical fire siren by AudioPapkin, CC0 1.0).
Use: Level 25 on, each time a Siren's ring slows the ship (60 % speed and acceleration for 2 s, 1.5 s on easy; then 3 s immune): once, as the ring's band passes the ship, with the sonar shimmer on the hull.
Brief: the ship dragged down: a falling tone, as of the hull's engines losing power, with a metallic shimmer on the hull over it, about 2 s; muffled, nothing that pops; a real recording, heavy, nothing bubbly or popping.
This one: a bowed cymbal's ring sliding down from 0.9 to 0.45 (the metal's own tone falling) over an electro-mechanical fire siren winding down between two wails, slowed to 0.6 and muffled at 900 Hz: more metal and less engine than a, a sinking 'shiiiing', 2.0 s.
Edit: 2.00 s, the loudest 100 ms band −23.0 dB, peak −12.8 dBFS, −20.1 LUFS.
**Chosen** (round 54, S.2: b picked; to be rebuilt from the originals when the download limit allows).

### siren-death-r54-a — "alfonso_low_01.wav" by Bram + "DepthBomb_06.aif" by ZeSoundResearchInc.

Proposed (round 54), cut from the HQ previews by `tools/concept/audio/sfx_r54e.py`.
Sources: <https://freesound.org/people/Bram/sounds/19517/> (alfonso_low_01.wav by Bram, CC-BY 4.0), <https://freesound.org/people/ZeSoundResearchInc./sounds/147879/> (DepthBomb_06.aif by ZeSoundResearchInc., CC0 1.0).
Use: Level 25 (and Levels 26-27), each Siren nest in the trench walls: every 4.5 s a nest (6 s on easy, 3.6 s on hard), up to four nests on the screen, their rings at least 1.5 s apart: a Siren killed (90 HP, its throat sac shot), with the under-water `medium` burst, as the sac bursts and the nest goes dark; its song stops.
Brief: the song choked off: the sung tone falling away and dying over a deep soft boom, about 2 s, muffled; no squeal, no splash, no bubbles; a real recording, heavy, nothing bubbly or popping.
This one: the throat singing of ring a slowed from 0.6 to 0.25 so it sinks and dies, muffled, over a depth bomb's sub boom: the Siren's own voice choked off, 2.2 s; pairs with ring a.
Edit: 2.20 s, the loudest 100 ms band −21.0 dB, peak −4.6 dBFS, −16.0 LUFS.
**Chosen** (round 54, S.3: a picked; to be rebuilt from the originals when the download limit allows).

### siren-death-r54-b — "humpback_whales_and_orcas_ambient_underwater_recording" by raiden04 + "DepthBomb_05.aif" by ZeSoundResearchInc.

Proposed (round 54), cut from the HQ previews by `tools/concept/audio/sfx_r54e.py`.
Sources: <https://freesound.org/people/raiden04/sounds/797762/> (humpback_whales_and_orcas_ambient_underwater_recording by raiden04, CC-BY 4.0), <https://freesound.org/people/ZeSoundResearchInc./sounds/147880/> (DepthBomb_05.aif by ZeSoundResearchInc., CC0 1.0).
Use: Level 25 (and Levels 26-27), each Siren nest in the trench walls: every 4.5 s a nest (6 s on easy, 3.6 s on hard), up to four nests on the screen, their rings at least 1.5 s apart: a Siren killed (90 HP, its throat sac shot), with the under-water `medium` burst, as the sac bursts and the nest goes dark; its song stops.
Brief: the song choked off: the sung tone falling away and dying over a deep soft boom, about 2 s, muffled; no squeal, no splash, no bubbles; a real recording, heavy, nothing bubbly or popping.
This one: a humpback whale's call (a real hydrophone recording) slowed from 0.75 to 0.45 so it falls away (about 170 down to 95 Hz), muffled, over a depth bomb's boom: more of an animal's dying moan than a, 2.2 s.
Edit: 2.20 s, the loudest 100 ms band −25.0 dB, peak −7.0 dBFS, −16.0 LUFS.
**Rejected** (round 54, S.3, 2026-10-23: the other picked); the file is in `rejected/`.

### nautilus-spin-r54-a — "Water mill - the inside of a grind stone" by borralbi + "cement mixer small empty turning with horrible grinding metal friction on and offmic1 from front.wav" by kyles

Proposed (round 54), cut from the HQ previews by `tools/concept/audio/sfx_r54e.py`.
Sources: <https://freesound.org/people/borralbi/sounds/342821/> (Water mill - the inside of a grind stone by borralbi, CC-BY 4.0), <https://freesound.org/people/kyles/sounds/407295/> (cement mixer small empty turning with horrible grinding metal friction on and offmic1 from front.wav by kyles, CC0 1.0).
Use: Level 25 on, each Spiral Nautilus (five pairs in Level 25, orbiting the ship in opposite directions): while one is on the screen, as its shell spins round the ship (one quiet loop for all of them, the nearest; it stops with the last).
Brief: a heavy rolling grind: a big stone shell turning, a rough slow rumble with grit in it, low in the mix, no clicks, a seamless loop; a real recording, heavy, nothing bubbly or popping.
This one: a water mill's millstone grinding (recorded inside the stone; a mill from 1863) slowed to 0.6 and muffled at 900 Hz (its feeder's taps dulled), over a small cement mixer's empty drum turning, slowed to 0.5 and muffled: a steady deep stone grind, 4.0 s loop.
Edit: 4.00 s, band RMS −34.1 dB, peak −14.3 dBFS, −27.1 LUFS.
**Chosen** (round 54, S.4: a picked; to be rebuilt from the originals when the download limit allows).

### nautilus-spin-r54-b — "Trash Can Wheels Rolling" by Filmscore + "Tumbler Rotary (Bryte) -low Speed - 1" by FOSSarts

Proposed (round 54), cut from the HQ previews by `tools/concept/audio/sfx_r54e.py`.
Sources: <https://freesound.org/people/Filmscore/sounds/823071/> (Trash Can Wheels Rolling by Filmscore, CC0 1.0), <https://freesound.org/people/FOSSarts/sounds/763459/> (Tumbler Rotary (Bryte) -low Speed - 1 by FOSSarts, CC0 1.0).
Use: Level 25 on, each Spiral Nautilus (five pairs in Level 25, orbiting the ship in opposite directions): while one is on the screen, as its shell spins round the ship (one quiet loop for all of them, the nearest; it stops with the last).
Brief: a heavy rolling grind: a big stone shell turning, a rough slow rumble with grit in it, low in the mix, no clicks, a seamless loop; a real recording, heavy, nothing bubbly or popping.
This one: a wheeled bin rolled over pavement (a boulder-roll foley) slowed to 0.5 and muffled at 1 kHz, over a rotary rock tumbler slowed to 0.5: rougher and more of a roll than a, rocks churning in a drum, 4.0 s loop.
Edit: 4.00 s, band RMS −40.3 dB, peak −13.0 dBFS, −27.0 LUFS.
**Rejected** (round 54, S.4, 2026-10-23: the other picked); the file is in `rejected/`.

### nautilus-uncoil-r54-a — "Opening the sarcophagus" by Breviceps + "shells and quartz crystals delicately scraping marble and tapping ceramic plate.wav" by CVLTIV8R

Proposed (round 54), cut from the HQ previews by `tools/concept/audio/sfx_r54e.py`.
Sources: <https://freesound.org/people/Breviceps/sounds/457529/> (Opening the sarcophagus by Breviceps, CC0 1.0), <https://freesound.org/people/CVLTIV8R/sounds/800116/> (shells and quartz crystals delicately scraping marble and tapping ceramic plate.wav by CVLTIV8R, CC0 1.0).
Use: Level 25 on, each Spiral Nautilus (five pairs in Level 25, orbiting the ship in opposite directions): each uncoil, every cycle (about 5 s) at the start of its 0.6 s opening, with the 0.4 s crown tell before it fires its spiral.
Brief: a shell scraping open: a heavy stone-on-shell scrape, dry and gritty, about 0.6 s; nothing bubbly, no squelch; a real recording, heavy, nothing bubbly or popping.
This one: a heavy stone sarcophagus lid sliding, slowed to 0.7 and muffled at 2.5 kHz, with shells and quartz scraping marble quietly under it: a gritty stone-and-shell scrape, 0.7 s.
Edit: 0.70 s, the loudest 100 ms band −22.9 dB, peak −5.4 dBFS, −18.1 LUFS.
**Chosen** (round 54, S.5: a picked; to be rebuilt from the originals when the download limit allows).

### nautilus-uncoil-r54-b — "Stone Slab Door Grinding - Heavy Rock Scrape (Mono)" by NahuelMartinez + "Heavy stone door opens" by PostProdDog

Proposed (round 54), cut from the HQ previews by `tools/concept/audio/sfx_r54e.py`.
Sources: <https://freesound.org/people/NahuelMartinez/sounds/844329/> (Stone Slab Door Grinding - Heavy Rock Scrape (Mono) by NahuelMartinez, CC0 1.0), <https://freesound.org/people/PostProdDog/sounds/578490/> (Heavy stone door opens by PostProdDog, CC0 1.0).
Use: Level 25 on, each Spiral Nautilus (five pairs in Level 25, orbiting the ship in opposite directions): each uncoil, every cycle (about 5 s) at the start of its 0.6 s opening, with the 0.4 s crown tell before it fires its spiral.
Brief: a shell scraping open: a heavy stone-on-shell scrape, dry and gritty, about 0.6 s; nothing bubbly, no squelch; a real recording, heavy, nothing bubbly or popping.
This one: a stone slab door grinding open, slowed to 0.6 and muffled at 2 kHz, over a heavy stone door's low drag: deeper and harder at the start than a, more stone than shell, 0.75 s.
Edit: 0.75 s, the loudest 100 ms band −22.0 dB, peak −5.9 dBFS, −19.2 LUFS.
**Rejected** (round 54, S.5, 2026-10-23: the other picked); the file is in `rejected/`.

### nautilus-death-r54-a — "Breaking pot low" by anna_bliss + "DepthBomb_06.aif" by ZeSoundResearchInc.

Proposed (round 54), cut from the HQ previews by `tools/concept/audio/sfx_r54e.py`.
Sources: <https://freesound.org/people/anna_bliss/sounds/810042/> (Breaking pot low by anna_bliss, CC0 1.0), <https://freesound.org/people/ZeSoundResearchInc./sounds/147879/> (DepthBomb_06.aif by ZeSoundResearchInc., CC0 1.0).
Use: Level 25 on, each Spiral Nautilus (five pairs in Level 25, orbiting the ship in opposite directions): one killed (100 HP; the closed shell glances off all but the specials and the anti-sub weapons), with the under-water `medium` burst, as the shell cracks into two halves that sink and the crown goes dark.
Brief: a big shell cracking: a hard, hollow, brittle crack and the halves breaking away, heavy and muffled, about 1.5 s; no splash, nothing that pops; a real recording, heavy, nothing bubbly or popping.
This one: a big clay pot smashed with a hammer (a hit, then the pot breaking apart), slowed to 0.65 and muffled at 3 kHz, over a depth bomb's boom: a hollow ceramic crack and pieces breaking away, 1.8 s.
Edit: 1.80 s, the loudest 100 ms band −20.1 dB, peak −4.1 dBFS, −18.4 LUFS.
**Rejected** (round 54, S.6, 2026-10-23: the other picked); the file is in `rejected/`.

### nautilus-death-r54-b — "Breaking_large_ice.wav" by CGEffex + "FOL_IMPACT_DROP_COCONUT_HIT_RODENTG3_JACKMENHORN.wav" by JackMenhorn + "DepthBomb_05.aif" by ZeSoundResearchInc.

Proposed (round 54), cut from the HQ previews by `tools/concept/audio/sfx_r54e.py`.
Sources: <https://freesound.org/people/CGEffex/sounds/113679/> (Breaking_large_ice.wav by CGEffex, CC-BY 4.0), <https://freesound.org/people/JackMenhorn/sounds/136494/> (FOL_IMPACT_DROP_COCONUT_HIT_RODENTG3_JACKMENHORN.wav by JackMenhorn, CC-BY 4.0), <https://freesound.org/people/ZeSoundResearchInc./sounds/147880/> (DepthBomb_05.aif by ZeSoundResearchInc., CC0 1.0).
Use: Level 25 on, each Spiral Nautilus (five pairs in Level 25, orbiting the ship in opposite directions): one killed (100 HP; the closed shell glances off all but the specials and the anti-sub weapons), with the under-water `medium` burst, as the shell cracks into two halves that sink and the crown goes dark.
Brief: a big shell cracking: a hard, hollow, brittle crack and the halves breaking away, heavy and muffled, about 1.5 s; no splash, nothing that pops; a real recording, heavy, nothing bubbly or popping.
This one: a large sheet of ice cracking underfoot (meaty cracks and snaps) slowed to 0.6 and muffled, on a coconut's hollow knock on concrete slowed to 0.5, over a depth bomb's boom: sharper and more splintering than a, 1.8 s.
Edit: 1.80 s, the loudest 100 ms band −20.1 dB, peak −7.8 dBFS, −22.7 LUFS.
**Chosen** (round 54, S.6: b picked; to be rebuilt from the originals when the download limit allows).

### europa-trench-r54-a — "Deep Sea Ambience" by jhumbucker + "130525 rock stone moving scraping concrete shifting tectonic plate, 77mel" by TRP + "humpback_whales_and_orcas_ambient_underwater_recording" by raiden04

Proposed (round 54), cut from the HQ previews by `tools/concept/audio/sfx_r54e.py`.
Sources: <https://freesound.org/people/jhumbucker/sounds/193822/> (Deep Sea Ambience by jhumbucker, CC0 1.0), <https://freesound.org/people/TRP/sounds/715671/> (130525 rock stone moving scraping concrete shifting tectonic plate, 77mel by TRP, CC0 1.0), <https://freesound.org/people/raiden04/sounds/797762/> (humpback_whales_and_orcas_ambient_underwater_recording by raiden04, CC-BY 4.0).
Use: Level 25 throughout (and Level 26 at the scarp): the ambience bed of the Siren Trench, in place of `europa-under`.
Brief: the under-water bed lower and closer: a deep cut's heavy rumble close round the ship, far rock grinding now and then and distant whale calls; quiet, under the music, no bubbles, no drips, a seamless loop; a real recording, heavy, nothing bubbly or popping.
This one: the deep-sea rumble of `europa-under` slowed to 0.75 (lower and heavier), rock and concrete scraping and shifting slowed to 0.5 and muffled at 700 Hz far off, and humpback whales calling far off (a real hydrophone recording, other calls than `europa-under`'s) slowed to 0.7: 16.0 s loop.
Edit: 16.00 s, band RMS −42.4 dB, peak −10.9 dBFS, −28.0 LUFS.
**Chosen** (round 54, S.7: a picked; to be rebuilt from the originals when the download limit allows).

### europa-trench-r54-b — "Dark Cave Drone" by Kinoton + "Heavy stone door opens" by PostProdDog + "Blue Whale B call @ 5X" by MBARI_MARS

Proposed (round 54), cut from the HQ previews by `tools/concept/audio/sfx_r54e.py`.
Sources: <https://freesound.org/people/Kinoton/sounds/421826/> (Dark Cave Drone by Kinoton, CC0 1.0), <https://freesound.org/people/PostProdDog/sounds/578490/> (Heavy stone door opens by PostProdDog, CC0 1.0), <https://freesound.org/people/MBARI_MARS/sounds/343682/> (Blue Whale B call @ 5X by MBARI_MARS, CC-BY 4.0).
Use: Level 25 throughout (and Level 26 at the scarp): the ambience bed of the Siren Trench, in place of `europa-under`.
Brief: the under-water bed lower and closer: a deep cut's heavy rumble close round the ship, far rock grinding now and then and distant whale calls; quiet, under the music, no bubbles, no drips, a seamless loop; a real recording, heavy, nothing bubbly or popping.
This one: a dark cave drone (layered thunder-drum recordings, a low rumble with slow air swells) slowed to 0.8, a heavy stone door dragging slowed to 0.6 and muffled at 600 Hz far off, and a blue whale's B call (a deep-sea hydrophone recording at 900 m, played at 2.5 times its speed) far off: more of a closed cave than a, two long moans a loop, 16.0 s loop.
Edit: 16.00 s, band RMS −45.0 dB, peak −11.7 dBFS, −28.0 LUFS.
**Rejected** (round 54, S.7, 2026-10-23: the other picked); the file is in `rejected/`.

## Concept round 55, step AUD-F (Level 26: the Glow Angler, the Nautilus crown in the dark, Rook's loss, the headlights, the Choir's static, the black-water bed)

The generator is `tools/concept/audio/sfx_r55f.py` (Freesound HQ previews; nothing synthesized: the sfx are cut, slowed,
glided, layered, gated, pulsed, faded, filtered and looped). Pending the user's picks in round 55. Passed over (read
in their descriptions): GLORB's "Electric Arcs, Relays and Transformer Hum" (generated), a "Kill Switch (Large Breaker
Switch)" (generated by its author), beepbox-synthesized inverter hums, "Retro, Underwater Explosion" and other designed
explosions, a hydrophone wave recording tagged with bubbles, a telephone-pickup recording of a blinking lightbulb (a
steady 13 kHz whine, no flicker), a circuit-breaker recording that is a constant buzz (no clunk). The warm pad (music,
not here) is in [music/concept/prompts.md](../../music/concept/prompts.md).

### lure-hum-r55-a — "neon sign buzz hum stereo close lowcut to taste4.flac" by kyles

Proposed (round 55), cut from the HQ previews by `tools/concept/audio/sfx_r55f.py`.
Source: <https://freesound.org/people/kyles/sounds/453491/> (neon sign buzz hum stereo close lowcut to taste4.flac by kyles, CC0 1.0).
Use: Level 26 on, each Glow Angler (Level 26: 78 of them; the dark: its teal lure is its only visible part): while one is on the screen, its lure's pulsing teal bead (one quiet loop for all of them, the nearest, louder as the ship comes close; it stops with the last).
Brief: a faint electric hum, the lure's glow made audible: soft, a little enticing, pulsing slowly with the bead, low in the mix under the music; no crackle, no bubbles, a seamless loop; a real recording, heavy, nothing bubbly or popping.
This one: a neon sign's buzzing hum (recorded close) slowed to 0.6 and muffled at 1.4 kHz, pulsing slowly (every 1.25 s, 5 dB deep) with the bead: a low, soft electric buzz breathing in and out, 5.0 s loop.
Edit: 5.00 s, band RMS −38.0 dB, peak −20.1 dBFS, −32.4 LUFS.

### lure-hum-r55-b — "hum electric transformer in large room1.flac" by kyles + "Glass Harp - Singing Vibration" by Vrymaa

Proposed (round 55), cut from the HQ previews by `tools/concept/audio/sfx_r55f.py`.
Sources: <https://freesound.org/people/kyles/sounds/453444/> (hum electric transformer in large room1.flac by kyles, CC0 1.0), <https://freesound.org/people/Vrymaa/sounds/797583/> (Glass Harp - Singing Vibration by Vrymaa, CC0 1.0).
Use: Level 26 on, each Glow Angler (Level 26: 78 of them; the dark: its teal lure is its only visible part): while one is on the screen, its lure's pulsing teal bead (one quiet loop for all of them, the nearest, louder as the ship comes close; it stops with the last).
Brief: a faint electric hum, the lure's glow made audible: soft, a little enticing, pulsing slowly with the bead, low in the mix under the music; no crackle, no bubbles, a seamless loop; a real recording, heavy, nothing bubbly or popping.
This one: a transformer's hum in a large room slowed to 0.8 and muffled at 1.8 kHz, with a singing wine glass's clear tone faint on top, pulsing the same (4 dB deep): rounder and sweeter than a, a hum with a glassy shine in it, 5.0 s loop.
Edit: 5.00 s, band RMS −38.0 dB, peak −20.6 dBFS, −31.8 LUFS.

### angler-lunge-r55-a — "WATRMvmt underwater passby slosh medium light whoosh" by jerry.berumen + "Loud Chomping or Biting Sound with Chewing (Animal)" by OBXJohn + "DepthBomb_06.aif" by ZeSoundResearchInc.

Proposed (round 55), cut from the HQ previews by `tools/concept/audio/sfx_r55f.py`.
Sources: <https://freesound.org/people/jerry.berumen/sounds/772895/> (WATRMvmt underwater passby slosh medium light whoosh by jerry.berumen, CC-BY 4.0), <https://freesound.org/people/OBXJohn/sounds/535616/> (Loud Chomping or Biting Sound with Chewing (Animal) by OBXJohn, CC-BY 4.0), <https://freesound.org/people/ZeSoundResearchInc./sounds/147879/> (DepthBomb_06.aif by ZeSoundResearchInc., CC0 1.0).
Use: Level 26 on, each Glow Angler (Level 26: 78 of them; the dark: its teal lure is its only visible part): each lunge (the ship within 130 px, 100 on easy, 160 on hard, or touching the lure): from the start of its dive (up to 0.6 s at 220 px/s) to the jaws closing as it stops, just before its ring of 8 (the ring plays the enemy small shot); every 5 s at most an Angler.
Brief: a lunge out of the dark: a rushing surge of water building fast for half a second, then a heavy jaw snap, dull and big (no click, no bite crunch), muffled under water; no bubbles, no splash; a real recording, heavy, nothing bubbly or popping.
This one: a hydrophone's under-water pass-by surge (a real recording of water rushing past the microphone) slowed to 0.65 and muffled, swelling for 0.45 s into a dog's loud jaw snap (catching a treat in the air, 'like a bear trap slamming shut') slowed to 0.5 and muffled at 2.5 kHz, on a depth bomb's thump: a 'whoosh-CHOMP', 1.3 s.
Edit: 1.30 s, the loudest 100 ms band −19.1 dB, peak −6.7 dBFS, −20.5 LUFS.

### angler-lunge-r55-b — "Crashing Waves into Rocks 1" by amholma + "Book_Slammed_Down_01" by LewisEmmott5 + "DepthBomb_05.aif" by ZeSoundResearchInc.

Proposed (round 55), cut from the HQ previews by `tools/concept/audio/sfx_r55f.py`.
Sources: <https://freesound.org/people/amholma/sounds/376799/> (Crashing Waves into Rocks 1 by amholma, CC0 1.0), <https://freesound.org/people/LewisEmmott5/sounds/732154/> (Book_Slammed_Down_01 by LewisEmmott5, CC0 1.0), <https://freesound.org/people/ZeSoundResearchInc./sounds/147880/> (DepthBomb_05.aif by ZeSoundResearchInc., CC0 1.0).
Use: Level 26 on, each Glow Angler (Level 26: 78 of them; the dark: its teal lure is its only visible part): each lunge (the ship within 130 px, 100 on easy, 160 on hard, or touching the lure): from the start of its dive (up to 0.6 s at 220 px/s) to the jaws closing as it stops, just before its ring of 8 (the ring plays the enemy small shot); every 5 s at most an Angler.
Brief: a lunge out of the dark: a rushing surge of water building fast for half a second, then a heavy jaw snap, dull and big (no click, no bite crunch), muffled under water; no bubbles, no splash; a real recording, heavy, nothing bubbly or popping.
This one: a sea wave's rush as it rears against the rocks (before it breaks; the crash left out) slowed to 0.5 and muffled at 900 Hz, swelling into a heavy book slammed down on a table slowed to 0.45 and muffled at 2 kHz, on a depth bomb's thump: deeper and flatter than a, a 'whooom-THUD' of a big mouth shutting, 1.3 s.
Edit: 1.30 s, the loudest 100 ms band −19.0 dB, peak −5.3 dBFS, −19.1 LUFS.

### crown-hum-r55-a — "Singing Bowl, long without reverb" by hollandm

Proposed (round 55), cut from the HQ previews by `tools/concept/audio/sfx_r55f.py`.
Source: <https://freesound.org/people/hollandm/sounds/573805/> (Singing Bowl, long without reverb by hollandm, CC0 1.0).
Use: Level 26 on (the dark), each Spiral Nautilus, its violet crown the light it shows: while one is on the screen (one quiet loop for all of them, the nearest; with its shell spin, nautilus-spin; it stops with the last).
Brief: the violet crown's glow made audible: a low, soft, resonant hum, sung rather than electric (the lure's hum is electric), steady, under the spin's grind; no ring-out pings, a seamless loop; a real recording, heavy, nothing bubbly or popping.
This one: a Tibetan singing bowl rubbed round its rim (a long held tone, the bowl's strikes edited out by its recordist) slowed to 0.5 and muffled at 1 kHz: a deep, warm, steady 'ooom', 4.0 s loop.
Edit: 4.00 s, band RMS −37.9 dB, peak −31.7 dBFS, −35.5 LUFS.

### crown-hum-r55-b — "Glass Harp - Singing Vibration" by Vrymaa

Proposed (round 55), cut from the HQ previews by `tools/concept/audio/sfx_r55f.py`.
Source: <https://freesound.org/people/Vrymaa/sounds/797583/> (Glass Harp - Singing Vibration by Vrymaa, CC0 1.0).
Use: Level 26 on (the dark), each Spiral Nautilus, its violet crown the light it shows: while one is on the screen (one quiet loop for all of them, the nearest; with its shell spin, nautilus-spin; it stops with the last).
Brief: the violet crown's glow made audible: a low, soft, resonant hum, sung rather than electric (the lure's hum is electric), steady, under the spin's grind; no ring-out pings, a seamless loop; a real recording, heavy, nothing bubbly or popping.
This one: a singing wine glass (a finger round its wet rim, a long tonal note) slowed to 0.45 and muffled at 1.2 kHz: thinner and glassier than a, with a slow beating in the tone, 4.0 s loop.
Edit: 4.00 s, band RMS −37.9 dB, peak −30.1 dBFS, −35.5 LUFS.

### torpedo-trails-r55-a — "airliner_approach_2.aif" by Heigh-hoo

Proposed (round 55), cut from the HQ previews by `tools/concept/audio/sfx_r55f.py`.
Source: <https://freesound.org/people/Heigh-hoo/sounds/51491/> (airliner_approach_2.aif by Heigh-hoo, CC0 1.0).
Use: Level 26 at t=162.5 (the event ROOK_TRAILS; t=157 when proposed): Rook's loss, three thin torpedo trails rising from the bottom edge and converging on his lamp at t=163.5, where the hits take over (rook-hit); with Rook only (not with the Bulwark, not after he ejected); the Depth Hunters themselves are never seen.
Brief: a long, distant, rising hiss: three torpedoes running up fast from far below, the hiss growing and rising in pitch for half a second, then lost under the hits; far and muffled, no whistle, no bubbles; a real recording, heavy, nothing bubbly or popping.
This one: an airliner's jet approach heard from the ground (its roar swelling as it comes in) slowed and gliding up from 0.6 to 0.95, muffled at 2.5 kHz and without its low rumble: a far jet-like hiss rising fast, 1.2 s.
Edit: 1.20 s, the loudest 100 ms band −27.1 dB, peak −16.9 dBFS, −27.1 LUFS.

### torpedo-trails-r55-b — "G39-24-Steam Hiss.wav" by craigsmith + "G45-18-Submarine Motor Stop.wav" by craigsmith

Proposed (round 55), cut from the HQ previews by `tools/concept/audio/sfx_r55f.py`.
Sources: <https://freesound.org/people/craigsmith/sounds/438641/> (G39-24-Steam Hiss.wav by craigsmith, CC0 1.0), <https://freesound.org/people/craigsmith/sounds/438720/> (G45-18-Submarine Motor Stop.wav by craigsmith, CC0 1.0).
Use: Level 26 at t=162.5 (the event ROOK_TRAILS; t=157 when proposed): Rook's loss, three thin torpedo trails rising from the bottom edge and converging on his lamp at t=163.5, where the hits take over (rook-hit); with Rook only (not with the Bulwark, not after he ejected); the Depth Hunters themselves are never seen.
Brief: a long, distant, rising hiss: three torpedoes running up fast from far below, the hiss growing and rising in pitch for half a second, then lost under the hits; far and muffled, no whistle, no bubbles; a real recording, heavy, nothing bubbly or popping.
This one: a long steam hiss gliding up from 0.55 to 0.85, muffled at 3 kHz, over a submarine's motors speeding up (their whirr gliding 0.8 to 1.3): more machine than a, a torpedo's motor under the hiss, 1.2 s.
Edit: 1.20 s, the loudest 100 ms band −27.3 dB, peak −16.1 dBFS, −25.9 LUFS.

### rook-hit-r55-a — "DepthBomb_01.aif" by ZeSoundResearchInc. + "DepthBomb_02.aif" by ZeSoundResearchInc. + "DepthBomb_04.aif" by ZeSoundResearchInc. + "door metal heavy close both sides same time impact rattle2 +bg voices.wav" by kyles

Proposed (round 55), cut from the HQ previews by `tools/concept/audio/sfx_r55f.py`.
Sources: <https://freesound.org/people/ZeSoundResearchInc./sounds/147876/> (DepthBomb_01.aif by ZeSoundResearchInc., CC0 1.0), <https://freesound.org/people/ZeSoundResearchInc./sounds/147875/> (DepthBomb_02.aif by ZeSoundResearchInc., CC0 1.0), <https://freesound.org/people/ZeSoundResearchInc./sounds/147873/> (DepthBomb_04.aif by ZeSoundResearchInc., CC0 1.0), <https://freesound.org/people/kyles/sounds/407300/> (door metal heavy close both sides same time impact rattle2 +bg voices.wav by kyles, CC0 1.0).
Use: Level 26 at t=163.5 (the event ROOK_LOST; t=157.5 when proposed): the three torpedoes strike Rook's lamp, three white-orange flashes, then the lamp spins down (rook-spin plays with it); with Rook only, once a sortie (a retry replays it); harmless to the player.
Brief: three heavy hits in quick succession on a small hull: deep under-water blasts with a hard metal crunch in them, the last ringing off into a low rumble, about 2.5 s; the act's beat, bigger than a kill but not a boss death; no splash, no bubbles; a real recording, heavy, nothing bubbly or popping.
This one: three depth bombs' sub booms 0.13 s apart, the first and last on a heavy steel door's double slam (both halves shut at once) slowed to 0.6 and muffled at 2 kHz: 'BOOM-boom-BOOM' with a hull's clang in it, 2.6 s.
Edit: 2.60 s, the loudest 100 ms band −18.1 dB, peak −3.1 dBFS, −14.6 LUFS.

### rook-hit-r55-b — "Underwater Impact" by deleted_user_7709760 + "big metal side impact 3 scraping.wav" by Halleck + "DepthBomb_05.aif" by ZeSoundResearchInc.

Proposed (round 55), cut from the HQ previews by `tools/concept/audio/sfx_r55f.py`.
Sources: <https://freesound.org/people/deleted_user_7709760/sounds/400793/> (Underwater Impact by deleted_user_7709760, CC-BY 3.0), <https://freesound.org/people/Halleck/sounds/121623/> (big metal side impact 3 scraping.wav by Halleck, CC-BY 4.0), <https://freesound.org/people/ZeSoundResearchInc./sounds/147880/> (DepthBomb_05.aif by ZeSoundResearchInc., CC0 1.0).
Use: Level 26 at t=163.5 (the event ROOK_LOST; t=157.5 when proposed): the three torpedoes strike Rook's lamp, three white-orange flashes, then the lamp spins down (rook-spin plays with it); with Rook only, once a sortie (a retry replays it); harmless to the player.
Brief: three heavy hits in quick succession on a small hull: deep under-water blasts with a hard metal crunch in them, the last ringing off into a low rumble, about 2.5 s; the act's beat, bigger than a kill but not a boss death; no splash, no bubbles; a real recording, heavy, nothing bubbly or popping.
This one: three real under-water impacts 0.13 s apart (each slower and deeper), over a big metal side impact scraping slowed to 0.5 (the hull torn open) and a depth bomb's boom: more tearing metal than a, 2.6 s.
Edit: 2.60 s, the loudest 100 ms band −17.1 dB, peak −3.6 dBFS, −18.9 LUFS.

### rook-spin-r55-a — "Buzzing Faulty and Sparking Fluorescent Light" by iainmccurdy + "G45-18-Submarine Motor Stop.wav" by craigsmith

Proposed (round 55), cut from the HQ previews by `tools/concept/audio/sfx_r55f.py`.
Sources: <https://freesound.org/people/iainmccurdy/sounds/737060/> (Buzzing Faulty and Sparking Fluorescent Light by iainmccurdy, CC-BY 4.0), <https://freesound.org/people/craigsmith/sounds/438720/> (G45-18-Submarine Motor Stop.wav by craigsmith, CC0 1.0).
Use: Level 26 at t=163.5 (the event ROOK_LOST; t=157.5 when proposed), with the hits (rook-hit): Rook's lamp flickers and spins down off the bottom edge in 2.5 s, going dark; with Rook only.
Brief: a dying lamp and engine: an electrical flicker (the lamp's buzz cutting in and out), then his engine winding down into silence as he falls away, about 3 s, muffled; no pops, no bubbles; a real recording, heavy, nothing bubbly or popping.
This one: a faulty fluorescent tube's buzz (iainmccurdy's recording) muffled at 2.5 kHz and switched on and off in five irregular bursts over the first second (the lamp flickering), over a submarine's motors running down to a stop (an engine-room recording) slowed from 0.75 to 0.4 and fading out: 3.0 s.
Edit: 3.00 s, the loudest 100 ms band −22.0 dB, peak −8.7 dBFS, −23.6 LUFS.

### rook-spin-r55-b — "Fluorescent light fixture, buzz, clicks, noisy, lofi, 2011.wav" by TRP + "Wind-down 1.aif" by timgormly

Proposed (round 55), cut from the HQ previews by `tools/concept/audio/sfx_r55f.py`.
Sources: <https://freesound.org/people/TRP/sounds/577277/> (Fluorescent light fixture, buzz, clicks, noisy, lofi, 2011.wav by TRP, CC0 1.0), <https://freesound.org/people/timgormly/sounds/151288/> (Wind-down 1.aif by timgormly, CC0 1.0).
Use: Level 26 at t=163.5 (the event ROOK_LOST; t=157.5 when proposed), with the hits (rook-hit): Rook's lamp flickers and spins down off the bottom edge in 2.5 s, going dark; with Rook only.
Brief: a dying lamp and engine: an electrical flicker (the lamp's buzz cutting in and out), then his engine winding down into silence as he falls away, about 3 s, muffled; no pops, no bubbles; a real recording, heavy, nothing bubbly or popping.
This one: a fluorescent fixture flickering on with its buzz (the tube's starter muffled at 1.8 kHz) slowed to 0.8, over a machine's motor winding down after it is switched off, slowed from 0.8 to 0.5: lighter and higher than a, a smaller engine whining down, 3.0 s.
Edit: 3.00 s, the loudest 100 ms band −22.1 dB, peak −9.6 dBFS, −30.2 LUFS.

### headlight-click-r55-a — "Contactor/motor overload relay noises" by tt_runscript + "Deep Thumps.wav" by eardeer

Proposed (round 55), cut from the HQ previews by `tools/concept/audio/sfx_r55f.py`.
Sources: <https://freesound.org/people/tt_runscript/sounds/807384/> (Contactor/motor overload relay noises by tt_runscript, CC0 1.0), <https://freesound.org/people/eardeer/sounds/400997/> (Deep Thumps.wav by eardeer, CC0 1.0).
Use: Level 26: once as the ship's twin headlights come on at the level start (t=0), and as the last pipeline lamps go out behind the ship (t=8-18, the dark coming down); not on the Sonar Pulse.
Brief: a heavy relay clunk: a big electrical switch closing, deep and solid, short; muffled, no click-pop at the top; a real recording, heavy, nothing bubbly or popping.
This one: a 90 A motor contactor (a Siemens industrial switch) closing with its loud thunk, slowed to 0.6 and muffled at 2.5 kHz, on a deep thump: a solid 'KA-CHUNK', 0.6 s.
Edit: 0.60 s, the loudest 100 ms band −22.3 dB, peak −8.0 dBFS, −23.8 LUFS.

### headlight-click-r55-b — "switching_a_rattling_lever_with_room_ambience.wav" by mediatheksuche + "hum electric transformer in large room1.flac" by kyles

Proposed (round 55), cut from the HQ previews by `tools/concept/audio/sfx_r55f.py`.
Sources: <https://freesound.org/people/mediatheksuche/sounds/623712/> (switching_a_rattling_lever_with_room_ambience.wav by mediatheksuche, CC0 1.0), <https://freesound.org/people/kyles/sounds/453444/> (hum electric transformer in large room1.flac by kyles, CC0 1.0).
Use: Level 26: once as the ship's twin headlights come on at the level start (t=0), and as the last pipeline lamps go out behind the ship (t=8-18, the dark coming down); not on the Sonar Pulse.
Brief: a heavy relay clunk: a big electrical switch closing, deep and solid, short; muffled, no click-pop at the top; a real recording, heavy, nothing bubbly or popping.
This one: a heavy rattling lever switch thrown, slowed to 0.55 and muffled at 2.5 kHz, with a transformer's hum swelling up after it for a moment (the lamps taking the power): a 'CLACK-hmmm', 0.8 s.
Edit: 0.80 s, the loudest 100 ms band −22.1 dB, peak −6.5 dBFS, −24.2 LUFS.

### choir-static-r55-a — "Washy Radio Interference - Industrial SFX" by 9voltplasma

Proposed (round 55), cut from the HQ previews by `tools/concept/audio/sfx_r55f.py`.
Source: <https://freesound.org/people/9voltplasma/sounds/724710/> (Washy Radio Interference - Industrial SFX by 9voltplasma, CC0 1.0).
Use: Level 26 at t=97: the Choir's static showing in the HUD portrait frame, the 2 s before its whispered line (t=99, the music ducking 6 dB under the line); once a sortie. (Or no sound: the static is a look; the round decides).
Brief: radio interference creeping into the comms: a soft washing hiss and murmur swelling for 2 s and dropping away as the whisper starts; under the voices, no crackle bursts, no squeal; a real recording, heavy, nothing bubbly or popping.
This one: an AM boombox's washy interference (a real recording) slowed to 0.8, its crackle smoothed out (a 40 ms running median per frequency), muffled at 2.5 kHz and swelling over 2 s: a hollow, washing murmur, 2.4 s.
Edit: 2.40 s, the loudest 100 ms band −28.1 dB, peak −15.1 dBFS, −29.8 LUFS.

### choir-static-r55-b — "Radio interference when searching for a station 001" by Wastefield + "hum electric transformer in large room1.flac" by kyles

Proposed (round 55), cut from the HQ previews by `tools/concept/audio/sfx_r55f.py`.
Sources: <https://freesound.org/people/Wastefield/sounds/691079/> (Radio interference when searching for a station 001 by Wastefield, CC0 1.0), <https://freesound.org/people/kyles/sounds/453444/> (hum electric transformer in large room1.flac by kyles, CC0 1.0).
Use: Level 26 at t=97: the Choir's static showing in the HUD portrait frame, the 2 s before its whispered line (t=99, the music ducking 6 dB under the line); once a sortie. (Or no sound: the static is a look; the round decides).
Brief: radio interference creeping into the comms: a soft washing hiss and murmur swelling for 2 s and dropping away as the whisper starts; under the voices, no crackle bursts, no squeal; a real recording, heavy, nothing bubbly or popping.
This one: a radio's white-noise hiss between stations slowed to 0.6 and muffled at 1.8 kHz, over a transformer's hum, swelling over 2 s: a plainer, softer hiss than a with a low hum under it, 2.4 s.
Edit: 2.40 s, the loudest 100 ms band −28.2 dB, peak −16.2 dBFS, −28.3 LUFS.

### europa-black-r55-a — "G45-22-Submarine Air Conditioner.wav" by craigsmith + "Earthquake  **audible only with appropriate speakers**" by MBARI_MARS + "blue whale D calls **audible only with appropriate speakers**" by MBARI_MARS

Proposed (round 55), cut from the HQ previews by `tools/concept/audio/sfx_r55f.py`.
Sources: <https://freesound.org/people/craigsmith/sounds/438724/> (G45-22-Submarine Air Conditioner.wav by craigsmith, CC0 1.0), <https://freesound.org/people/MBARI_MARS/sounds/369485/> (Earthquake  **audible only with appropriate speakers** by MBARI_MARS, CC0 1.0), <https://freesound.org/people/MBARI_MARS/sounds/681896/> (blue whale D calls **audible only with appropriate speakers** by MBARI_MARS, CC-BY 4.0).
Use: Level 26 throughout but the scarp (where `europa-trench` takes over): the ambience bed of the black water, in place of `europa-under`.
Brief: total dark: a heavy pressure drone close round the ship, nothing to see and almost nothing to hear, a very far call now and then (one a loop); quieter and emptier than `europa-under`, no bubbles, no drips, a seamless loop; a real recording, heavy, nothing bubbly or popping.
This one: a submarine's air conditioner (a steady low rumble recorded inside the boat) slowed to 0.5 and muffled at 500 Hz; once a loop a deep-sea earthquake's rumble (a hydrophone at 900 m) swelling in and out, and a blue whale's D call far off (a hydrophone at 900 m, sped up 2.2 times to be heard, a low falling moan): 24.0 s loop.
Edit: 24.00 s, band RMS −44.0 dB, peak −25.5 dBFS, −38.3 LUFS.

### europa-black-r55-b — "air pump drone and hum.aif" by Tomlija + "G45-24-Submarine Interior Engine Room.wav" by craigsmith + "Blue Whale B call @ 5X" by MBARI_MARS

Proposed (round 55), cut from the HQ previews by `tools/concept/audio/sfx_r55f.py`.
Sources: <https://freesound.org/people/Tomlija/sounds/101278/> (air pump drone and hum.aif by Tomlija, CC-BY 3.0), <https://freesound.org/people/craigsmith/sounds/438726/> (G45-24-Submarine Interior Engine Room.wav by craigsmith, CC0 1.0), <https://freesound.org/people/MBARI_MARS/sounds/343682/> (Blue Whale B call @ 5X by MBARI_MARS, CC-BY 4.0).
Use: Level 26 throughout but the scarp (where `europa-trench` takes over): the ambience bed of the black water, in place of `europa-under`.
Brief: total dark: a heavy pressure drone close round the ship, nothing to see and almost nothing to hear, a very far call now and then (one a loop); quieter and emptier than `europa-under`, no bubbles, no drips, a seamless loop; a real recording, heavy, nothing bubbly or popping.
This one: a giant air pump's drone (inflating a marquee) slowed to 0.55 and muffled at 400 Hz, a submarine engine room's undulating hum slowed to 0.45 under it; once a loop a blue whale's B call far off (a deep-sea hydrophone, slowed to 0.7): rounder and more machine-like than a, 24.0 s loop.
Edit: 24.00 s, band RMS −45.7 dB, peak −16.1 dBFS, −30.0 LUFS.

## Concept round 57, step AUD-G (Level 27: the vent field's bed, the heat plumes, the bend, the Depth Hunter's torpedoes and hull break, the relay beacons, the *Marlin*, the hull-code scan)

The generator is `tools/concept/audio/sfx_r57g.py` (Freesound HQ previews: the sfx are cut, slowed, glided, layered,
smoothed, pulsed, faded, filtered and looped; only the scan ping is synthesized). Round 57 closed (2026-10-25): the picks are bed a, tell b, eruption a, bend a, launch a, run b, impact b, hum a, break b, crack a, hull break a, scan b; the rejected files are in `rejected/`.
Passed over (read in their descriptions): "Simulated jet engine burner" (a processed gasoline burner), a "Rocket launch"
mixed from jets, fireworks and dynamite, synthesized power-line hums (ZynAddSubFX), "Underwater Rumble" and
"Underwater_rumble_CsG" (designed), Akkaittou's "UnderWater_Explosion" set (synthetic), the circuit-breaker "sparks"
(wavetable synthesis), torpedo launches built with V-Station and phaser bubbles, every mud pot, hot spring and
geothermal hydrophone recording (all bubbling), "Underwater swishes" tagged with bubbles. Reused, no entry: the Glassfin
shatter (round 52 b), the under-water explosion family (round 52 a), the Decoy Flares' launch (special-flares-r08-a).

### europa-vents-r57-a — "High Force Waterfall, Bottom of Gorge, Massive Roar, Teesdale UK - Stereo" by Hear_it_use_it + "d_s478_blowboiler.wav" by relwin + "Metallic Groan" by hinchinbrook

Proposed (round 57), cut from the HQ previews by `tools/concept/audio/sfx_r57g.py`.
Sources: <https://freesound.org/people/Hear_it_use_it/sounds/849934/> (High Force Waterfall, Bottom of Gorge, Massive Roar, Teesdale UK - Stereo by Hear_it_use_it, CC-BY 4.0), <https://freesound.org/people/relwin/sounds/686055/> (d_s478_blowboiler.wav by relwin, CC0 1.0), <https://freesound.org/people/hinchinbrook/sounds/496836/> (Metallic Groan by hinchinbrook, CC0 1.0).
Use: Level 27 throughout: the ambience bed of the vent field (a caldera variant may follow for Level 28's arena), in place of `europa-under`; under the music, and alone with Vorne's motif while Varga reads the hull code (t=68-82).
Brief: a deep rumble of hot water, a distant black-smoker roar swelling now and then, a slow groan of the chimneys; heavy, warm and close, louder and busier than `europa-black`; no bubbles, no boiling, no hiss-pops, a seamless loop; a real recording, heavy, nothing bubbly or popping.
This one: a waterfall's massive roar recorded at the bottom of its gorge (High Force, Teesdale) slowed to 0.45 and muffled at 350 Hz: a deep, steady rumble of water; once a loop a steam locomotive's boiler blowdown far off (slowed to 0.5, muffled at 500 Hz) swelling in and out over 8 s, the black smoker roaring, and a bunker door's low metal groan slowed to 0.4 (the chimneys): 24.0 s loop.
Edit: 24.00 s, band RMS −42.0 dB, peak −23.3 dBFS, −34.9 LUFS.

### europa-vents-r57-b — "NIAGARA WATERS ROARING.mp3" by KevinT1001 + "G53-24-River or Rapids or Waterfall.wav" by craigsmith + "Islanti, geysir purkautuu, turistit / Iceland, geyser erupting twice, tourists reacting, talking" by YleArkisto + "Metallic Groan" by hinchinbrook

Proposed (round 57), cut from the HQ previews by `tools/concept/audio/sfx_r57g.py`.
Sources: <https://freesound.org/people/KevinT1001/sounds/379905/> (NIAGARA WATERS ROARING.mp3 by KevinT1001, CC0 1.0), <https://freesound.org/people/craigsmith/sounds/438835/> (G53-24-River or Rapids or Waterfall.wav by craigsmith, CC0 1.0), <https://freesound.org/people/YleArkisto/sounds/263878/> (Islanti, geysir purkautuu, turistit / Iceland, geyser erupting twice, tourists reacting, talking by YleArkisto, CC-BY 4.0), <https://freesound.org/people/hinchinbrook/sounds/496836/> (Metallic Groan by hinchinbrook, CC0 1.0).
Use: Level 27 throughout: the ambience bed of the vent field (a caldera variant may follow for Level 28's arena), in place of `europa-under`; under the music, and alone with Vorne's motif while Varga reads the hull code (t=68-82).
Brief: a deep rumble of hot water, a distant black-smoker roar swelling now and then, a slow groan of the chimneys; heavy, warm and close, louder and busier than `europa-black`; no bubbles, no boiling, no hiss-pops, a seamless loop; a real recording, heavy, nothing bubbly or popping.
This one: Niagara's roar (a recording layered for its deep lows) slowed to 0.4 and muffled at 300 Hz, a river's rapids slowed to 0.3 under it: rounder and more surging than a; once a loop a geyser's eruption far off (Iceland's Geysir, slowed to 0.5, muffled at 500 Hz) and the bunker door's other groan slowed to 0.35: 24.0 s loop.
Edit: 24.00 s, band RMS −43.9 dB, peak −16.2 dBFS, −29.0 LUFS.

### plume-tell-r57-a — "Earthquake  **audible only with appropriate speakers**" by MBARI_MARS + "High Force Waterfall, Bottom of Gorge, Massive Roar, Teesdale UK - Stereo" by Hear_it_use_it

Proposed (round 57), cut from the HQ previews by `tools/concept/audio/sfx_r57g.py`.
Sources: <https://freesound.org/people/MBARI_MARS/sounds/369485/> (Earthquake  **audible only with appropriate speakers** by MBARI_MARS, CC0 1.0), <https://freesound.org/people/Hear_it_use_it/sounds/849934/> (High Force Waterfall, Bottom of Gorge, Massive Roar, Teesdale UK - Stereo by Hear_it_use_it, CC-BY 4.0).
Use: Level 27 from t=26 to the gate (sections 2-5): each heat plume (16 groups of 2-3 vents rippling 0.5 s apart, each vent every 4.5 s; easy every 6 s, hard every 4 s), one per vent: the 1.4 s tell (easy 1.8, hard 1.0) as the mouth glows amber and silt lifts off it, right before the eruption (plume-eruption).
Brief: a low rising rumble from below: the vent waking, deep and heavy, growing and rising in pitch into the eruption; no crack, no hiss-pop, no bubbles; a real recording, heavy, nothing bubbly or popping.
This one: a deep-sea earthquake's rumble (a hydrophone at 900 m) gliding up from 1.0 to 1.6 and swelling, the waterfall's roar low under it rising with it (0.3 to 0.5, muffled at 500 Hz): a rumble from the sea floor climbing into a roar, 1.4 s.
Edit: 1.40 s, the loudest 100 ms band −21.2 dB, peak −4.1 dBFS, −18.2 LUFS.

### plume-tell-r57-b — "d_s478_blowboiler.wav" by relwin + "G53-24-River or Rapids or Waterfall.wav" by craigsmith

Proposed (round 57), cut from the HQ previews by `tools/concept/audio/sfx_r57g.py`.
Sources: <https://freesound.org/people/relwin/sounds/686055/> (d_s478_blowboiler.wav by relwin, CC0 1.0), <https://freesound.org/people/craigsmith/sounds/438835/> (G53-24-River or Rapids or Waterfall.wav by craigsmith, CC0 1.0).
Use: Level 27 from t=26 to the gate (sections 2-5): each heat plume (16 groups of 2-3 vents rippling 0.5 s apart, each vent every 4.5 s; easy every 6 s, hard every 4 s), one per vent: the 1.4 s tell (easy 1.8, hard 1.0) as the mouth glows amber and silt lifts off it, right before the eruption (plume-eruption).
Brief: a low rising rumble from below: the vent waking, deep and heavy, growing and rising in pitch into the eruption; no crack, no hiss-pop, no bubbles; a real recording, heavy, nothing bubbly or popping.
This one: the boiler blowdown's roar slowed and gliding up from 0.3 to 0.55, muffled at 600 Hz, the rapids rising under it: more of a roar building than a's rumble, 1.4 s.
Edit: 1.40 s, the loudest 100 ms band −21.0 dB, peak −11.3 dBFS, −22.7 LUFS.

### plume-eruption-r57-a — "Islanti, geysir purkautuu, turistit / Iceland, geyser erupting twice, tourists reacting, talking" by YleArkisto + "High Force Waterfall, Bottom of Gorge, Massive Roar, Teesdale UK - Stereo" by Hear_it_use_it + "DepthBomb_01.aif" by ZeSoundResearchInc.

Proposed (round 57), cut from the HQ previews by `tools/concept/audio/sfx_r57g.py`.
Sources: <https://freesound.org/people/YleArkisto/sounds/263878/> (Islanti, geysir purkautuu, turistit / Iceland, geyser erupting twice, tourists reacting, talking by YleArkisto, CC-BY 4.0), <https://freesound.org/people/Hear_it_use_it/sounds/849934/> (High Force Waterfall, Bottom of Gorge, Massive Roar, Teesdale UK - Stereo by Hear_it_use_it, CC-BY 4.0), <https://freesound.org/people/ZeSoundResearchInc./sounds/147876/> (DepthBomb_01.aif by ZeSoundResearchInc., CC0 1.0).
Use: Level 27 from t=26 to the gate (sections 2-5): each heat plume (16 groups of 2-3 vents rippling 0.5 s apart, each vent every 4.5 s; easy every 6 s, hard every 4 s), one per vent: the 1.6 s eruption, the column of shimmering water standing over the mouth (it burns and bends shots), right after its tell.
Brief: a heavy roar of superheated water tearing upward: big and dense, a soft onset (no hiss-pop at the start), the roar dying away at the end; muffled, no bubbles, no boiling; a real recording, heavy, nothing bubbly or popping.
This one: Iceland's Geysir erupting (a 1981 tape recording, the column itself) slowed to 0.7 and muffled at 1.8 kHz, the waterfall's roar under it slowed to 0.6 and a depth bomb's low boom: a deep, wet roar with weight under it, 2.0 s.
Edit: 2.00 s, the loudest 100 ms band −19.1 dB, peak −5.6 dBFS, −16.5 LUFS.

### plume-eruption-r57-b — "d_s478_blowboiler.wav" by relwin + "NIAGARA WATERS ROARING.mp3" by KevinT1001 + "DepthBomb_05.aif" by ZeSoundResearchInc.

Proposed (round 57), cut from the HQ previews by `tools/concept/audio/sfx_r57g.py`.
Sources: <https://freesound.org/people/relwin/sounds/686055/> (d_s478_blowboiler.wav by relwin, CC0 1.0), <https://freesound.org/people/KevinT1001/sounds/379905/> (NIAGARA WATERS ROARING.mp3 by KevinT1001, CC0 1.0), <https://freesound.org/people/ZeSoundResearchInc./sounds/147880/> (DepthBomb_05.aif by ZeSoundResearchInc., CC0 1.0).
Use: Level 27 from t=26 to the gate (sections 2-5): each heat plume (16 groups of 2-3 vents rippling 0.5 s apart, each vent every 4.5 s; easy every 6 s, hard every 4 s), one per vent: the 1.6 s eruption, the column of shimmering water standing over the mouth (it burns and bends shots), right after its tell.
Brief: a heavy roar of superheated water tearing upward: big and dense, a soft onset (no hiss-pop at the start), the roar dying away at the end; muffled, no bubbles, no boiling; a real recording, heavy, nothing bubbly or popping.
This one: the boiler blowdown (a steam locomotive blowing its boiler down) slowed to 0.6 and muffled at 1.5 kHz over Niagara's roar slowed to 0.55, a depth bomb's low boom: steadier and more steam-like than a, a jet of pressure, 2.0 s.
Edit: 2.00 s, the loudest 100 ms band −19.0 dB, peak −8.1 dBFS, −17.4 LUFS.

### bend-swish-r57-a — "WATRMvmt-Samsung Galaxy Smartphone, CU_Whoosh By_Nicholas Judy_TDC" by designerschoice

Proposed (round 57), cut from the HQ previews by `tools/concept/audio/sfx_r57g.py`.
Source: <https://freesound.org/people/designerschoice/sounds/814845/> (WATRMvmt-Samsung Galaxy Smartphone, CU_Whoosh By_Nicholas Judy_TDC by designerschoice, CC-BY 4.0).
Use: Level 27 from t=26: each bullet a plume's erupting disc turns 25° (the player's shots, enemy bullets, torpedoes; once a bullet), so very often under fire; at most a few a second (the game throttles it).
Brief: a short, soft swish of water as the shot is turned: quick and light, well under the shots; muffled, no splash, no bubbles; a real recording, heavy, nothing bubbly or popping.
This one: water whooshing by (a phone recording close up) slowed to 0.8 and muffled at 1.5 kHz: a soft 'fsshh', 0.4 s.
Edit: 0.40 s, the loudest 100 ms band −27.5 dB, peak −8.7 dBFS, −inf LUFS.

### bend-swish-r57-b — "WATRMvmt underwater passby slosh medium light whoosh" by jerry.berumen

Proposed (round 57), cut from the HQ previews by `tools/concept/audio/sfx_r57g.py`.
Source: <https://freesound.org/people/jerry.berumen/sounds/772895/> (WATRMvmt underwater passby slosh medium light whoosh by jerry.berumen, CC-BY 4.0).
Use: Level 27 from t=26: each bullet a plume's erupting disc turns 25° (the player's shots, enemy bullets, torpedoes; once a bullet), so very often under fire; at most a few a second (the game throttles it).
Brief: a short, soft swish of water as the shot is turned: quick and light, well under the shots; muffled, no splash, no bubbles; a real recording, heavy, nothing bubbly or popping.
This one: a hydrophone's under-water pass-by whoosh (water rushing past the microphone) sped to 1.1 and muffled at 1.3 kHz: rounder and more under-water than a, a 'whoom', 0.4 s.
Edit: 0.40 s, the loudest 100 ms band −27.1 dB, peak −14.5 dBFS, −inf LUFS.

### torpedo-launch-r57-a — "Cannon Fires, Take 1, Quarter Speed" by CHallSmith + "G45-18-Submarine Motor Stop.wav" by craigsmith

Proposed (round 57), cut from the HQ previews by `tools/concept/audio/sfx_r57g.py`.
Sources: <https://freesound.org/people/CHallSmith/sounds/870742/> (Cannon Fires, Take 1, Quarter Speed by CHallSmith, CC0 1.0), <https://freesound.org/people/craigsmith/sounds/438720/> (G45-18-Submarine Motor Stop.wav by craigsmith, CC0 1.0).
Use: Level 27 (and Level 28 as escorts): each Depth Hunter (10 at L27, in columns of 4 rising from below): each torpedo it fires up the screen (every 3.5 s a Hunter; easy 4.5, hard 2.8; up to 8 torpedoes in flight).
Brief: a heavy pneumatic launch thump: a tube blowing a torpedo out, deep and short, the motor catching after it; built, not grown; muffled, no bubbles; a real recording, heavy, nothing bubbly or popping.
This one: an air cannon's blast (compressed air through a pipe, recorded at quarter speed) slowed to 0.8 and muffled at 1.2 kHz, a submarine motor's whirr speeding up after it: a deep 'THOOMP' and a motor spinning up, 0.9 s.
Edit: 0.90 s, the loudest 100 ms band −23.1 dB, peak −8.7 dBFS, −21.4 LUFS.

### torpedo-launch-r57-b — "Tire Bead Seater Air Blast" by j_soundeffects + "Deep Thumps.wav" by eardeer

Proposed (round 57), cut from the HQ previews by `tools/concept/audio/sfx_r57g.py`.
Sources: <https://freesound.org/people/j_soundeffects/sounds/863981/> (Tire Bead Seater Air Blast by j_soundeffects, CC-BY 4.0), <https://freesound.org/people/eardeer/sounds/400997/> (Deep Thumps.wav by eardeer, CC0 1.0).
Use: Level 27 (and Level 28 as escorts): each Depth Hunter (10 at L27, in columns of 4 rising from below): each torpedo it fires up the screen (every 3.5 s a Hunter; easy 4.5, hard 2.8; up to 8 torpedoes in flight).
Brief: a heavy pneumatic launch thump: a tube blowing a torpedo out, deep and short, the motor catching after it; built, not grown; muffled, no bubbles; a real recording, heavy, nothing bubbly or popping.
This one: a tyre bead seater's air blast (a compressed-air bazooka) slowed to 0.6 and muffled at 1 kHz, on a deep thump: a harder, airier 'WHUMP' than a, no motor, 0.8 s.
Edit: 0.80 s, the loudest 100 ms band −23.1 dB, peak −8.0 dBFS, −21.2 LUFS.

### torpedo-run-r57-a — "underwater ship propeller 3.45 PM 220920_0532.wav" by klankbeeld

Proposed (round 57), cut from the HQ previews by `tools/concept/audio/sfx_r57g.py`.
Source: <https://freesound.org/people/klankbeeld/sounds/680037/> (underwater ship propeller 3.45 PM 220920_0532.wav by klankbeeld, CC-BY 4.0).
Use: Level 27 (and Level 28 as escorts): each Depth Hunter (10 at L27, in columns of 4 rising from below): while a torpedo runs (one quiet loop for all of them, the nearest, louder as it closes on the ship; it stops with the last).
Brief: a torpedo's thruster: a small, fast propeller and motor churning through the water, a steady muffled whirr, menacing as it nears; no bubbles, a seamless loop; a real recording, heavy, nothing bubbly or popping.
This one: a river boat's propeller recorded under water with hydrophones (its rhythmic churn) sped to 1.3 and muffled at 1.5 kHz: a smaller, faster screw beating, 4.0 s loop.
Edit: 4.00 s, band RMS −38.2 dB, peak −17.4 dBFS, −32.5 LUFS.

### torpedo-run-r57-b — "coast_guard_zodiac_14may2007_05.mp3" by aguasonic

Proposed (round 57), cut from the HQ previews by `tools/concept/audio/sfx_r57g.py`.
Source: <https://freesound.org/people/aguasonic/sounds/52205/> (coast_guard_zodiac_14may2007_05.mp3 by aguasonic, CC-BY 4.0).
Use: Level 27 (and Level 28 as escorts): each Depth Hunter (10 at L27, in columns of 4 rising from below): while a torpedo runs (one quiet loop for all of them, the nearest, louder as it closes on the ship; it stops with the last).
Brief: a torpedo's thruster: a small, fast propeller and motor churning through the water, a steady muffled whirr, menacing as it nears; no bubbles, a seamless loop; a real recording, heavy, nothing bubbly or popping.
This one: a Coast Guard Zodiac's outboards at full throttle heard under water (a hydrophone) slowed to 0.8 and muffled at 1.4 kHz: a smoother, whining motor drone than a, 4.0 s loop.
Edit: 4.00 s, band RMS −38.1 dB, peak −24.2 dBFS, −33.7 LUFS.

### torpedo-impact-r57-a — "Underwater Impact" by deleted_user_7709760 + "heavy_mechanical_impacts_different_pitches_from_4_drawer_metal_file_cabinet_03232024" by Artninja

Proposed (round 57), cut from the HQ previews by `tools/concept/audio/sfx_r57g.py`.
Sources: <https://freesound.org/people/deleted_user_7709760/sounds/400793/> (Underwater Impact by deleted_user_7709760, CC-BY 3.0), <https://freesound.org/people/Artninja/sounds/751170/> (heavy_mechanical_impacts_different_pitches_from_4_drawer_metal_file_cabinet_03232024 by Artninja, CC-BY 4.0).
Use: Level 27 (and Level 28 as escorts): each Depth Hunter (10 at L27, in columns of 4 rising from below): a torpedo bursting on the ship (`heavy` 10), the Bulwark or Rook; a torpedo shot down plays the under-water `tiny` burst instead.
Brief: a heavy hit on the hull under water: a deep pressure thud with a hard metal clang in it, short; bigger than a bullet hit; no splash, no bubbles; a real recording, heavy, nothing bubbly or popping.
This one: a real under-water impact slowed to 0.75 and muffled, over a heavy metal file cabinet's impact slowed to 0.6: a deep 'DOOM' with a steel clang in it, 1.5 s.
Edit: 1.50 s, the loudest 100 ms band −19.0 dB, peak −7.8 dBFS, −22.0 LUFS.

### torpedo-impact-r57-b — "DepthBomb_04.aif" by ZeSoundResearchInc. + "door metal heavy close both sides same time impact rattle1 on offmic +bg voices.wav" by kyles

Proposed (round 57), cut from the HQ previews by `tools/concept/audio/sfx_r57g.py`.
Sources: <https://freesound.org/people/ZeSoundResearchInc./sounds/147873/> (DepthBomb_04.aif by ZeSoundResearchInc., CC0 1.0), <https://freesound.org/people/kyles/sounds/407301/> (door metal heavy close both sides same time impact rattle1 on offmic +bg voices.wav by kyles, CC0 1.0).
Use: Level 27 (and Level 28 as escorts): each Depth Hunter (10 at L27, in columns of 4 rising from below): a torpedo bursting on the ship (`heavy` 10), the Bulwark or Rook; a torpedo shot down plays the under-water `tiny` burst instead.
Brief: a heavy hit on the hull under water: a deep pressure thud with a hard metal clang in it, short; bigger than a bullet hit; no splash, no bubbles; a real recording, heavy, nothing bubbly or popping.
This one: a depth bomb's boom (its sub cut below 70 Hz) with a heavy steel door's slam (both halves at once, off-mic) slowed to 0.5 in it: rounder and more boom than a, the clang further off, 1.6 s.
Edit: 1.60 s, the loudest 100 ms band −21.0 dB, peak −3.1 dBFS, −15.0 LUFS.

### beacon-hum-r57-a — "Standing under large power lines during light rain buzzing" by Whats_The_Frequency_Kennett

Proposed (round 57), cut from the HQ previews by `tools/concept/audio/sfx_r57g.py`.
Source: <https://freesound.org/people/Whats_The_Frequency_Kennett/sounds/760860/> (Standing under large power lines during light rain buzzing by Whats_The_Frequency_Kennett, CC0 1.0).
Use: Level 27's Beacon Ridge (t≈94-124): each of the four relay beacons, the secondary's targets: while a beacon lives on the screen (one quiet loop for all of them, the nearest; its lamp pulses red-gold every 4 s; it stops with the last or when the ridge scrolls off).
Brief: a low electrical hum, the relay alive: steady, swelling once with each 4 s pulse of the lamp, low in the mix; human-built (not the Vrell songs), no crackle, a seamless loop; a real recording, heavy, nothing bubbly or popping.
This one: big power lines buzzing in light rain (a real recording under them), the rain's crackle smoothed out, slowed to 0.7 and muffled at 1.2 kHz, swelling once a loop (5 dB) with the lamp: a high-voltage buzz breathing every 4 s, 4.0 s loop.
Edit: 4.00 s, band RMS −38.1 dB, peak −18.8 dBFS, −32.5 LUFS.

### beacon-hum-r57-b — "hum electric transformer in large room1.flac" by kyles

Proposed (round 57), cut from the HQ previews by `tools/concept/audio/sfx_r57g.py`.
Source: <https://freesound.org/people/kyles/sounds/453444/> (hum electric transformer in large room1.flac by kyles, CC0 1.0).
Use: Level 27's Beacon Ridge (t≈94-124): each of the four relay beacons, the secondary's targets: while a beacon lives on the screen (one quiet loop for all of them, the nearest; its lamp pulses red-gold every 4 s; it stops with the last or when the ridge scrolls off).
Brief: a low electrical hum, the relay alive: steady, swelling once with each 4 s pulse of the lamp, low in the mix; human-built (not the Vrell songs), no crackle, a seamless loop; a real recording, heavy, nothing bubbly or popping.
This one: a transformer's hum in a large room slowed to 0.7 and muffled at 1 kHz, swelling the same: lower and rounder than a, a hum rather than a buzz, 4.0 s loop.
Edit: 4.00 s, band RMS −38.0 dB, peak −17.9 dBFS, −31.1 LUFS.

### beacon-break-r57-a — "Standing under large power lines during light rain buzzing" by Whats_The_Frequency_Kennett + "tree cut crack and fall in forest3 movement not thud.flac" by kyles

Proposed (round 57), cut from the HQ previews by `tools/concept/audio/sfx_r57g.py`.
Sources: <https://freesound.org/people/Whats_The_Frequency_Kennett/sounds/760860/> (Standing under large power lines during light rain buzzing by Whats_The_Frequency_Kennett, CC0 1.0), <https://freesound.org/people/kyles/sounds/637912/> (tree cut crack and fall in forest3 movement not thud.flac by kyles, CC0 1.0).
Use: Level 27's Beacon Ridge (t≈94-124): each of the four relay beacons, the secondary's targets: a beacon destroyed (60 HP; with its under-water burst): the lamp goes dark and the mast breaks (the hum loop stops).
Brief: the relay dying: its hum dropping away, a heavy mast snapping and falling; muffled, about 2 s; no sparks crackle, no pops, no bubbles; a real recording, heavy, nothing bubbly or popping.
This one: the power line hum (smoothed) dying from 0.7 to 0.3, under a tree trunk's crack as it is felled and starts to fall, slowed to 0.5 and muffled at 2.5 kHz: the hum sinking away under a long splintering crack, 2.2 s.
Edit: 2.25 s, the loudest 100 ms band −19.3 dB, peak −3.1 dBFS, −18.4 LUFS.

### beacon-break-r57-b — "hum electric transformer in large room1.flac" by kyles + "Cracking, snapping, and clanging (metal can)" by Ashley12275 + "door metal heavy close both sides same time impact rattle1 on offmic +bg voices.wav" by kyles

Proposed (round 57), cut from the HQ previews by `tools/concept/audio/sfx_r57g.py`.
Sources: <https://freesound.org/people/kyles/sounds/453444/> (hum electric transformer in large room1.flac by kyles, CC0 1.0), <https://freesound.org/people/Ashley12275/sounds/518731/> (Cracking, snapping, and clanging (metal can) by Ashley12275, CC0 1.0), <https://freesound.org/people/kyles/sounds/407301/> (door metal heavy close both sides same time impact rattle1 on offmic +bg voices.wav by kyles, CC0 1.0).
Use: Level 27's Beacon Ridge (t≈94-124): each of the four relay beacons, the secondary's targets: a beacon destroyed (60 HP; with its under-water burst): the lamp goes dark and the mast breaks (the hum loop stops).
Brief: the relay dying: its hum dropping away, a heavy mast snapping and falling; muffled, about 2 s; no sparks crackle, no pops, no bubbles; a real recording, heavy, nothing bubbly or popping.
This one: the transformer hum dying the same, under a metal can's snap and clang slowed to 0.35 (a mast snapping) and a heavy steel door's clang slowed to 0.5 as it lands: more metal than a, 2.0 s.
Edit: 2.00 s, the loudest 100 ms band −19.0 dB, peak −4.6 dBFS, −20.3 LUFS.

### marlin-crack-r57-a — "tree cut crack and fall in forest1 mono.flac" by kyles + "Deep Thumps.wav" by eardeer

Proposed (round 57), cut from the HQ previews by `tools/concept/audio/sfx_r57g.py`.
Sources: <https://freesound.org/people/kyles/sounds/637910/> (tree cut crack and fall in forest1 mono.flac by kyles, CC0 1.0), <https://freesound.org/people/eardeer/sounds/400997/> (Deep Thumps.wav by eardeer, CC0 1.0).
Use: Level 27 at t≈40, once a run (the secret): the hauler *Marlin* destroyed (20 HP, 28 on hard), the Vrell resin sealing her hold cracking open; her crate floats up.
Brief: a thick skin of resin cracking open: a deep, splintering crack, heavy and dull, with the hull under it; muffled, about 1.5 s; no glassy tinkle, no pops; a real recording, heavy, nothing bubbly or popping.
This one: a tree trunk cracking as it is felled (the trunk's last crack) slowed to 0.45 and muffled at 2 kHz, on a deep thump: a deep, woody 'KRRRACK', 1.6 s.
Edit: 1.60 s, the loudest 100 ms band −20.0 dB, peak −5.3 dBFS, −19.1 LUFS.

### marlin-crack-r57-b — "ice cracking breaking up on river styrofoam squeaking awesome great detail1 stereo XY.flac" by kyles + "Metallic Groan" by hinchinbrook

Proposed (round 57), cut from the HQ previews by `tools/concept/audio/sfx_r57g.py`.
Sources: <https://freesound.org/people/kyles/sounds/637606/> (ice cracking breaking up on river styrofoam squeaking awesome great detail1 stereo XY.flac by kyles, CC0 1.0), <https://freesound.org/people/hinchinbrook/sounds/496836/> (Metallic Groan by hinchinbrook, CC0 1.0).
Use: Level 27 at t≈40, once a run (the secret): the hauler *Marlin* destroyed (20 HP, 28 on hard), the Vrell resin sealing her hold cracking open; her crate floats up.
Brief: a thick skin of resin cracking open: a deep, splintering crack, heavy and dull, with the hull under it; muffled, about 1.5 s; no glassy tinkle, no pops; a real recording, heavy, nothing bubbly or popping.
This one: a river's ice cracking and breaking up (a field recording, close) slowed to 0.4 and muffled at 2.2 kHz, the bunker door's metal groan slowed under it (the hull): a crackling, glassier break than a, 1.6 s.
Edit: 1.60 s, the loudest 100 ms band −20.0 dB, peak −4.7 dBFS, −21.0 LUFS.

### hunter-break-r57-a — "Scrap metal dropping / crashing" by SamsterBirdies + "G45-18-Submarine Motor Stop.wav" by craigsmith

Proposed (round 57), cut from the HQ previews by `tools/concept/audio/sfx_r57g.py`.
Sources: <https://freesound.org/people/SamsterBirdies/sounds/587443/> (Scrap metal dropping / crashing by SamsterBirdies, CC0 1.0), <https://freesound.org/people/craigsmith/sounds/438720/> (G45-18-Submarine Motor Stop.wav by craigsmith, CC0 1.0).
Use: Level 27 (and Level 28 as escorts): each Depth Hunter (10 at L27, in columns of 4 rising from below): each one destroyed, over its `medium` under-water burst (the explosion family's a): its hull breaking up, grey debris and an oil bloom; no wreck.
Brief: a machine breaking up: metal crumpling and tearing, its motor dying; it must sound built, not grown (no screech); muffled, about 2 s; no bubbles; a real recording, heavy, nothing bubbly or popping.
This one: a pile of scrap metal dropped and crashing on concrete, slowed to 0.55 and muffled at 2.2 kHz, a submarine's motors running down under it: a heavy crumpling crash and a dying whine, 1.8 s.
Edit: 1.80 s, the loudest 100 ms band −19.1 dB, peak −8.0 dBFS, −19.1 LUFS.

### hunter-break-r57-b — "Car Crash (with Glass)" by magnuswaker + "heavy_mechanical_impacts_different_pitches_from_4_drawer_metal_file_cabinet_03232024" by Artninja

Proposed (round 57), cut from the HQ previews by `tools/concept/audio/sfx_r57g.py`.
Sources: <https://freesound.org/people/magnuswaker/sounds/592388/> (Car Crash (with Glass) by magnuswaker, CC0 1.0), <https://freesound.org/people/Artninja/sounds/751170/> (heavy_mechanical_impacts_different_pitches_from_4_drawer_metal_file_cabinet_03232024 by Artninja, CC-BY 4.0).
Use: Level 27 (and Level 28 as escorts): each Depth Hunter (10 at L27, in columns of 4 rising from below): each one destroyed, over its `medium` under-water burst (the explosion family's a): its hull breaking up, grey debris and an oil bloom; no wreck.
Brief: a machine breaking up: metal crumpling and tearing, its motor dying; it must sound built, not grown (no screech); muffled, about 2 s; no bubbles; a real recording, heavy, nothing bubbly or popping.
This one: PC side panels dropped on each other (a 'car crash' foley) slowed to 0.5 and muffled at 2 kHz, a heavy metal file cabinet's double impact slowed in it: a tighter, more panel-like crunch than a, no motor, 1.8 s.
Edit: 1.80 s, the loudest 100 ms band −19.1 dB, peak −6.1 dBFS, −19.2 LUFS.

### scan-ping-r57-a — synthesized

Proposed (round 57), synthesized by `tools/concept/audio/sfx_r57g.py` (no third-party source).
Use: Level 27 at t≈68, once a run: Varga's hull-code scan of the wreck as it scrolls in (her line at t=70; Vorne's motif alone over the ambience).
Brief: a single scanner ping: clean, clear and short with a soft water tail, an instrument, not the Sonar Pulse's sonar ping; well under the voice and the motif (synthesized: a UI-like sound).
This one: synthesized: one clean 1.5 kHz ping (4 ms attack, 0.22 s decay) with a soft second partial, through a 1.6 s water reverb: a bright 'pinnng', 2.2 s with its tail.
Edit: 2.20 s, the loudest 100 ms band −24.0 dB, peak −17.5 dBFS, −27.1 LUFS.

### scan-ping-r57-b — synthesized

Proposed (round 57), synthesized by `tools/concept/audio/sfx_r57g.py` (no third-party source).
Use: Level 27 at t≈68, once a run: Varga's hull-code scan of the wreck as it scrolls in (her line at t=70; Vorne's motif alone over the ambience).
Brief: a single scanner ping: clean, clear and short with a soft water tail, an instrument, not the Sonar Pulse's sonar ping; well under the voice and the motif (synthesized: a UI-like sound).
This one: synthesized: a soft rising sweep (600 to 1800 Hz in 0.28 s) landing on a glassy two-tone ping (1800 and 2700 Hz), through the same reverb: a 'whee-ping', more of a scan locking on, 2.2 s.
Edit: 2.20 s, the loudest 100 ms band −24.0 dB, peak −16.4 dBFS, −25.0 LUFS.

## Concept round 58, step AUD-H (Level 28: the hatchery and caldera beds, the Abyssal Maw, the shaft, the breakout, the Ascendancy cruisers)

The generator is `tools/concept/audio/sfx_r58h.py` (Freesound HQ previews: the sfx are cut, slowed, glided, layered,
swelled, faded, filtered, looped and levelled, three of them through a reverb for the brief's long tails; the
hydrophone pass-by and the horse's snort are smoothed before cutting, their short pops and flutter out; nothing is
synthesized). Under water (the beds and the Maw) every layer is muffled by its own low-pass; above the ice (the
breakout and the cruisers) the layers stay open. Passed over (read in their descriptions): a "Lion" growl of unclear
origin, "fake" and synthesized whales (a creaking door, Ableton, a virtual analog), flak and cannon booms made of
firecrackers or in Audacity, a "Distant Cannon Fire (simulated)", bear growls emulated with a voice transformer, the
"Closing large metal door" (a Kontakt sound-design impact), "Big Swoosh" (heavy processing), an M777 recording with
abusive tags, the "Designed Ice" library, a lamb-meat tear recorded too quietly. Reused, no entry: the under-water
explosion family (round 52 a), the *Marlin*'s crack for *Pelagic-3* (round 57 a), `europa-ice` above the ice (round
51), the Depth Hunters' sounds (round 57).

### europa-hatchery-r58-a — "26.07.21- Real Human Heartbeat - Contact Mic Recording 4" by Valerie-Vivegnis + "Deep Sea Ambience" by jhumbucker + "Throat Singing in a Cave" by ItsThatRoboGuy + "wood creaks deep long door or ship hull1.flac" by kyles

Proposed (round 58), cut from the HQ previews by `tools/concept/audio/sfx_r58h.py`.
Sources: <https://freesound.org/people/Valerie-Vivegnis/sounds/863689/> (26.07.21- Real Human Heartbeat - Contact Mic Recording 4 by Valerie-Vivegnis, CC-BY 4.0), <https://freesound.org/people/jhumbucker/sounds/193822/> (Deep Sea Ambience by jhumbucker, CC0 1.0), <https://freesound.org/people/ItsThatRoboGuy/sounds/816640/> (Throat Singing in a Cave by ItsThatRoboGuy, CC0 1.0), <https://freesound.org/people/kyles/sounds/452107/> (wood creaks deep long door or ship hull1.flac by kyles, CC0 1.0).
Use: Level 28 from the launch through the Brood Galleries and the Spawning Cavern (sections 1-3, t=0-80): the hatchery's ambience bed under the music (alone in section 1), in place of `europa-vents`; the caldera rumble takes over in the antechamber.
Brief: a slow warm throb (the hatchery alive), a distant Choir hum now and then, the egg racks creaking; deep, organic and close, quieter than the vent field; no bubbles, nothing that pops, a seamless loop; a real recording, heavy, nothing bubbly or popping.
This one: a real heartbeat (a contact microphone on the chest) slowed to half speed and muffled at 300 Hz, a slow double throb, over a deep under-water drone slowed to 0.7; once a loop throat singing recorded in a cave, slowed to half and far off (muffled at 700 Hz), swelling in and out over 9 s (the Choir), and two deep wooden creaks of a ship's hull slowed to 0.5 (the egg racks): 24.0 s loop.
Edit: 24.00 s, band RMS −44.6 dB, peak −8.8 dBFS, −29.0 LUFS.

### europa-hatchery-r58-b — "Massive Structure Bend" by Kostas17 + "26.07.21- Real Human Heartbeat - Contact Mic Recording 4" by Valerie-Vivegnis + "choir vocalising 01.wav" by klankbeeld + "Mooring Rope.wav" by Rmutt

Proposed (round 58), cut from the HQ previews by `tools/concept/audio/sfx_r58h.py`.
Sources: <https://freesound.org/people/Kostas17/sounds/529620/> (Massive Structure Bend by Kostas17, CC-BY 3.0), <https://freesound.org/people/Valerie-Vivegnis/sounds/863689/> (26.07.21- Real Human Heartbeat - Contact Mic Recording 4 by Valerie-Vivegnis, CC-BY 4.0), <https://freesound.org/people/klankbeeld/sounds/169946/> (choir vocalising 01.wav by klankbeeld, CC-BY 4.0), <https://freesound.org/people/Rmutt/sounds/145721/> (Mooring Rope.wav by Rmutt, CC0 1.0).
Use: Level 28 from the launch through the Brood Galleries and the Spawning Cavern (sections 1-3, t=0-80): the hatchery's ambience bed under the music (alone in section 1), in place of `europa-vents`; the caldera rumble takes over in the antechamber.
Brief: a slow warm throb (the hatchery alive), a distant Choir hum now and then, the egg racks creaking; deep, organic and close, quieter than the vent field; no bubbles, nothing that pops, a seamless loop; a real recording, heavy, nothing bubbly or popping.
This one: a cello's low bowed rumble slowed to 0.6 (a massive structure bending) muffled at 300 Hz, the heartbeat slower under it (0.42): darker and more of a drone than a, the throb further down; once a loop a church choir vocalising, slowed to half and far off (the Choir), and a ferry's mooring rope creaking on its cleat, slowed (the racks): 24.0 s loop.
Edit: 24.00 s, band RMS −42.1 dB, peak −13.8 dBFS, −29.0 LUFS.

### caldera-rumble-r58-a — "Foundry Furnace.wav" by Walter_Odington + "High Force Waterfall, Bottom of Gorge, Massive Roar, Teesdale UK - Stereo" by Hear_it_use_it

Proposed (round 58), cut from the HQ previews by `tools/concept/audio/sfx_r58h.py`.
Sources: <https://freesound.org/people/Walter_Odington/sounds/31566/> (Foundry Furnace.wav by Walter_Odington, CC0 1.0), <https://freesound.org/people/Hear_it_use_it/sounds/849934/> (High Force Waterfall, Bottom of Gorge, Massive Roar, Teesdale UK - Stereo by Hear_it_use_it, CC-BY 4.0).
Use: Level 28 from the antechamber (section 4, t=80) through the arena: the vent's rumble swelling over the hatchery bed (the near-silence before the Choir at t=97.5), under the fight, cut at the kill.
Brief: the vent under the caldera: a deep, hot, steady roar of water, closer and heavier than the hatchery, swelling now and then; no bubbles, no hiss-pops, a seamless loop; a real recording, heavy, nothing bubbly or popping.
This one: a foundry furnace's roar slowed to half and muffled at 700 Hz over the bottom-of-the-gorge waterfall (the vent field's bed) slowed further to 0.35: a hot, close furnace roar, 16.0 s loop.
Edit: 16.00 s, band RMS −40.5 dB, peak −15.1 dBFS, −27.0 LUFS.

### caldera-rumble-r58-b — "NIAGARA WATERS ROARING.mp3" by KevinT1001 + "Earthquake  **audible only with appropriate speakers**" by MBARI_MARS

Proposed (round 58), cut from the HQ previews by `tools/concept/audio/sfx_r58h.py`.
Sources: <https://freesound.org/people/KevinT1001/sounds/379905/> (NIAGARA WATERS ROARING.mp3 by KevinT1001, CC0 1.0), <https://freesound.org/people/MBARI_MARS/sounds/369485/> (Earthquake  **audible only with appropriate speakers** by MBARI_MARS, CC0 1.0).
Use: Level 28 from the antechamber (section 4, t=80) through the arena: the vent's rumble swelling over the hatchery bed (the near-silence before the Choir at t=97.5), under the fight, cut at the kill.
Brief: the vent under the caldera: a deep, hot, steady roar of water, closer and heavier than the hatchery, swelling now and then; no bubbles, no hiss-pops, a seamless loop; a real recording, heavy, nothing bubbly or popping.
This one: Niagara's roar slowed to 0.3 and muffled at 380 Hz, and once a loop a deep-sea earthquake (a hydrophone at 900 m) slowed to 0.6 swelling under it: lower and more of a water rumble than a, with a heave, 16.0 s loop.
Edit: 16.00 s, band RMS −39.1 dB, peak −12.7 dBFS, −27.0 LUFS.

### shaft-climb-r58-a — "High Force Waterfall, Bottom of Gorge, Massive Roar, Teesdale UK - Stereo" by Hear_it_use_it + "Ice - Lake fractures" by Vrymaa

Proposed (round 58), cut from the HQ previews by `tools/concept/audio/sfx_r58h.py`.
Sources: <https://freesound.org/people/Hear_it_use_it/sounds/849934/> (High Force Waterfall, Bottom of Gorge, Massive Roar, Teesdale UK - Stereo by Hear_it_use_it, CC-BY 4.0), <https://freesound.org/people/Vrymaa/sounds/737896/> (Ice - Lake fractures by Vrymaa, CC0 1.0).
Use: Level 28 after the kill, once a run (t=105-115, K+5 to K+15): the climb up the Vrell drill shaft under the ambience alone (the music silent), Okafor's "up the shaft" at ≈ K+8.6; the breakout follows at its end.
Brief: a long rush rising with the climb: the water roaring past, its pitch and brightness climbing as the pressure eases and the light grows, the ice ceiling groaning and cracking ahead in its second half; no bubbles; a real recording, heavy, nothing bubbly or popping.
This one: the waterfall's roar gliding up from 0.3 to 0.75 and swelling, muffled at 1.4 kHz, and from 4.5 s a frozen lake's deep fracture booms (a real lake, 15 cm of ice) slowed to 0.6: a climbing rush with the ice booming ahead, 10.0 s.
Edit: 10.00 s, the loudest 100 ms band −24.1 dB, peak −10.8 dBFS, −24.8 LUFS.

### shaft-climb-r58-b — "NIAGARA WATERS ROARING.mp3" by KevinT1001 + "glacierCalving.mp3" by CCCanary

Proposed (round 58), cut from the HQ previews by `tools/concept/audio/sfx_r58h.py`.
Sources: <https://freesound.org/people/KevinT1001/sounds/379905/> (NIAGARA WATERS ROARING.mp3 by KevinT1001, CC0 1.0), <https://freesound.org/people/CCCanary/sounds/271008/> (glacierCalving.mp3 by CCCanary, CC0 1.0).
Use: Level 28 after the kill, once a run (t=105-115, K+5 to K+15): the climb up the Vrell drill shaft under the ambience alone (the music silent), Okafor's "up the shaft" at ≈ K+8.6; the breakout follows at its end.
Brief: a long rush rising with the climb: the water roaring past, its pitch and brightness climbing as the pressure eases and the light grows, the ice ceiling groaning and cracking ahead in its second half; no bubbles; a real recording, heavy, nothing bubbly or popping.
This one: Niagara's roar gliding up from 0.3 to 0.7 and swelling, and from 3.5 s a glacier's ice grinding against itself (Glacier Bay, Alaska) slowed to half: rounder than a, the ice groaning rather than booming, 10.0 s.
Edit: 10.00 s, the loudest 100 ms band −24.6 dB, peak −7.6 dBFS, −20.0 LUFS.

### maw-arrival-r58-a — "Blue Whale B call @ 5X" by MBARI_MARS + "Earthquake  **audible only with appropriate speakers**" by MBARI_MARS

Proposed (round 58), cut from the HQ previews by `tools/concept/audio/sfx_r58h.py`.
Sources: <https://freesound.org/people/MBARI_MARS/sounds/343682/> (Blue Whale B call @ 5X by MBARI_MARS, CC-BY 4.0), <https://freesound.org/people/MBARI_MARS/sounds/369485/> (Earthquake  **audible only with appropriate speakers** by MBARI_MARS, CC0 1.0).
Use: Level 28's arena (t=104 on, the clock halted while the Maw lives; ≈ 130 s at medium): once, the Maw's arrival after the boss warning, the coil rising round the edges over 3 s and the head lifting last, jaw open (≈ 6 s).
Brief: a vast, slow, subsonic groan, a whale or a ship's hull under stress pitched down, with a long reverb tail; huge and far below; no bubbles; a real recording, heavy, nothing bubbly or popping.
This one: a blue whale's B call (a hydrophone at 900 m, recorded at 5×) slowed and sinking (0.95 to 0.7) over a deep-sea earthquake's rumble slowed to half, through a 3 s reverb: a long, moaning, rising-and-falling groan over a heave of the sea floor.
Edit: 7.80 s, the loudest 100 ms band −16.9 dB, peak −5.2 dBFS, −16.8 LUFS.

### maw-arrival-r58-b — "blue whale D calls **audible only with appropriate speakers**" by MBARI_MARS + "Massive Structure Bend" by Kostas17 + "wood creaks deep long door or ship hull1.flac" by kyles

Proposed (round 58), cut from the HQ previews by `tools/concept/audio/sfx_r58h.py`.
Sources: <https://freesound.org/people/MBARI_MARS/sounds/681896/> (blue whale D calls **audible only with appropriate speakers** by MBARI_MARS, CC-BY 4.0), <https://freesound.org/people/Kostas17/sounds/529620/> (Massive Structure Bend by Kostas17, CC-BY 3.0), <https://freesound.org/people/kyles/sounds/452107/> (wood creaks deep long door or ship hull1.flac by kyles, CC0 1.0).
Use: Level 28's arena (t=104 on, the clock halted while the Maw lives; ≈ 130 s at medium): once, the Maw's arrival after the boss warning, the coil rising round the edges over 3 s and the head lifting last, jaw open (≈ 6 s).
Brief: a vast, slow, subsonic groan, a whale or a ship's hull under stress pitched down, with a long reverb tail; huge and far below; no bubbles; a real recording, heavy, nothing bubbly or popping.
This one: a blue whale's D calls (real speed sub-bass, sped to 3 to be heard) sweeping down, a bowed cello's low structure groan and a ship's hull creak slowed to 0.35, through the same reverb: two falling groans, more of a hull straining than a.
Edit: 7.80 s, the loudest 100 ms band −17.0 dB, peak −6.1 dBFS, −17.2 LUFS.

### maw-gill-r58-a — "26.07.21- Real Human Heartbeat - Contact Mic Recording 4" by Valerie-Vivegnis + "20170507_horse.snort.wav" by dobroide

Proposed (round 58), cut from the HQ previews by `tools/concept/audio/sfx_r58h.py`.
Sources: <https://freesound.org/people/Valerie-Vivegnis/sounds/863689/> (26.07.21- Real Human Heartbeat - Contact Mic Recording 4 by Valerie-Vivegnis, CC-BY 4.0), <https://freesound.org/people/dobroide/sounds/392307/> (20170507_horse.snort.wav by dobroide, CC-BY 4.0).
Use: Level 28's arena (t=104 on, the clock halted while the Maw lives; ≈ 130 s at medium), phases 1-2: each gill sac's 0.6 s swell before its ring of 10 (phase 1 a sac every 1.6 s, phase 2 every 2.4 s; a destroyed sac's turn is skipped), so often, under the fire.
Brief: a deep pressure thump with a soft exhale: like a heartbeat under water, a breath going out; never a bubble, nothing that pops; a real recording, heavy, nothing bubbly or popping.
This one: the real heartbeat's double thump slowed to 0.55, then a horse's snort slowed and muffled at 1 kHz: a 'dum-dum, hhhff', 0.9 s.
Edit: 0.94 s, the loudest 100 ms band −25.3 dB, peak −6.1 dBFS, −20.3 LUFS.

### maw-gill-r58-b — "Deep Thumps.wav" by eardeer + "Sea blowhole, breathe. Bufador de mar, respira." by SonoRec

Proposed (round 58), cut from the HQ previews by `tools/concept/audio/sfx_r58h.py`.
Sources: <https://freesound.org/people/eardeer/sounds/400997/> (Deep Thumps.wav by eardeer, CC0 1.0), <https://freesound.org/people/SonoRec/sounds/824087/> (Sea blowhole, breathe. Bufador de mar, respira. by SonoRec, CC0 1.0).
Use: Level 28's arena (t=104 on, the clock halted while the Maw lives; ≈ 130 s at medium), phases 1-2: each gill sac's 0.6 s swell before its ring of 10 (phase 1 a sac every 1.6 s, phase 2 every 2.4 s; a destroyed sac's turn is skipped), so often, under the fire.
Brief: a deep pressure thump with a soft exhale: like a heartbeat under water, a breath going out; never a bubble, nothing that pops; a real recording, heavy, nothing bubbly or popping.
This one: a deep thump, then a sea blowhole breathing out through the rock, slowed to 0.6 and muffled: one heavier 'thoom' and a longer, airier exhale than a, 1.0 s.
Edit: 1.00 s, the loudest 100 ms band −27.5 dB, peak −6.7 dBFS, −19.4 LUFS.

### maw-throat-r58-a — "Angry tiger.wav" by schots + "Steel mill low frequency drone" by sarana

Proposed (round 58), cut from the HQ previews by `tools/concept/audio/sfx_r58h.py`.
Sources: <https://freesound.org/people/schots/sounds/439280/> (Angry tiger.wav by schots, CC0 1.0), <https://freesound.org/people/sarana/sounds/567148/> (Steel mill low frequency drone by sarana, CC0 1.0).
Use: Level 28's arena (t=104 on, the clock halted while the Maw lives; ≈ 130 s at medium): the throat's glow before each attack from the jaw: the phase-1 fan (0.6 s, the jaw open every 5 s) and the phase-2 fan wall (0.8 s, every 4 s).
Brief: a rising low drone with a growl in it: the throat charging, building into the shot; animal, deep, short; a real recording, heavy, nothing bubbly or popping.
This one: a caged tiger's growl slowed and gliding up (0.45 to 0.7), swelling, over a steel mill's low drone rising with it: a growl climbing into the shot, 0.9 s.
Edit: 0.90 s, the loudest 100 ms band −22.0 dB, peak −8.9 dBFS, −22.6 LUFS.

### maw-throat-r58-b — "Throat Singing in a Cave" by ItsThatRoboGuy + "Bear Angry Growl" by celldroid

Proposed (round 58), cut from the HQ previews by `tools/concept/audio/sfx_r58h.py`.
Sources: <https://freesound.org/people/ItsThatRoboGuy/sounds/816640/> (Throat Singing in a Cave by ItsThatRoboGuy, CC0 1.0), <https://freesound.org/people/celldroid/sounds/763026/> (Bear Angry Growl by celldroid, CC0 1.0).
Use: Level 28's arena (t=104 on, the clock halted while the Maw lives; ≈ 130 s at medium): the throat's glow before each attack from the jaw: the phase-1 fan (0.6 s, the jaw open every 5 s) and the phase-2 fan wall (0.8 s, every 4 s).
Brief: a rising low drone with a growl in it: the throat charging, building into the shot; animal, deep, short; a real recording, heavy, nothing bubbly or popping.
This one: the cave's throat singing slowed and gliding up (0.4 to 0.62) with a black bear's angry growl (recorded in the wild) slowed in it: more of a sung drone than a, the growl under it, 0.9 s.
Edit: 0.90 s, the loudest 100 ms band −22.1 dB, peak −5.6 dBFS, −21.3 LUFS.

### maw-sweep-groan-r58-a — "glacierCalving.mp3" by CCCanary + "wood creaks deep long door or ship hull1.flac" by kyles

Proposed (round 58), cut from the HQ previews by `tools/concept/audio/sfx_r58h.py`.
Sources: <https://freesound.org/people/CCCanary/sounds/271008/> (glacierCalving.mp3 by CCCanary, CC0 1.0), <https://freesound.org/people/kyles/sounds/452107/> (wood creaks deep long door or ship hull1.flac by kyles, CC0 1.0).
Use: Level 28's arena (t=104 on, the clock halted while the Maw lives; ≈ 130 s at medium), phases 2-3: each coil sweep's tell (1.2 s; easy 1.5, hard 0.9), silt streaming off the bight's plates and its glow line brightening (a sweep every 7 s, phase 3 every 6 s); the swing follows (maw-sweep-swing).
Brief: a grinding groan of plates: the coil's armour straining before it swings, ice or a hull creaking, slowed and heavy; a real recording, heavy, nothing bubbly or popping.
This one: the glacier's ice grinding against itself, slowed and rising (0.45 to 0.6), with a ship's hull creak slowed to 0.4: an icy grinding groan, 1.2 s.
Edit: 1.20 s, the loudest 100 ms band −21.0 dB, peak −8.2 dBFS, −18.7 LUFS.

### maw-sweep-groan-r58-b — "Massive Structure Bend" by Kostas17 + "wood creaks deep long door or ship hull1.flac" by kyles

Proposed (round 58), cut from the HQ previews by `tools/concept/audio/sfx_r58h.py`.
Sources: <https://freesound.org/people/Kostas17/sounds/529620/> (Massive Structure Bend by Kostas17, CC-BY 3.0), <https://freesound.org/people/kyles/sounds/452107/> (wood creaks deep long door or ship hull1.flac by kyles, CC0 1.0).
Use: Level 28's arena (t=104 on, the clock halted while the Maw lives; ≈ 130 s at medium), phases 2-3: each coil sweep's tell (1.2 s; easy 1.5, hard 0.9), silt streaming off the bight's plates and its glow line brightening (a sweep every 7 s, phase 3 every 6 s); the swing follows (maw-sweep-swing).
Brief: a grinding groan of plates: the coil's armour straining before it swings, ice or a hull creaking, slowed and heavy; a real recording, heavy, nothing bubbly or popping.
This one: the cello's low structure groan slowed to 0.55 with another of the hull's deep creaks slowed to 0.35: deeper and woodier than a, no ice in it, 1.2 s.
Edit: 1.20 s, the loudest 100 ms band −24.7 dB, peak −5.3 dBFS, −17.0 LUFS.

### maw-sweep-swing-r58-a — "WATRMvmt underwater passby slosh medium light whoosh" by jerry.berumen + "DepthBomb_01.aif" by ZeSoundResearchInc.

Proposed (round 58), cut from the HQ previews by `tools/concept/audio/sfx_r58h.py`.
Sources: <https://freesound.org/people/jerry.berumen/sounds/772895/> (WATRMvmt underwater passby slosh medium light whoosh by jerry.berumen, CC-BY 4.0), <https://freesound.org/people/ZeSoundResearchInc./sounds/147876/> (DepthBomb_01.aif by ZeSoundResearchInc., CC0 1.0).
Use: Level 28's arena (t=104 on, the clock halted while the Maw lives; ≈ 130 s at medium), phases 2-3: each coil sweep's swing right after its groan (in 0.5 s, held 0.8 s; it hurts: `heavy` 10 and a push).
Brief: a heavy swish of displaced water: a huge body moving fast, a deep 'whoom' with weight; no splash, no bubbles; a real recording, heavy, nothing bubbly or popping.
This one: a hydrophone's under-water pass-by slowed to 0.6 and muffled, a depth bomb's low boom under it: a deep 'whoomm', 1.15 s.
Edit: 1.15 s, the loudest 100 ms band −19.0 dB, peak −4.8 dBFS, −18.3 LUFS.

### maw-sweep-swing-r58-b — "R24-18-Tent or Tarp Whoosh.wav" by craigsmith + "Deep Thumps.wav" by eardeer

Proposed (round 58), cut from the HQ previews by `tools/concept/audio/sfx_r58h.py`.
Sources: <https://freesound.org/people/craigsmith/sounds/481896/> (R24-18-Tent or Tarp Whoosh.wav by craigsmith, CC0 1.0), <https://freesound.org/people/eardeer/sounds/400997/> (Deep Thumps.wav by eardeer, CC0 1.0).
Use: Level 28's arena (t=104 on, the clock halted while the Maw lives; ≈ 130 s at medium), phases 2-3: each coil sweep's swing right after its groan (in 0.5 s, held 0.8 s; it hurts: `heavy` 10 and a push).
Brief: a heavy swish of displaced water: a huge body moving fast, a deep 'whoom' with weight; no splash, no bubbles; a real recording, heavy, nothing bubbly or popping.
This one: a tarp whooshed (the Abyss Ray's glide's source) slowed to half and muffled, a deep thump in it: drier and more of a sweep than a, 1.0 s.
Edit: 1.00 s, the loudest 100 ms band −19.3 dB, peak −3.1 dBFS, −15.4 LUFS.

### maw-shed-r58-a — "Birch bark tearing, patting, rubbing (denoised)" by DeqstersLab + "WATRMvmt underwater passby slosh medium light whoosh" by jerry.berumen

Proposed (round 58), cut from the HQ previews by `tools/concept/audio/sfx_r58h.py`.
Sources: <https://freesound.org/people/DeqstersLab/sounds/847210/> (Birch bark tearing, patting, rubbing (denoised) by DeqstersLab, CC-BY 4.0), <https://freesound.org/people/jerry.berumen/sounds/772895/> (WATRMvmt underwater passby slosh medium light whoosh by jerry.berumen, CC-BY 4.0).
Use: Level 28's arena (t=104 on, the clock halted while the Maw lives; ≈ 130 s at medium): each Eel Swarm shed, as the coil plate's 1.0 s lime flare ends and the eel slips out (phase 1 every 10 s, phase 2 every 12, phase 3 every 8).
Brief: a wet tearing slide, short: a plate peeling open and something long slipping out; no squelch, no pop; a real recording, heavy, nothing bubbly or popping.
This one: birch bark torn from a branch, slowed to half and muffled at 2.5 kHz, a hydrophone's slosh past the microphone under it: a fibrous 'krrrsh' sliding off, 0.7 s.
Edit: 0.70 s, the loudest 100 ms band −23.5 dB, peak −5.3 dBFS, −23.2 LUFS.

### maw-shed-r58-b — "Peeling corn on the cob" by AlexLane + "Tearing.wav" by juskiddink

Proposed (round 58), cut from the HQ previews by `tools/concept/audio/sfx_r58h.py`.
Sources: <https://freesound.org/people/AlexLane/sounds/484178/> (Peeling corn on the cob by AlexLane, CC0 1.0), <https://freesound.org/people/juskiddink/sounds/59966/> (Tearing.wav by juskiddink, CC-BY 4.0).
Use: Level 28's arena (t=104 on, the clock halted while the Maw lives; ≈ 130 s at medium): each Eel Swarm shed, as the coil plate's 1.0 s lime flare ends and the eel slips out (phase 1 every 10 s, phase 2 every 12, phase 3 every 8).
Brief: a wet tearing slide, short: a plate peeling open and something long slipping out; no squelch, no pop; a real recording, heavy, nothing bubbly or popping.
This one: a husk peeled off a corn cob (close, a Schoeps microphone) slowed to 0.6, with sole leather tearing on a cobbler's wheel slowed in it: wetter and more of a peel than a, 0.7 s.
Edit: 0.70 s, the loudest 100 ms band −23.1 dB, peak −5.1 dBFS, −22.6 LUFS.

### maw-dive-r58-a — "High Force Waterfall, Bottom of Gorge, Massive Roar, Teesdale UK - Stereo" by Hear_it_use_it + "blue whale D calls **audible only with appropriate speakers**" by MBARI_MARS + "DepthBomb_05.aif" by ZeSoundResearchInc.

Proposed (round 58), cut from the HQ previews by `tools/concept/audio/sfx_r58h.py`.
Sources: <https://freesound.org/people/Hear_it_use_it/sounds/849934/> (High Force Waterfall, Bottom of Gorge, Massive Roar, Teesdale UK - Stereo by Hear_it_use_it, CC-BY 4.0), <https://freesound.org/people/MBARI_MARS/sounds/681896/> (blue whale D calls **audible only with appropriate speakers** by MBARI_MARS, CC-BY 4.0), <https://freesound.org/people/ZeSoundResearchInc./sounds/147880/> (DepthBomb_05.aif by ZeSoundResearchInc., CC0 1.0).
Use: Level 28's arena (t=104 on, the clock halted while the Maw lives; ≈ 130 s at medium), phase 2: once, as the head falls below 50 %: the roar and the coil sliding off the edges into the dark (2 s) while the music drops away; the 8 s of darkness follow.
Brief: a long falling roar and a pressure swell: the whole coil sinking, the sound dropping with it into the deep; no bubbles; a real recording, heavy, nothing bubbly or popping.
This one: the waterfall's roar falling (0.6 to 0.25), a blue whale's D call sweeping down under it and a depth bomb's pressure boom, through a 2.5 s reverb: a roar sinking away, 5.0 s with its tail.
Edit: 5.00 s, the loudest 100 ms band −19.7 dB, peak −3.1 dBFS, −14.2 LUFS.

### maw-dive-r58-b — "glacierCalving.mp3" by CCCanary + "Earthquake  **audible only with appropriate speakers**" by MBARI_MARS

Proposed (round 58), cut from the HQ previews by `tools/concept/audio/sfx_r58h.py`.
Sources: <https://freesound.org/people/CCCanary/sounds/271008/> (glacierCalving.mp3 by CCCanary, CC0 1.0), <https://freesound.org/people/MBARI_MARS/sounds/369485/> (Earthquake  **audible only with appropriate speakers** by MBARI_MARS, CC0 1.0).
Use: Level 28's arena (t=104 on, the clock halted while the Maw lives; ≈ 130 s at medium), phase 2: once, as the head falls below 50 %: the roar and the coil sliding off the edges into the dark (2 s) while the music drops away; the 8 s of darkness follow.
Brief: a long falling roar and a pressure swell: the whole coil sinking, the sound dropping with it into the deep; no bubbles; a real recording, heavy, nothing bubbly or popping.
This one: the glacier's calving roar (the ice splitting apart) falling from 0.6 to 0.3 over the deep-sea earthquake falling with it, the same reverb: rougher and more of a collapse than a.
Edit: 5.00 s, the loudest 100 ms band −18.5 dB, peak −3.0 dBFS, −22.5 LUFS.

### maw-lunge-r58-a — "WATRMvmt underwater passby slosh medium light whoosh" by jerry.berumen + "Loud Chomping or Biting Sound with Chewing (Animal)" by OBXJohn + "DepthBomb_01.aif" by ZeSoundResearchInc.

Proposed (round 58), cut from the HQ previews by `tools/concept/audio/sfx_r58h.py`.
Sources: <https://freesound.org/people/jerry.berumen/sounds/772895/> (WATRMvmt underwater passby slosh medium light whoosh by jerry.berumen, CC-BY 4.0), <https://freesound.org/people/OBXJohn/sounds/535616/> (Loud Chomping or Biting Sound with Chewing (Animal) by OBXJohn, CC-BY 4.0), <https://freesound.org/people/ZeSoundResearchInc./sounds/147876/> (DepthBomb_01.aif by ZeSoundResearchInc., CC0 1.0).
Use: Level 28's arena (t=104 on, the clock halted while the Maw lives; ≈ 130 s at medium), phase 2: once, after the dive: the head bursting up through the play plane and racing up the column in 0.8 s, jaws open (after the 2 s shadow tell; it hurts: `heavy` 10).
Brief: a rushing surge and a jaw snap: water tearing past, then a heavy bite; big, no click; a real recording, heavy, nothing bubbly or popping.
This one: the hydrophone's pass-by surging up (0.5 to 1.0), then a dog's jaws snapping shut on a treat slowed to 0.45 on a depth bomb's boom: a 'whoooOSH-CHOMP', 2.0 s (the Glow Angler's lunge made huge).
Edit: 1.98 s, the loudest 100 ms band −17.0 dB, peak −2.8 dBFS, −20.6 LUFS.

### maw-lunge-r58-b — "NIAGARA WATERS ROARING.mp3" by KevinT1001 + "Deep Thumps.wav" by eardeer + "DepthBomb_04.aif" by ZeSoundResearchInc.

Proposed (round 58), cut from the HQ previews by `tools/concept/audio/sfx_r58h.py`.
Sources: <https://freesound.org/people/KevinT1001/sounds/379905/> (NIAGARA WATERS ROARING.mp3 by KevinT1001, CC0 1.0), <https://freesound.org/people/eardeer/sounds/400997/> (Deep Thumps.wav by eardeer, CC0 1.0), <https://freesound.org/people/ZeSoundResearchInc./sounds/147873/> (DepthBomb_04.aif by ZeSoundResearchInc., CC0 1.0).
Use: Level 28's arena (t=104 on, the clock halted while the Maw lives; ≈ 130 s at medium), phase 2: once, after the dive: the head bursting up through the play plane and racing up the column in 0.8 s, jaws open (after the 2 s shadow tell; it hurts: `heavy` 10).
Brief: a rushing surge and a jaw snap: water tearing past, then a heavy bite; big, no click; a real recording, heavy, nothing bubbly or popping.
This one: Niagara's roar surging up (0.35 to 0.9), then a huge gate's deep closing thump slowed to 0.7 on a depth bomb: a broader rush and a duller, heavier bite than a, 2.0 s.
Edit: 1.98 s, the loudest 100 ms band −17.3 dB, peak −2.1 dBFS, −16.4 LUFS.

### implant-spark-r58-a — "Welding_Electric Sparks.wav" by weaveofkev + "door metal heavy close both sides same time impact rattle1 on offmic +bg voices.wav" by kyles

Proposed (round 58), cut from the HQ previews by `tools/concept/audio/sfx_r58h.py`.
Sources: <https://freesound.org/people/weaveofkev/sounds/396240/> (Welding_Electric Sparks.wav by weaveofkev, CC0 1.0), <https://freesound.org/people/kyles/sounds/407301/> (door metal heavy close both sides same time impact rattle1 on offmic +bg voices.wav by kyles, CC0 1.0).
Use: Level 28's arena (t=104 on, the clock halted while the Maw lives; ≈ 130 s at medium), phase 3: each Helix implant's 1.0 s tell (neck, left, right in turn), sparks and an amber flicker under the cracked casing, then its plate blowing aside and the core open.
Brief: electrical crackle and an arcing buzz, a machine's sound, not a creature's (the clue), ending in a hard metal clunk as the plate lets go; no bubbles; a real recording, heavy, nothing bubbly or popping.
This one: an arc welder's sparks (a real welding machine), three bursts run together and muffled at 4 kHz, then a heavy steel door's slam slowed: crackling sparks and a 'CLANK', 1.4 s.
Edit: 1.38 s, the loudest 100 ms band −22.2 dB, peak −5.4 dBFS, −21.4 LUFS.

### implant-spark-r58-b — "S16-18 Tesla coil sparking and humming.wav" by craigsmith + "heavy_mechanical_impacts_different_pitches_from_4_drawer_metal_file_cabinet_03232024" by Artninja

Proposed (round 58), cut from the HQ previews by `tools/concept/audio/sfx_r58h.py`.
Sources: <https://freesound.org/people/craigsmith/sounds/675760/> (S16-18 Tesla coil sparking and humming.wav by craigsmith, CC0 1.0), <https://freesound.org/people/Artninja/sounds/751170/> (heavy_mechanical_impacts_different_pitches_from_4_drawer_metal_file_cabinet_03232024 by Artninja, CC-BY 4.0).
Use: Level 28's arena (t=104 on, the clock halted while the Maw lives; ≈ 130 s at medium), phase 3: each Helix implant's 1.0 s tell (neck, left, right in turn), sparks and an amber flicker under the cracked casing, then its plate blowing aside and the core open.
Brief: electrical crackle and an arcing buzz, a machine's sound, not a creature's (the clue), ending in a hard metal clunk as the plate lets go; no bubbles; a real recording, heavy, nothing bubbly or popping.
This one: a Tesla coil sparking and humming (a vintage film library recording) muffled at 4 kHz, then a heavy file cabinet's metal impact slowed: more hum and buzz than a's crackle, 1.4 s.
Edit: 1.38 s, the loudest 100 ms band −22.3 dB, peak −10.5 dBFS, −18.7 LUFS.

### implant-zap-r58-a — "high-voltage-discharge-2.wav" by follytowers

Proposed (round 58), cut from the HQ previews by `tools/concept/audio/sfx_r58h.py`.
Source: <https://freesound.org/people/follytowers/sounds/415962/> (high-voltage-discharge-2.wav by follytowers, CC0 1.0).
Use: Level 28's arena (t=104 on, the clock halted while the Maw lives; ≈ 130 s at medium), phase 3: each discharge of an open implant (every 1.5 s while one is open): the 0.4 s amber arc crawling over the housing, then its burst of 3 electric bolts.
Brief: a short arcing build and a hard electric zap: high voltage, dry and mechanical, not a laser and not a creature; a real recording, heavy, nothing bubbly or popping.
This one: a Tesla coil's rotary spark-gap discharge building for 0.45 s, then one of its loudest cracks: a 'zzzzt-KRAK', 0.8 s.
Edit: 0.80 s, the loudest 100 ms band −22.1 dB, peak −9.1 dBFS, −20.0 LUFS.

### implant-zap-r58-b — "JacobsLadderSingle1.flac" by Halleck + "Taser/High Voltage discharge" by The_Chemical_Workshop

Proposed (round 58), cut from the HQ previews by `tools/concept/audio/sfx_r58h.py`.
Sources: <https://freesound.org/people/Halleck/sounds/19486/> (JacobsLadderSingle1.flac by Halleck, CC-BY 4.0), <https://freesound.org/people/The_Chemical_Workshop/sounds/403252/> (Taser/High Voltage discharge by The_Chemical_Workshop, CC0 1.0).
Use: Level 28's arena (t=104 on, the clock halted while the Maw lives; ≈ 130 s at medium), phase 3: each discharge of an open implant (every 1.5 s while one is open): the 0.4 s amber arc crawling over the housing, then its burst of 3 electric bolts.
Brief: a short arcing build and a hard electric zap: high voltage, dry and mechanical, not a laser and not a creature; a real recording, heavy, nothing bubbly or popping.
This one: a Jacob's ladder's arc climbing (slowed to 0.8), then a high-voltage taser's discharge: thinner and snappier than a, 0.8 s.
Edit: 0.80 s, the loudest 100 ms band −22.3 dB, peak −8.6 dBFS, −19.8 LUFS.

### maw-death-r58-a — "high-voltage-discharge-2.wav" by follytowers + "blue whale D calls **audible only with appropriate speakers**" by MBARI_MARS + "DepthBomb_01.aif" by ZeSoundResearchInc.

Proposed (round 58), cut from the HQ previews by `tools/concept/audio/sfx_r58h.py`.
Sources: <https://freesound.org/people/follytowers/sounds/415962/> (high-voltage-discharge-2.wav by follytowers, CC0 1.0), <https://freesound.org/people/MBARI_MARS/sounds/681896/> (blue whale D calls **audible only with appropriate speakers** by MBARI_MARS, CC-BY 4.0), <https://freesound.org/people/ZeSoundResearchInc./sounds/147876/> (DepthBomb_01.aif by ZeSoundResearchInc., CC0 1.0).
Use: Level 28's arena (t=104 on, the clock halted while the Maw lives; ≈ 130 s at medium): once, the kill: the three implants blowing out 0.4 s apart, the coil convulsing and sinking segment by segment, the head last into the vent, whose glow flares (the 4 s chain; the screen flash and the credit shower follow; the Choir's fragment at K+3).
Brief: three heavy electrical cracks, a long dying groan, a deep detonation-like pressure boom as the vent flares; the biggest sound of the act; no bubbles; a real recording, heavy, nothing bubbly or popping.
This one: three Tesla-coil cracks slowed to 0.7, a blue whale's D call sweeping down (sped to 2.5) and a depth bomb's boom at 3.4 s, through a 3 s reverb: crack-crack-crack, a falling moan, a deep boom, 8.2 s with its tail.
Edit: 8.20 s, the loudest 100 ms band −15.2 dB, peak −1.6 dBFS, −12.6 LUFS.

### maw-death-r58-b — "Taser/High Voltage discharge" by The_Chemical_Workshop + "Blue Whale B call @ 5X" by MBARI_MARS + "Quarry blasting.wav" by juskiddink

Proposed (round 58), cut from the HQ previews by `tools/concept/audio/sfx_r58h.py`.
Sources: <https://freesound.org/people/The_Chemical_Workshop/sounds/403252/> (Taser/High Voltage discharge by The_Chemical_Workshop, CC0 1.0), <https://freesound.org/people/MBARI_MARS/sounds/343682/> (Blue Whale B call @ 5X by MBARI_MARS, CC-BY 4.0), <https://freesound.org/people/juskiddink/sounds/82682/> (Quarry blasting.wav by juskiddink, CC-BY 4.0).
Use: Level 28's arena (t=104 on, the clock halted while the Maw lives; ≈ 130 s at medium): once, the kill: the three implants blowing out 0.4 s apart, the coil convulsing and sinking segment by segment, the head last into the vent, whose glow flares (the 4 s chain; the screen flash and the credit shower follow; the Choir's fragment at K+3).
Brief: three heavy electrical cracks, a long dying groan, a deep detonation-like pressure boom as the vent flares; the biggest sound of the act; no bubbles; a real recording, heavy, nothing bubbly or popping.
This one: three taser discharges slowed to 0.7, a blue whale's B call sinking (1.0 to 0.6) and a granite quarry blast slowed to 0.8 and muffled, the same reverb: a longer, more voiced groan than a and a rolling blast instead of a boom, 8.2 s with its tail.
Edit: 8.20 s, the loudest 100 ms band −15.0 dB, peak −7.0 dBFS, −16.6 LUFS.

### breakout-r58-a — "glacierCalving.mp3" by CCCanary + "Breaking_large_ice.wav" by CGEffex + "Crashing Waves into Rocks 1" by amholma

Proposed (round 58), cut from the HQ previews by `tools/concept/audio/sfx_r58h.py`.
Sources: <https://freesound.org/people/CCCanary/sounds/271008/> (glacierCalving.mp3 by CCCanary, CC0 1.0), <https://freesound.org/people/CGEffex/sounds/113679/> (Breaking_large_ice.wav by CGEffex, CC-BY 4.0), <https://freesound.org/people/amholma/sounds/376799/> (Crashing Waves into Rocks 1 by amholma, CC0 1.0).
Use: Level 28 at t=115 (K+15), once a run: the ice ceiling shattering in a white burst, slabs tumbling out of the field, the medium switching to air; Europa's night-ice wind (`europa-ice`) from here.
Brief: ice cracking and tearing (calving), a huge crash, a roar of water and air bursting into the open: the biggest open sound after an act of muffled water; a real recording, heavy, nothing bubbly or popping.
This one: a glacier splitting apart with a roar (Glacier Bay, Alaska) slowed to 0.75, a large ice sheet's crack stomped open slowed to 0.6 on top, and big waves crashing into rocks behind it: a calving roar with a wash of water, 4.5 s.
Edit: 4.50 s, the loudest 100 ms band −16.1 dB, peak −5.3 dBFS, −22.5 LUFS.

### breakout-r58-b — "Ice - Lake fractures" by Vrymaa + "BreakingIce.wav" by iainmccurdy + "High Force Waterfall, Bottom of Gorge, Massive Roar, Teesdale UK - Stereo" by Hear_it_use_it

Proposed (round 58), cut from the HQ previews by `tools/concept/audio/sfx_r58h.py`.
Sources: <https://freesound.org/people/Vrymaa/sounds/737896/> (Ice - Lake fractures by Vrymaa, CC0 1.0), <https://freesound.org/people/iainmccurdy/sounds/440858/> (BreakingIce.wav by iainmccurdy, CC-BY 4.0), <https://freesound.org/people/Hear_it_use_it/sounds/849934/> (High Force Waterfall, Bottom of Gorge, Massive Roar, Teesdale UK - Stereo by Hear_it_use_it, CC-BY 4.0).
Use: Level 28 at t=115 (K+15), once a run: the ice ceiling shattering in a white burst, slabs tumbling out of the field, the medium switching to air; Europa's night-ice wind (`europa-ice`) from here.
Brief: ice cracking and tearing (calving), a huge crash, a roar of water and air bursting into the open: the biggest open sound after an act of muffled water; a real recording, heavy, nothing bubbly or popping.
This one: the frozen lake's biggest fracture boom slowed to 0.6, a sheet of ice breaking underfoot slowed with it, and the waterfall's roar unmuffled behind: a sharper boom-and-shatter than a, the water roaring in the open, 4.4 s.
Edit: 4.40 s, the loudest 100 ms band −17.0 dB, peak −2.1 dBFS, −12.7 LUFS.

### cruiser-rise-r58-a — "ferry engine room1.wav" by mrmayo + "Oskar Eisbrecher.mp3" by Master Block

Proposed (round 58), cut from the HQ previews by `tools/concept/audio/sfx_r58h.py`.
Sources: <https://freesound.org/people/mrmayo/sounds/74914/> (ferry engine room1.wav by mrmayo, CC0 1.0), <https://freesound.org/people/Master Block/sounds/94167/> (Oskar Eisbrecher.mp3 by Master Block, CC0 1.0).
Use: Level 28 at t=118 (K+18), once a run: the two Ascendancy cruisers rising through the ice on both flanks (4 s), ice sheeting off their hulls, under the reveal sting (track 30).
Brief: a deep engine thrum swelling and ice grinding and sheeting off a hull: two huge machines coming up; in the air now, heavy but not muffled; a real recording, heavy, nothing bubbly or popping.
This one: a ferry's engine room slowed (0.35 to 0.5) and swelling, under a boat breaking through harbour ice slowed to 0.6: a thrumming engine and crunching, grinding ice, 5.0 s.
Edit: 5.00 s, the loudest 100 ms band −19.1 dB, peak −3.1 dBFS, −17.5 LUFS.

### cruiser-rise-r58-b — "fishing boat loud throaty engine onboard various speeds wandering perspective metal plastic hull creaks India.flac" by kyles + "glacierCalving.mp3" by CCCanary + "Breaking_Ice_01.wav" by dheming

Proposed (round 58), cut from the HQ previews by `tools/concept/audio/sfx_r58h.py`.
Sources: <https://freesound.org/people/kyles/sounds/451398/> (fishing boat loud throaty engine onboard various speeds wandering perspective metal plastic hull creaks India.flac by kyles, CC0 1.0), <https://freesound.org/people/CCCanary/sounds/271008/> (glacierCalving.mp3 by CCCanary, CC0 1.0), <https://freesound.org/people/dheming/sounds/268023/> (Breaking_Ice_01.wav by dheming, CC-BY 4.0).
Use: Level 28 at t=118 (K+18), once a run: the two Ascendancy cruisers rising through the ice on both flanks (4 s), ice sheeting off their hulls, under the reveal sting (track 30).
Brief: a deep engine thrum swelling and ice grinding and sheeting off a hull: two huge machines coming up; in the air now, heavy but not muffled; a real recording, heavy, nothing bubbly or popping.
This one: a fishing boat's throaty engine slowed (0.3 to 0.45) and swelling, the glacier's ice grinding against itself and a sheet of ice cracking: a rougher engine and a slower grind than a, 5.0 s.
Edit: 5.00 s, the loudest 100 ms band −19.2 dB, peak −3.0 dBFS, −15.9 LUFS.

### cruiser-broadside-r58-a — "Howitzer Artillery Gun Shot Sound Effect 03" by qubodup

Proposed (round 58), cut from the HQ previews by `tools/concept/audio/sfx_r58h.py`.
Source: <https://freesound.org/people/qubodup/sounds/161341/> (Howitzer Artillery Gun Shot Sound Effect 03 by qubodup, CC0 1.0).
Use: Level 28's run (t=122-157, above the ice: the medium is air again), the two Ascendancy cruisers keeping pace on both flanks: each broadside, a cruiser's 7 shells crossing a 70 px band after the 1.2 s tell (one every 3.0 s, the flanks in turn; from 20 s two bands at once).
Brief: a big naval gun volley: a ripple of heavy guns firing down the hull, deep booms with a long rolling tail; a real recording, heavy, nothing bubbly or popping.
This one: a howitzer's shot (US Army footage) seven times down the hull, 0.07-0.08 s apart, each slowed a little differently (0.85 to 1.0): a rippling 'BA-BA-BA-BA-BOOM', 1.65 s.
Edit: 1.65 s, the loudest 100 ms band −18.0 dB, peak −3.5 dBFS, −15.0 LUFS.

### cruiser-broadside-r58-b — "Navy Battleship Soundscape: Turret Gunshots, Mechanical Engine Humm, Radio Chatter, Officer Command Voices.flac" by qubodup + "Howitzer Gun Shots" by qubodup

Proposed (round 58), cut from the HQ previews by `tools/concept/audio/sfx_r58h.py`.
Sources: <https://freesound.org/people/qubodup/sounds/162365/> (Navy Battleship Soundscape: Turret Gunshots, Mechanical Engine Humm, Radio Chatter, Officer Command Voices.flac by qubodup, CC0 1.0), <https://freesound.org/people/qubodup/sounds/67516/> (Howitzer Gun Shots by qubodup, CC0 1.0).
Use: Level 28's run (t=122-157, above the ice: the medium is air again), the two Ascendancy cruisers keeping pace on both flanks: each broadside, a cruiser's 7 shells crossing a 70 px band after the 1.2 s tell (one every 3.0 s, the flanks in turn; from 20 s two bands at once).
Brief: a big naval gun volley: a ripple of heavy guns firing down the hull, deep booms with a long rolling tail; a real recording, heavy, nothing bubbly or popping.
This one: a battleship's turret firing (US Navy footage: one deep main-gun boom) with three howitzer shots rippling over it: one huge low blast and a crackle of guns, more weight and less ripple than a, 2.2 s.
Edit: 2.20 s, the loudest 100 ms band −18.1 dB, peak −5.6 dBFS, −18.6 LUFS.

### cruiser-flak-r58-a — "Fireworks 1.wav" by KandiKlover

Proposed (round 58), cut from the HQ previews by `tools/concept/audio/sfx_r58h.py`.
Source: <https://freesound.org/people/KandiKlover/sounds/640817/> (Fireworks 1.wav by KandiKlover, CC-BY 4.0).
Use: Level 28's run (t=122-157, above the ice: the medium is air again), the two Ascendancy cruisers keeping pace on both flanks: each flak burst, at a marker near the ship after its 1.0 s, a ring of 8 (two markers every 4 s; hard every 3 s, easy one marker).
Brief: an airburst: a sharp, hollow high explosion with a short rolling echo, lighter than the broadside; a real recording, heavy, nothing bubbly or popping.
This one: a firework shell's full burst recorded 1.2 km away (unprocessed): a deep, hollow 'BOOMF' with its echo, 1.15 s.
Edit: 1.15 s, the loudest 100 ms band −21.6 dB, peak −4.0 dBFS, −24.4 LUFS.

### cruiser-flak-r58-b — "Howitzer Gun Shots" by qubodup + "Fireworks 1.wav" by KandiKlover

Proposed (round 58), cut from the HQ previews by `tools/concept/audio/sfx_r58h.py`.
Sources: <https://freesound.org/people/qubodup/sounds/67516/> (Howitzer Gun Shots by qubodup, CC0 1.0), <https://freesound.org/people/KandiKlover/sounds/640817/> (Fireworks 1.wav by KandiKlover, CC-BY 4.0).
Use: Level 28's run (t=122-157, above the ice: the medium is air again), the two Ascendancy cruisers keeping pace on both flanks: each flak burst, at a marker near the ship after its 1.0 s, a ring of 8 (two markers every 4 s; hard every 3 s, easy one marker).
Brief: an airburst: a sharp, hollow high explosion with a short rolling echo, lighter than the broadside; a real recording, heavy, nothing bubbly or popping.
This one: a howitzer shot's crack sped to 1.1 and thinned, on the firework's other burst slowed to 0.75: a sharper crack and a lower boom than a, 0.9 s.
Edit: 0.88 s, the loudest 100 ms band −21.2 dB, peak −6.4 dBFS, −22.2 LUFS.

### cruiser-pd-r58-a — "Autocannon Three Shot Burst" by qubodup

Proposed (round 58), cut from the HQ previews by `tools/concept/audio/sfx_r58h.py`.
Source: <https://freesound.org/people/qubodup/sounds/854186/> (Autocannon Three Shot Burst by qubodup, CC0 1.0).
Use: Level 28's run (t=122-157, above the ice: the medium is air again), the two Ascendancy cruisers keeping pace on both flanks: each point-defence burst of 3 from a cruiser's turret line (each cruiser every 2.5 s; hard bursts of 4).
Brief: rapid autocannon: three quick heavy shots, lighter than the flak; a real recording, heavy, nothing bubbly or popping.
This one: a 25 mm Bushmaster autocannon's three-shot burst (US Marines footage) as recorded: a 'dak-dak-dak', 0.8 s.
Edit: 0.80 s, the loudest 100 ms band −23.0 dB, peak −7.7 dBFS, −23.0 LUFS.

### cruiser-pd-r58-b — "Navy MK 15 Phalanx CIWS Anti-Air Fast Burst" by qubodup

Proposed (round 58), cut from the HQ previews by `tools/concept/audio/sfx_r58h.py`.
Source: <https://freesound.org/people/qubodup/sounds/163119/> (Navy MK 15 Phalanx CIWS Anti-Air Fast Burst by qubodup, CC0 1.0).
Use: Level 28's run (t=122-157, above the ice: the medium is air again), the two Ascendancy cruisers keeping pace on both flanks: each point-defence burst of 3 from a cruiser's turret line (each cruiser every 2.5 s; hard bursts of 4).
Brief: rapid autocannon: three quick heavy shots, lighter than the flak; a real recording, heavy, nothing bubbly or popping.
This one: a Navy Phalanx CIWS's fast burst slowed to 0.9: a ripping buzz of rounds rather than three shots, 0.8 s.
Edit: 0.80 s, the loudest 100 ms band −23.4 dB, peak −13.1 dBFS, −18.8 LUFS.

## Concept round 60, step AUD-P (Act 5's shared sounds: the Ascendancy sets, the brown-out, the intercept, the mines, the Sovereign's ring, the belt beds)

The generator is `tools/concept/audio/sfx_r60.py` (Freesound HQ previews, found with an API-key search: the sfx are
cut, slowed, glided, layered, swelled, faded, filtered, looped and levelled; nothing in them is synthesized except the
mine's proximity blink, a signal the README allows to be synthesized). The rest of the units' sounds come in their
parts' rounds. Passed over (read in their descriptions or measured): a model-rocket "scream" (a whistle), mine-tunnel
ambiences full of drips, an office relay recording mostly hiss, shortwave recordings with faint voices, and the steel
mill drone (all its energy under 200 Hz: the user's note on Act 4's beds). The beds keep the drones' high-pass at
60–150 Hz so they are not the deep drones Act 4's were. Measured only, not listened to.

### enemy-shield-hit-r60-a — "knife-tapping-wine-glass" by BogumilaMerc

Proposed (round 60), cut from the HQ previews by `tools/concept/audio/sfx_r60.py`.
Source: <https://freesound.org/people/BogumilaMerc/sounds/835810/> (knife-tapping-wine-glass by BogumilaMerc, CC0 1.0).
Use: every hit on a hex shield of the Talon, the Hornet, the Gilded Gunship and the Iron Sovereign's core (the shared hex shield), throttled to once per 0.15 s per unit: often, under the fire.
Brief: a small glassy tick: a glass tapped with metal, dry and short; no ring-out, no pop; a real recording, heavy, nothing bubbly or popping.
This one: a knife tapping a wine glass: a bright dry 'tink', 0.18 s.
Edit: 0.18 s, the loudest 100 ms band −28.3 dB, peak −6.0 dBFS, −inf LUFS.

### enemy-shield-hit-r60-b — "large wine glass.wav" by Tairblenn + "anvil strike 8.wav" by l0calh05t

Proposed (round 60), cut from the HQ previews by `tools/concept/audio/sfx_r60.py`.
Sources: <https://freesound.org/people/Tairblenn/sounds/549895/> (large wine glass.wav by Tairblenn, CC0 1.0), <https://freesound.org/people/l0calh05t/sounds/19020/> (anvil strike 8.wav by l0calh05t, CC-BY 4.0).
Use: every hit on a hex shield of the Talon, the Hornet, the Gilded Gunship and the Iron Sovereign's core (the shared hex shield), throttled to once per 0.15 s per unit: often, under the fire.
Brief: a small glassy tick: a glass tapped with metal, dry and short; no ring-out, no pop; a real recording, heavy, nothing bubbly or popping.
This one: a large wine glass tapped, slowed to 0.85 (a little lower and fuller than a), with the tip of an anvil strike sped up under it for the metal: a 'tick' with more body, 0.16 s.
Edit: 0.18 s, the loudest 100 ms band −27.0 dB, peak −13.4 dBFS, −inf LUFS.

### enemy-shield-break-r60-a — "Windshield_Hit_02.wav" by thefsoundman

Proposed (round 60), cut from the HQ previews by `tools/concept/audio/sfx_r60.py`.
Source: <https://freesound.org/people/thefsoundman/sounds/118515/> (Windshield_Hit_02.wav by thefsoundman, CC0 1.0).
Use: a hex shield of the Talon, the Hornet, the Gilded Gunship and the Iron Sovereign's core (the shared hex shield) emptied (by damage, a `shield-breaker` hit or the EMP): now and then, a clear reward.
Brief: a brittle shatter: tempered glass bursting, about 0.3 s, small enough for a Talon; a real recording, heavy, nothing bubbly or popping.
This one: a hit on a car's windshield: a hard crack and the safety glass crazing, 0.38 s.
Edit: 0.38 s, the loudest 100 ms band −27.8 dB, peak −5.1 dBFS, −inf LUFS.

### enemy-shield-break-r60-b — "Glass Break" by unfa + "large wine glass.wav" by Tairblenn

Proposed (round 60), cut from the HQ previews by `tools/concept/audio/sfx_r60.py`.
Sources: <https://freesound.org/people/unfa/sounds/221528/> (Glass Break by unfa, CC0 1.0), <https://freesound.org/people/Tairblenn/sounds/549895/> (large wine glass.wav by Tairblenn, CC0 1.0).
Use: a hex shield of the Talon, the Hornet, the Gilded Gunship and the Iron Sovereign's core (the shared hex shield) emptied (by damage, a `shield-breaker` hit or the EMP): now and then, a clear reward.
Brief: a brittle shatter: tempered glass bursting, about 0.3 s, small enough for a Talon; a real recording, heavy, nothing bubbly or popping.
This one: a glass breaking, cut to the burst and its first shards, over the wine glass's tap sped up (the shield's own 'tick' in the burst): brighter and more splintery than a, 0.36 s.
Edit: 0.36 s, the loudest 100 ms band −22.6 dB, peak −5.0 dBFS, −inf LUFS.

### enemy-shield-refill-r60-a — "Rising electric hum.aif" by jukkako + "large wine glass.wav" by Tairblenn

Proposed (round 60), cut from the HQ previews by `tools/concept/audio/sfx_r60.py`.
Sources: <https://freesound.org/people/jukkako/sounds/616084/> (Rising electric hum.aif by jukkako, CC0 1.0), <https://freesound.org/people/Tairblenn/sounds/549895/> (large wine glass.wav by Tairblenn, CC0 1.0).
Use: a hex shield of the Talon, the Hornet, the Gilded Gunship and the Iron Sovereign's core (the shared hex shield) starting to refill after its delay: now and then.
Brief: a soft rising electrical shimmer, quiet: a feedback cue that the shield is back; a real recording, heavy, nothing bubbly or popping.
This one: a rising electric hum, its pitch gliding up an octave over the cue and swelling in, the wine glass's ring slowed to 0.7 faint under its end: 0.7 s.
Edit: 0.70 s, the loudest 100 ms band −31.9 dB, peak −17.7 dBFS, −28.2 LUFS.

### enemy-shield-refill-r60-b — "S16-18 Tesla coil sparking and humming.wav" by craigsmith

Proposed (round 60), cut from the HQ previews by `tools/concept/audio/sfx_r60.py`.
Source: <https://freesound.org/people/craigsmith/sounds/675760/> (S16-18 Tesla coil sparking and humming.wav by craigsmith, CC0 1.0).
Use: a hex shield of the Talon, the Hornet, the Gilded Gunship and the Iron Sovereign's core (the shared hex shield) starting to refill after its delay: now and then.
Brief: a soft rising electrical shimmer, quiet: a feedback cue that the shield is back; a real recording, heavy, nothing bubbly or popping.
This one: a Tesla coil humming and sparking, high-passed to its fizz and gliding up: a crackling shimmer, more electric than a, 0.7 s.
Edit: 0.70 s, the loudest 100 ms band −38.4 dB, peak −23.1 dBFS, −28.4 LUFS.

### homing-launch-r60-a — "Rocket Launch" by qubodup

Proposed (round 60), cut from the HQ previews by `tools/concept/audio/sfx_r60.py`.
Source: <https://freesound.org/people/qubodup/sounds/840511/> (Rocket Launch by qubodup, CC0 1.0).
Use: each homing-missile launch of the Hornet's pods, the SAM Nest, the Strider's pod and the Iron Sovereign's SAM batteries (the shared homing diamond; Levels 29-35); pitched per launcher in the game (the Hornet's pods higher, the SAM racks lower); a salvo plays it 2-4 times in a row.
Brief: a short hissing rocket launch (a model rocket or a firework's launch, cut short); the round compares it with reusing the chosen enemy missile launch (concept/enemy-missile-r08-a.ogg); a real recording, heavy, nothing bubbly or popping.
This one: a rocket's launch: an ignition and a hard rushing hiss, cut short, 0.6 s.
Edit: 0.60 s, the loudest 100 ms band −26.8 dB, peak −11.8 dBFS, −22.2 LUFS.

### homing-launch-r60-b — "bottleRocket4.wav" by nicStage + "hobby rocket blastoff" by reg7783

Proposed (round 60), cut from the HQ previews by `tools/concept/audio/sfx_r60.py`.
Sources: <https://freesound.org/people/nicStage/sounds/60904/> (bottleRocket4.wav by nicStage, CC-BY 4.0), <https://freesound.org/people/reg7783/sounds/730041/> (hobby rocket blastoff by reg7783, CC0 1.0).
Use: each homing-missile launch of the Hornet's pods, the SAM Nest, the Strider's pod and the Iron Sovereign's SAM batteries (the shared homing diamond; Levels 29-35); pitched per launcher in the game (the Hornet's pods higher, the SAM racks lower); a salvo plays it 2-4 times in a row.
Brief: a short hissing rocket launch (a model rocket or a firework's launch, cut short); the round compares it with reusing the chosen enemy missile launch (concept/enemy-missile-r08-a.ogg); a real recording, heavy, nothing bubbly or popping.
This one: a bottle rocket going off (a sharp ignition and a thin whooshing hiss) over a hobby rocket's burn: thinner and higher than a, 0.55 s.
Edit: 0.55 s, the loudest 100 ms band −27.0 dB, peak −7.1 dBFS, −22.2 LUFS.

### homing-run-r60-a — "hobby rocket blastoff" by reg7783

Proposed (round 60), cut from the HQ previews by `tools/concept/audio/sfx_r60.py`.
Source: <https://freesound.org/people/reg7783/sounds/730041/> (hobby rocket blastoff by reg7783, CC0 1.0).
Use: one quiet loop while any homing missile flies (Level 31's 'missile run'), for the nearest.
Brief: a thin rocket-motor hiss, quiet, steady, under the fire; no whistle; a real recording, heavy, nothing bubbly or popping.
This one: a hobby rocket's burn at its steadiest, thinned (high-passed at 400 Hz): a fizzing hiss, 1.0 s loop.
Edit: 1.00 s, band RMS −37.1 dB, peak −22.9 dBFS, −30.3 LUFS.

### homing-run-r60-b — "Rocket Launch" by qubodup

Proposed (round 60), cut from the HQ previews by `tools/concept/audio/sfx_r60.py`.
Source: <https://freesound.org/people/qubodup/sounds/840511/> (Rocket Launch by qubodup, CC0 1.0).
Use: one quiet loop while any homing missile flies (Level 31's 'missile run'), for the nearest.
Brief: a thin rocket-motor hiss, quiet, steady, under the fire; no whistle; a real recording, heavy, nothing bubbly or popping.
This one: the rocket launch's sustained rush sped to 1.1: rougher and more of a roar than a, 1.0 s loop.
Edit: 1.00 s, band RMS −36.4 dB, peak −21.2 dBFS, −30.3 LUFS.

### homing-down-r60-a — "Whip Crack 01.wav" by CGEffex + "anvil strike 4.wav" by l0calh05t

Proposed (round 60), cut from the HQ previews by `tools/concept/audio/sfx_r60.py`.
Sources: <https://freesound.org/people/CGEffex/sounds/93100/> (Whip Crack 01.wav by CGEffex, CC-BY 4.0), <https://freesound.org/people/l0calh05t/sounds/19016/> (anvil strike 4.wav by l0calh05t, CC-BY 4.0).
Use: a homing missile shot down (by the player's fire or a shield-breaker): often in a salvo.
Brief: a small dry crack with a metal ring; no pop, smaller than an enemy's death; a real recording, heavy, nothing bubbly or popping.
This one: a whip crack's dry snap with an anvil strike sped to 1.2 ringing under it: 'crack-ting', 0.45 s.
Edit: 0.45 s, the loudest 100 ms band −24.3 dB, peak −6.1 dBFS, −23.6 LUFS.

### homing-down-r60-b — "Distant Explosion of Propelled Grenade" by qubodup + "Iron Hits.wav" by Mrthenoronha

Proposed (round 60), cut from the HQ previews by `tools/concept/audio/sfx_r60.py`.
Sources: <https://freesound.org/people/qubodup/sounds/162363/> (Distant Explosion of Propelled Grenade by qubodup, CC0 1.0), <https://freesound.org/people/Mrthenoronha/sounds/371353/> (Iron Hits.wav by Mrthenoronha, CC0 1.0).
Use: a homing missile shot down (by the player's fire or a shield-breaker): often in a salvo.
Brief: a small dry crack with a metal ring; no pop, smaller than an enemy's death; a real recording, heavy, nothing bubbly or popping.
This one: a distant propelled grenade's burst (a dry thud-crack) with a struck iron bar's ring: darker and more of a blast than a, 0.42 s.
Edit: 0.42 s, the loudest 100 ms band −24.0 dB, peak −5.6 dBFS, −24.9 LUFS.

### rail-charge-r60-a — "Camera Flash Charging.aif" by ftpalad + "50Hz  electrical mains hum" by chungus43A

Proposed (round 60), cut from the HQ previews by `tools/concept/audio/sfx_r60.py`.
Sources: <https://freesound.org/people/ftpalad/sounds/119902/> (Camera Flash Charging.aif by ftpalad, CC0 1.0), <https://freesound.org/people/chungus43A/sounds/733736/> (50Hz  electrical mains hum by chungus43A, CC0 1.0).
Use: the rail line's 1.0 s telegraph: the Rail Bunker's rail, the Iron Sovereign's rail batteries and (its long version) Level 35's mass relay; the shot follows at its end.
Brief: a rising capacitor whine (a camera flash charging, pitched down) over a low hum, exactly as long as the telegraph (1.0 s), building to the shot; a real recording, heavy, nothing bubbly or popping.
This one: a camera flash's charging whine pitched down and gliding up an octave, swelling, over a 50 Hz mains hum: 'wheeeee' rising to the shot, 1.0 s.
Edit: 1.00 s, the loudest 100 ms band −28.0 dB, peak −17.4 dBFS, −24.2 LUFS.

### rail-charge-r60-b — "Rising electric hum.aif" by jukkako + "hum electric transformer in large room1.flac" by kyles

Proposed (round 60), cut from the HQ previews by `tools/concept/audio/sfx_r60.py`.
Sources: <https://freesound.org/people/jukkako/sounds/616084/> (Rising electric hum.aif by jukkako, CC0 1.0), <https://freesound.org/people/kyles/sounds/453444/> (hum electric transformer in large room1.flac by kyles, CC0 1.0).
Use: the rail line's 1.0 s telegraph: the Rail Bunker's rail, the Iron Sovereign's rail batteries and (its long version) Level 35's mass relay; the shot follows at its end.
Brief: a rising capacitor whine (a camera flash charging, pitched down) over a low hum, exactly as long as the telegraph (1.0 s), building to the shot; a real recording, heavy, nothing bubbly or popping.
This one: a rising electric hum gliding up, swelling hard at its end, over a transformer's hum slowed to 0.8: lower and more of a building drone than a, 1.0 s.
Edit: 1.00 s, the loudest 100 ms band −25.0 dB, peak −12.6 dBFS, −24.5 LUFS.

### rail-shot-r60-a — "Taser/High Voltage discharge" by The_Chemical_Workshop + "50 Cal Sniper Shot 1" by qubodup + "anvil strike 8.wav" by l0calh05t

Proposed (round 60), cut from the HQ previews by `tools/concept/audio/sfx_r60.py`.
Sources: <https://freesound.org/people/The_Chemical_Workshop/sounds/403252/> (Taser/High Voltage discharge by The_Chemical_Workshop, CC0 1.0), <https://freesound.org/people/qubodup/sounds/854699/> (50 Cal Sniper Shot 1 by qubodup, CC0 1.0), <https://freesound.org/people/l0calh05t/sounds/19020/> (anvil strike 8.wav by l0calh05t, CC-BY 4.0).
Use: each rail shot: the Rail Bunker's rail, the Iron Sovereign's rail batteries and (its long version) Level 35's mass relay (the mass relay plays it with a long thunder tail, made at the part).
Brief: a violent electric crack with a short metallic ring (a large spark gap or a whip crack layered on a heavy gunshot); a real recording, heavy, nothing bubbly or popping.
This one: a high-voltage discharge's crack on a .50 calibre sniper shot, an anvil strike slowed to 0.8 ringing under them: a hard electric 'KRACK' with a metal ring and the rifle's boom, 0.9 s.
Edit: 0.90 s, the loudest 100 ms band −22.0 dB, peak −4.1 dBFS, −17.8 LUFS.

### rail-shot-r60-b — "Whip Crack 01.wav" by CGEffex + "Howitzer Gun Shots" by qubodup + "metal glass makes long 'PING!' sound.aif" by Tomlija

Proposed (round 60), cut from the HQ previews by `tools/concept/audio/sfx_r60.py`.
Sources: <https://freesound.org/people/CGEffex/sounds/93100/> (Whip Crack 01.wav by CGEffex, CC-BY 4.0), <https://freesound.org/people/qubodup/sounds/67516/> (Howitzer Gun Shots by qubodup, CC0 1.0), <https://freesound.org/people/Tomlija/sounds/101246/> (metal glass makes long 'PING!' sound.aif by Tomlija, CC-BY 3.0).
Use: each rail shot: the Rail Bunker's rail, the Iron Sovereign's rail batteries and (its long version) Level 35's mass relay (the mass relay plays it with a long thunder tail, made at the part).
Brief: a violent electric crack with a short metallic ring (a large spark gap or a whip crack layered on a heavy gunshot); a real recording, heavy, nothing bubbly or popping.
This one: a whip crack on a howitzer's shot, a long metal 'PING' under them: heavier and more of a cannon than a, the ring brighter, 0.9 s.
Edit: 0.91 s, the loudest 100 ms band −20.1 dB, peak −6.7 dBFS, −18.2 LUFS.

### ascendancy-turbine-r60-a — "Idle F-22 Jet Plane.flac" by qubodup

Proposed (round 60), cut from the HQ previews by `tools/concept/audio/sfx_r60.py`.
Source: <https://freesound.org/people/qubodup/sounds/187734/> (Idle F-22 Jet Plane.flac by qubodup, CC0 1.0).
Use: the engine loops of the Gilded Gunship, the Hornet and the Minelayer, once per kind, for the nearest; pitched per hull in the game (the Gunship this pitch, the Hornet higher, the Minelayer lower).
Brief: a real jet or turbine idle: a deep turbine drone, quiet, under the enemy fire; a real recording, heavy, nothing bubbly or popping.
This one: an F-22's idle slowed to 0.6: a deep steady turbine drone with its high whine kept faint, 6.0 s loop.
Edit: 6.00 s, band RMS −35.2 dB, peak −21.9 dBFS, −30.4 LUFS.

### ascendancy-turbine-r60-b — "G08-27-Jet Idle or Taxi.wav" by craigsmith

Proposed (round 60), cut from the HQ previews by `tools/concept/audio/sfx_r60.py`.
Source: <https://freesound.org/people/craigsmith/sounds/437890/> (G08-27-Jet Idle or Taxi.wav by craigsmith, CC0 1.0).
Use: the engine loops of the Gilded Gunship, the Hornet and the Minelayer, once per kind, for the nearest; pitched per hull in the game (the Gunship this pitch, the Hornet higher, the Minelayer lower).
Brief: a real jet or turbine idle: a deep turbine drone, quiet, under the enemy fire; a real recording, heavy, nothing bubbly or popping.
This one: a jet idling or taxiing slowed to 0.65: rougher, with a throbbing low end and a thinner whine than a, 6.0 s loop.
Edit: 6.00 s, band RMS −35.7 dB, peak −21.7 dBFS, −30.2 LUFS.

### brownout-hum-r60-a — "hum electric transformer in large room1.flac" by kyles + "A relay switch being activated." by bassmosphere

Proposed (round 60), cut from the HQ previews by `tools/concept/audio/sfx_r60.py`.
Sources: <https://freesound.org/people/kyles/sounds/453444/> (hum electric transformer in large room1.flac by kyles, CC0 1.0), <https://freesound.org/people/bassmosphere/sounds/384698/> (A relay switch being activated. by bassmosphere, CC0 1.0).
Use: a loop while the generator's drained output is below the load (the Void Leech's drain, the Shard Drone's links): the player's ship browning out.
Brief: a low sagging hum (a transformer under load, its pitch drooping) with a faint relay chatter; uneasy, not an alarm; a real recording, heavy, nothing bubbly or popping.
This one: a transformer's hum in a large room slowed to 0.85, sagging 5 dB every 2 s, a relay clicking twice and once more now and then: 8.0 s loop.
Edit: 8.00 s, band RMS −34.6 dB, peak −16.8 dBFS, −28.0 LUFS.

### brownout-hum-r60-b — "power transformer in basement.wav" by itinerantmonk108 + "Contactor/motor overload relay noises" by tt_runscript

Proposed (round 60), cut from the HQ previews by `tools/concept/audio/sfx_r60.py`.
Sources: <https://freesound.org/people/itinerantmonk108/sounds/648726/> (power transformer in basement.wav by itinerantmonk108, CC0 1.0), <https://freesound.org/people/tt_runscript/sounds/807384/> (Contactor/motor overload relay noises by tt_runscript, CC0 1.0).
Use: a loop while the generator's drained output is below the load (the Void Leech's drain, the Shard Drone's links): the player's ship browning out.
Brief: a low sagging hum (a transformer under load, its pitch drooping) with a faint relay chatter; uneasy, not an alarm; a real recording, heavy, nothing bubbly or popping.
This one: a power transformer in a basement slowed to 0.8, sagging 6 dB every 4 s, a contactor and motor relays chattering once a loop: buzzier than a, 8.0 s loop.
Edit: 8.00 s, band RMS −34.0 dB, peak −17.7 dBFS, −28.7 LUFS.

### intercept-open-r60-a — "Radio tuning-static-interference" by quantumriver + "Short Wave Radio Noise 1" by zmobie

Proposed (round 60), cut from the HQ previews by `tools/concept/audio/sfx_r60.py`.
Sources: <https://freesound.org/people/quantumriver/sounds/552160/> (Radio tuning-static-interference by quantumriver, CC0 1.0), <https://freesound.org/people/zmobie/sounds/257880/> (Short Wave Radio Noise 1 by zmobie, CC0 1.0).
Use: an enemy-channel line (the HUD) or an intercepted briefing page opening, instead of the squelch: Vorne and Harker.
Brief: a short static sweep: a shortwave receiver tuned across a band onto a strong carrier, 0.3 s, no crackle; a real recording, heavy, nothing bubbly or popping.
This one: a radio tuned through static, played gliding down from 1.8 to 0.9, landing on a steady shortwave noise: 'shhwip-hhh', 0.35 s.
Edit: 0.35 s, the loudest 100 ms band −24.2 dB, peak −12.0 dBFS, −inf LUFS.

### intercept-open-r60-b — "radio02.wav" by theplax

Proposed (round 60), cut from the HQ previews by `tools/concept/audio/sfx_r60.py`.
Source: <https://freesound.org/people/theplax/sounds/615171/> (radio02.wav by theplax, CC-BY 4.0).
Use: an enemy-channel line (the HUD) or an intercepted briefing page opening, instead of the squelch: Vorne and Harker.
Brief: a short static sweep: a shortwave receiver tuned across a band onto a strong carrier, 0.3 s, no crackle; a real recording, heavy, nothing bubbly or popping.
This one: an AM radio tuned across the band, gliding up and swelling in: a rising sweep, 0.3 s.
Edit: 0.30 s, the loudest 100 ms band −24.3 dB, peak −13.8 dBFS, −inf LUFS.

### intercept-close-r60-a — "Radio tuning-static-interference" by quantumriver

Proposed (round 60), cut from the HQ previews by `tools/concept/audio/sfx_r60.py`.
Source: <https://freesound.org/people/quantumriver/sounds/552160/> (Radio tuning-static-interference by quantumriver, CC0 1.0).
Use: an enemy-channel line or intercepted page closing.
Brief: the open's sweep falling away, 0.3 s, no crackle; a real recording, heavy, nothing bubbly or popping.
This one: the radio's static gliding down from 1.0 to 0.5 and fading: a falling 'hhwoo', 0.3 s.
Edit: 0.32 s, the loudest 100 ms band −26.1 dB, peak −13.7 dBFS, −inf LUFS.

### intercept-close-r60-b — "radio02.wav" by theplax

Proposed (round 60), cut from the HQ previews by `tools/concept/audio/sfx_r60.py`.
Source: <https://freesound.org/people/theplax/sounds/615171/> (radio02.wav by theplax, CC-BY 4.0).
Use: an enemy-channel line or intercepted page closing.
Brief: the open's sweep falling away, 0.3 s, no crackle; a real recording, heavy, nothing bubbly or popping.
This one: the AM radio's sweep gliding down and fading, 0.3 s.
Edit: 0.30 s, the loudest 100 ms band −26.1 dB, peak −14.1 dBFS, −inf LUFS.

### intercept-carrier-r60-a — "Short Wave Radio Noise 2" by zmobie

Proposed (round 60), cut from the HQ previews by `tools/concept/audio/sfx_r60.py`.
Source: <https://freesound.org/people/zmobie/sounds/257879/> (Short Wave Radio Noise 2 by zmobie, CC0 1.0).
Use: an intercepted briefing page while its text types, in place of the typing blip (Vorne's broadcast pages, the act's intro).
Brief: a faint steady carrier hiss with a slow swell; distinct from the Choir's static lead (concept/choir-static-r55-b.ogg) so the two enemy voices never sound alike; a real recording, heavy, nothing bubbly or popping.
This one: a shortwave radio's noise band-passed to 400 Hz-4 kHz, swelling 3 dB every 4 s: a soft even 'hhhh', 8.0 s loop.
Edit: 8.00 s, band RMS −38.2 dB, peak −23.3 dBFS, −33.7 LUFS.

### intercept-carrier-r60-b — "pulsating vibrating Shortwave radio noise" by Mick_

Proposed (round 60), cut from the HQ previews by `tools/concept/audio/sfx_r60.py`.
Source: <https://freesound.org/people/Mick_/sounds/709192/> (pulsating vibrating Shortwave radio noise by Mick_, CC0 1.0).
Use: an intercepted briefing page while its text types, in place of the typing blip (Vorne's broadcast pages, the act's intro).
Brief: a faint steady carrier hiss with a slow swell; distinct from the Choir's static lead (concept/choir-static-r55-b.ogg) so the two enemy voices never sound alike; a real recording, heavy, nothing bubbly or popping.
This one: a pulsating shortwave noise band-passed the same: the interference's own slow throb in it, 8.0 s loop.
Edit: 8.00 s, band RMS −38.1 dB, peak −19.4 dBFS, −33.6 LUFS.

### mine-blink-r60-a — synthesized

Proposed (round 60), synthesized by `tools/concept/audio/sfx_r60.py` (no third-party source).
Use: an armed mine's 0.6 s proximity blink as the ship comes near (the Minelayer's mine lines, Level 30's minefield), before its burst.
Brief: a faster tick, rising, over the 0.6 s blink (synthesized allowed: a signal, not a machine); dry, no beep melody.
This one: 13 dry ticks, the gap shrinking from 110 to 25 ms and the pitch rising from 2.0 to 3.2 kHz: 'tik . tik . tik tik-tiktiktik', 0.6 s.
Edit: 0.63 s, the loudest 100 ms band −27.9 dB, peak −9.4 dBFS, −24.0 LUFS.

### mine-blink-r60-b — synthesized

Proposed (round 60), synthesized by `tools/concept/audio/sfx_r60.py` (no third-party source).
Use: an armed mine's 0.6 s proximity blink as the ship comes near (the Minelayer's mine lines, Level 30's minefield), before its burst.
Brief: a faster tick, rising, over the 0.6 s blink (synthesized allowed: a signal, not a machine); dry, no beep melody.
This one: 7 short square-ish beeps, the gap shrinking from 120 to 45 ms and the pitch rising an octave from 1.2 kHz, the last held: 'bip . bip . bip-bip-bipbeeep', 0.6 s.
Edit: 0.62 s, the loudest 100 ms band −25.7 dB, peak −20.6 dBFS, −24.0 LUFS.

### mine-burst-r60-a — "Bang/Explosion Metallic" by Werra

Proposed (round 60), cut from the HQ previews by `tools/concept/audio/sfx_r60.py`.
Source: <https://freesound.org/people/Werra/sounds/244394/> (Bang/Explosion Metallic by Werra, CC0 1.0).
Use: a mine bursting (proximity, shot or in a chain); the chain's ripple is cut from the pick at the part.
Brief: a sharp metallic blast, the `small` rung's weight but harder (a mine must be heard); a real recording, heavy, nothing bubbly or popping.
This one: a metallic bang: a hard crack with a steel body ringing into the boom, 1.0 s.
Edit: 1.00 s, the loudest 100 ms band −23.0 dB, peak −4.1 dBFS, −19.1 LUFS.

### mine-burst-r60-b — "Explosion in a can" by SamsterBirdies + "Metal Boom" by Johnnyfarmer

Proposed (round 60), cut from the HQ previews by `tools/concept/audio/sfx_r60.py`.
Sources: <https://freesound.org/people/SamsterBirdies/sounds/587887/> (Explosion in a can by SamsterBirdies, CC0 1.0), <https://freesound.org/people/Johnnyfarmer/sounds/209772/> (Metal Boom by Johnnyfarmer, CC0 1.0).
Use: a mine bursting (proximity, shot or in a chain); the chain's ripple is cut from the pick at the part.
Brief: a sharp metallic blast, the `small` rung's weight but harder (a mine must be heard); a real recording, heavy, nothing bubbly or popping.
This one: an explosion in a metal can over a deep metal boom: tighter and tinnier at the top than a, heavier underneath, 1.0 s.
Edit: 1.00 s, the loudest 100 ms band −19.1 dB, peak −5.4 dBFS, −20.2 LUFS.

### sovereign-ring-turn-r60-a — "Rail Grinding Machine with Rythmic Clang Berlin Night" by giuliorasi

Proposed (round 60), cut from the HQ previews by `tools/concept/audio/sfx_r60.py`.
Source: <https://freesound.org/people/giuliorasi/sounds/812360/> (Rail Grinding Machine with Rythmic Clang Berlin Night by giuliorasi, CC0 1.0).
Use: Level 35's boss fight: a low bed while the Iron Sovereign's rings turn (the spin-up, the ring step and the lock are separate one-shots, made at the boss's part).
Brief: a deep grind with slow gear-tooth ticks: a huge bearing turning, heavy and mechanical; a real recording, heavy, nothing bubbly or popping.
This one: a rail-grinding machine at night with its rhythmic clang, slowed to half: a deep grinding drone, the clangs slowed into heavy ticks, 8.0 s loop.
Edit: 8.00 s, band RMS −33.8 dB, peak −9.6 dBFS, −27.1 LUFS.

### sovereign-ring-turn-r60-b — "Inside a traditional Dutch windmill, 2" by pcwvdmark + "hum electric transformer in large room1.flac" by kyles

Proposed (round 60), cut from the HQ previews by `tools/concept/audio/sfx_r60.py`.
Sources: <https://freesound.org/people/pcwvdmark/sounds/345846/> (Inside a traditional Dutch windmill, 2 by pcwvdmark, CC0 1.0), <https://freesound.org/people/kyles/sounds/453444/> (hum electric transformer in large room1.flac by kyles, CC0 1.0).
Use: Level 35's boss fight: a low bed while the Iron Sovereign's rings turn (the spin-up, the ring step and the lock are separate one-shots, made at the boss's part).
Brief: a deep grind with slow gear-tooth ticks: a huge bearing turning, heavy and mechanical; a real recording, heavy, nothing bubbly or popping.
This one: inside a working windmill, its great gears and millstone turning, slowed to 0.55, over a transformer's hum slowed to 0.6: slower, creakier ticks than a over a heavier bed, 8.0 s loop.
Edit: 8.00 s, band RMS −34.2 dB, peak −13.3 dBFS, −27.0 LUFS.

### belt-station-r60-a — "room tone large industrial heavy ventilation fan like inflated building3 distant softer.flac" by kyles + "hum electric transformer in large room1.flac" by kyles + "Metal Boom" by Johnnyfarmer + "liner gangplank groaning.WAV" by phonoflora

Proposed (round 60), cut from the HQ previews by `tools/concept/audio/sfx_r60.py`.
Sources: <https://freesound.org/people/kyles/sounds/453541/> (room tone large industrial heavy ventilation fan like inflated building3 distant softer.flac by kyles, CC0 1.0), <https://freesound.org/people/kyles/sounds/453444/> (hum electric transformer in large room1.flac by kyles, CC0 1.0), <https://freesound.org/people/Johnnyfarmer/sounds/209772/> (Metal Boom by Johnnyfarmer, CC0 1.0), <https://freesound.org/people/phonoflora/sounds/500008/> (liner gangplank groaning.WAV by phonoflora, CC0 1.0).
Use: Level 29 (Ceres Hub's dock canyon) and Level 35 (the Helix yards in Ceres orbit): the act's ambience bed on the effects bus at the beds' 0.70, under the music; heard in the quiet stretches, a level's start and the pause before a boss; Level 29 adds the hub's alarm as a layer from the turn.
Brief: a big station heard through the hull: a deep ventilation roar, a low transformer hum, distant dock machinery (a crane's slew, a heavy hatch now and then), hull creaks; no voices, no PA; a seamless 16 s loop; a real recording, heavy, nothing bubbly or popping.
This one: a large industrial ventilation fan's room tone muffled at 1.5 kHz over a transformer's hum; once a loop a far heavy metal boom (a hatch) and a liner's gangplank groaning, slowed (the dock): 16.0 s loop.
Edit: 16.00 s, band RMS −42.1 dB, peak −25.1 dBFS, −35.8 LUFS.

### belt-station-r60-b — "ferry engine room2.wav" by mrmayo + "room tone large industrial ventilation fan like inflated building.flac" by kyles + "Rail Grinding Machine with Rythmic Clang Berlin Night" by giuliorasi + "Creaking Metal - Slow" by EagleStealthTeam

Proposed (round 60), cut from the HQ previews by `tools/concept/audio/sfx_r60.py`.
Sources: <https://freesound.org/people/mrmayo/sounds/74915/> (ferry engine room2.wav by mrmayo, CC0 1.0), <https://freesound.org/people/kyles/sounds/453546/> (room tone large industrial ventilation fan like inflated building.flac by kyles, CC0 1.0), <https://freesound.org/people/giuliorasi/sounds/812360/> (Rail Grinding Machine with Rythmic Clang Berlin Night by giuliorasi, CC0 1.0), <https://freesound.org/people/EagleStealthTeam/sounds/172411/> (Creaking Metal - Slow by EagleStealthTeam, CC0 1.0).
Use: Level 29 (Ceres Hub's dock canyon) and Level 35 (the Helix yards in Ceres orbit): the act's ambience bed on the effects bus at the beds' 0.70, under the music; heard in the quiet stretches, a level's start and the pause before a boss; Level 29 adds the hub's alarm as a layer from the turn.
Brief: a big station heard through the hull: a deep ventilation roar, a low transformer hum, distant dock machinery (a crane's slew, a heavy hatch now and then), hull creaks; no voices, no PA; a seamless 16 s loop; a real recording, heavy, nothing bubbly or popping.
This one: a ferry's engine room slowed to 0.7 and muffled (heard through a bulkhead) under a ventilation fan; once a loop a far rail-grinding machine's clanking (the dock machinery) and a slow metal creak: more of a working machine than a, 16.0 s loop.
Edit: 16.00 s, band RMS −42.0 dB, peak −25.3 dBFS, −37.5 LUFS.

### belt-open-r60-a — "G08-27-Jet Idle or Taxi.wav" by craigsmith + "pulsating vibrating Shortwave radio noise" by Mick_ + "Creaking Metal - Slow" by EagleStealthTeam + "distant explosions" by Kostrava

Proposed (round 60), cut from the HQ previews by `tools/concept/audio/sfx_r60.py`.
Sources: <https://freesound.org/people/craigsmith/sounds/437890/> (G08-27-Jet Idle or Taxi.wav by craigsmith, CC0 1.0), <https://freesound.org/people/Mick_/sounds/709192/> (pulsating vibrating Shortwave radio noise by Mick_, CC0 1.0), <https://freesound.org/people/EagleStealthTeam/sounds/172411/> (Creaking Metal - Slow by EagleStealthTeam, CC0 1.0), <https://freesound.org/people/Kostrava/sounds/320788/> (distant explosions by Kostrava, CC0 1.0).
Use: Levels 30, 31, 33 and 34 (the shipping lane, the rock fields, the leech field, the graveyard): the act's ambience bed on the effects bus at the beds' 0.70, under the music; heard in the quiet stretches, a level's start and the pause before a boss.
Brief: open space heard through the hull (no silent vacuum): the cockpit's low hum, hull creaks and ticks of metal in the cold, distant dull impacts (rocks on rock, far off), a faint radio hiss; no wind; a seamless 16 s loop; a real recording, heavy, nothing bubbly or popping.
This one: a jet's idle slowed to half and muffled at 600 Hz (the cockpit's hum), a pulsating shortwave hiss far under it; twice a loop a slow metal creak, once a distant explosion's dull thud muffled to a far impact: 16.0 s loop.
Edit: 16.00 s, band RMS −42.0 dB, peak −21.1 dBFS, −35.7 LUFS.

### belt-open-r60-b — "ferry engine room1.wav" by mrmayo + "pulsating vibrating Shortwave radio noise" by Mick_ + "liner gangplank groaning.WAV" by phonoflora + "Deep Thumps.wav" by eardeer

Proposed (round 60), cut from the HQ previews by `tools/concept/audio/sfx_r60.py`.
Sources: <https://freesound.org/people/mrmayo/sounds/74914/> (ferry engine room1.wav by mrmayo, CC0 1.0), <https://freesound.org/people/Mick_/sounds/709192/> (pulsating vibrating Shortwave radio noise by Mick_, CC0 1.0), <https://freesound.org/people/phonoflora/sounds/500008/> (liner gangplank groaning.WAV by phonoflora, CC0 1.0), <https://freesound.org/people/eardeer/sounds/400997/> (Deep Thumps.wav by eardeer, CC0 1.0).
Use: Levels 30, 31, 33 and 34 (the shipping lane, the rock fields, the leech field, the graveyard): the act's ambience bed on the effects bus at the beds' 0.70, under the music; heard in the quiet stretches, a level's start and the pause before a boss.
Brief: open space heard through the hull (no silent vacuum): the cockpit's low hum, hull creaks and ticks of metal in the cold, distant dull impacts (rocks on rock, far off), a faint radio hiss; no wind; a seamless 16 s loop; a real recording, heavy, nothing bubbly or popping.
This one: a ferry's engine room slowed to half and muffled at 600 Hz, a pulsating shortwave hiss far under it; once a loop a liner's gangplank groaning, slowed (the hull), and a deep far thump: darker and more of a hull than a, 16.0 s loop.
Edit: 16.00 s, band RMS −42.0 dB, peak −24.4 dBFS, −35.9 LUFS.

### belt-tunnel-r60-a — "room tone large industrial ventilation fan like inflated building.flac" by kyles + "Jackhammer, pneumatic drill in underground mine, very loud motor" by felix.blume + "Massive Structure Bend" by Kostas17 + "Deep Thumps.wav" by eardeer

Proposed (round 60), cut from the HQ previews by `tools/concept/audio/sfx_r60.py`.
Sources: <https://freesound.org/people/kyles/sounds/453546/> (room tone large industrial ventilation fan like inflated building.flac by kyles, CC0 1.0), <https://freesound.org/people/felix.blume/sounds/639019/> (Jackhammer, pneumatic drill in underground mine, very loud motor by felix.blume, CC0 1.0), <https://freesound.org/people/Kostas17/sounds/529620/> (Massive Structure Bend by Kostas17, CC-BY 3.0), <https://freesound.org/people/eardeer/sounds/400997/> (Deep Thumps.wav by eardeer, CC0 1.0).
Use: Level 32 (the hollowed asteroid; the refinery's furnace roar under its section 6 is Level 19's vent and furnace sounds): the act's ambience bed on the effects bus at the beds' 0.70, under the music; heard in the quiet stretches, a level's start and the pause before a boss.
Brief: an asteroid's interior: rock creaking and groaning, pumps and ventilation fans through the walls, a distant drill biting rock now and then, a machine thump; no drips; a seamless 16 s loop; a real recording, heavy, nothing bubbly or popping.
This one: a ventilation fan's room tone slowed to 0.8 and muffled; once a loop a jackhammer in an underground mine far off through the rock, a massive structure's slow bend (the rock groaning) and a deep machine thump: 16.0 s loop.
Edit: 16.00 s, band RMS −42.1 dB, peak −23.5 dBFS, −35.5 LUFS.

### belt-tunnel-r60-b — "room tone large industrial heavy ventilation fan like inflated building3 distant softer.flac" by kyles + "machine metal vibrating grinding drilling hole maybe.flac" by kyles + "Earthquake  **audible only with appropriate speakers**" by MBARI_MARS + "drilling a rock in a career" by charleswassner

Proposed (round 60), cut from the HQ previews by `tools/concept/audio/sfx_r60.py`.
Sources: <https://freesound.org/people/kyles/sounds/453541/> (room tone large industrial heavy ventilation fan like inflated building3 distant softer.flac by kyles, CC0 1.0), <https://freesound.org/people/kyles/sounds/453328/> (machine metal vibrating grinding drilling hole maybe.flac by kyles, CC0 1.0), <https://freesound.org/people/MBARI_MARS/sounds/369485/> (Earthquake  **audible only with appropriate speakers** by MBARI_MARS, CC0 1.0), <https://freesound.org/people/charleswassner/sounds/609930/> (drilling a rock in a career by charleswassner, CC-BY 3.0).
Use: Level 32 (the hollowed asteroid; the refinery's furnace roar under its section 6 is Level 19's vent and furnace sounds): the act's ambience bed on the effects bus at the beds' 0.70, under the music; heard in the quiet stretches, a level's start and the pause before a boss.
Brief: an asteroid's interior: rock creaking and groaning, pumps and ventilation fans through the walls, a distant drill biting rock now and then, a machine thump; no drips; a seamless 16 s loop; a real recording, heavy, nothing bubbly or popping.
This one: a large industrial ventilation fan's room tone slowed to 0.6 (the pumps and fans) over a machine grinding and drilling, slowed and muffled to a drone in the walls; once a loop a deep earthquake rumble (the rock heaving) and a quarry drill biting rock far off: lower and rockier than a, 16.0 s loop.
Edit: 16.00 s, band RMS −42.1 dB, peak −24.0 dBFS, −35.5 LUFS.

## Concept round 62, step AUD-B (Level 29: the turning station's klaxon and relays, the SAM Nest's hatch, lock and wake, Dock 9's clamp guns and clamps, the Ember's launch, the Bulwark's departure, the Talon, the Asteroid Mite)

Generator: `tools/concept/audio/sfx_r62.py` (cut from the Freesound HQ previews; the SAM Nest's lock beep synthesized). Not listened to by Claude: checked by level, peaks, onsets and pitch tracks. Passed over (read in their descriptions or measured): synthesized alarms and horns ("horn 01", "Security Alarm", a ChucK "two_tone_alarm", an FM klaxon), an AI-generated afterburner and a designed jet passby, a breaker switch "generated" by its author, a hydraulic-servo clip its uploader credits to a YouTube channel, and a rock scrape recorded too quietly (below −54 dBFS).

### station-klaxon-r62-a — "Gamewell Diaphone fire station horn" by HootOwl

Proposed (round 62), cut from the HQ previews by `tools/concept/audio/sfx_r62.py`.
Source: <https://freesound.org/people/HootOwl/sounds/464894/> (Gamewell Diaphone fire station horn by HootOwl, CC0 1.0).
Use: Level 29, once at t=60: Block C declares for the Ascendancy, its lights switching white to red over 1 s and the dormant SAM Nests waking; the music turns to the full mix with Vorne's motif (the gap check's QB-3 asks for the register: a or b).
Brief: a reverberant station klaxon, three blasts: the station turning on the player, industrial, heard over the music; no bubbly or popping sounds; a real recording, heavy, nothing bubbly or popping.
This one: a fire station's Gamewell diaphone air horn: three slow two-tone blasts ('bee-OHH', a low grunt rising into a big brassy tone), each 1.4 s with its echo off the buildings, 4.7 s.
Edit: 4.70 s, the loudest 100 ms band −22.0 dB, peak −12.3 dBFS, −24.1 LUFS.

### station-klaxon-r62-b — "Aurora Colorado Whelen WPS-3016 Test Part Two: WHOOP Loop. READ DESCRIPTION." by theblockofsound235

Proposed (round 62), cut from the HQ previews by `tools/concept/audio/sfx_r62.py`.
Source: <https://freesound.org/people/theblockofsound235/sounds/174869/> (Aurora Colorado Whelen WPS-3016 Test Part Two: WHOOP Loop. READ DESCRIPTION. by theblockofsound235, CC0 1.0).
Use: Level 29, once at t=60: Block C declares for the Ascendancy, its lights switching white to red over 1 s and the dormant SAM Nests waking; the music turns to the full mix with Vorne's motif (the gap check's QB-3 asks for the register: a or b).
Brief: a reverberant station klaxon, three blasts: the station turning on the player, industrial, heard over the music; no bubbly or popping sounds; a real recording, heavy, nothing bubbly or popping.
This one: a real Whelen electronic siren's 'whoop' sped to double: three fast rising whoops (650 Hz up to 1.4 kHz in 1.25 s each), the last with a dull echo, 4.0 s; harder and more urgent than a.
Edit: 3.97 s, the loudest 100 ms band −22.0 dB, peak −13.8 dBFS, −22.5 LUFS.

### station-relays-r62-a — "Switch.Big.Power.wav" by jessepash

Proposed (round 62), cut from the HQ previews by `tools/concept/audio/sfx_r62.py`.
Source: <https://freesound.org/people/jessepash/sounds/139970/> (Switch.Big.Power.wav by jessepash, CC0 1.0).
Use: Level 29, t=60, with the klaxon: the block's lights switching white to red over 1 s (and the red running up the canyon after it).
Brief: a bank of heavy relays slamming one after another over about 1 s: big power switches, mechanical and heavy, no electronic tone; a real recording, heavy, nothing bubbly or popping.
This one: a 50 A three-phase breaker thrown in a concrete metal shop, six times over 0.9 s at slightly different pitches: six heavy 'KLUNK's with the hall's ring, 1.6 s.
Edit: 1.65 s, the loudest 100 ms band −22.8 dB, peak −5.2 dBFS, −21.1 LUFS.

### station-relays-r62-b — "Starter_75hp.wav" by Engineer_815 + "Contactor/motor overload relay noises" by tt_runscript

Proposed (round 62), cut from the HQ previews by `tools/concept/audio/sfx_r62.py`.
Sources: <https://freesound.org/people/Engineer_815/sounds/524908/> (Starter_75hp.wav by Engineer_815, CC0 1.0), <https://freesound.org/people/tt_runscript/sounds/807384/> (Contactor/motor overload relay noises by tt_runscript, CC0 1.0).
Use: Level 29, t=60, with the klaxon: the block's lights switching white to red over 1 s (and the red running up the canyon after it).
Brief: a bank of heavy relays slamming one after another over about 1 s: big power switches, mechanical and heavy, no electronic tone; a real recording, heavy, nothing bubbly or popping.
This one: a 75 hp motor starter's contactor pulling in, then six motor contactors slammed in turn, slowed to 0.7 for weight: drier and quicker clacks than a, with an electric thud, 1.3 s.
Edit: 1.23 s, the loudest 100 ms band −22.2 dB, peak −9.5 dBFS, −21.3 LUFS.

### nest-hatch-r62-a — "Metal_Hatch_Small_Close_01" by Rudmer_Rotteveel + "door, open, space hatch, hydraulic.wav" by pointparkcinema

Proposed (round 62), cut from the HQ previews by `tools/concept/audio/sfx_r62.py`.
Sources: <https://freesound.org/people/Rudmer_Rotteveel/sounds/718440/> (Metal_Hatch_Small_Close_01 by Rudmer_Rotteveel, CC0 1.0), <https://freesound.org/people/pointparkcinema/sounds/402500/> (door, open, space hatch, hydraulic.wav by pointparkcinema, CC0 1.0).
Use: each SAM Nest salvo: the hatch opening at the start of its 0.4 s lock (the lock beep plays with it); up to three nests firing at once in Levels 29-35.
Brief: a short hydraulic clunk: a hatch thrown open by a ram, dry, about 0.5 s; a real recording, heavy, nothing bubbly or popping.
This one: an old heater's small metal hatch slammed, a hydraulic hatch's hiss under it: a hissing 'chunk', 0.5 s.
Edit: 0.50 s, the loudest 100 ms band −25.0 dB, peak −11.2 dBFS, −23.8 LUFS.

### nest-hatch-r62-b — "hydraulic shears breaks concrete 001.wav" by klankbeeld + "Metal_Hatch_Small_Close_01" by Rudmer_Rotteveel

Proposed (round 62), cut from the HQ previews by `tools/concept/audio/sfx_r62.py`.
Sources: <https://freesound.org/people/klankbeeld/sounds/541015/> (hydraulic shears breaks concrete 001.wav by klankbeeld, CC-BY 4.0), <https://freesound.org/people/Rudmer_Rotteveel/sounds/718440/> (Metal_Hatch_Small_Close_01 by Rudmer_Rotteveel, CC0 1.0).
Use: each SAM Nest salvo: the hatch opening at the start of its 0.4 s lock (the lock beep plays with it); up to three nests firing at once in Levels 29-35.
Brief: a short hydraulic clunk: a hatch thrown open by a ram, dry, about 0.5 s; a real recording, heavy, nothing bubbly or popping.
This one: an excavator's hydraulic shears biting (the ram's groan and crunch) with the hatch's clunk slowed under it: heavier and more of a machine than a, 0.47 s.
Edit: 0.45 s, the loudest 100 ms band −25.1 dB, peak −13.8 dBFS, −25.8 LUFS.

### nest-lock-r62-a — synthesized

Proposed (round 62), synthesized by `tools/concept/audio/sfx_r62.py` (no third-party source).
Use: each SAM Nest salvo: the 0.4 s lock after the hatch opens, before the two missiles launch; also at the end of a dormant nest's wake.
Brief: a two-tone electronic lock beep (synthesized allowed: a signal, not a machine); dry, short, no melody.
This one: a low beep and a held high one a fifth up (1.5 kHz, then 2.25 kHz): 'bip-beeep', 0.4 s.
Edit: 0.42 s, the loudest 100 ms band −30.0 dB, peak −27.2 dBFS, −26.1 LUFS.

### nest-lock-r62-b — synthesized

Proposed (round 62), synthesized by `tools/concept/audio/sfx_r62.py` (no third-party source).
Use: each SAM Nest salvo: the 0.4 s lock after the hatch opens, before the two missiles launch; also at the end of a dormant nest's wake.
Brief: a two-tone electronic lock beep (synthesized allowed: a signal, not a machine); dry, short, no melody.
This one: two quick high-low pairs (2.2 and 1.65 kHz, 60 ms each): 'bidi-bidi', 0.3 s; busier than a.
Edit: 0.44 s, the loudest 100 ms band −28.8 dB, peak −25.3 dBFS, −26.1 LUFS.

### nest-wake-r62-a — "motorized_door_servo_sound_11062024" by Artninja + a synthesized lock beep

Proposed (round 62), cut from the HQ previews by `tools/concept/audio/sfx_r62.py` (the lock beep synthesized).
Source: <https://freesound.org/people/Artninja/sounds/764849/> (motorized_door_servo_sound_11062024 by Artninja, CC-BY 4.0).
Use: Level 29, t=60: each dormant SAM Nest on Block C waking (its 1 s tell: the rack lifts and lights red); three nests at once, so it plays once for the group.
Brief: a servo spin-up as the rack lifts, ending on the lock beep; a real recording, heavy, nothing bubbly or popping.
This one: a motorised door's servo gliding up from 0.8 to 1.25 speed (a whirring rise), then the lock beep a: 'whirrr - bip-beeep', 1.06 s.
Edit: 1.06 s, the loudest 100 ms band −25.0 dB, peak −17.6 dBFS, −25.9 LUFS.

### nest-wake-r62-b — "Tank Turret Rotate" by KieranKeegan + a synthesized lock beep

Proposed (round 62), cut from the HQ previews by `tools/concept/audio/sfx_r62.py` (the lock beep synthesized).
Source: <https://freesound.org/people/KieranKeegan/sounds/418881/> (Tank Turret Rotate by KieranKeegan, CC0 1.0).
Use: Level 29, t=60: each dormant SAM Nest on Block C waking (its 1 s tell: the rack lifts and lights red); three nests at once, so it plays once for the group.
Brief: a servo spin-up as the rack lifts, ending on the lock beep; a real recording, heavy, nothing bubbly or popping.
This one: a geared motor (recorded from a printer) gliding up from 0.7 to 1.15 speed: a grittier, toothier whine than a, then the lock beep b: 'grrrr - bidi-bidi', 1.0 s.
Edit: 0.98 s, the loudest 100 ms band −25.0 dB, peak −15.7 dBFS, −25.2 LUFS.

### clamp-gun-shot-r62-a — "Autocannon Shot 2" by qubodup

Proposed (round 62), cut from the HQ previews by `tools/concept/audio/sfx_r62.py`.
Source: <https://freesound.org/people/qubodup/sounds/854184/> (Autocannon Shot 2 by qubodup, CC0 1.0).
Use: Level 29's Dock 9 halt: each clamp gun's aimed single shot (four guns, every 2.0 s; 2.6 s on easy), the enemy shot small's bullets.
Brief: a turret's single cannon shot, short and dry; the enemy shot small's weight (concept/enemy-shot-small-r08-a.ogg plays if neither is picked); a real recording, heavy, nothing bubbly or popping.
This one: a 25 mm autocannon's single shot (a LAV-25's Bushmaster): a hard dry 'BAK' with a short tail, 0.45 s.
Edit: 0.45 s, the loudest 100 ms band −27.2 dB, peak −13.0 dBFS, −26.6 LUFS.

### clamp-gun-shot-r62-b — "Guns & Explosions Album - Autocannon - Shot 1.wav" by OGsoundFX

Proposed (round 62), cut from the HQ previews by `tools/concept/audio/sfx_r62.py`.
Source: <https://freesound.org/people/OGsoundFX/sounds/423108/> (Guns & Explosions Album - Autocannon - Shot 1.wav by OGsoundFX, CC-BY 4.0).
Use: Level 29's Dock 9 halt: each clamp gun's aimed single shot (four guns, every 2.0 s; 2.6 s on easy), the enemy shot small's bullets.
Brief: a turret's single cannon shot, short and dry; the enemy shot small's weight (concept/enemy-shot-small-r08-a.ogg plays if neither is picked); a real recording, heavy, nothing bubbly or popping.
This one: a heavy autocannon's single shot: deeper and boomier than a, 0.55 s.
Edit: 0.55 s, the loudest 100 ms band −27.2 dB, peak −12.4 dBFS, −25.0 LUFS.

### clamp-release-r62-a — "heavy_metal_hit_w_loud_ringout_noises_01_08242024" by Artninja + "Train Air Brake 01.wav" by totalcult + "hydraulic shears breaks concrete 001.wav" by klankbeeld

Proposed (round 62), cut from the HQ previews by `tools/concept/audio/sfx_r62.py`.
Sources: <https://freesound.org/people/Artninja/sounds/752078/> (heavy_metal_hit_w_loud_ringout_noises_01_08242024 by Artninja, CC-BY 4.0), <https://freesound.org/people/totalcult/sounds/388568/> (Train Air Brake 01.wav by totalcult, CC0 1.0), <https://freesound.org/people/klankbeeld/sounds/541015/> (hydraulic shears breaks concrete 001.wav by klankbeeld, CC-BY 4.0).
Use: Level 29's Dock 9 halt: each clamp gun destroyed, its docking clamp springing open with a burst of gas and sparks (four times); the gun's own death plays its `medium` rung.
Brief: a heavy hydraulic release with a gas burst and a clang: a big clamp letting go; a real recording, heavy, nothing bubbly or popping.
This one: a heavy metal hit ringing out, a train's air brake venting over it and an excavator's hydraulic ram groaning under: 'KLANG-psshhh', 1.2 s.
Edit: 1.20 s, the loudest 100 ms band −25.0 dB, peak −8.4 dBFS, −20.2 LUFS.

### clamp-release-r62-b — "Closing large metal door-001" by AudioPapkin + "Air (or steam) pressure release" by brunoboselli + "door, open, space hatch, hydraulic.wav" by pointparkcinema

Proposed (round 62), cut from the HQ previews by `tools/concept/audio/sfx_r62.py`.
Sources: <https://freesound.org/people/AudioPapkin/sounds/814976/> (Closing large metal door-001 by AudioPapkin, CC0 1.0), <https://freesound.org/people/brunoboselli/sounds/457294/> (Air (or steam) pressure release by brunoboselli, CC0 1.0), <https://freesound.org/people/pointparkcinema/sounds/402500/> (door, open, space hatch, hydraulic.wav by pointparkcinema, CC0 1.0).
Use: Level 29's Dock 9 halt: each clamp gun destroyed, its docking clamp springing open with a burst of gas and sparks (four times); the gun's own death plays its `medium` rung.
Brief: a heavy hydraulic release with a gas burst and a clang: a big clamp letting go; a real recording, heavy, nothing bubbly or popping.
This one: a large metal door slammed (a clang with its latch rattling), an air valve blowing open and a hydraulic hatch's hiss: more of a rattle and a longer gas burst than a, 1.2 s.
Edit: 1.20 s, the loudest 100 ms band −25.5 dB, peak −6.8 dBFS, −20.2 LUFS.

### ember-launch-r62-a — "SDL Jet Engine Spool Up" by guitarduane + "Afterburner" by TiesWijnen

Proposed (round 62), cut from the HQ previews by `tools/concept/audio/sfx_r62.py`.
Sources: <https://freesound.org/people/guitarduane/sounds/788132/> (SDL Jet Engine Spool Up by guitarduane, CC-BY 4.0), <https://freesound.org/people/TiesWijnen/sounds/413312/> (Afterburner by TiesWijnen, CC0 1.0).
Use: Level 29, once: the last clamp gun falls, the prison tender's hold opens and Rook's Ember launches out of it (with Rook's return sting, track 31, over the music).
Brief: a jet engine spooling up into a launch whoosh, about 2.5 s: a fighter lighting up and going; a real recording, heavy, nothing bubbly or popping.
This one: a jet engine's spool-up at an airport, its whine rising (sped from 1.2 to 2.0 and swelling), into a turbojet's afterburner lighting: 'wheeeeee-WHOOSH', 2.55 s.
Edit: 2.55 s, the loudest 100 ms band −21.2 dB, peak −7.9 dBFS, −19.6 LUFS.

### ember-launch-r62-b — "G08-27-Jet Idle or Taxi.wav" by craigsmith + "Eurofighter Typhoon Flyby 003 – Close Proximity" by TimoSchmied

Proposed (round 62), cut from the HQ previews by `tools/concept/audio/sfx_r62.py`.
Sources: <https://freesound.org/people/craigsmith/sounds/437890/> (G08-27-Jet Idle or Taxi.wav by craigsmith, CC0 1.0), <https://freesound.org/people/TimoSchmied/sounds/640499/> (Eurofighter Typhoon Flyby 003 – Close Proximity by TimoSchmied, CC-BY 4.0).
Use: Level 29, once: the last clamp gun falls, the prison tender's hold opens and Rook's Ember launches out of it (with Rook's return sting, track 31, over the music).
Brief: a jet engine spooling up into a launch whoosh, about 2.5 s: a fighter lighting up and going; a real recording, heavy, nothing bubbly or popping.
This one: a jet idling and taxiing, sped from 0.8 to 1.8 into a rising roar, then a Eurofighter's close pass: rougher spool and a bigger, more airborne roar than a, 2.4 s.
Edit: 2.40 s, the loudest 100 ms band −21.0 dB, peak −10.0 dBFS, −21.2 LUFS.

### bulwark-depart-r62-a — "Eurofighter Typhoon Flyby 007 – Close Proximity" by TimoSchmied

Proposed (round 62), cut from the HQ previews by `tools/concept/audio/sfx_r62.py`.
Source: <https://freesound.org/people/TimoSchmied/sounds/640503/> (Eurofighter Typhoon Flyby 007 – Close Proximity by TimoSchmied, CC-BY 4.0).
Use: Level 29, once, right after the Ember's launch: the loaner Bulwark drone in the escort slot peels away to its side's edge ('Bulwark, RTB') as Rook takes the slot.
Brief: a thruster burn going away: a short roar fading off to the side, about 2 s; a real recording, heavy, nothing bubbly or popping.
This one: a Eurofighter's close flyby from just after its peak: the roar falling away into the distance, 2.0 s.
Edit: 2.00 s, the loudest 100 ms band −25.0 dB, peak −10.4 dBFS, −28.2 LUFS.

### bulwark-depart-r62-b — "Afterburner" by TiesWijnen + "Jet Flyby 02" by mrspivey

Proposed (round 62), cut from the HQ previews by `tools/concept/audio/sfx_r62.py`.
Sources: <https://freesound.org/people/TiesWijnen/sounds/413312/> (Afterburner by TiesWijnen, CC0 1.0), <https://freesound.org/people/mrspivey/sounds/805169/> (Jet Flyby 02 by mrspivey, CC0 1.0).
Use: Level 29, once, right after the Ember's launch: the loaner Bulwark drone in the escort slot peels away to its side's edge ('Bulwark, RTB') as Rook takes the slot.
Brief: a thruster burn going away: a short roar fading off to the side, about 2 s; a real recording, heavy, nothing bubbly or popping.
This one: an afterburner's light-up, then a jet flyby going away: a sharper kick at the start and a thinner, whinier tail than a, 2.05 s.
Edit: 2.05 s, the loudest 100 ms band −25.1 dB, peak −13.8 dBFS, −26.0 LUFS.

### talon-whine-r62-a — "Eurofighter Typhoon Flyby 003 – Close Proximity" by TimoSchmied

Proposed (round 62), cut from the HQ previews by `tools/concept/audio/sfx_r62.py`.
Source: <https://freesound.org/people/TimoSchmied/sounds/640499/> (Eurofighter Typhoon Flyby 003 – Close Proximity by TimoSchmied, CC-BY 4.0).
Use: each Talon wing's swoop turning in (a V of 5, or a pair): often in Levels 29-35, throttled to one per wing.
Brief: a short jet whine on the turn-in, quiet, under the fire; a real recording, heavy, nothing bubbly or popping.
This one: a Eurofighter's approach just before its pass, sped to 1.2 and high-passed to its whine: a thin rising 'wheee', 0.6 s.
Edit: 0.60 s, the loudest 100 ms band −28.2 dB, peak −14.7 dBFS, −27.1 LUFS.

### talon-whine-r62-b — "Jet Flyby 02" by mrspivey

Proposed (round 62), cut from the HQ previews by `tools/concept/audio/sfx_r62.py`.
Source: <https://freesound.org/people/mrspivey/sounds/805169/> (Jet Flyby 02 by mrspivey, CC0 1.0).
Use: each Talon wing's swoop turning in (a V of 5, or a pair): often in Levels 29-35, throttled to one per wing.
Brief: a short jet whine on the turn-in, quiet, under the fire; a real recording, heavy, nothing bubbly or popping.
This one: a jet flyby's approach gliding up from 1.0 to 1.35 speed: a whine bending up into the turn, more pitch movement than a, 0.6 s.
Edit: 0.60 s, the loudest 100 ms band −28.1 dB, peak −16.3 dBFS, −28.4 LUFS.

### talon-shot-r62-a — "Autocannon Shot 1" by qubodup

Proposed (round 62), cut from the HQ previews by `tools/concept/audio/sfx_r62.py`.
Source: <https://freesound.org/people/qubodup/sounds/854183/> (Autocannon Shot 1 by qubodup, CC0 1.0).
Use: each Talon pair's twin shot (two shots 12 px apart, heard as one): often, Levels 29-35.
Brief: a dry twin crack: a real cannon, two shots heard as one doubled crack; the enemy shot small's weight; a real recording, heavy, nothing bubbly or popping.
This one: a 25 mm autocannon's shot sped to 1.25, twice 45 ms apart: a tight doubled 'ka-RAK', 0.33 s.
Edit: 0.35 s, the loudest 100 ms band −28.2 dB, peak −14.1 dBFS, −inf LUFS.

### talon-shot-r62-b — "Mk 46 30mm gun - single shot" by qubodup

Proposed (round 62), cut from the HQ previews by `tools/concept/audio/sfx_r62.py`.
Source: <https://freesound.org/people/qubodup/sounds/162258/> (Mk 46 30mm gun - single shot by qubodup, CC0 1.0).
Use: each Talon pair's twin shot (two shots 12 px apart, heard as one): often, Levels 29-35.
Brief: a dry twin crack: a real cannon, two shots heard as one doubled crack; the enemy shot small's weight; a real recording, heavy, nothing bubbly or popping.
This one: a 30 mm gun's single shot sped to 1.4, twice 50 ms apart: a heavier, boomier double crack than a, 0.35 s.
Edit: 0.35 s, the loudest 100 ms band −28.1 dB, peak −13.5 dBFS, −inf LUFS.

### leader-squelch-r62-a — "Walkie-talkie end of transmission" by LukaCafuka

Proposed (round 62), cut from the HQ previews by `tools/concept/audio/sfx_r62.py`.
Source: <https://freesound.org/people/LukaCafuka/sounds/760245/> (Walkie-talkie end of transmission by LukaCafuka, CC0 1.0).
Use: a Talon squad leader's death (over its `small` rung): the wing scatters; a few times a level.
Brief: a short radio squelch: the leader's channel cut off, a dry static burst, no wet squelch (the README drafted the chosen radio close, concept/ui-radio-close-r08-a.ogg, which every radio line already ends with); a real recording, heavy, nothing bubbly or popping.
This one: a walkie-talkie's end of transmission: a sharp static 'kshh' falling off, 0.43 s.
Edit: 0.43 s, the loudest 100 ms band −24.0 dB, peak −8.7 dBFS, −24.9 LUFS.

### leader-squelch-r62-b — "End radio transmission" by ReadeOnly

Proposed (round 62), cut from the HQ previews by `tools/concept/audio/sfx_r62.py`.
Source: <https://freesound.org/people/ReadeOnly/sounds/47646/> (End radio transmission by ReadeOnly, CC0 1.0).
Use: a Talon squad leader's death (over its `small` rung): the wing scatters; a few times a level.
Brief: a short radio squelch: the leader's channel cut off, a dry static burst, no wet squelch (the README drafted the chosen radio close, concept/ui-radio-close-r08-a.ogg, which every radio line already ends with); a real recording, heavy, nothing bubbly or popping.
This one: a toy walkie-talkie's burst of static at the end of a transmission: brighter and buzzier than a, 0.37 s.
Edit: 0.36 s, the loudest 100 ms band −24.1 dB, peak −14.5 dBFS, −inf LUFS.

### mite-tell-r62-a — "Rock on Rock Scrape" by alegemaate

Proposed (round 62), cut from the HQ previews by `tools/concept/audio/sfx_r62.py`.
Source: <https://freesound.org/people/alegemaate/sounds/364709/> (Rock on Rock Scrape by alegemaate, CC0 1.0).
Use: an Asteroid Mite's 0.4 s leap tell (it crouches, its back glow flares teal); heard only close.
Brief: a short gritty rock scrape starting with the tell, 0.4 s, quiet; a real recording, heavy, nothing bubbly or popping.
This one: two rocks scraped together, slowed to 0.75: a low gritty 'krrsh', 0.4 s.
Edit: 0.39 s, the loudest 100 ms band −30.3 dB, peak −8.1 dBFS, −inf LUFS.

### mite-tell-r62-b — "Stone Scrape 2" by alegemaate

Proposed (round 62), cut from the HQ previews by `tools/concept/audio/sfx_r62.py`.
Source: <https://freesound.org/people/alegemaate/sounds/667283/> (Stone Scrape 2 by alegemaate, CC0 1.0).
Use: an Asteroid Mite's 0.4 s leap tell (it crouches, its back glow flares teal); heard only close.
Brief: a short gritty rock scrape starting with the tell, 0.4 s, quiet; a real recording, heavy, nothing bubbly or popping.
This one: a stone scraped over stone: a longer, finer and more hissing grind than a, 0.4 s.
Edit: 0.40 s, the loudest 100 ms band −33.3 dB, peak −8.0 dBFS, −inf LUFS.

### mite-leap-r62-a — "crab walking" by stuniverso + "quick woosh" by florianreichelt

Proposed (round 62), cut from the HQ previews by `tools/concept/audio/sfx_r62.py`.
Sources: <https://freesound.org/people/stuniverso/sounds/761559/> (crab walking by stuniverso, CC0 1.0), <https://freesound.org/people/florianreichelt/sounds/683101/> (quick woosh by florianreichelt, CC0 1.0).
Use: an Asteroid Mite leaping off its rock after the tell (the dive at the ship); heard only close.
Brief: a quick scuttle and a short whoosh: a small creature springing, dry, no squeak, no pop; a real recording, heavy, nothing bubbly or popping.
This one: a crab's claw click, then a quick woosh: 'tk-fwip', 0.36 s.
Edit: 0.36 s, the loudest 100 ms band −29.1 dB, peak −13.7 dBFS, −inf LUFS.

### mite-leap-r62-b — "bug legs tapping" by c64cosmin + "swish Bamboo pole w insect net 11.wav" by ra_gun

Proposed (round 62), cut from the HQ previews by `tools/concept/audio/sfx_r62.py`.
Sources: <https://freesound.org/people/c64cosmin/sounds/828623/> (bug legs tapping by c64cosmin, CC0 1.0), <https://freesound.org/people/ra_gun/sounds/75543/> (swish Bamboo pole w insect net 11.wav by ra_gun, CC0 1.0).
Use: an Asteroid Mite leaping off its rock after the tell (the dive at the ship); heard only close.
Brief: a quick scuttle and a short whoosh: a small creature springing, dry, no squeak, no pop; a real recording, heavy, nothing bubbly or popping.
This one: fingertips tapping like insect legs sped to 1.3 (a scuttle), then a bamboo pole's swish: a busier skitter and a thinner whoosh than a, 0.46 s.
Edit: 0.46 s, the loudest 100 ms band −34.5 dB, peak −13.8 dBFS, −27.1 LUFS.

## Concept round 63, step AUD-C (Level 30: the mine's arm, drop and chain, the Rail Serpent's shot, rumble and split, the Gilded Gunship's ring and bay doors, the Dart's launch, the beacon buoys)

Generator: `tools/concept/audio/sfx_r63.py` (cut from the Freesound HQ previews; the mine's arm tick and the buoy's tick synthesized). Round 60's picks are not proposed again: the mine's blink and burst (the chain's ripple is cut from the burst), the Ascendancy turbine (the Gunship's engine and the Minelayer's drone at their pitches). Not listened to by Claude: checked by level, peaks, onsets, pitch tracks and the loop seams. Passed over (read in their descriptions): an AI-generated "heavy object drop" (810170) and the synthesized sparks and power-downs of the searches (393820, 849772, 210333).

### mine-arm-r63-a — synthesized

Proposed (round 63), synthesized by `tools/concept/audio/sfx_r63.py` (no third-party source).
Use: each laid mine arming one second after it is dropped (its dim light turning to the red-gold blink): a line of 12 arms over about 3 s, up to three lines at once in Level 30.
Brief: a short dry electronic tick (synthesized allowed: a signal, not a machine), quiet; the family of the chosen proximity blink (concept/mine-blink-r60-a.ogg), which follows when the ship comes near.
This one: the chosen blink's tick, lower and doubled: two dry ticks at 1.6 kHz 25 ms apart: 'tk-tk', 0.05 s.
Edit: 0.10 s, the loudest 100 ms band −31.8 dB, peak −13.1 dBFS, −inf LUFS.

### mine-arm-r63-b — synthesized

Proposed (round 63), synthesized by `tools/concept/audio/sfx_r63.py` (no third-party source).
Use: each laid mine arming one second after it is dropped (its dim light turning to the red-gold blink): a line of 12 arms over about 3 s, up to three lines at once in Level 30.
Brief: a short dry electronic tick (synthesized allowed: a signal, not a machine), quiet; the family of the chosen proximity blink (concept/mine-blink-r60-a.ogg), which follows when the ship comes near.
This one: a short two-note chirp, 1.2 then 1.8 kHz, 30 ms each, square-edged: 'bi-dip', 0.07 s; more of a beep than a.
Edit: 0.10 s, the loudest 100 ms band −31.8 dB, peak −26.2 dBFS, −inf LUFS.

### mine-drop-r63-a — "Heavy Metal Thud on Ground" by 7of9Designs

Proposed (round 63), cut from the HQ previews by `tools/concept/audio/sfx_r63.py`.
Source: <https://freesound.org/people/7of9Designs/sounds/640204/> (Heavy Metal Thud on Ground by 7of9Designs, CC0 1.0).
Use: each mine the Minelayer drops as it strafes across the field (12 in about 3.2 s, a line every few seconds in Level 30).
Brief: a dull metal clunk, short and quiet: a heavy canister let go; a real recording, heavy, nothing bubbly or popping.
This one: a heavy metal weight dropped on dirt: a dull low 'thud', 0.4 s.
Edit: 0.40 s, the loudest 100 ms band −31.6 dB, peak −14.9 dBFS, −inf LUFS.

### mine-drop-r63-b — "Metal Thud" by BennettFilmTeacher

Proposed (round 63), cut from the HQ previews by `tools/concept/audio/sfx_r63.py`.
Source: <https://freesound.org/people/BennettFilmTeacher/sounds/585575/> (Metal Thud by BennettFilmTeacher, CC0 1.0).
Use: each mine the Minelayer drops as it strafes across the field (12 in about 3.2 s, a line every few seconds in Level 30).
Brief: a dull metal clunk, short and quiet: a heavy canister let go; a real recording, heavy, nothing bubbly or popping.
This one: a metal bottle dropped on carpet, slowed to 0.75: a dull 'tunk' with a short metal ring, brighter than a, 0.45 s.
Edit: 0.45 s, the loudest 100 ms band −30.0 dB, peak −14.8 dBFS, −30.4 LUFS.

### mine-chain-r63-a — "Bang/Explosion Metallic" by Werra

Proposed (round 63), cut from the HQ previews by `tools/concept/audio/sfx_r63.py`.
Source: <https://freesound.org/people/Werra/sounds/244394/> (Bang/Explosion Metallic by Werra, CC0 1.0).
Use: a shot mine setting off the mines within 40 px, hop by hop (0.08 s a hop): one file for the chain, played 0.08 s after the shot mine's own burst (concept/mine-burst-r60-a.ogg); one shot clears its cluster of 4.
Brief: a quick ripple of smaller blasts: the chosen mine burst's metallic bang, three times, each smaller (round 60: the ripple is cut from the pick); a crackle allowed, no popping; a real recording, heavy, nothing bubbly or popping.
This one: the chosen burst's bang three times, 0.08 s apart, each smaller and higher (sped 1.1, 1.2, 1.3), the last ringing out: 'ba-ba-BANG' rippling off, 0.8 s.
Edit: 0.78 s, the loudest 100 ms band −23.0 dB, peak −5.8 dBFS, −19.1 LUFS.

### mine-chain-r63-b — "Bang/Explosion Metallic" by Werra + "Debris Falling on Tank 1.wav" by Richard27

Proposed (round 63), cut from the HQ previews by `tools/concept/audio/sfx_r63.py`.
Sources: <https://freesound.org/people/Werra/sounds/244394/> (Bang/Explosion Metallic by Werra, CC0 1.0), <https://freesound.org/people/Richard27/sounds/530209/> (Debris Falling on Tank 1.wav by Richard27, CC0 1.0).
Use: a shot mine setting off the mines within 40 px, hop by hop (0.08 s a hop): one file for the chain, played 0.08 s after the shot mine's own burst (concept/mine-burst-r60-a.ogg); one shot clears its cluster of 4.
Brief: a quick ripple of smaller blasts: the chosen mine burst's metallic bang, three times, each smaller (round 60: the ripple is cut from the pick); a crackle allowed, no popping; a real recording, heavy, nothing bubbly or popping.
This one: the same ripple with a metal debris crackle after it (rocks and bricks rattling onto a steel plate, high-passed): a longer, crackling tail than a, 1.0 s.
Edit: 0.96 s, the loudest 100 ms band −21.1 dB, peak −6.2 dBFS, −19.7 LUFS.

### serpent-shot-r63-a — "50 cal shot.wav" by ajhartman42295

Proposed (round 63), cut from the HQ previews by `tools/concept/audio/sfx_r63.py`.
Source: <https://freesound.org/people/ajhartman42295/sounds/668071/> (50 cal shot.wav by ajhartman42295, CC0 1.0).
Use: each turret car's aimed single shot (every 2.4 s, the turret cars 0.6 s apart: a shot about every 0.8 s per train; hard: a burst of 2); Level 30 and later Act 5 levels.
Brief: a turret's single shot, short and dry, the enemy shot small's weight (the unit document has the turret cars fire the enemy shot small: concept/enemy-shot-small-r08-a.ogg plays if neither is picked); a real recording, heavy, nothing bubbly or popping.
This one: a .50 BMG's single shot at an outdoor range: a sharp 'CRACK' with a short boom, 0.35 s.
Edit: 0.35 s, the loudest 100 ms band −28.1 dB, peak −14.4 dBFS, −inf LUFS.

### serpent-shot-r63-b — "Flak_Shot.wav" by kantouth

Proposed (round 63), cut from the HQ previews by `tools/concept/audio/sfx_r63.py`.
Source: <https://freesound.org/people/kantouth/sounds/104399/> (Flak_Shot.wav by kantouth, CC-BY 4.0).
Use: each turret car's aimed single shot (every 2.4 s, the turret cars 0.6 s apart: a shot about every 0.8 s per train; hard: a burst of 2); Level 30 and later Act 5 levels.
Brief: a turret's single shot, short and dry, the enemy shot small's weight (the unit document has the turret cars fire the enemy shot small: concept/enemy-shot-small-r08-a.ogg plays if neither is picked); a real recording, heavy, nothing bubbly or popping.
This one: an anti-aircraft gun's shot slowed to 0.9: a duller, deeper 'thok' than a, 0.22 s.
Edit: 0.22 s, the loudest 100 ms band −28.1 dB, peak −14.8 dBFS, −inf LUFS.

### serpent-rumble-r63-a — "Fast Freight Train Clickety-Clack over Track Joint" by WeenyBeany

Proposed (round 63), cut from the HQ previews by `tools/concept/audio/sfx_r63.py`.
Source: <https://freesound.org/people/WeenyBeany/sounds/769890/> (Fast Freight Train Clickety-Clack over Track Joint by WeenyBeany, CC-BY 4.0).
Use: one loop while a Rail Serpent is on the screen, for the nearest (the Act 3 rule: once per kind); Level 30 and later Act 5 levels.
Brief: a heavy rail car's rumble with a rhythmic coupling clatter (a real train), under the fire, seamless; a real recording, heavy, nothing bubbly or popping.
This one: a cement freight train passing close at 90 km/h over a track joint, slowed to 0.85: a heavy rumble with a fast, irregular clickety-clack, 6.0 s loop.
Edit: 6.00 s, band RMS −34.1 dB, peak −11.9 dBFS, −29.1 LUFS.

### serpent-rumble-r63-b — "freight train railway bridge 01 140916_0415.wav" by klankbeeld

Proposed (round 63), cut from the HQ previews by `tools/concept/audio/sfx_r63.py`.
Source: <https://freesound.org/people/klankbeeld/sounds/249425/> (freight train railway bridge 01 140916_0415.wav by klankbeeld, CC-BY 4.0).
Use: one loop while a Rail Serpent is on the screen, for the nearest (the Act 3 rule: once per kind); Level 30 and later Act 5 levels.
Brief: a heavy rail car's rumble with a rhythmic coupling clatter (a real train), under the fire, seamless; a real recording, heavy, nothing bubbly or popping.
This one: an empty freight train crossing a steel bridge, slowed to 0.9: a steady roaring iron rumble, the clatter blurred into it; smoother and further off than a, 6.0 s loop.
Edit: 6.00 s, band RMS −34.1 dB, peak −21.4 dBFS, −31.2 LUFS.

### serpent-split-r63-a — "Carrige_Coupler.wav" by pkri + "Wind-down 1.aif" by timgormly + "Electric Sparks, Railway, A.wav" by InspectorJ

Proposed (round 63), cut from the HQ previews by `tools/concept/audio/sfx_r63.py`.
Sources: <https://freesound.org/people/pkri/sounds/434710/> (Carrige_Coupler.wav by pkri, CC0 1.0), <https://freesound.org/people/timgormly/sounds/151288/> (Wind-down 1.aif by timgormly, CC0 1.0), <https://freesound.org/people/InspectorJ/sounds/412313/> (Electric Sparks, Railway, A.wav by InspectorJ, CC-BY 4.0).
Use: a car (not the engine) destroyed: the train cut, the cars behind the cut losing power and drifting for 1.5 s, sparking and smoking, before they blow car by car (their `medium` rungs); a few times a train.
Brief: a coupler snap, then a falling power-down whine with electric sparks over the 1.5 s drift; a real recording, heavy, nothing bubbly or popping.
This one: a rail carriage's coupler clanking open, a machine's motor winding down (glided from 1.0 to 0.6) and a railway's power line sparking under them: 'KLANK - wheeoouu' over a crackle, 1.6 s.
Edit: 1.60 s, the loudest 100 ms band −24.1 dB, peak −9.2 dBFS, −28.8 LUFS.

### serpent-split-r63-b — "snapping-chain" by CosmicEmbers + "Carrige_Coupler.wav" by pkri + "engine3_down.wav" by ReadeOnly + "Welding_Electric Sparks.wav" by weaveofkev

Proposed (round 63), cut from the HQ previews by `tools/concept/audio/sfx_r63.py`.
Sources: <https://freesound.org/people/CosmicEmbers/sounds/161650/> (snapping-chain by CosmicEmbers, CC-BY 3.0), <https://freesound.org/people/pkri/sounds/434710/> (Carrige_Coupler.wav by pkri, CC0 1.0), <https://freesound.org/people/ReadeOnly/sounds/186961/> (engine3_down.wav by ReadeOnly, CC0 1.0), <https://freesound.org/people/weaveofkev/sounds/396240/> (Welding_Electric Sparks.wav by weaveofkev, CC0 1.0).
Use: a car (not the engine) destroyed: the train cut, the cars behind the cut losing power and drifting for 1.5 s, sparking and smoking, before they blow car by car (their `medium` rungs); a few times a train.
Brief: a coupler snap, then a falling power-down whine with electric sparks over the 1.5 s drift; a real recording, heavy, nothing bubbly or popping.
This one: a chain snapping over a coupler's clank (slowed to 0.85), a machine's whine stuttering and dropping (sped from 2.0 to 1.4) and an arc welder's sparks: a sharper snap and a harsher, buzzier crackle than a, 1.6 s.
Edit: 1.60 s, the loudest 100 ms band −24.1 dB, peak −7.7 dBFS, −26.7 LUFS.

### gunship-ring-r63-a — "M224 Mortar Firing Close 02" by Nanashi

Proposed (round 63), cut from the HQ previews by `tools/concept/audio/sfx_r63.py`.
Source: <https://freesound.org/people/Nanashi/sounds/426162/> (M224 Mortar Firing Close 02 by Nanashi, CC-BY 4.0).
Use: each Gilded Gunship's ring of 12 slow bullets (every 3.0 s, its gold ring lamps lighting 0.4 s before); 2-3 Gunships in a column, Level 30's convoy.
Brief: a hollow cannon thump, heavy and short: the ring fired at once; a real recording, heavy, nothing bubbly or popping.
This one: an M224 mortar fired 15 feet away: a hollow 'thoomp' with a short rolling tail, 1.0 s.
Edit: 1.00 s, the loudest 100 ms band −25.2 dB, peak −14.3 dBFS, −28.0 LUFS.

### gunship-ring-r63-b — "Mortar Shots.flac" by qubodup

Proposed (round 63), cut from the HQ previews by `tools/concept/audio/sfx_r63.py`.
Source: <https://freesound.org/people/qubodup/sounds/184382/> (Mortar Shots.flac by qubodup, CC0 1.0).
Use: each Gilded Gunship's ring of 12 slow bullets (every 3.0 s, its gold ring lamps lighting 0.4 s before); 2-3 Gunships in a column, Level 30's convoy.
Brief: a hollow cannon thump, heavy and short: the ring fired at once; a real recording, heavy, nothing bubbly or popping.
This one: an 81 mm mortar's shot: darker and boomier than a, a shorter tail, 0.8 s.
Edit: 0.80 s, the loudest 100 ms band −25.1 dB, peak −15.5 dBFS, −26.3 LUFS.

### gunship-doors-r63-a — "Heavy steal door closing and locking.WAV" by Sandermotions

Proposed (round 63), cut from the HQ previews by `tools/concept/audio/sfx_r63.py`.
Source: <https://freesound.org/people/Sandermotions/sounds/275441/> (Heavy steal door closing and locking.WAV by Sandermotions, CC0 1.0).
Use: each Dart stream (every 5 s a Gunship launches 3 Darts out of its belly bays, 0.25 s apart): the doors once, before the first Dart's launch.
Brief: a heavy metallic door clank; a real recording, heavy, nothing bubbly or popping.
This one: a heavy steel door slamming and its lock snapping in: a deep 'KLUNK-tchak', 0.57 s.
Edit: 0.57 s, the loudest 100 ms band −27.0 dB, peak −6.0 dBFS, −26.1 LUFS.

### gunship-doors-r63-b — "Metallic door - Open & close" by Vrymaa

Proposed (round 63), cut from the HQ previews by `tools/concept/audio/sfx_r63.py`.
Source: <https://freesound.org/people/Vrymaa/sounds/734930/> (Metallic door - Open & close by Vrymaa, CC0 1.0).
Use: each Dart stream (every 5 s a Gunship launches 3 Darts out of its belly bays, 0.25 s apart): the doors once, before the first Dart's launch.
Brief: a heavy metallic door clank; a real recording, heavy, nothing bubbly or popping.
This one: an antique iron oven door's clank slowed to 0.75: a ringing iron 'KLANG', more metal and less weight than a, 0.7 s.
Edit: 0.70 s, the loudest 100 ms band −27.1 dB, peak −12.0 dBFS, −30.7 LUFS.

### dart-launch-r63-a — "air_nailer_single_shot_1.wav" by reg7783

Proposed (round 63), cut from the HQ previews by `tools/concept/audio/sfx_r63.py`.
Source: <https://freesound.org/people/reg7783/sounds/131304/> (air_nailer_single_shot_1.wav by reg7783, CC0 1.0).
Use: each Dart launched from a Gunship's bay (streams of 3, 0.25 s apart, every 5 s per Gunship; 2-4 on easy/hard), and the Iron Sovereign's bays later.
Brief: a short pneumatic launch thump (real pneumatics), quiet enough that a stream does not drown the music; a real recording, heavy, nothing bubbly or popping.
This one: a pneumatic nail gun's single shot slowed to 0.8: a dry 'tchunk' with a short air hiss, 0.3 s.
Edit: 0.30 s, the loudest 100 ms band −31.1 dB, peak −13.0 dBFS, −inf LUFS.

### dart-launch-r63-b — "Pneumatic hammer - Atlas Copco r4n - Start 1" by ldezem + "Pneumatic grease bomb" by prnkys

Proposed (round 63), cut from the HQ previews by `tools/concept/audio/sfx_r63.py`.
Sources: <https://freesound.org/people/ldezem/sounds/386306/> (Pneumatic hammer - Atlas Copco r4n - Start 1 by ldezem, CC0 1.0), <https://freesound.org/people/prnkys/sounds/437205/> (Pneumatic grease bomb by prnkys, CC-BY 3.0).
Use: each Dart launched from a Gunship's bay (streams of 3, 0.25 s apart, every 5 s per Gunship; 2-4 on easy/hard), and the Iron Sovereign's bays later.
Brief: a short pneumatic launch thump (real pneumatics), quiet enough that a stream does not drown the music; a real recording, heavy, nothing bubbly or popping.
This one: a pneumatic hammer's first blow slowed to 0.7 with a grease gun's air blast under it (low-passed): a rounder, heavier 'thunk-ff' than a, 0.27 s.
Edit: 0.27 s, the loudest 100 ms band −31.0 dB, peak −12.6 dBFS, −inf LUFS.

### buoy-tick-r63-a — synthesized

Proposed (round 63), synthesized by `tools/concept/audio/sfx_r63.py` (no third-party source).
Use: a beacon buoy's strobe as it passes (one flash in 2 s, white), Level 30's shipping lane.
Brief: a faint electronic tick (synthesized), barely heard.
This one: one soft high tick at 3.2 kHz with a short shimmer ringing out: 'tink', 0.06 s.
Edit: 0.10 s, the loudest 100 ms band −39.9 dB, peak −20.1 dBFS, −inf LUFS.

### buoy-tick-r63-b — synthesized

Proposed (round 63), synthesized by `tools/concept/audio/sfx_r63.py` (no third-party source).
Use: a beacon buoy's strobe as it passes (one flash in 2 s, white), Level 30's shipping lane.
Brief: a faint electronic tick (synthesized), barely heard.
This one: two dry ticks at 2.4 kHz, 60 ms apart: 'tik . tik', drier than a, 0.08 s.
Edit: 0.10 s, the loudest 100 ms band −40.0 dB, peak −21.7 dBFS, −inf LUFS.

## Concept round 64, step AUD-D (Level 31: the Shard links' hum, flicker and snap, the Buzzsaw's whine, spin-up and ricochet, the mining tug's hull hit, section alarm, section loss and engine bed)

Generator: `tools/concept/audio/sfx_r64.py` (cut from the Freesound HQ previews; nothing synthesized). Round 60's picks are not proposed again: the homing launch and run (the Hornet's launch and missile run), the shot-down crack, the enemy shield set, the Ascendancy turbine (the Hornet's hover hum). Not listened to by Claude: checked by level, peaks, onsets, spectra and the loop seams. Passed over (read in their descriptions): synthesized or AI-generated hums, klaxons and engines of the searches (721812, 733736, 721021, 125714, 350508, 705386, 786067), the rejected round-47 klaxon (514981) and its sister takes.

### link-hum-r64-a — "Jacob's ladder.wav" by jensfelger

Chosen (round 64, S.1 a picked), cut from the HQ previews by `tools/concept/audio/sfx_r64.py`.
Source: <https://freesound.org/people/jensfelger/sounds/367456/> (Jacob's ladder.wav by jensfelger, CC0 1.0).
Use: one loop while any Shard link is lit, panned to the nearest group (Level 31's groups of 3-4 drones joined by crimson-gold links, sweeping down as barriers and across the tug).
Brief: a steady electrical transformer hum (a 50/60 Hz buzz) that crackles: live energy between the drones, under the fire; distinct from the generator brown-out's sagging hum (concept/brownout-hum-r60-a.ogg), which can play with it; a real recording, heavy, nothing bubbly or popping.
This one: a 50 kV Jacob's ladder: a raw 100 Hz arc buzz with sharp crackles running through it, 4.0 s loop.
Edit: 4.00 s, band RMS −35.0 dB, peak −8.6 dBFS, −30.6 LUFS.

### link-hum-r64-b — "transformer at night.wav" by ideaph + "Zapping - High Voltage Electricity Buzz Isolated" by el_boss

Rejected (round 64, S.1 a picked), cut from the HQ previews by `tools/concept/audio/sfx_r64.py`.
Sources: <https://freesound.org/people/ideaph/sounds/641208/> (transformer at night.wav by ideaph, CC0 1.0), <https://freesound.org/people/el_boss/sounds/740198/> (Zapping - High Voltage Electricity Buzz Isolated by el_boss, CC0 1.0).
Use: one loop while any Shard link is lit, panned to the nearest group (Level 31's groups of 3-4 drones joined by crimson-gold links, sweeping down as barriers and across the tug).
Brief: a steady electrical transformer hum (a 50/60 Hz buzz) that crackles: live energy between the drones, under the fire; distinct from the generator brown-out's sagging hum (concept/brownout-hum-r60-a.ogg), which can play with it; a real recording, heavy, nothing bubbly or popping.
This one: a pedestal transformer's 240 Hz hum at night under high-voltage lines buzzing in fog: a smoother, more tonal hum than a with a fine fizzing crackle on top, 6.0 s loop.
Edit: 6.00 s, band RMS −35.0 dB, peak −25.6 dBFS, −32.4 LUFS.

### link-flicker-r64-a — "Jacob's ladder.wav" by jensfelger

Chosen (round 64, S.2 a picked), cut from the HQ previews by `tools/concept/audio/sfx_r64.py`.
Source: <https://freesound.org/people/jensfelger/sounds/367456/> (Jacob's ladder.wav by jensfelger, CC0 1.0).
Use: a group's links flickering for 0.8 s before they light (0.5 s on hard): after the group's last drone enters, and when the links relight after the EMP's stun; then the hum loop takes over.
Brief: a stuttering buzz: the links' hum cutting in and out, then holding as they light; a real recording, heavy, nothing bubbly or popping.
This one: the Jacob's ladder's buzz (the hum a) cut into seven bursts, longer and louder towards the end: 'bzt . bzt . bzzt .. bzzzzt', 0.8 s.
Edit: 0.80 s, the loudest 100 ms band −28.0 dB, peak −16.1 dBFS, −30.0 LUFS.

### link-flicker-r64-b — "transformer at night.wav" by ideaph + "Zapping - High Voltage Electricity Buzz Isolated" by el_boss

Rejected (round 64, S.2 a picked), cut from the HQ previews by `tools/concept/audio/sfx_r64.py`.
Sources: <https://freesound.org/people/ideaph/sounds/641208/> (transformer at night.wav by ideaph, CC0 1.0), <https://freesound.org/people/el_boss/sounds/740198/> (Zapping - High Voltage Electricity Buzz Isolated by el_boss, CC0 1.0).
Use: a group's links flickering for 0.8 s before they light (0.5 s on hard): after the group's last drone enters, and when the links relight after the EMP's stun; then the hum loop takes over.
Brief: a stuttering buzz: the links' hum cutting in and out, then holding as they light; a real recording, heavy, nothing bubbly or popping.
This one: the transformer hum and the power lines' buzz (the hum b) alternating in seven bursts: a hummier, less crackling stutter than a, 0.8 s.
Edit: 0.82 s, the loudest 100 ms band −28.0 dB, peak −21.4 dBFS, −31.4 LUFS.

### link-snap-r64-a — "Flash 1,2MV_3.wav" by jensfelger

Chosen (round 64, S.3 a picked), cut from the HQ previews by `tools/concept/audio/sfx_r64.py`.
Source: <https://freesound.org/people/jensfelger/sounds/367453/> (Flash 1,2MV_3.wav by jensfelger, CC0 1.0).
Use: a link breaking as one of its two drones dies (a middle drone drops both its links: one snap); a few times a group, over the drone's own death (its `small` rung).
Brief: a sharp electric snap: the energy shorting out, dry and short; no pop; a real recording, heavy, nothing bubbly or popping.
This one: a 1.2 MV flashover's crack, cut short: a big dry 'KSSHAK' with a short rolling tail, 0.45 s.
Edit: 0.45 s, the loudest 100 ms band −24.3 dB, peak −9.1 dBFS, −22.4 LUFS.

### link-snap-r64-b — "JacobsLadderSingle2.flac" by Halleck

Rejected (round 64, S.3 a picked), cut from the HQ previews by `tools/concept/audio/sfx_r64.py`.
Source: <https://freesound.org/people/Halleck/sounds/19487/> (JacobsLadderSingle2.flac by Halleck, CC-BY 4.0).
Use: a link breaking as one of its two drones dies (a middle drone drops both its links: one snap); a few times a group, over the drone's own death (its `small` rung).
Brief: a sharp electric snap: the energy shorting out, dry and short; no pop; a real recording, heavy, nothing bubbly or popping.
This one: a Jacob's ladder arc striking and buzzing out: a thinner, brighter 'TSSK-zzz' than a, 0.35 s.
Edit: 0.35 s, the loudest 100 ms band −25.1 dB, peak −5.1 dBFS, −inf LUFS.

### saw-whine-r64-a — "9in Angle Grinder" by jameswrowles

Chosen (round 64, S.4 a picked), cut from the HQ previews by `tools/concept/audio/sfx_r64.py`.
Source: <https://freesound.org/people/jameswrowles/sounds/516593/> (9in Angle Grinder by jameswrowles, CC0 1.0).
Use: one loop while a Buzzsaw Drone is on the screen, for the nearest; its pitch follows the spin (the game pitches it: 2 turns/s chasing, up to 6 when spun up).
Brief: a real circular saw or angle grinder's whine, steady, under the fire; a real recording, heavy, nothing bubbly or popping.
This one: a 9-inch angle grinder running free: a rough, heavy whine with a gritty motor under it, 3.0 s loop.
Edit: 3.00 s, band RMS −36.4 dB, peak −23.4 dBFS, −30.3 LUFS.

### saw-whine-r64-b — "Foley_Mechanism_Circular_Saw_Powering_Stereo.wav" by Nox_Sound

Rejected (round 64, S.4 a picked), cut from the HQ previews by `tools/concept/audio/sfx_r64.py`.
Source: <https://freesound.org/people/Nox_Sound/sounds/554366/> (Foley_Mechanism_Circular_Saw_Powering_Stereo.wav by Nox_Sound, CC0 1.0).
Use: one loop while a Buzzsaw Drone is on the screen, for the nearest; its pitch follows the spin (the game pitches it: 2 turns/s chasing, up to 6 when spun up).
Brief: a real circular saw or angle grinder's whine, steady, under the fire; a real recording, heavy, nothing bubbly or popping.
This one: a circular saw running free, slowed to 0.85: a thinner, shriller and airier whine than a, 2.0 s loop.
Edit: 2.00 s, band RMS −40.8 dB, peak −20.1 dBFS, −30.3 LUFS.

### saw-spinup-r64-a — "9in Angle Grinder" by jameswrowles

Rejected (round 64, S.5 ab picked), cut from the HQ previews by `tools/concept/audio/sfx_r64.py`.
Source: <https://freesound.org/people/jameswrowles/sounds/516593/> (9in Angle Grinder by jameswrowles, CC0 1.0).
Use: each Buzzsaw's 0.6 s spin-up tell before a dash (0.8 s on easy, 0.5 s on hard; the blades speed up and the gold rim flickers red); a group staggers its tells 0.4 s apart.
Brief: the saw's whine rising sharply to a scream: the machine spinning up, ending on the loop's pitch (the a/b should match the whine's); a real recording, heavy, nothing bubbly or popping.
This one: the angle grinder (the whine a) glided from 0.35 to 1.0 speed and swelling: a rough 'wrrrRRIIII' rising to its run, 0.6 s.
Edit: 0.61 s, the loudest 100 ms band −26.3 dB, peak −13.8 dBFS, −25.3 LUFS.

### saw-spinup-r64-b — "Foley_Mechanism_Circular_Saw_Powering_Stereo.wav" by Nox_Sound

Rejected (round 64, S.5 ab picked), cut from the HQ previews by `tools/concept/audio/sfx_r64.py`.
Source: <https://freesound.org/people/Nox_Sound/sounds/554366/> (Foley_Mechanism_Circular_Saw_Powering_Stereo.wav by Nox_Sound, CC0 1.0).
Use: each Buzzsaw's 0.6 s spin-up tell before a dash (0.8 s on easy, 0.5 s on hard; the blades speed up and the gold rim flickers red); a group staggers its tells 0.4 s apart.
Brief: the saw's whine rising sharply to a scream: the machine spinning up, ending on the loop's pitch (the a/b should match the whine's); a real recording, heavy, nothing bubbly or popping.
This one: the circular saw (the whine b) glided from 0.3 to 0.85 speed and swelling: a thinner 'wheeeEEE' rising to a shriek, 0.6 s.
Edit: 0.61 s, the loudest 100 ms band −31.5 dB, peak −12.9 dBFS, −24.3 LUFS.

### saw-spinup-r64-ab — "9in Angle Grinder" by jameswrowles

Chosen (round 64, S.5 b's shape with S.4 a's grinder), cut from the HQ preview by `tools/concept/audio/sfx_r64.py saw-spinup-r64-ab`.
Source: <https://freesound.org/people/jameswrowles/sounds/516593/> (9in Angle Grinder by jameswrowles, CC0 1.0).
Use: each Buzzsaw's 0.6 s spin-up tell before a dash.
Brief: the user picked b's spin-up (its whine's recording glided up) with the grinder loop (S.4 a), so b's shape on the grinder's recording, ending on the loop's pitch.
This one: the angle grinder glided from 0.3 up to 1.0 speed, swelling, high-passed at 100 Hz and low-passed at 7 kHz as b: a thinner 'wheeeEEE' than a that ends on the grinder's whine, 0.6 s.
Edit: 0.61 s, the loudest 100 ms band −26.4 dB, peak −14.4 dBFS, −25.3 LUFS.

### saw-ricochet-r64-a — "METLFric-Circular Saw Blade, Scrape on Concrete_02_With Impact_RG_2023_NTG4" by Riley_Garinger

Chosen (round 64, S.6 a picked), cut from the HQ previews by `tools/concept/audio/sfx_r64.py`.
Source: <https://freesound.org/people/Riley_Garinger/sounds/720829/> (METLFric-Circular Saw Blade, Scrape on Concrete_02_With Impact_RG_2023_NTG4 by Riley_Garinger, CC-BY 4.0).
Use: a Buzzsaw's dash bouncing off a big drifting rock or the field's edge (up to 3 bounces a dash, 2 on easy; small rocks are smashed instead and play their own break).
Brief: a metal scrape-clang: the spinning blade striking stone and skidding off; a real recording, heavy, nothing bubbly or popping.
This one: a circular saw blade slammed on concrete and skidding off, slowed to 0.85: a hard 'KLANG-shhk', 0.55 s.
Edit: 0.55 s, the loudest 100 ms band −27.4 dB, peak −16.9 dBFS, −25.2 LUFS.

### saw-ricochet-r64-b — "METLImpt_Metal Resonant Scrape Impact_SDFY_Metal Resonant.wav" by SoundDesignForYou

Rejected (round 64, S.6 a picked), cut from the HQ previews by `tools/concept/audio/sfx_r64.py`.
Source: <https://freesound.org/people/SoundDesignForYou/sounds/697758/> (METLImpt_Metal Resonant Scrape Impact_SDFY_Metal Resonant.wav by SoundDesignForYou, CC0 1.0).
Use: a Buzzsaw's dash bouncing off a big drifting rock or the field's edge (up to 3 bounces a dash, 2 on easy; small rocks are smashed instead and play their own break).
Brief: a metal scrape-clang: the spinning blade striking stone and skidding off; a real recording, heavy, nothing bubbly or popping.
This one: a resonant metal scrape into a heavy hit, sped to 1.2: a scrape first, then a deeper, ringing 'skrr-DONG' than a, 0.6 s.
Edit: 0.60 s, the loudest 100 ms band −27.4 dB, peak −11.5 dBFS, −24.4 LUFS.

### tug-hit-r64-a — "HUTCH HD SFX 2014 0143" by leonelmail + "metal crank groan.flac" by kyles

Rejected (round 64, S.7 b picked), cut from the HQ previews by `tools/concept/audio/sfx_r64.py`.
Sources: <https://freesound.org/people/leonelmail/sounds/462819/> (HUTCH HD SFX 2014 0143 by leonelmail, CC0 1.0), <https://freesound.org/people/kyles/sounds/453333/> (metal crank groan.flac by kyles, CC0 1.0).
Use: each hit on one of the tug's three sections (a Hornet missile bursting on it, a Dart diving it, a Shard link dragged across it), throttled to once per 0.2 s; the HUD's section bar flashes white.
Brief: a heavy hull impact: metal struck hard with a groan, big and hollow (a ship's hull, not a tin can); a real recording, heavy, nothing bubbly or popping.
This one: a deep, hollow metal impact (slowed to 0.9) with a crank's metal groan slowed to 0.6 under its tail: a booming 'DOOMM-mmrrr', 0.7 s.
Edit: 0.70 s, the loudest 100 ms band −25.0 dB, peak −7.8 dBFS, −23.0 LUFS.

### tug-hit-r64-b — "Kicking_Dumpster.wav" by kmyers1316 + "cemeterygate15.aif" by celadon_noise

Chosen (round 64, S.7 b picked), cut from the HQ previews by `tools/concept/audio/sfx_r64.py`.
Sources: <https://freesound.org/people/kmyers1316/sounds/344592/> (Kicking_Dumpster.wav by kmyers1316, CC0 1.0), <https://freesound.org/people/celadon_noise/sounds/399450/> (cemeterygate15.aif by celadon_noise, CC0 1.0).
Use: each hit on one of the tug's three sections (a Hornet missile bursting on it, a Dart diving it, a Shard link dragged across it), throttled to once per 0.2 s; the HUD's section bar flashes white.
Brief: a heavy hull impact: metal struck hard with a groan, big and hollow (a ship's hull, not a tin can); a real recording, heavy, nothing bubbly or popping.
This one: a kicked steel dumpster slowed to 0.7 with an iron gate's hinge groan slowed to 0.55: a harder, clangier 'KLONGG' than a with a creaking groan, 0.7 s.
Edit: 0.70 s, the loudest 100 ms band −25.1 dB, peak −15.4 dBFS, −25.8 LUFS.

### tug-alarm-r64-a — "Factory Warning Buzzer" by qubodup

Chosen (round 64, S.8 a picked), cut from the HQ previews by `tools/concept/audio/sfx_r64.py`.
Source: <https://freesound.org/people/qubodup/sounds/832414/> (Factory Warning Buzzer by qubodup, CC0 1.0).
Use: a tug section falling below 50 % (it starts to smoke) and again when it is lost (with the loss below): up to six times a level; over the music, with the radio's `first-ally-hit` / `ally-lost` line.
Brief: a klaxon burst: a ship's alarm, two blasts; distinct from Level 29's station klaxon (concept/station-klaxon-r62-a.ogg). The relay's chosen integrity alarm (concept/integrity-alarm-r35-a.ogg, the same 'an ally is hurt' event, kept in round 47) is the third option; a real recording, heavy, nothing bubbly or popping.
This one: a bridge crane's warning buzzer, twice: two harsh electric 'BRAAAP's 0.9 s apart, the second ringing out, 2.1 s.
Edit: 2.10 s, the loudest 100 ms band −23.0 dB, peak −12.4 dBFS, −20.1 LUFS.

### tug-alarm-r64-b — "120616 Boat horn, harbour tour, NYC.flac" by TRP

Rejected (round 64, S.8 a picked), cut from the HQ previews by `tools/concept/audio/sfx_r64.py`.
Source: <https://freesound.org/people/TRP/sounds/577494/> (120616 Boat horn, harbour tour, NYC.flac by TRP, CC0 1.0).
Use: a tug section falling below 50 % (it starts to smoke) and again when it is lost (with the loss below): up to six times a level; over the music, with the radio's `first-ally-hit` / `ally-lost` line.
Brief: a klaxon burst: a ship's alarm, two blasts; distinct from Level 29's station klaxon (concept/station-klaxon-r62-a.ogg). The relay's chosen integrity alarm (concept/integrity-alarm-r35-a.ogg, the same 'an ally is hurt' event, kept in round 47) is the third option; a real recording, heavy, nothing bubbly or popping.
This one: a tour boat's horn heard on board, cut into two blasts: two deep 215 Hz 'HOOONK's, lower and rounder than a, 1.65 s.
Edit: 1.65 s, the loudest 100 ms band −22.0 dB, peak −12.5 dBFS, −21.9 LUFS.

### tug-lost-r64-a — "Big Structure Collision Heard from Inside" by Osvoldon + "designed metal shearing, ripping.wav" by vestibule-door + "Fire Burst.wav" by SilverIllusionist

Chosen (round 64, S.9 a picked), cut from the HQ previews by `tools/concept/audio/sfx_r64.py`.
Sources: <https://freesound.org/people/Osvoldon/sounds/653294/> (Big Structure Collision Heard from Inside by Osvoldon, CC0 1.0), <https://freesound.org/people/vestibule-door/sounds/669692/> (designed metal shearing, ripping.wav by vestibule-door, CC0 1.0), <https://freesound.org/people/SilverIllusionist/sounds/472688/> (Fire Burst.wav by SilverIllusionist, CC-BY 4.0).
Use: a tug section lost (its lights go out, a short fire flare, then it vents): up to three times a level, with the alarm above; the third loss breaks the hull and fails the escort.
Brief: a structural failure: tearing metal and a fire flare's roar; heavy, no pop; a real recording, heavy, nothing bubbly or popping.
This one: a massive structure's collision heard from inside, metal shearing (slowed to 0.8) and a burst of fire (slowed to 0.8): a deep 'BOOM' with a rip and a flaring whoosh, 1.8 s.
Edit: 1.80 s, the loudest 100 ms band −23.1 dB, peak −5.8 dBFS, −19.1 LUFS.

### tug-lost-r64-b — "Kicking_Dumpster.wav" by kmyers1316 + "designed metal shearing, ripping.wav" by vestibule-door + "TF_Flame.flac" by copyc4t

Rejected (round 64, S.9 a picked), cut from the HQ previews by `tools/concept/audio/sfx_r64.py`.
Sources: <https://freesound.org/people/kmyers1316/sounds/344592/> (Kicking_Dumpster.wav by kmyers1316, CC0 1.0), <https://freesound.org/people/vestibule-door/sounds/669692/> (designed metal shearing, ripping.wav by vestibule-door, CC0 1.0), <https://freesound.org/people/copyc4t/sounds/235731/> (TF_Flame.flac by copyc4t, CC-BY 4.0).
Use: a tug section lost (its lights go out, a short fire flare, then it vents): up to three times a level, with the alarm above; the third loss breaks the hull and fails the escort.
Brief: a structural failure: tearing metal and a fire flare's roar; heavy, no pop; a real recording, heavy, nothing bubbly or popping.
This one: a steel dumpster kicked (slowed to 0.55), a longer metal rip (slowed to 0.7) and a flamethrower's roar: more tearing and a lower, longer roar than a, a smaller impact, 1.8 s.
Edit: 1.70 s, the loudest 100 ms band −21.8 dB, peak −7.7 dBFS, −19.1 LUFS.

### tug-engine-r64-a — "Engine Train Idle" by Wakerone

Chosen (round 64, S.10 a picked), cut from the HQ previews by `tools/concept/audio/sfx_r64.py`.
Source: <https://freesound.org/people/Wakerone/sounds/417553/> (Engine Train Idle by Wakerone, CC0 1.0).
Use: Level 31: a layer over the `belt-open` bed while the tug's engines section lives (it stops when the engines are lost), from the tug's rise into its station until its climb-out.
Brief: a huge mining machine's low drive rumble, a diesel-electric thrum heard through the hull; seamless, steady, under everything; a real recording, heavy, nothing bubbly or popping.
This one: a diesel locomotive idling in a station, slowed to 0.8: a deep throbbing diesel drone, 8.0 s loop.
Edit: 8.00 s, band RMS −38.1 dB, peak −22.3 dBFS, −33.9 LUFS.

### tug-engine-r64-b — "Diesel Locomotive V16 GE Engine Idling (Loop)" by WeenyBeany + "Engine-Room-door-018.wav" by Astounded

Rejected (round 64, S.10 a picked), cut from the HQ previews by `tools/concept/audio/sfx_r64.py`.
Sources: <https://freesound.org/people/WeenyBeany/sounds/773336/> (Diesel Locomotive V16 GE Engine Idling (Loop) by WeenyBeany, CC-BY 4.0), <https://freesound.org/people/Astounded/sounds/520807/> (Engine-Room-door-018.wav by Astounded, CC-BY 4.0).
Use: Level 31: a layer over the `belt-open` bed while the tug's engines section lives (it stops when the engines are lost), from the tug's rise into its station until its climb-out.
Brief: a huge mining machine's low drive rumble, a diesel-electric thrum heard through the hull; seamless, steady, under everything; a real recording, heavy, nothing bubbly or popping.
This one: a GE V16 diesel-electric locomotive idling, slowed to 0.6, over a contact microphone on an engine-room door: a smoother, more humming thrum than a with a machine's whine in the hull, 6.0 s loop.
Edit: 6.00 s, band RMS −38.0 dB, peak −26.2 dBFS, −35.2 LUFS.

### serpent-rumble-r64-a — "electric train engine sound effect" by Garuda1982 + "TTC - Subway Ride, Union to Dundas, Ambience - Toronto, Canada" by Podel1

Chosen (round 64, S.11 a picked), cut from the HQ previews by `tools/concept/audio/sfx_r64.py`.
Sources: <https://freesound.org/people/Garuda1982/sounds/559381/> (electric train engine sound effect by Garuda1982, CC-BY 4.0), <https://freesound.org/people/Podel1/sounds/736492/> (TTC - Subway Ride, Union to Dundas, Ambience - Toronto, Canada by Podel1, CC-BY 4.0).
Use: one loop while a Rail Serpent is on the screen, for the nearest (the Act 3 rule: once per kind); Level 30 and later Act 5 levels.
Brief: a heavy rail car's rumble with a rhythmic coupling clatter (a real train), under the fire, seamless; this redo is to carry the engine's electric whine, which neither round-63 loop had; a real recording, heavy, nothing bubbly or popping.
This one: an electric locomotive's traction-motor whine (a steady 2 kHz tone with its 12 Hz flutter) over a subway train's steady wheel clatter on the rail joints (about 2.5 clacks a second): a train felt as a machine, closer and more regular than the round-63 a, 6.0 s loop.
Edit: 6.00 s, band RMS −34.7 dB, peak −16.4 dBFS, −29.1 LUFS.

### serpent-rumble-r64-b — "Moscow monorail_Interior.WAV" by softwalls + "TTC - Subway Ride, Union to Dundas, Ambience - Toronto, Canada" by Podel1

Rejected (round 64, S.11 a picked), cut from the HQ previews by `tools/concept/audio/sfx_r64.py`.
Sources: <https://freesound.org/people/softwalls/sounds/384528/> (Moscow monorail_Interior.WAV by softwalls, CC0 1.0), <https://freesound.org/people/Podel1/sounds/736492/> (TTC - Subway Ride, Union to Dundas, Ambience - Toronto, Canada by Podel1, CC-BY 4.0).
Use: one loop while a Rail Serpent is on the screen, for the nearest (the Act 3 rule: once per kind); Level 30 and later Act 5 levels.
Brief: a heavy rail car's rumble with a rhythmic coupling clatter (a real train), under the fire, seamless; this redo is to carry the engine's electric whine, which neither round-63 loop had; a real recording, heavy, nothing bubbly or popping.
This one: a monorail car's low electric drone (slowed to 0.8) with the soft thumps of a subway car over the rail joints, low-passed to 900 Hz: a hum and a faint rhythmic thud, no clatter and no whine, 6.0 s loop.
Edit: 6.00 s, band RMS −35.0 dB, peak −14.4 dBFS, −29.0 LUFS.

## Concept round 65, step AUD-E (Level 32: the Sentinel Tower's charge, beam, head turn and death, the Strider's step, torso servo, twin cannons, leg snap and fall, the blast door's grind and the mining charge's blast)

Generator: `tools/concept/audio/sfx_r65.py` (cut from the Freesound HQ previews; nothing synthesized). Not proposed again: the tunnel bed (round 60 S.19 a), the furnace roar (Level 19's foundry bed, vent tell and burst), the SAM Nest's hatch, lock and salvo (round 62), the shared homing launch (the Strider's pod). Not listened to by Claude: checked by level, peaks, onsets, spectra and the loop seams. Passed over (read in their descriptions): designed or synthesized "robot", "laser charge" and "power up" sounds of the searches (Artninja's TF-inspired footsteps, magnuswaker's laser charge-ups, DAN2008's "Charged up", the sci-fi cannons).

### sentinel-charge-r65-a — "Device powering up.wav" by spoonsandlessspoons + "ELECSprk_Electrical Discharges With Sparking.Steady 2_EM.mp3" by newlocknew

Proposed (round 65), cut from the HQ previews by `tools/concept/audio/sfx_r65.py`.
Sources: <https://freesound.org/people/spoonsandlessspoons/sounds/361339/> (Device powering up.wav by spoonsandlessspoons, CC-BY 4.0), <https://freesound.org/people/newlocknew/sounds/702908/> (ELECSprk_Electrical Discharges With Sparking.Steady 2_EM.mp3 by newlocknew, CC-BY 4.0).
Use: each Sentinel sweep's telegraph (Level 32's side galleries; at most 2 towers sweeping at once): the 0.8 s arc telegraph (the crimson arc drawn), and from 0.2 s into the full turn's 1.0 s telegraph (the reach ring drawn first); the live beam's loop follows at its end.
Brief: a rising electrical hum: a transformer or a capacitor bank charging, building to the beam; distinct from the rail line's chosen charge (concept/rail-charge-r60-b.ogg, a rising whine); a real recording, heavy, nothing bubbly or popping.
This one: a machine powering up, its 40 Hz hum glided up from 1.5 to 3 times its speed, with an electric discharge's crackle swelling in over its last half: a low 'mmmMMMM' rising into a fizz, 0.8 s.
Edit: 0.80 s, the loudest 100 ms band −29.1 dB, peak −12.8 dBFS, −26.2 LUFS.

### sentinel-charge-r65-b — "Gym Lights Powering Up" by OBXJohn

Proposed (round 65), cut from the HQ previews by `tools/concept/audio/sfx_r65.py`.
Source: <https://freesound.org/people/OBXJohn/sounds/262510/> (Gym Lights Powering Up by OBXJohn, CC0 1.0).
Use: each Sentinel sweep's telegraph (Level 32's side galleries; at most 2 towers sweeping at once): the 0.8 s arc telegraph (the crimson arc drawn), and from 0.2 s into the full turn's 1.0 s telegraph (the reach ring drawn first); the live beam's loop follows at its end.
Brief: a rising electrical hum: a transformer or a capacitor bank charging, building to the beam; distinct from the rail line's chosen charge (concept/rail-charge-r60-b.ogg, a rising whine); a real recording, heavy, nothing bubbly or popping.
This one: a gym's metal-halide lights powering up: their ballasts' 120 Hz hum gliding up and swelling into the lamps' strike at its end: a buzzier, more mains-like 'bzzzZZT' than a, 0.8 s.
Edit: 0.80 s, the loudest 100 ms band −29.9 dB, peak −16.5 dBFS, −25.3 LUFS.

### sentinel-beam-r65-a — "welder 2.wav" by SpeedY

Proposed (round 65), cut from the HQ previews by `tools/concept/audio/sfx_r65.py`.
Source: <https://freesound.org/people/SpeedY/sounds/62923/> (welder 2.wav by SpeedY, CC0 1.0).
Use: one loop while a Sentinel's beam sweeps (1.6 s an arc, 3.0 s a full turn; the nearest tower when two sweep), panned to the tower; cut at once when the EMP stuns it.
Brief: a harsh sizzle: an arc welder or an electric arc, raw and steady, under the fire; not the Mantis's sweep (concept/enemy-mantis-sweep-r23-b.ogg); a real recording, heavy, nothing bubbly or popping.
This one: a stick welder's arc burning steadily: a harsh frying sizzle over its 100 Hz buzz, 3.0 s loop.
Edit: 3.00 s, band RMS −34.0 dB, peak −15.6 dBFS, −29.2 LUFS.

### sentinel-beam-r65-b — "ELECSprk_Electrical Discharges With Sparking.Steady 2_EM.mp3" by newlocknew

Proposed (round 65), cut from the HQ previews by `tools/concept/audio/sfx_r65.py`.
Source: <https://freesound.org/people/newlocknew/sounds/702908/> (ELECSprk_Electrical Discharges With Sparking.Steady 2_EM.mp3 by newlocknew, CC-BY 4.0).
Use: one loop while a Sentinel's beam sweeps (1.6 s an arc, 3.0 s a full turn; the nearest tower when two sweep), panned to the tower; cut at once when the EMP stuns it.
Brief: a harsh sizzle: an arc welder or an electric arc, raw and steady, under the fire; not the Mantis's sweep (concept/enemy-mantis-sweep-r23-b.ogg); a real recording, heavy, nothing bubbly or popping.
This one: a steady high-voltage discharge sparking: a brighter, crackling sizzle than a with less buzz under it, 3.0 s loop.
Edit: 3.00 s, band RMS −41.0 dB, peak −22.2 dBFS, −29.3 LUFS.

### sentinel-servo-r65-a — "Running Gear" by Vurca

Proposed (round 65), cut from the HQ previews by `tools/concept/audio/sfx_r65.py`.
Source: <https://freesound.org/people/Vurca/sounds/315753/> (Running Gear by Vurca, CC0 1.0).
Use: under the beam while a Sentinel's head turns with it (its sweeps: the arc's 1.6 s and the full turn's 3.0 s), quieter than the beam; the game may pitch it down for the full turn.
Brief: a low servo grind: a heavy turret's gear motor turning, steady; a real recording, heavy, nothing bubbly or popping.
This one: a running gear train slowed to 0.7: a low, even mechanical grind with a gear whine, 2.0 s loop.
Edit: 2.00 s, band RMS −39.1 dB, peak −18.5 dBFS, −35.1 LUFS.

### sentinel-servo-r65-b — "Stripped Gears in VAV Box" by itickets

Proposed (round 65), cut from the HQ previews by `tools/concept/audio/sfx_r65.py`.
Source: <https://freesound.org/people/itickets/sounds/490919/> (Stripped Gears in VAV Box by itickets, CC0 1.0).
Use: under the beam while a Sentinel's head turns with it (its sweeps: the arc's 1.6 s and the full turn's 3.0 s), quieter than the beam; the game may pitch it down for the full turn.
Brief: a low servo grind: a heavy turret's gear motor turning, steady; a real recording, heavy, nothing bubbly or popping.
This one: stripped gears grinding in a ventilation box, slowed to 0.8: a rougher, rattling grind than a, 2.0 s loop.
Edit: 2.00 s, band RMS −39.1 dB, peak −25.6 dBFS, −35.5 LUFS.

### sentinel-death-r65-a — "Big Structure Collision Heard from Inside" by Osvoldon + "Scrap metal dropping / crashing" by SamsterBirdies + "snd_metal_smash.wav" by gristi

Proposed (round 65), cut from the HQ previews by `tools/concept/audio/sfx_r65.py`.
Sources: <https://freesound.org/people/Osvoldon/sounds/653294/> (Big Structure Collision Heard from Inside by Osvoldon, CC0 1.0), <https://freesound.org/people/SamsterBirdies/sounds/587443/> (Scrap metal dropping / crashing by SamsterBirdies, CC0 1.0), <https://freesound.org/people/gristi/sounds/562198/> (snd_metal_smash.wav by gristi, CC0 1.0).
Use: a Sentinel destroyed (four in Level 32; `TOWERS DOWN` counts them), over its `medium` ground explosion: the head topples off and the scorched stump stays.
Brief: a heavy metal crash with the head falling: a structure breaking, then the head's smaller clattering impact; a real recording, heavy, nothing bubbly or popping.
This one: a massive structure's collision heard from inside with scrap metal crashing over it, then a metal smash 0.5 s later (the head landing): 'KRRASH ... klang', 1.2 s.
Edit: 1.20 s, the loudest 100 ms band −23.3 dB, peak −4.0 dBFS, −17.4 LUFS.

### sentinel-death-r65-b — "Kicking_Dumpster.wav" by kmyers1316 + "metal-impact" by JeffPearson

Proposed (round 65), cut from the HQ previews by `tools/concept/audio/sfx_r65.py`.
Sources: <https://freesound.org/people/kmyers1316/sounds/344592/> (Kicking_Dumpster.wav by kmyers1316, CC0 1.0), <https://freesound.org/people/JeffPearson/sounds/850040/> (metal-impact by JeffPearson, CC0 1.0).
Use: a Sentinel destroyed (four in Level 32; `TOWERS DOWN` counts them), over its `medium` ground explosion: the head topples off and the scorched stump stays.
Brief: a heavy metal crash with the head falling: a structure breaking, then the head's smaller clattering impact; a real recording, heavy, nothing bubbly or popping.
This one: a kicked steel dumpster slowed to 0.6, then a resonant metal impact ringing 0.4 s later (the head): a duller 'DOOMF' with a long bell-like 'dongg' after it, 1.3 s.
Edit: 1.30 s, the loudest 100 ms band −19.0 dB, peak −9.0 dBFS, −21.3 LUFS.

### strider-step-r65-a — "Big robot footstep 002" by AudioPapkin + "Hydraulic Hiss, TTC bus door" by OttisG

Proposed (round 65), cut from the HQ previews by `tools/concept/audio/sfx_r65.py`.
Sources: <https://freesound.org/people/AudioPapkin/sounds/813053/> (Big robot footstep 002 by AudioPapkin, CC0 1.0), <https://freesound.org/people/OttisG/sounds/852991/> (Hydraulic Hiss, TTC bus door by OttisG, CC-BY 4.0).
Use: each footfall of a walking Strider (Level 32's tunnel floors, in pairs: at most two walking; 32 px/s, an 8-frame walk cycle, two footfalls a cycle), throttled so a pair does not drown the music.
Brief: a heavy metal stomp with a short hydraulic hiss (a hydraulic press or a car lift); smaller than the Revenant Walker's chosen stride (concept/walker-stomp-r46-a.ogg); a real recording, heavy, nothing bubbly or popping.
This one: the Revenant Walker's chosen stride made smaller: the same big robot footstep and bus door's hydraulic hiss, sped to 1.25 (higher and shorter): 'KLUMP-tsss', 0.8 s.
Edit: 0.80 s, the loudest 100 ms band −27.1 dB, peak −12.0 dBFS, −22.4 LUFS.

### strider-step-r65-b — "big metallic robot footsteps" by gladkiy + "Air (or steam) pressure release" by brunoboselli

Proposed (round 65), cut from the HQ previews by `tools/concept/audio/sfx_r65.py`.
Sources: <https://freesound.org/people/gladkiy/sounds/342235/> (big metallic robot footsteps by gladkiy, CC0 1.0), <https://freesound.org/people/brunoboselli/sounds/457294/> (Air (or steam) pressure release by brunoboselli, CC0 1.0).
Use: each footfall of a walking Strider (Level 32's tunnel floors, in pairs: at most two walking; 32 px/s, an 8-frame walk cycle, two footfalls a cycle), throttled so a pair does not drown the music.
Brief: a heavy metal stomp with a short hydraulic hiss (a hydraulic press or a car lift); smaller than the Revenant Walker's chosen stride (concept/walker-stomp-r46-a.ogg); a real recording, heavy, nothing bubbly or popping.
This one: a big metal footstep (sped to 1.1) with an air pressure release's short hiss: a clangier, deeper thud than a with a sharper 'pssh', 0.75 s.
Edit: 0.75 s, the loudest 100 ms band −27.0 dB, peak −11.0 dBFS, −23.1 LUFS.

### strider-servo-r65-a — "ELECTRIC_MOTOR.aif" by jacobsteel

Proposed (round 65), cut from the HQ previews by `tools/concept/audio/sfx_r65.py`.
Source: <https://freesound.org/people/jacobsteel/sounds/336665/> (ELECTRIC_MOTOR.aif by jacobsteel, CC-BY 4.0).
Use: the Strider's torso twisting toward the ship to fire (32 angles), as it starts a turn; throttled to once per 0.5 s a Strider (it turns often).
Brief: a servo whine: an electric motor driving the torso round, short and mechanical; a real recording, heavy, nothing bubbly or popping.
This one: an electric motor's whine glided up from 0.75 to its own speed: a gritty, mid 'vwiiir', 0.5 s.
Edit: 0.50 s, the loudest 100 ms band −32.0 dB, peak −22.2 dBFS, −28.8 LUFS.

### strider-servo-r65-b — "ServoMotor.wav" by derjuli

Proposed (round 65), cut from the HQ previews by `tools/concept/audio/sfx_r65.py`.
Source: <https://freesound.org/people/derjuli/sounds/824121/> (ServoMotor.wav by derjuli, CC0 1.0).
Use: the Strider's torso twisting toward the ship to fire (32 angles), as it starts a turn; throttled to once per 0.5 s a Strider (it turns often).
Brief: a servo whine: an electric motor driving the torso round, short and mechanical; a real recording, heavy, nothing bubbly or popping.
This one: a servo motor slowed to 0.5: a cleaner, higher and thinner 'eeeee' than a, 0.5 s.
Edit: 0.50 s, the loudest 100 ms band −35.0 dB, peak −21.4 dBFS, −28.6 LUFS.

### strider-cannon-r65-a — "Autocannon Shot 1" by qubodup + "Autocannon Shot 2" by qubodup

Proposed (round 65), cut from the HQ previews by `tools/concept/audio/sfx_r65.py`.
Sources: <https://freesound.org/people/qubodup/sounds/854183/> (Autocannon Shot 1 by qubodup, CC0 1.0), <https://freesound.org/people/qubodup/sounds/854184/> (Autocannon Shot 2 by qubodup, CC0 1.0).
Use: each fan of the Strider's twin cannons (3 shots 12° apart, every 2.4 s; easy 3.0 s, hard a fan of 5 every 2.0 s), after the torso's 0.3 s hold; the shoulder pod's homing salvo plays the shared homing launch.
Brief: a deep double gunshot: two heavy cannons firing as one doubled boom; a real recording, heavy, nothing bubbly or popping.
This one: two autocannon shots slowed to 0.85, 70 ms apart: a hard, cracking 'BA-DAM', 0.67 s.
Edit: 0.67 s, the loudest 100 ms band −22.2 dB, peak −11.4 dBFS, −22.6 LUFS.

### strider-cannon-r65-b — "Howitzer Artillery Gun Shot Sound Effect 01" by qubodup + "Howitzer Artillery Gun Shot Sound Effect 02" by qubodup

Proposed (round 65), cut from the HQ previews by `tools/concept/audio/sfx_r65.py`.
Sources: <https://freesound.org/people/qubodup/sounds/161343/> (Howitzer Artillery Gun Shot Sound Effect 01 by qubodup, CC0 1.0), <https://freesound.org/people/qubodup/sounds/161342/> (Howitzer Artillery Gun Shot Sound Effect 02 by qubodup, CC0 1.0).
Use: each fan of the Strider's twin cannons (3 shots 12° apart, every 2.4 s; easy 3.0 s, hard a fan of 5 every 2.0 s), after the torso's 0.3 s hold; the shoulder pod's homing salvo plays the shared homing launch.
Brief: a deep double gunshot: two heavy cannons firing as one doubled boom; a real recording, heavy, nothing bubbly or popping.
This one: two howitzer shots sped to 1.3, 80 ms apart: a deeper, boomier 'BOOM-BOOM' than a, less crack, 0.68 s.
Edit: 0.68 s, the loudest 100 ms band −22.3 dB, peak −12.0 dBFS, −21.1 LUFS.

### strider-snap-r65-a — "snd_metal_smash.wav" by gristi + "Power Spark" by qubodup

Proposed (round 65), cut from the HQ previews by `tools/concept/audio/sfx_r65.py`.
Sources: <https://freesound.org/people/gristi/sounds/562198/> (snd_metal_smash.wav by gristi, CC0 1.0), <https://freesound.org/people/qubodup/sounds/442670/> (Power Spark by qubodup, CC-BY 4.0).
Use: a Strider's leg destroyed (80 HP; it sparks at the knee and the stagger starts); the fall below follows at once.
Brief: a metal snap with sparks: a load-bearing strut breaking, dry and sharp; no pop; a real recording, heavy, nothing bubbly or popping.
This one: a metal smash with an electric power spark crackling under its tail: a bright 'KSHANG-tssk', 0.54 s.
Edit: 0.56 s, the loudest 100 ms band −23.5 dB, peak −10.6 dBFS, −20.1 LUFS.

### strider-snap-r65-b — "Breaking lock.aif" by MAJ061785 + "CircuitBreaker_02" by Wavewire

Proposed (round 65), cut from the HQ previews by `tools/concept/audio/sfx_r65.py`.
Sources: <https://freesound.org/people/MAJ061785/sounds/85533/> (Breaking lock.aif by MAJ061785, CC-BY 3.0), <https://freesound.org/people/Wavewire/sounds/855099/> (CircuitBreaker_02 by Wavewire, CC-BY 4.0).
Use: a Strider's leg destroyed (80 HP; it sparks at the knee and the stagger starts); the fall below follows at once.
Brief: a metal snap with sparks: a load-bearing strut breaking, dry and sharp; no pop; a real recording, heavy, nothing bubbly or popping.
This one: a lock breaking (slowed to 0.8) with a circuit breaker's arc snapping: a tighter, lower 'KRAK-zt' than a, 0.4 s.
Edit: 0.40 s, the loudest 100 ms band −21.2 dB, peak −4.0 dBFS, −inf LUFS.

### strider-fall-r65-a — "Metallic Groan" by hinchinbrook + "Big Structure Collision Heard from Inside" by Osvoldon + "Scrap metal dropping / crashing" by SamsterBirdies

Proposed (round 65), cut from the HQ previews by `tools/concept/audio/sfx_r65.py`.
Sources: <https://freesound.org/people/hinchinbrook/sounds/496836/> (Metallic Groan by hinchinbrook, CC0 1.0), <https://freesound.org/people/Osvoldon/sounds/653294/> (Big Structure Collision Heard from Inside by Osvoldon, CC0 1.0), <https://freesound.org/people/SamsterBirdies/sounds/587443/> (Scrap metal dropping / crashing by SamsterBirdies, CC0 1.0).
Use: a Strider's topple, played at the leg's loss: the 0.6 s stagger and the 0.8 s fall, the crash landing at 1.4 s with the crush (the units it crushes play their own rungs).
Brief: a long metal groan into a heavy crash and rumble: the hull tipping over and slamming down; its crash exactly 1.4 s in; a real recording, heavy, nothing bubbly or popping.
This one: a metallic groan slowed to 0.75 swelling for 1.4 s, then a massive structure's collision heard from inside with scrap metal crashing over it, rolling out low: 'grrrooaaan-KRRASH-rrrmm', 3.2 s.
Edit: 3.20 s, the loudest 100 ms band −20.0 dB, peak −6.7 dBFS, −20.5 LUFS.

### strider-fall-r65-b — "Door_Metal_Groans_Ext.wav" by Cell31_Sound_Productions + "Kicking_Dumpster.wav" by kmyers1316 + "Scrap metal dropping / crashing" by SamsterBirdies + "Quake Short.wav" by TheMinkman

Proposed (round 65), cut from the HQ previews by `tools/concept/audio/sfx_r65.py`.
Sources: <https://freesound.org/people/Cell31_Sound_Productions/sounds/264014/> (Door_Metal_Groans_Ext.wav by Cell31_Sound_Productions, CC-BY 4.0), <https://freesound.org/people/kmyers1316/sounds/344592/> (Kicking_Dumpster.wav by kmyers1316, CC0 1.0), <https://freesound.org/people/SamsterBirdies/sounds/587443/> (Scrap metal dropping / crashing by SamsterBirdies, CC0 1.0), <https://freesound.org/people/TheMinkman/sounds/199803/> (Quake Short.wav by TheMinkman, CC0 1.0).
Use: a Strider's topple, played at the leg's loss: the 0.6 s stagger and the 0.8 s fall, the crash landing at 1.4 s with the crush (the units it crushes play their own rungs).
Brief: a long metal groan into a heavy crash and rumble: the hull tipping over and slamming down; its crash exactly 1.4 s in; a real recording, heavy, nothing bubbly or popping.
This one: a heavy door's metal groan slowed to 0.8 for 1.4 s, then a kicked steel dumpster slowed to 0.5 with a scrap metal crash and an earthquake's rumble under it: a creakier groan and a longer, lower rumble than a, 3.25 s.
Edit: 3.25 s, the loudest 100 ms band −20.1 dB, peak −8.2 dBFS, −22.6 LUFS.

### door-grind-r65-a — "open rolling door 150328_0543 MS.wav" by klankbeeld + "HUTCH HD SFX 2014 0143" by leonelmail

Proposed (round 65), cut from the HQ previews by `tools/concept/audio/sfx_r65.py`.
Sources: <https://freesound.org/people/klankbeeld/sounds/315943/> (open rolling door 150328_0543 MS.wav by klankbeeld, CC-BY 4.0), <https://freesound.org/people/leonelmail/sounds/462819/> (HUTCH HD SFX 2014 0143 by leonelmail, CC0 1.0).
Use: a blast door's lock destroyed (Level 32: the two richer channels' caches and the hall's secret): the door pair slides open and the cache's crate floats out; three times a level at most.
Brief: a heavy door grinding open on rails, a deep clunk as it stops; a real recording, heavy, nothing bubbly or popping.
This one: an industrial rolling door opening, slowed to 0.85, then a deep hollow metal impact as it stops: a low rumbling 'rrrrrrRRR-DOMM', 2.05 s.
Edit: 2.05 s, the loudest 100 ms band −25.4 dB, peak −8.6 dBFS, −22.0 LUFS.

### door-grind-r65-b — "KA_Warehouse_Creaking_Door_Roll_Open_ST" by Kristoffer_Andersson + "Mechanical Slide (aiff)" by steaq

Proposed (round 65), cut from the HQ previews by `tools/concept/audio/sfx_r65.py`.
Sources: <https://freesound.org/people/Kristoffer_Andersson/sounds/851264/> (KA_Warehouse_Creaking_Door_Roll_Open_ST by Kristoffer_Andersson, CC0 1.0), <https://freesound.org/people/steaq/sounds/144315/> (Mechanical Slide (aiff) by steaq, CC0 1.0).
Use: a blast door's lock destroyed (Level 32: the two richer channels' caches and the hall's secret): the door pair slides open and the cache's crate floats out; three times a level at most.
Brief: a heavy door grinding open on rails, a deep clunk as it stops; a real recording, heavy, nothing bubbly or popping.
This one: a warehouse door creaking as it rolls open, slowed to 0.7, then a mechanical slide's thud: a screechier, more grinding 'skrrreeeek-THUNK' than a, 2.05 s.
Edit: 2.05 s, the loudest 100 ms band −25.0 dB, peak −7.6 dBFS, −22.2 LUFS.

### charge-blast-r65-a — "Quarry blasting.wav" by juskiddink + "Rocks hit.wav" by scarbelly25

Proposed (round 65), cut from the HQ previews by `tools/concept/audio/sfx_r65.py`.
Sources: <https://freesound.org/people/juskiddink/sounds/82682/> (Quarry blasting.wav by juskiddink, CC-BY 4.0), <https://freesound.org/people/scarbelly25/sounds/33940/> (Rocks hit.wav by scarbelly25, CC0 1.0).
Use: a mining charge shot (1 hit): its 64 px burst (Level 32: 3 in each richer channel, 2 by the hall's SAM pair, 4 on the refinery's gantry; later Level 34); a charge does not set off another.
Brief: a sharp quarry blast, the `medium` rung's weight with a rock crack in it; a real recording, heavy, nothing bubbly or popping.
This one: a quarry blast recorded at the quarry, with rocks striking rock on its front: a deep 'BOOM' with a stony 'krak' in its attack, 1.4 s.
Edit: 1.40 s, the loudest 100 ms band −18.3 dB, peak −3.3 dBFS, −18.3 LUFS.

### charge-blast-r65-b — "120527 05 Dynamite 1.WAV" by Evan Cruder + "Cracking Earthquake (cracking soil, cracking stone)" by uagadugu

Proposed (round 65), cut from the HQ previews by `tools/concept/audio/sfx_r65.py`.
Sources: <https://freesound.org/people/Evan Cruder/sounds/160797/> (120527 05 Dynamite 1.WAV by Evan Cruder, CC0 1.0), <https://freesound.org/people/uagadugu/sounds/222521/> (Cracking Earthquake (cracking soil, cracking stone) by uagadugu, CC0 1.0).
Use: a mining charge shot (1 hit): its 64 px burst (Level 32: 3 in each richer channel, 2 by the hall's SAM pair, 4 on the refinery's gantry; later Level 34); a charge does not set off another.
Brief: a sharp quarry blast, the `medium` rung's weight with a rock crack in it; a real recording, heavy, nothing bubbly or popping.
This one: a dynamite charge going off, with stone cracking under it: a sharper, drier blast than a with a longer crumbling crack, 1.4 s.
Edit: 1.40 s, the loudest 100 ms band −18.2 dB, peak −3.0 dBFS, −16.5 LUFS.

## Concept round 66, step AUD-F (Level 33: the Void Leech's latch, drain whine, latched hum, shake-off and death, the generator's brown-out warning, the Rail Bunker's death)

Generator: `tools/concept/audio/sfx_r66.py` (cut from the Freesound HQ previews; the brown-out warning synthesized, the warnings' family). Not proposed again: the brown-out hum (round 60 S.10 a), the rail line's charge and shot (round 60 S.7 b, S.8 b), the hex shield's set (round 60), the open-space bed (round 60 S.18 a), the Talon (round 62). Not listened to by Claude: checked by level, peaks, onsets, spectra and the loop seams. Passed over (read in their descriptions): designed or synthesized "power down", "electro bomb" and sci-fi buzz sounds of the searches, wet insect crushes (the leech is dry), the transmission-line buzz and the pylon hum (already the Lamprey's drain, round 46).

### leech-latch-r66-a — "staple_gun_02.wav" by movingplaid + "Leg vise - Clang" by ldezem

Proposed (round 66), cut from the HQ previews by `tools/concept/audio/sfx_r66.py`.
Sources: <https://freesound.org/people/movingplaid/sounds/88564/> (staple_gun_02.wav by movingplaid, CC-BY 4.0), <https://freesound.org/people/ldezem/sounds/386196/> (Leg vise - Clang by ldezem, CC0 1.0).
Use: a Void Leech latching on the ship's hull (the left, right or tail spot; at most 3 latched, 2 on easy; Level 33's rear and side streams), panned to its spot; a leech latching on a prey Talon plays it quieter.
Brief: a dry clamp: a heavy staple gun or a ratchet biting on metal; not the Lamprey's wet latch (concept/lamprey-latch-r34-a.ogg, a suck and bite): this one is dry and mechanical; a real recording, heavy, nothing bubbly or popping.
This one: a heavy staple gun fired into metal, slowed to 0.75, a vise's clang faint under it: one hard dry 'TCHAK' with a short ring, 0.4 s.
Edit: 0.41 s, the loudest 100 ms band −24.2 dB, peak −8.9 dBFS, −22.6 LUFS.

### leech-latch-r66-b — "ratchet socket wrench tool" by AlaskaRobotics + "Chambering 1" by magnuswaker

Proposed (round 66), cut from the HQ previews by `tools/concept/audio/sfx_r66.py`.
Sources: <https://freesound.org/people/AlaskaRobotics/sounds/551497/> (ratchet socket wrench tool by AlaskaRobotics, CC0 1.0), <https://freesound.org/people/magnuswaker/sounds/523058/> (Chambering 1 by magnuswaker, CC0 1.0).
Use: a Void Leech latching on the ship's hull (the left, right or tail spot; at most 3 latched, 2 on easy; Level 33's rear and side streams), panned to its spot; a leech latching on a prey Talon plays it quieter.
Brief: a dry clamp: a heavy staple gun or a ratchet biting on metal; not the Lamprey's wet latch (concept/lamprey-latch-r34-a.ogg, a suck and bite): this one is dry and mechanical; a real recording, heavy, nothing bubbly or popping.
This one: a socket wrench's ratchet slowed to 0.65 biting, then a rifle bolt's clack locking it: a two-stage 'krrrk-CHAK', busier and longer than a, 0.55 s.
Edit: 0.54 s, the loudest 100 ms band −26.9 dB, peak −11.7 dBFS, −22.1 LUFS.

### leech-drain-r66-a — "Angle grinder - Milwaukee 125mm - Stop 1 time" by ldezem

Proposed (round 66), cut from the HQ previews by `tools/concept/audio/sfx_r66.py`.
Source: <https://freesound.org/people/ldezem/sounds/386084/> (Angle grinder - Milwaukee 125mm - Stop 1 time by ldezem, CC0 1.0).
Use: each leech starting to drain (with its latch: the generator's output −2 MW, stacking to −6): once per latch, from 0.1 s after the clamp; the latched hum below carries the drain after it.
Brief: a falling electrical whine: a motor spinning down, the power being pulled out of the ship; a real recording, heavy, nothing bubbly or popping.
This one: an angle grinder spinning down after its trigger is let go, glided from its own speed to 0.8: a gritty high whine falling 'vwiiiooow', 1.1 s.
Edit: 1.10 s, the loudest 100 ms band −29.7 dB, peak −15.3 dBFS, −26.3 LUFS.

### leech-drain-r66-b — "Electric motor engine start stop" by kentspublicdomain

Proposed (round 66), cut from the HQ previews by `tools/concept/audio/sfx_r66.py`.
Source: <https://freesound.org/people/kentspublicdomain/sounds/476943/> (Electric motor engine start stop by kentspublicdomain, CC0 1.0).
Use: each leech starting to drain (with its latch: the generator's output −2 MW, stacking to −6): once per latch, from 0.1 s after the clamp; the latched hum below carries the drain after it.
Brief: a falling electrical whine: a motor spinning down, the power being pulled out of the ship; a real recording, heavy, nothing bubbly or popping.
This one: an electric motor stopping, sped from 1.4 to its own speed: a lower, smoother hum falling away 'mmmmooowm' than a, 1.0 s.
Edit: 1.00 s, the loudest 100 ms band −28.0 dB, peak −17.8 dBFS, −27.7 LUFS.

### leech-hum-r66-a — "Power Buzz SFX loop" by unfa

Proposed (round 66), cut from the HQ previews by `tools/concept/audio/sfx_r66.py`.
Source: <https://freesound.org/people/unfa/sounds/222676/> (Power Buzz SFX loop by unfa, CC0 1.0).
Use: one loop while any leech is latched on the ship (not on a Talon), its level stepping with the latched count (the file is the three-leech level: one leech −6 dB, two −3 dB); under the brown-out hum when the load passes the output (concept/brownout-hum-r60-a.ogg, a transformer's sag), cut at once when the last leech is shaken off, killed or thrown off by the EMP.
Brief: a low electrical hum, dry, the ship's power being sucked out; distinct from the brown-out hum and the Lamprey's drain (concept/lamprey-drain-r46-b.ogg, a Tesla coil's hum); a real recording, heavy, nothing bubbly or popping.
This one: a mains power buzz (its 50 Hz fundamental filtered out, its raw harmonics from 150 Hz up kept), throbbing once a second with the leech's sac (dipping 5 dB between the beats): 'bzzZZ bzzZZ', 4.0 s loop.
Edit: 4.00 s, band RMS −38.1 dB, peak −22.4 dBFS, −27.9 LUFS.

### leech-hum-r66-b — "Foley_Mechanism_Light_Buzz_Short_Loop_Mono_DR05.wav" by Nox_Sound + "Hard-drive spin-up/spin-down" by SamsterBirdies

Proposed (round 66), cut from the HQ previews by `tools/concept/audio/sfx_r66.py`.
Sources: <https://freesound.org/people/Nox_Sound/sounds/553075/> (Foley_Mechanism_Light_Buzz_Short_Loop_Mono_DR05.wav by Nox_Sound, CC0 1.0), <https://freesound.org/people/SamsterBirdies/sounds/567609/> (Hard-drive spin-up/spin-down by SamsterBirdies, CC0 1.0).
Use: one loop while any leech is latched on the ship (not on a Talon), its level stepping with the latched count (the file is the three-leech level: one leech −6 dB, two −3 dB); under the brown-out hum when the load passes the output (concept/brownout-hum-r60-a.ogg, a transformer's sag), cut at once when the last leech is shaken off, killed or thrown off by the EMP.
Brief: a low electrical hum, dry, the ship's power being sucked out; distinct from the brown-out hum and the Lamprey's drain (concept/lamprey-drain-r46-b.ogg, a Tesla coil's hum); a real recording, heavy, nothing bubbly or popping.
This one: a light fitting's buzz slowed to 0.8 over a hard drive's 120 Hz spin hum: a steady, thinner and higher 'zzzzz' than a, no throb, 4.0 s loop.
Edit: 4.00 s, band RMS −33.0 dB, peak −20.4 dBFS, −29.6 LUFS.

### leech-shake-r66-a — "metal plate scratch and hit" by Ridderick + "scratching metal with fingernails 01" by rubberduck9999

Proposed (round 66), cut from the HQ previews by `tools/concept/audio/sfx_r66.py`.
Sources: <https://freesound.org/people/Ridderick/sounds/707638/> (metal plate scratch and hit by Ridderick, CC0 1.0), <https://freesound.org/people/rubberduck9999/sounds/672951/> (scratching metal with fingernails 01 by rubberduck9999, CC0 1.0).
Use: the shake-off (four reversals in 1.2 s) throwing every latched leech off at once, and the EMP throwing them off: once per throw, however many leeches.
Brief: a scrape of claws on metal: the leeches torn off the hull; not the Lamprey's wet slap (concept/lamprey-shake-r34-a.ogg); a real recording, heavy, nothing bubbly or popping.
This one: a metal plate knocked, then fingernails dragged across metal, slowed to 0.7: a 'tok' into a raspy claw screech 'skrrrriik', 0.75 s.
Edit: 0.74 s, the loudest 100 ms band −27.1 dB, peak −10.8 dBFS, −22.3 LUFS.

### leech-shake-r66-b — "metal plate scratch" by Ridderick

Proposed (round 66), cut from the HQ previews by `tools/concept/audio/sfx_r66.py`.
Source: <https://freesound.org/people/Ridderick/sounds/707639/> (metal plate scratch by Ridderick, CC0 1.0).
Use: the shake-off (four reversals in 1.2 s) throwing every latched leech off at once, and the EMP throwing them off: once per throw, however many leeches.
Brief: a scrape of claws on metal: the leeches torn off the hull; not the Lamprey's wet slap (concept/lamprey-shake-r34-a.ogg); a real recording, heavy, nothing bubbly or popping.
This one: a metal plate scraped hard, slowed to 0.85: a lower, grittier grinding scrape 'shhkrrrk' than a, no knock, 0.7 s.
Edit: 0.70 s, the loudest 100 ms band −24.2 dB, peak −12.1 dBFS, −22.3 LUFS.

### leech-death-r66-a — "crack nut.wav" by gusgus26 + "electricspark.wav" by knova

Proposed (round 66), cut from the HQ previews by `tools/concept/audio/sfx_r66.py`.
Sources: <https://freesound.org/people/gusgus26/sounds/121193/> (crack nut.wav by gusgus26, CC0 1.0), <https://freesound.org/people/knova/sounds/169666/> (electricspark.wav by knova, CC-BY 4.0).
Use: a Void Leech killed (free, latched on the ship or on a Talon), over its `small` death: up to a stream of 4-6 in a few seconds.
Brief: a dry chitin crack with a violet spark: a shell cracking, short; no splat, no pop; a real recording, heavy, nothing bubbly or popping.
This one: a nut cracking, slowed to 0.5, a short electric spark fizzing after it: a hard 'KRAK-tsss', 0.5 s.
Edit: 0.51 s, the loudest 100 ms band −27.8 dB, peak −5.2 dBFS, −24.0 LUFS.

### leech-death-r66-b — "Bone crack" by shtran + "shell crunch - wide 05.wav" by Anthousai + "Electric Sparks" by mikiko850

Proposed (round 66), cut from the HQ previews by `tools/concept/audio/sfx_r66.py`.
Sources: <https://freesound.org/people/shtran/sounds/512049/> (Bone crack by shtran, CC0 1.0), <https://freesound.org/people/Anthousai/sounds/336625/> (shell crunch - wide 05.wav by Anthousai, CC0 1.0), <https://freesound.org/people/mikiko850/sounds/849772/> (Electric Sparks by mikiko850, CC-BY 4.0).
Use: a Void Leech killed (free, latched on the ship or on a Talon), over its `small` death: up to a stream of 4-6 in a few seconds.
Brief: a dry chitin crack with a violet spark: a shell cracking, short; no splat, no pop; a real recording, heavy, nothing bubbly or popping.
This one: a bone cracking, slowed to 0.8, with a shell crunch on it and a quick spark snap: a duller, crunchier 'KRRCH-tk' than a, 0.45 s.
Edit: 0.42 s, the loudest 100 ms band −31.2 dB, peak −5.1 dBFS, −22.6 LUFS.

### brownout-warn-r66-a — synthesized

Proposed (round 66), synthesized by `tools/concept/audio/sfx_r66.py` (no third-party source).
Use: the ship's load first passing the drained output (a second latched leech on the hand plan; once per drop below the load, not again while it stays below); the brown-out hum loop (concept/brownout-hum-r60-a.ogg) starts with it.
Brief: a short falling two-note warning tone (synthesized: the warnings' family, as the edge warning's ping, concept/ui-edge-warning-r11-b.ogg); uneasy, not an alarm.
This one: two pings a minor third down (1.6 kHz, then 1.35 kHz, 0.17 s apart), the edge warning's own ping: a light 'dee-doo', 0.5 s.
Edit: 0.55 s, the loudest 100 ms band −25.0 dB, peak −17.9 dBFS, −24.4 LUFS.

### brownout-warn-r66-b — synthesized

Proposed (round 66), synthesized by `tools/concept/audio/sfx_r66.py` (no third-party source).
Use: the ship's load first passing the drained output (a second latched leech on the hand plan; once per drop below the load, not again while it stays below); the brown-out hum loop (concept/brownout-hum-r60-a.ogg) starts with it.
Brief: a short falling two-note warning tone (synthesized: the warnings' family, as the edge warning's ping, concept/ui-edge-warning-r11-b.ogg); uneasy, not an alarm.
This one: a beep and one a fifth lower (1.2 kHz, then 800 Hz sagging 8 % as it fades): a heavier, drooping 'beep-bowww' than a, 0.5 s.
Edit: 0.55 s, the loudest 100 ms band −25.0 dB, peak −18.9 dBFS, −24.7 LUFS.

### bunker-death-r66-a — "Guns & Explosions Album - Metal impact 5.wav" by OGsoundFX + "G32-10-Massive Electrical Discharges.wav" by craigsmith

Proposed (round 66), cut from the HQ previews by `tools/concept/audio/sfx_r66.py`.
Sources: <https://freesound.org/people/OGsoundFX/sounds/423117/> (Guns & Explosions Album - Metal impact 5.wav by OGsoundFX, CC-BY 4.0), <https://freesound.org/people/craigsmith/sounds/438494/> (G32-10-Massive Electrical Discharges.wav by craigsmith, CC0 1.0).
Use: a Rail Bunker destroyed (hardened: only an `anti-ground` weapon, a special, a mining charge or a Strider's fall kills one; Level 33's asteroid faces, later Level 35's hull), over its `medium` ground explosion; its telegraph, if running, is cancelled.
Brief: a heavy metal explosion: the turret wrecked, its rail coil shorting out; a real recording, heavy, nothing bubbly or popping.
This one: a heavy metal impact, a massive electrical discharge buzzing out under its tail (the coil shorting): 'KLANG-bzzzrt', 1.0 s.
Edit: 1.00 s, the loudest 100 ms band −20.1 dB, peak −5.4 dBFS, −19.0 LUFS.

### bunker-death-r66-b — "Loud Bang Crashing Metal Chunk Dropped in Metal Dumpster" by qubodup + "Electric Sparks" by mikiko850

Proposed (round 66), cut from the HQ previews by `tools/concept/audio/sfx_r66.py`.
Sources: <https://freesound.org/people/qubodup/sounds/859154/> (Loud Bang Crashing Metal Chunk Dropped in Metal Dumpster by qubodup, CC0 1.0), <https://freesound.org/people/mikiko850/sounds/849772/> (Electric Sparks by mikiko850, CC-BY 4.0).
Use: a Rail Bunker destroyed (hardened: only an `anti-ground` weapon, a special, a mining charge or a Strider's fall kills one; Level 33's asteroid faces, later Level 35's hull), over its `medium` ground explosion; its telegraph, if running, is cancelled.
Brief: a heavy metal explosion: the turret wrecked, its rail coil shorting out; a real recording, heavy, nothing bubbly or popping.
This one: a metal chunk crashing into a steel dumpster, sparks spitting twice after it: a deeper, rattling 'BLAMM ... tsk tsk' than a, 1.2 s.
Edit: 1.20 s, the loudest 100 ms band −20.0 dB, peak −7.6 dBFS, −25.0 LUFS.

## Concept round 68, step AUD-H2 (Level 35: the Iron Sovereign's spin-up, inner-ring step and hold, ring lock, klaxon, core shutters, `fan` and `spiral` guns, cook-offs and implosion; the mass relay's charge and flash)

Generator: `tools/concept/audio/sfx_r68.py` (cut from the Freesound HQ previews; the klaxons played into a 300 m steel tunnel's recorded impulse response; nothing synthesized). Not proposed again: the ring bed (round 60 S.16 a), the rail line's charge and shot (round 60 S.7 b, S.8 b: the batteries' and the core rail's; the mass relay is their long version, made here from their own recordings), the hex shield's set and the homing launch, run and kill (round 60), the Sentinel's sweep (round 65: the laser batteries), the Dart's launch (round 63: the rim bays), the `huge` rung. The inner ring's step is split in two files (the unlock with its 0.4 s tell, the hold's clunk) because the turn's length changes with the phase (4.0 / 3.2 / 2.7 s) and the difficulty. Not listened to by Claude: checked by level, peaks and onsets. Passed over (read in their descriptions): synthesized alarms ("FmKlaxonReverberant", "Industrial Alarm" made in a DAW, "horn 01/02"), designed implosions and shockwaves ("implosion_near", "Shockwave"), a 1925 car klaxon (comic).

### sovereign-spinup-r68-a — "Rail Grinding Machine with Rythmic Clang Berlin Night" by giuliorasi + "big_metal_unlock_and_slam.wav" by simpsi

Proposed (round 68), cut from the HQ previews by `tools/concept/audio/sfx_r68.py`.
Sources: <https://freesound.org/people/giuliorasi/sounds/812360/> (Rail Grinding Machine with Rythmic Clang Berlin Night by giuliorasi, CC0 1.0), <https://freesound.org/people/simpsi/sounds/175663/> (big_metal_unlock_and_slam.wav by simpsi, CC-BY 3.0).
Use: the boss's ≈ 8 s intro: each ring starting to turn as its lights come on, outer → middle → inner (three times, a few seconds apart), panned to nothing (the station fills the field); the ring bed (concept/sovereign-ring-turn-r60-a.ogg) takes over once the rings reach speed.
Brief: a huge bearing grinding up to speed: a crane's slewing ring or a turntable, pitched down, a rising rumble; a real recording, heavy, nothing bubbly or popping.
This one: the ring bed's own recording (a rail-grinding machine with its rhythmic clang) glided from 0.15 up to the bed's 0.5 and swelling, a big latch's clank as it lets go: 'KLANK ... grrrrRRRR', the clangs speeding up into the bed's ticks, 2.5 s.
Edit: 2.50 s, the loudest 100 ms band −26.1 dB, peak −8.7 dBFS, −25.4 LUFS.

### sovereign-spinup-r68-b — "R03-30-Large Factory Machines Starts, Works, Stops.wav" by craigsmith

Proposed (round 68), cut from the HQ previews by `tools/concept/audio/sfx_r68.py`.
Source: <https://freesound.org/people/craigsmith/sounds/482740/> (R03-30-Large Factory Machines Starts, Works, Stops.wav by craigsmith, CC0 1.0).
Use: the boss's ≈ 8 s intro: each ring starting to turn as its lights come on, outer → middle → inner (three times, a few seconds apart), panned to nothing (the station fills the field); the ring bed (concept/sovereign-ring-turn-r60-a.ogg) takes over once the rings reach speed.
Brief: a huge bearing grinding up to speed: a crane's slewing ring or a turntable, pitched down, a rising rumble; a real recording, heavy, nothing bubbly or popping.
This one: a big factory machine starting up, an octave down rising to near its own pitch: a rougher, airier motor whine winding up 'vvvoooOOOMM' than a, no clangs, 2.5 s.
Edit: 2.50 s, the loudest 100 ms band −26.1 dB, peak −9.0 dBFS, −23.8 LUFS.

### sovereign-step-r68-a — "big_metal_unlock_and_slam.wav" by simpsi + "ratchet_winching_and_turning_sound_slow_speed" by Artninja

Proposed (round 68), cut from the HQ previews by `tools/concept/audio/sfx_r68.py`.
Sources: <https://freesound.org/people/simpsi/sounds/175663/> (big_metal_unlock_and_slam.wav by simpsi, CC-BY 3.0), <https://freesound.org/people/Artninja/sounds/755384/> (ratchet_winching_and_turning_sound_slow_speed by Artninja, CC-BY 4.0).
Use: the inner ring unlocking for each 120° step (every 8 s: 4 s turning, 4 s holding; phase 2 3.2 s, phase 3 2.7 s turning): with the 0.4 s tell (its gold teeth flash), the turn's first clicks under it; the ring bed carries the turn, the hold's clunk below ends it.
Brief: a heavy ratchet clank, then the grind starting: a giant pawl lifting off a toothed rim; a real recording, heavy, nothing bubbly or popping.
This one: a big metal latch unlocked, slowed to 0.75, then a winch's ratchet turning, slowed to 0.55: 'KLA-KLANK, tak-tak-tak', 1.1 s.
Edit: 1.10 s, the loudest 100 ms band −24.2 dB, peak −9.0 dBFS, −24.4 LUFS.

### sovereign-step-r68-b — "door metal big heavy close kinda slam thud echo offmic.wav" by kyles + "Ratchet - Stahlwille - Tighten 1" by ldezem

Proposed (round 68), cut from the HQ previews by `tools/concept/audio/sfx_r68.py`.
Sources: <https://freesound.org/people/kyles/sounds/406197/> (door metal big heavy close kinda slam thud echo offmic.wav by kyles, CC0 1.0), <https://freesound.org/people/ldezem/sounds/386206/> (Ratchet - Stahlwille - Tighten 1 by ldezem, CC0 1.0).
Use: the inner ring unlocking for each 120° step (every 8 s: 4 s turning, 4 s holding; phase 2 3.2 s, phase 3 2.7 s turning): with the 0.4 s tell (its gold teeth flash), the turn's first clicks under it; the ring bed carries the turn, the hold's clunk below ends it.
Brief: a heavy ratchet clank, then the grind starting: a giant pawl lifting off a toothed rim; a real recording, heavy, nothing bubbly or popping.
This one: a heavy steel door's slam, slowed to 0.8, then a socket ratchet tightening slowed to 0.45 (each click a big clack): a deeper 'DOMM, krrk-krrk-krrk' than a, 1.0 s.
Edit: 1.02 s, the loudest 100 ms band −24.2 dB, peak −6.8 dBFS, −23.0 LUFS.

### sovereign-hold-r68-a — "big_metal_unlock_and_slam.wav" by simpsi

Proposed (round 68), cut from the HQ previews by `tools/concept/audio/sfx_r68.py`.
Source: <https://freesound.org/people/simpsi/sounds/175663/> (big_metal_unlock_and_slam.wav by simpsi, CC-BY 3.0).
Use: the inner ring locking into its hold at the end of each 120° step (then 4 s still while its laser batteries sweep).
Brief: a heavy clunk: the giant pawl dropping back into the rim; a real recording, heavy, nothing bubbly or popping.
This one: a big metal bolt slammed home, slowed to 0.85: a hard ringing 'KLANNG', 1.0 s.
Edit: 1.00 s, the loudest 100 ms band −22.2 dB, peak −5.1 dBFS, −17.6 LUFS.

### sovereign-hold-r68-b — "Closing large metal door-001" by AudioPapkin

Proposed (round 68), cut from the HQ previews by `tools/concept/audio/sfx_r68.py`.
Source: <https://freesound.org/people/AudioPapkin/sounds/814976/> (Closing large metal door-001 by AudioPapkin, CC0 1.0).
Use: the inner ring locking into its hold at the end of each 120° step (then 4 s still while its laser batteries sweep).
Brief: a heavy clunk: the giant pawl dropping back into the rim; a real recording, heavy, nothing bubbly or popping.
This one: a large metal door closing, slowed to 0.75: a deeper, duller 'DOOMF' than a, less ring, 1.0 s.
Edit: 1.00 s, the loudest 100 ms band −22.0 dB, peak −4.1 dBFS, −18.2 LUFS.

### sovereign-lock-r68-a — "Train Brakes" by KodyTron + "Rail Grinding Machine with Rythmic Clang Berlin Night" by giuliorasi + "Metallic Groan" by hinchinbrook + "Electric Sparks" by mikiko850 + "door metal big heavy close kinda slam thud echo offmic.wav" by kyles

Proposed (round 68), cut from the HQ previews by `tools/concept/audio/sfx_r68.py`.
Sources: <https://freesound.org/people/KodyTron/sounds/640998/> (Train Brakes by KodyTron, CC-BY 4.0), <https://freesound.org/people/giuliorasi/sounds/812360/> (Rail Grinding Machine with Rythmic Clang Berlin Night by giuliorasi, CC0 1.0), <https://freesound.org/people/hinchinbrook/sounds/496836/> (Metallic Groan by hinchinbrook, CC0 1.0), <https://freesound.org/people/mikiko850/sounds/849772/> (Electric Sparks by mikiko850, CC-BY 4.0), <https://freesound.org/people/kyles/sounds/406197/> (door metal big heavy close kinda slam thud echo offmic.wav by kyles, CC0 1.0).
Use: a ring's last battery killed (three times a fight, in any order; a `boss-phase`): the ring grinds to a halt in 2 s, sparks, its lights going out, then stays still; the bed goes on for the rings still turning.
Brief: a long metal brake screech and a groan as the ring grinds to a halt, sparks crackling; the stop at 2 s; a real recording, heavy, nothing bubbly or popping.
This one: a train's brakes screeching, glided down from its own pitch to 0.6 (slowing), over the ring bed's own grind winding down from 0.5 to 0.12, a metal groan and sparks crackling, a heavy door's thud as it stops at 2.0 s: 'SKREEEEeeeoww ... DOMM', 2.6 s.
Edit: 2.60 s, the loudest 100 ms band −21.7 dB, peak −3.3 dBFS, −16.0 LUFS.

### sovereign-lock-r68-b — "Metallic Screech.wav" by astronaut_13579 + "Bending Metal" by NachtmahrTV + "electricspark.wav" by knova + "Closing large metal door-001" by AudioPapkin

Proposed (round 68), cut from the HQ previews by `tools/concept/audio/sfx_r68.py`.
Sources: <https://freesound.org/people/astronaut_13579/sounds/117081/> (Metallic Screech.wav by astronaut_13579, CC-BY 3.0), <https://freesound.org/people/NachtmahrTV/sounds/556714/> (Bending Metal by NachtmahrTV, CC0 1.0), <https://freesound.org/people/knova/sounds/169666/> (electricspark.wav by knova, CC-BY 4.0), <https://freesound.org/people/AudioPapkin/sounds/814976/> (Closing large metal door-001 by AudioPapkin, CC0 1.0).
Use: a ring's last battery killed (three times a fight, in any order; a `boss-phase`): the ring grinds to a halt in 2 s, sparks, its lights going out, then stays still; the bed goes on for the rings still turning.
Brief: a long metal brake screech and a groan as the ring grinds to a halt, sparks crackling; the stop at 2 s; a real recording, heavy, nothing bubbly or popping.
This one: a metal screech glided down from 0.85 to 0.5 (two of its strokes run together), over bending metal slowed to 0.6 (a deep creaking groan), two spark bursts, a large door's closing thud at 2.0 s: a harsher, more grinding 'KRRIIIIIK' with a lower groan than a, 2.75 s.
Edit: 2.75 s, the loudest 100 ms band −21.0 dB, peak −12.1 dBFS, −21.2 LUFS.

### sovereign-klaxon-r68-a — "Klaxon on axis Long.wav" by jameswrowles + "Balloon Pop, 300m Long Steel Tunnel (192khz IR Sample / Impulse Response)" by modusmogulus

Proposed (round 68), cut from the HQ previews by `tools/concept/audio/sfx_r68.py`.
Sources: <https://freesound.org/people/jameswrowles/sounds/514981/> (Klaxon on axis Long.wav by jameswrowles, CC0 1.0), <https://freesound.org/people/modusmogulus/sounds/846029/> (Balloon Pop, 300m Long Steel Tunnel (192khz IR Sample / Impulse Response) by modusmogulus, CC0 1.0).
Use: twice a fight: the intro (as the rings spin up, from the core) and the core's opening (3 s while the petals part and the core is invulnerable); over the music (no change at the opening: the klaxon and Vorne's line carry it).
Brief: an industrial or ship's alarm horn, reverberant, 3 s: a real klaxon, the warnings' family; distinct from Level 29's station klaxon (concept/station-klaxon-r62-a.ogg, a fire station's diaphone) and the tug's buzzer (concept/tug-alarm-r64-a.ogg); a real recording, heavy, nothing bubbly or popping.
This one: an industrial hooter (a mains-driven klaxon horn) slowed to 0.85, three 0.8 s blasts a second apart, played into a 300 m steel tunnel: a harsh buzzing 'BRAAAP BRAAAP BRAAAP' ringing on between the blasts, 3.4 s.
Edit: 3.40 s, the loudest 100 ms band −22.0 dB, peak −11.8 dBFS, −20.1 LUFS.

### sovereign-klaxon-r68-b — "Submarie dive horn_better.wav" by CGEffex + "Balloon Pop, 300m Long Steel Tunnel (192khz IR Sample / Impulse Response)" by modusmogulus

Proposed (round 68), cut from the HQ previews by `tools/concept/audio/sfx_r68.py`.
Sources: <https://freesound.org/people/CGEffex/sounds/104882/> (Submarie dive horn_better.wav by CGEffex, CC-BY 4.0), <https://freesound.org/people/modusmogulus/sounds/846029/> (Balloon Pop, 300m Long Steel Tunnel (192khz IR Sample / Impulse Response) by modusmogulus, CC0 1.0).
Use: twice a fight: the intro (as the rings spin up, from the core) and the core's opening (3 s while the petals part and the core is invulnerable); over the music (no change at the opening: the klaxon and Vorne's line carry it).
Brief: an industrial or ship's alarm horn, reverberant, 3 s: a real klaxon, the warnings' family; distinct from Level 29's station klaxon (concept/station-klaxon-r62-a.ogg, a fire station's diaphone) and the tug's buzzer (concept/tug-alarm-r64-a.ogg); a real recording, heavy, nothing bubbly or popping.
This one: a submarine's dive alarm (an archive recording), three of its rising-and-falling 'aaOOO-gah' cycles, in the same steel tunnel: lower and more of a ship's horn than a, 3.3 s.
Edit: 3.30 s, the loudest 100 ms band −22.0 dB, peak −11.6 dBFS, −23.6 LUFS.

### sovereign-shutters-r68-a — "Air (or steam) pressure release" by brunoboselli + "Large hydraulic door" by nooly + "Closing large metal door-001" by AudioPapkin + "door metal big heavy close kinda slam thud echo offmic.wav" by kyles

Proposed (round 68), cut from the HQ previews by `tools/concept/audio/sfx_r68.py`.
Sources: <https://freesound.org/people/brunoboselli/sounds/457294/> (Air (or steam) pressure release by brunoboselli, CC0 1.0), <https://freesound.org/people/nooly/sounds/331357/> (Large hydraulic door by nooly, CC-BY 3.0), <https://freesound.org/people/AudioPapkin/sounds/814976/> (Closing large metal door-001 by AudioPapkin, CC0 1.0), <https://freesound.org/people/kyles/sounds/406197/> (door metal big heavy close kinda slam thud echo offmic.wav by kyles, CC0 1.0).
Use: the core's opening (phase 4): its six armoured petals part over 3 s (with the klaxon); the thud is the petals reaching open at ≈ 2.9 s.
Brief: massive hydraulic doors: a hiss and a deep sliding thud (six petals moving together); a real recording, heavy, nothing bubbly or popping.
This one: an air pressure release's hiss, a large hypermarket door's hydraulic run slowed to 0.6, then a large metal door's closing thud and a steel door's slam as they reach open: 'PSSSHHH-rrrrrrrr-DOMM', 3.65 s.
Edit: 3.65 s, the loudest 100 ms band −22.2 dB, peak −3.5 dBFS, −20.2 LUFS.

### sovereign-shutters-r68-b — "Hydraulic Hiss, TTC bus door" by OttisG + "Hydraulic Door" by Autobot8299

Proposed (round 68), cut from the HQ previews by `tools/concept/audio/sfx_r68.py`.
Sources: <https://freesound.org/people/OttisG/sounds/852991/> (Hydraulic Hiss, TTC bus door by OttisG, CC-BY 4.0), <https://freesound.org/people/Autobot8299/sounds/841192/> (Hydraulic Door by Autobot8299, CC0 1.0).
Use: the core's opening (phase 4): its six armoured petals part over 3 s (with the klaxon); the thud is the petals reaching open at ≈ 2.9 s.
Brief: massive hydraulic doors: a hiss and a deep sliding thud (six petals moving together); a real recording, heavy, nothing bubbly or popping.
This one: a bus door's hydraulic hiss slowed to 0.75 over a hydraulic door's run slowed to 0.7 and its own heavy stop: a longer hiss and a lower, rumbling slide 'hsssss-mmmmmm-THUMM' than a, 3.75 s.
Edit: 3.75 s, the loudest 100 ms band −22.2 dB, peak −5.4 dBFS, −21.1 LUFS.

### sovereign-fan-r68-a — "Autocannon Three Shot Burst" by qubodup

Proposed (round 68), cut from the HQ previews by `tools/concept/audio/sfx_r68.py`.
Source: <https://freesound.org/people/qubodup/sounds/854186/> (Autocannon Three Shot Burst by qubodup, CC0 1.0).
Use: the shut core's aimed `fan` of 5 through its shutter slits (phases 1–3, every 3.0 / 2.5 / 2.0 s, after a 0.5 s gold glow).
Brief: a hard mechanical gun burst: an autocannon; a real recording, heavy, nothing bubbly or popping.
This one: an autocannon's three-shot burst slowed to 0.9: 'BAM-BAM-BAM' 0.3 s apart, 0.9 s.
Edit: 0.90 s, the loudest 100 ms band −20.0 dB, peak −5.0 dBFS, −19.6 LUFS.

### sovereign-fan-r68-b — "Air Defense Artillery Avenger Training .50cal (tH4YBTw8vk8).flac" by qubodup

Proposed (round 68), cut from the HQ previews by `tools/concept/audio/sfx_r68.py`.
Source: <https://freesound.org/people/qubodup/sounds/160355/> (Air Defense Artillery Avenger Training .50cal (tH4YBTw8vk8).flac by qubodup, CC0 1.0).
Use: the shut core's aimed `fan` of 5 through its shutter slits (phases 1–3, every 3.0 / 2.5 / 2.0 s, after a 0.5 s gold glow).
Brief: a hard mechanical gun burst: an autocannon; a real recording, heavy, nothing bubbly or popping.
This one: a .50 calibre air-defence gun's burst slowed to 0.85: five quicker, tighter shots 'BRRRAP', 0.75 s.
Edit: 0.75 s, the loudest 100 ms band −20.0 dB, peak −3.4 dBFS, −18.1 LUFS.

### sovereign-spiral-r68-a — "Autocannon Shot 1" by qubodup

Proposed (round 68), cut from the HQ previews by `tools/concept/audio/sfx_r68.py`.
Source: <https://freesound.org/people/qubodup/sounds/854183/> (Autocannon Shot 1 by qubodup, CC0 1.0).
Use: the open core's 2-arm `spiral` (phase 4: 5 bullets a second an arm, 4 s on, 2 s rest; 3 arms on hard): one shot per bullet, throttled in the game.
Brief: the `fan`'s gun, lighter: a short single shot that can repeat without drowning the fight; a real recording, heavy, nothing bubbly or popping.
This one: one autocannon shot sped to 1.25: a short hard 'tak', 0.2 s.
Edit: 0.20 s, the loudest 100 ms band −28.1 dB, peak −13.6 dBFS, −inf LUFS.

### sovereign-spiral-r68-b — "Air Defense Artillery Avenger Training .50cal (tH4YBTw8vk8).flac" by qubodup

Proposed (round 68), cut from the HQ previews by `tools/concept/audio/sfx_r68.py`.
Source: <https://freesound.org/people/qubodup/sounds/160355/> (Air Defense Artillery Avenger Training .50cal (tH4YBTw8vk8).flac by qubodup, CC0 1.0).
Use: the open core's 2-arm `spiral` (phase 4: 5 bullets a second an arm, 4 s on, 2 s rest; 3 arms on hard): one shot per bullet, throttled in the game.
Brief: the `fan`'s gun, lighter: a short single shot that can repeat without drowning the fight; a real recording, heavy, nothing bubbly or popping.
This one: one shot of the .50 calibre's burst sped to 1.1: a thinner, drier 'tk' than a, 0.15 s.
Edit: 0.17 s, the loudest 100 ms band −28.1 dB, peak −9.4 dBFS, −inf LUFS.

### sovereign-cookoff-r68-a — "Sharp Explosion 1 (of 5)" by Rudmer_Rotteveel + "Sharp Explosion 4 (of 5)" by Rudmer_Rotteveel + "Sharp Explosion 3 (of 5)" by Rudmer_Rotteveel + "Sharp Explosion 2 (of 5)" by Rudmer_Rotteveel + "Sharp Explosion 5 (of 5)" by Rudmer_Rotteveel + "Bending Metal" by NachtmahrTV

Proposed (round 68), cut from the HQ previews by `tools/concept/audio/sfx_r68.py`.
Sources: <https://freesound.org/people/Rudmer_Rotteveel/sounds/336006/> (Sharp Explosion 1 (of 5) by Rudmer_Rotteveel, CC0 1.0), <https://freesound.org/people/Rudmer_Rotteveel/sounds/336011/> (Sharp Explosion 4 (of 5) by Rudmer_Rotteveel, CC0 1.0), <https://freesound.org/people/Rudmer_Rotteveel/sounds/336004/> (Sharp Explosion 3 (of 5) by Rudmer_Rotteveel, CC0 1.0), <https://freesound.org/people/Rudmer_Rotteveel/sounds/336005/> (Sharp Explosion 2 (of 5) by Rudmer_Rotteveel, CC0 1.0), <https://freesound.org/people/Rudmer_Rotteveel/sounds/336010/> (Sharp Explosion 5 (of 5) by Rudmer_Rotteveel, CC0 1.0), <https://freesound.org/people/NachtmahrTV/sounds/556714/> (Bending Metal by NachtmahrTV, CC0 1.0).
Use: the core at 0: the wrecked batteries cook off ring by ring, outer → middle → inner, 0.15 s apart (6 SAM, 4 rail, 3 laser: 13 in 1.8 s), each a short vacuum flash; the rings crack at their joints; the implosion follows.
Brief: rapid secondary detonations and tearing metal: a chain, each ring a little heavier; a real recording, heavy, nothing bubbly or popping.
This one: five sharp explosion recordings in turn, 13 of them 0.15 s apart: the outer six sped to 1.2, the middle four as recorded, the inner three slowed to 0.8 (deeper), bending metal creaking under them: a rattling 'ba-ba-ba-ba-ba-ba-BA-BA-BA-BA-BOM-BOM-BOM', 2.6 s.
Edit: 2.60 s, the loudest 100 ms band −18.9 dB, peak −2.2 dBFS, −17.1 LUFS.

### sovereign-cookoff-r68-b — "Multiple Deep Explosions (Noisy rec)" by Rudmer_Rotteveel + "Metallic Groan" by hinchinbrook

Proposed (round 68), cut from the HQ previews by `tools/concept/audio/sfx_r68.py`.
Sources: <https://freesound.org/people/Rudmer_Rotteveel/sounds/336007/> (Multiple Deep Explosions (Noisy rec) by Rudmer_Rotteveel, CC0 1.0), <https://freesound.org/people/hinchinbrook/sounds/496836/> (Metallic Groan by hinchinbrook, CC0 1.0).
Use: the core at 0: the wrecked batteries cook off ring by ring, outer → middle → inner, 0.15 s apart (6 SAM, 4 rail, 3 laser: 13 in 1.8 s), each a short vacuum flash; the rings crack at their joints; the implosion follows.
Brief: rapid secondary detonations and tearing metal: a chain, each ring a little heavier; a real recording, heavy, nothing bubbly or popping.
This one: 13 deep explosions cut from one field recording of a series, 0.15 s apart, the outer six sped to 1.2, the inner three slowed to 0.8, a metal groan under them: a deeper, rounder rolling chain than a (the recording's distant road hiss under it), 2.6 s.
Edit: 2.60 s, the loudest 100 ms band −18.0 dB, peak −5.9 dBFS, −18.8 LUFS.

### sovereign-implosion-r68-a — "Dry thunder2.wav" by juskiddink + "Bending Metal" by NachtmahrTV + "Fireball" by NearTheAtmoshphere

Proposed (round 68), cut from the HQ previews by `tools/concept/audio/sfx_r68.py`.
Sources: <https://freesound.org/people/juskiddink/sounds/101934/> (Dry thunder2.wav by juskiddink, CC-BY 4.0), <https://freesound.org/people/NachtmahrTV/sounds/556714/> (Bending Metal by NachtmahrTV, CC0 1.0), <https://freesound.org/people/NearTheAtmoshphere/sounds/683179/> (Fireball by NearTheAtmoshphere, CC0 1.0).
Use: after the cook-offs: the core collapses inward (the petals folding in, a white-gold implosion) and then blows; the `huge` rung starts at the whoomph, 1.15 s in, under the screen flash.
Brief: a deep implosion: the air sucked inward, metal folding, a deep 'whoomph' (no pop); a real recording, heavy, nothing bubbly or popping.
This one: a dry thunderclap reversed and slowed to 0.8 (its rumble sucked back into the crack), bending metal under it, cut at the crack into a recorded fireball slowed to 0.5: 'hhhhwwuuUUP-WHOOMF', 2.15 s.
Edit: 2.15 s, the loudest 100 ms band −18.1 dB, peak −5.4 dBFS, −19.1 LUFS.

### sovereign-implosion-r68-b — "Thunder, Close" by Fission9 + "Closing large metal door-001" by AudioPapkin + "Sharp Explosion 2 (of 5)" by Rudmer_Rotteveel

Proposed (round 68), cut from the HQ previews by `tools/concept/audio/sfx_r68.py`.
Sources: <https://freesound.org/people/Fission9/sounds/505113/> (Thunder, Close by Fission9, CC0 1.0), <https://freesound.org/people/AudioPapkin/sounds/814976/> (Closing large metal door-001 by AudioPapkin, CC0 1.0), <https://freesound.org/people/Rudmer_Rotteveel/sounds/336005/> (Sharp Explosion 2 (of 5) by Rudmer_Rotteveel, CC0 1.0).
Use: after the cook-offs: the core collapses inward (the petals folding in, a white-gold implosion) and then blows; the `huge` rung starts at the whoomph, 1.15 s in, under the screen flash.
Brief: a deep implosion: the air sucked inward, metal folding, a deep 'whoomph' (no pop); a real recording, heavy, nothing bubbly or popping.
This one: a close thunder reversed and slowed to 0.7, then a large metal door's thud slowed to 0.5 (the petals slamming in) with an explosion slowed to 0.45: a darker suck and a heavier metal 'DOOMF' than a, 2.15 s.
Edit: 2.15 s, the loudest 100 ms band −18.2 dB, peak −2.1 dBFS, −17.3 LUFS.

### relay-charge-r68-a — "Rising electric hum.aif" by jukkako + "hum electric transformer in large room1.flac" by kyles

Proposed (round 68), cut from the HQ previews by `tools/concept/audio/sfx_r68.py`.
Sources: <https://freesound.org/people/jukkako/sounds/616084/> (Rising electric hum.aif by jukkako, CC0 1.0), <https://freesound.org/people/kyles/sounds/453444/> (hum electric transformer in large room1.flac by kyles, CC0 1.0).
Use: Level 35, once at t=59.8: the mass relay's 1.2 s telegraph (a thin red line across the whole field through the ship; easy 1.5 s: it starts 0.3 s into the telegraph); the flash below at its end.
Brief: the rail line's chosen charge (concept/rail-charge-r60-b.ogg, a rising hum over a slowed transformer) in its long version: a huge capacitor whine building for 1.2 s; a real recording, heavy, nothing bubbly or popping.
This one: the chosen rail charge's own two recordings stretched to 1.2 s: the rising electric hum gliding up from 0.7 to 1.5 and swelling, over the transformer hum slowed to 0.8: the bunker's charge, longer, 1.2 s.
Edit: 1.20 s, the loudest 100 ms band −24.0 dB, peak −12.0 dBFS, −22.9 LUFS.

### relay-charge-r68-b — "Rising electric hum.aif" by jukkako + "hum electric transformer in large room1.flac" by kyles + "Camera Flash Charging.aif" by ftpalad

Proposed (round 68), cut from the HQ previews by `tools/concept/audio/sfx_r68.py`.
Sources: <https://freesound.org/people/jukkako/sounds/616084/> (Rising electric hum.aif by jukkako, CC0 1.0), <https://freesound.org/people/kyles/sounds/453444/> (hum electric transformer in large room1.flac by kyles, CC0 1.0), <https://freesound.org/people/ftpalad/sounds/119902/> (Camera Flash Charging.aif by ftpalad, CC0 1.0).
Use: Level 35, once at t=59.8: the mass relay's 1.2 s telegraph (a thin red line across the whole field through the ship; easy 1.5 s: it starts 0.3 s into the telegraph); the flash below at its end.
Brief: the rail line's chosen charge (concept/rail-charge-r60-b.ogg, a rising hum over a slowed transformer) in its long version: a huge capacitor whine building for 1.2 s; a real recording, heavy, nothing bubbly or popping.
This one: the same two recordings lower (the hum gliding from 0.5 to 1.2, the transformer at 0.6) with a camera flash's charging whine pitched well down rising over them: bigger and deeper than a, a whine on top, 1.2 s.
Edit: 1.20 s, the loudest 100 ms band −24.6 dB, peak −10.6 dBFS, −21.0 LUFS.

### relay-flash-r68-a — "Whip Crack 01.wav" by CGEffex + "Howitzer Gun Shots" by qubodup + "metal glass makes long 'PING!' sound.aif" by Tomlija + "Thunder Clap" by DaniloSFX

Proposed (round 68), cut from the HQ previews by `tools/concept/audio/sfx_r68.py`.
Sources: <https://freesound.org/people/CGEffex/sounds/93100/> (Whip Crack 01.wav by CGEffex, CC-BY 4.0), <https://freesound.org/people/qubodup/sounds/67516/> (Howitzer Gun Shots by qubodup, CC0 1.0), <https://freesound.org/people/Tomlija/sounds/101246/> (metal glass makes long 'PING!' sound.aif by Tomlija, CC-BY 3.0), <https://freesound.org/people/DaniloSFX/sounds/672776/> (Thunder Clap by DaniloSFX, CC0 1.0).
Use: Level 35, once at t=61: the mass relay's flash along its line (16 px wide, live 0.2 s, 20 damage), at the charge's end; the thunder's tail runs under the next seconds (no wave until t=63.5).
Brief: the rail line's chosen shot (concept/rail-shot-r60-b.ogg, a whip crack on a howitzer shot with a long metal ping) in its long version: the crack with a long rolling thunder tail; a real recording, heavy, nothing bubbly or popping.
This one: the chosen rail shot's three recordings (the whip crack, the howitzer, the metal ping, its ping longer) with a thunderclap's roll under them: 'KRAKK-rrrrooooommmm', 3.9 s.
Edit: 3.88 s, the loudest 100 ms band −16.2 dB, peak −2.3 dBFS, −19.9 LUFS.

### relay-flash-r68-b — "Whip Crack 01.wav" by CGEffex + "Howitzer Gun Shots" by qubodup + "metal glass makes long 'PING!' sound.aif" by Tomlija + "Huge Thunder Crack [XY + MS]" by -CASK-

Proposed (round 68), cut from the HQ previews by `tools/concept/audio/sfx_r68.py`.
Sources: <https://freesound.org/people/CGEffex/sounds/93100/> (Whip Crack 01.wav by CGEffex, CC-BY 4.0), <https://freesound.org/people/qubodup/sounds/67516/> (Howitzer Gun Shots by qubodup, CC0 1.0), <https://freesound.org/people/Tomlija/sounds/101246/> (metal glass makes long 'PING!' sound.aif by Tomlija, CC-BY 3.0), <https://freesound.org/people/-CASK-/sounds/732992/> (Huge Thunder Crack [XY + MS] by -CASK-, CC-BY 4.0).
Use: Level 35, once at t=61: the mass relay's flash along its line (16 px wide, live 0.2 s, 20 damage), at the charge's end; the thunder's tail runs under the next seconds (no wave until t=63.5).
Brief: the rail line's chosen shot (concept/rail-shot-r60-b.ogg, a whip crack on a howitzer shot with a long metal ping) in its long version: the crack with a long rolling thunder tail; a real recording, heavy, nothing bubbly or popping.
This one: the same shot with a huge thunder crack slowed to 0.8 under it: a second, deeper crack and a longer, lower rumble than a, 4.0 s.
Edit: 4.00 s, the loudest 100 ms band −16.2 dB, peak −2.9 dBFS, −21.2 LUFS.

### container-burst-r67-a — "Large Bolt Cutter" by jameswrowles + "Closing large metal door-002" by AudioPapkin

Proposed (round 67), cut from the HQ previews by `tools/concept/audio/sfx_r67.py`.
Sources: <https://freesound.org/people/jameswrowles/sounds/516591/> (Large Bolt Cutter by jameswrowles, CC0 1.0), <https://freesound.org/people/AudioPapkin/sounds/814977/> (Closing large metal door-002 by AudioPapkin, CC0 1.0).
Use: a cargo container destroyed (Level 34: six, t≈18, 38, 58, 80, 150 and 170; 12 HP, no bounty; the secondary `CONTAINERS n/6` counts them), over its destruction, as its salvage pickup of 50 (328) drifts out.
Brief: a cargo latch blowing and a short metal clang: a latch or bolt cracking, then the container's steel door slamming open; short and dry, no pop; a real recording, heavy, nothing bubbly or popping.
This one: a large bolt cutter snapping a bolt (slowed to 0.85), then a big metal door slammed 40 ms later: a sharp 'KTAK' into a ringing 'KLANGG', 1.35 s.
Edit: 1.34 s, the loudest 100 ms band −22.2 dB, peak −9.5 dBFS, −23.9 LUFS.

### container-burst-r67-b — "industrial_nailgun_1shot+ring" by gilly11 + "big_metal_unlock_and_slam.wav" by simpsi

Proposed (round 67), cut from the HQ previews by `tools/concept/audio/sfx_r67.py`.
Sources: <https://freesound.org/people/gilly11/sounds/143615/> (industrial_nailgun_1shot+ring by gilly11, CC-BY 3.0), <https://freesound.org/people/simpsi/sounds/175663/> (big_metal_unlock_and_slam.wav by simpsi, CC-BY 3.0).
Use: a cargo container destroyed (Level 34: six, t≈18, 38, 58, 80, 150 and 170; 12 HP, no bounty; the secondary `CONTAINERS n/6` counts them), over its destruction, as its salvage pickup of 50 (328) drifts out.
Brief: a cargo latch blowing and a short metal clang: a latch or bolt cracking, then the container's steel door slamming open; short and dry, no pop; a real recording, heavy, nothing bubbly or popping.
This one: an industrial nail gun's shot (a pneumatic bang), then a heavy garage door's metal slam (slowed to 0.9) 50 ms later: a drier 'PTAK' into a lower, boomier 'BLAMM' with the garage's echo, 1.45 s.
Edit: 1.45 s, the loudest 100 ms band −23.4 dB, peak −5.9 dBFS, −20.1 LUFS.

### hold-clamp-r67-a — "Automotive Two Post Lift" by j_soundeffects + "door metal big heavy close kinda slam thud echo offmic.wav" by kyles + "Pressure Hiss" by magnuswaker

Proposed (round 67), cut from the HQ previews by `tools/concept/audio/sfx_r67.py`.
Sources: <https://freesound.org/people/j_soundeffects/sounds/863975/> (Automotive Two Post Lift by j_soundeffects, CC-BY 4.0), <https://freesound.org/people/kyles/sounds/406197/> (door metal big heavy close kinda slam thud echo offmic.wav by kyles, CC0 1.0), <https://freesound.org/people/magnuswaker/sounds/581083/> (Pressure Hiss by magnuswaker, CC0 1.0).
Use: each of the freighter hold's four clamps destroyed (Level 34, t≈108-120; 15 HP, no bounty; a player who shoots at once clears all four in about 2 s, so up to four close together); with the fourth the Helix core floats up out of the hold.
Brief: a heavy hydraulic unlock: a locking bolt clacking free and the pressure venting; heavier than Level 29's Dock 9 clamp (concept/clamp-release-r62-b.ogg, the fallback: answer 'dock'); a real recording, heavy, nothing bubbly or popping.
This one: a car lift's hydraulic carriage dropping onto its lock (slowed to 0.75) over a heavy steel door's thud, with a pressure valve's hiss over it: a heavy 'KLAKK-pssshh', 0.95 s.
Edit: 0.92 s, the loudest 100 ms band −25.1 dB, peak −12.2 dBFS, −26.2 LUFS.

### hold-clamp-r67-b — "Motors_Hydraulic_Handycap_Ramp" by SoundsofTheMachine + "Air release" by davidlay1

Proposed (round 67), cut from the HQ previews by `tools/concept/audio/sfx_r67.py`.
Sources: <https://freesound.org/people/SoundsofTheMachine/sounds/784981/> (Motors_Hydraulic_Handycap_Ramp by SoundsofTheMachine, CC0 1.0), <https://freesound.org/people/davidlay1/sounds/416080/> (Air release by davidlay1, CC0 1.0).
Use: each of the freighter hold's four clamps destroyed (Level 34, t≈108-120; 15 HP, no bounty; a player who shoots at once clears all four in about 2 s, so up to four close together); with the fourth the Helix core floats up out of the hold.
Brief: a heavy hydraulic unlock: a locking bolt clacking free and the pressure venting; heavier than Level 29's Dock 9 clamp (concept/clamp-release-r62-b.ogg, the fallback: answer 'dock'); a real recording, heavy, nothing bubbly or popping.
This one: a van's hydraulic ramp locking home (slowed to 0.8) with an air release venting under its tail: a lower, rattling 'kr-KUNK-unk' with a longer 'hhhhh', 1.25 s.
Edit: 1.25 s, the loudest 100 ms band −25.1 dB, peak −11.3 dBFS, −23.2 LUFS.

### helix-core-r67-a — "Transformer buzz" by logant547

Proposed (round 67), cut from the HQ previews by `tools/concept/audio/sfx_r67.py`.
Source: <https://freesound.org/people/logant547/sounds/677736/> (Transformer buzz by logant547, CC0 1.0).
Use: once, the Helix core collected (Level 34, t≈116-124: flown through as it drifts after the clamps; `HELIX CORE SALVAGED`), over the large salvage pickup; Vorne's motif stem is on in the music then; not the secrets' cue; a missed core plays nothing (Okafor's tug line).
Brief: a deep power hum swelling: a big machine or transformer powering up as the core comes alive, about 2 s (the sfx README's row: the large salvage pickup with a short power-up hum); a real recording, heavy, nothing bubbly or popping.
This one: a power transformer's mains hum glided up from 0.55 to its own speed while it swells 18 dB: a deep, even 'mmmMMMM' rising in pitch, 2.0 s.
Edit: 2.00 s, the loudest 100 ms band −22.5 dB, peak −8.1 dBFS, −20.0 LUFS.

### helix-core-r67-b — "Large Industrial Machine Starting up and running" by ryks88

Proposed (round 67), cut from the HQ previews by `tools/concept/audio/sfx_r67.py`.
Source: <https://freesound.org/people/ryks88/sounds/709257/> (Large Industrial Machine Starting up and running by ryks88, CC-BY 4.0).
Use: once, the Helix core collected (Level 34, t≈116-124: flown through as it drifts after the clamps; `HELIX CORE SALVAGED`), over the large salvage pickup; Vorne's motif stem is on in the music then; not the secrets' cue; a missed core plays nothing (Okafor's tug line).
Brief: a deep power hum swelling: a big machine or transformer powering up as the core comes alive, about 2 s (the sfx README's row: the large salvage pickup with a short power-up hum); a real recording, heavy, nothing bubbly or popping.
This one: a large industrial air scrubber switching on, slowed (0.6 gliding to 0.9) and swelling: a rougher machine 'vrrumMMMM' with the motor's kick at its start, 2.2 s.
Edit: 2.20 s, the loudest 100 ms band −22.0 dB, peak −10.7 dBFS, −22.4 LUFS.

### wreck-groans-r67-a — "Metal Creaks_Groans.wav" by Cell31_Sound_Productions

Proposed (round 67), cut from the HQ previews by `tools/concept/audio/sfx_r67.py`.
Source: <https://freesound.org/people/Cell31_Sound_Productions/sounds/380403/> (Metal Creaks_Groans.wav by Cell31_Sound_Productions, CC-BY 4.0).
Use: a sparse layer over the `belt-open` bed through the ship graveyard (Level 34, t=0-174: the level's `ambience_layers`, faded in over 3 s and out over 3 s before section 6's clear space), looping at the ambience's level.
Brief: the wrecks' slow metal groans: huge hulls stressing in the cold, a few long low groans with quiet between; not rhythmic, no clanks or pops; under the music, heard in the quiet stretches; a real recording, heavy, nothing bubbly or popping.
This one: three groans of stressed metal slowed to 0.6 (deep and slow), 1.0, 11.5 and 20.5 s into the loop with quiet between: long low 'grrroooann's, 30 s loop.
Edit: 30.00 s, re-levelled 2026-10-29 (round 67 S.4 re-shown: too quiet) to its loudest short-term loudness (3 s) −13.2 LUFS under a −1.5 dBFS ceiling: peak −1.6 dBFS, −13.7 LUFS (was −38.3 LUFS).

### wreck-groans-r67-b — "liner gangplank groaning.WAV" by phonoflora + "Bending Metal" by NachtmahrTV

Proposed (round 67), cut from the HQ previews by `tools/concept/audio/sfx_r67.py`.
Sources: <https://freesound.org/people/phonoflora/sounds/500008/> (liner gangplank groaning.WAV by phonoflora, CC0 1.0), <https://freesound.org/people/NachtmahrTV/sounds/556714/> (Bending Metal by NachtmahrTV, CC0 1.0).
Use: a sparse layer over the `belt-open` bed through the ship graveyard (Level 34, t=0-174: the level's `ambience_layers`, faded in over 3 s and out over 3 s before section 6's clear space), looping at the ambience's level.
Brief: the wrecks' slow metal groans: huge hulls stressing in the cold, a few long low groans with quiet between; not rhythmic, no clanks or pops; under the music, heard in the quiet stretches; a real recording, heavy, nothing bubbly or popping.
This one: a cruise liner's steel gangplank groaning (slowed to 0.55) twice and a bending metal sheet (slowed to 0.65) between them, 1.5, 11 and 20 s into the loop: creakier, more grinding groans than a, 30 s loop.
Edit: 30.00 s, re-levelled 2026-10-29 (round 67 S.4 re-shown: too quiet) to its loudest short-term loudness (3 s) −13.2 LUFS under a −1.5 dBFS ceiling: peak −1.7 dBFS, −15.0 LUFS (was −31.0 LUFS).

### wreck-groans-solo-r67-a, -b and wreck-groans-mix-r67-a, -b — review files (round 67 S.4, re-shown 2026-10-29)

Made by `tools/concept/audio/wreck_groans_mix_r67.py` from the re-levelled `wreck-groans-r67-a`/`-b` (their sources
above) and, for the mix, the game's own files: `assets/music/hollow-rock-base.ogg` (track 12's base stem, ours) and
`assets/sfx/belt-open-r60-a.ogg` (the bed, its sources in CREDITS.md). Not proposals: what the round plays so the
layer can be judged.
Solo: each variant alone, scaled so its loudest momentary loudness (400 ms) matches the mean of S.1–S.3's a (−19.5
LUFS): a −10.7 dB, b −9.2 dB.
In the mix: Level 34 t=8–28 rendered offline at the game's gains (default settings: music 1, effects 0.5): the base
stem at 0.6 × −3 dB (0.425), the bed and the layer at 0.7 × 0.5 (0.35), no radio duck, no effects. Measured (EBU R128
short-term, 3 s): the groans peak at −22.8 (a) and −22.4 (b) LUFS against the bed + music's −21.3 (median; −20.8 at
most): 1.5 and 1.1 dB under it, clearly heard; before the re-level they were 26.1 (a) and 17.2 (b) dB under it.

## Concept round 72, step AUD-R (Act 6: the shared hybrid set's regrowth creak and tissue tear, the mirror's ping, the Reflex ricochet, the Harrow's lock and burst, the lightning's tell and strike, Io's eruption, the pylon dome's up and down, the Graft Turret's regrowth, the beds `jovian-sky`, `aurelia`, `io-refinery`, `callisto-fortress`)

### regrowth-creak-r72-a — "LETHRCreak_Leather Stretching Creak 01_KVV AUDIO_FREE" by KVV_Audio

Proposed (round 72), cut from the HQ previews by `tools/concept/audio/sfx_r72.py`.
Source: <https://freesound.org/people/KVV_Audio/sounds/797919/> (LETHRCreak_Leather Stretching Creak 01_KVV AUDIO_FREE by KVV_Audio, CC-BY 4.0).
Use: the shared hybrid set's healing sound: while a Chimera's HP regrows (repeated for the nearest healing unit), a Graft Turret's regrowth tail and (an octave down) the Ascendant's sacs healing; Levels 37-42, often but quiet, under the fire.
Brief: a soft rising creak: stretched leather or a rope twisting under load, dry and close; never wet, bubbly or squelching; a real recording, heavy, nothing bubbly or popping.
This one: a leather strap stretched slowly, slowed to 0.85 and swelling up: a tight, grainy leather creak that rises, 1.2 s.
Edit: 1.20 s, the loudest 100 ms band −30.8 dB, peak −8.1 dBFS, −28.1 LUFS.

### regrowth-creak-r72-b — "creaking sailing boat.wav" by jimsim

Proposed (round 72), cut from the HQ previews by `tools/concept/audio/sfx_r72.py`.
Source: <https://freesound.org/people/jimsim/sounds/411087/> (creaking sailing boat.wav by jimsim, CC-BY 4.0).
Use: the shared hybrid set's healing sound: while a Chimera's HP regrows (repeated for the nearest healing unit), a Graft Turret's regrowth tail and (an octave down) the Ascendant's sacs healing; Levels 37-42, often but quiet, under the fire.
Brief: a soft rising creak: stretched leather or a rope twisting under load, dry and close; never wet, bubbly or squelching; a real recording, heavy, nothing bubbly or popping.
This one: a sailing boat's rigging and hull creaking at the mooring, slowed to 0.9 and swelling up: a lower, woodier rope creak than a, 1.2 s.
Edit: 1.20 s, the loudest 100 ms band −31.9 dB, peak −13.4 dBFS, −28.0 LUFS.

### tissue-tear-r72-a — "Cloth Tearing and Ripping - Flannel, Cotton, Scraps - Close Mic - Long & Fast" by ChristopherJngs

Proposed (round 72), cut from the HQ previews by `tools/concept/audio/sfx_r72.py`.
Source: <https://freesound.org/people/ChristopherJngs/sounds/719003/> (Cloth Tearing and Ripping - Flannel, Cotton, Scraps - Close Mic - Long & Fast by ChristopherJngs, CC0 1.0).
Use: the shared hybrid set's tear: a Graft Turret root's death, the Ascendant's brood splitting and the lifeboat tearing free, Level 37's growth over the decks (drawn long); now and then.
Brief: a dry fibrous tearing: heavy canvas or sailcloth ripping, rope fibres parting; never flesh, never wet; a real recording, heavy, nothing bubbly or popping.
This one: cotton cloth torn by hand, close, slowed to 0.6 so it reads as heavy canvas: a long dry rip, 0.9 s.
Edit: 0.90 s, the loudest 100 ms band −24.1 dB, peak −10.4 dBFS, −21.7 LUFS.

### tissue-tear-r72-b — "230104-044_normalized_ship_sail_rip_tear_1.wav" by mmmtoop + "Cloth Rip Uneven Long" by Rudmer_Rotteveel

Proposed (round 72), cut from the HQ previews by `tools/concept/audio/sfx_r72.py`.
Sources: <https://freesound.org/people/mmmtoop/sounds/674035/> (230104-044_normalized_ship_sail_rip_tear_1.wav by mmmtoop, CC0 1.0), <https://freesound.org/people/Rudmer_Rotteveel/sounds/591525/> (Cloth Rip Uneven Long by Rudmer_Rotteveel, CC0 1.0).
Use: the shared hybrid set's tear: a Graft Turret root's death, the Ascendant's brood splitting and the lifeboat tearing free, Level 37's growth over the decks (drawn long); now and then.
Brief: a dry fibrous tearing: heavy canvas or sailcloth ripping, rope fibres parting; never flesh, never wet; a real recording, heavy, nothing bubbly or popping.
This one: a ship's sail ripping, with a quick cloth rip slowed under it: rougher and wider than a, the fibres parting in bursts, 0.75 s.
Edit: 0.75 s, the loudest 100 ms band −24.1 dB, peak −8.3 dBFS, −22.0 LUFS.

### mirror-ping-r72-a — "Glass Ping" by tix99

Proposed (round 72), cut from the HQ previews by `tools/concept/audio/sfx_r72.py`.
Source: <https://freesound.org/people/tix99/sounds/745014/> (Glass Ping by tix99, CC0 1.0).
Use: every shot a Mirror Interceptor's mirror bounces (throttled: once per 0.08 s per unit) and, lower, the Ascendant's panels; Levels 38-42, often in a mirror fight.
Brief: a bright glassy ping: a struck glass or glass bowl, short; it must never sound like the Reflex Shield's ricochet (glass, not a bullet); a real recording, heavy, nothing bubbly or popping.
This one: a glass struck once: a clean high 'ping' with a short ring, 0.4 s.
Edit: 0.40 s, the loudest 100 ms band −26.4 dB, peak −12.7 dBFS, −inf LUFS.

### mirror-ping-r72-b — "Crystal bowl F#3 1.wav" by caiogracco

Proposed (round 72), cut from the HQ previews by `tools/concept/audio/sfx_r72.py`.
Source: <https://freesound.org/people/caiogracco/sounds/150454/> (Crystal bowl F#3 1.wav by caiogracco, CC0 1.0).
Use: every shot a Mirror Interceptor's mirror bounces (throttled: once per 0.08 s per unit) and, lower, the Ascendant's panels; Levels 38-42, often in a mirror fight.
Brief: a bright glassy ping: a struck glass or glass bowl, short; it must never sound like the Reflex Shield's ricochet (glass, not a bullet); a real recording, heavy, nothing bubbly or popping.
This one: a crystal singing bowl struck, sped to 1.5: a rounder, more bell-like glass tone than a, 0.45 s.
Edit: 0.45 s, the loudest 100 ms band −26.8 dB, peak −13.6 dBFS, −25.9 LUFS.

### reflex-ricochet-r72-a — "bullet ricochet.wav" by aust_paul

Proposed (round 72), cut from the HQ previews by `tools/concept/audio/sfx_r72.py`.
Source: <https://freesound.org/people/aust_paul/sounds/30932/> (bullet ricochet.wav by aust_paul, CC0 1.0).
Use: the Reflex Shield (Act 6's shield): each enemy bullet it turns into a bolt, throttled to once per 0.06 s, over the shield hit; often while the player tanks fire.
Brief: a real bullet ricochet (a rifle round whining off steel), bright and short: the player's side of the mirrors, never the mirror's glassy ping; a real recording, heavy, nothing bubbly or popping.
This one: a bullet's ricochet: the hit and a thin whine flying off, 0.45 s.
Edit: 0.45 s, the loudest 100 ms band −27.1 dB, peak −5.1 dBFS, −26.6 LUFS.

### reflex-ricochet-r72-b — "Ricochet metal3.wav" by CGEffex

Proposed (round 72), cut from the HQ previews by `tools/concept/audio/sfx_r72.py`.
Source: <https://freesound.org/people/CGEffex/sounds/96631/> (Ricochet metal3.wav by CGEffex, CC-BY 4.0).
Use: the Reflex Shield (Act 6's shield): each enemy bullet it turns into a bolt, throttled to once per 0.06 s, over the shield hit; often while the player tanks fire.
Brief: a real bullet ricochet (a rifle round whining off steel), bright and short: the player's side of the mirrors, never the mirror's glassy ping; a real recording, heavy, nothing bubbly or popping.
This one: a round glancing off metal, sped to 1.2: more of a metallic 'spang' than a's whine, 0.5 s.
Edit: 0.50 s, the loudest 100 ms band −24.1 dB, peak −12.4 dBFS, −25.1 LUFS.

### harrow-lock-r72-a — "Jet Flyby 02" by mrspivey

Proposed (round 72), cut from the HQ previews by `tools/concept/audio/sfx_r72.py`.
Source: <https://freesound.org/people/mrspivey/sounds/805169/> (Jet Flyby 02 by mrspivey, CC0 1.0).
Use: the Harrow's 0.6 s lock tell (Levels 38-42): it slows, its sac pulses and its nose swings onto the ship before the ram; the warning the player must hear.
Brief: a short rising whine, thin and urgent, 0.6 s; a real recording, heavy, nothing bubbly or popping.
This one: a jet's approach whine, glided up (0.9 → 1.5) and swelling: a thin rising jet scream, 0.6 s.
Edit: 0.60 s, the loudest 100 ms band −24.1 dB, peak −13.5 dBFS, −28.8 LUFS.

### harrow-lock-r72-b — "Start-up Sound - U.S. Army M1A1 Tank - Gas Turbine Engine.mp3" by adr1911

Proposed (round 72), cut from the HQ previews by `tools/concept/audio/sfx_r72.py`.
Source: <https://freesound.org/people/adr1911/sounds/542582/> (Start-up Sound - U.S. Army M1A1 Tank - Gas Turbine Engine.mp3 by adr1911, CC0 1.0).
Use: the Harrow's 0.6 s lock tell (Levels 38-42): it slows, its sac pulses and its nose swings onto the ship before the ram; the warning the player must hear.
Brief: a short rising whine, thin and urgent, 0.6 s; a real recording, heavy, nothing bubbly or popping.
This one: a tank's gas turbine spooling up, glided up (0.9 → 1.6) and swelling: a harder mechanical whine climbing, 0.6 s.
Edit: 0.60 s, the loudest 100 ms band −24.8 dB, peak −12.8 dBFS, −25.9 LUFS.

### harrow-burst-r72-a — "Muffled Boom" by UnderlinedDesigns + "sw_Fireworks_Mortar_02.wav" by jppi_Stu

Proposed (round 72), cut from the HQ previews by `tools/concept/audio/sfx_r72.py`.
Sources: <https://freesound.org/people/UnderlinedDesigns/sounds/191697/> (Muffled Boom by UnderlinedDesigns, CC0 1.0), <https://freesound.org/people/jppi_Stu/sounds/161688/> (sw_Fireworks_Mortar_02.wav by jppi_Stu, CC-BY 4.0).
Use: every Harrow death (shot, ramming, struck by lightning), instead of a size rung; Levels 38-42, often in a swarm.
Brief: a dull heavy thud with a crackle (a muffled blast, a firework mortar's thump); never a pop, never wet; a real recording, heavy, nothing bubbly or popping.
This one: a muffled boom (a deep dull thud) with a firework shell's crackle tail over it (its burst crack cut away), 0.9 s.
Edit: 0.90 s, the loudest 100 ms band −24.7 dB, peak −3.1 dBFS, −17.4 LUFS.

### harrow-burst-r72-b — "muffled_explosion.wav" by SypherZent + "sw_Fireworks_Mortar_03.wav" by jppi_Stu

Proposed (round 72), cut from the HQ previews by `tools/concept/audio/sfx_r72.py`.
Sources: <https://freesound.org/people/SypherZent/sounds/420671/> (muffled_explosion.wav by SypherZent, CC-BY 4.0), <https://freesound.org/people/jppi_Stu/sounds/161687/> (sw_Fireworks_Mortar_03.wav by jppi_Stu, CC-BY 4.0).
Use: every Harrow death (shot, ramming, struck by lightning), instead of a size rung; Levels 38-42, often in a swarm.
Brief: a dull heavy thud with a crackle (a muffled blast, a firework mortar's thump); never a pop, never wet; a real recording, heavy, nothing bubbly or popping.
This one: a muffled explosion (more body and mid than a's boom) with another mortar shell's crackle tail, 0.9 s.
Edit: 0.90 s, the loudest 100 ms band −19.0 dB, peak −4.3 dBFS, −18.1 LUFS.

### lightning-tell-r72-a — "S16-18 Tesla coil sparking and humming.wav" by craigsmith

Proposed (round 72), cut from the HQ previews by `tools/concept/audio/sfx_r72.py`.
Source: <https://freesound.org/people/craigsmith/sounds/675760/> (S16-18 Tesla coil sparking and humming.wav by craigsmith, CC0 1.0).
Use: a lightning column's 1.0 s telegraph (Level 38, a few in Level 36): the strike lands where it glows; quiet, under the fire.
Brief: a faint high-voltage buzz rising (a Tesla coil, a power line's corona), its length the telegraph's; a real recording, heavy, nothing bubbly or popping.
This one: a Tesla coil sparking and humming, glided up (0.85 → 1.15) and swelling to the strike: a crackling buzz, 1.0 s.
Edit: 1.00 s, the loudest 100 ms band −38.4 dB, peak −23.8 dBFS, −30.2 LUFS.

### lightning-tell-r72-b — "Zapping - High Voltage Electricity Buzz Isolated" by el_boss

Proposed (round 72), cut from the HQ previews by `tools/concept/audio/sfx_r72.py`.
Source: <https://freesound.org/people/el_boss/sounds/740198/> (Zapping - High Voltage Electricity Buzz Isolated by el_boss, CC0 1.0).
Use: a lightning column's 1.0 s telegraph (Level 38, a few in Level 36): the strike lands where it glows; quiet, under the fire.
Brief: a faint high-voltage buzz rising (a Tesla coil, a power line's corona), its length the telegraph's; a real recording, heavy, nothing bubbly or popping.
This one: a high-voltage buzz, glided up (0.8 → 1.2) and swelling: smoother and higher than a, more of a rising hum, 1.0 s.
Edit: 1.00 s, the loudest 100 ms band −35.0 dB, peak −15.0 dBFS, −31.1 LUFS.

### lightning-strike-r72-a — "Close lightning strike 2018 07 07" by csengeri + "Closeup Thunder Strike 01" by loganzsound

Proposed (round 72), cut from the HQ previews by `tools/concept/audio/sfx_r72.py`.
Sources: <https://freesound.org/people/csengeri/sounds/434359/> (Close lightning strike 2018 07 07 by csengeri, CC0 1.0), <https://freesound.org/people/loganzsound/sounds/840628/> (Closeup Thunder Strike 01 by loganzsound, CC0 1.0).
Use: a lightning strike (Level 38, a few in Level 36) as its telegraph ends; never two within 1.5 s; a big moment.
Brief: a close crack (a real lightning strike recorded near: the sharp tearing crack, its first 0.3 s) with Level 12's chosen thunder (thunder-1-r34-b's source) keyed to it: the crack, then the roll; a real recording, heavy, nothing bubbly or popping.
This one: a lightning strike recorded close in a storm, its crack, then Level 12's close thunder strike rolling away: a natural crack and roll, 3.7 s.
Edit: 3.72 s, the loudest 100 ms band −14.2 dB, peak −1.1 dBFS, −21.1 LUFS.

### lightning-strike-r72-b — "Flash 1,2MV_1.wav" by jensfelger + "Closeup Thunder Strike 01" by loganzsound

Proposed (round 72), cut from the HQ previews by `tools/concept/audio/sfx_r72.py`.
Sources: <https://freesound.org/people/jensfelger/sounds/367451/> (Flash 1,2MV_1.wav by jensfelger, CC0 1.0), <https://freesound.org/people/loganzsound/sounds/840628/> (Closeup Thunder Strike 01 by loganzsound, CC0 1.0).
Use: a lightning strike (Level 38, a few in Level 36) as its telegraph ends; never two within 1.5 s; a big moment.
Brief: a close crack (a real lightning strike recorded near: the sharp tearing crack, its first 0.3 s) with Level 12's chosen thunder (thunder-1-r34-b's source) keyed to it: the crack, then the roll; a real recording, heavy, nothing bubbly or popping.
This one: a 1.2 MV flashover in a high-voltage lab (a sharper, more electric snap than a), then the same thunder roll, 3.7 s.
Edit: 3.72 s, the loudest 100 ms band −14.7 dB, peak −1.2 dBFS, −19.9 LUFS.

### eruption-r72-a — "Cracking Earthquake (cracking soil, cracking stone)" by uagadugu + "volcano_eruption.wav" by smcameron

Proposed (round 72), cut from the HQ previews by `tools/concept/audio/sfx_r72.py`.
Sources: <https://freesound.org/people/uagadugu/sounds/222521/> (Cracking Earthquake (cracking soil, cracking stone) by uagadugu, CC0 1.0), <https://freesound.org/people/smcameron/sounds/51429/> (volcano_eruption.wav by smcameron, CC-BY 4.0).
Use: Level 39 (Io): each eruption, its 1.2 s tell then the blast (the rocks' sounds are the belt's).
Brief: Level 27's tell and roar in a dry variant: a rising ground rumble, then a volcanic blast's roar; no water, no steam hiss; a real recording, heavy, nothing bubbly or popping.
This one: an earthquake's rumble swelling for 1.2 s (the tell), then a volcanic eruption's roar: dense and dry, 3.2 s.
Edit: 3.20 s, the loudest 100 ms band −17.6 dB, peak −6.3 dBFS, −17.0 LUFS.

### eruption-r72-b — "EARTHQUAKE OR DISTANT SPACE SHUTTLE RUMBLE.WAV" by metrostock99 + "661147__felixblume__volcano-mount-yasur-tanna-island-short" by pwigger0029

Proposed (round 72), cut from the HQ previews by `tools/concept/audio/sfx_r72.py`.
Sources: <https://freesound.org/people/metrostock99/sounds/203281/> (EARTHQUAKE OR DISTANT SPACE SHUTTLE RUMBLE.WAV by metrostock99, CC0 1.0), <https://freesound.org/people/pwigger0029/sounds/710561/> (661147__felixblume__volcano-mount-yasur-tanna-island-short by pwigger0029, CC0 1.0).
Use: Level 39 (Io): each eruption, its 1.2 s tell then the blast (the rocks' sounds are the belt's).
Brief: Level 27's tell and roar in a dry variant: a rising ground rumble, then a volcanic blast's roar; no water, no steam hiss; a real recording, heavy, nothing bubbly or popping.
This one: a deep rumble (a distant shuttle or a quake, slowed) swelling for 1.2 s, then a real explosion of Mount Yasur: a rougher, rockier blast than a, 3.4 s.
Edit: 3.40 s, the loudest 100 ms band −17.8 dB, peak −7.4 dBFS, −17.0 LUFS.

### pylon-dome-up-r72-a — "Gym Lights Powering Up" by OBXJohn

Proposed (round 72), cut from the HQ previews by `tools/concept/audio/sfx_r72.py`.
Source: <https://freesound.org/people/OBXJohn/sounds/262510/> (Gym Lights Powering Up by OBXJohn, CC0 1.0).
Use: a Shield Pylon's dome coming up (Level 41): at the level's start of a gate and after an EMP or a `shield-breaker` hit, as the dome flickers back over 0.3 s.
Brief: a short electrical hum rising, the tell that the dome is back; a real recording, heavy, nothing bubbly or popping.
This one: a gym's lights powering up (the ballasts' hum and tick), glided up: a quick rising buzz, 0.45 s.
Edit: 0.45 s, the loudest 100 ms band −31.3 dB, peak −15.8 dBFS, −28.1 LUFS.

### pylon-dome-up-r72-b — "070916 Machine room transformer hum 1.wav" by thaighaudio + "Zapping - High Voltage Electricity Buzz Isolated" by el_boss

Proposed (round 72), cut from the HQ previews by `tools/concept/audio/sfx_r72.py`.
Sources: <https://freesound.org/people/thaighaudio/sounds/349688/> (070916 Machine room transformer hum 1.wav by thaighaudio, CC0 1.0), <https://freesound.org/people/el_boss/sounds/740198/> (Zapping - High Voltage Electricity Buzz Isolated by el_boss, CC0 1.0).
Use: a Shield Pylon's dome coming up (Level 41): at the level's start of a gate and after an EMP or a `shield-breaker` hit, as the dome flickers back over 0.3 s.
Brief: a short electrical hum rising, the tell that the dome is back; a real recording, heavy, nothing bubbly or popping.
This one: a machine room's transformer hum gliding up an octave (0.5 → 1.0) with a faint high-voltage buzz on its end: deeper and smoother than a, 0.45 s.
Edit: 0.45 s, the loudest 100 ms band −40.6 dB, peak −18.9 dBFS, −27.9 LUFS.

### pylon-dome-down-r72-a — "Electric Failure – High Voltage Power Down #2 ATP2 (CC0)" by ATP2-kh

Proposed (round 72), cut from the HQ previews by `tools/concept/audio/sfx_r72.py`.
Source: <https://freesound.org/people/ATP2-kh/sounds/844244/> (Electric Failure – High Voltage Power Down #2 ATP2 (CC0) by ATP2-kh, CC0 1.0).
Use: a Shield Pylon's dome collapsing (Level 41): the pylon's death, an EMP or a `shield-breaker` hit; a reward.
Brief: a falling hum into a hard electrical snap; a real recording, heavy, nothing bubbly or popping.
This one: a high-voltage power failure: a crackling discharge collapsing, 0.9 s.
Edit: 0.90 s, the loudest 100 ms band −28.0 dB, peak −11.6 dBFS, −22.2 LUFS.

### pylon-dome-down-r72-b — "070916 Machine room transformer hum 1.wav" by thaighaudio + "Flash 1,2MV_3.wav" by jensfelger

Proposed (round 72), cut from the HQ previews by `tools/concept/audio/sfx_r72.py`.
Sources: <https://freesound.org/people/thaighaudio/sounds/349688/> (070916 Machine room transformer hum 1.wav by thaighaudio, CC0 1.0), <https://freesound.org/people/jensfelger/sounds/367453/> (Flash 1,2MV_3.wav by jensfelger, CC0 1.0).
Use: a Shield Pylon's dome collapsing (Level 41): the pylon's death, an EMP or a `shield-breaker` hit; a reward.
Brief: a falling hum into a hard electrical snap; a real recording, heavy, nothing bubbly or popping.
This one: the transformer hum falling an octave, cut by a 1.2 MV flash's hard snap: the brief's shape exactly, 0.9 s.
Edit: 0.92 s, the loudest 100 ms band −33.2 dB, peak −13.5 dBFS, −22.0 LUFS.

### graft-regrowth-r72-a — "tree_creak_04.wav" by Department64 + "Door Knock - 18.wav" by Ligidium

Proposed (round 72), cut from the HQ previews by `tools/concept/audio/sfx_r72.py`.
Sources: <https://freesound.org/people/Department64/sounds/95262/> (tree_creak_04.wav by Department64, CC0 1.0), <https://freesound.org/people/Ligidium/sounds/193834/> (Door Knock - 18.wav by Ligidium, CC0 1.0).
Use: a Graft Turret regrowing on its root (Levels 39-42) as the turret stands again.
Brief: a longer twisting creak that ends in a woody knock as the turret stands; dry; a real recording, heavy, nothing bubbly or popping.
This one: a tree creaking in the wind, slowed to 0.9 and swelling, then a single knock on a wooden door: 1.8 s.
Edit: 1.75 s, the loudest 100 ms band −26.3 dB, peak −6.3 dBFS, −27.8 LUFS.

### graft-regrowth-r72-b — "door wood old slow creaks open close or ship hull list2.flac" by kyles + "Door Knock - 26.wav" by Ligidium

Proposed (round 72), cut from the HQ previews by `tools/concept/audio/sfx_r72.py`.
Sources: <https://freesound.org/people/kyles/sounds/455757/> (door wood old slow creaks open close or ship hull list2.flac by kyles, CC0 1.0), <https://freesound.org/people/Ligidium/sounds/193863/> (Door Knock - 26.wav by Ligidium, CC0 1.0).
Use: a Graft Turret regrowing on its root (Levels 39-42) as the turret stands again.
Brief: a longer twisting creak that ends in a woody knock as the turret stands; dry; a real recording, heavy, nothing bubbly or popping.
This one: an old wooden door's (or a ship hull's) slow creak, slowed to 0.85 (the source of round 36's chosen root growth), then a deeper door knock: lower and heavier than a, 1.9 s.
Edit: 1.85 s, the loudest 100 ms band −26.6 dB, peak −15.8 dBFS, −26.0 LUFS.

### jovian-sky-r72-a — "Strong Wind Recorded with Soundfield Microphone Manchester 2" by keirofinch + "EARTHQUAKE OR DISTANT SPACE SHUTTLE RUMBLE.WAV" by metrostock99 + "Large, Low Rumble.wav" by duckduckpony

Proposed (round 72), cut from the HQ previews by `tools/concept/audio/sfx_r72.py`.
Sources: <https://freesound.org/people/keirofinch/sounds/376534/> (Strong Wind Recorded with Soundfield Microphone Manchester 2 by keirofinch, CC0 1.0), <https://freesound.org/people/metrostock99/sounds/203281/> (EARTHQUAKE OR DISTANT SPACE SHUTTLE RUMBLE.WAV by metrostock99, CC0 1.0), <https://freesound.org/people/duckduckpony/sounds/130972/> (Large, Low Rumble.wav by duckduckpony, CC-BY 4.0).
Use: Levels 36 and 38 (Jupiter's cloud bands): the level's ambience bed on the effects bus at the beds' 0.70, under the music; heard in the quiet stretches, a level's start and the pause before a boss; Level 38 +3 dB in the storm sections.
Brief: high wind and a distant thunder bed without baked strikes (the strikes' own thunder follows them); a seamless 16 s loop; a real recording, heavy, nothing bubbly or popping.
This one: strong wind on a soundfield microphone (high, steady) over a deep rumble slowed to 0.7; once a loop a large low rumble swelling and fading (a far roll, no crack): 16.0 s loop.
Edit: 16.00 s, band RMS −42.1 dB, peak −22.0 dBFS, −35.9 LUFS.

### jovian-sky-r72-b — "210523_1598_FR_StrongWind.wav" by kevp888 + "EARTHQUAKE OR DISTANT SPACE SHUTTLE RUMBLE.WAV" by metrostock99 + "Large, Low Rumble.wav" by duckduckpony

Proposed (round 72), cut from the HQ previews by `tools/concept/audio/sfx_r72.py`.
Sources: <https://freesound.org/people/kevp888/sounds/573289/> (210523_1598_FR_StrongWind.wav by kevp888, CC-BY 4.0), <https://freesound.org/people/metrostock99/sounds/203281/> (EARTHQUAKE OR DISTANT SPACE SHUTTLE RUMBLE.WAV by metrostock99, CC0 1.0), <https://freesound.org/people/duckduckpony/sounds/130972/> (Large, Low Rumble.wav by duckduckpony, CC-BY 4.0).
Use: Levels 36 and 38 (Jupiter's cloud bands): the level's ambience bed on the effects bus at the beds' 0.70, under the music; heard in the quiet stretches, a level's start and the pause before a boss; Level 38 +3 dB in the storm sections.
Brief: high wind and a distant thunder bed without baked strikes (the strikes' own thunder follows them); a seamless 16 s loop; a real recording, heavy, nothing bubbly or popping.
This one: a gusting strong wind (lower and more uneven than a) over the deep rumble slowed to half; once a loop the far rumble, deeper: 16.0 s loop.
Edit: 16.00 s, band RMS −42.0 dB, peak −21.4 dBFS, −37.3 LUFS.

### aurelia-r72-a — "Airship propeller engine" by ilm0player + "Industrial factory working 02.wav" by dersinnsspace + "gas vuur dakwerken gasbrander luchtballon.wav" by NickPeeters

Proposed (round 72), cut from the HQ previews by `tools/concept/audio/sfx_r72.py`.
Sources: <https://freesound.org/people/ilm0player/sounds/578181/> (Airship propeller engine by ilm0player, CC0 1.0), <https://freesound.org/people/dersinnsspace/sounds/439402/> (Industrial factory working 02.wav by dersinnsspace, CC0 1.0), <https://freesound.org/people/NickPeeters/sounds/243763/> (gas vuur dakwerken gasbrander luchtballon.wav by NickPeeters, CC0 1.0).
Use: Level 37 (Aurelia, the floating capital): the level's ambience bed on the effects bus at the beds' 0.70, under the music; heard in the quiet stretches, a level's start and the pause before a boss; the burning layer goes over it from the turn.
Brief: city machinery: the floating city's engines and works, distant; no voices, no alarms (they come with the burning layer); a seamless 16 s loop; a real recording, heavy, nothing bubbly or popping.
This one: an airship's propeller engine slowed to 0.6 (big, slow engines holding the city up) over a factory working, muffled; once a loop a gas burner's roar far off (a lift balloon's): 16.0 s loop.
Edit: 16.00 s, band RMS −42.1 dB, peak −20.7 dBFS, −33.5 LUFS.

### aurelia-r72-b — "Engine-Room-Machine-020.wav" by Astounded + "Airship propeller engine" by ilm0player + "gas vuur dakwerken gasbrander luchtballon.wav" by NickPeeters

Proposed (round 72), cut from the HQ previews by `tools/concept/audio/sfx_r72.py`.
Sources: <https://freesound.org/people/Astounded/sounds/520809/> (Engine-Room-Machine-020.wav by Astounded, CC-BY 4.0), <https://freesound.org/people/ilm0player/sounds/578181/> (Airship propeller engine by ilm0player, CC0 1.0), <https://freesound.org/people/NickPeeters/sounds/243763/> (gas vuur dakwerken gasbrander luchtballon.wav by NickPeeters, CC0 1.0).
Use: Level 37 (Aurelia, the floating capital): the level's ambience bed on the effects bus at the beds' 0.70, under the music; heard in the quiet stretches, a level's start and the pause before a boss; the burning layer goes over it from the turn.
Brief: city machinery: the floating city's engines and works, distant; no voices, no alarms (they come with the burning layer); a seamless 16 s loop; a real recording, heavy, nothing bubbly or popping.
This one: an engine room's machinery slowed to 0.8 over the propeller slowed to 0.4 (deeper, heavier than a); once a loop the far burner: 16.0 s loop.
Edit: 16.00 s, band RMS −42.0 dB, peak −30.5 dBFS, −39.2 LUFS.

### io-refinery-r72-a — "Furnace Room 4.WAV" by magnus589 + "EARTHQUAKE OR DISTANT SPACE SHUTTLE RUMBLE.WAV" by metrostock99 + "Large, Low Rumble.wav" by duckduckpony

Proposed (round 72), cut from the HQ previews by `tools/concept/audio/sfx_r72.py`.
Sources: <https://freesound.org/people/magnus589/sounds/379738/> (Furnace Room 4.WAV by magnus589, CC-BY 4.0), <https://freesound.org/people/metrostock99/sounds/203281/> (EARTHQUAKE OR DISTANT SPACE SHUTTLE RUMBLE.WAV by metrostock99, CC0 1.0), <https://freesound.org/people/duckduckpony/sounds/130972/> (Large, Low Rumble.wav by duckduckpony, CC-BY 4.0).
Use: Level 39 (Io's refineries): the level's ambience bed on the effects bus at the beds' 0.70, under the music; heard in the quiet stretches, a level's start and the pause before a boss.
Brief: ground rumble and furnace roar; dry (no water, no steam); a seamless 16 s loop; a real recording, heavy, nothing bubbly or popping.
This one: a furnace room's steady roar over a deep ground rumble; once a loop a large low rumble heaving up and away: 16.0 s loop.
Edit: 16.00 s, band RMS −42.2 dB, peak −15.7 dBFS, −33.4 LUFS.

### io-refinery-r72-b — "Standing above a reactor" by OSFX + "Furnace Room 4.WAV" by magnus589 + "Cracking Earthquake (cracking soil, cracking stone)" by uagadugu + "gas vuur dakwerken gasbrander luchtballon.wav" by NickPeeters

Proposed (round 72), cut from the HQ previews by `tools/concept/audio/sfx_r72.py`.
Sources: <https://freesound.org/people/OSFX/sounds/420538/> (Standing above a reactor by OSFX, CC-BY 4.0), <https://freesound.org/people/magnus589/sounds/379738/> (Furnace Room 4.WAV by magnus589, CC-BY 4.0), <https://freesound.org/people/uagadugu/sounds/222521/> (Cracking Earthquake (cracking soil, cracking stone) by uagadugu, CC0 1.0), <https://freesound.org/people/NickPeeters/sounds/243763/> (gas vuur dakwerken gasbrander luchtballon.wav by NickPeeters, CC0 1.0).
Use: Level 39 (Io's refineries): the level's ambience bed on the effects bus at the beds' 0.70, under the music; heard in the quiet stretches, a level's start and the pause before a boss.
Brief: ground rumble and furnace roar; dry (no water, no steam); a seamless 16 s loop; a real recording, heavy, nothing bubbly or popping.
This one: standing above a reactor (a low industrial roar) with the furnace room under it; once a loop the ground cracking and a gas burner's roar: busier than a, 16.0 s loop.
Edit: 16.00 s, band RMS −42.0 dB, peak −20.0 dBFS, −33.6 LUFS.

### callisto-fortress-r72-a — "ferry engine room2.wav" by mrmayo + "070916 Machine room transformer hum 1.wav" by thaighaudio + "door wood old slow creaks open close or ship hull list2.flac" by kyles + "Muffled Boom" by UnderlinedDesigns

Proposed (round 72), cut from the HQ previews by `tools/concept/audio/sfx_r72.py`.
Sources: <https://freesound.org/people/mrmayo/sounds/74915/> (ferry engine room2.wav by mrmayo, CC0 1.0), <https://freesound.org/people/thaighaudio/sounds/349688/> (070916 Machine room transformer hum 1.wav by thaighaudio, CC0 1.0), <https://freesound.org/people/kyles/sounds/455757/> (door wood old slow creaks open close or ship hull list2.flac by kyles, CC0 1.0), <https://freesound.org/people/UnderlinedDesigns/sounds/191697/> (Muffled Boom by UnderlinedDesigns, CC0 1.0).
Use: Level 41 (Callisto's fortress decks; Levels 40 and 42 keep `belt-open`): the level's ambience bed on the effects bus at the beds' 0.70, under the music; heard in the quiet stretches, a level's start and the pause before a boss.
Brief: fortress machinery heard through the hull (the belt's vacuum treatment: no wind, sounds as through the cockpit): deep machines, a transformer hum, hull creaks, a far heavy clunk now and then; a seamless 16 s loop; a real recording, heavy, nothing bubbly or popping.
This one: a ferry's engine room slowed to 0.6 and muffled (heard through a bulkhead) over a transformer hum; once a loop a slow hull creak and a far muffled boom: 16.0 s loop.
Edit: 16.00 s, band RMS −42.0 dB, peak −24.3 dBFS, −36.0 LUFS.

### callisto-fortress-r72-b — "Engine-Room-door-018.wav" by Astounded + "Low Roomtone" by IENBA + "door wood old slow creaks open close or ship hull list2.flac" by kyles + "Door Knock - 26.wav" by Ligidium

Proposed (round 72), cut from the HQ previews by `tools/concept/audio/sfx_r72.py`.
Sources: <https://freesound.org/people/Astounded/sounds/520807/> (Engine-Room-door-018.wav by Astounded, CC-BY 4.0), <https://freesound.org/people/IENBA/sounds/752273/> (Low Roomtone by IENBA, CC0 1.0), <https://freesound.org/people/kyles/sounds/455757/> (door wood old slow creaks open close or ship hull list2.flac by kyles, CC0 1.0), <https://freesound.org/people/Ligidium/sounds/193863/> (Door Knock - 26.wav by Ligidium, CC0 1.0).
Use: Level 41 (Callisto's fortress decks; Levels 40 and 42 keep `belt-open`): the level's ambience bed on the effects bus at the beds' 0.70, under the music; heard in the quiet stretches, a level's start and the pause before a boss.
Brief: fortress machinery heard through the hull (the belt's vacuum treatment: no wind, sounds as through the cockpit): deep machines, a transformer hum, hull creaks, a far heavy clunk now and then; a seamless 16 s loop; a real recording, heavy, nothing bubbly or popping.
This one: a ship's engine-room machinery slowed to 0.6 and muffled over a deep room tone; once a loop the hull creak and a door knock slowed to half (a far heavy clunk): darker than a, 16.0 s loop.
Edit: 16.00 s, band RMS −42.1 dB, peak −23.9 dBFS, −35.0 LUFS.

## Concept round 73, step AUD-A (M10 part A: the mirror's heat loop and its climb render, break and regrow chime, the Shield Pylon dome's hit, the Reflex bolt absorbed on a mirror)

### mirror-heat-r73-a — "Gas Torch Burn" by SyntaxSeed + "Torch - Full work cycle of cutting metal" by ldezem

Proposed (round 73), cut from the HQ previews by `tools/concept/audio/sfx_r73.py`.
Sources: <https://freesound.org/people/SyntaxSeed/sounds/244230/> (Gas Torch Burn by SyntaxSeed, CC0 1.0), <https://freesound.org/people/ldezem/sounds/386382/> (Torch - Full work cycle of cutting metal by ldezem, CC0 1.0).
Use: a beam held on a Mirror Interceptor's mirror face (Levels 38-42; the Ascendant's panels too): one loop while any mirror heats, for the nearest, its pitch rising with the heat (0.85 at 0 % to 1.35 at the break, about 1.0 s of the plan's beam) and stopping when the beam leaves or the mirror breaks; often in a mirror fight, under the beam's own loop.
Brief: a rising hot-metal hiss: a gas torch on sheet steel, its pitch climbing as the plate goes red to white; dry (no quench, no steam, nothing bubbly); this file is the steady loop the game pitches; a real recording, nothing bubbly or popping.
This one: a gas torch's steady burn over the hiss of a cutting torch biting into steel plate: a dry roaring hiss, bright on top, 2.0 s loop.
Edit: 2.00 s, band RMS −34.1 dB, peak −19.1 dBFS, −28.6 LUFS.

### mirror-heat-r73-b — "070916 Machine room transformer hum 1.wav" by thaighaudio + "welder 2.wav" by SpeedY

Proposed (round 73), cut from the HQ previews by `tools/concept/audio/sfx_r73.py`.
Sources: <https://freesound.org/people/thaighaudio/sounds/349688/> (070916 Machine room transformer hum 1.wav by thaighaudio, CC0 1.0), <https://freesound.org/people/SpeedY/sounds/62923/> (welder 2.wav by SpeedY, CC0 1.0).
Use: a beam held on a Mirror Interceptor's mirror face (Levels 38-42; the Ascendant's panels too): one loop while any mirror heats, for the nearest, its pitch rising with the heat (0.85 at 0 % to 1.35 at the break, about 1.0 s of the plan's beam) and stopping when the beam leaves or the mirror breaks; often in a mirror fight, under the beam's own loop.
Brief: a rising hot-metal hiss: a gas torch on sheet steel, its pitch climbing as the plate goes red to white; dry (no quench, no steam, nothing bubbly); this file is the steady loop the game pitches; a real recording, nothing bubbly or popping.
This one: an arc welder at work: a machine-room transformer's hum under the welding arc's dry crackle, more of an electric hum than a, 2.0 s loop.
Edit: 2.00 s, band RMS −34.8 dB, peak −17.6 dBFS, −28.0 LUFS.

### mirror-heat-demo-r73-a — "Gas Torch Burn" by SyntaxSeed + "Torch - Full work cycle of cutting metal" by ldezem

Proposed (round 73), cut from the HQ previews by `tools/concept/audio/sfx_r73.py`.
Sources: <https://freesound.org/people/SyntaxSeed/sounds/244230/> (Gas Torch Burn by SyntaxSeed, CC0 1.0), <https://freesound.org/people/ldezem/sounds/386382/> (Torch - Full work cycle of cutting metal by ldezem, CC0 1.0).
Use: review file for the heat loop: what the game plays while a beam heats a mirror to its break in 1.0 s (the loop gliding 0.85 → 1.35); the game plays the loop itself.
Brief: a rising hot-metal hiss: a gas torch on sheet steel, its pitch climbing as the plate goes red to white; dry (no quench, no steam, nothing bubbly); a real recording, nothing bubbly or popping.
This one: loop a (the gas torch on steel) climbing over 1.0 s, as heard in the game, 1.25 s.
Edit: 1.25 s, band RMS −34.1 dB, peak −19.4 dBFS, −28.9 LUFS.

### mirror-heat-demo-r73-b — "070916 Machine room transformer hum 1.wav" by thaighaudio + "welder 2.wav" by SpeedY

Proposed (round 73), cut from the HQ previews by `tools/concept/audio/sfx_r73.py`.
Sources: <https://freesound.org/people/thaighaudio/sounds/349688/> (070916 Machine room transformer hum 1.wav by thaighaudio, CC0 1.0), <https://freesound.org/people/SpeedY/sounds/62923/> (welder 2.wav by SpeedY, CC0 1.0).
Use: review file for the heat loop: what the game plays while a beam heats a mirror to its break in 1.0 s (the loop gliding 0.85 → 1.35); the game plays the loop itself.
Brief: a rising hot-metal hiss: a gas torch on sheet steel, its pitch climbing as the plate goes red to white; dry (no quench, no steam, nothing bubbly); a real recording, nothing bubbly or popping.
This one: loop b (the arc welder's hum) climbing over 1.0 s, as heard in the game, 1.25 s.
Edit: 1.25 s, band RMS −35.8 dB, peak −19.2 dBFS, −28.0 LUFS.

### mirror-break-r73-a — "Glass Shattering and Falling" by 221Beimesche

Proposed (round 73), cut from the HQ previews by `tools/concept/audio/sfx_r73.py`.
Source: <https://freesound.org/people/221Beimesche/sounds/336425/> (Glass Shattering and Falling by 221Beimesche, CC0 1.0).
Use: a Mirror Interceptor's mirror breaking (Levels 38-42): broken by its heat, a `shield-breaker` hit or the EMP, as its shards fly (and the Ascendant's panels); a reward, a few a fight.
Brief: a real glass pane shattering, heavier and longer than the enemy shield's break (round 60's glass break) and apart from the player shield's break (a falling zap), so the three never mix; a real recording, nothing bubbly or popping.
This one: a glass pane shattering and its shards falling, slowed to 0.85 so it reads as a big heavy plate: a long crash with a rain of shards, 2.8 s.
Edit: 2.80 s, the loudest 100 ms band −22.2 dB, peak −11.0 dBFS, −31.2 LUFS.

### mirror-break-r73-b — "BROKEN CAR WINDOW.wav" by thecommondanger

Proposed (round 73), cut from the HQ previews by `tools/concept/audio/sfx_r73.py`.
Source: <https://freesound.org/people/thecommondanger/sounds/587928/> (BROKEN CAR WINDOW.wav by thecommondanger, CC0 1.0).
Use: a Mirror Interceptor's mirror breaking (Levels 38-42): broken by its heat, a `shield-breaker` hit or the EMP, as its shards fly (and the Ascendant's panels); a reward, a few a fight.
Brief: a real glass pane shattering, heavier and longer than the enemy shield's break (round 60's glass break) and apart from the player shield's break (a falling zap), so the three never mix; a real recording, nothing bubbly or popping.
This one: a car's side window (tempered glass) breaking: a bright burst into small grains, shorter and drier than a, 0.95 s.
Edit: 0.95 s, the loudest 100 ms band −30.3 dB, peak −14.7 dBFS, −22.3 LUFS.

### mirror-regrow-r73-a — "Singing Bowl - Soft Stick Meditative Hit" by Rimych

Proposed (round 73), cut from the HQ previews by `tools/concept/audio/sfx_r73.py`.
Source: <https://freesound.org/people/Rimych/sounds/616335/> (Singing Bowl - Soft Stick Meditative Hit by Rimych, CC0 1.0).
Use: a mirror coming back (Levels 38-42): with its 0.4 s silver shimmer, 3 s after a break; now and then, the tell that the mirror stands again.
Brief: a low soft chime: a singing bowl struck softly, short; never a ping (the bounce) or a bell's alarm; a real recording, nothing bubbly or popping.
This one: a deep singing bowl struck with a soft stick, sped to 1.5: a warm low hum-tone that blooms and fades, 1.0 s.
Edit: 1.00 s, the loudest 100 ms band −27.9 dB, peak −21.7 dBFS, −27.6 LUFS.

### mirror-regrow-r73-b — "singing bowl strike sound" by inoshirodesign

Proposed (round 73), cut from the HQ previews by `tools/concept/audio/sfx_r73.py`.
Source: <https://freesound.org/people/inoshirodesign/sounds/271370/> (singing bowl strike sound by inoshirodesign, CC0 1.0).
Use: a mirror coming back (Levels 38-42): with its 0.4 s silver shimmer, 3 s after a break; now and then, the tell that the mirror stands again.
Brief: a low soft chime: a singing bowl struck softly, short; never a ping (the bounce) or a bell's alarm; a real recording, nothing bubbly or popping.
This one: a smaller singing bowl struck softly: a brighter, clearer chime than a, ringing higher, 1.0 s.
Edit: 1.00 s, the loudest 100 ms band −28.0 dB, peak −19.4 dBFS, −28.1 LUFS.

### pylon-dome-hit-r73-a — "Civilian Stun Gun 1/10s zap" by AnthonyChan0 + "23cm glass bowl - tone - wooden spoon 01.wav" by Anthousai

Proposed (round 73), cut from the HQ previews by `tools/concept/audio/sfx_r73.py`.
Sources: <https://freesound.org/people/AnthonyChan0/sounds/264779/> (Civilian Stun Gun 1/10s zap by AnthonyChan0, CC0 1.0), <https://freesound.org/people/Anthousai/sounds/448070/> (23cm glass bowl - tone - wooden spoon 01.wav by Anthousai, CC0 1.0).
Use: a shot hitting a Shield Pylon's dome (Level 41; throttled: once per 0.15 s), the dome absorbing it; often in a pylon gate.
Brief: a short bright electric zap with a glassy ring; never the Level 17 colony dome's big glass boom, never the mirror's ping; a real recording, nothing bubbly or popping.
This one: a stun gun's tenth-of-a-second zap with a glass bowl struck under it (sped to 1.5): a crackling snap and a short clear ring, 0.47 s.
Edit: 0.47 s, the loudest 100 ms band −29.6 dB, peak −9.7 dBFS, −26.1 LUFS.

### pylon-dome-hit-r73-b — "ELECTRIC_ZAP_001.wav" by JoelAudio + "ANI - Wine Glass - Hard" by ani_music

Proposed (round 73), cut from the HQ previews by `tools/concept/audio/sfx_r73.py`.
Sources: <https://freesound.org/people/JoelAudio/sounds/136542/> (ELECTRIC_ZAP_001.wav by JoelAudio, CC0 1.0), <https://freesound.org/people/ani_music/sounds/198401/> (ANI - Wine Glass - Hard by ani_music, CC0 1.0).
Use: a shot hitting a Shield Pylon's dome (Level 41; throttled: once per 0.15 s), the dome absorbing it; often in a pylon gate.
Brief: a short bright electric zap with a glassy ring; never the Level 17 colony dome's big glass boom, never the mirror's ping; a real recording, nothing bubbly or popping.
This one: an electric zap's buzzy snap with a wine glass struck hard under it: a thinner, higher ring than a, 0.47 s.
Edit: 0.47 s, the loudest 100 ms band −36.0 dB, peak −12.2 dBFS, −26.2 LUFS.

### bolt-absorbed-r73-a — "Quick static noise" by Rvgerxini

Proposed (round 73), cut from the HQ previews by `tools/concept/audio/sfx_r73.py`.
Source: <https://freesound.org/people/Rvgerxini/sounds/456827/> (Quick static noise by Rvgerxini, CC0 1.0).
Use: a Reflex Shield bolt hitting a standing mirror's face (Levels 38-42; a spark, no bounce): now and then while the Reflex turns enemy fire, quiet, under the fire.
Brief: a short dry electric tick; quiet; a real recording, nothing bubbly or popping.
This one: a quick burst of static cut to a tick: a dry electric crackle, 0.1 s.
Edit: 0.10 s, the loudest 100 ms band −32.0 dB, peak −10.9 dBFS, −inf LUFS.
