-- Crea la tabla de licencias si no existe
CREATE TABLE IF NOT EXISTS licenses (
    license_id      VARCHAR(255) NOT NULL,
    organization_id VARCHAR(255) NOT NULL,
    description     VARCHAR(255),
    product_name    VARCHAR(255) NOT NULL,
    license_type    VARCHAR(255) NOT NULL,
    comment         VARCHAR(255),
    CONSTRAINT pk_licenses PRIMARY KEY (license_id)
);
