package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SFProDisplayFontFamily
import com.example.ui.theme.VioraNeonLime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTeamBottomSheet(
    onDismiss: () -> Unit,
    onCreate: (String, String) -> Unit,
    initialTeamName: String = "",
    initialDeadline: String = "Daily",
    title: String = "Create Team"
) {
    var step by remember { mutableIntStateOf(1) }
    var teamName by remember { mutableStateOf(initialTeamName) }
    var deadline by remember { mutableStateOf(initialDeadline) }
    var customDays by remember { mutableIntStateOf(3) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = com.example.ui.utils.dynamicBottomSheetShape(sheetState = sheetState, defaultRadius = 28.dp),
        containerColor = Color(0xFF333333),
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp)
                .navigationBarsPadding()
                .imePadding()
        ) {
            // Header: Title and dots
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = if (step == 1) title else "Team Default\nDeadline",
                    color = Color.White,
                    fontSize = 34.sp,
                    fontFamily = SFProDisplayFontFamily,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 40.sp,
                    letterSpacing = (-0.5).sp
                )

                Row(
                    modifier = Modifier.padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (step >= 1) VioraNeonLime else Color.Black)
                    )
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (step >= 2) VioraNeonLime else Color.Black)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (step == 1) "By deleting this task, it will be permanently unavailable.\nIf you think you may need it later." else "Lists without deadlines use this deadline.",
                color = Color(0xFFCCCCCC),
                fontSize = 14.sp,
                fontFamily = SFProDisplayFontFamily,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (step == 1) {
                // Step 1: TextField
                OutlinedTextField(
                    value = teamName,
                    onValueChange = { teamName = it },
                    label = { Text("Team name", fontFamily = SFProDisplayFontFamily) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VioraNeonLime,
                        unfocusedBorderColor = VioraNeonLime,
                        focusedLabelColor = Color.White.copy(alpha = 0.5f),
                        unfocusedLabelColor = Color.White.copy(alpha = 0.5f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = VioraNeonLime,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth().height(64.dp),
                    shape = RoundedCornerShape(24.dp),
                    singleLine = true,
                    trailingIcon = {
                        if (teamName.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .border(1.dp, Color(0xFFAAAAAA), CircleShape)
                                    .clickable { teamName = "" },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "Clear",
                                    tint = Color(0xFFAAAAAA),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    },
                    textStyle = TextStyle(fontSize = 18.sp, fontFamily = SFProDisplayFontFamily)
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { if (teamName.isNotBlank()) step = 2 },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VioraNeonLime, contentColor = Color.Black),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(
                        text = "Next",
                        fontSize = 18.sp,
                        fontFamily = SFProDisplayFontFamily,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = "Cancel",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontFamily = SFProDisplayFontFamily,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                com.example.ui.components.DefaultDeadlineSelector(
                    selectedOption = deadline,
                    onOptionSelected = { deadline = it },
                    customDays = customDays,
                    onCustomDaysChanged = { customDays = it },
                    textColor = Color.White,
                    unselectedTextColor = Color.White,
                    borderColor = Color.White,
                    selectedBackgroundColor = Color(0xFF1E3300),
                    selectedItemTextColor = VioraNeonLime,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { onCreate(teamName, deadline) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VioraNeonLime, contentColor = Color.Black),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(
                        text = "Update",
                        fontSize = 18.sp,
                        fontFamily = SFProDisplayFontFamily,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = { onCreate(teamName, "Account Default") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = "Skip & use account default",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontFamily = SFProDisplayFontFamily,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
