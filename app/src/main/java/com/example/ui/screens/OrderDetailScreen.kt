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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.OutdoorGrill
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.RoomService
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.TableBar
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.FulfillmentStage
import com.example.data.model.OrderItem
import com.example.data.model.OrderStatus
import com.example.data.model.OrderType
import com.example.data.model.OrderWithItems
import com.example.data.model.RestaurantTable
import com.example.ui.components.EditItemNoteDialog
import com.example.ui.components.FulfillmentStageBadge
import com.example.ui.components.OrderStatusBadge
import com.example.ui.components.OrderTypeBadge
import com.example.ui.theme.StatusAmber
import com.example.ui.theme.StatusBlue
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusOrange
import com.example.ui.theme.StatusPurple
import com.example.ui.viewmodel.ComandaViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    orderId: Long,
    viewModel: ComandaViewModel,
    onNavigateBack: () -> Unit,
    onAddMoreProducts: () -> Unit,
    modifier: Modifier = Modifier
) {
    val allOrders by viewModel.allOrdersHistory.collectAsStateWithLifecycle()
    val tables by viewModel.tables.collectAsStateWithLifecycle()
    val orderWithItems = allOrders.firstOrNull { it.order.id == orderId }

    var showPreBillDialog by remember { mutableStateOf(false) }
    var showPostgresPayloadDialog by remember { mutableStateOf(false) }
    var showPaymentDialog by remember { mutableStateOf(false) }
    var selectedPaymentMethod by remember { mutableStateOf("Efectivo") }
    var selectedTipPct by remember { mutableDoubleStateOf(5.0) } // Default 5% tip
    var customTipText by remember { mutableStateOf("") }
    var editingOrderItem by remember { mutableStateOf<OrderItem?>(null) }
    var showEditGeneralNotesDialog by remember { mutableStateOf(false) }
    var tempGeneralNotes by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Comanda #${orderId}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        orderWithItems?.let {
                            Text(
                                text = "${it.order.tableNumber} • Mesero: ${it.order.waiterName}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("order_detail_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Regresar"
                        )
                    }
                },
                actions = {
                    if (orderWithItems?.order?.status != OrderStatus.PAGADA) {
                        IconButton(
                            onClick = {
                                orderWithItems?.let { owi ->
                                    val table = if (owi.order.orderType == OrderType.DINE_IN) {
                                        tables.firstOrNull { it.id == owi.order.tableId }
                                            ?: RestaurantTable(id = owi.order.tableId, tableNumber = owi.order.tableNumber)
                                    } else null
                                    viewModel.prepareForAddingToOrder(
                                        table = table,
                                        orderId = orderId,
                                        orderType = owi.order.orderType,
                                        customerName = owi.order.customerName
                                    )
                                    onAddMoreProducts()
                                }
                            },
                            modifier = Modifier.testTag("top_bar_add_products_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Agregar más productos",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    IconButton(
                        onClick = { showPostgresPayloadDialog = true },
                        modifier = Modifier.testTag("show_postgres_payload_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudSync,
                            contentDescription = "Ver JSON API PostgreSQL"
                        )
                    }
                    IconButton(
                        onClick = { showPreBillDialog = true },
                        modifier = Modifier.testTag("show_prebill_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Receipt,
                            contentDescription = "Ver pre-cuenta"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            orderWithItems?.let { owi ->
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Quick status action buttons
                        when (owi.order.status) {
                            OrderStatus.DRAFT -> {
                                Button(
                                    onClick = {
                                        viewModel.updateOrderStatus(orderId, OrderStatus.ENVIADA_COCINA)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(imageVector = Icons.Default.OutdoorGrill, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Enviar Borrador a Cocina")
                                }
                            }
                            OrderStatus.ENVIADA_COCINA -> {
                                Button(
                                    onClick = {
                                        viewModel.updateOrderStatus(orderId, OrderStatus.LISTA)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = StatusPurple),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(imageVector = Icons.Default.RoomService, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Simular Cocina Lista (Listo p/ Servir)")
                                }
                            }
                            OrderStatus.LISTA -> {
                                Button(
                                    onClick = {
                                        viewModel.updateOrderStatus(orderId, OrderStatus.SERVIDA)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00796B)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("¡Comida Lista! Marcar como Servido")
                                }
                            }
                            OrderStatus.SERVIDA -> {
                                Button(
                                    onClick = {
                                        viewModel.updateOrderStatus(orderId, OrderStatus.CUENTA)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = StatusBlue),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(imageVector = Icons.Default.Receipt, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Solicitar Cuenta")
                                }
                            }
                            OrderStatus.CUENTA -> {
                                Button(
                                    onClick = {
                                        selectedTipPct = if (owi.order.orderType == OrderType.DINE_IN) 5.0 else 0.0
                                        showPaymentDialog = true
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = StatusGreen),
                                    modifier = Modifier.fillMaxWidth().testTag("btn_open_payment_dialog")
                                ) {
                                    Icon(imageVector = Icons.Default.Payments, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Cobrar Comanda & Registrar Propina")
                                }
                            }
                            OrderStatus.PAGADA -> {
                                Text(
                                    text = "Comanda Pagada y Cerrada",
                                    color = Color(0xFF616161),
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                )
                            }
                        }

                        if (owi.order.status != OrderStatus.PAGADA) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    val table = if (owi.order.orderType == OrderType.DINE_IN) {
                                        tables.firstOrNull { it.id == owi.order.tableId }
                                            ?: RestaurantTable(id = owi.order.tableId, tableNumber = owi.order.tableNumber)
                                    } else null
                                    viewModel.prepareForAddingToOrder(
                                        table = table,
                                        orderId = orderId,
                                        orderType = owi.order.orderType,
                                        customerName = owi.order.customerName
                                    )
                                    onAddMoreProducts()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("order_detail_add_more_btn")
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Agregar más productos (Reenviar a cocina)", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        if (orderWithItems == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Comanda no encontrada")
            }
        } else {
            val order = orderWithItems.order
            val items = orderWithItems.items

            LazyColumn(
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = innerPadding.calculateTopPadding() + 8.dp,
                    bottom = innerPadding.calculateBottomPadding() + 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // Status Header Card
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
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
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                if (order.orderType == OrderType.DINE_IN) {
                                    OrderStatusBadge(status = order.status)
                                } else {
                                    FulfillmentStageBadge(stage = order.fulfillmentStage)
                                }
                            }

                            if (order.orderType != OrderType.DINE_IN) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Person,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(15.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Cliente: ${order.customerName.ifBlank { "Mostrador" }}",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp
                                            )
                                            if (order.customerPhone.isNotBlank()) {
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Text(
                                                    text = "📞 ${order.customerPhone}",
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                        if (order.orderType == OrderType.DELIVERY) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "📍 ${order.deliveryZone.label}: ${order.deliveryAddress}",
                                                fontSize = 12.sp
                                            )
                                            Text(
                                                text = "🛵 Repartidor: ${order.assignedDriver.ifBlank { "Por asignar" }} • Envío: $${String.format(Locale.US, "%.2f", order.deliveryFee)}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFF00838F)
                                            )
                                        } else {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "🛍️ Retiro en Mostrador • Registrado por: ${order.waiterName} (${order.createdByRole})",
                                                fontSize = 11.sp,
                                                color = Color(0xFF6A1B9A)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Order Time and Elapsed
                            val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
                            val createdStr = timeFormat.format(Date(order.createdAt))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Hora comanda: $createdStr",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                order.sentToKitchenAt?.let { sentAt ->
                                    val elapsedMinutes = ((System.currentTimeMillis() - sentAt) / (60 * 1000)).coerceAtLeast(0)
                                    Text(
                                        text = "⏱ En cocina: hace ${elapsedMinutes}m",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (order.status == OrderStatus.LISTA) StatusPurple else StatusOrange
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Visual Progress Stepper
                            OrderProgressTrack(currentStatus = order.status)
                        }
                    }
                }

                // General Notes section
                if (order.generalNotes.isNotBlank()) {
                    item {
                        Surface(
                            color = Color(0xFFFFF9C4),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = order.status != OrderStatus.PAGADA) {
                                    tempGeneralNotes = order.generalNotes
                                    showEditGeneralNotesDialog = true
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Color(0xFFF57F17),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Indicaciones generales de la comanda:",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF5D4037)
                                    )
                                    Text(
                                        text = order.generalNotes,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF3E2723)
                                    )
                                }
                                if (order.status != OrderStatus.PAGADA) {
                                    IconButton(
                                        onClick = {
                                            tempGeneralNotes = order.generalNotes
                                            showEditGeneralNotesDialog = true
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Editar notas generales",
                                            tint = Color(0xFFF57F17),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else if (order.status != OrderStatus.PAGADA) {
                    item {
                        TextButton(
                            onClick = {
                                tempGeneralNotes = ""
                                showEditGeneralNotesDialog = true
                            },
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "+ Agregar indicación general a comanda",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Items list header
                item {
                    Text(
                        text = "Productos en la Comanda (${items.sumOf { it.quantity }})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Items list
                items(items, key = { it.id }) { item ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = "${item.quantity}",
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = item.productName,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = String.format(Locale.US, "$%.2f c/u", item.unitPrice),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Text(
                                    text = String.format(Locale.US, "$%.2f", item.subtotal),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            if (item.notes.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    color = Color(0xFFFFF9C4),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(enabled = order.status != OrderStatus.PAGADA) {
                                            editingOrderItem = item
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = null,
                                            tint = Color(0xFFF57F17),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Nota cocina: ${item.notes}",
                                            fontSize = 12.sp,
                                            color = Color(0xFF3E2723),
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.weight(1f)
                                        )
                                        if (order.status != OrderStatus.PAGADA) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Editar nota",
                                                tint = Color(0xFFF57F17),
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            } else if (order.status != OrderStatus.PAGADA) {
                                Spacer(modifier = Modifier.height(4.dp))
                                TextButton(
                                    onClick = { editingOrderItem = item },
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = null,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "+ Nota a cocina (término, sin...)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }

                // Direct Add More Products Card
                if (order.status != OrderStatus.PAGADA) {
                    item {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val table = if (order.orderType == OrderType.DINE_IN) {
                                        tables.firstOrNull { it.id == order.tableId }
                                            ?: RestaurantTable(id = order.tableId, tableNumber = order.tableNumber)
                                    } else null
                                    viewModel.prepareForAddingToOrder(
                                        table = table,
                                        orderId = orderId,
                                        orderType = order.orderType,
                                        customerName = order.customerName
                                    )
                                    onAddMoreProducts()
                                }
                                .testTag("card_add_more_products_direct")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Agregar más productos a la comanda",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = "Mesa ocupada • Sumar platillos y reenviar al cocinero",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                    )
                                }
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                // Financial Breakdown
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Subtotal productos", style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    text = String.format(Locale.US, "$%.2f", order.totalAmount),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            if (order.deliveryFee > 0.0) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "Tarifa de envío (Delivery)", style = MaterialTheme.typography.bodySmall)
                                    Text(
                                        text = String.format(Locale.US, "+$%.2f", order.deliveryFee),
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            if (order.status == OrderStatus.PAGADA) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Propina voluntaria (${order.tipPercentage.toInt()}%)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = StatusGreen
                                    )
                                    Text(
                                        text = String.format(Locale.US, "+$%.2f", order.effectiveTip),
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = StatusGreen
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Método de Pago",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = order.paymentMethod,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            } else if (order.orderType == OrderType.DINE_IN) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Propina sugerida (5%)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = String.format(Locale.US, "$%.2f", order.totalAmount * 0.05),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (order.status == OrderStatus.PAGADA) "Total Cobrado" else "Total a Pagar",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = String.format(
                                        Locale.US,
                                        "$%.2f",
                                        if (order.status == OrderStatus.PAGADA) order.grandTotal else (order.totalAmount + order.deliveryFee)
                                    ),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Pre-bill ticket dialog
    if (showPreBillDialog && orderWithItems != null) {
        val owi = orderWithItems
        AlertDialog(
            onDismissRequest = { showPreBillDialog = false },
            icon = {
                Icon(imageVector = Icons.Default.Receipt, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            },
            title = {
                Text("Pre-cuenta para el Cliente", fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(text = "Restaurante ComandaPro", fontWeight = FontWeight.Bold)
                    Text(text = "${owi.order.tableNumber} • Folio #${owi.order.id}", style = MaterialTheme.typography.bodySmall)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    owi.items.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "${item.quantity}x ${item.productName}", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                            Text(text = String.format(Locale.US, "$%.2f", item.subtotal), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "TOTAL:", fontWeight = FontWeight.Bold)
                        Text(text = String.format(Locale.US, "$%.2f", owi.order.totalAmount), fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showPreBillDialog = false }) {
                    Text("Cerrar")
                }
            }
        )
    }

    // PostgreSQL / REST API Payload Dialog for React POS integration
    if (showPostgresPayloadDialog && orderWithItems != null) {
        val owi = orderWithItems
        val jsonPayload = buildString {
            append("{\n")
            append("  \"event\": \"SYNC_ORDER\",\n")
            append("  \"timestamp\": ${System.currentTimeMillis()},\n")
            append("  \"order\": {\n")
            append("    \"id\": ${owi.order.id},\n")
            append("    \"order_type\": \"${owi.order.orderType.name}\",\n")
            append("    \"fulfillment_stage\": \"${owi.order.fulfillmentStage.name}\",\n")
            append("    \"table_id\": ${if (owi.order.orderType == OrderType.DINE_IN) owi.order.tableId.toString() else "null"},\n")
            append("    \"table_number\": \"${owi.order.tableNumber}\",\n")
            append("    \"customer_name\": \"${owi.order.customerName}\",\n")
            append("    \"customer_phone\": \"${owi.order.customerPhone}\",\n")
            append("    \"delivery_address\": \"${owi.order.deliveryAddress}\",\n")
            append("    \"delivery_zone\": \"${owi.order.deliveryZone.name}\",\n")
            append("    \"assigned_driver\": \"${owi.order.assignedDriver}\",\n")
            append("    \"delivery_fee\": ${String.format(Locale.US, "%.2f", owi.order.deliveryFee)},\n")
            append("    \"created_by_role\": \"${owi.order.createdByRole}\",\n")
            append("    \"waiter_id\": \"${owi.order.waiterId}\",\n")
            append("    \"waiter_name\": \"${owi.order.waiterName}\",\n")
            append("    \"status\": \"${owi.order.status.name}\",\n")
            append("    \"total_amount\": ${String.format(Locale.US, "%.2f", owi.order.totalAmount)},\n")
            append("    \"general_notes\": \"${owi.order.generalNotes}\"\n")
            append("  },\n")
            append("  \"items\": [\n")
            owi.items.forEachIndexed { idx, item ->
                append("    {\n")
                append("      \"id\": ${item.id},\n")
                append("      \"product_id\": ${item.productId},\n")
                append("      \"product_name\": \"${item.productName}\",\n")
                append("      \"quantity\": ${item.quantity},\n")
                append("      \"unit_price\": ${String.format(Locale.US, "%.2f", item.unitPrice)},\n")
                append("      \"subtotal\": ${String.format(Locale.US, "%.2f", item.subtotal)},\n")
                append("      \"kitchen_notes\": \"${item.notes}\"\n")
                append("    }${if (idx < owi.items.lastIndex) "," else ""}\n")
            }
            append("  ]\n")
            append("}")
        }

        AlertDialog(
            onDismissRequest = { showPostgresPayloadDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.CloudSync,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = {
                Text("Payload JSON (API & PostgreSQL)", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Estructura que recibe tu backend REST API / PostgreSQL para sincronizar con tu vista de meseros en React:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        LazyColumn(modifier = Modifier.height(240.dp).padding(10.dp)) {
                            item {
                                Text(
                                    text = jsonPayload,
                                    fontSize = 11.sp,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Mesero autenticado: ${owi.order.waiterName} (${owi.order.waiterId})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showPostgresPayloadDialog = false }) {
                    Text("Entendido")
                }
            }
        )
    }

    // Payment and Tip Collection Dialog for closing the order
    if (showPaymentDialog && orderWithItems != null) {
        val owi = orderWithItems
        val order = owi.order
        val subtotal = order.totalAmount
        val calculatedTip = if (selectedTipPct >= 0) {
            subtotal * (selectedTipPct / 100.0)
        } else {
            customTipText.toDoubleOrNull() ?: 0.0
        }
        val totalWithTip = subtotal + order.deliveryFee + calculatedTip

        AlertDialog(
            onDismissRequest = { showPaymentDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Payments,
                    contentDescription = null,
                    tint = StatusGreen,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text("Cobro y Propina • ${order.tableNumber}", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Subtotal
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Subtotal consumo:", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            String.format(Locale.US, "$%.2f", subtotal),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    HorizontalDivider()

                    // Payment Method selector
                    Text(
                        "Método de Pago:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Efectivo", "Tarjeta Débito", "Tarjeta Crédito", "Transferencia").forEach { method ->
                            FilterChip(
                                selected = selectedPaymentMethod == method,
                                onClick = { selectedPaymentMethod = method },
                                label = { Text(method, fontSize = 12.sp) }
                            )
                        }
                    }

                    // Tip Selection
                    Text(
                        "Propina (Voluntaria):",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            5.0 to "5% Sugerida",
                            10.0 to "10%",
                            15.0 to "15%",
                            0.0 to "Sin Propina"
                        ).forEach { (pct, label) ->
                            FilterChip(
                                selected = selectedTipPct == pct,
                                onClick = { selectedTipPct = pct },
                                label = { Text(label, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = StatusGreen.copy(alpha = 0.2f),
                                    selectedLabelColor = Color(0xFF1B5E20)
                                )
                            )
                        }
                    }

                    // Explanatory note
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "En restaurantes la propina es voluntaria y no exigida por ley. Por defecto se calcula el 5% sugerido ($${String.format(Locale.US, "%.2f", subtotal * 0.05)}).",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    HorizontalDivider()

                    // Totals preview
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Propina calculada:", style = MaterialTheme.typography.bodyMedium, color = StatusGreen)
                        Text(
                            String.format(Locale.US, "+$%.2f", calculatedTip),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = StatusGreen
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("TOTAL FINAL A COBRAR:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Black)
                        Text(
                            String.format(Locale.US, "$%.2f", totalWithTip),
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
                            paymentMethod = selectedPaymentMethod,
                            tipPercentage = if (selectedTipPct >= 0) selectedTipPct else 0.0,
                            tipAmount = calculatedTip
                        ) {
                            showPaymentDialog = false
                            onNavigateBack()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusGreen),
                    modifier = Modifier.testTag("btn_confirm_payment_close")
                ) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Confirmar Cobro")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showPaymentDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Dialog for editing specific item note in existing order
    editingOrderItem?.let { itemToEdit ->
        EditItemNoteDialog(
            initialNotes = itemToEdit.notes,
            productName = itemToEdit.productName,
            confirmButtonText = "Actualizar en Comanda",
            titleText = "Modificar Nota para Cocina",
            onDismiss = { editingOrderItem = null },
            onSaveNotes = { newNotes ->
                viewModel.updateExistingOrderItemNotes(itemToEdit.id, newNotes)
                editingOrderItem = null
            }
        )
    }

    // Dialog for editing general order notes in existing order
    if (showEditGeneralNotesDialog && orderWithItems != null) {
        val owi = orderWithItems
        AlertDialog(
            onDismissRequest = { showEditGeneralNotesDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = {
                Text("Indicaciones Generales", fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(
                        text = "Instrucciones de la comanda para cocina y barra (${owi.order.tableNumber}):",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = tempGeneralNotes,
                        onValueChange = { tempGeneralNotes = it },
                        placeholder = { Text("Ej. Servir bebidas primero, mesa con prisa...") },
                        trailingIcon = {
                            if (tempGeneralNotes.isNotBlank()) {
                                IconButton(onClick = { tempGeneralNotes = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Limpiar"
                                    )
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 3
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Sugerencias rápidas:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "⚡ Mesa con prisa",
                            "🥤 Servir bebidas primero",
                            "🍽️ Traer todo al centro",
                            "🎂 Cumpleaños / Festejo",
                            "🧾 Cuentas separadas"
                        ).forEach { sug ->
                            FilterChip(
                                selected = tempGeneralNotes.contains(sug),
                                onClick = {
                                    val curr = tempGeneralNotes.trim()
                                    tempGeneralNotes = if (curr.contains(sug)) {
                                        curr.replace(sug, "").replace(", ,", ",").trim(',', ' ')
                                    } else {
                                        if (curr.isEmpty()) sug else "$curr, $sug"
                                    }
                                },
                                label = { Text(sug, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateExistingOrderGeneralNotes(orderId, tempGeneralNotes.trim())
                        showEditGeneralNotesDialog = false
                    }
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditGeneralNotesDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun OrderProgressTrack(currentStatus: OrderStatus) {
    val steps = listOf(
        Pair(OrderStatus.DRAFT, "Borrador"),
        Pair(OrderStatus.ENVIADA_COCINA, "Cocina"),
        Pair(OrderStatus.LISTA, "Listo"),
        Pair(OrderStatus.SERVIDA, "Servido"),
        Pair(OrderStatus.CUENTA, "Cuenta"),
        Pair(OrderStatus.PAGADA, "Pagada")
    )

    val currentIndex = steps.indexOfFirst { it.first == currentStatus }.coerceAtLeast(0)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, pair ->
            val isCompleted = index <= currentIndex
            val isCurrent = index == currentIndex

            val stepColor = when {
                isCurrent -> MaterialTheme.colorScheme.primary
                isCompleted -> StatusGreen
                else -> MaterialTheme.colorScheme.surfaceVariant
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = CircleShape,
                    color = stepColor,
                    modifier = Modifier.size(if (isCurrent) 22.dp else 16.dp)
                ) {
                    if (isCompleted && !isCurrent) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = pair.second,
                    fontSize = 9.sp,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                    color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (index < steps.lastIndex) {
                Box(
                    modifier = Modifier
                        .height(3.dp)
                        .weight(0.5f)
                        .background(
                            if (index < currentIndex) StatusGreen else MaterialTheme.colorScheme.surfaceVariant
                        )
                )
            }
        }
    }
}
