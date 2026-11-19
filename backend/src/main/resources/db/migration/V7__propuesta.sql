-- Horario que propone el tutor (hora de La Paz). Mientras no sea NULL, la solicitud espera la respuesta del estudiante.
ALTER TABLE solicitudes ADD COLUMN propuesta TIMESTAMP;
