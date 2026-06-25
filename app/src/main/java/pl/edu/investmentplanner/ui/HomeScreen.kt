package pl.edu.investmentplanner.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Button
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pl.edu.investmentplanner.InvestmentViewModel
import pl.edu.investmentplanner.data.GoalWithTotal
import pl.edu.investmentplanner.data.MonthlyGoalStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: InvestmentViewModel,
    onAddGoal: () -> Unit,
    onOpenGoal: (Long) -> Unit,
    onOpenCushion: () -> Unit,
    onOpenPlan: () -> Unit,
    onOpenProfile: () -> Unit,
    onQuickPayment: () -> Unit,
    onNavigate: (String) -> Unit,
    message: String,
    onMessageShown: () -> Unit
) {
    val goals by viewModel.goals.collectAsStateWithLifecycle()
    val cushion by viewModel.financialCushion.collectAsStateWithLifecycle()
    val plan by viewModel.investmentPlan.collectAsStateWithLifecycle()
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val totalPaid = goals.sumOf { it.totalPaid }
    val totalTarget = goals.sumOf { it.goal.targetAmount }
    val totalMonthlyPlan = goals.sumOf { it.goal.plannedMonthlyPayment }
    val totalMonthlyPaid = goals.sumOf { it.monthlyPaid }
    val totalMonthlyRemaining = goals.sumOf { it.monthlyRemaining }
    val highPriorityGoals = goals.filter { it.goal.priority == "Wysoki" }
    val monthlyActionGoals = goals
        .filter { it.monthlyRemaining > 0 }
        .sortedWith(
            compareBy<GoalWithTotal> {
                goalPriorities.indexOf(it.goal.priority).let { index -> if (index < 0) 1 else index }
            }.thenByDescending { it.monthlyRemaining }
        )
    val nextAction = when {
        totalMonthlyRemaining <= 0 -> "Cel miesięczny wykonany w 100%."
        monthlyActionGoals.firstOrNull()?.goal?.priority == "Wysoki" -> {
            val item = monthlyActionGoals.first()
            "Najwyższy priorytet: ${item.goal.name}. Wpłać jeszcze ${formatAmount(item.monthlyRemaining)}."
        }
        monthlyActionGoals.isNotEmpty() -> {
            val item = monthlyActionGoals.first()
            "Wpłać jeszcze ${formatAmount(item.monthlyRemaining)} na cel „${item.goal.name}”."
        }
        else -> "Brakuje ${formatAmount(totalMonthlyRemaining)} do wykonania planu miesiąca."
    }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(message) {
        if (message.isNotBlank()) {
            snackbarHostState.showSnackbar(message)
            onMessageShown()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("InvestmentPlanner", fontWeight = FontWeight.Bold)
                        Text("Twój finansowy dashboard", style = MaterialTheme.typography.bodySmall)
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = onAddGoal) { Text("＋ Nowy cel") }
        },
        bottomBar = { MainBottomBar(AppRoutes.HOME, onNavigate) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(20.dp, 8.dp, 20.dp, 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SummaryCard(
                    goalsCount = goals.size,
                    totalPaid = totalPaid,
                    monthlyPlan = totalMonthlyPlan,
                    monthlyRemaining = totalMonthlyRemaining
                )
            }
            item {
                Text("Ten miesiąc", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            item {
                MonthlyPlanChart(monthlyPlan = totalMonthlyPlan, monthlyPaid = totalMonthlyPaid)
            }
            item {
                MonthlyActionsCard(
                    goals = monthlyActionGoals.take(5),
                    onQuickPayment = onQuickPayment
                )
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text("Najbliższe działanie", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(nextAction)
                    }
                }
            }
            profile?.let { userProfile ->
                item {
                    BudgetCapacityCard(
                        monthlyPlan = totalMonthlyPlan,
                        investableAmount = userProfile.monthlyInvestableAmount
                    )
                }
            }
            item {
                Text("Postęp długoterminowy", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            item {
                FinancialProgressChart(totalPaid = totalPaid, totalTarget = totalTarget)
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Wysoki priorytet", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("${highPriorityGoals.size}", color = MaterialTheme.colorScheme.primary)
                }
            }
            if (highPriorityGoals.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("Brak pilnych celów", style = MaterialTheme.typography.titleMedium)
                            Text("Cele z wysokim priorytetem pojawią się w tym miejscu.")
                        }
                    }
                }
            } else {
                items(highPriorityGoals.take(3), key = { it.goal.id }) { item ->
                    GoalCard(item = item, onClick = { onOpenGoal(item.goal.id) })
                }
            }
            item {
                Text("Narzędzia finansowe", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    FeatureCard(
                        icon = "●",
                        title = "Profil finansowy",
                        subtitle = profile?.let { "Możesz inwestować ${formatAmount(it.monthlyInvestableAmount)} miesięcznie" }
                            ?: "Uzupełnij dochód, wydatki i profil ryzyka",
                        details = profile?.let {
                            listOf(
                                "Dochód: ${formatAmount(it.monthlyIncome)}",
                                "Wydatki: ${formatAmount(it.monthlyExpenses)}"
                            )
                        } ?: emptyList(),
                        color = Color(0xFFE0EEE9),
                        onClick = onOpenProfile
                    )
                    FeatureCard(
                        icon = "🛟",
                        title = "Poduszka finansowa",
                        subtitle = cushion?.let { "${it.progressPercent}% realizacji" }
                            ?: "Oblicz swoją bezpieczną rezerwę",
                        details = cushion?.let {
                            listOf(
                                "Zebrano: ${formatAmount(it.currentAmount)}",
                                "Cel: ${formatAmount(it.targetAmount)}",
                                "Brakuje: ${formatAmount(it.missingAmount)}"
                            )
                        } ?: emptyList(),
                        color = Color(0xFFDDF1E9),
                        onClick = onOpenCushion
                    )
                    FeatureCard(
                        icon = "🧭",
                        title = "Plan inwestycyjny",
                        subtitle = plan?.let { "Profil ${it.riskProfile.lowercase()}" }
                            ?: "Dobierz portfel do swojego profilu",
                        details = plan?.let {
                            listOf(
                                "${formatAmount(it.monthlyAmount)} miesięcznie • ${it.horizonYears} lat",
                                "Łączny plan: ${formatAmount(it.totalContributions)}"
                            )
                        } ?: emptyList(),
                        color = Color(0xFFE5E8F7),
                        onClick = onOpenPlan
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(
    goalsCount: Int,
    totalPaid: Double,
    monthlyPlan: Double,
    monthlyRemaining: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Text("Podsumowanie", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                SummaryValue("Liczba celów", goalsCount.toString(), Modifier.weight(1f))
                SummaryValue("Zebrane środki", formatAmount(totalPaid), Modifier.weight(1f))
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                SummaryValue("Plan na miesiąc", formatAmount(monthlyPlan), Modifier.weight(1f))
                SummaryValue("Brakuje w miesiącu", formatAmount(monthlyRemaining), Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun SummaryValue(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f))
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun MonthlyActionsCard(goals: List<GoalWithTotal>, onQuickPayment: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(11.dp)
        ) {
            Text("Gdzie wpłacić?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (goals.isEmpty()) {
                Text("Wszystkie miesięczne wpłaty zostały wykonane.")
            } else {
                goals.forEach { item ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(item.goal.name, modifier = Modifier.weight(1f))
                        Text(
                            "wpłać ${formatAmount(item.monthlyRemaining)}",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
            Button(onClick = onQuickPayment, modifier = Modifier.fillMaxWidth()) {
                Text("＋ Dodaj wpłatę")
            }
        }
    }
}

@Composable
private fun BudgetCapacityCard(monthlyPlan: Double, investableAmount: Double) {
    val withinBudget = monthlyPlan <= investableAmount
    val difference = kotlin.math.abs(investableAmount - monthlyPlan)
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (withinBudget) {
                MaterialTheme.colorScheme.primaryContainer
            } else MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                if (withinBudget) "Plan mieści się w Twoim budżecie" else "Plan przekracza Twój budżet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                if (withinBudget) {
                    "Po realizacji planu zostanie ${formatAmount(difference)} wolnych środków."
                } else {
                    "Zmniejsz plany celów łącznie o ${formatAmount(difference)} miesięcznie."
                }
            )
        }
    }
}

@Composable
private fun FeatureCard(
    icon: String,
    title: String,
    subtitle: String,
    details: List<String>,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(icon, style = MaterialTheme.typography.headlineMedium)
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium)
                details.forEach { detail ->
                    Text(detail, style = MaterialTheme.typography.bodySmall)
                }
            }
            Text("›", style = MaterialTheme.typography.headlineSmall)
        }
    }
}

