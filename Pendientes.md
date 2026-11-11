
## Derrick — Backend y mensajes

Carpetas principales:

```text
data/
ui/mensajes/
```

### Pendiente

- [ ] Elegir qué vamos a utilizar para el backend (Firebase, Supabase o API propia).
- [ ] Guardar las solicitudes para que no se pierdan al cerrar la aplicación.
- [ ] Conectar `TutoriasRepo` al backend.
- [ ] Implementar inicio de sesión con cuentas reales.
- [ ] Agregar recuperación de contraseña.
- [ ] Agregar registro utilizando correo institucional.
- [ ] Crear el apartado de mensajes entre estudiante y tutor.
- [ ] Conectar la opción de mensajes desde `PantallaPrincipal.kt`.

Al conectar el backend hay que intentar mantener las funciones actuales de `TutoriasRepo` para no romper las pantallas que ya funcionan.

### Limpieza del código

- [x] Cambiar `open class TutoriasRepo` + `object Tutorias` por `val Tutorias = TutoriasRepo { LocalDateTime.now() }` y quitar el `open`.
- [x] Corregir `Tutor.iniciales`: truena si el nombre tiene doble espacio (usar `it.firstOrNull()?.isUpperCase() == true`).
- [x] Agregar pruebas para `parecidos`, `solicitarCambio` y la posición en la lista de espera.
- [x] En `InicioSesion.kt`, guardar la contraseña con `remember` en lugar de `rememberSaveable` para que no quede en el estado guardado.
- [x] Quitar `ConexionSegura` ("cifrado TLS") de `Componentes.kt` e `InicioSesion.kt` hasta que exista el backend.

---

## Taddheo — Tutor y Frontend


### Hecho (primera versión, ya en `main`)

- [x] Relacionar la cuenta de Tutor con la información del tutor correspondiente (`Usuario.tutorId`).
- [x] Crear la pantalla de solicitudes recibidas.
- [x] Permitir aceptar solicitudes.
- [x] Permitir rechazar solicitudes indicando el motivo.
- [x] Crear el apartado para administrar disponibilidad y horarios.
- [x] Permitir registrar una sesión realizada.
- [x] Registrar asistencia y notas de la sesión.
- [x] Cambiar una sesión realizada al estado `COMPLETADA`.
- [x] Crear la pantalla de notificaciones.
- [x] Reemplazar el número fijo de la campana por notificaciones reales.
- [x] Agregar la sección de ayuda del botón `?`.

### Pendiente: ajustar el frontend al Figma (página "Tutor", Módulo 6)

Las pantallas de arriba funcionan, pero no siguen el diseño. Esto es lo que falta, por pantalla.

**Navegación (`PantallaPrincipal.kt`)**
- [x] Cambiar la barra del tutor a `Inicio · Solicitudes · Sesiones · Mensajes · Perfil`. Disponibilidad sale de la barra y se abre desde Inicio ("Mi horario → Editar") y desde Perfil ("Mi horario de atención"). *Falta la entrada desde Perfil.*
- [x] Agregar `TipoRuta.DISPONIBILIDAD`.
- [ ] Agregar a `TipoRuta` las rutas Detalle de sesión, Registrar sesión y Editar perfil cuando existan sus pantallas.

**Inicio (pantalla nueva)**
- [x] Saludo "Hola, {nombre}" con la fecha de hoy.
- [x] Tarjeta "Por atender": "N solicitudes nuevas · la más antigua, hace X días" y "N sesiones sin registrar". Cada fila lleva a su pantalla. *Falta "la más antigua, hace X días" (requiere la fecha de creación).*
- [x] Sección "Hoy" con las sesiones confirmadas del día ("4:00 PM · Derrick Ramírez / Matemáticas · Presencial") y el enlace "Ver todas" hacia Sesiones. *Muestra la materia en lugar del estudiante y abre Sesiones en lugar del detalle.*
- [x] Tarjeta "Mi horario": "N h libres esta semana" con el botón "Editar" hacia Disponibilidad.
- [x] Tarjeta de avisos institucionales (por ejemplo, "Tutorías de regularización de Cálculo").

