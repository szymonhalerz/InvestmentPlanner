package pl.edu.investmentplanner.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPaymentScreen(
    goalId: Long,
    viewModel: InvestmentViewModel,
    onBack: () -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    val amount = parsePositiveAmount(amountText)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nowa wpłata") },
                navigationIcon = { androidx.compose.material3.TextButton(onClick = onBack) { Text("Wstecz") } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it },
                label = { Text("Kwota wpłaty") },
                suffix = { Text("zł") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = showError && amount == null,
                supportingText = {
                    if (showError && amount == null) Text("Podaj kwotę większą od zera")
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Notatka (opcjonalnie)") },
                placeholder = { Text("np. wpłata miesięczna") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = {
                    showError = true
                    amount?.let {
                        viewModel.addPayment(
                            goalId = goalId,
                            amount = it,
                            note = note,
                            onSaved = onBack
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Zapisz wpłatę") }
            OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text("Anuluj")
            }
        }
    }
}
