package pl.edu.investmentplanner.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val monthlyIncome: Double,
    val monthlyExpenses: Double,
    val monthlyInvestableAmount: Double,
    val livingSituation: String,
    val riskProfile: String,
    val updatedAt: Long = System.currentTimeMillis()
) {
    val monthlySurplus: Double
        get() = (monthlyIncome - monthlyExpenses).coerceAtLeast(0.0)
}
