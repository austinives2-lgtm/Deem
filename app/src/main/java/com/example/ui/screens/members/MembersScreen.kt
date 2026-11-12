package com.example.ui.screens.members

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
import com.example.data.model.UserModel
import com.example.data.model.VolunteerItem
import com.example.ui.MainViewModel
import com.example.ui.components.*
import com.example.ui.theme.SevaGoldAccent
import com.example.ui.theme.SevaGreenPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MembersScreen(
    viewModel: MainViewModel,
    onNavigateToChat: (userId: String, userName: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val members by viewModel.members.collectAsState()
    val volunteers by viewModel.volunteers.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    val isAdmin = currentUser?.isAdmin() == true
    val isSuperAdmin = currentUser?.isSuperAdmin() == true

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("সদস্যবৃন্দ", "স্বেচ্ছাসেবক টিম")

    var searchQuery by remember { mutableStateOf("") }
    var selectedBloodGroup by remember { mutableStateOf("ALL") }
    val bloodGroups = listOf("ALL", "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")

    var showAddVolunteerDialog by remember { mutableStateOf(false) }
    var memberToManageRole by remember { mutableStateOf<UserModel?>(null) }

    Scaffold(
        floatingActionButton = {
            if (isAdmin && selectedTab == 1) {
                FloatingActionButton(
                    onClick = { showAddVolunteerDialog = true },
                    containerColor = SevaGreenPrimary,
                    contentColor = Color.White
                ) {
                    Row(modifier = Modifier.padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("স্বেচ্ছাসেবক যোগ", fontWeight = FontWeight.Bold)
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
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = SevaGreenPrimary
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    )
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("নাম বা ফোন দিয়ে খুঁজুন...") },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp)
            )

            // Blood group filter
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(bloodGroups) { bg ->
                    FilterChip(
                        selected = selectedBloodGroup == bg,
                        onClick = { selectedBloodGroup = bg },
                        label = { Text(if (bg == "ALL") "সকল গ্রুপ" else bg) }
                    )
                }
            }

            if (selectedTab == 0) {
                // Members List
                val filteredMembers = remember(members, searchQuery, selectedBloodGroup) {
                    members.filter { m ->
                        val matchesQuery = searchQuery.isBlank() ||
                                m.name.contains(searchQuery, ignoreCase = true) ||
                                m.phone.contains(searchQuery, ignoreCase = true)
                        val matchesBg = selectedBloodGroup == "ALL" || m.bloodGroup.equals(selectedBloodGroup, ignoreCase = true)
                        matchesQuery && matchesBg
                    }
                }

                if (filteredMembers.isEmpty()) {
                    EmptyStateView(
                        title = "কোনো সদস্য পাওয়া যায়নি",
                        description = "বর্তমান অনুসন্ধানে কোনো সদস্য তালিকাভুক্ত নেই।",
                        icon = Icons.Outlined.Group,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(filteredMembers) { member ->
                            MemberCardItem(
                                member = member,
                                isSuperAdmin = isSuperAdmin,
                                isAdmin = isAdmin,
                                onCall = {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${member.phone}"))
                                    context.startActivity(intent)
                                },
                                onChat = { onNavigateToChat(member.uid, member.name) },
                                onToggleStatus = {
                                    val nextStatus = if (member.status == "active") "inactive" else "active"
                                    viewModel.updateMemberStatus(member.uid, nextStatus)
                                },
                                onManageRole = { memberToManageRole = member }
                            )
                        }
                    }
                }
            } else {
                // Volunteers List
                val filteredVolunteers = remember(volunteers, searchQuery, selectedBloodGroup) {
                    volunteers.filter { v ->
                        val matchesQuery = searchQuery.isBlank() ||
                                v.name.contains(searchQuery, ignoreCase = true) ||
                                v.skills.contains(searchQuery, ignoreCase = true)
                        val matchesBg = selectedBloodGroup == "ALL" || v.bloodGroup.equals(selectedBloodGroup, ignoreCase = true)
                        matchesQuery && matchesBg
                    }
                }

                if (filteredVolunteers.isEmpty()) {
                    EmptyStateView(
                        title = "কোনো স্বেচ্ছাসেবক নেই",
                        description = "এখনও কোনো স্বেচ্ছাসেবক তালিকাভুক্ত হয়নি।",
                        icon = Icons.Outlined.VolunteerActivism,
                        actionButtonText = if (isAdmin) "স্বেচ্ছাসেবক যোগ করুন" else null,
                        onActionClick = if (isAdmin) { { showAddVolunteerDialog = true } } else null,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(filteredVolunteers) { volunteer ->
                            VolunteerCardItem(
                                volunteer = volunteer,
                                isAdmin = isAdmin,
                                onCall = {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${volunteer.phone}"))
                                    context.startActivity(intent)
                                },
                                onDelete = { viewModel.deleteVolunteer(volunteer.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Role Management Dialog (Super Admin only)
    if (memberToManageRole != null && isSuperAdmin) {
        val member = memberToManageRole!!
        var role by remember { mutableStateOf(member.role) }
        var position by remember { mutableStateOf(member.position) }

        AlertDialog(
            onDismissRequest = { memberToManageRole = null },
            title = { Text("রোল ও পদবি ব্যবস্থাপনা") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("সদস্য: ${member.name}", fontWeight = FontWeight.Bold)
                    Text("ইমেইল: ${member.email}", style = MaterialTheme.typography.bodySmall)

                    HorizontalDivider()

                    Text("ব্যবহারকারীর ভূমিকা (Role):")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = role == "member",
                            onClick = { role = "member" },
                            label = { Text("সাধারণ সদস্য") }
                        )
                        FilterChip(
                            selected = role == "admin",
                            onClick = { role = "admin" },
                            label = { Text("অ্যাডমিন") }
                        )
                    }

                    OutlinedTextField(
                        value = position,
                        onValueChange = { position = it },
                        label = { Text("সাংগঠনিক পদবি") },
                        placeholder = { Text("যেমন: সভাপতি, সাধারণ সম্পাদক, সদস্য") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                SevaButton(
                    text = "আপডেট করুন",
                    onClick = {
                        viewModel.updateMemberRole(member.uid, role, position)
                        memberToManageRole = null
                    }
                )
            },
            dismissButton = {
                TextButton(onClick = { memberToManageRole = null }) { Text("বাতিল") }
            }
        )
    }

    // Add Volunteer Dialog
    if (showAddVolunteerDialog) {
        AddVolunteerDialog(
            onDismiss = { showAddVolunteerDialog = false },
            onSubmit = { newVol ->
                viewModel.addVolunteer(newVol) {
                    showAddVolunteerDialog = false
                }
            }
        )
    }
}

@Composable
fun MemberCardItem(
    member: UserModel,
    isSuperAdmin: Boolean,
    isAdmin: Boolean,
    onCall: () -> Unit,
    onChat: () -> Unit,
    onToggleStatus: () -> Unit,
    onManageRole: () -> Unit
) {
    NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(SevaGreenPrimary.copy(alpha = 0.1f))
                    .border(1.dp, SevaGreenPrimary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (member.photoURL.isNotBlank()) {
                    AsyncImage(
                        model = member.photoURL,
                        contentDescription = member.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Filled.Person, contentDescription = null, tint = SevaGreenPrimary, modifier = Modifier.size(28.dp))
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
                        text = member.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    StatusBadge(status = if (member.status == "active") "সক্রিয়" else "নিষ্ক্রিয়")
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = member.position.ifBlank { "সদস্য" },
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = SevaGreenPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    if (member.bloodGroup.isNotBlank()) {
                        Text(
                            text = "রক্ত: ${member.bloodGroup}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }

                if (member.role != "member") {
                    Text(
                        text = if (member.role == "super_admin") "★ সুপার অ্যাডমিন" else "♦ অ্যাডমিন",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = SevaGoldAccent,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (member.phone.isNotBlank()) {
                    OutlinedButton(
                        onClick = onCall,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Outlined.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("কল", fontSize = 12.sp)
                    }
                }
                OutlinedButton(
                    onClick = onChat,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Outlined.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("মেসেজ", fontSize = 12.sp)
                }
            }

            if (isSuperAdmin) {
                TextButton(onClick = onManageRole) {
                    Text("রোল পরিবর্তন", fontSize = 12.sp, color = SevaGreenPrimary)
                }
            } else if (isAdmin) {
                TextButton(onClick = onToggleStatus) {
                    Text(
                        text = if (member.status == "active") "নিষ্ক্রিয় করুন" else "সক্রিয় করুন",
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
fun VolunteerCardItem(
    volunteer: VolunteerItem,
    isAdmin: Boolean,
    onCall: () -> Unit,
    onDelete: () -> Unit
) {
    NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = volunteer.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "দক্ষতা: ${volunteer.skills}",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
                Text(
                    text = "উপলব্ধতা: ${volunteer.availability} • রক্ত: ${volunteer.bloodGroup}",
                    style = MaterialTheme.typography.labelSmall.copy(color = SevaGreenPrimary, fontWeight = FontWeight.SemiBold)
                )
            }

            StatusBadge(status = volunteer.status)
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (volunteer.phone.isNotBlank()) {
                OutlinedButton(
                    onClick = onCall,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Outlined.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(volunteer.phone, fontSize = 12.sp)
                }
            }

            if (isAdmin) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Outlined.Delete, contentDescription = "মুছুন", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun AddVolunteerDialog(
    onDismiss: () -> Unit,
    onSubmit: (VolunteerItem) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var skills by remember { mutableStateOf("") }
    var availability by remember { mutableStateOf("সব সময়") }
    var bloodGroup by remember { mutableStateOf("A+") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("নতুন স্বেচ্ছাসেবক নিবন্ধন") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("স্বেচ্ছাসেবকের নাম *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("মোবাইল নম্বর *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = skills,
                    onValueChange = { skills = it },
                    label = { Text("দক্ষতা বা অভিজ্ঞতা") },
                    placeholder = { Text("যেমন: রক্তদান সমন্বয়, প্রাথমিক চিকিৎসা, খাদ্য বিতরণ") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = availability,
                    onValueChange = { availability = it },
                    label = { Text("কখন সময় দিতে পারবেন") },
                    placeholder = { Text("যেমন: সব সময়, ছুটির দিন, শুক্রবার") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = bloodGroup,
                    onValueChange = { bloodGroup = it },
                    label = { Text("রক্তের গ্রুপ") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            SevaButton(
                text = "সংরক্ষণ করুন",
                onClick = {
                    if (name.isNotBlank() && phone.isNotBlank()) {
                        onSubmit(
                            VolunteerItem(
                                name = name.trim(),
                                phone = phone.trim(),
                                skills = skills.trim(),
                                availability = availability.trim(),
                                bloodGroup = bloodGroup.trim(),
                                status = "সক্রিয়"
                            )
                        )
                    }
                }
            )
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("বাতিল") } }
    )
}
