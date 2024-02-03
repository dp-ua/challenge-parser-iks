#!/bin/bash

echo "---------------------- Docker build image -------------------"

# Выполняем команду docker build
docker build -t challenge-parser:latest -f deploy/Dockerfile .
# TODO переместить докер файлы в папку deploy
# TODO использовать в дженкинсе скриптовые файлы

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
rm -rf "$target_directory"
if [ ! -d "$target_directory" ]; then
	mkdir -p "$target_directory"
fi

# Копируем Docker Compose файл в другую директорию
cp deploy/docker-compose.yml "$target_directory/"
ls -la "$target_directory"

echo "---------------------- Finish Deploy docker compose file -------------------"