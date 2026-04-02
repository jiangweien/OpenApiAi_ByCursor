package com.dsg.aiSdk.converter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MySQLToPostgreSQLConverterTest {

    private MySQLToPostgreSQLConverter converter;

    @BeforeEach
    void setUp() {
        converter = new MySQLToPostgreSQLConverter();
    }

    @Test
    void testConvertAutoIncrement() {
        String mysql = "CREATE TABLE users (id INT AUTO_INCREMENT PRIMARY KEY)";
        String expected = "CREATE TABLE users (id SERIAL PRIMARY KEY)";
        assertEquals(expected, converter.convert(mysql));
    }

    @Test
    void testConvertBigIntAutoIncrement() {
        String mysql = "CREATE TABLE users (id BIGINT AUTO_INCREMENT PRIMARY KEY)";
        String expected = "CREATE TABLE users (id BIGSERIAL PRIMARY KEY)";
        assertEquals(expected, converter.convert(mysql));
    }

    @Test
    void testConvertBackticks() {
        String mysql = "SELECT `id`, `name` FROM `users`";
        String expected = "SELECT \"id\", \"name\" FROM \"users\"";
        assertEquals(expected, converter.convert(mysql));
    }

    @Test
    void testConvertTinyInt() {
        String mysql = "CREATE TABLE users (is_active TINYINT(1))";
        String expected = "CREATE TABLE users (is_active BOOLEAN)";
        assertEquals(expected, converter.convert(mysql));
    }

    @Test
    void testConvertDouble() {
        String mysql = "CREATE TABLE products (price DOUBLE)";
        String expected = "CREATE TABLE products (price DOUBLE PRECISION)";
        assertEquals(expected, converter.convert(mysql));
    }

    @Test
    void testConvertDateTime() {
        String mysql = "CREATE TABLE events (created_at DATETIME)";
        String expected = "CREATE TABLE events (created_at TIMESTAMP)";
        assertEquals(expected, converter.convert(mysql));
    }

    @Test
    void testConvertBlob() {
        String mysql = "CREATE TABLE files (data BLOB, large_data LONGBLOB)";
        String expected = "CREATE TABLE files (data BYTEA, large_data BYTEA)";
        assertEquals(expected, converter.convert(mysql));
    }

    @Test
    void testConvertText() {
        String mysql = "CREATE TABLE articles (content MEDIUMTEXT, summary LONGTEXT)";
        String expected = "CREATE TABLE articles (content TEXT, summary TEXT)";
        assertEquals(expected, converter.convert(mysql));
    }

    @Test
    void testRemoveEngineClause() {
        String mysql = "CREATE TABLE users (id INT) ENGINE=InnoDB";
        String result = converter.convert(mysql);
        assertFalse(result.contains("ENGINE"));
    }

    @Test
    void testRemoveCharsetCollation() {
        String mysql = "CREATE TABLE users (name VARCHAR(100) CHARACTER SET utf8 COLLATE utf8_general_ci)";
        String result = converter.convert(mysql);
        assertFalse(result.contains("CHARACTER SET"));
        assertFalse(result.contains("COLLATE"));
    }

    @Test
    void testRemoveDefaultCharset() {
        String mysql = "CREATE TABLE users (id INT) DEFAULT CHARSET=utf8mb4";
        String result = converter.convert(mysql);
        assertFalse(result.contains("DEFAULT CHARSET"));
    }

    @Test
    void testConvertNowFunction() {
        String mysql = "INSERT INTO logs (created_at) VALUES (NOW())";
        String expected = "INSERT INTO logs (created_at) VALUES (CURRENT_TIMESTAMP)";
        assertEquals(expected, converter.convert(mysql));
    }

    @Test
    void testConvertCurDateFunction() {
        String mysql = "SELECT * FROM events WHERE event_date = CURDATE()";
        String expected = "SELECT * FROM events WHERE event_date = CURRENT_DATE";
        assertEquals(expected, converter.convert(mysql));
    }

    @Test
    void testConvertCurTimeFunction() {
        String mysql = "SELECT CURTIME() as current_time";
        String expected = "SELECT CURRENT_TIME as current_time";
        assertEquals(expected, converter.convert(mysql));
    }

    @Test
    void testConvertIfNull() {
        String mysql = "SELECT IFNULL(name, 'N/A') FROM users";
        String expected = "SELECT COALESCE(name, 'N/A') FROM users";
        assertEquals(expected, converter.convert(mysql));
    }

    @Test
    void testConvertLimitClause() {
        String mysql = "SELECT * FROM users LIMIT 10, 20";
        String expected = "SELECT * FROM users LIMIT 20 OFFSET 10";
        assertEquals(expected, converter.convert(mysql));
    }

    @Test
    void testRemoveUnsigned() {
        String mysql = "CREATE TABLE users (age INT UNSIGNED)";
        String result = converter.convert(mysql);
        assertFalse(result.contains("UNSIGNED"));
    }

    @Test
    void testRemoveZeroFill() {
        String mysql = "CREATE TABLE codes (code INT ZEROFILL)";
        String result = converter.convert(mysql);
        assertFalse(result.contains("ZEROFILL"));
    }

    @Test
    void testConvertEnum() {
        String mysql = "CREATE TABLE users (status ENUM('active', 'inactive'))";
        String result = converter.convert(mysql);
        assertTrue(result.contains("VARCHAR") || result.contains("CHECK"));
    }

    @Test
    void testRemoveIndexHints() {
        String mysql = "SELECT * FROM users USE INDEX (idx_name) WHERE name = 'John'";
        String result = converter.convert(mysql);
        assertFalse(result.contains("USE INDEX"));
    }

    @Test
    void testComplexCreateTable() {
        String mysql = "CREATE TABLE `users` (" +
                "`id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY," +
                "`name` VARCHAR(100) NOT NULL," +
                "`email` VARCHAR(255) UNIQUE," +
                "`is_active` TINYINT(1) DEFAULT 0," +
                "`created_at` DATETIME DEFAULT NOW()," +
                "`updated_at` TIMESTAMP" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
        
        String result = converter.convert(mysql);
        
        assertTrue(result.contains("BIGSERIAL"));
        assertTrue(result.contains("\"users\""));
        assertTrue(result.contains("BOOLEAN"));
        assertTrue(result.contains("TIMESTAMP"));
        assertFalse(result.contains("ENGINE"));
        assertFalse(result.contains("CHARSET"));
        assertFalse(result.contains("COLLATE"));
    }

    @Test
    void testComplexSelect() {
        String mysql = "SELECT `u`.`id`, `u`.`name`, IFNULL(`u`.`email`, 'no-email') AS email " +
                "FROM `users` `u` " +
                "WHERE `u`.`created_at` >= CURDATE() " +
                "ORDER BY `u`.`name` " +
                "LIMIT 5, 10";
        
        String result = converter.convert(mysql);
        
        assertTrue(result.contains("COALESCE"));
        assertTrue(result.contains("CURRENT_DATE"));
        assertTrue(result.contains("LIMIT 10 OFFSET 5"));
        assertFalse(result.contains("`"));
        assertTrue(result.contains("\""));
    }

    @Test
    void testNullInput() {
        assertNull(converter.convert(null));
    }

    @Test
    void testEmptyInput() {
        assertEquals("", converter.convert(""));
        assertEquals("   ", converter.convert("   "));
    }

    @Test
    void testBatchConvert() {
        List<String> mysqlStatements = Arrays.asList(
                "CREATE TABLE users (id INT AUTO_INCREMENT PRIMARY KEY)",
                "SELECT * FROM `users` WHERE created_at >= NOW()",
                "SELECT * FROM orders LIMIT 10, 20"
        );
        
        List<String> results = converter.convertBatch(mysqlStatements);
        
        assertEquals(3, results.size());
        assertTrue(results.get(0).contains("SERIAL"));
        assertTrue(results.get(1).contains("CURRENT_TIMESTAMP"));
        assertTrue(results.get(2).contains("LIMIT 20 OFFSET 10"));
    }

    @Test
    void testInsertStatement() {
        String mysql = "INSERT INTO `logs` (`message`, `created_at`) VALUES ('test', NOW())";
        String result = converter.convert(mysql);
        
        assertTrue(result.contains("\"logs\""));
        assertTrue(result.contains("CURRENT_TIMESTAMP"));
    }

    @Test
    void testUpdateStatement() {
        String mysql = "UPDATE `users` SET `updated_at` = NOW() WHERE `id` = 1";
        String result = converter.convert(mysql);
        
        assertTrue(result.contains("\"users\""));
        assertTrue(result.contains("CURRENT_TIMESTAMP"));
    }

    @Test
    void testDeleteStatement() {
        String mysql = "DELETE FROM `users` WHERE `created_at` < CURDATE()";
        String result = converter.convert(mysql);
        
        assertTrue(result.contains("\"users\""));
        assertTrue(result.contains("CURRENT_DATE"));
    }

    @Test
    void testAutoIncrementWithStartValue() {
        String mysql = "CREATE TABLE users (id INT AUTO_INCREMENT PRIMARY KEY) AUTO_INCREMENT=1000";
        String result = converter.convert(mysql);
        
        assertTrue(result.contains("SERIAL"));
        assertFalse(result.contains("AUTO_INCREMENT="));
    }

    @Test
    void testMultipleDataTypes() {
        String mysql = "CREATE TABLE test (" +
                "col1 TINYINT, " +
                "col2 SMALLINT, " +
                "col3 INT, " +
                "col4 BIGINT, " +
                "col5 DOUBLE, " +
                "col6 DATETIME, " +
                "col7 BLOB, " +
                "col8 TEXT" +
                ")";
        
        String result = converter.convert(mysql);
        
        assertTrue(result.contains("SMALLINT"));
        assertTrue(result.contains("DOUBLE PRECISION"));
        assertTrue(result.contains("TIMESTAMP"));
        assertTrue(result.contains("BYTEA"));
        assertTrue(result.contains("TEXT"));
    }

    @Test
    void testCaseInsensitivity() {
        String mysql = "create table users (id int auto_increment primary key, name varchar(100))";
        String result = converter.convert(mysql);
        
        assertTrue(result.toLowerCase().contains("serial"));
    }

    @Test
    void testConvertUnixTimestamp() {
        String mysql = "SELECT UNIX_TIMESTAMP() as timestamp";
        String result = converter.convert(mysql);
        
        assertTrue(result.contains("EXTRACT(EPOCH FROM CURRENT_TIMESTAMP)"));
    }
}
