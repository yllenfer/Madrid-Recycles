package com.example.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageProxy
import androidx.camera.view.LifecycleCameraController
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.GarbageSortingResult
import com.example.data.MadridData
import com.example.data.WasteContainer
import com.example.ui.theme.EcoForestGreen
import com.example.ui.theme.EcoMintTeal
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.io.InputStream
import java.nio.ByteBuffer

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun WasteApp(
    viewModel: WasteViewModel = viewModel()
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) }

    // Observe state from ViewModel
    val scanUiState by viewModel.scanState.collectAsStateWithLifecycle()
    val isCustomLocationMocked by viewModel.isCustomLocationMocked.collectAsStateWithLifecycle()
    val userLat by viewModel.userLatitude.collectAsStateWithLifecycle()
    val userLng by viewModel.userLongitude.collectAsStateWithLifecycle()
    val language by viewModel.language.collectAsStateWithLifecycle()

    // Request GPS location on start (if permission is already granted)
    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    
    LaunchedEffect(locationPermissionState.status.isGranted) {
        if (locationPermissionState.status.isGranted) {
            viewModel.requestDeviceLocation(context)
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                modifier = Modifier.testTag("main_navigation_bar"),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.PhotoCamera, contentDescription = AppStrings.get("scanner_tab", language)) },
                    label = { Text(AppStrings.get("scanner_tab", language), fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.testTag("nav_btn_scan")
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Search, contentDescription = AppStrings.get("search_tab", language)) },
                    label = { Text(AppStrings.get("search_tab", language), fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.testTag("nav_btn_search")
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Map, contentDescription = AppStrings.get("points_tab", language)) },
                    label = { Text(AppStrings.get("points_tab", language), fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.testTag("nav_btn_locations")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (selectedTab) {
                0 -> ScannerScreen(viewModel = viewModel)
                1 -> SearchScreen(viewModel = viewModel)
                2 -> LocationsScreen(viewModel = viewModel)
            }

            // Language Selection Toggle Button (Floating pill in top-right of screen content)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.TopEnd
            ) {
                Surface(
                    onClick = { viewModel.toggleLanguage() },
                    shape = RoundedCornerShape(20.dp),
                    color = Color.Black.copy(alpha = 0.5f),
                    contentColor = Color.White,
                    modifier = Modifier.testTag("language_toggle_btn")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = if (language == "en") "🇬🇧 EN" else "🇪🇸 ES",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = "Switch language",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            // Global Scan Result Overlay Dialog
            when (val state = scanUiState) {
                is ScanUiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f))
                            .clickable(enabled = false) {}, // absorb taps
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier.padding(32.dp).widthIn(max = 340.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator(color = EcoForestGreen, strokeWidth = 5.dp)
                                Spacer(modifier = Modifier.height(20.dp))
                                Text(
                                    text = AppStrings.get("analyzing_ai", language),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = AppStrings.get("consulting_rules", language),
                                    style = MaterialTheme.typography.bodySmall,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                is ScanUiState.Success -> {
                    ResultDialog(
                        result = state.result,
                        language = language,
                        onDismiss = { viewModel.clearScanState() }
                    )
                }
                is ScanUiState.Error -> {
                    ResultErrorDialog(
                        message = state.message,
                        language = language,
                        onDismiss = { viewModel.clearScanState() }
                    )
                }
                else -> {}
            }
        }
    }
}

