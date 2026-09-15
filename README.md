# Webliix ERP & CRM Backend (`webliix-backend`)

Official Enterprise REST API Service for **Webliix ERP & CRM SaaS Platform**.

## 🚀 Overview

Webliix Backend is a high-performance Java Spring Boot 3 enterprise application powering the Webliix ecosystem. Built with **Spring Data JPA**, **Hibernate**, **PostgreSQL**, **Flyway**, **Spring Security (JWT)**, and **Cloudinary CDN Integration**, it provides scalable REST endpoints for authentication, customer & lead management, CRM controls, public article publishing, notification dispatching, developer API key management, and security audit logs.

## 🛠️ Tech Stack

- **Java**: Java 17 LTS
- **Framework**: Spring Boot 3.x (Spring Web, Spring Security, Spring Actuator, Spring Data JPA)
- **Database**: PostgreSQL 15+ (with Flyway database migrations)
- **Security**: JWT Authentication, BCrypt Password Hashing, Role/Permission RBAC
- **Media CDN**: Cloudinary Media API + Local Storage Provider Fallback (up to 50MB uploads)
- **Build Tool**: Apache Maven (`mvn compile`, `mvn package`)

## ✨ Key Modules & Capabilities

1. **Authentication & Profile Control**:
   - 7-Day Persistent JWT Session duration with auto-bootstrap `/api/v1/auth/me`.
   - 5-Tab Profile controls: personal details, password strength updates, TOTP 2FA configuration, and notification preferences.
   - Developer API Key token generator (`/api/v1/api-tokens`) with custom permission scopes (`Read-Only` / `Full Admin`).

2. **Notifications Suite**:
   - In-App & Email notification dispatch system (`/api/v1/notifications`).
   - Initial notification seeding, read/unread status updates, and clear-all controls.

3. **Public & Admin Blog Engine**:
   - Full CRUD endpoints (`/api/v1/blogs`, `/api/v1/public/blogs`).
   - Dual numeric ID and string slug resolution (e.g. `getPostBySlug("1574")`).
   - Unauthenticated public reader liking and recursive threaded commenting (`buildCommentTree`).
   - Google AdSense per-article monetization metadata mapping (`enableAds`, `adSenseClientId`, ad slots).

4. **CRM Lead & Customer Engine**:
   - Lead capture, status tracking, conversion workflows, and customer account management.

## ⚙️ Prerequisites & Environment Setup

### Database Configuration

Ensure PostgreSQL is running locally or remotely on port `5432`:
- **Database Name**: `webliix_db`
- **Port**: `5432`

### Application Properties (`application.properties`)

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/webliix_db
spring.datasource.username=postgres
spring.datasource.password=12345
server.port=8082
jwt.secret=webliix-super-secret-key-change-this-in-production-2026
jwt.expiration=604800000

# Multipart File Upload Limits (50MB Max File Size)
spring.servlet.multipart.enabled=true
spring.servlet.multipart.max-file-size=50MB
spring.servlet.multipart.max-request-size=100MB
```

## 🛠️ Build & Run Instructions

```bash
# Compile project
mvn compile -DskipTests

# Package application JAR
mvn package -DskipTests

# Run Spring Boot application
java -jar target/webliix-0.0.1-SNAPSHOT.jar
```

The REST API server will start on `http://localhost:8082`.

## 🔗 Repository Information

- **Repository**: [https://github.com/webliix/webliix-backend.git](https://github.com/webliix/webliix-backend.git)
- **Branch**: `main`

## 📄 License

© Webliix Hub Platform. All rights reserved.
