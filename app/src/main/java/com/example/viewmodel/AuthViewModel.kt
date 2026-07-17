package com.example.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthViewModel : ViewModel() {
    
    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()
    
    private val _otp = MutableStateFlow("")
    val otp: StateFlow<String> = _otp.asStateFlow()
    
    private val _fullName = MutableStateFlow("")
    val fullName: StateFlow<String> = _fullName.asStateFlow()
    
    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username.asStateFlow()
    
    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()
    
    private val _defaultDeadline = MutableStateFlow("Weekly")
    val defaultDeadline: StateFlow<String> = _defaultDeadline.asStateFlow()

    private val _customDays = MutableStateFlow(3)
    val customDays: StateFlow<Int> = _customDays.asStateFlow()

    fun updateEmail(newEmail: String) {
        _email.value = newEmail
    }
    
    fun updateOtp(newOtp: String) {
        _otp.value = newOtp
    }
    
    fun updateFullName(newName: String) {
        _fullName.value = newName
    }
    
    fun updateUsername(newUsername: String) {
        _username.value = newUsername
    }
    
    fun updatePassword(newPassword: String) {
        _password.value = newPassword
    }
    
    fun updateDefaultDeadline(newDeadline: String) {
        _defaultDeadline.value = newDeadline
    }
    
    fun updateCustomDays(days: Int) {
        _customDays.value = days
    }
    
    // Future backend integration functions can be added here
    fun submitEmail() {
        // Mock sending OTP
    }
    
    fun verifyOtp() {
        // Mock OTP verification
    }
    
    fun completeProfile() {
        // Mock profile completion
    }
    
    fun saveDeadlineSetting() {
        // Mock saving deadline
    }
}
