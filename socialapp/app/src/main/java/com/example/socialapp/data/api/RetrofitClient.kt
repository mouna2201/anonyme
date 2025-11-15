package com.example.socialapp.data.api

import com.example.socialapp.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private fun getAuthToken(forceRefresh: Boolean = false): String? {
        return try {
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                val tokenResult = runBlocking {
                    try {
                        user.getIdToken(forceRefresh).await()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        null
                    }
                }
                tokenResult?.token
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private val authInterceptor = Interceptor { chain ->
        val requestBuilder = chain.request().newBuilder()
        val request = chain.request()

        // Pour l'endpoint register, forcer le rafraîchissement du token
        val isRegisterEndpoint = request.url.encodedPath.contains("/auth/register")
        val forceRefresh = isRegisterEndpoint

        // Ajouter le token Firebase à chaque requête
        val token = getAuthToken(forceRefresh)
        token?.let {
            requestBuilder.addHeader("Authorization", "Bearer $it")
        }

        chain.proceed(requestBuilder.build())
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(Constants.BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: ApiService = retrofit.create(ApiService::class.java)
}