# ============================
# 1) Dependencies Stage
# ============================
FROM gradle:8.12.1-jdk21 AS dependencies
WORKDIR /app

COPY build.gradle settings.gradle gradle.properties* gradlew gradlew.bat ./
COPY gradle/ ./gradle/

RUN --mount=type=cache,target=/root/.gradle/caches \
    --mount=type=cache,target=/root/.gradle/wrapper \
    chmod +x ./gradlew && \
    (./gradlew dependencies --no-daemon || gradle dependencies --no-daemon)

# ============================
# 2) Build Stage
# ============================
FROM dependencies AS build

COPY src/ ./src/

RUN --mount=type=cache,target=/root/.gradle/caches \
    --mount=type=cache,target=/root/.gradle/wrapper \
    ./gradlew clean build -x test --no-daemon

# ============================
# 3) Runtime Stage
# ============================
ARG DSP_BACKEND_PROJECT_NAME=dsp
ARG DSP_BACKEND_VERSION=0.0.1-SNAPSHOT

FROM eclipse-temurin:21-jre-jammy AS runtime
WORKDIR /app

ARG DSP_BACKEND_PROJECT_NAME=dsp
ARG DSP_BACKEND_VERSION=0.0.1-SNAPSHOT

RUN apt-get update && apt-get upgrade -y \
    && rm -rf /var/lib/apt/lists/* \
    && apt-get clean

COPY --from=build /app/build/libs/${DSP_BACKEND_PROJECT_NAME}-${DSP_BACKEND_VERSION}.jar /app/app.jar

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
