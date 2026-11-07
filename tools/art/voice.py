#!/usr/bin/env python3
"""The spoken radio lines and briefing pages (design/audio/voice): Chatterbox, offline.

Outputs (assets/voice/):
  <voice>/<key>.ogg   one file per spoken line, OGG Vorbis q4, 44.1 kHz mono, -16 LUFS, with a
                      COMMENT tag naming this tool, the voice, the key and the take's seed(s).
  The key (12 hex digits of a SHA-256 over the voice's settings, the text as spoken, the
  expression, the shout flag and the filter) comes from vanguard.content.voice.VoiceLines, which
  the game uses too: `./gradlew :pipeline:voiceLines` writes the list this script reads. The Gradle run is
  isolated as the agents' steps run theirs (M8 part I): its own project cache dir (build/voice-cache), an init script
  that moves every project's build dir under $TV_VOICE_BUILD (default ~/.cache/tv-tts/gradle/build; the list in
  _pipeline/voice/lines.json there) and the daemon's 10-minute idle timeout. A line whose file exists is not rendered again; a file no
  line uses any more is deleted (--keep lists them instead); files not named by a key, such as
  a speaker's stage sound (the Choir sings, copied by :pipeline:copyPlaceholderStageSounds), are
  left alone.

Render: Chatterbox (ResembleAI/chatterbox, chatterbox-tts 0.1.7) in its venv
~/.cache/tv-tts/venv-chatterbox (set up as in tools/concept/audio/tts_r18.py; models in
~/.cache/tv-tts/hf), cloning the speaker's clip in design/audio/voice/refs/ with the settings of
the line's expression (or the shout row) from design/audio/voice/data.yaml. Long lines (the
briefing pages) are spoken sentence by sentence (chunks of up to CHUNK_WORDS words) and joined
with PAUSE s of silence. Each chunk is spoken with a fixed seed (from the key, or the speaker
table's pin); the generation is capped at MAX_FACTOR x the expected length from the word count
(WORDS_PER_SECOND), and a take that hits the cap, runs on past it or is shorter than MIN_FACTOR x
the expected length is retried with the next seed, up to TAKES seeds, and so is a take holding a
silence longer than MAX_PAUSE s inside it (M7 part B: Ares Control's audition held 2 s pauses inside
two lines; `longest_pause`, 20 ms frames more than PAUSE_DB under the take's loudest); every retry is
logged (~/.cache/tv-tts/raw/voice/render-log.json and stdout).

Post: the Choir's layering (choir() of tools/concept/audio/tts_r18.py) for a voice with
`layering: choir`; then radio filter b (radio() there) for a radio line, the same plus dropouts
and more crackle for a distorted one, the public-address filter (pa() there: horn band, slap
echoes, a hall) for a voice with `filter: pa` (the Level 06 perimeter beacon), the enemy channel's filter (enemy()
below) for a voice with `filter: enemy` (Vorne, Harker; M9 part B) and for an intercepted briefing page (`channel:
enemy`), and no filter for any other briefing page (dry: the same mastering, -16 LUFS, -1.5 dBFS ceiling).
The radio's noise (M11 part B, step VOX-B; the voice README's Act 7 voices): a radio or distorted line carries its
level's noise step n (`noise` in the line list, the level's `radio_noise`, 0-5), and filter b's static rises with it:
the hiss bed x1.33^n (+2.5 dB a step) and the crackle on 0.1 % x (1 + n/2) of the samples, its peak unchanged (radio()
below; step 0 is tts_r18.radio() sample for sample); never a dropout. The `clean` filter (the Choir's Act 7 radio
lines: its channel close) is filter b's band and saturation without its hiss, crackle and clicks (5 ms fades), as
round 84 V.1 a (voice_r84.py's channel() without a step); the Act 7 intro's page 3 (the Choir through the gate) is a
radio line at step 1 over the Choir's layering, that round's line 1.
The `fused` and `fused_lost` filters (M11 part F, step VOX-EF; Vorne's fused voice in Level 48, round 84 V.2 b, as
voice_r84.py's variant b): his take, then the Choir's lower copies of it a breath behind (FUSE: -12, -5 and -19
semitones through ffmpeg's rubberband, formants preserved, 140, 100 and 120 ms late, the third 6 dB under the others),
the copies alone through a 3.2 s reverb (50 % wet), 8 dB under his voice (`fused`, his lines before his shout) or 4 dB
(`fused_lost`, from his shout on); then the enemy filter. A fused line ending in a dash is cut as a radio one (below),
the copies and their hall cut with him, then through the enemy filter and ending in the dead channel.
A radio line whose text ends in a dash is cut off (Level 10's Lifeline Three, "…What is that—",
as auditioned in concept round 32): the dash is spoken as a comma, Whisper (faster-whisper
base.en, in the Dia venv ~/.cache/tv-tts/venv-dia) times the take's words, and the take is cut hard
where its last word ends (the word's tail clipped) into a burst of static and a dead channel
(cut_off() of tools/concept/audio/tts_r32.py: 0.3 s of static as loud as the voice, then 0.25 s of
fading hiss); the file ends there, without the filter's closing click. The word timings go to
~/.cache/tv-tts/raw/voice/timings.json.

Per-cue treatments (TREATMENTS, by the line's key; M10 part H, step VOX-H): a line whose production take is not a plain
pipeline take is rendered and post-processed as its round picked it, so a re-render reproduces it. Level 42's line 5,
Vorne's one shout "No! We had an agreement!" (round 73 R.4 b, tools/concept/audio/voice_r73.py's take b): his clip
lowered a further 4 semitones (ffmpeg's rubberband, formants preserved, into the raw cache, not refs/), the line's own
row (the shout row on his theatrical base, 1.3 / 0.2 / 0.8) on its pinned seed only (105, no retries), the take cut
after the line's last word (Whisper's timing, then where the level stays 30 dB down for 60 ms) and lowered by WORLD
resynthesis (pyworld in ~/.cache/tv-tts/venv-world: the f0 times 110/211, the formants and timing untouched) to about
104 Hz, then through the enemy filter as his other lines. VoiceFilesTest names the key, so a changed line fails there
until this table follows it. Level 48's line 5, his shout "Get out of my head!" (the voice README's Act 7 voices,
round 84 V.2 b: round 73's treatment, then the fused copies at -4 dB): his clip lowered 4 semitones, the shout row on its
pinned seed only (120, round 84's auditioned take), cut after the last word, then the fused filter; no WORLD lowering
(the cut take is about 115 Hz, under voice_r84.py's 130 Hz threshold, as the round heard it).

Rerun: python3 tools/art/voice.py            render what is missing, delete unused files
       python3 tools/art/voice.py --keep     the same, but only list the unused files
       python3 tools/art/voice.py --list     only print what would be rendered or deleted
       python3 tools/art/voice.py --only <source prefix> ...   render only the missing lines whose source starts with
                                             a prefix (repeatable: `--only "act-5-the-belt briefing"`); implies --keep
Seeds are fixed, but GPU sampling is not bit-exact across drivers, so a rerun can differ slightly.
"""
import json
import os
import re
import subprocess
import sys
import time
from pathlib import Path

