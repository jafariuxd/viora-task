package com.example.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.viora.*
import com.example.network.viora.VioraNetworkModule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    
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

    private val _avatarUri = MutableStateFlow<String?>(null)
    val avatarUri: StateFlow<String?> = _avatarUri.asStateFlow()

    // UI state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _isSuccess = MutableStateFlow(false)
    val isSuccess: StateFlow<Boolean> = _isSuccess.asStateFlow()

    private val _isLoginSuccess = MutableStateFlow(false)
    val isLoginSuccess: StateFlow<Boolean> = _isLoginSuccess.asStateFlow()

    fun updateAvatarUri(uri: String?) { _avatarUri.value = uri }
    fun updateEmail(newEmail: String) { _email.value = newEmail }
    fun updateOtp(newOtp: String) { _otp.value = newOtp }
    fun updateFullName(newName: String) { _fullName.value = newName }
    fun updateUsername(newUsername: String) { _username.value = newUsername }
    fun updatePassword(newPassword: String) { _password.value = newPassword }
    fun updateDefaultDeadline(newDeadline: String) { _defaultDeadline.value = newDeadline }
    fun updateCustomDays(days: Int) { _customDays.value = days }
    fun clearError() { _errorMessage.value = null }
    fun resetSuccess() { _isSuccess.value = false; _isLoginSuccess.value = false }

    fun submitEmail() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val response = VioraNetworkModule.api.requestOtp(RequestOtpDto(email.value))
                if (response.success) {
                    _isSuccess.value = true
                } else {
                    // if it says user already exists, we should probably login instead. 
                    // for now, we pass the error message
                    _errorMessage.value = response.message
                }
            } catch (e: Exception) {
                // If 409 conflict, it means user already exists. We can login.
                if (e.message?.contains("409") == true || e.message?.contains("USER_EXISTS") == true) {
                    _errorMessage.value = "User already exists. Please login instead."
                } else {
                    _errorMessage.value = com.example.util.ErrorUtil.getErrorMessage(e)
                }
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun login() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val response = VioraNetworkModule.api.login(LoginDto(email.value, password.value))
                if (response.success && response.data != null) {
                    saveTokens(response.data)
                    _isLoginSuccess.value = true
                } else {
                    _errorMessage.value = response.message
                }
            } catch (e: Exception) {
                _errorMessage.value = "Login failed: ${com.example.util.ErrorUtil.getErrorMessage(e)}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun verifyOtp() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val days = when (defaultDeadline.value) {
                    "Daily" -> 1
                    "Weekly" -> 7
                    "Monthly" -> 30
                    "Custom" -> customDays.value
                    else -> 7
                }
                
                val avatarToSend = com.example.util.ImageUtil.toSmallBase64(getApplication(), avatarUri.value)
                val req = VerifyOtpDto(
                    email = email.value,
                    otp = otp.value,
                    fullName = fullName.value,
                    username = username.value,
                    deadline = days,
                    password = password.value,
                    avatar = avatarToSend
                )
                
                val response = VioraNetworkModule.api.verifyOtp(req)
                if (response.success && response.data != null) {
                    saveTokens(response.data)
                    saveDeadlineSettingLocally()
                    _isLoginSuccess.value = true
                } else {
                    _errorMessage.value = response.message
                }
            } catch (e: Exception) {
                _errorMessage.value = "Verification failed: ${com.example.util.ErrorUtil.getErrorMessage(e)}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    private fun saveTokens(authData: AuthResponseDto) {
        val tm = VioraNetworkModule.getTokenManager()
        tm?.saveTokens(authData.tokens.accessToken, authData.tokens.refreshToken)
        
        val prefs = getApplication<Application>().getSharedPreferences("viora_auth_prefs", Context.MODE_PRIVATE)
        val rawAvatar = authData.user.avatar ?: avatarUri.value
        val avatarToSave = if (com.example.util.ImageUtil.isLocalFileOrUriValid(rawAvatar)) rawAvatar else null
        prefs.edit()
            .putBoolean("is_registered", true)
            .putString("user_id", authData.user.id)
            .putString("user_email", authData.user.email)
            .putString("user_name", authData.user.fullName)
            .putString("user_username", authData.user.username)
            .putString("user_avatar_uri", avatarToSave)
            .apply()
    }
    
    fun completeProfile() {
        // Now handled by verifyOtpAndRegister
    }
    
    fun saveDeadlineSetting() {
        // Handled by verifyOtpAndRegister
    }
    
    private fun saveDeadlineSettingLocally() {
        val prefs = getApplication<Application>().getSharedPreferences("viora_auth_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("user_default_deadline", defaultDeadline.value)
            .putInt("user_custom_days", customDays.value)
            .apply()
    }

    fun logout() {
        viewModelScope.launch {
            try {
                VioraNetworkModule.api.logout()
            } catch (e: Exception) {
                // Ignore API failure
            }
            VioraNetworkModule.getTokenManager()?.clearTokens()
        }
    }
}
