# Database Workspace

This directory organizes CampusSphere SQL assets using a production-friendly structure.

## Layout

- `schema/` - future DDL and ER-aligned schema scripts
- `queries/` - reporting and operational SQL
- `sample-data/` - controlled seed sets for demos and development
- `views/` - logical database views
- `indexes/` - index strategy scripts
- `procedures/` - stored procedures
- `triggers/` - trigger definitions
- `backup/` - backup and restore notes

## Standards

- Use consistent lower_snake_case naming for database objects.
- Normalize core data to Third Normal Form.
- Separate schema, seed, and query concerns.
- Keep multi-college tenancy in mind when designing keys and constraints.
