-- Cuentas de prueba del escenario academico. No contienen contrasenas ni credenciales de acceso.
INSERT INTO users (username, email, active) VALUES
('andrea', 'andrea@email.com', TRUE),
('carlos', 'carlos@email.com', TRUE),
('laura', 'laura@email.com', TRUE),
('miguel', 'miguel@email.com', FALSE);

INSERT INTO user_profiles (first_name, last_name, birth_date, user_id)
SELECT 'Andrea', 'Prueba', CURRENT_DATE - INTERVAL '25 years' - INTERVAL '180 days', id FROM users WHERE username = 'andrea';
INSERT INTO user_profiles (first_name, last_name, birth_date, user_id)
SELECT 'Carlos', 'Prueba', CURRENT_DATE - INTERVAL '21 years' - INTERVAL '180 days', id FROM users WHERE username = 'carlos';
INSERT INTO user_profiles (first_name, last_name, birth_date, user_id)
SELECT 'Laura', 'Prueba', CURRENT_DATE - INTERVAL '17 years' - INTERVAL '180 days', id FROM users WHERE username = 'laura';
INSERT INTO user_profiles (first_name, last_name, birth_date, user_id)
SELECT 'Miguel', 'Prueba', CURRENT_DATE - INTERVAL '30 years' - INTERVAL '180 days', id FROM users WHERE username = 'miguel';
