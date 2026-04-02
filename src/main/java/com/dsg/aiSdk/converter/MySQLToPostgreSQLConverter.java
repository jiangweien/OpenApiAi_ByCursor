package com.dsg.aiSdk.converter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * MySQL to PostgreSQL SQL converter
 * Converts common MySQL syntax to PostgreSQL-compatible syntax
 */
public class MySQLToPostgreSQLConverter {

    /**
     * Convert MySQL SQL to PostgreSQL SQL
     * 
     * @param mysqlSql MySQL SQL statement
     * @return PostgreSQL-compatible SQL statement
     */
    public String convert(String mysqlSql) {
        if (mysqlSql == null || mysqlSql.trim().isEmpty()) {
            return mysqlSql;
        }

        String result = mysqlSql;

        result = convertAutoIncrement(result);
        result = convertBackticks(result);
        result = convertDataTypes(result);
        result = convertEngineClause(result);
        result = convertCharsetCollation(result);
        result = convertDateTimeFunctions(result);
        result = convertStringFunctions(result);
        result = convertLimitClause(result);
        result = convertBooleanValues(result);
        result = convertCommentSyntax(result);
        result = convertIfExists(result);
        result = convertUnsigned(result);
        result = convertZeroFill(result);
        result = convertEnumSet(result);
        result = convertIndexHints(result);

        return result;
    }

    /**
     * Convert AUTO_INCREMENT to SERIAL or GENERATED
     */
    private String convertAutoIncrement(String sql) {
        sql = Pattern.compile("\\bBIGINT\\s+UNSIGNED\\s+AUTO_INCREMENT\\b", Pattern.CASE_INSENSITIVE)
                .matcher(sql).replaceAll("BIGSERIAL");
        sql = Pattern.compile("\\bINT\\s+UNSIGNED\\s+AUTO_INCREMENT\\b", Pattern.CASE_INSENSITIVE)
                .matcher(sql).replaceAll("SERIAL");
        sql = Pattern.compile("\\bSMALLINT\\s+UNSIGNED\\s+AUTO_INCREMENT\\b", Pattern.CASE_INSENSITIVE)
                .matcher(sql).replaceAll("SMALLSERIAL");
        
        sql = Pattern.compile("\\bINT\\s+AUTO_INCREMENT\\b", Pattern.CASE_INSENSITIVE)
                .matcher(sql).replaceAll("SERIAL");
        sql = Pattern.compile("\\bBIGINT\\s+AUTO_INCREMENT\\b", Pattern.CASE_INSENSITIVE)
                .matcher(sql).replaceAll("BIGSERIAL");
        sql = Pattern.compile("\\bSMALLINT\\s+AUTO_INCREMENT\\b", Pattern.CASE_INSENSITIVE)
                .matcher(sql).replaceAll("SMALLSERIAL");
        
        sql = Pattern.compile("\\bAUTO_INCREMENT\\s*=\\s*\\d+", Pattern.CASE_INSENSITIVE)
                .matcher(sql).replaceAll("");
        
        return sql;
    }

    /**
     * Convert MySQL backticks to PostgreSQL double quotes
     */
    private String convertBackticks(String sql) {
        return sql.replace("`", "\"");
    }

