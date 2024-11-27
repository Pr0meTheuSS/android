package ru.nsu.converter

import android.app.Service
import android.content.Intent
import android.os.*
import android.util.Log

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CurrencyService : Service() {

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.apilayer.com/exchangerates_data/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val api = retrofit.create(CurrencyApi::class.java)

    companion object {
        const val ACTION_GET_CURRENCIES = 1
        const val ACTION_CONVERT_CURRENCY = 2

        const val RESULT_CURRENCIES = "result_currencies"
        const val RESULT_CONVERSION = "result_conversion"
    }

    private val messenger = Messenger(IncomingHandler())

    override fun onBind(intent: Intent?): IBinder {
        return messenger.binder
    }

    inner class IncomingHandler : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            Log.d("CurrencyService", "Полученное сообщение: ${msg.what}")
            when (msg.what) {
                ACTION_GET_CURRENCIES -> fetchCurrencies(msg.replyTo)
                ACTION_CONVERT_CURRENCY -> {
                    val data = msg.data
                    val from = data.getString("from")!!
                    val to = data.getString("to")!!
                    val amount = data.getDouble("amount")

                    fetchConversion(from, to, amount, msg.replyTo)
                }
                else -> super.handleMessage(msg)
            }
        }
    }


    private fun fetchCurrencies(replyTo: Messenger) {
        Log.d("CurrencyService", "Запрос валют...")
        api.getCurrencies().enqueue(object : Callback<SymbolsResponse> {
            override fun onResponse(call: Call<SymbolsResponse>, response: Response<SymbolsResponse>) {
                if (response.isSuccessful) {
                    val result = response.body()?.symbols?.keys?.toList() ?: emptyList()

                    Log.d("CurrencyService", "Валюты получены  успешно: $result")
                    val reply = Message.obtain(null, ACTION_GET_CURRENCIES)

                    reply.data = Bundle().apply { putStringArrayList(RESULT_CURRENCIES, ArrayList(result)) }
                    replyTo.send(reply)
                } else {
                    Log.e("CurrencyService", "Ошибка получения валют: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<SymbolsResponse>, t: Throwable) {
                Log.e("CurrencyService", "Ошибка получения валют:", t)
            }
        })
    }

    private fun fetchConversion(from: String, to: String, amount: Double, replyTo: Messenger) {
        Log.d("CurrencyService", "Converting $amount $from to $to...")

        api.convertCurrency(from, to, amount).enqueue(object : Callback<ConversionResponse> {
            override fun onResponse(call: Call<ConversionResponse>, response: Response<ConversionResponse>) {
                if (response.isSuccessful) {
                    val result = response.body()?.result ?: 0.0
                    Log.d("CurrencyService", "Конветрация прошла успешно: $result")
                    val reply = Message.obtain(null, ACTION_CONVERT_CURRENCY)

                    reply.data = Bundle().apply { putDouble(RESULT_CONVERSION, result) }
                    replyTo.send(reply)
                } else {
                    Log.e("CurrencyService", "Ошибка конветрации: ${response.errorBody()}")
                }
            }

            override fun onFailure(call: Call<ConversionResponse>, t: Throwable) {
                Log.e("CurrencyService", "Ошибка конветрации", t)
            }
        })
    }
}
