package ru.nsu.converter

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

const val apiKey = "p4lOWa0Drd0N9ubar8pknUDFKSmpFJxk";

interface CurrencyApi {
    @Headers("apikey: $apiKey")
    @GET("symbols")
    fun getCurrencies(): Call<SymbolsResponse>

    @Headers("apikey: $apiKey")
    @GET("convert")
    fun convertCurrency(
        @Query("from") from: String,
        @Query("to") to: String,
        @Query("amount") amount: Double
    ): Call<ConversionResponse>
}

data class SymbolsResponse(
    val symbols: Map<String, String>
)

data class ConversionResponse(
    val result: Double
)