    /**
     * Convert MySQL data types to PostgreSQL equivalents
     */
    private String convertDataTypes(String sql) {
        sql = Pattern.compile("\\bTINYINT\\s*\\(1\\)", Pattern.CASE_INSENSITIVE)
                .matcher(sql).replaceAll("BOOLEAN");
        sql = Pattern.compile("\\bTINYINT", Pattern.CASE_INSENSITIVE)
                .matcher(sql).replaceAll("SMALLINT");
        sql = Pattern.compile("\\bDOUBLE\\b", Pattern.CASE_INSENSITIVE)
                .matcher(sql).replaceAll("DOUBLE PRECISION");
        sql = Pattern.compile("\\bDATETIME\\b", Pattern.CASE_INSENSITIVE)
                .matcher(sql).replaceAll("TIMESTAMP");
        sql = Pattern.compile("\\bTEXT\\b", Pattern.CASE_INSENSITIVE)
                .matcher(sql).replaceAll("TEXT");
        sql = Pattern.compile("\\bBLOB\\b", Pattern.CASE_INSENSITIVE)
                .matcher(sql).replaceAll("BYTEA");
        sql = Pattern.compile("\\bMEDIUMBLOB\\b", Pattern.CASE_INSENSITIVE)
                .matcher(sql).replaceAll("BYTEA");
        sql = Pattern.compile("\\bLONGBLOB\\b", Pattern.CASE_INSENSITIVE)
                .matcher(sql).replaceAll("BYTEA");
        sql = Pattern.compile("\\bMEDIUMTEXT\\b", Pattern.CASE_INSENSITIVE)
                .matcher(sql).replaceAll("TEXT");
        sql = Pattern.compile("\\bLONGTEXT\\b", Pattern.CASE_INSENSITIVE)
                .matcher(sql).replaceAll("TEXT");
        
        return sql;
    }

    /**
     * Remove MySQL ENGINE clause
     */
    private String convertEngineClause(String sql) {
        return Pattern.compile("\\bENGINE\\s*=\\s*\\w+", Pattern.CASE_INSENSITIVE)
                .matcher(sql).replaceAll("");
    }

    /**
     * Remove MySQL CHARACTER SET and COLLATE clauses
     */
    private String convertCharsetCollation(String sql) {
        sql = Pattern.compile("\\bCHARACTER\\s+SET\\s+\\w+", Pattern.CASE_INSENSITIVE)
                .matcher(sql).replaceAll("");
        sql = Pattern.compile("\\bCHARSET\\s*=\\s*\\w+", Pattern.CASE_INSENSITIVE)
                .matcher(sql).replaceAll("");
        sql = Pattern.compile("\\bCHARSET\\s+\\w+", Pattern.CASE_INSENSITIVE)
                .matcher(sql).replaceAll("");
        sql = Pattern.compile("\\bCOLLATE\\s*=\\s*\\w+", Pattern.CASE_INSENSITIVE)
                .matcher(sql).replaceAll("");
        sql = Pattern.compile("\\bCOLLATE\\s+\\w+", Pattern.CASE_INSENSITIVE)
                .matcher(sql).replaceAll("");
        sql = Pattern.compile("\\bDEFAULT\\s+CHARSET\\s*=\\s*\\w+", Pattern.CASE_INSENSITIVE)
                .matcher(sql).replaceAll("");
        
        return sql;
    }

    /**
     * Convert MySQL datetime functions to PostgreSQL
     */
    private String convertDateTimeFunctions(String sql) {
        sql = Pattern.compile("\\bNOW\\s*\\(\\s*\\)", Pattern.CASE_INSENSITIVE)
                .matcher(sql).replaceAll("CURRENT_TIMESTAMP");
        sql = Pattern.compile("\\bCURDATE\\s*\\(\\s*\\)", Pattern.CASE_INSENSITIVE)
                .matcher(sql).replaceAll("CURRENT_DATE");
        sql = Pattern.compile("\\bCURTIME\\s*\\(\\s*\\)", Pattern.CASE_INSENSITIVE)
                .matcher(sql).replaceAll("CURRENT_TIME");
        sql = Pattern.compile("\\bUNIX_TIMESTAMP\\s*\\(\\s*\\)", Pattern.CASE_INSENSITIVE)
                .matcher(sql).replaceAll("EXTRACT(EPOCH FROM CURRENT_TIMESTAMP)");
        
        return sql;
    }

    /**
     * Convert MySQL string functions to PostgreSQL
     */
    private String convertStringFunctions(String sql) {
        sql = Pattern.compile("\\bCONCAT\\s*\\(", Pattern.CASE_INSENSITIVE)
                .matcher(sql).replaceAll("CONCAT(");
        
        Pattern ifnullPattern = Pattern.compile("\\bIFNULL\\s*\\(([^,]+),([^)]+)\\)", Pattern.CASE_INSENSITIVE);
        Matcher ifnullMatcher = ifnullPattern.matcher(sql);
        StringBuffer sb = new StringBuffer();
        while (ifnullMatcher.find()) {
            String replacement = "COALESCE(" + ifnullMatcher.group(1) + "," + ifnullMatcher.group(2) + ")";
            ifnullMatcher.appendReplacement(sb, replacement);
        }
        ifnullMatcher.appendTail(sb);
        sql = sb.toString();
        
        return sql;
    }

