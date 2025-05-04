package com.swarup.entrybuddy.admin.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.swarup.entrybuddy.admin.model.RegisterData

class AdminViewModel(application: Application) : AndroidViewModel(application) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val dbRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("admin")

    private val _adminData = MutableLiveData<RegisterData?>()
    val adminData: MutableLiveData<RegisterData?> get() = _adminData

    fun fetchAdminData() {
        val uid = auth.currentUser?.uid ?: return

        dbRef.child(uid).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val data = snapshot.getValue(RegisterData::class.java)
                if (data != null) {
                    _adminData.postValue(data)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Log or handle error
            }
        })
    }
}
