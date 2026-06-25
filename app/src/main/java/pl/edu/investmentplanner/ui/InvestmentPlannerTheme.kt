package pl.edu.investmentplanner.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val PlannerColors = lightColorScheme(
    primary = Color(0xFF285F54),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD9EFE8),
    onPrimaryContainer = Color(0xFF123D35),
    secondary = Color(0xFF5666A3),
    secondaryContainer = Color(0xFFE2E6F8),
    tertiary = Color(0xFF8A5D3B),
    tertiaryContainer = Color(0xFFF5E2D2),
    background = Color(0xFFF5F7F9),
    surface = Color(0xFFFFFFFF),
    surfaceContainer = Color(0xFFEDF1F2)
)

private val PlannerShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(26.dp)
)

@Composable
fun InvestmentPlannerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = PlannerColors,
        shapes = PlannerShapes,
        content = content
    )
}
