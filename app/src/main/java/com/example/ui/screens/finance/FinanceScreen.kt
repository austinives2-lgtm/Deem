package com.example.ui.screens.finance

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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Contribution
import com.example.data.model.Donation
import com.example.data.model.Expense
import com.example.ui.MainViewModel
import com.example.ui.components.EmptyStateView
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
fun FinanceScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val contributions by viewModel.contributions.collectAsState()
    val donations by viewModel.donations.collectAsState()
    val expenses by viewModel.expenses.collectAsState()
    val summary by viewModel.financialSummary.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val isAdmin = currentUser?.isAdmin() == true

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("সদস্য চাঁদা", "সাধারণ অনুদান", "ব্যয় হিসাব")

    var showAddContributionDialog by remember { mutableStateOf(false) }
    var showAddDonationDialog by remember { mutableStateOf(false) }
    var showAddExpenseDialog by remember { mutableStateOf(false) }

    val currencyFormatter = remember { NumberFormat.getNumberInstance(Locale.US) }

    Scaffold(
        floatingActionButton = {
            if (isAdmin) {
                FloatingActionButton(
                    onClick = {
                        when (selectedTabIndex) {
                            0 -> showAddContributionDialog = true
                            1 -> showAddDonationDialog = true
                            2 -> showAddExpenseDialog = true
                        }
                    },
                    containerColor = SevaGreenPrimary,
                    contentColor = Color.White
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = when (selectedTabIndex) {
                                0 -> "চাঁদা এন্ট্রি"
                                1 -> "অনুদান যোগ"
                                else -> "ব্যয় এন্ট্রি"
                            },
                            fontWeight = FontWeight.Bold
                        )
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
            // Live Summary Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .shadow(4.dp, RoundedCornerShape(18.dp))
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF134E4A),
                                Color(0xFF0F7A3E)
                            )
                        )
                    )
                    .padding(18.dp)
            ) {
                Column {
                    Text(
                        text = "স্বচ্ছ আর্থিক হিসাব ও স্থিতি",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color(0xFFD1FAE5),
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("মোট সদস্য চাঁদা", style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFE2E8F0)))
                            Text("৳${currencyFormatter.format(summary.totalContributions.toLong())}", style = MaterialTheme.typography.titleSmall.copy(color = Color.White, fontWeight = FontWeight.Bold))
                        }
                        Column {
                            Text("মোট অনুদান", style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFE2E8F0)))
                            Text("৳${currencyFormatter.format(summary.totalDonations.toLong())}", style = MaterialTheme.typography.titleSmall.copy(color = SevaGoldAccent, fontWeight = FontWeight.Bold))
                        }
                        Column {
                            Text("মোট ব্যয়", style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFE2E8F0)))
                            Text("৳${currencyFormatter.format(summary.totalExpenses.toLong())}", style = MaterialTheme.typography.titleSmall.copy(color = Color(0xFFFCA5A5), fontWeight = FontWeight.Bold))
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("বর্তমান তহবিল স্থিতি (Balance):", style = MaterialTheme.typography.titleSmall.copy(color = Color.White, fontWeight = FontWeight.Medium))
                        Text(
                            text = "৳${currencyFormatter.format(summary.currentBalance.toLong())}",
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = if (summary.currentBalance >= 0) Color(0xFF86EFAC) else Color(0xFFFCA5A5),
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        )
                    }
                }
            }

            // Tab Row
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = SevaGreenPrimary
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

            // Tab Content
            when (selectedTabIndex) {
                0 -> {
                    // Member Contributions
                    if (contributions.isEmpty()) {
                        EmptyStateView(
                            title = "কোনো চাঁদার রেকর্ড নেই",
                            description = "এখনও কোনো সদস্যের চাঁদা রেকর্ড করা হয়নি।",
                            icon = Icons.Outlined.ReceiptLong,
                            actionButtonText = if (isAdmin) "চাঁদা এন্ট্রি করুন" else null,
                            onActionClick = if (isAdmin) { { showAddContributionDialog = true } } else null,
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(contributions) { item ->
                                ContributionCard(item = item)
                            }
                        }
                    }
                }
                1 -> {
                    // Donations
                    if (donations.isEmpty()) {
                        EmptyStateView(
                            title = "কোনো অনুদানের রেকর্ড নেই",
                            description = "এখনও সাধারণ অনুদানের কোনো হিসাব এন্ট্রি করা হয়নি।",
                            icon = Icons.Outlined.Savings,
                            actionButtonText = if (isAdmin) "অনুদান রেকর্ড করুন" else null,
                            onActionClick = if (isAdmin) { { showAddDonationDialog = true } } else null,
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(donations) { don ->
                                DonationCard(item = don)
                            }
                        }
                    }
                }
                2 -> {
                    // Expenses
                    if (expenses.isEmpty()) {
                        EmptyStateView(
                            title = "কোনো ব্যয়ের হিসাব নেই",
                            description = "এখনও কোনো খরচের হিসাব রেকর্ড করা হয়নি।",
                            icon = Icons.Outlined.Payments,
                            actionButtonText = if (isAdmin) "ব্যয় এন্ট্রি করুন" else null,
                            onActionClick = if (isAdmin) { { showAddExpenseDialog = true } } else null,
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(expenses) { exp ->
                                ExpenseCard(item = exp)
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialogs
    if (showAddContributionDialog) {
        AddContributionDialog(
            currentUser = currentUser,
            onDismiss = { showAddContributionDialog = false },
            onSubmit = { item ->
                viewModel.addContribution(item) {
                    showAddContributionDialog = false
                }
            }
        )
    }

    if (showAddDonationDialog) {
        AddDonationDialog(
            currentUser = currentUser,
            onDismiss = { showAddDonationDialog = false },
            onSubmit = { don ->
                viewModel.addDonation(don) {
                    showAddDonationDialog = false
                }
            }
        )
    }

    if (showAddExpenseDialog) {
        AddExpenseDialog(
            currentUser = currentUser,
            onDismiss = { showAddExpenseDialog = false },
            onSubmit = { exp ->
                viewModel.addExpense(exp) {
                    showAddExpenseDialog = false
                }
            }
        )
    }
}

@Composable
fun ContributionCard(item: Contribution) {
    NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = item.memberName,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "মাস: ${item.month}",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = SevaGreenPrimary.copy(alpha = 0.12f)
            ) {
                Text(
                    text = "৳${item.paidAmount.toLong()}",
                    color = SevaGreenPrimary,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("প্রত্যাশিত: ৳${item.expectedAmount.toLong()}", style = MaterialTheme.typography.bodySmall)
            if (item.dueAmount > 0) {
                Text("বকেয়া: ৳${item.dueAmount.toLong()}", style = MaterialTheme.typography.bodySmall.copy(color = SevaBloodRed, fontWeight = FontWeight.Bold))
            } else {
                Text("বকেয়া নেই", style = MaterialTheme.typography.bodySmall.copy(color = SevaGreenPrimary))
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("মাধ্যম: ${item.paymentMethod}", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
            Text("তারিখ: ${item.paymentDate}", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
        }
    }
}

@Composable
fun DonationCard(item: Donation) {
    NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = item.donorName,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "উদ্দেশ্য: ${item.purpose}",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }

            Text(
                text = "৳${item.amount.toLong()}",
                color = SevaGoldAccent,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (item.phone.isNotBlank()) {
                Text("ফোন: ${item.phone}", style = MaterialTheme.typography.bodySmall)
            }
            Text("তারিখ: ${item.date}", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
        }
    }
}

@Composable
fun ExpenseCard(item: Expense) {
    NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "খাত: ${item.category}",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }

            Text(
                text = "-৳${item.amount.toLong()}",
                color = SevaBloodRed,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (item.authorizedBy.isNotBlank()) {
                Text("অনুমোদনে: ${item.authorizedBy}", style = MaterialTheme.typography.bodySmall)
            }
            Text("তারিখ: ${item.date}", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
        }
    }
}

@Composable
fun AddContributionDialog(
    currentUser: com.example.data.model.UserModel?,
    onDismiss: () -> Unit,
    onSubmit: (Contribution) -> Unit
) {
    var memberName by remember { mutableStateOf("") }
    var expectedAmount by remember { mutableStateOf("500") }
    var paidAmount by remember { mutableStateOf("500") }
    var month by remember { mutableStateOf("নভেম্বর ২০২৬") }
    var paymentDate by remember { mutableStateOf("১২ নভেম্বর ২০২৬") }
    var paymentMethod by remember { mutableStateOf("বিকাশ") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("সদস্য মাসিক চাঁদা এন্ট্রি") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = memberName,
                    onValueChange = { memberName = it },
                    label = { Text("সদস্যের নাম *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = month,
                    onValueChange = { month = it },
                    label = { Text("চাঁদার মাস *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = expectedAmount,
                        onValueChange = { expectedAmount = it.filter { ch -> ch.isDigit() } },
                        label = { Text("ধার্য চাঁদা (৳)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = paidAmount,
                        onValueChange = { paidAmount = it.filter { ch -> ch.isDigit() } },
                        label = { Text("পরিশোধ (৳)") },
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = paymentMethod,
                    onValueChange = { paymentMethod = it },
                    label = { Text("পেমেন্ট মাধ্যম *") },
                    placeholder = { Text("যেমন: বিকাশ, নগদ, ব্যাংক, নগদ টাকা") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = paymentDate,
                    onValueChange = { paymentDate = it },
                    label = { Text("পরিশোধের তারিখ *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            SevaButton(
                text = "সংরক্ষণ করুন",
                onClick = {
                    val exp = expectedAmount.toDoubleOrNull() ?: 0.0
                    val paid = paidAmount.toDoubleOrNull() ?: 0.0
                    val due = if (exp > paid) exp - paid else 0.0
                    if (memberName.isNotBlank()) {
                        onSubmit(
                            Contribution(
                                memberName = memberName.trim(),
                                expectedAmount = exp,
                                paidAmount = paid,
                                dueAmount = due,
                                month = month.trim(),
                                paymentDate = paymentDate.trim(),
                                paymentMethod = paymentMethod.trim(),
                                notes = notes.trim(),
                                recordedBy = currentUser?.name ?: "অ্যাডমিন"
                            )
                        )
                    }
                }
            )
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("বাতিল") } }
    )
}

@Composable
fun AddDonationDialog(
    currentUser: com.example.data.model.UserModel?,
    onDismiss: () -> Unit,
    onSubmit: (Donation) -> Unit
) {
    var donorName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var purpose by remember { mutableStateOf("সাধারণ তহবিল") }
    var date by remember { mutableStateOf("১২ নভেম্বর ২০২৬") }
    var receiptNumber by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("সাধারণ অনুদান লিপিবদ্ধকরণ") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = donorName,
                    onValueChange = { donorName = it },
                    label = { Text("দানকারীর নাম *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("ফোন নম্বর") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it.filter { ch -> ch.isDigit() } },
                    label = { Text("অনুদানের পরিমাণ (টাকা) *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = purpose,
                    onValueChange = { purpose = it },
                    label = { Text("অনুদানের খাত / উদ্দেশ্য") },
                    placeholder = { Text("যেমন: খাদ্য বিতরণ, শীতবস্ত্র, সাধারণ তহবিল") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
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
                text = "অনুদান সংরক্ষণ",
                onClick = {
                    val amt = amount.toDoubleOrNull() ?: 0.0
                    if (donorName.isNotBlank() && amt > 0) {
                        onSubmit(
                            Donation(
                                donorName = donorName.trim(),
                                phone = phone.trim(),
                                amount = amt,
                                purpose = purpose.trim(),
                                date = date.trim(),
                                receiptNumber = receiptNumber.trim(),
                                recordedBy = currentUser?.name ?: "অ্যাডমিন"
                            )
                        )
                    }
                }
            )
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("বাতিল") } }
    )
}

@Composable
fun AddExpenseDialog(
    currentUser: com.example.data.model.UserModel?,
    onDismiss: () -> Unit,
    onSubmit: (Expense) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("ত্রাণ সামগ্রী ক্রয়") }
    var amount by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("১২ নভেম্বর ২০২৬") }
    var authorizedBy by remember { mutableStateOf(currentUser?.name ?: "সভাপতি / সাধারণ সম্পাদক") }
    var voucherNumber by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("ব্যয়ের হিসাব এন্ট্রি") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("ব্যয়ের শিরোনাম / বিবরণ *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("ব্যয়ের খাত *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it.filter { ch -> ch.isDigit() } },
                    label = { Text("ব্যয়ের পরিমাণ (টাকা) *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = authorizedBy,
                    onValueChange = { authorizedBy = it },
                    label = { Text("অনুমোদনকারী কর্মকর্তা") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
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
                text = "ব্যয় সংরক্ষণ",
                onClick = {
                    val amt = amount.toDoubleOrNull() ?: 0.0
                    if (title.isNotBlank() && amt > 0) {
                        onSubmit(
                            Expense(
                                title = title.trim(),
                                category = category.trim(),
                                amount = amt,
                                date = date.trim(),
                                authorizedBy = authorizedBy.trim(),
                                voucherNumber = voucherNumber.trim(),
                                recordedBy = currentUser?.name ?: "অ্যাডমিন"
                            )
                        )
                    }
                }
            )
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("বাতিল") } }
    )
}
