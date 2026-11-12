package com.example.ui.screens.messages

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.ChatMessage
import com.example.data.model.Conversation
import com.example.data.model.UserModel
import com.example.ui.MainViewModel
import com.example.ui.components.EmptyStateView
import com.example.ui.components.ImageUrlPickerField
import com.example.ui.components.NeumorphicCard
import com.example.ui.theme.SevaGreenPrimary
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationsListScreen(
    viewModel: MainViewModel,
    onSelectConversation: (conversationId: String, recipientId: String, recipientName: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val conversations by viewModel.conversations.collectAsState()
    val members by viewModel.members.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var showNewChatDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showNewChatDialog = true },
                containerColor = SevaGreenPrimary,
                contentColor = Color.White
            ) {
                Row(modifier = Modifier.padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Chat, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("নতুন বার্তা", fontWeight = FontWeight.Bold)
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
                        text = "ইনবক্স ও যোগাযোগ",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = SevaGreenPrimary,
                            fontSize = 19.sp
                        )
                    )
                    Text(
                        text = "সংগঠনের অন্যান্য সদস্যদের সাথে সরাসরি যোগাযোগ করুন",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            }

            if (conversations.isEmpty()) {
                EmptyStateView(
                    title = "কোনো বার্তা নেই",
                    description = "আপনার এখনও কোনো বার্তালাপ শুরু হয়নি। নতুন বার্তা পাঠাতে নিচের বাটনে চাপুন।",
                    icon = Icons.Outlined.ChatBubbleOutline,
                    actionButtonText = "সদস্যের সাথে চ্যাট শুরু করুন",
                    onActionClick = { showNewChatDialog = true },
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(conversations) { conv ->
                        val currentUid = currentUser?.uid ?: ""
                        val otherParticipantId = conv.participantIds.firstOrNull { it != currentUid } ?: ""
                        val otherParticipantName = conv.participantNames[otherParticipantId] ?: "সদস্য"

                        ConversationItemCard(
                            otherParticipantName = otherParticipantName,
                            lastMessage = conv.lastMessage,
                            time = formatTimestamp(conv.lastMessageTime),
                            onClick = {
                                onSelectConversation(conv.id, otherParticipantId, otherParticipantName)
                            }
                        )
                    }
                }
            }
        }
    }

    // New Chat Member Selection Dialog
    if (showNewChatDialog) {
        val currentUid = currentUser?.uid ?: ""
        val otherMembers = remember(members) { members.filter { it.uid != currentUid } }

        AlertDialog(
            onDismissRequest = { showNewChatDialog = false },
            title = { Text("সদস্য নির্বাচন করুন") },
            text = {
                if (otherMembers.isEmpty()) {
                    Text("বর্তমানে মেসেজ পাঠানোর জন্য অন্য কোনো সদস্য নেই।")
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(otherMembers) { member ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        showNewChatDialog = false
                                        val convId = viewModel.firestoreRepo.getConversationId(currentUid, member.uid)
                                        onSelectConversation(convId, member.uid, member.name)
                                    },
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(SevaGreenPrimary.copy(alpha = 0.15f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Filled.Person, contentDescription = null, tint = SevaGreenPrimary, modifier = Modifier.size(20.dp))
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(member.name, fontWeight = FontWeight.Bold)
                                        Text(member.position, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showNewChatDialog = false }) { Text("বন্ধ করুন") }
            }
        )
    }
}

@Composable
fun ConversationItemCard(
    otherParticipantName: String,
    lastMessage: String,
    time: String,
    onClick: () -> Unit
) {
    NeumorphicCard(
        modifier = Modifier.fillMaxWidth(),
        shapeRadius = 12.dp,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(SevaGreenPrimary.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = null,
                    tint = SevaGreenPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = otherParticipantName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = time,
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = lastMessage.ifBlank { "বার্তালাপ শুরু হয়েছে" },
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatConversationScreen(
    conversationId: String,
    recipientId: String,
    recipientName: String,
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val currentUid = currentUser?.uid ?: ""

    val messagesFlow = remember(conversationId) {
        viewModel.firestoreRepo.getMessages(conversationId)
    }
    val messages by messagesFlow.collectAsState(initial = emptyList())

    var messageText by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var showImageDialog by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(recipientName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("বাংলাদেশ সেবামূলক সংগঠন", style = MaterialTheme.typography.labelSmall, color = SevaGreenPrimary)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "ফিরে যান")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Messages List
            if (messages.isEmpty()) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        text = "এখনও কোনো বার্তা আদান-প্রদান হয়নি।\nনিচে লিখে মেসেজ পাঠান।",
                        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(messages) { msg ->
                        val isMine = msg.senderId == currentUid
                        MessageBubble(message = msg, isMine = isMine)
                    }
                }
            }

            // Input Bar
            Surface(
                tonalElevation = 3.dp,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)) {
                    if (imageUrl.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 6.dp)
                        ) {
                            Text("ছবি সংযুক্ত: $imageUrl", style = MaterialTheme.typography.labelSmall, maxLines = 1, modifier = Modifier.weight(1f))
                            IconButton(onClick = { imageUrl = "" }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Filled.Close, contentDescription = "মুছুন", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { showImageDialog = true }) {
                            Icon(Icons.Outlined.Image, contentDescription = "ছবি যোগ করুন", tint = SevaGreenPrimary)
                        }

                        OutlinedTextField(
                            value = messageText,
                            onValueChange = { messageText = it },
                            placeholder = { Text("বার্তা লিখুন...") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(20.dp),
                            maxLines = 4
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        IconButton(
                            onClick = {
                                if (messageText.isNotBlank() || imageUrl.isNotBlank()) {
                                    viewModel.sendMessage(
                                        recipientId = recipientId,
                                        recipientName = recipientName,
                                        text = messageText,
                                        imageUrl = imageUrl
                                    )
                                    messageText = ""
                                    imageUrl = ""
                                }
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .background(SevaGreenPrimary, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "পাঠান",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showImageDialog) {
        var tempUrl by remember { mutableStateOf(imageUrl) }
        AlertDialog(
            onDismissRequest = { showImageDialog = false },
            title = { Text("ছবির লিংক দিন") },
            text = {
                ImageUrlPickerField(
                    imageUrl = tempUrl,
                    onImageUrlChange = { tempUrl = it },
                    label = "ছবির HTTPS URL বা ডিভাইস ছবি"
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    imageUrl = tempUrl
                    showImageDialog = false
                }) { Text("সংযুক্ত করুন") }
            },
            dismissButton = {
                TextButton(onClick = { showImageDialog = false }) { Text("বাতিল") }
            }
        )
    }
}

@Composable
fun MessageBubble(
    message: ChatMessage,
    isMine: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isMine) Alignment.End else Alignment.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 14.dp,
                topEnd = 14.dp,
                bottomStart = if (isMine) 14.dp else 2.dp,
                bottomEnd = if (isMine) 2.dp else 14.dp
            ),
            color = if (isMine) SevaGreenPrimary else MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                if (message.imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = message.imageUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 180.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }

                if (message.text.isNotBlank()) {
                    Text(
                        text = message.text,
                        color = if (isMine) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatTimestamp(message.timestamp),
                        color = if (isMine) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp)
                    )
                    if (isMine) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = if (message.isSeen) Icons.Filled.DoneAll else Icons.Filled.Done,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun formatTimestamp(timestamp: com.google.firebase.Timestamp?): String {
    if (timestamp == null) return ""
    val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
    return sdf.format(timestamp.toDate())
}
