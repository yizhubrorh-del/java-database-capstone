# Smart Clinic Management System

A web application for running a medical clinic: patients book appointments with doctors, doctors manage
their daily schedule and write prescriptions, and an administrator manages the doctor roster.

Built with **Spring Boot 3.4 / Java 17**, using **MySQL** for relational data, **MongoDB** for
prescriptions, **Thymeleaf** for the admin and doctor dashboards, and a **vanilla JavaScript** frontend that
talks to a REST API.

## Features by role

| Role | What they can do |
|---|---|
| **Admin** | Log in, list / search / filter doctors, add and delete doctors. |
| **Doctor** | Log in, see their appointments for a given day (optionally filtered by patient name), open a patient's record, write and view prescriptions. |
| **Patient** | Sign up, log in, browse and filter doctors, book / reschedule / cancel appointments, see upcoming and past appointments. |

## Tech stack

| Layer | Technology |
|---|---|
| Frontend | HTML, CSS, JavaScript ES modules, Thymeleaf (admin and doctor dashboards) |
| Backend | Spring Boot 3.4.4 (Web, Data JPA, Data MongoDB, Validation, Actuator), Lombok |
| Security | JWT (jjwt 0.12, HS512) with role claims: `admin`, `doctor`, `patient` |
| Databases | MySQL 8 (JPA / Hibernate), MongoDB 6 |
| Build and run | Maven, Docker (multi-stage), Docker Compose |
| CI | GitHub Actions: compile, Checkstyle, HTMLHint / Stylelint / ESLint, hadolint |

## Documentation

| Document | Contents |
|---|---|
| [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) | Architecture analysis: layers, class diagrams, request flows, security, deployment, assessment and recommendations |
| [docs/API.md](docs/API.md) | REST and MVC endpoint reference |
| [schema-design.md](schema-design.md) | MySQL tables and MongoDB collection design |
| [schema-architecture.md](schema-architecture.md) | Original architecture summary |
| [user_stories.md](user_stories.md) | User stories per role |

## Getting started

### Prerequisites

- JDK 17. The Lombok version in use does not support newer JDKs for command-line builds.
- Maven 3.9+
- MySQL 8 (or MariaDB, e.g. XAMPP) and MongoDB 6, **or** Docker Desktop

### Configuration

The application reads its secrets from environment variables. For local runs, create `app/.env`
(it is git-ignored and loaded automatically through `spring.config.import`):

```properties
DATABASE_URL=jdbc:mysql://localhost:3306/cms?createDatabaseIfNotExist=true
DATABASE_USER=root
DATABASE_PASSWORD=
MONGO_URI=mongodb://localhost:27017/cms
JWT_SECRET=<a random string of at least 64 characters, required by HS512>
```

### Option 1: run locally

```bash
cd app
mvn spring-boot:run
```

Open <http://localhost:8080>. Hibernate creates the MySQL tables on first start (`ddl-auto=update`).

### Option 2: Docker Compose (app + MySQL + MongoDB)

```bash
cd app
docker compose up -d --build
```

Compose overrides the database URLs so the app talks to the `mysql` and `mongodb` containers. On the host,
MySQL is exposed on port **3307** and MongoDB on port **27018**, so they don't clash with local installations.
Data is kept in named volumes. `docker compose down -v` deletes it.

### Loading sample data

The seed scripts in [`database/`](database) insert sample doctors, patients, appointments and prescriptions.
The MySQL seed only contains `INSERT`s, so **start the app once first** so that Hibernate creates the tables.

```bash
# Local MySQL
mysql -uroot cms < database/mysql-seed.sql

# Docker Compose MySQL
docker compose exec -T mysql mysql -uroot -proot < ../database/mysql-seed.sql
```

The MongoDB seed (`database/mongo-seed.js`) is loaded automatically by Docker Compose the first time the volume is
created. Locally, run it with `mongosh "mongodb://localhost:27017/cms" database/mongo-seed.js`.

Seeded admin credentials: **`admin` / `admin@1234`**.

## Project structure

```
java-database-capstone/
├── .github/workflows/        CI: compile, lint (Java, frontend, Dockerfile)
├── database/                 MySQL and MongoDB seed scripts
├── docs/                     Architecture and API documentation
└── app/
    ├── Dockerfile
    ├── docker-compose.yml
    ├── pom.xml
    └── src/main/
        ├── java/com/project/back_end/
        │   ├── config/       CORS configuration
        │   ├── controllers/  REST controllers and validation error handler
        │   ├── mvc/          Thymeleaf dashboard controller
        │   ├── services/     Service interfaces
        │   │   └── impl/     Service implementations, shared ServiceImpl, TokenServiceImpl
        │   ├── repo/         Spring Data JPA and MongoDB repositories
        │   ├── models/       JPA entities and MongoDB documents
        │   ├── DTO/          AppointmentDTO, Login
        │   └── Exception/    Global exception handler, JWT filter, ErrorResponse
        └── resources/
            ├── static/       index.html, pages/, js/, assets/css
            ├── templates/    admin/ and doctor/ dashboards (Thymeleaf)
            └── application.properties
```

## Continuous integration

Every push and pull request runs four GitHub Actions workflows:

| Workflow | Tool | Blocks the build? |
|---|---|---|
| `compile-backend.yml` | `mvn clean compile` | Yes |
| `lint-docker.yml` | hadolint | Yes |
| `lint-backend.yml` | Checkstyle (Google style) | No, findings are reported as warnings |
| `lint-frontend.yml` | HTMLHint, Stylelint, ESLint | No, findings are reported as warnings |

## License

See [LICENSE](LICENSE).
