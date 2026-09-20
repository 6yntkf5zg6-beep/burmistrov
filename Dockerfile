# Сборка бэкенда «СТАН».
#
# Две стадии: в первой Maven собирает jar, во второй остаётся только JRE и сам jar —
# в образ не попадают ни исходники, ни кэш Maven, ни сам Maven. Образ выходит около
# 250 МБ вместо полутора гигабайт.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

# Зависимости отдельным слоем: пока pom.xml не менялся, Docker переиспользует этот слой
# и повторная сборка не ходит в интернет за теми же библиотеками.
COPY pom.xml .
RUN mvn -B -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app

# Приложение не должно бегать под root: если кто-то дотянется до выполнения кода,
# у него не будет прав на сам контейнер.
RUN useradd --system --uid 1001 --home /app app

COPY --from=build /build/target/*.jar /app/app.jar

# Каталог для загруженных фото и видео упражнений. В compose сюда примонтирован том,
# чтобы файлы пережили пересборку образа.
RUN mkdir -p /app/uploads && chown -R app:app /app

USER app
EXPOSE 8080

# MaxRAMPercentage вместо -Xmx: JVM сама посчитает кучу от лимита контейнера,
# и на машине с 1 ГБ и на машине с 8 ГБ образ работает без правок.
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/app.jar"]
