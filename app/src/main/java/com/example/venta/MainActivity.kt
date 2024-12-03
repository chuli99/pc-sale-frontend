package com.example.venta


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.venta.ui.theme.VentaTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.Composable

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VentaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    DispositivoListScreen()
                }
            }
        }
    }
}

@Composable
fun DispositivoListScreen() {
    val assembler = remember { DataAssembler(RetrofitClient.apiService) }
    val dispositivosWithDetails = remember { mutableStateOf<List<DispositivoWithDetails>>(emptyList()) }
    val selectedDispositivo = remember { mutableStateOf<DispositivoWithDetails?>(null) }
    val isLoading = remember { mutableStateOf(true) }
    val errorMessage = remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            val fetchedDispositivos = assembler.fetchCompleteDispositivos()
            dispositivosWithDetails.value = fetchedDispositivos
        } catch (e: Exception) {
            errorMessage.value = "Error al cargar los dispositivos: ${e.message}"
        } finally {
            isLoading.value = false
        }
    }

    if (selectedDispositivo.value != null) {
        DispositivoDetailsScreen(
            dispositivoDetails = selectedDispositivo.value!!,
            onBack = { selectedDispositivo.value = null },
            onVentaExitosa = {
                println("Compra realizada con éxito")
                selectedDispositivo.value = null
            },
            onError = { mensaje ->
                println("Error durante la compra: $mensaje")
            }
        )
    } else if (isLoading.value) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                color = Color(0xFF007BFF),
                modifier = Modifier.size(50.dp)
            )
        }
    } else if (errorMessage.value != null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = errorMessage.value ?: "Error desconocido",
                color = MaterialTheme.colors.error,
                style = MaterialTheme.typography.h6.copy(color = Color(0xFFD32F2F))
            )
        }
    } else {
        DispositivoList(dispositivosWithDetails.value) { dispositivo ->
            selectedDispositivo.value = dispositivo
        }
    }
}