HERE = Path(__file__).resolve().parent
ROOT = HERE.parents[1]
ASSETS = ROOT / "assets" / "voice"
REFDIR = ROOT / "design" / "audio" / "voice" / "refs"
BASE = Path.home() / ".cache" / "tv-tts"
# M8 part I: the inner Gradle run's own build dir (an init script moves every project's there) and project cache dir.
GRADLE = Path(os.environ.get("TV_VOICE_BUILD", BASE / "gradle"))
INIT = GRADLE / "init.gradle"
LINES = GRADLE / "build" / "_pipeline" / "voice" / "lines.json"
RAW = BASE / "raw" / "voice"
LOG = RAW / "render-log.json"

WORDS_PER_SECOND = 2.6   # Chatterbox's pace at the neutral row, measured on rounds 18-19
MAX_FACTOR = 1.5         # the length cap: 1.5 x the expected length
MIN_FACTOR = 0.5         # shorter than this is a truncated take
TAKES = 8                # seeds tried per chunk
CHUNK_WORDS = 28
PAUSE = 0.35             # s between the chunks of a long line
TOKENS_PER_SECOND = 25   # Chatterbox's speech tokens
SENTENCE_PAUSE = 0.4     # s expected per sentence end
WHISPER_PY = BASE / "venv-dia" / "bin" / "python"   # faster-whisper, for a cut-off line's word timing
TIMINGS = RAW / "timings.json"
MAX_PAUSE = 0.8          # s: a take with a longer silence inside it is retried with the next seed
PAUSE_DB = 40.0          # dB under the take's loudest 20 ms frame that count as silence
WORLD_PY = BASE / "venv-world" / "bin" / "python"   # pyworld 0.3.5 (+ numpy, soundfile), for a treatment's lowering

