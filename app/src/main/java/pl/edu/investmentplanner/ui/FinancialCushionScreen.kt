package pl.edu.investmentplanner.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pl.edu.investmentplanner.InvestmentViewModel

val livingSituations = listOf("Mieszka z rodzicami", "Wynajmuje", "Ma kredyt", "Ma rodzinę")
val incomeStabilities = listOf("Stabilny", "Średni", "Niestabilny")

fun recommendedCushionMonths(livingSituation: String, incomeStability: String): Int {
    val livingRisk = when (livingSituation) {
        "Mieszka z rodzicami" -> 0
        "Wynajmuje" -> 1
        "Ma kredyt", "Ma rodzinę" -> 2
        else -> 1
    }
    val incomeRisk = when (incomeStability) {
        "Stabilny" -> 0
        "Średni" -> 1
        "Niestabilny" -> 2
        else -> 1
    }
    return when (livingRisk + incomeRisk) {
        0 -> 3
        1, 2 -> 6
        3 -> 9
        else -> 12
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FinancialCushionScreen(
    viewModel: InvestmentViewModel,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val saved by viewModel.financialCushion.collectAsStateWithLifecycle()
    var expensesText by remember { mutableStateOf("") }
    var currentText by remember { mutableStateOf("") }
    var livingSituation by remember { mutableStateOf("") }
    var incomeStability by remember { mutableStateOf("") }
    var showErrors by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    LaunchedEffect(saved?.updatedAt) {
        saved?.let {
            expensesText = it.monthlyExpenses.toString()
            currentText = it.currentAmount.toString()
            livingSituation = it.livingSituation
            incomeStability = it.incomeStability
        }
    }

    val expenses = parsePositiveAmount(expensesText)
    val current = parsePositiveAmount(currentText)
    val months = recommendedCushionMonths(livingSituation, incomeStability)
    val valid = expenses != null && current != null && livingSituation.isNotBlank() && incomeStability.isNotBlank()
    val target = (expenses ?: 0.0) * months
    val missing = (target - (current ?: 0.0)).coerceAtLeast(0.0)
    val progress = if (target > 0) ((current ?: 0.0) / target).coerceIn(0.0, 1.0).toFloat() else 0f
    val percent = if (target > 0) (((current ?: 0.0) / target) * 100).toInt() else 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Poduszka finansowa") },
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
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("🛟 Zabezpiecz swoją przyszłość", style = MaterialTheme.typography.headlineSmall)
                    Text("Oblicz rezerwę dopasowaną do Twojej sytuacji i dochodów.")
                }
            }

            OutlinedTextField(
                value = expensesText,
                onValueChange = { expensesText = it },
                label = { Text("Miesięczne wydatki") },
                suffix = { Text("zł") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = showErrors && expenses == null,
                supportingText = {
                    if (showErrors && expenses == null) Text("Podaj kwotę większą od zera")
                },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = currentText,
                onValueChange = { currentText = it },
                label = { Text("Aktualnie zebrana kwota") },
                suffix = { Text("zł") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = showErrors && current == null,
                supportingText = {
                    if (showErrors && current == null) Text("Podaj kwotę większą od zera")
                },
                modifier = Modifier.fillMaxWidth()
            )

            ChoiceSection("Sytuacja życiowa", livingSituations, livingSituation) { livingSituation = it }
            ChoiceSection("Stabilność dochodu", incomeStabilities, incomeStability) { incomeStability = it }

            if (valid) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Rekomendacja: $months miesięcy", style = MaterialTheme.typography.titleLarge)
                        Text("Kwota poduszki: ${formatAmount(target)}")
                        Text("Zebrano: ${formatAmount(current ?: 0.0)}")
                        Text("Brakuje: ${formatAmount(missing)}")
                        LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
                        Text("Realizacja: $percent%")
                    }
                }
            }

            if (showErrors && !valid) {
                Text("Uzupełnij wszystkie pola poprawnymi wartościami.", color = MaterialTheme.colorScheme.error)
            }
            Button(
                onClick = {
                    showErrors = true
                    if (valid) {
                        isSaving = true
                        viewModel.saveFinancialCushion(
                            expenses!!,
                            current!!,
                            livingSituation,
                            incomeStability,
                            months,
                            onSaved
                        )
                    }
                },
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (isSaving) "Zapisywanie…" else "Zapisz poduszkę") }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChoiceSection(
    title: String,
    options: List<String>,
    selected: String,
    onSelected: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { option ->
                FilterChip(
                    selected = selected == option,
                    onClick = { onSelected(option) },
                    label = { Text(option) }
                )
            }
        }
    }
}
