#!/bin/bash
set -euo pipefail

BACKEND_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../../../.." && pwd)"
FRONTEND_DIR="/Users/leonid/UIprojects/burmistrov-ui"
RUN_DIR="$BACKEND_DIR/.run"
mkdir -p "$RUN_DIR"

BACKEND_PORT=8080
FRONTEND_PORT=5173
BACKEND_LOG="$RUN_DIR/backend.log"
FRONTEND_LOG="$RUN_DIR/frontend.log"
MAVEN_SETTINGS="$RUN_DIR/maven-settings-local.xml"

# Только слушающие сокеты: без -sTCP:LISTEN сюда попадают и клиентские
# подключения к этому порту — например вкладка браузера, открытая на сайте.
listening_pids() {
  lsof -ti tcp:"$1" -sTCP:LISTEN 2>/dev/null || true
}

# Адрес Mac в локальной сети — по нему сайт открывается с телефона.
lan_ip() {
  local iface ip
  for iface in $(route -n get default 2>/dev/null | awk '/interface:/{print $2}') en0 en1; do
    ip="$(ipconfig getifaddr "$iface" 2>/dev/null || true)"
    if [ -n "$ip" ]; then
      echo "$ip"
      return
    fi
  done
}

if [ -n "$(listening_pids "$BACKEND_PORT")" ]; then
  echo "Backend already running on port $BACKEND_PORT"
else
  # ~/.m2/settings.xml is pinned to corporate Nexus mirrors that don't resolve
  # outside the work VPN; a blank settings file falls back to real Maven Central.
  cat > "$MAVEN_SETTINGS" <<'SETTINGS'
<settings>
</settings>
SETTINGS
  echo "Starting backend (log: $BACKEND_LOG)..."
  (cd "$BACKEND_DIR" && nohup ./mvnw -s "$MAVEN_SETTINGS" spring-boot:run > "$BACKEND_LOG" 2>&1 &)
fi

if [ -n "$(listening_pids "$FRONTEND_PORT")" ]; then
  echo "Frontend already running on port $FRONTEND_PORT"
else
  echo "Starting frontend (log: $FRONTEND_LOG)..."
  # --host: сервер слушает не только localhost, но и локальную сеть, чтобы сайт
  # можно было открыть с телефона. Бэкенд при этом остаётся на localhost —
  # телефон ходит только на 5173, а /api Vite проксирует уже со стороны Mac.
  (cd "$FRONTEND_DIR" && nohup npm run dev -- --host > "$FRONTEND_LOG" 2>&1 &)
fi

echo
echo "Backend:  http://localhost:$BACKEND_PORT"
echo "Frontend: http://localhost:$FRONTEND_PORT"

IP="$(lan_ip)"
if [ -n "$IP" ]; then
  echo "С телефона (та же Wi-Fi сеть): http://$IP:$FRONTEND_PORT"
else
  echo "С телефона: адрес в сети не определился — посмотрите строку Network в $FRONTEND_LOG"
fi
