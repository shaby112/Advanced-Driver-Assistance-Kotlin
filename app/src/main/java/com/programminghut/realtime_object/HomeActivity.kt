package com.programminghut.realtime_object

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val openCameraButton = findViewById<Button>(R.id.openCameraButton)
        openCameraButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }
}
