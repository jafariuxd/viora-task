import re
with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "r") as f:
    content = f.read()

print("articles:", bool(re.search(r"AnimatedContent.*?articles_anim", content, re.DOTALL)))
print("articles_end:", bool(re.search(r"QuickAddActivity", content)))