**Solicitudes**
- [ ] Pestañas `Nuevas (N) · Aceptadas · Historial` en lugar de "Por responder" + "Historial".
- [ ] Tarjeta con avatar de iniciales, nombre del estudiante, carrera y semestre, badge de estado, "Materia · Tema" y "fecha · hora · modalidad · Grupal (N)".
- [ ] Mostrar el archivo adjunto del estudiante (nombre, "PDF · adjuntado por el estudiante" y botón de descarga).
- [ ] Botones: "Aceptar" (principal, a todo el ancho), "Proponer otro horario" (secundario) y "Rechazar" (de texto).
- [ ] Diálogo "Proponer otro horario": selector de fecha, chips de hora libres, "Mensaje (opcional)", "Enviar propuesta" y "Cancelar". Requiere una función nueva en `TutoriasRepo` y sus pruebas.
- [ ] Pasar el rechazo a un diálogo "¿Rechazar la solicitud?" con un **menú de motivos** (por ejemplo, "No tengo disponibilidad ese día"), el texto "{Estudiante} verá este motivo.", el botón rojo "Rechazar solicitud" y "Volver".
- [ ] Toasts: "Solicitud aceptada · {Estudiante} ya la ve como confirmada. La encuentras en Sesiones.", "Propuesta enviada" y "Solicitud rechazada".

**Sesiones**
- [ ] Tres secciones en forma de lista con flecha: "Por registrar" (lápiz, "ya pasó"), "Próximas" (reloj; hoy falta mostrar las confirmadas futuras) y "Completadas" (check, "registrada").
- [ ] Pantalla "Detalle de la sesión": badge de estado; filas Estudiante, Dónde, Cuándo y Tema; archivo adjunto; botones "Registrar sesión", "Mensaje a {Estudiante}" y "Cancelar sesión".
- [ ] Interruptor "Compartir acuerdos con Coordinación" ("Apagado. Solo ven fecha y asistencia.") en el detalle. Ya existe `permitirCoordinacion`; falta separar lo que decide el tutor de lo que decide el estudiante.
- [ ] Diálogo "¿Cancelar la sesión?" con menú de motivos, el texto "el horario queda libre para otros estudiantes", el botón rojo "Cancelar sesión" y "Volver", más el toast "Sesión cancelada". Requiere una función `cancelar` del tutor que guarde el motivo, porque la actual escribe "Cancelada por ti".

**Registrar sesión (pantalla propia, ya no dentro de la tarjeta)**
- [ ] Encabezado con título, badge "Por registrar", "Estudiante · fecha · hora" y la casilla "El estudiante asistió".
- [ ] Cambiar "Notas" por "Acuerdos" (texto de varias líneas).
- [ ] Tarjeta "Actividades pendientes": lista con casillas, botón de basura para quitar y "+ Agregar actividad".
- [ ] "Siguiente sesión (opcional)": menú con los horarios libres del tutor.
- [ ] Tarjeta "Materiales": archivo con botón para quitarlo y "+ Adjuntar material".
- [ ] Botones "Guardar borrador" y "Guardar registro", más el toast "Registro guardado".
- [ ] Datos: `Solicitud` necesita acuerdos, actividades, materiales, siguiente sesión y borrador. **Avisar a Derrick y a Pancho antes de cambiar el modelo.**

