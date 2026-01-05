package com.fitgen.app.repositories

import android.util.Log
import com.fitgen.app.models.Friend
import com.fitgen.app.models.FriendRequest
import com.fitgen.app.models.FriendRequestStatus
import com.fitgen.app.models.User
import com.fitgen.app.utils.Constants
import com.fitgen.app.utils.FriendCodeGenerator
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.tasks.await

class FriendRepository {
    private val firestore = FirebaseFirestore.getInstance()
    
    // Keep reference to active listeners
    private var friendsListener: ListenerRegistration? = null
    private var requestsListener: ListenerRegistration? = null

    /**
     * Send a friend request by friend code
     */
    suspend fun sendFriendRequest(fromUserId: String, toFriendCode: String): Result<FriendRequest> {
        return try {
            // Validate format
            if (!FriendCodeGenerator.isValidFormat(toFriendCode)) {
                return Result.failure(Exception("Invalid friend code format. Use: XXXX-YYYY"))
            }
            
            // Find user by friend code (simple query, no index needed)
            val toUser = searchUserByFriendCode(toFriendCode).getOrNull()
                ?: return Result.failure(Exception("User not found"))

            // Check if same user
            if (fromUserId == toUser.uid) {
                return Result.failure(Exception("Cannot send friend request to yourself"))
            }

            // Check if already friends (simple query)
            val existingFriendship = firestore.collection(Constants.COLLECTION_FRIENDS)
                .whereEqualTo("userId", fromUserId)
                .whereEqualTo("friendId", toUser.uid)
                .get()
                .await()

            if (!existingFriendship.isEmpty) {
                return Result.failure(Exception("Already friends with this user"))
            }

            // Check existing PENDING request (allows re-sending after rejection/cancellation)
            val existingRequest = firestore.collection(Constants.COLLECTION_FRIEND_REQUESTS)
                .whereEqualTo("fromUserId", fromUserId)
                .whereEqualTo("toUserId", toUser.uid)
                .whereEqualTo("status", FriendRequestStatus.PENDING.name)
                .get()
                .await()

            if (!existingRequest.isEmpty) {
                return Result.failure(Exception("Friend request already sent"))
            }

            // Get sender's data
            val fromUserDoc = firestore.collection(Constants.COLLECTION_USERS)
                .document(fromUserId)
                .get()
                .await()
            val fromUsername = fromUserDoc.getString("username") ?: ""

            // Create friend request
            val docRef = firestore.collection(Constants.COLLECTION_FRIEND_REQUESTS).document()
            val friendRequest = FriendRequest(
                id = docRef.id,
                fromUserId = fromUserId,
                fromUsername = fromUsername,
                toUserId = toUser.uid,
                toUsername = toUser.username,
                status = FriendRequestStatus.PENDING,
                timestamp = System.currentTimeMillis()
            )

            docRef.set(friendRequest.toMap()).await()
            Result.success(friendRequest)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Accept a friend request
     */
    suspend fun acceptFriendRequest(requestId: String): Result<Boolean> {
        return try {
            Log.d("FriendRepository", "==== ACCEPTING FRIEND REQUEST ====")
            Log.d("FriendRepository", "Request ID: $requestId")
            
            // Get current user
            val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
            Log.d("FriendRepository", "Current user ID: $currentUserId")
            
            if (currentUserId == null) {
                Log.e("FriendRepository", "ERROR: User not authenticated!")
                return Result.failure(Exception("User not authenticated"))
            }
            
            // Step 1: Get the friend request
            Log.d("FriendRepository", "Step 1: Fetching friend request document...")
            val requestDoc = firestore.collection(Constants.COLLECTION_FRIEND_REQUESTS)
                .document(requestId)
                .get()
                .await()

            if (!requestDoc.exists()) {
                Log.e("FriendRepository", "ERROR: Friend request not found!")
                return Result.failure(Exception("Friend request not found"))
            }
            
            Log.d("FriendRepository", "Friend request found: ${requestDoc.data}")

            val request = FriendRequest.fromMap(requestDoc.data ?: emptyMap())
            Log.d("FriendRepository", "Parsed request - From: ${request.fromUserId}, To: ${request.toUserId}")
            
            // Verify current user is the receiver
            if (request.toUserId != currentUserId) {
                Log.e("FriendRepository", "ERROR: Current user ($currentUserId) is not the receiver (${request.toUserId})")
                return Result.failure(Exception("You are not the receiver of this request"))
            }

            // Step 2: Update request status
            Log.d("FriendRepository", "Step 2: Updating friend request status to ACCEPTED...")
            try {
                firestore.collection(Constants.COLLECTION_FRIEND_REQUESTS)
                    .document(requestId)
                    .update("status", FriendRequestStatus.ACCEPTED.name)
                    .await()
                Log.d("FriendRepository", "✅ Request status updated successfully")
            } catch (e: Exception) {
                Log.e("FriendRepository", "❌ FAILED at Step 2: Updating request status", e)
                throw e
            }

            // Step 3: Get both users' usernames
            Log.d("FriendRepository", "Step 3: Fetching user documents...")
            
            val fromUserDoc = try {
                Log.d("FriendRepository", "Fetching fromUser document: ${request.fromUserId}")
                firestore.collection(Constants.COLLECTION_USERS)
                    .document(request.fromUserId)
                    .get()
                    .await()
            } catch (e: Exception) {
                Log.e("FriendRepository", "❌ FAILED at Step 3a: Reading fromUser document", e)
                throw e
            }
            
            val toUserDoc = try {
                Log.d("FriendRepository", "Fetching toUser document: ${request.toUserId}")
                firestore.collection(Constants.COLLECTION_USERS)
                    .document(request.toUserId)
                    .get()
                    .await()
            } catch (e: Exception) {
                Log.e("FriendRepository", "❌ FAILED at Step 3b: Reading toUser document", e)
                throw e
            }

            val fromUsername = fromUserDoc.getString("username") ?: ""
            val toUsername = toUserDoc.getString("username") ?: ""
            
            Log.d("FriendRepository", "From username: '$fromUsername', To username: '$toUsername'")

            // Step 4: Create bidirectional friendship
            Log.d("FriendRepository", "Step 4: Creating friend documents...")
            
            // Friend document 1: fromUser → toUser
            val friend1Ref = firestore.collection(Constants.COLLECTION_FRIENDS).document()
            val friend1 = Friend(
                id = friend1Ref.id,
                userId = request.fromUserId,
                friendId = request.toUserId,
                friendUsername = toUsername,
                friendName = toUsername,
                currentStreak = 0,
                addedAt = System.currentTimeMillis()
            )
            
            Log.d("FriendRepository", "Creating friend1 document: ${friend1Ref.id}")
            Log.d("FriendRepository", "Friend1 data: userId=${friend1.userId}, friendId=${friend1.friendId}")
            
            try {
                friend1Ref.set(friend1.toMap()).await()
                Log.d("FriendRepository", "✅ Friend1 document created successfully")
            } catch (e: Exception) {
                Log.e("FriendRepository", "❌ FAILED at Step 4a: Creating friend1 document", e)
                Log.e("FriendRepository", "Friend1 data that failed: ${friend1.toMap()}")
                throw e
            }

            // Friend document 2: toUser → fromUser
            val friend2Ref = firestore.collection(Constants.COLLECTION_FRIENDS).document()
            val friend2 = Friend(
                id = friend2Ref.id,
                userId = request.toUserId,
                friendId = request.fromUserId,
                friendUsername = fromUsername,
                friendName = fromUsername,
                currentStreak = 0,
                addedAt = System.currentTimeMillis()
            )
            
            Log.d("FriendRepository", "Creating friend2 document: ${friend2Ref.id}")
            Log.d("FriendRepository", "Friend2 data: userId=${friend2.userId}, friendId=${friend2.friendId}")
            
            try {
                friend2Ref.set(friend2.toMap()).await()
                Log.d("FriendRepository", "✅ Friend2 document created successfully")
            } catch (e: Exception) {
                Log.e("FriendRepository", "❌ FAILED at Step 4b: Creating friend2 document", e)
                Log.e("FriendRepository", "Friend2 data that failed: ${friend2.toMap()}")
                throw e
            }

            Log.d("FriendRepository", "==== FRIEND REQUEST ACCEPTED SUCCESSFULLY ====")
            Result.success(true)
        } catch (e: Exception) {
            Log.e("FriendRepository", "==== ACCEPT REQUEST FAILED ====")
            Log.e("FriendRepository", "Exception type: ${e.javaClass.simpleName}")
            Log.e("FriendRepository", "Error message: ${e.message}")
            Log.e("FriendRepository", "Stack trace:", e)
            Result.failure(e)
        }
    }

    /**
     * Reject a friend request
     */
    suspend fun rejectFriendRequest(requestId: String): Result<Boolean> {
        return try {
            firestore.collection(Constants.COLLECTION_FRIEND_REQUESTS)
                .document(requestId)
                .update("status", FriendRequestStatus.REJECTED.name)
                .await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get friends with direct callback - NO COROUTINES
     */
    fun observeFriends(
        userId: String, 
        onSuccess: (List<Friend>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        Log.d("FriendRepository", "Setting up direct friends listener for user: $userId")
        
        // Remove old listener if exists
        friendsListener?.remove()
        
        // Create new listener
        friendsListener = firestore.collection(Constants.COLLECTION_FRIENDS)
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FriendRepository", "Error in friends listener", error)
                    onError(error)
                    return@addSnapshotListener
                }

                val friends = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.data?.let { Friend.fromMap(it) }
                    } catch (e: Exception) {
                        Log.e("FriendRepository", "Error parsing friend", e)
                        null
                    }
                }?.sortedByDescending { it.addedAt } ?: emptyList()

                Log.d("FriendRepository", "Friends received: ${friends.size} friends")
                onSuccess(friends)
            }
    }
    
    /**
     * Get friend requests with direct callback - NO COROUTINES
     */
    fun observeFriendRequests(
        userId: String,
        onSuccess: (List<FriendRequest>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        Log.d("FriendRepository", "Setting up direct requests listener for user: $userId")
        
        // Remove old listener if exists
        requestsListener?.remove()
        
        // Create new listener
        requestsListener = firestore.collection(Constants.COLLECTION_FRIEND_REQUESTS)
            .whereEqualTo("toUserId", userId)
            .whereEqualTo("status", FriendRequestStatus.PENDING.name)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FriendRepository", "Error in requests listener", error)
                    onError(error)
                    return@addSnapshotListener
                }

                val requests = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.data?.let { FriendRequest.fromMap(it) }
                    } catch (e: Exception) {
                        Log.e("FriendRepository", "Error parsing request", e)
                        null
                    }
                }?.sortedByDescending { it.timestamp } ?: emptyList()

