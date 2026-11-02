package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.TableBar
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DeliveryZone
import com.example.data.model.OrderType
import com.example.data.model.RestaurantTable
import com.example.data.model.TableStatus
import com.example.data.model.User
import com.example.data.model.UserRole
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateChannelOrderDialog(
    initialOrderType: OrderType = OrderType.DINE_IN,
    availableTables: List<RestaurantTable>,
    currentUser: User?,
    operationRolePolicy: String,
    onDismiss: () -> Unit,
    onStartDineIn: (RestaurantTable, Int) -> Unit,
    onStartPickUp: (customerName: String, customerPhone: String, notes: String) -> Unit,
    onStartDelivery: (
        customerName: String,
        customerPhone: String,
        address: String,
        zone: DeliveryZone,
        driver: String,
        deliveryFee: Double,
        notes: String
    ) -> Unit
) {
    var selectedType by remember { mutableStateOf(initialOrderType) }

    // Dine-In states
    val freeTables = remember(availableTables) {
        availableTables.filter { it.status == TableStatus.LIBRE }
    }
    var selectedTable by remember(freeTables, availableTables) {
        mutableStateOf(freeTables.firstOrNull() ?: availableTables.firstOrNull())
    }
    var dinersCount by remember { mutableIntStateOf(2) }

    // Pick-Up & Delivery states
    var customerName by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }
    var deliveryAddress by remember { mutableStateOf("") }
    var selectedZone by remember { mutableStateOf(DeliveryZone.ZONA_NORTE) }
    var selectedDriver by remember { mutableStateOf("") }
    var selectedFee by remember { mutableDoubleStateOf(35.0) }
    var channelNotes by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }

    val driverOptions = listOf(
        "" to "Por asignar",
        "Javier R. (Moto 1)" to "Javier R. (Moto 1)",
        "Diego V. (Moto 2)" to "Diego V. (Moto 2)",
        "Repartidor App Externa" to "App Externa"
    )

    val isWaiterInDedicatedMode = operationRolePolicy == "DESPACHADOR" &&
            currentUser?.role == UserRole.MESERO &&
            selectedType != OrderType.DINE_IN

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Crear Nuevo Pedido",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Selecciona el tipo de servicio (Dine-In, Pick-Up o Delivery)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 3-Channel Selector Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        Triple(OrderType.DINE_IN, "Dine-In", Icons.Default.TableBar),
                        Triple(OrderType.PICK_UP, "Pick-Up", Icons.Default.ShoppingBag),
                        Triple(OrderType.DELIVERY, "Delivery", Icons.Default.DeliveryDining)
                    ).forEach { (type, title, icon) ->
                        val isSelected = selectedType == type
                        val accentColor = when (type) {
                            OrderType.DINE_IN -> MaterialTheme.colorScheme.primary
                            OrderType.PICK_UP -> Color(0xFF7B1FA2)
                            OrderType.DELIVERY -> Color(0xFF00838F)
                        }
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) accentColor.copy(alpha = 0.14f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                            ),
                            border = if (isSelected) BorderStroke(2.dp, accentColor) else null,
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    selectedType = type
                                    validationError = null
                                }
                                .testTag("channel_option_${type.name.lowercase()}")
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp, horizontal = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = title,
                                    tint = if (isSelected) accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = title,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                    color = if (isSelected) accentColor else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                // Advisory banner when policy suggests dedicated counter staff
                if (isWaiterInDedicatedMode) {
                    Surface(
                        color = Color(0xFFFFF8E1),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = Color(0xFFF57F17),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Modo 'Despachador Dedicado' activo: normalmente Mostrador/Caja crea los ${selectedType.shortLabel}, pero puedes registrarlo como apoyo.",
                                fontSize = 11.sp,
                                color = Color(0xFF5D4037)
                            )
                        }
                    }
                }

                HorizontalDivider()

                when (selectedType) {
                    OrderType.DINE_IN -> {
                        Text(
                            text = "1. Asignar Mesa Disponible:",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        if (freeTables.isEmpty()) {
                            Text(
                                text = "Todas las mesas están ocupadas actualmente. Selecciona una para agregar productos:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        val listToShow = if (freeTables.isNotEmpty()) freeTables else availableTables
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listToShow.forEach { table ->
                                FilterChip(
                                    selected = selectedTable?.id == table.id,
                                    onClick = { selectedTable = table },
                                    label = { Text("${table.tableNumber} (${table.capacity}p)", fontSize = 12.sp) }
                                )
                            }
                        }

                        Text(
                            text = "2. Número de Comensales:",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            IconButton(onClick = { if (dinersCount > 1) dinersCount-- }) {
                                Text("-", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                            }
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "$dinersCount",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                            IconButton(onClick = { if (dinersCount < 14) dinersCount++ }) {
                                Text("+", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    OrderType.PICK_UP -> {
                        Surface(
                            color = Color(0xFFF3E5F5),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Storefront,
                                    contentDescription = null,
                                    tint = Color(0xFF6A1B9A),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "El pedido irá directo a Cocina y aparecerá en el Mostrador Central del Mapa de Seguimiento.",
                                    fontSize = 11.sp,
                                    color = Color(0xFF4A148C)
                                )
                            }
                        }

                        OutlinedTextField(
                            value = customerName,
                            onValueChange = {
                                customerName = it
                                validationError = null
                            },
                            label = { Text("Nombre del Cliente *") },
                            placeholder = { Text("Ej. Mariana López") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("pickup_customer_name_input")
                        )

                        OutlinedTextField(
                            value = customerPhone,
                            onValueChange = { customerPhone = it },
                            label = { Text("Teléfono / WhatsApp (opcional)") },
                            placeholder = { Text("Ej. 555-239-8812") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text(
                            text = "Notas rápidas de retiro:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("Pasa en 15 min", "Incluir cubiertos", "Salsas aparte", "Pagado por transferencia").forEach { chip ->
                                FilterChip(
                                    selected = channelNotes.contains(chip),
                                    onClick = {
                                        channelNotes = if (channelNotes.contains(chip)) {
                                            channelNotes.replace(chip, "").trim(',', ' ')
                                        } else {
                                            if (channelNotes.isBlank()) chip else "$channelNotes, $chip"
                                        }
                                    },
                                    label = { Text(chip, fontSize = 11.sp) }
                                )
                            }
                        }
                    }

                    OrderType.DELIVERY -> {
                        OutlinedTextField(
                            value = customerName,
                            onValueChange = {
                                customerName = it
                                validationError = null
                            },
                            label = { Text("Nombre del Cliente *") },
                            placeholder = { Text("Ej. Andrea Castillo") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("delivery_customer_name_input")
                        )

                        OutlinedTextField(
                            value = customerPhone,
                            onValueChange = { customerPhone = it },
                            label = { Text("Teléfono de Contacto") },
                            placeholder = { Text("Ej. 555-402-9910") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = deliveryAddress,
                            onValueChange = {
                                deliveryAddress = it
                                validationError = null
                            },
                            label = { Text("Dirección de Entrega y Referencias *") },
                            placeholder = { Text("Ej. Av. Reforma 240, Depto 4B") },
                            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                            maxLines = 2,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("delivery_address_input")
                        )

                        Text(
                            text = "Zona en Mapa de Seguimiento:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            DeliveryZone.entries.filter { it != DeliveryZone.MOSTRADOR }.forEach { zone ->
                                FilterChip(
                                    selected = selectedZone == zone,
                                    onClick = { selectedZone = zone },
                                    label = { Text("${zone.label} (~${zone.estimatedMinutes}m)", fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFFE0F7FA),
                                        selectedLabelColor = Color(0xFF006064)
                                    )
                                )
                            }
                        }

                        Text(
                            text = "Repartidor & Costo de Envío:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            driverOptions.forEach { (value, label) ->
                                FilterChip(
                                    selected = selectedDriver == value,
                                    onClick = { selectedDriver = value },
                                    label = { Text(label, fontSize = 11.sp) }
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(0.0 to "Envío Gratis", 25.0 to "Envío $25", 35.0 to "Envío $35", 45.0 to "Envío $45").forEach { (fee, label) ->
                                FilterChip(
                                    selected = selectedFee == fee,
                                    onClick = { selectedFee = fee },
                                    label = { Text(label, fontSize = 11.sp) }
                                )
                            }
                        }
                    }
                }

                validationError?.let { err ->
                    Text(
                        text = err,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when (selectedType) {
                        OrderType.DINE_IN -> {
                            val table = selectedTable
                            if (table == null) {
                                validationError = "Selecciona una mesa"
                            } else {
                                onStartDineIn(table, dinersCount)
                            }
                        }
                        OrderType.PICK_UP -> {
                            if (customerName.isBlank()) {
                                validationError = "Ingresa el nombre del cliente que retirará el pedido"
                            } else {
                                onStartPickUp(customerName, customerPhone, channelNotes)
                            }
                        }
                        OrderType.DELIVERY -> {
                            if (customerName.isBlank() || deliveryAddress.isBlank()) {
                                validationError = "Ingresa nombre del cliente y dirección de entrega"
                            } else {
                                onStartDelivery(
                                    customerName,
                                    customerPhone,
                                    deliveryAddress,
                                    selectedZone,
                                    selectedDriver,
                                    selectedFee,
                                    channelNotes
                                )
                            }
                        }
                    }
                },
                modifier = Modifier.testTag("confirm_channel_order_btn")
            ) {
                Text("Elegir Productos")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun RoleResponsibilityAdvisorDialog(
    currentPolicy: String,
    onSelectPolicy: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var showSqlTab by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Badge,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
        },
        title = {
            Text(
                text = "¿Mesero o Persona Designada para Pick-Up y Delivery?",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = !showSqlTab,
                        onClick = { showSqlTab = false },
                        label = { Text("Modelos Operativos", fontSize = 12.sp) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = showSqlTab,
                        onClick = { showSqlTab = true },
                        leadingIcon = { Icon(Icons.Default.Storage, contentDescription = null, modifier = Modifier.size(14.dp)) },
                        label = { Text("Schema PostgreSQL", fontSize = 12.sp) },
                        modifier = Modifier.weight(1f)
                    )
                }

                if (!showSqlTab) {
                    Text(
                        text = "En restaurantes reales existen 3 esquemas. Selecciona cuál aplicar en tu POS:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    val policies = listOf(
                        Triple(
                            "HIBRIDO",
                            "1. Modelo Híbrido Flexible (Recomendado)",
                            "• Turnos tranquilos: El mesero o cajero crea Pick-Up/Delivery desde su misma vista.\n" +
                                    "• Horas pico: Una persona en Mostrador/Despacho (ej. Sofía PIN 4444) supervisa el Mapa de Seguimiento, empaca y entrega a clientes/repartidores.\n" +
                                    "• Ventaja: No necesitas cambiar tu código si el restaurante crece."
                        ),
                        Triple(
                            "DESPACHADOR",
                            "2. Mostrador / Despachador Dedicado",
                            "• Ideal cuando tienes +15 pedidos diarios para llevar o apps de reparto.\n" +
                                    "• ¿Por qué no el mesero? En Pick-Up y Delivery casi nunca dejan propina (0%), por lo que atender llamadas y repartidores distrae al mesero de sus mesas con propina del 5%-10%."
                        ),
                        Triple(
                            "MESERO",
                            "3. Todo por el Mesero (Locales Pequeños)",
                            "• Útil en cafeterías o restaurantes pequeños con 1-2 empleados por turno.\n" +
                                    "• El mesero toma la orden, vigila el contador de pendientes y cobra en caja."
                        )
                    )

                    policies.forEach { (code, title, desc) ->
                        val isSelected = currentPolicy == code
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            ),
                            border = if (isSelected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectPolicy(code) }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = desc,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    Text(
                        text = "Para tu Web App (React + Kotlin + PostgreSQL), separa quién CREÓ el pedido de quién lo DESPACHA así:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Surface(
                        color = Color(0xFF1E293B),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = """
ALTER TABLE orders
  ADD COLUMN order_type VARCHAR(20) NOT NULL DEFAULT 'DINE_IN',
  -- 'DINE_IN' | 'PICK_UP' | 'DELIVERY'
  ADD COLUMN table_id INT NULL, -- NULL en PickUp/Delivery
  ADD COLUMN fulfillment_stage VARCHAR(30) DEFAULT 'EN_COCINA',
  -- 'EN_COCINA' | 'LISTO_MOSTRADOR' | 'LISTO_DESPACHO' | 'EN_RUTA' | 'ENTREGADO'
  ADD COLUMN customer_name VARCHAR(120),
  ADD COLUMN customer_phone VARCHAR(30),
  ADD COLUMN delivery_address TEXT,
  ADD COLUMN delivery_zone VARCHAR(40),
  ADD COLUMN delivery_fee NUMERIC(10,2) DEFAULT 0,
  ADD COLUMN created_by_user_id UUID REFERENCES users(id),
  ADD COLUMN dispatcher_user_id UUID NULL REFERENCES users(id),
  ADD COLUMN driver_name VARCHAR(80);
                            """.trimIndent(),
                            color = Color(0xFFE2E8F0),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                    Text(
                        text = "Tip clave: Hacer table_id NULLABLE en PostgreSQL evita tener que inventar mesas falsas para Pick-Up y Delivery.",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Listo")
            }
        }
    )
}
