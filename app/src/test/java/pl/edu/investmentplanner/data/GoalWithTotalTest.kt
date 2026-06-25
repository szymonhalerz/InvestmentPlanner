package pl.edu.investmentplanner.data

import org.junit.Assert.assertEquals
import org.junit.Test
import pl.edu.investmentplanner.ui.calculateGoalForecast
import java.time.YearMonth
import java.time.ZoneId

class GoalWithTotalTest {
    @Test
    fun `progress is calculated from payments and target`() {
        val item = GoalWithTotal(
            goal = InvestmentGoalEntity(
                name = "ETF",
                category = "Inwestycje",
                targetAmount = 10_000.0,
                description = ""
            ),
            totalPaid = 2_500.0
        )

        assertEquals(0.25f, item.progress)
        assertEquals(25, item.progressPercent)
    }

    @Test
    fun `progress bar is capped but percentage may exceed target`() {
        val item = GoalWithTotal(
            goal = InvestmentGoalEntity(
                name = "Złoto",
                category = "Inwestycje",
                targetAmount = 1_000.0,
                description = ""
            ),
            totalPaid = 1_200.0
        )

        assertEquals(1f, item.progress)
        assertEquals(120, item.progressPercent)
    }

    @Test
    fun `monthly plan calculates remaining amount and delayed status`() {
        val item = GoalWithTotal(
            goal = InvestmentGoalEntity(
                name = "Mieszkanie",
                category = "Zakup mieszkania",
                targetAmount = 120_000.0,
                description = "",
                horizonMonths = 100,
                plannedMonthlyPayment = 1_000.0
            ),
            totalPaid = 20_000.0,
            monthlyPaid = 470.0
        )

        assertEquals(530.0, item.monthlyRemaining, 0.01)
        assertEquals(47, item.monthlyProgressPercent)
        assertEquals(MonthlyGoalStatus.DELAYED, item.monthlyStatus)
    }

    @Test
    fun `monthly statuses distinguish completed and exceeded plans`() {
        val goal = InvestmentGoalEntity(
            name = "ETF",
            category = "ETF-y",
            targetAmount = 50_000.0,
            description = "",
            plannedMonthlyPayment = 1_000.0
        )

        assertEquals(
            MonthlyGoalStatus.COMPLETED,
            GoalWithTotal(goal, totalPaid = 5_000.0, monthlyPaid = 1_000.0).monthlyStatus
        )
        assertEquals(
            MonthlyGoalStatus.EXCEEDED,
            GoalWithTotal(goal, totalPaid = 5_200.0, monthlyPaid = 1_200.0).monthlyStatus
        )
    }

    @Test
    fun `forecast uses average over elapsed calendar months`() {
        val zone = ZoneId.systemDefault()
        val january = YearMonth.of(2026, 1).atDay(10).atStartOfDay(zone).toInstant().toEpochMilli()
        val march = YearMonth.of(2026, 3).atDay(10).atStartOfDay(zone).toInstant().toEpochMilli()
        val goal = GoalWithTotal(
            goal = InvestmentGoalEntity(
                name = "ETF",
                category = "ETF-y",
                targetAmount = 12_000.0,
                description = "",
                plannedMonthlyPayment = 500.0
            ),
            totalPaid = 900.0
        )
        val payments = listOf(
            PaymentEntity(goalId = 1, amount = 300.0, date = january, note = ""),
            PaymentEntity(goalId = 1, amount = 600.0, date = march, note = "")
        )

        val forecast = calculateGoalForecast(goal, payments, now = march)

        assertEquals(300.0, forecast.averageMonthlyPayment, 0.01)
        assertEquals(37, forecast.estimatedMonths)
        assertEquals(200.0, forecast.monthlyShortfall, 0.01)
    }
}
