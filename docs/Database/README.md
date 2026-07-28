# Database Documentation

This section captures ER model notes, naming standards, normalization decisions, and schema evolution guidance.

## Authentication Model

The authentication foundation uses the following tables:

- `users`
- `roles`
- `permissions`
- `role_permissions`
- `user_roles`
- `password_reset_tokens`
- `refresh_tokens`

## Design Notes

- Surrogate numeric primary keys are used throughout.
- Unique constraints protect login identifiers and token hashes.
- Audit columns are standardized via the shared base entity contract.
- Soft delete is supported with `deleted` and `deleted_at` fields.
- Refresh and reset tokens are stored hashed, not in plain text.
- Role and permission relationships are normalized for future expansion.
