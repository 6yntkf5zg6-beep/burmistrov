#!/usr/bin/env bash
#
# Выкатка новой версии НА СЕРВЕРЕ: забрать свежий код с GitHub, пересобрать образы,
# перезапустить. База и загруженные файлы живут в томах Docker и пересборку переживают.
set -euo pipefail
cd "$(dirname "$0")"

if [ ! -f .env ]; then
  echo "Нет файла .env. Скопируйте: cp .env.example .env — и заполните." >&2
  exit 1
fi

set -a
# shellcheck disable=SC1091
. ./.env
set +a

BACKEND_PATH="${BACKEND_PATH:-..}"
UI_PATH="${UI_PATH:-../../burmistrov-ui}"

echo "==> Забираем изменения с GitHub"
# --ff-only: если на сервере что-то правили руками, выкатка остановится здесь,
# а не создаст неожиданный merge-коммит поверх чужих правок.
git -C "$BACKEND_PATH" pull --ff-only
git -C "$UI_PATH" pull --ff-only

echo "==> Собираем образы"
docker compose build

echo "==> Перезапускаем"
docker compose up -d

echo "==> Убираем образы прошлых сборок"
docker image prune -f

docker compose ps
