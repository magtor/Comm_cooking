package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.model.Category
import com.example.data.model.FulfillmentStage
import com.example.data.model.Order
import com.example.data.model.OrderItem
import com.example.data.model.OrderStatus
import com.example.data.model.OrderWithItems
import com.example.data.model.Product
import com.example.data.model.RestaurantTable
import com.example.data.model.TableStatus
import com.example.data.model.User
import kotlinx.coroutines.flow.Flow

@Dao
interface TableDao {
    @Query("SELECT * FROM restaurant_tables ORDER BY id ASC")
    fun getAllTables(): Flow<List<RestaurantTable>>

    @Query("SELECT * FROM restaurant_tables WHERE id = :id")
    fun getTableById(id: Int): Flow<RestaurantTable?>

    @Query("SELECT * FROM restaurant_tables WHERE id = :id")
    suspend fun getTableByIdDirect(id: Int): RestaurantTable?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTable(table: RestaurantTable)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tables: List<RestaurantTable>)

    @Update
    suspend fun updateTable(table: RestaurantTable)

    @Query("UPDATE restaurant_tables SET status = :status, activeOrderId = :activeOrderId WHERE id = :id")
    suspend fun updateTableStatus(id: Int, status: TableStatus, activeOrderId: Long?)
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY displayOrder ASC")
    fun getAllCategories(): Flow<List<Category>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categories: List<Category>)
}

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY popular DESC, name ASC")
    fun getAllProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE categoryId = :categoryId ORDER BY name ASC")
    fun getProductsByCategory(categoryId: String): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: Long): Product?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(products: List<Product>)
}

@Dao
interface OrderDao {
    @Transaction
    @Query("SELECT * FROM orders ORDER BY createdAt DESC")
    fun getAllOrdersWithItems(): Flow<List<OrderWithItems>>

    @Transaction
    @Query("SELECT * FROM orders WHERE status != 'PAGADA' ORDER BY createdAt DESC")
    fun getActiveOrdersWithItems(): Flow<List<OrderWithItems>>

    @Transaction
    @Query("SELECT * FROM orders WHERE status != 'PAGADA' AND orderType != 'DINE_IN' ORDER BY createdAt ASC")
    fun getPendingTakeoutOrdersWithItems(): Flow<List<OrderWithItems>>

    @Transaction
    @Query("SELECT * FROM orders WHERE status = 'PAGADA' ORDER BY COALESCE(paidAt, createdAt) DESC")
    fun getClosedOrdersWithItems(): Flow<List<OrderWithItems>>

    @Transaction
    @Query("SELECT * FROM orders WHERE id = :orderId")
    fun getOrderWithItemsById(orderId: Long): Flow<OrderWithItems?>

    @Transaction
    @Query("SELECT * FROM orders WHERE id = :orderId")
    suspend fun getOrderWithItemsByIdDirect(orderId: Long): OrderWithItems?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: Order): Long

    @Update
    suspend fun updateOrder(order: Order)

    @Query("UPDATE orders SET status = :status, fulfillmentStage = :stage, sentToKitchenAt = :sentAt WHERE id = :orderId")
    suspend fun updateOrderStatusAndStage(orderId: Long, status: OrderStatus, stage: FulfillmentStage, sentAt: Long?)

    @Query("UPDATE orders SET status = :status, sentToKitchenAt = :sentAt WHERE id = :orderId")
    suspend fun updateOrderStatus(orderId: Long, status: OrderStatus, sentAt: Long?)

    @Query("UPDATE orders SET assignedDriver = :driverName WHERE id = :orderId")
    suspend fun updateAssignedDriver(orderId: Long, driverName: String)

    @Query("UPDATE orders SET status = :status, fulfillmentStage = :stage, paidAt = :paidAt, paymentMethod = :paymentMethod, tipPercentage = :tipPercentage, tipAmount = :tipAmount WHERE id = :orderId")
    suspend fun markOrderAsPaid(
        orderId: Long,
        status: OrderStatus = OrderStatus.PAGADA,
        stage: FulfillmentStage = FulfillmentStage.ENTREGADO,
        paidAt: Long,
        paymentMethod: String,
        tipPercentage: Double,
        tipAmount: Double
    )

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrderItems(items: List<OrderItem>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrderItem(item: OrderItem): Long

    @Update
    suspend fun updateOrderItem(item: OrderItem)

    @Query("UPDATE order_items SET notes = :notes WHERE id = :itemId")
    suspend fun updateOrderItemNotes(itemId: Long, notes: String)

    @Query("UPDATE orders SET generalNotes = :notes WHERE id = :orderId")
    suspend fun updateOrderGeneralNotes(orderId: Long, notes: String)

    @Delete
    suspend fun deleteOrderItem(item: OrderItem)

    @Query("DELETE FROM order_items WHERE id = :itemId")
    suspend fun deleteOrderItemById(itemId: Long)

    @Query("DELETE FROM order_items WHERE orderId = :orderId")
    suspend fun deleteItemsByOrderId(orderId: Long)

    @Query("DELETE FROM orders WHERE id = :orderId")
    suspend fun deleteOrderById(orderId: Long)
}

@Dao
interface UserDao {
    @Query("SELECT * FROM users ORDER BY fullName ASC")
    fun getAllUsers(): Flow<List<User>>

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserById(id: String): User?

    @Query("SELECT * FROM users WHERE (username = :usernameOrEmail OR email = :usernameOrEmail) AND pin = :pin")
    suspend fun authenticate(usernameOrEmail: String, pin: String): User?

    @Query("SELECT * FROM users WHERE pin = :pin AND isActive = 1")
    suspend fun getUserByPin(pin: String): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(users: List<User>)

    @Update
    suspend fun updateUser(user: User)

    @Query("UPDATE users SET lastLoginAt = :timestamp WHERE id = :userId")
    suspend fun updateLastLogin(userId: String, timestamp: Long)

    @Delete
    suspend fun deleteUser(user: User)
}
