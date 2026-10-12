CREATE TABLE department (
    id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    CONSTRAINT uk_department_name UNIQUE (name)
);
