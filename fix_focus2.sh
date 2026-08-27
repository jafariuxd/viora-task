sed -i 's/\.pointerInput(Unit) {/.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { focusManager.clearFocus() }/g' app/src/main/java/com/example/ui/screens/TaskDetailScreen.kt
sed -i 's/detectTapGestures(onTap = {//g' app/src/main/java/com/example/ui/screens/TaskDetailScreen.kt
sed -i 's/focusManager.clearFocus()//g' app/src/main/java/com/example/ui/screens/TaskDetailScreen.kt
sed -i 's/})//g' app/src/main/java/com/example/ui/screens/TaskDetailScreen.kt
