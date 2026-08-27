import re

with open('app/src/main/java/com/example/ui/components/UserAvatar.kt', 'r') as f:
    content = f.read()

bad_mock = """            Icon(
                imageVector = Icons.Rounded.Person,
                contentDescription = "User",
                tint = Color.White,
                modifier = Modifier.size(size * 0.6f)
            )"""

good_mock = """            androidx.compose.material3.Text(
                text = userId.take(1).uppercase(),
                color = Color.White,
                fontSize = androidx.compose.ui.unit.TextUnit(size.value * 0.5f, androidx.compose.ui.unit.TextUnitType.Sp),
                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
            )"""

if bad_mock in content:
    content = content.replace(bad_mock, good_mock)
else:
    print("bad_mock not found!")

with open('app/src/main/java/com/example/ui/components/UserAvatar.kt', 'w') as f:
    f.write(content)
