package com.example.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import android.app.Activity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.auth.DeadlineScreen
import com.example.ui.screens.auth.OtpScreen
import com.example.ui.screens.auth.ProfileScreen
import com.example.ui.screens.auth.WelcomeScreen
import com.example.viewmodel.AuthViewModel
import com.example.viewmodel.VioraTaskViewModel
import com.example.ui.screens.NotificationsScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.UserProfileScreen
import com.example.ui.screens.EditProfileScreen
import com.example.viewmodel.NotificationsViewModel
import com.example.viewmodel.SearchViewModel
import com.example.viewmodel.UserProfileViewModel

@Composable
fun AppNavGraph(
    viewModel: VioraTaskViewModel,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = viewModel(),
    notificationsViewModel: NotificationsViewModel = viewModel(),
    searchViewModel: SearchViewModel = viewModel(),
    userProfileViewModel: UserProfileViewModel = viewModel()
) {
    val isVerticalTransition = { route: String? ->
        route == "welcome" || route == "user_profile" || route == "edit_profile" || route == "search" || route == "agenda"
    }

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    val isWhiteBackground = currentRoute in listOf("welcome", "otp", "profile", "deadline")

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = isWhiteBackground
        }
    }

    NavHost(
        navController = navController,
        startDestination = "welcome",
        modifier = modifier,
        enterTransition = { 
            val isTargetVertical = isVerticalTransition(targetState.destination.route)
            if (isTargetVertical) {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up, animationSpec = tween(300, easing = FastOutSlowInEasing))
            } else {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(300, easing = FastOutSlowInEasing)) 
            }
        },
        exitTransition = { 
            val isTargetVertical = isVerticalTransition(targetState.destination.route)
            if (isTargetVertical) {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Up, animationSpec = tween(300, easing = FastOutSlowInEasing))
            } else {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(300, easing = FastOutSlowInEasing)) 
            }
        },
        popEnterTransition = { 
            val isInitialVertical = isVerticalTransition(initialState.destination.route)
            if (isInitialVertical) {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Down, animationSpec = tween(300, easing = FastOutSlowInEasing))
            } else {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(300, easing = FastOutSlowInEasing)) 
            }
        },
        popExitTransition = { 
            val isInitialVertical = isVerticalTransition(initialState.destination.route)
            if (isInitialVertical) {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down, animationSpec = tween(300, easing = FastOutSlowInEasing))
            } else {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(300, easing = FastOutSlowInEasing)) 
            }
        }
    ) {
        composable("welcome") {
            WelcomeScreen(
                viewModel = authViewModel,
                onClose = { navController.navigate("home") },
                onNext = { 
                    authViewModel.submitEmail()
                    navController.navigate("otp") 
                }
            )
        }
        
        composable("otp") {
            OtpScreen(
                viewModel = authViewModel,
                onBack = { navController.popBackStack() },
                onNext = { 
                    authViewModel.verifyOtp()
                    navController.navigate("profile") 
                }
            )
        }
        
        composable("profile") {
            ProfileScreen(
                viewModel = authViewModel,
                onBack = { navController.popBackStack() },
                onNext = { 
                    authViewModel.completeProfile()
                    navController.navigate("deadline") 
                }
            )
        }
        
        composable("deadline") {
            DeadlineScreen(
                viewModel = authViewModel,
                onBack = { navController.popBackStack() },
                onNext = { 
                    authViewModel.saveDeadlineSetting()
                    navController.navigate("home") {
                        popUpTo("welcome") { inclusive = true }
                    }
                }
            )
        }
        
        composable("home") {
            HomeScreen(
                viewModel = viewModel,
                modifier = Modifier,
                onNavigateToSearch = { navController.navigate("search") },
                onNavigateToNotifications = { navController.navigate("notifications") },
                onNavigateToProfile = { navController.navigate("user_profile") },
                onNavigateToAgenda = { navController.navigate("agenda") }
            )
        }
        
        composable("user_profile") {
            UserProfileScreen(
                viewModel = userProfileViewModel,
                onBack = { navController.popBackStack() },
                onNavigateToEditProfile = { navController.navigate("edit_profile") }
            )
        }
        
        composable("edit_profile") {
            EditProfileScreen(
                onBack = { navController.popBackStack() },
                onSave = { navController.popBackStack() }
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
                viewModel = searchViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable("agenda") {
            com.example.ui.screens.AgendaScreen(
                onClose = { navController.popBackStack() }
            )
        }
    }
}
