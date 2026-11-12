package com.example.ui.screens.home

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.ActivityItem
import com.example.data.model.BloodRequest
import com.example.data.model.ServiceItem
import com.example.ui.MainViewModel
import com.example.ui.components.EmptyStateView
import com.example.ui.components.NeumorphicCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.SevaBloodRed
import com.example.ui.theme.SevaGoldAccent
import com.example.ui.theme.SevaGreenPrimary
import com.example.ui.theme.SevaGreenSecondary
import java.text.NumberFormat
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToServices: () -> Unit,
    onNavigateToActivities: () -> Unit,
    onNavigateToBlood: () -> Unit,
    onNavigateToFinance: () -> Unit,
    onNavigateToNotices: () -> Unit,
    onServiceSelected: (ServiceItem) -> Unit,
    onActivitySelected: (ActivityItem) -> Unit,
    onRequestBloodClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val services by viewModel.services.collectAsState()
    val activities by viewModel.activities.collectAsState()
    val bloodRequests by viewModel.bloodRequests.collectAsState()
    val members by viewModel.members.collectAsState()
    val financeSummary by viewModel.financialSummary.collectAsState()

    val currencyFormatter = remember { NumberFormat.getNumberInstance(Locale.US) }
    val scrollState = rememberScrollState()

    val activeBloodRequests = remember(bloodRequests) {
        bloodRequests.filter { it.status.equals("Active", ignoreCase = true) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = 24.dp)
    ) {
        // Hero Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .shadow(6.dp, RoundedCornerShape(20.dp))
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF0F7A3E),
                            Color(0xFF166534),
                            Color(0xFF064E3B)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.org_logo),
                        contentDescription = "লোগো",
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.5.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(14.dp)),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = "বাংলাদেশ সেবামূলক সংগঠন",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 17.sp
                            )
                        )
                        Text(
                            text = "মানুষের পাশে, মানবতার জন্য",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFFE2E8F0),
                                fontSize = 12.sp
                            )
                        )
                        if (currentUser != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "স্বাগতম, ${currentUser?.name ?: "সদস্য"}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = SevaGoldAccent,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Live Stats in Hero
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatMiniChip(
                        title = "মোট সদস্য",
                        value = "${members.size} জন",
                        icon = Icons.Outlined.Group
                    )
                    StatMiniChip(
                        title = "রক্তদান আবেদন",
                        value = "${activeBloodRequests.size} টি",
                        icon = Icons.Outlined.WaterDrop
                    )
                    StatMiniChip(
                        title = "তহবিল স্থিতি",
                        value = "৳${currencyFormatter.format(financeSummary.currentBalance.toLong())}",
                        icon = Icons.Outlined.AccountBalanceWallet
                    )
                }
            }
        }

        // Quick Actions Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            QuickActionButton(
                label = "রক্তের আবেদন",
                icon = Icons.Outlined.Bloodtype,
                color = SevaBloodRed,
                onClick = onRequestBloodClick,
                modifier = Modifier.weight(1f)
            )
            QuickActionButton(
                label = "সকল সেবা",
                icon = Icons.Outlined.VolunteerActivism,
                color = SevaGreenPrimary,
                onClick = onNavigateToServices,
                modifier = Modifier.weight(1f)
            )
            QuickActionButton(
                label = "তহবিল হিসাব",
                icon = Icons.Outlined.Savings,
                color = SevaGoldAccent,
                onClick = onNavigateToFinance,
                modifier = Modifier.weight(1f)
            )
        }

        // Urgent Blood Request Notice (if active requests exist)
        if (activeBloodRequests.isNotEmpty()) {
            Spacer(modifier = Modifier.height(20.dp))
            SectionHeader(
                title = "জরুরি রক্তের আবেদন",
                actionText = "সকল আবেদন",
                onActionClick = onNavigateToBlood
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(activeBloodRequests.take(5)) { req ->
                    BloodRequestCard(
                        request = req,
                        onClick = onNavigateToBlood
                    )
                }
            }
        }

        // Humanitarian Services Section
        Spacer(modifier = Modifier.height(24.dp))
        SectionHeader(
            title = "মানবিক সেবাসমূহ",
            actionText = "সকল সেবা দেখুন",
            onActionClick = onNavigateToServices
        )
        Spacer(modifier = Modifier.height(10.dp))

        if (services.isEmpty()) {
            EmptyStateView(
                title = "কোনো সেবা পাওয়া যায়নি",
                description = "এখনও কোনো সেবা তালিকাভুক্ত করা হয়নি।",
                modifier = Modifier.padding(16.dp)
            )
        } else {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(services.take(8)) { service ->
                    ServicePillCard(
                        service = service,
                        onClick = { onServiceSelected(service) }
                    )
                }
            }
        }

        // Recent Humanitarian Activities Section
        Spacer(modifier = Modifier.height(24.dp))
        SectionHeader(
            title = "সাম্প্রতিক মানবিক কার্যক্রম",
            actionText = "সকল কার্যক্রম",
            onActionClick = onNavigateToActivities
        )
        Spacer(modifier = Modifier.height(10.dp))

        if (activities.isEmpty()) {
            EmptyStateView(
                title = "এখনও কোনো কার্যক্রম শুরু হয়নি",
                description = "শীঘ্রই নতুন সামাজিক ও মানবিক কর্মসূচি শুরু হবে ইনশাআল্লাহ।",
                modifier = Modifier.padding(16.dp)
            )
        } else {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                activities.take(3).forEach { act ->
                    ActivityListItem(
                        activity = act,
                        onClick = { onActivitySelected(act) }
                    )
                }
            }
        }
    }
}

