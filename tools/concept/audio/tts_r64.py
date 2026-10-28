#!/usr/bin/env python3
"""Concept round 64 (M9 part D, step VOX-D): the casting of the Bellwether, the mining tug's captain (Level 31); see
design/audio/voice/README.md "Act 5 voices". Pending the user's review.

Outputs:
  design/audio/voice/refs/ref-bellwether-r64-<a|b>.wav (+ .txt, Whisper's transcript): the candidates' reference
      clips, 12 s cuts from LibriVox chapter recordings (public domain / CC0, REFS), mono 24 kHz WAV, made as in rounds
      19-62 with
          ffmpeg -ss <start> -t 12 -i https://archive.org/download/<item>/<file> -ac 1 -ar 24000 <clip>.wav
      and their pauses shortened as Ares Control's clip (trim_pauses() of tts_r36.py: every silence under -50 dBFS
      inside the clip to 0.3 s, the lead-in to 0.1 s), since b's cut holds a 1.5 s pause.
  design/audio/voice/concept/voice-bellwether-r64-<a|b>.ogg: each candidate's audition, Level 31's t=1 call and the
      t=86 / t=93 medicine story, 1 s apart, cut from the production renders of tools/art/voice.py (a:
      assets/voice/bellwether/<key>.ogg, b: concept/voice-bellwether-r64-b/<key>.ogg; AUDITION's keys), so the audition
      is the game's own take.

Casting (the voice README, Act 5 voices: a belt miner in her fifties, dry and tired, rougher and warmer than Dr. Brandt
(168 Hz, dry and precise), ideally a regional accent, lower than Okafor (145-152 Hz) or clearly apart by timbre; new
readers from LibriVox's public-domain and CC0 recordings, never a main-cast reader nor a voice already cast or
auditioned). Clip pitches measured with librosa pyin (median, IQR) and the pace with Whisper's word timings over the
12 s cut:
  a Lucy Burgoyne (1950-2014; Sisters, ch. 1, Ada Cambridge, an Australian novel; cut at 60 s): 134 Hz, IQR 27 Hz,
    2.9 words/s: a low, level, older English woman's voice, under Okafor (her other books 130-145 Hz: The Garden
    Party 130, The Dark House 139, Madame Midas 143-145)
  b Laurie Anne Walden (A Diary from Dixie, ch. 1, Mary Chesnut; cut at 120 s): 181 Hz, IQR 37 Hz, 2.4 words/s: a
    slow, plain, unhurried American read of a Southern diary, apart from Okafor's English voice by accent and pace
    rather than pitch (the accent is the reader's own: not verified by ear in this step)
(Also measured and passed over: Mil Nicholson, 142-157 Hz, Okafor's pitch, and passed over in round 51; Susan
Umpleby, 164-201 Hz; Kirsty Leishman, 193-208 Hz; Moira Fogarty, 189 Hz; Christine Dufour, 170 Hz but 3.2 words/s;
Lee Ann Howlett, 180 Hz; Annise, 185 Hz; Sibella Denton, 192 Hz; Bellona Times, 103-112 Hz (a man's register); Maria
Therese, Ophelia Darcy, Lynne Thompson, Jean Bascom, Lizzie Driver, Catharine Eastman and GabrielleC, 188-262 Hz,
above Okafor and Varga's register.)

Setup: Whisper (faster-whisper) and librosa from the Dia venv (~/.cache/tv-tts/venv-dia), for the transcripts, the
pitch check and the read-back.
Rerun: python3 tools/concept/audio/tts_r64.py --refs        (cut the missing clips, trim, transcripts)
       python3 tools/concept/audio/tts_r64.py --auditions   (the audition files from the rendered lines)
       python3 tools/concept/audio/tts_r64.py --read-back <source prefix> [--b]
                                             (Whisper's read-back of the rendered lines of a source, plain and with
                                             the Act 5 name prompt, and their median pitch; --b reads candidate b's
                                             Bellwether takes in concept/voice-bellwether-r64-b/)
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
ASSETS = ROOT / "assets" / "voice"
BASE = Path.home() / ".cache" / "tv-tts"
WHISPER_PY = BASE / "venv-dia" / "bin" / "python"
LINES = Path(os.environ.get("TV_VOICE_BUILD", BASE / "gradle")) / "build" / "_pipeline" / "voice" / "lines.json"

SPEAKER = "bellwether"
# variant: (archive.org item, chapter file, cut start s, reader, licence)
REFS = {
    "a": ("sisters_0712_librivox", "sisters_01_cambridge_64kb.mp3", 60, "Lucy Burgoyne", "public domain"),
    "b": ("diaryfromdixie_1101_librivox", "diaryfromdixie_01_chesnut_64kb.mp3", 120, "Laurie Anne Walden",
          "Public Domain Mark 1.0"),
}
NAMES = ("Bellwether, Aegis, Ceres, Vesta, Callisto, Helix, Vorne, Harker, Rook, Lancer, Dacosta, Hornet, Vrell, "
         "Ascendancy.")
# variant -> the keys of the three audition lines (t=1, t=86, t=93): tools/art/voice.py's keys for each ref
AUDITION = {"a": ["7d524c00ec14", "6f693a8b1b13", "d8fe3296137f"], "b": ["6590e84c18f1", "d59185a39a79", "ebc47d3f8f25"]}
GAP = 1.0


def ref(variant):
    return REFDIR / f"ref-{SPEAKER}-r64-{variant}.wav"


def refs():
    """Cut the missing reference clips, shorten their pauses, write the transcripts."""
    sys.path.insert(0, str(HERE))
    import tts_r36
    for variant, (item, name, start, _, _) in REFS.items():
        out = ref(variant)
        if out.exists():
            continue
        cut = out.with_name(out.stem + "-untrimmed.wav")
        subprocess.run(["ffmpeg", "-nostdin", "-loglevel", "error", "-y", "-ss", str(start), "-t", "12",
                        "-i", f"https://archive.org/download/{item}/{name}", "-ac", "1", "-ar", "24000", str(cut)],
                       check=True)
        x, sr = tts_r36.read_wav(cut)
        tts_r36.write_wav(out, tts_r36.trim_pauses(x, sr), sr)
        cut.unlink()
    run_whisper("--transcribe-refs")


def whisper(args):
    """In the Dia venv: the clips' transcripts and pitches, or the read-back of rendered lines."""
    import librosa
    import numpy as np
    from faster_whisper import WhisperModel
    asr = WhisperModel("base.en", device="cpu", compute_type="int8")

    def text(path, prompt=None):
        segs, _ = asr.transcribe(str(path), language="en", initial_prompt=prompt, word_timestamps=True)
        segs = list(segs)
        words = [w for seg in segs for w in seg.words]
        gap = max((b.start - a.end for a, b in zip(words, words[1:])), default=0.0)
        span = words[-1].end - words[0].start if words else 1.0
        return (" ".join(seg.text.strip() for seg in segs)
                + f"  [{len(words) / span:.1f} words/s, longest pause {gap:.2f} s]")

    def pitch(path):
        y, sr = librosa.load(str(path), sr=24000)
        f0, voiced, _ = librosa.pyin(y, fmin=55, fmax=400, sr=sr)
        f = f0[voiced & ~np.isnan(f0)]
        if len(f) == 0:
            return "unvoiced"
        iqr = np.percentile(f, 75) - np.percentile(f, 25)
        return f"{np.median(f):.0f} Hz (IQR {iqr:.0f}, p95 {np.percentile(f, 95):.0f})"

    if args[0] == "--transcribe-refs":
        for variant in REFS:
            clip = ref(variant)
            t = text(clip)
            if not clip.with_suffix(".txt").exists():
                clip.with_suffix(".txt").write_text(t.split("  [")[0] + "\n")
            print(clip.name, pitch(clip), ":", t, flush=True)
        return
    if args[0] == "--auditions":
        for path in sorted(DEST.glob(f"voice-{SPEAKER}-r64-*.ogg")):
            print("read back", path.name, pitch(path), ":", text(path, NAMES), flush=True)
        return
    prefix, use_b = args[1], "--b" in args
    for line in json.loads(LINES.read_text()):
        if not line["source"].startswith(prefix):
            continue
        path = ROOT / "assets" / line["path"]
        if use_b:
            if line["voice"] != SPEAKER:
                continue
            path = DEST / f"voice-{SPEAKER}-r64-b" / Path(line["path"]).name
        if not path.exists():
            print("MISSING", line["key"], line["voice"], line["spoken"], flush=True)
            continue
        plain = text(path).split("  [")[0]
        named = text(path, NAMES).split("  [")[0]
        print(f"{line['voice']} {line['key']} {pitch(path)} | {line['spoken']}\n   plain: {plain}\n   names: {named}",
              flush=True)


