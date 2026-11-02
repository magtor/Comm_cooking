package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.Dining
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.TableBar
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.model.OrderStatus
import com.example.data.model.OrderType
import com.example.data.model.OrderWithItems
import com.example.data.model.RestaurantTable
import com.example.data.model.TableStatus
import com.example.ui.components.CreateChannelOrderDialog
import com.example.ui.components.TableStatusBadge
import com.example.ui.components.UserProfileMenuDialog
import com.example.ui.theme.StatusAmber
import com.example.ui.theme.StatusBlue
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusOrange
import com.example.ui.theme.StatusPurple
import com.example.ui.viewmodel.ComandaViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TablesScreen(
    viewModel: ComandaViewModel,
    onNavigateToCatalog: () -> Unit,
    onNavigateToOrderDetail: (Long) -> Unit,
    onNavigateToTracking: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val tables by viewModel.filteredTables.collectAsStateWithLifecycle()
    val allTables by viewModel.tables.collectAsStateWithLifecycle()
    val activeOrders by viewModel.activeOrders.collectAsStateWithLifecycle()
    val pendingTakeoutOrders by viewModel.pendingTakeoutOrders.collectAsStateWithLifecycle()
    val selectedZone by viewModel.selectedZone.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val operationPolicy by viewModel.operationRolePolicy.collectAsStateWithLifecycle()

    var showChannelOrderDialog by remember { mutableStateOf<OrderType?>(null) }
    var showOpenTableDialog by remember { mutableStateOf<RestaurantTable?>(null) }
    var showTableQuickMenu by remember { mutableStateOf<RestaurantTable?>(null) }
    var showUserMenuDialog by remember { mutableStateOf(false) }

    val pendingPickUpCount = pendingTakeoutOrders.count { it.order.orderType == OrderType.PICK_UP }
    val pendingDeliveryCount = pendingTakeoutOrders.count { it.order.orderType == OrderType.DELIVERY }

    // Quick stats
    val totalOccupied = allTables.count { it.status != TableStatus.LIBRE }
    val inKitchenCount = allTables.count { it.status == TableStatus.EN_COCINA }
    val readyToServeCount = allTables.count { it.status == TableStatus.LISTO }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Dining,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "ComandaPro POS",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${currentUser?.role?.label ?: "Personal"}: ${currentUser?.fullName ?: "Carlos M."} • Turno Activo",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    Surface(
                        onClick = { showUserMenuDialog = true },
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("btn_top_user_profile")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = currentUser?.role?.let { Color(it.badgeColor) } ?: MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = currentUser?.fullName?.take(1) ?: "U",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = currentUser?.fullName?.split(" ")?.firstOrNull() ?: "Personal",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    showChannelOrderDialog = OrderType.DINE_IN
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.testTag("fab_new_comanda")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Nuevo Pedido")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Nuevo Pedido", fontWeight = FontWeight.Bold)
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = innerPadding.calculateTopPadding() + 8.dp,
                bottom = innerPadding.calculateBottomPadding() + 80.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Hero Banner & Metrics item spanning both columns
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                Column {
                    // Hero Banner Card
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(modifier = Modifier.height(130.dp).fillMaxWidth()) {
                            Image(
                                painter = painterResource(id = R.drawable.banner_restaurant),
                                contentDescription = "Banner Restaurante",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Black.copy(alpha = 0.35f),
                                                Color.Black.copy(alpha = 0.85f)
                                            )
                                        )
                                    )
                            )
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.Bottom
                            ) {
                                Text(
                                    text = "Mesas & Comandas en Vivo",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Supervisa el estado de cocina y pedidos en tiempo real",
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Status Metrics Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricPill(
                            title = "Ocupadas",
                            count = "$totalOccupied/${allTables.size}",
                            color = StatusAmber,
                            modifier = Modifier.weight(1f)
                        )
                        MetricPill(
                            title = "En Cocina",
                            count = "$inKitchenCount",
                            color = StatusOrange,
                            modifier = Modifier.weight(1f)
                        )
                        MetricPill(
                            title = "Listas",
                            count = "$readyToServeCount",
                            color = StatusPurple,
                            isAlert = readyToServeCount > 0,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 3 Order Types Quick Creation & Live Tracking Strip
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Crear Pedido por Canal:",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Mapa: $pendingPickUpCount Pick-Up • $pendingDeliveryCount Delivery →",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .clickable { onNavigateToTracking() }
                                        .padding(vertical = 2.dp)
                                        .testTag("quick_jump_to_tracking_map")
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { showChannelOrderDialog = OrderType.DINE_IN },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("quick_create_dine_in_btn")
                                ) {
                                    Icon(Icons.Default.TableBar, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Dine-In", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                OutlinedButton(
                                    onClick = { showChannelOrderDialog = OrderType.PICK_UP },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("quick_create_pickup_btn")
                                ) {
                                    Icon(
                                        Icons.Default.ShoppingBag,
                                        contentDescription = null,
                                        tint = Color(0xFF7B1FA2),
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Pick-Up", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7B1FA2))
                                }
                                OutlinedButton(
                                    onClick = { showChannelOrderDialog = OrderType.DELIVERY },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("quick_create_delivery_btn")
                                ) {
                                    Icon(
                                        Icons.Default.DeliveryDining,
                                        contentDescription = null,
                                        tint = Color(0xFF00838F),
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Delivery", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00838F))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Zone filter chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Todas", "Salón Principal", "Terraza", "Barra").forEach { zone ->
                            FilterChip(
                                selected = selectedZone == zone,
                                onClick = { viewModel.setSelectedZone(zone) },
                                label = { Text(text = zone, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }
                }
            }

            // Tables Grid
            items(tables, key = { it.id }) { table ->
                val activeOrder = activeOrders.firstOrNull { it.order.tableId == table.id }
                TableCardItem(
                    table = table,
                    activeOrder = activeOrder,
                    onClick = {
                        if (table.status == TableStatus.LIBRE) {
                            showOpenTableDialog = table
                        } else {
                            showTableQuickMenu = table
                        }
                    }
                )
            }
        }
    }

    // Dialog to open table and start comanda
    showOpenTableDialog?.let { table ->
        var diners by remember { mutableIntStateOf(2) }

        AlertDialog(
            onDismissRequest = { showOpenTableDialog = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.TableBar,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Abrir ${table.tableNumber}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Zona: ${table.zone} • Capacidad máx: ${table.capacity} personas",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "¿Cuántos comensales ocupan la mesa?",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        IconButton(
                            onClick = { if (diners > 1) diners-- },
                            modifier = Modifier.testTag("decrease_diners_btn")
                        ) {
                            Text("-", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        }
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "$diners",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        IconButton(
                            onClick = { if (diners < 12) diners++ },
                            modifier = Modifier.testTag("increase_diners_btn")
                        ) {
                            Text("+", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.openTable(table.id, diners)
                        viewModel.prepareForNewOrder(table.copy(activeDinersCount = diners))
                        showOpenTableDialog = null
                        onNavigateToCatalog()
                    },
                    modifier = Modifier.testTag("start_comanda_btn")
                ) {
                    Text("Tomar Comanda")
                }
            },
            dismissButton = {
                TextButton(onClick = { showOpenTableDialog = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Modal Sheet for active table options
    showTableQuickMenu?.let { table ->
        val activeOrder = activeOrders.firstOrNull { it.order.tableId == table.id }
        val sheetState = rememberModalBottomSheetState()

        ModalBottomSheet(
            onDismissRequest = { showTableQuickMenu = null },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${table.tableNumber} - ${table.zone}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Comensales: ${table.activeDinersCount} • Mesero: ${table.waiterName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    TableStatusBadge(status = table.status)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action 1: View Order Details
                if (activeOrder != null) {
                    Button(
                        onClick = {
                            showTableQuickMenu = null
                            onNavigateToOrderDetail(activeOrder.order.id)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("view_order_detail_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Receipt, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Ver Comanda en Vivo (${String.format(Locale.US, "$%.2f", activeOrder.order.totalAmount)})")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Action 2: Add more products to table
                Button(
                    onClick = {
                        if (activeOrder != null) {
                            viewModel.prepareForAddingToOrder(table, activeOrder.order.id)
                        } else {
                            viewModel.prepareForNewOrder(table)
                        }
                        showTableQuickMenu = null
                        onNavigateToCatalog()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_products_to_table_btn")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (activeOrder != null) "Agregar más productos a la comanda" else "Tomar Comanda",
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Quick status advance buttons
                if (table.status == TableStatus.LISTO && activeOrder != null) {
                    Button(
                        onClick = {
                            viewModel.updateOrderStatus(activeOrder.order.id, OrderStatus.SERVIDA)
                            showTableQuickMenu = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00796B)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("¡Comida Lista! Marcar Servido")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (table.status == TableStatus.SERVIDO && activeOrder != null) {
                    OutlinedButton(
                        onClick = {
                            viewModel.updateOrderStatus(activeOrder.order.id, OrderStatus.CUENTA)
                            showTableQuickMenu = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Receipt, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Solicitar Cuenta a Caja")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Free / Release table
                TextButton(
                    onClick = {
                        viewModel.releaseTable(table.id)
                        showTableQuickMenu = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cobrar y Liberar Mesa")
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    if (showUserMenuDialog) {
        UserProfileMenuDialog(
            viewModel = viewModel,
            currentUser = currentUser,
            onSwitchUser = onNavigateToLogin,
            onDismiss = { showUserMenuDialog = false }
        )
    }

    showChannelOrderDialog?.let { initialChannel ->
        CreateChannelOrderDialog(
            initialOrderType = initialChannel,
            availableTables = allTables,
            currentUser = currentUser,
            operationRolePolicy = operationPolicy,
            onDismiss = { showChannelOrderDialog = null },
            onStartDineIn = { table, diners ->
                if (table.status == TableStatus.LIBRE) {
                    viewModel.openTable(table.id, diners)
                    viewModel.prepareForNewOrder(table.copy(activeDinersCount = diners))
                } else if (table.activeOrderId != null) {
                    viewModel.prepareForAddingToOrder(table, table.activeOrderId)
                } else {
                    viewModel.prepareForNewOrder(table)
                }
                showChannelOrderDialog = null
                onNavigateToCatalog()
            },
            onStartPickUp = { name, phone, notes ->
                viewModel.prepareForPickUpOrder(name, phone, notes)
                showChannelOrderDialog = null
                onNavigateToCatalog()
            },
            onStartDelivery = { name, phone, address, zone, driver, fee, notes ->
                viewModel.prepareForDeliveryOrder(name, phone, address, zone, driver, fee, notes)
                showChannelOrderDialog = null
                onNavigateToCatalog()
            }
        )
    }
}

@Composable
fun TableCardItem(
    table: RestaurantTable,
    activeOrder: OrderWithItems?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = when (table.status) {
        TableStatus.LISTO -> StatusPurple
        TableStatus.EN_COCINA -> StatusOrange
        TableStatus.CUENTA -> StatusBlue
        TableStatus.OCUPADA -> StatusAmber
        TableStatus.SERVIDO -> Color(0xFF00796B)
        TableStatus.LIBRE -> Color.Transparent
    }

    val elevation = if (table.status == TableStatus.LISTO) 4.dp else 1.dp

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = if (table.status != TableStatus.LIBRE) {
            androidx.compose.foundation.BorderStroke(1.5.dp, borderColor.copy(alpha = 0.7f))
        } else null,
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("table_card_${table.id}")
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = table.tableNumber,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                TableStatusBadge(status = table.status)
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = table.zone,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Capacity & Diner info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (table.status == TableStatus.LIBRE) {
                        "Cap: ${table.capacity} pers"
                    } else {
                        "${table.activeDinersCount} comensales"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (activeOrder != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "${activeOrder.items.sumOf { it.quantity }} platillos",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = String.format(Locale.US, "$%.2f", activeOrder.order.totalAmount),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Elapsed time if sent to kitchen
                        activeOrder.order.sentToKitchenAt?.let { sentAt ->
                            val elapsedMinutes = ((System.currentTimeMillis() - sentAt) / (60 * 1000)).coerceAtLeast(0)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "⏱ Cocina: hace ${elapsedMinutes}m",
                                fontSize = 10.sp,
                                color = if (table.status == TableStatus.LISTO) StatusPurple else StatusOrange,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            } else if (table.status == TableStatus.LIBRE) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = Color(0xFFE8F5E9),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "+ Toca para abrir",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = StatusGreen,
                        modifier = Modifier.padding(6.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun MetricPill(
    title: String,
    count: String,
    color: Color,
    isAlert: Boolean = false,
    modifier: Modifier = Modifier
) {
    Surface(
        color = if (isAlert) color else MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 1.dp,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isAlert) Color.White else color
            )
            Text(
                text = title,
                fontSize = 10.sp,
                color = if (isAlert) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
