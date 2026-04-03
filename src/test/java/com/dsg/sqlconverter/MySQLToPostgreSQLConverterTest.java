package com.dsg.sqlconverter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MySQLToPostgreSQLConverterTest {
    
    private MySQLToPostgreSQLConverter converter;
    
    @BeforeEach
    public void setUp() {
        converter = new MySQLToPostgreSQLConverter();
    }
    
    @Test
    public void testNullInput() {
        assertNull(converter.convert(null));
    }
    
    @Test
    public void testEmptyInput() {
        assertEquals("", converter.convert(""));
    }
    
    @Test
    public void testCreateTableWithAutoIncrement() {
        String mysqlSql = "CREATE TABLE `users` (" +
                "`id` INT AUTO_INCREMENT PRIMARY KEY," +
                "`name` VARCHAR(100)" +
                ")";
        
        String result = converter.convert(mysqlSql);
        
        assertTrue(result.contains("SERIAL"));
        assertFalse(result.contains("AUTO_INCREMENT"));
        assertFalse(result.contains("`"));
        assertTrue(result.contains("\""));
    }
    
    @Test
    public void testDataTypeConversion() {
        String mysqlSql = "CREATE TABLE test (" +
                "col1 TINYINT, " +
                "col2 DATETIME, " +
                "col3 DOUBLE, " +
                "col4 TEXT, " +
                "col5 BLOB" +
                ")";
        
        String result = converter.convert(mysqlSql);
        
        assertTrue(result.contains("SMALLINT"));
        assertTrue(result.contains("TIMESTAMP"));
        assertTrue(result.contains("DOUBLE PRECISION"));
        assertTrue(result.contains("TEXT"));
        assertTrue(result.contains("BYTEA"));
    }
    
    @Test
    public void testBackticksConversion() {
        String mysqlSql = "SELECT `name`, `age` FROM `users`";
        String result = converter.convert(mysqlSql);
        
        assertFalse(result.contains("`"));
        assertTrue(result.contains("\"name\""));
        assertTrue(result.contains("\"age\""));
        assertTrue(result.contains("\"users\""));
    }
    
    @Test
    public void testEngineRemoval() {
        String mysqlSql = "CREATE TABLE users (id INT) ENGINE=InnoDB";
        String result = converter.convert(mysqlSql);
        
        assertFalse(result.contains("ENGINE"));
        assertFalse(result.contains("InnoDB"));
    }
    
    @Test
    public void testCharsetRemoval() {
        String mysqlSql = "CREATE TABLE users (id INT) DEFAULT CHARSET=utf8mb4";
        String result = converter.convert(mysqlSql);
        
        assertFalse(result.contains("CHARSET"));
        assertFalse(result.contains("utf8mb4"));
    }
    
    @Test
    public void testLimitOffsetConversion() {
        String mysqlSql = "SELECT * FROM users LIMIT 10, 20";
        String result = converter.convert(mysqlSql);
        
        assertTrue(result.contains("LIMIT 20 OFFSET 10"));
        assertFalse(result.matches(".*LIMIT\\s+\\d+\\s*,\\s*\\d+.*"));
    }
    
    @Test
    public void testNowFunctionConversion() {
        String mysqlSql = "SELECT NOW() FROM dual";
        String result = converter.convert(mysqlSql);
        
        assertTrue(result.contains("CURRENT_TIMESTAMP"));
        assertFalse(result.contains("NOW()"));
    }
    
    @Test
    public void testCurdateFunctionConversion() {
        String mysqlSql = "SELECT CURDATE() FROM dual";
        String result = converter.convert(mysqlSql);
        
        assertTrue(result.contains("CURRENT_DATE"));
        assertFalse(result.contains("CURDATE()"));
    }
    
    @Test
    public void testCurtimeFunctionConversion() {
        String mysqlSql = "SELECT CURTIME() FROM dual";
        String result = converter.convert(mysqlSql);
        
        assertTrue(result.contains("CURRENT_TIME"));
        assertFalse(result.contains("CURTIME()"));
    }
    
    @Test
    public void testComplexCreateTable() {
        String mysqlSql = "CREATE TABLE `products` (" +
                "`id` INT AUTO_INCREMENT PRIMARY KEY," +
                "`name` VARCHAR(255) NOT NULL," +
                "`price` DECIMAL(10,2)," +
                "`created_at` DATETIME DEFAULT NOW()," +
                "`description` LONGTEXT" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
        
        String result = converter.convert(mysqlSql);
        
        assertTrue(result.contains("SERIAL"));
        assertTrue(result.contains("VARCHAR(255)"));
        assertTrue(result.contains("DECIMAL(10,2)"));
        assertTrue(result.contains("TIMESTAMP"));
        assertTrue(result.contains("CURRENT_TIMESTAMP"));
        assertTrue(result.contains("TEXT"));
        assertFalse(result.contains("ENGINE"));
        assertFalse(result.contains("CHARSET"));
        assertFalse(result.contains("`"));
    }
    
    @Test
    public void testTinyIntBooleanConversion() {
        String mysqlSql = "CREATE TABLE test (is_active TINYINT(1))";
        String result = converter.convert(mysqlSql);
        
        assertTrue(result.contains("BOOLEAN"));
    }
    
    @Test
    public void testVarcharWithLength() {
        String mysqlSql = "CREATE TABLE test (name VARCHAR(50))";
        String result = converter.convert(mysqlSql);
        
        assertTrue(result.contains("VARCHAR(50)"));
    }
    
    @Test
    public void testCharWithLength() {
        String mysqlSql = "CREATE TABLE test (code CHAR(10))";
        String result = converter.convert(mysqlSql);
        
        assertTrue(result.contains("CHAR(10)"));
    }
}
