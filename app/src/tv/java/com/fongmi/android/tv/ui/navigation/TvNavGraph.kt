package com.fongmi.android.tv.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.fongmi.android.tv.ui.screens.category.CategoryScreen
import com.fongmi.android.tv.ui.screens.detail.DetailScreen
import com.fongmi.android.tv.ui.screens.favorites.FavoritesScreen
import com.fongmi.android.tv.ui.screens.history.HistoryScreen
import com.fongmi.android.tv.ui.screens.home.HomeScreen
import com.fongmi.android.tv.ui.screens.live.LiveScreen
import com.fongmi.android.tv.ui.screens.player.PlayerScreen
import com.fongmi.android.tv.ui.screens.push.PushScreen
import com.fongmi.android.tv.ui.screens.search.SearchScreen
import com.fongmi.android.tv.ui.screens.settings.CustomSettingsScreen
import com.fongmi.android.tv.ui.screens.settings.DanmuSettingsScreen
import com.fongmi.android.tv.ui.screens.settings.PlayerSettingsScreen
import com.fongmi.android.tv.ui.screens.settings.SettingsScreen
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * TV App Navigation Routes
 */
sealed class TvRoute(val route: String) {
    // Main screens
    data object Home : TvRoute("home")
    data object Search : TvRoute("search")
    data object Settings : TvRoute("settings")
    data object Live : TvRoute("live")
    data object History : TvRoute("history")
    data object Favorites : TvRoute("favorites")

    // Detail screens with arguments
    data object Category : TvRoute("category/{typeId}?typeName={typeName}") {
        fun createRoute(typeId: String, typeName: String = "") =
            "category/${encode(typeId)}?typeName=${encode(typeName)}"
        const val ARG_TYPE_ID = "typeId"
        const val ARG_TYPE_NAME = "typeName"
    }

    data object Detail : TvRoute("detail/{siteKey}/{vodId}") {
        fun createRoute(siteKey: String, vodId: String) = "detail/${encode(siteKey)}/${encode(vodId)}"
        const val ARG_SITE_KEY = "siteKey"
        const val ARG_VOD_ID = "vodId"
    }

    data object Player : TvRoute("player?url={url}&name={name}&episode={episode}") {
        fun createRoute(
            url: String,
            vodName: String,
            episodeName: String
        ) = "player?url=${encode(url)}&name=${encode(vodName)}&episode=${encode(episodeName)}"

        const val ARG_URL = "url"
        const val ARG_NAME = "name"
        const val ARG_EPISODE = "episode"
    }

    // Live player
    data object LivePlayer : TvRoute("live/player?url={url}&name={name}") {
        fun createRoute(url: String, channelName: String) =
            "live/player?url=${encode(url)}&name=${encode(channelName)}"
        const val ARG_URL = "url"
        const val ARG_NAME = "name"
    }

    // Settings sub-screens
    data object SettingsPlayer : TvRoute("settings/player")
    data object SettingsDanmu : TvRoute("settings/danmu")
    data object SettingsCustom : TvRoute("settings/custom")

    // Push screen
    data object Push : TvRoute("push")

    companion object {
        fun encode(value: String): String =
            URLEncoder.encode(value, StandardCharsets.UTF_8.toString())

        fun decode(value: String): String =
            URLDecoder.decode(value, StandardCharsets.UTF_8.toString())
    }
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
            SearchScreen(
                onVodClick = { siteKey, vodId ->
                    navController.navigate(TvRoute.Detail.createRoute(siteKey, vodId))
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(TvRoute.Settings.route) {
            SettingsScreen(
                onBackClick = { navController.popBackStack() },
                onPlayerSettingsClick = { navController.navigate(TvRoute.SettingsPlayer.route) },
                onDanmuSettingsClick = { navController.navigate(TvRoute.SettingsDanmu.route) },
                onCustomSettingsClick = { navController.navigate(TvRoute.SettingsCustom.route) }
            )
        }

        composable(TvRoute.Live.route) {
            LiveScreen(
                onChannelClick = { url ->
                    navController.navigate(TvRoute.LivePlayer.createRoute(url, "直播"))
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(TvRoute.History.route) {
            HistoryScreen(
                onItemClick = { siteKey, vodId ->
                    navController.navigate(TvRoute.Detail.createRoute(siteKey, vodId))
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(TvRoute.Favorites.route) {
            FavoritesScreen(
                onItemClick = { siteKey, vodId ->
                    navController.navigate(TvRoute.Detail.createRoute(siteKey, vodId))
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        // ========== Detail Screens ==========

        composable(
            route = TvRoute.Category.route,
            arguments = listOf(
                navArgument(TvRoute.Category.ARG_TYPE_ID) { type = NavType.StringType },
                navArgument(TvRoute.Category.ARG_TYPE_NAME) {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val typeId = TvRoute.decode(backStackEntry.arguments?.getString(TvRoute.Category.ARG_TYPE_ID) ?: "")
            val typeName = TvRoute.decode(backStackEntry.arguments?.getString(TvRoute.Category.ARG_TYPE_NAME) ?: "")

            CategoryScreen(
                typeId = typeId,
                typeName = typeName,
                onVodClick = { siteKey, vodId ->
                    navController.navigate(TvRoute.Detail.createRoute(siteKey, vodId))
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = TvRoute.Detail.route,
            arguments = listOf(
                navArgument(TvRoute.Detail.ARG_SITE_KEY) { type = NavType.StringType },
                navArgument(TvRoute.Detail.ARG_VOD_ID) { type = NavType.StringType }
            )
        ) {
            DetailScreen(
                onPlayClick = { url, headers, vodName, episodeName ->
                    navController.navigate(TvRoute.Player.createRoute(url, vodName, episodeName))
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = TvRoute.Player.route,
            arguments = listOf(
                navArgument(TvRoute.Player.ARG_URL) {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument(TvRoute.Player.ARG_NAME) {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument(TvRoute.Player.ARG_EPISODE) {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) {
            // PlayerScreen now uses PlayerViewModel which gets data from PlayerStateHolder
            PlayerScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        // ========== Live Player ==========

        composable(
            route = TvRoute.LivePlayer.route,
            arguments = listOf(
                navArgument(TvRoute.LivePlayer.ARG_URL) {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument(TvRoute.LivePlayer.ARG_NAME) {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) {
            // PlayerScreen uses PlayerViewModel which gets data from PlayerStateHolder
            // LiveScreen sets the PlayerStateHolder before navigating here
            PlayerScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        // ========== Settings Sub-screens ==========

        composable(TvRoute.SettingsPlayer.route) {
            PlayerSettingsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(TvRoute.SettingsDanmu.route) {
            DanmuSettingsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(TvRoute.SettingsCustom.route) {
            CustomSettingsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        // ========== Push Screen ==========

        composable(TvRoute.Push.route) {
            PushScreen(
                onBackClick = { navController.popBackStack() },
                onPlayUrl = { url ->
                    navController.navigate(TvRoute.Player.createRoute(url, "", ""))
                }
            )
        }
    }
}
