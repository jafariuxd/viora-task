package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SFProDisplayFontFamily
import com.example.ui.theme.VioraNeonLime
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Context descriptor for creating invitations in Viora.
 * Follows the product principle: "Don't invite people to Viora. Invite them to do something on Viora."
 */
sealed class InviteContext(
    val title: String,
    val subtitle: String,
    val actionVerb: String,
    val shareMessage: String
) {
    data class TaskAssignment(
        val taskTitle: String,
        val taskId: String,
        val client: String? = null,
        val daysLeft: Int? = null,
        val status: String? = null,
        val description: String? = null
    ) : InviteContext(
        title = "Assign Task",
        subtitle = "Invite someone to collaborate directly on this task with you.",
        actionVerb = "Assign & Invite",
        shareMessage = "Hey! I'd like to assign you to \"$taskTitle\" on Viora. Open the task here: ${com.example.util.VioraDeepLinkHandler.createTaskUrl(taskId = taskId, title = taskTitle, client = client, daysLeft = daysLeft, status = status, description = description)}"
    )

    data class TeamMembership(val teamName: String, val teamId: String) : InviteContext(
        title = "Invite to Team",
        subtitle = "Collaborate with teammates on shared lists and deadlines in \"$teamName\".",
        actionVerb = "Invite to Team",
        shareMessage = "Join our team \"$teamName\" on Viora to collaborate on tasks and deadlines: ${com.example.util.VioraDeepLinkHandler.createTeamInviteUrl(teamId = teamId, teamName = teamName)}"
    )

    data class GeneralCollaboration(val inviterName: String) : InviteContext(
        title = "Invite Collaborator",
        subtitle = "Work together seamlessly on client deliverables and deadlines.",
        actionVerb = "Share Invite",
        shareMessage = "Hey! Let's collaborate on Viora to track projects and deadlines together: https://viora.group/invite?by=${inviterName.lowercase().replace(" ", "_")}"
    )

    val inviteUrl: String
        get() = when (this) {
            is TaskAssignment -> com.example.util.VioraDeepLinkHandler.createTaskUrl(
                taskId = taskId,
                title = taskTitle,
                client = client,
                daysLeft = daysLeft,
                status = status,
                description = description
            )
            is TeamMembership -> com.example.util.VioraDeepLinkHandler.createTeamInviteUrl(teamId, teamName)
            is GeneralCollaboration -> "${com.example.util.VioraDeepLinkHandler.BASE_URL}/invite?by=${inviterName.lowercase().replace(" ", "_")}"
        }
}

/**
 * Unified, production-quality InviteBottomSheet adhering strictly to Viora's design system:
 * - Immersive dark background (Color(0xFF1E1E1E))
 * - Rounded corners (28.dp)
 * - Neon Lime accent (#B4FF00) for primary CTA
 * - Pure link-based sharing (native Android Share sheet + 1-tap Copy with haptic-like visual feedback)
 * - Clear contextual UX writing tailored to the 5 entry points
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InviteBottomSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    inviteContext: InviteContext,
    onInviteSent: (() -> Unit)? = null
) {
    if (!visible) return

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isCopied by remember { mutableStateOf(false) }

    LaunchedEffect(visible) {
        if (visible) isCopied = false
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = com.example.ui.utils.dynamicBottomSheetShape(sheetState = sheetState, defaultRadius = 28.dp),
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp, top = 8.dp)
                .navigationBarsPadding()
                .testTag("invite_bottom_sheet")
        ) {
            // Title
            Text(
                text = inviteContext.title,
                color = Color.White,
                fontSize = 24.sp,
                fontFamily = SFProDisplayFontFamily,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Subtitle
            Text(
                text = inviteContext.subtitle,
                color = Color(0xFFAAAAAA),
                fontSize = 14.sp,
                fontFamily = SFProDisplayFontFamily,
                fontWeight = FontWeight.Normal,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Invite link container with 1-tap copy
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF2C2C2E),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Viora Invite Link", inviteContext.inviteUrl)
                        clipboard.setPrimaryClip(clip)
                        isCopied = true
                        onInviteSent?.invoke()
                        coroutineScope.launch {
                            delay(2200)
                            isCopied = false
                        }
                    }
                    .testTag("invite_copy_link_box")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Link,
                            contentDescription = null,
                            tint = if (isCopied) VioraNeonLime else Color(0xFF8E8E93),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = inviteContext.inviteUrl,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontFamily = SFProDisplayFontFamily,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Copy Icon Button
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Viora Invite Link", inviteContext.inviteUrl)
                            clipboard.setPrimaryClip(clip)
                            isCopied = true
                            onInviteSent?.invoke()
                            coroutineScope.launch {
                                delay(2200)
                                isCopied = false
                            }
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (isCopied) VioraNeonLime.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.1f))
                    ) {
                        Icon(
                            imageVector = if (isCopied) Icons.Rounded.Check else Icons.Rounded.ContentCopy,
                            contentDescription = if (isCopied) "Copied" else "Copy Link",
                            tint = if (isCopied) VioraNeonLime else Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Primary Action: Native Share Sheet
            Button(
                onClick = {
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, inviteContext.shareMessage)
                        type = "text/plain"
                    }
                    val shareIntent = Intent.createChooser(sendIntent, inviteContext.title)
                    context.startActivity(shareIntent)
                    onInviteSent?.invoke()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = VioraNeonLime,
                    contentColor = Color.Black
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("invite_share_button"),
                shape = RoundedCornerShape(28.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Share,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Share Invite Link",
                        fontSize = 16.sp,
                        fontFamily = SFProDisplayFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Calm secondary note
            Text(
                text = "Anyone with this link can join and collaborate without an existing account.",
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 12.sp,
                fontFamily = SFProDisplayFontFamily,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
