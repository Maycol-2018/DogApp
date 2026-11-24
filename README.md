# DogApp

**DogApp** es una agenda de citas para una veterinaria, hecha como aplicación nativa Android en Kotlin.
El administrador entra con su huella digital, ve la lista de citas agendadas y puede crear, consultar,
editar y eliminar citas. Cada cita muestra una foto del perro según su raza, obtenida desde la API pública
[dog.ceo](https://dog.ceo/dog-api/).

Proyecto del curso *Desarrollo de Aplicaciones para Dispositivos Móviles* — Universidad del Valle,
Miniproyecto 1 (Sprint 1).

## ¿Qué hace la app?

| Pantalla | Historia de usuario | Qué permite |
|---|---|---|
| Login | HU1 | Entrar con la huella digital (BiometricPrompt) tocando una animación Lottie. |
| Home Administrador de Citas | HU2 | Ver todas las citas guardadas, con foto, nombre, síntoma y turno. |
| Nueva Cita | HU3 | Registrar una cita: mascota, raza (sugerida desde la API), propietario, teléfono y síntoma. |
| Detalle de la Cita | HU4 | Ver toda la información de una cita y eliminarla o ir a editarla. |
| Editar Cita | HU5 | Modificar nombre, raza, propietario y teléfono de una cita existente. |

### Flujo de uso

```text
Login (huella digital)
   │ autenticación correcta
   ▼
Home Administrador de Citas ──(botón +)──► Nueva Cita ──(Guardar)──► Home
   │ tocar una cita
   ▼
Detalle de la Cita ──(botón rosado)──► elimina la cita y vuelve al Home
   │ botón azul
   ▼
Editar Cita ──(Editar Cita)──► actualiza la cita y vuelve al Home
            ──(flecha atrás)──► vuelve al Detalle
```

- Desde el Home, el botón atrás del teléfono envía la app al escritorio (no regresa al Login).
- La lista del Home se actualiza sola cuando se crea, edita o elimina una cita, porque observa la base de datos.

## Detalle de cada pantalla

### HU1 · Login
- Fondo gris oscuro, sin toolbar.
- Cabeza de perro con fondo transparente arriba a la derecha y título `DogApp` con fuente externa (`Lilita One`).
- Animación Lottie de huella (`assets/fingerprint_animation.json`). Al tocarla aparece el diálogo nativo
  "Autenticación con Biometría" con el botón *Cancelar*.
- Si la huella es correcta navega al Home; si no, el sistema muestra su propio error.
- Ícono propio de la aplicación.

### HU2 · Home Administrador de Citas
- Header gris oscuro con la esquina inferior izquierda curva, título "Administrador de Citas" y logo de DogApp.
- `RecyclerView` con tarjetas blancas redondeadas: foto circular de la mascota (Glide), nombre, síntoma y
  turno (`# id` autogenerado por Room).
- Botón flotante rosado `#F05861` que abre Nueva Cita. Cada tarjeta abre el Detalle de la Cita.

### HU3 · Nueva Cita
- Toolbar oscura con esquinas inferiores curvas, título centrado y flecha rosada.
- Campos Material con hint que sube al enfocar y borde morado: nombre (máx. 15), raza, propietario (máx. 30)
  y teléfono (solo números, máx. 10).
- La raza es un `AutoCompleteTextView` que sugiere razas desde la 2.ª letra (API `breeds/list/all`).
- Lista de síntomas fija; si no se elige ninguno aparece "Selecciona un síntoma".
- Botón "Guardar Cita" inactivo hasta llenar los 4 campos. Al guardar, se pide una foto de la raza
  (API `breed/{raza}/images/random`), se guarda en Room y se vuelve al Home.

### HU4 · Detalle de la Cita
- Toolbar oscura con el nombre de la mascota y flecha rosada que vuelve al Home.
- Tarjeta con la foto de la mascota montada sobre una tarjeta blanca con: turno, raza en negro bold, síntoma,
  línea rosada, propietario y teléfono.
- Botón flotante rosado: elimina la cita de Room y vuelve al Home (sin diálogo de confirmación).
- Botón flotante azul oscuro: abre Editar Cita enviando el `appointmentId`.

### HU5 · Editar Cita
- Mismo diseño de formulario que Nueva Cita, con el título "Editar Cita" y los datos actuales precargados.
- Botón "Editar Cita" inactivo hasta que los 4 campos estén llenos; al habilitarse el texto queda blanco bold.
- Al editar, actualiza la cita en Room y vuelve al Home. Si cambió la raza, se pide una foto nueva de esa raza;
  si no, se conserva la foto anterior.

## Arquitectura

La app usa **MVVM con Repository**. Cada capa solo habla con la que tiene debajo:

```text
Fragment (View)        muestra la pantalla y escucha los eventos del usuario
      │ observa LiveData / llama funciones
      ▼
AppointmentViewModel   guarda el estado de la pantalla y lanza corrutinas (viewModelScope)
      │
      ▼
AppointmentRepository  único punto de acceso a los datos
      │
      ├──► Room (AppDatabase + AppointmentDao)   base de datos local SQLite
      └──► Retrofit (ApiService)                 API REST dog.ceo
```

- Los Fragments **no** acceden a Room ni a Retrofit directamente.
- Las consultas de Room devuelven `Flow`, que el ViewModel convierte a `LiveData`; por eso las pantallas se
  actualizan solas cuando cambian los datos.
- Las operaciones de red y base de datos corren en `Dispatchers.IO`.
- El guardado y la edición publican su estado con `SaveState` (`Idle`, `Loading`, `Success`, `Error`) para
  mostrar el círculo de carga y los mensajes.

### Estructura del código

```text
app/src/main/java/com/example/dogapp/
├── MainActivity.kt                  Activity única que aloja el NavHostFragment
├── data/
│   ├── AppDatabase.kt               base de datos Room (singleton)
│   └── AppointmentDao.kt            consultas: listar, buscar por id, insertar, actualizar, eliminar
├── model/
│   ├── Appointment.kt               entidad Room de la cita
│   ├── BreedsResponse.kt            respuesta de la API con la lista de razas
│   └── BreedImageResponse.kt        respuesta de la API con la foto
├── repository/
│   └── AppointmentRepository.kt     une Room y la API
├── utils/
│   └── Constants.kt                 URL base y endpoints de dog.ceo
├── view/
│   ├── adapter/AppointmentAdapter.kt
│   └── fragment/
│       ├── LoginFragment.kt               HU1
│       ├── HomeAdminFragment.kt           HU2
│       ├── NewAppointmentFragment.kt      HU3
│       ├── AppointmentDetailFragment.kt   HU4
│       └── EditAppointmentFragment.kt     HU5
├── viewmodel/
│   ├── AppointmentViewModel.kt
│   └── SaveState.kt
└── webservice/
    ├── ApiService.kt                endpoints de Retrofit
    ├── RetrofitClient.kt
    └── ApiUtils.kt
```

La navegación entre pantallas está en `app/src/main/res/navigation/nav_graph.xml` (Navigation Component,
una sola Activity con Fragments).

### Modelo de datos

```kotlin
@Entity(tableName = "appointments")
data class Appointment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,                   // turno de la cita
    val petName: String,
    val symptomDescription: String,
    val petImageName: String = "dog_head",
    val breed: String = "",
    val ownerName: String = "",
    val ownerPhone: String = "",
    val petImageUrl: String = ""        // foto de la raza obtenida de dog.ceo
)
```

### API REST (dog.ceo)

| Endpoint | Uso |
|---|---|
| `GET breeds/list/all` | Lista de razas para el autocompletado de Nueva y Editar Cita. |
| `GET breed/{raza}/images/random` | Foto aleatoria de la raza elegida. |
| `GET breeds/image/random` | Foto aleatoria general, de respaldo si la raza no tiene foto. |

## Tecnologías

- **Kotlin** y layouts **XML** con **View Binding**
- **Navigation Component** (Fragments)
- **Room** (SQLite) con **KSP**
- **ViewModel**, **LiveData**, **Flow** y **Coroutines**
- **Retrofit** + **Gson** para la API REST
- **Glide** para cargar las fotos
- **BiometricPrompt** para la huella digital
- **Lottie** para la animación del login
- **Material Components** (TextInputLayout, MaterialButton, MaterialCardView)

Requisitos: Android 7.0 (API 24) o superior. Compila con `targetSdk 37`.

## Cómo ejecutar

1. Abrir la carpeta del proyecto en Android Studio y esperar el *Gradle Sync*.
2. Conectar un dispositivo o emulador **con una huella registrada** (en el emulador:
   *Extended controls → Fingerprint*).
3. Ejecutar la configuración `app`, o desde la terminal:

```powershell
.\gradlew.bat :app:assembleDebug
```

La app necesita internet para sugerir razas y descargar las fotos. Sin conexión las citas se guardan igual,
con una imagen por defecto.

> Mientras la base de datos está vacía, el Repository crea tres citas de ejemplo (Cory, Zeus y Rocky) para
> visualizar el Home. Estas citas no tienen raza, propietario ni teléfono.

## Trabajo en equipo

Cada integrante trabaja en su propia rama creada desde `develop` y la integra con un Pull Request.

| Rama | Historias de usuario |
|---|---|
| `maycol` | HU1 Login y HU2 Home |
| `yair` | HU3 Nueva Cita |
| `allan` | HU4 Detalle de la Cita |
| `joel` | HU5 Editar Cita |

Los commits siguen *Conventional Commits* en inglés y mencionan la HU:

```text
feat(detail): show selected appointment and delete it from Room returning to Home (HU4)
feat(edit): prefill, validate and update appointment in Room returning to Home (HU5)
style(detail): match info card corners and shadow to HU4 mockup
docs: describe the project in the README
```
