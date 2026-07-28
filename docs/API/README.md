# API Documentation

The API layer is standardized around a consistent response envelope:

```json
{
  "success": true,
  "message": "Operation completed successfully.",
  "timestamp": "...",
  "data": {}
}
```

## Authentication Endpoints

- `POST /api/auth/login`
- `POST /api/auth/logout`
- `POST /api/auth/register/student`
- `POST /api/auth/forgot-password`
- `POST /api/auth/reset-password`
- `POST /api/auth/change-password`
- `POST /api/auth/refresh-token`

## User Endpoints

- `GET /api/users/me`
- `PUT /api/users/profile`
- `GET /api/users/{id}`
- `PUT /api/users/{id}`
- `DELETE /api/users/{id}`

## Security Contract

- All protected endpoints require a valid JWT access token.
- Unauthorized requests return `401 Unauthorized`.
- Role violations return `403 Forbidden`.
- Validation failures return a structured field error payload.
- Duplicate identifiers and expired tokens use explicit domain errors.
