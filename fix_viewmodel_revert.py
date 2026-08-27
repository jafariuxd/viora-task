import re

with open('app/src/main/java/com/example/viewmodel/VioraTaskViewModel.kt', 'r') as f:
    content = f.read()

bad_mock = "_teams.value.find { it.name == teamName }"
good_mock = "mockTeams.values.find { it.name == teamName }"

content = content.replace(bad_mock, good_mock)

bad_mock_2 = "_teams.value.find { it.name == oldTeamName }"
good_mock_2 = "mockTeams.values.find { it.name == oldTeamName }"

content = content.replace(bad_mock_2, good_mock_2)

bad_mock_3 = "_lists.value.find { it.name == listName }"
good_mock_3 = "mockLists.values.find { it.name == listName }"

content = content.replace(bad_mock_3, good_mock_3)

with open('app/src/main/java/com/example/viewmodel/VioraTaskViewModel.kt', 'w') as f:
    f.write(content)
