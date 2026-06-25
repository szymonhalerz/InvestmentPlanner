package pl.edu.investmentplanner.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pl.edu.investmentplanner.InvestmentViewModel
import pl.edu.investmentplanner.data.PaymentEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalDetailsScreen(
    goalId: Long,
    viewModel: InvestmentViewModel,
    onBack: () -> Unit,
    onAddPayment: () -> Unit
) {
    LaunchedEffect(goalId) { viewModel.selectGoal(goalId) }
    val item by viewModel.selectedGoal.collectAsStateWithLifecycle()
    val payments by viewModel.payments.collectAsStateWithLifecycle()
    var confirmGoalDelete by remember { mutableStateOf(false) }
    var paymentToDelete by remember { mutableStateOf<PaymentEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(item?.goal?.name ?: "Szczegóły celu") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Wstecz") } },
                actions = {
                    if (item != null) {
                        TextButton(onClick = { confirmGoalDelete = true }) { Text("Usuń") }
                    }
                }
            )
        }
    ) { padding ->
        val current = item
        if (current == null) {
            Column(modifier = Modifier.padding(padding).padding(24.dp)) {
                Text("Wczytywanie celu...")
            }
        } else {
            val forecast = calculateGoalForecast(current, payments)
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(current.goal.category, color = MaterialTheme.colorScheme.primary)
                            Text(
                                "${formatAmount(current.totalPaid)} z ${formatAmount(current.goal.targetAmount)}",
                                style = MaterialTheme.typography.headlineSmall
                            )
                            LinearProgressIndicator(
                                progress = { current.progress },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text("Realizacja: ${current.progressPercent}%")
                            Text("Horyzont: ${current.goal.horizonMonths} miesięcy")
                            if (current.goal.description.isNotBlank()) {
                                Text(current.goal.description)
                            }
                            Button(onClick = onAddPayment, modifier = Modifier.fillMaxWidth()) {
                                Text("Dodaj wpłatę")
                            }
                        }
                    }
                }
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(9.dp)
                        ) {
                            Text("Plan na ten miesiąc", style = MaterialTheme.typography.titleLarge)
                            Text(
                                "Planowana wpłata: ${formatAmount(current.goal.plannedMonthlyPayment)}"
                            )
                            Text("Wpłacono: ${formatAmount(current.monthlyPaid)}")
                            Text("Pozostało: ${formatAmount(current.monthlyRemaining)}")
                            LinearProgressIndicator(
                                progress = { current.monthlyProgress },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                "${current.monthlyStatus.label} • ${current.monthlyProgressPercent}%",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Prognoza celu", style = MaterialTheme.typography.titleLarge)
                            Text(
                                "Średnia miesięczna wpłata: ${formatAmount(forecast.averageMonthlyPayment)}"
                            )
                            when {
                                current.remainingAmount <= 0 ->
                                    Text("Cel długoterminowy został osiągnięty.")
                                forecast.estimatedMonths == null ->
                                    Text("Dodaj wpłaty, aby aplikacja mogła wyliczyć prognozę.")
                                else ->
                                    Text(
                                        "Przy obecnym tempie osiągniesz cel za około " +
                                            "${forecast.estimatedMonths} miesięcy."
                                    )
                            }
                            if (forecast.monthlyShortfall > 0 && forecast.averageMonthlyPayment > 0) {
                                Text(
                                    "Brakuje Ci średnio ${formatAmount(forecast.monthlyShortfall)} " +
                                        "miesięcznie, żeby realizować założony plan.",
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
                item {
                    Text("Historia wpłat", style = MaterialTheme.typography.titleLarge)
                }
                if (payments.isEmpty()) {
                    item { Text("Brak wpłat dla tego celu.") }
                } else {
                    items(payments, key = { it.id }) { payment ->
                        PaymentRow(payment = payment, onDelete = { paymentToDelete = payment })
                    }
                }
            }
        }
    }

    if (confirmGoalDelete) {
        AlertDialog(
            onDismissRequest = { confirmGoalDelete = false },
            title = { Text("Usunąć cel?") },
            text = { Text("Cel i cała historia jego wpłat zostaną usunięte.") },
            confirmButton = {
                TextButton(onClick = {
                    item?.goal?.let { viewModel.deleteGoal(it, onBack) }
                    confirmGoalDelete = false
                }) { Text("Usuń") }
            },
            dismissButton = {
                TextButton(onClick = { confirmGoalDelete = false }) { Text("Anuluj") }
            }
        )
    }

    paymentToDelete?.let { payment ->
        AlertDialog(
            onDismissRequest = { paymentToDelete = null },
            title = { Text("Usunąć wpłatę?") },
            text = { Text("Postęp celu zostanie przeliczony automatycznie.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deletePayment(payment)
                    paymentToDelete = null
                }) { Text("Usuń") }
            },
            dismissButton = {
                TextButton(onClick = { paymentToDelete = null }) { Text("Anuluj") }
            }
        )
    }
}

@Composable
private fun PaymentRow(payment: PaymentEntity, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(formatAmount(payment.amount), style = MaterialTheme.typography.titleMedium)
                Text(formatDate(payment.date), style = MaterialTheme.typography.bodySmall)
                if (payment.note.isNotBlank()) Text(payment.note)
            }
            TextButton(onClick = onDelete) { Text("Usuń") }
        }
    }
}
