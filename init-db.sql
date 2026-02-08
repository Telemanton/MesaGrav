-- Crear el enum de roles
CREATE TYPE user_role AS ENUM ('KEYUSER', 'ADMIN', 'USER');

-- Crear la tabla de usuarios
CREATE TABLE app_user (
    id           BIGSERIAL PRIMARY KEY,
    username     VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255)      NOT NULL,
    name         VARCHAR(100)       NOT NULL,
    surname      VARCHAR(100)       NOT NULL,
    email        VARCHAR(100)       NOT NULL,
    role         user_role          NOT NULL,
    created_at   TIMESTAMP          NOT NULL DEFAULT NOW()
);

-- Usuarios de prueba (password = "password" hasheada con BCrypt)
INSERT INTO app_user (username, password_hash, name, surname, email, role) VALUES 
    ('admin', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Admin', 'Root', 'admin@ujaen.es', 'ADMIN'),
    ('keyuser1', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Key', 'User', 'keyuser@ujaen.es', 'KEYUSER'),
    ('user1', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Juan', 'Pérez', 'user@ujaen.es', 'USER');