**Disponibilidad**
- [ ] Reemplazar los chips de día y hora por la cuadrícula semanal Lun–Vie × horas, con "+" para abrir un bloque y un check para cerrarlo.
- [ ] Botones "Guardar cambios" y "Copiar a la próxima semana". Hoy cada toque se guarda al instante; el diseño guarda en bloque.
- [ ] Tarjeta "Materias y áreas que atiendo": chips con check y "+ Agregar materia" (edita `Tutor.materias`).
- [ ] Tarjeta "Modalidades" con interruptores: Presencial (cubículo), En línea (Google Meet), Sesiones grupales (hasta 5 estudiantes) y "Aceptar solicitudes solo si el horario está libre" (= `confirmaAutomatico`).
- [ ] Decidir si la cuadrícula es una plantilla semanal o la semana actual. Hoy `Tutor.horarios` son fechas concretas de los próximos 14 días.

**Notificaciones**
- [ ] Encabezado "N sin leer" con el botón "Marcar todas como leídas". Ya no marcarlas todas al abrir.
- [ ] Agrupar por "Hoy" y "Esta semana", con hora relativa ("Hace 10 min", "Ayer"). `Aviso` necesita una fecha.
- [ ] Ícono en círculo de color, punto azul para las no leídas y la lista en una sola tarjeta con divisores, en lugar de una tarjeta por aviso.
- [ ] Textos del Figma: "Nueva solicitud de {Estudiante}" con fecha límite ("Tienes hasta el sáb 17 para responder"), "{Estudiante} aceptó tu propuesta", "Sesión cancelada" con motivo y "Tienes una sesión sin registrar".
- [ ] Tocar una notificación debe abrir la solicitud o sesión correspondiente.

**Ayuda**
- [ ] Preguntas en acordeón (se abren y se cierran) en una sola tarjeta.
- [ ] Preguntas del Figma para tutor: "¿Cómo acepto o rechazo una solicitud?", "¿Qué pasa si no respondo una solicitud?", "¿Cómo registro una sesión?", "¿Cómo cambio mi horario de atención?" y "¿Quién puede ver mis datos?".
- [ ] Tarjeta "Contacto de soporte": correo, teléfono, horario, lugar y botón.

**Perfil del tutor** (coordinar con Pancho, que hace la pantalla de perfil)
- [ ] "Mi perfil": avatar de iniciales, nombre, departamento, cubículo y "Editar perfil".
- [ ] "Visibilidad de mis datos": interruptores para correo institucional, teléfono y foto de perfil ("Solo estudiantes con cita").
- [ ] Lista de opciones: Mi horario de atención ("N horas libres esta semana"), Mis datos ("Descargar o borrar tus datos"), Ajustes y Ayuda y soporte, más "Cerrar sesión".
- [ ] "Editar perfil": "Cambiar foto", Cubículo, "Teléfono (opcional)", "Enlace para sesiones en línea", "Sobre mí (opcional, máx. 300)", "Guardar cambios" y "Cancelar", más el toast "Perfil actualizado".

**Mensajes** (lo construye Derrick; aquí solo lo que toca al tutor)
- [ ] Conectar "Mensaje a {Estudiante}" del detalle de sesión con el chat cuando exista.

**Datos que faltan para el tutor**
- [ ] Datos del estudiante en `Solicitud` (nombre, carrera, semestre). Hoy todo es del único estudiante demo. **Avisar antes.**
- [ ] Fecha de creación de la solicitud (para "la más antigua, hace 2 días" y la fecha límite para responder).

Si es necesario modificar la estructura de `Tutor` o `Solicitud`, avisar antes para evitar problemas con las demás ramas.

### Limpieza del código

- [x] Bug en `CambiarHorario` (`MisSolicitudes.kt`): si el horario ya no está libre, la pantalla se cierra. Debe quedarse abierta y avisar, como en `NuevaSolicitud`.
- [x] El calendario de `MisSolicitudes.kt` usa `LocalDate.now()`; usar el reloj de `TutoriasRepo` para que respete las pruebas.
- [x] Validar los correos de compañeros en sesión grupal: sin repetidos y sin el correo del propio estudiante.
- [x] En `NuevaSolicitud.kt`, cambiar las 4 `OpcionTarjeta` de modalidad y formato por un `forEach` sobre `Modalidad.entries` y `Formato.entries`.
- [x] Quitar la dependencia `core-ktx` de `app/build.gradle.kts` y `libs.versions.toml` (no se usa).
- [x] Corregir el README: dice que se usa Navigation Compose, pero la navegación es la pila de `PantallaPrincipal.kt`.

