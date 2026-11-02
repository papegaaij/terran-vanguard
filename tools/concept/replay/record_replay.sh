#!/bin/sh
# Records the content autopilot's commands for a level start (RecordReplay.java), for the game's --replay
# debug option. Usage, from anywhere:
#   tools/concept/replay/record_replay.sh <level> <easy|medium|hard> <out.rec> [--loadout ...] [--special ...] [--escort ...]
# The options are the game's own level-start options, so the game flown with the same options plus
# --replay <out.rec> starts from the same state. Extra Gradle arguments (an init script, a project cache
# directory) go in GRADLE_ARGS.
set -eu
ROOT=$(cd "$(dirname "$0")/../../.." && pwd)
WORK=$(mktemp -d)
trap 'rm -rf "$WORK"' EXIT
cat > "$WORK/classpath.gradle" <<'GRADLE'
allprojects {
    if (path == ':content') {
        tasks.register('printReplayClasspath') {
            def classpath = project.sourceSets.test.runtimeClasspath
            dependsOn 'testClasses', classpath
            def out = file(System.getenv('REPLAY_CLASSPATH_FILE'))
            doLast { out.text = classpath.asPath }
        }
    }
}
GRADLE
# shellcheck disable=SC2086
REPLAY_CLASSPATH_FILE="$WORK/classpath.txt" "$ROOT/gradlew" -p "$ROOT" -q --init-script "$WORK/classpath.gradle" \
    ${GRADLE_ARGS:-} :content:printReplayClasspath
java -cp "$(cat "$WORK/classpath.txt")" "$ROOT/tools/concept/replay/RecordReplay.java" "$@"
