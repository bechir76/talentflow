# syntax=docker/dockerfile:1

FROM eclipse-temurin:25-jdk AS build
WORKDIR /workspace
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw -B -q dependency:go-offline
COPY src src
RUN ./mvnw -B -q -DskipTests package && cp target/talentflow-backend-*.jar app.jar

FROM eclipse-temurin:25-jre
WORKDIR /app
RUN groupadd --system talentflow && useradd --system --gid talentflow talentflow
COPY --from=build /workspace/app.jar app.jar
USER talentflow
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
