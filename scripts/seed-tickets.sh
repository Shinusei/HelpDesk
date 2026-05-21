#!/usr/bin/env bash
set -euo pipefail

# Seed test tickets into running HelpDesk application
# Usage: ./scripts/seed-tickets.sh [server_url] [username] [password]

SERVER_URL=${1:-http://localhost:8080}
USERNAME=${2:-user}
PASSWORD=${3:-user}
COOKIE_JAR=".seed_cookies.txt"

echo "Server: $SERVER_URL"

echo "Logging in as $USERNAME..."
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" -c "$COOKIE_JAR" -d "username=${USERNAME}&password=${PASSWORD}" "$SERVER_URL/login")
if [ "$HTTP_CODE" != "200" ]; then
  echo "Login failed (HTTP $HTTP_CODE). Ensure the app is running and credentials are correct." >&2
  exit 1
fi

echo "Login successful, creating tickets..."

create_ticket() {
  local title="$1"
  local description="$2"
  local importance="$3"
  local urgency="$4"
  local impact="$5"
  local category="$6"

  payload=$(cat <<JSON
{
  "title": "${title}",
  "description": "${description}",
  "importance": "${importance}",
  "urgency": "${urgency}",
  "impact": "${impact}",
  "category": "${category}"
}
JSON
)

  resp=$(curl -s -o /dev/null -w "%{http_code}" -b "$COOKIE_JAR" -c "$COOKIE_JAR" -H "Content-Type: application/json" -d "$payload" "$SERVER_URL/api/tickets")
  if [ "$resp" = "200" ] || [ "$resp" = "201" ]; then
    echo "Created: $title"
  else
    echo "Failed ($resp): $title" >&2
  fi
}

# Tickets (title, description, importance, urgency, impact, category)
create_ticket "Принтер в офисе №3 не печатает" "Бумага не подаётся, на панели мигает ошибка. Нужна проверка и замена ролика, если потребуется." "HIGH" "HIGH" "DEPARTMENT" "HARDWARE"
create_ticket "Не удаётся войти в систему" "При вводе пароля возвращается ошибка 401, пользователь не может авторизоваться." "HIGH" "CRITICAL" "USER" "SOFTWARE"
create_ticket "Запрос доступа к БД аналитики" "Нужен readonly доступ к базе аналитики для формирования отчётов." "MEDIUM" "MEDIUM" "DEPARTMENT" "ACCESS"
create_ticket "Замена батареи в ноутбуке" "Ноутбук держит заряд меньше часа, требуется замена батареи." "MEDIUM" "LOW" "USER" "HARDWARE"
create_ticket "Вложения не открываются в заявках" "При попытке открыть вложение возникает ошибка 500 на сервере." "HIGH" "HIGH" "COMPANY" "SOFTWARE"
create_ticket "Обновить Chrome на рабочих станциях" "Требуется обновить браузер до версии 120+ на рабочих станциях отдела." "LOW" "LOW" "DEPARTMENT" "SOFTWARE"
create_ticket "Настройка VPN для удалённого доступа" "Удалённому сотруднику нужен доступ через VPN, требуется настройка и проверка подключения." "HIGH" "HIGH" "COMPANY" "NETWORK"
create_ticket "Подключить новый монитор и перенести рабочий стол" "Требуется подключить внешний монитор, адаптировать разрешение и перенести ярлыки." "LOW" "LOW" "USER" "HARDWARE"

# Cleanup
rm -f "$COOKIE_JAR"

echo "Seeding complete."
