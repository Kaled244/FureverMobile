package com.example.furever.login

interface LoginContract {
    interface View {
        fun showLoading()
        fun hideLoading()
        fun onLoginSuccess()
        fun onLoginError(message: String)
        fun navigateToRegister()
    }

    interface Presenter {
        fun navigateToHome()
        fun doLogin(username: String, pass: String)
        fun onDestroy()
    }
}
