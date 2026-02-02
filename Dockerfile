# ---- BUILD STAGE ----
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Copier Maven Wrapper + pom.xml d'abord (cache)
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Rendre mvnw exécutable (Linux)
RUN chmod +x mvnw

# Télécharger les dépendances (cache)
RUN ./mvnw -q -DskipTests dependency:go-offline

# Copier le code et builder
COPY src src
RUN ./mvnw -q -DskipTests clean package

# ---- RUN STAGE ----
FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]