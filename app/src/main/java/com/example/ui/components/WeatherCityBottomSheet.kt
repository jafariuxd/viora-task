package com.example.ui.components

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SFProDisplayFontFamily
import com.example.ui.theme.VioraNeonLime
import com.example.viewmodel.VioraTaskViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class WeatherCityItem(
    val name: String,
    val country: String? = null,
    val admin1: String? = null,
    val latitude: Double,
    val longitude: Double
)

val defaultPresetCities = listOf(
    WeatherCityItem("Tehran", "Iran", "Tehran", 35.6944, 51.4215),
    WeatherCityItem("Mashhad", "Iran", "Razavi Khorasan", 36.2970, 59.6062),
    WeatherCityItem("Isfahan", "Iran", "Isfahan", 32.6546, 51.6680),
    WeatherCityItem("Shiraz", "Iran", "Fars", 29.5918, 52.5837),
    WeatherCityItem("Tabriz", "Iran", "East Azerbaijan", 38.0962, 46.2738),
    WeatherCityItem("Karaj", "Iran", "Alborz", 35.8327, 50.9915),
    WeatherCityItem("Ahvaz", "Iran", "Khuzestan", 31.3183, 48.6706),
    WeatherCityItem("Qom", "Iran", "Qom", 34.6416, 50.8746),
    WeatherCityItem("Kermanshah", "Iran", "Kermanshah", 34.3142, 47.0650),
    WeatherCityItem("Rasht", "Iran", "Gilan", 37.2808, 49.5831),
    WeatherCityItem("Kerman", "Iran", "Kerman", 30.2839, 57.0834),
    WeatherCityItem("Zahedan", "Iran", "Sistan and Baluchestan", 29.4963, 60.8629),
    WeatherCityItem("Hamedan", "Iran", "Hamedan", 34.7989, 48.5150),
    WeatherCityItem("Yazd", "Iran", "Yazd", 31.8974, 54.3569),
    WeatherCityItem("Urmia", "Iran", "West Azerbaijan", 37.5527, 45.0761),
    WeatherCityItem("Ardabil", "Iran", "Ardabil", 38.2498, 48.2933),
    WeatherCityItem("Bandar Abbas", "Iran", "Hormozgan", 27.1832, 56.2666),
    WeatherCityItem("Sari", "Iran", "Mazandaran", 36.5659, 53.0581),
    WeatherCityItem("Zanjan", "Iran", "Zanjan", 36.6736, 48.4787),
    WeatherCityItem("Sanandaj", "Iran", "Kurdistan", 35.3113, 46.9961),
    WeatherCityItem("Khorramabad", "Iran", "Lorestan", 33.4878, 48.3558),
    WeatherCityItem("Gorgan", "Iran", "Golestan", 36.8427, 54.4439),
    WeatherCityItem("London", "United Kingdom", "England", 51.5074, -0.1278),
    WeatherCityItem("Paris", "France", "Île-de-France", 48.8566, 2.3522),
    WeatherCityItem("New York", "United States", "New York", 40.7128, -74.0060),
    WeatherCityItem("Tokyo", "Japan", "Tokyo", 35.6762, 139.6503),
    WeatherCityItem("Dubai", "United Arab Emirates", "Dubai", 25.2048, 55.2708),
    WeatherCityItem("Istanbul", "Turkey", "Istanbul", 41.0082, 28.9784),
    WeatherCityItem("Toronto", "Canada", "Ontario", 43.6532, -79.3832),
    WeatherCityItem("Berlin", "Germany", "Berlin", 52.5200, 13.4050),
    WeatherCityItem("Sydney", "Australia", "New South Wales", -33.8688, 151.2093)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherCityBottomSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    taskViewModel: VioraTaskViewModel
) {
    if (!visible) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isAutoDetect by taskViewModel.weatherAutoDetect.collectAsState()
    val manualCityVal by taskViewModel.manualCity.collectAsState()
    val weatherInfo by taskViewModel.weatherInfo.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var isSearchingOnline by remember { mutableStateOf(false) }
    var onlineResults by remember { mutableStateOf<List<WeatherCityItem>>(emptyList()) }

    val coroutineScope = rememberCoroutineScope()

    // Trigger geocoding search on query change
    LaunchedEffect(searchQuery) {
        val q = searchQuery.trim()
        if (q.length < 2) {
            onlineResults = emptyList()
            isSearchingOnline = false
            return@LaunchedEffect
        }

        delay(350) // Debounce user typing
        isSearchingOnline = true
        withContext(Dispatchers.IO) {
            val fetched = mutableListOf<WeatherCityItem>()
            try {
                val encodedQuery = Uri.encode(q)
                val urlString = "https://geocoding-api.open-meteo.com/v1/search?name=$encodedQuery&count=20&language=en&format=json"
                val connection = URL(urlString).openConnection() as HttpURLConnection
                connection.connectTimeout = 5000
                connection.readTimeout = 5000
                connection.requestMethod = "GET"
                connection.setRequestProperty("User-Agent", "VioraTask/1.0")

                if (connection.responseCode == 200) {
                    val body = connection.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(body)
                    if (json.has("results")) {
                        val resultsArr = json.getJSONArray("results")
                        for (i in 0 until resultsArr.length()) {
                            val itemObj = resultsArr.getJSONObject(i)
                            val name = itemObj.optString("name", "")
                            val lat = itemObj.optDouble("latitude", 0.0)
                            val lon = itemObj.optDouble("longitude", 0.0)
                            val country = itemObj.optString("country", null)
                            val admin = itemObj.optString("admin1", null)
                            if (name.isNotBlank()) {
                                fetched.add(WeatherCityItem(name, country, admin, lat, lon))
                            }
                        }
                    }
                }
                connection.disconnect()
            } catch (e: Exception) {
                // Fallback gracefully on network error
            }

            withContext(Dispatchers.Main) {
                onlineResults = fetched
                isSearchingOnline = false
            }
        }
    }

    // Filtered city items to display
    val displayedCities = remember(searchQuery, onlineResults) {
        val q = searchQuery.trim()
        if (q.isEmpty()) {
            defaultPresetCities
        } else {
            val localMatches = defaultPresetCities.filter {
                it.name.contains(q, ignoreCase = true) ||
                        (it.country?.contains(q, ignoreCase = true) == true) ||
                        (it.admin1?.contains(q, ignoreCase = true) == true)
            }
            // Merge local and online results with uniqueness by name + country
            val combined = (onlineResults + localMatches).distinctBy { "${it.name}_${it.country ?: ""}_${Math.round(it.latitude * 100)}" }
            combined
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = com.example.ui.utils.dynamicBottomSheetShape(sheetState = sheetState, defaultRadius = 28.dp),
        containerColor = Color(0xFF1C1C1E),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(36.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.25f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp, top = 4.dp)
                .navigationBarsPadding()
                .testTag("weather_city_bottom_sheet")
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Weather Location",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = SFProDisplayFontFamily
                    )
                    Text(
                        text = "Currently: ${weatherInfo.location} (${weatherInfo.temperature})",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 13.sp,
                        fontFamily = SFProDisplayFontFamily
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Search input field
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF2C2C2E))
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = "Search",
                        tint = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Box(modifier = Modifier.weight(1f)) {
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = "Search any city (e.g. Tehran, London, Tokyo...)",
                                color = Color.White.copy(alpha = 0.35f),
                                fontSize = 14.sp,
                                fontFamily = SFProDisplayFontFamily,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            textStyle = TextStyle(
                                color = Color.White,
                                fontSize = 14.sp,
                                fontFamily = SFProDisplayFontFamily
                            ),
                            cursorBrush = SolidColor(VioraNeonLime),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("weather_city_search_input")
                        )
                    }

                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { searchQuery = "" },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = Color.White.copy(alpha = 0.5f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    if (isSearchingOnline) {
                        Spacer(modifier = Modifier.width(6.dp))
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = VioraNeonLime,
                            strokeWidth = 2.dp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Auto-detect Location Option
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .clickable {
                        taskViewModel.setWeatherSettings(true, "", 0.0, 0.0)
                        onDismiss()
                    }
                    .testTag("weather_city_auto_detect_btn"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isAutoDetect) VioraNeonLime.copy(alpha = 0.15f) else Color(0xFF2C2C2E)
                ),
                border = if (isAutoDetect) CardDefaults.outlinedCardBorder().copy(brush = SolidColor(VioraNeonLime.copy(alpha = 0.4f))) else null
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isAutoDetect) VioraNeonLime else Color.White.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.MyLocation,
                                contentDescription = "Auto Detect",
                                tint = if (isAutoDetect) Color.Black else Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "Auto-detect Location",
                                color = if (isAutoDetect) VioraNeonLime else Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = SFProDisplayFontFamily
                            )
                            Text(
                                text = "Uses network IP and GPS automatically",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 12.sp,
                                fontFamily = SFProDisplayFontFamily
                            )
                        }
                    }

                    if (isAutoDetect) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(VioraNeonLime),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Sub-header for Cities List
            Text(
                text = if (searchQuery.isBlank()) "Popular Cities" else "Matching Cities (${displayedCities.size})",
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = SFProDisplayFontFamily,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
            )

            // Scrollable list of cities
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 380.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (displayedCities.isEmpty() && !isSearchingOnline) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.2f),
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "No cities found for \"$searchQuery\"",
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontSize = 14.sp,
                                    fontFamily = SFProDisplayFontFamily
                                )
                            }
                        }
                    }
                } else {
                    items(displayedCities, key = { "${it.name}_${it.country ?: ""}_${it.latitude}_${it.longitude}" }) { city ->
                        val isSelected = !isAutoDetect && (city.name.equals(manualCityVal, ignoreCase = true) || city.name.equals(weatherInfo.location, ignoreCase = true))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) VioraNeonLime.copy(alpha = 0.15f) else Color(0xFF242426))
                                .clickable {
                                    taskViewModel.setWeatherSettings(
                                        autoDetect = false,
                                        city = city.name,
                                        lat = city.latitude,
                                        lon = city.longitude
                                    )
                                    onDismiss()
                                }
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                                .testTag("weather_city_item_${city.name}"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = if (isSelected) VioraNeonLime else Color.White.copy(alpha = 0.4f),
                                    modifier = Modifier.size(18.dp)
                                )

                                Column {
                                    Text(
                                        text = city.name,
                                        color = if (isSelected) VioraNeonLime else Color.White,
                                        fontSize = 15.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontFamily = SFProDisplayFontFamily,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    val subtitle = listOfNotNull(city.admin1, city.country).joinToString(", ")
                                    if (subtitle.isNotBlank()) {
                                        Text(
                                            text = subtitle,
                                            color = Color.White.copy(alpha = 0.45f),
                                            fontSize = 12.sp,
                                            fontFamily = SFProDisplayFontFamily,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }

                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = VioraNeonLime,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
