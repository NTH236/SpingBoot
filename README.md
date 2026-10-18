# SOA Architecture System: Auth Service & Product Service

Dự án này đã được cấu trúc lại hoàn toàn theo **Kiến trúc Hướng dịch vụ (Service-Oriented Architecture - SOA)** với 2 microservice độc lập chạy trên 2 cổng mạng riêng biệt và giao tiếp liên dịch vụ qua giao thức HTTP REST.

---

## 1. Tổng quan Kiến trúc Dịch vụ (Service Topology)

```
[ Client / Postman ]
       │
       ├─ (1) POST http://localhost:8080/auth/login
       │      └──► [ auth-service (Port 8080) ]
       │           ├── CSDL riêng: ./data/auth_db
       │           └── Trả về JWT Token
       │
       └─ (2) GET / POST http://localhost:8081/products
              Header: Authorization: Bearer <token>
              └──► [ product-service (Port 8081) ]
                   ├── CSDL riêng: ./data/product_db
                   │
                   └── (3) [Inter-Service HTTP REST Call]
                           GET http://localhost:8080/auth/validate
                           Header: Bearer <token>
                           └──► [ auth-service ] xác thực token & trả về user
```

---

## 2. Thông tin Dịch vụ & Cổng mạng

| Dịch vụ | Cổng mạng (Port) | Cơ sở dữ liệu | Trách nhiệm chính | Swagger UI |
| :--- | :---: | :---: | :--- | :--- |
| **`auth-service`** | **`8080`** | `auth_db` | Quản lý người dùng, cấp phát token (`/auth/login`), API xác thực liên dịch vụ (`/auth/validate`) | [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) |
| **`product-service`** | **`8081`** | `product_db` | Quản lý sản phẩm (`/products/**`), gọi sang Auth Service qua `AuthServiceClient` để bảo vệ tài nguyên | [http://localhost:8081/swagger-ui.html](http://localhost:8081/swagger-ui.html) |

---

## 3. Cấu trúc Thư mục Dự án (Maven Multi-Module)

```
my-first-project/
├── pom.xml                               <-- Root Aggregator POM
├── run-all.sh                            <-- Script khởi chạy đồng thời 2 service
├── test-soa.sh                           <-- Script kiểm thử tự động luồng liên dịch vụ
│
├── auth-service/                         <-- Dịch vụ Auth (Port 8080)
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/auth/
│       │   ├── AuthServiceApplication.java
│       │   ├── controller/AuthController.java
│       │   ├── entity/User.java
│       │   ├── repository/UserRepository.java
│       │   ├── security/JwtUtil.java
│       │   └── config/OpenApiConfig.java
│       └── main/resources/application.properties
│
└── product-service/                      <-- Dịch vụ Product (Port 8081)
    ├── pom.xml
    └── src/
        ├── main/java/com/product/
        │   ├── ProductServiceApplication.java
        │   ├── client/AuthServiceClient.java   <-- Gọi HTTP REST sang Auth Service (8080)
        │   ├── security/AuthInterceptor.java    <-- Chặn và gọi client xác thực
        │   ├── controller/ProductController.java
        │   ├── entity/Product.java
        │   ├── repository/ProductRepository.java
        │   └── config/WebConfig.java
        └── main/resources/application.properties
```

---

## 4. Hướng dẫn Khởi chạy & Kiểm thử

### Cách 1: Khởi chạy cả 2 dịch vụ bằng một lệnh
```bash
./run-all.sh
```

### Cách 2: Khởi chạy từng dịch vụ bằng lệnh Maven
* **Auth Service (Port 8080):**
  ```bash
  JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./mvnw spring-boot:run -pl auth-service
  ```
* **Product Service (Port 8081):**
  ```bash
  JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./mvnw spring-boot:run -pl product-service
  ```

### Cách 3: Kiểm thử toàn bộ hệ thống liên dịch vụ
Khi cả 2 dịch vụ đang chạy, thực hiện:
```bash
./test-soa.sh
```

### Chạy Unit & Integration Test
```bash
JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./mvnw clean test
```
