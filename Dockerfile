FROM eclipse-temurin:25-jdk as builder
WORKDIR /app
COPY build.gradle.kts settings.gradle.kts gradlew ./
COPY gradle ./gradle
RUN ./gradlew dependencies --no-daemon
COPY frontend ./frontend
COPY src ./src
ENV PATH="/app/.gradle/nodejs/node-v20.11.1-linux-x64/bin:${PATH}"
RUN ./gradlew bootJar --no-daemon

FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
