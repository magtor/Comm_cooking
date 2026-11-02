package com.example.ui.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.RoomService
import androidx.compose.material.icons.filled.TableBar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.OrderStatus
import com.example.data.model.OrderType
import com.example.data.model.OrderWithItems
import com.example.ui.components.OrderStatusBadge
import com.example.ui.components.OrderTypeBadge
import com.example.ui.theme.StatusAmber
import com.example.ui.theme.StatusBlue
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusOrange
import com.example.ui.theme.StatusPurple
import com.example.ui.viewmodel.ComandaViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(
    viewModel: ComandaViewModel,
    onNavigateToOrderDetail: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val activeOrders by viewModel.activeOrders.collectAsStateWithLifecycle()
    var statusFilter by remember { mutableStateOf<OrderStatus?>(null) }
    var typeFilter by remember { mutableStateOf<OrderType?>(null) }

    val filteredOrders = remember(activeOrders, statusFilter, typeFilter) {
        activeOrders.filter { owi ->
            val matchesStatus = statusFilter == null || owi.order.status == statusFilter
            val matchesType = typeFilter == null || owi.order.orderType == typeFilter
            matchesStatus && matchesType
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Comandas Activas",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${activeOrders.size} órdenes en curso en restaurante",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Order Type Filter Row (Dine-In, Pick-Up, Delivery)
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = typeFilter == null,
                        onClick = { typeFilter = null },
                        label = { Text("Todos Canales") }
                    )
                }
                OrderType.entries.forEach { ot ->
                    val count = activeOrders.count { it.order.orderType == ot }
                    item {
                        FilterChip(
                            selected = typeFilter == ot,
                            onClick = { typeFilter = if (typeFilter == ot) null else ot },
                            label = { Text("${ot.shortLabel} ($count)") }
                        )
                    }
                }
            }

            // Status Filter chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = statusFilter == null,
                        onClick = { statusFilter = null },
                        label = { Text("Todas (${activeOrders.size})") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }

                listOf(
                    OrderStatus.ENVIADA_COCINA to "En Cocina",
                    OrderStatus.LISTA to "Listas p/ Servir",
                    OrderStatus.SERVIDA to "Servidas",
                    OrderStatus.CUENTA to "Cuentas"
                ).forEach { (status, label) ->
                    val count = activeOrders.count { it.order.status == status }
                    item {
                        FilterChip(
                            selected = statusFilter == status,
                            onClick = { statusFilter = if (statusFilter == status) null else status },
                            label = { Text("$label ($count)") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }

            if (filteredOrders.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Receipt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No hay comandas con este estado",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredOrders, key = { it.order.id }) { owi ->
                        ComandaOverviewCard(
                            orderWithItems = owi,
                            onClick = { onNavigateToOrderDetail(owi.order.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ComandaOverviewCard(
    orderWithItems: OrderWithItems,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val order = orderWithItems.order
    val items = orderWithItems.items

    val borderColor = when (order.status) {
        OrderStatus.LISTA -> StatusPurple
        OrderStatus.ENVIADA_COCINA -> StatusOrange
        OrderStatus.CUENTA -> StatusBlue
        OrderStatus.SERVIDA -> Color(0xFF00796B)
        else -> Color.Transparent
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor.copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("overview_order_card_${order.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    OrderTypeBadge(orderType = order.orderType)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = order.tableNumber,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "#${order.id}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                OrderStatusBadge(status = order.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Time in kitchen
            order.sentToKitchenAt?.let { sentAt ->
                val elapsedMinutes = ((System.currentTimeMillis() - sentAt) / (60 * 1000)).coerceAtLeast(0)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.HourglassTop,
                        contentDescription = null,
                        tint = if (order.status == OrderStatus.LISTA) StatusPurple else StatusOrange,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Tiempo en cocina: hace $elapsedMinutes min",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (order.status == OrderStatus.LISTA) StatusPurple else StatusOrange
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            // Preview items
            val itemSummary = items.take(3).joinToString(", ") { "${it.quantity}x ${it.productName}" }
            val remaining = items.size - 3
            Text(
                text = if (remaining > 0) "$itemSummary y $remaining más..." else itemSummary,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${items.sumOf { it.quantity }} productos",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = String.format(Locale.US, "$%.2f", order.totalAmount),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