def run_whisper(*args):
    subprocess.run([str(WHISPER_PY), __file__, "--whisper", *args], check=True,
                   env=dict(os.environ, HF_HOME=str(BASE / "hf")))


def auditions():
    import numpy as np
    sys.path.insert(0, str(HERE))
    import tts_r18
    s = tts_r18._synth()
    for variant, keys in AUDITION.items():
        where = ASSETS / SPEAKER if variant == "a" else DEST / f"voice-{SPEAKER}-r64-{variant}"
        parts = []
        for key in keys:
            parts += [s.decode(where / f"{key}.ogg")[:1], np.zeros((1, int(GAP * s.SR)))]
        y = np.concatenate(parts[:-1], axis=1)
        tags = {"COMMENT": f"tools/concept/audio/tts_r64.py {SPEAKER} {variant} " + " ".join(keys)}
        s.write_ogg(DEST / f"voice-{SPEAKER}-r64-{variant}.ogg", y, quality=4, tags=tags)
        print(SPEAKER, variant, round(y.shape[1] / s.SR, 2), "s", flush=True)
    run_whisper("--auditions")


def main(args):
    if args[:1] == ["--whisper"]:
        whisper(args[1:])
    elif args[:1] == ["--refs"]:
        refs()
    elif args[:1] == ["--auditions"]:
        auditions()
    elif args[:1] == ["--read-back"]:
        run_whisper("--read-back", *args[1:])
    else:
        print(__doc__)


if __name__ == "__main__":
    main(sys.argv[1:])
