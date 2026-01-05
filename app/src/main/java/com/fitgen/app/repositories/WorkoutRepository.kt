package com.fitgen.app.repositories

import com.fitgen.app.models.Workout
import com.fitgen.app.utils.Constants

class WorkoutRepository {
    
    /**
     * Get all workouts
     * In a real app, this would fetch from Firestore
     * For now, we'll use hardcoded data
     */
    fun getAllWorkouts(): List<Workout> {
        return getSampleWorkouts()
    }

    /**
     * Get workouts by category
     */
    fun getWorkoutsByCategory(category: String): List<Workout> {
        return getAllWorkouts().filter { it.category == category }
    }

    /**
     * Get workout by ID
     */
    fun getWorkoutById(id: String): Workout? {
        return getAllWorkouts().find { it.id == id }
    }

    /**
     * Get all workout categories
     */
    fun getAllCategories(): List<String> {
        return listOf(
            Constants.CATEGORY_ABS,
            Constants.CATEGORY_BICEPS,
            Constants.CATEGORY_CHEST,
            Constants.CATEGORY_LEGS,
            Constants.CATEGORY_SHOULDERS,
            Constants.CATEGORY_BACK,
            Constants.CATEGORY_TRICEPS,
            Constants.CATEGORY_CARDIO
        )
    }

