FROM maven:3.9.9-amazoncorretto-21-alpine AS builder
WORKDIR /app

COPY backend/pom.xml ./
COPY backend/src ./src
RUN mvn clean package -DskipTests

FROM maven:3.9.9-amazoncorretto-21-alpine
WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