@Composable
fun DispositivoList(
    dispositivos: List<DispositivoWithDetails>,
    onDispositivoSelected: (DispositivoWithDetails) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xff424242),
                        Color(0xff212121)
                    )
                )
            )
            .padding(16.dp)
    ) {
        items(dispositivos) { dispositivoDetails ->
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(500)) +
                        slideInVertically(animationSpec = tween(500))
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clip(RoundedCornerShape(15.dp))
                        .clickable { onDispositivoSelected(dispositivoDetails) },
                    elevation = 6.dp,
                    backgroundColor = Color.Black
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = dispositivoDetails.dispositivo.nombre,
                            style = MaterialTheme.typography.h6.copy(
                                fontWeight = FontWeight.Black,
                                color = Color(0xffffffff)
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = dispositivoDetails.dispositivo.descripcion,
                            style = MaterialTheme.typography.body2.copy(
                                color = Color(0xF3F3F3F3)
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Precio: ${dispositivoDetails.dispositivo.precioBase} ${dispositivoDetails.dispositivo.moneda}",
                                style = MaterialTheme.typography.body1.copy(
                                    fontWeight = FontWeight.Black,
                                    color = Color.Green
                                )
                            )
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = "Seleccionar dispositivo",
                                tint = Color.Green
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CaracteristicasDropdown(
    caracteristicas: List<Caracteristica>
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        // Botón que activa o desactiva el menú desplegable
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(8.dp)
                .background(
                    color = Color(0xFF424242),
                    shape = RoundedCornerShape(8.dp)
                )
                .border(1.dp, Color.White, RoundedCornerShape(8.dp))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Características:",
                style = MaterialTheme.typography.subtitle1.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = Color.White
            )
        }

        // Menú desplegable
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF333333))
        ) {
            caracteristicas.forEach { caracteristica ->
                DropdownMenuItem(
                    onClick = { /* No es necesario manejar clics aquí, solo mostramos información */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Column {
                        Text(
                            text = " ${caracteristica.nombre}",
                            style = MaterialTheme.typography.body1.copy(color = Color.White)
                        )
                        Text(
                            text = caracteristica.descripcion,
                            style = MaterialTheme.typography.body2.copy(color = Color.Gray)
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun DispositivoDetailsScreen(
    dispositivoDetails: DispositivoWithDetails,
    onBack: () -> Unit,
    onVentaExitosa: () -> Unit,
    onError: (String) -> Unit
) {
    val selectedOpciones = remember { mutableStateMapOf<String, Opcion>() }
    val selectedAdicionales = remember { mutableStateMapOf<Int, Boolean>() }

    val basePlusPersonalizations by remember {
        derivedStateOf {
            dispositivoDetails.dispositivo.precioBase + selectedOpciones.values.sumOf { it.precioAdicional }
        }
    }

    val totalPrice by remember {
        derivedStateOf {
            val adicionalPrice = selectedAdicionales.filterValues { it }.keys.sumOf { id ->
                val adicional = dispositivoDetails.adicionales.firstOrNull { it.id == id }
                if (adicional != null && adicional.precioGratis != -1.0 &&
                    basePlusPersonalizations >= adicional.precioGratis
                ) {
                    0.0
                } else {
                    adicional?.precio ?: 0.0
                }
            }
            basePlusPersonalizations + adicionalPrice
        }
    }

    var isProcessingVenta by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xff424242),
                        Color(0xff212121)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            IconButton(
                onClick = { onBack() },
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(bottom = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Volver",
                    tint = Color(0xFFFFFFFF)
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                item {
                    Text(
                        text = dispositivoDetails.dispositivo.nombre,
                        style = MaterialTheme.typography.h5.copy(
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFFFFFFF)
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = dispositivoDetails.dispositivo.descripcion,
                        style = MaterialTheme.typography.body1.copy(
                            color = Color(0xF3F3F3F3)
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Sección "Características" con menú desplegable
                    CaracteristicasDropdown(caracteristicas = dispositivoDetails.caracteristicas)

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Personalizaciones:",
                        style = MaterialTheme.typography.h6.copy(
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFFFFFFF)
                        )
                    )
                    dispositivoDetails.personalizaciones.forEach { personalizacion ->
                        PersonalizacionItem(personalizacion, selectedOpciones)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    AdicionalesSection(
                        adicionales = dispositivoDetails.adicionales,
                        selectedAdicionales = selectedAdicionales,
                        basePlusPersonalizations = basePlusPersonalizations
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "$${String.format("%.2f", totalPrice)}",
                style = MaterialTheme.typography.h5.copy(
                    fontWeight = FontWeight.Black,
                    color = Color.Green
                ),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (isProcessingVenta) {
                CircularProgressIndicator(
                    color = Color(0xFF007BFF),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = {
                            isProcessingVenta = true
                            realizarVenta(
                                dispositivoDetails,
                                selectedOpciones,
                                selectedAdicionales,
                                totalPrice,
                                onVentaExitosa = {
                                    isProcessingVenta = false
                                    onVentaExitosa()
                                },
                                onError = { mensaje ->
                                    isProcessingVenta = false
                                    onError(mensaje)
                                }
                            )
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color.Green,
                            contentColor = Color.Black
                        )
                    ) {
                        Text("Comprar")
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(
                        onClick = { onBack() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color.White,
                            contentColor = Color(0xFF1A2B3C)
                        )
                    ) {
                        Text("Cancelar")
                    }
                }
            }
        }
    }
}

// Las siguientes funciones (realizarVenta, PersonalizacionItem, AdicionalItem, AdicionalesSection)
// permanecen igual que en el código original, solo con pequeñas modificaciones de estilo


fun realizarVenta(
    dispositivoDetails: DispositivoWithDetails,
    selectedOpciones: Map<String, Opcion>,
    selectedAdicionales: Map<Int, Boolean>,
    totalPrice: Double,
    onVentaExitosa: () -> Unit,
    onError: (String) -> Unit
) {
    val apiService = RetrofitClient.apiService
    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
    dateFormat.timeZone = TimeZone.getTimeZone("UTC")
    val fechaVenta = dateFormat.format(Date())

    val ventaRequest = VentaRequest(
        idDispositivo = dispositivoDetails.dispositivo.id,
        personalizaciones = dispositivoDetails.personalizaciones.map { personalizacion ->
            val opcionSeleccionada = selectedOpciones[personalizacion.nombre]
                ?: personalizacion.opciones.first() // Selecciona la primera opción si no hay una seleccionada
            PersonalizacionVenta(
                id = personalizacion.id, // ID de la personalización
                precio = opcionSeleccionada.precioAdicional,
                opcion = OpcionVenta(id = opcionSeleccionada.id) // ID de la opción seleccionada
            )
        },
        adicionales = selectedAdicionales.filterValues { it }.keys.map { id ->
            val adicional = dispositivoDetails.adicionales.first { it.id == id }
            AdicionalVenta(
                id = adicional.id,
                precio = adicional.precio
            )
        },
        precioFinal = totalPrice,
        fechaVenta = fechaVenta
    )


    kotlinx.coroutines.GlobalScope.launch {
        try {
            val response = apiService.realizarVenta(ventaRequest)
            // Manejo de respuesta directa
            withContext(Dispatchers.Main) {
                onVentaExitosa()
            }
        } catch (e: Exception) {
            // Manejo de errores
            withContext(Dispatchers.Main) {
                onError("Error al realizar la venta: ${e.message}")
            }
        }
    }
}

@Composable
fun PersonalizacionItem(
    personalizacion: Personalizacion,
    selectedOpciones: MutableMap<String, Opcion>
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = personalizacion.nombre,
            style = MaterialTheme.typography.subtitle1.copy(color = Color.White)
        )
        Spacer(modifier = Modifier.height(4.dp))
        personalizacion.opciones.forEach { opcion ->
            val isSelected = selectedOpciones[personalizacion.nombre] == opcion

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isSelected) Color(0xFF444444) else Color.Transparent) // Fondo seleccionado más oscuro
                    .border(1.dp, if (isSelected) Color.White else Color.Gray) // Borde opcional
                    .clickable {
                        selectedOpciones[personalizacion.nombre] = opcion
                    }
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = isSelected,
                    onClick = {
                        selectedOpciones[personalizacion.nombre] = opcion
                    },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = Color.White, // Color del RadioButton seleccionado
                        unselectedColor = Color.Gray // Color del RadioButton no seleccionado
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${opcion.nombre} \n+$${opcion.precioAdicional}",
                    style = MaterialTheme.typography.body1.copy(color = Color.White)
                )
            }
        }
    }
}



@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AdicionalItem(
    adicional: Adicional,
    selectedAdicionales: MutableMap<Int, Boolean>,
    basePlusPersonalizations: Double
) {
    val isSelected = selectedAdicionales[adicional.id] ?: false
    val enPromocion = adicional.precioGratis != -1.0 && basePlusPersonalizations >= adicional.precioGratis

    // Diseño del cuadrado para cada adicional
    Column(
        modifier = Modifier
            .padding(8.dp)
            .size(120.dp) // Tamaño del cuadrado
            .background(
                color = if (isSelected) Color.DarkGray else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            ) // Fondo y bordes redondeados
            .border(
                width = 2.dp,
                color = if (isSelected) Color.White else Color.Gray,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable {
                selectedAdicionales[adicional.id] = !isSelected
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = adicional.nombre,
            style = MaterialTheme.typography.body1.copy(color = Color.White)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (enPromocion) {
                "Gratis"
            } else {
                "$${String.format("%.2f", adicional.precio)}"
            },
            style = MaterialTheme.typography.body2,
            color = MaterialTheme.colors.secondary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Checkbox(
            checked = isSelected,
            onCheckedChange = { selectedAdicionales[adicional.id] = it },
            colors = CheckboxDefaults.colors(
                checkedColor = Color.White,
                uncheckedColor = Color.Gray,
                checkmarkColor = Color.Black
            )
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AdicionalesSection(
    adicionales: List<Adicional>,
    selectedAdicionales: MutableMap<Int, Boolean>,
    basePlusPersonalizations: Double
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Adicionales:",
            style = MaterialTheme.typography.h6.copy(color = Color.White)
        )
        Spacer(modifier = Modifier.height(8.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(3), // Tres columnas
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp), // Altura máxima de la cuadrícula
            contentPadding = PaddingValues(8.dp)
        ) {
            items(adicionales.size) { index ->
                val adicional = adicionales[index]
                AdicionalItem(
                    adicional = adicional,
                    selectedAdicionales = selectedAdicionales,
                    basePlusPersonalizations = basePlusPersonalizations
                )
            }
        }
    }
}










