# Flyway migration conventions

- Store all schema changes in this folder (`src/main/resources/db/migration`).
- Use monotonic versioned files: `V{n}__{snake_case}.sql`.
- Keep versions strictly increasing without gaps for new schema changes.
- Use optional repeatable migrations (`R__{name}.sql`) only for deterministic idempotent objects (for example views).
- Never modify an already-applied versioned migration; create a new `V{n+1}__*.sql` file instead.
