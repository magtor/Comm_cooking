package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Embedded
import androidx.room.Relation

enum class TableStatus(val label: String) {
    LIBRE("Libre"),
    OCUPADA("Ocupada"),
    EN_COCINA("En Cocina"),
    LISTO("Listo p/ Servir"),
    SERVIDO("Servido"),
    CUENTA("Cuenta Pedida")
}

enum class OrderStatus(val label: String) {
    DRAFT("Borrador"),
    ENVIADA_COCINA("En Cocina"),
    LISTA("Listo para Servir"),
    SERVIDA("Servida"),
    CUENTA("Cuenta Solicitada"),
    PAGADA("Pagada")
}

enum class OrderType(val label: String, val shortLabel: String) {
    DINE_IN("Comer en Salón (Dine-In)", "Dine-In"),
    PICK_UP("Para Llevar (Pick-Up)", "Pick-Up"),
    DELIVERY("A Domicilio (Delivery)", "Delivery")
}

enum class FulfillmentStage(val label: String) {
    EN_COCINA("En Cocina"),
    LISTO_MOSTRADOR("Listo en Mostrador"),
    LISTO_DESPACHO("Listo p/ Despacho"),
    EN_RUTA("En Ruta / Camino"),
    POR_COBRAR("Por Cobrar"),
    ENTREGADO("Entregado")
}

enum class DeliveryZone(
    val label: String,
    val mapX: Float,
    val mapY: Float,
    val estimatedMinutes: Int
) {
    MOSTRADOR("Mostrador Local", 0.50f, 0.50f, 0),
    ZONA_NORTE("Zona Norte", 0.46f, 0.19f, 15),
    ZONA_CENTRO("Zona Centro", 0.68f, 0.36f, 10),
    ZONA_SUR("Zona Sur", 0.54f, 0.82f, 20),
    ZONA_PONIENTE("Zona Poniente", 0.20f, 0.45f, 16),
    ZONA_ORIENTE("Zona Oriente", 0.82f, 0.66f, 18)
}

enum class UserRole(val label: String, val badgeColor: Long) {
    ADMIN("Administrador", 0xFFD32F2F),
    MESERO("Mesero / Sala", 0xFF1976D2),
    DESPACHADOR("Mostrador / Delivery", 0xFF7B1FA2),
    CAJERO("Caja / Cobro", 0xFF388E3C)
}

@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: String,
    val username: String,
    val fullName: String,
    val role: UserRole = UserRole.MESERO,
    val pin: String = "1234",
    val email: String = "",
    val isActive: Boolean = true,
    val lastLoginAt: Long? = null
)

@Entity(tableName = "restaurant_tables")
data class RestaurantTable(
    @PrimaryKey val id: Int,
    val tableNumber: String,
    val capacity: Int = 4,
    val status: TableStatus = TableStatus.LIBRE,
    val zone: String = "Salón Principal",
    val activeOrderId: Long? = null,
    val activeDinersCount: Int = 0,
    val waiterName: String = "Carlos M."
)

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey val id: String,
    val name: String,
    val icon: String,
    val displayOrder: Int = 0
)

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val categoryId: String,
    val name: String,
    val description: String,
    val price: Double,
    val isAvailable: Boolean = true,
    val preparationTimeMinutes: Int = 15,
    val popular: Boolean = false
)

@Entity(tableName = "orders")
data class Order(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tableId: Int,
    val tableNumber: String,
    val orderType: OrderType = OrderType.DINE_IN,
    val fulfillmentStage: FulfillmentStage = FulfillmentStage.EN_COCINA,
    val customerName: String = "",
    val customerPhone: String = "",
    val deliveryAddress: String = "",
    val deliveryZone: DeliveryZone = DeliveryZone.MOSTRADOR,
    val assignedDriver: String = "",
    val deliveryFee: Double = 0.0,
    val createdByRole: String = "MESERO",
    val waiterId: String = "",
    val waiterName: String = "Carlos M.",
    val status: OrderStatus = OrderStatus.DRAFT,
    val createdAt: Long = System.currentTimeMillis(),
    val sentToKitchenAt: Long? = null,
    val paidAt: Long? = null,
    val generalNotes: String = "",
    val totalAmount: Double = 0.0,
    val tipPercentage: Double = 5.0,
    val tipAmount: Double = 0.0,
    val paymentMethod: String = "Efectivo"
) {
    val effectiveTip: Double
        get() = if (tipAmount > 0.0) tipAmount else (totalAmount * (tipPercentage / 100.0))

    val grandTotal: Double
        get() = totalAmount + deliveryFee + effectiveTip
}

@Entity(tableName = "order_items")
data class OrderItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orderId: Long,
    val productId: Long,
    val productName: String,
    val categoryId: String = "",
    val unitPrice: Double,
    var quantity: Int,
    val notes: String = "",
    val subtotal: Double = unitPrice * quantity
)

data class OrderWithItems(
    @Embedded val order: Order,
    @Relation(
        parentColumn = "id",
        entityColumn = "orderId"
    )
    val items: List<OrderItem>
)

data class BackendSyncConfig(
    val serverUrl: String = "http://192.168.1.100:8000/api",
    val apiKey: String = "pos_secret_token",
    val autoSync: Boolean = false,
    val isOnlineMode: Boolean = true,
    val lastSyncStatus: String = "Listo para conectar con PostgreSQL / REST API",
    val lastSyncTimestamp: Long? = null
)

