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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun UserProfileScreen(
    viewModel: InvestmentViewModel,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val saved by viewModel.userProfile.collectAsStateWithLifecycle()
    var incomeText by remember { mutableStateOf("") }
    var expensesText by remember { mutableStateOf("") }
    var investableText by remember { mutableStateOf("") }
    var livingSituation by remember { mutableStateOf("") }
    var riskProfile by remember { mutableStateOf("Zrównoważony") }
    var showErrors by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    LaunchedEffect(saved?.updatedAt) {
        saved?.let {
            incomeText = it.monthlyIncome.toString()
            expensesText = it.monthlyExpenses.toString()
            investableText = it.monthlyInvestableAmount.toString()
            livingSituation = it.livingSituation
            riskProfile = it.riskProfile
        }
    }

    val income = parsePositiveAmount(incomeText)
    val expenses = parseNonNegativeAmount(expensesText)
    val investable = parseNonNegativeAmount(investableText)
    val surplus = ((income ?: 0.0) - (expenses ?: 0.0)).coerceAtLeast(0.0)
    val amountFitsBudget = investable != null && investable <= surplus
    val valid = income != null && expenses != null && expenses <= income &&
        amountFitsBudget && livingSituation.isNotBlank()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profil finansowy") },
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
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Twój punkt wyjścia", style = MaterialTheme.typography.headlineSmall)
                    Text("Profil pomaga ocenić, czy miesięczne plany są realne dla Twojego budżetu.")
                }
            }

            MoneyField("Miesięczny dochód", incomeText, { incomeText = it }, showErrors && income == null)
            MoneyField(
                "Miesięczne wydatki",
                expensesText,
                { expensesText = it },
                showErrors && (expenses == null || (income != null && expenses > income))
            )
            MoneyField(
                "Kwota możliwa do inwestowania",
                investableText,
                { investableText = it },
                showErrors && !amountFitsBudget
            )

            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Miesięczna nadwyżka", style = MaterialTheme.typography.bodySmall)
                    Text(formatAmount(surplus), style = MaterialTheme.typography.titleLarge)
                    if (investable != null && investable > surplus) {
                        Text(
                            "Kwota inwestycji przekracza nadwyżkę.",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            ProfileChoices("Sytuacja życiowa", livingSituations, livingSituation) { livingSituation = it }
            ProfileChoices("Profil ryzyka", riskProfiles, riskProfile) { riskProfile = it }

            if (showErrors && !valid) {
                Text("Popraw dane profilu przed zapisem.", color = MaterialTheme.colorScheme.error)
            }
            Button(
                onClick = {
                    showErrors = true
                    if (valid) {
                        isSaving = true
                        viewModel.saveUserProfile(
                            income!!,
                            expenses!!,
                            investable!!,
                            livingSituation,
                            riskProfile,
                            onSaved
                        )
                    }
                },
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (isSaving) "Zapisywanie…" else "Zapisz profil") }
        }
    }
}

@Composable
private fun MoneyField(
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProfileChoices(
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
