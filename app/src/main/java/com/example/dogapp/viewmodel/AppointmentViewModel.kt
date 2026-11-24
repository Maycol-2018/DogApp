package com.example.dogapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.dogapp.data.AppDatabase
import com.example.dogapp.repository.AppointmentRepository
import kotlinx.coroutines.launch

// (Yair) HU3: LiveData para exponer la lista de razas y el estado del guardado a la pantalla
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class AppointmentViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AppointmentRepository(
        AppDatabase.getInstance(application).appointmentDao()
    )

    val appointments = repository.observeAppointments().asLiveData()


    // (Yair) HU3: lista de razas que llega de la API (dog.ceo) para el AutoCompleteTextView.
    // _breeds es privada y modificable (solo el ViewModel la cambia);
    // breeds es pública y de solo lectura (el Fragment solo la observa).
    private val _breeds = MutableLiveData<List<String>>()
    val breeds: LiveData<List<String>> = _breeds

    // (Yair) HU3: estado del guardado de la cita (Idle, Loading, Success o Error, ver SaveState.kt).
    // Empieza en Idle porque todavía no se ha intentado guardar nada.
    private val _saveState = MutableLiveData<SaveState>(SaveState.Idle)
    val saveState: LiveData<SaveState> = _saveState


    init {
        viewModelScope.launch {
            repository.seedDefaultAppointmentsIfEmpty()
        }
    }



    // (Yair) HU3: carga la lista de razas desde la API.
    // viewModelScope.launch = corrutina ligada al ViewModel (se cancela sola si el ViewModel muere).
    // El Fragment la llama al abrirse y observa "breeds" para llenar el AutoCompleteTextView.
    fun loadBreeds() {
        viewModelScope.launch {
            _breeds.value = repository.getBreeds()
        }
    }

    // (Yair) HU3: guarda la cita nueva y le avisa a la pantalla cómo va, usando saveState.
    // Flujo: Loading -> (repository guarda en Room con la foto de la raza) -> Success, o Error si algo falla.
    fun saveAppointment(
        petName: String,
        breed: String,
        ownerName: String,
        ownerPhone: String,
        symptom: String
    ) {
        viewModelScope.launch {
            _saveState.value = SaveState.Loading
            try {
                repository.createAppointment(petName, breed, ownerName, ownerPhone, symptom)
                _saveState.value = SaveState.Success
            } catch (e: Exception) {
                _saveState.value = SaveState.Error
            }
        }
    }

}