# Per-cue treatments by the line's key (the docstring): ref_semitones lowers the speaker's clip (rubberband, formants
# preserved) into RAW; takes caps the seeds tried (1: the pinned take only); cut ends the take after the line's last
# word; world scales its pitch by WORLD resynthesis. The seed is the speaker table's pin (design/audio/voice/data.yaml).
TREATMENTS = {
    # Level 42 line 5, Vorne's shout (round 73 R.4 b: vorne-shout-r73-b's clip, row, seed 105 and lowering, 211 -> 110 Hz)
    "90d82c165dc1": {"ref_semitones": -4.0, "takes": 1, "cut": True, "world": 110.0 / 211.0},
    # Level 48 line 5, Vorne's shout in the fused voice (step VOX-EF: round 73's treatment on round 84 V.2 b's take, seed
    # 120; no WORLD lowering: the cut take is about 115 Hz, under voice_r84.py's 130 Hz threshold, as auditioned)
    "35b7899ef803": {"ref_semitones": -4.0, "takes": 1, "cut": True},
}

# Vorne's fused voice (round 84 V.2 b, voice_r84.py's VARIANTS["b"]): the copies (semitones, extra dB) and their delays
# (s), the hall on the copies (seconds, wet), and the copies' dB under his voice by filter.
FUSE = {"copies": ((-12, 0.0), (-5, 0.0), (-19, -6.0)), "delays": (0.14, 0.10, 0.12), "reverb": (3.2, 0.5),
        "under": {"fused": 8.0, "fused_lost": 4.0}}


def treated_ref(line):
    """The clip a line is cloned from: the speaker's, or for a treatment with ref_semitones its lowered copy in RAW."""
    shift = TREATMENTS.get(line["key"], {}).get("ref_semitones")
    if shift is None:
        return REFDIR / f"{line['ref']}.wav"
    return RAW / f"{line['ref']}{shift:+.1f}st.wav"


def lower_ref(line):
    """Writes a treatment's lowered clip (ffmpeg's rubberband, formants preserved, as voice_r73.py's low_clip())."""
    shift = TREATMENTS.get(line["key"], {}).get("ref_semitones")
    dst = treated_ref(line)
    if shift is None or dst.exists():
        return
    subprocess.run(["ffmpeg", "-nostdin", "-loglevel", "error", "-y", "-i", str(REFDIR / f"{line['ref']}.wav"), "-af",
                    f"rubberband=pitch={2 ** (shift / 12):.6f}:formant=preserved:pitchq=quality:window=long",
                    "-ac", "1", "-ar", "24000", str(dst)], check=True)


def cut_word(line):
    """The word a cut-off line ends on (its text ends in a dash), in lower case; None otherwise (a dry page)."""
    if line["filter"] == "dry" or not re.search(r"[—–]\s*$", line["text"]):
        return None
    return re.sub(r"[^\w']", "", line["spoken"].split()[-1]).lower()


def expected_seconds(text):
    """The expected length: the words at the measured pace, a short pause per comma, colon or
    semicolon and a longer one per sentence end (a run of dots, an ellipsis, counts once), which
    terse lines of short sentences need ("Domes intact. Airlocks open. Nobody's home." hit the
    cap with every seed at 0.12 s a stop)."""
    words = len(text.split())
    commas = len(re.findall(r"[;:,]", text))
    ends = len(re.findall(r"[.!?]+", text))
    return 0.3 + words / WORDS_PER_SECOND + 0.12 * commas + SENTENCE_PAUSE * ends


def chunks(text):
    """The sentences of a line, merged into chunks of up to CHUNK_WORDS words."""
    sentences = [s for s in re.split(r"(?<=[.!?])\s+", text.strip()) if s]
    out = []
    for s in sentences:
        if out and len(out[-1].split()) + len(s.split()) <= CHUNK_WORDS:
            out[-1] += " " + s
        else:
            out.append(s)
    return out


def base_seed(line):
    return line["pin"] if line.get("pin") is not None else int(line["key"][:7], 16)


# ---------------------------------------------------------------- worker (runs in the venv)

def trim(wav, sr):
    """Leading and trailing silence cut (-45 dB under the peak, 50 ms frames, 60 ms kept)."""
    import numpy as np
    frame = int(0.05 * sr)
    n = len(wav) // frame
    if n == 0:
        return wav
    rms = np.sqrt(np.mean(wav[: n * frame].reshape(n, frame) ** 2, axis=1) + 1e-12)
    loud = np.nonzero(rms > rms.max() * 10 ** (-45 / 20))[0]
    if len(loud) == 0:
        return wav
    keep = int(0.06 * sr)
    return wav[max(0, loud[0] * frame - keep): min(len(wav), (loud[-1] + 1) * frame + keep)]


def longest_pause(wav, sr):
    """The longest run of silent 20 ms frames (PAUSE_DB under the loudest) between the take's first and
    last sounding frame, in seconds."""
    import numpy as np
    frame = int(0.02 * sr)
    n = len(wav) // frame
    if n == 0:
        return 0.0
    rms = np.sqrt(np.mean(wav[: n * frame].reshape(n, frame) ** 2, axis=1) + 1e-12)
    loud = rms > rms.max() * 10 ** (-PAUSE_DB / 20)
    sounding = np.nonzero(loud)[0]
    longest = run = 0
    for v in loud[sounding[0]:sounding[-1] + 1]:
        run = 0 if v else run + 1
        longest = max(longest, run)
    return longest * frame / sr


