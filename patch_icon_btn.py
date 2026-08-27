import re

with open("app/src/main/java/com/example/ui/components/HeaderComponents.kt", "r") as f:
    content = f.read()

new_btn = """@Composable
fun VioraHeaderIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = Color.White,
    iconSize: Dp = 22.dp,
    badge: @Composable (BoxScope.() -> Unit)? = null
) {
    androidx.compose.material3.IconButton(
        onClick = onClick,
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .border(1.dp, VIORA_HEADER_BUTTON_BORDER, CircleShape)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = tint,
                modifier = Modifier.size(iconSize)
            )
            if (badge != null) {
                badge()
            }
        }
    }
}"""

content = re.sub(r'@Composable\nfun VioraHeaderIconButton\(.*?\}\n\}', new_btn, content, flags=re.DOTALL)

with open("app/src/main/java/com/example/ui/components/HeaderComponents.kt", "w") as f:
    f.write(content)
