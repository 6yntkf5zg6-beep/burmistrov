# Установка «СТАН» на сервер

Пошаговая инструкция: VPS с Ubuntu, Docker, свой домен и бесплатный сертификат
Let's Encrypt. После неё сайт открывается по `https://ваш-домен`, а выкатка новой
версии делается одной командой.

Всё приложение живёт в четырёх контейнерах:

| Контейнер | Что делает |
|---|---|
| `db` | PostgreSQL. Данные — в отдельном томе, наружу порт не открыт. |
| `app` | Spring Boot. Слушает 8080 только внутри, миграции Liquibase накатывает сам при старте. |
| `web` | nginx. Отдаёт собранный фронтенд и проксирует `/api` и `/uploads` на `app`. Единственный, кто смотрит в интернет. |
| `certbot` | Раз в 12 часов проверяет, не пора ли продлить сертификат. |

---

## 1. Что нужно до начала

- **VPS**: 2 ГБ оперативной памяти и 20 ГБ диска — минимум, комфортно с 4 ГБ.
  Сборка фронтенда и Maven на 1 ГБ падают по памяти.
  Подойдут Timeweb Cloud, Selectel, Beget, Yandex Cloud — любой с Ubuntu 24.04.
- **Домен** и доступ к его DNS.
- **Рабочий Мак** с обеими папками проекта: код заливается на сервер прямо с него
  (git в проектах пока не заведён).

## 2. Домен смотрит на сервер

В панели управления доменом заведите A-запись:

```
Тип: A    Имя: @ (или поддомен)    Значение: IP вашего сервера
```

Проверьте с любой машины — адрес должен совпасть с IP сервера:

```bash
dig +short ваш-домен
```

Обновление DNS занимает от минуты до нескольких часов. **Не переходите к шагу 6,
пока команда не показывает нужный IP**: у Let's Encrypt лимит в 5 выпусков
сертификата на домен в неделю, и неудачные попытки его тоже тратят.

## 3. Docker на сервере

Заходим на сервер и ставим Docker официальным скриптом:

```bash
ssh root@IP-сервера

curl -fsSL https://get.docker.com | sh
```

Проверяем:

```bash
docker --version
docker compose version
```

Открываем порты (если на сервере включён фаервол):

```bash
ufw allow 22/tcp && ufw allow 80/tcp && ufw allow 443/tcp && ufw enable
```

> Порт 5432 наружу открывать не нужно и не следует: к базе ходит только `app`,
> по внутренней сети Docker.

## 4. Код на сервере

Проекты не в git, поэтому код заливается прямо с рабочего компьютера. Этим занимается
`push-from-mac.sh` — он запускается **на Маке**, а не на сервере.

Сначала создайте папку на сервере:

```bash
mkdir -p /opt/stan
```

Затем **в отдельном окне терминала на Маке**:

```bash
cd /Users/leonid/IdeaProjects/burmistrov/deploy
./push-from-mac.sh root@IP-сервера --no-restart
```

`--no-restart` на первый раз обязателен: собирать ещё нечем, `.env` не заполнен.

Скрипт копирует обе папки, пропуская `node_modules`, `target`, `pgdata`, `.idea` и
прочее, чему на сервере делать нечего. На сервере получится:

```
/opt/stan/
├── burmistrov/          ← бэкенд, здесь же папка deploy
│   └── deploy/
└── burmistrov-ui/       ← фронтенд
```

Если фронтенд лежит не в `~/UIprojects/burmistrov-ui`, укажите путь явно:

```bash
UI_SRC=/путь/к/burmistrov-ui ./push-from-mac.sh root@IP-сервера --no-restart
```

> Когда проекты попадут в git, заливку можно будет заменить на `git clone` и `git pull`,
> а `push-from-mac.sh` выбросить. Пока источник правды — папки на вашем компьютере,
> и держать их резервную копию стоит независимо от сервера.

## 5. Настройки

```bash
cd /opt/stan/burmistrov/deploy
cp .env.example .env
nano .env
```

Заполните обязательное:

```ini
DOMAIN=ваш-домен.ру
CERTBOT_EMAIL=ваша@почта.ру
DB_PASSWORD=...
JWT_SECRET=...
SEED_TRAINER_PASSWORD=...
```

Пароли **не придумывайте руками** — сгенерируйте:

```bash
openssl rand -base64 24   # для DB_PASSWORD
openssl rand -base64 48   # для JWT_SECRET
```

Что это за ключи:

- `JWT_SECRET` — ключ, которым подписываются токены входа. Кто его знает, тот
  может войти под любым пользователем. Значение по умолчанию из `application.yml`
  лежит в открытом коде и годится только для разработки.
- `SEED_TRAINER_EMAIL` / `SEED_TRAINER_PASSWORD` — учётная запись тренера, которую
  приложение заводит при первом запуске на пустой базе. **Смените пароль сразу
  после первого входа.**
- `GYM_TIMEZONE` — часовой пояс зала. По нему сервер решает, наступил ли день
  тренировки; без него сервер в UTC закроет доступ, когда в зале ещё вечер.

Закройте файл от чужих глаз:

```bash
chmod 600 .env
```

## 6. Первый запуск и сертификат

Один скрипт делает всё: поднимает контейнеры, получает сертификат и включает
автопродление.

```bash
./init-letsencrypt.sh
```

Хотите сначала проверить, что всё складывается, не тратя лимит Let's Encrypt:

```bash
./init-letsencrypt.sh --staging
```

