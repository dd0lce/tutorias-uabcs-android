
> Revisión completa del proyecto: 16 de noviembre de 2026, con `main` en `e21df22` (después de #18 y #19).
> El PR #21 (correcciones de la revisión de #18 y #19) está abierto. Lo marcado como hecho en Disponibilidad y Notificaciones cuenta con que entre #21.

## Derrick — Backend, datos y mensajes

Carpetas principales:

```text
backend/
data/
ui/mensajes/
```

### Hecho

- [x] Elegir qué vamos a utilizar para el backend (API propia con Spring Boot + PostgreSQL).
- [x] Login con JWT en el backend (`/api/auth/login`, `/api/auth/yo`), límite de intentos y usuarios demo (#14, #15).
- [x] La app inicia sesión contra el backend (#16).
- [x] Las solicitudes se guardan en el servidor y la sesión queda guardada al cerrar la app (#23 y este PR).
- [x] `TutoriasRepo` conectado al backend: las pantallas usan las mismas funciones y cada cambio se manda al servidor.
- [x] Recuperación de contraseña con código por correo.
- [x] Registro de estudiantes con correo `@alu.uabcs.mx` y código de verificación.
- [x] Mensajes entre estudiante y tutor (pestaña Mensajes y chat por solicitud).
- [x] Mensajes conectados desde `PantallaPrincipal.kt` (tarjetas, detalle de cita y detalle de sesión).

### Pendiente nuevo

**Datos que bloquean a los demás (prioridad alta)**
- [x] Agregar a `Solicitud` los datos del estudiante (`estudiante`: correo, nombre, carrera y semestre) y la fecha de creación (`creadaEn`). Taddheo los necesita para "la más antigua, hace X días", la tarjeta de solicitud, la fila Estudiante del detalle y la fecha límite para responder. Pancho los necesita para Sesiones autorizadas. **Avisar antes de subirlo.**
- [x] Crear en el backend las tablas `tutores`, `horarios` y `solicitudes` (#23) (migraciones `V4__…` en adelante; no editar V1–V3, que ya se aplicaron). Después, los endpoints mínimos: listar tutores, horarios libres, crear solicitud y cambiar estado (aceptar, rechazar, cancelar y registrar).

**Inicio de sesión**
- [x] La respuesta de login solo trae `token`, `nombre` y `rol`. La app la ignora y toma el usuario de `Cuentas` (`Api.kt`). Hay que devolver también correo, detalle y `tutorId`, y armar el `Usuario` con esa respuesta.
- [x] Solo hay 3 cuentas en `DatosDemo`. Sofía, Luis y Rubén no tienen cuenta de tutor y no se pueden probar.
- [x] Sesión después de que Android cierra la app: `TutoriasApp` guarda el correo con `rememberSaveable`, pero `Api.token` vive solo en memoria. Al volver, la app aparece con la sesión iniciada pero sin token. Hay que guardar el token (DataStore) o regresar al login cuando `token == null`.
- [x] `Api.BASE_URL` está fijo en `http://10.0.2.2:8081` y solo funciona en el emulador. Moverlo a `BuildConfig` por tipo de build para poder probar en un teléfono físico.
- [x] Cuando el login real esté completo, quitar `Cuentas.iniciarSesion` y `CONTRASENA_DEMO`. Ya solo los usan las pruebas.

**Reglas que la app promete y no cumple**
- [x] Solicitudes pendientes que nunca vencen. `SolicitudEnviada` dice que el tutor "tiene hasta 24 horas para aceptar", pero una `PENDIENTE` se queda así para siempre, aunque su horario ya haya pasado. Definir la regla en `TutoriasRepo` (por ejemplo, pasar a `RECHAZADA` con "Sin respuesta del tutor") con su prueba.
- [x] Lista de espera. Al cancelar o rechazar una sesión no se avisa a quien está en la lista ni se le ofrece el horario libre, aunque la app dice "te avisaremos". Agregar la función en `TutoriasRepo` con su prueba.
- [x] Recordatorios "24 h y 1 h antes" (`SolicitudEnviada`). Implementarlos con notificaciones locales o quitar el texto.
- [x] Archivo adjunto. `NuevaSolicitud` solo guarda el nombre y descarta el `Uri`, así que el tutor nunca podrá descargarlo. Hace falta un endpoint para subirlo (máximo 10 MB) y guardar la referencia en `Solicitud`.

**Backend**
- [x] `contextLoads` necesita PostgreSQL y las variables de `.env`, así que falla en cualquier máquina sin base de datos. Usar un perfil `test` (H2 o Testcontainers) o quitar la prueba.
- [x] Agregar pruebas de `AuthController` con MockMvc: login correcto, 401, 429 y `/api/auth/yo` con y sin token.
- [x] En `application.properties` la URL de la base de datos está fija y `spring.jpa.show-sql=true` aplica a todos los perfiles. Mover la URL a una variable `DB_URL` y `show-sql` al perfil `dev`.

**Proyecto**
- [x] No hay CI (no existe `.github/`). Agregar GitHub Actions que corra `./gradlew :app:testDebugUnitTest :app:lintDebug` y las pruebas del backend en cada PR.
- [x] README: documentar cómo levantar el backend: PostgreSQL, `.env` con `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET` (Base64 de al menos 32 bytes) y `DEMO_PASSWORD`, perfil `dev` y puerto 8081. Hoy el README dice que no hay backend.

### Limpieza del código

- [x] Cambiar `open class TutoriasRepo` + `object Tutorias` por `val Tutorias = TutoriasRepo { LocalDateTime.now() }` y quitar el `open`.
- [x] Corregir `Tutor.iniciales`: truena si el nombre tiene doble espacio (usar `it.firstOrNull()?.isUpperCase() == true`).
- [x] Agregar pruebas para `parecidos`, `solicitarCambio` y la posición en la lista de espera.
- [x] En `InicioSesion.kt`, guardar la contraseña con `remember` en lugar de `rememberSaveable` para que no quede en el estado guardado.
- [x] Quitar `ConexionSegura` ("cifrado TLS") de `Componentes.kt` e `InicioSesion.kt` hasta que exista el backend.

---

## Taddheo — Tutor y Frontend

### Hecho (ya en `main`)

- [x] Relacionar la cuenta de Tutor con la información del tutor correspondiente (`Usuario.tutorId`).
- [x] Navegación del tutor: `Inicio · Solicitudes · Sesiones · Mensajes · Perfil`, con `TipoRuta.DISPONIBILIDAD`, `DETALLE_SESION` y `REGISTRAR_SESION`.
- [x] Inicio: saludo, "Por atender", "Hoy", "Mi horario" y avisos.
- [x] Solicitudes: pestañas `Nuevas (N) · Aceptadas · Historial`, aceptar, y rechazar con un diálogo de motivos (#17).
- [x] Sesiones: Por registrar, Próximas y Completadas; Detalle de la sesión; cancelar con motivo (`cancelarComoTutor`).
- [x] Registrar sesión en pantalla propia, con asistencia y acuerdos, que pasa la sesión a `COMPLETADA`.
- [x] Disponibilidad: cuadrícula semanal Lun–Vie, "Guardar cambios", "Copiar a la próxima semana", materias y modalidades (#19, #21). Se decidió editar semanas concretas, no una plantilla.
- [x] Notificaciones: "N sin leer", "Marcar todas como leídas", punto de no leída, una sola tarjeta y abrir al tocar (#18, #21).
- [x] Ayuda del botón `?`.

### Pendiente (ya estaba)

**Solicitudes**
- [x] Tarjeta con avatar, nombre del estudiante, carrera y semestre (se muestran en `SolicitudesTutor.kt`).
- [x] Botón para descargar el archivo adjunto desde la solicitud (el backend expone `GET /api/solicitudes/{id}/archivo`).
- [ ] "Proponer otro horario": diálogo con fecha, chips de horas libres, mensaje opcional, "Enviar propuesta" y el toast "Propuesta enviada". Falta acordar con Derrick cómo se guarda la propuesta y crear el flujo del estudiante para aceptarla. Requiere una función nueva en `TutoriasRepo` con sus pruebas.

**Sesiones**
- [ ] Interruptor "Compartir acuerdos con Coordinación" en el detalle. Ya existe `permitirCoordinacion`; falta separar lo que decide el tutor de lo que decide el estudiante.
- [x] Fila Estudiante en el detalle y en "Hoy" de Inicio (nombre, carrera y semestre se muestran desde `Solicitud`).

**Registrar sesión**
- [ ] Badge "Por registrar" y nombre del estudiante en el encabezado.
- [ ] Tarjeta "Actividades pendientes": casillas, botón para quitar y "+ Agregar actividad".
- [ ] "Siguiente sesión (opcional)": menú con los horarios libres del tutor.
- [ ] Tarjeta "Materiales": archivo, botón para quitarlo y "+ Adjuntar material".
- [ ] Botón "Guardar borrador".
- [ ] Datos: `Solicitud` necesita acuerdos, actividades, materiales, siguiente sesión y borrador. **Avisar a Derrick y a Pancho antes de cambiar el modelo.**

**Notificaciones**
- [ ] Agrupar por "Hoy" y "Esta semana", con hora relativa ("Hace 10 min", "Ayer"). `Aviso` necesita una fecha.
- [ ] Textos del Figma: "Nueva solicitud de {Estudiante}" con la fecha límite, "{Estudiante} aceptó tu propuesta", "Sesión cancelada" con el motivo y "Tienes una sesión sin registrar".

**Ayuda**
- [ ] Preguntas en acordeón dentro de una sola tarjeta.
- [ ] Preguntas del Figma para tutor: "¿Qué pasa si no respondo una solicitud?" y "¿Quién puede ver mis datos?".
- [ ] Tarjeta "Contacto de soporte": correo, teléfono, horario, lugar y botón.

**Perfil del tutor** (coordinar con Pancho, que hace la pantalla de perfil)
- [x] "Mi perfil", "Visibilidad de mis datos", la lista de opciones y "Editar perfil", según el Figma.
- [x] Entrada "Mi horario de atención" desde Perfil hacia Disponibilidad.
- [x] Agregar `TipoRuta` para Editar perfil cuando exista su pantalla.

**Mensajes**
- [x] Conectar "Mensaje al estudiante" del detalle de sesión con el chat.

### Pendiente nuevo

**Errores de flujo**
- [x] Solicitud de cambio de horario: el botón ahora dice "Mantener horario" en lugar de "Aceptar". Cuando el estudiante pide un cambio, el tutor ve "Piden cambio de horario", pero `aceptar()` borra la nota y confirma **el mismo horario**. El tutor no puede elegir otro. Resolverlo junto con "Proponer otro horario"; mientras tanto, ocultar "Aceptar" en las solicitudes de cambio.
- [ ] La lista de espera es invisible para el tutor. `SolicitudesTutor` manda las `LISTA_ESPERA` a Historial porque no son `activa`. Mostrarlas aparte, con su contador.
- [x] El estudiante no se entera cuando el tutor cancela (agregado `TipoAviso.CANCELADA` con el motivo y su prueba). `avisos()` no tiene un caso para `CANCELADA`. Agregar `TipoAviso.CANCELADA` con el motivo, más su prueba.
- [ ] Sábados. Sofía tiene horarios en sábado en los datos demo, y el estudiante ve sábados (la vista Semana y el filtro Día los incluyen). Pero la cuadrícula del tutor solo edita Lun–Vie y `guardarSemana` no toca el sábado. Decidir si se agrega el sábado a la cuadrícula o se quita de la búsqueda y de los datos demo.
- [ ] "N h libres" en Inicio cuenta de lunes a domingo, pero la cuadrícula solo de lunes a viernes. Usar el mismo rango.

**Textos desactualizados**
- [x] Ayuda del tutor: "Elige el día y toca cada hora" ya no aplica con la cuadrícula. Explicar los bloques y "Guardar cambios".
- [ ] Hay valores escritos a mano que deberían usar las constantes de `TutoriasRepo`. "24 h" en `PerfilTutor.REQUISITOS` debe usar `ANTICIPACION_HORAS`. "2 semanas" en `proximoHorario`, `VistaHorarios` y `NuevaSolicitud` debe usar `DIAS_AGENDA`.

Si es necesario modificar la estructura de `Tutor` o `Solicitud`, avisar antes para evitar problemas con las demás ramas.

### Limpieza del código

- [x] Bug en `CambiarHorario` (`MisSolicitudes.kt`): si el horario ya no está libre, la pantalla se cierra. Debe quedarse abierta y avisar, como en `NuevaSolicitud`.
- [x] El calendario de `MisSolicitudes.kt` usa `LocalDate.now()`; usar el reloj de `TutoriasRepo` para que respete las pruebas.
- [x] Validar los correos de compañeros en sesión grupal: sin repetidos y sin el correo del propio estudiante.
- [x] En `NuevaSolicitud.kt`, cambiar las 4 `OpcionTarjeta` de modalidad y formato por un `forEach` sobre `Modalidad.entries` y `Formato.entries`.
- [x] Quitar la dependencia `core-ktx` de `app/build.gradle.kts` y `libs.versions.toml` (no se usa).
- [x] Corregir el README: dice que se usa Navigation Compose, pero la navegación es la pila de `PantallaPrincipal.kt`.
- [ ] Quitar `data class Quad` de `NuevaSolicitud.kt`: con un `Triple` y el ícono calculado aparte alcanza.

---

## Pancho — Coordinación, perfil y diseño

Carpetas principales:

```text
ui/coordinacion/
ui/perfil/
```

### Hecho (primera versión, con datos fijos)

- [x] Crear el panel principal de Coordinación.
- [x] Mostrar un resumen de solicitudes. *Los números están escritos a mano.*
- [x] Mostrar solicitudes autorizadas para Coordinación. *Usa una lista de ejemplo.*
- [x] Crear el directorio de tutores.
- [x] Crear la pantalla de perfil de Coordinación, con cerrar sesión.

### Pendiente (ya estaba)

- [x] Adaptar el perfil dependiendo del tipo de usuario. `ui/perfil/Perfil.kt` sirve a los tres roles, con Editar perfil (nombre y carrera o departamento, `POST /api/auth/perfil`).
- [x] Crear la pantalla de aviso de privacidad (`ui/Privacidad.kt`). **Es un borrador:** la Unidad de Transparencia de la UABCS debe revisarlo y completar domicilio y contacto.
- [x] Conectar el aviso de privacidad con Perfil y `PieLegal`.
- [x] Agregar la fuente utilizada en Figma: Inter (`res/font/`, licencia SIL OFL 1.1).
- [x] Exportar y agregar los íconos del diseño de Figma (`ui/Iconos.kt`).

### Pendiente nuevo

**Usar datos reales en lugar de valores fijos**
- [x] Panel: los 6 KPIs ("128", "37", "24", "14", "9", "87 %") y "Autorizadas recientemente (2 de 37)" están escritos a mano. Calcularlos desde `Tutorias.solicitudes` y `Tutorias.tutores`, con funciones en `TutoriasRepo` y sus pruebas.
- [x] Sesiones autorizadas: cambiar `sesionesMock` por `Tutorias.solicitudes.filter { it.coordinacionPuedeVer }`. El "de 37" del contador también es fijo.
- [x] "Ver contenido académico" abre "Disponible en una próxima versión". Crear la pantalla con asistencia y acuerdos (`asistio`, `notasSesion`), solo si `coordinacionPuedeVer` está activo. Necesita su `TipoRuta`.
- [x] Directorio de tutores: "de 24" y `estadisticasMes()` son fijos por id. Calcularlos con las solicitudes de cada tutor.
- [x] Interruptores del perfil: queda solo "Mostrar mi correo a los estudiantes" para tutores, guardado en el servidor (`usuarios.mostrar_correo`) y visible en el perfil del tutor. Se quitó el de foto: la app no tiene fotos.

**Errores**
- [x] Directorio: `remember(textoBusqueda, todos)` no se vuelve a calcular cuando un tutor edita sus materias, porque la lista es la misma instancia. Quitar el `remember`.
- [x] Directorio: la búsqueda no quita acentos ("calculo" no encuentra "Cálculo"). Usar `Tutorias.buscar(Filtros(texto))`, que ya normaliza, o su `normalizar`.

**Componentes repetidos**
- [x] Hay piezas duplicadas en Coordinación. `TarjetaKpi` y `TarjetaKpiSesiones` son iguales. `TarjetaAutorizada` y `TarjetaSesionAutorizada` también. La tarjeta del escudo se repite en Panel y en Sesiones autorizadas. Dejar una sola versión de cada una.
- [x] Las pantallas de Coordinación arman `Card(colors…, border…)` a mano en lugar de usar `Tarjeta`, y dibujan el círculo de iniciales en lugar de usar `AvatarIniciales`.
- [x] `modalidadesFormatosTexto` repite lo que ya hacen `Modalidad.nombre` y `Formato.nombre` (ver `TarjetaTutor`).
- [x] Mover a `ui/Componentes.kt` los componentes que usan los tres roles y hoy viven en pantallas del estudiante o del tutor: `Pantalla`, `Tarjeta`, `AvatarIniciales`, `BadgeEstado`, `DatoFila` y `FilaIcono` (`estudiante/Comunes.kt`), `Encabezado` y `Aviso` (`estudiante/InicioBuscar.kt`) y `Fila` (`tutor/InicioTutor.kt`). **Hacerlo en un PR chico y avisar**, porque toca casi todas las pantallas.

**Avisos institucionales**
- [x] Los avisos de Inicio están escritos a mano en `InicioEstudiante` e `InicioTutor`, y ya vencieron ("Del 19 al 30 de octubre"). Pasarlos a una lista en `TutoriasRepo` y agregar en Coordinación una forma de publicarlos. *Hecho: tabla `avisos_institucionales`, se publican y quitan desde el Panel.*

**README**
- [x] Actualizar "Estructura del proyecto" y "Estado actual": faltan `coordinacion/`, `notificaciones/`, `Ayuda.kt`, `Api.kt`, `Avisos.kt` y `backend/`, y dice que Coordinación, Notificaciones y Ayuda están pendientes. La parte de cómo levantar el backend la escribe Derrick.

### Limpieza del código

- [x] Crear `Pantalla(modifier) { … }` en `Comunes.kt` para el `Column` con scroll y `padding(16.dp)` que se repite en 10 pantallas.
- [x] Crear `BotonPrimario` (forma `shapes.small` y alto de 48 dp) igual que `BotonSecundario`, y usarlo en los 8 `Button` que repiten esos valores.
- [x] Quitar el parámetro `shape` de `BotonSecundario`: todas las llamadas pasan el valor por defecto.
- [x] Usar `Tarjeta` en `PantallaPendiente` (`PantallaPrincipal.kt`) en lugar de armar la `Card` a mano.
- [x] Borrar el color `skeleton` de `Tema.kt` (no se usa).
- [x] Sacar los nombres de los días con `DayOfWeek.getDisplayName` en `InicioBuscar.kt` y `Comunes.kt` (revisar que no salgan con punto).
- [x] En `AccionFila`, usar `MaterialTheme.colorScheme.onSurface` como color por defecto en lugar de `Color.Unspecified`.
- [x] Quitar `material-icons-extended` cuando estén los íconos de Figma.

---

# Forma de trabajar con Git

Para evitar problemas al juntar el trabajo vamos a seguir estas reglas.

### 1. Una rama por tarea

Cada tarea se trabaja en una rama diferente creada desde `main`.

Ejemplos:

```text
tutor-solicitudes
tutor-horarios
coord-panel
perfil
mensajes
notificaciones
```

No subir cambios directamente a `main`.

Cuando una tarea esté terminada se abre un Pull Request.

### 2. PantallaPrincipal.kt

Este archivo lo vamos a necesitar los tres, así que hay que intentar modificar solamente la parte que corresponda.

**Taddheo**
- Navegación del Tutor.
- Campana de notificaciones.
- Botón de ayuda.

**Pancho**
- Navegación de Coordinación.
- Navegación hacia Perfil.

**Derrick**
- Navegación hacia Mensajes.

Si dos PR tocan las mismas líneas (como pasó con #18 y #19), el segundo se actualiza con `main` y se conservan los cambios de ambos.

### 3. Cambios en data/

Se pueden agregar nuevas funciones a `TutoriasRepo`, pero hay que evitar hacer cambios grandes de golpe.

Si se modifica la estructura de `Tutor`, `Solicitud` u otro modelo que estén utilizando los demás, avisar antes de subir el cambio.

Cuando se agregue lógica nueva al repositorio también hay que agregar o actualizar sus pruebas.

Las pantallas no modifican el estado directamente (por ejemplo, `Tutorias.leidos += …`); siempre pasan por una función de `TutoriasRepo`.

### 4. Antes de abrir un Pull Request

Actualizar la rama con los últimos cambios de `main`:

```bash
git pull --rebase origin main
```

Después ejecutar las pruebas y el lint:

```bash
./gradlew :app:testDebugUnitTest :app:lintDebug
```

Si se tocó el backend:

```bash
cd backend && ./gradlew test
```

Si todo funciona, ya se puede abrir el PR.

### 5. Reutilizar componentes

Antes de crear un componente nuevo revisar:

```text
ui/Componentes.kt
ui/estudiante/Comunes.kt
```

Si ya existe algo parecido, es mejor reutilizarlo para mantener el mismo diseño en toda la aplicación.

---

# Orden de trabajo

## Ahora

**Todos**
- Revisar y mergear #21.

**Derrick**
- Agregar los datos del estudiante y `creadaEn` a `Solicitud`. Desbloquea a Taddheo y a Pancho.
- Arreglar `contextLoads` y agregar el CI.

**Taddheo**
- Errores de flujo: solicitud de cambio, lista de espera visible y aviso de cancelación.
- Acordar con Derrick cómo se guarda "Proponer otro horario".

**Pancho**
- PR chico para mover los componentes compartidos a `ui/Componentes.kt`, antes de que los demás abran ramas nuevas.
- Cambiar los datos fijos de Coordinación por datos de `TutoriasRepo`.

## Después

Trabajar en paralelo con:

- Registrar sesión completo (actividades, materiales, borrador).
- Perfil de los tres roles y aviso de privacidad.
- Tablas y endpoints de tutores, horarios y solicitudes.
- Fuente e íconos de Figma.

## Al final

- Conexión completa con el backend.
- Mensajes.
- Recordatorios y notificaciones push.
- Correcciones generales.
- Pruebas de los tres roles.

La prioridad es terminar primero las funciones principales de cada rol y después conectar todo con el backend.
