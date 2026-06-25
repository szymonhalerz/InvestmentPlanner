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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.MaterialTheme
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
import pl.edu.investmentplanner.InvestmentViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddGoalScreen(
    viewModel: InvestmentViewModel,
    onBack: () -> Unit,
    onOpenHousingCalculator: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var goalType by remember { mutableStateOf(GOAL_TYPE_INVESTMENT) }
    var amountText by remember { mutableStateOf("") }
    var initialAmountText by remember { mutableStateOf("0") }
    var horizonText by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("Średni") }
    var description by remember { mutableStateOf("") }
    var showErrors by remember { mutableStateOf(false) }
    val amount = parsePositiveAmount(amountText)
    val initialAmount = parseNonNegativeAmount(initialAmountText)
    val horizonMonths = horizonText.toIntOrNull()?.takeIf { it in 1..600 }
    val amountsValid = amount != null && initialAmount != null && initialAmount <= amount
    val isValid = name.isNotBlank() && category.isNotBlank() && amountsValid && horizonMonths != null
    val requiredMonthlyPayment = if (amountsValid && horizonMonths != null) {
        ((amount!! - initialAmount!!).coerceAtLeast(0.0) / horizonMonths)
    } else 0.0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nowy cel") },
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
            Text("Co chcesz osiągnąć?", style = androidx.compose.material3.MaterialTheme.typography.headlineSmall)
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nazwa celu") },
                placeholder = { Text("np. ETF na emeryturę") },
                isError = showErrors && name.isBlank(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Typ celu", style = MaterialTheme.typography.titleMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    goalTypes.forEach { option ->
                        FilterChip(
                            selected = goalType == option,
                            onClick = {
                                goalType = option
                                category = if (option == GOAL_TYPE_CUSHION) {
                                    "Poduszka finansowa"
                                } else ""
                            },
                            label = { Text(option) }
                        )
                    }
                }
            }

            if (goalType == GOAL_TYPE_CUSTOM) {
                OutlinedButton(
                    onClick = onOpenHousingCalculator,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("🔑 Kalkulator wkładu własnego na mieszkanie")
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Wybierz kategorię")
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    categoriesForGoalType(goalType).forEach { option ->
                        FilterChip(
                            selected = category == option.name,
                            onClick = { category = option.name },
                            label = { Text("${option.icon} ${option.name}") }
                        )
                    }
                }
                if (showErrors && category.isBlank()) {
                    Text("Wybierz jedną kategorię", color = androidx.compose.material3.MaterialTheme.colorScheme.error)
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Priorytet celu", style = MaterialTheme.typography.titleMedium)
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

            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it },
                label = { Text("Kwota docelowa") },
                suffix = { Text("zł") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = showErrors && amount == null,
                supportingText = {
                    if (showErrors && amount == null) Text("Podaj kwotę większą od zera")
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = initialAmountText,
                onValueChange = { initialAmountText = it },
                label = { Text("Aktualnie zebrana kwota") },
                suffix = { Text("zł") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = showErrors && (initialAmount == null || (amount != null && initialAmount > amount)),
                supportingText = {
                    if (showErrors && initialAmount == null) {
                        Text("Podaj kwotę równą lub większą od zera")
                    } else if (showErrors && amount != null && initialAmount != null && initialAmount > amount) {
                        Text("Zebrana kwota nie może być większa od celu")
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = horizonText,
                onValueChange = { horizonText = it.filter(Char::isDigit) },
                label = { Text("Czas do osiągnięcia celu") },
                suffix = { Text("mies.") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = showErrors && horizonMonths == null,
                supportingText = { Text("Wpisz od 1 do 600 miesięcy") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            if (isValid) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("Twój plan miesięczny", style = MaterialTheme.typography.titleMedium)
                        Text(
                            formatAmount(requiredMonthlyPayment),
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text("Tyle średnio należy wpłacać przez $horizonMonths miesięcy.")
                    }
                }
            }
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Opis (opcjonalnie)") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = {
                    showErrors = true
                    if (isValid) {
                        viewModel.addGoal(
                            name = name,
                            category = category,
                            targetAmount = amount!!,
                            description = description,
                            horizonMonths = horizonMonths!!,
                            initialAmount = initialAmount!!,
                            priority = priority,
                            goalType = goalType,
                            onSaved = onBack
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Zapisz cel") }
            OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text("Anuluj")
            }
        }
    }
}
