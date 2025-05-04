package com.swarup.entrybuddy.admin.ui

import android.app.Dialog
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.swarup.entrybuddy.DialogUtils
import com.swarup.entrybuddy.NetworkUtils
import com.swarup.entrybuddy.R
import com.swarup.entrybuddy.admin.viewmodel.AdminViewModel
import com.swarup.entrybuddy.databinding.ActivityAdminDashboardBinding

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminDashboardBinding
    private lateinit var viewModel: AdminViewModel
    private lateinit var dialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Handle system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
//        if (!NetworkUtils.isInternetAvailable(this)) {
//            DialogUtils.showNoInternetDialog(this)
//            return
//        }
        viewModel = ViewModelProvider(this)[AdminViewModel::class.java]
        dialog = DialogUtils.showLoadingDialog(this)
        // Observe admin data
        viewModel.adminData.observe(this) { admin ->
            dialog.dismiss()
            if (admin != null) {
                binding.socityname.text = admin.societyName
                binding.coins.text = "🪙 ${admin.coin} "
                Glide.with(this)
                    .load(admin.avatar)
                    .placeholder(R.drawable.placeholder)
                    .into(binding.profileImg)
            }
        }

        viewModel.fetchAdminData()
    }
}
