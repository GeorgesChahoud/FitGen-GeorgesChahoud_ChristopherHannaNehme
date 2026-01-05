package com.fitgen.app.fragments

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fitgen.app.R
import com.fitgen.app.activities.LoginActivity
import com.fitgen.app.adapters.ProgressAdapter
import com.fitgen.app.utils.Constants
import com.fitgen.app.viewmodels.UserViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth

class ProfileFragment : Fragment() {

    private val userViewModel: UserViewModel by activityViewModels()
    private val auth = FirebaseAuth.getInstance()
    
    private lateinit var emailTextView: TextView
    private lateinit var friendCodeTextView: TextView
    private lateinit var copyFriendCodeButton: MaterialButton
    private lateinit var ageTextView: TextView
    private lateinit var heightTextView: TextView
    private lateinit var weightTextView: TextView
    private lateinit var goalTextView: TextView
    private lateinit var activityLevelTextView: TextView
    private lateinit var editProfileButton: MaterialButton
    private lateinit var addProgressButton: MaterialButton
    private lateinit var progressRecyclerView: RecyclerView
    private lateinit var logoutButton: MaterialButton
    
    private val progressAdapter = ProgressAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initViews(view)
        setupRecyclerView()
        setupListeners()
        observeViewModels()
    }

    private fun initViews(view: View) {
        emailTextView = view.findViewById(R.id.emailTextView)
        friendCodeTextView = view.findViewById(R.id.friendCodeTextView)
        copyFriendCodeButton = view.findViewById(R.id.copyFriendCodeButton)
        ageTextView = view.findViewById(R.id.ageTextView)
        heightTextView = view.findViewById(R.id.heightTextView)
        weightTextView = view.findViewById(R.id.weightTextView)
        goalTextView = view.findViewById(R.id.goalTextView)
        activityLevelTextView = view.findViewById(R.id.activityLevelTextView)
        editProfileButton = view.findViewById(R.id.editProfileButton)
        addProgressButton = view.findViewById(R.id.addProgressButton)
        progressRecyclerView = view.findViewById(R.id.progressRecyclerView)
        logoutButton = view.findViewById(R.id.logoutButton)
    }

    private fun setupRecyclerView() {
        progressRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = progressAdapter
        }
    }

    private fun setupListeners() {
        editProfileButton.setOnClickListener {
            showEditProfileDialog()
        }

        addProgressButton.setOnClickListener {
            showAddProgressDialog()
        }

        logoutButton.setOnClickListener {
            showLogoutConfirmation()
        }
        
        copyFriendCodeButton.setOnClickListener {
            val friendCode = friendCodeTextView.text.toString()
            if (friendCode.isNotEmpty()) {
                copyToClipboard(friendCode)
            }
        }
    }

    private fun observeViewModels() {
        userViewModel.currentUser.observe(viewLifecycleOwner) { user ->
            user?.let {
                emailTextView.text = it.email
                friendCodeTextView.text = it.friendCode.ifEmpty { "N/A" }
                ageTextView.text = getString(R.string.years_format, it.age)
                heightTextView.text = getString(R.string.cm_format, it.height)
                weightTextView.text = getString(R.string.kg_format, it.weight)
                
                goalTextView.text = when (it.goal) {
                    Constants.GOAL_LOSE_WEIGHT -> getString(R.string.lose_weight)
                    Constants.GOAL_MAINTAIN -> getString(R.string.maintain_weight)
                    Constants.GOAL_GAIN_MUSCLE -> getString(R.string.gain_muscle)
                    else -> it.goal
                }
                
                activityLevelTextView.text = when (it.activityLevel) {
                    Constants.ACTIVITY_SEDENTARY -> getString(R.string.sedentary)
                    Constants.ACTIVITY_LIGHTLY_ACTIVE -> getString(R.string.lightly_active)
                    Constants.ACTIVITY_MODERATELY_ACTIVE -> getString(R.string.moderately_active)
                    Constants.ACTIVITY_VERY_ACTIVE -> getString(R.string.very_active)
                    else -> it.activityLevel
                }
            }
        }

        userViewModel.progressEntries.observe(viewLifecycleOwner) { entries ->
            progressAdapter.submitList(entries.take(10))
        }
        
        // Observe error state
        userViewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                view?.let { v ->
                    Snackbar.make(v, it, Snackbar.LENGTH_LONG)
                        .setAction(R.string.ok) { userViewModel.clearError() }
                        .show()
                }
                userViewModel.clearError()
            }
        }
        
        // Load progress entries
        userViewModel.loadProgressEntries()
    }

    private fun showEditProfileDialog() {
        val user = userViewModel.currentUser.value
        if (user == null) {
            // Try to reload user data
            userViewModel.syncUserData()
            view?.let {
                Snackbar.make(it, "Loading user data... Please try again.", Snackbar.LENGTH_SHORT).show()
            }
            return
        }
        
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_edit_profile, null)
        
        val ageEditText = dialogView.findViewById<TextInputEditText>(R.id.ageEditText)
        val heightEditText = dialogView.findViewById<TextInputEditText>(R.id.heightEditText)
        val weightEditText = dialogView.findViewById<TextInputEditText>(R.id.weightEditText)
        val goalDropdown = dialogView.findViewById<AutoCompleteTextView>(R.id.goalDropdown)
        val activityLevelDropdown = dialogView.findViewById<AutoCompleteTextView>(R.id.activityLevelDropdown)
        val loadingProgressBar = dialogView.findViewById<ProgressBar>(R.id.loadingProgressBar)
        
        // Set current values
        ageEditText.setText(user.age.toString())
        heightEditText.setText(user.height.toString())
        weightEditText.setText(user.weight.toString())
        
        // Setup goal dropdown
        val goals = arrayOf(
            getString(R.string.lose_weight),
            getString(R.string.maintain_weight),
            getString(R.string.gain_muscle)
        )
        val goalAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, goals)
        goalDropdown?.setAdapter(goalAdapter)
        
        // Set current goal
        val currentGoalIndex = when (user.goal) {
            Constants.GOAL_LOSE_WEIGHT -> 0
            Constants.GOAL_MAINTAIN -> 1
            Constants.GOAL_GAIN_MUSCLE -> 2
            else -> 0
        }
        goalDropdown?.setText(goals[currentGoalIndex], false)
        
        // Setup activity level dropdown
        val activityLevels = arrayOf(
            getString(R.string.sedentary),
            getString(R.string.lightly_active),
            getString(R.string.moderately_active),
            getString(R.string.very_active)
        )
        val activityAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, activityLevels)
        activityLevelDropdown?.setAdapter(activityAdapter)
        
        // Set current activity level
        val currentActivityIndex = when (user.activityLevel) {
            Constants.ACTIVITY_SEDENTARY -> 0
            Constants.ACTIVITY_LIGHTLY_ACTIVE -> 1
            Constants.ACTIVITY_MODERATELY_ACTIVE -> 2
            Constants.ACTIVITY_VERY_ACTIVE -> 3
            else -> 0
        }
        activityLevelDropdown?.setText(activityLevels[currentActivityIndex], false)
        
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.edit_profile)
            .setView(dialogView)
            .setPositiveButton(R.string.save_changes, null) // Set to null to handle manually
            .setNegativeButton(R.string.cancel, null)
            .create()
        
        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                // Validate inputs
                val ageText = ageEditText.text.toString()
                val heightText = heightEditText.text.toString()
                val weightText = weightEditText.text.toString()
                
                val age = ageText.toIntOrNull()
                val height = heightText.toIntOrNull()
                val weight = weightText.toDoubleOrNull()
                
                // Validate age
                if (age == null || age < Constants.MIN_AGE || age > Constants.MAX_AGE) {
                    ageEditText.error = getString(R.string.invalid_age)
                    return@setOnClickListener
                }
                
                // Validate height
                if (height == null || height < Constants.MIN_HEIGHT || height > Constants.MAX_HEIGHT) {
                    heightEditText.error = getString(R.string.invalid_height)
                    return@setOnClickListener
                }
                
                // Validate weight
                if (weight == null || weight < Constants.MIN_WEIGHT || weight > Constants.MAX_WEIGHT) {
                    weightEditText.error = getString(R.string.invalid_weight)
                    return@setOnClickListener
                }
                
                // Get selected goal
                val selectedGoal = when (goalDropdown?.text.toString()) {
                    getString(R.string.lose_weight) -> Constants.GOAL_LOSE_WEIGHT
                    getString(R.string.maintain_weight) -> Constants.GOAL_MAINTAIN
                    getString(R.string.gain_muscle) -> Constants.GOAL_GAIN_MUSCLE
                    else -> user.goal
                }
                
                // Get selected activity level
                val selectedActivityLevel = when (activityLevelDropdown?.text.toString()) {
                    getString(R.string.sedentary) -> Constants.ACTIVITY_SEDENTARY
                    getString(R.string.lightly_active) -> Constants.ACTIVITY_LIGHTLY_ACTIVE
                    getString(R.string.moderately_active) -> Constants.ACTIVITY_MODERATELY_ACTIVE
                    getString(R.string.very_active) -> Constants.ACTIVITY_VERY_ACTIVE
                    else -> user.activityLevel
                }
                
                // Show loading state
                loadingProgressBar?.visibility = View.VISIBLE
                positiveButton.isEnabled = false
                
                // Update profile
                userViewModel.updateProfile(age, height, weight, selectedGoal, selectedActivityLevel)
                
                // Observe loading state to dismiss dialog
                userViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
                    if (!isLoading) {
                        loadingProgressBar?.visibility = View.GONE
                        positiveButton.isEnabled = true
                        dialog.dismiss()
                        view?.let { v ->
                            Snackbar.make(v, R.string.profile_updated, Snackbar.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
        
        dialog.show()
    }

    private fun showAddProgressDialog() {
        val user = userViewModel.currentUser.value
        if (user == null) {
            // Try to reload user data
            userViewModel.syncUserData()
            view?.let {
                Snackbar.make(it, "Loading user data... Please try again.", Snackbar.LENGTH_SHORT).show()
            }
            return
        }
        
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_progress, null)
        
        val weightEditText = dialogView.findViewById<TextInputEditText>(R.id.weightEditText)
        val notesEditText = dialogView.findViewById<TextInputEditText>(R.id.notesEditText)
        
        weightEditText.setText(user.weight.toString())
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.add_progress)
            .setView(dialogView)
            .setPositiveButton(R.string.save) { _, _ ->
                val weight = weightEditText.text.toString().toDoubleOrNull()
                val notes = notesEditText.text.toString()
                
                if (weight != null && weight >= Constants.MIN_WEIGHT && weight <= Constants.MAX_WEIGHT) {
                    userViewModel.addProgressEntry(weight, notes)
                    view?.let {
                        Snackbar.make(it, R.string.progress_added, Snackbar.LENGTH_SHORT).show()
                    }
                } else {
                    view?.let {
                        Snackbar.make(it, R.string.please_enter_valid_weight, Snackbar.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showLogoutConfirmation() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.logout)
            .setMessage(R.string.logout_confirmation)
            .setPositiveButton(R.string.logout) { _, _ ->
                logout()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun logout() {
        Log.d("ProfileFragment", "==== LOGGING OUT ====")
        
        val currentUser = FirebaseAuth.getInstance().currentUser
        Log.d("ProfileFragment", "Logging out user: ${currentUser?.uid}")
        
        // Sign out from Firebase
        FirebaseAuth.getInstance().signOut()
        Log.d("ProfileFragment", "✅ Signed out from Firebase")
        
        // Clear user preferences and ViewModel
        userViewModel.clearUserData()
        Log.d("ProfileFragment", "✅ Cleared SharedPreferences")
        
        // Navigate to login
        Log.d("ProfileFragment", "Navigating to LoginActivity...")
        val intent = Intent(requireActivity(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }
    
    private fun copyToClipboard(text: String) {
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Friend Code", text)
        clipboard.setPrimaryClip(clip)
        
        view?.let {
            Snackbar.make(it, R.string.friend_code_copied, Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        // Sync user data when fragment resumes
        userViewModel.syncUserData()
        userViewModel.loadProgressEntries()
    }
}
