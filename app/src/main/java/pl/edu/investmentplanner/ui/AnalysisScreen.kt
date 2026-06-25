package pl.edu.investmentplanner.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pl.edu.investmentplanner.InvestmentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisScreen(viewModel: InvestmentViewModel, onNavigate: (String) -> Unit) {
    val goals by viewModel.goals.collectAsStateWithLifecycle()
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val totalTarget = goals.sumOf { it.goal.targetAmount }
    val totalPaid = goals.sumOf { it.totalPaid }
    val overallPercent = if (totalTarget > 0) (totalPaid / totalTarget * 100).toInt() else 0
    val monthlyPlan = goals.sumOf { it.goal.plannedMonthlyPayment }
    val monthlyPaid = goals.sumOf { it.monthlyPaid }
    val monthlyPercent = if (monthlyPlan > 0) {
        (monthlyPaid / monthlyPlan * 100).coerceIn(0.0, 100.0).toInt()
    } else 0
    val biggest = goals.maxByOrNull { it.goal.targetAmount }
    val best = goals.maxByOrNull { it.progress }
    val onTrack = monthlyPlan > 0 && monthlyPaid >= monthlyPlan * 0.75

    Scaffold(
        topBar = { TopAppBar(title = { Text("Analiza finansowa", fontWeight = FontWeight.Bold) }) },
        bottomBar = { MainBottomBar(AppRoutes.ANALYSIS, onNavigate) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(20.dp, 8.dp, 20.dp, 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text("Obraz Twoich finansów", style = MaterialTheme.typography.headlineSmall)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            AnalysisValue("Wartość celów", formatAmount(totalTarget), Modifier.weight(1f))
                            AnalysisValue("Zebrano", formatAmount(totalPaid), Modifier.weight(1f))
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            AnalysisValue("Realizacja", "$overallPercent%", Modifier.weight(1f))
                            AnalysisValue("Plan miesiąca", "$monthlyPercent%", Modifier.weight(1f))
                        }
                    }
                }
            }
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (onTrack) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            if (onTrack) "Jesteś na dobrej drodze" else "Miesięczny plan wymaga uwagi",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text("Wpłacono ${formatAmount(monthlyPaid)} z ${formatAmount(monthlyPlan)}.")
                        profile?.let {
                            Text("Deklarowana możliwość inwestowania: ${formatAmount(it.monthlyInvestableAmount)}.")
                        }
                    }
                }
            }
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    InsightCard(
                        "Największy cel",
                        biggest?.goal?.name ?: "Brak danych",
                        biggest?.let { formatAmount(it.goal.targetAmount) } ?: "—",
                        Modifier.weight(1f)
                    )
                    InsightCard(
                        "Najlepsza realizacja",
                        best?.goal?.name ?: "Brak danych",
                        best?.let { "${it.progressPercent}%" } ?: "—",
                        Modifier.weight(1f)
                    )
                }
            }
            item { MonthlyPlanChart(monthlyPlan, monthlyPaid) }
            item { FinancialProgressChart(totalPaid, totalTarget) }
            item { CategoryBreakdownChart(goals) }
        }
    }
}

@Composable
private fun AnalysisValue(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f))
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun InsightCard(title: String, name: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(title, style = MaterialTheme.typography.labelMedium)
            Text(name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(value, color = MaterialTheme.colorScheme.primary)
        }
    }
}
