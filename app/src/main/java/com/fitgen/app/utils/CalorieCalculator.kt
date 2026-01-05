package com.fitgen.app.utils

import com.fitgen.app.models.User
import kotlin.math.roundToInt

object CalorieCalculator {
    /**
     * Calculate Basal Metabolic Rate (BMR) using Mifflin-St Jeor equation
     * For men: BMR = 10 × weight(kg) + 6.25 × height(cm) - 5 × age(years) + 5
     * For women: BMR = 10 × weight(kg) + 6.25 × height(cm) - 5 × age(years) - 161
     */
    fun calculateBMR(user: User): Double {
        val weight = user.weight
        val height = user.height.toDouble()
        val age = user.age.toDouble()

        val bmr = (10 * weight) + (6.25 * height) - (5 * age)

        return if (user.gender == Constants.GENDER_MALE) {
            bmr + 5
        } else {
            bmr - 161
        }
    }

    /**
     * Calculate Total Daily Energy Expenditure (TDEE)
     * TDEE = BMR × Activity Level Multiplier
     */
    fun calculateTDEE(user: User): Double {
        val bmr = calculateBMR(user)
        val multiplier = getActivityMultiplier(user.activityLevel)
        return bmr * multiplier
    }

    /**
     * Get activity level multiplier
     */
    private fun getActivityMultiplier(activityLevel: String): Double {
        return when (activityLevel) {
            Constants.ACTIVITY_SEDENTARY -> Constants.MULTIPLIER_SEDENTARY
            Constants.ACTIVITY_LIGHTLY_ACTIVE -> Constants.MULTIPLIER_LIGHTLY_ACTIVE
            Constants.ACTIVITY_MODERATELY_ACTIVE -> Constants.MULTIPLIER_MODERATELY_ACTIVE
            Constants.ACTIVITY_VERY_ACTIVE -> Constants.MULTIPLIER_VERY_ACTIVE
            else -> Constants.MULTIPLIER_SEDENTARY
        }
    }

    /**
     * Calculate daily calorie target based on user goal
     * - Lose weight: TDEE - 500 calories
     * - Maintain: TDEE
     * - Gain muscle: TDEE + 300 calories
     */
    fun calculateDailyCalorieTarget(user: User): Int {
        val tdee = calculateTDEE(user)

        val adjustment = when (user.goal) {
            Constants.GOAL_LOSE_WEIGHT -> Constants.CALORIE_DEFICIT_LOSE_WEIGHT
            Constants.GOAL_GAIN_MUSCLE -> Constants.CALORIE_SURPLUS_GAIN_MUSCLE
            else -> 0 // maintain
        }

        return (tdee + adjustment).roundToInt()
    }

    /**
     * Calculate BMI (Body Mass Index)
     * BMI = weight(kg) / (height(m))^2
     */
    fun calculateBMI(weight: Double, heightCm: Int): Double {
        val heightM = heightCm / 100.0
        return weight / (heightM * heightM)
    }

    /**
     * Get BMI category
     */
    fun getBMICategory(bmi: Double): String {
        return when {
            bmi < 18.5 -> "Underweight"
            bmi < 25.0 -> "Normal"
            bmi < 30.0 -> "Overweight"
            else -> "Obese"
        }
    }

    /**
     * Calculate estimated weight change per week based on calorie deficit/surplus
     * 1 kg of body weight ≈ 7700 calories
     */
    fun calculateWeeklyWeightChange(user: User): Double {
        val tdee = calculateTDEE(user)
        val target = calculateDailyCalorieTarget(user)
        val dailyDifference = target - tdee
        val weeklyDifference = dailyDifference * 7

        // Convert calories to kg (7700 calories ≈ 1 kg)
        return weeklyDifference / 7700.0
    }
}
