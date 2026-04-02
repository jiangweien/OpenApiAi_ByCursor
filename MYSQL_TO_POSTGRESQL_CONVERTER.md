# MySQL to PostgreSQL SQL Converter

A comprehensive Java utility for converting MySQL SQL syntax to PostgreSQL-compatible SQL.

## Features

This converter handles the following MySQL to PostgreSQL conversions:

### 1. Data Types
- `TINYINT(1)` → `BOOLEAN`
- `TINYINT` → `SMALLINT`
- `DOUBLE` → `DOUBLE PRECISION`
- `DATETIME` → `TIMESTAMP`
- `BLOB`, `MEDIUMBLOB`, `LONGBLOB` → `BYTEA`
- `MEDIUMTEXT`, `LONGTEXT` → `TEXT`

### 2. Auto-Increment
- `INT AUTO_INCREMENT` → `SERIAL`
- `BIGINT AUTO_INCREMENT` → `BIGSERIAL`
- `SMALLINT AUTO_INCREMENT` → `SMALLSERIAL`
- Removes `AUTO_INCREMENT=n` table options

### 3. Identifiers
- Backticks (\`) → Double quotes (")
- `SELECT \`id\` FROM \`users\`` → `SELECT "id" FROM "users"`

### 4. MySQL-Specific Clauses (Removed)
- `ENGINE=InnoDB`
- `CHARACTER SET` / `CHARSET`
- `COLLATE`
- `DEFAULT CHARSET=...`
- `UNSIGNED`
- `ZEROFILL`

### 5. Functions
- `NOW()` → `CURRENT_TIMESTAMP`
- `CURDATE()` → `CURRENT_DATE`
- `CURTIME()` → `CURRENT_TIME`
- `UNIX_TIMESTAMP()` → `EXTRACT(EPOCH FROM CURRENT_TIMESTAMP)`
- `IFNULL(a, b)` → `COALESCE(a, b)`

### 6. LIMIT Clause
- `LIMIT offset, count` → `LIMIT count OFFSET offset`
- Example: `LIMIT 10, 20` → `LIMIT 20 OFFSET 10`

### 7. Index Hints (Removed)
- `USE INDEX (...)`
- `FORCE INDEX (...)`
- `IGNORE INDEX (...)`

### 8. ENUM and SET
- `ENUM(...)` → `VARCHAR(255) CHECK (...)`
- `SET(...)` → `TEXT[]`

## Usage

### Basic Usage

```java
import com.dsg.aiSdk.converter.MySQLToPostgreSQLConverter;

public class Example {
    public static void main(String[] args) {
        MySQLToPostgreSQLConverter converter = new MySQLToPostgreSQLConverter();
        
        String mysqlSql = "CREATE TABLE `users` (" +
            "`id` INT AUTO_INCREMENT PRIMARY KEY," +
            "`name` VARCHAR(100)," +
            "`created_at` DATETIME DEFAULT NOW()" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
        
        String pgSql = converter.convert(mysqlSql);
        System.out.println(pgSql);
    }
}
```

**Output:**
```sql
CREATE TABLE "users" (
    "id" SERIAL PRIMARY KEY,
    "name" VARCHAR(100),
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP
)
```

### Batch Conversion

```java
import com.dsg.aiSdk.converter.MySQLToPostgreSQLConverter;
import java.util.Arrays;
import java.util.List;

public class BatchExample {
    public static void main(String[] args) {
        MySQLToPostgreSQLConverter converter = new MySQLToPostgreSQLConverter();
        
        List<String> mysqlStatements = Arrays.asList(
            "CREATE TABLE products (id INT AUTO_INCREMENT PRIMARY KEY)",
            "SELECT * FROM `users` WHERE created_at >= NOW()",
            "SELECT * FROM orders LIMIT 10, 20"
        );
        
        List<String> pgStatements = converter.convertBatch(mysqlStatements);
        
        for (String sql : pgStatements) {
            System.out.println(sql);
        }
    }
}
```

## Examples

### Example 1: CREATE TABLE

**MySQL:**
```sql
CREATE TABLE `users` (
    `id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(100) NOT NULL,
    `email` VARCHAR(255) UNIQUE,
    `is_active` TINYINT(1) DEFAULT 0,
    `created_at` DATETIME DEFAULT NOW()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

**PostgreSQL:**
```sql
CREATE TABLE "users" (
    "id" BIGSERIAL PRIMARY KEY,
    "name" VARCHAR(100) NOT NULL,
    "email" VARCHAR(255) UNIQUE,
    "is_active" BOOLEAN DEFAULT 0,
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP
)
```

### Example 2: SELECT with LIMIT

**MySQL:**
```sql
SELECT `id`, `name`, IFNULL(`email`, 'N/A') AS email
FROM `users`
WHERE `created_at` >= CURDATE()
ORDER BY `name`
LIMIT 10, 20;
```

**PostgreSQL:**
```sql
SELECT "id", "name", COALESCE("email", 'N/A') AS email
FROM "users"
WHERE "created_at" >= CURRENT_DATE
ORDER BY "name"
LIMIT 20 OFFSET 10
```

### Example 3: INSERT Statement

**MySQL:**
```sql
INSERT INTO `logs` (`message`, `created_at`)
VALUES ('User logged in', NOW());
```

**PostgreSQL:**
```sql
INSERT INTO "logs" ("message", "created_at")
VALUES ('User logged in', CURRENT_TIMESTAMP)
```

### Example 4: UPDATE Statement

**MySQL:**
```sql
UPDATE `users`
SET `updated_at` = NOW()
WHERE `id` = 1;
```

**PostgreSQL:**
```sql
UPDATE "users"
SET "updated_at" = CURRENT_TIMESTAMP
WHERE "id" = 1
```

## Running the Examples

To run the comprehensive examples:

```bash
mvn clean compile
mvn exec:java -Dexec.mainClass="com.dsg.aiSdk.example.MySQLToPostgreSQLExample"
```

## Testing

Run the test suite:

```bash
mvn test
```

The test suite includes:
- Auto-increment conversion tests
- Data type conversion tests
- Function conversion tests
- LIMIT clause conversion tests
- Backtick to double-quote conversion tests
- MySQL-specific clause removal tests
- Batch conversion tests
- Edge case handling

## Limitations

1. **ENUM Types**: Converted to `VARCHAR(255) CHECK` constraint, which may need manual adjustment
2. **Comments**: MySQL column/table comments are removed (PostgreSQL uses `COMMENT ON` syntax)
3. **Complex Functions**: Some complex MySQL-specific functions may require manual conversion
4. **Stored Procedures**: Not supported (requires significant refactoring)
5. **Triggers**: Not supported (syntax differs significantly between MySQL and PostgreSQL)

## Important Notes

1. **Review Output**: Always review the converted SQL before executing it
2. **Test Thoroughly**: Test the converted SQL in a development environment first
3. **Manual Adjustments**: Some conversions may require manual fine-tuning
4. **Performance**: PostgreSQL query optimization may differ from MySQL
5. **Constraints**: Check that CHECK constraints from ENUM conversions are appropriate

## Project Structure

```
src/
├── main/java/com/dsg/aiSdk/
│   ├── converter/
│   │   └── MySQLToPostgreSQLConverter.java
│   └── example/
│       └── MySQLToPostgreSQLExample.java
└── test/java/com/dsg/aiSdk/
    └── converter/
        └── MySQLToPostgreSQLConverterTest.java
```

## License

This converter is part of the OpenApiAI_ByCursor project.

## Contributing

When adding new conversion rules:
1. Add the conversion method to `MySQLToPostgreSQLConverter.java`
2. Call the method in the `convert()` method
3. Add comprehensive tests to `MySQLToPostgreSQLConverterTest.java`
4. Update this documentation
