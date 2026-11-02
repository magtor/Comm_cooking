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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.OutdoorGrill
import androidx.compose.material.icons.filled.RemoveShoppingCart
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.TableBar
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.OrderItem
import com.example.data.model.OrderType
import com.example.ui.components.EditItemNoteDialog
import com.example.ui.viewmodel.ComandaViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    viewModel: ComandaViewModel,
    onNavigateBack: () -> Unit,
    onComandaSent: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    val cartCount by viewModel.cartTotalCount.collectAsStateWithLifecycle()
    val cartTotal by viewModel.cartTotalAmount.collectAsStateWithLifecycle()
    val targetTable by viewModel.targetTable.collectAsStateWithLifecycle()
    val targetOrderId by viewModel.targetOrderId.collectAsStateWithLifecycle()
    val draftOrderType by viewModel.draftOrderType.collectAsStateWithLifecycle()
    val draftCustomerName by viewModel.draftCustomerName.collectAsStateWithLifecycle()
    val draftDeliveryZone by viewModel.draftDeliveryZone.collectAsStateWithLifecycle()
    val draftDeliveryFee by viewModel.draftDeliveryFee.collectAsStateWithLifecycle()
    val generalNotes by viewModel.generalNotes.collectAsStateWithLifecycle()

    var editingItemIndex by remember { mutableStateOf<Int?>(null) }
    var showConfirmSendDialog by remember { mutableStateOf(false) }
    var showConfirmClearDialog by remember { mutableStateOf(false) }

    val destinationLabel = when (draftOrderType) {
        OrderType.DINE_IN -> targetTable?.let { "${it.tableNumber} (${it.zone})" } ?: "En Salón"
        OrderType.PICK_UP -> "Pick-Up • Cliente: ${draftCustomerName.ifBlank { "Mostrador" }}"
        OrderType.DELIVERY -> "Delivery (${draftDeliveryZone.label}) • ${draftCustomerName.ifBlank { "Domicilio" }}"
    }
    val effectiveCartTotal = cartTotal + (if (targetOrderId == null && draftOrderType == OrderType.DELIVERY) draftDeliveryFee else 0.0)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (targetOrderId != null) "Adicionales Comanda #${targetOrderId}" else "Revisar ${draftOrderType.shortLabel}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = destinationLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("cart_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Regresar al menú"
                        )
                    }
                },
                actions = {
                    if (cartItems.isNotEmpty()) {
                        IconButton(
                            onClick = { showConfirmClearDialog = true },
                            modifier = Modifier.testTag("clear_cart_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Vaciar comanda",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            if (cartItems.isNotEmpty()) {
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
                        // Summary row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (targetOrderId == null && draftOrderType == OrderType.DELIVERY && draftDeliveryFee > 0) {
                                        "Total ($cartCount items + Envío $${String.format(Locale.US, "%.0f", draftDeliveryFee)})"
                                    } else {
                                        "Total Comanda ($cartCount ${if (cartCount == 1) "item" else "items"})"
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = String.format(Locale.US, "$%.2f", effectiveCartTotal),
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            // Secondary Draft option
                            if (targetOrderId == null) {
                                OutlinedButton(
                                    onClick = {
                                        viewModel.saveComandaAsDraft { orderId ->
                                            onComandaSent(orderId)
                                        }
                                    },
                                    modifier = Modifier.testTag("save_draft_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Save,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Borrador")
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Primary Send to Kitchen button
                        Button(
                            onClick = { showConfirmSendDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("confirm_send_to_kitchen_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.OutdoorGrill,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (targetOrderId != null) "ENVIAR ADICIONALES AL COCINERO" else "ENVIAR PEDIDO A COCINA",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        if (cartItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.size(80.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.RemoveShoppingCart,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "El carrito de comanda está vacío",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Selecciona platillos o bebidas del menú para agregarlos a la orden.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = onNavigateBack,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("empty_cart_return_btn")
                    ) {
                        Text("Explorar Menú")
                    }
                }
            }
        } else {
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
                // Table header info
                item {
                    targetTable?.let { table ->
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TableBar,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "${table.tableNumber} • ${table.zone}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = "Mesero: ${table.waiterName} • ${table.activeDinersCount} comensales",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }
                }

                // If adding to existing order, show highlighted comanda banner
                if (targetOrderId != null) {
                    item {
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Adicionales para Comanda #${targetOrderId}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    Text(
                                        text = "Se agregarán estos ${cartItems.sumOf { it.quantity }} platillos nuevos a la comanda activa y la mesa pasará a estado 'En Cocina'.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.85f)
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "Platillos seleccionados (${cartItems.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // List of items in cart
                itemsIndexed(cartItems) { index, item ->
                    CartItemRow(
                        item = item,
                        index = index,
                        onIncrease = {
                            viewModel.updateItemQuantity(index, item.quantity + 1)
                        },
                        onDecrease = {
                            viewModel.updateItemQuantity(index, item.quantity - 1)
                        },
                        onRemove = {
                            viewModel.removeItemFromCart(index)
                        },
                        onEditNotes = {
                            editingItemIndex = index
                        },
                        onClearNotes = {
                            viewModel.updateItemNotes(index, "")
                        },
                        onQuickNote = { quickNote ->
                            val current = item.notes
                            val updated = if (current.isBlank()) {
                                quickNote
                            } else if (current.contains(quickNote)) {
                                current.replace(quickNote, "").replace(", ,", ",").trim(',', ' ')
                            } else {
                                "$current, $quickNote"
                            }
                            viewModel.updateItemNotes(index, updated)
                        }
                    )
                }

                // General order note for kitchen
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Indicaciones generales para la comanda",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = generalNotes,
                        onValueChange = { viewModel.setGeneralNotes(it) },
                        placeholder = { Text("Ej. Servir bebidas primero, mesa celebra aniversario...") },
                        trailingIcon = {
                            if (generalNotes.isNotBlank()) {
                                IconButton(onClick = { viewModel.setGeneralNotes("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Limpiar nota general"
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("general_order_notes_input"),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 3
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Sugerencias generales rápidas:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
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
                        ).forEach { suggestion ->
                            FilterChip(
                                selected = generalNotes.contains(suggestion),
                                onClick = {
                                    val current = generalNotes.trim()
                                    if (current.contains(suggestion)) {
                                        viewModel.setGeneralNotes(
                                            current.replace(suggestion, "").replace(", ,", ",").trim(',', ' ')
                                        )
                                    } else {
                                        viewModel.setGeneralNotes(
                                            if (current.isEmpty()) suggestion else "$current, $suggestion"
                                        )
                                    }
                                },
                                label = { Text(suggestion, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialog to edit specific item notes
    editingItemIndex?.let { index ->
        if (index in cartItems.indices) {
            val item = cartItems[index]
            EditItemNoteDialog(
                initialNotes = item.notes,
                productName = item.productName,
                onDismiss = { editingItemIndex = null },
                onSaveNotes = { notes ->
                    viewModel.updateItemNotes(index, notes)
                    editingItemIndex = null
                }
            )
        }
    }

    // Confirmation dialog before sending to kitchen
    if (showConfirmSendDialog) {
        val isAddingToExisting = targetOrderId != null
        AlertDialog(
            onDismissRequest = { showConfirmSendDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.OutdoorGrill,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = if (isAddingToExisting) "¿Enviar adicionales al cocinero?" else "¿Enviar comanda a cocina?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = if (isAddingToExisting) {
                            "Se enviarán $cartCount productos adicionales a la comanda #${targetOrderId} ($destinationLabel). Volverá a marcarse 'En Cocina' para preparación."
                        } else {
                            when (draftOrderType) {
                                OrderType.DINE_IN -> "Se enviarán $cartCount productos para ${targetTable?.tableNumber ?: "la mesa"} a cocina."
                                OrderType.PICK_UP -> "Se enviarán $cartCount productos de Pick-Up ($destinationLabel) a cocina y se sumará al Mapa de Seguimiento de Mostrador."
                                OrderType.DELIVERY -> "Se enviarán $cartCount productos de Delivery ($destinationLabel) a cocina y aparecerá en el Mapa de Seguimiento."
                            }
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Total: ${String.format(Locale.US, "$%.2f", effectiveCartTotal)}",
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 16.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmSendDialog = false
                        viewModel.sendComandaToKitchen { orderId ->
                            onComandaSent(orderId)
                        }
                    },
                    modifier = Modifier.testTag("modal_confirm_send_btn")
                ) {
                    Text(if (isAddingToExisting) "Confirmar y Reenviar" else "Confirmar y Enviar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmSendDialog = false }) {
                    Text("Revisar más")
                }
            }
        )
    }

    // Clear cart confirmation
    if (showConfirmClearDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmClearDialog = false },
            title = { Text("¿Vaciar carrito?") },
            text = { Text("Se eliminarán todos los platillos agregados a esta comanda.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearCart()
                        showConfirmClearDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Vaciar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmClearDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun CartItemRow(
    item: OrderItem,
    index: Int,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onRemove: () -> Unit,
    onEditNotes: () -> Unit,
    onClearNotes: () -> Unit = {},
    onQuickNote: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("cart_item_$index")
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.productName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${String.format(Locale.US, "$%.2f", item.unitPrice)} c/u",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Delete button
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("remove_item_${item.productId}")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Eliminar ${item.productName}",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Stepper and Subtotal
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Quantity Stepper
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.height(40.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        IconButton(
                            onClick = onDecrease,
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("decrease_qty_${item.productId}")
                        ) {
                            Text("-", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            text = "${item.quantity}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                        IconButton(
                            onClick = onIncrease,
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("increase_qty_${item.productId}")
                        ) {
                            Text("+", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Item Subtotal
                Text(
                    text = String.format(Locale.US, "$%.2f", item.unitPrice * item.quantity),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Kitchen note or Add note button
            if (item.notes.isNotBlank()) {
                Surface(
                    color = Color(0xFFFFF9C4),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Color(0xFFF57F17),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "NOTA PARA COCINA:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE65100)
                                )
                            }

                            Row {
                                IconButton(
                                    onClick = onEditNotes,
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Editar nota",
                                        tint = Color(0xFFF57F17),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                IconButton(
                                    onClick = onClearNotes,
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Quitar nota",
                                        tint = Color(0xFF8D6E63),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = item.notes,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF3E2723)
                        )
                    }
                }
            } else {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(
                            onClick = onEditNotes,
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                            modifier = Modifier.testTag("add_note_btn_${item.productId}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "+ Agregar nota a cocina",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Direct quick chips for fastest entry (user requested examples: sin pimienta, término medio, sin cebolla)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "Sin cebolla",
                            "Carne término medio",
                            "Sin pimienta",
                            "Salsa aparte",
                            "Bien cocido"
                        ).forEach { quickOption ->
                            SuggestionChip(
                                onClick = { onQuickNote(quickOption) },
                                label = { Text("+ $quickOption", fontSize = 11.sp) },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
