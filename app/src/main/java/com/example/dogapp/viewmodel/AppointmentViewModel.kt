package com.example.dogapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.dogapp.data.AppDatabase
import com.example.dogapp.repository.AppointmentRepository
import kotlinx.coroutines.launch

class AppointmentViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AppointmentRepository(
        AppDatabase.getInstance(application).appointmentDao()
    )

    val appointments = repository.observeAppointments().asLiveData()

    init {
        viewModelScope.launch {
            repository.seedDefaultAppointmentsIfEmpty()
        }
    }
}
