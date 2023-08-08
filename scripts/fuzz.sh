#!/bin/bash
readonly RESULTS_DIRECTORY=$1
readonly SUBJECT=$2
readonly DURATION=$3
readonly PROJECT_ROOT=$(pwd)
readonly SETTINGS_FILE="$(pwd)/resources/settings.xml"

# Write a trace for each command to standard error
set -x
# Exit immediately if any simple command fails
set -e

# Print the arguments
echo "Running: results_directory=$RESULTS_DIRECTORY, subject=$SUBJECT, duration=$DURATION"

# Export Java home
export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64

# Create a temporary directory for Meringue's output
readonly OUTPUT_DIRECTORY="$PROJECT_ROOT/target/meringue"
mkdir -p "$OUTPUT_DIRECTORY"

# Build and install the project
mvn -B -q -ntp -e \
  -f "$PROJECT_ROOT" \
  -s "$SETTINGS_FILE" \
  -DskipTests install

# Run the fuzzing campaign
mvn -ntp -B -e \
  -f "$PROJECT_ROOT" \
  -s "$SETTINGS_FILE" \
  -pl :zeugma-hint-evaluation \
  -P"$SUBJECT" \
  meringue:fuzz \
  meringue:analyze \
  -Dmeringue.duration="$DURATION" \
  -Dmeringue.outputDirectory="$OUTPUT_DIRECTORY"

# Record configuration information
echo "{
  \"subject\": \"$SUBJECT\",
  \"commit_sha\": \"$(git --git-dir "$PROJECT_ROOT/.git" rev-parse HEAD)\",
  \"branch_name\": \"$(git --git-dir "$PROJECT_ROOT/.git" rev-parse --abbrev-ref HEAD)\",
  \"remote_origin_url\": \"$(git --git-dir "$PROJECT_ROOT/.git" config --get remote.origin.url)\"
}" >"$OUTPUT_DIRECTORY/fuzz-info.json"

# Create a TAR archive of the Meringue output directory and move it to the results directory
ARCHIVE_NAME='meringue.tgz'
mkdir -p "$RESULTS_DIRECTORY"
tar -czf "$ARCHIVE_NAME" -C "$OUTPUT_DIRECTORY" .
mv "$ARCHIVE_NAME" "$RESULTS_DIRECTORY/"

# Copy all normal files in the Meringue output directory to the results directory
find "$OUTPUT_DIRECTORY" -maxdepth 1 -type f -exec cp -t "$RESULTS_DIRECTORY" {} +
# If Zeugma's statistics file exists, copy it to the results directory
ZEUGMA_STATS_FILE="$OUTPUT_DIRECTORY/campaign/statistics.csv"
if [ -f "$ZEUGMA_STATS_FILE" ]; then
  cp "$ZEUGMA_STATS_FILE" "$RESULTS_DIRECTORY/zeugma.csv"
fi
