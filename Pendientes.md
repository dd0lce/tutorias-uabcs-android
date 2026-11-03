
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
- [ ] En `InicioSesion.kt`, guardar la contraseña con `remember` en lugar de `rememberSaveable` para que no quede en el estado guardado.
- [ ] Quitar `ConexionSegura` ("cifrado TLS") de `Componentes.kt` e `InicioSesion.kt` hasta que exista el backend.

---

## Thaddeo — Tutor y notificaciones

Carpetas principales:

```text
ui/tutor/
ui/notificaciones/
```

### Pendiente

- [x] Relacionar la cuenta de Tutor con la información del tutor correspondiente.
- [x] Crear la pantalla de solicitudes recibidas.
- [x] Permitir aceptar solicitudes.
- [x] Permitir rechazar solicitudes indicando el motivo.
- [x] Crear el apartado para administrar disponibilidad y horarios.
- [x] Permitir registrar una sesión realizada.
- [x] Registrar asistencia y notas de la sesión.
- [x] Cambiar una sesión realizada al estado `COMPLETADA`.
- [ ] Crear la pantalla de notificaciones.
- [ ] Reemplazar el número fijo de la campana por notificaciones reales.
- [ ] Agregar la sección de ayuda del botón `?`.

Si es necesario modificar la estructura de `Tutor` o `Solicitud`, avisar antes para evitar problemas con las demás ramas.

### Limpieza del código

- [ ] Bug en `CambiarHorario` (`MisSolicitudes.kt`): si el horario ya no está libre, la pantalla se cierra. Debe quedarse abierta y avisar, como en `NuevaSolicitud`.
- [ ] El calendario de `MisSolicitudes.kt` usa `LocalDate.now()`; usar el reloj de `TutoriasRepo` para que respete las pruebas.
- [ ] Validar los correos de compañeros en sesión grupal: sin repetidos y sin el correo del propio estudiante.
- [ ] En `NuevaSolicitud.kt`, cambiar las 4 `OpcionTarjeta` de modalidad y formato por un `forEach` sobre `Modalidad.entries` y `Formato.entries`.
- [ ] Quitar la dependencia `core-ktx` de `app/build.gradle.kts` y `libs.versions.toml` (no se usa).
- [ ] Corregir el README: dice que se usa Navigation Compose, pero la navegación es la pila de `PantallaPrincipal.kt`.

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

**Thaddeo**
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

**Thaddeo**
- Relacionar la cuenta del Tutor con su información.

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