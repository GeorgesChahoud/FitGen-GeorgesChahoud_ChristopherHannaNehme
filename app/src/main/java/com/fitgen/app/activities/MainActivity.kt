package com.fitgen.app.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.fitgen.app.R
import com.fitgen.app.viewmodels.ChallengeViewModel
import com.fitgen.app.viewmodels.UserViewModel
import com.fitgen.app.workers.StreakCheckerWorker
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private val userViewModel: UserViewModel by viewModels()
    private val challengeViewModel: ChallengeViewModel by viewModels()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        Log.d("MainActivity", "==== MAIN ACTIVITY STARTED ====")
        
        // Verify user is authenticated
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Log.e("MainActivity", "User not authenticated in MainActivity - redirecting to login")
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
        
        Log.d("MainActivity", "User authenticated: ${currentUser.uid}")
        
        setContentView(R.layout.activity_main)

        // Ensure user data is loaded
        ensureUserDataLoaded()
        setupNavigation()
        setupWorkManager()
        checkMissedChallenges()
    }

    private fun ensureUserDataLoaded() {
        // First try to load from SharedPreferences
        userViewModel.loadCurrentUser()
        
        // If no user in SharedPreferences but Firebase Auth has a user, load from Firestore
        val firebaseUser = auth.currentUser
        if (userViewModel.currentUser.value == null && firebaseUser != null) {
            userViewModel.loadUserFromFirestore(firebaseUser.uid)
        }
        
        // If still no user, redirect to login
        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setupWithNavController(navController)
    }

    private fun setupWorkManager() {
        // Schedule daily streak checker
        val dailyWorkRequest = PeriodicWorkRequestBuilder<StreakCheckerWorker>(
            1, TimeUnit.DAYS
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "streak_checker",
            ExistingPeriodicWorkPolicy.KEEP,
            dailyWorkRequest
        )
    }

    private fun checkMissedChallenges() {
        // Check for missed challenges when app opens
        challengeViewModel.checkMissedChallenges()
    }
    
    override fun onResume() {
        super.onResume()
        // Refresh user data when returning to the app
        userViewModel.loadCurrentUser()
        checkMissedChallenges()
    }
}
