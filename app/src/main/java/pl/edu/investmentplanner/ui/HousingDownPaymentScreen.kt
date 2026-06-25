package pl.edu.investmentplanner.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.time.YearMonth
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit
import pl.edu.investmentplanner.InvestmentViewModel

data class HousingGoalCalculation(
    val downPayment: Double,
    val totalTarget: Double,
    val missingAmount: Double,
    val monthlyPayment: Double,
    val progressPercent: Int
)

fun calculateHousingGoal(
    propertyPrice: Double,
    downPaymentPercent: Int,
    additionalCosts: Double,
    currentAmount: Double,
    months: Int
): HousingGoalCalculation {
    val downPayment = propertyPrice * downPaymentPercent / 100.0
    val target = downPayment + additionalCosts
    val missing = (target - currentAmount).coerceAtLeast(0.0)
    return HousingGoalCalculation(
        downPayment = downPayment,
        totalTarget = target,
        missingAmount = missing,
        monthlyPayment = if (months > 0) missing / months else 0.0,
        progressPercent = if (target > 0) {
            (currentAmount / target * 100).coerceIn(0.0, 100.0).toInt()
        } else 0
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HousingDownPaymentScreen(
    viewModel: InvestmentViewModel,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    var priceText by remember { mutableStateOf("") }
    var percent by remember { mutableStateOf(20) }
    var costsText by remember { mutableStateOf("0") }
    var currentText by remember { mutableStateOf("0") }
    var targetMonthText by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("Wysoki") }
    var showErrors by remember { mutableStateOf(false) }

    val price = parsePositiveAmount(priceText)
    val costs = parseNonNegativeAmount(costsText)
    val current = parseNonNegativeAmount(currentText)
    val targetMonth = try {
        YearMonth.parse(targetMonthText)
    } catch (_: DateTimeParseException) {
        null
    }
    val months = targetMonth?.let {
        ChronoUnit.MONTHS.between(YearMonth.now(), it).toInt().takeIf { value -> value > 0 }
    }
    val preliminaryTarget = (price ?: 0.0) * percent / 100.0 + (costs ?: 0.0)
    val valid = price != null && costs != null && current != null &&
        current <= preliminaryTarget && months != null
    val calculation = if (valid) {
        calculateHousingGoal(price!!, percent, costs!!, current!!, months!!)
    } else null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Wkład własny") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Wstecz") } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text("🔑 Zaplanuj wkład własny", style = MaterialTheme.typography.headlineSmall)
                    Text("Połącz wkład, koszty dodatkowe i termin zakupu w jeden mierzalny cel.")
                }
            }
            HousingMoneyField("Cena mieszkania", priceText, { priceText = it }, showErrors && price == null)

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Procent wkładu własnego", style = MaterialTheme.typography.titleMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(10, 20, 30).forEach { option ->
                        FilterChip(
                            selected = percent == option,
                            onClick = { percent = option },
                            label = { Text("$option%") }
                        )
                    }
                }
            }
            HousingMoneyField("Dodatkowe koszty", costsText, { costsText = it }, showErrors && costs == null)
            HousingMoneyField("Aktualnie zebrano", currentText, { currentText = it }, showErrors && current == null)
            OutlinedTextField(
                value = targetMonthText,
                onValueChange = { targetMonthText = it },
                label = { Text("Planowany miesiąc zakupu") },
                placeholder = { Text("np. 2029-06") },
                isError = showErrors && months == null,
                supportingText = { Text("Format RRRR-MM; data musi być w przyszłości") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Priorytet", style = MaterialTheme.typography.titleMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    goalPriorities.forEach { option ->
                        FilterChip(
                            selected = priority == option,
                            onClick = { priority = option },
                            label = { Text(option) }
                        )
                    }
                }
            }

            calculation?.let { result ->
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Wyliczenie celu", style = MaterialTheme.typography.titleLarge)
                        Text("Wymagany wkład: ${formatAmount(result.downPayment)}")
                        Text("Cel z kosztami: ${formatAmount(result.totalTarget)}")
                        Text("Brakuje: ${formatAmount(result.missingAmount)}")
                        Text("Miesięcznie: ${formatAmount(result.monthlyPayment)}")
                        LinearProgressIndicator(
                            progress = { result.progressPercent / 100f },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text("Realizacja: ${result.progressPercent}%")
                    }
                }
            }
            if (showErrors && !valid) {
                Text("Uzupełnij poprawnie wszystkie dane.", color = MaterialTheme.colorScheme.error)
            }
            Button(
                onClick = {
                    showErrors = true
                    calculation?.let { result ->
                        viewModel.addGoal(
                            name = "Wkład własny na mieszkanie",
                            category = "Wkład własny na mieszkanie",
                            targetAmount = result.totalTarget,
                            description = "Cena mieszkania: ${formatAmount(price!!)}, wkład: $percent%, koszty dodatkowe: ${formatAmount(costs!!)}.",
                            horizonMonths = months!!,
                            initialAmount = current!!,
                            priority = priority,
                            goalType = GOAL_TYPE_CUSTOM,
                            onSaved = onSaved
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Utwórz cel wkładu własnego") }
        }
    }
}

@Composable
private fun HousingMoneyField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    isError: Boolean
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        suffix = { Text("zł") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        isError = isError,
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
}
