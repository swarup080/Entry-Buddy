package com.swarup.entrybuddy.admin.ui

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.swarup.entrybuddy.DialogUtils
import com.swarup.entrybuddy.NetworkUtils
import com.swarup.entrybuddy.R
import com.swarup.entrybuddy.admin.model.RegisterData
import com.swarup.entrybuddy.admin.viewmodel.RegisterViewModel
import com.swarup.entrybuddy.databinding.ActivityRegisterBinding


class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var viewModel: RegisterViewModel
    private var selectedImageUri: Uri? = null
    private lateinit var dialog: Dialog

    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                selectedImageUri = result.data!!.data
                binding.btnPickImage.setImageURI(selectedImageUri)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        viewModel = ViewModelProvider(this)[RegisterViewModel::class.java]

        // ✅ Check Internet at start
        if (!NetworkUtils.isInternetAvailable(this)) {
            DialogUtils.showNoInternetDialog(this)
            return
        }

        binding.btnPickImage.setOnClickListener {
            if (checkAndRequestImagePermission()) {
                val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                    type = "image/*"
                }
                imagePickerLauncher.launch(intent)
            }
        }

        binding.btnRegister.setOnClickListener {
            if (!NetworkUtils.isInternetAvailable(this)) {
                DialogUtils.showNoInternetDialog(this)
                return@setOnClickListener
            }

            val name = binding.etName.text.toString().trim()
            val phone = binding.etPhone.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val society = binding.etSociety.text.toString().trim()
            val address = binding.etAddress.text.toString().trim()

            // Email validation
            val emailRegex = Regex("^[A-Za-z](.*)([@]{1})(.{1,})(\\.)(.{1,})")
            if (!email.matches(emailRegex)) {
                Toast.makeText(this, "Enter a valid email address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Password validation
            if (password.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Phone number validation
            if (phone.length != 10 || !phone.all { it.isDigit() }) {
                Toast.makeText(this, "Enter a valid 10-digit phone number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Required fields
            if (name.isEmpty() || society.isEmpty() || address.isEmpty()) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val data = RegisterData(
                id = "",
                fullname = name,
                phone = phone,
                email = email,
                password = password,
                societyName = society,
                avatar = "",
                role = "Admin",
                address = address,
                time = ""
            )
            dialog = DialogUtils.showLoadingDialog(this)
            viewModel.registerUser(this, data, password, selectedImageUri)
        }


        viewModel.registrationStatus.observe(this) { isSuccess ->
            dialog.dismiss()
            if (isSuccess) {
                Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, AdminDashboardActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Registration failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkAndRequestImagePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                    101
                )
            }
            granted
        } else {
            val granted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    101
                )
            }
            granted
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                    type = "image/*"
                }
                imagePickerLauncher.launch(intent)
            } else {
                Toast.makeText(this, "Permission denied to access images.", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }
}
