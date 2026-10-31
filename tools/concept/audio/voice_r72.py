#!/usr/bin/env python3
"""Concept round 72 (M10 "Act 6 - Jovian Storm" design phase, step AUD-R): the Act 6 voice auditions of the voice
README's *Act 6 voices (draft)*; pending the user's review.

Outputs:
  design/audio/voice/refs/ref-<vesper|kestrel|aurelia-traffic>-r72-<a|b>.wav (+ .txt, Whisper's transcript): the new
      speakers' candidate reference clips, 12 s cuts from LibriVox chapter recordings (public domain / CC0, CANDIDATES),
      mono 24 kHz WAV, made as in rounds 19-64 with
          ffmpeg -ss <start> -t 12 -i https://archive.org/download/<item>/<file> -ac 1 -ar 24000 <clip>.wav
  design/audio/voice/concept/voice-<vesper|kestrel>-r72-<a|b>.ogg: each ace candidate's two audition lines (the Honour
      Guard's *Lines*: the entry taunt and the survivor line), 1 s apart, through the enemy-channel filter
      (tools/art/voice.py enemy(): the game's intercept sweep is not baked in).
  design/audio/voice/concept/voice-aurelia-traffic-r72-<a|b>.ogg: Aurelia Traffic's t=1 call (neutral) and t=150
      sign-off (grim, slower), 1 s apart, through radio filter b (tts_r18.radio()).
  design/audio/voice/concept/voice-harker-r72-a.ogg: Harker's cast voice (refs/ref-harker-r62-b.wav, David Clarke) for
      one ace line, his entry taunt, at the fierce row through the enemy filter (no audition: the cast voice heard
      as an ace).
  design/audio/voice/concept/vorne-shout-r72-<a|b>.ogg: Vorne's Level 42 line 5 ("No! We had an agreement!") with
      his production clip (refs/ref-vorne.wav, Jeffrey Church -2 semitones) and his theatrical base (exaggeration +0.1,
      cfg_weight -0.1 on every row, the speaker table's `shift`): a the shout row (1.2 / 0.3 / 0.8 -> 1.3 / 0.2 / 0.8),
      b the fierce row (0.8 / 0.4 / 0.8 -> 0.9 / 0.3 / 0.8), through the enemy filter.

Rows (the voice README's *Speakers and expression*): neutral 0.5 / 0.5 / 0.7, grim 0.45 / 0.5 / 0.7, fierce
0.8 / 0.4 / 0.8, shout 1.2 / 0.3 / 0.8 (exaggeration / cfg_weight / temperature). The grim sign-off is read slower:
cfg_weight 0.4 (the README's "slow").

Takes: seeds SEEDS for every line; the pin (chosen automatically by pin(), printed) is the take with the most of the
line's words read back by Whisper (at least 90 %), then without a pause over 0.8 s, then the most words, then the
narrowest pitch spread (Vorne: the lowest pitch); PINS overrides it. Each pinned take is cut 0.15 s after the line's last word as Whisper times it (Chatterbox runs on after short
lines: "One.", "There."), with a 40 ms fade.

Setup: the Chatterbox venv of round 18 (~/.cache/tv-tts/venv-chatterbox, see tts_r18.py); Whisper (faster-whisper)
and librosa from the Dia venv (~/.cache/tv-tts/venv-dia), for the read-back and the pitch check.
Rerun: python3 tools/concept/audio/voice_r72.py --refs     (cut the missing clips, transcripts, clip pitches)
       python3 tools/concept/audio/voice_r72.py            (generate every seed, read back, pin, post-process)
       python3 tools/concept/audio/voice_r72.py --post     (read back, pin and post-process only)
Raw WAVs go to ~/.cache/tv-tts/raw/r72/. Seeds are fixed, but GPU sampling is not bit-exact.
"""
import json
import os
import subprocess
import sys
from pathlib import Path

HERE = Path(__file__).resolve().parent
ROOT = HERE.parents[2]
REFDIR = ROOT / "design" / "audio" / "voice" / "refs"
DEST = ROOT / "design" / "audio" / "voice" / "concept"
BASE = Path.home() / ".cache" / "tv-tts"
RAW = BASE / "raw" / "r72"
WHISPER_PY = BASE / "venv-dia" / "bin" / "python"
READBACK = RAW / "readback.json"

