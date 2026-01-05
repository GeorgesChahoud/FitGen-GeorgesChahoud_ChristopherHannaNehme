# Firestore Security Rules for Daily Challenges & Friends Leaderboard

This document contains the security rules that need to be added to your Firestore database to support the Daily Challenges and Friends Leaderboard features.

## How to Apply These Rules

1. Go to the Firebase Console: https://console.firebase.google.com
2. Select your project
3. Navigate to Firestore Database
4. Click on the "Rules" tab
5. Add the rules below to your existing rules
6. Click "Publish"

## Security Rules

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // Existing rules for users collection
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
    
    // Existing rules for progress collection
    match /progress/{progressId} {
      allow read, write: if request.auth != null && request.auth.uid == resource.data.userId;
    }
    
    // Existing rules for completed_workouts collection
    match /completed_workouts/{workoutId} {
      allow read, write: if request.auth != null && request.auth.uid == resource.data.userId;
    }
    
    // NEW: Friend requests
    match /friend_requests/{requestId} {
      // Users can read requests where they are either the sender or receiver
      allow read: if request.auth != null && 
                    (request.auth.uid == resource.data.fromUserId || 
                     request.auth.uid == resource.data.toUserId);
      
      // Users can create requests only as the sender
      allow create: if request.auth != null && 
                      request.auth.uid == request.resource.data.fromUserId;
      
      // Only the receiver can update (accept/reject) a request
      allow update: if request.auth != null && 
                      request.auth.uid == resource.data.toUserId;
      
      // Both sender and receiver can delete the request
      allow delete: if request.auth != null && 
                      (request.auth.uid == resource.data.fromUserId || 
                       request.auth.uid == resource.data.toUserId);
    }
    
    // NEW: Friends
    match /friends/{friendId} {
      // Users can read friend relationships where they are one of the parties
      allow read: if request.auth != null && 
                    (request.auth.uid == resource.data.userId || 
                     request.auth.uid == resource.data.friendId);
      
      // Users can create friend relationships only for themselves
      allow create: if request.auth != null && 
                      request.auth.uid == request.resource.data.userId;
      
      // Users can update friend relationships where they are the owner
      allow update: if request.auth != null && 
                      request.auth.uid == resource.data.userId;
      
      // Users can delete friend relationships where they are one of the parties
      allow delete: if request.auth != null && 
                      (request.auth.uid == resource.data.userId || 
                       request.auth.uid == resource.data.friendId);
    }
    
    // NEW: Daily challenges
    match /daily_challenges/{challengeId} {
      // Users can only read and write their own challenges
      allow read, write: if request.auth != null && 
                           request.auth.uid == resource.data.userId;
      
      // Allow creation of new challenges
      allow create: if request.auth != null && 
                      request.auth.uid == request.resource.data.userId;
    }
    
    // NEW: User streaks
    match /user_streaks/{userId} {
      // Anyone authenticated can read streaks (for leaderboard)
      allow read: if request.auth != null;
      
      // Only the user can write their own streak
      allow write: if request.auth != null && 
                     request.auth.uid == userId;
    }
  }
}
```

## Explanation of Rules

### Friend Requests (`friend_requests`)
- **Read**: Users can see requests they sent or received
- **Create**: Users can send requests (as the sender)
- **Update**: Only the receiver can accept/reject requests
- **Delete**: Both parties can delete the request

### Friends (`friends`)
- **Read**: Users can see friendships they're part of
- **Create**: Users can create friendships for themselves (bidirectional relationships)
- **Update**: Users can update their own friend entries
- **Delete**: Both users in the friendship can remove it

### Daily Challenges (`daily_challenges`)
- **Read/Write**: Users can only access their own challenges
- **Create**: Users can create challenges for themselves

### User Streaks (`user_streaks`)
- **Read**: All authenticated users can read streaks (needed for leaderboard)
- **Write**: Users can only update their own streak data

## Security Considerations

1. **Privacy**: User streak data is readable by all authenticated users to enable the leaderboard feature. If you need more privacy, you can create a separate `public_streaks` collection.

2. **Rate Limiting**: Consider implementing rate limiting for friend requests to prevent spam. This can be done using Firebase Functions with Cloud Firestore triggers.

3. **Data Validation**: The rules above provide basic security. For production, consider adding field-level validation:
   ```javascript
   // Example: Validate friend request fields
   allow create: if request.auth != null && 
                   request.auth.uid == request.resource.data.fromUserId &&
                   request.resource.data.status == 'PENDING' &&
                   request.resource.data.timestamp is timestamp;
   ```

4. **Indexes**: You'll need to create composite indexes for efficient queries. Firestore will prompt you to create these when you first run queries that need them.

## Required Firestore Indexes

Create these indexes in the Firebase Console under Firestore > Indexes:

### Collection: `friend_requests`
- Fields: `toUserId` (Ascending), `status` (Ascending), `timestamp` (Descending)
- Fields: `fromUserId` (Ascending), `status` (Ascending), `timestamp` (Descending)

### Collection: `friends`
- Fields: `userId` (Ascending), `addedAt` (Descending)

### Collection: `daily_challenges`
- Fields: `userId` (Ascending), `date` (Ascending)

### Collection: `user_streaks`
- No additional indexes needed for basic queries

Firebase will automatically create single-field indexes. These composite indexes are only needed for complex queries.

## Testing the Rules

After deploying the rules, test them thoroughly:

1. Try to read another user's challenge (should fail)
2. Try to update another user's streak (should fail)
3. Send a friend request (should succeed)
4. Try to accept your own request as the sender (should fail)
5. View the leaderboard (should show all users' streaks)

## Production Recommendations

For production deployment:

1. Add request validation to check data types and required fields
2. Implement server-side Cloud Functions for complex operations
3. Add rate limiting for friend requests
4. Consider separating public vs. private streak data
5. Add monitoring and alerts for security rule violations
6. Regularly audit access patterns and update rules as needed
