DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_database WHERE datname = 'iks_db') THEN
        CREATE DATABASE iks_db;
    END IF;
END $$;