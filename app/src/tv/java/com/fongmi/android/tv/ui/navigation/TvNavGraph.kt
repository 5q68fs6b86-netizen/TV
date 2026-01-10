package com.fongmi.android.tv.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.fongmi.android.tv.ui.screens.home.HomeScreen

/**
 * TV App Navigation Routes
 */
sealed class TvRoute(val route: String) {
    // Main screens
    object Home : TvRoute("home")
    object Search : TvRoute("search")
    object Settings : TvRoute("settings")
    object Live : TvRoute("live")
    object History : TvRoute("history")
    object Favorites : TvRoute("favorites")

    // Detail screens with arguments
    object Category : TvRoute("category/{typeId}") {
        fun createRoute(typeId: String) = "category/$typeId"
        const val ARG_TYPE_ID = "typeId"
    }

    object Detail : TvRoute("detail/{siteKey}/{vodId}") {
        fun createRoute(siteKey: String, vodId: String) = "detail/$siteKey/$vodId"
        const val ARG_SITE_KEY = "siteKey"
        const val ARG_VOD_ID = "vodId"
    }

    object Player : TvRoute("player/{siteKey}/{vodId}/{flag}/{episode}") {
        fun createRoute(
            siteKey: String,
            vodId: String,
            flag: String,
            episode: Int
        ) = "player/$siteKey/$vodId/$flag/$episode"

        const val ARG_SITE_KEY = "siteKey"
        const val ARG_VOD_ID = "vodId"
        const val ARG_FLAG = "flag"
        const val ARG_EPISODE = "episode"
    }

    // Live TV screens
    object LiveChannel : TvRoute("live/channel/{groupIndex}/{channelIndex}") {
        fun createRoute(groupIndex: Int, channelIndex: Int) = "live/channel/$groupIndex/$channelIndex"
        const val ARG_GROUP_INDEX = "groupIndex"
        const val ARG_CHANNEL_INDEX = "channelIndex"
    }

    // Settings sub-screens
    object SettingsPlayer : TvRoute("settings/player")
    object SettingsDanmu : TvRoute("settings/danmu")
    object SettingsCustom : TvRoute("settings/custom")
}

/**
 * TV Navigation Graph
 * Single Activity architecture with Compose Navigation
 */
@Composable
fun TvNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = TvRoute.Home.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // ========== Main Screens ==========

        composable(TvRoute.Home.route) {
            HomeScreen(
                onCategoryClick = { typeId ->
                    navController.navigate(TvRoute.Category.createRoute(typeId))
                },
                onVodClick = { siteKey, vodId ->
                    navController.navigate(TvRoute.Detail.createRoute(siteKey, vodId))
                },
                onSearchClick = {
                    navController.navigate(TvRoute.Search.route)
                },
                onSettingsClick = {
                    navController.navigate(TvRoute.Settings.route)
                },
                onLiveClick = {
                    navController.navigate(TvRoute.Live.route)
                },
                onHistoryClick = {
                    navController.navigate(TvRoute.History.route)
                },
                onFavoritesClick = {
                    navController.navigate(TvRoute.Favorites.route)
                }
            )
        }

        composable(TvRoute.Search.route) {
            // TODO: SearchScreen
            PlaceholderScreen(title = "Search", onBack = { navController.popBackStack() })
        }

        composable(TvRoute.Settings.route) {
            // TODO: SettingsScreen
            PlaceholderScreen(title = "Settings", onBack = { navController.popBackStack() })
        }

        composable(TvRoute.Live.route) {
            // TODO: LiveScreen
            PlaceholderScreen(title = "Live TV", onBack = { navController.popBackStack() })
        }

        composable(TvRoute.History.route) {
            // TODO: HistoryScreen
            PlaceholderScreen(title = "History", onBack = { navController.popBackStack() })
        }

        composable(TvRoute.Favorites.route) {
            // TODO: FavoritesScreen
            PlaceholderScreen(title = "Favorites", onBack = { navController.popBackStack() })
        }

        // ========== Detail Screens ==========

        composable(
            route = TvRoute.Category.route,
            arguments = listOf(
                navArgument(TvRoute.Category.ARG_TYPE_ID) {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val typeId = backStackEntry.arguments?.getString(TvRoute.Category.ARG_TYPE_ID) ?: ""
            // TODO: CategoryScreen
            PlaceholderScreen(
                title = "Category: $typeId",
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = TvRoute.Detail.route,
            arguments = listOf(
                navArgument(TvRoute.Detail.ARG_SITE_KEY) { type = NavType.StringType },
                navArgument(TvRoute.Detail.ARG_VOD_ID) { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val siteKey = backStackEntry.arguments?.getString(TvRoute.Detail.ARG_SITE_KEY) ?: ""
            val vodId = backStackEntry.arguments?.getString(TvRoute.Detail.ARG_VOD_ID) ?: ""
            // TODO: DetailScreen
            PlaceholderScreen(
                title = "Detail: $vodId",
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = TvRoute.Player.route,
            arguments = listOf(
                navArgument(TvRoute.Player.ARG_SITE_KEY) { type = NavType.StringType },
                navArgument(TvRoute.Player.ARG_VOD_ID) { type = NavType.StringType },
                navArgument(TvRoute.Player.ARG_FLAG) { type = NavType.StringType },
                navArgument(TvRoute.Player.ARG_EPISODE) { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val siteKey = backStackEntry.arguments?.getString(TvRoute.Player.ARG_SITE_KEY) ?: ""
            val vodId = backStackEntry.arguments?.getString(TvRoute.Player.ARG_VOD_ID) ?: ""
            val flag = backStackEntry.arguments?.getString(TvRoute.Player.ARG_FLAG) ?: ""
            val episode = backStackEntry.arguments?.getInt(TvRoute.Player.ARG_EPISODE) ?: 0
            // TODO: PlayerScreen
            PlaceholderScreen(
                title = "Player: Episode $episode",
                onBack = { navController.popBackStack() }
            )
        }

        // ========== Live TV Screens ==========

        composable(
            route = TvRoute.LiveChannel.route,
            arguments = listOf(
                navArgument(TvRoute.LiveChannel.ARG_GROUP_INDEX) { type = NavType.IntType },
                navArgument(TvRoute.LiveChannel.ARG_CHANNEL_INDEX) { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val groupIndex = backStackEntry.arguments?.getInt(TvRoute.LiveChannel.ARG_GROUP_INDEX) ?: 0
            val channelIndex = backStackEntry.arguments?.getInt(TvRoute.LiveChannel.ARG_CHANNEL_INDEX) ?: 0
            // TODO: LiveChannelScreen
            PlaceholderScreen(
                title = "Live Channel",
                onBack = { navController.popBackStack() }
            )
        }

        // ========== Settings Sub-screens ==========

        composable(TvRoute.SettingsPlayer.route) {
            // TODO: SettingsPlayerScreen
            PlaceholderScreen(
                title = "Player Settings",
                onBack = { navController.popBackStack() }
            )
        }

        composable(TvRoute.SettingsDanmu.route) {
            // TODO: SettingsDanmuScreen
            PlaceholderScreen(
                title = "Danmu Settings",
                onBack = { navController.popBackStack() }
            )
        }

        composable(TvRoute.SettingsCustom.route) {
            // TODO: SettingsCustomScreen
            PlaceholderScreen(
                title = "Custom Settings",
                onBack = { navController.popBackStack() }
            )
        }
    }
}
