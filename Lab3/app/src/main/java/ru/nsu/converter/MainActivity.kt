package ru.nsu.converter

import android.app.Activity
import android.os.*
import android.view.View
import android.widget.*

class MainActivity : Activity() {
    private lateinit var spinnerFromCurrency: Spinner
    private lateinit var spinnerToCurrency: Spinner
    private lateinit var editTextAmount: EditText
    private lateinit var buttonSwap: ImageButton
    private lateinit var buttonConvert: Button
    private lateinit var progressBar: ProgressBar

    private lateinit var currencyInteractor: CurrencyInteractor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        spinnerFromCurrency = findViewById(R.id.spinner_from_currency)
        spinnerToCurrency = findViewById(R.id.spinner_to_currency)
        editTextAmount = findViewById(R.id.et_amount)
        buttonSwap = findViewById(R.id.btn_swap)
        buttonConvert = findViewById(R.id.btn_convert)
        progressBar = findViewById(R.id.progress_bar)

        currencyInteractor = CurrencyInteractor(
            context = this,
            onCurrenciesLoaded = ::updateSpinners,
            onConversionResult = ::showConversionResult
        )

        buttonSwap.setOnClickListener { swapCurrencies() }
        buttonConvert.setOnClickListener { convertCurrency() }

        currencyInteractor.bindService()
    }

    private fun swapCurrencies() {
        val fromPosition = spinnerFromCurrency.selectedItemPosition
        val toPosition = spinnerToCurrency.selectedItemPosition

        spinnerFromCurrency.setSelection(toPosition)
        spinnerToCurrency.setSelection(fromPosition)
    }

    private fun convertCurrency() {
        val fromCurrency = spinnerFromCurrency.selectedItem.toString()
        val toCurrency = spinnerToCurrency.selectedItem.toString()
        val amount = editTextAmount.text.toString().toDoubleOrNull()

        if (amount != null) {
            currencyInteractor.requestConversion(fromCurrency, toCurrency, amount)
        } else {
            Toast.makeText(this, "Введите сумму", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateSpinners(currencies: List<String>) {
        progressBar.visibility = View.GONE
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            currencies
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        spinnerFromCurrency.adapter = adapter
        spinnerToCurrency.adapter = adapter
    }

    private fun showConversionResult(result: Double) {
        Toast.makeText(this, "Результат: $result", Toast.LENGTH_LONG).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        currencyInteractor.unbindService()
    }
}
