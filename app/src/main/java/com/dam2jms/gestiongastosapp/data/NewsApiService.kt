package com.dam2jms.gestiongastosapp.data

import com.dam2jms.gestiongastosapp.states.NewsResponse
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface NewsApiService {
    companion object {
        private const val TIMEOUT_SECONDS = 30L

        fun create(): NewsApiService {
            val client = OkHttpClient.Builder()
                .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .build()

            return Retrofit.Builder()
                .baseUrl("https://newsapi.org/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(NewsApiService::class.java)
        }
    }

    @GET("v2/top-headlines")
    suspend fun getFinancialNews(
        @Query("category") category: String = "business",
        @Query("country") country: String,
        @Query("pageSize") pageSize: Int = 20,
        @Query("apiKey") apiKey: String
    ): Response<NewsResponse>

    @GET("v2/everything")
    suspend fun getGlobalFinancialNews(
        @Query("q") query: String = "(finance OR business OR economics) AND (market OR trading OR investment)",
        @Query("language") language: String,
        @Query("sortBy") sortBy: String = "publishedAt",
        @Query("pageSize") pageSize: Int = 20,
        @Query("apiKey") apiKey: String
    ): Response<NewsResponse>
}


