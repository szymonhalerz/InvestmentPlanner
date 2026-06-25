package pl.edu.investmentplanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import pl.edu.investmentplanner.ui.InvestmentPlannerApp
import pl.edu.investmentplanner.ui.InvestmentPlannerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            InvestmentPlannerTheme {
                InvestmentPlannerApp()
            }
        }
    }
}
