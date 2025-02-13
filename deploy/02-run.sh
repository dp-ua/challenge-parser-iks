#!/bin/bash
echo "=======Check ENV and VARIABLES========"
ls -la

echo "Bot name: ${IKS_TELEGRAM_BOT_NAME}"
echo "AdminId: ${IKS_TELEGRAM_ADMIN_ID}"
echo "DB_Admin: ${IKS_DB_USERNAME}"
# exit if variables are not set
if [ -z "$IKS_TELEGRAM_BOT_NAME" ] || [ -z "$IKS_TELEGRAM_ADMIN_ID" ] || [ -z "$IKS_DB_USERNAME" ] || [ -z "$IKS_DB_PASSWORD" ]; then
    echo "Error: One or more required environment variables are not set"
    exit 1
fi

echo "=======Shutting Down docker-compose========"
docker-compose down

# Check if the docker-compose down command was successful
if [ $? -eq 0 ]; then
    echo "docker-compose down succeeded"
else
    echo "Error: docker-compose down failed"
    exit 1
fi

echo "=======Starting docker-compose========"
docker-compose up -d

echo "Проверяем, существует ли SSL-сертификат..."
if [ -d "/etc/letsencrypt/live/bots.dns-cloud.net" ]; then
    echo "SSL-сертификат уже существует. Пропускаем запрос Certbot."
else
    echo "Запрашиваем новый SSL-сертификат..."
    docker-compose run --rm certbot certonly --webroot -w /var/lib/letsencrypt -d bots.dns-cloud.net --email pavel.reshetilo@gmail.com --agree-tos --no-eff-email --rsa-key-size 4096
fi

# Check if the docker-compose up command was successful
if [ $? -eq 0 ]; then
    echo "docker-compose up succeeded"
else
    echo "Error: docker-compose up failed"
    exit 1
fi

echo "=======Finish restarting IKS-Bot========"
