# FitGen - Fitness Android Application

FitGen is a comprehensive fitness Android application built with Kotlin that helps users track their fitness journey, discover workouts, and achieve their health goals. The app features Firebase authentication and personalized calorie calculations.

## Features

### 🔐 Authentication
- Firebase email/password authentication
- User registration and login
- Password reset functionality
- Auto-login for authenticated users

### 📊 Personalized Onboarding
- Multi-step questionnaire to capture user information:
  - Age and gender
  - Height (cm) and weight (kg)
  - Fitness goal (lose weight, maintain, gain muscle)
  - Activity level (sedentary to very active)

### 🏠 Dashboard Home Screen
- Daily calorie target display
- BMR (Basal Metabolic Rate) calculation
- BMI (Body Mass Index) display
- Recommended workout suggestions

### 💪 Comprehensive Workout Library
- **8 Categories**: Abs, Biceps, Chest, Legs, Shoulders, Back, Triceps, Cardio
- **26 Pre-loaded Exercises** with:
  - Exercise name and category
  - Difficulty level (Beginner, Intermediate, Advanced)
  - Duration and calories burned
  - Sets and reps recommendations
  - Detailed instructions
- Category-based browsing

### 👤 User Profile Management
- View and edit personal information
- Track progress with weight history
- Add progress entries with notes
- Logout functionality

### 📈 Calorie Calculator
- **BMR Calculation** using Mifflin-St Jeor equation
  - Men: BMR = 10 × weight(kg) + 6.25 × height(cm) - 5 × age(years) + 5
  - Women: BMR = 10 × weight(kg) + 6.25 × height(cm) - 5 × age(years) - 161
- **TDEE Calculation** based on activity level multipliers:
  - Sedentary: BMR × 1.2
  - Lightly Active: BMR × 1.375
  - Moderately Active: BMR × 1.55
  - Very Active: BMR × 1.725
- **Goal-based adjustments**:
  - Lose weight: -500 cal/day
  - Maintain: TDEE
  - Gain muscle: +300 cal/day

## Technical Architecture

### MVVM Architecture Pattern
- **Models**: User, Workout, ProgressEntry
- **ViewModels**: UserViewModel, WorkoutViewModel
- **Views**: Activities and Fragments
- **Repositories**: UserRepository, WorkoutRepository

### Technology Stack
- **Language**: Kotlin
- **UI**: Material Design 3 components
- **Architecture**: MVVM (Model-View-ViewModel)
- **Backend**: Firebase (Authentication + Firestore)
- **Navigation**: AndroidX Navigation Component
- **Lifecycle**: ViewModel + LiveData
- **Storage**: Firebase Firestore + SharedPreferences
- **Layout**: ConstraintLayout

### Dependencies
```gradle
// AndroidX Core
implementation("androidx.core:core-ktx:1.12.0")
implementation("androidx.appcompat:appcompat:1.6.1")
implementation("androidx.constraintlayout:constraintlayout:2.1.4")

// Material Design 3
implementation("com.google.android.material:material:1.11.0")

// Navigation Component
implementation("androidx.navigation:navigation-fragment-ktx:2.7.6")
implementation("androidx.navigation:navigation-ui-ktx:2.7.6")

// Lifecycle components
implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")
implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")

// RecyclerView
implementation("androidx.recyclerview:recyclerview:1.3.2")

// Firebase
implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
implementation("com.google.firebase:firebase-auth-ktx")
implementation("com.google.firebase:firebase-firestore-ktx")

// Coroutines
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
```

## Project Structure

```
app/
├── src/main/
│   ├── java/com/fitgen/app/
│   │   ├── models/              # Data models
│   │   │   ├── User.kt
│   │   │   ├── Workout.kt
│   │   │   └── ProgressEntry.kt
│   │   ├── activities/          # Activities
│   │   │   ├── LoginActivity.kt
│   │   │   ├── RegisterActivity.kt
│   │   │   ├── OnboardingActivity.kt
│   │   │   ├── MainActivity.kt
│   │   │   ├── WorkoutCategoryActivity.kt
│   │   │   └── WorkoutDetailActivity.kt
│   │   ├── fragments/           # Fragments
│   │   │   ├── HomeFragment.kt
│   │   │   ├── WorkoutsFragment.kt
│   │   │   └── ProfileFragment.kt
│   │   ├── adapters/            # RecyclerView Adapters
│   │   │   ├── WorkoutCategoryAdapter.kt
│   │   │   ├── WorkoutAdapter.kt
│   │   │   └── ProgressAdapter.kt
│   │   ├── viewmodels/          # ViewModels
│   │   │   ├── UserViewModel.kt
│   │   │   └── WorkoutViewModel.kt
│   │   ├── repositories/        # Data repositories
│   │   │   ├── UserRepository.kt
│   │   │   └── WorkoutRepository.kt
│   │   ├── utils/               # Utility classes
│   │       ├── CalorieCalculator.kt
│   │       └── Constants.kt
│   ├── res/
│   │   ├── layout/             # XML layouts
│   │   ├── values/             # Resources
│   │   │   ├── strings.xml
│   │   │   ├── colors.xml
│   │   │   └── themes.xml
│   │   ├── drawable/           # Icons and images
│   │   ├── navigation/         # Navigation graph
│   │   └── menu/               # Bottom navigation menu
│   └── AndroidManifest.xml
└── build.gradle.kts
```

## Setup Instructions

### Prerequisites
- Android Studio (Latest version recommended)
- JDK 8 or higher
- Android SDK (API 24 - API 34)
- Firebase account

