with open("app/src/main/java/com/example/ui/screens/TeamsScreen.kt", "r") as f:
    content = f.read()

target = """    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VioraBackground)
            .statusBarsPadding()
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = { showCreateTeamSheet = true }) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "Add Team",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Header
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Select Team",
                color = Color.White,
                fontSize = 28.sp,
                fontFamily = SFProDisplayFontFamily,
                fontWeight = FontWeight.Medium,
                letterSpacing = (-0.5).sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "You have ${teams.size} created teams",
                color = Color(0xFFAAAAAA),
                fontSize = 14.sp,
                fontFamily = SFProDisplayFontFamily,
                fontWeight = FontWeight.Normal
            )
        }

        Spacer(modifier = Modifier.height(48.dp))"""

rep = """    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VioraBackground)
            .statusBarsPadding()
    ) {
        // Top Bar
        Box(modifier = Modifier.animateEnter(delayMillis = 0)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = { showCreateTeamSheet = true }) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "Add Team",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Header
        Box(modifier = Modifier.animateEnter(delayMillis = 50)) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Select Team",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontFamily = SFProDisplayFontFamily,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = (-0.5).sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "You have ${teams.size} created teams",
                    color = Color(0xFFAAAAAA),
                    fontSize = 14.sp,
                    fontFamily = SFProDisplayFontFamily,
                    fontWeight = FontWeight.Normal
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))"""
content = content.replace(target, rep)
with open("app/src/main/java/com/example/ui/screens/TeamsScreen.kt", "w") as f:
    f.write(content)

