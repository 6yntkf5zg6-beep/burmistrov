#!/usr/bin/env bash
#
# Первый выпуск сертификата Let's Encrypt. Запускается один раз, после того как
# домен уже указывает на этот сервер.
#
#   ./init-letsencrypt.sh            — боевой сертификат
#   ./init-letsencrypt.sh --staging  — тестовый: браузер ему не поверит, зато можно
#                                      ошибаться сколько угодно (у боевого лимит
#                                      5 выпусков в неделю на домен)
#
# Здесь есть замкнутый круг: certbot проверяет домен через работающий сайт, а nginx
# не стартует без файла сертификата. Разрываем его самоподписанной заглушкой: она
# нужна ровно на минуту, чтобы nginx поднялся и ответил на проверку.
set -euo pipefail
cd "$(dirname "$0")"

if [ ! -f .env ]; then
  echo "Нет файла .env. Скопируйте: cp .env.example .env — и заполните." >&2
  exit 1
fi

# set -a: всё прочитанное из .env уходит в окружение, откуда его возьмёт docker compose.
set -a
# shellcheck disable=SC1091
. ./.env
set +a

: "${DOMAIN:?В .env не заполнен DOMAIN}"
: "${CERTBOT_EMAIL:?В .env не заполнен CERTBOT_EMAIL}"

STAGING_ARG=""
if [ "${1:-}" = "--staging" ]; then
  STAGING_ARG="--staging"
  echo "Режим проверки: сертификат будет тестовым."
fi

echo "==> Домен: $DOMAIN"

# Проверяем заранее: если A-запись ещё не разошлась, certbot всё равно откажет,
# но уже после того, как мы потратим попытку из недельного лимита.
SERVER_IP="$(curl -fsS --max-time 10 https://api.ipify.org || echo '')"
DOMAIN_IP="$(getent hosts "$DOMAIN" | awk '{print $1}' | head -n1 || echo '')"
if [ -n "$SERVER_IP" ] && [ -n "$DOMAIN_IP" ] && [ "$SERVER_IP" != "$DOMAIN_IP" ]; then
  echo "Внимание: $DOMAIN ведёт на $DOMAIN_IP, а сервер — $SERVER_IP." >&2
  echo "Если A-запись только что изменена, подождите и запустите снова." >&2
  read -r -p "Продолжить всё равно? [y/N] " answer
  [ "$answer" = "y" ] || exit 1
fi

echo "==> Ставим временную заглушку вместо сертификата"
docker compose run --rm --entrypoint sh certbot -c "
  mkdir -p /etc/letsencrypt/live/$DOMAIN &&
  openssl req -x509 -nodes -newkey rsa:2048 -days 1 \
    -keyout /etc/letsencrypt/live/$DOMAIN/privkey.pem \
    -out   /etc/letsencrypt/live/$DOMAIN/fullchain.pem \
    -subj  '/CN=localhost'"

echo "==> Поднимаем базу, бэкенд и веб-сервер"
docker compose up -d db app web

# Бэкенд на первом старте прогоняет миграции — веб-серверу нужно дать ему время.
echo "==> Ждём, пока nginx начнёт отвечать"
for _ in $(seq 1 30); do
  if curl -fsS --max-time 3 "http://localhost/.well-known/acme-challenge/ping" >/dev/null 2>&1; then break; fi
  # 404 на несуществующий файл — тоже ответ: значит, nginx работает.
  if curl -sS --max-time 3 -o /dev/null -w '%{http_code}' "http://localhost/" | grep -qE '^(2|3|4)'; then break; fi
  sleep 2
done

echo "==> Убираем заглушку"
docker compose run --rm --entrypoint sh certbot -c "
  rm -rf /etc/letsencrypt/live/$DOMAIN \
         /etc/letsencrypt/archive/$DOMAIN \
         /etc/letsencrypt/renewal/$DOMAIN.conf"

echo "==> Просим настоящий сертификат"
docker compose run --rm --entrypoint certbot certbot certonly \
  --webroot -w /var/www/certbot \
  $STAGING_ARG \
  --email "$CERTBOT_EMAIL" \
  -d "$DOMAIN" \
  --rsa-key-size 4096 \
  --agree-tos \
  --no-eff-email \
  --non-interactive

echo "==> Перечитываем конфиг nginx"
docker compose exec web nginx -s reload

echo "==> Запускаем продление по расписанию"
docker compose up -d

echo
echo "Готово. Откройте https://$DOMAIN"
