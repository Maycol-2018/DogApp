package com.example.dogapp.repository

import com.example.dogapp.data.AppointmentDao
import com.example.dogapp.model.Appointment
import kotlinx.coroutines.flow.Flow

// Yair
import com.example.dogapp.webservice.ApiService
import com.example.dogapp.webservice.ApiUtils
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class AppointmentRepository(
    private val appointmentDao: AppointmentDao,
    // (Yair) HU3: servicio de red para consultar dog.ceo. Tiene valor por defecto, así el código de Maycol que crea el Repository con un solo parámetro sigue funcionando sin cambios.
    private val apiService: ApiService = ApiUtils.getApiService()
) {


    fun observeAppointments(): Flow<List<Appointment>> =
        appointmentDao.observeAppointments()

    suspend fun seedDefaultAppointmentsIfEmpty() {
        if (appointmentDao.countAppointments() == 0) {
            appointmentDao.insertAll(
                listOf(
                    Appointment(
                        petName = "Cory",
                        symptomDescription = "Fractura extremidad"
                    ),
                    Appointment(
                        petName = "Zeus",
                        symptomDescription = "Solo duerme"
                    ),
                    Appointment(
                        petName = "Rocky",
                        symptomDescription = "No come"
                    )
                )
            )
        }
    }

    // (Yair) HU3: pide a la API la lista de razas para el AutoCompleteTextView.
    // Devuelve solo los nombres (las claves del Map), ordenados y con la primera letra en mayúscula.
    // Si falla la red devuelve lista vacía para que la app no se cierre.
    suspend fun getBreeds(): List<String> = withContext(Dispatchers.IO) {
        try {
            apiService.getBreeds().message.keys
                .sorted()
                .map { breed -> breed.replaceFirstChar { it.uppercase() } }
        } catch (e: CancellationException) {
            throw e // una corrutina cancelada no es un error de red: se deja propagar
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // (Yair) HU3: guarda la cita nueva. Primero busca la foto de la raza y luego inserta en Room.
    // Devuelve el id que asigna Room (el "turno").
    suspend fun createAppointment(
        petName: String,
        breed: String,
        ownerName: String,
        ownerPhone: String,
        symptom: String
    ): Long = withContext(Dispatchers.IO) {
        val imageUrl = getBreedImageUrl(breed)
        appointmentDao.insert(
            Appointment(
                petName = petName,
                symptomDescription = symptom,
                breed = breed,
                ownerName = ownerName,
                ownerPhone = ownerPhone,
                petImageUrl = imageUrl
            )
        )
    }

    // (Allan) HU4: la cita seleccionada en el Home, para mostrar su detalle
    fun observeAppointment(id: Long): Flow<Appointment?> =
        appointmentDao.observeAppointment(id)

    // (Allan) HU4: elimina la cita de Room (en un hilo de fondo, no en el de la pantalla)
    suspend fun deleteAppointment(appointment: Appointment) = withContext(Dispatchers.IO) {
        appointmentDao.delete(appointment)
    }

    // (Yair) HU3: foto de la raza elegida; si la API no la encuentra usa una foto aleatoria general.
    // Si también falla (sin internet) devuelve "" y la cita se guarda igual, con la imagen por defecto.
    private suspend fun getBreedImageUrl(breed: String): String {
        return try {
            apiService.getBreedImage(breed.trim().lowercase()).message
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            try {
                apiService.getRandomImage().message
            } catch (e2: CancellationException) {
                throw e2
            } catch (e2: Exception) {
                e2.printStackTrace()
                ""
            }
        }
    }
}
