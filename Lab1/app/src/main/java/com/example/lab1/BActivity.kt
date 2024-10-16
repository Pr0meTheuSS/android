package com.example.lab1


import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity

class BActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_b)

        val nextButton: Button = findViewById(R.id.button_next)
        nextButton.setOnClickListener {
            val intent = Intent(this, CActivity::class.java)
            startActivity(intent)
        }
    }
}
