package com.example.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import android.app.Activity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.ui.screens.TaskDetailScreen
import com.example.ui.screens.VioraPassScreen
import com.example.ui.screens.ScannerCameraOverlay
import androidx.navigation.compose.NavHost

import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.auth.DeadlineScreen
import com.example.ui.screens.auth.OtpScreen
import com.example.ui.screens.auth.ProfileScreen
import com.example.ui.screens.auth.WelcomeScreen
import com.example.ui.screens.auth.LoginScreen
import com.example.viewmodel.AuthViewModel
import com.example.viewmodel.VioraTaskViewModel
import com.example.ui.screens.NotificationsScreen
import com.example.ui.screens.ListDetailScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.UserProfileScreen
import com.example.ui.screens.EditProfileScreen
import com.example.ui.screens.ArchivesScreen
import com.example.viewmodel.NotificationsViewModel
import com.example.viewmodel.SearchViewModel
import com.example.viewmodel.UserProfileViewModel
import com.example.viewmodel.AgendaViewModel

@Composable
fun AppNavGraph(
    viewModel: VioraTaskViewModel,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = viewModel(),
    notificationsViewModel: NotificationsViewModel = viewModel(),
    searchViewModel: SearchViewModel = viewModel(),
    userProfileViewModel: UserProfileViewModel = viewModel(),
    agendaViewModel: AgendaViewModel = viewModel(), startDestinationOverride: String? = null
) {
    val isVerticalTransition = { route: String? ->
        if (route == null) false else {
            val baseRoute = route.substringBefore("?").substringBefore("/")
            baseRoute == "welcome" || baseRoute == "user_profile" || baseRoute == "edit_profile" || baseRoute == "search" || baseRoute == "agenda"
        }
    }

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    val isWhiteBackground = currentRoute in listOf("welcome", "otp", "profile", "deadline")

    val context = androidx.compose.ui.platform.LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("viora_auth_prefs", android.content.Context.MODE_PRIVATE) }
    val isRegistered = remember { sharedPrefs.getBoolean("is_registered", false) }
    val startDest = startDestinationOverride ?: if (isRegistered) "home" else "welcome"

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = isWhiteBackground
        }
    }

    val navigateToBrief by viewModel.navigateToBriefSignal.collectAsStateWithLifecycle()
    androidx.compose.runtime.LaunchedEffect(navigateToBrief) {
        if (navigateToBrief) {
            navController.navigate("daily_brief") {
                launchSingleTop = true
            }
            viewModel.consumeDailyBrief()
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDest,
        modifier = modifier,
        enterTransition = { 
            val isTargetVertical = isVerticalTransition(targetState.destination.route)
            val EmphasizedDecelerate = androidx.compose.animation.core.CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)
            val durationEnter = 450
            if (isTargetVertical) {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Up, 
                    animationSpec = tween(durationEnter, easing = EmphasizedDecelerate)
                ) + fadeIn(animationSpec = tween(durationEnter))
            } else {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left, 
                    animationSpec = tween(durationEnter, easing = EmphasizedDecelerate)
                ) + fadeIn(animationSpec = tween(durationEnter))
            }
        },
        exitTransition = { 
            val isTargetVertical = isVerticalTransition(targetState.destination.route)
            val EmphasizedDecelerate = androidx.compose.animation.core.CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)
            val EmphasizedAccelerate = androidx.compose.animation.core.CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f)
            val durationExit = 400
            if (isTargetVertical) {
                fadeOut(animationSpec = tween(durationExit)) + 
                scaleOut(targetScale = 0.96f, animationSpec = tween(durationExit, easing = EmphasizedAccelerate))
            } else {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left, 
                    animationSpec = tween(durationExit, easing = EmphasizedDecelerate),
                    targetOffset = { - (it * 0.2f).toInt() }
                ) + fadeOut(animationSpec = tween(durationExit))
            }
        },
        popEnterTransition = { 
            val isInitialVertical = isVerticalTransition(initialState.destination.route)
            val EmphasizedDecelerate = androidx.compose.animation.core.CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)
            val durationEnter = 450
            if (isInitialVertical) {
                fadeIn(animationSpec = tween(durationEnter)) + 
                scaleIn(initialScale = 0.96f, animationSpec = tween(durationEnter, easing = EmphasizedDecelerate))
            } else {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right, 
                    animationSpec = tween(durationEnter, easing = EmphasizedDecelerate),
                    initialOffset = { - (it * 0.2f).toInt() }
                ) + fadeIn(animationSpec = tween(durationEnter))
            }
        },
        popExitTransition = { 
            val isInitialVertical = isVerticalTransition(initialState.destination.route)
            val EmphasizedDecelerate = androidx.compose.animation.core.CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)
            val durationExit = 400
            if (isInitialVertical) {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Down, 
                    animationSpec = tween(durationExit, easing = EmphasizedDecelerate)
                ) + fadeOut(animationSpec = tween(durationExit))
            } else {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right, 
                    animationSpec = tween(durationExit, easing = EmphasizedDecelerate)
                ) + fadeOut(animationSpec = tween(durationExit))
            }
        }
    ) {
        
        composable("welcome") {
            WelcomeScreen(
                viewModel = authViewModel,
                onClose = { (context as? Activity)?.finish() },
                onNavigateToOtp = { navController.navigate("otp") },
                onNavigateToLogin = { navController.navigate("login") }
            )
        }
        
        composable("login") {
            LoginScreen(
                viewModel = authViewModel,
                onBack = { navController.popBackStack() },
                onLoginSuccess = {
                    viewModel.loadState()
                    userProfileViewModel.loadProfile()
                    navController.navigate("home") {
                        popUpTo("welcome") { inclusive = true }
                    }
                }
            )
        }

        
        composable("otp") {
            OtpScreen(
                viewModel = authViewModel,
                onBack = { navController.popBackStack() },
                onNext = { navController.navigate("profile") }
            )
        }
        
        composable("profile") {
            ProfileScreen(
                viewModel = authViewModel,
                onBack = { navController.popBackStack() },
                onNext = { navController.navigate("deadline") }
            )
        }
        
        composable("deadline") {
            DeadlineScreen(
                viewModel = authViewModel,
                onBack = { navController.popBackStack() },
                onNext = {
                    viewModel.loadState()
                    userProfileViewModel.loadProfile()
                    navController.navigate("home") {
                        popUpTo("welcome") { inclusive = true }
                    }
                }
            )
        }

        composable("home") {

            HomeScreen(
                onVioraPassClick = { },
                onNavigateToScanner = { navController.navigate("scanner_camera") },

                viewModel = viewModel,
                agendaViewModel = agendaViewModel,
                modifier = Modifier,
                onNavigateToSearch = { navController.navigate("search") },
                onNavigateToNotifications = { navController.navigate("notifications") },
                onNavigateToProfile = { navController.navigate("user_profile") },
                onNavigateToAgenda = { navController.navigate("agenda") },
                onNavigateToList = { listName, teamName -> 
                    navController.navigate("list_detail/$listName/$teamName")
                }
            )
        }
        
        composable("user_profile") {
            UserProfileScreen(
                taskViewModel = viewModel,
                viewModel = userProfileViewModel,
                onBack = { navController.popBackStack() },
                onNavigateToEditProfile = { focusDeadline -> navController.navigate("edit_profile?focusDeadline=$focusDeadline") },
                onNavigateToSettings = { navController.navigate("settings") },
                onNavigateToScanner = { navController.navigate("scanner_camera") },
                onAvatarChanged = { 
                    userProfileViewModel.loadProfile()
                    viewModel.loadUserInfo() 
                },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate("welcome") {
                        popUpTo(0)
                    }
                }
            )
        }
        
        composable("edit_profile?focusDeadline={focusDeadline}", arguments = listOf(navArgument("focusDeadline") { defaultValue = false })) { backStackEntry ->
            val focusDeadline = backStackEntry.arguments?.getBoolean("focusDeadline") ?: false
            EditProfileScreen(
                focusDeadline = focusDeadline,
                onBack = { navController.popBackStack() },
                onSave = { 
                    userProfileViewModel.loadProfile()
                    viewModel.loadUserInfo()
                    navController.popBackStack() 
                }
            )
        }
        
        composable("notifications") {
            NotificationsScreen(
                viewModel = notificationsViewModel,
                onBack = { navController.popBackStack() }
            )
        }
        
        composable("search") {
            SearchScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "list_detail/{listName}/{teamName}",
            arguments = listOf(
                navArgument("listName") { type = NavType.StringType },
                navArgument("teamName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val listName = backStackEntry.arguments?.getString("listName") ?: ""
            val teamName = backStackEntry.arguments?.getString("teamName") ?: ""
            ListDetailScreen(
                listName = listName,
                teamName = teamName,
                onBack = { navController.popBackStack() },
                onTaskClick = { /* Can handle clicks here */ },
                onStatusClick = { /* Can handle status clicks here */ },
                viewModel = viewModel
            )
        }
        
        composable("agenda") {
            com.example.ui.screens.AgendaScreen(
                viewModel = agendaViewModel,
                onClose = { navController.popBackStack() }
            )
        }
        
        composable("settings") {
            com.example.ui.screens.SettingsScreen(
                onBack = { navController.popBackStack() },
                onNavigateToArchives = { navController.navigate("archives") },
                agendaViewModel = agendaViewModel,
                taskViewModel = viewModel
            )
        }


        composable("viora_pass") {
            VioraPassScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToScanner = { navController.navigate("scanner_camera") }
            )
        }
        composable("scanner_camera") {
            ScannerCameraOverlay(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("daily_brief") {

            com.example.ui.screens.DailyBriefScreen(
                taskViewModel = viewModel,
                agendaViewModel = agendaViewModel,
                onClose = { navController.popBackStack() }
            )
        }
        composable("archives") {
            ArchivesScreen(
                onBack = { navController.popBackStack() },
                viewModel = viewModel
            )
        }
    }
}
