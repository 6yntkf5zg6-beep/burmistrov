#!/usr/bin/env bash
#
# Заливка кода с рабочего компьютера на сервер. Запускается НА МАКЕ, не на сервере.
#
#   ./push-from-mac.sh root@1.2.3.4
#   ./push-from-mac.sh root@1.2.3.4 --no-restart   # только залить, не пересобирать
#
# Проекты не в git, поэтому источник правды — папки на этом компьютере.
# rsync копирует только изменившиеся файлы, так что вторая и все следующие
# выкатки занимают секунды.
set -euo pipefail

SERVER="${1:-${SERVER:-}}"
if [ -z "$SERVER" ]; then
  echo "Укажите сервер: ./push-from-mac.sh root@IP-адрес" >&2
  exit 1
fi

REMOTE_DIR="${REMOTE_DIR:-/opt/stan}"

# Папка deploy лежит внутри проекта бэкенда, значит на уровень выше — его корень.
BACKEND_SRC="$(cd "$(dirname "$0")/.." && pwd)"
UI_SRC="${UI_SRC:-$HOME/UIprojects/burmistrov-ui}"

if [ ! -d "$UI_SRC" ]; then
  echo "Не нашёл фронтенд в $UI_SRC. Укажите путь: UI_SRC=/путь ./push-from-mac.sh $SERVER" >&2
  exit 1
fi

echo "==> Бэкенд: $BACKEND_SRC"
echo "==> Фронтенд: $UI_SRC"
echo "==> Сервер: $SERVER:$REMOTE_DIR"
echo

# --delete убирает на сервере файлы, которых больше нет у нас, иначе удалённый
# класс или картинка остались бы там навсегда. Исключённые пути rsync при этом
# не трогает — поэтому .env, база и загруженные файлы на сервере в безопасности.
echo "==> Заливаем бэкенд"
rsync -az --delete \
  --exclude '.git/' \
  --exclude '.idea/' \
  --exclude '.claude/' \
  --exclude 'target/' \
  --exclude 'pgdata/' \
  --exclude 'uploads/' \
  --exclude 'local-postgres/' \
  --exclude 'insomnia/' \
  --exclude '_to_delete/' \
  --exclude 'Claude outputs/' \
  --exclude '*.tgz' \
  --exclude '.DS_Store' \
  --exclude 'deploy/.env' \
  "$BACKEND_SRC/" "$SERVER:$REMOTE_DIR/burmistrov/"

echo "==> Заливаем фронтенд"
rsync -az --delete \
  --exclude '.git/' \
  --exclude '.idea/' \
  --exclude 'node_modules/' \
  --exclude 'dist/' \
  --exclude '_to_delete/' \
  --exclude '*.tgz' \
  --exclude '.DS_Store' \
  --exclude '/*.mjs' \
  --exclude '/*.png' \
  "$UI_SRC/" "$SERVER:$REMOTE_DIR/burmistrov-ui/"

if [ "${2:-}" = "--no-restart" ]; then
  echo
  echo "Код залит. Пересборку не запускал (--no-restart)."
  exit 0
fi

echo
echo "==> Пересобираем на сервере"
# -t даёт нормальный живой вывод сборки вместо тишины на несколько минут.
ssh -t "$SERVER" "cd $REMOTE_DIR/burmistrov/deploy && ./update.sh"
