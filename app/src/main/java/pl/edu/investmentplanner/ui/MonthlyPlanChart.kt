package pl.edu.investmentplanner.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun MonthlyPlanChart(monthlyPlan: Double, monthlyPaid: Double) {
    val remaining = (monthlyPlan - monthlyPaid).coerceAtLeast(0.0)
    val progress = if (monthlyPlan > 0) {
        (monthlyPaid / monthlyPlan).coerceIn(0.0, 1.0).toFloat()
    } else 0f
    val percent = if (monthlyPlan > 0) {
        ((monthlyPaid / monthlyPlan) * 100).coerceIn(0.0, 100.0).toInt()
    } else 0
    val paidColor = MaterialTheme.colorScheme.primary
    val remainingColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Miesięczny cel", style = MaterialTheme.typography.bodySmall)
                    Text(formatAmount(monthlyPlan), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
                Column {
                    Text("Realizacja", style = MaterialTheme.typography.bodySmall)
                    Text("$percent%", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
            }
            Canvas(modifier = Modifier.fillMaxWidth().height(18.dp)) {
                val radius = CornerRadius(size.height / 2, size.height / 2)
                drawRoundRect(color = remainingColor, cornerRadius = radius)
                if (progress > 0) {
                    drawRoundRect(
                        color = paidColor,
                        size = size.copy(width = size.width * progress),
                        cornerRadius = radius
                    )
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Wpłacono", style = MaterialTheme.typography.bodySmall)
                    Text(
                        formatAmount(monthlyPaid),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End
                ) {
                    Text("Pozostało", style = MaterialTheme.typography.bodySmall)
                    Text(
                        formatAmount(remaining),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
