package com.example.seawarsgenerator

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val field = findViewById<FieldView>(R.id.field)
        field.generate()
        findViewById<Button>(R.id.button).setOnClickListener {
            field.generate()
        }
    }


}