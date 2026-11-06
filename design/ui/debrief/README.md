---
title: Debrief screen
design: approved
implementation: done
art: final
depends-on: [../../systems/scoring, ../../systems/economy, ../../systems/saves]
updated: 2026-11-06
---

# Debrief screen

## Summary

After a level is completed, the debrief tallies the results with counting-up numbers and
satisfying sounds, awards the grade, and adds the grade's credit bonus. Then the game moves on
to the next briefing.

## Design

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                     MISSION 21 COMPLETE · DUST COLOSSUS                      │
├──────────────────────────────────────────────────────────────────────────────┤
│   ENEMIES DESTROYED        184 / 203     91 %   + 18 200   RATING ─────────  │
│   ARMOUR DAMAGE TAKEN      12                              KILLS     36 / 40 │
│   SECRETS FOUND            2 / 3                           ARMOUR    21 / 30 │
│   MAX CHAIN                58   ×3.5                       SECRETS   10 / 15 │
│   BOSS RUSH                1:42  (par 2:00)        + 6 000  CHAIN     15 / 15 │
│                                                            ───────────────── │
│                                                            RATING         82 │
│                                                                     A+ AT 85 │
│                                                                              │
│   CREDITS EARNED           2 710                                             │
│   GRADE BONUS  A  +20 %      542                                             │
│   ────────────────────────────────                                           │
│   TOTAL CREDITS           15 702                            ┌─────┐          │
│   SCORE                1 382 900                            │  A  │          │
│                                                             └─────┘          │
│                                                  [ENTER] continue            │
└──────────────────────────────────────────────────────────────────────────────┘
```

(Example: level 21 *Dust Colossus*, budget ≈ 3 870 per the
[economy](../../systems/economy/README.md#per-level-budget) curve; numbers are illustrative.
Total credits = balance at level start + credits earned + grade bonus.)

- Lines appear one after another, 0.3 s apart, with a tick SFX while numbers count; confirm
  skips the animation. Back (Esc, the gamepad's back button) does what confirm does: there is
  nothing to go back to.
- The grade stamp lands with a heavy SFX. A new best grade for the level gets a "NEW BEST" tag.
- With the stamp, the **grade breakdown** appears above it in the right column: the rating's parts
  ([scoring](../../systems/scoring/README.md#grades)) as points of their weight (KILLS of 40,
  ARMOUR of 30, SECRETS of 15, CHAIN of 15; a part at full marks in green), a rule, the RATING
  (rounded down, so it never shows a threshold the grade missed) and the next grade's threshold
  (`A+ AT 85`; the top grade shows its own).
- **BOSS TIME** (levels with a boss or mid-boss: L05, L07): the time from the boss bar appearing
  to the kill, the boss's par in brackets and, under par, the
  [Boss rush](../../systems/scoring/README.md#level-end-bonuses) bonus: the row's label then reads
  **BOSS RUSH** and the amount stands alone in the right column (`BOSS RUSH  0:46 (PAR 1:06)  +12 000`;
  2026-11-03: the word in the amount ran into the times); without a boss the row is left out. The
  widest row, `9:59 (PAR 9:59)` with `+99 999`, keeps two characters between the time and the amount
  (`DebriefActEndTest`).
- Data cores found in the level show as a small list with their lore titles (readable later;
  the title is the secret's name in capitals, `SETTLEMENT LOG`).
- **Act summary** (act-final debriefs, M4 part G): after the level's own tally and grade, confirm
  turns to a second page instead of leaving: `ACT I COMPLETE · FIRST CONTACT`, one row per level
  of the act (number, name, best grade, the credits it banked with its grade bonus and its kills),
  the act's totals (kills, credits) and the data cores found in the act with their lore titles;
  confirm then goes on to the act outro. A level a save did not record (saves from before format
  version 2, see [saves](../../systems/saves/README.md)) shows dashes and is left out of the
  totals, which then say so (`6 OF 7 MISSIONS RECORDED`). A replay from mission select has no act
  summary.
- **Secondary objective row**: shows the objective's result; the Level 17 "No dome lost" secondary
  reads `DOMES STANDING n / 5` (the number of domes still standing at the end).
- **Hand-over from the level**: the debrief takes over 15 s after the level's end, but waits while a
  closing radio line has not been heard in full (see [Voice](../../audio/voice/README.md#playback)).

### The campaign end (Act 7, draft)

*Draft, step `m11-d-end` (2026-11-04), decided 2026-11-05, round 84 (Q12 = a). The source of truth for the
order after Level 50; the [act](../../campaign/act-7-beyond-the-gate/README.md) and Level 50's README link it, and own
what plays before the debrief (the Heart's death, the closing gate) and the ending pages' texts and images.*

Act 7 has no act outro and no `next_act`: the campaign's last level ends the game. Level 50's win runs, in order:

1. **The level's end** (Level 50's: the Choir Heart dies, the closing gate and the race back through it, the near
   side, the closing lines); the debrief takes over as after every level (the 15 s rule, waiting for the last line).
2. **The final debrief**: Level 50's tally (the `BOSS TIME` / `BOSS RUSH` row for the Heart), its grade, then the
   **act summary** `ACT VII COMPLETE · BEYOND THE GATE` (Levels 43–50) as every act's last debrief.
3. **The ending pages** in the act outro's place: the [briefing screen](../briefing/README.md)'s format (voiced pages
   with their images, confirm turns a page), track 27 *Ending* under them ([music](../../audio/music/README.md)); the
   texts and images are the act README's.
4. **The end card, campaign form** (the release's end card's layout, `ReleaseEndScreen`):

   ```
                        CAMPAIGN COMPLETE                    (heading, x2)
               MEDIUM - ALL 50 MISSIONS FLOWN                (cyan)
         ┌ CAMPAIGN ─────────────────────────────┐
         │ SCORE                       4 812 300 │           (green)
         │ ENEMIES DESTROYED              21 406 │
         │ DATA CORES FOUND              38 OF 47 │
         │ A+ GRADES                     19 OF 50 │
         │ SHIPS LOST                          63 │
         │ PLAYTIME                       9:41:07 │
         └───────────────────────────────────────┘
                       THE GATE IS DARK.                     (amber, in TO BE CONTINUED's place)
          EVERY MISSION IS OPEN FOR REPLAY IN MISSIONS.      (body, in the next act's line's place)
                         AUTOSAVED                           (as ever)
   ```

   The difficulty and the totals are the release's end card's (the numbers above are illustrative); `DATA CORES FOUND`
   gains the game's total (`n OF m`), and the A+ count runs over the 50 missions. Track 27 plays on under the card and
   the name entry. The card writes the **completed campaign** to the autosave
   ([saves](../../systems/saves/README.md#the-completed-campaign-act-7-draft)); never in a debug run (`AUTOSAVE OFF -
   DEBUG RUN`).
5. **The name entry** when the score makes the difficulty's top 10, with the completion mark
   ([scoring](../../systems/scoring/README.md#the-campaign-end-act-7-draft)).
6. **The credits roll** with track 28 *Credits* ([credits](../credits/README.md#after-the-final-level-act-7-draft)),
   then the **main menu**, where Continue now opens the mission select
   ([main menu](../main-menu/README.md#after-the-campaign-act-7-draft)).

Rules:
- The order runs on every difficulty and only in a campaign: a **replay** of Level 50 ends at its debrief (no act
  summary, no ending pages, no end card) and returns to the mission select, as every replay.
- A **debug run** (`--level 50`, `--invulnerable`, …) plays the whole order and writes nothing (no save, no score),
  as the release's end does today. `--act-end` changes nothing for Level 50 (it ends its act anyway); on an earlier
  level it keeps its meaning (that act's summary, outro, hangar), never the campaign end.
- The **release's end** stays for a build whose last built level is below 50 (M11's builds move it level by level as
  before); a build with Level 50 has no release's end and no `COMING SOON` hangar.
- A **hard game over** in Level 50 is a game over like any other (the game over screen, its entry reading `M50`),
  never the campaign end.

## Concept art

Concept [round 08](../../concept-rounds/round-08/README.md) — glass style (out-of-game) per the ui style rule; generator `tools/concept/ui_r08.py`. Prompts: [concept/prompts.md](concept/prompts.md).

| File | What | Status |
|---|---|---|
| [concept/debrief-r08-a.png](concept/debrief-r08-a.png) | Debrief — mission 10 complete: tally, credits breakdown, chain and score, grade stamp | chosen |
| [concept/mission-failed-r08-a.png](concept/mission-failed-r08-a.png) | Mission failed — frozen frame tinted red, retry / retry from boss / hangar / quit, discarded earnings | chosen |
| [concept/game-over-r08-a.png](concept/game-over-r08-a.png) | Game over (hard, 0/3 retries) — campaign stats, Okafor's last transmission, top-10 with letter-grid name entry | chosen |

Production art, UI batch part U2 (for concept round 13, opened by part U3): no art of its own; the screen draws the production glass kit ([tools/art/ui_kit.py](../../../tools/art/README.md)), its sheet made from the capture by `tools/art/ui_review.py`. Prompts: [concept/prompts.md](concept/prompts.md).

| File | What | Status |
|---|---|---|
| [concept/debrief-final-r13-a.png](concept/debrief-final-r13-a.png) | Review sheet: the game capture at 1× with a 2× detail of the title, tally and header rules | chosen |
| [concept/debrief-capture-final-r13-a.png](concept/debrief-capture-final-r13-a.png) | Game capture: Level 01 complete (an invulnerable run at debug speed 8), the tally and credits on glass over the dimmed title scene, the grade on its amber-trimmed glass | chosen |
| [concept/campaign-end-capture-r88-a.png](concept/campaign-end-capture-r88-a.png) | Game capture (A4 rig: Act 6 made the last act, its outro pages standing in for the ending): the ending pages' place, the end card's campaign form, the name entry's `WON` | chosen (round 88 F.7 accept) |

## Implementation

- [x] Tally sequence with count-up animation and skip
- [x] Grade calculation display and credit bonus
- [x] Back (Esc, the gamepad's back button) skips and goes on like confirm (`DebriefExitTest`)
- [x] Grade breakdown: the rating's parts, the rating and the next grade's threshold beside the stamp (`DebriefRatingTest`)
- [x] Data core list with the lore titles (M4 part G)
- [x] Act summary page after the act's last level, from the save's per-level records; not on a
      replay (M4 part G)
- [x] BOSS TIME row with the par and the Boss rush bonus in levels with a boss (M4 part G; the
      bonus itself is paid since part E)
- [x] Game over screen to game-over-r08-a with the last transmission and the top 10 with its name
      entry (M6 part B; see [retry](../../systems/retry/README.md) and
      [scoring](../../systems/scoring/README.md#high-scores))
- [ ] The campaign end after Level 50 as in *The campaign end (Act 7, draft)*: the final debrief with the act
      summary, the ending pages (track 27), the end card's campaign form (`CAMPAIGN COMPLETE`, the difficulty, the
      totals, `THE GATE IS DARK.`), the name entry, the credits (track 28), the main menu; the completed campaign
      autosaved; a replay and a hard game over in Level 50 untouched; a route test (as `OutroOnwardTest`) — M11 (the route and the screens built in M11 part A on fixtures, step A3b: `CampaignEndTest`, `EndCardLayoutTest`; in place with Level 50, part H)

## Open questions

None open: round 84's items were decided 2026-11-05 (see *Decisions*).

## Decisions

- 2026-09-30: Grade and bonuses as defined in [scoring](../../systems/scoring/README.md).
- 2026-10-01: Concept round 08: accepted.
- 2026-10-01: Approved by the user in the [Acts 1–2 design review](../../reviews/acts-1-2/README.md).
- 2026-10-02: M2 placeholder (`vanguard.game.screen.DebriefScreen`), in the glass style of
  the chosen debrief-r08-a: the mission complete jingle, the tally (kills with the Destruction
  bonus, armour damage, secrets with Explorer, max chain, secondary objective), the credits by
  source with the grade bonus and the total, the score and the grade stamp; lines 0.3 s apart with
  a tick while their numbers count up, the total chime and the stamp sound. Confirm skips, then
  returns to the title. No "NEW BEST" tag until there are saves.
- 2026-10-02: M3 part A: drawn with the UI kit's fonts (20×30 title and grade, 10×20 rows, 8×12
  label); confirm at the end returns to the main menu until the briefing and the hangar follow
  (part B).
- 2026-10-02: M3 part B1: the debrief banks the level in the campaign (credits with the grade bonus,
  score, kills, the armour left, the best grade) and shows "NEW BEST" under the stamp when the grade
  beats the level's recorded best; confirm at the end goes to the next level's briefing, or to the
  hangar while the next level is not built (after Level 01 for now).
- 2026-10-02: M3 close-out (user decision): the data core list and the act summary move to M4; the document is done for M3.
- 2026-10-02: Production art, UI batch part U2: the debrief draws the production glass kit (a
  glass panel with its trim, header rules, the grade on an amber-trimmed glass card) over the title
  scene dimmed to 35 % (Earth orbit, Level 01's setting) instead of flat fills over black; layout
  and fonts unchanged. Review files proposed for round 13; `art` stays `chosen`.
- 2026-10-02: Concept round 13 closed (user decision): the debrief in the production glass kit over the dimmed title scene approved as **final**; it has no art of its own, so `art: final`.
- 2026-10-04: The top grade is **A+** (user decision, see [scoring](../../systems/scoring/README.md)):
  two full-size 20×30 glyphs at ×3 overrun the 110 px stamp, so the letter stays at ×3 and the plus
  is drawn at ×2, raised beside it.
- 2026-10-04: Back on the debrief (user decision): Esc and the gamepad's back button go on like
  confirm (skip the count-up, then leave), since the debrief has nothing to go back to. Test:
  `DebriefExitTest`.
- 2026-10-05: Grade breakdown in the debrief (user decision): KILLS, ARMOUR, SECRETS and CHAIN as
  points of their weight, the rating and the next grade's threshold (`RATING 88`, `A+ AT 85`), in
  the right column above the stamp with an amber RATING heading and trim rules like the tally's; it
  appears with the stamp. The tally's and credits' heading rules now end at the number column
  (they ran under the new column). The sim's `LevelResult.Rating` carries the parts and the target
  grade.
- 2026-10-05: M4 part G (user decision D2 and its defaults): the act summary comes in the act's
  last debrief, before the act outro; it sums each won level's banked credits (with the grade
  bonus) and kills, which the save records from format version 2 on (older saves show what is
  recorded). Main-agent layout choices: a second page after the level's tally, rows per level
  with the best grade, the act's data cores with their lore titles, and no summary on a replay.
  The BOSS TIME row of the mock, missing since part E, is built with it. Status back to
  `in-progress` for these items.
- 2026-10-05: Round 25 closed (user): the act summary layout, the DATA CORE rows and the BOSS TIME
  row accepted as built (seen in the Level 07 capture); every item is done.
- 2026-10-12: M6 part B: the game over screen (the chosen game-over-r08-a, listed here) is built
  to its concept: the campaign stats, Okafor's voiced last transmission and the hard top 10 with
  the letter-grid name entry, typing and the mouse; **pending the user's review in round 35** (Q2,
  Q4, Q5). Details in [retry](../../systems/retry/README.md) and
  [scoring](../../systems/scoring/README.md#high-scores).
- 2026-10-12: Back to `design: review` until [concept round 35](../../concept-rounds/round-35/README.md) closes (C2: the game over screen differs from the chosen game-over-r08-a).
- 2026-10-12 (user, round 35): the game over screen accepted as built, with its differences from
  game-over-r08-a (C2: `GAME OVER` in the red heading font, SHIPS LOST instead of CREDITS SPENT,
  Okafor's voiced line, the footer, `SP` instead of `.`), Q2 = a, Q4 and Q5; `design: approved`.
- 2026-10-19 (user, round 41, P.e): the debrief music wiring accepted (track 29 "Stand Down" at 0.8 after the jingle, under the act summary, 0.5 s fade, a cut jingle on an early exit).
- 2026-10-19: The Level 17 debrief's secondary row reads `DOMES STANDING n / 5` for the "No dome lost"
  secondary (user decision, "Both"). The hand-over from the level waits for a closing radio line
  that has not been heard in full (the radio outro fix, see Voice); **pending the user's review**.
- 2026-11-03 (user, Act 6 playthrough; step PT2): the boss row's `0:46 (par 1:06) rush +12 000` ran the `)` into the
  `r`. The bonus word moved to the label (`BOSS RUSH` when the rush pays, `BOSS TIME` otherwise), the amount alone on
  the right: the widest case keeps a 20 px gap.
- 2026-11-04 (step `m11-d-end`, decided 2026-11-05, round 84, Q12 = a): *The campaign end (Act 7, draft)*:
  after Level 50 the final debrief with Act 7's summary, the ending pages in the outro's place (track 27), the end
  card's campaign form, the name entry, the credits (track 28) and the main menu. Decided as trivia: the end card keeps
  the release's card layout and totals (`DATA CORES FOUND` gains the game's total), `THE GATE IS DARK.` in `TO BE
  CONTINUED`'s place (the story's epilogue line), a replay or a hard game over in Level 50 never reaches it, and the
  release's end stays only for builds that stop short of Level 50.
- 2026-11-05, round 84 (the user's page answers; decided as drafted unless stated, the alternatives rejected). The open items were:
  - **The campaign end** (Q12 of round 84; recommended a, drafted above): **b** the same screens, but Continue opens
    Level 50's hangar again (a dead end); **c** a, plus a New Game+ (a harder campaign keeping the loadout; balance
    across 50 levels).
- 2026-11-06, round 88: F.7 accepted: the campaign end's card (`CAMPAIGN COMPLETE`, `THE GATE IS DARK.`) and the name entry's `WON`.
