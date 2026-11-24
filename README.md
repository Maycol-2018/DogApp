# DogApp

Aplicación móvil Android para gestionar citas de una veterinaria. El proyecto se desarrolla con Kotlin, XML, Room, Navigation Component, ViewModel, Repository, Coroutines, LiveData, RecyclerView y BiometricPrompt.

## Estado actual

La rama de trabajo actual es `maycol`. Las siguientes historias de usuario están implementadas:

- HU1: Login con autenticación biométrica.
- HU2: Home del administrador de citas.
- HU4: Detalle de la cita (rama `allan`).
- HU5: Editar cita (rama `joel`).

## HU1: Login con biometría

La pantalla inicial contiene:

- Fondo gris oscuro y tema sin toolbar.
- Imagen transparente del perro en la parte superior derecha.
- Título `DogApp` con fuente externa descargable `Lilita One`.
- Animación Lottie de huella digital.
- Diálogo nativo de `BiometricPrompt` con título, subtítulo y botón cancelar.
- Mensajes para biometría no disponible, huella no registrada o huella incorrecta.
- Navegación hacia Home después de una autenticación exitosa.
- Ícono de la aplicación usando la imagen real del perro.

Recursos principales:

```text
app/src/main/res/drawable/dog_head.png
app/src/main/assets/fingerprint_animation.json
app/src/main/res/font/dogapp_display.xml
```

La animación debe conservar el nombre `fingerprint_animation.json`. La imagen del perro debe conservar el nombre `dog_head.png` para que las referencias actuales sigan funcionando.

## HU2: Home Administrador de Citas

La pantalla de Home contiene:

- Header gris oscuro con esquina inferior izquierda redondeada.
- Título `Administrador de Citas`.
- Imagen del perro y texto `DogApp` en el header.
- Lista de citas debajo del header mediante `RecyclerView`.
- Tarjetas blancas con bordes redondeados, elevación y avatar circular.
- Nombre de la mascota y descripción del síntoma.
- Turno mostrado como `#id`, usando el ID autogenerado por Room.
- Botón cuadrado rosado con bordes redondeados y signo `+` centrado.
- Navegación del botón hacia HU3.
- Navegación de cada tarjeta hacia HU4.
- El botón atrás del sistema desde Home envía la aplicación al escritorio del dispositivo.

Mientras se desarrolla HU3, el Repository crea tres citas de ejemplo únicamente cuando la base de datos está vacía:

```text
Cory  - Fractura extremidad
Zeus  - Solo duerme
Rocky - No come
```

Estos datos sirven para visualizar la interfaz. Más adelante HU3 deberá permitir crear citas reales desde la aplicación.

## Arquitectura

El proyecto usa una arquitectura por capas inspirada en MVC, implementada en la práctica como un MVVM sencillo con Repository:

```text
Fragment / View
        |
        v
ViewModel
        |
        v
Repository
        |
        +--> Room / SQLite local
        |
        +--> Retrofit / API remota futura
```

La interfaz no debe acceder directamente a Room ni a Retrofit.

### Estructura recomendada

```text
app/src/main/java/com/example/dogapp/
├── data/
│   ├── AppDatabase.kt
│   └── AppointmentDao.kt
├── model/
│   └── Appointment.kt
├── repository/
│   └── AppointmentRepository.kt
├── utils/
├── view/
│   ├── adapter/
│   │   └── AppointmentAdapter.kt
│   ├── fragment/
│   │   ├── LoginFragment.kt
│   │   ├── HomeAdminFragment.kt
│   │   ├── NewAppointmentFragment.kt
│   │   └── AppointmentDetailFragment.kt
│   └── viewholder/
├── viewmodel/
│   └── AppointmentViewModel.kt
└── webservice/
```

### Responsabilidad de cada paquete

#### `model/`

Contiene las entidades del dominio y los DTOs de servicios remotos.

`Appointment` es actualmente la entidad Room principal:

```kotlin
@Entity(tableName = "appointments")
data class Appointment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val petName: String,
    val symptomDescription: String,
    val petImageName: String = "dog_head"
)
```

Las entidades no deben contener lógica de interfaz.

#### `data/`

Contiene Room:

- `AppDatabase`: base de datos singleton.
- `AppointmentDao`: consultas y operaciones de persistencia.

Las operaciones de Room deben ser `suspend` o devolver `Flow`/`LiveData`. No deben ejecutarse en el hilo principal.

#### `repository/`

Es el único punto de acceso del ViewModel a las fuentes de datos. Aquí se combinan Room, Retrofit u otras fuentes futuras.

El Fragment no debe llamar directamente a un DAO ni a un servicio Retrofit.

#### `viewmodel/`

Coordina las acciones de la pantalla y expone el estado observable. Debe utilizar `viewModelScope` para coroutines y mantener las referencias de datos fuera del ciclo de vida de la vista.

Patrón recomendado:

```kotlin
private val _state = MutableLiveData<UiState>()
val state: LiveData<UiState> = _state
```

El ViewModel no debe contener referencias a `View`, `TextView`, `ViewBinding`, Activity ni Fragment.

#### `view/`

Contiene la interfaz:

- Un Fragment por pantalla.
- Un Adapter por lista.
- Un ViewHolder cuando la lista lo requiera.
- Un layout XML por pantalla o componente reutilizable.

Los Fragments deben configurar listeners, observar el ViewModel y actualizar la interfaz. La lógica de negocio debe permanecer en Repository/ViewModel.

#### `webservice/`

Se utilizará para futuras APIs REST:

```text
ApiService.kt
RetrofitClient.kt
ApiUtils.kt
```

Las URL base y endpoints deben centralizarse en `utils/Constants.kt`. No deben escribirse dentro de un Fragment.

#### `utils/`

Contiene constantes, claves de navegación, nombres de base de datos y configuraciones compartidas.

## Navegación actual

La navegación está declarada en:

```text
app/src/main/res/navigation/nav_graph.xml
```

Flujo actual:

```text
LoginFragment
    |
    | autenticación biométrica exitosa
    v
HomeAdminFragment
    ├── NewAppointmentFragment    [HU3]
    └── AppointmentDetailFragment [HU4]
            └── EditAppointmentFragment [HU5]
```

Al ingresar a Home, el login se elimina de la pila de navegación. Por eso, el botón atrás no retorna al login. Además, `HomeAdminFragment` maneja el botón atrás del sistema para enviar la tarea al segundo plano y mostrar el escritorio del dispositivo.

## Reglas para implementar las siguientes HUS

1. Crear primero el modelo o entidad necesaria.
2. Agregar o modificar el DAO para las operaciones de Room.
3. Implementar la operación en el Repository.
4. Exponer el estado o acción desde el ViewModel.
5. Crear el Fragment y su layout XML.
6. Declarar la pantalla y sus acciones en `nav_graph.xml`.
7. Conectar los eventos de la interfaz al ViewModel o a la navegación.
8. Ejecutar operaciones de red o base de datos con coroutines y `Dispatchers.IO` cuando corresponda.
9. Validar campos antes de guardar.
10. Mostrar estados de carga, éxito, error y lista vacía.
11. No implementar lógica de negocio directamente dentro de los layouts o Fragments.
12. No crear una segunda base de datos ni acceder a SQLite directamente fuera de Room.

## Próximas implementaciones

### HU3: Nueva Cita

Debe reemplazar `NewAppointmentFragment` y permitir:

- Capturar los datos de la mascota.
- Validar campos obligatorios.
- Guardar una nueva entidad mediante `AppointmentViewModel` y `AppointmentRepository`.
- Regresar a Home y actualizar la lista automáticamente.

El DAO deberá incorporar una operación como:

```kotlin
@Insert
suspend fun insert(appointment: Appointment): Long
```

### HU4: Detalle de la Cita (implementada)

`AppointmentDetailFragment` con el layout `fragment_appointment_detail.xml`:

- Recibe el `appointmentId` desde el Home y observa la cita en Room con `Flow` (ViewModel → Repository → DAO).
- Toolbar gris oscuro con esquinas inferiores curvas, nombre de la mascota centrado y flecha rosada que vuelve al Home.
- Tarjeta con la foto de la mascota (Glide) montada sobre una tarjeta blanca con el turno, la raza en negro bold, el síntoma, una línea rosada, el propietario y el teléfono.
- Botón flotante rosado: elimina la cita de Room y regresa al Home, donde ya no aparece.
- Botón flotante azul oscuro: navega a `EditAppointmentFragment` (HU5) enviando el `appointmentId`.

Operaciones agregadas al DAO:

```kotlin
@Query("SELECT * FROM appointments WHERE id = :id")
fun observeAppointment(id: Long): Flow<Appointment?>

@Delete
suspend fun delete(appointment: Appointment)
```

### HU5: Editar Cita (implementada)

`EditAppointmentFragment` con el layout `fragment_edit_appointment.xml`:

- Toolbar gris oscuro con esquinas inferiores curvas, título `Editar Cita` centrado y flecha rosada que vuelve al Detalle.
- Recibe el `appointmentId` desde el Detalle y llena el formulario con los datos actuales de la cita.
- Campos con hint gris que sube al tener el foco y borde morado: nombre (máx. 15), raza con `AutoCompleteTextView` de la API dog.ceo, propietario (máx. 30) y teléfono (solo números, máx. 10).
- Botón rosado `Editar Cita` con icono, inactivo hasta que los 4 campos estén llenos; al habilitarse el texto queda blanco bold.
- Al editar, actualiza la cita en Room y regresa al Home, donde se ve la cita modificada.
- Si la raza cambió, el Repository pide a la API una foto de la nueva raza; si es la misma, conserva la foto anterior.

Operación agregada al DAO:

```kotlin
@Update
suspend fun update(appointment: Appointment)
```

## Compilación y verificación

Desde la raíz del proyecto ejecutar:

```powershell
.\gradlew.bat :app:assembleDebug
```

La compilación debe finalizar con:

```text
BUILD SUCCESSFUL
```

También se recomienda verificar:

- Que no existan errores de recursos XML.
- Que los IDs usados por View Binding coincidan con los layouts.
- Que las rutas del `nav_graph.xml` coincidan con las clases reales.
- Que los datos se consulten desde Room.
- Que las pantallas no llamen directamente al DAO.

## Convención de commits

Los commits deben ser pequeños, relacionados y escritos en inglés usando Conventional Commits:

```text
feat: add appointment creation form
fix: validate empty appointment fields
style: refine appointment detail layout
refactor: move appointment lookup to repository
test: add appointment repository tests
build: update Room dependencies
```

No mezclar en un mismo commit una nueva funcionalidad, una refactorización grande y cambios visuales no relacionados.

## Historial relevante

```text
0b1082e build: add Room persistence support
45dc0c6 feat: add local appointment data layer
e5391c0 feat: add appointments home screen
d340eb1 fix: align appointments list and action button
f556d45 style: make add appointment button rounded
```