---

## Pancho — Coordinación, perfil y diseño

Carpetas principales:

```text
ui/coordinacion/
ui/perfil/
```

### Pendiente

- [ ] Crear el panel principal de Coordinación.
- [ ] Mostrar un resumen de solicitudes.
- [ ] Mostrar solicitudes autorizadas para Coordinación.
- [ ] Crear el directorio de tutores.
- [ ] Crear la pantalla de perfil.
- [ ] Adaptar el perfil dependiendo del tipo de usuario.
- [ ] Mantener la opción de cerrar sesión.
- [ ] Crear la pantalla de aviso de privacidad.
- [ ] Conectar el aviso de privacidad con Perfil y `PieLegal`.
- [ ] Agregar la fuente utilizada en Figma.
- [ ] Exportar y agregar los íconos del diseño de Figma.

Los íconos y la fuente conviene agregarlos primero para que podamos utilizarlos desde el inicio en las nuevas pantallas.

### Limpieza del código

- [ ] Crear `Pantalla(modifier) { … }` en `Comunes.kt` para el `Column` con scroll y `padding(16.dp)` que se repite en 10 pantallas.
- [ ] Crear `BotonPrimario` (forma `shapes.small` y alto de 48 dp) igual que `BotonSecundario`, y usarlo en los 8 `Button` que repiten esos valores.
- [ ] Quitar el parámetro `shape` de `BotonSecundario`: todas las llamadas pasan el valor por defecto.
- [ ] Usar `Tarjeta` en `PantallaPendiente` (`PantallaPrincipal.kt`) en lugar de armar la `Card` a mano.
- [ ] Borrar el color `skeleton` de `Tema.kt` (no se usa).
- [ ] Sacar los nombres de los días con `DayOfWeek.getDisplayName` en `InicioBuscar.kt` y `Comunes.kt` (revisar que no salgan con punto).
- [ ] En `AccionFila`, usar `MaterialTheme.colorScheme.onSurface` como color por defecto en lugar de `Color.Unspecified`.
- [ ] Quitar `material-icons-extended` cuando estén los íconos de Figma.

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

### 3. Cambios en data/

Se pueden agregar nuevas funciones a `TutoriasRepo`, pero hay que evitar hacer cambios grandes de golpe.

Si se modifica la estructura de `Tutor`, `Solicitud` u otro modelo que estén utilizando los demás, avisar antes de subir el cambio.

Cuando se agregue lógica nueva al repositorio también hay que agregar o actualizar sus pruebas.

### 4. Antes de abrir un Pull Request

Actualizar la rama con los últimos cambios de `main`:

```bash
git pull --rebase origin main
```

Después ejecutar las pruebas:

```bash
./gradlew :app:testDebugUnitTest
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

## Primero

**Derrick**
- Elegir backend.

**Taddheo**
- ~~Relacionar la cuenta del Tutor con su información.~~ Hecho.
- ~~Navegación del tutor (Inicio en la barra).~~ Hecho.
- Siguiente: pantallas de Detalle y Registro de sesión.

**Pancho**
- Agregar fuente e íconos de Figma.
- Crear `Pantalla` y `BotonPrimario` en un PR chico. Tocan todas las pantallas, así que conviene que entren a `main` antes de que los demás empiecen sus ramas.

## Después

Trabajar en paralelo con:

- Pantallas del Tutor.
- Pantallas de Coordinación.
- Perfil.
- Persistencia de datos.

## Al final

- Conexión completa con el backend.
- Mensajes.
- Notificaciones.
- Correcciones generales.
- Pruebas de los tres roles.

La prioridad es terminar primero las funciones principales de cada rol y después conectar todo con el backend.