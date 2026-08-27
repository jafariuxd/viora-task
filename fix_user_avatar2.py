import re

with open('app/src/main/java/com/example/ui/components/UserAvatar.kt', 'r') as f:
    content = f.read()

bad_mock = """            androidx.compose.material3.Text(
                text = userId.take(1).uppercase(),
                color = Color.White,
                fontSize = androidx.compose.ui.unit.TextUnit(size.value * 0.5f, androidx.compose.ui.unit.TextUnitType.Sp),
                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
            )"""

good_mock = """            androidx.compose.material3.Text(
                text = userId.take(1).uppercase(),
                color = Color.White,
                fontSize = (size.value * 0.45f).sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                fontFamily = com.example.ui.theme.SFProDisplayFontFamily
            )"""

if bad_mock in content:
    content = content.replace(bad_mock, good_mock)
else:
    print("bad_mock not found!")

# Add import for sp
if "import androidx.compose.ui.unit.sp" not in content:
    content = content.replace("import androidx.compose.ui.unit.dp", "import androidx.compose.ui.unit.dp\nimport androidx.compose.ui.unit.sp")

with open('app/src/main/java/com/example/ui/components/UserAvatar.kt', 'w') as f:
    f.write(content)
