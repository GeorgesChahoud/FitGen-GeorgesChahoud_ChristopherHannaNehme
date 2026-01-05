package com.fitgen.app.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.fitgen.app.models.Friend
import com.fitgen.app.models.FriendRequest
import com.fitgen.app.repositories.FriendRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class FriendViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = FriendRepository()
    private val auth = FirebaseAuth.getInstance()

    // Cached data - survives fragment recreation
    private val _friends = MutableLiveData<List<Friend>>(emptyList())
    val friends: LiveData<List<Friend>> = _friends

    private val _friendRequests = MutableLiveData<List<FriendRequest>>(emptyList())
    val friendRequests: LiveData<List<FriendRequest>> = _friendRequests

    private val _pendingRequestsCount = MutableLiveData<Int>(0)
    val pendingRequestsCount: LiveData<Int> = _pendingRequestsCount

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>("")
    val errorMessage: LiveData<String> = _errorMessage

    private val _successMessage = MutableLiveData<String>("")
    val successMessage: LiveData<String> = _successMessage

    private val _searchedUser = MutableLiveData<com.fitgen.app.models.User?>()
    val searchedUser: LiveData<com.fitgen.app.models.User?> = _searchedUser

    // Track if listeners are active
    private var listenersActive = false

    init {
        Log.d("FriendViewModel", "FriendViewModel created")
        // Start listeners immediately when ViewModel is created
        startListeners()
    }

    /**
     * Start Firebase listeners - called once
     * NO COROUTINES - direct Firebase callbacks
     */
    fun startListeners() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Log.e("FriendViewModel", "Cannot start listeners - user not authenticated")
            return
        }
        
        if (listenersActive) {
            Log.d("FriendViewModel", "Listeners already active, re-emitting cached data")
            // Re-emit cached data to new observers (main thread)
            _friends.value = _friends.value
            _friendRequests.value = _friendRequests.value
            _pendingRequestsCount.value = _pendingRequestsCount.value
            return
        }
        
        Log.d("FriendViewModel", "Starting listeners for user: $userId")
        listenersActive = true
        
        // Start friends listener
        repository.observeFriends(
            userId = userId,
            onSuccess = { friendsList ->
                Log.d("FriendViewModel", "Friends updated: ${friendsList.size} friends")
                _friends.postValue(friendsList)  // postValue is thread-safe
            },
            onError = { error ->
                Log.e("FriendViewModel", "Friends listener error", error)
                _errorMessage.postValue(error.message ?: "Failed to load friends")
            }
        )
        
        // Start friend requests listener
        repository.observeFriendRequests(
            userId = userId,
            onSuccess = { requestsList ->
                Log.d("FriendViewModel", "Requests updated: ${requestsList.size} requests")
                _friendRequests.postValue(requestsList)
                _pendingRequestsCount.postValue(requestsList.size)
            },
            onError = { error ->
                Log.e("FriendViewModel", "Requests listener error", error)
                _errorMessage.postValue(error.message ?: "Failed to load requests")
            }
        )
    }
    
    /**
     * Stop listeners when ViewModel is destroyed
     */
    override fun onCleared() {
        super.onCleared()
        Log.d("FriendViewModel", "ViewModel cleared, stopping listeners")
        repository.stopListeners()
        listenersActive = false
    }

    /**
     * Send friend request to a user by friend code
     */
    fun sendFriendRequest(toFriendCode: String) {
        val userId = auth.currentUser?.uid ?: return

        if (toFriendCode.isBlank()) {
            _errorMessage.value = "Please enter a friend code"
            return
        }

        viewModelScope.launch {
            try {
                _isLoading.value = true
                val result = repository.sendFriendRequest(userId, toFriendCode)
                
                if (result.isSuccess) {
                    _successMessage.value = "Friend request sent!"
                    _searchedUser.value = null
                } else {
                    _errorMessage.value = result.exceptionOrNull()?.message ?: "Failed to send request"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Accept a friend request
     */
    fun acceptRequest(requestId: String) {
        Log.d("FriendViewModel", "User clicked Accept for request: $requestId")
        
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                Log.d("FriendViewModel", "Calling repository.acceptFriendRequest()...")
                val result = repository.acceptFriendRequest(requestId)
                
                if (result.isSuccess) {
                    Log.d("FriendViewModel", "✅ Friend request accepted successfully!")
                    _successMessage.value = "Friend request accepted!"
                } else {
                    val error = result.exceptionOrNull()?.message ?: "Failed to accept request"
                    Log.e("FriendViewModel", "❌ Failed to accept request: $error")
                    _errorMessage.value = error
                }
            } catch (e: Exception) {
                Log.e("FriendViewModel", "❌ Exception in acceptRequest", e)
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Reject a friend request
     */
    fun rejectRequest(requestId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val result = repository.rejectFriendRequest(requestId)
                
                if (result.isSuccess) {
                    _successMessage.value = "Friend request rejected"
                } else {
                    _errorMessage.value = result.exceptionOrNull()?.message ?: "Failed to reject request"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Remove a friend
     */
    fun removeFriend(friendId: String) {
        val userId = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            try {
                _isLoading.value = true
                val result = repository.removeFriend(userId, friendId)
                
                if (result.isSuccess) {
                    _successMessage.value = "Friend removed"
                } else {
                    _errorMessage.value = result.exceptionOrNull()?.message ?: "Failed to remove friend"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Search for a user by friend code
     */
    fun searchUser(friendCode: String) {
        if (friendCode.isBlank()) {
            _errorMessage.value = "Please enter a friend code"
            return
        }

        viewModelScope.launch {
            try {
                _isLoading.value = true
                val result = repository.searchUserByFriendCode(friendCode)
                
                if (result.isSuccess) {
                    val user = result.getOrNull()
                    if (user != null) {
                        _searchedUser.value = user
                    } else {
                        _errorMessage.value = "User not found"
                        _searchedUser.value = null
                    }
                } else {
                    _errorMessage.value = result.exceptionOrNull()?.message ?: "Failed to search user"
                    _searchedUser.value = null
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
                _searchedUser.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Clear searched user
     */
    fun clearSearchedUser() {
        _searchedUser.value = null
    }

    /**
     * Clear messages
     */
    fun clearMessages() {
        _errorMessage.value = ""
        _successMessage.value = ""
    }
}
