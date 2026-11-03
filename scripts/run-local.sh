#!/usr/bin/env bash
# .env.local içindeki değişkenleri okuyup uygulamayı başlatır.
cd "$(dirname "$0")/.." || exit 1
set -a
# Windows Not Defteri CRLF kaydederse parolanın sonundaki \r silinir.
[[ -f .env.local ]] && source <(tr -d '\r' < .env.local)
set +a
exec ./mvnw spring-boot:run
