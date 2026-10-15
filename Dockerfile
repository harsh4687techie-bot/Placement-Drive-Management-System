# ==============================================================
# Stage 1: Build stage using Maven and Eclipse Temurin JDK 17
# ==============================================================
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /build

# Cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build production executable JAR
COPY src ./src
RUN mvn clean package -DskipTests

# ==============================================================
# Stage 2: Runtime stage using lightweight Eclipse Temurin JRE 17
# ==============================================================
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Configurable environment variables for cloud hosts (Render, Docker, etc.)
ENV PORT=8080
ENV DATA_DIR=/data
ENV BACKUP_DIR=/backups
ENV REPORT_DIR=/reports
ENV JAVA_OPTS="-Xms256m -Xmx512m"

# Create persistence directories with proper permissions
RUN mkdir -p /data /backups /reports && chmod 777 /data /backups /reports

# Declare persistent storage volumes
VOLUME ["/data", "/backups", "/reports"]

# Copy compiled Spring Boot JAR from builder
COPY --from=builder /build/target/placement-drive-management-system-1.0.0.jar app.jar

# Expose default HTTP port
EXPOSE 8080

# Health check using the /api/health endpoint
HEALTHCHECK --interval=30s --timeout=5s --start-period=20s --retries=3 \
  CMD curl -f http://localhost:${PORT}/api/health || exit 1

# Launch Spring Boot with dynamic PORT and DATA_DIR binding
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -Dserver.port=${PORT} -Dapp.storage.data-dir=${DATA_DIR} -Dapp.storage.backup-dir=${BACKUP_DIR} -Dapp.storage.report-dir=${REPORT_DIR} -jar app.jar"]
