package com.example.ui.screens.activities

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.ActivityItem
import com.example.ui.MainViewModel
import com.example.ui.components.*
import com.example.ui.theme.SevaGreenPrimary
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ActivitiesScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val activities by viewModel.activities.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val isAdmin = currentUser?.isAdmin() == true

    var selectedFilter by remember { mutableStateOf("ALL") } // ALL, Ongoing, Completed
    var showCreateDialog by remember { mutableStateOf(false) }
    var activityToEdit by remember { mutableStateOf<ActivityItem?>(null) }
    var selectedActivityDetail by remember { mutableStateOf<ActivityItem?>(null) }

    val filteredActivities = remember(activities, selectedFilter) {
        when (selectedFilter) {
            "Ongoing" -> activities.filter { it.status.equals("Ongoing", ignoreCase = true) }
            "Completed" -> activities.filter { it.status.equals("Completed", ignoreCase = true) }
            else -> activities
        }
    }

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
                        Text("নতুন কার্যক্রম", fontWeight = FontWeight.Bold)
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
            // Filter Chips Header
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FilterChip(
                        selected = selectedFilter == "ALL",
                        onClick = { selectedFilter = "ALL" },
                        label = { Text("সকল কার্যক্রম (${activities.size})") }
                    )
                    FilterChip(
                        selected = selectedFilter == "Ongoing",
                        onClick = { selectedFilter = "Ongoing" },
                        label = { Text("চলমান (${activities.count { it.status.equals("Ongoing", true) }})") }
                    )
                    FilterChip(
                        selected = selectedFilter == "Completed",
                        onClick = { selectedFilter = "Completed" },
                        label = { Text("সম্পন্ন (${activities.count { it.status.equals("Completed", true) }})") }
                    )
                }
            }

            if (filteredActivities.isEmpty()) {
                EmptyStateView(
                    title = "কোনো কার্যক্রম পাওয়া যায়নি",
                    description = if (selectedFilter == "ALL")
                        "এখনও কোনো মানবিক কর্মসূচি এন্ট্রি করা হয়নি।"
                    else "এই ফিল্টারে কোনো কার্যক্রম নেই।",
                    icon = Icons.Outlined.EventAvailable,
                    actionButtonText = if (isAdmin) "নতুন কার্যক্রম তৈরি করুন" else null,
                    onActionClick = if (isAdmin) { { showCreateDialog = true } } else null,
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredActivities) { activity ->
                        ActivityDetailCard(
                            activity = activity,
                            isAdmin = isAdmin,
                            onCardClick = { selectedActivityDetail = activity },
                            onEdit = { activityToEdit = activity },
                            onDelete = { viewModel.deleteActivity(activity.id) }
                        )
                    }
                }
            }
        }
    }

    // Detail Dialog
    if (selectedActivityDetail != null) {
        val act = selectedActivityDetail!!
        AlertDialog(
            onDismissRequest = { selectedActivityDetail = null },
            title = { Text(act.title, fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (act.imageUrl.isNotBlank()) {
                        AsyncImage(
                            model = act.imageUrl,
                            contentDescription = act.title,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatusBadge(status = act.status)
                        Text("তারিখ: ${act.date}", style = MaterialTheme.typography.bodySmall)
                    }

                    Text("বিভাগ: ${act.category}", style = MaterialTheme.typography.labelMedium)
                    if (act.location.isNotBlank()) {
                        Text("স্থান: ${act.location}", style = MaterialTheme.typography.labelMedium)
                    }

                    HorizontalDivider()

                    Text(act.description, style = MaterialTheme.typography.bodyMedium)

                    HorizontalDivider()

                    Text("অংশগ্রহণকারী: ${act.participants} জন", style = MaterialTheme.typography.bodySmall)
                    Text("উপকারভোগী মানুষ: ${act.beneficiaries} জন", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = SevaGreenPrimary))
                    if (act.budget > 0) {
                        Text("প্রকল্প বাজেট: ৳${act.budget.toLong()}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedActivityDetail = null }) {
                    Text("বন্ধ করুন")
                }
            }
        )
    }

    // Add / Edit Activity Dialog (Admin)
    if (showCreateDialog || activityToEdit != null) {
        val editing = activityToEdit
        AddEditActivityDialog(
            activity = editing,
            currentUser = currentUser,
            onDismiss = {
                showCreateDialog = false
                activityToEdit = null
            },
            onSave = { updated ->
                if (editing != null) {
                    viewModel.updateActivity(updated) {
                        activityToEdit = null
                    }
                } else {
                    viewModel.createActivity(updated) {
                        showCreateDialog = false
                    }
                }
            }
        )
    }
}

@Composable
fun ActivityDetailCard(
    activity: ActivityItem,
    isAdmin: Boolean,
    onCardClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val formatter = remember { NumberFormat.getNumberInstance(Locale.US) }

    NeumorphicCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onCardClick
    ) {
        if (activity.imageUrl.isNotBlank()) {
            AsyncImage(
                model = activity.imageUrl,
                contentDescription = activity.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            StatusBadge(status = activity.status)
            Text(
                text = activity.date,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = activity.title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = activity.description,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "উপকারভোগী: ${activity.beneficiaries} জন",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = SevaGreenPrimary
                    )
                )
                if (activity.budget > 0) {
                    Text(
                        text = "বাজেট: ৳${formatter.format(activity.budget.toLong())}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }

            if (isAdmin) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Outlined.Edit, contentDescription = "সম্পাদনা", tint = SevaGreenPrimary)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Outlined.Delete, contentDescription = "মুছুন", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
fun AddEditActivityDialog(
    activity: ActivityItem?,
    currentUser: com.example.data.model.UserModel?,
    onDismiss: () -> Unit,
    onSave: (ActivityItem) -> Unit
) {
    var title by remember { mutableStateOf(activity?.title ?: "") }
    var description by remember { mutableStateOf(activity?.description ?: "") }
    var category by remember { mutableStateOf(activity?.category ?: "খাদ্য ও ত্রাণ") }
    var imageUrl by remember { mutableStateOf(activity?.imageUrl ?: "") }
    var date by remember { mutableStateOf(activity?.date ?: "") }
    var location by remember { mutableStateOf(activity?.location ?: "") }
    var participants by remember { mutableStateOf(activity?.participants?.toString() ?: "0") }
    var beneficiaries by remember { mutableStateOf(activity?.beneficiaries?.toString() ?: "0") }
    var budget by remember { mutableStateOf(activity?.budget?.toInt()?.toString() ?: "0") }
    var status by remember { mutableStateOf(activity?.status ?: "Ongoing") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (activity != null) "কার্যক্রম সম্পাদনা" else "নতুন কার্যক্রম প্রকাশ") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("কার্যক্রমের শিরোনাম *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("ক্যাটেগরি *") },
                    placeholder = { Text("যেমন: খাদ্য বিতরণ, শীতবস্ত্র বিতরণ, চিকিৎসা") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("তারিখ *") },
                    placeholder = { Text("যেমন: ১২ নভেম্বর ২০২৬") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("স্থান / এলাকা *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("বিস্তারিত বিবরণ *") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                ImageUrlPickerField(
                    imageUrl = imageUrl,
                    onImageUrlChange = { imageUrl = it },
                    label = "কার্যক্রমের ছবি (URL বা গ্যালারি)"
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = beneficiaries,
                        onValueChange = { beneficiaries = it.filter { ch -> ch.isDigit() } },
                        label = { Text("উপকারভোগী (জন)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = participants,
                        onValueChange = { participants = it.filter { ch -> ch.isDigit() } },
                        label = { Text("স্বেচ্ছাসেবক") },
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = budget,
                    onValueChange = { budget = it.filter { ch -> ch.isDigit() } },
                    label = { Text("বাজেট (টাকা)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("অবস্থা:")
                    FilterChip(
                        selected = status == "Ongoing",
                        onClick = { status = "Ongoing" },
                        label = { Text("চলমান") }
                    )
                    FilterChip(
                        selected = status == "Completed",
                        onClick = { status = "Completed" },
                        label = { Text("সম্পন্ন") }
                    )
                }
            }
        },
        confirmButton = {
            SevaButton(
                text = "সংরক্ষণ করুন",
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(
                            ActivityItem(
                                id = activity?.id ?: "",
                                title = title.trim(),
                                description = description.trim(),
                                category = category.trim(),
                                imageUrl = imageUrl.trim(),
                                date = date.trim(),
                                location = location.trim(),
                                participants = participants.toIntOrNull() ?: 0,
                                beneficiaries = beneficiaries.toIntOrNull() ?: 0,
                                budget = budget.toDoubleOrNull() ?: 0.0,
                                status = status,
                                createdBy = currentUser?.name ?: "অ্যাডমিন"
                            )
                        )
                    }
                }
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("বাতিল") }
        }
    )
}
