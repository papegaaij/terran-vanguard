# Mining tug — concept prompts

Round 60 (Act 5 "The Belt" world proposals, step ART-P-world): a/b mockups by `tools/concept/belt_r60.py` on the
frozen `render` package (`python3 tools/concept/belt_r60.py tug [a|b]`), pending the user's review in round 60. Each
section (bow, cargo, engines) is rendered on its own in three states (intact, damaged, lost) and composed, as the
game would draw them.

**Shared look:** late-1990s pre-rendered CGI game sprite, straight top-down orthographic, nose up, lit from the
top-left (the belt's sun). A Coalition-era civilian belt mining tug, older than anything else on the screen: belt
grime, patched plates, warm yellow and white paint, cool white-cyan running lights, no red; the name *BELLWETHER*
hand-painted on the bow. Three visibly separate sections along the hull (seams, colour shifts), so a dark section
reads as lost. Footprint 160×260 px.

**Shared negative prompt:** photo, painterly, cel shading, perspective or 3/4 view, motion blur, black-and-gold
Ascendancy livery, red lights, military markings, weapons, sleek warship lines.

## mining-tug-r60-a

**Mining tug A, ore frame (ally, Level 31)**. Files: `mining-tug-r60-a.png` (sheet) and `mining-tug-r60-a.gif`
(the play field).

- **Prompt:** The shared look. An open ore frame: two long spine girders striped yellow and dark grey, grey cross
  girders; the bow a blunt off-white bridge with a lit window strip, a yellow crane arm and an ore grapple folded
  back; the cargo three white habitat cans lying along the frame with rows of warm lit windows (people inside),
  lashed on with dark cables, beside two ochre ore containers; the engines three big grey drive bells with a
  blue-white glow inside a grey fuel-tank ring, a yellow stripe across the frame.
- **Negative prompt:** the shared list above.
- **Animation brief:** Holds its station and sways on a 14 s figure-eight (x ± 20, y ± 5); the living drive bells
  glow blue-white. A hit flashes the section white; below 50 % a thin venting plume drifts back with the scroll and
  the section's lights flicker; a lost section: a short fire flare, then dark with a scorched outline, the tug flies
  on. All three lost: the hull breaks at the cargo's joints and the pieces drift down off the screen.
- **Mockup generator:** `python3 tools/concept/belt_r60.py tug a`

## mining-tug-r60-b

**Mining tug B, pressure hull (ally, Level 31)**. Files: `mining-tug-r60-b.png` (sheet) and `mining-tug-r60-b.gif`.

- **Prompt:** As A, but one enclosed, older pressure hull with a waist at each section joint: a cream bow with a
  small bridge window and a yellow-and-black hazard chevron band at its rear edge; the cargo a long white and
  blue-grey middle with rows of warm windows down its spine and two grey tanks strapped along its sides; the
  engines a darker grey stern with hazard chevrons, one big drive bell and two small ones.
- **Negative prompt:** the shared list above.
- **Animation brief:** as A.
- **Mockup generator:** `python3 tools/concept/belt_r60.py tug b`
