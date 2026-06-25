package pl.edu.investmentplanner.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
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
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeParseException
import pl.edu.investmentplanner.InvestmentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickPaymentScreen(
    viewModel: InvestmentViewModel,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val goals by viewModel.goals.collectAsStateWithLifecycle()
    var selectedGoalId by remember { mutableStateOf<Long?>(null) }
    var expanded by remember { mutableStateOf(false) }
    var amountText by remember { mutableStateOf("") }
    var dateText by remember { mutableStateOf(LocalDate.now().toString()) }
    var note by remember { mutableStateOf("") }
    var showErrors by remember { mutableStateOf(false) }

    LaunchedEffect(goals) {
        if (selectedGoalId == null) selectedGoalId = goals.firstOrNull()?.goal?.id
    }

    val selectedGoal = goals.firstOrNull { it.goal.id == selectedGoalId }
    val amount = parsePositiveAmount(amountText)
    val paymentDate = try {
        LocalDate.parse(dateText)
    } catch (_: DateTimeParseException) {
        null
    }
    val valid = selectedGoal != null && amount != null && paymentDate != null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Szybka wpłata") },
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
            Text("Dodaj wpłatę do wybranego celu", style = MaterialTheme.typography.headlineSmall)

            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                OutlinedTextField(
                    value = selectedGoal?.goal?.name ?: "Wybierz cel",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Cel") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    goals.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item.goal.name) },
                            onClick = {
                                selectedGoalId = item.goal.id
                                expanded = false
                            }
                        )
                    }
                }
            }
            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it },
                label = { Text("Kwota") },
                suffix = { Text("zł") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = showErrors && amount == null,
                supportingText = {
                    selectedGoal?.let { Text("Do wpłaty w tym miesiącu: ${formatAmount(it.monthlyRemaining)}") }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = dateText,
                onValueChange = { dateText = it },
                label = { Text("Data wpłaty") },
                placeholder = { Text("RRRR-MM-DD") },
                isError = showErrors && paymentDate == null,
                supportingText = { Text("Format RRRR-MM-DD") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Krótki opis") },
                modifier = Modifier.fillMaxWidth()
            )
            if (showErrors && !valid) {
                Text("Wybierz cel i podaj poprawną kwotę oraz datę.", color = MaterialTheme.colorScheme.error)
            }
            Button(
                onClick = {
                    showErrors = true
                    if (valid) {
                        val timestamp = paymentDate!!
                            .atStartOfDay(ZoneId.systemDefault())
                            .toInstant()
                            .toEpochMilli()
                        viewModel.addPayment(
                            goalId = selectedGoalId!!,
                            amount = amount!!,
                            note = note,
                            date = timestamp,
                            onSaved = onSaved
                        )
                    }
                },
                enabled = goals.isNotEmpty(),
                modifier = Modifier.fillMaxWidth()
            ) { Text("Dodaj wpłatę") }
        }
    }
}
