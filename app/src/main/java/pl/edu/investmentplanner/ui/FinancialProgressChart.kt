package pl.edu.investmentplanner.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun FinancialProgressChart(totalPaid: Double, totalTarget: Double) {
    val progress = if (totalTarget > 0) {
        (totalPaid / totalTarget).coerceIn(0.0, 1.0).toFloat()
    } else 0f
    val percent = if (totalTarget > 0) {
        ((totalPaid / totalTarget) * 100).coerceAtLeast(0.0).toInt()
    } else 0
    val trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.13f)
    val progressColor = MaterialTheme.colorScheme.primary

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(22.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(132.dp), contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.size(116.dp)) {
                    val stroke = Stroke(width = 18.dp.toPx(), cap = StrokeCap.Round)
                    drawArc(
                        color = trackColor,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = stroke
                    )
                    if (progress > 0f) {
                        drawArc(
                            color = progressColor,
                            startAngle = -90f,
                            sweepAngle = 360f * progress,
                            useCenter = false,
                            style = stroke
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$percent%", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("realizacji", style = MaterialTheme.typography.bodySmall)
                }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Łączny postęp", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Zebrano", style = MaterialTheme.typography.bodySmall)
                Text(formatAmount(totalPaid), fontWeight = FontWeight.SemiBold)
                Text("z celu ${formatAmount(totalTarget)}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
