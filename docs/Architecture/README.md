# Architecture

CampusSphere follows a layered enterprise architecture:

- frontend shell for routing, layouts, and reusable UI
- backend foundation for controllers, services, repositories, DTOs, security, and exception handling
- database workspace organized for schema and operational SQL

## Authentication Layer

The authentication module now introduces:

- JWT access-token validation
- refresh-token rotation support
- role-based authorization
- protected route handling
- audit-aware user management entities
- centralized password reset and change-password flows

The initial build still excludes business modules so later modules can be introduced cleanly without architectural rework.
