with open("app/src/main/java/com/example/viewmodel/AgendaViewModel.kt", "r") as f:
    content = f.read()

content = content.replace("import android.content.Intent", "import android.content.Intent\nimport android.util.Log")

new_handle_auth = """    fun handleAuthorizationResult(context: android.content.Context, intent: Intent?) {
        viewModelScope.launch {
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(intent)
                val account = task.getResult(com.google.android.gms.common.api.ApiException::class.java)
                
                if (account?.account != null) {
                    _isLoading.value = true
                    val token = withContext(Dispatchers.IO) {
                        GoogleAuthUtil.getToken(context, account.account!!, "oauth2:https://www.googleapis.com/auth/calendar.readonly")
                    }
                    Log.d("AgendaViewModel", "Got token: ${token.take(10)}...")
                    currentAccessToken = token
                    fetchEvents(context, token)
                } else {
                    _error.value = "Account is null"
                    Log.e("AgendaViewModel", "Account is null")
                }
            } catch (e: retrofit2.HttpException) {
                Log.e("AgendaViewModel", "HTTP Exception in auth: ${e.code()} ${e.message()}")
                // ... rest is handled by fetchEvents usually anyway, wait handleAuthorizationResult doesn't make network calls using retrofit
            } catch (e: Exception) {
                _error.value = "Auth error: ${e.message}"
                Log.e("AgendaViewModel", "Auth error", e)
            } finally {
                // _isLoading.value = false // let fetchEvents handle this
            }
        }
    }"""

import re
content = re.sub(r"    fun handleAuthorizationResult.*?    private fun fetchEvents", new_handle_auth + "\n\n    private fun fetchEvents", content, flags=re.DOTALL)

new_fetch_events = """                val items = response.items ?: emptyList()
                Log.d("AgendaViewModel", "Fetched ${items.size} events")
                _events.value = items.map { parseEvent(it) }
            } catch (e: retrofit2.HttpException) {
                Log.e("AgendaViewModel", "HTTP Error fetching events", e)"""
content = content.replace("""                val items = response.items ?: emptyList()
                _events.value = items.map { parseEvent(it) }
            } catch (e: retrofit2.HttpException) {""", new_fetch_events)

content = content.replace("""            } catch (e: Exception) {
                _error.value = "Failed to fetch events: ${e.message}"
            } finally {""", """            } catch (e: Exception) {
                Log.e("AgendaViewModel", "Error fetching events", e)
                _error.value = "Failed to fetch events: ${e.message}"
            } finally {""")

with open("app/src/main/java/com/example/viewmodel/AgendaViewModel.kt", "w") as f:
    f.write(content)
