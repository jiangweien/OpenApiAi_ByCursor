-- MySQL 视图转换为 GaussDB(PG) 视图
-- 原 MySQL 视图：monthly_transaction_summary

CREATE OR REPLACE VIEW monthly_transaction_summary AS
SELECT 
    EXTRACT(YEAR FROM transaction_date) AS year,
    EXTRACT(MONTH FROM transaction_date) AS month,
    COUNT(*) AS total_transactions18,
    SUM(amount) AS total_amount
FROM transactions18
GROUP BY year, month
ORDER BY year, month;

-- 说明：
-- 1. GaussDB(PG) 不支持 ALGORITHM、DEFINER、SQL SECURITY 等 MySQL 特有语法
-- 2. 使用 CREATE OR REPLACE VIEW 替代 CREATE VIEW，方便更新
-- 3. COUNT(0) 改为 COUNT(*)，这是 PostgreSQL 标准写法
-- 4. EXTRACT 函数在 PostgreSQL 中支持，语法相同
-- 5. GROUP BY 可以直接使用别名 year, month（PostgreSQL 支持）
-- 6. ORDER BY 在视图定义中保留（PostgreSQL 允许）
