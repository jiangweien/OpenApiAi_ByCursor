package com.dsg.sqlconverter.example;

import com.dsg.sqlconverter.MySQLToPostgreSQLConverter;

public class MySQLViewExample {
    
    public static void main(String[] args) {
        MySQLToPostgreSQLConverter converter = new MySQLToPostgreSQLConverter();
        
        System.out.println("=== MySQL View to GaussDB PostgreSQL View Converter ===\n");
        
        String mysqlView = 
            "CREATE ALGORITHM=UNDEFINED DEFINER=`wzw`@`%` SQL SECURITY DEFINER VIEW `monthly_transaction_summary` AS " +
            "select extract(year from `transactions18`.`transaction_date`) AS `year`," +
            "extract(month from `transactions18`.`transaction_date`) AS `month`," +
            "count(0) AS `total_transactions18`," +
            "sum(`transactions18`.`amount`) AS `total_amount` " +
            "from `transactions18` " +
            "group by `year`,`month` " +
            "order by `year`,`month`";
        
        System.out.println("Original MySQL View:");
        System.out.println(mysqlView);
        System.out.println("\n" + "=".repeat(80) + "\n");
        
        String pgView = converter.convert(mysqlView);
        System.out.println("Converted GaussDB PostgreSQL View:");
        System.out.println(pgView);
        
        System.out.println("\n" + "=".repeat(80) + "\n");
        System.out.println("Key Transformations:");
        System.out.println("1. Removed: ALGORITHM=UNDEFINED");
        System.out.println("2. Removed: DEFINER=`wzw`@`%`");
        System.out.println("3. Removed: SQL SECURITY DEFINER");
        System.out.println("4. Changed: Backticks (`) to Double Quotes (\")");
        System.out.println("5. Changed: extract(year from ...) to EXTRACT(YEAR FROM ...)");
        System.out.println("6. Changed: extract(month from ...) to EXTRACT(MONTH FROM ...)");
        System.out.println("7. Changed: count(0) to COUNT(*)");
        
        System.out.println("\n" + "=".repeat(80) + "\n");
        
        String simpleView = 
            "CREATE DEFINER=`admin`@`localhost` VIEW `user_summary` AS " +
            "SELECT extract(year from created_at) as year, count(0) as total FROM users";
        
        System.out.println("Another Example:");
        System.out.println("MySQL: " + simpleView);
        System.out.println("\nGaussDB PG: " + converter.convert(simpleView));
    }
}
