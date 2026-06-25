package pl.edu.investmentplanner.data

import androidx.room.Entity
import androidx.room.ColumnInfo
import androidx.room.PrimaryKey

@Entity(tableName = "investment_goals")
data class InvestmentGoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String,
    val targetAmount: Double,
    val description: String,
    @ColumnInfo(defaultValue = "12")
    val horizonMonths: Int = 12,
    @ColumnInfo(defaultValue = "0.0")
    val plannedMonthlyPayment: Double = 0.0,
    @ColumnInfo(defaultValue = "'Średni'")
    val priority: String = "Średni",
    @ColumnInfo(defaultValue = "'Inwestycyjny'")
    val goalType: String = "Inwestycyjny",
    val createdAt: Long = System.currentTimeMillis(),
    val generatedByPlan: Boolean = false
)
