#!/usr/bin/env bash
#
# Пересборка и перезапуск НА СЕРВЕРЕ. Код сюда приезжает с рабочего компьютера
# скриптом push-from-mac.sh — git на сервере не используется.
# База и загруженные файлы живут в томах Docker и пересборку переживают.
set -euo pipefail
cd "$(dirname "$0")"

if [ ! -f .env ]; then
  echo "Нет файла .env. Скопируйте: cp .env.example .env — и заполните." >&2
  exit 1
fi

echo "==> Собираем образы"
docker compose build

echo "==> Перезапускаем"
docker compose up -d

echo "==> Убираем образы прошлых сборок"
docker image prune -f

docker compose ps