def worker(jobs_path):
    import random

    import numpy as np
    import soundfile as sf
    import torch
    from chatterbox.tts import ChatterboxTTS
    model = ChatterboxTTS.from_pretrained(device="cuda")
    state = {"cap": 1000}
    inference = model.t3.inference

    def capped(*args, **kwargs):
        kwargs["max_new_tokens"] = state["cap"]
        return inference(*args, **kwargs)
    model.t3.inference = capped
    jobs = json.loads(Path(jobs_path).read_text())
    log = {}
    for job in jobs:
        t0 = time.time()
        parts, entry = [], {"voice": job["voice"], "source": job["source"], "chunks": []}
        for c, text in enumerate(chunks(job["spoken"])):
            expected = expected_seconds(text)
            cap_s = max(2.0, MAX_FACTOR * expected)
            state["cap"] = int(cap_s * TOKENS_PER_SECOND)
            tries, best = [], None
            for take in range(TREATMENTS.get(job["key"], {}).get("takes", TAKES)):
                seed = base_seed(job) + 1000 * c + take
                for f in (random.seed, np.random.seed, torch.manual_seed):
                    f(seed)
                wav = model.generate(text, audio_prompt_path=str(treated_ref(job)),
                                     exaggeration=job["exaggeration"], cfg_weight=job["cfg_weight"],
                                     temperature=job["temperature"])
                wav = wav.cpu().numpy().reshape(-1)
                raw_s = len(wav) / model.sr
                wav = trim(wav, model.sr)
                dur = len(wav) / model.sr
                hit_cap = raw_s >= 0.97 * cap_s
                pause = longest_pause(wav, model.sr)
                why = ("hit the cap" if hit_cap else "ran on" if dur > cap_s
                       else "too short" if dur < MIN_FACTOR * expected
                       else f"a {pause:.1f} s pause" if pause > MAX_PAUSE else "")
                tries.append({"seed": seed, "seconds": round(dur, 2), "rejected": why})
                if not why:
                    best = (wav, seed)
                    break
                if best is None or abs(dur - expected) < abs(len(best[0]) / model.sr - expected):
                    best = (wav, seed)
            if len(tries) > 1:
                print(f"  retry {job['voice']} {job['key']} chunk {c}: "
                      + ", ".join(f"{t['seed']} {t['seconds']}s {t['rejected'] or 'kept'}"
                                  for t in tries), flush=True)
            entry["chunks"].append({"text": text, "expected": round(expected, 2), "cap": cap_s,
                                    "takes": tries, "resolved": not tries[-1]["rejected"]})
            parts.append(best[0])
            parts.append(np.zeros(int(PAUSE * model.sr), dtype=best[0].dtype))
        wav = np.concatenate(parts[:-1])
        sf.write(str(RAW / f"{job['key']}.wav"), wav.astype("float32"), model.sr)
        entry["seconds"] = round(len(wav) / model.sr, 2)
        entry["seeds"] = [ch["takes"][-1]["seed"] if ch["resolved"] else None
                          for ch in entry["chunks"]]
        log[job["key"]] = entry
        print(job["voice"], job["key"], entry["seconds"], "s",
              f"({time.time() - t0:.1f} s)", flush=True)
        (RAW / "worker-log.json").write_text(json.dumps(log, indent=1))


def whisper(keys_path):
    """In the Dia venv: the word timings of the raw takes named in keys_path, into TIMINGS."""
    from faster_whisper import WhisperModel
    asr = WhisperModel("base.en", device="cpu", compute_type="int8")
    timings = json.loads(TIMINGS.read_text()) if TIMINGS.exists() else {}
    for key in json.loads(Path(keys_path).read_text()):
        segs, _ = asr.transcribe(str(RAW / f"{key}.wav"), language="en", word_timestamps=True)
        timings[key] = [(w.word.strip(), round(w.start, 3), round(w.end, 3)) for seg in segs for w in seg.words]
        print("timed", key, ":", " ".join(w for w, _, _ in timings[key]), flush=True)
    TIMINGS.write_text(json.dumps(timings, indent=1))


