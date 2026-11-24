package com.example.dogapp.webservice

// (Yair) HU3: punto de acceso al ApiService. El Repository llama ApiUtils.getApiService()
// y así no necesita saber cómo se construye Retrofit.
class ApiUtils {

    // companion object = se llama sin crear un ApiUtils: ApiUtils.getApiService()
    companion object {
        fun getApiService(): ApiService {
            return RetrofitClient.apiService
        }
    }
}