package com.fitgen.app.utils

import com.fitgen.app.models.DailyWorkoutPlan
import com.fitgen.app.models.User
import com.fitgen.app.models.WeeklyWorkoutPlan
import com.fitgen.app.models.Workout
import kotlin.random.Random

object WorkoutPlanGenerator {
    
    /**
     * Generate a weekly workout plan based on user profile
     */
    fun generateWeeklyPlan(
        user: User,
        allWorkouts: List<Workout>
    ): WeeklyWorkoutPlan {
        val daysPerWeek = user.workoutDaysPerWeek.coerceIn(2, 7)
        val dailyPlans = mutableListOf<DailyWorkoutPlan>()
        
        val workoutDays = selectWorkoutDays(daysPerWeek)
        val daysOfWeek = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
        
        daysOfWeek.forEach { day ->
            if (workoutDays.contains(day)) {
                val plan = createDailyPlan(day, user, allWorkouts, workoutDays.indexOf(day))
                dailyPlans.add(plan)
            } else {
                dailyPlans.add(createRestDay(day))
            }
        }
        
        return WeeklyWorkoutPlan(
            userId = user.uid,
            weekStartDate = getCurrentWeekStart(),
            dailyPlans = dailyPlans
        )
    }
    
    /**
     * Select which days to workout based on frequency
     */
    private fun selectWorkoutDays(daysPerWeek: Int): List<String> {
        return when (daysPerWeek) {
            2 -> listOf("Monday", "Thursday")
            3 -> listOf("Monday", "Wednesday", "Friday")
            4 -> listOf("Monday", "Tuesday", "Thursday", "Friday")
            5 -> listOf("Monday", "Tuesday", "Wednesday", "Friday", "Saturday")
            6 -> listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
            7 -> listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
            else -> listOf("Monday", "Wednesday", "Friday")
        }
    }
    
    /**
     * Create a daily workout plan with themed exercises
     */
    private fun createDailyPlan(
        day: String,
        user: User,
        allWorkouts: List<Workout>,
        dayIndex: Int
    ): DailyWorkoutPlan {
        // Determine workout theme based on day and user goal
        val (dayName, focusCategories) = getDayTheme(day, dayIndex, user.goal, user.workoutDaysPerWeek)
        
        // Use seeded random based on user ID and week to ensure consistency
        val seed = "${user.uid}-${getCurrentWeekStart()}".hashCode().toLong()
        val random = Random(seed)
        
        // Select workouts from focused categories with seeded random
        val exercisesPerCategory = 2
        val selectedWorkouts = focusCategories.flatMap { category ->
            allWorkouts.filter { it.category == category }
                .shuffled(random)  // ✅ Deterministic shuffle
                .take(exercisesPerCategory)
        }
        
        val workoutIds = selectedWorkouts.map { it.id }
        val totalDuration = selectedWorkouts.sumOf { it.duration }
        val totalCalories = selectedWorkouts.sumOf { it.caloriesBurn }
        
        return DailyWorkoutPlan(
            dayOfWeek = day,
            dayName = dayName,
            description = "Focus on ${focusCategories.joinToString(" & ")}",
            workoutIds = workoutIds,
            isRestDay = false,
            totalDuration = totalDuration,
            totalCalories = totalCalories
        ).apply {
            workouts = selectedWorkouts  // Populate transient field
        }
    }
    
    /**
     * Determine workout theme for each day
     */
    private fun getDayTheme(day: String, dayIndex: Int, goal: String, daysPerWeek: Int): Pair<String, List<String>> {
        // For weight loss: more cardio and full body
        // For muscle gain: split by muscle groups
        // For maintenance: balanced approach
        
        return when (goal) {
            Constants.GOAL_LOSE_WEIGHT -> {
                when (dayIndex % 3) {
                    0 -> "Cardio & Core" to listOf("cardio", "abs")
                    1 -> "Full Body Burn" to listOf("legs", "chest", "back")
                    else -> "HIIT & Strength" to listOf("cardio", "shoulders", "biceps")
                }
            }
            Constants.GOAL_GAIN_MUSCLE -> {
                when (dayIndex % 4) {
                    0 -> "Chest & Triceps" to listOf("chest", "triceps")
                    1 -> "Back & Biceps" to listOf("back", "biceps")
                    2 -> "Legs & Core" to listOf("legs", "abs")
                    else -> "Shoulders & Arms" to listOf("shoulders", "biceps", "triceps")
                }
            }
            else -> {  // Maintenance or any other goal
                when (dayIndex % 3) {
                    0 -> "Upper Body" to listOf("chest", "back", "shoulders")
                    1 -> "Lower Body & Core" to listOf("legs", "abs")
                    else -> "Full Body Mix" to listOf("cardio", "biceps", "triceps")
                }
            }
        }
    }
    
    private fun createRestDay(day: String): DailyWorkoutPlan {
        return DailyWorkoutPlan(
            dayOfWeek = day,
            dayName = "Rest Day",
            description = "Recovery and rest",
            workoutIds = emptyList(),
            isRestDay = true,
            totalDuration = 0,
            totalCalories = 0
        )
    }
    
    private fun getCurrentWeekStart(): String {
        // Return current week's Monday in "yyyy-MM-dd" format
        val calendar = java.util.Calendar.getInstance()
        calendar.firstDayOfWeek = java.util.Calendar.MONDAY
        calendar.set(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.MONDAY)
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        return sdf.format(calendar.time)
    }
}
