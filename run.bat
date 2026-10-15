@echo off
echo ========================================================
echo Starting Placement Drive Management System...
echo ========================================================

if not exist "target\placement-drive-management-system-1.0.0.jar" (
    echo Building project JAR first...
    call mvn package -DskipTests
)

set PORT=8080
set DATA_DIR=./data
java -jar target\placement-drive-management-system-1.0.0.jar --server.port=%PORT%
