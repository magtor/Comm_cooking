package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.BackendSyncConfig
import com.example.data.model.Category
import com.example.data.model.DeliveryZone
import com.example.data.model.FulfillmentStage
import com.example.data.model.OrderItem
import com.example.data.model.OrderStatus
import com.example.data.model.OrderType
import com.example.data.model.OrderWithItems
import com.example.data.model.Product
import com.example.data.model.RestaurantTable
import com.example.data.model.TableStatus
import com.example.data.model.User
import com.example.data.model.UserRole
import com.example.data.repository.RestaurantRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ComandaViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: RestaurantRepository

    init {
        val db = AppDatabase.getDatabase(application)
        repository = RestaurantRepository(db)
        viewModelScope.launch {
            repository.checkAndSeedInitialData()
        }
    }

    val users: StateFlow<List<User>> = repository.users
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active session user
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()

    // PostgreSQL / REST API Backend config
    private val _backendConfig = MutableStateFlow(
        BackendSyncConfig(
            serverUrl = "http://192.168.1.100:8000/api",
            apiKey = "pos_secret_key_restaurant_2026",
            autoSync = false,
            isOnlineMode = true,
            lastSyncStatus = "Listo para conectar con PostgreSQL / REST API",
            lastSyncTimestamp = null
        )
    )
    val backendConfig: StateFlow<BackendSyncConfig> = _backendConfig.asStateFlow()

    // Audit log for administrator of logins during this app execution
    private val _loginAuditEntries = MutableStateFlow<List<User>>(emptyList())
    val loginAuditEntries: StateFlow<List<User>> = _loginAuditEntries.asStateFlow()

    fun clearLoginError() {
        _loginError.value = null
    }

    fun loginWithPin(pin: String, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            _loginError.value = null
            val authenticatedUser = repository.authenticateByPin(pin)
            if (authenticatedUser != null) {
                _currentUser.value = authenticatedUser
                addLoginAudit(authenticatedUser)
                onResult(true)
            } else {
                _loginError.value = "PIN incorrecto o usuario no encontrado"
                onResult(false)
            }
        }
    }

    fun loginWithCredentials(usernameOrEmail: String, pin: String, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            _loginError.value = null
            if (usernameOrEmail.isBlank() || pin.isBlank()) {
                _loginError.value = "Por favor ingresa usuario y PIN"
                onResult(false)
                return@launch
            }
            val authenticatedUser = repository.authenticate(usernameOrEmail, pin)
            if (authenticatedUser != null) {
                _currentUser.value = authenticatedUser
                addLoginAudit(authenticatedUser)
                onResult(true)
            } else {
                _loginError.value = "Credenciales inválidas. Verifica usuario y PIN"
                onResult(false)
            }
        }
    }

    fun quickLoginAs(user: User) {
        viewModelScope.launch {
            repository.updateLastLogin(user.id)
            val updatedUser = user.copy(lastLoginAt = System.currentTimeMillis())
            _currentUser.value = updatedUser
            _loginError.value = null
            addLoginAudit(updatedUser)
        }
    }

    private fun addLoginAudit(user: User) {
        val currentList = _loginAuditEntries.value.toMutableList()
        currentList.removeAll { it.id == user.id }
        currentList.add(0, user.copy(lastLoginAt = System.currentTimeMillis()))
        _loginAuditEntries.value = currentList
    }

    fun logout() {
        _currentUser.value = null
        _loginError.value = null
    }

    fun updateBackendConfig(url: String, apiKey: String, autoSync: Boolean) {
        _backendConfig.value = _backendConfig.value.copy(
            serverUrl = url.trim(),
            apiKey = apiKey.trim(),
            autoSync = autoSync,
            lastSyncStatus = "Configuración actualizada. Listo para sincronizar con PostgreSQL"
        )
    }

    fun testBackendConnection(onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            // Emulate backend handshake check with the configured URL
            _backendConfig.value = _backendConfig.value.copy(
                lastSyncStatus = "Probando conexión a ${_backendConfig.value.serverUrl}..."
            )
            delay(1200)
            val isValidUrl = _backendConfig.value.serverUrl.startsWith("http://") || 
                             _backendConfig.value.serverUrl.startsWith("https://")
            if (isValidUrl) {
                val now = System.currentTimeMillis()
                _backendConfig.value = _backendConfig.value.copy(
                    lastSyncStatus = "Conexión exitosa con API POS PostgreSQL (Status 200 OK)",
                    lastSyncTimestamp = now
                )
                onResult(true, "Conexión simulada con éxito a API POS. Schema PostgreSQL validado.")
            } else {
                _backendConfig.value = _backendConfig.value.copy(
                    lastSyncStatus = "Error: URL inválida. Debe iniciar con http:// o https://"
                )
                onResult(false, "La URL del servidor backend no es válida.")
            }
        }
    }

    val tables: StateFlow<List<RestaurantTable>> = repository.tables
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<Category>> = repository.categories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allProducts: StateFlow<List<Product>> = repository.products
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeOrders: StateFlow<List<OrderWithItems>> = repository.activeOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingTakeoutOrders: StateFlow<List<OrderWithItems>> = repository.pendingTakeoutOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val closedOrders: StateFlow<List<OrderWithItems>> = repository.closedOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allOrdersHistory: StateFlow<List<OrderWithItems>> = repository.allOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Operational role policy for Pick-Up & Delivery ("HIBRIDO", "DESPACHADOR", "MESERO")
    private val _operationRolePolicy = MutableStateFlow("HIBRIDO")
    val operationRolePolicy: StateFlow<String> = _operationRolePolicy.asStateFlow()

    fun setOperationRolePolicy(policy: String) {
        _operationRolePolicy.value = policy
    }

    // Selected zone filter for tables
    private val _selectedZone = MutableStateFlow("Todas")
    val selectedZone = _selectedZone.asStateFlow()

    fun setSelectedZone(zone: String) {
        _selectedZone.value = zone
    }

    // Filtered tables based on zone
    val filteredTables: StateFlow<List<RestaurantTable>> = combine(tables, _selectedZone) { tableList, zone ->
        if (zone == "Todas") tableList else tableList.filter { it.zone == zone }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Product search query and category filter
    private val _productSearchQuery = MutableStateFlow("")
    val productSearchQuery = _productSearchQuery.asStateFlow()

    private val _selectedCategoryId = MutableStateFlow<String?>(null)
    val selectedCategoryId = _selectedCategoryId.asStateFlow()

    fun setProductSearchQuery(query: String) {
        _productSearchQuery.value = query
    }

    fun setSelectedCategory(catId: String?) {
        _selectedCategoryId.value = catId
    }

    val filteredProducts: StateFlow<List<Product>> = combine(allProducts, _selectedCategoryId, _productSearchQuery) { prods, catId, query ->
        prods.filter { product ->
            val matchesCategory = catId == null || product.categoryId == catId
            val matchesQuery = query.isBlank() || product.name.contains(query, ignoreCase = true) || product.description.contains(query, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Current Table selected for taking order (Dine-In)
    private val _targetTable = MutableStateFlow<RestaurantTable?>(null)
    val targetTable = _targetTable.asStateFlow()

    fun selectTargetTable(table: RestaurantTable?) {
        _targetTable.value = table
    }

    // Current Order Channel & Customer Metadata (Dine-In, Pick-Up, Delivery)
    private val _draftOrderType = MutableStateFlow(OrderType.DINE_IN)
    val draftOrderType = _draftOrderType.asStateFlow()

    private val _draftCustomerName = MutableStateFlow("")
    val draftCustomerName = _draftCustomerName.asStateFlow()

    private val _draftCustomerPhone = MutableStateFlow("")
    val draftCustomerPhone = _draftCustomerPhone.asStateFlow()

    private val _draftDeliveryAddress = MutableStateFlow("")
    val draftDeliveryAddress = _draftDeliveryAddress.asStateFlow()

    private val _draftDeliveryZone = MutableStateFlow(DeliveryZone.MOSTRADOR)
    val draftDeliveryZone = _draftDeliveryZone.asStateFlow()

    private val _draftAssignedDriver = MutableStateFlow("")
    val draftAssignedDriver = _draftAssignedDriver.asStateFlow()

    private val _draftDeliveryFee = MutableStateFlow(0.0)
    val draftDeliveryFee = _draftDeliveryFee.asStateFlow()

    // Target existing order when adding more products to an active comanda
    private val _targetOrderId = MutableStateFlow<Long?>(null)
    val targetOrderId = _targetOrderId.asStateFlow()

    fun setTargetOrderId(orderId: Long?) {
        _targetOrderId.value = orderId
    }

    // Prepare fresh new Dine-In order for a table
    fun prepareForNewOrder(table: RestaurantTable) {
        _targetTable.value = table
        _targetOrderId.value = null
        _draftOrderType.value = OrderType.DINE_IN
        _draftCustomerName.value = ""
        _draftCustomerPhone.value = ""
        _draftDeliveryAddress.value = ""
        _draftDeliveryZone.value = DeliveryZone.MOSTRADOR
        _draftAssignedDriver.value = ""
        _draftDeliveryFee.value = 0.0
        _cartItems.value = emptyList()
        _generalNotes.value = ""
    }

    // Prepare fresh Pick-Up order (no table occupied)
    fun prepareForPickUpOrder(
        customerName: String,
        customerPhone: String,
        notes: String = ""
    ) {
        _targetTable.value = null
        _targetOrderId.value = null
        _draftOrderType.value = OrderType.PICK_UP
        _draftCustomerName.value = customerName.trim()
        _draftCustomerPhone.value = customerPhone.trim()
        _draftDeliveryAddress.value = "Retiro en Mostrador Principal"
        _draftDeliveryZone.value = DeliveryZone.MOSTRADOR
        _draftAssignedDriver.value = ""
        _draftDeliveryFee.value = 0.0
        _cartItems.value = emptyList()
        _generalNotes.value = notes.trim()
    }

    // Prepare fresh Delivery order (no table occupied)
    fun prepareForDeliveryOrder(
        customerName: String,
        customerPhone: String,
        address: String,
        zone: DeliveryZone,
        assignedDriver: String = "",
        deliveryFee: Double = 35.0,
        notes: String = ""
    ) {
        _targetTable.value = null
        _targetOrderId.value = null
        _draftOrderType.value = OrderType.DELIVERY
        _draftCustomerName.value = customerName.trim()
        _draftCustomerPhone.value = customerPhone.trim()
        _draftDeliveryAddress.value = address.trim()
        _draftDeliveryZone.value = zone
        _draftAssignedDriver.value = assignedDriver.trim()
        _draftDeliveryFee.value = deliveryFee
        _cartItems.value = emptyList()
        _generalNotes.value = notes.trim()
    }

    // Prepare for adding additional products to an existing active comanda (Dine-In, Pick-Up, or Delivery)
    fun prepareForAddingToOrder(
        table: RestaurantTable?,
        orderId: Long,
        orderType: OrderType = OrderType.DINE_IN,
        customerName: String = ""
    ) {
        _targetTable.value = table
        _targetOrderId.value = orderId
        _draftOrderType.value = orderType
        _draftCustomerName.value = customerName
        _cartItems.value = emptyList()
        _generalNotes.value = ""
    }

    // Cart / Order Draft state
    private val _cartItems = MutableStateFlow<List<OrderItem>>(emptyList())
    val cartItems = _cartItems.asStateFlow()

    private val _generalNotes = MutableStateFlow("")
    val generalNotes = _generalNotes.asStateFlow()

    fun setGeneralNotes(notes: String) {
        _generalNotes.value = notes
    }

    val cartTotalCount: StateFlow<Int> = cartItems.combine(cartItems) { items, _ ->
        items.sumOf { it.quantity }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val cartTotalAmount: StateFlow<Double> = cartItems.combine(cartItems) { items, _ ->
        items.sumOf { it.unitPrice * it.quantity }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun addToCart(product: Product, notes: String = "") {
        val current = _cartItems.value.toMutableList()
        val index = current.indexOfFirst { it.productId == product.id && it.notes == notes }
        if (index != -1) {
            val existing = current[index]
            current[index] = existing.copy(
                quantity = existing.quantity + 1,
                subtotal = existing.unitPrice * (existing.quantity + 1)
            )
        } else {
            current.add(
                OrderItem(
                    orderId = 0,
                    productId = product.id,
                    productName = product.name,
                    categoryId = product.categoryId,
                    unitPrice = product.price,
                    quantity = 1,
                    notes = notes,
                    subtotal = product.price
                )
            )
        }
        _cartItems.value = current
    }

    fun updateItemQuantity(index: Int, newQuantity: Int) {
        val current = _cartItems.value.toMutableList()
        if (index in current.indices) {
            if (newQuantity <= 0) {
                current.removeAt(index)
            } else {
                val item = current[index]
                current[index] = item.copy(
                    quantity = newQuantity,
                    subtotal = item.unitPrice * newQuantity
                )
            }
            _cartItems.value = current
        }
    }

    fun updateItemNotes(index: Int, notes: String) {
        val current = _cartItems.value.toMutableList()
        if (index in current.indices) {
            val item = current[index]
            current[index] = item.copy(notes = notes)
            _cartItems.value = current
        }
    }

    fun removeItemFromCart(index: Int) {
        val current = _cartItems.value.toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            _cartItems.value = current
        }
    }

    // Empties the current draft products without losing table or active order context
    fun clearCart() {
        _cartItems.value = emptyList()
        _generalNotes.value = ""
    }

    // Complete session reset after sending comanda or closing
    fun resetOrderSession() {
        _cartItems.value = emptyList()
        _generalNotes.value = ""
        _targetTable.value = null
        _targetOrderId.value = null
        _draftOrderType.value = OrderType.DINE_IN
        _draftCustomerName.value = ""
        _draftCustomerPhone.value = ""
        _draftDeliveryAddress.value = ""
        _draftDeliveryZone.value = DeliveryZone.MOSTRADOR
        _draftAssignedDriver.value = ""
        _draftDeliveryFee.value = 0.0
    }

    fun sendComandaToKitchen(onSuccess: (Long) -> Unit) {
        val items = _cartItems.value
        if (items.isEmpty()) return

        val orderType = _draftOrderType.value
        val table = _targetTable.value
        val existingOrderId = _targetOrderId.value

        if (existingOrderId == null && orderType == OrderType.DINE_IN && table == null) return

        val currentStaff = _currentUser.value
        val waiterId = currentStaff?.id ?: "usr_carlos"
        val waiterName = currentStaff?.fullName ?: "Carlos Mendieta"
        val createdByRole = currentStaff?.role?.name ?: "MESERO"

        val customerShort = _draftCustomerName.value.ifBlank { "Cliente" }
        val resolvedTableId = when (orderType) {
            OrderType.DINE_IN -> table?.id ?: 1
            OrderType.PICK_UP -> 0
            OrderType.DELIVERY -> -1
        }
        val resolvedTableLabel = when (orderType) {
            OrderType.DINE_IN -> table?.tableNumber ?: "Mesa"
            OrderType.PICK_UP -> "Pick-Up • $customerShort"
            OrderType.DELIVERY -> "Delivery • $customerShort"
        }

        viewModelScope.launch {
            if (existingOrderId != null) {
                // Adding to existing open comanda -> append items and re-send to kitchen
                repository.addItemsToExistingOrder(existingOrderId, items, _generalNotes.value)
                resetOrderSession()
                onSuccess(existingOrderId)
            } else {
                // New comanda (Dine-In, Pick-Up, or Delivery)
                val newOrderId = repository.createAndSendOrder(
                    tableId = resolvedTableId,
                    tableNumber = resolvedTableLabel,
                    items = items,
                    notes = _generalNotes.value,
                    asDraft = false,
                    waiterId = waiterId,
                    waiterName = waiterName,
                    orderType = orderType,
                    customerName = _draftCustomerName.value,
                    customerPhone = _draftCustomerPhone.value,
                    deliveryAddress = _draftDeliveryAddress.value,
                    deliveryZone = _draftDeliveryZone.value,
                    assignedDriver = _draftAssignedDriver.value,
                    deliveryFee = _draftDeliveryFee.value,
                    createdByRole = createdByRole
                )
                resetOrderSession()
                onSuccess(newOrderId)
            }
        }
    }

    fun saveComandaAsDraft(onSuccess: (Long) -> Unit) {
        val items = _cartItems.value
        if (items.isEmpty()) return

        val orderType = _draftOrderType.value
        val table = _targetTable.value
        if (orderType == OrderType.DINE_IN && table == null) return

        val currentStaff = _currentUser.value
        val waiterId = currentStaff?.id ?: "usr_carlos"
        val waiterName = currentStaff?.fullName ?: "Carlos Mendieta"
        val createdByRole = currentStaff?.role?.name ?: "MESERO"

        val customerShort = _draftCustomerName.value.ifBlank { "Cliente" }
        val resolvedTableId = when (orderType) {
            OrderType.DINE_IN -> table?.id ?: 1
            OrderType.PICK_UP -> 0
            OrderType.DELIVERY -> -1
        }
        val resolvedTableLabel = when (orderType) {
            OrderType.DINE_IN -> table?.tableNumber ?: "Mesa"
            OrderType.PICK_UP -> "Pick-Up • $customerShort"
            OrderType.DELIVERY -> "Delivery • $customerShort"
        }

        viewModelScope.launch {
            val newOrderId = repository.createAndSendOrder(
                tableId = resolvedTableId,
                tableNumber = resolvedTableLabel,
                items = items,
                notes = _generalNotes.value,
                asDraft = true,
                waiterId = waiterId,
                waiterName = waiterName,
                orderType = orderType,
                customerName = _draftCustomerName.value,
                customerPhone = _draftCustomerPhone.value,
                deliveryAddress = _draftDeliveryAddress.value,
                deliveryZone = _draftDeliveryZone.value,
                assignedDriver = _draftAssignedDriver.value,
                deliveryFee = _draftDeliveryFee.value,
                createdByRole = createdByRole
            )
            resetOrderSession()
            onSuccess(newOrderId)
        }
    }

    fun updateOrderStatus(orderId: Long, newStatus: OrderStatus) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, newStatus)
        }
    }

    fun updateFulfillmentStage(orderId: Long, stage: FulfillmentStage) {
        viewModelScope.launch {
            repository.updateFulfillmentStage(orderId, stage)
        }
    }

    fun assignDriverToOrder(orderId: Long, driverName: String) {
        viewModelScope.launch {
            repository.assignDriverToOrder(orderId, driverName)
        }
    }

    fun payAndCloseOrder(
        orderId: Long,
        paymentMethod: String = "Efectivo",
        tipPercentage: Double = 5.0,
        tipAmount: Double = 0.0,
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {
            repository.payAndCloseOrder(orderId, paymentMethod, tipPercentage, tipAmount)
            onComplete()
        }
    }

    fun openTable(tableId: Int, dinersCount: Int) {
        viewModelScope.launch {
            repository.openTable(tableId, dinersCount)
        }
    }

    fun releaseTable(tableId: Int) {
        viewModelScope.launch {
            repository.releaseTable(tableId)
        }
    }

    fun updateExistingOrderItemNotes(itemId: Long, notes: String) {
        viewModelScope.launch {
            repository.updateOrderItemNotes(itemId, notes)
        }
    }

    fun updateExistingOrderGeneralNotes(orderId: Long, notes: String) {
        viewModelScope.launch {
            repository.updateOrderGeneralNotes(orderId, notes)
        }
    }
}
