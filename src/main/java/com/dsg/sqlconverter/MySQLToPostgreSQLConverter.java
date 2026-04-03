package com.dsg.sqlconverter;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MySQLToPostgreSQLConverter {
    
    private static final Map<String, String> DATA_TYPE_MAPPING = new HashMap<>();
    
    static {
        DATA_TYPE_MAPPING.put("TINYINT", "SMALLINT");
        DATA_TYPE_MAPPING.put("TINYINT(1)", "BOOLEAN");
        DATA_TYPE_MAPPING.put("INT", "INTEGER");
        DATA_TYPE_MAPPING.put("BIGINT", "BIGINT");
        DATA_TYPE_MAPPING.put("FLOAT", "REAL");
        DATA_TYPE_MAPPING.put("DOUBLE", "DOUBLE PRECISION");
        DATA_TYPE_MAPPING.put("DATETIME", "TIMESTAMP");
        DATA_TYPE_MAPPING.put("TEXT", "TEXT");
        DATA_TYPE_MAPPING.put("BLOB", "BYTEA");
        DATA_TYPE_MAPPING.put("LONGTEXT", "TEXT");
        DATA_TYPE_MAPPING.put("LONGBLOB", "BYTEA");
        DATA_TYPE_MAPPING.put("MEDIUMTEXT", "TEXT");
        DATA_TYPE_MAPPING.put("MEDIUMBLOB", "BYTEA");
        DATA_TYPE_MAPPING.put("TINYTEXT", "TEXT");
        DATA_TYPE_MAPPING.put("TINYBLOB", "BYTEA");
    }
    
    public String convert(String mysqlSql) {
        if (mysqlSql == null || mysqlSql.trim().isEmpty()) {
            return mysqlSql;
        }
        
        String result = mysqlSql;
        
        result = convertView(result);
        result = convertDataTypes(result);
        result = convertAutoIncrement(result);
        result = convertBackticks(result);
        result = convertEngine(result);
        result = convertCharset(result);
        result = convertLimit(result);
        result = convertIfNotExists(result);
        result = convertStringFunctions(result);
        result = convertDateFunctions(result);
        result = convertExtractFunction(result);
        result = convertCountFunction(result);
        
        return result;
    }
    
    private String convertDataTypes(String sql) {
        String result = sql;
        
        result = Pattern.compile("\\bTINYINT\\s*\\(\\s*1\\s*\\)", Pattern.CASE_INSENSITIVE)
                .matcher(result).replaceAll("BOOLEAN");
        
        for (Map.Entry<String, String> entry : DATA_TYPE_MAPPING.entrySet()) {
            String mysqlType = entry.getKey();
            String pgType = entry.getValue();
            
            if (mysqlType.equals("TINYINT(1)")) {
                continue;
            }
            
            Pattern pattern = Pattern.compile("\\b" + Pattern.quote(mysqlType) + "\\b", Pattern.CASE_INSENSITIVE);
            result = pattern.matcher(result).replaceAll(pgType);
        }
        
        result = Pattern.compile("VARCHAR\\((\\d+)\\)", Pattern.CASE_INSENSITIVE)
                .matcher(result).replaceAll("VARCHAR($1)");
        
        result = Pattern.compile("CHAR\\((\\d+)\\)", Pattern.CASE_INSENSITIVE)
                .matcher(result).replaceAll("CHAR($1)");
        
        result = Pattern.compile("DECIMAL\\((\\d+),\\s*(\\d+)\\)", Pattern.CASE_INSENSITIVE)
                .matcher(result).replaceAll("DECIMAL($1,$2)");
        
        return result;
    }
    
    private String convertAutoIncrement(String sql) {
        Pattern pattern = Pattern.compile("\\bAUTO_INCREMENT\\b", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(sql);
        
        if (matcher.find()) {
            sql = matcher.replaceAll("");
            sql = Pattern.compile("\\bINTEGER\\b", Pattern.CASE_INSENSITIVE)
                    .matcher(sql).replaceFirst("SERIAL");
        }
        
        return sql;
    }
    
    private String convertBackticks(String sql) {
        return sql.replace("`", "\"");
    }
    
    private String convertEngine(String sql) {
        Pattern pattern = Pattern.compile("ENGINE\\s*=\\s*\\w+", Pattern.CASE_INSENSITIVE);
        return pattern.matcher(sql).replaceAll("");
    }
    
    private String convertCharset(String sql) {
        Pattern pattern1 = Pattern.compile("DEFAULT\\s+CHARSET\\s*=\\s*\\w+", Pattern.CASE_INSENSITIVE);
        Pattern pattern2 = Pattern.compile("CHARACTER\\s+SET\\s+\\w+", Pattern.CASE_INSENSITIVE);
        Pattern pattern3 = Pattern.compile("COLLATE\\s+\\w+", Pattern.CASE_INSENSITIVE);
        
        sql = pattern1.matcher(sql).replaceAll("");
        sql = pattern2.matcher(sql).replaceAll("");
        sql = pattern3.matcher(sql).replaceAll("");
        
        return sql;
    }
    
    private String convertLimit(String sql) {
        Pattern pattern = Pattern.compile("LIMIT\\s+(\\d+)\\s*,\\s*(\\d+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(sql);
        
        if (matcher.find()) {
            String offset = matcher.group(1);
            String limit = matcher.group(2);
            return matcher.replaceAll("LIMIT " + limit + " OFFSET " + offset);
        }
        
        return sql;
    }
    
    private String convertIfNotExists(String sql) {
        return sql;
    }
    
    private String convertStringFunctions(String sql) {
        sql = Pattern.compile("CONCAT\\s*\\(", Pattern.CASE_INSENSITIVE)
                .matcher(sql).replaceAll("CONCAT(");
        
        sql = Pattern.compile("SUBSTRING\\s*\\(", Pattern.CASE_INSENSITIVE)
                .matcher(sql).replaceAll("SUBSTRING(");
        
        return sql;
    }
    
    private String convertDateFunctions(String sql) {
        Pattern nowPattern = Pattern.compile("\\bNOW\\s*\\(\\s*\\)", Pattern.CASE_INSENSITIVE);
        sql = nowPattern.matcher(sql).replaceAll("CURRENT_TIMESTAMP");
        
        Pattern curdatePattern = Pattern.compile("\\bCURDATE\\s*\\(\\s*\\)", Pattern.CASE_INSENSITIVE);
        sql = curdatePattern.matcher(sql).replaceAll("CURRENT_DATE");
        
        Pattern curtimePattern = Pattern.compile("\\bCURTIME\\s*\\(\\s*\\)", Pattern.CASE_INSENSITIVE);
        sql = curtimePattern.matcher(sql).replaceAll("CURRENT_TIME");
        
        return sql;
    }
    
    private String convertView(String sql) {
        Pattern viewPattern = Pattern.compile(
            "CREATE\\s+(?:OR\\s+REPLACE\\s+)?ALGORITHM\\s*=\\s*\\w+\\s+DEFINER\\s*=\\s*`[^`]+`@`[^`]+`\\s+SQL\\s+SECURITY\\s+\\w+\\s+VIEW",
            Pattern.CASE_INSENSITIVE
        );
        sql = viewPattern.matcher(sql).replaceAll("CREATE OR REPLACE VIEW");
        
        Pattern definerOnlyPattern = Pattern.compile(
            "CREATE\\s+(?:OR\\s+REPLACE\\s+)?DEFINER\\s*=\\s*`[^`]+`@`[^`]+`\\s+(?:SQL\\s+SECURITY\\s+\\w+\\s+)?VIEW",
            Pattern.CASE_INSENSITIVE
        );
        sql = definerOnlyPattern.matcher(sql).replaceAll("CREATE OR REPLACE VIEW");
        
        Pattern algorithmOnlyPattern = Pattern.compile(
            "CREATE\\s+(?:OR\\s+REPLACE\\s+)?ALGORITHM\\s*=\\s*\\w+\\s+VIEW",
            Pattern.CASE_INSENSITIVE
        );
        sql = algorithmOnlyPattern.matcher(sql).replaceAll("CREATE OR REPLACE VIEW");
        
        return sql;
    }
    
    private String convertExtractFunction(String sql) {
        Pattern pattern = Pattern.compile(
            "extract\\s*\\(\\s*(year|month|day|hour|minute|second)\\s+from\\s+",
            Pattern.CASE_INSENSITIVE
        );
        Matcher matcher = pattern.matcher(sql);
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String unit = matcher.group(1).toUpperCase();
            matcher.appendReplacement(result, "EXTRACT(" + unit + " FROM ");
        }
        matcher.appendTail(result);
        
        return result.toString();
    }
    
    private String convertCountFunction(String sql) {
        Pattern pattern = Pattern.compile("count\\s*\\(\\s*0\\s*\\)", Pattern.CASE_INSENSITIVE);
        return pattern.matcher(sql).replaceAll("COUNT(*)");
    }
}
