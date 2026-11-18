# Tutorías UABCS - Android

Este proyecto consiste en una aplicación Android para el sistema de tutorías académicas de la Universidad Autónoma de Baja California Sur (UABCS).

La idea de la aplicación es facilitar el proceso para solicitar y administrar tutorías dentro de la universidad. Se están manejando tres tipos de usuario:

- Estudiante
- Tutor
- Coordinación

Por el momento, las funciones del estudiante y del tutor son las más avanzadas. Coordinación ya tiene su panel, sesiones autorizadas y directorio.

El diseño de las pantallas se realizó previamente en Figma y se está utilizando como referencia para desarrollar la interfaz de la aplicación.

## Tecnologías utilizadas

El proyecto está desarrollado principalmente con:

- Kotlin
- Jetpack Compose
- Material 3
- Gradle

La aplicación utiliza una sola `Activity` y las pantallas se manejan con una pila propia en `PantallaPrincipal.kt` (sin Navigation Compose).

También se utiliza `NavigationSuiteScaffold` para adaptar la navegación dependiendo del dispositivo. En teléfonos se muestra una barra de navegación inferior y en pantallas más grandes se utiliza un menú lateral.

Los usuarios, tutores, horarios, solicitudes y mensajes vienen del backend (`backend/`, Spring Boot + PostgreSQL). La app inicia sesión con JWT y guarda el token para no pedir la contraseña al volver a abrirla.

## Cómo levantar el backend

Se necesita JDK 21 y PostgreSQL.

1. Crear la base de datos:

   ```bash
   createdb tutorias_db
   ```

2. Copiar `backend/.env.example` a `backend/.env` y llenar los valores:

   | Variable | Para qué |
   |---|---|
   | `DB_USERNAME`, `DB_PASSWORD` | Usuario de PostgreSQL. |
   | `JWT_SECRET` | Base64 de al menos 32 bytes (`openssl rand -base64 32`). |
   | `DEMO_PASSWORD` | Contraseña de las cuentas demo. |
   | `DB_URL` | Opcional. Por defecto `jdbc:postgresql://localhost:5432/tutorias_db`. |

3. Arrancar con el perfil `dev`:

   ```bash
   cd backend
   ./gradlew bootRun --args='--spring.profiles.active=dev'
   ```

El servidor queda en el puerto `8081`. Flyway crea las tablas al arrancar. El perfil `dev` además:

- Crea las cuentas demo (todas con `DEMO_PASSWORD`): estudiante `derrick.ramirez@alu.uabcs.mx`, coordinación `lpena@uabcs.mx` y los tutores `mcota@uabcs.mx`, `samador@uabcs.mx`, `lgeraldo@uabcs.mx` y `rcastro@uabcs.mx`.
- Escribe en la consola los correos (códigos de registro y de recuperación) en lugar de enviarlos.
- Muestra el SQL en la consola.

Las pruebas del backend levantan su propio PostgreSQL con Testcontainers, así que solo necesitan Docker:

```bash
cd backend && ./gradlew test
```

## Cómo ejecutar el proyecto

Para ejecutar el proyecto se necesita Android Studio.

1. Clonar o descargar el repositorio.
2. Abrir la carpeta del proyecto en Android Studio.
3. Esperar a que Gradle termine de sincronizar las dependencias.
4. Seleccionar un emulador o conectar un dispositivo Android.
5. Ejecutar la configuración `app`.

En el emulador la app busca el backend en `http://10.0.2.2:8081`, que apunta a tu computadora. Para usar un teléfono físico en la misma red Wi-Fi, agrega a `local.properties` la IP de tu computadora y vuelve a compilar:

```properties
api.url=http://192.168.1.50:8081
```

También se puede compilar desde la terminal con:

```bash
./gradlew :app:assembleDebug
```

Para ejecutar las pruebas:

```bash
./gradlew :app:testDebugUnitTest
```

## Cuentas de prueba

Las crea el backend con el perfil `dev` (ver [Cómo levantar el backend](#cómo-levantar-el-backend)). También se puede crear una cuenta de estudiante desde la app con un correo `@alu.uabcs.mx`.

## Estructura del proyecto

```text
app/src/main/java/mx/uabcs/tutorias/
├── MainActivity.kt
├── data/
│   ├── Api.kt            Llamadas al backend, sesión guardada y sincronización
│   ├── Avisos.kt         Tipos de notificación
│   ├── Cuentas.kt        Usuario y validación de correos institucionales
│   └── Tutorias.kt       Modelos y reglas (TutoriasRepo)
└── ui/
    ├── theme/Tema.kt
    ├── Componentes.kt    Botones, campos de texto, marca UABCS y pie legal
    ├── InicioSesion.kt
    ├── Cuenta.kt         Registro y recuperación de contraseña
    ├── PantallaPrincipal.kt  Navegación de los tres roles
    ├── Mensajes.kt       Conversaciones y chat por solicitud
    ├── Ayuda.kt
    ├── notificaciones/
    ├── estudiante/
    ├── tutor/
    └── coordinacion/

backend/                  API en Spring Boot + PostgreSQL (ver "Cómo levantar el backend")
```

`TutoriasRepo` (`data/Tutorias.kt`) guarda el estado de la app y aplica las reglas (anticipación de 24 h, horarios libres, lista de espera, avisos, resumen de Coordinación). Las pantallas lo cambian al instante y cada cambio se manda al servidor, que vuelve a revisar las mismas reglas.

### Carpeta `estudiante`

Buscar tutores, ver su perfil y horarios, crear una solicitud (con archivo adjunto), consultar las citas en lista, semana o mes, cambiarlas o cancelarlas. `Comunes.kt` tiene los componentes que usan los tres roles (`Pantalla`, `Tarjeta`, `AvatarIniciales`, `BadgeEstado`, `DatoFila`…).

### Carpeta `tutor`

Inicio con lo que hay por atender, solicitudes (aceptar o rechazar con motivo), sesiones, detalle y registro de la sesión, y disponibilidad semanal.

### Carpeta `coordinacion`

Panel con el resumen del mes, sesiones que los estudiantes autorizaron (con su contenido académico), directorio de tutores y perfil.

## Estado actual

Los tres roles inician sesión contra el backend y sus datos se guardan en el servidor.

- **Estudiante**: busca tutores, crea solicitudes, se une a la lista de espera, cambia o cancela citas, recibe avisos y recordatorios, y escribe al tutor.
- **Tutor**: atiende solicitudes, administra su disponibilidad, cancela y registra sesiones, y escribe al estudiante.
- **Coordinación**: ve el resumen del mes, las sesiones autorizadas y el directorio de tutores.

## Cosas que faltan

La lista completa y quién la tiene está en `Pendientes.md`. Lo principal:

- Perfil del estudiante y del tutor, y aviso de privacidad.
- "Proponer otro horario" y el registro completo de la sesión (actividades, materiales, borrador).
- Avisos institucionales publicados por Coordinación.
- Notificaciones push.
- Fuente e íconos del diseño de Figma.

## Sobre el proyecto

Este proyecto se encuentra en desarrollo y forma parte de un proyecto universitario de la UABCS.

La intención es ir implementando los diferentes módulos de acuerdo con los requerimientos definidos para el sistema y utilizar el prototipo de Figma como referencia para mantener consistencia entre el diseño y la aplicación.