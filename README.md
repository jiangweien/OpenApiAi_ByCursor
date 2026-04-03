# OpenApiAi_ByCursor

## MySQL to PostgreSQL SQL Converter

一个用于将MySQL SQL语句转换为PostgreSQL SQL语句的Java工具库。

### 功能特性

- **数据类型转换**：自动将MySQL数据类型映射到PostgreSQL对应类型
  - `TINYINT` → `SMALLINT`
  - `TINYINT(1)` → `BOOLEAN`
  - `INT` → `INTEGER`
  - `DATETIME` → `TIMESTAMP`
  - `DOUBLE` → `DOUBLE PRECISION`
  - `BLOB/LONGBLOB` → `BYTEA`
  - `TEXT/LONGTEXT` → `TEXT`

- **语法转换**：
  - `AUTO_INCREMENT` → `SERIAL`
  - 反引号 `` ` `` → 双引号 `"`
  - `LIMIT offset, count` → `LIMIT count OFFSET offset`
  - 移除 `ENGINE=InnoDB` 等引擎声明
  - 移除 `CHARSET` 和 `COLLATE` 声明

- **函数转换**：
  - `NOW()` → `CURRENT_TIMESTAMP`
  - `CURDATE()` → `CURRENT_DATE`
  - `CURTIME()` → `CURRENT_TIME`

### 快速开始

```java
import com.dsg.sqlconverter.MySQLToPostgreSQLConverter;

MySQLToPostgreSQLConverter converter = new MySQLToPostgreSQLConverter();

String mysqlSql = "CREATE TABLE `users` (" +
    "`id` INT AUTO_INCREMENT PRIMARY KEY," +
    "`name` VARCHAR(100)" +
    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";

String pgSql = converter.convert(mysqlSql);
System.out.println(pgSql);
```

### 示例

运行示例程序：

```bash
mvn compile exec:java -Dexec.mainClass="com.dsg.sqlconverter.example.MySQLToPostgreSQLExample"
```

### 测试

运行单元测试：

```bash
mvn test
```

### 构建

```bash
mvn clean package
```

### 环境要求

- JDK 8+
- Maven 3.6+
