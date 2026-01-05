package com.fitgen.app.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.fitgen.app.R
import com.fitgen.app.viewmodels.ChallengeViewModel
import com.fitgen.app.viewmodels.FriendViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class FriendsFragment : Fragment() {

    companion object {
        private const val TAB_COUNT = 3
        
        // ViewPager2 uses "f{position}" tag pattern for fragments
        private const val FRIENDS_TAB_POSITION = 0
        private const val REQUESTS_TAB_POSITION = 1
        private const val LEADERBOARD_TAB_POSITION = 2
    }

    // Use activityViewModels to survive fragment lifecycle
    private val friendViewModel: FriendViewModel by activityViewModels()
    private val challengeViewModel: ChallengeViewModel by activityViewModels()

    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    private lateinit var fabAddFriend: FloatingActionButton



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_friends, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initializeViews(view)
        setupViewPager()
        setupObservers()
        setupListeners()
        
        // Listeners auto-start in ViewModel init block
        challengeViewModel.loadLeaderboard()
    }

    override fun onResume() {
        super.onResume()
        Log.d("FriendsFragment", "onResume() - refreshing current fragments")
        
        // Force refresh current fragments with latest data
        friendViewModel.friends.value?.let { friends ->
            getCurrentFriendsTab()?.setFriends(friends)
        }
        friendViewModel.friendRequests.value?.let { requests ->
            getCurrentRequestsTab()?.setRequests(requests)
        }
        challengeViewModel.leaderboard.value?.let { leaderboard ->
            getCurrentLeaderboardTab()?.setLeaderboard(leaderboard)
        }
    }

    override fun onPause() {
        super.onPause()
        Log.d("FriendsFragment", "onPause() - Fragment going to background")
    }

    private fun initializeViews(view: View) {
        tabLayout = view.findViewById(R.id.tabLayout)
        viewPager = view.findViewById(R.id.viewPager)
        fabAddFriend = view.findViewById(R.id.fabAddFriend)
    }

    private fun getCurrentTabFragment(position: Int): TabRecyclerViewFragment? {
        val fragment = childFragmentManager.findFragmentByTag("f$position")
        return fragment as? TabRecyclerViewFragment
    }

    private fun getCurrentFriendsTab(): TabRecyclerViewFragment? {
        return getCurrentTabFragment(FRIENDS_TAB_POSITION)
    }

    private fun getCurrentRequestsTab(): TabRecyclerViewFragment? {
        return getCurrentTabFragment(REQUESTS_TAB_POSITION)
    }

    private fun getCurrentLeaderboardTab(): TabRecyclerViewFragment? {
        return getCurrentTabFragment(LEADERBOARD_TAB_POSITION)
    }

    private fun setupViewPager() {
        val adapter = ViewPagerAdapter(this)
        viewPager.adapter = adapter
        viewPager.offscreenPageLimit = TAB_COUNT  // Keep all tabs in memory

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                FRIENDS_TAB_POSITION -> getString(R.string.friends)
                REQUESTS_TAB_POSITION -> getString(R.string.friend_requests)
                LEADERBOARD_TAB_POSITION -> getString(R.string.leaderboard)
                else -> ""
            }
        }.attach()
    }

    private fun setupObservers() {
        friendViewModel.friends.observe(viewLifecycleOwner) { friends ->
            Log.d("FriendsFragment", "Friends observer triggered: ${friends.size} friends")
            getCurrentFriendsTab()?.setFriends(friends)
        }

        friendViewModel.friendRequests.observe(viewLifecycleOwner) { requests ->
            getCurrentRequestsTab()?.setRequests(requests)
        }

        challengeViewModel.leaderboard.observe(viewLifecycleOwner) { leaderboard ->
            getCurrentLeaderboardTab()?.setLeaderboard(leaderboard)
        }

        friendViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            getCurrentFriendsTab()?.setLoading(isLoading)
            getCurrentRequestsTab()?.setLoading(isLoading)
        }

        challengeViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            getCurrentLeaderboardTab()?.setLoading(isLoading)
        }

        friendViewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            if (message.isNotEmpty()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                friendViewModel.clearMessages()
            }
        }

        friendViewModel.successMessage.observe(viewLifecycleOwner) { message ->
            if (message.isNotEmpty()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                friendViewModel.clearMessages()
            }
        }
    }

    private fun setupListeners() {
        fabAddFriend.setOnClickListener {
            showAddFriendDialog()
        }
    }

    private fun showAddFriendDialog() {
        val dialog = AddFriendDialogFragment()
        dialog.show(childFragmentManager, "AddFriendDialog")
    }

    inner class ViewPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int = TAB_COUNT

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                FRIENDS_TAB_POSITION -> {
                    TabRecyclerViewFragment.newInstance(TabRecyclerViewFragment.TabType.FRIENDS).also { fragment ->
                        fragment.setOnRemoveFriendListener { friend ->
                            showRemoveFriendConfirmation(friend)
                        }
                        // No refresh listener needed - data updates automatically via listeners
                    }
                }
                REQUESTS_TAB_POSITION -> {
                    TabRecyclerViewFragment.newInstance(TabRecyclerViewFragment.TabType.REQUESTS).also { fragment ->
                        fragment.setOnAcceptRequestListener { request ->
                            friendViewModel.acceptRequest(request.id)
                        }
                        fragment.setOnRejectRequestListener { request ->
                            friendViewModel.rejectRequest(request.id)
                        }
                        // No refresh listener needed - data updates automatically via listeners
                    }
                }
                LEADERBOARD_TAB_POSITION -> {
                    TabRecyclerViewFragment.newInstance(TabRecyclerViewFragment.TabType.LEADERBOARD).also { fragment ->
                        fragment.setOnRefreshListener {
                            challengeViewModel.loadLeaderboard()
                        }
                    }
                }
                else -> TabRecyclerViewFragment.newInstance(TabRecyclerViewFragment.TabType.FRIENDS)
            }
        }
    }

    private fun showRemoveFriendConfirmation(friend: com.fitgen.app.models.Friend) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.remove_friend))
            .setMessage("Remove @${friend.friendUsername} from friends?")
            .setPositiveButton(android.R.string.ok) { _, _ ->
                friendViewModel.removeFriend(friend.friendId)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
}
