# Firebase Setup Guide for FitGen

This guide will walk you through setting up Firebase for the FitGen Android application.

## Prerequisites
- A Google account
- Android Studio with the FitGen project loaded

## Step-by-Step Setup

### 1. Create a Firebase Project

1. Go to the [Firebase Console](https://console.firebase.google.com/)
2. Click on "Add project" or "Create a project"
3. Enter a project name (e.g., "FitGen")
4. Click "Continue"
5. (Optional) Enable Google Analytics for your project
6. Click "Create project"
7. Wait for the project to be created, then click "Continue"

### 2. Add Android App to Firebase

1. In your Firebase project, click on the Android icon (🤖) to add an Android app
2. Register your app with these details:
   - **Android package name**: `com.fitgen.app` (IMPORTANT: Must match exactly)
   - **App nickname** (optional): "FitGen Android"
   - **Debug signing certificate SHA-1** (optional): You can add this later for advanced features
3. Click "Register app"

### 3. Download Configuration File

1. Download the `google-services.json` file
2. Move this file to your Android Studio project's `app/` directory
   ```
   YourProject/
   ├── app/
   │   ├── google-services.json  <-- Place it here
   │   ├── build.gradle.kts
   │   └── src/
   ```
3. **IMPORTANT**: Do NOT commit this file to version control. It contains sensitive API keys.
4. The `.gitignore` file is already configured to exclude `google-services.json`

### 4. Enable Firebase Services

#### 4.1 Enable Authentication

1. In the Firebase Console, click on "Authentication" in the left sidebar
2. Click on "Get started"
3. Go to the "Sign-in method" tab
4. Click on "Email/Password"
5. Enable the "Email/Password" toggle
6. Click "Save"

**Result**: Users can now register and login with email/password

#### 4.2 Set up Firestore Database

1. In the Firebase Console, click on "Firestore Database" in the left sidebar
2. Click "Create database"
3. Choose "Start in production mode" (we'll add security rules next)
4. Select a Cloud Firestore location (choose one closest to your users)
5. Click "Enable"

**Result**: A Firestore database is created for storing user data

#### 4.3 Configure Firestore Security Rules

1. In Firestore Database, go to the "Rules" tab
2. Replace the default rules with the following:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Allow users to read and write their own user document
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
    
    // Allow authenticated users to read and write progress entries
    match /progress/{progressId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && 
                     request.resource.data.userId == request.auth.uid;
    }
    
    // Deny all other access
    match /{document=**} {
      allow read, write: if false;
    }
  }
}
```

3. Click "Publish"

**What these rules do**:
- Users can only access their own user profile
- Users can add progress entries but only for themselves
- All other access is denied by default

### 5. Create Firestore Collections (Optional)

The app will automatically create collections when needed, but you can create them manually:

1. In Firestore Database, click "Start collection"
2. Collection ID: `users`
3. Add a dummy document (you can delete it later):
   - Document ID: `dummy`
   - Field: `name` (string) = `"test"`
4. Repeat for `progress` collection

### 6. Verify Setup in Android Studio

1. Open the project in Android Studio
2. Verify that `google-services.json` is in the `app/` directory
3. Sync the project with Gradle files
4. Build the project (should compile successfully)

### 7. Test the Setup

1. Run the app on an emulator or physical device
2. Try to register a new user
3. Check Firebase Console:
   - Go to Authentication → Users
   - You should see the newly registered user
4. Complete the onboarding process
5. Check Firestore Database:
   - Go to Firestore Database → Data
   - You should see a document in the `users` collection with the user's data

## Troubleshooting

### Problem: "google-services.json not found"
**Solution**: Ensure the file is in the `app/` directory (not the root directory)

### Problem: Authentication fails
**Solution**: 
- Check that Email/Password authentication is enabled in Firebase Console
- Verify the package name matches exactly: `com.fitgen.app`
- Check your internet connection

### Problem: Firestore operations fail
**Solution**:
- Verify Firestore is enabled in Firebase Console
- Check security rules are properly set
- Ensure user is authenticated before Firestore operations

### Problem: "Default FirebaseApp is not initialized"
**Solution**:
- Ensure `google-services.json` is in the correct location
- Clean and rebuild the project
- Verify the Firebase Gradle plugin is applied in `build.gradle.kts`

### Problem: SHA-1 certificate error
**Solution**:
- This is only needed for advanced features (Google Sign-In, Dynamic Links)
- For basic authentication, SHA-1 is not required
- To get your SHA-1:
  ```bash
  ./gradlew signingReport
  ```

## Security Best Practices

1. **Never commit `google-services.json` to version control**
   - The `.gitignore` file excludes it by default
   - Store it securely and share it privately with team members

2. **Use strong Firestore security rules**
   - The rules provided ensure users can only access their own data
   - Test your rules using the Firebase Console Rules simulator

3. **Enable App Check (Optional but Recommended)**
   - Protects your Firebase resources from abuse
   - Go to Firebase Console → App Check
   - Follow the setup instructions

4. **Monitor Usage**
   - Regularly check Firebase Console → Usage
   - Set up budget alerts to avoid unexpected costs

## Additional Firebase Features (Optional)

### Cloud Storage
For storing workout images or user profile pictures:
1. Go to Storage in Firebase Console
2. Click "Get started"
3. Set up security rules similar to Firestore

### Cloud Messaging (FCM)
For push notifications (workout reminders):
1. Go to Cloud Messaging in Firebase Console
2. Follow the setup guide
3. Add FCM dependency to your app

### Remote Config
For A/B testing and feature flags:
1. Go to Remote Config in Firebase Console
2. Set up configuration parameters
3. Add Remote Config dependency to your app

### Analytics
For tracking user behavior:
1. Enable Google Analytics when creating the project
2. View analytics data in Firebase Console → Analytics

## Support

If you encounter any issues:
1. Check the [Firebase Documentation](https://firebase.google.com/docs/android/setup)
2. Visit [Stack Overflow](https://stackoverflow.com/questions/tagged/firebase) with the `firebase` tag
3. Open an issue in the GitHub repository

## Next Steps

After completing Firebase setup:
1. ✅ Test user registration and login
2. ✅ Complete the onboarding flow
3. ✅ Verify user data is saved to Firestore
4. ✅ Test the injury-safe mode feature
5. ✅ Add progress entries and verify they're saved
6. ✅ Explore the workout library

Congratulations! Your Firebase setup is complete. The FitGen app is now ready to use! 🎉
