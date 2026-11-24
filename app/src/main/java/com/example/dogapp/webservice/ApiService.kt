package com.example.dogapp.webservice

import com.example.dogapp.model.BreedImageResponse
import com.example.dogapp.model.BreedsResponse
import com.example.dogapp.utils.Constants
import retrofit2.http.GET
import retrofit2.http.Path

// (Yair) HU3: interfaz de Retrofit. Cada función es un endpoint de la API dog.ceo.
// Son "suspend" para llamarlas desde una corrutina sin bloquear la pantalla.
interface ApiService {

    // Lista de todas las razas -> llena el AutoCompleteTextView de "Nueva Cita"
    @GET(Constants.END_POINT_BREEDS)
    suspend fun getBreeds(): BreedsResponse

    // Foto aleatoria de la raza elegida. {breed} se reemplaza por el valor de @Path("breed")
    @GET(Constants.END_POINT_BREED_IMAGE)
    suspend fun getBreedImage(@Path("breed") breed: String): BreedImageResponse

    // Foto aleatoria general: respaldo si la raza no tiene foto
    @GET(Constants.END_POINT_RANDOM_IMAGE)
    suspend fun getRandomImage(): BreedImageResponse
}