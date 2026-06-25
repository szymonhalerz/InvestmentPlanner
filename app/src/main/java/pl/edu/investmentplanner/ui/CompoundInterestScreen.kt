package pl.edu.investmentplanner.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlin.math.pow

fun calculateCompoundInterest(
    initialAmount: Double,
    monthlyPayment: Double,
    years: Int,
    annualRatePercent: Double
): Double {
    val months = years * 12
    val monthlyRate = annualRatePercent / 100.0 / 12.0
    return if (monthlyRate == 0.0) {
        initialAmount + monthlyPayment * months
    } else {
        val growth = (1 + monthlyRate).pow(months)
        initialAmount * growth + monthlyPayment * ((growth - 1) / monthlyRate)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompoundInterestScreen(onNavigate: (String) -> Unit) {
    var initialText by remember { mutableStateOf("") }
    var monthlyText by remember { mutableStateOf("") }
    var yearsText by remember { mutableStateOf("") }
    var rateText by remember { mutableStateOf("") }

    val initial = parseNonNegativeAmount(initialText)
    val monthly = parsePositiveAmount(monthlyText)
    val years = yearsText.toIntOrNull()?.takeIf { it in 1..60 }
    val rate = parseNonNegativeAmount(rateText)?.takeIf { it <= 100 }
    val valid = initial != null && monthly != null && years != null && rate != null
    val result = if (valid) calculateCompoundInterest(initial!!, monthly!!, years!!, rate!!) else 0.0
    val contributions = if (valid) initial!! + monthly!! * years!! * 12 else 0.0

    Scaffold(
        topBar = { TopAppBar(title = { Text("Procent składany", fontWeight = FontWeight.Bold) }) },
        bottomBar = { MainBottomBar(AppRoutes.CALCULATOR, onNavigate) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(20.dp, 8.dp, 20.dp, 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Symulacja przyszłej wartości", style = MaterialTheme.typography.headlineSmall)
                        Text("Sprawdź efekt regularnych wpłat i długiego horyzontu.")
                    }
                }
            }
            item { CalculatorField("Kwota początkowa", initialText, { initialText = it }, "zł") }
            item { CalculatorField("Miesięczna wpłata", monthlyText, { monthlyText = it }, "zł") }
            item { CalculatorField("Liczba lat", yearsText, { yearsText = it.filter(Char::isDigit) }, "lat", KeyboardType.Number) }
            item { CalculatorField("Średnia roczna stopa zwrotu", rateText, { rateText = it }, "%") }
            if (valid) {
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Szacowany wynik", style = MaterialTheme.typography.titleMedium)
                            Text(formatAmount(result), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                            Text("Suma wpłat: ${formatAmount(contributions)}")
                            Text("Szacowany zysk: ${formatAmount((result - contributions).coerceAtLeast(0.0))}")
                        }
                    }
                }
            }
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)) {
                    Text(
                        "To wyłącznie symulacja edukacyjna, a nie porada inwestycyjna. Wynik nie uwzględnia podatków, opłat ani inflacji.",
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CalculatorField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    suffix: String,
    keyboardType: KeyboardType = KeyboardType.Decimal
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        suffix = { Text(suffix) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
}
