package com.example.furever.register

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.furever.R

class RegisterActivity : AppCompatActivity(), RegisterContract.View {

    private lateinit var presenter: RegisterContract.Presenter
    private lateinit var editTextEmail: EditText
    private lateinit var editTextName: EditText
    private lateinit var editTextUsername: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var editTextAddress: EditText
    private lateinit var buttonSignUp: Button
    private lateinit var buttonNavSignIn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        presenter = RegisterPresenter(this)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.register)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initializeViews()

        buttonSignUp.setOnClickListener {
            // Calling the name exactly as it appears in your Contract
            presenter.registerUser(
                editTextEmail.text.toString().trim(),
                editTextName.text.toString().trim(),
                editTextUsername.text.toString().trim(),
                editTextPassword.text.toString(),
                editTextAddress.text.toString().trim()
            )
        }

        buttonNavSignIn.setOnClickListener {
            navigateToLogin()
        }
    }

    private fun initializeViews() {
        editTextEmail = findViewById(R.id.email)
        editTextName = findViewById(R.id.name)
        editTextUsername = findViewById(R.id.username)
        editTextPassword = findViewById(R.id.password)
        editTextAddress = findViewById(R.id.address)
        buttonSignUp = findViewById(R.id.btn_signup)
        buttonNavSignIn = findViewById(R.id.btn_nav_signin)
    }

    override fun showLoading(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onRegistrationSuccess() {
        Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()
    }

    override fun onRegistrationError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun clearFields() {
        editTextEmail.text.clear()
        editTextName.text.clear()
        editTextUsername.text.clear()
        editTextPassword.text.clear()
        editTextAddress.text.clear()
    }

    override fun navigateToLogin() {
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.onDestroy()
    }
}