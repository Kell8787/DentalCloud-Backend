-- Controles de stock y trazabilidad de movimientos.
-- Los valores por defecto permiten conservar productos creados antes de V2.

ALTER TABLE inventory_products
    ADD COLUMN minimum_stock INTEGER NOT NULL DEFAULT 0;

ALTER TABLE inventory_products
    ADD COLUMN unit VARCHAR(32) NOT NULL DEFAULT 'unidad';

ALTER TABLE inventory_products
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

ALTER TABLE inventory_products
    ADD CONSTRAINT chk_inventory_products_quantity_non_negative
        CHECK (quantity >= 0);

ALTER TABLE inventory_products
    ADD CONSTRAINT chk_inventory_products_minimum_stock_non_negative
        CHECK (minimum_stock >= 0);

ALTER TABLE inventory_products
    ADD CONSTRAINT chk_inventory_products_version_non_negative
        CHECK (version >= 0);

CREATE TABLE stock_movements (
    id UUID NOT NULL,
    product_id UUID NOT NULL,
    movement_type VARCHAR(32) NOT NULL,
    quantity INTEGER NOT NULL,
    reason TEXT NOT NULL,
    actor_id UUID NOT NULL,
    occurred_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_stock_movements PRIMARY KEY (id),
    CONSTRAINT fk_stock_movements_product
        FOREIGN KEY (product_id) REFERENCES inventory_products (id),
    CONSTRAINT fk_stock_movements_actor
        FOREIGN KEY (actor_id) REFERENCES dental_users (id),
    CONSTRAINT chk_stock_movements_type
        CHECK (movement_type IN ('ENTRADA', 'SALIDA', 'AJUSTE')),
    CONSTRAINT chk_stock_movements_quantity_positive
        CHECK (quantity > 0)
);

CREATE INDEX idx_stock_movements_product_occurred_at
    ON stock_movements (product_id, occurred_at);

CREATE INDEX idx_stock_movements_actor
    ON stock_movements (actor_id);
