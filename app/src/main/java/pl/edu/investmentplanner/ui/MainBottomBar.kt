package pl.edu.investmentplanner.ui

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

object AppRoutes {
    const val HOME = "home"
    const val GOALS = "goals"
    const val ANALYSIS = "analysis"
    const val CALCULATOR = "calculator"
    const val PROFILE = "profile"
}

@Composable
fun MainBottomBar(currentRoute: String, onNavigate: (String) -> Unit) {
    val destinations = listOf(
        Triple(AppRoutes.HOME, "⌂", "Dashboard"),
        Triple(AppRoutes.GOALS, "◎", "Cele"),
        Triple(AppRoutes.ANALYSIS, "▥", "Analiza"),
        Triple(AppRoutes.CALCULATOR, "%", "Kalkulator")
    )

    NavigationBar {
        destinations.forEach { (route, icon, label) ->
            NavigationBarItem(
                selected = currentRoute == route,
                onClick = { onNavigate(route) },
                icon = { Text(icon) },
                label = { Text(label) }
            )
        }
    }
}
