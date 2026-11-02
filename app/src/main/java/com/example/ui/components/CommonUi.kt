package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LunchDining
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SoupKitchen
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.TableBar
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FulfillmentStage
import com.example.data.model.OrderStatus
import com.example.data.model.OrderType
import com.example.data.model.Product
import com.example.data.model.TableStatus
import com.example.ui.theme.StatusAmber
import com.example.ui.theme.StatusBlue
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusOrange
import com.example.ui.theme.StatusPurple
import java.util.Locale

@Composable
fun TableStatusBadge(status: TableStatus, modifier: Modifier = Modifier) {
    val (bgColor, textColor, icon) = when (status) {
        TableStatus.LIBRE -> Triple(Color(0xFFE8F5E9), StatusGreen, Icons.Default.CheckCircle)
        TableStatus.OCUPADA -> Triple(Color(0xFFFFF8E1), StatusAmber, Icons.Default.Restaurant)
        TableStatus.EN_COCINA -> Triple(Color(0xFFFFE0B2), StatusOrange, Icons.Default.HourglassTop)
        TableStatus.LISTO -> Triple(Color(0xFFF3E5F5), StatusPurple, Icons.Default.NotificationsActive)
        TableStatus.SERVIDO -> Triple(Color(0xFFE0F2F1), Color(0xFF00796B), Icons.Default.CheckCircle)
        TableStatus.CUENTA -> Triple(Color(0xFFE3F2FD), StatusBlue, Icons.Default.Receipt)
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = status.label,
                color = textColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun OrderStatusBadge(status: OrderStatus, modifier: Modifier = Modifier) {
    val (bgColor, textColor) = when (status) {
        OrderStatus.DRAFT -> Pair(Color(0xFFFFF8E1), StatusAmber)
        OrderStatus.ENVIADA_COCINA -> Pair(Color(0xFFFFE0B2), StatusOrange)
        OrderStatus.LISTA -> Pair(Color(0xFFF3E5F5), StatusPurple)
        OrderStatus.SERVIDA -> Pair(Color(0xFFE0F2F1), Color(0xFF00796B))
        OrderStatus.CUENTA -> Pair(Color(0xFFE3F2FD), StatusBlue)
        OrderStatus.PAGADA -> Pair(Color(0xFFEEEEEE), Color(0xFF616161))
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Text(
            text = status.label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun OrderTypeBadge(orderType: OrderType, modifier: Modifier = Modifier) {
    val (bgColor, textColor, icon) = when (orderType) {
        OrderType.DINE_IN -> Triple(Color(0xFFE3F2FD), Color(0xFF1565C0), Icons.Default.TableBar)
        OrderType.PICK_UP -> Triple(Color(0xFFF3E5F5), Color(0xFF6A1B9A), Icons.Default.ShoppingBag)
        OrderType.DELIVERY -> Triple(Color(0xFFE0F7FA), Color(0xFF00838F), Icons.Default.DeliveryDining)
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = orderType.shortLabel,
                color = textColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun FulfillmentStageBadge(stage: FulfillmentStage, modifier: Modifier = Modifier) {
    val (bgColor, textColor, icon) = when (stage) {
        FulfillmentStage.EN_COCINA -> Triple(Color(0xFFFFE0B2), StatusOrange, Icons.Default.HourglassTop)
        FulfillmentStage.LISTO_MOSTRADOR -> Triple(Color(0xFFF3E5F5), StatusPurple, Icons.Default.Storefront)
        FulfillmentStage.LISTO_DESPACHO -> Triple(Color(0xFFEDE7F6), Color(0xFF5E35B1), Icons.Default.ShoppingBag)
        FulfillmentStage.EN_RUTA -> Triple(Color(0xFFE0F7FA), Color(0xFF00838F), Icons.Default.DeliveryDining)
        FulfillmentStage.POR_COBRAR -> Triple(Color(0xFFE3F2FD), StatusBlue, Icons.Default.Receipt)
        FulfillmentStage.ENTREGADO -> Triple(Color(0xFFE8F5E9), StatusGreen, Icons.Default.CheckCircle)
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = stage.label,
                color = textColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

fun getCategoryIcon(iconKey: String): ImageVector {
    return when (iconKey) {
        "soup_kitchen" -> Icons.Default.SoupKitchen
        "restaurant" -> Icons.Default.Restaurant
        "lunch_dining" -> Icons.Default.LunchDining
        "local_cafe" -> Icons.Default.LocalCafe
        "local_bar" -> Icons.Default.LocalBar
        "cake" -> Icons.Default.Cake
        else -> Icons.Default.Restaurant
    }
}

@Composable
fun ProductItemCard(
    product: Product,
    cartQuantity: Int,
    onAddClick: () -> Unit,
    onCardClick: () -> Unit,
    onAddWithNoteClick: () -> Unit = onCardClick,
    hasNotesInCart: Boolean = false,
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
            .clickable { onCardClick() }
            .testTag("product_card_${product.id}")
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (product.popular) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "★ Popular",
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = product.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = String.format(Locale.US, "$%.2f", product.price),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "⏱ ~${product.preparationTimeMinutes} min",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.85f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .clickable { onAddWithNoteClick() }
                            .testTag("add_with_note_${product.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "+ Nota cocina (término, sin...)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }

                    if (hasNotesInCart) {
                        Surface(
                            color = Color(0xFFFFF9C4),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "📝 Con nota",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF5D4037),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // Quick add / counter button
            Box(contentAlignment = Alignment.Center) {
                if (cartQuantity > 0) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(44.dp)
                            .clickable { onAddClick() }
                            .testTag("add_more_product_${product.id}")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$cartQuantity",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "+",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                } else {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier
                            .size(44.dp)
                            .clickable { onAddClick() }
                            .testTag("add_product_${product.id}")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Agregar ${product.name}",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditItemNoteDialog(
    initialNotes: String,
    productName: String,
    onDismiss: () -> Unit,
    onSaveNotes: (String) -> Unit,
    confirmButtonText: String = "Guardar Nota",
    titleText: String = "Indicaciones para Cocina"
) {
    var notesText by remember { mutableStateOf(initialNotes) }
    var selectedCategoryFilter by remember { mutableStateOf("Todos") }

    val categories = listOf(
        "Todos",
        "🥩 Términos",
        "🚫 Sin ingredientes",
        "🥣 Salsas y Extras",
        "🥤 Bebidas",
        "⚠️ Alergias"
    )

    val quickNoteGroups = mapOf(
        "🥩 Términos y Cocción" to listOf(
            "Carne término medio",
            "Término 3/4",
            "Bien cocido",
            "Término inglés (sellado)"
        ),
        "🚫 Omitir Ingredientes (\"Sin...\")" to listOf(
            "Sin cebolla",
            "Sin pimienta",
            "Sin sal / Bajo en sal",
            "Sin cilantro",
            "Sin picante / Sin chile",
            "Sin ajo",
            "Sin mayonesa",
            "Sin aguacate",
            "Sin limón"
        ),
        "🥣 Salsas y Guarnición" to listOf(
            "Salsa aparte",
            "Aderezo aparte",
            "Para compartir al centro",
            "Extra caliente",
            "Bien dorado / Crujiente"
        ),
        "🥤 Bebidas y Bar" to listOf(
            "Sin hielo",
            "Hielo aparte",
            "Poco hielo",
            "Sin azúcar / Endulzante aparte"
        ),
        "⚠️ Alergias e Intolerancias" to listOf(
            "Sin gluten / Celíaco",
            "Sin lácteos / Intolerante",
            "Alergia a mariscos",
            "Alergia a nueces / cacahuate"
        )
    )

    fun toggleChip(chipText: String) {
        val currentNotes = notesText.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toMutableList()
        val existingIndex = currentNotes.indexOfFirst { it.equals(chipText, ignoreCase = true) }
        if (existingIndex >= 0) {
            currentNotes.removeAt(existingIndex)
        } else {
            currentNotes.add(chipText)
        }
        notesText = currentNotes.joinToString(", ")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = titleText,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = productName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Freeform text field
                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("Nota para cocina") },
                    placeholder = { Text("Ej. Término medio, sin cebolla, sin pimienta...") },
                    trailingIcon = {
                        if (notesText.isNotBlank()) {
                            IconButton(onClick = { notesText = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Limpiar notas",
                                    tint = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("item_notes_input"),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 2,
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Category pill selector
                Text(
                    text = "Opciones rápidas frecuentes:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                // Horizontal scroll of categories
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = selectedCategoryFilter == cat,
                            onClick = { selectedCategoryFilter = cat },
                            label = { Text(cat, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Display chips according to filter
                val groupsToShow = if (selectedCategoryFilter == "Todos") {
                    quickNoteGroups
                } else {
                    quickNoteGroups.filterKeys { key ->
                        when (selectedCategoryFilter) {
                            "🥩 Términos" -> key.contains("Términos")
                            "🚫 Sin ingredientes" -> key.contains("Sin")
                            "🥣 Salsas y Extras" -> key.contains("Salsas")
                            "🥤 Bebidas" -> key.contains("Bebidas")
                            "⚠️ Alergias" -> key.contains("Alergias")
                            else -> true
                        }
                    }
                }

                groupsToShow.forEach { (groupTitle, chips) ->
                    Text(
                        text = groupTitle,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 6.dp, bottom = 4.dp)
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        chips.forEach { chipText ->
                            val isSelected = notesText.split(",")
                                .map { it.trim().lowercase() }
                                .contains(chipText.trim().lowercase())

                            FilterChip(
                                selected = isSelected,
                                onClick = { toggleChip(chipText) },
                                label = { Text(text = chipText, fontSize = 11.sp) },
                                leadingIcon = if (isSelected) {
                                    {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                } else null
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSaveNotes(notesText.trim()) },
                modifier = Modifier.testTag("save_notes_btn")
            ) {
                Text(confirmButtonText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
