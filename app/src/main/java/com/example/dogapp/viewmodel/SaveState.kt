package com.example.dogapp.viewmodel

// (Yair) HU3: estados posibles al guardar una cita nueva.
// "sealed class" = solo existen estos 4 estados, no puede haber otros.
// El Fragment los observa y cambia la pantalla según el estado.
sealed class SaveState {
    // Todavía no se ha intentado guardar (estado inicial)
    object Idle : SaveState()

    // Guardando: se muestra el ProgressBar y se bloquea el botón
    object Loading : SaveState()

    // Guardó bien: se muestra el mensaje de éxito y se vuelve a Home
    object Success : SaveState()

    // Falló: se muestra el mensaje de error y el usuario puede reintentar
    object Error : SaveState()
}