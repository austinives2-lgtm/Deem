package com.example.ui.screens.profile

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.UserModel
import com.example.ui.MainViewModel
import com.example.ui.PanelMode
import com.example.ui.components.ImageUrlPickerField
import com.example.ui.components.NeumorphicCard
import com.example.ui.components.SevaButton
import com.example.ui.components.StatusBadge
import com.example.ui.theme.SevaBloodRed
import com.example.ui.theme.SevaGoldAccent
import com.example.ui.theme.SevaGreenPrimary

@Composable
fun ProfileScreen(
    viewModel: MainViewModel,
    onNavigateToAdminDashboard: () -> Unit,
    onLoggedOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    var showEditDialog by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Profile Avatar Card
        Box(
            modifier = Modifier
                .size(100.dp)
                .shadow(6.dp, CircleShape)
                .clip(CircleShape)
                .background(SevaGreenPrimary.copy(alpha = 0.15f))
                .border(2.dp, SevaGreenPrimary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (!currentUser?.photoURL.isNullOrBlank()) {
                AsyncImage(
                    model = currentUser?.photoURL,
                    contentDescription = currentUser?.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = null,
                    tint = SevaGreenPrimary,
                    modifier = Modifier.size(52.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = currentUser?.name ?: "সদস্য",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        )

        Text(
            text = currentUser?.email ?: "",
            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val roleBn = when (currentUser?.role) {
                "super_admin" -> "★ সুপার অ্যাডমিন"
                "admin" -> "♦ অ্যাডমিন"
                else -> "সাধারণ সদস্য"
            }
            StatusBadge(
                status = roleBn,
                customColor = if (currentUser?.isAdmin() == true) SevaGoldAccent else SevaGreenPrimary
            )
            if (!currentUser?.bloodGroup.isNullOrBlank()) {
                StatusBadge(status = "রক্ত: ${currentUser?.bloodGroup}", customColor = SevaBloodRed)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Profile Details Card
        NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "ব্যক্তিগত তথ্য",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(12.dp))

            ProfileInfoRow(label = "মোবাইল নম্বর", value = currentUser?.phone.takeIf { !it.isNullOrBlank() } ?: "দেওয়া হয়নি")
            ProfileInfoRow(label = "সাংগঠনিক পদবি", value = currentUser?.position.takeIf { !it.isNullOrBlank() } ?: "সদস্য")
            ProfileInfoRow(label = "ঠিকানা / এলাকা", value = currentUser?.address.takeIf { !it.isNullOrBlank() } ?: "বাংলাদেশ")
            ProfileInfoRow(label = "রক্তদানের প্রস্তুতি", value = if (currentUser?.isAvailableForBlood == true) "সক্রিয় রক্তদাতা" else "অনুপলব্ধ")

            Spacer(modifier = Modifier.height(10.dp))

            SevaButton(
                text = "তথ্য সম্পাদন করুন",
                onClick = { showEditDialog = true },
                isPrimary = false,
                icon = Icons.Outlined.Edit,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Admin Panel Switch Card (if Admin)
        if (currentUser?.isAdmin() == true) {
            Spacer(modifier = Modifier.height(16.dp))
            NeumorphicCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = SevaGreenPrimary.copy(alpha = 0.05f),
                borderColor = SevaGreenPrimary.copy(alpha = 0.3f),
                onClick = {
                    viewModel.switchPanel(PanelMode.ADMIN)
                    onNavigateToAdminDashboard()
                }
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
                        Icon(Icons.Filled.AdminPanelSettings, contentDescription = null, tint = SevaGreenPrimary)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "অ্যাডমিন ড্যাশবোর্ড",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = SevaGreenPrimary
                            )
                        )
                        Text(
                            text = "সংগঠনের সামগ্রিক পরিচালনা ও হিসাব ব্যবস্থাপনা",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = SevaGreenPrimary)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Organization Info Card
        NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.org_logo),
                    contentDescription = null,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("বাংলাদেশ সেবামূলক সংগঠন", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("মানুষের পাশে, মানবতার জন্য", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Logout Button
        SevaButton(
            text = "লগআউট করুন",
            onClick = {
                viewModel.logout(onLoggedOut)
            },
            isPrimary = false,
            icon = Icons.Outlined.Logout,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.error,
            modifier = Modifier.fillMaxWidth().testTag("logout_button")
        )

        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showEditDialog) {
        EditProfileDialog(
            currentUser = currentUser,
            onDismiss = { showEditDialog = false },
            onSave = { name, phone, bg, addr, photo, avail ->
                viewModel.updateProfile(name, phone, bg, addr, photo, avail)
                showEditDialog = false
            }
        )
    }
}

@Composable
fun ProfileInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
        Text(text = value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
    }
}

@Composable
fun EditProfileDialog(
    currentUser: UserModel?,
    onDismiss: () -> Unit,
    onSave: (name: String, phone: String, bloodGroup: String, address: String, photoURL: String, isAvailable: Boolean) -> Unit
) {
    var name by remember { mutableStateOf(currentUser?.name ?: "") }
    var phone by remember { mutableStateOf(currentUser?.phone ?: "") }
    var bloodGroup by remember { mutableStateOf(currentUser?.bloodGroup ?: "A+") }
    var address by remember { mutableStateOf(currentUser?.address ?: "") }
    var photoURL by remember { mutableStateOf(currentUser?.photoURL ?: "") }
    var isAvailable by remember { mutableStateOf(currentUser?.isAvailableForBlood ?: true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("প্রোফাইল সম্পাদন") },
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
                    label = { Text("পূর্ণ নাম") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("মোবাইল নম্বর") },
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

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("ঠিকানা") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                ImageUrlPickerField(
                    imageUrl = photoURL,
                    onImageUrlChange = { photoURL = it },
                    label = "প্রোফাইল ছবির লিংক"
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = isAvailable,
                        onCheckedChange = { isAvailable = it }
                    )
                    Text("রক্তদানে প্রস্তুত আছি")
                }
            }
        },
        confirmButton = {
            SevaButton(
                text = "সংরক্ষণ করুন",
                onClick = {
                    onSave(name, phone, bloodGroup, address, photoURL, isAvailable)
                }
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("বাতিল") }
        }
    )
}
