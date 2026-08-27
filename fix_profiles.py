import re

with open('app/src/main/java/com/example/ui/screens/UserProfileScreen.kt', 'r') as f:
    content = f.read()

content = content.replace("profile?.email ?: profile?.name", "profile?.username ?: profile?.name")

with open('app/src/main/java/com/example/ui/screens/UserProfileScreen.kt', 'w') as f:
    f.write(content)

with open('app/src/main/java/com/example/ui/screens/EditProfileScreen.kt', 'r') as f:
    content = f.read()

content = content.replace("userId = email,", "userId = username,")

with open('app/src/main/java/com/example/ui/screens/EditProfileScreen.kt', 'w') as f:
    f.write(content)

