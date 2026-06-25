package pl.edu.investmentplanner.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pl.edu.investmentplanner.InvestmentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(
    viewModel: InvestmentViewModel,
    onAddGoal: () -> Unit,
    onOpenGoal: (Long) -> Unit,
    onNavigate: (String) -> Unit
) {
    val goals by viewModel.goals.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Cele finansowe", fontWeight = FontWeight.Bold) }) },
        bottomBar = { MainBottomBar(AppRoutes.GOALS, onNavigate) },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = onAddGoal) { Text("＋ Nowy cel") }
        }
    ) { padding ->
        if (goals.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Nie masz jeszcze celów", style = MaterialTheme.typography.titleLarge)
                Text("Dodaj pierwszy cel, aby rozpocząć planowanie.")
            }
        } else {
            val sections = listOf(
                GOAL_TYPE_INVESTMENT to "Inwestycje",
                GOAL_TYPE_CUSHION to "Poduszka finansowa",
                GOAL_TYPE_CUSTOM to "Cele zakupowe / dowolne"
            )
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(20.dp, 8.dp, 20.dp, 100.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                sections.forEach { (type, title) ->
                    val sectionGoals = goals
                        .filter { it.goal.goalType == type }
                        .sortedBy {
                            goalPriorities.indexOf(it.goal.priority)
                                .let { index -> if (index < 0) 1 else index }
                        }
                    item(key = "header-$type") {
                        Text(
                            "$title (${sectionGoals.size})",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (sectionGoals.isEmpty()) {
                        item(key = "empty-$type") {
                            Text(
                                "Brak celów w tej sekcji.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        items(sectionGoals, key = { it.goal.id }) { item ->
                            GoalCard(item = item, onClick = { onOpenGoal(item.goal.id) })
                        }
                    }
                }
            }
        }
    }
}
