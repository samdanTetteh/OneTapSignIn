package com.ijikod.onetapsignin

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

const val USER_TOKEN_KEY = "user_token_key"
const val USER_ID_KEY = "user_id_key"
const val IS_USER_LOGGED_IN = "in_user_logged_in"

class OneTapSignInViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {

    private val _loggedUserState = MutableStateFlow(UserUIState())
    val loggedUserState: StateFlow<UserUIState> = _loggedUserState.asStateFlow()


    fun saveUserDetails(
        userId: String? = null,
        userToken: String? = null,
        isUserLoggedIn: Boolean? = null
    ) {
        savedStateHandle[USER_ID_KEY] = userId
        savedStateHandle[USER_TOKEN_KEY] = userToken
        savedStateHandle[IS_USER_LOGGED_IN] = isUserLoggedIn
        _loggedUserState.update {
            it.copy(
                userId = savedStateHandle[USER_ID_KEY],
                userToken = savedStateHandle[USER_TOKEN_KEY],
                isUserLoggedIn = savedStateHandle[IS_USER_LOGGED_IN]
            )
        }
    }

}


data class UserUIState(
    val userToken: String? = null,
    val userId: String? = null,
    val isUserLoggedIn: Boolean? = null
)