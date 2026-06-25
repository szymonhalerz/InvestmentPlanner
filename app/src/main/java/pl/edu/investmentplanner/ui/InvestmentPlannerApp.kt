package pl.edu.investmentplanner.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import pl.edu.investmentplanner.InvestmentViewModel

private object Routes {
    const val ADD_GOAL = "add-goal"
    const val DETAILS = "details/{goalId}"
    const val ADD_PAYMENT = "add-payment/{goalId}"
    const val FINANCIAL_CUSHION = "financial-cushion"
    const val INVESTMENT_PLAN = "investment-plan"
    const val HOUSING_DOWN_PAYMENT = "housing-down-payment"
    const val QUICK_PAYMENT = "quick-payment"

    fun details(goalId: Long) = "details/$goalId"
    fun addPayment(goalId: Long) = "add-payment/$goalId"
}

@Composable
fun InvestmentPlannerApp(viewModel: InvestmentViewModel = viewModel()) {
    val navController = rememberNavController()
    val navigateTopLevel: (String) -> Unit = { route ->
        navController.navigate(route) {
            popUpTo(AppRoutes.HOME) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    NavHost(navController = navController, startDestination = AppRoutes.HOME) {
        composable(AppRoutes.HOME) {
            val homeEntry = remember(navController) {
                navController.getBackStackEntry(AppRoutes.HOME)
            }
            val message by homeEntry.savedStateHandle
                .getStateFlow("dashboard_message", "")
                .collectAsStateWithLifecycle()
            HomeScreen(
                viewModel = viewModel,
                onAddGoal = { navController.navigate(Routes.ADD_GOAL) },
                onOpenGoal = { navController.navigate(Routes.details(it)) },
                onOpenCushion = { navController.navigate(Routes.FINANCIAL_CUSHION) },
                onOpenPlan = { navController.navigate(Routes.INVESTMENT_PLAN) },
                onOpenProfile = { navController.navigate(AppRoutes.PROFILE) },
                onQuickPayment = { navController.navigate(Routes.QUICK_PAYMENT) },
                onNavigate = navigateTopLevel,
                message = message,
                onMessageShown = { homeEntry.savedStateHandle["dashboard_message"] = "" }
            )
        }
        composable(AppRoutes.GOALS) {
            GoalsScreen(
                viewModel = viewModel,
                onAddGoal = { navController.navigate(Routes.ADD_GOAL) },
                onOpenGoal = { navController.navigate(Routes.details(it)) },
                onNavigate = navigateTopLevel
            )
        }
        composable(AppRoutes.ANALYSIS) {
            AnalysisScreen(viewModel = viewModel, onNavigate = navigateTopLevel)
        }
        composable(AppRoutes.CALCULATOR) {
            CompoundInterestScreen(onNavigate = navigateTopLevel)
        }
        composable(Routes.ADD_GOAL) {
            AddGoalScreen(
                viewModel = viewModel,
                onBack = navController::navigateUp,
                onOpenHousingCalculator = {
                    navController.navigate(Routes.HOUSING_DOWN_PAYMENT)
                }
            )
        }
        composable(Routes.HOUSING_DOWN_PAYMENT) {
            HousingDownPaymentScreen(
                viewModel = viewModel,
                onBack = navController::navigateUp,
                onSaved = { navigateTopLevel(AppRoutes.GOALS) }
            )
        }
        composable(Routes.QUICK_PAYMENT) {
            QuickPaymentScreen(
                viewModel = viewModel,
                onBack = navController::navigateUp,
                onSaved = {
                    navController.getBackStackEntry(AppRoutes.HOME)
                        .savedStateHandle["dashboard_message"] = "Wpłata dodana"
                    navController.popBackStack()
                }
            )
        }
        composable(
            route = Routes.DETAILS,
            arguments = listOf(navArgument("goalId") { type = NavType.LongType })
        ) { entry ->
            val goalId = entry.arguments?.getLong("goalId") ?: return@composable
            GoalDetailsScreen(
                goalId = goalId,
                viewModel = viewModel,
                onBack = navController::navigateUp,
                onAddPayment = { navController.navigate(Routes.addPayment(goalId)) }
            )
        }
        composable(
            route = Routes.ADD_PAYMENT,
            arguments = listOf(navArgument("goalId") { type = NavType.LongType })
        ) { entry ->
            val goalId = entry.arguments?.getLong("goalId") ?: return@composable
            AddPaymentScreen(
                goalId = goalId,
                viewModel = viewModel,
                onBack = navController::navigateUp
            )
        }
        composable(Routes.FINANCIAL_CUSHION) {
            FinancialCushionScreen(
                viewModel = viewModel,
                onBack = navController::navigateUp,
                onSaved = {
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("dashboard_message", "Poduszka finansowa zapisana")
                    navController.popBackStack()
                }
            )
        }
        composable(Routes.INVESTMENT_PLAN) {
            InvestmentPlanScreen(
                viewModel = viewModel,
                onBack = navController::navigateUp,
                onSaved = {
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("dashboard_message", "Plan inwestycyjny zapisany")
                    navController.popBackStack()
                }
            )
        }
        composable(AppRoutes.PROFILE) {
            UserProfileScreen(
                viewModel = viewModel,
                onBack = navController::navigateUp,
                onSaved = {
                    navController.getBackStackEntry(AppRoutes.HOME)
                        .savedStateHandle["dashboard_message"] = "Profil finansowy zapisany"
                    navController.popBackStack()
                }
            )
        }
    }
}
