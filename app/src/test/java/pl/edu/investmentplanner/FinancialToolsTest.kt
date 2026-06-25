package pl.edu.investmentplanner

import org.junit.Assert.assertEquals
import org.junit.Test
import pl.edu.investmentplanner.data.FinancialCushionEntity
import pl.edu.investmentplanner.data.InvestmentPlanEntity
import pl.edu.investmentplanner.data.generateGoalsForPlan
import pl.edu.investmentplanner.data.UserProfileEntity
import pl.edu.investmentplanner.ui.calculateCompoundInterest
import pl.edu.investmentplanner.ui.calculateHousingGoal
import pl.edu.investmentplanner.ui.recommendedCushionMonths

class FinancialToolsTest {
    @Test
    fun `low risk gives three months of cushion`() {
        assertEquals(3, recommendedCushionMonths("Mieszka z rodzicami", "Stabilny"))
    }

    @Test
    fun `standard risk gives six months of cushion`() {
        assertEquals(6, recommendedCushionMonths("Wynajmuje", "Średni"))
    }

    @Test
    fun `high risk gives twelve months of cushion`() {
        assertEquals(12, recommendedCushionMonths("Ma rodzinę", "Niestabilny"))
    }

    @Test
    fun `cushion amounts and progress are calculated correctly`() {
        val cushion = FinancialCushionEntity(
            monthlyExpenses = 4_000.0,
            currentAmount = 6_000.0,
            livingSituation = "Wynajmuje",
            incomeStability = "Średni",
            recommendedMonths = 6
        )

        assertEquals(24_000.0, cushion.targetAmount, 0.01)
        assertEquals(18_000.0, cushion.missingAmount, 0.01)
        assertEquals(0.25f, cushion.progress)
        assertEquals(25, cushion.progressPercent)
    }

    @Test
    fun `balanced five year plan creates correct financial goals`() {
        val plan = InvestmentPlanEntity(
            monthlyAmount = 3_000.0,
            horizonYears = 5,
            riskProfile = "Zrównoważony"
        )

        val goals = generateGoalsForPlan(plan, createdAt = 1_000L)

        assertEquals(180_000.0, plan.totalContributions, 0.01)
        assertEquals(listOf(90_000.0, 54_000.0, 18_000.0, 18_000.0), goals.map { it.targetAmount })
        assertEquals(listOf("ETF-y", "Obligacje skarbowe", "Akcje", "Metale szlachetne"), goals.map { it.category })
        assertEquals(listOf(1_500.0, 900.0, 300.0, 300.0), goals.map { it.plannedMonthlyPayment })
        assertEquals(listOf(60, 60, 60, 60), goals.map { it.horizonMonths })
        assertEquals(true, goals.all { it.generatedByPlan })
        assertEquals(true, goals.all { it.priority == "Średni" })
    }

    @Test
    fun `compound calculator handles zero return`() {
        val result = calculateCompoundInterest(
            initialAmount = 1_000.0,
            monthlyPayment = 100.0,
            years = 1,
            annualRatePercent = 0.0
        )

        assertEquals(2_200.0, result, 0.01)
    }

    @Test
    fun `compound calculator applies monthly compounding`() {
        val result = calculateCompoundInterest(
            initialAmount = 0.0,
            monthlyPayment = 100.0,
            years = 1,
            annualRatePercent = 12.0
        )

        assertEquals(1_268.25, result, 0.02)
    }

    @Test
    fun `profile calculates monthly surplus`() {
        val profile = UserProfileEntity(
            monthlyIncome = 8_000.0,
            monthlyExpenses = 5_500.0,
            monthlyInvestableAmount = 2_000.0,
            livingSituation = "Wynajmuje",
            riskProfile = "Zrównoważony"
        )

        assertEquals(2_500.0, profile.monthlySurplus, 0.01)
    }

    @Test
    fun `housing calculator includes down payment and extra costs`() {
        val result = calculateHousingGoal(
            propertyPrice = 500_000.0,
            downPaymentPercent = 20,
            additionalCosts = 30_000.0,
            currentAmount = 20_000.0,
            months = 100
        )

        assertEquals(100_000.0, result.downPayment, 0.01)
        assertEquals(130_000.0, result.totalTarget, 0.01)
        assertEquals(110_000.0, result.missingAmount, 0.01)
        assertEquals(1_100.0, result.monthlyPayment, 0.01)
        assertEquals(15, result.progressPercent)
    }
}