def world(src, dst, factor):
    """In the WORLD venv: src's pitch times ``factor`` by WORLD analysis and resynthesis (harvest f0, CheapTrick
    envelope, D4C aperiodicity), the spectral envelope (the formants) and the timing untouched (voice_r73.py's)."""
    import numpy as np
    import pyworld as pw
    import soundfile as sf
    x, sr = sf.read(src)
    x = (x.mean(1) if x.ndim > 1 else x).astype(np.float64)
    f0, t = pw.harvest(x, sr, f0_floor=60, f0_ceil=600, frame_period=5.0)
    y = pw.synthesize(f0 * factor, pw.cheaptrick(x, f0, t, sr), pw.d4c(x, f0, t, sr), sr, frame_period=5.0)
    sf.write(dst, (y / np.max(np.abs(y)) * 0.8).astype(np.float32), sr)


# ---------------------------------------------------------------- post-process (system Python)

def _filters():
    sys.path.insert(0, str(ROOT / "tools" / "concept" / "audio"))
    import tts_r18
    return tts_r18


def radio(x, step=0):
    """Radio filter b (tts_r18.radio()) at the noise step `step`: the hiss bed x1.33^step and the crackle's density
    x(1 + step/2), its peak (0.11) and the random draws unchanged, so step 0 is tts_r18.radio() sample for sample and
    every step adds static only (no dropout). Mono float at SR in, (1, n) out at -16 LUFS."""
    import numpy as np
    r18 = _filters()
    if not step:
        return r18.radio(x)
    s = r18._synth()
    sr = s.SR
    rng = np.random.default_rng(18)
    x = x / max(1e-9, np.max(np.abs(x))) * 0.8
    pad = int(0.15 * sr)
    x = np.concatenate([np.zeros(pad), x, np.zeros(pad)])
    n = len(x)
    flutter = 1 + 0.15 * s.sine(0.7, n) + 0.1 * s.sine(3.1, n)
    hiss = s.noise(n, rng) * 0.019 * 1.33 ** step * flutter
    crackle = (rng.random(n) < 0.001 * (1 + step / 2)) * rng.uniform(-1, 1, n) * 0.11
    y = x + hiss + crackle
    for _ in range(2):
        y = s.svf(y, 300, mode="hp")
        y = s.svf(y, 3400, mode="lp")
    y = s.saturate(y * 1.6, 2.0) * 0.8
    for at in (int(0.03 * sr), n - int(0.05 * sr)):
        m = int(0.004 * sr)
        click = s.noise(m, rng) * np.exp(-np.arange(m) / (0.0008 * sr)) * 0.6
        y[at:at + m] += s.svf(click, 2500, q=1.2, mode="bp") * 2.5
    return s.master(y[None, :], target_lufs=-16.0, ceiling_db=-1.5)


def clean(x, s):
    """The Choir's clean channel in Act 7 (round 84 V.1 a, voice_r84.py's channel() without a step): filter b's band
    (300 Hz-3.4 kHz, two passes) and saturation, no hiss, crackle or clicks, 5 ms fades. Mono float at s.SR in, (1, n)
    out at -16 LUFS."""
    import numpy as np
    sr = s.SR
    x = x / max(1e-9, np.max(np.abs(x))) * 0.8
    pad = int(0.15 * sr)
    y = np.concatenate([np.zeros(pad), x, np.zeros(pad)])
    for _ in range(2):
        y = s.svf(y, 300, mode="hp")
        y = s.svf(y, 3400, mode="lp")
    y = s.saturate(y * 1.6, 2.0) * 0.8
    f = int(0.005 * sr)
    y[:f] *= np.linspace(0, 1, f)
    y[-f:] *= np.linspace(1, 0, f)
    return s.master(y[None, :], target_lufs=-16.0, ceiling_db=-1.5)


def distorted(y, s):
    """A damaged channel on top of filter b: dropouts and more crackle."""
    import numpy as np
    rng = np.random.default_rng(23)
    x = y[0].copy()
    n = len(x)
    i = int(0.3 * s.SR)
    while i < n:
        i += int(rng.uniform(0.25, 0.9) * s.SR)
        m = int(rng.uniform(0.03, 0.12) * s.SR)
        x[i:i + m] *= 0.08
    x += (rng.random(n) < 0.002) * rng.uniform(-1, 1, n) * 0.15
    return s.master(x[None, :], target_lufs=-16.0, ceiling_db=-1.5)


def cut(line, x, s):
    """A cut-off line through radio filter b: the take cut where Whisper ends its last word, the
    static and the dead channel after it (tts_r32's cut_off()), and the file ending there."""
    import numpy as np
    import tts_r32
    word = cut_word(line)
    words = json.loads(TIMINGS.read_text())[line["key"]]
    hits = [end for w, _, end in words if re.sub(r"[^\w']", "", w).lower() == word]
    if not hits:
        print(f"  cut {line['key']}: Whisper did not hear {word!r} ({' '.join(w for w, _, _ in words)}); "
              "cut at its last word", flush=True)
    end = hits[-1] if hits else words[-1][2]
    y = radio(tts_r32.cut_off(x, s.SR, end, np.random.default_rng(32), s), line.get("noise", 0))
    # the channel dies: no closing click, the file ends DEAD s after the static
    y = y[:, :int((0.15 + end + tts_r32.STATIC + tts_r32.DEAD) * s.SR)]
    y[:, -int(0.002 * s.SR):] *= np.linspace(1, 0, int(0.002 * s.SR))
    print(f"  cut {line['key']} after {word!r} at {end:.2f} s of the take", flush=True)
    return y


