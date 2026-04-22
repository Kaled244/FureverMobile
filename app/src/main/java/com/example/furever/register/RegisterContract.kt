package com.example.furever.register

interface RegisterContract {
    interface View {
        fun showLoading(message: String)
        fun onRegistrationSuccess()
        fun onRegistrationError(message: String)
        fun clearFields()
        fun navigateToLogin()
    }

    interface Presenter {
        fun registerUser(email: String, name: String, username: String, pass: String, address: String)
        fun onDestroy()
    }
}