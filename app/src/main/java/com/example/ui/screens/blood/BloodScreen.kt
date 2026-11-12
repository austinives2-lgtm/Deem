package com.example.ui.screens.blood

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.BloodRequest
import com.example.data.model.UserModel
import com.example.ui.MainViewModel
import com.example.ui.components.EmptyStateView
import com.example.ui.components.NeumorphicCard
import com.example.ui.components.SevaButton
import com.example.ui.components.StatusBadge
import com.example.ui.theme.SevaBloodRed
import com.example.ui.theme.SevaGreenPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BloodScreen(
    viewModel: MainViewModel,
    onNavigateToChatWithUser: (userId: String, userName: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val bloodRequests by viewModel.bloodRequests.collectAsState()
    val bloodDonors by viewModel.bloodDonors.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("রক্তের আবেদন", "রক্তদাতাদের তালিকা")

    var selectedBloodGroupFilter by remember { mutableStateOf("ALL") }
    val bloodGroups = listOf("ALL", "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")

    var showRequestDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            if (selectedTabIndex == 0) {
                FloatingActionButton(
                    onClick = { showRequestDialog = true },
                    containerColor = SevaBloodRed,
                    contentColor = Color.White
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("রক্তের আবেদন", fontWeight = FontWeight.Bold)
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
            // Top Tab Row
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = SevaBloodRed
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    )
                }
            }

            // Tab 1: Blood Requests
            if (selectedTabIndex == 0) {
                val filteredRequests = remember(bloodRequests, selectedBloodGroupFilter) {
                    if (selectedBloodGroupFilter == "ALL") bloodRequests
                    else bloodRequests.filter { it.bloodGroup.equals(selectedBloodGroupFilter, ignoreCase = true) }
                }

                Column(modifier = Modifier.fillMaxSize()) {
                    // Blood Filter Chips
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(bloodGroups) { bg ->
                            FilterChip(
                                selected = selectedBloodGroupFilter == bg,
                                onClick = { selectedBloodGroupFilter = bg },
                                label = { Text(if (bg == "ALL") "সকল গ্রুপ" else bg) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SevaBloodRed.copy(alpha = 0.15f),
                                    selectedLabelColor = SevaBloodRed
                                )
                            )
                        }
                    }

                    if (filteredRequests.isEmpty()) {
                        EmptyStateView(
                            title = "কোনো রক্তের আবেদন নেই",
                            description = "বর্তমানে রক্তের কোনো জরুরি আবেদন তালিকাভুক্ত নেই।",
                            icon = Icons.Outlined.WaterDrop,
                            actionButtonText = "নতুন রক্তের আবেদন করুন",
                            onActionClick = { showRequestDialog = true },
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(filteredRequests) { req ->
                                BloodRequestItemCard(
                                    request = req,
                                    isOwnerOrAdmin = currentUser?.isAdmin() == true || currentUser?.uid == req.requestedBy,
                                    onCall = {
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${req.contact}"))
                                        context.startActivity(intent)
                                    },
                                    onMarkFulfilled = {
                                        viewModel.updateBloodRequestStatus(req.id, "Fulfilled")
                                    }
                                )
                            }
                        }
                    }
                }
            } else {
                // Tab 2: Blood Donors Directory
                val filteredDonors = remember(bloodDonors, selectedBloodGroupFilter) {
                    val list = if (selectedBloodGroupFilter == "ALL") bloodDonors
                    else bloodDonors.filter { it.bloodGroup.equals(selectedBloodGroupFilter, ignoreCase = true) }
                    list.filter { it.bloodGroup.isNotBlank() }
                }

                Column(modifier = Modifier.fillMaxSize()) {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(bloodGroups) { bg ->
                            FilterChip(
                                selected = selectedBloodGroupFilter == bg,
                                onClick = { selectedBloodGroupFilter = bg },
                                label = { Text(if (bg == "ALL") "সকল গ্রুপ" else bg) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SevaGreenPrimary.copy(alpha = 0.15f),
                                    selectedLabelColor = SevaGreenPrimary
                                )
                            )
                        }
                    }

                    if (filteredDonors.isEmpty()) {
                        EmptyStateView(
                            title = "কোনো রক্তদাতা পাওয়া যায়নি",
                            description = "এই গ্রুপের কোনো রক্তদাতা বর্তমানে নিবন্ধিত নেই।",
                            icon = Icons.Outlined.PersonSearch,
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(filteredDonors) { donor ->
                                BloodDonorCard(
                                    donor = donor,
                                    onCall = {
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${donor.phone}"))
                                        context.startActivity(intent)
                                    },
                                    onChat = {
                                        onNavigateToChatWithUser(donor.uid, donor.name)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Blood Request Dialog
    if (showRequestDialog) {
        CreateBloodRequestDialog(
            currentUser = currentUser,
            onDismiss = { showRequestDialog = false },
            onSubmit = { newReq ->
                viewModel.createBloodRequest(newReq) {
                    showRequestDialog = false
                }
            }
        )
    }
}

@Composable
fun BloodRequestItemCard(
    request: BloodRequest,
    isOwnerOrAdmin: Boolean,
    onCall: () -> Unit,
    onMarkFulfilled: () -> Unit
) {
    NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = SevaBloodRed
            ) {
                Text(
                    text = request.bloodGroup,
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                StatusBadge(status = request.urgency)
                StatusBadge(status = if (request.status.equals("Active", ignoreCase = true)) "প্রয়োজন" else "রক্ত পাওয়া গেছে")
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "রোগী: ${request.patientName}",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.LocalHospital, contentDescription = null, tint = SevaBloodRed, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "${request.hospital}, ${request.location}",
                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "প্রয়োজন: ${request.units} ব্যাগ রক্ত",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = SevaBloodRed)
            )
            Text(
                text = "তারিখ: ${request.requiredDate}",
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            SevaButton(
                text = "কল করুন (${request.contact})",
                onClick = onCall,
                icon = Icons.Outlined.Call,
                containerColor = SevaBloodRed,
                contentColor = Color.White
            )

            if (isOwnerOrAdmin && request.status.equals("Active", ignoreCase = true)) {
                OutlinedButton(
                    onClick = onMarkFulfilled,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, SevaGreenPrimary)
                ) {
                    Text("রক্ত পাওয়া গেছে", color = SevaGreenPrimary, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
fun BloodDonorCard(
    donor: UserModel,
    onCall: () -> Unit,
    onChat: () -> Unit
) {
    NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile photo or avatar
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(SevaGreenPrimary.copy(alpha = 0.1f))
                    .border(1.5.dp, SevaBloodRed, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (donor.photoURL.isNotBlank()) {
                    AsyncImage(
                        model = donor.photoURL,
                        contentDescription = donor.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = null,
                        tint = SevaGreenPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = donor.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SevaBloodRed.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = donor.bloodGroup,
                            color = SevaBloodRed,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = if (donor.address.isNotBlank()) donor.address else "বাংলাদেশ",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = if (donor.isAvailableForBlood) "● রক্তদানে প্রস্তুত" else "○ সাময়িক অনুপলব্ধ",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (donor.isAvailableForBlood) SevaGreenPrimary else Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    if (donor.donationCount > 0) {
                        Text(
                            text = "দান: ${donor.donationCount} বার",
                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SevaButton(
                text = "কল করুন",
                onClick = onCall,
                icon = Icons.Outlined.Call,
                containerColor = SevaGreenPrimary,
                contentColor = Color.White,
                modifier = Modifier.weight(1f)
            )
            SevaButton(
                text = "মেসেজ",
                onClick = onChat,
                icon = Icons.Outlined.Chat,
                isPrimary = false,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateBloodRequestDialog(
    currentUser: UserModel?,
    onDismiss: () -> Unit,
    onSubmit: (BloodRequest) -> Unit
) {
    var patientName by remember { mutableStateOf("") }
    var bloodGroup by remember { mutableStateOf("A+") }
    var bloodMenuExpanded by remember { mutableStateOf(false) }
    var hospital by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var requiredDate by remember { mutableStateOf("") }
    var units by remember { mutableIntStateOf(1) }
    var contact by remember { mutableStateOf(currentUser?.phone ?: "") }
    var urgency by remember { mutableStateOf("জরুরি") }

    val bloodGroups = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("রক্তের জরুরি আবেদন প্রকাশ") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = patientName,
                    onValueChange = { patientName = it },
                    label = { Text("রোগীর নাম *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Blood Group
                ExposedDropdownMenuBox(
                    expanded = bloodMenuExpanded,
                    onExpandedChange = { bloodMenuExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = bloodGroup,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("প্রয়োজনীয় রক্তের গ্রুপ *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = bloodMenuExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = bloodMenuExpanded,
                        onDismissRequest = { bloodMenuExpanded = false }
                    ) {
                        bloodGroups.forEach { bg ->
                            DropdownMenuItem(
                                text = { Text(bg) },
                                onClick = {
                                    bloodGroup = bg
                                    bloodMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = hospital,
                    onValueChange = { hospital = it },
                    label = { Text("হাসপাতালের নাম *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("লোকেশন / জেলা *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = requiredDate,
                    onValueChange = { requiredDate = it },
                    label = { Text("রক্তের প্রয়োজনীয় তারিখ ও সময় *") },
                    placeholder = { Text("যেমন: আজ বিকেল ৫টা / ১২ নভেম্বর") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = contact,
                    onValueChange = { contact = it },
                    label = { Text("যোগাযোগের নম্বর *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("কত ব্যাগ রক্ত প্রয়োজন:")
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { if (units > 1) units-- }) {
                            Icon(Icons.Filled.Remove, contentDescription = "কমান")
                        }
                        Text("$units ব্যাগ", fontWeight = FontWeight.Bold)
                        IconButton(onClick = { units++ }) {
                            Icon(Icons.Filled.Add, contentDescription = "বাড়ান")
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("জরুরিতা:")
                    FilterChip(
                        selected = urgency == "জরুরি",
                        onClick = { urgency = "জরুরি" },
                        label = { Text("জরুরি") }
                    )
                    FilterChip(
                        selected = urgency == "সাধারণ",
                        onClick = { urgency = "সাধারণ" },
                        label = { Text("সাধারণ") }
                    )
                }
            }
        },
        confirmButton = {
            SevaButton(
                text = "আবেদন প্রকাশ করুন",
                onClick = {
                    if (patientName.isNotBlank() && hospital.isNotBlank() && contact.isNotBlank()) {
                        onSubmit(
                            BloodRequest(
                                patientName = patientName.trim(),
                                bloodGroup = bloodGroup,
                                hospital = hospital.trim(),
                                location = location.trim(),
                                requiredDate = requiredDate.trim(),
                                units = units,
                                contact = contact.trim(),
                                urgency = urgency,
                                status = "Active",
                                requestedBy = currentUser?.uid ?: "",
                                requestedByName = currentUser?.name ?: "সদস্য"
                            )
                        )
                    }
                },
                containerColor = SevaBloodRed,
                contentColor = Color.White
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("বাতিল") }
        }
    )
}
