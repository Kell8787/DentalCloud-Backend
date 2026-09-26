-- Catálogo inicial para que el paciente pueda elegir el motivo de su solicitud.
-- La comparación por nombre evita duplicar tratamientos en bases existentes.

INSERT INTO tratamientos (id, nombre, descripcion, duracion_minutos, precio, active)
SELECT CAST('10000000-0000-0000-0000-000000000001' AS UUID),
       'Blanqueamiento dental',
       'Procedimiento estético para aclarar el tono de los dientes.',
       60,
       0.00,
       TRUE
WHERE NOT EXISTS (
    SELECT 1 FROM tratamientos WHERE LOWER(nombre) = LOWER('Blanqueamiento dental')
);

INSERT INTO tratamientos (id, nombre, descripcion, duracion_minutos, precio, active)
SELECT CAST('10000000-0000-0000-0000-000000000002' AS UUID),
       'Limpieza dental',
       'Limpieza profesional y eliminación de placa y sarro.',
       45,
       0.00,
       TRUE
WHERE NOT EXISTS (
    SELECT 1 FROM tratamientos WHERE LOWER(nombre) = LOWER('Limpieza dental')
);

INSERT INTO tratamientos (id, nombre, descripcion, duracion_minutos, precio, active)
SELECT CAST('10000000-0000-0000-0000-000000000003' AS UUID),
       'Extracciones',
       'Evaluación y extracción dental indicada por el doctor.',
       45,
       0.00,
       TRUE
WHERE NOT EXISTS (
    SELECT 1 FROM tratamientos WHERE LOWER(nombre) = LOWER('Extracciones')
);

INSERT INTO tratamientos (id, nombre, descripcion, duracion_minutos, precio, active)
SELECT CAST('10000000-0000-0000-0000-000000000004' AS UUID),
       'Ortodoncia',
       'Valoración y control de tratamiento ortodóntico.',
       60,
       0.00,
       TRUE
WHERE NOT EXISTS (
    SELECT 1 FROM tratamientos WHERE LOWER(nombre) = LOWER('Ortodoncia')
);

INSERT INTO tratamientos (id, nombre, descripcion, duracion_minutos, precio, active)
SELECT CAST('10000000-0000-0000-0000-000000000005' AS UUID),
       'Radiografía',
       'Toma y revisión de radiografías dentales.',
       30,
       0.00,
       TRUE
WHERE NOT EXISTS (
    SELECT 1 FROM tratamientos WHERE LOWER(nombre) = LOWER('Radiografía')
);

INSERT INTO tratamientos (id, nombre, descripcion, duracion_minutos, precio, active)
SELECT CAST('10000000-0000-0000-0000-000000000006' AS UUID),
       'Cirugía de cordales',
       'Evaluación y procedimiento quirúrgico de terceros molares.',
       120,
       0.00,
       TRUE
WHERE NOT EXISTS (
    SELECT 1 FROM tratamientos WHERE LOWER(nombre) = LOWER('Cirugía de cordales')
);

INSERT INTO tratamientos (id, nombre, descripcion, duracion_minutos, precio, active)
SELECT CAST('10000000-0000-0000-0000-000000000007' AS UUID),
       'Rellenos',
       'Restauración dental con material de relleno.',
       60,
       0.00,
       TRUE
WHERE NOT EXISTS (
    SELECT 1 FROM tratamientos WHERE LOWER(nombre) = LOWER('Rellenos')
);

INSERT INTO tratamientos (id, nombre, descripcion, duracion_minutos, precio, active)
SELECT CAST('10000000-0000-0000-0000-000000000008' AS UUID),
       'Diagnósticos generales',
       'Evaluación general de salud bucal y diagnóstico inicial.',
       45,
       0.00,
       TRUE
WHERE NOT EXISTS (
    SELECT 1 FROM tratamientos WHERE LOWER(nombre) = LOWER('Diagnósticos generales')
);