def rubberband(x, sr, semis):
    """Pitch-shifted by `semis` semitones through ffmpeg's rubberband, formants preserved, mono float in and out
    (voice_r84.py's)."""
    import numpy as np
    opts = [f"pitch={2 ** (semis / 12):.6f}", "tempo=1.000000", "pitchq=quality", "formant=preserved"]
    r = subprocess.run(["ffmpeg", "-nostdin", "-hide_banner", "-loglevel", "error", "-f", "f32le", "-ar", str(sr),
                        "-ac", "1", "-i", "-", "-af", "rubberband=" + ":".join(opts), "-f", "f32le", "-"],
                       input=x.astype("<f4").tobytes(), capture_output=True, check=True).stdout
    return np.frombuffer(r, dtype="<f4").astype(float)


def fuse(x, s, under):
    """Vorne's fused voice (round 84 V.2 b, voice_r84.py's fuse()): his take with FUSE's copies a breath behind it,
    each at `under` dB (plus its extra) under his RMS, the copies alone through FUSE's reverb, a 1.5 s tail. Mono float
    at s.SR in and out; his voice stays at the start, so a word timing of the raw take still holds."""
    import numpy as np
    sr = s.SR
    x = x / max(1e-9, np.max(np.abs(x)))
    bus = np.zeros(len(x) + int(1.5 * sr))
    for (semi, extra), d in zip(FUSE["copies"], FUSE["delays"]):
        y = rubberband(x, sr, semi)
        y = y / max(1e-9, np.sqrt(np.mean(y ** 2))) * np.sqrt(np.mean(x ** 2)) * 10 ** ((-under + extra) / 20)
        i = int(d * sr)
        m = min(len(y), len(bus) - i)
        bus[i:i + m] += y[:m]
    seconds, wet = FUSE["reverb"]
    out = s.reverb(np.vstack([bus, bus]), seconds=seconds, mix=wet, seed=84)[0][:len(bus)].copy()
    out[:len(x)] += x
    return out


def fused_cut(line, x, s):
    """A fused line ending in a dash (Level 48's line 9): the fused take cut where Whisper ends its last word into
    the static and the dead channel (tts_r32's cut_off(), the copies and their hall cut with him), through the enemy
    filter, the file ending DEAD s after the static."""
    import numpy as np
    import tts_r32
    word = cut_word(line)
    words = json.loads(TIMINGS.read_text())[line["key"]]
    hits = [end for w, _, end in words if re.sub(r"[^\w']", "", w).lower() == word]
    if not hits:
        print(f"  cut {line['key']}: Whisper did not hear {word!r} ({' '.join(w for w, _, _ in words)}); "
              "cut at its last word", flush=True)
    end = hits[-1] if hits else words[-1][2]
    y = enemy(tts_r32.cut_off(x, s.SR, end, np.random.default_rng(32), s), s)
    y = y[:, :int((0.15 + end + tts_r32.STATIC + tts_r32.DEAD) * s.SR)]
    y[:, -int(0.002 * s.SR):] *= np.linspace(1, 0, int(0.002 * s.SR))
    print(f"  cut {line['key']} after {word!r} at {end:.2f} s of the take (fused)", flush=True)
    return y


