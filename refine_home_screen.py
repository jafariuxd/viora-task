import re

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "r") as f:
    content = f.read()

# 1. Update Crossfade animation spec
crossfade_target = """                Crossfade(
                    targetState = currentTab, 
                    label = "tab_fade",
                    animationSpec = tween(300, easing = LinearOutSlowInEasing)
                ) { tab ->"""
crossfade_rep = """                Crossfade(
                    targetState = currentTab, 
                    label = "tab_fade",
                    animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f)
                ) { tab ->"""
content = content.replace(crossfade_target, crossfade_rep)

# 2. Add Modifier to HeaderSection signature
header_sig_target = """fun HeaderSection(
    userName: String,
    avatarUri: String? = null,
    onSearchClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onProfileClick: () -> Unit
) {"""
header_sig_rep = """fun HeaderSection(
    userName: String,
    avatarUri: String? = null,
    modifier: Modifier = Modifier,
    onSearchClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onProfileClick: () -> Unit
) {"""
content = content.replace(header_sig_target, header_sig_rep)

header_row_target = """fun HeaderSection(
    userName: String,
    avatarUri: String? = null,
    modifier: Modifier = Modifier,
    onSearchClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()"""
header_row_rep = """fun HeaderSection(
    userName: String,
    avatarUri: String? = null,
    modifier: Modifier = Modifier,
    onSearchClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()"""
content = content.replace(header_row_target, header_row_rep)

# 3. Apply animateEnter to HeaderSection
header_call_target = """                            Column(modifier = Modifier.fillMaxSize()) {
                                HeaderSection(
                                    userName = currentUserName,
                                    avatarUri = currentUserAvatarUri,
                                    onSearchClick = onNavigateToSearch,
                                    onNotificationClick = onNavigateToNotifications,
                                    onProfileClick = onNavigateToProfile
                                )
                                 LazyColumn("""
header_call_rep = """                            Column(modifier = Modifier.fillMaxSize()) {
                                HeaderSection(
                                    userName = currentUserName,
                                    avatarUri = currentUserAvatarUri,
                                    modifier = Modifier.animateEnter(delayMillis = 0),
                                    onSearchClick = onNavigateToSearch,
                                    onNotificationClick = onNavigateToNotifications,
                                    onProfileClick = onNavigateToProfile
                                )
                                 LazyColumn("""
content = content.replace(header_call_target, header_call_rep)

# 4. Apply animateEnter to BottomNavigationBar
bottom_nav_target = """            // Bottom Navigation Bar
            VioraBottomNavigationBar(
                selectedTab = currentTab,"""
bottom_nav_rep = """            // Bottom Navigation Bar
            Box(modifier = Modifier.animateEnter(delayMillis = 400, initialOffsetY = 100f)) {
                VioraBottomNavigationBar(
                    selectedTab = currentTab,"""
# Since there is a Box, we also need to close it. We'll find the call:
bottom_nav_full_target = """            // Bottom Navigation Bar
            VioraBottomNavigationBar(
                selectedTab = currentTab,
                onTabSelected = { tab -> viewModel.selectTab(tab) },
                onNewTaskClick = {
                    activeDetailTask = Task(
                        id = java.util.UUID.randomUUID().toString(),
                        title = "",
                        client = "Viora design",
                        userId = "user1",
                        daysLeft = 7,
                        status = TaskStatus.TODO
                    )
                }
            )"""
bottom_nav_full_rep = """            // Bottom Navigation Bar
            Box(modifier = Modifier.animateEnter(delayMillis = 400, initialOffsetY = 100f)) {
                VioraBottomNavigationBar(
                    selectedTab = currentTab,
                    onTabSelected = { tab -> viewModel.selectTab(tab) },
                    onNewTaskClick = {
                        activeDetailTask = Task(
                            id = java.util.UUID.randomUUID().toString(),
                            title = "",
                            client = "Viora design",
                            userId = "user1",
                            daysLeft = 7,
                            status = TaskStatus.TODO
                        )
                    }
                )
            }"""
content = content.replace(bottom_nav_full_target, bottom_nav_full_rep)

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "w") as f:
    f.write(content)
