package com.swarup.entrybuddy.admin.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.swarup.entrybuddy.FileUtil
import com.swarup.entrybuddy.SharedPrefManager
import com.swarup.entrybuddy.admin.model.RegisterData
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.*
import io.appwrite.Client
import io.appwrite.exceptions.AppwriteException
import io.appwrite.models.InputFile
import io.appwrite.services.Storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File

class RegisterViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val dbRef = FirebaseDatabase.getInstance().getReference("admin")
    private lateinit var sharedPrefManager: SharedPrefManager


    val registrationStatus = MutableLiveData<Boolean>()

    fun registerUser(
        context: Context,
        data: RegisterData,
        password: String,
        imageUri: Uri?
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Step 1: Register user with Firebase Auth
                val result = auth.createUserWithEmailAndPassword(data.email, password).await()
                val uid = result.user?.uid ?: return@launch
                val currentTime = System.currentTimeMillis().toString()

                // Step 2: Compress and upload image to Appwrite
                val avatarUrl = try {
                    imageUri?.let { compressAndUploadToAppwrite(context, it, uid) } ?: ""
                } catch (e: Exception) {
                    e.printStackTrace()
                    ""
                }
                Log.d("TAG", "registerUser: $avatarUrl")
                // Step 3: Save user data in Firebase Realtime Database
                val updatedData = data.copy(id = uid, avatar = avatarUrl, time = currentTime)
                dbRef.child(uid).setValue(updatedData).await()
                sharedPrefManager = SharedPrefManager(context)
                sharedPrefManager.saveUserRole(data.role)
                registrationStatus.postValue(true)

            } catch (e: Exception) {
                e.printStackTrace()
                registrationStatus.postValue(false)
            }
        }
    }

    private suspend fun compressAndUploadToAppwrite(
        context: Context,
        uri: Uri,
        uid: String
    ): String {
        val file = FileUtil.from(context, uri) ?: return ""

        val compressedFile = Compressor.compress(context, file) {
            default()
            quality(75)
            format(Bitmap.CompressFormat.JPEG)
            destination(File(context.cacheDir, "compressed_${file.name}"))
        }

        val projectId = "68033e3600032f71d622" // Replace with your project ID
        val bucketId = "680343ed002e7be07de1" // Replace with your bucket ID
        val client = Client(context)
            .setEndpoint("https://fra.cloud.appwrite.io/v1")
            .setProject(projectId)

        val storage = Storage(client)

        return try {
            val fileUpload = storage.createFile(
                bucketId = bucketId,
                fileId = "unique()",
                file = InputFile.fromFile(compressedFile)
            )

            // Construct and return the view URL
            "${client.endpoint}/storage/buckets/${bucketId}/files/${fileUpload.id}/view?project=$projectId"

        } catch (e: AppwriteException) {
            e.printStackTrace()
            ""
        }
    }
}