NEUTRAL = {"exaggeration": 0.5, "cfg_weight": 0.5, "temperature": 0.7}
GRIM_SLOW = {"exaggeration": 0.45, "cfg_weight": 0.4, "temperature": 0.7}
FIERCE = {"exaggeration": 0.8, "cfg_weight": 0.4, "temperature": 0.8}
VORNE_SHOUT = {"exaggeration": 1.3, "cfg_weight": 0.2, "temperature": 0.8}   # the shout row + his shift
VORNE_FIERCE = {"exaggeration": 0.9, "cfg_weight": 0.3, "temperature": 0.8}  # the fierce row + his shift

# speaker -> variant -> (archive.org item, chapter file, cut start s, reader, licence, book)
CANDIDATES = {
    "vesper": {
        "a": ("twainwhiteelephant", "twain_stolen_white_elephant_64kb.mp3", 60, "Kristen McQuillin",
              "Public Domain Mark 1.0", "The Stolen White Elephant (Twain)"),
        "b": ("harrington_1306_librivox", "harrington_01_edgeworth_64kb.mp3", 60, "Jenny Bradshaw", "CC0 1.0",
              "Harrington, ch. 1 (Edgeworth)"),
    },
    "kestrel": {
        "a": ("harriettfrean_1311_librivox", "harriettfrean_01_sinclair_64kb.mp3", 60, "Expatriate",
              "Public Domain Mark 1.0", "Life and Death of Harriett Frean, ch. 1 (Sinclair)"),
        "b": ("civil_disobedience_bn_librivox", "civildisobedience_01_thoreau_64kb.mp3", 60, "Bob Neufeld",
              "Public Domain Mark 1.0", "Civil Disobedience, part 1 (Thoreau)"),
    },
    "aurelia-traffic": {
        "a": ("cranford_sd_librivox", "cranford_01_gaskell_64kb.mp3", 60, "Sibella Denton", "public domain",
              "Cranford, ch. 1 (Gaskell)"),
        "b": ("gulliver_ld_librivox", "gulliverstravels_00_swift_64kb.mp3", 60, "Lizzie Driver", "public domain",
              "Gulliver's Travels, the publisher's preface (Swift)"),
    },
}
FILTER = {"vesper": "enemy", "kestrel": "enemy", "aurelia-traffic": "radio", "harker": "enemy", "vorne": "enemy"}
# speaker -> [(text, settings)]; the aces' lines are the Honour Guard's, Aurelia Traffic's Level 37's
LINES = {
    "vesper": [("Leave some for me, Harker. The quiet one's mine.", NEUTRAL),
               ("You'll pay for that one.", FIERCE)],
    "kestrel": [("Check your six, Lancer. I'm already there.", NEUTRAL),
                ("No more games.", GRIM_SLOW)],
    "aurelia-traffic": [("CDF flight, this is Aurelia Traffic. We have civilians on the pads. Please. Help us get them "
                         "out.", NEUTRAL),
                        ("The lift rings are failing. Aurelia is falling. Aurelia Traffic, signing off.", GRIM_SLOW)],
    "harker": [("Rook. They let you fly again? Then let's settle Ceres properly.", FIERCE)],
}
# the single-take subjects: name -> (speaker, clip, text, settings)
SINGLES = {
    "voice-harker-r72-a": ("harker", REFDIR / "ref-harker-r62-b.wav", LINES["harker"][0][0], FIERCE),
    "vorne-shout-r72-a": ("vorne", REFDIR / "ref-vorne.wav", "No! We had an agreement!", VORNE_SHOUT),
    "vorne-shout-r72-b": ("vorne", REFDIR / "ref-vorne.wav", "No! We had an agreement!", VORNE_FIERCE),
}
NAMES = "Harker, Vesper, Kestrel, Lancer, Rook, Ceres, Aurelia, Aurelia Traffic, CDF, Aegis, Vorne."
SEEDS = (100, 101, 102, 103)
GAP = 1.0  # s of silence between two filtered lines
# Pins set by hand over pin()'s choice: the shout row's takes that Whisper hears shouted ("No! We had an agreement!",
# seeds 101 and 103) are the ones that shout; pin() would take seed 100, the lowest, which is not shouted. 101 is the
# lower of the two (269 Hz against 376 Hz).
PINS = {"vorne-shout-r72-a-1": 101}


def ref(speaker, variant):
    return REFDIR / f"ref-{speaker}-r72-{variant}.wav"


