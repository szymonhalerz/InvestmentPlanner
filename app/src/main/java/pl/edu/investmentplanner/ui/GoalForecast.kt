package pl.edu.investmentplanner.ui

import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import kotlin.math.ceil
import pl.edu.investmentplanner.data.GoalWithTotal
import pl.edu.investmentplanner.data.PaymentEntity

data class GoalForecast(
    val averageMonthlyPayment: Double,
    val estimatedMonths: Int?,
    val monthlyShortfall: Double
)

fun calculateGoalForecast(
    goal: GoalWithTotal,
    payments: List<PaymentEntity>,
    now: Long = System.currentTimeMillis()
): GoalForecast {
    if (payments.isEmpty()) {
        return GoalForecast(0.0, null, goal.goal.plannedMonthlyPayment)
    }

    val zone = ZoneId.systemDefault()
    val firstMonth = YearMonth.from(
        Instant.ofEpochMilli(payments.minOf { it.date }).atZone(zone)
    )
    val currentMonth = YearMonth.from(Instant.ofEpochMilli(now).atZone(zone))
    val monthsWithHistory = (ChronoUnit.MONTHS.between(firstMonth, currentMonth) + 1)
        .coerceAtLeast(1)
    val average = payments.sumOf { it.amount } / monthsWithHistory
    val estimatedMonths = if (goal.remainingAmount <= 0) {
        0
    } else if (average > 0) {
        ceil(goal.remainingAmount / average).toInt()
    } else null

    return GoalForecast(
        averageMonthlyPayment = average,
        estimatedMonths = estimatedMonths,
        monthlyShortfall = (goal.goal.plannedMonthlyPayment - average).coerceAtLeast(0.0)
    )
}
