#!/usr/bin/env bash
set -e

# ==============================================================================
# 🔑 Avalon Chat - Release Keystore Generator
# ==============================================================================

BOLD="\033[1m"
GREEN="\033[0;32m"
YELLOW="\033[1;33m"
CYAN="\033[0;36m"
RED="\033[0;31m"
RESET="\033[0m"

echo -e "${CYAN}${BOLD}Генерация релизного Keystore для Avalon Chat...${RESET}\n"

KEYSTORE_FILE="avalon-release.jks"
KEY_ALIAS="avalon_chat_release"
PROPERTIES_FILE="keystore.properties"

if ! command -v keytool &> /dev/null; then
    echo -e "${RED}❌ Утилита keytool (часть JDK) не найдена! Установите JDK 17/21.${RESET}"
    exit 1
fi

# Генерация безопасного пароля если не передан
STORE_PASS="${1:-$(openssl rand -base64 24 | tr -dc 'a-zA-Z0-9' | head -c 20)}"
KEY_PASS="$STORE_PASS"

if [ -f "$KEYSTORE_FILE" ]; then
    echo -e "${YELLOW}⚠️ Файл $KEYSTORE_FILE уже существует. Перезаписать? (y/N)${RESET}"
    read -r CONFIRM
    if [[ ! "$CONFIRM" =~ ^[Yy]$ ]]; then
        echo "Отмена."
        exit 0
    fi
    rm -f "$KEYSTORE_FILE"
fi

echo -e "${CYAN}Генерируем RSA 4096-битный ключ (срок 25 лет)...${RESET}"
keytool -genkeypair -v \
    -keystore "$KEYSTORE_FILE" \
    -alias "$KEY_ALIAS" \
    -keyalg RSA \
    -keysize 4096 \
    -validity 10000 \
    -storepass "$STORE_PASS" \
    -keypass "$KEY_PASS" \
    -dname "CN=Avalon Chat Release, OU=Mobile, O=Avalon Community, L=Global, ST=Global, C=WW"

# Создаем локальный keystore.properties для сборки на машине
cat << PROP > "$PROPERTIES_FILE"
storeFile=$KEYSTORE_FILE
storePassword=$STORE_PASS
keyAlias=$KEY_ALIAS
keyPassword=$KEY_PASS
PROP

echo -e "${GREEN}✅ Keystore успешно создан: ${BOLD}$KEYSTORE_FILE${RESET}"
echo -e "${GREEN}✅ Локальный конфиг создан: ${BOLD}$PROPERTIES_FILE${RESET}\n"

# Подготовка base64 для GitHub Secrets
BASE64_KEY=$(base64 -w 0 "$KEYSTORE_FILE" 2>/dev/null || base64 "$KEYSTORE_FILE" | tr -d '\n')

echo -e "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo -e "${YELLOW}${BOLD}Данные для добавления в GitHub Repository Secrets:${RESET}"
echo -e "(Settings -> Secrets and variables -> Actions -> New repository secret)\n"
echo -e "1. ${BOLD}KEYSTORE_BASE64${RESET}:"
echo "$BASE64_KEY"
echo ""
echo -e "2. ${BOLD}KEYSTORE_PASSWORD${RESET}: $STORE_PASS"
echo -e "3. ${BOLD}KEY_ALIAS${RESET}: $KEY_ALIAS"
echo -e "4. ${BOLD}KEY_PASSWORD${RESET}: $KEY_PASS"
echo -e "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n"