def jobs():
    """(job id, clip, text, settings) for every take to generate."""
    out = []
    for speaker, cands in CANDIDATES.items():
        for variant in cands:
            for j, (text, settings) in enumerate(LINES[speaker]):
                for seed in SEEDS:
                    out.append((f"{speaker}-{variant}-{j + 1}-s{seed}", ref(speaker, variant), text, settings))
    for name, (_, clip, text, settings) in SINGLES.items():
        for seed in SEEDS:
            out.append((f"{name}-1-s{seed}", clip, text, settings))
    return out


def refs():
    """Cut the missing reference clips and write their Whisper transcripts."""
    for speaker, cands in CANDIDATES.items():
        for variant, (item, name, start, *_rest) in cands.items():
            out = ref(speaker, variant)
            if not out.exists():
                subprocess.run(["ffmpeg", "-nostdin", "-loglevel", "error", "-y", "-ss", str(start), "-t", "12",
                                "-i", f"https://archive.org/download/{item}/{name}", "-ac", "1", "-ar", "24000",
                                str(out)], check=True)
    run_whisper("--transcribe-refs")


def worker():
    import random

    import numpy as np
    import soundfile as sf
    import torch
    from chatterbox.tts import ChatterboxTTS
    sys.path.insert(0, str(ROOT / "tools" / "art"))
    from voice import trim
    model = ChatterboxTTS.from_pretrained(device="cuda")
    RAW.mkdir(parents=True, exist_ok=True)
    for job, clip, text, settings in jobs():
        path = RAW / f"{job}.wav"
        if path.exists():
            continue
        seed = int(job.rsplit("-s", 1)[1])
        for f in (random.seed, np.random.seed, torch.manual_seed):
            f(seed)
        wav = model.generate(text, audio_prompt_path=str(clip), **settings).cpu().numpy().reshape(-1)
        sf.write(str(path), trim(wav, model.sr).astype("float32"), model.sr)
        print(job, round(len(wav) / model.sr, 2), "s", flush=True)


def whisper(mode):
    """In the Dia venv: the reference transcripts and clip pitches, or every raw take's read-back, longest pause and
    median pitch (librosa pyin) into READBACK."""
    import librosa
    import numpy as np
    from faster_whisper import WhisperModel
    asr = WhisperModel("base.en", device="cpu", compute_type="int8")

    def text(path, prompt=None, want=None):
        """The read-back, its longest pause and, given the line (`want`), its words aligned to the line: how many of
        the line's words were heard and where the last of them ends (the cut: anything after it is run-on or
        Whisper's hallucination over the tail)."""
        import difflib
        segs, _ = asr.transcribe(str(path), language="en", initial_prompt=prompt, word_timestamps=True)
        segs = list(segs)
        words = [w for seg in segs for w in seg.words]
        said = " ".join(seg.text.strip() for seg in segs)
        if want is None:
            gap = max((b.start - a.end for a, b in zip(words, words[1:])), default=0.0)
            return said, round(gap, 2)
        heard = [_norm(w.word)[0] if _norm(w.word) else "" for w in words]
        exp = _norm(want)
        sm = difflib.SequenceMatcher(None, exp, heard, autojunk=False)
        covered, last = 0, None
        for blk in sm.get_matching_blocks():
            if blk.size:
                covered += blk.size
                last = blk.b + blk.size - 1
        if last is None or covered < len(exp) - 2:
            last = len(words) - 1
        # extend over a trailing misheard word aligned to the line's last word (e.g. "gains" for "games")
        tail = len(exp) - (sm.get_matching_blocks()[-2].a + sm.get_matching_blocks()[-2].size) \
            if len(sm.get_matching_blocks()) > 1 else 0
        last = min(len(words) - 1, last + max(0, min(tail, 1)))
        kept = words[:last + 1]
        gap = max((b.start - a.end for a, b in zip(kept, kept[1:])), default=0.0)
        return " ".join(w.word.strip() for w in kept), round(gap, 2), covered / len(exp), round(kept[-1].end, 2), said

    def pitch(path, end=None):
        y, sr = librosa.load(str(path), sr=24000)
        if end:
            y = y[:int(end * sr)]
        f0, voiced, _ = librosa.pyin(y, fmin=55, fmax=400, sr=sr)
        f = f0[voiced & ~np.isnan(f0)]
        return (round(float(np.median(f))), round(float(np.percentile(f, 75) - np.percentile(f, 25)))) if len(f) \
            else (0, 0)

    if mode == "--transcribe-refs":
        for speaker, cands in CANDIDATES.items():
            for variant in cands:
                t, _ = text(ref(speaker, variant))
                ref(speaker, variant).with_suffix(".txt").write_text(t + "\n")
                print(ref(speaker, variant).name, pitch(ref(speaker, variant)), "Hz (median, IQR) :", t)
        for clip in ("ref-harker-r62-b", "ref-vorne"):
            print(clip, pitch(REFDIR / f"{clip}.wav"), "Hz (median, IQR)")
        return
    out = json.loads(READBACK.read_text()) if READBACK.exists() else {}
    for job, _, line, _ in jobs():
        path = RAW / f"{job}.wav"
        if path.exists() and job not in out:
            t, gap, cover, end, said = text(path, NAMES, line)
            out[job] = {"text": t, "pause": gap, "cover": round(cover, 2), "end": end, "pitch": pitch(path, end),
                        "heard": said}
            print(job, out[job], flush=True)
    READBACK.write_text(json.dumps(out, indent=1))


