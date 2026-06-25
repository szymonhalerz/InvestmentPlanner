package pl.edu.investmentplanner.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface InvestmentDao {
    @Query(
        """
        SELECT investment_goals.*,
               COALESCE(SUM(payments.amount), 0.0) AS totalPaid,
               COALESCE(SUM(CASE WHEN payments.date >= :monthStart THEN payments.amount ELSE 0 END), 0.0) AS monthlyPaid
        FROM investment_goals
        LEFT JOIN payments ON investment_goals.id = payments.goalId
        GROUP BY investment_goals.id
        ORDER BY investment_goals.createdAt DESC
        """
    )
    fun observeGoals(monthStart: Long): Flow<List<GoalWithTotal>>

    @Query(
        """
        SELECT investment_goals.*,
               COALESCE(SUM(payments.amount), 0.0) AS totalPaid,
               COALESCE(SUM(CASE WHEN payments.date >= :monthStart THEN payments.amount ELSE 0 END), 0.0) AS monthlyPaid
        FROM investment_goals
        LEFT JOIN payments ON investment_goals.id = payments.goalId
        WHERE investment_goals.id = :goalId
        GROUP BY investment_goals.id
        """
    )
    fun observeGoal(goalId: Long, monthStart: Long): Flow<GoalWithTotal?>

    @Query("SELECT * FROM payments WHERE goalId = :goalId ORDER BY date DESC")
    fun observePayments(goalId: Long): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM financial_cushion WHERE id = 1")
    fun observeFinancialCushion(): Flow<FinancialCushionEntity?>

    @Query("SELECT * FROM investment_plan WHERE id = 1")
    fun observeInvestmentPlan(): Flow<InvestmentPlanEntity?>

    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun observeUserProfile(): Flow<UserProfileEntity?>

    @Insert
    suspend fun insertGoal(goal: InvestmentGoalEntity): Long

    @Transaction
    suspend fun insertGoalWithInitialPayment(
        goal: InvestmentGoalEntity,
        initialAmount: Double,
        initialPaymentDate: Long
    ): Long {
        val goalId = insertGoal(goal)
        if (initialAmount > 0) {
            insertPayment(
                PaymentEntity(
                    goalId = goalId,
                    amount = initialAmount,
                    date = initialPaymentDate,
                    note = "Kwota początkowa"
                )
            )
        }
        return goalId
    }

    @Insert
    suspend fun insertGoals(goals: List<InvestmentGoalEntity>)

    @Insert
    suspend fun insertPayment(payment: PaymentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveFinancialCushion(cushion: FinancialCushionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveInvestmentPlan(plan: InvestmentPlanEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserProfile(profile: UserProfileEntity)

    @Query("DELETE FROM investment_goals WHERE generatedByPlan = 1")
    suspend fun deleteGoalsGeneratedByPlan()

    @Transaction
    suspend fun replaceInvestmentPlan(
        plan: InvestmentPlanEntity,
        generatedGoals: List<InvestmentGoalEntity>
    ) {
        deleteGoalsGeneratedByPlan()
        saveInvestmentPlan(plan)
        insertGoals(generatedGoals)
    }

    @Delete
    suspend fun deleteGoal(goal: InvestmentGoalEntity)

    @Delete
    suspend fun deletePayment(payment: PaymentEntity)
}