Тестовому сертификату браузер не поверит и покажет предупреждение — это нормально.
Убедившись, что сайт открывается, удалите тестовый сертификат и получите настоящий:

```bash
docker compose run --rm --entrypoint sh certbot -c "rm -rf /etc/letsencrypt/*"
./init-letsencrypt.sh
```

Первая сборка идёт 5–15 минут: Maven тянет зависимости, npm ставит пакеты.
Дальнейшие сборки быстрее — Docker переиспользует слои.

## 7. Проверка

```bash
docker compose ps        # все четыре контейнера в состоянии Up
docker compose logs -f app
```

Откройте `https://ваш-домен` и войдите под `SEED_TRAINER_EMAIL`.
**Первым делом смените пароль тренера.**

## 8. Перенос загруженных файлов (если они уже есть)

Фото и видео упражнений лежат в томе `burmistrov_uploads`. Чтобы перенести их
с рабочего компьютера:

```bash
# на компьютере: упаковать
cd /Users/leonid/IdeaProjects/burmistrov
tar czf uploads.tgz uploads
scp uploads.tgz root@IP-сервера:/tmp/

# на сервере: распаковать прямо в том
cd /opt/stan/burmistrov/deploy
docker compose cp /tmp/uploads.tgz app:/tmp/
docker compose exec app sh -c "cd /app && tar xzf /tmp/uploads.tgz --strip-components=1 -C uploads"
```

---

## Повседневные команды

**На Маке**, из папки `/Users/leonid/IdeaProjects/burmistrov/deploy`:

```bash
./push-from-mac.sh root@IP-сервера     # залить изменения и пересобрать — обычная выкатка
./push-from-mac.sh root@IP-сервера --no-restart   # только залить
```

**На сервере**, из папки `/opt/stan/burmistrov/deploy`:

```bash
./update.sh                     # пересобрать и перезапустить (код уже залит)

docker compose ps               # что работает
docker compose logs -f app      # логи бэкенда
docker compose logs -f web      # логи nginx
docker compose restart app      # перезапустить бэкенд
docker compose down             # остановить всё (данные останутся)
docker compose up -d            # запустить снова
```

### Резервная копия базы

Без неё однажды будет очень грустно. Проверьте, что копия действительно
восстанавливается, — непроверенной копии не существует.

```bash
# сделать
docker compose exec -T db pg_dump -U "$DB_USER" "$DB_NAME" | gzip > ~/stan-$(date +%F).sql.gz

# восстановить
gunzip -c ~/stan-2026-09-20.sql.gz | docker compose exec -T db psql -U "$DB_USER" -d "$DB_NAME"
```

Копию каждую ночь в 3:00 и хранение две недели:

```bash
crontab -e
```

```cron
0 3 * * * cd /opt/stan/burmistrov/deploy && set -a && . ./.env && set +a && docker compose exec -T db pg_dump -U "$DB_USER" "$DB_NAME" | gzip > /root/backups/stan-$(date +\%F).sql.gz && find /root/backups -name 'stan-*.sql.gz' -mtime +14 -delete
```

Не забудьте `mkdir -p /root/backups`. И держите копии ещё где-то, кроме этого же
сервера: диск умирает вместе с копиями на нём.

### Файлы упражнений

Они в томе `burmistrov_uploads`, база про них только помнит имена. Копия базы без
копии файлов восстановит тренировки с битыми картинками:

```bash
docker run --rm -v burmistrov_uploads:/data -v /root/backups:/out alpine \
  tar czf /out/uploads-$(date +%F).tgz -C /data .
```

---

## Если что-то пошло не так

**`certbot` не выдаёт сертификат.** Почти всегда домен ещё не ведёт на сервер или
80-й порт закрыт. Проверьте `dig +short ваш-домен` и `ufw status`.

**`web` не стартует, в логах `cannot load certificate`.** Сертификата ещё нет —
запустите `./init-letsencrypt.sh`.

**`app` перезапускается по кругу.** Смотрите `docker compose logs app`.
Частые причины: не совпал пароль базы (меняли `DB_PASSWORD` после первого запуска —
старый пароль уже записан в томе `pgdata`) или упала миграция Liquibase.

**Сборка падает без объяснений.** Скорее всего кончилась память. Проверьте
`free -h`; на 2 ГБ помогает файл подкачки:

```bash
fallocate -l 2G /swapfile && chmod 600 /swapfile && mkswap /swapfile && swapon /swapfile
echo '/swapfile none swap sw 0 0' >> /etc/fstab
```

**Место на диске кончилось.** Старые образы копятся: `docker system prune -a -f`
(тома не трогает).

**Нужно сменить пароль базы.** Пароль уже записан внутри `pgdata`, поэтому одной
правки `.env` мало:

```bash
docker compose exec db psql -U "$DB_USER" -d "$DB_NAME" -c "ALTER USER \"$DB_USER\" WITH PASSWORD 'новый';"
# затем поменять DB_PASSWORD в .env и
docker compose up -d app
```

---

## Про безопасность, коротко

- `.env` — единственное место с паролями. В git его нет и быть не должно.
- Наружу открыты только 80 и 443. База и бэкенд видны лишь изнутри Docker.
- Сертификат продлевается сам; `web` раз в сутки перечитывает конфиг и подхватывает
  новый. Письмо на `CERTBOT_EMAIL` придёт, если продление сломается.
- Обновляйте сам сервер: `apt update && apt upgrade -y` хотя бы раз в месяц.
