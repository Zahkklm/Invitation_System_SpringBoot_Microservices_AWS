CREATE TABLE organizations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    normalized_name VARCHAR(255) NOT NULL,
    registry_number VARCHAR(100) NOT NULL,
    contact_email VARCHAR(255) NOT NULL,
    company_size INTEGER NOT NULL,
    year_founded INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID NOT NULL,
    updated_by UUID NOT NULL,
    CONSTRAINT uk_organization_registry_number UNIQUE (registry_number)
);

CREATE INDEX idx_organization_normalized_name ON organizations(normalized_name);
CREATE INDEX idx_organization_year_founded ON organizations(year_founded);
CREATE INDEX idx_organization_company_size ON organizations(company_size);