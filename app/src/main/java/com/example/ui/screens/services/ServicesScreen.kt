package com.example.ui.screens.services

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ServiceItem
import com.example.data.model.ServiceRequest
import com.example.ui.MainViewModel
import com.example.ui.components.EmptyStateView
import com.example.ui.components.NeumorphicCard
import com.example.ui.components.SevaButton
import com.example.ui.components.StatusBadge
import com.example.ui.theme.SevaGreenPrimary

@Composable
fun ServicesScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val services by viewModel.services.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val isAdmin = currentUser?.isAdmin() == true

    var selectedServiceForRequest by remember { mutableStateOf<ServiceItem?>(null) }
    var serviceToEdit by remember { mutableStateOf<ServiceItem?>(null) }
    var showAddServiceDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            if (isAdmin) {
                FloatingActionButton(
                    onClick = { showAddServiceDialog = true },
                    containerColor = SevaGreenPrimary,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "নতুন সেবা যোগ করুন")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Header summary
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "আমাদের মানবিক সেবাসমূহ",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = SevaGreenPrimary,
                            fontSize = 19.sp
                        )
                    )
                    Text(
                        text = "যেকোনো জরুরি মানবিক প্রয়োজনে আমাদের সেবা গ্রহণ করুন বা আবেদন করুন",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            }

            if (services.isEmpty()) {
                EmptyStateView(
                    title = "কোনো সেবা পাওয়া যায়নি",
                    description = "এখনও কোনো সেবা তালিকাভুক্ত নেই। অ্যাডমিন নতুন সেবা যোগ করতে পারেন।",
                    actionButtonText = if (isAdmin) "প্রথম সেবা যোগ করুন" else null,
                    onActionClick = if (isAdmin) { { showAddServiceDialog = true } } else null,
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(services) { service ->
                        ServiceCardItem(
                            service = service,
                            isAdmin = isAdmin,
                            onRequestClick = { selectedServiceForRequest = service },
                            onEditClick = { serviceToEdit = service },
                            onDeleteClick = { viewModel.deleteService(service.id) }
                        )
                    }
                }
            }
        }
    }

    // Dialog for requesting service
    if (selectedServiceForRequest != null) {
        ServiceRequestDialog(
            service = selectedServiceForRequest!!,
            currentUser = currentUser,
            onDismiss = { selectedServiceForRequest = null },
            onSubmit = { req ->
                viewModel.submitServiceRequest(req) {
                    selectedServiceForRequest = null
                }
            }
        )
    }

    // Dialog for Add / Edit Service (Admin)
    if (showAddServiceDialog || serviceToEdit != null) {
        val editing = serviceToEdit
        AddEditServiceDialog(
            service = editing,
            onDismiss = {
                showAddServiceDialog = false
                serviceToEdit = null
            },
            onSave = { updated ->
                if (editing != null) {
                    viewModel.updateService(updated) {
                        serviceToEdit = null
                    }
                } else {
                    viewModel.addService(updated) {
                        showAddServiceDialog = false
                    }
                }
            }
        )
    }
}

@Composable
fun ServiceCardItem(
    service: ServiceItem,
    isAdmin: Boolean,
    onRequestClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    NeumorphicCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(SevaGreenPrimary.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.VolunteerActivism,
                    contentDescription = null,
                    tint = SevaGreenPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = service.titleBn.ifBlank { service.title },
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    StatusBadge(status = if (service.isActive) "সক্রিয় সেবা" else "স্থগিত")
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = service.description,
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SevaButton(
                        text = "সেবার আবেদন করুন",
                        onClick = onRequestClick,
                        isPrimary = true,
                        icon = Icons.Outlined.Send
                    )

                    if (isAdmin) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(onClick = onEditClick) {
                                Icon(Icons.Outlined.Edit, contentDescription = "সম্পাদনা", tint = SevaGreenPrimary)
                            }
                            IconButton(onClick = onDeleteClick) {
                                Icon(Icons.Outlined.Delete, contentDescription = "মুছুন", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ServiceRequestDialog(
    service: ServiceItem,
    currentUser: com.example.data.model.UserModel?,
    onDismiss: () -> Unit,
    onSubmit: (ServiceRequest) -> Unit
) {
    var name by remember { mutableStateOf(currentUser?.name ?: "") }
    var phone by remember { mutableStateOf(currentUser?.phone ?: "") }
    var location by remember { mutableStateOf(currentUser?.address ?: "") }
    var details by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("${service.titleBn} - আবেদন ফরম") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("আবেদনকারীর নাম *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("যোগাযোগের মোবাইল নম্বর *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("ঠিকানা / অবস্থান *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = details,
                    onValueChange = { details = it },
                    label = { Text("সমস্যা বা প্রয়োজনীয়তার বিবরণ *") },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            SevaButton(
                text = "আবেদন জমা দিন",
                onClick = {
                    if (name.isNotBlank() && phone.isNotBlank() && details.isNotBlank()) {
                        onSubmit(
                            ServiceRequest(
                                serviceTitle = service.titleBn,
                                applicantName = name,
                                phone = phone,
                                location = location,
                                details = details,
                                requestedBy = currentUser?.uid ?: ""
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

@Composable
fun AddEditServiceDialog(
    service: ServiceItem?,
    onDismiss: () -> Unit,
    onSave: (ServiceItem) -> Unit
) {
    var titleBn by remember { mutableStateOf(service?.titleBn ?: service?.title ?: "") }
    var description by remember { mutableStateOf(service?.description ?: "") }
    var isActive by remember { mutableStateOf(service?.isActive ?: true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (service != null) "সেবা সম্পাদনা" else "নতুন সেবা যোগ করুন") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = titleBn,
                    onValueChange = { titleBn = it },
                    label = { Text("সেবার নাম (বাংলা) *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("সেবার বিস্তারিত বিবরণ *") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = isActive,
                        onCheckedChange = { isActive = it }
                    )
                    Text("সেবাটি বর্তমানে চালু আছে")
                }
            }
        },
        confirmButton = {
            SevaButton(
                text = if (service != null) "আপডেট করুন" else "সংরক্ষণ করুন",
                onClick = {
                    if (titleBn.isNotBlank()) {
                        onSave(
                            ServiceItem(
                                id = service?.id ?: "",
                                title = titleBn,
                                titleBn = titleBn,
                                description = description,
                                categoryKey = titleBn,
                                isActive = isActive
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
