package com.asr.financial.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.asr.financial.presentation.screens.details.DetailsScreen
import com.asr.financial.presentation.screens.home.HomeScreen

@Composable
fun NavGraph(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = HomeRoute
    ) {
        composable<HomeRoute> {
            HomeScreen(
                onItemClick = { itemId ->
                    navController.navigate(DetailsRoute(itemId = itemId))
                }
            )
        }

        composable<DetailsRoute> { backStackEntry ->
            val route: DetailsRoute = backStackEntry.toRoute()
            DetailsScreen(
                itemId = route.itemId,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
