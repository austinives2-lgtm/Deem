package com.example.ui.screens.notices

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NoticeItem
import com.example.ui.MainViewModel
import com.example.ui.components.EmptyStateView
import com.example.ui.components.NeumorphicCard
import com.example.ui.components.SevaButton
import com.example.ui.components.StatusBadge
import com.example.ui.theme.SevaBloodRed
import com.example.ui.theme.SevaGreenPrimary

@Composable
fun NoticesScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val notices by viewModel.notices.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val isAdmin = currentUser?.isAdmin() == true

    var showCreateDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            if (isAdmin) {
                FloatingActionButton(
                    onClick = { showCreateDialog = true },
                    containerColor = SevaGreenPrimary,
                    contentColor = Color.White
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("নতুন নোটিশ", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "নোটিশ ও জরুরি ঘোষণা",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = SevaGreenPrimary,
                            fontSize = 19.sp
                        )
                    )
                    Text(
                        text = "সংগঠনের জরুরি বিজ্ঞপ্তি, সভা ও মানবিক কার্যক্রমের সর্বশেষ আপডেট",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            }

            if (notices.isEmpty()) {
                EmptyStateView(
                    title = "কোনো নোটিশ নেই",
                    description = "বর্তমানে কোনো নতুন নোটিশ বা ঘোষণা প্রকাশিত হয়নি।",
                    icon = Icons.Outlined.Campaign,
                    actionButtonText = if (isAdmin) "প্রথম নোটিশ প্রকাশ করুন" else null,
                    onActionClick = if (isAdmin) { { showCreateDialog = true } } else null,
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(notices) { notice ->
                        NoticeCardItem(
                            notice = notice,
                            isAdmin = isAdmin,
                            onDelete = { viewModel.deleteNotice(notice.id) }
                        )
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateNoticeDialog(
            currentUser = currentUser,
            onDismiss = { showCreateDialog = false },
            onSubmit = { newNotice ->
                viewModel.createNotice(newNotice) {
                    showCreateDialog = false
                }
            }
        )
    }
}

@Composable
fun NoticeCardItem(
    notice: NoticeItem,
    isAdmin: Boolean,
    onDelete: () -> Unit
) {
    val isUrgent = notice.priority == "জরুরি"

    NeumorphicCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = if (isUrgent) SevaBloodRed.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusBadge(status = notice.type)
                if (isUrgent) {
                    StatusBadge(status = "জরুরি", customColor = SevaBloodRed)
                }
            }

            Text(
                text = notice.date,
                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = notice.title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = notice.content,
            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface)
        )

        if (isAdmin || notice.createdBy.isNotBlank()) {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "প্রচারক: ${notice.createdBy}",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
                if (isAdmin) {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Outlined.Delete, contentDescription = "মুছুন", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
fun CreateNoticeDialog(
    currentUser: com.example.data.model.UserModel?,
    onDismiss: () -> Unit,
    onSubmit: (NoticeItem) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("জরুরি নোটিশ") }
    var priority by remember { mutableStateOf("সাধারণ") }
    var date by remember { mutableStateOf("১২ নভেম্বর ২০২৬") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("নতুন নোটিশ / ঘোষণা প্রকাশ") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("নোটিশের শিরোনাম *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("বিস্তারিত বিষয়বস্তু *") },
                    minLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = type == "জরুরি নোটিশ",
                        onClick = { type = "জরুরি নোটিশ" },
                        label = { Text("জরুরি নোটিশ") }
                    )
                    FilterChip(
                        selected = type == "সভা / মিটিং",
                        onClick = { type = "সভা / মিটিং" },
                        label = { Text("সভা") }
                    )
                    FilterChip(
                        selected = type == "ইভেন্ট",
                        onClick = { type = "ইভেন্ট" },
                        label = { Text("ইভেন্ট") }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = priority == "সাধারণ",
                        onClick = { priority = "সাধারণ" },
                        label = { Text("সাধারণ") }
                    )
                    FilterChip(
                        selected = priority == "জরুরি",
                        onClick = { priority = "জরুরি" },
                        label = { Text("জরুরি গুরুত্ব") }
                    )
                }

                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("তারিখ") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            SevaButton(
                text = "প্রকাশ করুন",
                onClick = {
                    if (title.isNotBlank() && content.isNotBlank()) {
                        onSubmit(
                            NoticeItem(
                                title = title.trim(),
                                content = content.trim(),
                                type = type,
                                priority = priority,
                                date = date.trim(),
                                createdBy = currentUser?.name ?: "অ্যাডমিন"
                            )
                        )
                    }
                }
            )
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("বাতিল") } }
    )
}
