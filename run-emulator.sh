#!/usr/bin/env bash
# Start a local Android Virtual Device. Requires Android SDK + emulator on this machine.
#
# Usage:
#   ./run-emulator.sh                    # interactive menu (TTY only)
#   ./run-emulator.sh <avd-name> [extra emulator flags...]
#   EMULATOR_AVD=Pixel_6 ./run-emulator.sh [flags...]

set -euo pipefail

resolve_sdk() {
  if [[ -n "${ANDROID_SDK_ROOT:-}" && -d "${ANDROID_SDK_ROOT}/emulator" ]]; then
    printf '%s' "$ANDROID_SDK_ROOT"
    return 0
  fi
  if [[ -n "${ANDROID_HOME:-}" && -d "${ANDROID_HOME}/emulator" ]]; then
    printf '%s' "$ANDROID_HOME"
    return 0
  fi
  local candidates=(
    "${HOME}/Library/Android/sdk"
    "${HOME}/Android/Sdk"
  )
  for d in "${candidates[@]}"; do
    if [[ -d "${d}/emulator" ]]; then
      printf '%s' "$d"
      return 0
    fi
  done
  return 1
}

collect_avds() {
  avds=()
  while IFS= read -r line || [[ -n "$line" ]]; do
    line="${line//$'\r'/}"
    [[ -n "$line" ]] || continue
    avds+=("$line")
  done < <("${EMULATOR_BIN}" -list-avds 2>/dev/null || true)
}

SDK="$(resolve_sdk)" || {
  echo "Android SDK not found. Install Android Studio / SDK Platform Tools, or set ANDROID_SDK_ROOT or ANDROID_HOME." >&2
  exit 1
}

EMULATOR_BIN="${SDK}/emulator/emulator"
if [[ ! -x "$EMULATOR_BIN" ]]; then
  echo "Emulator binary missing: ${EMULATOR_BIN}" >&2
  exit 1
fi

avds=()

if [[ -n "${1:-}" ]]; then
  AVD="$1"
  shift
elif [[ -n "${EMULATOR_AVD:-}" ]]; then
  AVD="$EMULATOR_AVD"
elif [[ -t 0 ]]; then
  collect_avds
  n="${#avds[@]}"
  if [[ "$n" -eq 0 ]]; then
    echo "No AVDs found. Create one in Android Studio (Device Manager)." >&2
    exit 1
  fi
  echo ""
  echo "Available emulators — pick one:"
  echo "-----------------------------------"
  for i in "${!avds[@]}"; do
    printf '  %2d) %s\n' "$((i + 1))" "${avds[$i]}"
  done
  echo "-----------------------------------"
  while true; do
    read -r -p "Enter number (1–${n}), or q to quit: " choice || true
    choice="${choice//[$'\t\r\n ']/}"
    [[ "$choice" == [qQ] ]] && echo "Cancelled." >&2 && exit 0
    if [[ "$choice" =~ ^[0-9]+$ ]] && ((choice >= 1 && choice <= n)); then
      AVD="${avds[$((choice - 1))]}"
      echo "Starting: ${AVD}"
      break
    fi
    echo "Invalid choice. Try a number from 1 to ${n}." >&2
  done
else
  echo "No AVD specified (stdin is not a TTY — use a name or EMULATOR_AVD)." >&2
  "${EMULATOR_BIN}" -list-avds >&2 || true
  echo >&2
  echo "Usage: $0 <avd-name> [emulator options...]" >&2
  echo "   or: EMULATOR_AVD=<name> $0 [emulator options...]" >&2
  exit 1
fi

exec "${EMULATOR_BIN}" -avd "$AVD" "$@"
