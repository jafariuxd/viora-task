with open("app/src/main/java/com/example/ui/utils/ShimmerEffect.kt", "r") as f:
    content = f.read()

target = """            colors = listOf(
                Color(0xFF2C2C2E).copy(alpha = 0.6f),
                Color(0xFF3A3A3C).copy(alpha = 0.8f),
                Color(0xFF2C2C2E).copy(alpha = 0.6f)
            ),"""

rep = """            colors = listOf(
                Color.White.copy(alpha = 0.05f),
                Color.White.copy(alpha = 0.15f),
                Color.White.copy(alpha = 0.05f)
            ),"""

content = content.replace(target, rep)

with open("app/src/main/java/com/example/ui/utils/ShimmerEffect.kt", "w") as f:
    f.write(content)
