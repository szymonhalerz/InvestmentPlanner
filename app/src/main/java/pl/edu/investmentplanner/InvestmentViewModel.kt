package pl.edu.investmentplanner

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import pl.edu.investmentplanner.data.InvestmentGoalEntity
import pl.edu.investmentplanner.data.PaymentEntity

@OptIn(ExperimentalCoroutinesApi::class)
class InvestmentViewModel(application: Application) : AndroidViewModel(application) {
    private val repository =
        (application as InvestmentPlannerApplication).repository

    val goals = repository.goals.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    val financialCushion = repository.financialCushion.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null
    )

    val investmentPlan = repository.investmentPlan.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null
    )

    val userProfile = repository.userProfile.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null
    )

    private val selectedGoalId = MutableStateFlow(0L)

    val selectedGoal = selectedGoalId
        .flatMapLatest(repository::goal)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val payments = selectedGoalId
        .flatMapLatest(repository::payments)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun selectGoal(goalId: Long) {
        selectedGoalId.value = goalId
    }

    fun addGoal(
        name: String,
        category: String,
        targetAmount: Double,
        description: String,
        horizonMonths: Int,
        initialAmount: Double,
        priority: String,
        goalType: String,
        onSaved: () -> Unit
    ) {
        viewModelScope.launch {
            repository.addGoal(
                name,
                category,
                targetAmount,
                description,
                horizonMonths,
                initialAmount,
                priority,
                goalType
            )
            onSaved()
        }
    }

    fun addPayment(
        goalId: Long,
        amount: Double,
        note: String,
        date: Long = System.currentTimeMillis(),
        onSaved: () -> Unit
    ) {
        viewModelScope.launch {
            repository.addPayment(goalId, amount, note, date)
            onSaved()
        }
    }

    fun deleteGoal(goal: InvestmentGoalEntity, onDeleted: () -> Unit) {
        viewModelScope.launch {
            repository.deleteGoal(goal)
            onDeleted()
        }
    }

    fun deletePayment(payment: PaymentEntity) {
        viewModelScope.launch { repository.deletePayment(payment) }
    }

    fun saveFinancialCushion(
        monthlyExpenses: Double,
        currentAmount: Double,
        livingSituation: String,
        incomeStability: String,
        recommendedMonths: Int,
        onSaved: () -> Unit
    ) {
        viewModelScope.launch {
            repository.saveFinancialCushion(
                monthlyExpenses,
                currentAmount,
                livingSituation,
                incomeStability,
                recommendedMonths
            )
            onSaved()
        }
    }

    fun saveInvestmentPlan(
        monthlyAmount: Double,
        horizonYears: Int,
        riskProfile: String,
        onSaved: () -> Unit
    ) {
        viewModelScope.launch {
            repository.saveInvestmentPlan(monthlyAmount, horizonYears, riskProfile)
            onSaved()
        }
    }

    fun saveUserProfile(
        monthlyIncome: Double,
        monthlyExpenses: Double,
        monthlyInvestableAmount: Double,
        livingSituation: String,
        riskProfile: String,
        onSaved: () -> Unit
    ) {
        viewModelScope.launch {
            repository.saveUserProfile(
                monthlyIncome,
                monthlyExpenses,
                monthlyInvestableAmount,
                livingSituation,
                riskProfile
            )
            onSaved()
        }
    }
}
