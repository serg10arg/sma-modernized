-- Dato de ejemplo. Usa el mismo id (org-001) que referencia el licensing-service.
INSERT INTO organizations (organization_id, name, contact_name, contact_email, contact_phone)
VALUES ('org-001', 'Organización de prueba', 'Ana Contacto', 'ana@ejemplo.com', '600000000')
ON CONFLICT (organization_id) DO NOTHING;
