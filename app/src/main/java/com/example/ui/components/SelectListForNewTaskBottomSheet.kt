package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.PushPin
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TaskList
import com.example.ui.theme.SFProDisplayFontFamily
import com.example.ui.theme.VioraNeonLime

private data class TeamGroupedLists(
    val teamName: String,
    val lists: List<TaskList>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectListForNewTaskBottomSheet(
    allLists: List<TaskList>,
    allTeams: List<String>,
    personalListId: String?,
    getTeamName: (String) -> String,
    getTeamIdByName: ((String) -> String?)? = null,
    onDismiss: () -> Unit,
    onQuickTaskSelected: (() -> Unit)? = null,
    onListSelected: (TaskList) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
    val keyboardController = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current

    DisposableEffect(Unit) {
        onDispose {
            focusManager.clearFocus()
            keyboardController?.hide()
        }
    }

    val handleDismiss = {
        focusManager.clearFocus()
        keyboardController?.hide()
        onDismiss()
    }

    val handleSelectList: (TaskList) -> Unit = { list ->
        focusManager.clearFocus()
        keyboardController?.hide()
        onListSelected(list)
    }

    var searchQuery by remember { mutableStateOf("") }

    // Active non-archived lists
    val activeLists = remember(allLists) {
        allLists.filter { !it.isArchived && it.name != "Unplanned Tasks" && it.id != "unplanned_tasks" }
    }

    // Helper to determine which team a list belongs to
    val getParentTeam = remember(personalListId, allTeams, getTeamName, getTeamIdByName) {
        { list: TaskList ->
            if (list.isPersonal || list.id == personalListId || list.name.equals("My Tasks", ignoreCase = true)) {
                "Personal Space"
            } else if (list.teamId.isBlank() || list.teamId == "personal_space") {
                "Personal Space"
            } else {
                // 1. Try resolve team name via getTeamName(teamId)
                val resolvedName = getTeamName(list.teamId)
                if (resolvedName.isNotBlank() && 
                    resolvedName != "Personal Space" && 
                    resolvedName != "Personal" && 
                    resolvedName != "Unknown Team"
                ) {
                    resolvedName
                } else {
                    // 2. Try match teamId via getTeamIdByName
                    var matchedTeam: String? = null
                    if (getTeamIdByName != null) {
                        for (team in allTeams) {
                            if (team != "All Lists" && team != "Personal" && team != "Personal Space") {
                                val realId = getTeamIdByName(team)
                                if (realId != null && (realId == list.teamId || team == list.teamId)) {
                                    matchedTeam = team
                                    break
                                }
                            }
                        }
                    }
                    if (matchedTeam != null) {
                        matchedTeam
                    } else {
                        // 3. Direct match against allTeams
                        val directMatch = allTeams.find { team ->
                            team != "All Lists" && team != "Personal" && team != "Personal Space" &&
                            (team.equals(list.teamId, ignoreCase = true) ||
                             team.lowercase().replace(" ", "_") == list.teamId.lowercase().replace(" ", "_"))
                        }
                        directMatch ?: "Personal Space"
                    }
                }
            }
        }
    }

    // Desired team order: "Personal Space" first, then all valid teams in allTeams
    val orderedTeamNames = remember(allTeams) {
        val nonPersonal = allTeams.filter { it != "All Lists" && it != "Personal" && it != "Personal Space" }
        listOf("Personal Space") + nonPersonal
    }

    // Group all active lists by parent team
    val listsByTeam = remember(activeLists, getParentTeam) {
        activeLists.groupBy { getParentTeam(it) }
    }

    // Build the ordered grouped teams list
    val groupedTeams = remember(listsByTeam, orderedTeamNames, searchQuery) {
        val result = mutableListOf<TeamGroupedLists>()

        // 1. Ordered known teams
        orderedTeamNames.forEach { teamName ->
            val rawLists = listsByTeam[teamName] ?: emptyList()
            val processedLists = if (teamName == "Personal Space") {
                val distinct = rawLists.distinctBy { if (it.name.equals("My Tasks", ignoreCase = true)) "My Tasks" else it.id }
                val pinned = distinct.filter { it.isPersonal || it.id == personalListId || it.name.equals("My Tasks", ignoreCase = true) }
                val others = distinct.filter { it !in pinned }
                pinned + others
            } else {
                rawLists.distinctBy { it.id }
            }

            val filteredLists = if (searchQuery.isBlank()) {
                processedLists
            } else {
                processedLists.filter {
                    it.name.contains(searchQuery, ignoreCase = true) || teamName.contains(searchQuery, ignoreCase = true)
                }
            }

            if (filteredLists.isNotEmpty()) {
                result.add(TeamGroupedLists(teamName, filteredLists))
            }
        }

        // 2. Any additional teams present in listsByTeam
        listsByTeam.keys.filter { it !in orderedTeamNames }.forEach { teamName ->
            val rawLists = (listsByTeam[teamName] ?: emptyList()).distinctBy { it.id }
            val filteredLists = if (searchQuery.isBlank()) {
                rawLists
            } else {
                rawLists.filter {
                    it.name.contains(searchQuery, ignoreCase = true) || teamName.contains(searchQuery, ignoreCase = true)
                }
            }
            if (filteredLists.isNotEmpty()) {
                result.add(TeamGroupedLists(teamName, filteredLists))
            }
        }

        result
    }

    ModalBottomSheet(
        onDismissRequest = handleDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF000000),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .width(36.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.25f))
            )
        },
        modifier = Modifier.testTag("select_list_for_new_task_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(top = 4.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Select List",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontFamily = SFProDisplayFontFamily,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.3).sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Choose where to create your new task",
                        color = Color(0xFFAAAAAA),
                        fontSize = 14.sp,
                        fontFamily = SFProDisplayFontFamily
                    )
                }

                IconButton(
                    onClick = handleDismiss,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f))
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Search Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(48.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF1C1C1E))
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = null,
                    tint = Color(0xFF8E8E93),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                BasicTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.weight(1f),
                    textStyle = TextStyle(
                        color = Color.White,
                        fontSize = 15.sp,
                        fontFamily = SFProDisplayFontFamily
                    ),
                    cursorBrush = SolidColor(VioraNeonLime),
                    singleLine = true,
                    decorationBox = { innerTextField ->
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = "Search lists or spaces...",
                                color = Color(0xFF8E8E93),
                                fontSize = 15.sp,
                                fontFamily = SFProDisplayFontFamily
                            )
                        }
                        innerTextField()
                    }
                )
                if (searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = { searchQuery = "" },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Clear search",
                            tint = Color(0xFF8E8E93),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Lists LazyColumn grouped by Teams
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .heightIn(max = 480.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
            ) {
                if (searchQuery.isBlank() || "quick task".contains(searchQuery, ignoreCase = true)) {
                    item {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 2.dp)
                                .clickable {
                                    handleDismiss()
                                    onQuickTaskSelected?.invoke()
                                },
                            shape = RoundedCornerShape(24.dp),
                            color = Color(0xFF1C1C1E)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 18.dp)
                                    .testTag("select_quick_task_option"),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Bolt,
                                        contentDescription = null,
                                        tint = VioraNeonLime,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))

                                    Text(
                                        text = "Quick Task",
                                        color = Color.White,
                                        fontFamily = SFProDisplayFontFamily,
                                        fontSize = 19.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = Color(0xFF666666),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
                if (groupedTeams.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No matching lists found",
                                color = Color(0xFF8E8E93),
                                fontSize = 15.sp,
                                fontFamily = SFProDisplayFontFamily
                            )
                        }
                    }
                } else {
                    items(groupedTeams, key = { it.teamName }) { group ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 2.dp)
                        ) {
                            // Team Header Row above the card
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 24.dp, end = 16.dp, top = 4.dp, bottom = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = group.teamName,
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontFamily = SFProDisplayFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = (-0.3).sp
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Gray Card containing all child lists (no border, Viora dark gray card)
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(24.dp),
                                color = Color(0xFF1C1C1E)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    group.lists.forEachIndexed { index, list ->
                                        val isMyTasks = list.name.equals("My Tasks", ignoreCase = true) || list.isPersonal || list.id == personalListId
                                        if (index > 0) {
                                            HorizontalDivider(
                                                color = Color.White.copy(alpha = 0.05f),
                                                modifier = Modifier.padding(horizontal = 24.dp)
                                            )
                                        }

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { handleSelectList(list) }
                                                .padding(horizontal = 24.dp, vertical = 18.dp)
                                                .testTag("select_list_item_${list.name}"),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Rounded.List,
                                                    contentDescription = null,
                                                    tint = Color(0xFF8E8E93),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(modifier = Modifier.width(10.dp))

                                                // List title (larger, medium font weight)
                                                Text(
                                                    text = list.name,
                                                    color = Color.White,
                                                    fontFamily = SFProDisplayFontFamily,
                                                    fontSize = 19.sp,
                                                    fontWeight = FontWeight.Medium
                                                )

                                                if (isMyTasks) {
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Icon(
                                                        imageVector = Icons.Rounded.PushPin,
                                                        contentDescription = "Pinned",
                                                        tint = VioraNeonLime,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }

                                            Icon(
                                                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                                contentDescription = null,
                                                tint = Color(0xFF666666),
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
        }
    }
}
