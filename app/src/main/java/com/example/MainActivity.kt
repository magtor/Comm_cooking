package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.TableBar
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.model.TableStatus
import com.example.ui.screens.CartScreen
import com.example.ui.screens.CatalogScreen
import com.example.ui.screens.ClosedOrdersScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.OrderDetailScreen
import com.example.ui.screens.OrdersScreen
import com.example.ui.screens.TablesScreen
import com.example.ui.screens.TrackingScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.ComandaViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppContent()
            }
        }
    }
}

@Composable
fun MainAppContent() {
    val navController = rememberNavController()
    val viewModel: ComandaViewModel = viewModel()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val allTables by viewModel.tables.collectAsStateWithLifecycle()
    val activeOrders by viewModel.activeOrders.collectAsStateWithLifecycle()
    val pendingTakeoutOrders by viewModel.pendingTakeoutOrders.collectAsStateWithLifecycle()

    val readyCount = allTables.count { it.status == TableStatus.LISTO }
    val pendingTakeoutCount = pendingTakeoutOrders.size
    val showBottomBar = currentRoute in listOf("tables", "orders", "tracking", "closed_orders")

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .testTag("main_bottom_nav")
                ) {
                    NavigationBarItem(
                        selected = currentRoute == "tables",
                        onClick = {
                            navController.navigate("tables") {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(imageVector = Icons.Default.TableBar, contentDescription = "Mesas")
                        },
                        label = { Text("Mesas") },
                        modifier = Modifier.testTag("nav_tab_tables")
                    )

                    NavigationBarItem(
                        selected = currentRoute == "orders",
                        onClick = {
                            navController.navigate("orders") {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            if (readyCount > 0) {
                                BadgedBox(badge = { Badge { Text("$readyCount") } }) {
                                    Icon(imageVector = Icons.Default.Receipt, contentDescription = "Comandas")
                                }
                            } else {
                                Icon(imageVector = Icons.Default.Receipt, contentDescription = "Comandas")
                            }
                        },
                        label = { Text("Comandas") },
                        modifier = Modifier.testTag("nav_tab_orders")
                    )

                    NavigationBarItem(
                        selected = currentRoute == "tracking",
                        onClick = {
                            navController.navigate("tracking") {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            if (pendingTakeoutCount > 0) {
                                BadgedBox(badge = { Badge { Text("$pendingTakeoutCount") } }) {
                                    Icon(imageVector = Icons.Default.DeliveryDining, contentDescription = "Seguimiento Pick-Up y Delivery")
                                }
                            } else {
                                Icon(imageVector = Icons.Default.DeliveryDining, contentDescription = "Seguimiento Pick-Up y Delivery")
                            }
                        },
                        label = { Text("Entregas") },
                        modifier = Modifier.testTag("nav_tab_tracking")
                    )

                    NavigationBarItem(
                        selected = currentRoute == "closed_orders",
                        onClick = {
                            navController.navigate("closed_orders") {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(imageVector = Icons.Default.Payments, contentDescription = "Cerradas / Propinas")
                        },
                        label = { Text("Propinas") },
                        modifier = Modifier.testTag("nav_tab_closed_orders")
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "login",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("login") {
                LoginScreen(
                    viewModel = viewModel,
                    onLoginSuccess = {
                        navController.navigate("tables") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                )
            }

            composable("tables") {
                TablesScreen(
                    viewModel = viewModel,
                    onNavigateToCatalog = {
                        navController.navigate("catalog")
                    },
                    onNavigateToOrderDetail = { orderId ->
                        navController.navigate("order_detail/$orderId")
                    },
                    onNavigateToTracking = {
                        navController.navigate("tracking") {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToLogin = {
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            composable("orders") {
                OrdersScreen(
                    viewModel = viewModel,
                    onNavigateToOrderDetail = { orderId ->
                        navController.navigate("order_detail/$orderId")
                    }
                )
            }

            composable("tracking") {
                TrackingScreen(
                    viewModel = viewModel,
                    onNavigateToCatalog = {
                        navController.navigate("catalog")
                    },
                    onNavigateToOrderDetail = { orderId ->
                        navController.navigate("order_detail/$orderId")
                    }
                )
            }

            composable("closed_orders") {
                ClosedOrdersScreen(
                    viewModel = viewModel,
                    onNavigateToOrderDetail = { orderId ->
                        navController.navigate("order_detail/$orderId")
                    }
                )
            }

            composable("catalog") {
                CatalogScreen(
                    viewModel = viewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToCart = {
                        navController.navigate("cart")
                    }
                )
            }

            composable("cart") {
                CartScreen(
                    viewModel = viewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onComandaSent = { orderId ->
                        navController.navigate("order_detail/$orderId") {
                            popUpTo("tables")
                        }
                    }
                )
            }

            composable(
                route = "order_detail/{orderId}",
                arguments = listOf(navArgument("orderId") { type = NavType.LongType })
            ) { backStackEntry ->
                val orderId = backStackEntry.arguments?.getLong("orderId") ?: 0L
                OrderDetailScreen(
                    orderId = orderId,
                    viewModel = viewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onAddMoreProducts = {
                        navController.navigate("catalog")
                    }
                )
            }
        }
    }
}
