#!/usr/bin/env bash
# .env.local içindeki değişkenleri okuyup uygulamayı başlatır.
cd "$(dirname "$0")/.." || exit 1
set -a
[[ -f .env.local ]] && source .env.local
set +a
exec ./mvnw spring-boot:run
