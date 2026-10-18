package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SFProDisplayFontFamily
import com.example.ui.theme.VioraNeonLime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickTaskBottomSheet(
    onDismiss: () -> Unit,
    onTaskCreate: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var taskTitle by remember { mutableStateOf("") }
    val recentAddedTasks = remember { mutableStateListOf<String>() }

    val handleDismiss = {
        focusManager.clearFocus()
        keyboardController?.hide()
        onDismiss()
    }

    val submitTask = {
        val trimmed = taskTitle.trim()
        if (trimmed.isNotEmpty()) {
            onTaskCreate(trimmed)
            recentAddedTasks.add(0, trimmed)
            taskTitle = ""
        }
    }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(200)
        try {
            focusRequester.requestFocus()
            keyboardController?.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            focusManager.clearFocus()
            keyboardController?.hide()
        }
    }

    ModalBottomSheet(
        onDismissRequest = handleDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .width(36.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.2f))
            )
        },
        modifier = Modifier.testTag("quick_task_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Quick Task",
                    color = Color.Black.copy(alpha = 0.5f),
                    fontSize = 14.sp,
                    fontFamily = SFProDisplayFontFamily,
                    fontWeight = FontWeight.Medium
                )

                IconButton(
                    onClick = handleDismiss,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.06f))
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Close",
                        tint = Color.Black,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Text Field
            BasicTextField(
                value = taskTitle,
                onValueChange = { taskTitle = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .testTag("quick_task_input"),
                textStyle = TextStyle(
                    color = Color.Black,
                    fontSize = 26.sp,
                    lineHeight = 34.sp,
                    fontFamily = SFProDisplayFontFamily,
                    fontWeight = FontWeight.Normal
                ),
                cursorBrush = SolidColor(Color.Black),
                singleLine = false,
                minLines = 1,
                maxLines = 5,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { submitTask() }),
                decorationBox = { innerTextField ->
                    if (taskTitle.isEmpty()) {
                        Text(
                            text = "What's in your mind?",
                            color = Color.Black.copy(alpha = 0.35f),
                            fontSize = 26.sp,
                            lineHeight = 34.sp,
                            fontFamily = SFProDisplayFontFamily,
                            fontWeight = FontWeight.Normal
                        )
                    }
                    innerTextField()
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Add Task Button
            Button(
                onClick = { submitTask() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("quick_task_submit"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VioraNeonLime,
                    contentColor = Color.Black
                )
            ) {
                Text(
                    text = "Add Task",
                    fontSize = 16.sp,
                    fontFamily = SFProDisplayFontFamily,
                    fontWeight = FontWeight.Bold
                )
            }

            // Continuous recent tasks feed
            if (recentAddedTasks.isNotEmpty()) {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Added (${recentAddedTasks.size})",
                    color = Color.Black.copy(alpha = 0.5f),
                    fontSize = 13.sp,
                    fontFamily = SFProDisplayFontFamily,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    recentAddedTasks.take(3).forEach { title ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color.Black.copy(alpha = 0.05f))
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = null,
                                tint = Color(0xFF007AFF),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = title,
                                color = Color.Black,
                                fontSize = 15.sp,
                                fontFamily = SFProDisplayFontFamily,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}
