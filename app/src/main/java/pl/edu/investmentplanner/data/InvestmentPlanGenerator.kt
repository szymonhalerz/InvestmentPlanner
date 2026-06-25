package pl.edu.investmentplanner.data

data class PlanGoalAllocation(
    val name: String,
    val category: String,
    val percent: Int
)

fun allocationsForProfile(profile: String): List<PlanGoalAllocation> = when (profile) {
    "Ostrożny" -> listOf(
        PlanGoalAllocation("Obligacje i gotówka", "Obligacje skarbowe", 60),
        PlanGoalAllocation("ETF-y", "ETF-y", 30),
        PlanGoalAllocation("Metale szlachetne", "Metale szlachetne", 10)
    )
    "Dynamiczny" -> listOf(
        PlanGoalAllocation("ETF-y i akcje", "ETF-y", 70),
        PlanGoalAllocation("Obligacje", "Obligacje skarbowe", 15),
        PlanGoalAllocation("Metale szlachetne", "Metale szlachetne", 10),
        PlanGoalAllocation("Kryptowaluty", "Kryptowaluty", 5)
    )
    else -> listOf(
        PlanGoalAllocation("ETF-y", "ETF-y", 50),
        PlanGoalAllocation("Obligacje", "Obligacje skarbowe", 30),
        PlanGoalAllocation("Akcje", "Akcje", 10),
        PlanGoalAllocation("Metale szlachetne", "Metale szlachetne", 10)
    )
}

fun generateGoalsForPlan(
    plan: InvestmentPlanEntity,
    createdAt: Long = System.currentTimeMillis()
): List<InvestmentGoalEntity> = allocationsForProfile(plan.riskProfile)
    .mapIndexed { index, allocation ->
        InvestmentGoalEntity(
            name = "${allocation.name} – plan ${plan.riskProfile}",
            category = allocation.category,
            targetAmount = plan.totalContributions * allocation.percent / 100.0,
            description = "Cel utworzony automatycznie z planu inwestycyjnego.",
            horizonMonths = plan.horizonYears * 12,
            plannedMonthlyPayment = plan.monthlyAmount * allocation.percent / 100.0,
            createdAt = createdAt + index,
            generatedByPlan = true
        )
    }
