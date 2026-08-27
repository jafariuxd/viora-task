package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.network.inspector.ApiLog
import com.example.network.inspector.ApiInspectorManager
import com.example.ui.theme.VioraNeonLime

@Composable
fun ApiInspectorModalOverlay() {
    val currentLog by ApiInspectorManager.currentModalLog.collectAsState()
    val isInspectorOpen by ApiInspectorManager.isInspectorOpen.collectAsState()
    val logs by ApiInspectorManager.logs.collectAsState()

    val logToShow = currentLog ?: logs.firstOrNull() ?: ApiLog(
        method = "INFO",
        url = "viora://network-inspector/no-logs",
        requestHeaders = emptyMap(),
        requestBody = "No network requests have been logged yet.",
        statusCode = 200,
        statusMessage = "Ready",
        responseBody = "Network requests will automatically be recorded when server operations are performed.",
        isPending = false
    )

    AnimatedVisibility(
        visible = isInspectorOpen || currentLog != null,
        enter = fadeIn() + slideInVertically { it / 2 },
        exit = fadeOut() + slideOutVertically { it / 2 }
    ) {
        ApiInspectorModal(
            log = logToShow,
            allLogs = logs,
            onDismiss = { ApiInspectorManager.dismissModal() },
            onSelectLog = { ApiInspectorManager.showLog(it) },
            onClearAll = { ApiInspectorManager.clearLogs() }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApiInspectorModal(
    log: ApiLog,
    allLogs: List<ApiLog>,
    onDismiss: () -> Unit,
    onSelectLog: (ApiLog) -> Unit,
    onClearAll: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Request & Response, 1: History
    var activeSubTab by remember { mutableIntStateOf(0) } // 0: Request, 1: Response
    val context = LocalContext.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f)
                .clip(RoundedCornerShape(24.dp)),
            color = Color(0xFF141416),
            tonalElevation = 8.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2C2C2E))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Top Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        MethodBadge(method = log.method)

                        Text(
                            text = log.url.substringAfter("http://").substringAfter("https://").substringAfter("/"),
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = { selectedTab = if (selectedTab == 0) 1 else 0 },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = "History",
                                tint = if (selectedTab == 1) VioraNeonLime else Color.Gray
                            )
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Status & Duration Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1C1C1E), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusBadge(log = log)

                    if (log.durationMs != null) {
                        Text(
                            text = "${log.durationMs} ms",
                            color = Color(0xFF8E8E93),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (selectedTab == 1) {
                    // History View
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Request History (${allLogs.size})",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            TextButton(onClick = onClearAll) {
                                Text("Clear All", color = Color(0xFFFF453A), fontSize = 13.sp)
                            }
                        }

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(allLogs) { item ->
                                HistoryItemCard(
                                    log = item,
                                    isSelected = item.id == log.id,
                                    onClick = {
                                        onSelectLog(item)
                                        selectedTab = 0
                                    }
                                )
                            }
                        }
                    }
                } else {
                    // Main Request / Response View Tabs
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1C1C1E), RoundedCornerShape(12.dp))
                            .padding(4.dp)
                    ) {
                        TabButton(
                            text = "Request",
                            isSelected = activeSubTab == 0,
                            modifier = Modifier.weight(1f),
                            onClick = { activeSubTab = 0 }
                        )
                        TabButton(
                            text = "Response",
                            isSelected = activeSubTab == 1,
                            modifier = Modifier.weight(1f),
                            onClick = { activeSubTab = 1 }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Content Box
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        if (activeSubTab == 0) {
                            RequestDetailsView(log = log, context = context)
                        } else {
                            ResponseDetailsView(log = log, context = context)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Bottom Dismiss Button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VioraNeonLime,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = "Dismiss",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
fun TabButton(
    text: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) VioraNeonLime else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.Black else Color.Gray,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 14.sp
        )
    }
}

@Composable
fun RequestDetailsView(log: ApiLog, context: Context) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SectionCard(title = "Full Endpoint URL", content = log.url, context = context)

        if (log.requestHeaders.isNotEmpty()) {
            val headersText = log.requestHeaders.entries.joinToString("\n") { "${it.key}: ${it.value}" }
            SectionCard(title = "Headers (${log.requestHeaders.size})", content = headersText, context = context)
        }

        SectionCard(
            title = "Request Body",
            content = log.requestBody ?: "(No Request Body)",
            context = context
        )
    }
}

@Composable
fun ResponseDetailsView(log: ApiLog, context: Context) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (log.isPending) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(color = VioraNeonLime)
                    Text("Waiting for response...", color = Color.Gray, fontSize = 14.sp)
                }
            }
        } else if (log.error != null) {
            SectionCard(
                title = "Network Error",
                content = log.error ?: "Unknown error",
                isError = true,
                context = context
            )
        } else {
            if (!log.responseHeaders.isNullOrEmpty()) {
                val headersText = log.responseHeaders!!.entries.joinToString("\n") { "${it.key}: ${it.value}" }
                SectionCard(title = "Response Headers (${log.responseHeaders!!.size})", content = headersText, context = context)
            }

            SectionCard(
                title = "Response Body",
                content = log.responseBody ?: "(Empty Response Body)",
                context = context
            )
        }
    }
}

@Composable
fun SectionCard(
    title: String,
    content: String,
    isError: Boolean = false,
    context: Context
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1C1C1E), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = if (isError) Color(0xFFFF453A) else VioraNeonLime,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )

            IconButton(
                onClick = {
                    copyToClipboard(context, title, content)
                },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy",
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        SelectionContainer {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0D0D0E), RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Text(
                    text = content,
                    color = if (isError) Color(0xFFFF8888) else Color(0xFFE5E5EA),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
fun MethodBadge(method: String) {
    val bgColor = when (method.uppercase()) {
        "GET" -> Color(0xFF34C759)
        "POST" -> Color(0xFF0A84FF)
        "PATCH" -> Color(0xFFFF9F0A)
        "DELETE" -> Color(0xFFFF453A)
        else -> Color.Gray
    }

    Box(
        modifier = Modifier
            .background(bgColor.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
            .border(1.dp, bgColor, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = method.uppercase(),
            color = bgColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
fun StatusBadge(log: ApiLog) {
    if (log.isPending) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(12.dp),
                color = Color(0xFFFFD60A),
                strokeWidth = 2.dp
            )
            Text(
                text = "Sending Request...",
                color = Color(0xFFFFD60A),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
    } else if (log.error != null) {
        Text(
            text = "Failed: ${log.error}",
            color = Color(0xFFFF453A),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    } else {
        val code = log.statusCode ?: 0
        val isSuccess = code in 200..299
        val badgeColor = if (isSuccess) Color(0xFF34C759) else Color(0xFFFF453A)

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(badgeColor, CircleShape)
            )
            Text(
                text = "$code ${log.statusMessage ?: ""}",
                color = badgeColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun HistoryItemCard(
    log: ApiLog,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) Color(0xFF2C2C2E) else Color(0xFF1C1C1E))
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            MethodBadge(method = log.method)

            Column {
                Text(
                    text = log.url.substringAfter("http://").substringAfter("https://").substringAfter("/"),
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (log.isPending) "Pending..." else "${log.statusCode ?: "Err"} • ${log.durationMs ?: 0}ms",
                    color = if (log.isPending) Color(0xFFFFD60A) else if ((log.statusCode ?: 0) in 200..299) Color(0xFF34C759) else Color(0xFFFF453A),
                    fontSize = 11.sp
                )
            }
        }
    }
}

private fun copyToClipboard(context: Context, label: String, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(label, text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
}
