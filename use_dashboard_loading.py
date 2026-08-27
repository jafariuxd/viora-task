with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "r") as f:
    content = f.read()

target1 = """    val isAgendaAuthorized by agendaViewModel.isAuthorized.collectAsState()"""
rep1 = """    val isAgendaAuthorized by agendaViewModel.isAuthorized.collectAsState()
    val isDashboardLoading by viewModel.isDashboardLoading.collectAsState()"""

content = content.replace(target1, rep1)

target_weather_call = """                                            timeString = if (currentTime.isNotEmpty()) currentTime else "22:13"
                                        )"""
rep_weather_call = """                                            timeString = if (currentTime.isNotEmpty()) currentTime else "22:13",
                                            isLoading = isDashboardLoading
                                        )"""
content = content.replace(target_weather_call, rep_weather_call)

target_event_call = """                                                UpcomingEventCard(
                                                    event = eventToDisplay,
                                                    onClick = { onNavigateToAgenda() }
                                                )"""
rep_event_call = """                                                UpcomingEventCard(
                                                    event = eventToDisplay,
                                                    onClick = { onNavigateToAgenda() },
                                                    isLoading = isDashboardLoading
                                                )"""
content = content.replace(target_event_call, rep_event_call)

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "w") as f:
    f.write(content)
