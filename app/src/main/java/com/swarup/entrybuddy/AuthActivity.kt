package com.swarup.entrybuddy

import android.R
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.swarup.entrybuddy.admin.ui.RegisterActivity
import com.swarup.entrybuddy.databinding.ActivityAuthBinding

class AuthActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAuthBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }

        val roles = listOf("Select Role", "Admin", "Resident", "Guard")
        val adapter = ArrayAdapter(this, R.layout.simple_spinner_dropdown_item, roles)
        binding.spinnerRole.adapter = adapter
        binding.register.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

    }
}