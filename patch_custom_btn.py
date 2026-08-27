import re

with open("app/src/main/java/com/example/ui/components/HeaderComponents.kt", "r") as f:
    content = f.read()

new_btn = """@Composable
fun VioraHeaderCustomButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    androidx.compose.material3.IconButton(
        onClick = onClick,
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .border(1.dp, VIORA_HEADER_BUTTON_BORDER, CircleShape)
    ) {
        Box(contentAlignment = Alignment.Center) {
            content()
        }
    }
}"""

content = re.sub(r'@Composable\nfun VioraHeaderCustomButton\(.*?\}\n\}', new_btn, content, flags=re.DOTALL)

with open("app/src/main/java/com/example/ui/components/HeaderComponents.kt", "w") as f:
    f.write(content)