    /**
     * Sample workout data - 20+ exercises
     */
    private fun getSampleWorkouts(): List<Workout> {
        return listOf(
            // ABS
            Workout(
                id = "1",
                name = "Crunches",
                category = Constants.CATEGORY_ABS,
                difficulty = Constants.DIFFICULTY_BEGINNER,
                duration = 10,
                caloriesBurn = 50,
                instructions = "Lie on your back with knees bent and feet flat. Place hands behind head. Lift shoulders off ground, engaging core. Lower back down with control.",
                sets = 3,
                reps = "15-20"
            ),
            Workout(
                id = "2",
                name = "Plank",
                category = Constants.CATEGORY_ABS,
                difficulty = Constants.DIFFICULTY_BEGINNER,
                duration = 5,
                caloriesBurn = 30,
                instructions = "Start in push-up position. Lower to forearms. Keep body straight from head to heels. Hold position, engaging core.",
                sets = 3,
                reps = "30-60 seconds"
            ),
            Workout(
                id = "3",
                name = "Russian Twists",
                category = Constants.CATEGORY_ABS,
                difficulty = Constants.DIFFICULTY_INTERMEDIATE,
                duration = 10,
                caloriesBurn = 60,
                instructions = "Sit with knees bent, feet off ground. Lean back slightly. Rotate torso side to side, touching ground beside you.",
                sets = 3,
                reps = "20-30"
            ),
            
            // BICEPS
            Workout(
                id = "4",
                name = "Bicep Curls",
                category = Constants.CATEGORY_BICEPS,
                difficulty = Constants.DIFFICULTY_BEGINNER,
                duration = 15,
                caloriesBurn = 70,
                instructions = "Stand with dumbbells at sides. Keep elbows close to torso. Curl weights up to shoulders. Lower with control.",
                sets = 3,
                reps = "10-12"
            ),
            Workout(
                id = "5",
                name = "Hammer Curls",
                category = Constants.CATEGORY_BICEPS,
                difficulty = Constants.DIFFICULTY_BEGINNER,
                duration = 15,
                caloriesBurn = 75,
                instructions = "Hold dumbbells with palms facing each other. Curl up keeping palms neutral. Lower slowly.",
                sets = 3,
                reps = "10-12"
            ),
            Workout(
                id = "6",
                name = "Concentration Curls",
                category = Constants.CATEGORY_BICEPS,
                difficulty = Constants.DIFFICULTY_INTERMEDIATE,
                duration = 12,
                caloriesBurn = 65,
                instructions = "Sit with legs spread. Rest elbow on inner thigh. Curl dumbbell up. Focus on bicep contraction.",
                sets = 3,
                reps = "8-10"
            ),
            
            // CHEST
            Workout(
                id = "7",
                name = "Push-ups",
                category = Constants.CATEGORY_CHEST,
                difficulty = Constants.DIFFICULTY_BEGINNER,
                duration = 10,
                caloriesBurn = 55,
                instructions = "Start in plank position. Lower body until chest nearly touches floor. Push back up. Keep body straight.",
                sets = 3,
                reps = "10-15"
            ),
            Workout(
                id = "8",
                name = "Bench Press",
                category = Constants.CATEGORY_CHEST,
                difficulty = Constants.DIFFICULTY_INTERMEDIATE,
                duration = 20,
                caloriesBurn = 100,
                instructions = "Lie on bench with feet flat. Lower bar to chest. Press up, fully extending arms. Control the weight.",
                sets = 3,
                reps = "8-10"
            ),
            Workout(
                id = "9",
                name = "Chest Flyes",
                category = Constants.CATEGORY_CHEST,
                difficulty = Constants.DIFFICULTY_INTERMEDIATE,
                duration = 15,
                caloriesBurn = 80,
                instructions = "Lie on bench with dumbbells above chest. Lower arms out to sides in arc motion. Bring back together.",
                sets = 3,
                reps = "10-12"
            ),
            
            // LEGS
            Workout(
                id = "10",
                name = "Squats",
                category = Constants.CATEGORY_LEGS,
                difficulty = Constants.DIFFICULTY_BEGINNER,
                duration = 15,
                caloriesBurn = 90,
                instructions = "Stand with feet shoulder-width apart. Lower hips back and down. Keep chest up. Drive through heels to stand.",
                sets = 3,
                reps = "12-15"
            ),
            Workout(
                id = "11",
                name = "Lunges",
                category = Constants.CATEGORY_LEGS,
                difficulty = Constants.DIFFICULTY_BEGINNER,
                duration = 12,
                caloriesBurn = 80,
                instructions = "Step forward into lunge. Lower back knee toward ground. Push back to start. Alternate legs.",
                sets = 3,
                reps = "10-12 each leg"
            ),
            Workout(
                id = "12",
                name = "Leg Press",
                category = Constants.CATEGORY_LEGS,
                difficulty = Constants.DIFFICULTY_INTERMEDIATE,
                duration = 20,
                caloriesBurn = 110,
                instructions = "Sit in leg press machine. Place feet on platform. Push platform away. Lower with control.",
                sets = 3,
                reps = "10-12"
            ),
            Workout(
                id = "13",
                name = "Calf Raises",
                category = Constants.CATEGORY_LEGS,
                difficulty = Constants.DIFFICULTY_BEGINNER,
                duration = 10,
                caloriesBurn = 50,
                instructions = "Stand with balls of feet on edge. Rise up on toes. Lower heels below edge. Feel stretch in calves.",
                sets = 3,
                reps = "15-20"
            ),
            
            // SHOULDERS
            Workout(
                id = "14",
                name = "Shoulder Press",
                category = Constants.CATEGORY_SHOULDERS,
                difficulty = Constants.DIFFICULTY_BEGINNER,
                duration = 15,
                caloriesBurn = 85,
                instructions = "Sit or stand with dumbbells at shoulder height. Press weights overhead. Lower with control.",
                sets = 3,
                reps = "10-12"
            ),
            Workout(
                id = "15",
                name = "Lateral Raises",
                category = Constants.CATEGORY_SHOULDERS,
                difficulty = Constants.DIFFICULTY_BEGINNER,
                duration = 12,
                caloriesBurn = 65,
                instructions = "Stand with dumbbells at sides. Raise arms out to sides until shoulder height. Lower slowly.",
                sets = 3,
                reps = "12-15"
            ),
            Workout(
                id = "16",
                name = "Front Raises",
                category = Constants.CATEGORY_SHOULDERS,
                difficulty = Constants.DIFFICULTY_BEGINNER,
                duration = 12,
                caloriesBurn = 60,
                instructions = "Stand with dumbbells in front of thighs. Raise arms forward to shoulder height. Lower with control.",
                sets = 3,
                reps = "12-15"
            ),
            
            // BACK
            Workout(
                id = "17",
                name = "Pull-ups",
                category = Constants.CATEGORY_BACK,
                difficulty = Constants.DIFFICULTY_ADVANCED,
                duration = 10,
                caloriesBurn = 70,
                instructions = "Hang from bar with overhand grip. Pull body up until chin over bar. Lower with control.",
                sets = 3,
                reps = "6-10"
            ),
            Workout(
                id = "18",
                name = "Bent-over Rows",
                category = Constants.CATEGORY_BACK,
                difficulty = Constants.DIFFICULTY_INTERMEDIATE,
                duration = 15,
                caloriesBurn = 85,
                instructions = "Bend at hips with weights hanging down. Pull weights to sides of torso. Squeeze shoulder blades together.",
                sets = 3,
                reps = "10-12"
            ),
            Workout(
                id = "19",
                name = "Deadlifts",
                category = Constants.CATEGORY_BACK,
                difficulty = Constants.DIFFICULTY_ADVANCED,
                duration = 20,
                caloriesBurn = 120,
                instructions = "Stand with barbell over feet. Bend and grip bar. Lift by extending hips and knees. Keep back straight.",
                sets = 3,
                reps = "6-8"
            ),
            
            // TRICEPS
            Workout(
                id = "20",
                name = "Tricep Dips",
                category = Constants.CATEGORY_TRICEPS,
                difficulty = Constants.DIFFICULTY_INTERMEDIATE,
                duration = 10,
                caloriesBurn = 60,
                instructions = "Support yourself on parallel bars or bench. Lower body by bending elbows. Push back up.",
                sets = 3,
                reps = "10-12"
            ),
            Workout(
                id = "21",
                name = "Overhead Tricep Extension",
                category = Constants.CATEGORY_TRICEPS,
                difficulty = Constants.DIFFICULTY_BEGINNER,
                duration = 12,
                caloriesBurn = 55,
                instructions = "Hold weight overhead with both hands. Lower behind head by bending elbows. Extend back up.",
                sets = 3,
                reps = "12-15"
            ),
            Workout(
                id = "22",
                name = "Tricep Kickbacks",
                category = Constants.CATEGORY_TRICEPS,
                difficulty = Constants.DIFFICULTY_BEGINNER,
                duration = 12,
                caloriesBurn = 50,
                instructions = "Bend forward with dumbbell. Keep upper arm stationary. Extend forearm back. Squeeze tricep.",
                sets = 3,
                reps = "12-15"
            ),
            
            // CARDIO
            Workout(
                id = "23",
                name = "Running",
                category = Constants.CATEGORY_CARDIO,
                difficulty = Constants.DIFFICULTY_BEGINNER,
                duration = 30,
                caloriesBurn = 300,
                instructions = "Maintain steady pace. Land midfoot. Keep posture upright. Breathe rhythmically.",
                sets = 1,
                reps = "30 minutes"
            ),
            Workout(
                id = "24",
                name = "Jumping Jacks",
                category = Constants.CATEGORY_CARDIO,
                difficulty = Constants.DIFFICULTY_BEGINNER,
                duration = 10,
                caloriesBurn = 80,
                instructions = "Jump feet apart while raising arms overhead. Return to start. Maintain steady rhythm.",
                sets = 3,
                reps = "30-60 seconds"
            ),
            Workout(
                id = "25",
                name = "Burpees",
                category = Constants.CATEGORY_CARDIO,
                difficulty = Constants.DIFFICULTY_ADVANCED,
                duration = 10,
                caloriesBurn = 100,
                instructions = "Start standing. Drop to push-up. Do push-up. Jump feet to hands. Jump up. That's one rep.",
                sets = 3,
                reps = "10-15"
            ),
            Workout(
                id = "26",
                name = "Mountain Climbers",
                category = Constants.CATEGORY_CARDIO,
                difficulty = Constants.DIFFICULTY_INTERMEDIATE,
                duration = 10,
                caloriesBurn = 90,
                instructions = "Start in plank. Alternate bringing knees to chest rapidly. Keep core engaged.",
                sets = 3,
                reps = "30-45 seconds"
            )
        )
    }
    
