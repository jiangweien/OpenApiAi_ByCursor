package com.dsg.sqlconverter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MySQLViewConverterTest {
    
    private MySQLToPostgreSQLConverter converter;
    
    @BeforeEach
    public void setUp() {
        converter = new MySQLToPostgreSQLConverter();
    }
    
    @Test
    public void testMonthlyTransactionSummaryView() {
        String mysqlView = "CREATE ALGORITHM=UNDEFINED DEFINER=`wzw`@`%` SQL SECURITY DEFINER VIEW `monthly_transaction_summary` AS " +
                "select extract(year from `transactions18`.`transaction_date`) AS `year`," +
                "extract(month from `transactions18`.`transaction_date`) AS `month`," +
                "count(0) AS `total_transactions18`," +
                "sum(`transactions18`.`amount`) AS `total_amount` " +
                "from `transactions18` " +
                "group by `year`,`month` " +
                "order by `year`,`month`";
        
        String result = converter.convert(mysqlView);
        
        assertFalse(result.contains("ALGORITHM"));
        assertFalse(result.contains("DEFINER"));
        assertFalse(result.contains("SQL SECURITY"));
        assertTrue(result.contains("CREATE OR REPLACE VIEW"));
        assertTrue(result.contains("EXTRACT(YEAR FROM"));
        assertTrue(result.contains("EXTRACT(MONTH FROM"));
        assertTrue(result.contains("COUNT(*)"));
        assertFalse(result.contains("`"));
        assertTrue(result.contains("\"monthly_transaction_summary\""));
    }
    
    @Test
    public void testViewWithDefinerOnly() {
        String mysqlView = "CREATE DEFINER=`root`@`localhost` VIEW `test_view` AS SELECT * FROM users";
        String result = converter.convert(mysqlView);
        
        assertTrue(result.contains("CREATE OR REPLACE VIEW"));
        assertFalse(result.contains("DEFINER"));
    }
    
    @Test
    public void testViewWithAlgorithmOnly() {
        String mysqlView = "CREATE ALGORITHM=MERGE VIEW `test_view` AS SELECT * FROM users";
        String result = converter.convert(mysqlView);
        
        assertTrue(result.contains("CREATE OR REPLACE VIEW"));
        assertFalse(result.contains("ALGORITHM"));
    }
    
    @Test
    public void testExtractYearFunction() {
        String sql = "SELECT extract(year from date_column) FROM table1";
        String result = converter.convert(sql);
        
        assertTrue(result.contains("EXTRACT(YEAR FROM"));
    }
    
    @Test
    public void testExtractMonthFunction() {
        String sql = "SELECT extract(month from date_column) FROM table1";
        String result = converter.convert(sql);
        
        assertTrue(result.contains("EXTRACT(MONTH FROM"));
    }
    
    @Test
    public void testExtractDayFunction() {
        String sql = "SELECT extract(day from date_column) FROM table1";
        String result = converter.convert(sql);
        
        assertTrue(result.contains("EXTRACT(DAY FROM"));
    }
    
    @Test
    public void testCountZeroFunction() {
        String sql = "SELECT count(0) FROM users";
        String result = converter.convert(sql);
        
        assertTrue(result.contains("COUNT(*)"));
        assertFalse(result.contains("count(0)"));
    }
    
    @Test
    public void testComplexViewWithMultipleExtracts() {
        String mysqlView = "CREATE VIEW summary AS " +
                "SELECT extract(year from created_at) as y, " +
                "extract(month from created_at) as m, " +
                "count(0) as cnt FROM orders";
        
        String result = converter.convert(mysqlView);
        
        assertTrue(result.contains("EXTRACT(YEAR FROM"));
        assertTrue(result.contains("EXTRACT(MONTH FROM"));
        assertTrue(result.contains("COUNT(*)"));
    }
    
    @Test
    public void testViewWithMixedCaseExtract() {
        String sql = "SELECT EXTRACT(YEAR FROM date_col), Extract(Month From date_col)";
        String result = converter.convert(sql);
        
        assertTrue(result.contains("EXTRACT(YEAR FROM"));
        assertTrue(result.contains("EXTRACT(MONTH FROM"));
    }
}
