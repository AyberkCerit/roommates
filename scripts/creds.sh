#!/usr/bin/env bash
# source ile kullanılır: A, E, V, P değişkenlerini kullanici:parola biçiminde kurar.
_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
if [[ -f "$_root/.env.local" ]]; then
  set -a; source <(tr -d '\r' < "$_root/.env.local"); set +a
fi
A="admin:${SEED_ADMIN_PASSWORD:-Admin.2026!}"
E="editor:${SEED_EDITOR_PASSWORD:-Editor.2026!}"
V="viewer:${SEED_VIEWER_PASSWORD:-Viewer.2026!}"
P="pasif:${SEED_INACTIVE_PASSWORD:-Pasif.2026!}"
unset _root
