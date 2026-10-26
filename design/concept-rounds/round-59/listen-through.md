# Act 4 listen-through

Round 59's V.1 ([README](README.md#voices)): every voiced Act 4 line in play order (the act intro, Levels 22–28: the briefing pages, the radio cues in the data's order, the secret; the act outro), 119 lines, 14.0 minutes, 0 missing files (the keys computed by `:pipeline:voiceLines`). Rook's barks and the other calls are unchanged since round 45 (accepted there). Built by `build/round59-tools/listen_md.py` from step I3's check (do not edit by hand).

**The check** (step I3): every file through faster-whisper base.en (the renderer's model) against the text as spoken, plus its duration, peak and the last 30 ms's level. No line clips (the loudest peak 0.92, the -1.5 dBFS ceiling); every line ends quietly except the two cut-off lines, which end in their static as designed; every line's first and last word are heard. The transcripts differ mostly on names and numerals the small model does not know (Vrell, Aegis, Thera, Conamara, numbers): those are marked ok. **listen** marks a line whose transcript differs on a word the line needs, or whose delivery is unusually slow: a hint, not a verdict (Claude has not listened).

## Listen first (8 lines)

| Where | # | Speaker | Cue | Text | File | s | Check |
|---|---|---|---|---|---|---|---|
| Level 22 Ice Shelf | 9 | Rook | radio t=87.0 (pinned) | Mantis, both flanks! | [rook/26f36c7b9641.ogg](../../../assets/voice/rook/26f36c7b9641.ogg) | 2.3 | listen: whisper heard "Metis, both flanks"; slow (2.3 s for three words) |
| Level 26 Blackwater | 8 | The Choir | radio t=99.0 | …small ones… in the dark… your kin… came before you… | [choir/6430480eae04.ogg](../../../assets/voice/choir/6430480eae04.ogg) | 5.3 | listen: under the Choir's layering whisper lost "your kin… came" (heard "…much better than before you") |
| Level 27 Hydrothermal Vents | 5 | Thera Deep Control | radio t=24.0 | Thera Deep Control, Aegis. Our hauler Marlin went down by the smokers in week one. If you see her, say so. | [thera-control/3be118b94baa.ogg](../../../assets/voice/thera-control/3be118b94baa.ogg) | 9.1 | listen: the opening "Thera Deep Control" heard as "Dearer, deep-controlled" |
| Level 27 Hydrothermal Vents | 6 | Varga | radio t=52.0 | Contacts rising from below. Grey hulls, not Vrell. Stay high and shoot down their torpedoes. | [varga/f1f46d7ce5e8.ogg](../../../assets/voice/varga/f1f46d7ce5e8.ogg) | 6.1 | listen: "Grey hulls" heard as "Gray holes" (the word hull, see L27 t=70) |
| Level 27 Hydrothermal Vents | 7 | Varga | radio t=70.0 | That wreck on the floor, its paint burnt off by a plume. There's a hull code under it. It's Helix Dynamics, Commander. | [varga/5732a7f749ae.ogg](../../../assets/voice/varga/5732a7f749ae.ogg) | 7.2 | listen: "a hull code" heard as "a whole code" (hint 3's key word) |
| Level 28 Abyssal Maw | 8 | Okafor | radio t=76.0 | Antechamber, then the vent. Shields up, Lancer. Everything you have. | [okafor/f0ecd0235fa1.ogg](../../../assets/voice/okafor/f0ecd0235fa1.ogg) | 7.3 | listen: slow (7.3 s for ten words), long gaps? |
| Level 28 Abyssal Maw | 15 | The Choir | radio BOSS_DESTROYED | …many… we were many here… | [choir/f44735894dec.ogg](../../../assets/voice/choir/f44735894dec.ogg) | 3.7 | listen: whisper heard "Ready, we will be running here." (the Choir's layering) |
| Level 28 Abyssal Maw | 16 | Okafor | radio BOSS_DESTROYED | Maw's down. Aegis Wing, up the shaft. Now. | [okafor/1a34f5ef0931.ogg](../../../assets/voice/okafor/1a34f5ef0931.ogg) | 4.5 | listen: "Maw's down" heard as "Mores down" |

## Act 4 intro (round 51)

| # | Speaker | Cue | Text | File | s | Check |
|---|---|---|---|---|---|---|
| 1 | Okafor | briefing page 1 | Twelve days at full burn, and the Vrell beat us to Europa by a week. Their fleet is in orbit and their drill rigs are on the ice. Conamara Station has been feeling the tremors since before we left Mars. | [okafor/996fef2ab8eb.ogg](../../../assets/voice/okafor/996fef2ab8eb.ogg) | 15.1 | ok |
| 2 | Varga | briefing page 2 | Europa is a shell of ice kilometres thick over an ocean. Forty thousand people live under it, in Thera Deep, and eat from its kelp farms. The Vrell are drilling for that ocean. They are not here for the ice. | [varga/51e56de11c78.ogg](../../../assets/voice/varga/51e56de11c78.ogg) | 13.1 | ok |
| 3 | Okafor | briefing page 3 | The Stormhawks are refitted for the water: sealed intakes, a torpedo rack, a pressure-proof hull. Everything slows down there, theirs and yours. Guns lose a third of their bite, beams and lasers nearly all. Save your credits for torpedoes and charges. | [okafor/d9711cbe976c.ogg](../../../assets/voice/okafor/d9711cbe976c.ogg) | 20.1 | ok |
| 4 | Okafor | briefing page 4 | We hit the rigs on the surface first, then we follow the hole down. Drop in ninety minutes. Mission briefing follows. Aegis Actual out. | [okafor/6b32d9021455.ogg](../../../assets/voice/okafor/6b32d9021455.ogg) | 9.9 | ok |

## Level 22 Ice Shelf (round 51)

| # | Speaker | Cue | Text | File | s | Check |
|---|---|---|---|---|---|---|
| 1 | Okafor | briefing page 1 | Conamara Station has felt the ice shaking for a week. Now we know why: the Vrell have set four drill rigs on the shelf and they are boring for the ocean. Forty scientists are in that dome. Break the rigs before they go through, then we follow the hole down. | [okafor/fe095b8e9014.ogg](../../../assets/voice/okafor/fe095b8e9014.ogg) | 18.7 | ok |
| 2 | Varga | briefing page 2 | A rig is a collar and four pumps in a hardened shell. Shots glance off; bombs, mortars and an Airstrike get through. Aim for the bore core. And mind the cracks: when the ice glows, the steam is next. | [varga/ce73718b1b9e.ogg](../../../assets/voice/varga/ce73718b1b9e.ogg) | 13.7 | ok |
| 3 | Dr. Brandt | radio t=1.0 | Aegis flight, Conamara Station. Four rigs on the shelf, forty of us in the dome, and the ice will not hold still. | [conamara/4929ede323db.ogg](../../../assets/voice/conamara/4929ede323db.ogg) | 8.9 | ok |
| 4 | Rook | radio t=18.0 | Copy, Conamara. Last open sky for a while, Lancer. Enjoy it. | [rook/dabbe18dbbc0.ogg](../../../assets/voice/rook/dabbe18dbbc0.ogg) | 5.4 | ok |
| 5 | Varga | radio FIRST_GEYSER | Cracks glow, then steam. Do not sit on them. | [varga/b47eabb00927.ogg](../../../assets/voice/varga/b47eabb00927.ogg) | 3.6 | ok |
| 6 | Varga | radio t=29.0 | Drill rig ahead. Hardened shell: bombs, mortars and the Airstrike get through. Break the bore core. | [varga/cbd63c0501ef.ogg](../../../assets/voice/varga/cbd63c0501ef.ogg) | 6.4 | ok |
| 7 | Dr. Brandt | radio TARGET_DESTROYED (pinned) | That's one collar stopped and the shaking eased. Three to go. | [conamara/6ed33d226abf.ogg](../../../assets/voice/conamara/6ed33d226abf.ogg) | 4.8 | ok |
| 8 | Varga | radio t=58.0 (pinned) | Ilse? It's Elena. Hold on, we are coming. | [varga/38a0dbf42f62.ogg](../../../assets/voice/varga/38a0dbf42f62.ogg) | 2.9 | ok |
| 9 | Rook | radio t=87.0 (pinned) | Mantis, both flanks! | [rook/26f36c7b9641.ogg](../../../assets/voice/rook/26f36c7b9641.ogg) | 2.3 | listen: whisper heard "Metis, both flanks"; slow (2.3 s for three words) |
| 10 | Dr. Brandt | radio t=104.0 | Something went through under the north dome. There is water in the shaft at Kilo-Seven. It leads down. | [conamara/3d657dd22059.ogg](../../../assets/voice/conamara/3d657dd22059.ogg) | 7.5 | ok |
| 11 | Dr. Brandt | radio SECONDARY_OBJECTIVE | Enough rigs are down. The shelf is quiet. Thank you, Aegis. | [conamara/bba3f2966263.ogg](../../../assets/voice/conamara/bba3f2966263.ogg) | 4.4 | ok |
| 12 | Okafor | radio t=135.0 (pinned) | Conamara is in sight. We refit for the water tonight and we take that shaft in the morning. | [okafor/33d9c9134784.ogg](../../../assets/voice/okafor/33d9c9134784.ogg) | 7.7 | ok |
| 13 | Okafor | radio LEVEL_END | Conamara is safe, for now. Refit, Aegis. Tomorrow we go under the ice. | [okafor/028d2b76f4ed.ogg](../../../assets/voice/okafor/028d2b76f4ed.ogg) | 8.1 | ok |
| 14 | Dr. Brandt | secret core-sample cache | That's our deep core. Three years of ice. Keep it, you earned it. | [conamara/831bdd5759bc.ogg](../../../assets/voice/conamara/831bdd5759bc.ogg) | 5.8 | ok |

## Level 23 Through the Ice (round 52)

| # | Speaker | Cue | Text | File | s | Check |
|---|---|---|---|---|---|---|
| 1 | Okafor | briefing page 1 (pinned) | The Vrell drilled a shaft from Conamara to the ocean. We take it. Your Stormhawks have been fitted with hydro-kits overnight: they swim. Below is Thera Deep. Forty thousand people live there, and the Vrell are draining their farms. | [okafor/8dd79a07602d.ogg](../../../assets/voice/okafor/8dd79a07602d.ogg) | 17.7 | ok |
| 2 | Varga | briefing page 2 | Under water everything slows. Torpedoes and depth charges keep their strength; guns lose a third, beams and lasers nearly all. Eels have a head that fires: cut it. A Sonar Pulse lights the water. The Airstrike and the Lance cannot reach under the ice. | [varga/c4bd2b9ccbe9.ogg](../../../assets/voice/varga/c4bd2b9ccbe9.ogg) | 16.4 | ok |
| 3 | Okafor | radio t=1.0 (pinned) | Aegis flight, down the shaft. The hydro-kits are fitted; trust them. Thera Deep is at the bottom. | [okafor/954041a6a0b5.ogg](../../../assets/voice/okafor/954041a6a0b5.ogg) | 6.7 | ok |
| 4 | Varga | radio t=13.5 | Water in twelve seconds. Everything slows. Torpedoes and charges bite as ever; guns lose a third, beams nearly all. | [varga/d8e352fea2c2.ogg](../../../assets/voice/varga/d8e352fea2c2.ogg) | 8.6 | ok |
| 5 | Varga | radio WATERLINE (pinned) | Under the ice. Airstrike and Lance are dead weight now. A Sonar Pulse lights the water. | [varga/b1f0c1054141.ogg](../../../assets/voice/varga/b1f0c1054141.ogg) | 5.9 | ok |
| 6 | Varga | radio t=41.0 | Eel chain. Cut the head and the body dies; cut the body and the tail scatters. | [varga/62ee77ec935a.ogg](../../../assets/voice/varga/62ee77ec935a.ogg) | 5.1 | ok |
| 7 | Thera Deep Control | radio t=65.0 (pinned) | Aegis, Thera Deep Control. We see you on the shaft. The farms are below you, and so is whatever is draining them. | [thera-control/d6eaf31e59c8.ogg](../../../assets/voice/thera-control/d6eaf31e59c8.ogg) | 8.3 | ok |
| 8 | Varga | radio t=77.0 (pinned) | Siphon pumps on the racks: they drain the farms into the Vrell. Shoot the pumps, not the racks. | [varga/0e301c25bf15.ogg](../../../assets/voice/varga/0e301c25bf15.ogg) | 6.2 | ok |
| 9 | Thera Deep Control | radio t=88.5 | Those are our farms, Aegis. Forty thousand people eat from those racks. | [thera-control/c1f7c58397ac.ogg](../../../assets/voice/thera-control/c1f7c58397ac.ogg) | 5.4 | ok |
| 10 | Rook | radio t=99.5 | A shaft, a hydro-kit and a ship that was never meant to swim. Flying a submarine, Lancer. | [rook/1985088d9d35.ogg](../../../assets/voice/rook/1985088d9d35.ogg) | 7.4 | ok |
| 11 | Thera Deep Control | radio SECONDARY_OBJECTIVE | Enough pumps are off the racks. Thera Deep owes you a dinner, Aegis. | [thera-control/c612a56ba759.ogg](../../../assets/voice/thera-control/c612a56ba759.ogg) | 4.6 | ok |
| 12 | Okafor | radio t=140.0 (pinned) | The farms are clear. Beyond the lamps is the open plain. Hold your lights, Aegis. | [okafor/2a7ad409bba3.ogg](../../../assets/voice/okafor/2a7ad409bba3.ogg) | 6.5 | ok |
| 13 | Okafor | radio LEVEL_END | Good work. Thera Deep is breathing again. The Vrell's road leads out across the plain. | [okafor/926ebf149a7f.ogg](../../../assets/voice/okafor/926ebf149a7f.ogg) | 7.6 | ok |
| 14 | Thera Deep Control | secret survey probe (pinned) | That's Conamara's probe. Keep it, you earned it. | [thera-control/fc3128b738ec.ogg](../../../assets/voice/thera-control/fc3128b738ec.ogg) | 4.2 | ok |

## Level 24 Abyssal Plain (round 53)

| # | Speaker | Cue | Text | File | s | Check |
|---|---|---|---|---|---|---|
| 1 | Okafor | briefing page 1 (pinned) | Past the farms is the abyssal plain, and the Vrell are growing reefs on it: living nurseries that breed fry. Burn them down. Thera Deep's lamps end ahead, so you fly on your own lights. | [okafor/86ecad13e612.ogg](../../../assets/voice/okafor/86ecad13e612.ogg) | 15.6 | ok |
| 2 | Varga | briefing page 2 | Two new forms: a small swimmer with a barbed snout that holds and spits needles, and a manta the size of a cruiser that rises from the dark below you. Torpedoes reach it while it is a shadow. And there are human relay beacons out there. I want a closer look. | [varga/f2a54b593d7e.ogg](../../../assets/voice/varga/f2a54b593d7e.ogg) | 15.0 | ok |
| 3 | Okafor | radio t=1.0 (pinned) | Past the farms is the abyssal plain, Aegis. Thera Deep's lamps end ahead; from here you fly on your own lights. | [okafor/92374b1e90be.ogg](../../../assets/voice/okafor/92374b1e90be.ogg) | 9.8 | ok |
| 4 | Varga | radio t=13.0 | Small swimmers, needles aimed at you. They hold and shoot: take them one by one. | [varga/7d0d74f0678e.ogg](../../../assets/voice/varga/7d0d74f0678e.ogg) | 5.7 | ok |
| 5 | Rook | radio t=24.0 (pinned) | Dark's coming, Lancer. Rule one in a submarine: the ceiling is lower than it looks. | [rook/56de5260936b.ogg](../../../assets/voice/rook/56de5260936b.ogg) | 5.7 | ok |
| 6 | Varga | radio t=35.0 | Spawning reefs. They breed fry from the iris, and they are soft: torpedoes will do. | [varga/a1a9bd0fbe28.ogg](../../../assets/voice/varga/a1a9bd0fbe28.ogg) | 5.7 | ok |
| 7 | Varga | radio FIRST_RAY (pinned) | Something huge under you. A shadow first, then a manta. Torpedoes reach it now; the rest must wait. | [varga/907d82c74798.ogg](../../../assets/voice/varga/907d82c74798.ogg) | 6.9 | ok |
| 8 | Varga | radio t=98.0 | A beacon. Human-made, relay hardware, an amber pulse. And the carrier on it is Helix's. | [varga/f456be61f759.ogg](../../../assets/voice/varga/f456be61f759.ogg) | 6.7 | ok |
| 9 | Okafor | radio t=109.5 | Log it, Doctor. Full record, for my eyes only. | [okafor/94158b7f0742.ogg](../../../assets/voice/okafor/94158b7f0742.ogg) | 4.7 | ok |
| 10 | Rook | radio t=120.0 | Ray at six, Lancer: it is rising! | [rook/c5ce54776d36.ogg](../../../assets/voice/rook/c5ce54776d36.ogg) | 3.8 | ok |
| 11 | Okafor | radio SECONDARY_OBJECTIVE | Third reef down. The Vrell will have to breed somewhere else. | [okafor/8790bac2bdf7.ogg](../../../assets/voice/okafor/8790bac2bdf7.ogg) | 4.6 | ok |
| 12 | Okafor | radio SECONDARY_OBJECTIVE | Second reef down. The Vrell will have to breed somewhere else. | [okafor/d278216113a2.ogg](../../../assets/voice/okafor/d278216113a2.ogg) | 5.2 | ok |
| 13 | Varga | radio t=131.0 | A third beacon. The same pulse every two kilometres. Somebody laid a road down here. | [varga/fd1e04541f1b.ogg](../../../assets/voice/varga/fd1e04541f1b.ogg) | 5.6 | ok |
| 14 | Okafor | radio LEVEL_END | The plain is behind us. The beacons lead into the trench, and so do we. Refit, Aegis. | [okafor/abea00ba2f02.ogg](../../../assets/voice/okafor/abea00ba2f02.ogg) | 6.7 | ok |
| 15 | Varga | secret Meridian (pinned) | That's the Meridian. Thera Deep lost her to the dark, not to the Vrell. Take what she kept. | [varga/e20571c1375f.ogg](../../../assets/voice/varga/e20571c1375f.ogg) | 5.9 | ok |

## Level 25 Siren Trench (round 54)

| # | Speaker | Cue | Text | File | s | Check |
|---|---|---|---|---|---|---|
| 1 | Okafor | briefing page 1 | Beyond the plain the sea floor splits into a trench, and the Vrell have grown nests in its walls. Fly it end to end. Don't touch the rock, Lancer. | [okafor/d8f6e00ee783.ogg](../../../assets/voice/okafor/d8f6e00ee783.ogg) | 12.4 | ok |
| 2 | Varga | briefing page 2 (pinned) | The nests sing. The ring does no damage, but it slows you, and a slowed ship can't dodge. Slip through the gap between rings, or shoot the sac. Side and anti-sub guns. | [varga/d316a7f1c744.ogg](../../../assets/voice/varga/d316a7f1c744.ogg) | 10.6 | ok |
| 3 | Okafor | radio t=1.0 (pinned) | The Siren Trench, Lancer. Vrell nests sing in the walls. Get through it and out the other side. | [okafor/68b1de64c583.ogg](../../../assets/voice/okafor/68b1de64c583.ogg) | 8.8 | ok |
| 4 | Rook | radio t=13.0 | Water outside, singing inside. I'd like a refund. | [rook/aa6f893b7c6b.ogg](../../../assets/voice/rook/aa6f893b7c6b.ogg) | 4.3 | ok |
| 5 | Varga | radio t=20.0 (pinned) | A Siren. Its ring does no damage, it slows you. Slip through the gap, or shoot the sac. | [varga/8eaffeebd009.ogg](../../../assets/voice/varga/8eaffeebd009.ogg) | 5.4 | ok |
| 6 | Varga | radio t=33.0 (pinned) | The beacons on the walls again. Same carrier, same Helix handshake. | [varga/9e55419a9b2b.ogg](../../../assets/voice/varga/9e55419a9b2b.ogg) | 4.3 | ok |
| 7 | Okafor | radio t=44.0 | Log it, Doctor. Later. | [okafor/c68591010e7d.ogg](../../../assets/voice/okafor/c68591010e7d.ogg) | 2.3 | ok |
| 8 | Varga | radio t=63.5 | Nautilus. The shell is armour, the crown is the weak point. Shoot it when it opens. | [varga/2f31e1efcfbd.ogg](../../../assets/voice/varga/2f31e1efcfbd.ogg) | 5.7 | ok |
| 9 | Okafor | radio t=94.0 | The narrows, Lancer. Stay off the walls. | [okafor/b0118284f0e1.ogg](../../../assets/voice/okafor/b0118284f0e1.ogg) | 3.9 | ok |
| 10 | Varga | radio t=131.5 (pinned) | Sirens on both walls and Nautilus between. A slowed ship can't thread a spiral. Keep moving. | [varga/c80f1f044656.ogg](../../../assets/voice/varga/c80f1f044656.ogg) | 6.7 | ok |
| 11 | Okafor | radio t=158.0 (pinned) | The trench opens ahead. Almost through, Aegis. | [okafor/f6aaa2b9a46a.ogg](../../../assets/voice/okafor/f6aaa2b9a46a.ogg) | 4.3 | ok |
| 12 | Okafor | radio t=164.0 | Out of the trench. Dark water next. Keep your light on. | [okafor/710d117967f4.ogg](../../../assets/voice/okafor/710d117967f4.ogg) | 4.7 | ok |
| 13 | Okafor | radio SECONDARY_OBJECTIVE | Clean crossing. The song is quiet. | [okafor/1f9fd6ebb7df.ogg](../../../assets/voice/okafor/1f9fd6ebb7df.ogg) | 3.5 | ok |
| 14 | Varga | secret survey sub (pinned) | Conamara's survey sub. Someone was down here before the Vrell. | [varga/11407b40994f.ogg](../../../assets/voice/varga/11407b40994f.ogg) | 4.2 | ok |

## Level 26 Blackwater (round 55)

| # | Speaker | Cue | Text | File | s | Check |
|---|---|---|---|---|---|---|
| 1 | Okafor | briefing page 1 | Past the trench there is nothing on the charts, only black water between us and the vents. The Vrell route runs through it. Fly it on your headlight, Lancer, and keep Rook close. | [okafor/30a264f9cb79.ogg](../../../assets/voice/okafor/30a264f9cb79.ogg) | 12.3 | ok |
| 2 | Varga | briefing page 2 (pinned) | You'll see only what glows. The Vrell glow teal and violet, and so do their lures: a teal bead on a stalk is not a pickup. Bullets always show. The Sonar Pulse lights everything for six seconds. | [varga/98a59ec8c2b3.ogg](../../../assets/voice/varga/98a59ec8c2b3.ogg) | 12.4 | ok |
| 3 | Okafor | radio t=1.0 | Blackwater, Lancer. No lamps, no charts. Your headlight and whatever glows, nothing else. | [okafor/2981a39857ed.ogg](../../../assets/voice/okafor/2981a39857ed.ogg) | 7.0 | ok |
| 4 | Rook | radio t=13.0 | Dark and wet. At least nobody sees me being scared. | [rook/d88208bdd2cd.ogg](../../../assets/voice/rook/d88208bdd2cd.ogg) | 4.2 | ok |
| 5 | Varga | radio t=24.5 | That glow is not a pickup. Teal is Vrell. Touch it and it lunges. | [varga/91c33ba565ba.ogg](../../../assets/voice/varga/91c33ba565ba.ogg) | 4.9 | ok |
| 6 | Varga | radio t=46.0 | Lost them? Pulse the sonar. It lights everything for six seconds. | [varga/1aad656a66cc.ogg](../../../assets/voice/varga/1aad656a66cc.ogg) | 4.6 | ok |
| 7 | Varga | radio t=63.0 | A fault wall on your right. Sirens in its caves: watch for the glow before the ring. | [varga/36132b46c2cc.ogg](../../../assets/voice/varga/36132b46c2cc.ogg) | 5.4 | ok |
| 8 | The Choir | radio t=99.0 | …small ones… in the dark… your kin… came before you… | [choir/6430480eae04.ogg](../../../assets/voice/choir/6430480eae04.ogg) | 5.3 | listen: under the Choir's layering whisper lost "your kin… came" (heard "…much better than before you") |
| 9 | Varga | radio t=110.5 (pinned) | Your kin. It said your kin, Commander. | [varga/60a839dd854d.ogg](../../../assets/voice/varga/60a839dd854d.ogg) | 2.6 | ok |
| 10 | Okafor | radio t=118.0 | Noted, Doctor. Eyes front. | [okafor/696214560fb9.ogg](../../../assets/voice/okafor/696214560fb9.ogg) | 2.8 | slow delivery (1.4 words/s), low priority |
| 11 | Okafor | radio t=140.0 | Almost through the deep, Lancer. Vents ahead. | [okafor/e087776aa95a.ogg](../../../assets/voice/okafor/e087776aa95a.ogg) | 4.8 | slow delivery (1.4 words/s), low priority |
| 12 | Rook | radio t=148.0 (pinned) | Lancer, if I buy you a drink for this one, we're even. | [rook/8689cf844d62.ogg](../../../assets/voice/rook/8689cf844d62.ogg) | 4.1 | ok |
| 13 | Varga | radio t=155.0 | Contacts below! | [varga/87c74bacc450.ogg](../../../assets/voice/varga/87c74bacc450.ogg) | 1.9 | slow delivery (1.9 s for two words), low priority |
| 14 | Rook | radio t=161.2 (pinned) | Lancer, something's below u— | [rook/799501ca3f8b.ogg](../../../assets/voice/rook/799501ca3f8b.ogg) | 2.8 | the cut-off line, cut as designed |
| 15 | Okafor | radio t=173.5 (pinned) | Aegis Two, report. Rook? Rook, answer! | [okafor/553b9b37180b.ogg](../../../assets/voice/okafor/553b9b37180b.ogg) | 4.3 | ok |
| 16 | Varga | radio t=180.5 | His lamp went down, Commander. | [varga/2dc64f0b8210.ogg](../../../assets/voice/varga/2dc64f0b8210.ogg) | 2.1 | ok |
| 17 | Okafor | radio LEVEL_END | Finish the dive, Lancer. We will find him. | [okafor/a1feb7d4cf7c.ogg](../../../assets/voice/okafor/a1feb7d4cf7c.ogg) | 4.2 | ok |
| 18 | Okafor | radio SECONDARY_OBJECTIVE | Clean crossing. Now find him, Lancer. | [okafor/b5ed5548e278.ogg](../../../assets/voice/okafor/b5ed5548e278.ogg) | 4.0 | ok |
| 19 | Varga | secret farm barge | A farm barge, lamp still on. Warm light, Lancer: that one is ours. | [varga/37073cb16c3e.ogg](../../../assets/voice/varga/37073cb16c3e.ogg) | 4.8 | ok |

## Level 27 Hydrothermal Vents (round 57)

| # | Speaker | Cue | Text | File | s | Check |
|---|---|---|---|---|---|---|
| 1 | Okafor | briefing page 1 | Aegis Two is missing. Rook's beacon went dark in the black water, and a Bulwark drone flies his slot until we find him. The vents are the last stretch before the hatchery, Lancer. | [okafor/859a51e2e10b.ogg](../../../assets/voice/okafor/859a51e2e10b.ogg) | 13.3 | ok |
| 2 | Varga | briefing page 2 | Vent plumes erupt in cycles and bend every shot that crosses them. Something grey rises from below and fires up: stay high and shoot its torpedoes down. Rear guns, and anti-sub. | [varga/871ab9f8d7a9.ogg](../../../assets/voice/varga/871ab9f8d7a9.ogg) | 11.3 | ok |
| 3 | Okafor | radio t=1.0 | Aegis Two is dark, and a Bulwark drone has his slot. Vent field ahead, Lancer. Eyes up and hands steady. | [okafor/1b13d6cfa4df.ogg](../../../assets/voice/okafor/1b13d6cfa4df.ogg) | 8.9 | ok |
| 4 | Varga | radio t=12.0 | The vents erupt in cycles. A plume burns whatever it touches and bends every shot that crosses it. | [varga/47c9c66de2fc.ogg](../../../assets/voice/varga/47c9c66de2fc.ogg) | 6.5 | ok |
| 5 | Thera Deep Control | radio t=24.0 | Thera Deep Control, Aegis. Our hauler Marlin went down by the smokers in week one. If you see her, say so. | [thera-control/3be118b94baa.ogg](../../../assets/voice/thera-control/3be118b94baa.ogg) | 9.1 | listen: the opening "Thera Deep Control" heard as "Dearer, deep-controlled" |
| 6 | Varga | radio t=52.0 | Contacts rising from below. Grey hulls, not Vrell. Stay high and shoot down their torpedoes. | [varga/f1f46d7ce5e8.ogg](../../../assets/voice/varga/f1f46d7ce5e8.ogg) | 6.1 | listen: "Grey hulls" heard as "Gray holes" (the word hull, see L27 t=70) |
| 7 | Varga | radio t=70.0 | That wreck on the floor, its paint burnt off by a plume. There's a hull code under it. It's Helix Dynamics, Commander. | [varga/5732a7f749ae.ogg](../../../assets/voice/varga/5732a7f749ae.ogg) | 7.2 | listen: "a hull code" heard as "a whole code" (hint 3's key word) |
| 8 | Okafor | radio t=82.5 | I'm sending it to UTC High Command now. Keep scanning, Doctor. I want every frequency. | [okafor/709bd94918c9.ogg](../../../assets/voice/okafor/709bd94918c9.ogg) | 7.0 | ok |
| 9 | Varga | radio t=94.5 | The ridge beacons carry the same Helix handshake as that hull. Take them down, Lancer. | [varga/8d8648ec500c.ogg](../../../assets/voice/varga/8d8648ec500c.ogg) | 5.4 | ok |
| 10 | Varga | radio FIRST_RAY | Large contact below you! It's rising! | [varga/f6cb110342f9.ogg](../../../assets/voice/varga/f6cb110342f9.ogg) | 2.5 | ok |
| 11 | Okafor | radio t=117.0 | High Command answers: the drones were stolen, stand by. I'm done standing by, Aegis. | [okafor/bd33c7d9cfda.ogg](../../../assets/voice/okafor/bd33c7d9cfda.ogg) | 7.3 | ok |
| 12 | Varga | radio t=129.0 | Third column below you, and the gate's lights ahead. Flares, Lancer. | [varga/0812e146b568.ogg](../../../assets/voice/varga/0812e146b568.ogg) | 4.4 | ok |
| 13 | Okafor | radio LEVEL_END | The hatchery gate. Whatever Helix is hiding, it's down there. Aegis, we go in. | [okafor/f67c9ca6a094.ogg](../../../assets/voice/okafor/f67c9ca6a094.ogg) | 7.1 | ok |
| 14 | Thera Deep Control | secret Marlin | That's the Marlin. Whatever is in her hold is yours, Aegis. | [thera-control/8275eb3343fe.ogg](../../../assets/voice/thera-control/8275eb3343fe.ogg) | 4.2 | ok |

## Level 28 Abyssal Maw (round 58)

| # | Speaker | Cue | Text | File | s | Check |
|---|---|---|---|---|---|---|
| 1 | Okafor | briefing page 1 | Everything the Vrell sent at Europa was grown in one place: a hatchery round the deepest vent. End it, Lancer, and the ice is ours again. Rook is still out there. We'll find him. | [okafor/3b9d08d9cdbb.ogg](../../../assets/voice/okafor/3b9d08d9cdbb.ogg) | 13.5 | ok |
| 2 | Varga | briefing page 2 | A guardian lies coiled round the vent. Shoot its gill sacs, then the lime in its throat when the jaw opens. The grey hulls at the gate carry the code from the wreck. Helix, Lancer. | [varga/9337577dfdba.ogg](../../../assets/voice/varga/9337577dfdba.ogg) | 11.1 | ok |
| 3 | Okafor | radio t=1.0 | Everything the Vrell sent at Europa was grown in this hatchery. End it, Lancer, and we go home. | [okafor/dee87ac991d4.ogg](../../../assets/voice/okafor/dee87ac991d4.ogg) | 7.8 | ok |
| 4 | Varga | radio t=13.0 | Grey hulls again, guarding the gate. Helix hulls, Commander. Why is Helix guarding a Vrell nursery? | [varga/1dbfc1b6805b.ogg](../../../assets/voice/varga/1dbfc1b6805b.ogg) | 6.4 | ok |
| 5 | Okafor | radio t=26.0 | I don't know, Doctor. When this is over I will ask, and I will not accept stand by. | [okafor/38ed8de802fc.ogg](../../../assets/voice/okafor/38ed8de802fc.ogg) | 8.5 | ok |
| 6 | Varga | radio t=38.0 | Eggs on every rack. Thousands. This is where the Vrell on Europa came from. | [varga/496509818f77.ogg](../../../assets/voice/varga/496509818f77.ogg) | 5.9 | ok |
| 7 | Varga | radio t=63.0 | The floor is warm. The vent is under us, and something large is coiled round it. | [varga/6294b9dcae91.ogg](../../../assets/voice/varga/6294b9dcae91.ogg) | 5.8 | ok |
| 8 | Okafor | radio t=76.0 | Antechamber, then the vent. Shields up, Lancer. Everything you have. | [okafor/f0ecd0235fa1.ogg](../../../assets/voice/okafor/f0ecd0235fa1.ogg) | 7.3 | listen: slow (7.3 s for ten words), long gaps? |
| 9 | Varga | radio t=88.0 | My biosign is off the scale. It knows we're here. | [varga/8e5c5881a0c5.ogg](../../../assets/voice/varga/8e5c5881a0c5.ogg) | 3.7 | ok |
| 10 | Varga | radio BOSS_ARRIVAL | Gill sacs first, Lancer. Then the lime in its throat when the jaw opens. | [varga/7183d35a4316.ogg](../../../assets/voice/varga/7183d35a4316.ogg) | 4.9 | low priority: "Gill sacs … the lime" heard as "Gil-Sach's … the Lyman" |
| 11 | Okafor | radio BOSS_PHASE | Walls of light. Find the gap and keep moving. | [okafor/ba7c57bfc59a.ogg](../../../assets/voice/okafor/ba7c57bfc59a.ogg) | 4.5 | ok |
| 12 | Varga | radio BOSS_DIVE | It's gone under! Watch the dark. It comes back fast! | [varga/5406b813841c.ogg](../../../assets/voice/varga/5406b813841c.ogg) | 4.1 | ok |
| 13 | Varga | radio BOSS_PHASE | Commander, that's not Vrell. That's machinery in its spine. Relay housings… Helix pattern. | [varga/701e5dbf4c9f.ogg](../../../assets/voice/varga/701e5dbf4c9f.ogg) | 6.1 | ok |
| 14 | Okafor | radio BOSS_PHASE | Then hit the machinery, Lancer. | [okafor/47e5b3803d9d.ogg](../../../assets/voice/okafor/47e5b3803d9d.ogg) | 2.3 | ok |
| 15 | The Choir | radio BOSS_DESTROYED | …many… we were many here… | [choir/f44735894dec.ogg](../../../assets/voice/choir/f44735894dec.ogg) | 3.7 | listen: whisper heard "Ready, we will be running here." (the Choir's layering) |
| 16 | Okafor | radio BOSS_DESTROYED | Maw's down. Aegis Wing, up the shaft. Now. | [okafor/1a34f5ef0931.ogg](../../../assets/voice/okafor/1a34f5ef0931.ogg) | 4.5 | listen: "Maw's down" heard as "Mores down" |
| 17 | Varga | radio BOSS_DESTROYED | Those aren't Vrell hulls— | [varga/4d8896aff023.ogg](../../../assets/voice/varga/4d8896aff023.ogg) | 2.3 | the cut-off line (cut as designed); "Vrell hulls" heard as "rail holes" |
| 18 | Okafor | radio BOSS_DESTROYED | Those are not Vrell ships. God help us. Those are ours. | [okafor/9e69d125413d.ogg](../../../assets/voice/okafor/9e69d125413d.ogg) | 5.7 | ok |
| 19 | Okafor | radio BOSS_DESTROYED | All Aegis units, break for orbit. Don't look back. | [okafor/8f490a8730e6.ogg](../../../assets/voice/okafor/8f490a8730e6.ogg) | 5.9 | ok |
| 20 | Varga | radio BOSS_DESTROYED | That's the code from the wreck, painted on their bows. They aren't hiding it any more. | [varga/53d83e1cfef7.ogg](../../../assets/voice/varga/53d83e1cfef7.ogg) | 5.3 | ok |
| 21 | Okafor | secret Pelagic-3 | Pelagic-3. Our recon sub, eleven crew, lost in week one. Log it, Doctor. Take what's aboard, Lancer. | [okafor/1b3302771754.ogg](../../../assets/voice/okafor/1b3302771754.ogg) | 10.1 | ok |

## Act 4 outro (round 58)

| # | Speaker | Cue | Text | File | s | Check |
|---|---|---|---|---|---|---|
| 1 | Okafor | outro page 1 (pinned) | The Maw is dead and Europa's ocean is clear of the Vrell, for what that is worth. We are in orbit. The cruisers did not follow. We lost Aegis Two in the dark, and we do not know where he is. | [okafor/4b4bcd5a2392.ogg](../../../assets/voice/okafor/4b4bcd5a2392.ogg) | 17.1 | ok |
| 2 | Varga | outro page 2 | I matched the code. It is the one from the Depth Hunter's wreck, and it is painted on their bows, in the open now. The beacons, the grey hulls, the drones on Mars: they all say the same name. The Vrell did not find us alone. | [varga/5073c4503d90.ogg](../../../assets/voice/varga/5073c4503d90.ogg) | 13.4 | ok |
| 3 | Okafor | outro page 3 | On Mars I told Dr. Varga to keep her eyes on the canyon. I silenced her, and I was wrong. That does not happen again. What she sees, she says, on every channel. | [okafor/5680b14b0d32.ogg](../../../assets/voice/okafor/5680b14b0d32.ogg) | 15.1 | ok |
| 4 | Okafor | outro page 4 | Whatever comes over the radio next, we hear it together. Rook is out there. We ship out, and we find him. Aegis Actual out. | [okafor/bf0352b45df2.ogg](../../../assets/voice/okafor/bf0352b45df2.ogg) | 9.8 | ok |
