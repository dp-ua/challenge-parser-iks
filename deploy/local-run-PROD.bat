@echo off

echo ---------------------- Docker build image -------------------

REM Выполняем команду docker build
cd ..
docker build -t iks-bot:latest -f deploy/Dockerfile .

REM Проверяем код возврата
if %ERRORLEVEL% equ 0 (
    echo Docker build successful!
) else (
    echo Docker build failed!
    exit /b 1
)

echo ---------------------- Docker Finish -------------------

@echo off
cd deploy
echo =======Check ENV and VARIABLES========
dir

echo Bot name: %IKS_TELEGRAM_BOT_NAME%
echo AdminId: %IKS_TELEGRAM_ADMIN_ID%
echo DB_Admin: %IKS_DB_USERNAME%

REM exit if variables are not set
if "%IKS_TELEGRAM_BOT_NAME%"=="" (
    echo Error: IKS_TELEGRAM_BOT_NAME environment variable is not set
    exit /b 1
)

if "%IKS_TELEGRAM_ADMIN_ID%"=="" (
    echo Error: IKS_TELEGRAM_ADMIN_ID environment variable is not set
    exit /b 1
)

if "%IKS_DB_USERNAME%"=="" (
    echo Error: IKS_DB_USERNAME environment variable is not set
    exit /b 1
)

if "%IKS_DB_PASSWORD%"=="" (
    echo Error: IKS_DB_PASSWORD environment variable is not set
    exit /b 1
)

echo =======Shutting Down docker-compose========
docker-compose -f local-docker-compose.yml down

REM Check if the docker-compose down command was successful
if %ERRORLEVEL% equ 0 (
    echo docker-compose down succeeded
) else (
    echo Error: docker-compose down failed
    exit /b 1
)

echo =======Starting docker-compose========
docker-compose -f local-docker-compose.yml up -d

REM Check if the docker-compose up command was successful
if %ERRORLEVEL% equ 0 (
    echo docker-compose up succeeded
) else (
    echo Error: docker-compose up failed
    exit /b 1
)

echo =======Finish restarting IKS-Bot========