// ======================== SCREEN 1: SCANNER SCREEN ========================

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ScannerScreen(viewModel: WasteViewModel) {
    val context = LocalContext.current
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    val language by viewModel.language.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // App Premium Header Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(EcoForestGreen, EcoMintTeal)
                    )
                )
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Column {
                Text(
                    text = AppStrings.get("app_title", language),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )
                Text(
                    text = AppStrings.get("scanner_subtitle", language),
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 12.sp
                )
            }
        }

        if (cameraPermissionState.status.isGranted) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                // Initialize controller for CameraX
                val cameraController = remember {
                    LifecycleCameraController(context).apply {
                        setEnabledUseCases(LifecycleCameraController.IMAGE_CAPTURE)
                    }
                }

                // Camera Frame
                AndroidView(
                    modifier = Modifier.fillMaxSize().testTag("camera_preview_view"),
                    factory = { ctx ->
                        val previewView = androidx.camera.view.PreviewView(ctx).apply {
                            controller = cameraController
                        }
                        cameraController.bindToLifecycle(ctx as androidx.lifecycle.LifecycleOwner)
                        previewView
                    }
                )

                // Visual Overlay Scanning box
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(240.dp)
                            .border(width = 3.dp, color = Color.White.copy(alpha = 0.8f), shape = RoundedCornerShape(24.dp))
                    )
                }

                // Simulations panel on emulator / immediate try
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.65f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Text(
                            text = AppStrings.get("emulator_warning", language),
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                // Shutter Button triggers picture capture
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = {
                            capturePhotoAndScan(context, cameraController) { bitmap ->
                                viewModel.scanImage(bitmap)
                            }
                        },
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        modifier = Modifier
                            .size(76.dp)
                            .testTag("scan_button"),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Camera,
                            contentDescription = AppStrings.get("scanner_tab", language),
                            tint = EcoForestGreen,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        } else {
            // No permission layout
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = AppStrings.get("camera_access_required", language),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(72.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = AppStrings.get("camera_access_required", language),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = AppStrings.get("camera_access_desc", language),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { cameraPermissionState.launchPermissionRequest() },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("btn_request_camera")
                ) {
                    Text(AppStrings.get("enable_camera", language))
                }
            }
        }

        // Demo interactive simulations drawer at the bottom
        SimulationsDrawer(language = language, onSimulate = { selectedObject ->
            viewModel.searchWaste(selectedObject)
        })
    }
}

private fun capturePhotoAndScan(
    context: Context,
    controller: LifecycleCameraController,
    onSuccess: (Bitmap) -> Unit
) {
    // Take picture manually
    controller.takePicture(
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                try {
                    val buffer: ByteBuffer = image.planes[0].buffer
                    val bytes = ByteArray(buffer.remaining())
                    buffer.get(bytes)
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    if (bitmap != null) {
                        onSuccess(bitmap)
                    } else {
                        Log.e("CameraX", "Failed turning captured frame into bitmap.")
                    }
                } catch (e: Exception) {
                    Log.e("CameraX", "Error parsing captures: ${e.message}")
                } finally {
                    image.close()
                }
            }
        }
    )
}

@Composable
fun SimulationsDrawer(language: String, onSimulate: (String) -> Unit) {
    val quickObjects = if (language == "en") {
        listOf(
            Pair("🥤 Plastic Bottle", "Empty plastic bottle"),
            Pair("📦 Cardboard Box", "Folded cardboard packaging box"),
            Pair("🔋 Dead Batteries", "Dead batteries"),
            Pair("🍳 Old Frying Pan", "Old broken teflon frying pan"),
            Pair("🍎 Apple Peel", "Fruit peels and apple leftovers"),
            Pair("🍼 Milk Carton", "Empty milk carton")
        )
    } else {
        listOf(
            Pair("🥤 Botella Plástica", "Botella de plástico vacía"),
            Pair("📦 Caja de Cartón", "Caja de cartón de embalar plegada"),
            Pair("🔋 Pilas Gastadas", "Pilas gastadas"),
            Pair("🍳 Sartén Vieja", "Sartén de teflón rota vieja"),
            Pair("🍎 Cáscara de Manzana", "Peladuras de fruta y restos de manzana"),
            Pair("🍼 Brik de Leche", "Brik de leche vacío")
        )
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = AppStrings.get("emulator_drawer_title", language),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // First 3 items
                quickObjects.take(3).forEach { (display, query) ->
                    AssistChip(
                        onClick = { onSimulate(query) },
                        label = { Text(display, fontSize = 11.sp) },
                        modifier = Modifier.weight(1f).testTag("simulate_chip_${display.filter { it.isLetter() }}")
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Next 3 items
                quickObjects.drop(3).take(3).forEach { (display, query) ->
                    AssistChip(
                        onClick = { onSimulate(query) },
                        label = { Text(display, fontSize = 11.sp) },
                        modifier = Modifier.weight(1f).testTag("simulate_chip_${display.filter { it.isLetter() }}")
                    )
                }
            }
        }
    }
}

// ======================== SCREEN 2: SEARCH SCREEN ========================

