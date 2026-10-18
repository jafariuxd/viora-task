package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TeamRole
import com.example.model.User
import com.example.ui.theme.SFProDisplayFontFamily
import com.example.ui.theme.VioraNeonLime
import com.example.ui.utils.dynamicBottomSheetShape

/**
 * Visual badge for team member roles in Viora Design System.
 */
@Composable
fun TeamRoleBadge(
    role: TeamRole,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val (bgColor, textColor, borderColor, icon) = when (role) {
        TeamRole.OWNER -> Quadruple(
            VioraNeonLime.copy(alpha = 0.15f),
            VioraNeonLime,
            VioraNeonLime.copy(alpha = 0.45f),
            Icons.Rounded.Star
        )
        TeamRole.ADMIN -> Quadruple(
            Color(0xFF38BDF8).copy(alpha = 0.15f),
            Color(0xFF38BDF8),
            Color(0xFF38BDF8).copy(alpha = 0.45f),
            Icons.Rounded.Shield
        )
        TeamRole.MEMBER -> Quadruple(
            Color(0xFF28282A),
            Color(0xFFAAAAAA),
            Color.White.copy(alpha = 0.10f),
            Icons.Rounded.Person
        )
    }

    val baseModifier = if (onClick != null) {
        modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
    } else {
        modifier.clip(RoundedCornerShape(10.dp))
    }

    Box(
        modifier = baseModifier
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(10.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = role.title,
                color = textColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = SFProDisplayFontFamily
            )
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

/**
 * Team Members Management Sheet showing all members, role badges, and management options.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamMembersManagementBottomSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    teamName: String,
    teamMembers: List<User>,
    currentUsername: String,
    currentUserRole: TeamRole,
    getMemberRole: (String) -> TeamRole,
    onInviteClick: () -> Unit,
    onMemberClick: (User) -> Unit
) {
    if (!visible) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = dynamicBottomSheetShape(sheetState = sheetState, defaultRadius = 28.dp),
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
                .navigationBarsPadding()
        ) {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Team Members",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = SFProDisplayFontFamily,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "${teamMembers.size} members in $teamName",
                        color = Color(0xFFAAAAAA),
                        fontSize = 13.sp,
                        fontFamily = SFProDisplayFontFamily
                    )
                }

                if (currentUserRole.canManageMembers) {
                    Button(
                        onClick = onInviteClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VioraNeonLime,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                        modifier = Modifier.height(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PersonAdd,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Invite",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = SFProDisplayFontFamily,
                            color = Color.Black
                        )
                    }
                }
            }

            // Permissions Info Note if Member
            if (!currentUserRole.canManageMembers) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF262628))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Info,
                            contentDescription = null,
                            tint = Color(0xFFAAAAAA),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "You are a Member. Only Owners and Admins can manage members and roles.",
                            color = Color(0xFFAAAAAA),
                            fontSize = 12.sp,
                            fontFamily = SFProDisplayFontFamily,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // Member list
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(teamMembers, key = { it.id.ifEmpty { it.username } }) { member ->
                    val role = getMemberRole(member.username)
                    val isSelf = member.username.equals(currentUsername, ignoreCase = true)
                    val canEditThisMember = currentUserRole.canManageMembers && (
                        currentUserRole == TeamRole.OWNER || (currentUserRole == TeamRole.ADMIN && role == TeamRole.MEMBER)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFF242426))
                            .clickable(enabled = canEditThisMember) {
                                onMemberClick(member)
                            }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            UserAvatar(
                                userId = member.username,
                                avatarUri = member.avatarUrl,
                                size = 42.dp,
                                modifier = Modifier.border(1.5.dp, Color(0xFF333333), CircleShape)
                            )
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = member.name.ifBlank { member.username },
                                        color = Color.White,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        fontFamily = SFProDisplayFontFamily,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (isSelf) {
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            text = "(You)",
                                            color = Color(0xFFAAAAAA),
                                            fontSize = 12.sp,
                                            fontFamily = SFProDisplayFontFamily
                                        )
                                    }
                                }
                                Text(
                                    text = "@${member.username}",
                                    color = Color(0xFFAAAAAA),
                                    fontSize = 13.sp,
                                    fontFamily = SFProDisplayFontFamily
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TeamRoleBadge(role = role)
                            if (canEditThisMember) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                    contentDescription = "Edit Role",
                                    tint = Color.Gray,
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

/**
 * Interactive Role Selector Sheet allowing Owners and Admins to modify roles or remove members.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleSelectorBottomSheet(
    visible: Boolean,
    targetUser: User?,
    teamName: String,
    currentRole: TeamRole,
    currentUserRole: TeamRole,
    currentUsername: String,
    onDismiss: () -> Unit,
    onRoleSelected: (TeamRole) -> Unit,
    onRemoveMember: () -> Unit
) {
    if (!visible || targetUser == null) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedRole by remember(currentRole) { mutableStateOf(currentRole) }
    var showRemoveConfirm by remember { mutableStateOf(false) }

    val isSelf = targetUser.username.equals(currentUsername, ignoreCase = true)
    val isOwner = currentRole == TeamRole.OWNER

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = dynamicBottomSheetShape(sheetState = sheetState, defaultRadius = 28.dp),
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
                .navigationBarsPadding()
        ) {
            // Target User Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                UserAvatar(
                    userId = targetUser.username,
                    avatarUri = targetUser.avatarUrl,
                    size = 50.dp,
                    modifier = Modifier.border(2.dp, VioraNeonLime.copy(alpha = 0.5f), CircleShape)
                )
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(
                        text = targetUser.name.ifBlank { targetUser.username },
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = SFProDisplayFontFamily
                    )
                    Text(
                        text = "@${targetUser.username}",
                        color = Color(0xFFAAAAAA),
                        fontSize = 13.sp,
                        fontFamily = SFProDisplayFontFamily
                    )
                }
            }

            Text(
                text = "Select Team Role",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = SFProDisplayFontFamily,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Role Option: Admin
            RoleOptionCard(
                title = "Admin",
                description = "Can invite and remove members, assign roles, and edit team settings.",
                icon = Icons.Rounded.Shield,
                accentColor = Color(0xFF38BDF8),
                isSelected = selectedRole == TeamRole.ADMIN,
                enabled = !isOwner || currentUserRole == TeamRole.OWNER,
                onClick = { selectedRole = TeamRole.ADMIN }
            )

            Spacer(Modifier.height(10.dp))

            // Role Option: Member
            RoleOptionCard(
                title = "Member",
                description = "Can view team lists and collaborate on tasks.",
                icon = Icons.Rounded.Person,
                accentColor = Color.White,
                isSelected = selectedRole == TeamRole.MEMBER,
                enabled = !isOwner || currentUserRole == TeamRole.OWNER,
                onClick = { selectedRole = TeamRole.MEMBER }
            )

            // Transfer Ownership option (Owner only)
            if (currentUserRole == TeamRole.OWNER && !isSelf && !isOwner) {
                Spacer(Modifier.height(10.dp))
                RoleOptionCard(
                    title = "Transfer Ownership",
                    description = "Make this user the Team Owner. You will remain an Admin.",
                    icon = Icons.Rounded.Star,
                    accentColor = VioraNeonLime,
                    isSelected = selectedRole == TeamRole.OWNER,
                    enabled = true,
                    onClick = { selectedRole = TeamRole.OWNER }
                )
            }

            Spacer(Modifier.height(20.dp))

            // Update Role Button
            Button(
                onClick = {
                    onRoleSelected(selectedRole)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = VioraNeonLime,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    text = if (selectedRole == TeamRole.OWNER) "Transfer Ownership" else "Save Role",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = SFProDisplayFontFamily,
                    color = Color.Black
                )
            }

            // Remove Member Option
            if (!isSelf && !isOwner && currentUserRole.canManageMembers) {
                Spacer(Modifier.height(12.dp))

                if (!showRemoveConfirm) {
                    OutlinedButton(
                        onClick = { showRemoveConfirm = true },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFFF453A)
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF453A).copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PersonRemove,
                            contentDescription = null,
                            tint = Color(0xFFFF453A),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Remove from Team",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = SFProDisplayFontFamily,
                            color = Color(0xFFFF453A)
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF2A1515))
                            .border(1.dp, Color(0xFFFF453A).copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Remove @${targetUser.username} from $teamName?",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = SFProDisplayFontFamily
                        )
                        Spacer(Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { showRemoveConfirm = false },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF333333),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                            ) {
                                Text("Cancel", fontFamily = SFProDisplayFontFamily, fontSize = 13.sp)
                            }
                            Button(
                                onClick = {
                                    onRemoveMember()
                                    onDismiss()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFFF453A),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                            ) {
                                Text("Confirm Remove", fontFamily = SFProDisplayFontFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun RoleOptionCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    isSelected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) accentColor else Color.White.copy(alpha = 0.08f)
    val bgColor = if (isSelected) accentColor.copy(alpha = 0.10f) else Color(0xFF242426)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(bgColor)
            .border(1.5.dp, borderColor, RoundedCornerShape(18.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(accentColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = if (isSelected) Color.White else Color(0xFFE0E0E0),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = SFProDisplayFontFamily
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = description,
                color = Color(0xFFAAAAAA),
                fontSize = 12.sp,
                fontFamily = SFProDisplayFontFamily,
                lineHeight = 16.sp
            )
        }

        Spacer(Modifier.width(10.dp))

        RadioButton(
            selected = isSelected,
            onClick = if (enabled) onClick else null,
            colors = RadioButtonDefaults.colors(
                selectedColor = accentColor,
                unselectedColor = Color(0xFF555555)
            )
        )
    }
}
