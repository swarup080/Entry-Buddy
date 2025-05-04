package com.swarup.entrybuddy

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.swarup.entrybuddy.admin.ui.AdminDashboardActivity
import com.swarup.entrybuddy.admin.ui.RegisterActivity
import com.swarup.entrybuddy.databinding.ActivityAuthBinding

class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding
    private lateinit var dialog: Dialog
    private lateinit var signInViewmodel: SignInViewmodel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        signInViewmodel = ViewModelProvider(this)[SignInViewmodel::class.java]

        if (!NetworkUtils.isInternetAvailable(this)) {
            DialogUtils.showNoInternetDialog(this)
            return
        }

        setupRoleSpinner()
        setupClickListeners()
    }

    private fun setupRoleSpinner() {
        val roles = listOf("Select Role", "Admin", "Resident", "Guard")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, roles)
        binding.spinnerRole.adapter = adapter
    }

    private fun setupClickListeners() {
        binding.register.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.btnLogin.setOnClickListener {
            if (!NetworkUtils.isInternetAvailable(this)) {
                DialogUtils.showNoInternetDialog(this)
                return@setOnClickListener
            }

            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val selectedRole = binding.spinnerRole.selectedItem.toString()

            if (selectedRole == "Select Role") {
                showToast("Please select a valid role")
                return@setOnClickListener
            }

            if (!isValidEmail(email)) {
                showToast("Enter a valid email address")
                return@setOnClickListener
            }

            if (password.length < 6) {
                showToast("Password must be at least 6 characters")
                return@setOnClickListener
            }

            dialog = DialogUtils.showLoadingDialog(this)
            signInViewmodel.signInWithEmailAndPassword(
                email,
                password,
                this,
                selectedRole
            ) { success, error, role ->
                dialog.dismiss()

                if (success) {
                    Toast.makeText(this, "Login successful as $role", Toast.LENGTH_SHORT).show()

                    when (role) {
                        "Admin" -> {
                            val intent = Intent(this, AdminDashboardActivity::class.java)
                            intent.flags =
                                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        }

                        "Resident" -> //startActivity(Intent(this, ResidentHomeActivity::class.java))
                            showToast("Resident")

                        "Guard" -> //startActivity(Intent(this, GuardPanelActivity::class.java))
                            showToast("Guard")

                        else -> showToast("Unknown role: $role")
                    }

                    finish()
                } else {
                    showToast("Login failed")
                }
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        val regex = Regex("^[A-Za-z](.*)([@]{1})(.{1,})(\\.)(.{1,})")
        return email.matches(regex)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