@Composable
fun SearchScreen(viewModel: WasteViewModel) {
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val controller = LocalSoftwareKeyboardController.current
    val language by viewModel.language.collectAsStateWithLifecycle()

    val officialBins = remember { WasteContainer.values() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Simple search input
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.updateSearchQuery(it) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_text_input"),
            placeholder = { Text(AppStrings.get("search_placeholder", language)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = AppStrings.get("search_tab", language)) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = {
                if (searchQuery.isNotEmpty()) {
                    controller?.hide()
                    viewModel.searchWaste(searchQuery)
                }
            }),
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Large CTA Search Button
        Button(
            onClick = {
                if (searchQuery.isNotEmpty()) {
                    controller?.hide()
                    viewModel.searchWaste(searchQuery)
                }
            },
            enabled = searchQuery.isNotEmpty(),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("btn_search_submit")
        ) {
            Text(AppStrings.get("search_cta", language))
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = AppStrings.get("container_guide_title", language),
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(officialBins) { bin ->
                WasteBinInfoCard(bin = bin, language = language, onExampleClick = { example ->
                    viewModel.updateSearchQuery(example)
                    viewModel.searchWaste(example)
                })
            }
        }
    }
}

@Composable
fun WasteBinInfoCard(bin: WasteContainer, language: String, onExampleClick: (String) -> Unit) {
    val color = remember(bin.colorHex) { Color(android.graphics.Color.parseColor(bin.colorHex)) }

    Card(
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
        ) {
            // Container Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color.copy(alpha = 0.2f))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Container Color box indicator
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(color)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = bin.getName(language),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            }

            // Description and Examples
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = bin.getDescription(language),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))
                
                Text(
                    text = AppStrings.get("container_examples_label", language),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(6.dp))
                
                FlowRowLayout(
                    spacing = 6.dp
                ) {
                    bin.getExamples(language).forEach { itemexample ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
                                .clickable { onExampleClick(itemexample) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = itemexample,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }
        }
    }
}

// Custom flow row layout so we don't depend on external standard libraries which might break versions
@Composable
fun FlowRowLayout(
    spacing: androidx.compose.ui.unit.Dp = 8.dp,
    content: @Composable () -> Unit
) {
    androidx.compose.ui.layout.Layout(
        content = content
    ) { measurables, constraints ->
        var xPosition = 0
        var yPosition = 0
        var maxRowHeight = 0
        val placeables = measurables.map { measurable ->
            measurable.measure(constraints.copy(minWidth = 0, minHeight = 0))
        }

        val positions = mutableListOf<Pair<Int, Int>>()
        val spacingPx = spacing.roundToPx()

        placeables.forEach { placeable ->
            if (xPosition + placeable.width > constraints.maxWidth) {
                // Move to next row
                xPosition = 0
                yPosition += maxRowHeight + spacingPx
                maxRowHeight = 0
            }
            positions.add(Pair(xPosition, yPosition))
            xPosition += placeable.width + spacingPx
            maxRowHeight = maxOf(maxRowHeight, placeable.height)
        }

        layout(
            width = constraints.maxWidth,
            height = if (positions.isEmpty()) 0 else positions.last().second + maxRowHeight
        ) {
            placeables.forEachIndexed { index, placeable ->
                val (x, y) = positions[index]
                placeable.placeRelative(x, y)
            }
        }
    }
}

// ======================== SCREEN 3: LOCATIONS SCREEN ========================

