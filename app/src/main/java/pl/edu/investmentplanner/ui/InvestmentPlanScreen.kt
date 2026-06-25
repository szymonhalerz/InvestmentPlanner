package pl.edu.investmentplanner.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pl.edu.investmentplanner.InvestmentViewModel
import pl.edu.investmentplanner.data.allocationsForProfile

data class PortfolioPart(val name: String, val percent: Int, val color: Color)

val riskProfiles = listOf("Ostrożny", "Zrównoważony", "Dynamiczny")

fun portfolioFor(profile: String): List<PortfolioPart> = allocationsForProfile(profile).map {
    val color = when (it.category) {
        "ETF-y" -> Color(0xFF4F7CAC)
        "Akcje" -> Color(0xFF756BB1)
        "Obligacje skarbowe" -> Color(0xFF4E7D6A)
        "Metale szlachetne" -> Color(0xFFC49A2C)
        else -> Color(0xFFE0833D)
    }
    PortfolioPart(it.name, it.percent, color)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun InvestmentPlanScreen(
    viewModel: InvestmentViewModel,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val saved by viewModel.investmentPlan.collectAsStateWithLifecycle()
    var monthlyText by remember { mutableStateOf("") }
    var yearsText by remember { mutableStateOf("") }
    var profile by remember { mutableStateOf("Zrównoważony") }
    var showErrors by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    LaunchedEffect(saved?.updatedAt) {
        saved?.let {
            monthlyText = it.monthlyAmount.toString()
            yearsText = it.horizonYears.toString()
            profile = it.riskProfile
        }
    }

    val monthly = parsePositiveAmount(monthlyText)
    val years = yearsText.toIntOrNull()?.takeIf { it in 1..60 }
    val totalContributions = (monthly ?: 0.0) * (years ?: 0) * 12
    val valid = monthly != null && years != null && totalContributions.isFinite()
    val portfolio = portfolioFor(profile)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Plan inwestycyjny") },
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
                    Text("🧭 Zaplanuj regularne inwestowanie", style = MaterialTheme.typography.headlineSmall)
                    Text("Wybierz profil i zobacz prosty podział miesięcznej inwestycji.")
                }
            }
            OutlinedTextField(
                value = monthlyText,
                onValueChange = { monthlyText = it },
                label = { Text("Miesięczna kwota inwestycji") },
                suffix = { Text("zł") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = showErrors && monthly == null,
                supportingText = {
                    if (showErrors && monthly == null) Text("Podaj kwotę większą od zera")
                },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = yearsText,
                onValueChange = { yearsText = it.filter(Char::isDigit) },
                label = { Text("Horyzont inwestycji") },
                suffix = { Text("lat") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = showErrors && years == null,
                supportingText = { Text("Wpisz od 1 do 60 lat") },
                modifier = Modifier.fillMaxWidth()
            )

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Profil ryzyka", style = MaterialTheme.typography.titleMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    riskProfiles.forEach { option ->
                        FilterChip(
                            selected = profile == option,
                            onClick = { profile = option },
                            label = { Text(option) }
                        )
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text("Proponowany portfel", style = MaterialTheme.typography.titleLarge)
                    portfolio.forEach { part ->
                        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(part.name)
                                Text("${part.percent}%", style = MaterialTheme.typography.titleMedium)
                            }
                            LinearProgressIndicator(
                                progress = { part.percent / 100f },
                                modifier = Modifier.fillMaxWidth(),
                                color = part.color
                            )
                            if (monthly != null) {
                                Text(
                                    "${formatAmount(monthly * part.percent / 100)} miesięcznie",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                    if (valid) {
                        Text(
                            "Łączne wpłaty przez $years lat: ${formatAmount(totalContributions)}",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }

            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)) {
                Text(
                    "ℹ️ To tylko edukacyjna symulacja, a nie porada inwestycyjna. Wyniki nie uwzględniają zysków, inflacji ani podatków.",
                    modifier = Modifier.padding(16.dp)
                )
            }
            if (showErrors && !valid) {
                Text("Podaj poprawną kwotę i horyzont.", color = MaterialTheme.colorScheme.error)
            }
            Button(
                onClick = {
                    showErrors = true
                    if (valid) {
                        isSaving = true
                        viewModel.saveInvestmentPlan(monthly!!, years!!, profile, onSaved)
                    }
                },
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (isSaving) "Zapisywanie…" else "Zapisz plan") }
        }
    }
}
