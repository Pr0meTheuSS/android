package ru.nsu.converter


import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import android.util.Log

class CurrencyInteractor(
    private val context: Context,
    private val onCurrenciesLoaded: (List<String>) -> Unit,
    private val onConversionResult: (Double) -> Unit
) {
    private var serviceMessenger: Messenger? = null
    private var isBound = false

    private val responseHandler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                CurrencyService.ACTION_GET_CURRENCIES -> {
                    val currencies = msg.data.getStringArrayList(CurrencyService.RESULT_CURRENCIES) ?: emptyList()
                    onCurrenciesLoaded(currencies)
                }
                CurrencyService.ACTION_CONVERT_CURRENCY -> {
                    val result = msg.data.getDouble(CurrencyService.RESULT_CONVERSION)
                    onConversionResult(result)
                }
                else -> super.handleMessage(msg)
            }
        }
    }

    private val replyMessenger = Messenger(responseHandler)

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            serviceMessenger = Messenger(service)
            isBound = true
            Log.d("CurrencyInteractor", "Сервис подключен")
            requestCurrencies()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            serviceMessenger = null
            isBound = false
            Log.d("CurrencyInteractor", "Сервис отключен")
        }
    }

    fun bindService() {
        val intent = Intent(context, CurrencyService::class.java)
        if (!context.bindService(intent, connection, Context.BIND_AUTO_CREATE)) {
            Log.e("CurrencyInteractor", "Не удалось подключиться к сервису")
        }
    }

    fun unbindService() {
        if (isBound) {
            context.unbindService(connection)
            isBound = false
            Log.d("CurrencyInteractor", "Сервис отключен")
        }
    }

    private fun requestCurrencies() {
        val message = Message.obtain(null, CurrencyService.ACTION_GET_CURRENCIES)
        message.replyTo = replyMessenger
        serviceMessenger?.send(message)
    }

    fun requestConversion(from: String, to: String, amount: Double) {
        val message = Message.obtain(null, CurrencyService.ACTION_CONVERT_CURRENCY)
        message.replyTo = replyMessenger
        message.data = Bundle().apply {
            putString("from", from)
            putString("to", to)
            putDouble("amount", amount)
        }
        serviceMessenger?.send(message)
    }
}
