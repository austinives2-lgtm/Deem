package com.example.ui.screens.admin

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ServiceRequest
import com.example.ui.MainViewModel
import com.example.ui.PanelMode
import com.example.ui.components.NeumorphicCard
import com.example.ui.components.SevaButton
import com.example.ui.components.StatusBadge
import com.example.ui.theme.SevaBloodRed
import com.example.ui.theme.SevaGoldAccent
import com.example.ui.theme.SevaGreenPrimary
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    viewModel: MainViewModel,
    onBackToUserPanel: () -> Unit,
    onNavigateToServices: () -> Unit,
    onNavigateToActivities: () -> Unit,
    onNavigateToBlood: () -> Unit,
    onNavigateToFinance: () -> Unit,
    onNavigateToNotices: () -> Unit,
    onNavigateToMembers: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val isSuperAdmin = currentUser?.isSuperAdmin() == true

    val members by viewModel.members.collectAsState()
    val volunteers by viewModel.volunteers.collectAsState()
    val services by viewModel.services.collectAsState()
    val activities by viewModel.activities.collectAsState()
    val bloodRequests by viewModel.bloodRequests.collectAsState()
    val serviceRequests by viewModel.serviceRequests.collectAsState()
    val financeSummary by viewModel.financialSummary.collectAsState()

    val currencyFormatter = remember { NumberFormat.getNumberInstance(Locale.US) }
    val scrollState = rememberScrollState()

    val pendingRequests = remember(serviceRequests) {
        serviceRequests.filter { it.status == "অপেক্ষমাণ" }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("অ্যাডমিন ড্যাশবোর্ড", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(
                            text = if (isSuperAdmin) "সুপার অ্যাডমিন কন্ট্রোল" else "অ্যাডমিন প্যানেল",
                            style = MaterialTheme.typography.labelSmall,
                            color = SevaGoldAccent
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.switchPanel(PanelMode.USER)
                        onBackToUserPanel()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "ইউজার প্যানেলে ফিরুন")
                    }
                },
                actions = {
                    TextButton(onClick = {
                        viewModel.switchPanel(PanelMode.USER)
                        onBackToUserPanel()
                    }) {
                        Text("ইউজার ভিউ", color = SevaGreenPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // Super Admin Badge / Warning Banner
            if (isSuperAdmin) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SevaGoldAccent.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, SevaGoldAccent.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Stars, contentDescription = null, tint = SevaGoldAccent)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("সুপার অ্যাডমিন হিসেবে লগইন আছেন", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("আপনি সদস্য ব্যবস্থাপনা থেকে যে কাউকে অ্যাডমিন করতে বা পদবি নির্ধারণ করতে পারেন।", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Real Statistics Grid
            Text(
                text = "সংগঠনের বর্তমান পরিসংখ্যান (লাইভ ফায়ারস্টোর)",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Stat Cards Row 1
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AdminStatCard(
                    title = "মোট সদস্য",
                    value = "${members.size} জন",
                    icon = Icons.Outlined.Group,
                    color = SevaGreenPrimary,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToMembers
                )
                AdminStatCard(
                    title = "স্বেচ্ছাসেবক",
                    value = "${volunteers.size} জন",
                    icon = Icons.Outlined.VolunteerActivism,
                    color = SevaGreenPrimary,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToMembers
                )
                AdminStatCard(
                    title = "সক্রিয় সেবা",
                    value = "${services.count { it.isActive }} টি",
                    icon = Icons.Outlined.HomeRepairService,
                    color = SevaGreenPrimary,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToServices
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Stat Cards Row 2
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AdminStatCard(
                    title = "সম্পন্ন কর্মসূচি",
                    value = "${activities.count { it.status.equals("Completed", true) }} টি",
                    icon = Icons.Outlined.CheckCircle,
                    color = SevaGreenPrimary,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToActivities
                )
                AdminStatCard(
                    title = "রক্তের আবেদন",
                    value = "${bloodRequests.count { it.status.equals("Active", true) }} টি",
                    icon = Icons.Outlined.WaterDrop,
                    color = SevaBloodRed,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToBlood
                )
                AdminStatCard(
                    title = "সেবা আবেদন",
                    value = "${pendingRequests.size} টি অপেক্ষমাণ",
                    icon = Icons.Outlined.PendingActions,
                    color = SevaGoldAccent,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Financial Balance Overview Card
            NeumorphicCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = onNavigateToFinance
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("তহবিল ব্যালেন্স (লাইভ হিসাব)", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                        Text(
                            text = "৳${currencyFormatter.format(financeSummary.currentBalance.toLong())}",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (financeSummary.currentBalance >= 0) SevaGreenPrimary else SevaBloodRed,
                                fontSize = 22.sp
                            )
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("মোট চাঁদা: ৳${currencyFormatter.format(financeSummary.totalContributions.toLong())}", style = MaterialTheme.typography.labelSmall)
                        Text("মোট অনুদান: ৳${currencyFormatter.format(financeSummary.totalDonations.toLong())}", style = MaterialTheme.typography.labelSmall.copy(color = SevaGoldAccent))
                        Text("মোট ব্যয়: ৳${currencyFormatter.format(financeSummary.totalExpenses.toLong())}", style = MaterialTheme.typography.labelSmall.copy(color = SevaBloodRed))
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Quick Actions Section
            Text(
                text = "দ্রুত অ্যাকশন (Quick Actions)",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AdminQuickActionButton(
                    label = "কার্যক্রম",
                    icon = Icons.Outlined.Campaign,
                    onClick = onNavigateToActivities,
                    modifier = Modifier.weight(1f)
                )
                AdminQuickActionButton(
                    label = "রক্তের আবেদন",
                    icon = Icons.Outlined.Bloodtype,
                    onClick = onNavigateToBlood,
                    modifier = Modifier.weight(1f)
                )
                AdminQuickActionButton(
                    label = "চাঁদা/অনুদান",
                    icon = Icons.Outlined.Savings,
                    onClick = onNavigateToFinance,
                    modifier = Modifier.weight(1f)
                )
                AdminQuickActionButton(
                    label = "নোটিশ প্রকাশ",
                    icon = Icons.Outlined.NotificationAdd,
                    onClick = onNavigateToNotices,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Pending Service Requests Review
            Text(
                text = "অপেক্ষমাণ সেবা আবেদনসমূহ (${pendingRequests.size})",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (pendingRequests.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
                        Text("বর্তমানে কোনো অপেক্ষমাণ আবেদন নেই।", style = MaterialTheme.typography.bodySmall)
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    pendingRequests.take(4).forEach { req ->
                        PendingRequestItemCard(
                            request = req,
                            onApprove = { viewModel.updateServiceRequestStatus(req.id, "অনুমোদিত") },
                            onReject = { viewModel.updateServiceRequestStatus(req.id, "বাতিল") }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun AdminStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    NeumorphicCard(
        modifier = modifier,
        shapeRadius = 12.dp,
        onClick = onClick
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = title, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant), maxLines = 1)
            Text(text = value, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 12.sp), maxLines = 1)
        }
    }
}

@Composable
fun AdminQuickActionButton(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    NeumorphicCard(
        modifier = modifier,
        shapeRadius = 12.dp,
        onClick = onClick
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = SevaGreenPrimary, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = label, style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold), maxLines = 1)
        }
    }
}

@Composable
fun PendingRequestItemCard(
    request: ServiceRequest,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(request.serviceTitle, fontWeight = FontWeight.Bold)
            StatusBadge(status = request.status)
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text("আবেদনকারী: ${request.applicantName} (${request.phone})", style = MaterialTheme.typography.bodySmall)
        Text("ঠিকানা: ${request.location}", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
        Text("বিবরণ: ${request.details}", style = MaterialTheme.typography.bodySmall)

        Spacer(modifier = Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SevaButton(
                text = "অনুমোদন করুন",
                onClick = onApprove,
                isPrimary = true,
                modifier = Modifier.weight(1f)
            )
            OutlinedButton(
                onClick = onReject,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                modifier = Modifier.weight(1f)
            ) {
                Text("বাতিল", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