    /**
     * Convert MySQL LIMIT offset, count to PostgreSQL LIMIT count OFFSET offset
     */
    private String convertLimitClause(String sql) {
        Pattern limitPattern = Pattern.compile("\\bLIMIT\\s+(\\d+)\\s*,\\s*(\\d+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = limitPattern.matcher(sql);
        if (matcher.find()) {
            String offset = matcher.group(1);
            String count = matcher.group(2);
            sql = matcher.replaceAll("LIMIT " + count + " OFFSET " + offset);
        }
        return sql;
    }

    /**
     * Convert MySQL boolean values
     */
    private String convertBooleanValues(String sql) {
        sql = sql.replaceAll("\\b(['\"])true\\1", "TRUE");
        sql = sql.replaceAll("\\b(['\"])false\\1", "FALSE");
        return sql;
    }

    /**
     * Convert MySQL COMMENT syntax to PostgreSQL
     */
    private String convertCommentSyntax(String sql) {
        Pattern commentPattern = Pattern.compile("COMMENT\\s+'([^']*)'", Pattern.CASE_INSENSITIVE);
        Matcher matcher = commentPattern.matcher(sql);
        
        List<String> comments = new ArrayList<>();
        while (matcher.find()) {
            comments.add(matcher.group(1));
        }
        
        sql = matcher.replaceAll("");
        
        return sql;
    }

    /**
     * Convert IF EXISTS/IF NOT EXISTS
     */
    private String convertIfExists(String sql) {
        return sql;
    }

    /**
     * Remove UNSIGNED keyword (PostgreSQL doesn't support it)
     */
    private String convertUnsigned(String sql) {
        return Pattern.compile("\\bUNSIGNED\\b", Pattern.CASE_INSENSITIVE)
                .matcher(sql).replaceAll("");
    }

    /**
     * Remove ZEROFILL keyword (PostgreSQL doesn't support it)
     */
    private String convertZeroFill(String sql) {
        return Pattern.compile("\\bZEROFILL\\b", Pattern.CASE_INSENSITIVE)
                .matcher(sql).replaceAll("");
    }

    /**
     * Convert ENUM and SET types
     */
    private String convertEnumSet(String sql) {
        Pattern enumPattern = Pattern.compile("\\bENUM\\s*\\(([^)]+)\\)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = enumPattern.matcher(sql);
        sql = matcher.replaceAll("VARCHAR(255) CHECK (value IN ($1))");
        
        sql = Pattern.compile("\\bSET\\s*\\(([^)]+)\\)", Pattern.CASE_INSENSITIVE)
                .matcher(sql).replaceAll("TEXT[]");
        
        return sql;
    }

    /**
     * Remove MySQL index hints (USE INDEX, FORCE INDEX, IGNORE INDEX)
     */
    private String convertIndexHints(String sql) {
        sql = Pattern.compile("\\bUSE\\s+INDEX\\s*\\([^)]*\\)", Pattern.CASE_INSENSITIVE)
                .matcher(sql).replaceAll("");
        sql = Pattern.compile("\\bFORCE\\s+INDEX\\s*\\([^)]*\\)", Pattern.CASE_INSENSITIVE)
                .matcher(sql).replaceAll("");
        sql = Pattern.compile("\\bIGNORE\\s+INDEX\\s*\\([^)]*\\)", Pattern.CASE_INSENSITIVE)
                .matcher(sql).replaceAll("");
        
        return sql;
    }

    /**
     * Batch convert multiple SQL statements
     * 
     * @param mysqlStatements List of MySQL SQL statements
     * @return List of PostgreSQL-compatible SQL statements
     */
    public List<String> convertBatch(List<String> mysqlStatements) {
        List<String> result = new ArrayList<>();
        for (String statement : mysqlStatements) {
            result.add(convert(statement));
        }
        return result;
    }
}
