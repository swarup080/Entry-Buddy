package com.swarup.entrybuddy


import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.view.LayoutInflater
import android.view.Window
import com.swarup.entrybuddy.databinding.DialogInternetBinding
import com.swarup.entrybuddy.databinding.DialogLoadingBinding
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

object NetworkUtils {
    fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
            activeNetwork.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } else {
            val networkInfo = connectivityManager.activeNetworkInfo
            networkInfo != null && networkInfo.isConnected
        }
    }
}

object FileUtil {
    fun from(context: Context, uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val file = File(context.cacheDir, UUID.randomUUID().toString())
            val outputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)
            file
        } catch (e: Exception) {
            null
        }
    }
}

object DialogUtils {
    private var dialog: Dialog? = null

    fun showNoInternetDialog(context: Context) {
        if (dialog?.isShowing == true) return // Prevent multiple dialogs

        val binding = DialogInternetBinding.inflate(LayoutInflater.from(context))
        dialog = Dialog(context).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(binding.root)
            setCancelable(false) // 🚫 Prevent dismiss on back press
            setCanceledOnTouchOutside(false) // 🚫 Prevent dismiss on outside touch

            window?.setBackgroundDrawableResource(android.R.color.transparent)

            binding.btnRetry.setOnClickListener {
                if (NetworkUtils.isInternetAvailable(context)) {
                    dismiss()
                } else {
                    binding.status.text = "Still no internet 😞"
                }
            }

            show()
        }
    }
    fun showLoadingDialog(context: Context): Dialog {
        val dialog = Dialog(context)
        val binding = DialogLoadingBinding.inflate(LayoutInflater.from(context))
        dialog.setContentView(binding.root)
        dialog.setCancelable(false)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
        return dialog
    }
}