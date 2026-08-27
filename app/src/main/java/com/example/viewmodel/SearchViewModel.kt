package com.example.viewmodel

import androidx.lifecycle.ViewModel
import com.example.model.User
import com.example.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SearchViewModel : ViewModel() {

    private val allUsers = listOf(
        User("1", "Mohammad", "@user", R.drawable.img_profile_mohammad_1783672402325, 5),
        User("2", "Mohammadreza", "@mohre", R.drawable.img_profile_mohammad_1783672402325, 4), // Placeholder
        User("3", "Mohammad Moein", "@moein", R.drawable.img_profile_mohammad_1783672402325, 3), // Placeholder
        User("4", "Sara", "@sara", R.drawable.img_avatar_sara_1783672418392, 5)
    )

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<User>>(emptyList())
    val searchResults: StateFlow<List<User>> = _searchResults.asStateFlow()

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            _searchResults.value = emptyList()
        } else {
            _searchResults.value = allUsers.filter {
                it.name.contains(query, ignoreCase = true) || it.username.contains(query, ignoreCase = true)
            }
        }
    }
}
