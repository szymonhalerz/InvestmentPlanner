package pl.edu.investmentplanner.data

import java.time.YearMonth
import java.time.ZoneId

class InvestmentRepository(private val dao: InvestmentDao) {
    private val currentMonthStart = YearMonth.now()
        .atDay(1)
        .atStartOfDay(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()

    val goals = dao.observeGoals(currentMonthStart)
    val financialCushion = dao.observeFinancialCushion()
    val investmentPlan = dao.observeInvestmentPlan()
    val userProfile = dao.observeUserProfile()

    fun goal(goalId: Long) = dao.observeGoal(goalId, currentMonthStart)

    fun payments(goalId: Long) = dao.observePayments(goalId)

    suspend fun addGoal(
        name: String,
        category: String,
        targetAmount: Double,
        description: String,
        horizonMonths: Int,
        initialAmount: Double,
        priority: String,
        goalType: String
    ) = dao.insertGoalWithInitialPayment(
        goal = InvestmentGoalEntity(
            name = name.trim(),
            category = category.trim(),
            targetAmount = targetAmount,
            description = description.trim(),
            horizonMonths = horizonMonths,
            plannedMonthlyPayment =
                ((targetAmount - initialAmount).coerceAtLeast(0.0) / horizonMonths),
            priority = priority,
            goalType = goalType
        ),
        initialAmount = initialAmount,
        initialPaymentDate = currentMonthStart - 1
    )

    suspend fun addPayment(
        goalId: Long,
        amount: Double,
        note: String,
        date: Long = System.currentTimeMillis()
    ) =
        dao.insertPayment(
            PaymentEntity(
                goalId = goalId,
                amount = amount,
                date = date,
                note = note.trim()
            )
        )

    suspend fun deleteGoal(goal: InvestmentGoalEntity) = dao.deleteGoal(goal)

    suspend fun deletePayment(payment: PaymentEntity) = dao.deletePayment(payment)

    suspend fun saveFinancialCushion(
        monthlyExpenses: Double,
        currentAmount: Double,
        livingSituation: String,
        incomeStability: String,
        recommendedMonths: Int
    ) = dao.saveFinancialCushion(
        FinancialCushionEntity(
            monthlyExpenses = monthlyExpenses,
            currentAmount = currentAmount,
            livingSituation = livingSituation,
            incomeStability = incomeStability,
            recommendedMonths = recommendedMonths
        )
    )

    suspend fun saveInvestmentPlan(
        monthlyAmount: Double,
        horizonYears: Int,
        riskProfile: String
    ) {
        val plan = InvestmentPlanEntity(
            monthlyAmount = monthlyAmount,
            horizonYears = horizonYears,
            riskProfile = riskProfile
        )
        dao.replaceInvestmentPlan(plan, generateGoalsForPlan(plan))
    }

    suspend fun saveUserProfile(
        monthlyIncome: Double,
        monthlyExpenses: Double,
        monthlyInvestableAmount: Double,
        livingSituation: String,
        riskProfile: String
    ) = dao.saveUserProfile(
        UserProfileEntity(
            monthlyIncome = monthlyIncome,
            monthlyExpenses = monthlyExpenses,
            monthlyInvestableAmount = monthlyInvestableAmount,
            livingSituation = livingSituation,
            riskProfile = riskProfile
        )
    )
}
