package com.example.ui.navigation

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.R
import com.example.data.MoneyAiRepository
import com.example.data.MoneyHubRepository
import com.example.data.PreferencesRepository
import com.example.firebase.authStateFlow
import com.example.localization.AppLanguage
import com.example.localization.LocalAppStrings
import com.example.localization.stringsFor
import com.example.model.ThemeMode
import com.example.model.UiState
import com.example.ui.components.AppRoutes
import com.example.ui.components.MoneyHubBottomBar
import com.example.ui.components.MoneyHubTopBar
import com.example.ui.components.nextThemeMode
import com.example.ui.screens.AcademyScreen
import com.example.ui.screens.AdminScreen
import com.example.ui.screens.AiScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.DesignScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MarketPublishDraft
import com.example.ui.screens.MarketScreen
import com.example.ui.screens.PremiumScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.WalletScreen
import com.example.ui.theme.MoneyHubTheme
import com.example.viewmodel.MainViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

@Composable
fun MoneyHubApp() {
    val context = LocalContext.current
    val prefsRepo = remember(context) { PreferencesRepository(context.applicationContext) }
    val scope = rememberCoroutineScope()

    val savedLanguage by prefsRepo.languageFlow.collectAsStateWithLifecycle(initialValue = AppLanguage.PT)
    val savedThemeMode by prefsRepo.themeModeFlow.collectAsStateWithLifecycle(initialValue = ThemeMode.DARK)
    val currentUser by Firebase.auth.authStateFlow()
        .collectAsStateWithLifecycle(initialValue = Firebase.auth.currentUser)

    val systemDark = isSystemInDarkTheme()
    val useDarkTheme = when (savedThemeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> systemDark
    }

    CompositionLocalProvider(LocalAppStrings provides stringsFor(savedLanguage)) {
        MoneyHubTheme(darkTheme = useDarkTheme) {
            val user = currentUser
            if (user == null) {
                AuthScreen(
                    language = savedLanguage,
                    onLanguageSelected = { lang ->
                        scope.launch { prefsRepo.setLanguage(lang) }
                    },
                    onAuthSuccess = {}
                )
            } else {
                key(user.uid) {
                    AuthenticatedShell(
                        currentUserId = user.uid,
                        prefsRepo = prefsRepo,
                        language = savedLanguage,
                        themeMode = savedThemeMode,
                        isDark = useDarkTheme
                    )
                }
            }
        }
    }
}

