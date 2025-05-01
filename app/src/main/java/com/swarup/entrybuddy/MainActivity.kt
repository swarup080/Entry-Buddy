package com.swarup.entrybuddy

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.swarup.entrybuddy.admin.ui.AdminDashboardActivity

class MainActivity : AppCompatActivity() {
    private lateinit var sharedPrefManager: SharedPrefManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        sharedPrefManager = SharedPrefManager(this)

        Handler(Looper.getMainLooper()).postDelayed({
            val role = sharedPrefManager.getUserRole() // Get role from SharedPreferences
            Log.d("MainActivity", "Retrieved role: $role") // Debug log

            if (!role.isNullOrEmpty()) {
                checkRoleAndNavigate(role) // Navigate based on role
            } else {
                // If no role is found (meaning no user is logged in), navigate to AuthActivity
                startActivity(Intent(this, AuthActivity::class.java))
                finish()
            }
        }, 3000)
    }

    private fun checkRoleAndNavigate(role: String) {
        when (role) {
            "Admin" -> {
                // If role is Admin, navigate to Admin Dashboard
                startActivity(Intent(this, AdminDashboardActivity::class.java))
            }
            "Resident" -> {
                // If role is Resident, navigate to Resident Dashboard
                //startActivity(Intent(this, ResidentDashboardActivity::class.java))
            }
            "Guard" -> {
                // If role is Guard, navigate to Guard Dashboard
                //startActivity(Intent(this, GuardDashboardActivity::class.java))
            }
            else -> {
                // If no valid role found, navigate to login screen
                startActivity(Intent(this, AuthActivity::class.java))
            }
        }
        finish() // Close MainActivity after navigation
    }
}
