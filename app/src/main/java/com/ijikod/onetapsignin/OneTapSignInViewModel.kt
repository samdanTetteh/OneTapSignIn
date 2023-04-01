package com.ijikod.onetapsignin

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class OneTapSignInViewModel(private val savedStateHandle: SavedStateHandle): ViewModel() {

    private val _isUserLoggedInState = MutableStateFlow(false)
    val isUserLoggedIn: StateFlow<Boolean> = _isUserLoggedInState.asStateFlow()


    fun saveLoginStatus(isLoggedIn: Boolean) {
        savedStateHandle["isLoggedIn"] = isLoggedIn
        _isUserLoggedInState.update { savedStateHandle["isLoggedIn"]!! }
    }

}


