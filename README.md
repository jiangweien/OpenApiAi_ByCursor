# OpenApiAi_ByCursor

A Java SDK for AI development with utilities for database migration.

## Features

### MySQL to PostgreSQL SQL Converter

A comprehensive Java utility for converting MySQL SQL syntax to PostgreSQL-compatible SQL.

**Key Features:**
- Converts MySQL data types to PostgreSQL equivalents
- Handles AUTO_INCREMENT → SERIAL/BIGSERIAL conversion
- Converts MySQL backticks to PostgreSQL double quotes
- Removes MySQL-specific clauses (ENGINE, CHARSET, COLLATE, etc.)
- Converts MySQL functions to PostgreSQL equivalents
- Supports batch conversion

**Quick Start:**

```java
import com.dsg.aiSdk.converter.MySQLToPostgreSQLConverter;

MySQLToPostgreSQLConverter converter = new MySQLToPostgreSQLConverter();

String mysqlSql = "CREATE TABLE `users` (" +
    "`id` INT AUTO_INCREMENT PRIMARY KEY," +
    "`name` VARCHAR(100)," +
    "`created_at` DATETIME DEFAULT NOW()" +
    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";

String pgSql = converter.convert(mysqlSql);
// Result: CREATE TABLE "users" ("id" SERIAL PRIMARY KEY,"name" VARCHAR(100),"created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP)
```

**Documentation:**
- Full documentation: [MYSQL_TO_POSTGRESQL_CONVERTER.md](MYSQL_TO_POSTGRESQL_CONVERTER.md)
- 32 comprehensive unit tests
- Example application with 5 use cases

## Building

```bash
mvn clean compile
```

## Testing

```bash
mvn test
```

## Running Examples

```bash
mvn exec:java -Dexec.mainClass="com.dsg.aiSdk.example.MySQLToPostgreSQLExample"
```
