package pl.edu.investmentplanner.data

import androidx.room.ColumnInfo
import androidx.room.Embedded

data class GoalWithTotal(
    @Embedded val goal: InvestmentGoalEntity,
    @ColumnInfo(name = "totalPaid") val totalPaid: Double,
    @ColumnInfo(name = "monthlyPaid") val monthlyPaid: Double = 0.0
) {
    val remainingAmount: Double
        get() = (goal.targetAmount - totalPaid).coerceAtLeast(0.0)

    val progress: Float
        get() = if (goal.targetAmount > 0) {
            (totalPaid / goal.targetAmount).coerceIn(0.0, 1.0).toFloat()
        } else {
            0f
        }

    val progressPercent: Int
        get() = if (goal.targetAmount > 0) {
            ((totalPaid / goal.targetAmount) * 100).coerceAtLeast(0.0).toInt()
        } else {
            0
        }

    val monthlyRemaining: Double
        get() = (goal.plannedMonthlyPayment - monthlyPaid).coerceAtLeast(0.0)

    val monthlyProgress: Float
        get() = if (goal.plannedMonthlyPayment > 0) {
            (monthlyPaid / goal.plannedMonthlyPayment).coerceIn(0.0, 1.0).toFloat()
        } else 0f

    val monthlyProgressPercent: Int
        get() = if (goal.plannedMonthlyPayment > 0) {
            ((monthlyPaid / goal.plannedMonthlyPayment) * 100).coerceIn(0.0, 100.0).toInt()
        } else 0

    val monthlyStatus: MonthlyGoalStatus
        get() = when {
            totalPaid >= goal.targetAmount -> MonthlyGoalStatus.COMPLETED
            goal.plannedMonthlyPayment <= 0 -> MonthlyGoalStatus.ON_TRACK
            monthlyPaid > goal.plannedMonthlyPayment + 0.01 -> MonthlyGoalStatus.EXCEEDED
            monthlyPaid >= goal.plannedMonthlyPayment - 0.01 -> MonthlyGoalStatus.COMPLETED
            monthlyPaid >= goal.plannedMonthlyPayment * 0.75 -> MonthlyGoalStatus.ON_TRACK
            else -> MonthlyGoalStatus.DELAYED
        }
}

enum class MonthlyGoalStatus(val label: String) {
    ON_TRACK("Na dobrej drodze"),
    DELAYED("Lekko opóźniony"),
    COMPLETED("Cel wykonany"),
    EXCEEDED("Cel przekroczony")
}