@Composable
private fun AuthenticatedShell(
    currentUserId: String,
    prefsRepo: PreferencesRepository,
    language: AppLanguage,
    themeMode: ThemeMode,
    isDark: Boolean
) {
    val strings = LocalAppStrings.current
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }

    val viewModel: MainViewModel = viewModel(
        key = currentUserId,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                MainViewModel(
                    repository = MoneyHubRepository(db),
                    aiRepository = MoneyAiRepository(),
                    prefsRepository = prefsRepo,
                    currentUserId = currentUserId
                )
            }
        }
    )

    val userProfileState by viewModel.userProfileState.collectAsStateWithLifecycle()
    val academyProgressState by viewModel.academyProgressState.collectAsStateWithLifecycle()
    val designProjectsState by viewModel.designProjectsState.collectAsStateWithLifecycle()
    val aiGenerationsState by viewModel.aiGenerationsState.collectAsStateWithLifecycle()
    val activityLogsState by viewModel.activityLogsState.collectAsStateWithLifecycle()
    val payoutMethodsState by viewModel.payoutMethodsState.collectAsStateWithLifecycle()
    val marketProductsState by viewModel.marketProductsState.collectAsStateWithLifecycle()
    val myProductsState by viewModel.myProductsState.collectAsStateWithLifecycle()
    val adminReportsState by viewModel.adminReportsState.collectAsStateWithLifecycle()
    val selectedReviewsState by viewModel.selectedProductReviewsState.collectAsStateWithLifecycle()
    val aiResponseState by viewModel.aiResponseState.collectAsStateWithLifecycle()
    val designDraftAiCopy by viewModel.designDraftAiCopy.collectAsStateWithLifecycle()
    val isGeneratingDesignCopy by viewModel.isGeneratingDesignCopy.collectAsStateWithLifecycle()
    val prefilledAiMode by viewModel.prefilledAiMode.collectAsStateWithLifecycle()
    val prefilledAiPrompt by viewModel.prefilledAiPrompt.collectAsStateWithLifecycle()
    val prefilledDesignHeadline by viewModel.prefilledDesignHeadline.collectAsStateWithLifecycle()

    var marketPublishDraft by remember { mutableStateOf<MarketPublishDraft?>(null) }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val topLevelRoutes = setOf(
        AppRoutes.HOME,
        AppRoutes.ACADEMY,
        AppRoutes.DESIGN,
        AppRoutes.AI,
        AppRoutes.MARKET
    )
    val isTopLevel = currentRoute == null || currentRoute in topLevelRoutes

    val topBarTitle = when (currentRoute) {
        AppRoutes.ACADEMY -> strings.academyTitle
        AppRoutes.DESIGN -> strings.designTitle
        AppRoutes.AI -> strings.aiTitle
        AppRoutes.MARKET -> strings.marketTitle
        else -> strings.appName
    }

    val profilePhoto = (userProfileState as? UiState.Success)?.data?.photoUrl
        ?: Firebase.auth.currentUser?.photoUrl?.toString()

    val navigateTab: (String) -> Unit = { route ->
        if (route in topLevelRoutes) {
            navController.navigate(route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        } else {
            navController.navigate(route) {
                launchSingleTop = true
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            if (isTopLevel) {
                MoneyHubTopBar(
                    title = topBarTitle,
                    language = language,
                    isDark = isDark,
                    photoUrl = profilePhoto,
                    onToggleLanguage = {
                        val nextLang = if (language == AppLanguage.PT) AppLanguage.EN else AppLanguage.PT
                        viewModel.setLanguage(nextLang)
                    },
                    onToggleTheme = {
                        viewModel.setThemeMode(nextThemeMode(themeMode))
                    },
                    onProfileClick = { navigateTab(AppRoutes.PROFILE) }
                )
            }
        },
        bottomBar = {
            if (isTopLevel) {
                MoneyHubBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = navigateTab
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            NavHost(
                navController = navController,
                startDestination = AppRoutes.HOME
            ) {
                composable(AppRoutes.HOME) {
                    HomeScreen(
                        language = language,
                        userProfileState = userProfileState,
                        academyProgressState = academyProgressState,
                        designProjectsState = designProjectsState,
                        aiGenerationsState = aiGenerationsState,
                        marketProductsState = marketProductsState,
                        myProductsState = myProductsState,
                        isAdminUser = viewModel.isAdminUser,
                        onNavigate = navigateTab,
                        onOpenOpportunityInAi = { mode, prompt ->
                            viewModel.prepareAiPrompt(mode, prompt)
                            navigateTab(AppRoutes.AI)
                        }
                    )
                }
                composable(AppRoutes.ACADEMY) {
                    AcademyScreen(
                        language = language,
                        academyProgressState = academyProgressState,
                        onToggleLessonComplete = viewModel::toggleLessonComplete
                    )
                }
                composable(AppRoutes.DESIGN) {
                    DesignScreen(
                        language = language,
                        projectsState = designProjectsState,
                        aiCopySuggestion = designDraftAiCopy,
                        isGeneratingCopy = isGeneratingDesignCopy,
                        prefilledHeadline = prefilledDesignHeadline,
                        onClearPrefilledHeadline = viewModel::clearPrefilledDesignHeadline,
                        onClearAiCopy = viewModel::clearDesignDraftAiCopy,
                        onRequestAiCopy = { topic, label ->
                            viewModel.generateDesignCopyWithAi(topic, label, language)
                        },
                        onSaveProject = viewModel::saveDesignProject,
                        onDeleteProject = viewModel::deleteDesignProject,
                        onPublishToMarket = { title, desc, palId ->
                            marketPublishDraft = MarketPublishDraft(title, desc, palId)
                            navigateTab(AppRoutes.MARKET)
                        }
                    )
                }
                composable(AppRoutes.AI) {
                    AiScreen(
                        language = language,
                        aiResponseState = aiResponseState,
                        historyState = aiGenerationsState,
                        prefilledMode = prefilledAiMode,
                        prefilledPrompt = prefilledAiPrompt,
                        onClearPrefilled = viewModel::clearPrefilledAiPrompt,
                        onGenerate = { mode, prompt ->
                            viewModel.generateWithAi(mode, prompt, language)
                        },
                        onDeleteHistoryItem = viewModel::deleteAiGeneration,
                        onSendToDesign = { text ->
                            viewModel.sendTextToDesign(text)
                            navigateTab(AppRoutes.DESIGN)
                        }
                    )
                }
                composable(AppRoutes.MARKET) {
                    MarketScreen(
                        language = language,
                        currentUserId = currentUserId,
                        productsState = marketProductsState,
                        reviewsState = selectedReviewsState,
                        publishDraft = marketPublishDraft,
                        onClearPublishDraft = { marketPublishDraft = null },
                        onSelectProduct = viewModel::selectProductForReviews,
                        onPublishProduct = viewModel::publishMarketProduct,
                        onDeleteProduct = viewModel::deleteMarketProduct,
                        onSubmitReview = viewModel::submitReview,
                        onReportContent = viewModel::reportContent
                    )
                }
                composable(AppRoutes.PROFILE) {
                    ProfileScreen(
                        language = language,
                        themeMode = themeMode,
                        userProfileState = userProfileState,
                        activityLogsState = activityLogsState,
                        myProductsState = myProductsState,
                        isAdminUser = viewModel.isAdminUser,
                        onBack = { navController.popBackStack() },
                        onNavigate = navigateTab,
                        onUpdateProfile = viewModel::updateProfile
                    )
                }
                composable(AppRoutes.WALLET) {
                    WalletScreen(
                        language = language,
                        payoutMethodsState = payoutMethodsState,
                        onBack = { navController.popBackStack() },
                        onAddPayoutMethod = viewModel::addPayoutMethod,
                        onDeletePayoutMethod = viewModel::deletePayoutMethod
                    )
                }
                composable(AppRoutes.PREMIUM) {
                    PremiumScreen(
                        language = language,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(AppRoutes.ADMIN) {
                    AdminScreen(
                        isAdminUser = viewModel.isAdminUser,
                        reportsState = adminReportsState,
                        marketProductsState = marketProductsState,
                        onBack = { navController.popBackStack() },
                        onUpdateReportStatus = viewModel::updateReportStatus,
                        onModerateProduct = viewModel::moderateProductStatus
                    )
                }
            }
        }
    }
}
