# ── stage 1: build ───────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# copia o pom primeiro para cachear as dependências
COPY pom.xml .
RUN mvn dependency:go-offline -q

# compila e empacota sem rodar os testes
COPY src ./src
RUN mvn package -DskipTests -q

# ── stage 2: runtime ─────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
