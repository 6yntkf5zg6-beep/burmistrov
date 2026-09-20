#!/bin/bash

BACKEND_PORT=8080
FRONTEND_PORT=5173

stop_port() {
  local port="$1"
  local pids
  # -sTCP:LISTEN обязателен: иначе lsof вернёт и процессы, которые всего лишь
  # подключены к этому порту, и скрипт прибьёт браузер с открытой вкладкой.
  pids="$(lsof -ti tcp:"$port" -sTCP:LISTEN 2>/dev/null || true)"
  if [ -z "$pids" ]; then
    echo "Nothing running on port $port"
    return
  fi
  echo "Stopping process(es) on port $port: $pids"
  kill $pids 2>/dev/null || true
  sleep 2
  pids="$(lsof -ti tcp:"$port" -sTCP:LISTEN 2>/dev/null || true)"
  if [ -n "$pids" ]; then
    echo "Still alive, force killing: $pids"
    kill -9 $pids 2>/dev/null || true
  fi
}

stop_port "$BACKEND_PORT"
stop_port "$FRONTEND_PORT"
