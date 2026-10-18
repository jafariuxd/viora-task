package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SFProDisplayFontFamily
import com.example.ui.theme.VioraNeonLime
import com.example.model.User
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssigneePickerBottomSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    initialAssignees: List<String>,
    allTeamMembers: List<User>,
    currentUsername: String,
    searchPlaceholder: String = "Search users to assign",
    inviteUrl: String? = null,
    onInviteClick: (() -> Unit)? = null,
    onSubmit: (List<String>) -> Unit
) {
    if (visible) {
        val sheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = false)
        ModalBottomSheet(
            sheetState = sheetState,
            shape = com.example.ui.utils.dynamicBottomSheetShape(sheetState, 28.dp),
            onDismissRequest = onDismiss,
            containerColor = Color(0xFF1C1C1E),
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(top = 12.dp, bottom = 8.dp)
                        .width(36.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.25f))
                )
            }
        ) {
            AssigneePickerContent(
                initialAssignees = initialAssignees,
                allTeamMembers = allTeamMembers,
                currentUsername = currentUsername,
                searchPlaceholder = searchPlaceholder,
                inviteUrl = inviteUrl,
                onInviteClick = onInviteClick,
                onSubmit = {
                    onSubmit(it)
                    onDismiss()
                }
            )
        }
    }
}

@Composable
fun AssigneePickerContent(
    initialAssignees: List<String>,
    allTeamMembers: List<User>,
    currentUsername: String,
    searchPlaceholder: String = "Search users to assign",
    inviteUrl: String? = null,
    onInviteClick: (() -> Unit)? = null,
    onSubmit: (List<String>) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val cleanInitial = remember(initialAssignees) {
        initialAssignees.map { it.removePrefix("@") }.toSet()
    }
    var selectedAssignees by remember(cleanInitial) { mutableStateOf(cleanInitial) }
    var isCopied by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val effectiveInviteLink = remember(inviteUrl) {
        inviteUrl ?: "https://viora.group/invite?ref=assignee"
    }
    
    // Determine the users to show based on search
    val filteredMembers = remember(allTeamMembers, searchQuery, currentUsername) {
        val all = allTeamMembers
        
        // Sort so that current user is always first
        val sorted = all.sortedBy { 
            if (it.username.equals(currentUsername, ignoreCase = true) || it.name.equals(currentUsername, ignoreCase = true)) 0 else 1 
        }
        
        if (searchQuery.isBlank()) {
            sorted
        } else {
            sorted.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.username.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    val hasChanges = initialAssignees.toSet() != selectedAssignees

    Box(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = if (hasChanges) 92.dp else 16.dp) // space for sticky button
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Search bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(26.dp))
                        .background(Color(0xFF2C2C2E))
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
                        textStyle = TextStyle(
                            color = Color.White,
                            fontSize = 16.sp,
                            fontFamily = SFProDisplayFontFamily
                        ),
                        cursorBrush = SolidColor(VioraNeonLime),
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        decorationBox = { innerTextField ->
                            Box {
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = searchPlaceholder,
                                        color = Color(0xFF8E8E93),
                                        fontSize = 16.sp,
                                        fontFamily = SFProDisplayFontFamily
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Users grid (3 equal columns)
                if (filteredMembers.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No members found",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = SFProDisplayFontFamily
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Can't find who you're looking for? Invite them to Viora.",
                            color = Color(0xFFAAAAAA),
                            fontSize = 14.sp,
                            fontFamily = SFProDisplayFontFamily
                        )
                        if (onInviteClick != null) {
                            Spacer(modifier = Modifier.height(18.dp))
                            Button(
                                onClick = onInviteClick,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = VioraNeonLime,
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.height(44.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = Color.Black
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Invite Collaborator",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = SFProDisplayFontFamily
                                )
                            }
                        }
                    }
                } else {
                    filteredMembers.chunked(3).forEach { rowUsers ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        for (i in 0 until 3) {
                            if (i < rowUsers.size) {
                                val user = rowUsers[i]
                                val userKey = user.username.removePrefix("@")
                                val isSelected = selectedAssignees.contains(userKey) || selectedAssignees.contains(user.username)
                                val isCurrentUser = user.username.equals(currentUsername, ignoreCase = true) || user.name.equals(currentUsername, ignoreCase = true)
                                val displayName = if (isCurrentUser) "You" else user.name.ifBlank { user.username }

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) {
                                            if (isSelected) {
                                                selectedAssignees = selectedAssignees - userKey
                                            } else {
                                                selectedAssignees = selectedAssignees + userKey
                                            }
                                        }
                                ) {
                                    Box(
                                        contentAlignment = Alignment.BottomEnd,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(76.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF636366))
                                        ) {
                                            val resolvedAvatar = user.avatarUrl 
                                                ?: com.example.util.UserAvatarRegistry.getAvatarUrl(user.username)
                                                ?: com.example.util.UserAvatarRegistry.getAvatarUrl(user.id)
                                            UserAvatar(
                                                userId = user.username,
                                                avatarUri = resolvedAvatar,
                                                size = 76.dp
                                            )
                                        }
                                        if (isSelected) {
                                            Box(
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .clip(CircleShape)
                                                    .background(VioraNeonLime)
                                                    .border(2.dp, Color(0xFF1C1C1C), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = Color.Black,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                    }
                                    MarqueeText(
                                        text = displayName,
                                        modifier = Modifier.width(76.dp)
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(28.dp))
                }

                // Copy Link Outlined / Tonal Button (replacing "Can't find someone?" prompt)
                OutlinedButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                        val clip = ClipData.newPlainText("Viora Invite Link", effectiveInviteLink)
                        clipboard?.setPrimaryClip(clip)
                        Toast.makeText(context, "Invite link copied to clipboard", Toast.LENGTH_SHORT).show()
                        isCopied = true
                        coroutineScope.launch {
                            delay(2200)
                            isCopied = false
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(24.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isCopied) VioraNeonLime else Color.White.copy(alpha = 0.15f)
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (isCopied) VioraNeonLime.copy(alpha = 0.12f) else Color(0xFF262628),
                        contentColor = if (isCopied) VioraNeonLime else Color.White
                    )
                ) {
                    Icon(
                        imageVector = if (isCopied) Icons.Rounded.Check else Icons.Rounded.Link,
                        contentDescription = null,
                        tint = if (isCopied) VioraNeonLime else Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (isCopied) "Link Copied!" else "Copy Invite Link",
                        fontSize = 16.sp,
                        fontFamily = SFProDisplayFontFamily,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
            }
        }

        // Sticky Submit button container
        if (hasChanges) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color(0xFF1C1C1C)) // To cover content behind
                    .navigationBarsPadding()
                    .height(92.dp)
                    .padding(horizontal = 24.dp, vertical = 18.dp),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = { onSubmit(selectedAssignees.toList()) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VioraNeonLime,
                        contentColor = Color.Black
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text(
                        text = "Submit",
                        fontSize = 17.sp,
                        fontFamily = SFProDisplayFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun MarqueeText(
    text: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 15.sp,
            fontFamily = SFProDisplayFontFamily,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Clip,
            modifier = Modifier
                .basicMarquee()
                .padding(horizontal = 2.dp)
        )
    }
}
