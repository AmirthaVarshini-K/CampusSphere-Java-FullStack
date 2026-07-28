# Schema

Schema scripts live here as the domain model is introduced.

Recommended conventions:

- Primary keys use surrogate numeric identifiers.
- Foreign keys are explicit and indexed.
- Audit columns are standardized across tables.
- Tenant-scoping columns are introduced where required by the domain.

## Current Authentication Schema

- `authentication.sql` defines users, roles, permissions, user-role assignments, role-permission mappings, password reset tokens, and refresh tokens.
