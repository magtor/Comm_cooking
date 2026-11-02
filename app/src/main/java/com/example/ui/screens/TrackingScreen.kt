package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.FulfillmentStage
import com.example.data.model.OrderType
import com.example.data.model.OrderWithItems
import com.example.ui.components.CreateChannelOrderDialog
import com.example.ui.components.FulfillmentStageBadge
import com.example.ui.components.LiveTrackingMapCard
import com.example.ui.components.OrderTypeBadge
import com.example.ui.components.RoleResponsibilityAdvisorDialog
import com.example.ui.components.nextActionColor
import com.example.ui.components.nextActionLabel
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusOrange
import com.example.ui.theme.StatusPurple
import com.example.ui.viewmodel.ComandaViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackingScreen(
    viewModel: ComandaViewModel,
    onNavigateToCatalog: () -> Unit,
    onNavigateToOrderDetail: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val pendingTakeoutOrders by viewModel.pendingTakeoutOrders.collectAsStateWithLifecycle()
    val allTables by viewModel.tables.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val operationPolicy by viewModel.operationRolePolicy.collectAsStateWithLifecycle()

    val pendingPickUps = remember(pendingTakeoutOrders) {
        pendingTakeoutOrders.filter { it.order.orderType == OrderType.PICK_UP }
    }
    val pendingDeliveries = remember(pendingTakeoutOrders) {
        pendingTakeoutOrders.filter { it.order.orderType == OrderType.DELIVERY }
    }

    var channelFilter by remember { mutableStateOf("TODOS") } // TODOS, PICK_UP, DELIVERY, LISTOS, EN_RUTA, MESERO, DESPACHADOR
    var selectedMapOrderId by remember { mutableStateOf<Long?>(null) }
    var showCreateOrderDialog by remember { mutableStateOf<OrderType?>(null) }
    var showRoleAdvisorDialog by remember { mutableStateOf(false) }
    var orderForDriverAssign by remember { mutableStateOf<OrderWithItems?>(null) }
    var orderForQuickSettle by remember { mutableStateOf<OrderWithItems?>(null) }

    // Default map selection to first pending order if available
    LaunchedEffect(pendingTakeoutOrders) {
        if (selectedMapOrderId == null || pendingTakeoutOrders.none { it.order.id == selectedMapOrderId }) {
            selectedMapOrderId = pendingTakeoutOrders.firstOrNull()?.order?.id
        }
    }

    val filteredList = remember(pendingTakeoutOrders, channelFilter) {
        when (channelFilter) {
            "PICK_UP" -> pendingTakeoutOrders.filter { it.order.orderType == OrderType.PICK_UP }
            "DELIVERY" -> pendingTakeoutOrders.filter { it.order.orderType == OrderType.DELIVERY }
            "LISTOS" -> pendingTakeoutOrders.filter {
                it.order.fulfillmentStage == FulfillmentStage.LISTO_MOSTRADOR ||
                        it.order.fulfillmentStage == FulfillmentStage.LISTO_DESPACHO
            }
            "EN_RUTA" -> pendingTakeoutOrders.filter { it.order.fulfillmentStage == FulfillmentStage.EN_RUTA }
            "MESERO" -> pendingTakeoutOrders.filter { it.order.createdByRole == "MESERO" }
            "DESPACHADOR" -> pendingTakeoutOrders.filter { it.order.createdByRole != "MESERO" }
            else -> pendingTakeoutOrders
        }
    }

    fun handleAdvanceOrder(owi: OrderWithItems) {
        val order = owi.order
        when (order.orderType) {
            OrderType.PICK_UP -> {
                when (order.fulfillmentStage) {
                    FulfillmentStage.EN_COCINA -> {
                        viewModel.updateFulfillmentStage(order.id, FulfillmentStage.LISTO_MOSTRADOR)
                    }
                    else -> {
                        orderForQuickSettle = owi
                    }
                }
            }
            OrderType.DELIVERY -> {
                when (order.fulfillmentStage) {
                    FulfillmentStage.EN_COCINA -> {
                        viewModel.updateFulfillmentStage(order.id, FulfillmentStage.LISTO_DESPACHO)
                    }
                    FulfillmentStage.LISTO_DESPACHO -> {
                        if (order.assignedDriver.isBlank()) {
                            orderForDriverAssign = owi
                        } else {
                            viewModel.updateFulfillmentStage(order.id, FulfillmentStage.EN_RUTA)
                        }
                    }
                    else -> {
                        orderForQuickSettle = owi
                    }
                }
            }
            OrderType.DINE_IN -> onNavigateToOrderDetail(order.id)
        }
    }

    val policyLabel = when (operationPolicy) {
        "DESPACHADOR" -> "Rol: Mostrador Dedicado"
        "MESERO" -> "Rol: Meseros Multifunción"
        else -> "Rol: Híbrido (Mesero + Mostrador)"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Seguimiento Pick-Up & Delivery",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$policyLabel • ${pendingTakeoutOrders.size} pendientes",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    Surface(
                        onClick = { showRoleAdvisorDialog = true },
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier
                            .padding(end = 10.dp)
                            .testTag("btn_role_advisor")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.HelpOutline,
                                contentDescription = "Guía de roles",
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "¿Quién atiende?",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
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
            ExtendedFloatingActionButton(
                onClick = { showCreateOrderDialog = OrderType.PICK_UP },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Nuevo Pick-Up / Delivery", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("fab_new_takeout_order")
            )
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = innerPadding.calculateTopPadding() + 8.dp,
                bottom = innerPadding.calculateBottomPadding() + 88.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // 1. Dual Pending Summary Counters (Pick-Up vs Delivery)
            item {
                val pickUpReady = pendingPickUps.count { it.order.fulfillmentStage == FulfillmentStage.LISTO_MOSTRADOR }
                val pickUpKitchen = pendingPickUps.count { it.order.fulfillmentStage == FulfillmentStage.EN_COCINA }

                val deliveryEnRuta = pendingDeliveries.count { it.order.fulfillmentStage == FulfillmentStage.EN_RUTA }
                val deliveryReady = pendingDeliveries.count { it.order.fulfillmentStage == FulfillmentStage.LISTO_DESPACHO }
                val deliveryKitchen = pendingDeliveries.count { it.order.fulfillmentStage == FulfillmentStage.EN_COCINA }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Pick-Up Counter Card
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (channelFilter == "PICK_UP") Color(0xFFF3E5F5) else MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(
                            width = if (channelFilter == "PICK_UP") 2.dp else 1.dp,
                            color = Color(0xFF8E24AA).copy(alpha = if (channelFilter == "PICK_UP") 0.9f else 0.35f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                channelFilter = if (channelFilter == "PICK_UP") "TODOS" else "PICK_UP"
                            }
                            .testTag("kpi_pending_pickup_card")
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFFF3E5F5),
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.ShoppingBag,
                                            contentDescription = null,
                                            tint = Color(0xFF6A1B9A),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "${pendingPickUps.size}",
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF6A1B9A)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Pick-Ups Pendientes",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$pickUpReady en mostrador • $pickUpKitchen cocina",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Delivery Counter Card
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (channelFilter == "DELIVERY") Color(0xFFE0F7FA) else MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(
                            width = if (channelFilter == "DELIVERY") 2.dp else 1.dp,
                            color = Color(0xFF00838F).copy(alpha = if (channelFilter == "DELIVERY") 0.9f else 0.35f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                channelFilter = if (channelFilter == "DELIVERY") "TODOS" else "DELIVERY"
                            }
                            .testTag("kpi_pending_delivery_card")
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFFE0F7FA),
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.DeliveryDining,
                                            contentDescription = null,
                                            tint = Color(0xFF00838F),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "${pendingDeliveries.size}",
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF00838F)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Deliveries Pendientes",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$deliveryEnRuta ruta • $deliveryReady listos • $deliveryKitchen coc.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // 2. Interactive Tracking Map
            item {
                LiveTrackingMapCard(
                    pendingPickUps = pendingPickUps,
                    pendingDeliveries = pendingDeliveries,
                    selectedOrderId = selectedMapOrderId,
                    onSelectOrder = { selectedMapOrderId = it },
                    onSelectCounterHub = { channelFilter = "PICK_UP" },
                    onAdvanceStage = { handleAdvanceOrder(it) },
                    onOpenDetail = { onNavigateToOrderDetail(it) }
                )
            }

            // 3. Filter Chips Row (Channel + Stage + Staff Creator Role)
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val chips = listOf(
                        "TODOS" to "Todos (${pendingTakeoutOrders.size})",
                        "PICK_UP" to "🛍️ Pick-Up (${pendingPickUps.size})",
                        "DELIVERY" to "🛵 Delivery (${pendingDeliveries.size})",
                        "LISTOS" to "📦 Listos p/ Entregar",
                        "EN_RUTA" to "🛵 En Ruta",
                        "MESERO" to "👨‍🍳 Creado por Mesero",
                        "DESPACHADOR" to "🎧 Creado por Mostrador"
                    )
                    items(chips) { (code, label) ->
                        FilterChip(
                            selected = channelFilter == code,
                            onClick = { channelFilter = code },
                            label = { Text(label, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }

            // 4. Pending Queue List
            if (filteredList.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = StatusGreen,
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Sin pedidos pendientes en este filtro",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Crea un nuevo Pick-Up o Delivery con el botón inferior.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(filteredList, key = { it.order.id }) { owi ->
                    TakeoutTrackingOrderCard(
                        orderWithItems = owi,
                        isSelectedOnMap = owi.order.id == selectedMapOrderId,
                        onSelectOnMap = { selectedMapOrderId = owi.order.id },
                        onOpenDetail = { onNavigateToOrderDetail(owi.order.id) },
                        onAssignDriverClick = { orderForDriverAssign = owi },
                        onAdvanceStageClick = { handleAdvanceOrder(owi) }
                    )
                }
            }
        }
    }

    // Create Pick-Up / Delivery / Dine-In Dialog
    showCreateOrderDialog?.let { initialType ->
        CreateChannelOrderDialog(
            initialOrderType = initialType,
            availableTables = allTables,
            currentUser = currentUser,
            operationRolePolicy = operationPolicy,
            onDismiss = { showCreateOrderDialog = null },
            onStartDineIn = { table, diners ->
                viewModel.openTable(table.id, diners)
                viewModel.prepareForNewOrder(table.copy(activeDinersCount = diners))
                showCreateOrderDialog = null
                onNavigateToCatalog()
            },
            onStartPickUp = { name, phone, notes ->
                viewModel.prepareForPickUpOrder(name, phone, notes)
                showCreateOrderDialog = null
                onNavigateToCatalog()
            },
            onStartDelivery = { name, phone, address, zone, driver, fee, notes ->
                viewModel.prepareForDeliveryOrder(name, phone, address, zone, driver, fee, notes)
                showCreateOrderDialog = null
                onNavigateToCatalog()
            }
        )
    }

    // Operational Role Advisor Dialog
    if (showRoleAdvisorDialog) {
        RoleResponsibilityAdvisorDialog(
            currentPolicy = operationPolicy,
            onSelectPolicy = { viewModel.setOperationRolePolicy(it) },
            onDismiss = { showRoleAdvisorDialog = false }
        )
    }

    // Assign Delivery Driver Dialog
    orderForDriverAssign?.let { owi ->
        val drivers = listOf(
            "Javier R. (Moto 1)",
            "Diego V. (Moto 2)",
            "Luis P. (Moto 3)",
            "Repartidor App Externa"
        )
        var pickedDriver by remember {
            mutableStateOf(owi.order.assignedDriver.ifBlank { drivers.first() })
        }

        AlertDialog(
            onDismissRequest = { orderForDriverAssign = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.DeliveryDining,
                    contentDescription = null,
                    tint = Color(0xFF00838F),
                    modifier = Modifier.size(30.dp)
                )
            },
            title = {
                Text("Asignar Repartidor • #${owi.order.id}", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Destino: ${owi.order.deliveryZone.label} — ${owi.order.deliveryAddress}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    drivers.forEach { drv ->
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (pickedDriver == drv) Color(0xFFE0F7FA)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            ),
                            border = if (pickedDriver == drv) BorderStroke(1.5.dp, Color(0xFF00838F)) else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { pickedDriver = drv }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeliveryDining,
                                    contentDescription = null,
                                    tint = Color(0xFF00838F)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = drv,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.assignDriverToOrder(owi.order.id, pickedDriver)
                        if (owi.order.fulfillmentStage == FulfillmentStage.LISTO_DESPACHO) {
                            viewModel.updateFulfillmentStage(owi.order.id, FulfillmentStage.EN_RUTA)
                        }
                        orderForDriverAssign = null
                    }
                ) {
                    Text("Asignar y Despachar")
                }
            },
            dismissButton = {
                TextButton(onClick = { orderForDriverAssign = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Quick Settle & Deliver Payment Dialog
    orderForQuickSettle?.let { owi ->
        val order = owi.order
        var paymentMethod by remember { mutableStateOf("Efectivo") }
        var tipPct by remember { mutableDoubleStateOf(0.0) } // 0% default on takeout/delivery

        val tipValue = order.totalAmount * (tipPct / 100.0)
        val totalToCharge = order.totalAmount + order.deliveryFee + tipValue

        AlertDialog(
            onDismissRequest = { orderForQuickSettle = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.Payments,
                    contentDescription = null,
                    tint = StatusGreen,
                    modifier = Modifier.size(30.dp)
                )
            },
            title = {
                Text(
                    text = "Entregar y Cerrar ${order.orderType.shortLabel} #${order.id}",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Cliente: ${order.customerName.ifBlank { order.tableNumber }}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Productos:", style = MaterialTheme.typography.bodySmall)
                        Text(String.format(Locale.US, "$%.2f", order.totalAmount), fontWeight = FontWeight.Bold)
                    }
                    if (order.deliveryFee > 0.0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Tarifa de Envío:", style = MaterialTheme.typography.bodySmall)
                            Text(String.format(Locale.US, "+$%.2f", order.deliveryFee), fontWeight = FontWeight.SemiBold)
                        }
                    }

                    HorizontalDivider()

                    Text("Método de Pago:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Efectivo", "Transferencia", "Tarjeta Débito", "Tarjeta Crédito").forEach { method ->
                            FilterChip(
                                selected = paymentMethod == method,
                                onClick = { paymentMethod = method },
                                label = { Text(method, fontSize = 11.sp) }
                            )
                        }
                    }

                    Text("Propina Opcional:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(0.0 to "0% (Estándar)", 5.0 to "5%", 10.0 to "10%").forEach { (pct, lbl) ->
                            FilterChip(
                                selected = tipPct == pct,
                                onClick = { tipPct = pct },
                                label = { Text(lbl, fontSize = 11.sp) }
                            )
                        }
                    }

                    HorizontalDivider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("TOTAL COBRADO:", fontWeight = FontWeight.ExtraBold)
                        Text(
                            text = String.format(Locale.US, "$%.2f", totalToCharge),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.payAndCloseOrder(
                            orderId = order.id,
                            paymentMethod = paymentMethod,
                            tipPercentage = tipPct,
                            tipAmount = tipValue
                        ) {
                            orderForQuickSettle = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusGreen)
                ) {
                    Text("Confirmar Entrega")
                }
            },
            dismissButton = {
                TextButton(onClick = { orderForQuickSettle = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun TakeoutTrackingOrderCard(
    orderWithItems: OrderWithItems,
    isSelectedOnMap: Boolean,
    onSelectOnMap: () -> Unit,
    onOpenDetail: () -> Unit,
    onAssignDriverClick: () -> Unit,
    onAdvanceStageClick: () -> Unit
) {
    val order = orderWithItems.order
    val items = orderWithItems.items
    val elapsedMinutes = ((System.currentTimeMillis() - (order.sentToKitchenAt ?: order.createdAt)) / 60000L).coerceAtLeast(0)

    val accentBorder = when {
        isSelectedOnMap -> MaterialTheme.colorScheme.primary
        order.orderType == OrderType.PICK_UP -> Color(0xFF8E24AA).copy(alpha = 0.5f)
        else -> Color(0xFF00838F).copy(alpha = 0.5f)
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(if (isSelectedOnMap) 2.dp else 1.dp, accentBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelectedOnMap) 4.dp else 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelectOnMap() }
            .testTag("tracking_order_card_${order.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OrderTypeBadge(orderType = order.orderType)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "#${order.id}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = if (order.createdByRole == "MESERO") Color(0xFFE3F2FD) else Color(0xFFF3E5F5),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = if (order.createdByRole == "MESERO") "Mesero" else "Mostrador",
                            color = if (order.createdByRole == "MESERO") Color(0xFF1565C0) else Color(0xFF6A1B9A),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                FulfillmentStageBadge(stage = order.fulfillmentStage)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Customer & Destination Details
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = order.customerName.ifBlank { order.tableNumber },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (order.customerPhone.isNotBlank()) {
                    Spacer(modifier = Modifier.width(10.dp))
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = order.customerPhone,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            if (order.orderType == OrderType.DELIVERY) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color(0xFF00838F),
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${order.deliveryZone.label}: ${order.deliveryAddress}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        color = if (order.assignedDriver.isBlank()) Color(0xFFFFEBEE) else Color(0xFFE0F7FA),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.clickable { onAssignDriverClick() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeliveryDining,
                                contentDescription = null,
                                tint = if (order.assignedDriver.isBlank()) Color(0xFFC62828) else Color(0xFF006064),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (order.assignedDriver.isBlank()) "+ Asignar Repartidor" else order.assignedDriver,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (order.assignedDriver.isBlank()) Color(0xFFC62828) else Color(0xFF006064)
                            )
                        }
                    }

                    Text(
                        text = "⏱ Hace ${elapsedMinutes}m • Envío: $${String.format(Locale.US, "%.0f", order.deliveryFee)}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (elapsedMinutes >= 25) Color(0xFFD32F2F) else StatusOrange
                    )
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Storefront,
                            contentDescription = null,
                            tint = Color(0xFF6A1B9A),
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Retiro en Mostrador • Creado por ${order.waiterName.split(" ").firstOrNull()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "⏱ Hace ${elapsedMinutes}m",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (order.fulfillmentStage == FulfillmentStage.LISTO_MOSTRADOR) StatusPurple else StatusOrange
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Items preview
            val preview = items.joinToString(", ") { "${it.quantity}x ${it.productName}" }
            Text(
                text = preview,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))

            // Bottom Actions & Total
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${items.sumOf { it.quantity }} prod.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = String.format(Locale.US, "$%.2f", order.totalAmount + order.deliveryFee),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onOpenDetail,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("Comanda", fontSize = 12.sp)
                    }

                    Button(
                        onClick = onAdvanceStageClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = nextActionColor(order.orderType, order.fulfillmentStage)
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("advance_takeout_btn_${order.id}")
                    ) {
                        Text(
                            text = nextActionLabel(order.orderType, order.fulfillmentStage),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}
