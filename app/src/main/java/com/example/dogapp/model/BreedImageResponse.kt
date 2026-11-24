package com.example.dogapp.model

import com.google.gson.annotations.SerializedName

// (Yair) HU3: respuesta de las fotos de dog.ceo (breed/{breed}/images/random y breeds/image/random)
// El JSON llega así: {"message": "https://images.dog.ceo/breeds/akita/foto.jpg", "status": "success"}
// Aquí "message" es un solo texto: la URL de la foto.
data class BreedImageResponse(
    @SerializedName("message")
    val message: String,

    @SerializedName("status")
    val status: String
)