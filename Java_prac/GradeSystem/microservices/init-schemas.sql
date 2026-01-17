-- Grade System Database Initialization Script
-- Creates separate schemas for each microservice

-- Create schemas
CREATE SCHEMA IF NOT EXISTS auth_schema;
CREATE SCHEMA IF NOT EXISTS user_schema;
CREATE SCHEMA IF NOT EXISTS academic_schema;
CREATE SCHEMA IF NOT EXISTS grade_schema;
CREATE SCHEMA IF NOT EXISTS analytics_schema;

-- Grant permissions to grade_admin
GRANT ALL PRIVILEGES ON SCHEMA auth_schema TO grade_admin;
GRANT ALL PRIVILEGES ON SCHEMA user_schema TO grade_admin;
GRANT ALL PRIVILEGES ON SCHEMA academic_schema TO grade_admin;
GRANT ALL PRIVILEGES ON SCHEMA grade_schema TO grade_admin;
GRANT ALL PRIVILEGES ON SCHEMA analytics_schema TO grade_admin;

-- Grant default privileges for future tables
ALTER DEFAULT PRIVILEGES IN SCHEMA auth_schema GRANT ALL ON TABLES TO grade_admin;
ALTER DEFAULT PRIVILEGES IN SCHEMA user_schema GRANT ALL ON TABLES TO grade_admin;
ALTER DEFAULT PRIVILEGES IN SCHEMA academic_schema GRANT ALL ON TABLES TO grade_admin;
ALTER DEFAULT PRIVILEGES IN SCHEMA grade_schema GRANT ALL ON TABLES TO grade_admin;
ALTER DEFAULT PRIVILEGES IN SCHEMA analytics_schema GRANT ALL ON TABLES TO grade_admin;

-- Grant sequence privileges
ALTER DEFAULT PRIVILEGES IN SCHEMA auth_schema GRANT ALL ON SEQUENCES TO grade_admin;
ALTER DEFAULT PRIVILEGES IN SCHEMA user_schema GRANT ALL ON SEQUENCES TO grade_admin;
ALTER DEFAULT PRIVILEGES IN SCHEMA academic_schema GRANT ALL ON SEQUENCES TO grade_admin;
ALTER DEFAULT PRIVILEGES IN SCHEMA grade_schema GRANT ALL ON SEQUENCES TO grade_admin;
ALTER DEFAULT PRIVILEGES IN SCHEMA analytics_schema GRANT ALL ON SEQUENCES TO grade_admin;
