package com.dsg.sqlconverter.example;

import com.dsg.sqlconverter.MySQLToPostgreSQLConverter;

public class MySQLToPostgreSQLExample {
    
    public static void main(String[] args) {
        MySQLToPostgreSQLConverter converter = new MySQLToPostgreSQLConverter();
        
        System.out.println("=== MySQL to PostgreSQL SQL Converter ===\n");
        
        String mysqlCreateTable = 
            "CREATE TABLE `users` (\n" +
            "  `id` INT AUTO_INCREMENT PRIMARY KEY,\n" +
            "  `username` VARCHAR(50) NOT NULL,\n" +
            "  `email` VARCHAR(100) UNIQUE,\n" +
            "  `age` TINYINT,\n" +
            "  `balance` DECIMAL(10,2),\n" +
            "  `is_active` TINYINT(1) DEFAULT 1,\n" +
            "  `created_at` DATETIME DEFAULT NOW(),\n" +
            "  `bio` LONGTEXT\n" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
        
        System.out.println("Original MySQL SQL:");
        System.out.println(mysqlCreateTable);
        System.out.println("\nConverted PostgreSQL SQL:");
        System.out.println(converter.convert(mysqlCreateTable));
        
        System.out.println("\n" + "=".repeat(50) + "\n");
        
        String mysqlSelect = "SELECT `id`, `name` FROM `products` WHERE `price` > 100 LIMIT 10, 20";
        
        System.out.println("Original MySQL SELECT:");
        System.out.println(mysqlSelect);
        System.out.println("\nConverted PostgreSQL SELECT:");
        System.out.println(converter.convert(mysqlSelect));
        
        System.out.println("\n" + "=".repeat(50) + "\n");
        
        String mysqlDateQuery = 
            "SELECT `order_id`, NOW() as current_time, CURDATE() as today " +
            "FROM `orders` WHERE `created_at` > CURDATE()";
        
        System.out.println("Original MySQL Date Query:");
        System.out.println(mysqlDateQuery);
        System.out.println("\nConverted PostgreSQL Date Query:");
        System.out.println(converter.convert(mysqlDateQuery));
        
        System.out.println("\n" + "=".repeat(50) + "\n");
        
        String mysqlInsert = 
            "INSERT INTO `customers` (`name`, `email`, `created_at`) " +
            "VALUES ('John Doe', 'john@example.com', NOW())";
        
        System.out.println("Original MySQL INSERT:");
        System.out.println(mysqlInsert);
        System.out.println("\nConverted PostgreSQL INSERT:");
        System.out.println(converter.convert(mysqlInsert));
    }
}
