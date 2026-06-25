package pl.edu.investmentplanner.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "investment_plan")
data class InvestmentPlanEntity(
    @PrimaryKey val id: Int = 1,
    val monthlyAmount: Double,
    val horizonYears: Int,
    val riskProfile: String,
    val updatedAt: Long = System.currentTimeMillis()
) {
    val totalContributions: Double
        get() = monthlyAmount * horizonYears * 12
}
