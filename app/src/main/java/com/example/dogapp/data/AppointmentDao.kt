package com.example.dogapp.data

import androidx.room.Dao
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
}
