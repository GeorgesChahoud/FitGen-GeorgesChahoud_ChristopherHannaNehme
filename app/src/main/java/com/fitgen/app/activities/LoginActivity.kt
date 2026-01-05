package com.fitgen.app.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.fitgen.app.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.viewModels
import com.fitgen.app.viewmodels.UserViewModel
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var emailInputLayout: TextInputLayout
    private lateinit var emailEditText: TextInputEditText
    private lateinit var passwordInputLayout: TextInputLayout
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var loginButton: MaterialButton
    private lateinit var progressBar: ProgressBar
    private lateinit var signUpTextView: TextView
    private lateinit var forgotPasswordTextView: TextView
    
    private val userViewModel: UserViewModel by viewModels()
    private var hasNavigated = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        Log.d("LoginActivity", "════════════════════════════════════")
        Log.d("LoginActivity", "LOGIN ACTIVITY LAUNCHED")
        Log.d("LoginActivity", "This is the entry point of the app")
        Log.d("LoginActivity", "════════════════════════════════════")
        
        auth = FirebaseAuth.getInstance()
        
        // Check if user is already logged in
        val currentUser = auth.currentUser
        
        Log.d("LoginActivity", "Firebase user: ${currentUser?.uid}")
        Log.d("LoginActivity", "ViewModel isLoggedIn: ${userViewModel.isLoggedIn()}")
        Log.d("LoginActivity", "ViewModel hasOnboarding: ${userViewModel.hasCompletedOnboarding()}")
        
        if (currentUser != null) {
            // Firebase user exists, but check if we have valid local data
            val localUser = userViewModel.getCurrentUserFromPrefs()
            
            Log.d("LoginActivity", "Local user from prefs: ${localUser?.username}")
            
            if (localUser == null || localUser.username.isBlank() || localUser.uid.isBlank()) {
                // Firebase user exists but no valid local data
                // Sign out and show login screen
                Log.w("LoginActivity", "Firebase user exists but no valid local data - signing out")
                auth.signOut()
                userViewModel.clearUserData()
                // Fall through to show login screen
            } else if (userViewModel.hasCompletedOnboarding()) {
                // Valid user with completed onboarding → MainActivity
                Log.d("LoginActivity", "Auto-navigating to MainActivity")
                navigateToMain()
                return
            } else {
                // Valid user but onboarding incomplete → OnboardingActivity
                Log.d("LoginActivity", "Auto-navigating to OnboardingActivity")
                navigateToOnboarding()
                return
            }
        }
        
        // Show login screen
        Log.d("LoginActivity", "Showing login screen")
        setContentView(R.layout.activity_login)
        initViews()
        setupListeners()
    }

    private fun initViews() {
        emailInputLayout = findViewById(R.id.emailInputLayout)
        emailEditText = findViewById(R.id.emailEditText)
        passwordInputLayout = findViewById(R.id.passwordInputLayout)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)
        progressBar = findViewById(R.id.progressBar)
        signUpTextView = findViewById(R.id.signUpTextView)
        forgotPasswordTextView = findViewById(R.id.forgotPasswordTextView)
    }

    private fun setupListeners() {
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (validateInput(email, password)) {
                loginUser(email, password)
            }
        }

        signUpTextView.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        forgotPasswordTextView.setOnClickListener {
            showResetPasswordDialog()
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        var isValid = true

        if (email.isEmpty()) {
            emailInputLayout.error = getString(R.string.required_field)
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInputLayout.error = getString(R.string.invalid_email)
            isValid = false
        } else {
            emailInputLayout.error = null
        }

        if (password.isEmpty()) {
            passwordInputLayout.error = getString(R.string.required_field)
            isValid = false
        } else if (password.length < 6) {
            passwordInputLayout.error = getString(R.string.invalid_password)
            isValid = false
        } else {
            passwordInputLayout.error = null
        }

        return isValid
    }

    private fun loginUser(email: String, password: String) {
        showLoading(true)
        
        Log.d("LoginActivity", "==== ATTEMPTING LOGIN ====")

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                showLoading(false)
                
                if (task.isSuccessful) {
                    Log.d("LoginActivity", "✅ Firebase authentication successful")
                    
                    val currentUser = auth.currentUser
                    Log.d("LoginActivity", "Current user UID: ${currentUser?.uid}")
                    
                    if (currentUser == null) {
                        Log.e("LoginActivity", "❌ ERROR: currentUser is null after successful login!")
                        Snackbar.make(
                            findViewById(android.R.id.content),
                            "Error: Authentication failed",
                            Snackbar.LENGTH_LONG
                        ).show()
                        return@addOnCompleteListener
                    }
                    
                    // Load user data from Firestore
                    Log.d("LoginActivity", "Loading user data from Firestore...")
                    loadUserDataAndNavigate(currentUser.uid)
                    
                } else {
                    Log.e("LoginActivity", "❌ Login failed: ${task.exception?.message}")
                    Snackbar.make(
                        findViewById(android.R.id.content),
                        "Login failed: ${task.exception?.message}",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun loadUserDataAndNavigate(userId: String) {
        Log.d("LoginActivity", "==== LOADING USER DATA ====")
        Log.d("LoginActivity", "User ID: $userId")
        
        lifecycleScope.launch {
            try {
                // Fetch user from Firestore
                Log.d("LoginActivity", "Fetching user from Firestore...")
                val user = userViewModel.getUserFromFirestore(userId)
                
                if (user != null) {
                    Log.d("LoginActivity", "✅ User found in Firestore")
                    Log.d("LoginActivity", "Username: ${user.username}")
                    Log.d("LoginActivity", "Email: ${user.email}")
                    
                    // Save user to SharedPreferences
                    Log.d("LoginActivity", "Saving user to SharedPreferences...")
                    userViewModel.saveUserToPrefs(user)
                    
                    // Check if username exists (onboarding completed)
                    val hasUsername = user.username.isNotBlank()
                    Log.d("LoginActivity", "User has username: $hasUsername")
                    
                    if (hasUsername) {
                        // User completed onboarding → MainActivity
                        Log.d("LoginActivity", "User has completed onboarding → MainActivity")
                        userViewModel.setOnboardingCompleted()
                        
                        Snackbar.make(
                            findViewById(android.R.id.content),
                            "Welcome back, ${user.username}!",
                            Snackbar.LENGTH_SHORT
                        ).show()
                        
                        navigateToMain()
                    } else {
                        // User needs to complete onboarding → OnboardingActivity
                        Log.d("LoginActivity", "User needs onboarding → OnboardingActivity")
                        
                        Snackbar.make(
                            findViewById(android.R.id.content),
                            "Please complete your profile",
                            Snackbar.LENGTH_SHORT
                        ).show()
                        
                        navigateToOnboarding()
                    }
                } else {
                    // User not found in Firestore → needs onboarding
                    Log.w("LoginActivity", "⚠️ User not found in Firestore → OnboardingActivity")
                    
                    Snackbar.make(
                        findViewById(android.R.id.content),
                        "Please complete your profile",
                        Snackbar.LENGTH_SHORT
                    ).show()
                    
                    navigateToOnboarding()
                }
            } catch (e: Exception) {
                Log.e("LoginActivity", "❌ Error loading user data", e)
                Snackbar.make(
                    findViewById(android.R.id.content),
                    "Error loading profile: ${e.message}",
                    Snackbar.LENGTH_LONG
                ).show()
                
                // Default to onboarding on error
                navigateToOnboarding()
            }
        }
    }

    private fun showResetPasswordDialog() {
        val email = emailEditText.text.toString().trim()
        
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Snackbar.make(
                findViewById(android.R.id.content),
                "Please enter a valid email first",
                Snackbar.LENGTH_SHORT
            ).show()
            return
        }

        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Snackbar.make(
                        findViewById(android.R.id.content),
                        "Password reset email sent to $email",
                        Snackbar.LENGTH_LONG
                    ).show()
                } else {
                    Snackbar.make(
                        findViewById(android.R.id.content),
                        "Failed to send reset email: ${task.exception?.message}",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun showLoading(show: Boolean) {
        if (show) {
            progressBar.visibility = View.VISIBLE
            loginButton.isEnabled = false
            loginButton.text = ""
        } else {
            progressBar.visibility = View.GONE
            loginButton.isEnabled = true
            loginButton.text = getString(R.string.sign_in)
        }
    }

    private fun navigateToMain() {
        if (hasNavigated) {
            Log.w("LoginActivity", "Already navigated, skipping...")
            return
        }
        
        hasNavigated = true
        Log.d("LoginActivity", "==== NAVIGATING TO MAIN ====")
        
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
        
        Log.d("LoginActivity", "✅ Navigation initiated")
    }

    private fun navigateToOnboarding() {
        if (hasNavigated) {
            Log.w("LoginActivity", "Already navigated, skipping...")
            return
        }
        
        hasNavigated = true
        Log.d("LoginActivity", "==== NAVIGATING TO ONBOARDING ====")
        
        val intent = Intent(this, OnboardingActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
        
        Log.d("LoginActivity", "✅ Navigation initiated")
    }
}
