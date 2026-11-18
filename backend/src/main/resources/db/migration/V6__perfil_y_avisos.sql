-- El tutor decide si los estudiantes ven su correo en su perfil.
ALTER TABLE usuarios ADD COLUMN mostrar_correo BOOLEAN NOT NULL DEFAULT FALSE;

-- Avisos que publica Coordinación en el Inicio de estudiantes y tutores. Se muestran hasta la fecha `hasta`.
CREATE TABLE avisos_institucionales (
    id VARCHAR(40) PRIMARY KEY,
    titulo VARCHAR(120) NOT NULL,
    detalle VARCHAR(500) NOT NULL,
    hasta DATE NOT NULL,
    publicado_en TIMESTAMP WITH TIME ZONE NOT NULL
);
