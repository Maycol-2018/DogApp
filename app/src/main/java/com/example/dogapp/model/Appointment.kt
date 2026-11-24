package com.example.dogapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// (Yair) HU3: se agregaron los campos nuevos de la cita (raza, propietario, teléfono y foto)
@Entity(tableName = "appointments")
data class Appointment(

    // El "turno" que se muestra en el Home: lo genera Room solo (autoGenerate)
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val petName: String,
    val symptomDescription: String,
    val petImageName: String = "dog_head",

    // (Yair) HU3: datos nuevos que se capturan en "Nueva Cita".
    // Tienen valor por defecto para que el código de HU2 (citas de ejemplo) siga compilando.
    // Raza elegida en el AutoCompleteTextView (viene de la API dog.ceo)
    val breed: String = "",
    // Nombre del propietario (máx. 30 caracteres en el formulario)
    val ownerName: String = "",
    // Teléfono de contacto (solo números, máx. 10)
    val ownerPhone: String = "",
    // URL de la foto de la raza que devuelve la API; Glide la carga en el Home
    val petImageUrl: String = ""
)