def run_whisper(mode):
    subprocess.run([str(WHISPER_PY), __file__, "--whisper", mode], check=True,
                   env=dict(os.environ, HF_HOME=str(BASE / "hf")))


def _norm(t):
    return "".join(c for c in t.lower() if c.isalnum() or c == " ").split()


def pin(prefix, text):
    """The pinned seed for one line: the take with the most of the line's words read back, then no pause over 0.8 s,
    then the lowest pitch spread (IQR, no octave jump; for Vorne the lowest median pitch)."""
    rb = json.loads(READBACK.read_text())
    best = None
    for seed in SEEDS:
        r = rb.get(f"{prefix}-s{seed}")
        if not r:
            continue
        low = -r["pitch"][0] if prefix.startswith("vorne") else -r["pitch"][1]  # Vorne: the lowest, apart from Harker
        if PINS.get(prefix) == seed:
            return seed, r
        key = (r["cover"] >= 0.9, r["pause"] <= 0.8, r["cover"], low)
        if best is None or key > best[0]:
            best = (key, seed, r)
    return best[1], best[2]


def post():
    import numpy as np
    sys.path.insert(0, str(HERE))
    sys.path.insert(0, str(ROOT / "tools" / "art"))
    import tts_r18
    from voice import enemy
    s = tts_r18._synth()
    DEST.mkdir(parents=True, exist_ok=True)

    def filtered(speaker, x):
        return enemy(x, s) if FILTER[speaker] == "enemy" else tts_r18.radio(x)

    def render(name, speaker, takes):
        parts = []
        for prefix, text in takes:
            seed, r = pin(prefix, text)
            print(f"  {name}: {prefix} pin seed {seed}: {r['pitch'][0]} Hz (IQR {r['pitch'][1]}), pause "
                  f"{r['pause']} s, read back: {r['text']}", flush=True)
            x = s.decode(RAW / f"{prefix}-s{seed}.wav")[0]
            k = min(len(x), int((r["end"] + 0.15) * s.SR))  # cut after the line's last word (no run-on)
            x = x[:k]
            x[-int(0.04 * s.SR):] *= np.linspace(1, 0, int(0.04 * s.SR))
            parts += [filtered(speaker, x), np.zeros((1, int(GAP * s.SR)))]
        y = np.concatenate(parts[:-1], axis=1)
        s.write_ogg(DEST / f"{name}.ogg", y, quality=4, tags={"COMMENT": f"tools/concept/audio/voice_r72.py {name}"})
        print(name, round(y.shape[1] / s.SR, 2), "s", round(s.lufs(y)[0], 1), "LUFS", flush=True)

    for speaker, cands in CANDIDATES.items():
        for variant in cands:
            render(f"voice-{speaker}-r72-{variant}", speaker,
                   [(f"{speaker}-{variant}-{j + 1}", text) for j, (text, _) in enumerate(LINES[speaker])])
    for name, (speaker, _, text, _) in SINGLES.items():
        render(name, speaker, [(f"{name}-1", text)])


def main(args):
    if args[:1] == ["--worker"]:
        worker()
        return
    if args[:1] == ["--whisper"]:
        whisper(args[1])
        return
    if args[:1] == ["--refs"]:
        refs()
        return
    if args[:1] != ["--post"]:
        env = dict(os.environ, HF_HOME=str(BASE / "hf"), TOKENIZERS_PARALLELISM="false",
                   TORCH_FORCE_NO_WEIGHTS_ONLY_LOAD="1")
        subprocess.run([str(BASE / "venv-chatterbox" / "bin" / "python"), __file__, "--worker"], env=env, check=True)
    run_whisper("--read-back")
    post()


if __name__ == "__main__":
    main(sys.argv[1:])
