package com.example.furever.login

import android.content.Context
import android.util.Log
import com.example.furever.network.ApiResponse
import com.example.furever.network.LoginResponse
import com.example.furever.network.RetrofitClient
import com.google.gson.Gson
import kotlinx.coroutines.*

class LoginPresenter(private var view: LoginContract.View?) : LoginContract.Presenter {

    private val presenterScope = CoroutineScope(Dispatchers.Main + Job())

    override fun doLogin(username: String, pass: String) {
        if (username.isEmpty() || pass.isEmpty()) {
            view?.onLoginError("Please enter both username and password")
            return
        }

        view?.showLoading()

        presenterScope.launch {
            try {
                val loginData = mapOf(
                    "username" to username,
                    "password" to pass
                )

                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.login(loginData)
                }

                if (response.isSuccessful) {
                    val body = response.body()
                    Log.d("LoginPresenter", "Success Body: $body")
                    
                    body?.data?.let { 
                        saveSession(it) 
                    }
                    
                    withContext(Dispatchers.Main) {
                        view?.onLoginSuccess()
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("LoginPresenter", "Error Body: $errorBody")
                    
                    val errorResponse = try {
                        Gson().fromJson(errorBody, ApiResponse::class.java)
                    } catch (e: Exception) { null }
                    
                    val message = errorResponse?.message ?: "Invalid username or password"
                    withContext(Dispatchers.Main) {
                        view?.onLoginError(message)
                    }
                }

            } catch (e: Exception) {
                Log.e("LoginPresenter", "Network Exception", e)
                withContext(Dispatchers.Main) {
                    view?.onLoginError("Cannot reach server. Check connection!")
                }
            } finally {
                withContext(Dispatchers.Main) {
                    view?.hideLoading()
                }
            }
        }
    }

    private fun saveSession(data: LoginResponse) {
        val context = (view as? Context) ?: return
        val prefs = context.getSharedPreferences("furever_prefs", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString("auth_token", data.token)
            putString("username", data.username)
            putString("user_role", data.role)
            putString("avatar_url", data.avatarUrl) // Save avatar URL
            apply()
        }
    }

    override fun navigateToHome() {
        view?.onLoginSuccess()
    }

    override fun onDestroy() {
        view = null
        presenterScope.cancel()
    }
}
