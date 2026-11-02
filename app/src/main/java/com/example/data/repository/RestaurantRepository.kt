package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.model.Category
import com.example.data.model.DeliveryZone
import com.example.data.model.FulfillmentStage
import com.example.data.model.Order
import com.example.data.model.OrderItem
import com.example.data.model.OrderStatus
import com.example.data.model.OrderType
import com.example.data.model.OrderWithItems
import com.example.data.model.Product
import com.example.data.model.RestaurantTable
import com.example.data.model.TableStatus
import com.example.data.model.User
import com.example.data.model.UserRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class RestaurantRepository(private val database: AppDatabase) {
    private val tableDao = database.tableDao()
    private val categoryDao = database.categoryDao()
    private val productDao = database.productDao()
    private val orderDao = database.orderDao()
    private val userDao = database.userDao()

    val tables: Flow<List<RestaurantTable>> = tableDao.getAllTables()
    val categories: Flow<List<Category>> = categoryDao.getAllCategories()
    val products: Flow<List<Product>> = productDao.getAllProducts()
    val activeOrders: Flow<List<OrderWithItems>> = orderDao.getActiveOrdersWithItems()
    val pendingTakeoutOrders: Flow<List<OrderWithItems>> = orderDao.getPendingTakeoutOrdersWithItems()
    val closedOrders: Flow<List<OrderWithItems>> = orderDao.getClosedOrdersWithItems()
    val allOrders: Flow<List<OrderWithItems>> = orderDao.getAllOrdersWithItems()
    val users: Flow<List<User>> = userDao.getAllUsers()

    fun getTableById(id: Int): Flow<RestaurantTable?> = tableDao.getTableById(id)

    fun getOrderById(orderId: Long): Flow<OrderWithItems?> = orderDao.getOrderWithItemsById(orderId)

    suspend fun authenticate(usernameOrEmail: String, pin: String): User? = withContext(Dispatchers.IO) {
        val user = userDao.authenticate(usernameOrEmail.trim(), pin.trim())
        if (user != null) {
            userDao.updateLastLogin(user.id, System.currentTimeMillis())
        }
        user
    }

    suspend fun authenticateByPin(pin: String): User? = withContext(Dispatchers.IO) {
        val user = userDao.getUserByPin(pin.trim())
        if (user != null) {
            userDao.updateLastLogin(user.id, System.currentTimeMillis())
        }
        user
    }

    suspend fun updateLastLogin(userId: String) = withContext(Dispatchers.IO) {
        userDao.updateLastLogin(userId, System.currentTimeMillis())
    }

    suspend fun insertUser(user: User) = withContext(Dispatchers.IO) {
        userDao.insertUser(user)
    }

    suspend fun checkAndSeedInitialData() = withContext(Dispatchers.IO) {
        val existingTables = tableDao.getAllTables().first()
        if (existingTables.isEmpty()) {
            seedDatabase()
        }
        val existingUsers = userDao.getAllUsers().first()
        if (existingUsers.isEmpty()) {
            seedUsers()
        }
    }

    private suspend fun seedUsers() {
        val initialUsers = listOf(
            User(
                id = "usr_carlos",
                username = "carlos",
                fullName = "Carlos Mendieta",
                role = UserRole.MESERO,
                pin = "1111",
                email = "carlos@pos.local",
                isActive = true,
                lastLoginAt = System.currentTimeMillis() - 25 * 60 * 1000
            ),
            User(
                id = "usr_laura",
                username = "laura",
                fullName = "Laura Gómez",
                role = UserRole.MESERO,
                pin = "2222",
                email = "laura@pos.local",
                isActive = true,
                lastLoginAt = System.currentTimeMillis() - 120 * 60 * 1000
            ),
            User(
                id = "usr_admin",
                username = "admin",
                fullName = "Roberto Admin (Gerente)",
                role = UserRole.ADMIN,
                pin = "1234",
                email = "admin@pos.local",
                isActive = true,
                lastLoginAt = System.currentTimeMillis() - 10 * 60 * 1000
            ),
            User(
                id = "usr_ana",
                username = "ana",
                fullName = "Ana Silva (Caja / Bar)",
                role = UserRole.CAJERO,
                pin = "3333",
                email = "ana@pos.local",
                isActive = true,
                lastLoginAt = System.currentTimeMillis() - 60 * 60 * 1000
            ),
            User(
                id = "usr_sofia",
                username = "sofia",
                fullName = "Sofía Méndez (Mostrador & Delivery)",
                role = UserRole.DESPACHADOR,
                pin = "4444",
                email = "sofia@pos.local",
                isActive = true,
                lastLoginAt = System.currentTimeMillis() - 15 * 60 * 1000
            )
        )
        userDao.insertAll(initialUsers)
    }

    private suspend fun seedDatabase() {
        val initialCategories = listOf(
            Category("entradas", "Entradas", "soup_kitchen", 1),
            Category("fuertes", "Platos Fuertes", "restaurant", 2),
            Category("tacos", "Tacos & Cortes", "lunch_dining", 3),
            Category("bebidas", "Bebidas", "local_cafe", 4),
            Category("cocteles", "Cócteles & Cervezas", "local_bar", 5),
            Category("postres", "Postres", "cake", 6)
        )
        categoryDao.insertAll(initialCategories)

        val initialProducts = listOf(
            // Entradas
            Product(1, "entradas", "Guacamole con Totopos", "Aguacate fresco, pico de gallo, limón y totopos de maíz recién fritos", 135.0, true, 8, true),
            Product(2, "entradas", "Aguachile de Camarón", "Camarones marinados en limón, pepino, cebolla morada y serrano", 215.0, true, 12, true),
            Product(3, "entradas", "Queso Fundido con Chorizo", "Mezcla de quesos Oaxaca y Manchego con chorizo artesanal y tortillas", 160.0, true, 10, false),
            Product(4, "entradas", "Nachos Especiales con Arrachera", "Frijoles refritos, queso fundido, guacamole, crema y jalapeños", 195.0, true, 12, false),
            Product(5, "entradas", "Empanadas Criollas (3 pzas)", "Rellenas de carne cortada a cuchillo con salsa chimichurri", 145.0, true, 10, false),

            // Platos Fuertes
            Product(6, "fuertes", "Hamburguesa Doble Suprema", "Doble carne Angus 200g, queso cheddar, tocino crocante y papas rústicas", 235.0, true, 18, true),
            Product(7, "fuertes", "Enchiladas Suizas Gratinadas", "Rellenas de pollo deshebrado con salsa verde cremosa y queso gratinado", 185.0, true, 15, true),
            Product(8, "fuertes", "Salmón al Limón & Finas Hierbas", "Filete de salmón noruego a la plancha con espárragos y puré de papa", 295.0, true, 20, false),
            Product(9, "fuertes", "Costillas BBQ Glaseadas", "Costillas de cerdo horneadas lentamente bañadas en salsa BBQ de la casa", 280.0, true, 22, true),
            Product(10, "fuertes", "Fettuccine Alfredo con Pollo", "Pasta fresca con salsa cremosa de parmesano, pechuga a la plancha y perejil", 210.0, true, 15, false),

            // Tacos & Cortes
            Product(11, "tacos", "Tacos de Rib Eye (Orden 3)", "Finas láminas de Rib Eye a la plancha con cebolla caramelizada y aguacate", 245.0, true, 15, true),
            Product(12, "tacos", "Tacos Gobernador (Orden 3)", "Camarón salteado con pimiento, cebolla y costra de queso gouda", 225.0, true, 14, true),
            Product(13, "tacos", "Corte Arrachera Angus 300g", "Acompañada de cebollitas cambray, nopal asado, chiles toreados y guacamole", 310.0, true, 20, true),
            Product(14, "tacos", "Bife de Chorizo 400g", "Corte argentino a la parrilla con papas a la francesa y ensalada mixta", 365.0, true, 22, false),

            // Bebidas
            Product(15, "bebidas", "Limonada Natural con Menta", "Preparada al momento con limones frescos y hierbabuena", 55.0, true, 5, true),
            Product(16, "bebidas", "Naranjada Mineral", "Jugo de naranja natural con agua gasificada", 55.0, true, 5, false),
            Product(17, "bebidas", "Refresco de Cola 355ml", "Servido frío con vaso escarchado y limón opcional", 45.0, true, 3, false),
            Product(18, "bebidas", "Agua Embotellada Manantial 600ml", "Agua purificada natural sin gas", 38.0, true, 2, false),
            Product(19, "bebidas", "Café Americano de Olla", "Café recién colado con canela y piloncillo", 48.0, true, 5, false),

            // Cócteles & Cervezas
            Product(20, "cocteles", "Cerveza Corona Extra 355ml", "Clásica cerveza clara servida bien fría con limón y sal", 65.0, true, 3, true),
            Product(21, "cocteles", "Cerveza Artesanal Minerva IPA", "Aroma cítrico y lupulado, elaborada localmente", 95.0, true, 4, false),
            Product(22, "cocteles", "Margarita Tradicional", "Tequila reposado, licor de naranja, jugo de limón fresco y sal marina", 135.0, true, 6, true),
            Product(23, "cocteles", "Mojito Clásico con Ron", "Ron añejo blanco, menta fresca machacada, jarabe y agua con gas", 140.0, true, 6, true),
            Product(24, "cocteles", "Mezcalita de Maracuyá", "Mezcal espadín artesanal con pulpa de maracuyá y chile tajín en el borde", 150.0, true, 7, true),
            Product(25, "cocteles", "Carajillo Shakerato", "Licor 43 batido enérgicamente con espresso caliente y hielo", 160.0, true, 5, true),

            // Postres
            Product(26, "postres", "Pastel Tres Leches Casero", "Bizcocho húmedo bañado en salsa de tres leches y canela", 110.0, true, 5, true),
            Product(27, "postres", "Churros Artesanales con Cajeta", "Recién hechos, crujientes, espolvoreados con azúcar y canela", 98.0, true, 8, true),
            Product(28, "postres", "Volcán de Chocolate con Helado", "Pastel suave con centro líquido de chocolate caliente y helado de vainilla", 130.0, true, 10, true),
            Product(29, "postres", "Flan de Caramelo Tradicional", "Textura suave y cremosa con caramelo dorado", 85.0, true, 5, false)
        )
        productDao.insertAll(initialProducts)

        // Seed 10 tables
        val initialTables = listOf(
            RestaurantTable(1, "Mesa 1", capacity = 4, status = TableStatus.LIBRE, zone = "Salón Principal"),
            RestaurantTable(2, "Mesa 2", capacity = 2, status = TableStatus.EN_COCINA, zone = "Salón Principal", activeDinersCount = 2),
            RestaurantTable(3, "Mesa 3", capacity = 6, status = TableStatus.OCUPADA, zone = "Salón Principal", activeDinersCount = 4),
            RestaurantTable(4, "Mesa 4", capacity = 4, status = TableStatus.LIBRE, zone = "Salón Principal"),
            RestaurantTable(5, "Mesa 5", capacity = 4, status = TableStatus.LISTO, zone = "Terraza", activeDinersCount = 3),
            RestaurantTable(6, "Mesa 6", capacity = 2, status = TableStatus.LIBRE, zone = "Terraza"),
            RestaurantTable(7, "Mesa 7", capacity = 8, status = TableStatus.CUENTA, zone = "Terraza", activeDinersCount = 6),
            RestaurantTable(8, "Barra 1", capacity = 2, status = TableStatus.SERVIDO, zone = "Barra", activeDinersCount = 1),
            RestaurantTable(9, "Barra 2", capacity = 2, status = TableStatus.LIBRE, zone = "Barra"),
            RestaurantTable(10, "Barra 3", capacity = 2, status = TableStatus.LIBRE, zone = "Barra")
        )
        tableDao.insertAll(initialTables)

        // Create sample active orders for Mesa 2 (En Cocina), Mesa 5 (Listo), Mesa 7 (Cuenta), Barra 1 (Servido)
        val now = System.currentTimeMillis()

        // Order for Mesa 2 (En Cocina hace 12 min)
        val orderMesa2 = Order(
            id = 101,
            tableId = 2,
            tableNumber = "Mesa 2",
            waiterId = "usr_carlos",
            waiterName = "Carlos Mendieta",
            status = OrderStatus.ENVIADA_COCINA,
            createdAt = now - 14 * 60 * 1000,
            sentToKitchenAt = now - 12 * 60 * 1000,
            generalNotes = "Llevar bebidas primero por favor",
            totalAmount = 530.0
        )
        orderDao.insertOrder(orderMesa2)
        orderDao.insertOrderItems(listOf(
            OrderItem(id = 201, orderId = 101, productId = 1, productName = "Guacamole con Totopos", categoryId = "entradas", unitPrice = 135.0, quantity = 1, notes = "Totopos extra crujientes"),
            OrderItem(id = 202, orderId = 101, productId = 6, productName = "Hamburguesa Doble Suprema", categoryId = "fuertes", unitPrice = 235.0, quantity = 1, notes = "Término 3/4, sin cebolla"),
            OrderItem(id = 203, orderId = 101, productId = 22, productName = "Margarita Tradicional", categoryId = "cocteles", unitPrice = 135.0, quantity = 1, notes = "Escarchado con sal")
        ))
        tableDao.updateTableStatus(2, TableStatus.EN_COCINA, 101)

        // Order for Mesa 5 (Listo para Servir) - Atendido por Laura Gómez
        val orderMesa5 = Order(
            id = 102,
            tableId = 5,
            tableNumber = "Mesa 5",
            waiterId = "usr_laura",
            waiterName = "Laura Gómez",
            status = OrderStatus.LISTA,
            createdAt = now - 25 * 60 * 1000,
            sentToKitchenAt = now - 22 * 60 * 1000,
            generalNotes = "Mesa con niños",
            totalAmount = 730.0
        )
        orderDao.insertOrder(orderMesa5)
        orderDao.insertOrderItems(listOf(
            OrderItem(id = 204, orderId = 102, productId = 11, productName = "Tacos de Rib Eye (Orden 3)", categoryId = "tacos", unitPrice = 245.0, quantity = 2, notes = "Salsas picantes aparte"),
            OrderItem(id = 205, orderId = 102, productId = 15, productName = "Limonada Natural con Menta", categoryId = "bebidas", unitPrice = 55.0, quantity = 2, notes = "Poco dulce"),
            OrderItem(id = 206, orderId = 102, productId = 27, productName = "Churros Artesanales con Cajeta", categoryId = "postres", unitPrice = 98.0, quantity = 1, notes = "")
        ))
        tableDao.updateTableStatus(5, TableStatus.LISTO, 102)

        // Order for Mesa 7 (Cuenta Solicitada)
        val orderMesa7 = Order(
            id = 103,
            tableId = 7,
            tableNumber = "Mesa 7",
            waiterId = "usr_carlos",
            waiterName = "Carlos Mendieta",
            status = OrderStatus.CUENTA,
            createdAt = now - 65 * 60 * 1000,
            sentToKitchenAt = now - 60 * 60 * 1000,
            generalNotes = "Cuentas separadas si es posible",
            totalAmount = 1435.0
        )
        orderDao.insertOrder(orderMesa7)
        orderDao.insertOrderItems(listOf(
            OrderItem(id = 207, orderId = 103, productId = 13, productName = "Corte Arrachera Angus 300g", categoryId = "tacos", unitPrice = 310.0, quantity = 2, notes = "Término medio"),
            OrderItem(id = 208, orderId = 103, productId = 9, productName = "Costillas BBQ Glaseadas", categoryId = "fuertes", unitPrice = 280.0, quantity = 1, notes = ""),
            OrderItem(id = 209, orderId = 103, productId = 24, productName = "Mezcalita de Maracuyá", categoryId = "cocteles", unitPrice = 150.0, quantity = 3, notes = ""),
            OrderItem(id = 210, orderId = 103, productId = 28, productName = "Volcán de Chocolate con Helado", categoryId = "postres", unitPrice = 130.0, quantity = 1, notes = "")
        ))
        tableDao.updateTableStatus(7, TableStatus.CUENTA, 103)

        // Order for Barra 1 (Servido)
        val orderBarra1 = Order(
            id = 104,
            tableId = 8,
            tableNumber = "Barra 1",
            waiterId = "usr_carlos",
            waiterName = "Carlos Mendieta",
            status = OrderStatus.SERVIDA,
            createdAt = now - 35 * 60 * 1000,
            sentToKitchenAt = now - 30 * 60 * 1000,
            generalNotes = "",
            totalAmount = 295.0
        )
        orderDao.insertOrder(orderBarra1)
        orderDao.insertOrderItems(listOf(
            OrderItem(id = 211, orderId = 104, productId = 25, productName = "Carajillo Shakerato", categoryId = "cocteles", unitPrice = 160.0, quantity = 1, notes = "Bien agitado"),
            OrderItem(id = 212, orderId = 104, productId = 26, productName = "Pastel Tres Leches Casero", categoryId = "postres", unitPrice = 110.0, quantity = 1, notes = "")
        ))
        tableDao.updateTableStatus(8, TableStatus.SERVIDO, 104)

        // --- Seed Pending Pick-Up & Delivery Orders for Live Tracking Map ---
        // Pick-Up #105: Mariana López - Empacado y Listo en Mostrador (creado por Mesero Carlos)
        val pickupOrder1 = Order(
            id = 105,
            tableId = 0,
            tableNumber = "Pick-Up • Mariana L.",
            orderType = OrderType.PICK_UP,
            fulfillmentStage = FulfillmentStage.LISTO_MOSTRADOR,
            customerName = "Mariana López",
            customerPhone = "555-239-8812",
            deliveryAddress = "Retiro en Mostrador Principal",
            deliveryZone = DeliveryZone.MOSTRADOR,
            createdByRole = "MESERO",
            waiterId = "usr_carlos",
            waiterName = "Carlos Mendieta",
            status = OrderStatus.LISTA,
            createdAt = now - 20 * 60 * 1000,
            sentToKitchenAt = now - 18 * 60 * 1000,
            generalNotes = "Incluir cubiertos y servilletas extra",
            totalAmount = 415.0,
            tipPercentage = 0.0
        )
        orderDao.insertOrder(pickupOrder1)
        orderDao.insertOrderItems(listOf(
            OrderItem(id = 235, orderId = 105, productId = 6, productName = "Hamburguesa Doble Suprema", categoryId = "fuertes", unitPrice = 235.0, quantity = 1, notes = "Aderezos en vasito aparte"),
            OrderItem(id = 236, orderId = 105, productId = 1, productName = "Guacamole con Totopos", categoryId = "entradas", unitPrice = 135.0, quantity = 1, notes = ""),
            OrderItem(id = 237, orderId = 105, productId = 17, productName = "Refresco de Cola 355ml", categoryId = "bebidas", unitPrice = 45.0, quantity = 1, notes = "En lata fría")
        ))

        // Pick-Up #106: Roberto Vega - En Cocina (creado por Despachadora Sofía)
        val pickupOrder2 = Order(
            id = 106,
            tableId = 0,
            tableNumber = "Pick-Up • Roberto V.",
            orderType = OrderType.PICK_UP,
            fulfillmentStage = FulfillmentStage.EN_COCINA,
            customerName = "Roberto Vega",
            customerPhone = "555-814-3029",
            deliveryAddress = "Retiro en Mostrador Principal",
            deliveryZone = DeliveryZone.MOSTRADOR,
            createdByRole = "DESPACHADOR",
            waiterId = "usr_sofia",
            waiterName = "Sofía Méndez (Mostrador & Delivery)",
            status = OrderStatus.ENVIADA_COCINA,
            createdAt = now - 8 * 60 * 1000,
            sentToKitchenAt = now - 7 * 60 * 1000,
            generalNotes = "Cliente llega en 10 min en auto gris",
            totalAmount = 470.0,
            tipPercentage = 0.0
        )
        orderDao.insertOrder(pickupOrder2)
        orderDao.insertOrderItems(listOf(
            OrderItem(id = 238, orderId = 106, productId = 12, productName = "Tacos Gobernador (Orden 3)", categoryId = "tacos", unitPrice = 225.0, quantity = 1, notes = ""),
            OrderItem(id = 239, orderId = 106, productId = 11, productName = "Tacos de Rib Eye (Orden 3)", categoryId = "tacos", unitPrice = 245.0, quantity = 1, notes = "Bien envueltos en aluminio")
        ))

        // Delivery #107: Andrea Castillo - En Ruta hacia Zona Norte con Repartidor
        val deliveryOrder1 = Order(
            id = 107,
            tableId = -1,
            tableNumber = "Delivery • Andrea C.",
            orderType = OrderType.DELIVERY,
            fulfillmentStage = FulfillmentStage.EN_RUTA,
            customerName = "Andrea Castillo",
            customerPhone = "555-402-9910",
            deliveryAddress = "Av. Reforma 240, Depto 4B (Portón negro)",
            deliveryZone = DeliveryZone.ZONA_NORTE,
            assignedDriver = "Javier R. (Moto 1)",
            deliveryFee = 35.0,
            createdByRole = "DESPACHADOR",
            waiterId = "usr_sofia",
            waiterName = "Sofía Méndez (Mostrador & Delivery)",
            status = OrderStatus.SERVIDA,
            createdAt = now - 28 * 60 * 1000,
            sentToKitchenAt = now - 26 * 60 * 1000,
            generalNotes = "Llamar al llegar a caseta de vigilancia",
            totalAmount = 575.0,
            tipPercentage = 0.0
        )
        orderDao.insertOrder(deliveryOrder1)
        orderDao.insertOrderItems(listOf(
            OrderItem(id = 240, orderId = 107, productId = 9, productName = "Costillas BBQ Glaseadas", categoryId = "fuertes", unitPrice = 280.0, quantity = 1, notes = ""),
            OrderItem(id = 241, orderId = 107, productId = 8, productName = "Salmón al Limón & Finas Hierbas", categoryId = "fuertes", unitPrice = 295.0, quantity = 1, notes = "")
        ))

        // Delivery #108: Fernando Ruiz - Listo para Despacho en Zona Centro (sin repartidor aún)
        val deliveryOrder2 = Order(
            id = 108,
            tableId = -1,
            tableNumber = "Delivery • Fernando R.",
            orderType = OrderType.DELIVERY,
            fulfillmentStage = FulfillmentStage.LISTO_DESPACHO,
            customerName = "Fernando Ruiz",
            customerPhone = "555-771-6504",
            deliveryAddress = "Calle Pino Suárez 45, Piso 2",
            deliveryZone = DeliveryZone.ZONA_CENTRO,
            assignedDriver = "",
            deliveryFee = 25.0,
            createdByRole = "MESERO",
            waiterId = "usr_laura",
            waiterName = "Laura Gómez",
            status = OrderStatus.LISTA,
            createdAt = now - 17 * 60 * 1000,
            sentToKitchenAt = now - 16 * 60 * 1000,
            generalNotes = "Pago con terminal inalámbrica",
            totalAmount = 370.0,
            tipPercentage = 0.0
        )
        orderDao.insertOrder(deliveryOrder2)
        orderDao.insertOrderItems(listOf(
            OrderItem(id = 242, orderId = 108, productId = 7, productName = "Enchiladas Suizas Gratinadas", categoryId = "fuertes", unitPrice = 185.0, quantity = 2, notes = "Salsa extra")
        ))

        // Delivery #109: Lucía Morales - Preparándose en Cocina para Zona Sur
        val deliveryOrder3 = Order(
            id = 109,
            tableId = -1,
            tableNumber = "Delivery • Lucía M.",
            orderType = OrderType.DELIVERY,
            fulfillmentStage = FulfillmentStage.EN_COCINA,
            customerName = "Lucía Morales",
            customerPhone = "555-990-1123",
            deliveryAddress = "Blvd. del Valle 890, Casa 12",
            deliveryZone = DeliveryZone.ZONA_SUR,
            assignedDriver = "Diego V. (Moto 2)",
            deliveryFee = 45.0,
            createdByRole = "DESPACHADOR",
            waiterId = "usr_sofia",
            waiterName = "Sofía Méndez (Mostrador & Delivery)",
            status = OrderStatus.ENVIADA_COCINA,
            createdAt = now - 6 * 60 * 1000,
            sentToKitchenAt = now - 5 * 60 * 1000,
            generalNotes = "Empacar postres en bolsa fría separada",
            totalAmount = 620.0,
            tipPercentage = 0.0
        )
        orderDao.insertOrder(deliveryOrder3)
        orderDao.insertOrderItems(listOf(
            OrderItem(id = 243, orderId = 109, productId = 13, productName = "Corte Arrachera Angus 300g", categoryId = "tacos", unitPrice = 310.0, quantity = 1, notes = "Término 3/4"),
            OrderItem(id = 244, orderId = 109, productId = 10, productName = "Fettuccine Alfredo con Pollo", categoryId = "fuertes", unitPrice = 210.0, quantity = 1, notes = ""),
            OrderItem(id = 245, orderId = 109, productId = 27, productName = "Churros Artesanales con Cajeta", categoryId = "postres", unitPrice = 98.0, quantity = 1, notes = "")
        ))

        // Seed Closed/Paid Orders (for tip and sales history tracking)
        // 1. Hoy (hace 2 horas) - Mesa 1 (Carlos Mendieta)
        val closedToday1 = Order(
            id = 99,
            tableId = 1,
            tableNumber = "Mesa 1",
            waiterId = "usr_carlos",
            waiterName = "Carlos Mendieta",
            status = OrderStatus.PAGADA,
            createdAt = now - 3 * 3600 * 1000,
            sentToKitchenAt = now - 160 * 60 * 1000,
            paidAt = now - 2 * 3600 * 1000,
            generalNotes = "Cliente frecuente",
            totalAmount = 880.0,
            tipPercentage = 5.0,
            tipAmount = 44.0,
            paymentMethod = "Tarjeta Débito"
        )
        orderDao.insertOrder(closedToday1)
        orderDao.insertOrderItems(listOf(
            OrderItem(id = 213, orderId = 99, productId = 13, productName = "Corte Arrachera Angus 300g", categoryId = "tacos", unitPrice = 310.0, quantity = 2, notes = "Término medio"),
            OrderItem(id = 214, orderId = 99, productId = 24, productName = "Mezcalita de Maracuyá", categoryId = "cocteles", unitPrice = 150.0, quantity = 1, notes = ""),
            OrderItem(id = 215, orderId = 99, productId = 15, productName = "Limonada Natural con Menta", categoryId = "bebidas", unitPrice = 55.0, quantity = 2, notes = "")
        ))

        // 2. Hoy (hace 4 horas) - Mesa 4 (Carlos Mendieta)
        val closedToday2 = Order(
            id = 98,
            tableId = 4,
            tableNumber = "Mesa 4",
            waiterId = "usr_carlos",
            waiterName = "Carlos Mendieta",
            status = OrderStatus.PAGADA,
            createdAt = now - 5 * 3600 * 1000,
            sentToKitchenAt = now - 280 * 60 * 1000,
            paidAt = now - 4 * 3600 * 1000,
            generalNotes = "Almuerzo ejecutivo",
            totalAmount = 1450.0,
            tipPercentage = 5.0,
            tipAmount = 72.50,
            paymentMethod = "Efectivo"
        )
        orderDao.insertOrder(closedToday2)
        orderDao.insertOrderItems(listOf(
            OrderItem(id = 216, orderId = 98, productId = 9, productName = "Costillas BBQ Glaseadas", categoryId = "fuertes", unitPrice = 280.0, quantity = 3, notes = "Salsa aparte"),
            OrderItem(id = 217, orderId = 98, productId = 6, productName = "Hamburguesa Doble Suprema", categoryId = "fuertes", unitPrice = 235.0, quantity = 2, notes = ""),
            OrderItem(id = 218, orderId = 98, productId = 16, productName = "Agua de Horchata Artesanal", categoryId = "bebidas", unitPrice = 50.0, quantity = 2, notes = ""),
            OrderItem(id = 219, orderId = 98, productId = 28, productName = "Volcán de Chocolate con Helado", categoryId = "postres", unitPrice = 130.0, quantity = 1, notes = "")
        ))

        // 3. Hoy (hace 3.5 horas) - Mesa 6 (Laura Gómez)
        val closedTodayLaura = Order(
            id = 97,
            tableId = 6,
            tableNumber = "Mesa 6",
            waiterId = "usr_laura",
            waiterName = "Laura Gómez",
            status = OrderStatus.PAGADA,
            createdAt = now - 4 * 3600 * 1000,
            sentToKitchenAt = now - 230 * 60 * 1000,
            paidAt = now - 3 * 3600 * 1000 - 15 * 60 * 1000,
            generalNotes = "",
            totalAmount = 620.0,
            tipPercentage = 5.0,
            tipAmount = 31.0,
            paymentMethod = "Tarjeta Crédito"
        )
        orderDao.insertOrder(closedTodayLaura)
        orderDao.insertOrderItems(listOf(
            OrderItem(id = 220, orderId = 97, productId = 11, productName = "Tacos de Rib Eye (Orden 3)", categoryId = "tacos", unitPrice = 245.0, quantity = 2, notes = ""),
            OrderItem(id = 221, orderId = 97, productId = 27, productName = "Churros Artesanales con Cajeta", categoryId = "postres", unitPrice = 98.0, quantity = 1, notes = ""),
            OrderItem(id = 222, orderId = 97, productId = 18, productName = "Refresco Coca-Cola 355ml", categoryId = "bebidas", unitPrice = 40.0, quantity = 1, notes = "")
        ))

        // 4. Ayer - Mesa 3 (Carlos Mendieta)
        val yesterdayMillis = now - 24 * 3600 * 1000
        val closedYesterday1 = Order(
            id = 96,
            tableId = 3,
            tableNumber = "Mesa 3",
            waiterId = "usr_carlos",
            waiterName = "Carlos Mendieta",
            status = OrderStatus.PAGADA,
            createdAt = yesterdayMillis - 2 * 3600 * 1000,
            sentToKitchenAt = yesterdayMillis - 100 * 60 * 1000,
            paidAt = yesterdayMillis - 3600 * 1000,
            generalNotes = "Cena familiar",
            totalAmount = 1120.0,
            tipPercentage = 5.0,
            tipAmount = 56.0,
            paymentMethod = "Efectivo"
        )
        orderDao.insertOrder(closedYesterday1)
        orderDao.insertOrderItems(listOf(
            OrderItem(id = 223, orderId = 96, productId = 1, productName = "Guacamole con Totopos", categoryId = "entradas", unitPrice = 135.0, quantity = 2, notes = ""),
            OrderItem(id = 224, orderId = 96, productId = 6, productName = "Hamburguesa Doble Suprema", categoryId = "fuertes", unitPrice = 235.0, quantity = 2, notes = ""),
            OrderItem(id = 225, orderId = 96, productId = 22, productName = "Margarita Tradicional", categoryId = "cocteles", unitPrice = 135.0, quantity = 2, notes = ""),
            OrderItem(id = 226, orderId = 96, productId = 25, productName = "Carajillo Shakerato", categoryId = "cocteles", unitPrice = 160.0, quantity = 1, notes = "")
        ))

        // 5. Ayer - Barra 2 (Laura Gómez)
        val closedYesterdayLaura = Order(
            id = 95,
            tableId = 9,
            tableNumber = "Barra 2",
            waiterId = "usr_laura",
            waiterName = "Laura Gómez",
            status = OrderStatus.PAGADA,
            createdAt = yesterdayMillis - 4 * 3600 * 1000,
            sentToKitchenAt = yesterdayMillis - 210 * 60 * 1000,
            paidAt = yesterdayMillis - 3 * 3600 * 1000,
            generalNotes = "",
            totalAmount = 530.0,
            tipPercentage = 5.0,
            tipAmount = 26.50,
            paymentMethod = "Transferencia"
        )
        orderDao.insertOrder(closedYesterdayLaura)
        orderDao.insertOrderItems(listOf(
            OrderItem(id = 227, orderId = 95, productId = 24, productName = "Mezcalita de Maracuyá", categoryId = "cocteles", unitPrice = 150.0, quantity = 2, notes = ""),
            OrderItem(id = 228, orderId = 95, productId = 6, productName = "Hamburguesa Doble Suprema", categoryId = "fuertes", unitPrice = 235.0, quantity = 1, notes = "")
        ))

        // 6. Hace 3 días - Mesa 7 (Carlos Mendieta)
        val threeDaysAgo = now - 3L * 86400 * 1000
        val closed3DaysAgo = Order(
            id = 94,
            tableId = 7,
            tableNumber = "Mesa 7",
            waiterId = "usr_carlos",
            waiterName = "Carlos Mendieta",
            status = OrderStatus.PAGADA,
            createdAt = threeDaysAgo - 2 * 3600 * 1000,
            sentToKitchenAt = threeDaysAgo - 100 * 60 * 1000,
            paidAt = threeDaysAgo - 3600 * 1000,
            generalNotes = "Reunión de amigos",
            totalAmount = 1650.0,
            tipPercentage = 5.0,
            tipAmount = 82.50,
            paymentMethod = "Tarjeta Crédito"
        )
        orderDao.insertOrder(closed3DaysAgo)
        orderDao.insertOrderItems(listOf(
            OrderItem(id = 229, orderId = 94, productId = 13, productName = "Corte Arrachera Angus 300g", categoryId = "tacos", unitPrice = 310.0, quantity = 3, notes = ""),
            OrderItem(id = 230, orderId = 94, productId = 22, productName = "Margarita Tradicional", categoryId = "cocteles", unitPrice = 135.0, quantity = 3, notes = ""),
            OrderItem(id = 231, orderId = 94, productId = 28, productName = "Volcán de Chocolate con Helado", categoryId = "postres", unitPrice = 130.0, quantity = 2, notes = "")
        ))

        // 7. Hace 5 días - Terraza 1 (Carlos Mendieta)
        val fiveDaysAgo = now - 5L * 86400 * 1000
        val closed5DaysAgo = Order(
            id = 93,
            tableId = 11,
            tableNumber = "Terraza 1",
            waiterId = "usr_carlos",
            waiterName = "Carlos Mendieta",
            status = OrderStatus.PAGADA,
            createdAt = fiveDaysAgo - 2 * 3600 * 1000,
            sentToKitchenAt = fiveDaysAgo - 110 * 60 * 1000,
            paidAt = fiveDaysAgo - 3600 * 1000,
            generalNotes = "",
            totalAmount = 940.0,
            tipPercentage = 5.0,
            tipAmount = 47.0,
            paymentMethod = "Efectivo"
        )
        orderDao.insertOrder(closed5DaysAgo)
        orderDao.insertOrderItems(listOf(
            OrderItem(id = 232, orderId = 93, productId = 9, productName = "Costillas BBQ Glaseadas", categoryId = "fuertes", unitPrice = 280.0, quantity = 2, notes = ""),
            OrderItem(id = 233, orderId = 93, productId = 24, productName = "Mezcalita de Maracuyá", categoryId = "cocteles", unitPrice = 150.0, quantity = 2, notes = ""),
            OrderItem(id = 234, orderId = 93, productId = 16, productName = "Agua de Horchata Artesanal", categoryId = "bebidas", unitPrice = 50.0, quantity = 1, notes = "")
        ))
    }

    suspend fun openTable(tableId: Int, dinersCount: Int) = withContext(Dispatchers.IO) {
        val table = tableDao.getTableByIdDirect(tableId) ?: return@withContext
        val updated = table.copy(
            status = TableStatus.OCUPADA,
            activeDinersCount = dinersCount
        )
        tableDao.updateTable(updated)
    }

    suspend fun createAndSendOrder(
        tableId: Int,
        tableNumber: String,
        items: List<OrderItem>,
        notes: String,
        asDraft: Boolean,
        waiterId: String = "usr_carlos",
        waiterName: String = "Carlos Mendieta",
        orderType: OrderType = OrderType.DINE_IN,
        customerName: String = "",
        customerPhone: String = "",
        deliveryAddress: String = "",
        deliveryZone: DeliveryZone = DeliveryZone.MOSTRADOR,
        assignedDriver: String = "",
        deliveryFee: Double = 0.0,
        createdByRole: String = "MESERO"
    ): Long = withContext(Dispatchers.IO) {
        val total = items.sumOf { it.unitPrice * it.quantity }
        val now = System.currentTimeMillis()
        val orderStatus = if (asDraft) OrderStatus.DRAFT else OrderStatus.ENVIADA_COCINA
        val sentAt = if (asDraft) null else now
        val defaultTipPct = if (orderType == OrderType.DINE_IN) 5.0 else 0.0

        val order = Order(
            tableId = tableId,
            tableNumber = tableNumber,
            orderType = orderType,
            fulfillmentStage = FulfillmentStage.EN_COCINA,
            customerName = customerName,
            customerPhone = customerPhone,
            deliveryAddress = deliveryAddress,
            deliveryZone = deliveryZone,
            assignedDriver = assignedDriver,
            deliveryFee = deliveryFee,
            createdByRole = createdByRole,
            waiterId = waiterId,
            waiterName = waiterName,
            status = orderStatus,
            createdAt = now,
            sentToKitchenAt = sentAt,
            generalNotes = notes,
            totalAmount = total,
            tipPercentage = defaultTipPct
        )
        val orderId = orderDao.insertOrder(order)

        val itemsWithOrderId = items.map {
            it.copy(orderId = orderId, subtotal = it.unitPrice * it.quantity)
        }
        orderDao.insertOrderItems(itemsWithOrderId)

        if (tableId > 0 && orderType == OrderType.DINE_IN) {
            val tableStatus = if (asDraft) TableStatus.OCUPADA else TableStatus.EN_COCINA
            tableDao.updateTableStatus(tableId, tableStatus, orderId)
        }

        orderId
    }

    suspend fun addItemsToExistingOrder(
        orderId: Long,
        newItems: List<OrderItem>,
        additionalNotes: String = ""
    ) = withContext(Dispatchers.IO) {
        val existingOrderWithItems = orderDao.getOrderWithItemsByIdDirect(orderId) ?: return@withContext
        val itemsToInsert = newItems.map {
            it.copy(id = 0, orderId = orderId, subtotal = it.unitPrice * it.quantity)
        }
        orderDao.insertOrderItems(itemsToInsert)

        val allItems = existingOrderWithItems.items + itemsToInsert
        val newTotal = allItems.sumOf { it.unitPrice * it.quantity }

        val combinedNotes = when {
            additionalNotes.isBlank() -> existingOrderWithItems.order.generalNotes
            existingOrderWithItems.order.generalNotes.isBlank() -> additionalNotes
            else -> "${existingOrderWithItems.order.generalNotes} | Extra: $additionalNotes"
        }

        val updatedOrder = existingOrderWithItems.order.copy(
            totalAmount = newTotal,
            status = OrderStatus.ENVIADA_COCINA,
            fulfillmentStage = FulfillmentStage.EN_COCINA,
            sentToKitchenAt = System.currentTimeMillis(),
            generalNotes = combinedNotes
        )
        orderDao.updateOrder(updatedOrder)
        if (updatedOrder.tableId > 0 && updatedOrder.orderType == OrderType.DINE_IN) {
            tableDao.updateTableStatus(updatedOrder.tableId, TableStatus.EN_COCINA, orderId)
        }
    }

    suspend fun payAndCloseOrder(
        orderId: Long,
        paymentMethod: String = "Efectivo",
        tipPercentage: Double = 5.0,
        tipAmount: Double = 0.0
    ) = withContext(Dispatchers.IO) {
        val orderWithItems = orderDao.getOrderWithItemsByIdDirect(orderId) ?: return@withContext
        val now = System.currentTimeMillis()
        val calculatedTip = if (tipAmount > 0.0) tipAmount else (orderWithItems.order.totalAmount * (tipPercentage / 100.0))

        orderDao.markOrderAsPaid(
            orderId = orderId,
            status = OrderStatus.PAGADA,
            stage = FulfillmentStage.ENTREGADO,
            paidAt = now,
            paymentMethod = paymentMethod,
            tipPercentage = tipPercentage,
            tipAmount = calculatedTip
        )

        if (orderWithItems.order.tableId > 0 && orderWithItems.order.orderType == OrderType.DINE_IN) {
            val table = tableDao.getTableByIdDirect(orderWithItems.order.tableId)
            if (table != null) {
                tableDao.updateTable(
                    table.copy(
                        status = TableStatus.LIBRE,
                        activeOrderId = null,
                        activeDinersCount = 0
                    )
                )
            }
        }
    }

    suspend fun updateOrderStatus(orderId: Long, newStatus: OrderStatus) = withContext(Dispatchers.IO) {
        val orderWithItems = orderDao.getOrderWithItemsByIdDirect(orderId) ?: return@withContext
        val order = orderWithItems.order
        val now = System.currentTimeMillis()
        val sentAt = if (newStatus == OrderStatus.ENVIADA_COCINA && order.sentToKitchenAt == null) now else order.sentToKitchenAt

        val mappedStage = when (newStatus) {
            OrderStatus.DRAFT, OrderStatus.ENVIADA_COCINA -> FulfillmentStage.EN_COCINA
            OrderStatus.LISTA -> if (order.orderType == OrderType.DELIVERY) FulfillmentStage.LISTO_DESPACHO else FulfillmentStage.LISTO_MOSTRADOR
            OrderStatus.SERVIDA -> if (order.orderType == OrderType.DELIVERY) FulfillmentStage.EN_RUTA else FulfillmentStage.POR_COBRAR
            OrderStatus.CUENTA -> FulfillmentStage.POR_COBRAR
            OrderStatus.PAGADA -> FulfillmentStage.ENTREGADO
        }

        if (newStatus == OrderStatus.PAGADA) {
            val defaultPct = if (order.orderType == OrderType.DINE_IN) 5.0 else 0.0
            val tip = order.totalAmount * (defaultPct / 100.0)
            orderDao.markOrderAsPaid(
                orderId = orderId,
                status = OrderStatus.PAGADA,
                stage = FulfillmentStage.ENTREGADO,
                paidAt = now,
                paymentMethod = order.paymentMethod.ifBlank { "Efectivo" },
                tipPercentage = defaultPct,
                tipAmount = tip
            )
        } else {
            orderDao.updateOrderStatusAndStage(orderId, newStatus, mappedStage, sentAt)
        }

        if (order.tableId > 0 && order.orderType == OrderType.DINE_IN) {
            val tableStatus = when (newStatus) {
                OrderStatus.DRAFT -> TableStatus.OCUPADA
                OrderStatus.ENVIADA_COCINA -> TableStatus.EN_COCINA
                OrderStatus.LISTA -> TableStatus.LISTO
                OrderStatus.SERVIDA -> TableStatus.SERVIDO
                OrderStatus.CUENTA -> TableStatus.CUENTA
                OrderStatus.PAGADA -> TableStatus.LIBRE
            }

            val activeOrderId = if (newStatus == OrderStatus.PAGADA) null else orderId
            tableDao.updateTableStatus(order.tableId, tableStatus, activeOrderId)
        }
    }

    suspend fun updateFulfillmentStage(orderId: Long, stage: FulfillmentStage) = withContext(Dispatchers.IO) {
        val orderWithItems = orderDao.getOrderWithItemsByIdDirect(orderId) ?: return@withContext
        val order = orderWithItems.order
        val now = System.currentTimeMillis()
        val sentAt = order.sentToKitchenAt ?: now

        val mappedStatus = when (stage) {
            FulfillmentStage.EN_COCINA -> OrderStatus.ENVIADA_COCINA
            FulfillmentStage.LISTO_MOSTRADOR, FulfillmentStage.LISTO_DESPACHO -> OrderStatus.LISTA
            FulfillmentStage.EN_RUTA -> OrderStatus.SERVIDA
            FulfillmentStage.POR_COBRAR -> OrderStatus.CUENTA
            FulfillmentStage.ENTREGADO -> OrderStatus.PAGADA
        }

        if (stage == FulfillmentStage.ENTREGADO) {
            orderDao.markOrderAsPaid(
                orderId = orderId,
                status = OrderStatus.PAGADA,
                stage = FulfillmentStage.ENTREGADO,
                paidAt = now,
                paymentMethod = order.paymentMethod.ifBlank { "Efectivo" },
                tipPercentage = order.tipPercentage,
                tipAmount = order.tipAmount
            )
        } else {
            orderDao.updateOrderStatusAndStage(orderId, mappedStatus, stage, sentAt)
        }
    }

    suspend fun assignDriverToOrder(orderId: Long, driverName: String) = withContext(Dispatchers.IO) {
        orderDao.updateAssignedDriver(orderId, driverName)
    }

    suspend fun releaseTable(tableId: Int) = withContext(Dispatchers.IO) {
        val table = tableDao.getTableByIdDirect(tableId)
        if (table != null) {
            if (table.activeOrderId != null) {
                val now = System.currentTimeMillis()
                val activeOrder = orderDao.getOrderWithItemsByIdDirect(table.activeOrderId)
                val tip = (activeOrder?.order?.totalAmount ?: 0.0) * 0.05
                orderDao.markOrderAsPaid(
                    orderId = table.activeOrderId,
                    status = OrderStatus.PAGADA,
                    stage = FulfillmentStage.ENTREGADO,
                    paidAt = now,
                    paymentMethod = activeOrder?.order?.paymentMethod?.ifBlank { "Efectivo" } ?: "Efectivo",
                    tipPercentage = 5.0,
                    tipAmount = tip
                )
            }
            val resetTable = table.copy(
                status = TableStatus.LIBRE,
                activeOrderId = null,
                activeDinersCount = 0
            )
            tableDao.updateTable(resetTable)
        }
    }

    suspend fun updateOrderItemNotes(itemId: Long, notes: String) = withContext(Dispatchers.IO) {
        orderDao.updateOrderItemNotes(itemId, notes)
    }

    suspend fun updateOrderGeneralNotes(orderId: Long, notes: String) = withContext(Dispatchers.IO) {
        orderDao.updateOrderGeneralNotes(orderId, notes)
    }
}
