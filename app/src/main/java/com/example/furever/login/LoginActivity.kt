package com.example.furever.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.furever.R
import com.example.furever.home.HomeActivity
import com.example.furever.register.RegisterActivity

class LoginActivity : AppCompatActivity(), LoginContract.View {

    private lateinit var presenter: LoginContract.Presenter
    private lateinit var etUsername: EditText
    private lateinit var etPass: EditText
    private lateinit var btnSignin: Button
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_login)
            
            presenter = LoginPresenter(this)

            etUsername = findViewById(R.id.username1)
            etPass = findViewById(R.id.password1)
            btnSignin = findViewById(R.id.btn_signin)
            progressBar = findViewById(R.id.login_progress)
            val btnNavSignup = findViewById<Button>(R.id.btn_nav_signup)

            btnSignin.setOnClickListener {
                val username = etUsername.text.toString().trim()
                val password = etPass.text.toString()
                
                Log.d("LoginActivity", "Button clicked for: $username")
                Toast.makeText(this, "SIGN IN CLICKED", Toast.LENGTH_SHORT).show()
                
                presenter.doLogin(username, password)
            }

            btnNavSignup.setOnClickListener { navigateToRegister() }
            
        } catch (e: Exception) {
            Log.e("LoginActivity", "Error in onCreate", e)
            Toast.makeText(this, "Layout Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun showLoading() {
        runOnUiThread {
            Toast.makeText(this, "Connecting to server...", Toast.LENGTH_SHORT).show()
            progressBar.visibility = View.VISIBLE
            btnSignin.isEnabled = false
        }
    }

    override fun hideLoading() {
        runOnUiThread {
            progressBar.visibility = View.GONE
            btnSignin.isEnabled = true
        }
    }

    override fun onLoginSuccess() {
        runOnUiThread {
            Log.d("LoginActivity", "Login Success! Redirecting...")
            Toast.makeText(this, "LOGIN SUCCESS! GOING TO HOME...", Toast.LENGTH_LONG).show()
            
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    override fun onLoginError(message: String) {
        runOnUiThread {
            Log.e("LoginActivity", "Login Error: $message")
            Toast.makeText(this, "ERROR: $message", Toast.LENGTH_LONG).show()
        }
    }

    override fun navigateToRegister() {
        startActivity(Intent(this, RegisterActivity::class.java))
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.onDestroy()
    }
}