def enemy(x, s):
    """The enemy channel's filter (M9 part B; the voice README, Act 5 voices; auditioned in round 60 as enemy() of
    tools/concept/audio/tts_r60.py): radio filter b's band (300 Hz-3.4 kHz, two passes), hiss with its slow flutter and
    saturation, without its crackle and clicks; a steady static bed under the whole line (400 Hz-4 kHz band noise 26 dB
    under the voice's RMS, swelling +-3 dB at 0.25 Hz); no dropouts. The intercept's static sweep in and out is the
    game's (it plays instead of the squelch), not baked in. Mono float at s.SR in, (1, n) out at -16 LUFS."""
    import numpy as np
    sr = s.SR
    rng = np.random.default_rng(60)
    x = x / max(1e-9, np.max(np.abs(x))) * 0.8
    pad = int(0.15 * sr)
    voice = np.concatenate([np.zeros(pad), x, np.zeros(pad)])
    n = len(voice)
    t = np.arange(n) / sr
    flutter = 1 + 0.15 * s.sine(0.7, n) + 0.1 * s.sine(3.1, n)
    y = voice + s.noise(n, rng) * 0.019 * flutter
    for _ in range(2):
        y = s.svf(y, 300, mode="hp")
        y = s.svf(y, 3400, mode="lp")
    y = s.saturate(y * 1.6, 2.0) * 0.8
    band = s.noise(n, rng)
    for _ in range(2):
        band = s.svf(band, 400, mode="hp")
        band = s.svf(band, 4000, mode="lp")
    active = np.abs(y) > 0.02
    v_rms = np.sqrt(np.mean(y[active] ** 2)) if active.any() else 0.1
    band = band / (np.sqrt(np.mean(band ** 2)) + 1e-12) * v_rms * 10 ** (-26 / 20)
    band *= 10 ** (3 * np.sin(2 * np.pi * 0.25 * t) / 20)
    y = y + band
    # 5 ms fades: the channel opens and closes under the game's sweep, without filter b's clicks
    fade = int(0.005 * sr)
    y[:fade] *= np.linspace(0, 1, fade)
    y[-fade:] *= np.linspace(1, 0, fade)
    return s.master(y[None, :], target_lufs=-16.0, ceiling_db=-1.5)


def line_end(line):
    """Where the line's last word ends in its raw take: Whisper's words matched in order to the spoken words (a
    hallucinated tail after them, such as a repeated "agreement", is not the line), else Whisper's last word."""
    words = json.loads(TIMINGS.read_text())[line["key"]]
    want = [re.sub(r"[^\w']", "", w).lower() for w in line["spoken"].split()]
    i, end = 0, None
    for w, _, e in words:
        if i < len(want) and re.sub(r"[^\w']", "", w).lower() == want[i]:
            i, end = i + 1, e
    return end if i == len(want) else words[-1][2]


def treat(line, x, s):
    """A treatment's post-processing of the raw take (mono at s.SR): the cut after the line's last word (voice_r73.py's
    cut(): the first point after it where the level stays 30 dB under the peak for 60 ms, +50 ms, a 40 ms fade), then
    the WORLD lowering."""
    import numpy as np
    t = TREATMENTS.get(line["key"], {})
    if t.get("cut"):
        end = line_end(line)
        x = x.copy()
        sr, n, hop = s.SR, int(0.02 * s.SR), int(0.01 * s.SR)
        pk = np.max(np.abs(x)) + 1e-12
        lev = np.array([20 * np.log10(np.sqrt(np.mean(x[i:i + n] ** 2)) / pk + 1e-12)
                        for i in range(0, len(x) - n, hop)])
        k = len(x)
        for f in range(int(end * sr / hop), len(lev) - 6):
            if np.all(lev[f:f + 6] < -30):
                k = min(len(x), f * hop + int(0.05 * sr))
                break
        x = x[:k]
        x[-int(0.04 * sr):] *= np.linspace(1, 0, int(0.04 * sr))
        print(f"  treat {line['key']}: cut at {k / sr:.2f} s (the line's last word ends at {end:.2f} s)", flush=True)
    if t.get("world"):
        src, dst = RAW / f"{line['key']}-cut.wav", RAW / f"{line['key']}-world.wav"
        s.write_wav(src, x / max(1e-9, np.max(np.abs(x))) * 0.8)
        subprocess.run([str(WORLD_PY), __file__, "--world", str(src), str(dst), f"{t['world']:.6f}"], check=True)
        x = s.decode(dst)[0]
        print(f"  treat {line['key']}: WORLD x{t['world']:.3f} ({12 * np.log2(t['world']):+.1f} st)", flush=True)
    return x


def post(line, log_entry):
    import numpy as np
    r18 = _filters()
    s = r18._synth()
    x = s.decode(RAW / f"{line['key']}.wav")[0]
    if line["key"] in TREATMENTS:
        x = treat(line, x, s)
    if line["layering"] == "choir":
        x = r18.choir(x)
    if line["filter"] == "dry":
        x = x / max(1e-9, np.max(np.abs(x))) * 0.8
        pad = np.zeros(int(0.1 * s.SR))
        y = s.master(np.concatenate([pad, x, pad])[None, :], target_lufs=-16.0, ceiling_db=-1.5)
    elif line["filter"] == "enemy":
        y = enemy(x, s)
    elif line["filter"] in FUSE["under"]:
        x = fuse(x, s, FUSE["under"][line["filter"]])
        y = fused_cut(line, x, s) if cut_word(line) else enemy(x, s)
    elif line["filter"] == "clean":
        y = clean(x, s)
    elif cut_word(line):
        y = cut(line, x, s)
    else:
        y = r18.pa(x) if line["filter"] == "pa" else radio(x, line.get("noise", 0))
        if line["filter"] == "distorted":
            y = distorted(y, s)
    seeds = ",".join(str(seed) for seed in log_entry.get("seeds", []))
    tags = {"COMMENT": f"tools/art/voice.py {line['voice']} {line['key']} seeds {seeds}",
            "SOURCE": "tools/art/voice.py"}
    s.write_ogg(ASSETS / line["path"].removeprefix("voice/"), y, quality=4, tags=tags)