@Composable
fun GoalCard(item: GoalWithTotal, onClick: () -> Unit) {
    val category = categoryDetails(item.goal.category)
    val statusColor = when (item.monthlyStatus) {
        MonthlyGoalStatus.ON_TRACK -> Color(0xFF2E7D6B)
        MonthlyGoalStatus.DELAYED -> Color(0xFFC46A24)
        MonthlyGoalStatus.COMPLETED -> Color(0xFF2E7D32)
        MonthlyGoalStatus.EXCEEDED -> Color(0xFF5367B0)
    }
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = if (item.goal.priority == "Wysoki") {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else null,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(category.color.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) { Text(category.icon, style = MaterialTheme.typography.titleLarge) }
                Column(modifier = Modifier.weight(1f)) {
                    Text(item.goal.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(item.goal.category, style = MaterialTheme.typography.bodySmall, color = category.color)
                    Text(
                        "Priorytet: ${item.goal.priority.lowercase()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (item.goal.priority == "Wysoki") {
                            MaterialTheme.colorScheme.primary
                        } else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text("${item.progressPercent}%", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            LinearProgressIndicator(
                progress = { item.progress },
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)),
                color = category.color
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(formatAmount(item.totalPaid), fontWeight = FontWeight.SemiBold)
                Text("cel ${formatAmount(item.goal.targetAmount)}", style = MaterialTheme.typography.bodySmall)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    item.monthlyStatus.label,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(statusColor.copy(alpha = 0.13f))
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    color = statusColor,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "${formatAmount(item.monthlyPaid)} / ${formatAmount(item.goal.plannedMonthlyPayment)}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            LinearProgressIndicator(
                progress = { item.monthlyProgress },
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)),
                color = statusColor
            )
        }
    }
}
