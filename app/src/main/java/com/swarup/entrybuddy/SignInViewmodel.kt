package com.swarup.entrybuddy

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SignInViewmodel : ViewModel() {

    fun signInWithEmailAndPassword(
        email: String,
        password: String,
        context: Context,
        selectedRole: String, // Added selectedRole to validate
        onResult: (Boolean, String?, String?) -> Unit // success, error, role
    ) {
        viewModelScope.launch {
            try {
                val auth = FirebaseAuth.getInstance()
                val result = auth.signInWithEmailAndPassword(email, password).await()
                val uid = result.user?.uid ?: throw Exception("User ID not found")

                val db = FirebaseDatabase.getInstance().getReference("users").child(uid)
                val snapshot = db.get().await()
                val role = snapshot.child("role").getValue(String::class.java)
                    ?: throw Exception("Role not found")

                // Validate if the selected role matches the role from Firebase
                if (role != selectedRole) {
                    onResult(false, "Selected role does not match the stored role.", null)
                    return@launch
                }

                SharedPrefManager(context).saveUserRole(role)
                onResult(true, null, role)

            } catch (e: Exception) {
                onResult(false, e.message, null)
            }
        }
    }
}
