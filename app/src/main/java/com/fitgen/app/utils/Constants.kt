package com.fitgen.app.utils

object Constants {
    // Shared Preferences
    const val PREF_NAME = "FitGenPreferences"
    const val PREF_USER_UID = "user_uid"
    const val PREF_USER_EMAIL = "user_email"
    const val PREF_USER_USERNAME = "user_username"
    const val PREF_USER_FRIEND_CODE = "user_friend_code"
    const val PREF_USER_AGE = "user_age"
    const val PREF_USER_HEIGHT = "user_height"
    const val PREF_USER_WEIGHT = "user_weight"
    const val PREF_USER_GOAL = "user_goal"
    const val PREF_USER_ACTIVITY_LEVEL = "user_activity_level"
    const val PREF_USER_GENDER = "user_gender"
    const val PREF_USER_WORKOUT_DAYS_PER_WEEK = "user_workout_days_per_week"
    const val PREF_IS_LOGGED_IN = "is_logged_in"
    const val PREF_HAS_COMPLETED_ONBOARDING = "has_completed_onboarding"
    const val PREF_LAST_CHALLENGE_CHECK = "last_challenge_check"
    const val PREF_CURRENT_STREAK = "current_streak"

    // Firestore Collections
    const val COLLECTION_USERS = "users"
    const val COLLECTION_PROGRESS = "progress"
    const val COLLECTION_WORKOUTS = "workouts"
    const val COLLECTION_COMPLETED_WORKOUTS = "completed_workouts"
    const val COLLECTION_FRIEND_REQUESTS = "friend_requests"
    const val COLLECTION_FRIENDS = "friends"
    const val COLLECTION_DAILY_CHALLENGES = "daily_challenges"
    const val COLLECTION_USER_STREAKS = "user_streaks"
    const val COLLECTION_WEEKLY_PLANS = "weekly_workout_plans"

    // Goals
    const val GOAL_LOSE_WEIGHT = "lose_weight"
    const val GOAL_MAINTAIN = "maintain"
    const val GOAL_GAIN_MUSCLE = "gain_muscle"

    // Activity Levels
    const val ACTIVITY_SEDENTARY = "sedentary"
    const val ACTIVITY_LIGHTLY_ACTIVE = "lightly_active"
    const val ACTIVITY_MODERATELY_ACTIVE = "moderately_active"
    const val ACTIVITY_VERY_ACTIVE = "very_active"

    // Activity Level Multipliers for TDEE calculation
    const val MULTIPLIER_SEDENTARY = 1.2
    const val MULTIPLIER_LIGHTLY_ACTIVE = 1.375
    const val MULTIPLIER_MODERATELY_ACTIVE = 1.55
    const val MULTIPLIER_VERY_ACTIVE = 1.725

    // Calorie Adjustments
    const val CALORIE_DEFICIT_LOSE_WEIGHT = -500
    const val CALORIE_SURPLUS_GAIN_MUSCLE = 300

    // Workout Categories
    const val CATEGORY_ABS = "abs"
    const val CATEGORY_BICEPS = "biceps"
    const val CATEGORY_CHEST = "chest"
    const val CATEGORY_LEGS = "legs"
    const val CATEGORY_SHOULDERS = "shoulders"
    const val CATEGORY_BACK = "back"
    const val CATEGORY_TRICEPS = "triceps"
    const val CATEGORY_CARDIO = "cardio"

    // Difficulty Levels
    const val DIFFICULTY_BEGINNER = "beginner"
    const val DIFFICULTY_INTERMEDIATE = "intermediate"
    const val DIFFICULTY_ADVANCED = "advanced"

    // Gender
    const val GENDER_MALE = "male"
    const val GENDER_FEMALE = "female"

    // Validation Constants
    const val MIN_AGE = 13
    const val MAX_AGE = 120
    const val MIN_HEIGHT = 50
    const val MAX_HEIGHT = 300
    const val MIN_WEIGHT = 20.0
    const val MAX_WEIGHT = 500.0
}
