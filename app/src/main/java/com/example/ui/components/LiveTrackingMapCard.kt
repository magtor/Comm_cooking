package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FulfillmentStage
import com.example.data.model.OrderType
import com.example.data.model.OrderWithItems
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusOrange
import com.example.ui.theme.StatusPurple
import java.util.Locale

@Composable
fun LiveTrackingMapCard(
    pendingPickUps: List<OrderWithItems>,
    pendingDeliveries: List<OrderWithItems>,
    selectedOrderId: Long?,
    onSelectOrder: (Long?) -> Unit,
    onSelectCounterHub: () -> Unit,
    onAdvanceStage: (OrderWithItems) -> Unit,
    onOpenDetail: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val allPending = pendingPickUps + pendingDeliveries
    val selectedOrder = allPending.firstOrNull { it.order.id == selectedOrderId }

    val readyPickUpsCount = pendingPickUps.count { it.order.fulfillmentStage == FulfillmentStage.LISTO_MOSTRADOR }
    val enRouteCount = pendingDeliveries.count { it.order.fulfillmentStage == FulfillmentStage.EN_RUTA }

    // Precompute deterministic non-overlapping map coordinates for each delivery order
    val deliveryPinPositions = pendingDeliveries.mapIndexed { idx, owi ->
        val baseZone = owi.order.deliveryZone
        val offsetStep = (idx % 3 - 1) * 0.06f
        val xRatio = (baseZone.mapX + offsetStep).coerceIn(0.12f, 0.88f)
        val yRatio = (baseZone.mapY + ((idx % 2) * 0.05f - 0.025f)).coerceIn(0.14f, 0.86f)
        Triple(owi, xRatio, yRatio)
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("live_tracking_map_card")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Map Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Radar de Entregas & Mostrador",
                        color = Color.White,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Toca un pin o el mostrador central para gestionar",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        color = Color(0xFF7B1FA2).copy(alpha = 0.28f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFFCE93D8).copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "🛍️ ${pendingPickUps.size} Pick-Up",
                            color = Color(0xFFF3E5F5),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Surface(
                        color = Color(0xFF00838F).copy(alpha = 0.30f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFF80DEEA).copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "🛵 ${pendingDeliveries.size} Delivery",
                            color = Color(0xFFE0F7FA),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Interactive Urban Map Canvas + Overlay Pins
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .padding(horizontal = 12.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Color(0xFF1E293B), Color(0xFF0B1120))
                        )
                    )
            ) {
                val boxWidth = maxWidth
                val boxHeight = maxHeight

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val center = Offset(w * 0.5f, h * 0.5f)

                    // Subtle street grid
                    val gridColor = Color(0xFF334155).copy(alpha = 0.35f)
                    for (i in 1..7) {
                        val gx = w * (i / 8f)
                        drawLine(gridColor, Offset(gx, 0f), Offset(gx, h), strokeWidth = 1f)
                    }
                    for (j in 1..5) {
                        val gy = h * (j / 6f)
                        drawLine(gridColor, Offset(0f, gy), Offset(w, gy), strokeWidth = 1f)
                    }

                    // Diagonal main avenues
                    val avenueColor = Color(0xFF475569).copy(alpha = 0.45f)
                    drawLine(avenueColor, Offset(0f, h * 0.2f), Offset(w, h * 0.8f), strokeWidth = 2.5f)
                    drawLine(avenueColor, Offset(w * 0.15f, h), Offset(w * 0.85f, 0f), strokeWidth = 2.5f)

                    // Radar distance rings (1.5km, 3km, 5km)
                    val ringStroke = Stroke(
                        width = 1.2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                    )
                    drawCircle(
                        color = Color(0xFF38BDF8).copy(alpha = 0.22f),
                        radius = minOf(w, h) * 0.20f,
                        center = center,
                        style = ringStroke
                    )
                    drawCircle(
                        color = Color(0xFF38BDF8).copy(alpha = 0.16f),
                        radius = minOf(w, h) * 0.36f,
                        center = center,
                        style = ringStroke
                    )
                    drawCircle(
                        color = Color(0xFF38BDF8).copy(alpha = 0.10f),
                        radius = minOf(w, h) * 0.48f,
                        center = center,
                        style = ringStroke
                    )

                    // Dashed route trajectories from Restaurant Hub to each active Delivery
                    deliveryPinPositions.forEach { (owi, rx, ry) ->
                        val target = Offset(w * rx, h * ry)
                        val isSelected = owi.order.id == selectedOrderId
                        val routeColor = when (owi.order.fulfillmentStage) {
                            FulfillmentStage.EN_RUTA -> Color(0xFF22D3EE)
                            FulfillmentStage.LISTO_DESPACHO -> Color(0xFFC084FC)
                            else -> Color(0xFFFB923C)
                        }
                        drawLine(
                            color = routeColor.copy(alpha = if (isSelected) 0.95f else 0.55f),
                            start = center,
                            end = target,
                            strokeWidth = if (isSelected) 4f else 2.2f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                        )
                    }
                }

                // Cardinal Zone Labels
                Text(
                    text = "ZONA NORTE",
                    color = Color(0xFF64748B),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 6.dp)
                )
                Text(
                    text = "ZONA SUR",
                    color = Color(0xFF64748B),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 6.dp)
                )
                Text(
                    text = "PONIENTE",
                    color = Color(0xFF64748B),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 8.dp)
                )
                Text(
                    text = "ORIENTE",
                    color = Color(0xFF64748B),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 8.dp)
                )

                // Central Restaurant & Pick-Up Counter Hub Marker
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .clickable {
                            if (pendingPickUps.isNotEmpty()) {
                                onSelectOrder(pendingPickUps.first().order.id)
                            }
                            onSelectCounterHub()
                        }
                        .testTag("map_center_pickup_hub"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            shape = CircleShape,
                            color = if (readyPickUpsCount > 0) Color(0xFF9C27B0) else MaterialTheme.colorScheme.primary,
                            shadowElevation = 6.dp,
                            modifier = Modifier
                                .size(50.dp)
                                .border(2.dp, Color.White, CircleShape)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Storefront,
                                    contentDescription = "Mostrador Pick-Up",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Surface(
                            color = Color(0xFF0F172A).copy(alpha = 0.9f),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, Color(0xFFCE93D8))
                        ) {
                            Text(
                                text = "Mostrador (${pendingPickUps.size} Pick-Up)",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Active Delivery Markers plotted on Map
                deliveryPinPositions.forEach { (owi, xRatio, yRatio) ->
                    val isSelected = owi.order.id == selectedOrderId
                    val pinColor = when (owi.order.fulfillmentStage) {
                        FulfillmentStage.EN_RUTA -> Color(0xFF00ACC1)
                        FulfillmentStage.LISTO_DESPACHO -> Color(0xFF8E24AA)
                        else -> StatusOrange
                    }
                    val pinX = (boxWidth * xRatio) - 24.dp
                    val pinY = (boxHeight * yRatio) - 24.dp

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .offset(x = pinX, y = pinY)
                            .clickable { onSelectOrder(owi.order.id) }
                            .testTag("map_delivery_pin_${owi.order.id}")
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = pinColor,
                            shadowElevation = if (isSelected) 8.dp else 3.dp,
                            modifier = Modifier
                                .size(if (isSelected) 42.dp else 36.dp)
                                .border(
                                    width = if (isSelected) 2.5.dp else 1.5.dp,
                                    color = if (isSelected) Color(0xFFFFEE58) else Color.White,
                                    shape = CircleShape
                                )
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (owi.order.fulfillmentStage == FulfillmentStage.EN_RUTA) {
                                        Icons.Default.DeliveryDining
                                    } else {
                                        Icons.Default.LocationOn
                                    },
                                    contentDescription = "Pedido #${owi.order.id}",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Surface(
                            color = if (isSelected) Color(0xFFFFEE58) else Color(0xFF1E293B).copy(alpha = 0.92f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "#${owi.order.id}",
                                color = if (isSelected) Color(0xFF0F172A) else Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
            }

            // Legend Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendDot(color = StatusOrange, label = "Cocina")
                LegendDot(color = StatusPurple, label = "Listo Mostrador/Bolsa ($readyPickUpsCount)")
                LegendDot(color = Color(0xFF00ACC1), label = "En Ruta ($enRouteCount)")
            }

            // Selected Pin Quick-Inspector Panel
            if (selectedOrder != null) {
                val order = selectedOrder.order
                Surface(
                    color = Color(0xFF1E293B),
                    shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
                    modifier = Modifier.fillMaxWidth()
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
                                    text = "#${order.id} • ${order.customerName.ifBlank { order.tableNumber }}",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                            FulfillmentStageBadge(stage = order.fulfillmentStage)
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = if (order.orderType == OrderType.DELIVERY) {
                                "📍 ${order.deliveryZone.label}: ${order.deliveryAddress} • Moto: ${order.assignedDriver.ifBlank { "Sin asignar" }}"
                            } else {
                                "🛍️ Retiro en Mostrador • Tel: ${order.customerPhone.ifBlank { "No registrado" }} • Creado por: ${order.waiterName.split(" ").firstOrNull()}"
                            },
                            color = Color(0xFFCBD5E1),
                            fontSize = 12.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onOpenDetail(order.id) },
                                border = BorderStroke(1.dp, Color(0xFF64748B)),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.weight(0.42f)
                            ) {
                                Text("Ver Detalle", color = Color.White, fontSize = 12.sp)
                            }

                            Button(
                                onClick = { onAdvanceStage(selectedOrder) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = nextActionColor(order.orderType, order.fulfillmentStage)
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .weight(0.58f)
                                    .testTag("map_selected_advance_btn")
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
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = label,
            color = Color(0xFFCBD5E1),
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

fun nextActionLabel(orderType: OrderType, stage: FulfillmentStage): String {
    return when (orderType) {
        OrderType.PICK_UP -> when (stage) {
            FulfillmentStage.EN_COCINA -> "Marcar Listo Mostrador"
            FulfillmentStage.LISTO_MOSTRADOR -> "Entregar y Cobrar"
            else -> "Cobrar y Cerrar"
        }
        OrderType.DELIVERY -> when (stage) {
            FulfillmentStage.EN_COCINA -> "Listo p/ Despacho"
            FulfillmentStage.LISTO_DESPACHO -> "Enviar con Repartidor"
            FulfillmentStage.EN_RUTA -> "Confirmar Entrega/Cobro"
            else -> "Cobrar y Cerrar"
        }
        OrderType.DINE_IN -> "Ver Orden"
    }
}

fun nextActionColor(orderType: OrderType, stage: FulfillmentStage): Color {
    return when (stage) {
        FulfillmentStage.EN_COCINA -> StatusPurple
        FulfillmentStage.LISTO_MOSTRADOR -> StatusGreen
        FulfillmentStage.LISTO_DESPACHO -> Color(0xFF00838F)
        FulfillmentStage.EN_RUTA -> StatusGreen
        else -> StatusGreen
    }
}
