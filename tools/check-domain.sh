#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
OUT="${TMPDIR:-/tmp}/rice-field-domain-smoke.jar"
kotlinc \
 app/src/main/java/com/example/ricefieldsurvey/domain/model/Interview.kt \
 app/src/main/java/com/example/ricefieldsurvey/domain/model/SurveyOptions.kt \
 app/src/main/java/com/example/ricefieldsurvey/domain/model/SurveyValidator.kt \
 tools/DomainSmoke.kt -include-runtime -d "$OUT"
java -jar "$OUT"
