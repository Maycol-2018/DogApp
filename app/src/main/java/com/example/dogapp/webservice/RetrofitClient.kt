package com.example.dogapp.webservice

import com.example.dogapp.utils.Constants
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// (Yair) HU3: construye el cliente de Retrofit. Es un "object" (singleton): solo existe uno en toda la app.
object RetrofitClient {

    // "by lazy" = se crea la primera vez que alguien lo usa, no al abrir la app
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            // URL base: a esta se le suman las rutas de ApiService (breeds/list/all, etc.)
            .baseUrl(Constants.BASE_URL)
            // Gson convierte el JSON de la API en nuestras clases (BreedsResponse, BreedImageResponse)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // Retrofit genera la implementación de la interfaz ApiService
    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}