package com.example.rentmg.pages.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

// Data classes
data class TenantInfo(
    val name: String,
    val propertyName: String,
    val houseNumber: String,
    val bedrooms: Int,
    val monthlyRent: Double,
    val dueDate: Date,
    val isPaid: Boolean,
    val balance: Double,
    val lastPaymentDate: Date?
)

// Color Palette
object AppColors {
    val Primary = Color(0xFFE53935) // Red
    val PrimaryLight = Color(0xFFFFEBEE)
    val Background = Color(0xFFFAFAFA)
    val Surface = Color.White
    val TextPrimary = Color(0xFF212121)
    val TextSecondary = Color(0xFF757575)
    val Success = Color(0xFF4CAF50)
    val Warning = Color(0xFFFF9800)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TenantDashboard() {
    // Sample tenant data
    val tenant = remember {
        TenantInfo(
            name = "Sophie Mwangi",
            propertyName = "Greenview Apartments",
            houseNumber = "A-102",
            bedrooms = 2,
            monthlyRent = 25000.0,
            dueDate = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_MONTH, 5)
            }.time,
            isPaid = false,
            balance = 25000.0,
            lastPaymentDate = Calendar.getInstance().apply {
                add(Calendar.MONTH, -1)
            }.time
        )
    }

    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "KE"))

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Dashboard",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppColors.Surface,
                    titleContentColor = AppColors.TextPrimary
                ),
                actions = {
                    IconButton(onClick = { /* Notifications */ }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigationBar()
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.Background)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Greeting
            Text(
                text = "Hi, ${tenant.name.split(" ")[0]}",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.TextPrimary
            )

            Text(
                text = "Good ${getGreeting()}",
                fontSize = 14.sp,
                color = AppColors.TextSecondary
            )

            // Rent Status Card
            RentStatusCard(
                isPaid = tenant.isPaid,
                amount = tenant.balance,
                dueDate = dateFormat.format(tenant.dueDate),
                currencyFormat = currencyFormat
            )

            // Property Details Section
            Text(
                text = "Property Details",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.TextPrimary
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DetailRow(
                        icon = Icons.Default.Home,
                        label = "Property",
                        value = tenant.propertyName
                    )

                    DetailRow(
                        icon = Icons.Default.Apartment,
                        label = "House Number",
                        value = tenant.houseNumber
                    )

                    DetailRow(
                        icon = Icons.Default.Bed,
                        label = "Bedrooms",
                        value = "${tenant.bedrooms} Bedroom"
                    )

                    HorizontalDivider(color = AppColors.Background)

                    DetailRow(
                        icon = Icons.Default.CalendarMonth,
                        label = "Due Date",
                        value = "Every ${Calendar.getInstance().get(Calendar.DAY_OF_MONTH)}${getDaySuffix(Calendar.getInstance().get(Calendar.DAY_OF_MONTH))} of the month"
                    )

                    DetailRow(
                        icon = Icons.Default.AccountBalanceWallet,
                        label = "Monthly Rent",
                        value = currencyFormat.format(tenant.monthlyRent)
                    )
                }
            }

            // Payment History
            Text(
                text = "Recent Payment",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.TextPrimary
            )

            tenant.lastPaymentDate?.let { lastPayment ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = AppColors.Success,
                                modifier = Modifier.size(40.dp)
                            )

                            Column {
                                Text(
                                    text = "Paid via M-Pesa",
                                    fontWeight = FontWeight.Medium,
                                    color = AppColors.TextPrimary
                                )
                                Text(
                                    text = dateFormat.format(lastPayment),
                                    fontSize = 12.sp,
                                    color = AppColors.TextSecondary
                                )
                            }
                        }

                        Text(
                            text = currencyFormat.format(tenant.monthlyRent),
                            fontWeight = FontWeight.Bold,
                            color = AppColors.Success
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun RentStatusCard(
    isPaid: Boolean,
    amount: Double,
    dueDate: String,
    currencyFormat: NumberFormat
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isPaid) AppColors.Success else AppColors.Primary
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isPaid) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = if (isPaid) "Rent Paid" else "Rent Overdue",
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }

                Surface(
                    color = Color.White.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = if (isPaid) "Paid" else "Overdue",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            if (!isPaid) {
                Text(
                    text = "Payment Must Be Made On Or Before:",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 12.sp
                )

                Text(
                    text = dueDate,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { /* Navigate to payment */ },
                    modifier = Modifier.align(Alignment.End),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.TextPrimary
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Proceed to Payment")
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .size(18.dp)
                    )
                }
            } else {
                Text(
                    text = "Thank you for paying on time!",
                    color = Color.White,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun DetailRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AppColors.Primary,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = label,
                color = AppColors.TextSecondary,
                fontSize = 14.sp
            )
        }

        Text(
            text = value,
            color = AppColors.TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun BottomNavigationBar() {
    NavigationBar(
        containerColor = AppColors.Surface,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = true,
            onClick = { },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = AppColors.Primary,
                selectedTextColor = AppColors.Primary,
                indicatorColor = AppColors.PrimaryLight
            )
        )

        NavigationBarItem(
            selected = false,
            onClick = { },
            icon = { Icon(Icons.Default.History, contentDescription = "History") },
            label = { Text("History") }
        )

        NavigationBarItem(
            selected = false,
            onClick = { },
            icon = { Icon(Icons.Default.Receipt, contentDescription = "Receipts") },
            label = { Text("Receipts") }
        )

        NavigationBarItem(
            selected = false,
            onClick = { },
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
            label = { Text("Profile") }
        )
    }
}

fun getGreeting(): String {
    return when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
        in 0..11 -> "Morning"
        in 12..16 -> "Afternoon"
        else -> "Evening"
    }
}

fun getDaySuffix(day: Int): String {
    return when {
        day in 11..13 -> "th"
        day % 10 == 1 -> "st"
        day % 10 == 2 -> "nd"
        day % 10 == 3 -> "rd"
        else -> "th"
    }
}