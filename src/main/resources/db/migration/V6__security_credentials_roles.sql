-- Fase de seguridad. Los usuarios heredados no reciben contraseñas ni roles automáticamente.
CREATE TABLE user_credentials (
    user_id BIGINT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    password_hash VARCHAR(100) NOT NULL,
    password_changed_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role VARCHAR(20) NOT NULL CHECK (role IN ('USER', 'STAFF', 'ADMIN')),
    PRIMARY KEY (user_id, role)
);
CREATE INDEX idx_user_roles_role ON user_roles(role);
