package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TableBar
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.OrderWithItems
import com.example.ui.theme.StatusGreen
import com.example.ui.viewmodel.ComandaViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class DatePreset(val label: String) {
    HOY("Hoy"),
    AYER("Ayer"),
    ULTIMOS_7_DIAS("Últimos 7 días"),
    ESTE_MES("Este Mes"),
    TODO("Todo el Historial"),
    PERSONALIZADO("Personalizado...")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClosedOrdersScreen(
    viewModel: ComandaViewModel,
    onNavigateToOrderDetail: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val closedOrders by viewModel.closedOrders.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    var selectedPreset by remember { mutableStateOf(DatePreset.HOY) }
    var selectedWaiterFilter by remember { mutableStateOf("MIS_ORDENES") } // MIS_ORDENES, TODOS, or specific name
    var tipRateOption by remember { mutableDoubleStateOf(5.0) } // 5% default required by user prompt
    var searchQuery by remember { mutableStateOf("") }

    var showTipInfoDialog by remember { mutableStateOf(false) }
    var showReceiptDialog by remember { mutableStateOf<OrderWithItems?>(null) }
    var showCustomRangeDialog by remember { mutableStateOf(false) }

    // Custom date range bounds (default: start of today to end of today)
    val now = remember { System.currentTimeMillis() }
    var customStartMillis by remember {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        mutableStateOf(cal.timeInMillis)
    }
    var customEndMillis by remember {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        mutableStateOf(cal.timeInMillis)
    }

    // Determine start and end timestamps based on active preset
    val (rangeStart, rangeEnd) = remember(selectedPreset, customStartMillis, customEndMillis) {
        val cal = Calendar.getInstance()
        when (selectedPreset) {
            DatePreset.HOY -> {
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                cal.set(Calendar.MILLISECOND, 999)
                val end = cal.timeInMillis
                start to end
            }
            DatePreset.AYER -> {
                cal.add(Calendar.DAY_OF_YEAR, -1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                cal.set(Calendar.MILLISECOND, 999)
                val end = cal.timeInMillis
                start to end
            }
            DatePreset.ULTIMOS_7_DIAS -> {
                cal.add(Calendar.DAY_OF_YEAR, -7)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                val start = cal.timeInMillis
                val end = System.currentTimeMillis()
                start to end
            }
            DatePreset.ESTE_MES -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                val start = cal.timeInMillis
                val end = System.currentTimeMillis()
                start to end
            }
            DatePreset.TODO -> {
                0L to Long.MAX_VALUE
            }
            DatePreset.PERSONALIZADO -> {
                customStartMillis to customEndMillis
            }
        }
    }

    // Filter orders by date range, waiter, and search text
    val currentUserName = currentUser?.fullName ?: "Carlos Mendieta"
    val filteredOrders = remember(
        closedOrders,
        rangeStart,
        rangeEnd,
        selectedWaiterFilter,
        currentUserName,
        searchQuery
    ) {
        closedOrders.filter { owi ->
            val orderTime = owi.order.paidAt ?: owi.order.createdAt
            val matchesDate = orderTime in rangeStart..rangeEnd

            val matchesWaiter = when (selectedWaiterFilter) {
                "MIS_ORDENES" -> owi.order.waiterName.contains(currentUserName, ignoreCase = true) ||
                        currentUserName.contains(owi.order.waiterName, ignoreCase = true)
                "TODOS" -> true
                else -> owi.order.waiterName.equals(selectedWaiterFilter, ignoreCase = true)
            }

            val matchesSearch = if (searchQuery.isBlank()) true else {
                owi.order.tableNumber.contains(searchQuery, ignoreCase = true) ||
                        owi.order.id.toString().contains(searchQuery) ||
                        owi.order.waiterName.contains(searchQuery, ignoreCase = true) ||
                        owi.items.any { it.productName.contains(searchQuery, ignoreCase = true) }
            }

            matchesDate && matchesWaiter && matchesSearch
        }
    }

    // Financial calculations based on filtered orders & selected tip rate
    val totalSales = remember(filteredOrders) {
        filteredOrders.sumOf { it.order.totalAmount }
    }

    val totalTips = remember(filteredOrders, tipRateOption) {
        if (tipRateOption > 0.0) {
            // Apply selected tip rate (e.g. 5% by default)
            totalSales * (tipRateOption / 100.0)
        } else {
            // "Real cobrado" registered in database
            filteredOrders.sumOf { it.order.effectiveTip }
        }
    }

    val averageTipPerOrder = remember(totalTips, filteredOrders) {
        if (filteredOrders.isNotEmpty()) totalTips / filteredOrders.size else 0.0
    }

    val totalGrand = totalSales + totalTips

    // Payment methods breakdown
    val cashSales = remember(filteredOrders) {
        filteredOrders.filter { it.order.paymentMethod.contains("Efectivo", ignoreCase = true) }.sumOf { it.order.totalAmount }
    }
    val cardSales = remember(filteredOrders) {
        filteredOrders.filter { it.order.paymentMethod.contains("Tarjeta", ignoreCase = true) }.sumOf { it.order.totalAmount }
    }
    val transferSales = remember(filteredOrders) {
        filteredOrders.filter { it.order.paymentMethod.contains("Transferencia", ignoreCase = true) }.sumOf { it.order.totalAmount }
    }

    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale("es", "ES")) }
    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PointOfSale,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Órdenes Cerradas & Propinas",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = if (selectedWaiterFilter == "MIS_ORDENES") "Propinas de $currentUserName" else "Historial de Sala y Caja",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showTipInfoDialog = true },
                        modifier = Modifier.testTag("btn_tip_info")
                    ) {
                        Icon(
                            imageVector = Icons.Default.HelpOutline,
                            contentDescription = "¿Cómo funciona la propina?",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = innerPadding.calculateTopPadding() + 8.dp,
                bottom = innerPadding.calculateBottomPadding() + 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // 1. DATE PRESET CHIPS BAR
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Rango de Fechas",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (selectedPreset == DatePreset.PERSONALIZADO) {
                            Text(
                                text = "${dateFormat.format(Date(rangeStart))} - ${dateFormat.format(Date(rangeEnd))}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(DatePreset.values()) { preset ->
                            FilterChip(
                                selected = selectedPreset == preset,
                                onClick = {
                                    selectedPreset = preset
                                    if (preset == DatePreset.PERSONALIZADO) {
                                        showCustomRangeDialog = true
                                    }
                                },
                                label = { Text(preset.label, fontSize = 13.sp) },
                                leadingIcon = if (preset == DatePreset.PERSONALIZADO) {
                                    {
                                        Icon(
                                            imageVector = Icons.Default.CalendarMonth,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                } else null,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                modifier = Modifier.testTag("date_preset_${preset.name.lowercase()}")
                            )
                        }
                    }
                }
            }

            // 2. WAITER FILTER & TIP PERCENTAGE SELECTOR
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        // Waiter toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Mesero:",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                FilterChip(
                                    selected = selectedWaiterFilter == "MIS_ORDENES",
                                    onClick = { selectedWaiterFilter = "MIS_ORDENES" },
                                    label = { Text("Mis Órdenes", fontSize = 12.sp) }
                                )
                                FilterChip(
                                    selected = selectedWaiterFilter == "TODOS",
                                    onClick = { selectedWaiterFilter = "TODOS" },
                                    label = { Text("Todos", fontSize = 12.sp) }
                                )
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                        // Tip Percentage Selector
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AttachMoney,
                                    contentDescription = null,
                                    tint = StatusGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Cálculo Propina:",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Row(
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                listOf(5.0 to "5% (Cobrado)", 10.0 to "10%", 15.0 to "15%").forEach { (pct, label) ->
                                    FilterChip(
                                        selected = tipRateOption == pct,
                                        onClick = { tipRateOption = pct },
                                        label = {
                                            Text(
                                                text = label,
                                                fontSize = 11.sp,
                                                fontWeight = if (tipRateOption == pct) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = StatusGreen.copy(alpha = 0.2f),
                                            selectedLabelColor = Color(0xFF1B5E20)
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. MAIN KPI SUMMARY CARD: PROPINA ACUMULADA
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("card_tips_kpi_summary")
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Payments,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "PROPINA ACUMULADA (${tipRateOption.toInt()}%)",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = String.format(Locale.US, "$%.2f", totalTips),
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF1B5E20)
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Text(
                                    text = "${filteredOrders.size} cuentas",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(12.dp))

                        // Sub metrics: Venta Neta, Promedio por Comanda, Total con Propina
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Venta Neta Cerrada",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                                Text(
                                    text = String.format(Locale.US, "$%.2f", totalSales),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }

                            Column {
                                Text(
                                    text = "Promedio / Mesa",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                                Text(
                                    text = String.format(Locale.US, "$%.2f", averageTipPerOrder),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }

                            Column {
                                Text(
                                    text = "Total Cobrado",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                                Text(
                                    text = String.format(Locale.US, "$%.2f", totalGrand),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Payment Methods Quick Breakdown
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(imageVector = Icons.Default.LocalAtm, contentDescription = null, tint = StatusGreen, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Efec: ${String.format(Locale.US, "$%.0f", cashSales)}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(imageVector = Icons.Default.CreditCard, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Tarj: ${String.format(Locale.US, "$%.0f", cardSales)}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                            if (transferSales > 0) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Transf: ${String.format(Locale.US, "$%.0f", transferSales)}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 4. SEARCH BAR
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Buscar por mesa, folio (#99) o platillo...") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Limpiar")
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_closed_orders_input")
                )
            }

            // 5. ORDERS LIST HEADER
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Comandas Cerradas (${filteredOrders.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Ordenadas por fecha reciente",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 6. LIST OF CLOSED ORDERS
            if (filteredOrders.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Receipt,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No hay órdenes cerradas en este rango",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Prueba seleccionando 'Ayer', 'Últimos 7 días' o 'Todo el Historial' para visualizar registros anteriores.",
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { selectedPreset = DatePreset.TODO },
                                modifier = Modifier.testTag("btn_reset_date_filter")
                            ) {
                                Text("Ver Todo el Historial")
                            }
                        }
                    }
                }
            } else {
                items(filteredOrders, key = { it.order.id }) { owi ->
                    ClosedOrderItemCard(
                        orderWithItems = owi,
                        tipRateOption = tipRateOption,
                        onViewDetail = { onNavigateToOrderDetail(owi.order.id) },
                        onViewReceipt = { showReceiptDialog = owi }
                    )
                }
            }
        }
    }

    // EDUCATIONAL DIALOG: HOW TIPS WORK IN RESTAURANTS
    if (showTipInfoDialog) {
        AlertDialog(
            onDismissRequest = { showTipInfoDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.HelpOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "¿Cómo se maneja la propina en los restaurantes?",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "1. ¿Es exigida o voluntaria?",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "En la ley comercial (como PROFECO en México y normativas en Latinoamérica/España), la propina es 100% VOLUNTARIA. Ningún restaurante puede exigirla por ley, retener al cliente ni imponerla de forma forzosa en la cuenta final.",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Text(
                        text = "2. ¿Cómo la cobra este sistema POS?",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "El sistema calcula una propina sugerida base del 5% sobre el total de la comanda (tal como lo solicitaste). Al momento del cobro, el cliente o cajero pueden confirmar ese 5%, ajustarlo a 10%, 15% o 0% si el cliente no deseó dejar propina.",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Text(
                        text = "3. Liquidación del Mesero:",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Esta pantalla le permite al mesero ver exactamente cuánto acumuló en su turno o rango de fechas, facilitando el arqueo de caja y la entrega de sus gratificaciones.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showTipInfoDialog = false },
                    modifier = Modifier.testTag("btn_close_tip_info")
                ) {
                    Text("Entendido")
                }
            }
        )
    }

    // CUSTOM DATE RANGE DIALOG
    if (showCustomRangeDialog) {
        AlertDialog(
            onDismissRequest = { showCustomRangeDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = {
                Text("Seleccionar Rango Personalizado", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Filtra las órdenes cerradas por fecha exacta:",
                        style = MaterialTheme.typography.bodySmall
                    )

                    // Quick range helpers
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val cal = Calendar.getInstance()
                                cal.add(Calendar.DAY_OF_YEAR, -3)
                                cal.set(Calendar.HOUR_OF_DAY, 0)
                                cal.set(Calendar.MINUTE, 0)
                                customStartMillis = cal.timeInMillis
                                customEndMillis = System.currentTimeMillis()
                                selectedPreset = DatePreset.PERSONALIZADO
                                showCustomRangeDialog = false
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Últimos 3 días", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                val cal = Calendar.getInstance()
                                cal.add(Calendar.DAY_OF_YEAR, -15)
                                cal.set(Calendar.HOUR_OF_DAY, 0)
                                cal.set(Calendar.MINUTE, 0)
                                customStartMillis = cal.timeInMillis
                                customEndMillis = System.currentTimeMillis()
                                selectedPreset = DatePreset.PERSONALIZADO
                                showCustomRangeDialog = false
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Últimos 15 días", fontSize = 12.sp)
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            val cal = Calendar.getInstance()
                            cal.set(Calendar.DAY_OF_YEAR, 1)
                            cal.set(Calendar.HOUR_OF_DAY, 0)
                            cal.set(Calendar.MINUTE, 0)
                            customStartMillis = cal.timeInMillis
                            customEndMillis = System.currentTimeMillis()
                            selectedPreset = DatePreset.PERSONALIZADO
                            showCustomRangeDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Todo el Año en Curso")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        selectedPreset = DatePreset.PERSONALIZADO
                        showCustomRangeDialog = false
                    }
                ) {
                    Text("Aplicar Rango")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showCustomRangeDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // TICKET / RECEIPT DIALOG
    showReceiptDialog?.let { owi ->
        val order = owi.order
        val tip = if (tipRateOption > 0.0) order.totalAmount * (tipRateOption / 100.0) else order.effectiveTip
        val finalTotal = order.totalAmount + tip

        AlertDialog(
            onDismissRequest = { showReceiptDialog = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.Receipt,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = {
                Text(
                    text = "Comprobante de Cierre #${order.id}",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "RESTAURANTE COMANDA PRO",
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "${order.tableNumber} • Atendido por: ${order.waiterName}",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    val paidDateStr = (order.paidAt ?: order.createdAt).let {
                        "${dateFormat.format(Date(it))} ${timeFormat.format(Date(it))}"
                    }
                    Text(
                        text = "Fecha de cobro: $paidDateStr",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    // Products list
                    owi.items.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${item.quantity}x ${item.productName}",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = String.format(Locale.US, "$%.2f", item.subtotal),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    // Totals
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Subtotal Venta:", style = MaterialTheme.typography.bodyMedium)
                        Text(String.format(Locale.US, "$%.2f", order.totalAmount), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Propina voluntaria (${tipRateOption.toInt()}%):",
                            style = MaterialTheme.typography.bodyMedium,
                            color = StatusGreen
                        )
                        Text(
                            String.format(Locale.US, "+$%.2f", tip),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = StatusGreen
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Método de Pago:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(order.paymentMethod, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("TOTAL PAGADO:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                        Text(
                            String.format(Locale.US, "$%.2f", finalTotal),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showReceiptDialog = null },
                    modifier = Modifier.testTag("btn_close_receipt")
                ) {
                    Text("Cerrar")
                }
            }
        )
    }
}

@Composable
fun ClosedOrderItemCard(
    orderWithItems: OrderWithItems,
    tipRateOption: Double,
    onViewDetail: () -> Unit,
    onViewReceipt: () -> Unit
) {
    val order = orderWithItems.order
    val items = orderWithItems.items

    val tip = if (tipRateOption > 0.0) order.totalAmount * (tipRateOption / 100.0) else order.effectiveTip
    val grandTotal = order.totalAmount + tip

    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    val dateFormat = remember { SimpleDateFormat("dd MMM", Locale("es", "ES")) }
    val paidDateStr = remember(order.paidAt, order.createdAt) {
        val t = order.paidAt ?: order.createdAt
        "${dateFormat.format(Date(t))}, ${timeFormat.format(Date(t))}"
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("card_closed_order_${order.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Table, Folio, and Payment Method Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.TableBar,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = order.tableNumber,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Folio #${order.id} • $paidDateStr",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Payment Method Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when {
                        order.paymentMethod.contains("Efectivo", true) -> StatusGreen.copy(alpha = 0.15f)
                        order.paymentMethod.contains("Tarjeta", true) -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        else -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when {
                                order.paymentMethod.contains("Efectivo", true) -> Icons.Default.LocalAtm
                                order.paymentMethod.contains("Tarjeta", true) -> Icons.Default.CreditCard
                                else -> Icons.Default.Payments
                            },
                            contentDescription = null,
                            tint = when {
                                order.paymentMethod.contains("Efectivo", true) -> StatusGreen
                                order.paymentMethod.contains("Tarjeta", true) -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.secondary
                            },
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = order.paymentMethod,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                order.paymentMethod.contains("Efectivo", true) -> Color(0xFF1B5E20)
                                order.paymentMethod.contains("Tarjeta", true) -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.secondary
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Waiter Info and Dish Summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Mesero: ${order.waiterName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${items.sumOf { it.quantity }} productos",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }

            // Products snippet preview
            if (items.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = items.joinToString(", ") { "${it.quantity}x ${it.productName}" },
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

            // Financial Summary for this order
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Venta: ${String.format(Locale.US, "$%.2f", order.totalAmount)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Propina (${tipRateOption.toInt()}%): ",
                            style = MaterialTheme.typography.bodyMedium,
                            color = StatusGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = String.format(Locale.US, "+$%.2f", tip),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF1B5E20),
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Total Cobrado",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = String.format(Locale.US, "$%.2f", grandTotal),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Buttons: Ver Comanda / Ver Ticket
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onViewReceipt,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Recibo", fontSize = 12.sp)
                }

                Button(
                    onClick = onViewDetail,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Ver Detalle", fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}
