package com.fitgen.app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.fitgen.app.R
import com.fitgen.app.viewmodels.ChallengeViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class ChallengesFragment : Fragment() {

    // Use activityViewModels to share with other fragments
    private val viewModel: ChallengeViewModel by activityViewModels()

    private lateinit var tvCurrentStreak: TextView
    private lateinit var tvLongestStreak: TextView
    private lateinit var tvChallengeDescription: TextView
    private lateinit var tvChallengeTarget: TextView
    private lateinit var tvCountdown: TextView
    private lateinit var tvTotalChallenges: TextView
    private lateinit var tvCompletedStatus: TextView
    private lateinit var btnCompleteChallenge: MaterialButton
    private lateinit var cardChallenge: MaterialCardView
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_challenges, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initializeViews(view)
        setupObservers()
        setupListeners()
        
        loadData()
    }

    private fun initializeViews(view: View) {
        tvCurrentStreak = view.findViewById(R.id.tvCurrentStreak)
        tvLongestStreak = view.findViewById(R.id.tvLongestStreak)
        tvChallengeDescription = view.findViewById(R.id.tvChallengeDescription)
        tvChallengeTarget = view.findViewById(R.id.tvChallengeTarget)
        tvCountdown = view.findViewById(R.id.tvCountdown)
        tvTotalChallenges = view.findViewById(R.id.tvTotalChallenges)
        tvCompletedStatus = view.findViewById(R.id.tvCompletedStatus)
        btnCompleteChallenge = view.findViewById(R.id.btnCompleteChallenge)
        cardChallenge = view.findViewById(R.id.cardChallenge)
        progressBar = view.findViewById(R.id.progressBar)
    }

    private fun setupObservers() {
        viewModel.todayChallenge.observe(viewLifecycleOwner) { challenge ->
            challenge?.let {
                tvChallengeDescription.text = it.description
                tvChallengeTarget.text = "Target: ${it.target} ${it.unit}"
                
                if (it.isCompleted) {
                    btnCompleteChallenge.visibility = View.GONE
                    tvCompletedStatus.visibility = View.VISIBLE
                } else {
                    btnCompleteChallenge.visibility = View.VISIBLE
                    tvCompletedStatus.visibility = View.GONE
                }
            } ?: run {
                tvChallengeDescription.text = getString(R.string.no_challenge_today)
                tvChallengeTarget.text = ""
                btnCompleteChallenge.visibility = View.GONE
            }
        }

        viewModel.userStreak.observe(viewLifecycleOwner) { streak ->
            tvCurrentStreak.text = streak.currentStreak.toString()
            tvLongestStreak.text = streak.longestStreak.toString()
            tvTotalChallenges.text = streak.totalChallengesCompleted.toString()
        }

        viewModel.timeUntilMidnight.observe(viewLifecycleOwner) { millis ->
            tvCountdown.text = viewModel.formatTime(millis)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            if (message.isNotEmpty()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.completionSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.challenge_completed),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun setupListeners() {
        btnCompleteChallenge.setOnClickListener {
            viewModel.completeChallenge()
        }
    }

    private fun loadData() {
        viewModel.checkMissedChallenges()
        viewModel.loadTodayChallenge()
        viewModel.loadUserStreak()
        viewModel.startMidnightCountdown()
    }

    override fun onResume() {
        super.onResume()
        viewModel.startMidnightCountdown()
    }

    override fun onPause() {
        super.onPause()
        viewModel.stopMidnightCountdown()
    }
}
