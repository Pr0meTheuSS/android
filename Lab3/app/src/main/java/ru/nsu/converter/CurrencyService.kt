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
        api.getCurrencies().enqueue(createCallback(
            onSuccess = { symbols ->
                sendMessage(replyTo, ACTION_GET_CURRENCIES) {
                    putStringArrayList(RESULT_CURRENCIES, ArrayList(symbols.symbols.keys))
                }
            },
            onError = { error ->
                Log.e("CurrencyService", "Ошибка получения валют: $error")
            }
        ))
    }

    private fun fetchConversion(from: String, to: String, amount: Double, replyTo: Messenger) {
        api.convertCurrency(from, to, amount).enqueue(createCallback(
            onSuccess = { conversion ->
                sendMessage(replyTo, ACTION_CONVERT_CURRENCY) {
                    putDouble(RESULT_CONVERSION, conversion.result ?: 0.0)
                }
            },
            onError = { error ->
                Log.e("CurrencyService", "Ошибка конвертации: $error")
            }
        ))
    }

    private fun <T> createCallback(
        onSuccess: (response: T) -> Unit,
        onError: (Throwable) -> Unit
    ): Callback<T> = object : Callback<T> {
        override fun onResponse(call: Call<T>, response: Response<T>) {
            if (response.isSuccessful) {
                response.body()?.let { onSuccess(it) }
            } else {
                Log.e("CurrencyService", "Ошибка ответа: ${response.errorBody()?.string()}")
            }
        }

        override fun onFailure(call: Call<T>, t: Throwable) {
            onError(t)
        }
    }

    private fun sendMessage(replyTo: Messenger, action: Int, configureBundle: Bundle.() -> Unit) {
        val msg = Message.obtain(null, action)
        msg.data = Bundle().apply(configureBundle)

        try {
            replyTo.send(msg)
        } catch (e: RemoteException) {
            Log.e("CurrencyService", "Ошибка отправки сообщения", e)
        }
    }
}
