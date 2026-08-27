with open("app/src/main/java/com/example/widget/DailyBriefWidget.kt", "r") as f:
    content = f.read()

content = content.replace("import androidx.glance.action.actionStartActivity", "import androidx.glance.action.actionStartActivity\nimport androidx.glance.action.ActionParameters\nimport androidx.glance.action.actionParametersOf")
content = content.replace("actionStartActivity(intent)", "actionStartActivity<MainActivity>(actionParametersOf(ActionParameters.Key<Boolean>(\"OPEN_DAILY_BRIEF\") to true))")

with open("app/src/main/java/com/example/widget/DailyBriefWidget.kt", "w") as f:
    f.write(content)
