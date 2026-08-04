-- Baseline del esquema existente antes de adoptar Flyway.
-- Las futuras modificaciones de dominio deben ir en V2, V3, etc.

CREATE TABLE IF NOT EXISTS dental_users (
    id UUID NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    second_name VARCHAR(255),
    last_name VARCHAR(255) NOT NULL,
    second_last_name VARCHAR(255),
    direccion VARCHAR(255) NOT NULL,
    genero VARCHAR(255) NOT NULL,
    dui VARCHAR(10) NOT NULL,
    birth_date DATE NOT NULL,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    role VARCHAR(255),
    CONSTRAINT pk_dental_users PRIMARY KEY (id),
    CONSTRAINT uk_dental_users_dui UNIQUE (dui),
    CONSTRAINT uk_dental_users_email UNIQUE (email),
    CONSTRAINT uk_dental_users_phone UNIQUE (phone_number)
);

CREATE TABLE IF NOT EXISTS dentists (
    id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    user_id UUID,
    CONSTRAINT pk_dentists PRIMARY KEY (id),
    CONSTRAINT uk_dentists_user UNIQUE (user_id),
    CONSTRAINT fk_dentists_user FOREIGN KEY (user_id) REFERENCES dental_users (id)
);

CREATE TABLE IF NOT EXISTS contacto_emergencia (
    id UUID NOT NULL,
    user_id UUID NOT NULL,
    nombre_completo VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    parentesco VARCHAR(255) NOT NULL,
    CONSTRAINT pk_contacto_emergencia PRIMARY KEY (id),
    CONSTRAINT uk_contacto_emergencia_user UNIQUE (user_id),
    CONSTRAINT fk_contacto_emergencia_user FOREIGN KEY (user_id) REFERENCES dental_users (id)
);

CREATE TABLE IF NOT EXISTS informacion_medica (
    id UUID NOT NULL,
    user_id UUID NOT NULL,
    antecedentes_medicos TEXT,
    CONSTRAINT pk_informacion_medica PRIMARY KEY (id),
    CONSTRAINT uk_informacion_medica_user UNIQUE (user_id),
    CONSTRAINT fk_informacion_medica_user FOREIGN KEY (user_id) REFERENCES dental_users (id)
);

CREATE TABLE IF NOT EXISTS user_alergias (
    user_id UUID NOT NULL,
    alergia VARCHAR(255),
    CONSTRAINT fk_user_alergias_owner FOREIGN KEY (user_id) REFERENCES informacion_medica (id)
);

CREATE TABLE IF NOT EXISTS user_medicamentos (
    user_id UUID NOT NULL,
    medicamentos VARCHAR(255),
    CONSTRAINT fk_user_medicamentos_owner FOREIGN KEY (user_id) REFERENCES informacion_medica (id)
);

CREATE TABLE IF NOT EXISTS tratamientos (
    id UUID NOT NULL,
    nombre VARCHAR(150) NOT NULL,
    descripcion VARCHAR(255),
    duracion_minutos INTEGER NOT NULL,
    precio NUMERIC(38, 2) NOT NULL,
    CONSTRAINT pk_tratamientos PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS citas (
    id UUID NOT NULL,
    user_id UUID NOT NULL,
    dentista_id UUID NOT NULL,
    tratamiento_id UUID NOT NULL,
    fecha_cita VARCHAR(255) NOT NULL,
    motivo TEXT NOT NULL,
    hora TIMESTAMP(6) NOT NULL,
    hora_fin TIMESTAMP(6) NOT NULL,
    motivo_cancelacion VARCHAR(255),
    estado_cita VARCHAR(255) NOT NULL,
    CONSTRAINT pk_citas PRIMARY KEY (id),
    CONSTRAINT fk_citas_user FOREIGN KEY (user_id) REFERENCES dental_users (id),
    CONSTRAINT fk_citas_dentista FOREIGN KEY (dentista_id) REFERENCES dentists (id),
    CONSTRAINT fk_citas_tratamiento FOREIGN KEY (tratamiento_id) REFERENCES tratamientos (id)
);

CREATE TABLE IF NOT EXISTS inventory_categories (
    id UUID NOT NULL,
    name TEXT NOT NULL,
    CONSTRAINT pk_inventory_categories PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS inventory_products (
    id UUID NOT NULL,
    name TEXT NOT NULL,
    description TEXT NOT NULL,
    purchase_price NUMERIC(38, 2) NOT NULL,
    sale_price NUMERIC(38, 2) NOT NULL,
    quantity INTEGER NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    category_id UUID NOT NULL,
    CONSTRAINT pk_inventory_products PRIMARY KEY (id),
    CONSTRAINT fk_inventory_products_category FOREIGN KEY (category_id) REFERENCES inventory_categories (id)
);

CREATE TABLE IF NOT EXISTS dental_products (
    id VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    cantidad INTEGER,
    marca VARCHAR(255) NOT NULL,
    vendedor VARCHAR(255) NOT NULL,
    vendedor_phone_number VARCHAR(20) NOT NULL,
    re_stock_date DATE,
    CONSTRAINT pk_dental_products PRIMARY KEY (id),
    CONSTRAINT uk_dental_products_vendor_phone UNIQUE (vendedor_phone_number)
);

CREATE INDEX IF NOT EXISTS idx_citas_fecha_cita ON citas (fecha_cita);
CREATE INDEX IF NOT EXISTS idx_citas_dentista_fecha ON citas (dentista_id, fecha_cita);
CREATE INDEX IF NOT EXISTS idx_citas_estado ON citas (estado_cita);
CREATE INDEX IF NOT EXISTS idx_inventory_products_category ON inventory_products (category_id);
