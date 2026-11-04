# Tutorías UABCS - Android

Este proyecto consiste en una aplicación Android para el sistema de tutorías académicas de la Universidad Autónoma de Baja California Sur (UABCS).

La idea de la aplicación es facilitar el proceso para solicitar y administrar tutorías dentro de la universidad. Se están manejando tres tipos de usuario:

- Estudiante
- Tutor
- Coordinación

Por el momento, la mayor parte del desarrollo está enfocada en las funciones del estudiante. Las secciones de Tutor y Coordinación todavía están en desarrollo.

El diseño de las pantallas se realizó previamente en Figma y se está utilizando como referencia para desarrollar la interfaz de la aplicación.

## Tecnologías utilizadas

El proyecto está desarrollado principalmente con:

- Kotlin
- Jetpack Compose
- Material 3
- Gradle

La aplicación utiliza una sola `Activity` y las pantallas se manejan con una pila propia en `PantallaPrincipal.kt` (sin Navigation Compose).

También se utiliza `NavigationSuiteScaffold` para adaptar la navegación dependiendo del dispositivo. En teléfonos se muestra una barra de navegación inferior y en pantallas más grandes se utiliza un menú lateral.

Actualmente no hay conexión con un backend. Los usuarios, tutores, horarios y solicitudes que aparecen en la aplicación son datos de prueba guardados localmente en memoria.

## Cómo ejecutar el proyecto

Para ejecutar el proyecto se necesita Android Studio.

1. Clonar o descargar el repositorio.
2. Abrir la carpeta del proyecto en Android Studio.
3. Esperar a que Gradle termine de sincronizar las dependencias.
4. Seleccionar un emulador o conectar un dispositivo Android.
5. Ejecutar la configuración `app`.

También se puede compilar desde la terminal con:

```bash
./gradlew :app:assembleDebug
```

Para ejecutar las pruebas:

```bash
./gradlew :app:testDebugUnitTest
```

## Cuentas de prueba

Para probar la aplicación existen usuarios simulados para los tres roles.

Las cuentas se encuentran en:

```text
app/src/main/java/mx/uabcs/tutorias/data/Cuentas.kt
```

Actualmente hay una cuenta para:

- Estudiante
- Tutor
- Coordinación

Las cuentas utilizan la contraseña definida en:

```kotlin
Cuentas.CONTRASENA_DEMO
```

## Estructura del proyecto

La mayor parte del código que se ha trabajado hasta ahora se encuentra dentro de las carpetas `ui` y `data`.

```text
ui/
├── theme/
│   └── Tema.kt
├── estudiante/
│   ├── Comunes.kt
│   ├── InicioBuscar.kt
│   ├── PerfilTutor.kt
│   ├── NuevaSolicitud.kt
│   └── MisSolicitudes.kt
├── Componentes.kt
├── InicioSesion.kt
└── PantallaPrincipal.kt

data/
├── Cuentas.kt
└── Tutorias.kt
```

### `Tema.kt`

Contiene los colores, estilos de texto y algunos elementos del tema visual de la aplicación.

### `Componentes.kt`

Aquí se encuentran varios componentes que se reutilizan en diferentes pantallas, por ejemplo botones, campos de texto, elementos de la marca UABCS y algunos elementos del footer.

### `InicioSesion.kt`

Contiene la pantalla de inicio de sesión y la validación básica de las cuentas de prueba.

### `PantallaPrincipal.kt`

Se encarga de mostrar la pantalla principal y las opciones disponibles dependiendo del tipo de usuario que inició sesión.

### Carpeta `estudiante`

Contiene las pantallas relacionadas con las funciones del estudiante, como:

- Buscar tutores.
- Consultar el perfil de un tutor.
- Revisar horarios disponibles.
- Crear una solicitud de tutoría.
- Consultar las solicitudes realizadas.
- Cambiar o cancelar una cita.
- Consultar las citas en diferentes vistas.

### Carpeta `data`

Por ahora se utiliza para guardar los datos simulados de la aplicación.

`Cuentas.kt` contiene los usuarios de prueba y la validación del correo institucional.

`Tutorias.kt` contiene los tutores, horarios y solicitudes utilizadas durante las pruebas de la aplicación. También contiene algunas reglas que se están utilizando en el sistema, como el tiempo mínimo de anticipación para solicitar una tutoría.

## Estado actual

Actualmente el flujo del estudiante es la parte más avanzada del proyecto.

Ya se puede:

- Iniciar sesión con una cuenta de prueba.
- Buscar tutores.
- Aplicar algunos filtros de búsqueda.
- Consultar el perfil de un tutor.
- Revisar sus horarios.
- Crear una solicitud de tutoría.
- Consultar las solicitudes realizadas.
- Cambiar o cancelar algunas citas.
- Visualizar citas mediante diferentes vistas.

Los apartados de Tutor y Coordinación ya aparecen dentro de la aplicación, pero varias de sus pantallas todavía están pendientes y por ahora muestran una sección en construcción.

## Cosas que faltan

Todavía quedan varias partes por desarrollar, entre ellas:

- Terminar las funciones correspondientes al Tutor.
- Terminar las funciones de Coordinación.
- Agregar la sección de mensajes.
- Terminar el perfil del estudiante.
- Agregar las notificaciones.
- Conectar la aplicación a un backend.
- Guardar la información de forma persistente.
- Reemplazar algunos íconos de Material por los utilizados en el diseño de Figma.
- Agregar correctamente la fuente utilizada en el diseño.

Actualmente los datos se pierden cuando se cierra la aplicación porque todavía se trabaja con información simulada en memoria.

## Sobre el proyecto

Este proyecto se encuentra en desarrollo y forma parte de un proyecto universitario de la UABCS.

La intención es ir implementando los diferentes módulos de acuerdo con los requerimientos definidos para el sistema y utilizar el prototipo de Figma como referencia para mantener consistencia entre el diseño y la aplicación.