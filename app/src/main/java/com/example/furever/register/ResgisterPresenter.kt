package com.example.furever.register

import android.util.Log
import com.example.furever.network.RegisterRequest
import com.example.furever.network.RetrofitClient
import kotlinx.coroutines.*

class RegisterPresenter(private var view: RegisterContract.View?) : RegisterContract.Presenter {

    private val presenterScope = CoroutineScope(Dispatchers.Main + Job())

    override fun registerUser(email: String, name: String, username: String, pass: String, address: String) {
        if (email.isEmpty() || pass.isEmpty() || username.isEmpty()) {
            view?.onRegistrationError("Username, Email, and Password are required")
            return
        }

        view?.showLoading("Creating account...")

        presenterScope.launch {
            try {
                // Create the request object matching your Spring Boot UserEntity
                val registerRequest = RegisterRequest(
                    name = name.ifEmpty { null },
                    lName = null, // Backend has l_name, adding as null for now
                    username = username,
                    password = pass,
                    email = email,
                    address = address.ifEmpty { null }
                )

                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.register(registerRequest)
                }

                if (response.isSuccessful && response.body()?.success == true) {
                    view?.onRegistrationSuccess()
                    view?.clearFields()
                    delay(1500)
                    view?.navigateToLogin()
                } else {
                    // Handle specific backend error messages (e.g., "Username already taken")
                    val errorMsg = response.body()?.message ?: "Registration failed"
                    view?.onRegistrationError(errorMsg)
                }

            } catch (e: Exception) {
                Log.e("RegisterPresenter", "Registration error: ${e.message}", e)
                view?.onRegistrationError("Cannot reach server. Check your connection!")
            }
        }
    }

    override fun onDestroy() {
        view = null
        presenterScope.cancel()
    }
}
