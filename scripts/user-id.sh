#!/usr/bin/env bash
# Kullanım: scripts/user-id.sh <kullanici_adi>  -> kullanıcının id'sini yazdırır (mysql istemcisi gerekir).
root="$(cd "$(dirname "$0")/.." && pwd)"
if [ ! -f "$root/.env.local" ]; then
  echo ".env.local yok (.env.example'dan kopyalayın)" >&2
  exit 1
fi
set -a; source "$root/.env.local"; set +a
MYSQL_PWD="$DB_PASSWORD" mysql -N -h 127.0.0.1 -u "$DB_USERNAME" roommates \
  -e "SELECT id FROM users WHERE username = '$1'"
