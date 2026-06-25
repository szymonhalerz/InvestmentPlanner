package pl.edu.investmentplanner.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "financial_cushion")
data class FinancialCushionEntity(
    @PrimaryKey val id: Int = 1,
    val monthlyExpenses: Double,
    val currentAmount: Double,
    val livingSituation: String,
    val incomeStability: String,
    val recommendedMonths: Int,
    val updatedAt: Long = System.currentTimeMillis()
) {
    val targetAmount: Double
        get() = monthlyExpenses * recommendedMonths

    val missingAmount: Double
        get() = (targetAmount - currentAmount).coerceAtLeast(0.0)

    val progress: Float
        get() = if (targetAmount > 0) {
            (currentAmount / targetAmount).coerceIn(0.0, 1.0).toFloat()
        } else 0f

    val progressPercent: Int
        get() = if (targetAmount > 0) {
            ((currentAmount / targetAmount) * 100).coerceAtLeast(0.0).toInt()
        } else 0
}
