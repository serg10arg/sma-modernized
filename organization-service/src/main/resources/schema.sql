-- Crea la tabla de organizaciones si no existe
CREATE TABLE IF NOT EXISTS organizations (
    organization_id VARCHAR(255) NOT NULL,
    name            VARCHAR(255) NOT NULL,
    contact_name    VARCHAR(255),
    contact_email   VARCHAR(255),
    contact_phone   VARCHAR(255),
    CONSTRAINT pk_organizations PRIMARY KEY (organization_id)
);
