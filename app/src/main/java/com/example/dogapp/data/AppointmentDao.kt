package com.example.dogapp.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.dogapp.model.Appointment
import kotlinx.coroutines.flow.Flow

@Dao
interface AppointmentDao {

    @Query("SELECT * FROM appointments ORDER BY id ASC")
    fun observeAppointments(): Flow<List<Appointment>>

    @Query("SELECT COUNT(*) FROM appointments")
    suspend fun countAppointments(): Int

    @Insert
    suspend fun insertAll(appointments: List<Appointment>)

    // (Yair) HU3: guarda UNA cita nueva y devuelve el id (turno) que le asignó Room
    @Insert
    suspend fun insert(appointment: Appointment): Long

    // (Allan) HU4: observa UNA cita por su id para la ventana de detalle.
    // Es Flow para que la pantalla se actualice sola si la cita cambia (por ejemplo al editarla en HU5).
    // Devuelve null cuando la cita ya no existe (por ejemplo justo después de eliminarla).
    @Query("SELECT * FROM appointments WHERE id = :id")
    fun observeAppointment(id: Long): Flow<Appointment?>

    // (Allan) HU4: elimina la cita de la base de datos (botón rosado del detalle)
    @Delete
    suspend fun delete(appointment: Appointment)
}