@Composable
private fun StatMiniChip(
    title: String,
    value: String,
    icon: ImageVector
) {
    Surface(
        color = Color.White.copy(alpha = 0.12f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color(0xFFD1D5DB),
                        fontSize = 9.sp
                    )
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun QuickActionButton(
    label: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    NeumorphicCard(
        modifier = modifier,
        shapeRadius = 14.dp,
        onClick = onClick
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(color.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp
                ),
                maxLines = 1
            )
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        )
        if (actionText != null && onActionClick != null) {
            Text(
                text = actionText,
                style = MaterialTheme.typography.labelMedium.copy(
                    color = SevaGreenPrimary,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.clickable { onActionClick() }
            )
        }
    }
}

@Composable
fun BloodRequestCard(
    request: BloodRequest,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .width(260.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, SevaBloodRed.copy(alpha = 0.3f)),
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SevaBloodRed
                ) {
                    Text(
                        text = request.bloodGroup,
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                StatusBadge(status = request.urgency)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "রোগী: ${request.patientName}",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "হাসপাতাল: ${request.hospital}",
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "প্রয়োজন: ${request.units} ব্যাগ",
                    style = MaterialTheme.typography.labelSmall.copy(color = SevaBloodRed, fontWeight = FontWeight.Bold)
                )
                Text(
                    text = request.requiredDate,
                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }
        }
    }
}

@Composable
fun ServicePillCard(
    service: ServiceItem,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .width(160.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(SevaGreenPrimary.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.VolunteerActivism,
                    contentDescription = null,
                    tint = SevaGreenPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = service.titleBn.ifBlank { service.title },
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = service.description,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun ActivityListItem(
    activity: ActivityItem,
    onClick: () -> Unit
) {
    NeumorphicCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (activity.imageUrl.isNotBlank()) {
                AsyncImage(
                    model = activity.imageUrl,
                    contentDescription = activity.title,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(12.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    StatusBadge(status = activity.status)
                    Text(
                        text = activity.date,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = activity.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = activity.description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "উপকারভোগী: ${activity.beneficiaries} জন",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = SevaGreenPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    if (activity.location.isNotBlank()) {
                        Text(
                            text = "📍 ${activity.location}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }
    }
}
