package ru.nsu.converter

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import android.view.View
import android.widget.*
import android.util.Log

class MainActivity : Activity() {
    private lateinit var spinnerFromCurrency: Spinner
    private lateinit var spinnerToCurrency: Spinner
    private lateinit var editTextAmount: EditText
    private lateinit var buttonSwap: ImageButton
    private lateinit var buttonConvert: Button
    private lateinit var progressBar: ProgressBar

    private var serviceMessenger: Messenger? = null
    private var isBound = false

    private val responseHandler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                CurrencyService.ACTION_GET_CURRENCIES -> {
                    val currencies = msg.data.getStringArrayList(CurrencyService.RESULT_CURRENCIES) ?: emptyList()

                    Log.d("MainActivity", "Получены валюты: $currencies")
                    updateSpinners(currencies)
                    progressBar.visibility = View.GONE
                }
                CurrencyService.ACTION_CONVERT_CURRENCY -> {
                    val result = msg.data.getDouble(CurrencyService.RESULT_CONVERSION)
                    Log.d("MainActivity", "Результат конверсии: $result")

                    Toast.makeText(
                        this@MainActivity,
                        "Результат: $result",
                        Toast.LENGTH_SHORT
                    ).show()
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
            Log.d("MainActivity", "Service подключен")
            requestCurrencies()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            serviceMessenger = null
            isBound = false
            Log.d("MainActivity", "Service отключен")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        spinnerFromCurrency = findViewById(R.id.spinner_from_currency)
        spinnerToCurrency = findViewById(R.id.spinner_to_currency)
        editTextAmount = findViewById(R.id.et_amount)
        buttonSwap = findViewById(R.id.btn_swap)
        buttonConvert = findViewById(R.id.btn_convert)
        progressBar = findViewById(R.id.progress_bar)

        buttonSwap.setOnClickListener {
            Log.d("MainActivity", "Смена валют")
            val fromPosition = spinnerFromCurrency.selectedItemPosition
            val toPosition = spinnerToCurrency.selectedItemPosition

            spinnerFromCurrency.setSelection(toPosition)
            spinnerToCurrency.setSelection(fromPosition)
        }

        buttonConvert.setOnClickListener {
            val fromCurrency = spinnerFromCurrency.selectedItem.toString()
            val toCurrency = spinnerToCurrency.selectedItem.toString()
            val amount = editTextAmount.text.toString().toDoubleOrNull()

            if (amount != null) {
                Log.d("MainActivity", "Запрос на конвертацию: $fromCurrency -> $toCurrency, сумма: $amount")
                requestConversion(fromCurrency, toCurrency, amount)
            } else {
                Log.d("MainActivity", "Ошибка: введена неверная сумма")
                Toast.makeText(this, "Введите сумму", Toast.LENGTH_SHORT).show()
            }
        }

        if (bindService(Intent(this, CurrencyService::class.java), connection, Context.BIND_AUTO_CREATE)) {
            Log.println(Log.DEBUG,"MainActivity", "Bind service")
        } else {
            Log.println(Log.DEBUG,"MainActivity", "Cannot bind service")
        }

    }

    private fun requestCurrencies() {
        Log.d("MainActivity", "Запрос валют")
        progressBar.visibility = View.VISIBLE
        val message = Message.obtain(null, CurrencyService.ACTION_GET_CURRENCIES)
        message.replyTo = replyMessenger
        serviceMessenger?.send(message)
    }

    private fun requestConversion(from: String, to: String, amount: Double) {
        Log.d("MainActivity", "Запрос на конвертацию: $from -> $to, сумма: $amount")
        val message = Message.obtain(null, CurrencyService.ACTION_CONVERT_CURRENCY)
        message.replyTo = replyMessenger
        message.data = Bundle().apply {
            putString("from", from)
            putString("to", to)
            putDouble("amount", amount)
        }
        serviceMessenger?.send(message)
    }

    private fun updateSpinners(currencies: List<String>) {
        Log.d("MainActivity", "Обновление списка валют в спиннерах")
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            currencies
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        spinnerFromCurrency.adapter = adapter
        spinnerToCurrency.adapter = adapter
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isBound) {
            unbindService(connection)
            isBound = false
            Log.d("MainActivity", "Сервис отключен при уничтожении активности")
        }
    }
}
