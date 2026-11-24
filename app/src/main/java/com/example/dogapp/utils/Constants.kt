package com.example.dogapp.utils

// (Yair) HU3: URLs de la API dog.ceo centralizadas (no se escriben dentro de los Fragments)
object Constants {
    const val BASE_URL = "https://dog.ceo/api/"
    // Lista de todas las razas (AutoCompleteTextView de "Nueva Cita")
    const val END_POINT_BREEDS = "breeds/list/all"
    // Foto aleatoria de una raza concreta; {breed} se reemplaza por la raza elegida
    const val END_POINT_BREED_IMAGE = "breed/{breed}/images/random"
    // Foto aleatoria general (respaldo si la raza no tiene foto)
    const val END_POINT_RANDOM_IMAGE = "breeds/image/random"
}