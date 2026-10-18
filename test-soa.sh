#!/usr/bin/env bash
set -e

echo "=========================================================="
echo " KIỂM TRA HỆ THỐNG SOA: AUTH-SERVICE & PRODUCT-SERVICE"
echo "=========================================================="

AUTH_URL="http://localhost:8080"
PRODUCT_URL="http://localhost:8081"

echo ""
echo "1. Thử gọi API Product Service (Port 8081) KHÔNG CÓ TOKEN..."
HTTP_STATUS=$(curl -s -o /tmp/resp_no_token.json -w "%{http_code}" "$PRODUCT_URL/products" || true)
echo "HTTP Status: $HTTP_STATUS"
cat /tmp/resp_no_token.json
echo ""

echo ""
echo "2. Đăng nhập vào Auth Service (Port 8080) để nhận JWT Token..."
LOGIN_RESP=$(curl -s -X POST "$AUTH_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"userName": "hoang", "password": "123456"}')

echo "Login Response: $LOGIN_RESP"
TOKEN="$LOGIN_RESP"

if [[ -z "$TOKEN" || "$TOKEN" == *"error"* ]]; then
  echo "Đăng nhập thất bại!"
  exit 1
fi

echo ""
echo "3. Kiểm tra trực tiếp endpoint xác thực liên dịch vụ (GET $AUTH_URL/auth/validate)..."
VALIDATE_RESP=$(curl -s -X GET "$AUTH_URL/auth/validate" \
  -H "Authorization: Bearer $TOKEN")
echo "Validate Response từ Auth Service: $VALIDATE_RESP"

echo ""
echo "4. Gọi API Product Service (Port 8081) kèm Bearer Token vừa nhận..."
echo "-> Product Service trên Port 8081 sẽ tự động gọi HTTP REST sang Auth Service (Port 8080) để xác thực:"
PRODUCTS_RESP=$(curl -s -X GET "$PRODUCT_URL/products" \
  -H "Authorization: Bearer $TOKEN")
echo "Danh sách sản phẩm từ Product Service:"
echo "$PRODUCTS_RESP"

echo ""
echo "5. Thêm mới một sản phẩm vào Product Service (POST $PRODUCT_URL/products)..."
CREATE_RESP=$(curl -s -X POST "$PRODUCT_URL/products" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name": "Bàn di chuột SteelSeries", "description": "Size XL chống nước", "price": 450000.00, "quantity": 30}')
echo "Kết quả thêm sản phẩm:"
echo "$CREATE_RESP"

echo ""
echo "=========================================================="
echo " ĐÃ HOÀN TẤT THỬ NGHIỆM LIÊN DỊCH VỤ SOA THÀNH CÔNG!"
echo "=========================================================="
