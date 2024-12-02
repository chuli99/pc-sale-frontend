package com.example.venta

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:8080/" // URL para redirigir localhost en el emulador

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(getOkHttpClient()) // Usa el cliente con el interceptor y tiempos de espera
            .build()
    }

    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }

    private fun getOkHttpClient(): OkHttpClient {
        val token = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsImV4cCI6MTczMzIzMzA5MiwiYXV0aCI6IlJPTEVfQURNSU4gUk9MRV9VU0VSIiwiaWF0IjoxNzMzMTQ2NjkyfQ.dgs8fxjiCpyUP6sWFBZtKApaLI3P4SGk5DUKJmEv0spylWMzThOwFkkmCKVCkCE7pFKIWSUc2v0Q1OWTaE9o-A"

        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS) // Configura tiempo de espera al conectar
            .readTimeout(30, TimeUnit.SECONDS)    // Configura tiempo de espera para leer respuesta
            .writeTimeout(30, TimeUnit.SECONDS)   // Configura tiempo de espera para enviar datos
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $token") // Añade el token al header
                    .build()
                chain.proceed(request)
            }
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY // Habilita logs para depurar
            })
            .build()
    }
}

