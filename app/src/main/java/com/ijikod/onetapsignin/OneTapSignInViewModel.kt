package com.ijikod.onetapsignin

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

const val USER_TOKEN_KEY = "user_token_key"
const val USER_ID_KEY = "user_id_key"
class OneTapSignInViewModel(private val savedStateHandle: SavedStateHandle): ViewModel() {

    private val _loggedUserState = MutableStateFlow(UserUIState())
    val loggedUserState: StateFlow<UserUIState> = _loggedUserState.asStateFlow()


    fun saveUserDetails(user: UserUIState) {
        savedStateHandle[USER_ID_KEY] = user.userId
        savedStateHandle[USER_TOKEN_KEY] = user.userToken
        _loggedUserState.update {
            it.copy(
                userId = savedStateHandle[USER_ID_KEY],
                userToken = savedStateHandle[USER_TOKEN_KEY]
            )
        }
    }

}


data class UserUIState(val userToken: String? = null, val userId: String? = null)