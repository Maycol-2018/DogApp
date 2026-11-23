package com.example.dogapp.repository

import com.example.dogapp.data.AppointmentDao
import com.example.dogapp.model.Appointment
import kotlinx.coroutines.flow.Flow

class AppointmentRepository(private val appointmentDao: AppointmentDao) {

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
}
