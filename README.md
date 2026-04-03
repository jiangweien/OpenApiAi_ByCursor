# OpenApiAi_ByCursor

## SQL 转换示例

### MySQL 到 GaussDB(PG) 视图转换

查看 `src/main/resources/sql/monthly_transaction_summary_gaussdb.sql` 了解 MySQL 视图如何转换为 GaussDB(PostgreSQL) 视图。

主要转换要点：
- 移除 MySQL 特有的 `ALGORITHM`、`DEFINER`、`SQL SECURITY` 语法
- 使用 `CREATE OR REPLACE VIEW` 语法
- `COUNT(0)` 改为 `COUNT(*)`
- 保留 PostgreSQL 兼容的 `EXTRACT`、`GROUP BY`、`ORDER BY` 语法