                Log.d("FriendRepository", "Requests received: ${requests.size} requests")
                onSuccess(requests)
            }
    }
    
    /**
     * Stop all listeners when no longer needed
     */
    fun stopListeners() {
        Log.d("FriendRepository", "Stopping all listeners")
        friendsListener?.remove()
        requestsListener?.remove()
        friendsListener = null
        requestsListener = null
    }

    /**
     * Remove a friend
     */
    suspend fun removeFriend(userId: String, friendId: String): Result<Boolean> {
        return try {
            // Remove from user's friend list
            val userFriendDoc = firestore.collection(Constants.COLLECTION_FRIENDS)
                .whereEqualTo("userId", userId)
                .whereEqualTo("friendId", friendId)
                .get()
                .await()

            userFriendDoc.documents.forEach { it.reference.delete().await() }

            // Remove from friend's friend list (bidirectional)
            val friendUserDoc = firestore.collection(Constants.COLLECTION_FRIENDS)
                .whereEqualTo("userId", friendId)
                .whereEqualTo("friendId", userId)
                .get()
                .await()

            friendUserDoc.documents.forEach { it.reference.delete().await() }

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Search for a user by friend code (simple query, no index needed)
     */
    suspend fun searchUserByFriendCode(friendCode: String): Result<User?> {
        return try {
            val snapshot = firestore.collection(Constants.COLLECTION_USERS)
                .whereEqualTo("friendCode", friendCode)
                .limit(1)
                .get()
                .await()

            val user = snapshot.documents.firstOrNull()?.data?.let { User.fromMap(it) }
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
