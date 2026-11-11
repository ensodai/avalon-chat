#!/usr/bin/env bash
set -e

# ==============================================================================
# 🚀 Avalon Chat - Quickstart Self-Hosted Deployment Script
# ==============================================================================

BOLD="\033[1m"
GREEN="\033[0;32m"
YELLOW="\033[1;33m"
CYAN="\033[0;36m"
RED="\033[0;31m"
RESET="\033[0m"

echo -e "${CYAN}${BOLD}"
echo "  ___             _             ___ _           _   "
echo " / _ \           | |           /  _| |         | |  "
echo "/ /_\ \_   __ _  | | ___  _ __ | / | |__   __ _| |_ "
echo "|  _  \ \ / / _\ | |/ _ \| '_ \| | | '_ \ / _\ | __|"
echo "| | | |\ V / (_| | | (_) | | | | \_| | | | (_| | |_ "
echo "\_| |_/ \_/ \__,_|_|\___/|_| |_|\___|_| |_|\__,_|\__|"
echo -e "${RESET}"
echo -e "${BOLD}Запуск Self-Hosted инстанса Avalon Chat...${RESET}\n"

# 1. Проверка Docker
if ! command -v docker &> /dev/null; then
    echo -e "${RED}❌ Docker не установлен! Установите Docker и повторите запуск.${RESET}"
    exit 1
fi

COMPOSE_CMD=""
if docker compose version &> /dev/null; then
    COMPOSE_CMD="docker compose"
elif command -v docker-compose &> /dev/null; then
    COMPOSE_CMD="docker-compose"
else
    echo -e "${RED}❌ Docker Compose не найден! Установите docker compose plugin.${RESET}"
    exit 1
fi

# 2. Подготовка .env файла
if [ ! -f .env ]; then
    echo -e "${YELLOW}📝 Файл .env не найден. Создаем из .env.example...${RESET}"
    if [ -f .env.example ]; then
        cp .env.example .env
    else
        echo -e "${RED}❌ .env.example не найден!${RESET}"
        exit 1
    fi

    # Генерация JWT_SECRET
    RANDOM_JWT=$(openssl rand -hex 32 2>/dev/null || cat /proc/sys/kernel/random/uuid | tr -d '-')
    # Генерация надежного DB_PASSWORD
    RANDOM_DB_PASS=$(openssl rand -hex 16 2>/dev/null || cat /proc/sys/kernel/random/uuid | cut -c1-16)

    # Замена в .env
    sed -i "s|JWT_SECRET=.*|JWT_SECRET=${RANDOM_JWT}|g" .env
    sed -i "s|DB_PASSWORD=.*|DB_PASSWORD=${RANDOM_DB_PASS}|g" .env

    echo -e "${GREEN}✅ Сгенерированы надежные JWT_SECRET и пароль БД в .env${RESET}"
fi

# 3. Проверка ключа Gemini
if grep -q "SPECIAL_GEMINI_KEY=your_gemini_api_key_here" .env || grep -q "SPECIAL_GEMINI_KEY=$" .env; then
    echo -e "${YELLOW}⚠️  Внимание: В файле .env не указан SPECIAL_GEMINI_KEY!${RESET}"
    echo -e "${YELLOW}   Не забудьте вписать ваш Google Gemini API ключ для диалогов с ИИ.${RESET}\n"
fi

# 4. Создание необходимых директорий
mkdir -p server/uploads/system/gifts uploads/system/gifts 2>/dev/null || true

# 5. Сборка и запуск контейнеров
echo -e "${CYAN}🐳 Запуск контейнеров через ${COMPOSE_CMD}...${RESET}"
$COMPOSE_CMD up -d --build

echo -e "\n${GREEN}${BOLD}🎉 Сервер Avalon Chat успешно запущен!${RESET}"
echo -e "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo -e "🌐 ${BOLD}API & Сервер:${RESET}     http://localhost:8080"
echo -e "🗄️  ${BOLD}PostgreSQL:${RESET}       localhost:5432 (БД: avalon_chat)"
echo -e "⚡ ${BOLD}Redis Cache:${RESET}      localhost:6379"
echo -e "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo -e "🤖 ${BOLD}Telegram-бот администрирования:${RESET}"
echo -e "   Укажите TELEGRAM_BOT_TOKEN и TELEGRAM_ADMIN_ID в .env,"
echo -e "   затем выполните: ${CYAN}${COMPOSE_CMD} restart server${RESET}"
echo -e "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n"