def main(args):
    if args[:1] == ["--worker"]:
        RAW.mkdir(parents=True, exist_ok=True)
        worker(args[1])
        return
    if args[:1] == ["--whisper"]:
        whisper(args[1])
        return
    if args[:1] == ["--world"]:
        world(args[1], args[2], float(args[3]))
        return
    # Isolated as the steps' Gradle runs (M8 part I): its own project cache dir and build dir (the init script), and
    # the daemon's idle timeout (10 min) so a daemon started here ends on its own (never killed or stopped).
    GRADLE.mkdir(parents=True, exist_ok=True)
    INIT.write_text("allprojects {\n    layout.buildDirectory = file(\"" + (GRADLE / "build").as_posix()
                    + "/${project.path.replace(':', '_')}\")\n}\n")
    subprocess.run([str(ROOT / "gradlew"), "-q", "-Dorg.gradle.daemon.idletimeout=600000",
                    "--project-cache-dir", "build/voice-cache", "--init-script", str(INIT), ":pipeline:voiceLines"],
                   cwd=ROOT, check=True)
    lines = {line["path"]: line for line in json.loads(LINES.read_text())}
    todo = [line for path, line in lines.items() if not (ROOT / "assets" / path).exists()]
    only = [args[i + 1] for i, a in enumerate(args) if a == "--only"]
    if only:
        # M9 part B: a step renders its own lines only; the others (and the unused files) are left alone
        todo = [line for line in todo if any(line["source"].startswith(prefix) for prefix in only)]
        args = args + ["--keep"]
    # Only files named by a key are this renderer's: a speaker's stage sound (the Choir sings,
    # copied by :pipeline:copyPlaceholderStageSounds) stays.
    unused = sorted(p for p in ASSETS.rglob("*.ogg")
                    if re.fullmatch(r"[0-9a-f]{12}\.ogg", p.name)
                    and str(p.relative_to(ROOT / "assets")) not in lines)
    print(f"{len(lines)} lines, {len(todo)} to render, {len(unused)} unused files")
    for p in unused:
        print(("  unused " if "--keep" in args or "--list" in args else "  deleted ")
              + str(p.relative_to(ROOT)))
        if "--keep" not in args and "--list" not in args:
            p.unlink()
    if "--list" in args:
        for line in todo:
            print("  render", line["path"], line["spoken"][:60])
        return
    if todo:
        RAW.mkdir(parents=True, exist_ok=True)
        for line in todo:
            lower_ref(line)
        jobs = RAW / "jobs.json"
        jobs.write_text(json.dumps(todo, indent=1))
        env = dict(os.environ, HF_HOME=str(BASE / "hf"), TOKENIZERS_PARALLELISM="false",
                   TORCH_FORCE_NO_WEIGHTS_ONLY_LOAD="1")
        py = BASE / "venv-chatterbox" / "bin" / "python"
        subprocess.run([str(py), __file__, "--worker", str(jobs)], env=env, check=True)
        worker_log = json.loads((RAW / "worker-log.json").read_text())
        log = json.loads(LOG.read_text()) if LOG.exists() else {}
        log.update(worker_log)
        LOG.write_text(json.dumps(log, indent=1))
        cuts = [line["key"] for line in todo
                if cut_word(line) or TREATMENTS.get(line["key"], {}).get("cut")]
        if cuts:
            (RAW / "cuts.json").write_text(json.dumps(cuts))
            subprocess.run([str(WHISPER_PY), __file__, "--whisper", str(RAW / "cuts.json")], check=True,
                           env=dict(os.environ, HF_HOME=str(BASE / "hf")))
        for line in todo:
            post(line, worker_log[line["key"]])
        retried = [k for k, e in worker_log.items() if any(len(c["takes"]) > 1 for c in e["chunks"])]
        unresolved = [k for k, e in worker_log.items() if not all(c["resolved"] for c in e["chunks"])]
        print(f"rendered {len(todo)}, retried {len(retried)}, unresolved {len(unresolved)}: "
              + " ".join(unresolved))
    for d in ASSETS.glob("*"):
        if d.is_dir() and not any(d.iterdir()):
            d.rmdir()


if __name__ == "__main__":
    main(sys.argv[1:])
