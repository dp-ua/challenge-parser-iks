FROM maven:3.8.4-openjdk-17 as builder
WORKDIR /app
# Копируйте файлы для кэширования зависимостей
COPY pom.xml .
# Соберите зависимости отдельно, чтобы воспользоваться кэшем при изменении только файлов проекта
RUN mvn dependency:go-offline
COPY . /app/.
RUN mvn -f /app/pom.xml clean package -Dmaven.test.skip=true

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/ChallengeParser.jar /app/ChallengeParser.jar
EXPOSE 8181
ENTRYPOINT ["java", "-jar", "/app/ChallengeParser.jar", "--spring.profiles.active=prod"]