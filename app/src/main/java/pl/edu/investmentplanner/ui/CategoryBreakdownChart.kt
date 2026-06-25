package pl.edu.investmentplanner.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pl.edu.investmentplanner.data.GoalWithTotal

@Composable
fun CategoryBreakdownChart(goals: List<GoalWithTotal>) {
    val categories = goals
        .groupBy { it.goal.category }
        .mapValues { (_, items) -> items.sumOf { it.goal.targetAmount } }
        .toList()
        .sortedByDescending { it.second }
    val total = categories.sumOf { it.second }

    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("Podział według kategorii", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            if (categories.isEmpty() || total <= 0) {
                Text("Dodaj cele, aby zobaczyć podział portfela.")
            } else {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(160.dp)) {
                        var startAngle = -90f
                        categories.forEach { (name, value) ->
                            val sweep = (value / total * 360).toFloat()
                            drawArc(
                                color = categoryDetails(name).color,
                                startAngle = startAngle,
                                sweepAngle = sweep,
                                useCenter = false,
                                style = Stroke(width = 28.dp.toPx())
                            )
                            startAngle += sweep
                        }
                    }
                    Text("${categories.size}\nkategorii", fontWeight = FontWeight.Bold)
                }
                categories.take(6).forEach { (name, value) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(categoryDetails(name).color, CircleShape)
                            )
                            Text(name, style = MaterialTheme.typography.bodySmall)
                        }
                        Text("${(value / total * 100).toInt()}%")
                    }
                }
            }
        }
    }
}
