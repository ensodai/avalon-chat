#!/usr/bin/env bash
set -e

# ==============================================================================
# 📦 Avalon Chat - Server Deployment Bundle Packager
# ==============================================================================

DIST_DIR="build/dist/server"
ARCHIVE_NAME="avalon-chat-server-linux.tar.gz"

echo "📦 Формирование серверного релизного бандла..."

# Убедимся, что jar существует
if [ ! -f "server/build/libs/flow-backend.jar" ]; then
    echo "🔨 Сборка FatJar сервера..."
    ./gradlew :server:shadowJar --no-daemon -x test
fi

rm -rf "$DIST_DIR"
mkdir -p "$DIST_DIR/uploads/system/gifts"

# Копируем исполняемый jar
cp server/build/libs/flow-backend.jar "$DIST_DIR/app.jar"

# Создаем standalone Dockerfile для быстрого развертывания без сборки Gradle на VPS
cat << 'EOF' > "$DIST_DIR/Dockerfile"
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN apk add --no-cache curl tzdata
COPY app.jar app.jar
RUN mkdir -p /app/uploads
VOLUME /app/uploads
EXPOSE 8080
ENTRYPOINT ["java", "-XX:+UseG1GC", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
EOF

# Создаем standalone docker-compose.yml
cat << 'EOF' > "$DIST_DIR/docker-compose.yml"
services:
  # ── PostgreSQL Database (17+) ──
  db:
    image: postgres:17-alpine
    container_name: avalon-postgres
    restart: always
    environment:
      POSTGRES_DB: ${DB_NAME:-avalon_chat}
      POSTGRES_USER: ${DB_USER:-postgres}
      POSTGRES_PASSWORD: ${DB_PASSWORD:-postgres}
    ports:
      - "${DB_PORT:-5432}:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U ${DB_USER:-postgres} -d ${DB_NAME:-avalon_chat}"]
      interval: 5s
      timeout: 5s
      retries: 5

  # ── Redis Cache ──
  redis:
    image: redis:7-alpine
    container_name: avalon-redis
    restart: always
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data
    command: redis-server --appendonly yes

  # ── Avalon Chat Backend ──
  server:
    build: .
    container_name: avalon-server
    restart: always
    depends_on:
      db:
        condition: service_healthy
      redis:
        condition: service_started
    ports:
      - "${PORT:-8080}:8080"
    env_file:
      - path: .env
        required: false
    environment:
      - DB_HOST=db
      - DB_PORT=5432
      - REDIS_URL=redis://redis:6379
    volumes:
      - ./uploads:/app/uploads

volumes:
  postgres_data:
  redis_data:
EOF

# Копируем конфигурации и скрипты быстрого запуска
cp quickstart.sh "$DIST_DIR/"
cp .env.example "$DIST_DIR/"
cp README.md "$DIST_DIR/"

# Копируем стартовые системные ассеты подарков
if [ -d "server/uploads/system/gifts" ]; then
    cp -r server/uploads/system/gifts/* "$DIST_DIR/uploads/system/gifts/"
fi

# Упаковываем в архив tar.gz
mkdir -p build/release
tar -czf "build/release/$ARCHIVE_NAME" -C "$DIST_DIR" .

echo "✅ Бандл сервера успешно создан: build/release/$ARCHIVE_NAME"
ls -lh "build/release/$ARCHIVE_NAME"
