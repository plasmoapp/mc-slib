#!/bin/bash
set -e

if [[ -z "$1" ]]; then
    echo "Usage: $0 <gradle command>"
    echo "Example: $0 modded:1.21-neoforge:runServer -Pmodded.versions_dev=1.21-neoforge"
    echo "Example: $0 spigot:runServer -Pspigot.run_minecraft_version=1.16.5 -Pspigot.run_java_version=21"
    exit 1
fi

COMMAND="$*"
PATTERNS=("Done \(.*\)!" "Command 'ping' registered")
LOGFILE=$(mktemp)
FOUND_DIR=$(mktemp -d)
trap "rm -rf $FOUND_DIR $LOGFILE" EXIT

TIMEOUT=180
echo "Testing: $COMMAND"

# Run server in background, write to file
START_TIME=$(date +%s)
timeout $TIMEOUT ./gradlew $COMMAND \
    --console=plain \
    --no-daemon > "$LOGFILE" 2>&1 &
PID=$!

# Poll the log file
while kill -0 $PID 2>/dev/null; do
    for i in "${!PATTERNS[@]}"; do
        if [[ ! -f "$FOUND_DIR/$i" ]] && grep -Eq "${PATTERNS[$i]}" "$LOGFILE"; then
            touch "$FOUND_DIR/$i"
            MATCH=$(grep -Eo "${PATTERNS[$i]}" "$LOGFILE" | head -1)
            [[ -z "$CI" ]] && printf "\r\033[K"
            echo "Found '${PATTERNS[$i]}' -> $MATCH"
        fi
    done

    FOUND_COUNT=$(ls "$FOUND_DIR" 2>/dev/null | wc -l)
    ELAPSED=$(($(date +%s) - START_TIME))

    if [[ $FOUND_COUNT -eq ${#PATTERNS[@]} ]]; then
        kill $PID 2>/dev/null

        echo "=== Server output ==="
        cat "$LOGFILE"

        echo "=== Test result ==="
        echo "All ${#PATTERNS[@]} patterns matched in ${ELAPSED}s"

        exit 0
    fi

    [[ -z "$CI" ]] && printf "\rWaiting... %ds/%ds (%d/%d patterns found)" "$ELAPSED" "$TIMEOUT" "$FOUND_COUNT" "${#PATTERNS[@]}"
    sleep 1
done
[[ -z "$CI" ]] && echo

EXIT_CODE=0
wait $PID || EXIT_CODE=$?

echo "=== Server output ==="
cat "$LOGFILE"

if [[ $EXIT_CODE -eq 124 ]]; then
    echo "FAILED: Server startup timed out"
else
    echo "FAILED: Server exited (code $EXIT_CODE) before all patterns found"
fi
exit 1
