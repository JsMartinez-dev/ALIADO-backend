FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml ./
RUN mvn -B -ntp dependency:go-offline
COPY src ./src
RUN mvn -B -ntp -Dmaven.test.skip=true package

FROM eclipse-temurin:21-jre-jammy AS runtime
WORKDIR /app
RUN groupadd --system aliado && useradd --system --gid aliado --home-dir /app aliado \
    && mkdir /app/storage && chown aliado:aliado /app/storage
COPY --from=build --chown=aliado:aliado /build/target/ALIADO-0.0.1-SNAPSHOT.war /app/app.war
USER aliado
ENV SPRING_PROFILES_ACTIVE=prod PORT=8080
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.war"]
