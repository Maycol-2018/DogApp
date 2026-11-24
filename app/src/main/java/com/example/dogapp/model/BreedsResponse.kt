package com.example.dogapp.model

import com.google.gson.annotations.SerializedName

// (Yair) HU3: respuesta de la lista de razas. "message" es un mapa raza -> sub-razas
data class BreedsResponse(
    @SerializedName("message")
    val message: Map<String, List<String>>,

    @SerializedName("status")
    val status: String
)