-- ============================================================================
-- DECISIONHUB DATABASE INITIALIZATION SCRIPT (MySQL 8.4 LTS)
-- ============================================================================

-- Create database if it does not exist with explicit character set and collation
CREATE DATABASE IF NOT EXISTS decisionhub
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_0900_ai_ci;

-- Switch context
USE decisionhub;

-- ----------------------------------------------------------------------------
-- Recommended MySQL 8.4 Enterprise InnoDB Tuning Settings (Reference)
-- Note: These variables are set in my.cnf or parameter groups in RDS/Cloud.
-- ----------------------------------------------------------------------------
/*
SET GLOBAL innodb_file_per_table = ON;
SET GLOBAL innodb_strict_mode = ON;
SET GLOBAL innodb_print_all_deadlocks = ON;
SET GLOBAL innodb_lock_wait_timeout = 50;
*/

-- ----------------------------------------------------------------------------
-- Create dedicated application user and grant permissions
-- ----------------------------------------------------------------------------
CREATE USER IF NOT EXISTS 'decisionhub_app'@'%' IDENTIFIED BY 'dh_dev_sec_pwd_2026';

-- Grant DDL & DML permissions on application schema
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, DROP, REFERENCES, INDEX, ALTER, 
      CREATE VIEW, SHOW VIEW, TRIGGER 
ON decisionhub.* 
TO 'decisionhub_app'@'%';

-- Flush privileges to apply
FLUSH PRIVILEGES;

-- Verify database configuration
SELECT SCHEMA_NAME, DEFAULT_CHARACTER_SET_NAME, DEFAULT_COLLATION_NAME 
FROM information_schema.SCHEMATA 
WHERE SCHEMA_NAME = 'decisionhub';
