#!/bin/bash

echo "---------------------- Docker build image -------------------"

# Выполняем команду docker build
docker build -t iks-bot:latest -f deploy/Dockerfile .

# Проверяем код возврата
if [ $? -eq 0 ]; then
    echo "Docker build successful!"

else
    echo "Docker build failed!"
    exit 1
fi

echo "---------------------- Docker Finish -------------------"

echo "---------------------- Deploy docker compose file -------------------"

target_directory="/home/dp_ua/bots/docker/iks-bot"

echo "----------------------Deploy Start -------------------"
# Удаляем содержимое директории, но оставляем саму директорию
find "$target_directory" -mindepth 1 -delete

# Проверяем, существует ли директория, и если нет, создаем её
if [ ! -d "$target_directory" ]; then
    mkdir -p "$target_directory"
fi

# Копируем Docker Compose файл в другую директорию
cp deploy/docker-compose.yml "$target_directory/"
cp deploy/02-run.sh "$target_directory/"
# Копируем папку db и её содержимое рекурсивно
cp -r deploy/db "$target_directory/"

ls -la "$target_directory"
echo "---------------------- Finish Deploy docker compose file -------------------"
