with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "r") as f:
    content = f.read()

content = content.replace("fun HomeScreenTopBar(", "fun HomeScreenTopBar(\n    onVioraPassClick: () -> Unit = {},")

actions_str = """
            com.example.ui.components.VioraHeaderIconButton(
                icon = androidx.compose.material.icons.Icons.Rounded.QrCode,
                contentDescription = "Viora Pass",
                onClick = onVioraPassClick,
                modifier = Modifier.testTag("viora_pass_button")
            )

            com.example.ui.components.VioraHeaderIconButton(
                icon = Icons.Rounded.Search,
"""
content = content.replace("            com.example.ui.components.VioraHeaderIconButton(\n                icon = Icons.Rounded.Search,", actions_str)

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "w") as f:
    f.write(content)
