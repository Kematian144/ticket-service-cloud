# Etapa 1: Build con Maven y OpenJDK 21
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /workspace

# Copiar archivos de Maven Wrapper y pom.xml
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

RUN chmod +x ./mvnw && ./mvnw dependency:go-offline -B

# Copiar codigo fuente y compilar
COPY src src
RUN ./mvnw clean package -DskipTests

# Etapa 2: Imagen final de ejecucion
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Crear directorio /app/efs donde se monta el volumen EFS
RUN mkdir -p /app/efs && chmod 777 /app/efs

COPY --from=builder /workspace/target/ticket-service-1.0.0.jar app.jar

ENV PORT=8080 \
    EFS_PATH=/app/efs \
    AWS_REGION=us-east-1

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
