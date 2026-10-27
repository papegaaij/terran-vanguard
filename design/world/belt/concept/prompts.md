# The belt — concept prompts

Round 60 (Act 5 "The Belt" world proposals, step ART-P-world): mockups by `tools/concept/belt_r60.py` on the frozen
`render` package, pending the user's review in round 60. The backdrops are `tools/concept/enemies_r60.py`'s belt
backdrops (station hull, belt space with the sunlit dust band, warm sunlit rock, the tunnels, the graveyard) with
this round's set dressing; the units on them are that script's round-60 units and the production player for scale.
They show direction, not production backdrops (those come in the M9 parts).

**Shared look:** late-1990s pre-rendered CGI vertical shooter backdrop, straight top-down, harsh distant sunlight from
the top-left, very high contrast; dark brown-grey rock, orange refinery light, cool white station lights; the
Ascendancy's gloss black, gold and red on top.

**Shared negative prompt:** photo, painterly, perspective horizon, atmosphere haze or clouds (vacuum), water, text.

## belt-moods-r60-a

**The belt, mood ladder A — a step lighter (Q11 a)**: one still per level, L29–L35, each with its mean luminance and
its darkest 200×96 window. File: `belt-moods-r60-a.png`.

- **Prompt:** Seven top-down stills for one act: (L29) a lit grey-white station hull with window strips and white
  work lamps, one block turned red with a waking missile nest and a black-and-gold fighter wing; (L30) open belt
  space crossed by a warm sunlit dust band, beacon buoys with amber lights along a shipping lane, a line of black
  mines, a black-and-gold gunship; (L31) open space with big sunlit drifting asteroids and a yellow-white mining tug;
  (L32) a tunnel through a hollowed asteroid between dark rock walls with cool work lamps, a refinery's orange
  furnace glow ahead; (L33) warm sunlit rock with craters under dark violet Vrell growth with teal specks, a dug-in
  rail bunker, black-violet leeches; (L34) torn derelict hull slabs drifting over belt space, a black-and-gold tank,
  a yellow mining charge; (L35) the black-and-gold rim of a vast ring station entering from the top over Ceres's
  cratered curve far below, a thin red telegraph line across the screen. Grade A: every black hull has a lighter
  ground behind it (the sunlit band, warm sunlit rock faces, station plating a step lighter than Act 4's).
- **Negative prompt:** the shared list above.
- **Mockup generator:** `python3 tools/concept/belt_r60.py moods a`

## belt-moods-r60-b

**The belt, mood ladder B — harsh (as Act 4)**: the same seven scenes under a harsh grade. File:
`belt-moods-r60-b.png`.

- **Prompt:** As A, but deep shadows, a dimmer dust band and darker rock, cool and severe, as Act 4's darkness.
- **Negative prompt:** the shared list above.
- **Mockup generator:** `python3 tools/concept/belt_r60.py moods b`

## belt-readability-r60-a

**Q11 readability frame A — gloss black, gold trim, strong rim**: three Ascendancy units (Talon leader, Hornet, SAM
Nest) at play size over the darkest 200×96 window of every level's still, in grade A and grade B, with their
measured gold share, and a play-field frame (L31). File: `belt-readability-r60-a.png`.

- **Prompt:** Late-1990s pre-rendered CGI sprites, top-down: Ascendancy craft in gloss black hex-panelled armour,
  gold trim on at least a fifth of the silhouette, a 2 px bright gold rim on the edges facing the top-left sun, a red
  rim on the far edges, red running lights; over dark belt rock and space.
- **Negative prompt:** the shared list above.
- **Mockup generator:** `python3 tools/concept/belt_r60.py frame a`

## belt-readability-r60-b

**Q11 readability frame B — gunmetal hulls with gold**. File: `belt-readability-r60-b.png`.

- **Prompt:** As A, but gunmetal/charcoal hulls (#5a606c) with the same gold and a softer rim.
- **Negative prompt:** the shared list above.
- **Mockup generator:** `python3 tools/concept/belt_r60.py frame b`

## belt-readability-r60-c

**Q11 readability frame C — black silhouettes, gold accents**. File: `belt-readability-r60-c.png`.

- **Prompt:** As A, but black hulls with the gold dimmed to bronze accents, no rim light; the units read by their
  bright red lights.
- **Negative prompt:** the shared list above.
- **Mockup generator:** `python3 tools/concept/belt_r60.py frame c`
