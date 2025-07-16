package com.quiz.myquizapp.fragment.entry

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class EntryViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _authResult = MutableLiveData<Boolean?>()
    val authResult: LiveData<Boolean?> = _authResult

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun register(email: String, password: String) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _authResult.value = true
                } else {
                    _error.value = task.exception?.message ?: "Kayıt başarısız"
                }
            }
    }

    fun login(email: String, password: String) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _authResult.value = true
                } else {
                    _error.value = task.exception?.message ?: "Giriş başarısız"
                }
            }
    }

    // Durumu sıfırlamak için reset fonksiyonları
    fun resetAuthResult() {
        _authResult.value = null
    }

    fun resetError() {
        _error.value = null
    }
}
