#!/usr/bin/env bash
# Kullanım: scripts/check.sh <beklenen_kod> <METHOD> <yol> [kullanici:parola|-] [json_govde]
expected=$1; method=$2; path=$3; auth=${4:--}; body=$5
args=(-s -o /tmp/rm-check-body -w '%{http_code}' -X "$method" "${BASE_URL:-http://localhost:8080}$path")
[[ "$auth" != "-" ]] && args+=(-u "$auth")
[[ -n "$body" ]] && args+=(-H 'Content-Type: application/json' --data "$body")
code=$(curl "${args[@]}")
if [[ "$code" == "$expected" ]]; then
  echo "PASS $code $method $path"
  grep -o '"id":[0-9]*' /tmp/rm-check-body | head -1 | cut -d: -f2 > /tmp/rm-last-id
else
  : > /tmp/rm-last-id
  echo "FAIL $method $path: beklenen $expected, gelen $code"; cat /tmp/rm-check-body; echo; exit 1
fi