    /**
     * Search workouts by name with null safety
     */
    fun searchWorkouts(query: String?): List<Workout> {
        if (query.isNullOrBlank()) {
            return emptyList()
        }
        
        val lowerQuery = query.lowercase()
        return getAllWorkouts().filter { 
            it.name.lowercase().contains(lowerQuery) ||
            it.category.lowercase().contains(lowerQuery)
        }
    }
    
    /**
     * Filter workouts by difficulty with null safety
     */
    fun filterByDifficulty(difficulty: String?): List<Workout> {
        if (difficulty.isNullOrBlank()) {
            return getAllWorkouts()
        }
        
        return getAllWorkouts().filter { it.difficulty == difficulty }
    }
    
    /**
     * Filter workouts by duration range with validation
     */
    fun filterByDuration(minDuration: Int, maxDuration: Int): List<Workout> {
        if (minDuration < 0 || maxDuration < minDuration) {
            return emptyList()
        }
        
        return getAllWorkouts().filter { 
            it.duration >= minDuration && it.duration <= maxDuration 
        }
    }
    
    /**
     * Filter workouts by calorie range with validation
     */
    fun filterByCalories(minCalories: Int, maxCalories: Int): List<Workout> {
        if (minCalories < 0 || maxCalories < minCalories) {
            return emptyList()
        }
        
        return getAllWorkouts().filter { 
            it.caloriesBurn >= minCalories && it.caloriesBurn <= maxCalories 
        }
    }
    
    /**
     * Get workout count by category with null safety
     */
    fun getWorkoutCountByCategory(category: String?): Int {
        if (category.isNullOrBlank()) {
            return 0
        }
        
        return getWorkoutsByCategory(category).size
    }
}
