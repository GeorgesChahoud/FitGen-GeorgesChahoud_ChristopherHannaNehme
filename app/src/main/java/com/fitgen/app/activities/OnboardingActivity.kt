package com.fitgen.app.activities

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.fitgen.app.R
import com.fitgen.app.models.User
import com.fitgen.app.repositories.UserRepository
import com.fitgen.app.utils.Constants
import com.fitgen.app.utils.FriendCodeGenerator
import com.fitgen.app.utils.UsernameValidator
import com.fitgen.app.viewmodels.UserViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class OnboardingActivity : AppCompatActivity() {

    private lateinit var stepIndicator: TextView
    private lateinit var contentContainer: FrameLayout
    private lateinit var previousButton: MaterialButton
    private lateinit var nextButton: MaterialButton
    
    private val userViewModel: UserViewModel by viewModels()
    private val auth = FirebaseAuth.getInstance()
    
    private var currentStep = 0
    private val totalSteps = 7
    
    // User data
    private var username: String = ""
    private var age: Int = 0
    private var height: Int = 0
    private var weight: Double = 0.0
    private var gender: String = Constants.GENDER_MALE
    private var goal: String = Constants.GOAL_LOSE_WEIGHT
    private var activityLevel: String = Constants.ACTIVITY_SEDENTARY
    private var workoutDaysPerWeek: Int = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        Log.w("OnboardingActivity", "⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️")
        Log.w("OnboardingActivity", "ONBOARDING ACTIVITY LAUNCHED")
        Log.w("OnboardingActivity", "This should NOT be the entry point!")
        Log.w("OnboardingActivity", "⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️")
        
        setContentView(R.layout.activity_onboarding)

        initViews()
        setupListeners()
        showStep(0)
    }

    private fun initViews() {
        stepIndicator = findViewById(R.id.stepIndicator)
        contentContainer = findViewById(R.id.contentContainer)
        previousButton = findViewById(R.id.previousButton)
        nextButton = findViewById(R.id.nextButton)
    }

    private fun setupListeners() {
        nextButton.setOnClickListener {
            lifecycleScope.launch {
                // For username step, do async validation
                val isValid = if (currentStep == 0) {
                    validateUsernameStepAsync()
                } else {
                    validateCurrentStep()
                }
                
                if (isValid) {
                    saveCurrentStepData()
                    if (currentStep < totalSteps - 1) {
                        showStep(currentStep + 1)
                    } else {
                        finishOnboarding()
                    }
                }
            }
        }

        previousButton.setOnClickListener {
            if (currentStep > 0) {
                showStep(currentStep - 1)
            }
        }
    }

    private fun showStep(step: Int) {
        currentStep = step
        stepIndicator.text = getString(R.string.step_format, step + 1, totalSteps)
        
        // Show/hide previous button
        previousButton.visibility = if (step > 0) View.VISIBLE else View.GONE
        
        // Update next button text
        nextButton.text = if (step == totalSteps - 1) {
            getString(R.string.finish)
        } else {
            getString(R.string.next)
        }

        // Load step content
        val layoutResId = when (step) {
            0 -> R.layout.onboarding_step_username
            1 -> R.layout.onboarding_step_age
            2 -> R.layout.onboarding_step_height
            3 -> R.layout.onboarding_step_weight
            4 -> R.layout.onboarding_step_goal
            5 -> R.layout.onboarding_step_activity
            6 -> R.layout.onboarding_step_workout_days
            else -> R.layout.onboarding_step_username
        }

        contentContainer.removeAllViews()
        val view = LayoutInflater.from(this).inflate(layoutResId, contentContainer, false)
        contentContainer.addView(view)
        
        // Setup real-time validation for username step
        if (step == 0) {
            setupUsernameValidation()
        }
        
        // Restore previously entered data
        restoreStepData(step)
    }

    private fun validateCurrentStep(): Boolean {
        return when (currentStep) {
            0 -> true // Username validation handled separately (async)
            1 -> validateAgeStep()
            2 -> validateHeightStep()
            3 -> validateWeightStep()
            4 -> true // Goal is always valid (radio button in RadioGroup)
            5 -> true // Activity level is always valid (radio button in RadioGroup)
            6 -> true // Workout days is always valid (radio button in RadioGroup)
            else -> false
        }
    }

    private fun setupUsernameValidation() {
        val etUsername = contentContainer.findViewById<TextInputEditText>(R.id.etUsername)
        val tvUsernameError = contentContainer.findViewById<TextView>(R.id.tvUsernameError)
        
        etUsername.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val username = s.toString()
                if (username.isNotEmpty()) {
                    val validation = UsernameValidator.isValidFormat(username)
                    if (!validation.isValid) {
                        tvUsernameError.text = validation.errorMessage
                        tvUsernameError.visibility = View.VISIBLE
                    } else {
                        tvUsernameError.visibility = View.GONE
                    }
                } else {
                    tvUsernameError.visibility = View.GONE
                }
            }
        })
    }

    private suspend fun validateUsernameStepAsync(): Boolean {
        val etUsername = contentContainer.findViewById<TextInputEditText>(R.id.etUsername)
        val usernameStr = etUsername.text.toString().trim()
        
        Log.d("OnboardingActivity", "Validating username: '$usernameStr'")
        
        if (usernameStr.isEmpty()) {
            Toast.makeText(this, "Please enter a username", Toast.LENGTH_SHORT).show()
            return false
        }
        
        val validation = UsernameValidator.isValidFormat(usernameStr)
        if (!validation.isValid) {
            Log.d("OnboardingActivity", "Username format invalid: ${validation.errorMessage}")
            Toast.makeText(this, validation.errorMessage, Toast.LENGTH_SHORT).show()
            return false
        }
        
        // Check username availability
        val formattedUsername = UsernameValidator.formatUsername(usernameStr)
        Log.d("OnboardingActivity", "Checking availability for: '$formattedUsername'")
        
        val userRepository = UserRepository(this)
        
        return try {
            val isAvailable = userRepository.isUsernameAvailable(formattedUsername)
            
            Log.d("OnboardingActivity", "Availability result: $isAvailable")
            
            if (!isAvailable) {
                val errorMsg = "Username '@$formattedUsername' is already taken. Please choose a different one."
                Log.d("OnboardingActivity", errorMsg)
                Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show()
                return false
            }
            
            Log.d("OnboardingActivity", "Username is available!")
            true
        } catch (e: Exception) {
            Log.e("OnboardingActivity", "Error during username validation", e)
            Toast.makeText(this, "Unable to check username availability. Please try again.", Toast.LENGTH_LONG).show()
            false
        }
    }

    private fun validateAgeStep(): Boolean {
        val ageEditText = contentContainer.findViewById<TextInputEditText>(R.id.ageEditText)
        val ageStr = ageEditText.text.toString().trim()
        
        if (ageStr.isEmpty()) {
            Toast.makeText(this, "Please enter your age", Toast.LENGTH_SHORT).show()
            return false
        }
        
        val age = ageStr.toIntOrNull()
        if (age == null || age < 13 || age > 120) {
            Toast.makeText(this, "Please enter a valid age (13-120)", Toast.LENGTH_SHORT).show()
            return false
        }
        
        return true
    }

    private fun validateHeightStep(): Boolean {
        val heightEditText = contentContainer.findViewById<TextInputEditText>(R.id.heightEditText)
        val heightStr = heightEditText.text.toString().trim()
        
        if (heightStr.isEmpty()) {
            Toast.makeText(this, "Please enter your height", Toast.LENGTH_SHORT).show()
            return false
        }
        
        val height = heightStr.toIntOrNull()
        if (height == null || height < 100 || height > 250) {
            Toast.makeText(this, "Please enter a valid height (100-250 cm)", Toast.LENGTH_SHORT).show()
            return false
        }
        
        return true
    }

    private fun validateWeightStep(): Boolean {
        val weightEditText = contentContainer.findViewById<TextInputEditText>(R.id.weightEditText)
        val weightStr = weightEditText.text.toString().trim()
        
        if (weightStr.isEmpty()) {
            Toast.makeText(this, "Please enter your weight", Toast.LENGTH_SHORT).show()
            return false
        }
        
        val weight = weightStr.toDoubleOrNull()
        if (weight == null || weight < 30 || weight > 300) {
            Toast.makeText(this, "Please enter a valid weight (30-300 kg)", Toast.LENGTH_SHORT).show()
            return false
        }
        
        return true
    }

    private fun saveCurrentStepData() {
        when (currentStep) {
            0 -> {
                val etUsername = contentContainer.findViewById<TextInputEditText>(R.id.etUsername)
                username = UsernameValidator.formatUsername(etUsername.text.toString())
            }
            1 -> {
                val ageEditText = contentContainer.findViewById<TextInputEditText>(R.id.ageEditText)
                age = ageEditText.text.toString().toInt()
                
                val maleRadio = contentContainer.findViewById<RadioButton>(R.id.maleRadioButton)
                gender = if (maleRadio.isChecked) Constants.GENDER_MALE else Constants.GENDER_FEMALE
            }
            2 -> {
                val heightEditText = contentContainer.findViewById<TextInputEditText>(R.id.heightEditText)
                height = heightEditText.text.toString().toInt()
            }
            3 -> {
                val weightEditText = contentContainer.findViewById<TextInputEditText>(R.id.weightEditText)
                weight = weightEditText.text.toString().toDouble()
            }
            4 -> {
                val loseWeightRadio = contentContainer.findViewById<RadioButton>(R.id.loseWeightRadioButton)
                val maintainRadio = contentContainer.findViewById<RadioButton>(R.id.maintainRadioButton)
                
                goal = when {
                    loseWeightRadio.isChecked -> Constants.GOAL_LOSE_WEIGHT
                    maintainRadio.isChecked -> Constants.GOAL_MAINTAIN
                    else -> Constants.GOAL_GAIN_MUSCLE
                }
            }
            5 -> {
                val sedentaryRadio = contentContainer.findViewById<RadioButton>(R.id.sedentaryRadioButton)
                val lightlyActiveRadio = contentContainer.findViewById<RadioButton>(R.id.lightlyActiveRadioButton)
                val moderatelyActiveRadio = contentContainer.findViewById<RadioButton>(R.id.moderatelyActiveRadioButton)
                
                activityLevel = when {
                    sedentaryRadio.isChecked -> Constants.ACTIVITY_SEDENTARY
                    lightlyActiveRadio.isChecked -> Constants.ACTIVITY_LIGHTLY_ACTIVE
                    moderatelyActiveRadio.isChecked -> Constants.ACTIVITY_MODERATELY_ACTIVE
                    else -> Constants.ACTIVITY_VERY_ACTIVE
                }
            }
            6 -> {
                val twoDaysRadio = contentContainer.findViewById<RadioButton>(R.id.twoDaysRadioButton)
                val threeDaysRadio = contentContainer.findViewById<RadioButton>(R.id.threeDaysRadioButton)
                val fourDaysRadio = contentContainer.findViewById<RadioButton>(R.id.fourDaysRadioButton)
                val fiveDaysRadio = contentContainer.findViewById<RadioButton>(R.id.fiveDaysRadioButton)
                val sixDaysRadio = contentContainer.findViewById<RadioButton>(R.id.sixDaysRadioButton)
                
                workoutDaysPerWeek = when {
                    twoDaysRadio.isChecked -> 2
                    threeDaysRadio.isChecked -> 3
                    fourDaysRadio.isChecked -> 4
                    fiveDaysRadio.isChecked -> 5
                    sixDaysRadio.isChecked -> 6
                    else -> 7
                }
            }
        }
    }

    private fun restoreStepData(step: Int) {
        when (step) {
            0 -> {
                if (username.isNotEmpty()) {
                    val etUsername = contentContainer.findViewById<TextInputEditText>(R.id.etUsername)
                    etUsername.setText(username)
                }
            }
            1 -> {
                if (age > 0) {
                    val ageEditText = contentContainer.findViewById<TextInputEditText>(R.id.ageEditText)
                    ageEditText.setText(age.toString())
                }
                
                val maleRadio = contentContainer.findViewById<RadioButton>(R.id.maleRadioButton)
                val femaleRadio = contentContainer.findViewById<RadioButton>(R.id.femaleRadioButton)
                if (gender == Constants.GENDER_FEMALE) {
                    femaleRadio.isChecked = true
                } else {
                    maleRadio.isChecked = true
                }
            }
            2 -> {
                if (height > 0) {
                    val heightEditText = contentContainer.findViewById<TextInputEditText>(R.id.heightEditText)
                    heightEditText.setText(height.toString())
                }
            }
            3 -> {
                if (weight > 0) {
                    val weightEditText = contentContainer.findViewById<TextInputEditText>(R.id.weightEditText)
                    weightEditText.setText(weight.toString())
                }
            }
            4 -> {
                val loseWeightRadio = contentContainer.findViewById<RadioButton>(R.id.loseWeightRadioButton)
                val maintainRadio = contentContainer.findViewById<RadioButton>(R.id.maintainRadioButton)
                val gainMuscleRadio = contentContainer.findViewById<RadioButton>(R.id.gainMuscleRadioButton)
                
                when (goal) {
                    Constants.GOAL_LOSE_WEIGHT -> loseWeightRadio.isChecked = true
                    Constants.GOAL_MAINTAIN -> maintainRadio.isChecked = true
                    Constants.GOAL_GAIN_MUSCLE -> gainMuscleRadio.isChecked = true
                }
            }
            5 -> {
                val sedentaryRadio = contentContainer.findViewById<RadioButton>(R.id.sedentaryRadioButton)
                val lightlyActiveRadio = contentContainer.findViewById<RadioButton>(R.id.lightlyActiveRadioButton)
                val moderatelyActiveRadio = contentContainer.findViewById<RadioButton>(R.id.moderatelyActiveRadioButton)
                val veryActiveRadio = contentContainer.findViewById<RadioButton>(R.id.veryActiveRadioButton)
                
                when (activityLevel) {
                    Constants.ACTIVITY_SEDENTARY -> sedentaryRadio.isChecked = true
                    Constants.ACTIVITY_LIGHTLY_ACTIVE -> lightlyActiveRadio.isChecked = true
                    Constants.ACTIVITY_MODERATELY_ACTIVE -> moderatelyActiveRadio.isChecked = true
                    Constants.ACTIVITY_VERY_ACTIVE -> veryActiveRadio.isChecked = true
                }
            }
            6 -> {
                val twoDaysRadio = contentContainer.findViewById<RadioButton>(R.id.twoDaysRadioButton)
                val threeDaysRadio = contentContainer.findViewById<RadioButton>(R.id.threeDaysRadioButton)
                val fourDaysRadio = contentContainer.findViewById<RadioButton>(R.id.fourDaysRadioButton)
                val fiveDaysRadio = contentContainer.findViewById<RadioButton>(R.id.fiveDaysRadioButton)
                val sixDaysRadio = contentContainer.findViewById<RadioButton>(R.id.sixDaysRadioButton)
                val sevenDaysRadio = contentContainer.findViewById<RadioButton>(R.id.sevenDaysRadioButton)
                
                when (workoutDaysPerWeek) {
                    2 -> twoDaysRadio.isChecked = true
                    3 -> threeDaysRadio.isChecked = true
                    4 -> fourDaysRadio.isChecked = true
                    5 -> fiveDaysRadio.isChecked = true
                    6 -> sixDaysRadio.isChecked = true
                    7 -> sevenDaysRadio.isChecked = true
                }
            }
        }
    }

    private fun finishOnboarding() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Error: User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d("OnboardingActivity", "==== FINISHING ONBOARDING ====")
        Log.d("OnboardingActivity", "User ID: ${currentUser.uid}")
        Log.d("OnboardingActivity", "Username: $username")

        // Username is already validated in step 0, just save the user
        lifecycleScope.launch {
            // Generate unique friend code
            val friendCode = FriendCodeGenerator.generateUniqueFriendCode()
            Log.d("OnboardingActivity", "Generated friend code: $friendCode")
            
            val user = User(
                uid = currentUser.uid,
                email = currentUser.email ?: "",
                username = username,
                friendCode = friendCode,
                age = age,
                height = height,
                weight = weight,
                goal = goal,
                activityLevel = activityLevel,
                gender = gender,
                workoutDaysPerWeek = workoutDaysPerWeek
            )

            Log.d("OnboardingActivity", "Saving user to Firestore and SharedPreferences...")
            userViewModel.saveUser(user)
            
            // 🔥 CRITICAL: Set onboarding completed flag
            Log.d("OnboardingActivity", "Setting onboarding completed flag...")
            userViewModel.setOnboardingCompleted()
            
            Log.d("OnboardingActivity", "✅ Onboarding completed successfully")

            Toast.makeText(this@OnboardingActivity, "Profile created! Your friend code: $friendCode", Toast.LENGTH_LONG).show()
            
            // Navigate to MainActivity with proper flags to clear back stack
            Log.d("OnboardingActivity", "Navigating to MainActivity...")
            val intent = Intent(this@OnboardingActivity, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}
