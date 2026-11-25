#!/bin/bash
set -e

if [[ -z "$2" ]]; then
    echo "Usage: $0 <server|proxy> <gradle command>"
    echo "Example: $0 server modded:1.21-neoforge:runServer -Pmodded.versions_dev=1.21-neoforge"
    echo "Example: $0 server paper:runServer -Ppaper.run_minecraft_version=1.21.11"
    echo "Example: $0 proxy velocity:runVelocity"
    echo "Example: $0 proxy bungee:runWaterfall"
    exit 1
fi

ENV_TYPE="$1"
shift
COMMAND="$*"

FORBIDDEN_PATTERNS=()

case "$ENV_TYPE" in
    server)
        # console's stack is always the overworld on paper/modded, so there is a world even without /execute in.
        # minestom still takes it from the executor, which is null for the console.
        case "$COMMAND" in
            *minestom*) POSITION_WORLD="null" ;;
            *)          POSITION_WORLD="[a-zA-Z0-9_.]+World@[0-9a-f]+" ;;
        esac

        # modded 26.4+ only accepts permission requirements, so commands with raw requires fail to register.
        RAW_REQUIRES_REJECTED=0
        if [[ "$COMMAND" =~ modded:([0-9.]+)- ]] \
            && [[ "$(printf '%s\n' 26.4 "${BASH_REMATCH[1]}" | sort -V | head -1)" == "26.4" ]]; then
            RAW_REQUIRES_REJECTED=1
        fi

        PATTERNS=(
          "Done \\(.*\\)!"
          "Command 'ping' registered"
          "Command 'brigadier-entity-selector' registered"
          "Command 'brigadier-position-selector' registered"
          "Command 'brigadier-block-position-selector' registered"
          "Command 'brigadier-game-profiles-selector' registered"
          "Command 'brigadier-custom-type' registered"
          "Command 'brigadier-nested-custom-type' registered"
          "Command 'brigadier-server-translation' registered"
          "Command 'brigadier-multi-arg' registered"
          "Command 'brigadier-literal-after-argument' registered"
          "Command 'brigadier-redirect' registered"
          "Message from main thread"
          "Channel handler registered: slib:channels/test"
          "Permission 'slib.test' registered"
        )
        FORBIDDEN_PATTERNS+=(
          "not registered in the mod loader networking"
          "Denied requires bypassed"
          "slibtest\\.(argument|command)\\."
        )
        COMMAND_INPUTS=(
          "brigadier-custom-type invalid-uuid"
          "brigadier-nested-custom-type everyone"
          "brigadier-nested-custom-type @a"
          "brigadier-server-translation bad"
          "brigadier-server-translation ok"
          "brigadier-entity-selector entities @e"
          "brigadier-entity-selector players @a"
          "brigadier-position-selector 100 100 100"
          "brigadier-position-selector 100.5 64 -31.75"
          "brigadier-block-position-selector 100 100 100"
          "brigadier-multi-arg 7 13"
          "brigadier-literal-after-argument 21 double"
          "brigadier-redirect again again 5"
          "brigadier-redirect fork again 6"
          "brigadier-requirement-true"
          "brigadier-requirement-false"
          "brigadier-requirement-op"
          "brigadier-requirement-not-op"
          "brigadier-silent-feedback"
          "brigadier-multi-arg 7 abc"
          "brigadier-intermediate-custom-arg 1 2 3 \"unclosed"
        )
        COMMAND_OUTPUT_PATTERNS=(
          "Invalid UUID"
          "Nested custom type: everyone"
          "Nested custom type: native selector"
          "slib argument error: bad"
          "slib execute error: ok"
          "Found entities:"
          "Found players:"
          "Position: ServerPos3d\\(world=$POSITION_WORLD, x=100.0, y=100.0, z=100.0, yaw=0.0, pitch=0.0\\)"
          "Position: ServerPos3d\\(world=$POSITION_WORLD, x=100.5, y=64.0, z=-31.75,"
          "Block position: ServerPos3d\\(world=$POSITION_WORLD, x=100.0, y=100.0, z=100.0,"
          "Multi-arg: a=7, b=13"
          "Literal after argument: 42"
          "Redirect: 5"
          "Redirect: 6"
          "Permission requirement passed: TRUE, silent=false"
          "Permission requirement passed: FALSE, silent=false"
          "Permission requirement passed: OP, silent=false"
          "Permission requirement passed: NOT_OP, silent=false"
          "Silent check feedback"
          "Expected integer"
          "Unclosed quoted string"
        )

        if [[ $RAW_REQUIRES_REJECTED -eq 1 ]]; then
            PATTERNS+=(
              "Failed to register command 'brigadier-unbound-requires'"
              "Failed to register command 'brigadier-denied-requires'"
              "Command node 'brigadier-denied-requires literal': raw requires predicates are not supported"
              "Failed to load function slibtest:unbound_requires"
            )
            COMMAND_INPUTS+=(
              "brigadier-unbound-requires"
              "brigadier-denied-requires literal"
              "brigadier-denied-requires argument 7"
            )
            COMMAND_OUTPUT_PATTERNS+=(
              "Unknown or incomplete command"
              "Unknown or incomplete command"
              "Unknown or incomplete command"
            )
        else
            COMMAND_INPUTS+=(
              "brigadier-unbound-requires"
              "brigadier-denied-requires literal"
              "brigadier-denied-requires argument 7"
              "brigadier-denied-requires"
              "brigadier-denied-requires argument 8"
            )
            COMMAND_OUTPUT_PATTERNS+=(
              "Unbound requires guard survived parsing"
              "Denied requires checked: literal"
              "Denied requires checked: argument"
              "Unknown or incomplete command"
              "Incorrect argument for command"
            )
        fi

        # minestom has no datapacks, so it never hits bootstrap phase.
        if [[ "$COMMAND" != *minestom* ]]; then
            PATTERNS+=(
              "Silent check: silent=true"
              "Multi-arg: a=21, b=34"
              "Permission requirement passed: FALSE, silent=true"
            )
            if [[ $RAW_REQUIRES_REJECTED -eq 1 ]]; then
                FORBIDDEN_PATTERNS+=(
                  "Failed to load function slibtest:(test|macro)"
                )
            else
                PATTERNS+=(
                  "Requirement of the command node 'brigadier-unbound-requires' threw before the server was initialized"
                )
                FORBIDDEN_PATTERNS+=(
                  "Failed to load function"
                  "Whilst parsing command"
                )
            fi
            COMMAND_INPUTS+=(
              "execute positioned 100.5 64 -31.75 run brigadier-position-selector ~ ~ ~"
              "execute positioned 100.5 64 -31.75 run brigadier-block-position-selector ~ ~ ~"
            )
            COMMAND_OUTPUT_PATTERNS+=(
              "Position: ServerPos3d\\(world=$POSITION_WORLD, x=100.5, y=64.0, z=-31.75,"
              "Block position: ServerPos3d\\(world=$POSITION_WORLD, x=100.0, y=64.0, z=-32.0,"
            )
        fi

        if [[ "$COMMAND" == *neoforge* ]]; then
            PATTERNS+=(
              "PermissionAPI nodes: \\[slib\\.test\\]"
            )
        fi
        ;;
    proxy)
        PATTERNS=(
          "Listening on"
          "Command 'ping' registered"
          "Permission 'slib.test' registered"
          "Command 'brigadier-ping' registered"
          "Command 'brigadier-custom-type' registered"
          "Command 'brigadier-server-translation' registered"
        )
        COMMAND_INPUTS=(
          "brigadier-custom-type invalid-uuid"
          "brigadier-server-translation bad"
          "brigadier-server-translation ok"
        )
        COMMAND_OUTPUT_PATTERNS=(
          "Invalid UUID"
          "slib argument error: bad"
          "slib execute error: ok"
        )
        FORBIDDEN_PATTERNS+=(
          "slibtest\\.(argument|command)\\."
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

check_forbidden() {
    local pattern match
    for pattern in "${FORBIDDEN_PATTERNS[@]}"; do
        if grep -Eq "$pattern" "$LOGFILE"; then
            match=$(grep -E "$pattern" "$LOGFILE" | head -1)
            [[ -z "$CI" ]] && printf "\r\033[K"
            dump_and_fail "Forbidden pattern '$pattern' found -> $match"
        fi
    done
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

    check_forbidden

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

check_forbidden

echo "=== $ENV_TYPE output ==="
cat "$LOGFILE"
echo "=== Test result ==="
TOTAL_ELAPSED=$(($(date +%s) - START_TIME))
echo "All ${#PATTERNS[@]} startup patterns and ${#COMMAND_INPUTS[@]} command patterns matched in ${TOTAL_ELAPSED}s"
echo "None of the ${#FORBIDDEN_PATTERNS[@]} forbidden patterns matched"
kill $PID 2>/dev/null || true
exit 0
