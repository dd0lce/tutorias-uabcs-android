-- Desde cuándo espera respuesta del tutor (hora de La Paz). Vence a las 24 h o al llegar su horario.
ALTER TABLE solicitudes ADD COLUMN pendiente_desde TIMESTAMP;
UPDATE solicitudes SET pendiente_desde = creada_en AT TIME ZONE 'America/Mazatlan' WHERE estado = 'PENDIENTE';

-- Archivo adjunto de la solicitud (máximo 10 MB). El nombre queda en solicitudes.archivo.
CREATE TABLE archivos (
    solicitud_id VARCHAR(40) PRIMARY KEY REFERENCES solicitudes (id) ON DELETE CASCADE,
    datos BYTEA NOT NULL
);
