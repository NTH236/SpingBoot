#!/usr/bin/env bash
# Script khởi chạy đồng thời 2 dịch vụ trong kiến trúc SOA
set -e

DIR="$( cd "$( dirname "${bash_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
cd "$DIR"

echo "=========================================================="
echo " KHỞI CHẠY HỆ THỐNG SOA SPRING BOOT"
echo " 1. auth-service   : http://localhost:8080 (Swagger: /swagger-ui.html)"
echo " 2. product-service: http://localhost:8081 (Swagger: /swagger-ui.html)"
echo "=========================================================="

export JAVA_HOME=/usr/lib/jvm/java-21-openjdk

# Khởi chạy Auth Service (Port 8080)
echo "Đang khởi động Auth Service trên Port 8080..."
./mvnw spring-boot:run -pl auth-service &
AUTH_PID=$!

# Khởi chạy Product Service (Port 8081)
echo "Đang khởi động Product Service trên Port 8081..."
./mvnw spring-boot:run -pl product-service &
PRODUCT_PID=$!

cleanup() {
    echo ""
    echo "Đang dừng tất cả các dịch vụ..."
    kill "$AUTH_PID" "$PRODUCT_PID" 2>/dev/null || true
    echo "Đã dừng."
}

trap cleanup INT TERM

echo ""
echo "Các dịch vụ đang chạy (PIDs: Auth=$AUTH_PID, Product=$PRODUCT_PID)."
echo "Nhấn Ctrl+C để dừng cả 2 dịch vụ."
wait
