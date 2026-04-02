package com.dsg.aiSdk.example;

import com.dsg.aiSdk.converter.MySQLToPostgreSQLConverter;

import java.util.Arrays;
import java.util.List;

/**
 * Example demonstrating MySQL to PostgreSQL SQL conversion
 */
public class MySQLToPostgreSQLExample {

    public static void main(String[] args) {
        MySQLToPostgreSQLConverter converter = new MySQLToPostgreSQLConverter();

        System.out.println("=== MySQL to PostgreSQL SQL Converter ===\n");

        example1CreateTable(converter);
        example2SelectQuery(converter);
        example3InsertQuery(converter);
        example4UpdateQuery(converter);
        example5BatchConversion(converter);
    }

    private static void example1CreateTable(MySQLToPostgreSQLConverter converter) {
        System.out.println("Example 1: CREATE TABLE Statement");
        System.out.println("----------------------------------");

        String mysqlSql = "CREATE TABLE `users` (" +
                "`id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY," +
                "`name` VARCHAR(100) NOT NULL," +
                "`email` VARCHAR(255) UNIQUE," +
                "`age` INT UNSIGNED," +
                "`balance` DOUBLE," +
                "`is_active` TINYINT(1) DEFAULT 0," +
                "`avatar` BLOB," +
                "`bio` MEDIUMTEXT," +
                "`created_at` DATETIME DEFAULT NOW()," +
                "`updated_at` TIMESTAMP" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci AUTO_INCREMENT=1000";

        System.out.println("MySQL SQL:");
        System.out.println(mysqlSql);
        System.out.println();

        String pgSql = converter.convert(mysqlSql);
        System.out.println("PostgreSQL SQL:");
        System.out.println(pgSql);
        System.out.println("\n");
    }

    private static void example2SelectQuery(MySQLToPostgreSQLConverter converter) {
        System.out.println("Example 2: SELECT Query");
        System.out.println("-----------------------");

        String mysqlSql = "SELECT `u`.`id`, `u`.`name`, " +
                "IFNULL(`u`.`email`, 'no-email@example.com') AS email, " +
                "`u`.`created_at` " +
                "FROM `users` `u` " +
                "USE INDEX (idx_created_at) " +
                "WHERE `u`.`created_at` >= CURDATE() " +
                "AND `u`.`is_active` = 1 " +
                "ORDER BY `u`.`name` ASC " +
                "LIMIT 10, 20";

        System.out.println("MySQL SQL:");
        System.out.println(mysqlSql);
        System.out.println();

        String pgSql = converter.convert(mysqlSql);
        System.out.println("PostgreSQL SQL:");
        System.out.println(pgSql);
        System.out.println("\n");
    }

    private static void example3InsertQuery(MySQLToPostgreSQLConverter converter) {
        System.out.println("Example 3: INSERT Statement");
        System.out.println("---------------------------");

        String mysqlSql = "INSERT INTO `logs` (`level`, `message`, `created_at`) " +
                "VALUES ('INFO', 'User logged in', NOW())";

        System.out.println("MySQL SQL:");
        System.out.println(mysqlSql);
        System.out.println();

        String pgSql = converter.convert(mysqlSql);
        System.out.println("PostgreSQL SQL:");
        System.out.println(pgSql);
        System.out.println("\n");
    }

    private static void example4UpdateQuery(MySQLToPostgreSQLConverter converter) {
        System.out.println("Example 4: UPDATE Statement");
        System.out.println("---------------------------");

        String mysqlSql = "UPDATE `users` " +
                "SET `updated_at` = NOW(), `last_login` = CURTIME() " +
                "WHERE `id` = 1";

        System.out.println("MySQL SQL:");
        System.out.println(mysqlSql);
        System.out.println();

        String pgSql = converter.convert(mysqlSql);
        System.out.println("PostgreSQL SQL:");
        System.out.println(pgSql);
        System.out.println("\n");
    }

    private static void example5BatchConversion(MySQLToPostgreSQLConverter converter) {
        System.out.println("Example 5: Batch Conversion");
        System.out.println("---------------------------");

        List<String> mysqlStatements = Arrays.asList(
                "CREATE TABLE products (id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(100))",
                "CREATE TABLE orders (id BIGINT AUTO_INCREMENT PRIMARY KEY, order_date DATETIME)",
                "SELECT * FROM `products` WHERE created_at >= NOW() LIMIT 5, 10",
                "INSERT INTO `audit_log` (action, timestamp) VALUES ('login', UNIX_TIMESTAMP())"
        );

        System.out.println("MySQL SQL Statements:");
        for (int i = 0; i < mysqlStatements.size(); i++) {
            System.out.println((i + 1) + ". " + mysqlStatements.get(i));
        }
        System.out.println();

        List<String> pgStatements = converter.convertBatch(mysqlStatements);
        System.out.println("PostgreSQL SQL Statements:");
        for (int i = 0; i < pgStatements.size(); i++) {
            System.out.println((i + 1) + ". " + pgStatements.get(i));
        }
        System.out.println();
    }
}
