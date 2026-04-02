# MySQL to PostgreSQL Conversion Examples

This document showcases real conversion examples from the MySQL to PostgreSQL SQL converter.

## Example 1: Simple CREATE TABLE

### MySQL:
```sql
CREATE TABLE `users` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(100)
);
```

### PostgreSQL:
```sql
CREATE TABLE "users" (
    "id" SERIAL PRIMARY KEY,
    "name" VARCHAR(100)
)
```

---

## Example 2: Complex CREATE TABLE with MySQL Features

### MySQL:
```sql
CREATE TABLE `users` (
    `id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(100) NOT NULL,
    `email` VARCHAR(255) UNIQUE,
    `age` INT UNSIGNED,
    `balance` DOUBLE,
    `is_active` TINYINT(1) DEFAULT 0,
    `avatar` BLOB,
    `bio` MEDIUMTEXT,
    `created_at` DATETIME DEFAULT NOW(),
    `updated_at` TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci AUTO_INCREMENT=1000;
```

### PostgreSQL:
```sql
CREATE TABLE "users" (
    "id" BIGSERIAL PRIMARY KEY,
    "name" VARCHAR(100) NOT NULL,
    "email" VARCHAR(255) UNIQUE,
    "age" INT,
    "balance" DOUBLE PRECISION,
    "is_active" BOOLEAN DEFAULT 0,
    "avatar" BYTEA,
    "bio" TEXT,
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP
)
```

**Changes Made:**
- `BIGINT UNSIGNED AUTO_INCREMENT` → `BIGSERIAL`
- `INT UNSIGNED` → `INT` (UNSIGNED removed)
- `DOUBLE` → `DOUBLE PRECISION`
- `TINYINT(1)` → `BOOLEAN`
- `BLOB` → `BYTEA`
- `MEDIUMTEXT` → `TEXT`
- `DATETIME` → `TIMESTAMP`
- `NOW()` → `CURRENT_TIMESTAMP`
- Backticks → Double quotes
- `ENGINE=InnoDB` removed
- `DEFAULT CHARSET=utf8mb4` removed
- `COLLATE=utf8mb4_unicode_ci` removed
- `AUTO_INCREMENT=1000` removed

---

## Example 3: SELECT with Functions and LIMIT

### MySQL:
```sql
SELECT `u`.`id`, `u`.`name`, 
       IFNULL(`u`.`email`, 'no-email@example.com') AS email,
       `u`.`created_at`
FROM `users` `u`
USE INDEX (idx_created_at)
WHERE `u`.`created_at` >= CURDATE()
  AND `u`.`is_active` = 1
ORDER BY `u`.`name` ASC
LIMIT 10, 20;
```

### PostgreSQL:
```sql
SELECT "u"."id", "u"."name",
       COALESCE("u"."email", 'no-email@example.com') AS email,
       "u"."created_at"
FROM "users" "u"
WHERE "u"."created_at" >= CURRENT_DATE
  AND "u"."is_active" = 1
ORDER BY "u"."name" ASC
LIMIT 20 OFFSET 10
```

**Changes Made:**
- Backticks → Double quotes
- `IFNULL()` → `COALESCE()`
- `CURDATE()` → `CURRENT_DATE`
- `USE INDEX (idx_created_at)` removed
- `LIMIT 10, 20` → `LIMIT 20 OFFSET 10`

---

## Example 4: INSERT with Functions

### MySQL:
```sql
INSERT INTO `logs` (`level`, `message`, `created_at`)
VALUES ('INFO', 'User logged in', NOW());
```

### PostgreSQL:
```sql
INSERT INTO "logs" ("level", "message", "created_at")
VALUES ('INFO', 'User logged in', CURRENT_TIMESTAMP)
```

**Changes Made:**
- Backticks → Double quotes
- `NOW()` → `CURRENT_TIMESTAMP`

---

## Example 5: UPDATE with Multiple Functions

### MySQL:
```sql
UPDATE `users`
SET `updated_at` = NOW(),
    `last_login` = CURTIME()
WHERE `id` = 1;
```

### PostgreSQL:
```sql
UPDATE "users"
SET "updated_at" = CURRENT_TIMESTAMP,
    "last_login" = CURRENT_TIME
WHERE "id" = 1
```

**Changes Made:**
- Backticks → Double quotes
- `NOW()` → `CURRENT_TIMESTAMP`
- `CURTIME()` → `CURRENT_TIME`

---

## Example 6: Timestamp Function

### MySQL:
```sql
INSERT INTO `audit_log` (action, timestamp)
VALUES ('login', UNIX_TIMESTAMP());
```

### PostgreSQL:
```sql
INSERT INTO "audit_log" (action, timestamp)
VALUES ('login', EXTRACT(EPOCH FROM CURRENT_TIMESTAMP))
```

**Changes Made:**
- Backticks → Double quotes
- `UNIX_TIMESTAMP()` → `EXTRACT(EPOCH FROM CURRENT_TIMESTAMP)`

---

## Supported Conversions Summary

### Data Types
| MySQL | PostgreSQL |
|-------|-----------|
| TINYINT(1) | BOOLEAN |
| TINYINT | SMALLINT |
| DOUBLE | DOUBLE PRECISION |
| DATETIME | TIMESTAMP |
| BLOB, MEDIUMBLOB, LONGBLOB | BYTEA |
| MEDIUMTEXT, LONGTEXT | TEXT |

### Auto-Increment
| MySQL | PostgreSQL |
|-------|-----------|
| INT AUTO_INCREMENT | SERIAL |
| BIGINT AUTO_INCREMENT | BIGSERIAL |
| SMALLINT AUTO_INCREMENT | SMALLSERIAL |

### Functions
| MySQL | PostgreSQL |
|-------|-----------|
| NOW() | CURRENT_TIMESTAMP |
| CURDATE() | CURRENT_DATE |
| CURTIME() | CURRENT_TIME |
| UNIX_TIMESTAMP() | EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) |
| IFNULL(a, b) | COALESCE(a, b) |

### Removed Features
- `ENGINE=...`
- `CHARACTER SET` / `CHARSET`
- `COLLATE`
- `UNSIGNED`
- `ZEROFILL`
- `USE INDEX (...)`, `FORCE INDEX (...)`, `IGNORE INDEX (...)`
- `AUTO_INCREMENT=n` (table option)

### Syntax Changes
- Backticks (\`) → Double quotes (")
- `LIMIT offset, count` → `LIMIT count OFFSET offset`
