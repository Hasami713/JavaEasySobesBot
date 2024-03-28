# Используем базовый образ с Maven и Java, например, OpenJDK
FROM maven:latest AS build

# Устанавливаем рабочую директорию внутри контейнера
WORKDIR /app

# Копируем файлы проекта Maven в контейнер
COPY pom.xml .
COPY src ./src

# Собираем приложение Maven
RUN mvn clean package -DskipTests

# Второй этап - создаем образ на основе JRE и копируем JAR-файл приложения
FROM openjdk:latest

# Устанавливаем рабочую директорию внутри контейнера
WORKDIR /app

# Копируем собранный JAR файл из предыдущего этапа сборки в этот образ
COPY --from=build /app/target/*.jar /app/app.jar

# Определяем порт, который будет использоваться внутри контейнера
EXPOSE 8080

# Запускаем ваше приложение при старте контейнера
CMD ["java", "-jar", "app.jar"]
