-- Temporary admin seed script
-- Password: Admin@1234 (BCrypt $2b$ prefix, compatible with Spring BCrypt)
INSERT INTO users (name, email, password, role) 
VALUES ('Admin', 'admin@securetask.com', '$2b$10$2hfoD346kFabXEiSIQYGUe1YA26c25uV2xrRWwWVlhFQ6jyOBYlba', 'ROLE_ADMIN');
