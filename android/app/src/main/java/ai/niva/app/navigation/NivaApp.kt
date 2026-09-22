package ai.niva.app.navigation

import ai.niva.app.chat.ChatScreen
import ai.niva.app.chat.ChatViewModel
import ai.niva.app.privacy.PrivacyScreen
import ai.niva.app.privacy.PrivacyViewModel
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

@Composable
fun NivaApp() {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route ?: NivaDestination.Chat.route
    Scaffold(
        bottomBar = {
            NavigationBar {
                NivaDestination.entries.forEach { destination ->
                    NavigationBarItem(
                        selected = currentRoute == destination.route,
                        onClick = {
                            navController.navigate(destination.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Text(destination.label.first().toString()) },
                        label = { Text(destination.label) },
                    )
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = NivaDestination.Chat.route,
            modifier = Modifier.padding(padding),
        ) {
            composable(NivaDestination.Chat.route) {
                val viewModel: ChatViewModel = hiltViewModel()
                ChatScreen(viewModel = viewModel)
            }
            composable(NivaDestination.Privacy.route) {
                val viewModel: PrivacyViewModel = hiltViewModel()
                PrivacyScreen(viewModel = viewModel)
            }
        }
    }
}