@Composable
fun LocationsScreen(viewModel: WasteViewModel) {
    val context = LocalContext.current
    val sortedPoints by viewModel.sortedPuntosLimpios.collectAsStateWithLifecycle()
    val isMocked by viewModel.isCustomLocationMocked.collectAsStateWithLifecycle()
    val userLat by viewModel.userLatitude.collectAsStateWithLifecycle()
    val userLng by viewModel.userLongitude.collectAsStateWithLifecycle()
    val language by viewModel.language.collectAsStateWithLifecycle()

    val districts = if (language == "en") {
        listOf(
            Triple("Center (Puerta del Sol)", MadridData.MADRID_CENTER_LAT, MadridData.MADRID_CENTER_LNG),
            Triple("Retiro", 40.4111, -3.6750),
            Triple("Chamberí", 40.4362, -3.7038),
            Triple("Chamartín", 40.4620, -3.6764),
            Triple("Vallecas", 40.3840, -3.6521)
        )
    } else {
        listOf(
            Triple("Centro (Puerta del Sol)", MadridData.MADRID_CENTER_LAT, MadridData.MADRID_CENTER_LNG),
            Triple("Retiro", 40.4111, -3.6750),
            Triple("Chamberí", 40.4362, -3.7038),
            Triple("Chamartín", 40.4620, -3.6764),
            Triple("Vallecas", 40.3840, -3.6521)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Geo header
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = AppStrings.get("ref_location_title", language),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = AppStrings.get("ref_location_title", language),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (isMocked) {
                        AppStrings.get("loc_simulated", language) + " (${String.format("%.4f", userLat)}, ${String.format("%.4f", userLng)})"
                    } else {
                        AppStrings.get("loc_gps", language)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = AppStrings.get("loc_change_hint", language),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))

                // Scrollable district simulation pills
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    districts.take(3).forEach { (barrio, bLat, bLng) ->
                        val active = isMocked && Math.abs(userLat - bLat) < 0.005
                        FilterChip(
                            selected = active,
                            onClick = { viewModel.setDemoLocation(barrio, bLat, bLng) },
                            label = { Text(barrio, fontSize = 10.sp) },
                            modifier = Modifier.testTag("district_chip_${barrio.filter { it.isLetter() }}")
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = AppStrings.get("locations_main_title", language),
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(sortedPoints) { item ->
                PuntoLimpioItemRow(item = item, language = language, context = context)
            }
        }
    }
}

@Composable
fun PuntoLimpioItemRow(item: PuntoLimpioDistance, language: String, context: Context) {
    Card(
        elevation = CardDefaults.cardElevation(1.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.punto.name,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.punto.address,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Beautiful Km tag
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(EcoMintTeal.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${String.format("%.2f", item.distanceKm)} km",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = EcoMintTeal
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Divider(color = MaterialTheme.colorScheme.outlineVariant)

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = "Schedule",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (language == "en") item.punto.scheduleEn else item.punto.scheduleEs,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Direct route button triggers Maps Navigation intent
            Button(
                onClick = {
                    val mapUri = Uri.parse("google.navigation:q=${item.punto.latitude},${item.punto.longitude}&mode=d")
                    val intent = Intent(Intent.ACTION_VIEW, mapUri).apply {
                        setPackage("com.google.android.apps.maps")
                    }
                    if (intent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(intent)
                    } else {
                        // Fallback browser redirect
                        val webMapIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${item.punto.latitude},${item.punto.longitude}"))
                        context.startActivity(webMapIntent)
                    }
                },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .testTag("btn_directions_${item.punto.name.filter { it.isLetter() }}"),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                contentPadding = PaddingValues(0.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Directions,
                        contentDescription = "Route",
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        AppStrings.get("how_to_get_there", language),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }
}

// ======================== MODAL RESULT DIALOG ========================

@Composable
fun ResultDialog(
    result: GarbageSortingResult,
    language: String,
    onDismiss: () -> Unit
) {
    // Determine the corresponding waste container to style custom header colors
    val matchedBin = remember(result.container) {
        try {
            WasteContainer.valueOf(result.container.uppercase())
        } catch (e: Exception) {
            WasteContainer.GRIS
        }
    }
    val binColor = remember(matchedBin) { Color(android.graphics.Color.parseColor(matchedBin.colorHex)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(AppStrings.get("got_it_btn", language), fontWeight = FontWeight.Bold)
            }
        },
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Colored Header pill representing Madrid Container
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(binColor)
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = matchedBin.getName(language).uppercase(),
                        fontWeight = FontWeight.Black,
                        color = if (matchedBin == WasteContainer.AMARILLO) Color.Black else Color.White,
                        fontSize = 16.sp
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = result.itemName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Explanation Details
                Column {
                    Text(
                        text = AppStrings.get("classification_instructions_label", language),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = result.explanation,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Special treatment
                if (result.specialInstructions.trim().isNotEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Tip",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
							)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = result.specialInstructions,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Comunidad de Madrid directive policy
                Column {
                    Text(
                        text = AppStrings.get("madrid_policy_label", language),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = result.comunidadMadridPolicy,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
fun ResultErrorDialog(
    message: String,
    language: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(AppStrings.get("error_ok", language))
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.SentimentVeryDissatisfied,
                    contentDescription = "Error",
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(AppStrings.get("error_title", language))
            }
        },
        text = {
            Text(message)
        },
        shape = RoundedCornerShape(16.dp)
    )
}
