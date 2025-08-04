# 🏋️‍♂️ Fitness Trainer Booking App — Backend

Full-featured backend for a personal fitness trainer's studio booking system.

---

## 🚀 Features

- ✅ User Registration, Login, JWT Auth
- ✅ Role-based Access Control (`USER`, `USER_PRO`, `ADMIN`, `DEV`)
- ✅ Manage Studios: CRUD + Analytics
- ✅ Time Slot Management: conflict detection, buffer between studios
- ✅ Booking System: create/cancel/update, upcoming/history view
- ✅ Stripe payments: promo-codes, success/failed webhooks
- ✅ Admin payment search & CSV-ready pagination
- ✅ One-click refunds for admins
- ✅ Media upload/download via S3 presigned URLs
- ✅ One trial session per user per year
- ✅ Swagger UI for API documentation
- ✅ PostgreSQL support with JPA/Hibernate
- ✅ E-mail verification with “resend link” option
- ✅ Login rate-limiting (Resilience4j)
- ✅ Password-reset flow (request → token check → new password)
- ✅ Scheduled cleanup of stale unconfirmed accounts (7 days)
- ✅ Transactional e-mail delivery via SendGrid API

---

## 📷 API Documentation

Swagger UI available at:  
[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

---

## 🧰 Tech Stack

| Layer       | Technology                          |
|-------------|-------------------------------------|
| Language    | Java 17+                            |
| Framework   | Spring Boot                         |
| Security    | Spring Security, JWT                |
| DB          | PostgreSQL                          |
| Docs        | Swagger + OpenAPI                   |
| DTO Mapping | MapStruct                           |
| Build Tool  | Maven                               |
| Messaging   | SendGrid Cloud E-mail               |
| Resilience  | Resilience4j RateLimiter            |
| Payments    | Stripe PaymentIntent API            |
| Media       | AWS S3/MinIO presigned URLs         |
| Scheduling  | Spring Task Scheduling (@Scheduled) |
---
## 🚀 Getting Started

### 1  Clone & set up environment variables

```bash
git clone https://github.com/<your-org>/<repo>.git
cd <repo>
cp .env.example .env          # create a *private* file with real secrets
# └─ edit .env and set:
#    POSTGRES_PASSWORD=<strong-password>
#    JWT_SECRET=$(openssl rand -base64 32)
```
### 2 Run with Docker Compose
```
docker compose \
  -f docker-compose.yml \
  -f docker-compose.dev.yml \
  up --build -d
  ```
First run (or after code changes) needs --build to re-create the app image.

Swagger UI → http://localhost:8080/swagger-ui.html

PostgreSQL → localhost:5432 with credentials from .env.
### 3 Handy commands
Command	Purpose
```
docker compose stop	Graceful stop (containers stay)
docker compose start	Start again
docker compose down	Stop and remove containers
docker compose logs -f app	Tail application logs
docker compose ps	List running services
```
### 4 Run locally without Docker (optional)
```bash
./mvnw spring-boot:run               # picks variables from .env
```
Make sure PostgreSQL is running on localhost:5432 with the same credentials.

### 🔒 Security notes
Real secrets live only in .env; .env.example documents required keys.

Spring Boot loads .env via
spring.config.import=optional:file:.env[.properties].

Docker Compose also auto-loads .env, so local and container runs stay in sync.

The backend sends verification / reset links via SendGrid.
Set SENDGRID_API_KEY, MAIL_FROM and APP_BASE_URL in `.env` even for local runs
(otherwise links will still be printed to console for dev profiles).