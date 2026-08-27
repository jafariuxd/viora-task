with open('app/src/main/java/com/example/ui/screens/AgendaScreen.kt', 'r') as f:
    c = f.read()
c = c.replace('.bounceClick(enabled = !item.isPast) { onClick() }', '.clickable(enabled = !item.isPast) { onClick() }')
with open('app/src/main/java/com/example/ui/screens/AgendaScreen.kt', 'w') as f:
    f.write(c)

with open('app/src/main/java/com/example/ui/components/TaskComponents.kt', 'r') as f:
    c = f.read()
c = c.replace('.bounceClick(enabled = enabled) { onClick() }', '.clickable(enabled = enabled) { onClick() }')
with open('app/src/main/java/com/example/ui/components/TaskComponents.kt', 'w') as f:
    f.write(c)
