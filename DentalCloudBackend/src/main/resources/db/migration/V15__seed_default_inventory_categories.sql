-- Categorías iniciales por defecto para el módulo de inventario clínico.
-- La verificación por nombre evita duplicados si ya existen registros.

INSERT INTO inventory_categories (id, name)
SELECT CAST('20000000-0000-0000-0000-000000000001' AS UUID), 'Insumos Clínicos'
WHERE NOT EXISTS (
    SELECT 1 FROM inventory_categories WHERE LOWER(name) = LOWER('Insumos Clínicos')
);

INSERT INTO inventory_categories (id, name)
SELECT CAST('20000000-0000-0000-0000-000000000002' AS UUID), 'Anestésicos y Quirúrgicos'
WHERE NOT EXISTS (
    SELECT 1 FROM inventory_categories WHERE LOWER(name) = LOWER('Anestésicos y Quirúrgicos')
);

INSERT INTO inventory_categories (id, name)
SELECT CAST('20000000-0000-0000-0000-000000000003' AS UUID), 'Material de Restauración'
WHERE NOT EXISTS (
    SELECT 1 FROM inventory_categories WHERE LOWER(name) = LOWER('Material de Restauración')
);

INSERT INTO inventory_categories (id, name)
SELECT CAST('20000000-0000-0000-0000-000000000004' AS UUID), 'Protección y Esterilización'
WHERE NOT EXISTS (
    SELECT 1 FROM inventory_categories WHERE LOWER(name) = LOWER('Protección y Esterilización')
);

INSERT INTO inventory_categories (id, name)
SELECT CAST('20000000-0000-0000-0000-000000000005' AS UUID), 'Ortodoncia e Implantes'
WHERE NOT EXISTS (
    SELECT 1 FROM inventory_categories WHERE LOWER(name) = LOWER('Ortodoncia e Implantes')
);