### Firebase Setup

1. **Create a Firebase Project**
   - Go to [Firebase Console](https://console.firebase.google.com/)
   - Create a new project or select an existing one
   - Add an Android app to your Firebase project

2. **Register Your App**
   - Package name: `com.fitgen.app`
   - Download the `google-services.json` file
   - Place it in the `app/` directory of your project

3. **Enable Firebase Services**
   - **Authentication**: Enable Email/Password sign-in method
   - **Firestore Database**: Create a Firestore database in production mode
   - Set up the following Firestore collections:
     - `users` - User profiles
     - `progress` - Progress tracking entries

4. **Firestore Security Rules** (Optional but recommended)
   ```javascript
   rules_version = '2';
   service cloud.firestore {
     match /databases/{database}/documents {
       // Users collection
       match /users/{userId} {
         allow read, write: if request.auth != null && request.auth.uid == userId;
       }
       
       // Progress collection
       match /progress/{progressId} {
         allow read, write: if request.auth != null;
       }
     }
   }
   ```

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/Kris2k5/Christopher-Hanna-Nehme.git
   cd Christopher-Hanna-Nehme
   ```

2. **Open in Android Studio**
   - Open Android Studio
   - Select "Open an Existing Project"
   - Navigate to the cloned repository folder
   - Wait for Gradle sync to complete

3. **Add google-services.json**
   - Place your `google-services.json` file in the `app/` directory
   - **Important**: This file contains your Firebase configuration

4. **Build and Run**
   - Connect an Android device or start an emulator
   - Click "Run" (▶️) in Android Studio
   - Select your device/emulator
   - Wait for the app to install and launch

### Gradle Sync Issues

If you encounter Gradle sync issues:
```bash
./gradlew clean
./gradlew build
```

## Sample Workout Data

The app includes 26 pre-loaded exercises across 8 categories:

- **Abs** (3): Crunches, Plank, Russian Twists
- **Biceps** (3): Bicep Curls, Hammer Curls, Concentration Curls
- **Chest** (3): Push-ups, Bench Press, Chest Flyes
- **Legs** (4): Squats, Lunges, Leg Press, Calf Raises
- **Shoulders** (3): Shoulder Press, Lateral Raises, Front Raises
- **Back** (3): Pull-ups, Bent-over Rows, Deadlifts
- **Triceps** (3): Tricep Dips, Overhead Extension, Kickbacks
- **Cardio** (4): Running, Jumping Jacks, Burpees, Mountain Climbers

Each exercise includes:
- Detailed instructions
- Difficulty level
- Duration and calorie burn estimates
- Sets and reps recommendations

## Key Components Explained

### CalorieCalculator
Handles all calorie-related calculations:
- BMR calculation using Mifflin-St Jeor equation
- TDEE calculation with activity level multipliers
- Daily calorie target based on fitness goals
- BMI calculation and categorization

### UserRepository
Manages user data persistence:
- Firebase Firestore operations (read/write user data)
- SharedPreferences for quick access
- Progress entry management
- Onboarding status tracking

### WorkoutRepository
Provides workout data:
- Returns all pre-loaded workouts
- Filters by category
- Can be extended to fetch from Firestore

## Security Considerations

### Local Storage
- Sensitive user data is stored in Firebase Firestore
- SharedPreferences used for quick access to non-sensitive data
- User authentication state managed by Firebase Auth

### Firebase Security
- Firestore security rules should be configured to restrict access
- Users can only read/write their own data
- Authentication required for all operations

### Backup Rules
- Cache and SharedPreferences excluded from backups
- Configured in `backup_rules.xml` and `data_extraction_rules.xml`

## Future Enhancements

Potential features for future versions:
- [ ] Workout history and completion tracking
- [ ] Custom workout creation
- [ ] Social features (share workouts, compete with friends)
- [ ] Integration with fitness wearables
- [ ] Video demonstrations for exercises
- [ ] Meal planning and nutrition tracking
- [ ] Push notifications for workout reminders
- [ ] Dark theme support
- [ ] Multiple language support
- [ ] Export progress data

## Testing

### Manual Testing Checklist
- [ ] User registration with valid/invalid inputs
- [ ] Login with correct/incorrect credentials
- [ ] Password reset functionality
- [ ] Onboarding flow completion
- [ ] Profile editing and updates
- [ ] Progress entry addition
- [ ] Workout browsing by category
- [ ] Workout detail viewing
- [ ] Calorie calculation accuracy
- [ ] Logout functionality

## Troubleshooting

### Common Issues

**Issue**: App crashes on startup
- **Solution**: Ensure `google-services.json` is in the correct location

**Issue**: Login/Registration not working
- **Solution**: Check Firebase Authentication is enabled in Firebase Console

**Issue**: Data not saving to Firestore
- **Solution**: Verify Firestore security rules and user authentication

**Issue**: Gradle build fails
- **Solution**: Run `./gradlew clean` and rebuild

## License

This project is for educational purposes. Please ensure you have the proper rights and licenses for any production use.

## Contributors

- **Developer**: Christopher Hanna Nehme
- **Repository**: [GitHub - Kris2k5/Christopher-Hanna-Nehme](https://github.com/Kris2k5/Christopher-Hanna-Nehme)

## Acknowledgments

- Material Design 3 guidelines by Google
- Firebase platform by Google
- Android Jetpack components
- Kotlin programming language

## Support

For issues, questions, or contributions:
1. Open an issue on GitHub
2. Submit a pull request
3. Contact the repository maintainer

---

**Built with ❤️ using Kotlin and Material Design 3**
