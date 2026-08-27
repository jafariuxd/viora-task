import re

with open('app/src/main/java/com/example/ui/screens/EditProfileScreen.kt', 'r') as f:
    content = f.read()

bad_mock = """            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .clickable { launcher.launch("image/*") }
            ) {
                if (avatarUri != null) {
                    AsyncImage(
                        model = com.example.util.ImageUtil.toCoilModel(avatarUri),
                        contentDescription = "Profile Picture",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.img_profile_mohammad_1783672402325),
                        contentDescription = "Profile Picture",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }"""

good_mock = """            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .clickable { launcher.launch("image/*") }
            ) {
                com.example.ui.components.UserAvatar(
                    userId = email,
                    avatarUri = avatarUri,
                    size = 120.dp
                )
            }"""

if bad_mock in content:
    content = content.replace(bad_mock, good_mock)
else:
    print("bad_mock not found!")

with open('app/src/main/java/com/example/ui/screens/EditProfileScreen.kt', 'w') as f:
    f.write(content)
