#FROM arm64v8/gradle:jdk24-alpine AS builder
FROM gradle:jdk24-alpine AS builder
ARG PERSONAL_ACCESS_TOKEN
ARG GITHUB_ACTOR
ARG SPRING_PROFILE
ENV PERSONAL_ACCESS_TOKEN=$PERSONAL_ACCESS_TOKEN
ENV GITHUB_ACTOR=$GITHUB_ACTOR
WORKDIR /app
COPY --chown=gradle:gradle . .
RUN gradle clean build -x test --no-daemon

FROM arm64v8/eclipse-temurin:24-jdk-alpine
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
# por for debug
EXPOSE 8093
ENTRYPOINT java -jar app.jar --spring.profiles.active=${SPRING_PROFILE:-dev}