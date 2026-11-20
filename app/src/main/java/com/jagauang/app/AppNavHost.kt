package com.jagauang.app

import androidx.activity.compose.BackHandler
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.jagauang.app.domain.TransactionType
import com.jagauang.app.ui.history.HistoryScreen
import com.jagauang.app.ui.home.HomeScreen
import com.jagauang.app.ui.more.BooksScreen
import com.jagauang.app.ui.more.CategoriesScreen
import com.jagauang.app.ui.more.MoreScreen
import com.jagauang.app.ui.more.PaymentMethodsScreen
import com.jagauang.app.ui.more.RemindersScreen
import com.jagauang.app.ui.report.ReportScreen
import com.jagauang.app.ui.theme.Canvas
import com.jagauang.app.ui.transaction.TransactionDetailScreen
import com.jagauang.app.ui.transaction.TransactionFormScreen
import java.time.LocalDate

object Routes {
    const val MAIN = "main"
    const val HOME = "home"
    const val HISTORY = "history"
    const val REPORT = "report"
    const val MORE = "more"
    const val FORM = "form/{type}/{day}/{id}"
    const val DETAIL = "detail/{id}"
    const val BOOKS = "books"
    const val METHODS = "methods"
    const val CATEGORIES = "categories"
    const val REMINDERS = "reminders"

    fun form(type: TransactionType, day: Long, id: String?): String = "form/${type.code}/$day/${id ?: "none"}"
    fun detail(id: String): String = "detail/$id"
}

@Composable
fun JagaUangApp(container: AppContainer) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val route = backStackEntry?.destination?.route
    var selectedTab by rememberSaveable { mutableStateOf(Routes.HOME) }
    val tabStateHolder = rememberSaveableStateHolder()
    val showBottomBar = route == Routes.MAIN
    BackHandler(enabled = showBottomBar && selectedTab != Routes.HOME) {
        selectedTab = Routes.HOME
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val successMessage by container.successMessage.collectAsStateWithLifecycle()
    LaunchedEffect(successMessage) {
        successMessage?.let {
            snackbarHostState.showSnackbar(it)
            container.successMessage.value = null
        }
    }

    Scaffold(
        containerColor = Canvas,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(containerColor = Color.White) {
                    NavigationBarItem(
                        selected = selectedTab == Routes.HOME,
                        onClick = { selectedTab = Routes.HOME },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Beranda") },
                        label = { Text("Beranda") },
                    )
                    NavigationBarItem(
                        selected = selectedTab == Routes.HISTORY,
                        onClick = { selectedTab = Routes.HISTORY },
                        icon = { Icon(Icons.Default.History, contentDescription = "Riwayat") },
                        label = { Text("Riwayat") },
                    )
                    NavigationBarItem(
                        selected = selectedTab == Routes.REPORT,
                        onClick = { selectedTab = Routes.REPORT },
                        icon = { Icon(Icons.Default.PieChart, contentDescription = "Laporan") },
                        label = { Text("Laporan") },
                    )
                    NavigationBarItem(
                        selected = selectedTab == Routes.MORE,
                        onClick = { selectedTab = Routes.MORE },
                        icon = { Icon(Icons.Default.MoreHoriz, contentDescription = "Lainnya") },
                        label = { Text("Lainnya") },
                    )
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.MAIN,
            modifier = Modifier.padding(padding),
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
            popEnterTransition = { EnterTransition.None },
            popExitTransition = { ExitTransition.None },
        ) {
            composable(Routes.MAIN) {
                // Tab selection replaces content in one composition, without AnimatedContent.
                // Each tab retains its own rememberSaveable state (scroll/filter/subtab).
                tabStateHolder.SaveableStateProvider(selectedTab) {
                    when (selectedTab) {
                        Routes.HOME -> HomeScreen(
                            container = container,
                            onAddIncome = { navController.navigateOnce(Routes.form(TransactionType.INCOME, LocalDate.now().toEpochDay(), null)) },
                            onAddExpense = { navController.navigateOnce(Routes.form(TransactionType.EXPENSE, LocalDate.now().toEpochDay(), null)) },
                            onTransfer = { navController.navigateOnce(Routes.form(TransactionType.TRANSFER, LocalDate.now().toEpochDay(), null)) },
                            onOpenHistory = { selectedTab = Routes.HISTORY },
                            onOpenDetail = { id -> navController.navigateOnce(Routes.detail(id)) },
                        )
                        Routes.HISTORY -> HistoryScreen(
                            container = container,
                            onOpenDetail = { id -> navController.navigateOnce(Routes.detail(id)) },
                            onAddIncome = { navController.navigateOnce(Routes.form(TransactionType.INCOME, LocalDate.now().toEpochDay(), null)) },
                            onAddExpense = { navController.navigateOnce(Routes.form(TransactionType.EXPENSE, LocalDate.now().toEpochDay(), null)) },
                            onTransfer = { navController.navigateOnce(Routes.form(TransactionType.TRANSFER, LocalDate.now().toEpochDay(), null)) },
                        )
                        Routes.REPORT -> ReportScreen(container = container)
                        Routes.MORE -> MoreScreen(
                            onOpenBooks = { navController.navigateOnce(Routes.BOOKS) },
                            onOpenMethods = { navController.navigateOnce(Routes.METHODS) },
                            onOpenCategories = { navController.navigateOnce(Routes.CATEGORIES) },
                            onOpenReminders = { navController.navigateOnce(Routes.REMINDERS) },
                        )
                    }
                }
            }
            composable(
                route = Routes.FORM,
                arguments = listOf(
                    navArgument("type") { type = NavType.StringType },
                    navArgument("day") { type = NavType.LongType },
                    navArgument("id") { type = NavType.StringType },
                ),
            ) { entry ->
                val type = TransactionType.fromCode(entry.arguments?.getString("type") ?: "") ?: TransactionType.EXPENSE
                val day = entry.arguments?.getLong("day") ?: LocalDate.now().toEpochDay()
                val id = entry.arguments?.getString("id")?.takeIf { it != "none" }
                TransactionFormScreen(
                    container = container,
                    type = type,
                    day = day,
                    id = id,
                    onDone = { navController.popBackStack() },
                    onBack = { navController.popBackStack() },
                )
            }
            composable(
                route = Routes.DETAIL,
                arguments = listOf(navArgument("id") { type = NavType.StringType }),
            ) { entry ->
                val id = entry.arguments?.getString("id") ?: return@composable
                TransactionDetailScreen(
                    container = container,
                    id = id,
                    onEdit = { t -> navController.navigateOnce(Routes.form(t.type, t.day.toEpochDay(), t.id)) },
                    onBack = { navController.popBackStack() },
                )
            }
            composable(Routes.BOOKS) { BooksScreen(container, onBack = { navController.popBackStack() }) }
            composable(Routes.METHODS) { PaymentMethodsScreen(container, onBack = { navController.popBackStack() }) }
            composable(Routes.CATEGORIES) { CategoriesScreen(container, onBack = { navController.popBackStack() }) }
            composable(Routes.REMINDERS) { RemindersScreen(container, onBack = { navController.popBackStack() }) }
        }
    }
}


private fun androidx.navigation.NavHostController.navigateOnce(route: String) {
    navigate(route) { launchSingleTop = true }
}
