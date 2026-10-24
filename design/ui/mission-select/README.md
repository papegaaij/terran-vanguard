---
title: Mission select
design: review
implementation: done
art: n/a
depends-on: [../main-menu, ../debrief, ../../systems/scoring, ../../systems/saves, ../../systems/economy]
updated: 2026-10-24
---

# Mission select

## Summary

Replay a mission the current campaign has already flown (user decision 2026-10-04): the
campaign's levels act by act with their best grades, the flown ones open, every later one locked
with its name hidden. A replay starts like every mission, with its briefing and the hangar (user
decision 2026-10-18), and is a free flight for a better grade: it earns nothing and leaves the
campaign's progress where it is. This is the "level-select of a replay mode" that
[scoring](../../systems/scoring/README.md) keeps the best grade per level for.

## Design

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                     ┌──────────────────────────────────────┐                 │
│                     │            MISSION SELECT            │                 │
│                     │           REPLAY - MEDIUM            │                 │
│                     │ ──────────────────────────────────── │                 │
│                     │ ACT I  FIRST CONTACT ─────────────── │                 │
│                     │    01  BREAK AT DAWN               B │                 │
│                     │    02  SHIPYARD BURNING           A+ │                 │
│                     │  ► 03  SPORE DRIFT                 C │                 │
│                     │    04  ???                    LOCKED │                 │
│                     │    …                                 │                 │
│                     │ ACT II  ??? ──────────────────────── │                 │
│                     │ NO CREDITS EARNED - BEST GRADE KEPT  │                 │
│                     └──────────────────────────────────────┘                 │
│                 UP/DOWN SELECT    ENTER BRIEFING    ESC BACK                 │
└──────────────────────────────────────────────────────────────────────────────┘
```

- **Where:** the main menu's **Missions** item ([main menu](../main-menu/README.md)), disabled
  while the current campaign has flown no mission. The current campaign is the most recent save
  of any slot, the one Continue loads. Not in the hangar: the docs suggest no place there, and the
  hangar's command bar has no room to spare.
- **The list:** every act the game has data for, in order, as a header ("ACT I  FIRST CONTACT")
  with its levels below ("01  BREAK AT DAWN" and the best grade on the right, "-" if none). A level
  is **open** when the campaign has flown it (its number is below the save's next level) and it is
  built. Every other level is **locked**: dimmed, "???" for its name and "LOCKED" on the right,
  the campaign's next level too. An act whose first level the campaign has not reached shows "???"
  for its name. The list scrolls (14 rows) with the kit's scroll markers.
- **Controls:** up / down move between open missions (locked ones are skipped), confirm opens the
  selected one's briefing, Back returns to the main menu. The cursor starts on the last flown mission. Drawn
  with the glass kit over the dimmed title scene; the title theme plays on.
- **The way in** (user decision 2026-10-18): mission select → the level's **mission briefing**
  (its own pages, objectives and hangar teaser, without its act's title card and intro pages) →
  the **replay hangar** → the level as a replay → debrief → mission select. The briefing's Back
  asks to quit to the main menu, as every briefing's does.
- **The replay hangar** is the normal [hangar](../hangar/README.md) on the **real campaign** of the
  save the replay was started from, **refit only** (user correction 2026-10-19): the owned weapons,
  utility modules, core parts, special and Rook's guns are fitted and unfitted, and Rook's side set,
  and that is kept in the campaign; nothing is bought, sold, upgraded or repaired, so its credits
  never change. Its shop drawer is headed `REFIT`, lists only the owned items with their `FIT` /
  `UNFIT` choices (no bay row; `NOTHING OWNED FOR THIS SLOT` for an empty list), its message line
  reads `REPLAY: REFIT ONLY`, and the REPAIR command is greyed and skipped. Its top bar reads `REPLAY L05 - <NAME>` instead of
  `BEFORE L05`, its intel is the replayed level's, and its launch chip reads `> REPLAY`. It opens
  without an autosave (a save was just loaded, as after Continue); its **launch** writes the
  campaign as the hangar left it to the autosave, and the flight is a replay of that autosave; its
  quit and its SAVE command work as in the campaign's hangar.
- **A replay flight** (main-agent choice, confirmed by the user 2026-10-04; the design had no
  replay mode before, [campaign](../../campaign/README.md) left it for later):
  - it flies at the campaign's difficulty with the loadout and special charges the replay hangar
    left, at **full armour** (no repair bill, since nothing of the flight is kept);
  - it never changes the campaign's **progress** or next level, its credits, score, kills, armour,
    charges or retries, and the flight itself is never autosaved;
  - it earns **no credits** (no farming, see [economy](../../systems/economy/README.md)); the
    [debrief](../debrief/README.md) shows a REPLAY section with "NO CREDITS BANKED", the balance
    and the score in place of the credits;
  - it **keeps a better grade**: a grade that beats the level's best is written into the save the
    flight is a replay of (the autosave the replay hangar's launch wrote; the slot it was loaded
    from when that write failed), and the debrief shows NEW BEST; nothing else of that save
    changes (its write time neither, so it stays the current campaign);
  - its retries are unlimited, also on hard, so a replay never ends in a game over; the
    [pause](../pause/README.md) menu's abort reads ABORT REPLAY and the mission failed screen's
    BACK TO MISSIONS, both returning here, as the debrief does after a won replay. Quit to main menu
    works as in the campaign.

## Implementation

- [x] `vanguard.content.campaign.Missions`: the act-by-act list (open, locked and hidden names,
      hidden act names, best grades) and the save with a better grade
- [x] `Campaign.replay`: a copy of the save's campaign for one flown level (full armour, unlimited
      retries) that banks nothing, keeps its progress, records the grade and cannot be saved;
      `GameServices.save` writes nothing for a replay
- [x] `SaveSlots.mostRecentEntry`: the current campaign with its slot
- [x] `vanguard.game.screen.MissionSelectScreen`, opened from the main menu's Missions item; the
      better grade written into the source slot after a won replay (`keepGrade`)
- [x] Debrief, pause and mission failed screens route a replay back to the mission select; the
      debrief shows no credits for a replay
- [x] Tests: `MissionsTest` (the list's locked and open levels and act names, a better grade only,
      a replay earns nothing and keeps the progress, only flown levels)
- [x] Checked in a game capture: the list over the title scene with Levels 01–03 flown
- [x] A replay opens the level's briefing (`CampaignRoute.beforeReplay`, the act intro left out by
      `CampaignRoute.missionBriefing`) and then the replay hangar on the real campaign
      (`HangarScreen.replay`: `REPLAY L05` header, `> REPLAY` launch, the campaign written to the
      autosave at launch, the flight `HangarScreen.replayFlight`) — 2026-10-18
- [x] The replay hangar is refit only (`HangarState` with `refitOnly`: owned rows with their FIT /
      UNFIT choices, no bay row, no repair, buys and sales refused with `REPLAY: REFIT ONLY`) —
      2026-10-19
- [x] Tests: `MissionFlowTest` (the replay hangar refuses buying, selling, upgrading and repairing
      and leaves the credits alone, a refit in it is kept, the campaign's hangar still buys, the
      replay still earns nothing and keeps the progress), `CampaignRouteTest` (the replay's briefing without the act
      intro)

## Open questions

- None open.

## Decisions

- 2026-10-04 (user decision): a mission select to replay flown missions, reachable from the main
  menu; later missions and the names of later missions and unreached acts are hidden. The replay
  rules are main-agent choices (conservative: nothing kept but a better grade), for the user to
  confirm.
- 2026-10-04 (user decision): the replay rules are confirmed as built: the current loadout at full armour, no credits, progress or charges change, only a better best grade is kept, unlimited retries.
- 2026-10-18 (user decision, Act 2 playtest): the open question is answered: a replay starts with
  the level's briefing, then the hangar, then the level, as every mission does. The replay hangar
  works on the real campaign (purchases, sales and loadout changes are kept, credits spent are
  spent); the flight stays a replay as decided on 2026-10-04, built from the campaign as the hangar
  left it. Built choices (pending the user's review): the replay's briefing leaves out the act's
  title card and intro pages; the replay hangar opens without an autosave and writes the autosave
  at launch, which the flight then replays and into which its better grade goes; the header reads
  `REPLAY L05` and the launch `> REPLAY`.
- 2026-10-19 (user correction of the 2026-10-18 choice): "You will need to go through the briefing
  and the shop, but purchasing items seems odd at this point. I think we should restrict this to
  refitting only." The replay hangar now only refits with what the campaign owns (kept in the
  campaign as before); buying, selling, upgrading, bays and repairs are gone from it. Built choices
  (pending the user's review): the buyable and locked rows are hidden rather than greyed, the drawer
  is headed `REFIT` and the message line reads `REPLAY: REFIT ONLY`; the launch no longer warns
  about the ship's armour (the replay flies at full armour), but still about Rook's (a replay does
  not repair him, and the replay hangar cannot).
- 2026-10-19 (user, round 41, P.g): the replay hangar as built, refit only (owned items fitted and unfitted and kept; buying, selling, upgrading and repairs off).
- 2026-10-24 (the user's playtest: "The shop shows the bulwark even when selecting older missions. The shop should
  show what flies in the mission."): the replay hangar is the hangar **for the replayed mission**: its escort slot, the
  callout, the shop's escort rows, the Bulwark row and the launch warnings follow the escort that mission's flight takes
  (`Campaign.escortFlight(level)`): Rook with his gun before Rook's loss and after his return, the Bulwark where the
  player chose it (from its unlock, Level 22), the owned Bulwark or the loaner in Levels 27–29, and no escort slot for a
  mission before Rook joins (Level 08). The cause: the replay hangar ran on the real campaign and asked it for the
  escort of the campaign's **next** level (with Rook missing at Level 27, the Bulwark for every replay). Fixed by a
  hangar for a level (`Hangar(catalogue, campaign, level)`, `HangarState.of`); `ActFourHangarTest.
  theReplayHangarShowsTheEscortOfTheMissionItReplays` checks the hangar's escort against the replay flight's.
