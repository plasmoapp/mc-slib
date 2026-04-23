#!/bin/bash
set -e

if [[ -z "$2" ]]; then
    echo "Usage: $0 <server|proxy> <gradle command>"
    echo "Example: $0 server modded:1.21-neoforge:runServer -Pmodded.versions_dev=1.21-neoforge"
    echo "Example: $0 server spigot:runServer -Pspigot.run_minecraft_version=1.16.5"
    echo "Example: $0 proxy velocity:runVelocity"
    echo "Example: $0 proxy bungee:runWaterfall"
    exit 1
fi

ENV_TYPE="$1"
shift
COMMAND="$*"

case "$ENV_TYPE" in
    server)
        PATTERNS=(
          "Done \\(.*\\)!"
          "Command 'ping' registered"
          "Command 'brigadier-entity-selector' registered"
          "Command 'brigadier-position-selector' registered"
          "Command 'brigadier-game-profiles-selector' registered"
          "Command 'brigadier-custom-type' registered"
        )
        COMMAND_INPUTS=(
          "brigadier-custom-type invalid-uuid"
          "brigadier-entity-selector entities @e"
          "brigadier-entity-selector players @a"
          "brigadier-position-selector 100 100 100"
        )
        COMMAND_OUTPUT_PATTERNS=(
          "Invalid UUID"
          "Found entities:"
          "Found players:"
          "Position: ServerPos3d\\(world=null, x=100.0, y=100.0, z=100.0, yaw=100.0, pitch=100.0\\)"
        )
        ;;
    proxy)
        PATTERNS=(
          "Listening on"
          "Command 'ping' registered"
          "Command 'brigadier-ping' registered"
          "Command 'brigadier-custom-type' registered"
        )
        COMMAND_INPUTS=(
          "brigadier-custom-type invalid-uuid"
        )
        COMMAND_OUTPUT_PATTERNS=(
          "Invalid UUID"
        )
        ;;
    *)
        echo "Error: Invalid environment type '$ENV_TYPE'. Must be 'server' or 'proxy'"
        exit 1
        ;;
esac

if [[ ${#COMMAND_INPUTS[@]} -ne ${#COMMAND_OUTPUT_PATTERNS[@]} ]]; then
    echo "Error: COMMAND_INPUTS and COMMAND_OUTPUT_PATTERNS must have the same length"
    exit 1
fi

LOGFILE=$(mktemp)
FOUND_DIR=$(mktemp -d)
STDIN_PIPE=$(mktemp -u)
mkfifo "$STDIN_PIPE"
trap "rm -rf $FOUND_DIR $LOGFILE $STDIN_PIPE" EXIT

# Open read+write so this side doesn't block waiting for a peer, and so gradle
# doesn't see EOF before we send anything.
exec 3<>"$STDIN_PIPE"

TIMEOUT=${CI:+600}
TIMEOUT=${TIMEOUT:-180}
CMD_TIMEOUT=${CMD_TIMEOUT:-5}
echo "Testing [$ENV_TYPE]: $COMMAND"

# Run in background, write to file
START_TIME=$(date +%s)
timeout $TIMEOUT ./gradlew $COMMAND \
    --console=plain \
    --no-daemon < "$STDIN_PIPE" > "$LOGFILE" 2>&1 &
PID=$!

dump_and_fail() {
    local msg="$1"
    [[ -z "$CI" ]] && echo
    echo "=== $ENV_TYPE output ==="
    cat "$LOGFILE"
    echo "=== Test result ==="
    echo "FAILED: $msg"
    kill $PID 2>/dev/null || true
    exit 1
}

# Phase 1: wait for startup patterns.
STARTUP_OK=0
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
        [[ -z "$CI" ]] && printf "\r\033[K"
        echo "All ${#PATTERNS[@]} startup patterns matched in ${ELAPSED}s"
        STARTUP_OK=1
        break
    fi

    [[ -z "$CI" ]] && printf "\rWaiting... %ds/%ds (%d/%d patterns found)" "$ELAPSED" "$TIMEOUT" "$FOUND_COUNT" "${#PATTERNS[@]}"
    sleep 1
done

if [[ $STARTUP_OK -eq 0 ]]; then
    EXIT_CODE=0
    wait $PID || EXIT_CODE=$?
    if [[ $EXIT_CODE -eq 124 ]]; then
        dump_and_fail "Startup timed out"
    else
        dump_and_fail "Process exited (code $EXIT_CODE) before all startup patterns found"
    fi
fi

# Phase 2: send each command, wait for its expected output.
for i in "${!COMMAND_INPUTS[@]}"; do
    INPUT="${COMMAND_INPUTS[$i]}"
    PATTERN="${COMMAND_OUTPUT_PATTERNS[$i]}"
    BEFORE=$(wc -l < "$LOGFILE")

    echo "Sending: $INPUT"
    echo "$INPUT" >&3

    CMD_START=$(date +%s)
    MATCHED=0
    while kill -0 $PID 2>/dev/null; do
        if tail -n +$((BEFORE + 1)) "$LOGFILE" | grep -Eq "$PATTERN"; then
            MATCH=$(tail -n +$((BEFORE + 1)) "$LOGFILE" | grep -Eo "$PATTERN" | head -1)
            [[ -z "$CI" ]] && printf "\r\033[K"
            echo "Found '$PATTERN' -> $MATCH"
            MATCHED=1
            break
        fi

        CMD_ELAPSED=$(($(date +%s) - CMD_START))
        if [[ $CMD_ELAPSED -ge $CMD_TIMEOUT ]]; then
            dump_and_fail "Command '$INPUT' did not produce pattern '$PATTERN' within ${CMD_TIMEOUT}s"
        fi

        [[ -z "$CI" ]] && printf "\rWaiting for '%s'... %ds/%ds" "$PATTERN" "$CMD_ELAPSED" "$CMD_TIMEOUT"
        sleep 1
    done
    [[ -z "$CI" ]] && echo

    if [[ $MATCHED -eq 0 ]]; then
        EXIT_CODE=0
        wait $PID || EXIT_CODE=$?
        dump_and_fail "Process exited (code $EXIT_CODE) before pattern '$PATTERN' appeared for '$INPUT'"
    fi
done

echo "=== $ENV_TYPE output ==="
cat "$LOGFILE"
echo "=== Test result ==="
TOTAL_ELAPSED=$(($(date +%s) - START_TIME))
echo "All ${#PATTERNS[@]} startup patterns and ${#COMMAND_INPUTS[@]} command patterns matched in ${TOTAL_ELAPSED}s"
kill $PID 2>/dev/null || true
exit 0
