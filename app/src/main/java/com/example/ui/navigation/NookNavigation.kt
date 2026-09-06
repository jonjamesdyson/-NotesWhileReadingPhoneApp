package com.example.ui.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.screens.BookDetailsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.viewmodel.NookViewModel

object NookDestinations {
    const val HOME = "home"
    const val SEARCH = "search"
    const val BOOK_DETAILS = "book_details/{bookId}"

    fun bookDetailsRoute(bookId: Long) = "book_details/$bookId"
}

@Composable
fun NookNavHost(
    viewModel: NookViewModel,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = NookDestinations.HOME,
        modifier = modifier,
        enterTransition = { slideInHorizontally(initialOffsetX = { it / 2 }) + fadeIn() },
        exitTransition = { slideOutHorizontally(targetOffsetX = { -it / 2 }) + fadeOut() },
        popEnterTransition = { slideInHorizontally(initialOffsetX = { -it / 2 }) + fadeIn() },
        popExitTransition = { slideOutHorizontally(targetOffsetX = { it / 2 }) + fadeOut() }
    ) {
        composable(NookDestinations.HOME) {
            HomeScreen(
                viewModel = viewModel,
                onNavigateToSearch = { navController.navigate(NookDestinations.SEARCH) },
                onNavigateToBookDetails = { bookId ->
                    navController.navigate(NookDestinations.bookDetailsRoute(bookId))
                }
            )
        }

        composable(NookDestinations.SEARCH) {
            SearchScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = NookDestinations.BOOK_DETAILS,
            arguments = listOf(
                navArgument("bookId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getLong("bookId") ?: 0L
            BookDetailsScreen(
                bookId = bookId,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
