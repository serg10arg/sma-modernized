-- Datos de ejemplo para pruebas (no se duplican si ya existen)
INSERT INTO licenses (license_id, organization_id, description, product_name, license_type, comment)
VALUES ('lic-001', 'org-001', 'Licencia de prueba', 'O-stock', 'full', 'Registro de ejemplo')
ON CONFLICT (license_id) DO NOTHING;
