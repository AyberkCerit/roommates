#!/usr/bin/env bash

source scripts/creds.sh

scripts/check.sh 200 GET /actuator/health
scripts/check.sh 200 GET /actuator/info

grep -q '"name":"RoomMates"' /tmp/rm-check-body
echo "PASS name:RoomMates"
grep -q '"version":"0.1.0"' /tmp/rm-check-body
echo "PASS version: 0.1.0"
grep -q 'Öğrencilerin' /tmp/rm-check-body
echo "PASS description:Öğrencilerin"

scripts/check.sh 200 GET /v3/api-docs

grep -q '"basicAuth"' /tmp/rm-check-body
echo "PASS basicAuth"

scripts/check.sh 200 GET /swagger-ui/index.html
