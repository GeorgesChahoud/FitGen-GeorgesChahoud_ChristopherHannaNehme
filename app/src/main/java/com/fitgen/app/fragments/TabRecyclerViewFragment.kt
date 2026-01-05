package com.fitgen.app.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.fitgen.app.R
import com.fitgen.app.adapters.FriendAdapter
import com.fitgen.app.adapters.FriendRequestAdapter
import com.fitgen.app.adapters.LeaderboardAdapter
import com.fitgen.app.models.Friend
import com.fitgen.app.models.FriendRequest
import com.fitgen.app.models.LeaderboardEntry

class TabRecyclerViewFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyState: View

    private var tabType: TabType = TabType.FRIENDS
    private var friends: List<Friend> = emptyList()
    private var requests: List<FriendRequest> = emptyList()
    private var leaderboard: List<LeaderboardEntry> = emptyList()
    
    // Retain adapter instances to prevent recreation
    private var friendAdapter: FriendAdapter? = null
    private var requestAdapter: FriendRequestAdapter? = null
    private var leaderboardAdapter: LeaderboardAdapter? = null
    
    private var onFriendClick: ((Friend) -> Unit)? = null
    private var onRemoveFriend: ((Friend) -> Unit)? = null
    private var onAcceptRequest: ((FriendRequest) -> Unit)? = null
    private var onRejectRequest: ((FriendRequest) -> Unit)? = null
    private var onRefresh: (() -> Unit)? = null

    enum class TabType {
        FRIENDS, REQUESTS, LEADERBOARD
    }

    companion object {
        private const val ARG_TAB_TYPE = "tab_type"

        fun newInstance(tabType: TabType): TabRecyclerViewFragment {
            return TabRecyclerViewFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_TAB_TYPE, tabType.name)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tabType = TabType.valueOf(
            arguments?.getString(ARG_TAB_TYPE) ?: TabType.FRIENDS.name
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_tab_recyclerview, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        recyclerView = view.findViewById(R.id.recyclerView)
        swipeRefresh = view.findViewById(R.id.swipeRefresh)
        progressBar = view.findViewById(R.id.progressBar)
        emptyState = view.findViewById(R.id.emptyState)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        
        swipeRefresh.setOnRefreshListener {
            onRefresh?.invoke()
            swipeRefresh.isRefreshing = false
        }

        // DON'T call setupAdapter() here - do it lazily when data arrives
    }

    fun setFriends(friends: List<Friend>) {
        Log.d("TabRecyclerView", "setFriends: ${friends.size} friends")
        this.friends = friends
        
        // Create adapter lazily on first call (callbacks are set by now)
        if (friendAdapter == null && ::recyclerView.isInitialized) {
            Log.d("TabRecyclerView", "Creating FriendAdapter (callbacks: onRemove=${onRemoveFriend != null})")
            setupAdapter()
        }
        
        friendAdapter?.submitList(friends.toList())  // Create new list to trigger DiffUtil
        updateEmptyState()
    }

    fun setRequests(requests: List<FriendRequest>) {
        Log.d("TabRecyclerView", "setRequests: ${requests.size} requests")
        this.requests = requests
        
        if (requestAdapter == null && ::recyclerView.isInitialized) {
            Log.d("TabRecyclerView", "Creating RequestAdapter (callbacks: onAccept=${onAcceptRequest != null})")
            setupAdapter()
        }
        
        requestAdapter?.submitList(requests.toList())
        updateEmptyState()
    }

    fun setLeaderboard(leaderboard: List<LeaderboardEntry>) {
        Log.d("TabRecyclerView", "setLeaderboard: ${leaderboard.size} entries")
        this.leaderboard = leaderboard
        
        if (leaderboardAdapter == null && ::recyclerView.isInitialized) {
            Log.d("TabRecyclerView", "Creating LeaderboardAdapter")
            setupAdapter()
        }
        
        leaderboardAdapter?.submitList(leaderboard.toList())
        updateEmptyState()
    }

    fun setOnFriendClickListener(listener: (Friend) -> Unit) {
        this.onFriendClick = listener
    }

    fun setOnRemoveFriendListener(listener: (Friend) -> Unit) {
        this.onRemoveFriend = listener
    }

    fun setOnAcceptRequestListener(listener: (FriendRequest) -> Unit) {
        this.onAcceptRequest = listener
    }

    fun setOnRejectRequestListener(listener: (FriendRequest) -> Unit) {
        this.onRejectRequest = listener
    }

    fun setOnRefreshListener(listener: () -> Unit) {
        this.onRefresh = listener
    }

    fun setLoading(isLoading: Boolean) {
        if (::progressBar.isInitialized) {
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun setupAdapter() {
        Log.d("TabRecyclerView", "setupAdapter() for $tabType")
        
        when (tabType) {
            TabType.FRIENDS -> {
                friendAdapter = FriendAdapter(
                    onFriendClick = { friend -> 
                        Log.d("TabRecyclerView", "Friend clicked: ${friend.friendUsername}")
                        onFriendClick?.invoke(friend) 
                    },
                    onRemoveClick = { friend -> 
                        Log.d("TabRecyclerView", "Remove friend clicked: ${friend.friendUsername}")
                        onRemoveFriend?.invoke(friend) 
                    }
                )
                recyclerView.adapter = friendAdapter
                // Submit existing data if any
                if (friends.isNotEmpty()) {
                    friendAdapter?.submitList(friends.toList())
                }
                Log.d("TabRecyclerView", "FriendAdapter created with ${friends.size} friends")
            }
            TabType.REQUESTS -> {
                requestAdapter = FriendRequestAdapter(
                    onAccept = { request -> 
                        Log.d("TabRecyclerView", "Accept request clicked")
                        onAcceptRequest?.invoke(request) 
                    },
                    onReject = { request -> 
                        Log.d("TabRecyclerView", "Reject request clicked")
                        onRejectRequest?.invoke(request) 
                    }
                )
                recyclerView.adapter = requestAdapter
                if (requests.isNotEmpty()) {
                    requestAdapter?.submitList(requests.toList())
                }
                Log.d("TabRecyclerView", "RequestAdapter created with ${requests.size} requests")
            }
            TabType.LEADERBOARD -> {
                leaderboardAdapter = LeaderboardAdapter()
                recyclerView.adapter = leaderboardAdapter
                if (leaderboard.isNotEmpty()) {
                    leaderboardAdapter?.submitList(leaderboard.toList())
                }
                Log.d("TabRecyclerView", "LeaderboardAdapter created with ${leaderboard.size} entries")
            }
        }
    }

    private fun updateEmptyState() {
        if (!::emptyState.isInitialized) return

        val isEmpty = when (tabType) {
            TabType.FRIENDS -> friends.isEmpty()
            TabType.REQUESTS -> requests.isEmpty()
            TabType.LEADERBOARD -> leaderboard.isEmpty()
        }

        emptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
        recyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }
}
