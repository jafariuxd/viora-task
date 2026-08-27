import re

with open('app/src/main/java/com/example/ui/screens/UserProfileScreen.kt', 'r') as f:
    content = f.read()

bad_mock = """            // Avatar Profile
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 57.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(125.dp)
                        .clip(CircleShape)
                        .background(Color.Gray)
                        .clickable { launcher.launch("image/*") }
                ) {
                    if (profile?.profileImageUri != null) {
                        AsyncImage(
                            model = com.example.util.ImageUtil.toCoilModel(profile?.profileImageUri),
                            contentDescription = "Profile Picture",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else if (profile?.profileImageRes != null) {
                        Image(
                            painter = painterResource(id = profile!!.profileImageRes!!),
                            contentDescription = "Profile Picture",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }"""

good_mock = """            // Avatar Profile
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 57.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(125.dp)
                        .clip(CircleShape)
                        .clickable { launcher.launch("image/*") }
                ) {
                    com.example.ui.components.UserAvatar(
                        userId = profile?.email ?: profile?.name ?: "User",
                        avatarUri = profile?.profileImageUri,
                        size = 125.dp
                    )
                }"""

if bad_mock in content:
    content = content.replace(bad_mock, good_mock)
else:
    print("bad_mock not found!")

with open('app/src/main/java/com/example/ui/screens/UserProfileScreen.kt', 'w') as f:
    f.write(content)
