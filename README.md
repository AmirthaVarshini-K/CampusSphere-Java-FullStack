# CampusSphere

One Platform. Every Event.

CampusSphere is a production-oriented foundation for a multi-college Event & Symposium Management Platform. This repository establishes the backend, frontend, database, and documentation structure for a scalable enterprise application built with Java 21, Spring Boot 3, React, Vite, and MySQL 8.

## Technology Stack

- Java 21
- Spring Boot 3
- Spring Data JPA
- Hibernate
- Bean Validation
- REST API
- JWT-ready security architecture
- React
- Vite
- JavaScript (ES6+)
- React Router
- Axios
- MySQL 8

## Repository Structure

```text
CampusSphere/
backend/
frontend/
database/
docs/
assets/
screenshots/
README.md
LICENSE
```

## Backend

The backend follows layered architecture with dedicated packages for configuration, controllers, services, repositories, entities, DTOs, mappers, security, validation, exceptions, constants, and utilities.

### Current Backend Foundation

- Centralized API response envelope
- Global exception handling
- Request and service logging
- CORS configuration
- Validation infrastructure
- JWT token generation and validation
- Role-based authorization scaffolding
- Password hashing and reset token support
- Refresh token rotation support
- Soft delete and audit columns

### Authentication Module

CampusSphere now includes a complete authentication and user management foundation for:

- Administrator
- Faculty Coordinator
- Student

Supported flows:

- Login with email, register number, or employee ID
- Student registration
- Forgot password and reset password
- Change password
- Logout and refresh-token rotation
- Protected profile access
- Role-based endpoint authorization

### Run Backend

```bash
cd backend
./mvnw.cmd spring-boot:run
```

### Run Backend in Local Development Mode

The recommended local profile uses a persistent file-based H2 database so development accounts survive backend restarts.

```powershell
cd backend
.\mvnw.cmd -Dspring-boot.run.profiles=local spring-boot:run
```

Local development credentials:

- Admin: `admin@campussphere.local` / `Admin@Local123!`
- Faculty: `coordinator@campussphere.local` / `Faculty@Local123!`
- Student: `student@campussphere.local` / `Student@Local123!`

The local student account also supports login with register number `24CSE0001`.

### Build Backend

```bash
cd backend
./mvnw clean package
```

## Frontend

The frontend is structured as a reusable React application shell with routing, shared layouts, reusable components, design tokens, service wrappers, and accessibility-first styling.

### Current Frontend Foundation

- React Router layout hierarchy
- Protected route scaffolding
- Authentication layouts and pages
- Session persistence helpers
- Axios client with bearer token support
- Reusable form, feedback, avatar, and password components
- Responsive dashboard and auth presentation layers

### Run Frontend

```bash
cd frontend
npm install
npm run build
npm run dev
```

## How to Run CampusSphere

### Terminal 1 - Backend

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

### Terminal 2 - Frontend

```powershell
cd frontend
npm install
npm run dev
```

Then open the URL printed in the terminal, typically `http://localhost:5173`.

> Do not open `index.html` with Live Server. CampusSphere is a React application and must be started through the frontend development server.

### Build Frontend

```bash
cd frontend
npm run build
```

## Database

The `database/` directory contains a professional SQL organization for schema scripts, queries, sample data, views, indexes, procedures, triggers, and backup coordination.

Authentication and user management tables are modeled in 3NF and include:

- `users`
- `roles`
- `permissions`
- `role_permissions`
- `user_roles`
- `password_reset_tokens`
- `refresh_tokens`

The schema uses surrogate primary keys, foreign keys, unique constraints, audit columns, soft delete support, status fields, and indexes for login and token lookup paths.

## Troubleshooting

- Do not open the frontend `index.html` through Live Server. Run the frontend development server and open the URL printed in the terminal.
- If the frontend port is already in use, stop the existing process or change `PORT` before running `npm run dev`.
- If the backend port is already in use, stop the process using port `8080` or set `SERVER_PORT`.
- If MySQL is not running, start the database service before launching the backend with the MySQL profile.
- If the database password is invalid, update `DB_USERNAME` and `DB_PASSWORD`.
- If CORS fails, verify `CORS_ALLOWED_ORIGINS` includes the frontend origin.
- If the JWT secret is missing, provide `JWT_SECRET`.
- If local developer accounts disappear after restart, make sure the backend is started with the `local` Spring profile rather than the default in-memory profile.
- If a migration fails, clear the local test database and rerun the migrations from a clean state.
- If Node modules are missing, run `npm install` inside `frontend/`.
- If the browser console shows a blank page, check for failed asset loads or JavaScript syntax errors in the built bundle.

## Architecture

CampusSphere is prepared for:

- Multi-college tenancy
- JWT-based authentication
- Role-based authorization
- Protected routes and APIs
- Clean API response contracts
- Centralized exception handling
- Responsive enterprise dashboard UI
- Future analytics and reporting modules

### Authentication Flow

1. The client submits credentials to `/api/auth/login`.
2. The backend validates the identifier, password, account status, and role assignments.
3. A signed JWT access token and secure refresh token are returned.
4. The client stores the session state and sends the access token on protected requests.
5. The JWT filter validates the token before protected APIs execute.
6. Refresh-token rotation is available for continued sessions.

### Security Decisions

- Passwords are hashed with BCrypt.
- Access tokens are signed with HMAC-SHA256.
- Refresh tokens are stored hashed in the database.
- Password reset tokens are stored hashed and expire quickly.
- Sensitive values are never logged.
- Role-based authorization is enforced at the route and method levels.

## Installation Prerequisites

- Java 21
- Maven Wrapper included
- Node.js 18+
- npm 9+
- MySQL 8

## Contribution Guidelines

1. Keep code modular and readable.
2. Follow existing naming and layering conventions.
3. Avoid placeholder implementations.
4. Preserve accessibility and responsive design.
5. Update documentation when the architecture changes.

## Future Roadmap

- Authentication and authorization
- College tenancy model
- Event lifecycle management
- Registration and attendance workflows
- Certificates and reports
- Analytics dashboards
- Notification services
- Audit logging

## License

This project is licensed under the MIT License.
