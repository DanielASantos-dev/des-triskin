package br.com.triskin.presentation.navigation

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import br.com.triskin.R
import br.com.triskin.presentation.screen.activity.ActivityScreen
import br.com.triskin.presentation.screen.activity.ActivityViewModel
import br.com.triskin.presentation.screen.task.TaskListScreen
import br.com.triskin.presentation.screen.task.TaskListViewModel
import br.com.triskin.presentation.screen.weather.WeatherScreen
import br.com.triskin.presentation.screen.weather.WeatherViewModel

sealed class Screen(val route: String) {
    data object Tasks : Screen("tasks")
    data object Activities : Screen("activities")
    data object Weather : Screen("weather")
}

private data class NavItem(
    val screen: Screen,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

private val navItems = listOf(
    NavItem(Screen.Tasks, R.string.nav_tasks, Icons.Filled.Assignment, Icons.Outlined.Assignment),
    NavItem(Screen.Activities, R.string.nav_activities, Icons.Filled.Edit, Icons.Outlined.Edit),
    NavItem(Screen.Weather, R.string.nav_weather, Icons.Filled.Cloud, Icons.Outlined.Cloud),
)

@Composable
fun TriskinNavGraph(windowSizeClass: WindowSizeClass) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val expanded = windowSizeClass.widthSizeClass >= WindowWidthSizeClass.Medium

    if (expanded) {
        Row(Modifier.fillMaxSize()) {
            SideNavigation(currentDestination, navController)
            Host(navController, Modifier.fillMaxSize())
        }
    } else {
        Scaffold(
            bottomBar = { BottomNavigation(currentDestination, navController) },
        ) { padding ->
            Host(navController, Modifier.fillMaxSize().padding(padding))
        }
    }
}

@Composable
private fun BottomNavigation(
    currentDestination: NavDestination?,
    navController: NavHostController,
) {
    NavigationBar {
        navItems.forEach { item ->
            val selected = currentDestination?.matches(item.screen) == true
            NavigationBarItem(
                icon = {
                    Icon(
                        if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = stringResource(item.labelRes),
                    )
                },
                label = { Text(stringResource(item.labelRes)) },
                selected = selected,
                onClick = { navController.navigateToTab(item.screen) },
            )
        }
    }
}

@Composable
private fun SideNavigation(
    currentDestination: NavDestination?,
    navController: NavHostController,
) {
    NavigationRail {
        navItems.forEach { item ->
            val selected = currentDestination?.matches(item.screen) == true
            NavigationRailItem(
                icon = {
                    Icon(
                        if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = stringResource(item.labelRes),
                    )
                },
                label = { Text(stringResource(item.labelRes)) },
                selected = selected,
                onClick = { navController.navigateToTab(item.screen) },
            )
        }
    }
}

@Composable
private fun Host(navController: NavHostController, modifier: Modifier) {
    NavHost(
        navController = navController,
        startDestination = Screen.Tasks.route,
        modifier = modifier,
    ) {
        composable(Screen.Tasks.route) {
            val viewModel: TaskListViewModel = hiltViewModel()
            TaskListScreen(viewModel)
        }
        composable(Screen.Activities.route) {
            val viewModel: ActivityViewModel = hiltViewModel()
            ActivityScreen(viewModel)
        }
        composable(Screen.Weather.route) {
            val viewModel: WeatherViewModel = hiltViewModel()
            WeatherScreen(viewModel)
        }
    }
}

private fun NavDestination.matches(screen: Screen): Boolean =
    hierarchy.any { it.route == screen.route }

private fun NavHostController.navigateToTab(screen: Screen) {
    navigate(screen.route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
