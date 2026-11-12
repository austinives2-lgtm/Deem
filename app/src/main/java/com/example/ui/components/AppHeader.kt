package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.UserModel
import com.example.ui.PanelMode
import com.example.ui.theme.SevaGoldAccent
import com.example.ui.theme.SevaGreenPrimary

@Composable
fun AppHeader(
    currentUser: UserModel?,
    panelMode: PanelMode,
    unreadNotificationsCount: Int,
    onPanelToggle: () -> Unit,
    onNotificationsClick: () -> Unit,
    onMessagesClick: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 3.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Logo & Org Info
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.org_logo),
                        contentDescription = "বাংলাদেশ সেবামূলক সংগঠন লোগো",
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .border(1.dp, SevaGreenPrimary.copy(alpha = 0.2f), RoundedCornerShape(10.dp)),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = "বাংলাদেশ সেবামূলক সংগঠন",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = SevaGreenPrimary,
                                fontSize = 15.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "মানুষের পাশে, মানবতার জন্য",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            maxLines = 1
                        )
                    }
                }

                // Action Buttons
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Admin Toggle Button if admin
                    if (currentUser?.isAdmin() == true) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (panelMode == PanelMode.ADMIN) SevaGoldAccent else SevaGreenPrimary.copy(alpha = 0.1f),
                            modifier = Modifier
                                .clickable { onPanelToggle() }
                                .testTag("panel_toggle_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (panelMode == PanelMode.ADMIN) Icons.Filled.AdminPanelSettings else Icons.Outlined.AdminPanelSettings,
                                    contentDescription = "প্যানেল পরিবর্তন",
                                    tint = if (panelMode == PanelMode.ADMIN) Color.Black else SevaGreenPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (panelMode == PanelMode.ADMIN) "অ্যাডমিন" else "ইউজার",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (panelMode == PanelMode.ADMIN) Color.Black else SevaGreenPrimary
                                    )
                                )
                            }
                        }
                    }

                    // Messages Icon
                    IconButton(
                        onClick = onMessagesClick,
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("messages_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Chat,
                            contentDescription = "মেসেজ",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Notification Icon with Badge
                    IconButton(
                        onClick = onNotificationsClick,
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("notifications_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (unreadNotificationsCount > 0) {
                                    Badge(containerColor = MaterialTheme.colorScheme.error) {
                                        Text("$unreadNotificationsCount")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Notifications,
                                contentDescription = "নোটিফিকেশন",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Profile Avatar
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(SevaGreenPrimary.copy(alpha = 0.15f))
                            .border(1.dp, SevaGreenPrimary, CircleShape)
                            .clickable { onProfileClick() }
                            .testTag("header_profile_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!currentUser?.photoURL.isNullOrBlank()) {
                            AsyncImage(
                                model = currentUser?.photoURL,
                                contentDescription = "প্রোফাইল ছবি",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = "প্রোফাইল",
                                tint = SevaGreenPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
