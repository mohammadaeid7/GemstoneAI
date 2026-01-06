package com.example.gemstoneai.ui

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.gemstoneai.R
import com.example.gemstoneai.ai.*
import com.example.gemstoneai.data.HistoryEntry
import com.example.gemstoneai.data.UserPrefsRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private object Routes {
    const val ANALYZE = "analyze"
    const val HISTORY = "history"
    const val SETTINGS = "settings"
}

@Composable
fun GemstoneApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: Routes.ANALYZE

    MaterialTheme {
        Scaffold(
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentRoute == Routes.ANALYZE,
                        onClick = {
                            navController.navigate(Routes.ANALYZE) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Default.Search, contentDescription = null) },
                        label = { Text(stringResource(id = R.string.tab_analyze)) }
                    )
                    NavigationBarItem(
                        selected = currentRoute == Routes.HISTORY,
                        onClick = {
                            navController.navigate(Routes.HISTORY) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Default.History, contentDescription = null) },
                        label = { Text(stringResource(id = R.string.tab_history)) }
                    )
                    NavigationBarItem(
                        selected = currentRoute == Routes.SETTINGS,
                        onClick = {
                            navController.navigate(Routes.SETTINGS) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                        label = { Text(stringResource(id = R.string.tab_settings)) }
                    )
                }
            }
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = Routes.ANALYZE,
                modifier = Modifier.padding(padding)
            ) {
                composable(Routes.ANALYZE) { AnalyzeScreen() }
                composable(Routes.HISTORY) { HistoryScreen() }
                composable(Routes.SETTINGS) { SettingsScreen() }
            }
        }
    }
}

@Composable
private fun AnalyzeScreen() {
    val context = LocalContext.current
    val repo = remember { UserPrefsRepository(context) }
    val currency by repo.currencyFlow.collectAsState(initial = "USD")
    val rateToUsd by repo.rateToUsdFlow.collectAsState(initial = 1.0)

    val scope = rememberCoroutineScope()
    val analyzer = remember { GemAnalyzer() }

    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isAnalyzing by remember { mutableStateOf(false) }
    var analysisResult by remember { mutableStateOf<AnalysisResult?>(null) }
    var selectedGemType by remember { mutableStateOf(GemType.UNKNOWN) }

    var weightText by remember { mutableStateOf("") }
    var quality by remember { mutableStateOf(QualityGrade.B) }
    var purity by remember { mutableStateOf(PurityGrade.VS) }

    var priceEstimate by remember { mutableStateOf<PriceEstimate?>(null) }
    var errorText by remember { mutableStateOf<String?>(null) }

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val bmp = loadBitmapFromUri(context, uri)
        bitmap = bmp
        analysisResult = null
        priceEstimate = null
        errorText = null

        if (bmp != null) {
            scope.launch {
                isAnalyzing = true
                runCatching {
                    analyzer.analyze(bmp)
                }.onSuccess { result ->
                    analysisResult = result
                    selectedGemType = inferGemType(result)
                }.onFailure { ex ->
                    errorText = ex.message ?: "فشل التحليل"
                }
                isAnalyzing = false
            }
        }
    }

    val takePhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bmp ->
        bitmap = bmp
        analysisResult = null
        priceEstimate = null
        errorText = null

        if (bmp != null) {
            scope.launch {
                isAnalyzing = true
                runCatching {
                    analyzer.analyze(bmp)
                }.onSuccess { result ->
                    analysisResult = result
                    selectedGemType = inferGemType(result)
                }.onFailure { ex ->
                    errorText = ex.message ?: "فشل التحليل"
                }
                isAnalyzing = false
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "التعرّف على الأحجار الكريمة وتقدير قيمتها",
                style = MaterialTheme.typography.titleLarge
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = { takePhotoLauncher.launch(null) }) {
                    Text(stringResource(id = R.string.take_photo))
                }
                OutlinedButton(onClick = {
                    pickImageLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }) {
                    Text(stringResource(id = R.string.pick_from_gallery))
                }
            }
        }

        item {
            if (bitmap == null) {
                Text(stringResource(id = R.string.no_image))
            } else {
                Image(
                    bitmap = bitmap!!.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                )
            }
        }

        item {
            if (isAnalyzing) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            errorText?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }
        }

        item {
            AnalysisPanel(
                analysisResult = analysisResult,
                selectedGemType = selectedGemType,
                onGemTypeSelected = { selectedGemType = it }
            )
        }

        item {
            OutlinedTextField(
                value = weightText,
                onValueChange = { weightText = it },
                label = { Text(stringResource(id = R.string.weight_carat)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                DropdownField(
                    title = stringResource(id = R.string.quality),
                    value = quality.label,
                    options = QualityGrade.values().map { it.label },
                    onSelect = { picked -> quality = QualityGrade.values().first { it.label == picked } },
                    modifier = Modifier.weight(1f)
                )
                DropdownField(
                    title = stringResource(id = R.string.purity),
                    value = purity.label,
                    options = PurityGrade.values().map { it.label },
                    onSelect = { picked -> purity = PurityGrade.values().first { it.label == picked } },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Button(
                onClick = {
                    val weight = weightText.replace(",", ".").toDoubleOrNull()
                    if (weight == null || weight <= 0) {
                        errorText = "أدخل وزن صحيح بالقيراط"
                        return@Button
                    }
                    errorText = null
                    val estimate = PriceEstimator.estimate(
                        gemType = selectedGemType,
                        weightCarat = weight,
                        quality = quality,
                        purity = purity,
                        currency = currency,
                        currencyToUsd = rateToUsd
                    )
                    priceEstimate = estimate

                    // Save to history
                    scope.launch {
                        repo.addHistory(
                            HistoryEntry(
                                timestampMillis = System.currentTimeMillis(),
                                gemTypeAr = selectedGemType.displayNameAr,
                                weightCarat = weight,
                                quality = quality.label,
                                purity = purity.label,
                                currency = estimate.currency,
                                estimatedValue = estimate.amount,
                                perCarat = estimate.perCarat
                            )
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(id = R.string.estimate_price))
            }
        }

        item {
            priceEstimate?.let { pe ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(stringResource(id = R.string.estimated_value), style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = "${formatMoney(pe.amount)} ${pe.currency}",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text(text = "سعر/قيراط: ${formatMoney(pe.perCarat)} ${pe.currency}")
                        Spacer(Modifier.height(6.dp))
                        Text(stringResource(id = R.string.disclaimer), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun AnalysisPanel(
    analysisResult: AnalysisResult?,
    selectedGemType: GemType,
    onGemTypeSelected: (GemType) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("نتيجة الذكاء الاصطناعي (ML Kit)", style = MaterialTheme.typography.titleMedium)

            if (analysisResult == null) {
                Text("بعد اختيار صورة، سيظهر هنا أفضل الوسوم (Labels) مع نسبة الثقة.")
            } else {
                val top5 = analysisResult.predictions.take(5)
                if (top5.isEmpty()) {
                    Text("لم يتم استخراج وسوم كافية.")
                } else {
                    top5.forEach { p ->
                        Text("• ${p.text} (${(p.confidence * 100).toInt()}%)")
                    }
                }
            }

            Divider()

            Text(stringResource(id = R.string.gem_type), style = MaterialTheme.typography.titleSmall)
            DropdownField(
                title = "اختيار",
                value = selectedGemType.displayNameAr,
                options = GemType.values().map { it.displayNameAr },
                onSelect = { pickedAr ->
                    onGemTypeSelected(GemType.values().first { it.displayNameAr == pickedAr })
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun DropdownField(
    title: String,
    value: String,
    options: List<String>,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    Column(modifier = modifier) {
        Text(title, style = MaterialTheme.typography.labelLarge)
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(value, modifier = Modifier.weight(1f))
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { opt ->
                DropdownMenuItem(
                    text = { Text(opt) },
                    onClick = {
                        expanded = false
                        onSelect(opt)
                    }
                )
            }
        }
    }
}

@Composable
private fun HistoryScreen() {
    val context = LocalContext.current
    val repo = remember { UserPrefsRepository(context) }
    val history by repo.historyFlow.collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text("السجل", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
            TextButton(onClick = { scope.launch { repo.clearHistory() } }) {
                Text("مسح")
            }
        }

        if (history.isEmpty()) {
            Text("لا توجد عمليات حفظ بعد.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(history) { entry ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(entry.gemTypeAr, style = MaterialTheme.typography.titleMedium)
                            Text("الوزن: ${entry.weightCarat} قيراط | الجودة: ${entry.quality} | النقاء: ${entry.purity}")
                            Text("القيمة: ${formatMoney(entry.estimatedValue)} ${entry.currency} (لكل قيراط: ${formatMoney(entry.perCarat)} ${entry.currency})")
                            Text(dateFormat.format(Date(entry.timestampMillis)), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsScreen() {
    val context = LocalContext.current
    val repo = remember { UserPrefsRepository(context) }
    val currency by repo.currencyFlow.collectAsState(initial = "USD")
    val rateToUsd by repo.rateToUsdFlow.collectAsState(initial = 1.0)
    val scope = rememberCoroutineScope()

    var currencyLocal by remember(currency) { mutableStateOf(currency) }
    var rateLocal by remember(rateToUsd) { mutableStateOf(rateToUsd.toString()) }
    var savedMsg by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(stringResource(id = R.string.tab_settings), style = MaterialTheme.typography.titleLarge)

        DropdownField(
            title = stringResource(id = R.string.currency),
            value = currencyLocal,
            options = listOf("USD", "EUR", "SAR"),
            onSelect = { currencyLocal = it },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = rateLocal,
            onValueChange = { rateLocal = it },
            label = { Text(stringResource(id = R.string.rate_to_usd) + " (1 $currencyLocal = ? USD)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                val rate = rateLocal.replace(",", ".").toDoubleOrNull()
                if (rate == null || rate <= 0) {
                    savedMsg = "أدخل سعر تحويل صحيح"
                    return@Button
                }
                scope.launch {
                    repo.saveCurrency(currencyLocal)
                    repo.saveRateToUsd(rate)
                    savedMsg = "تم الحفظ"
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(id = R.string.save))
        }

        savedMsg?.let { Text(it) }

        Divider()
        Text(
            "معلومة: التطبيق يعمل بدون إنترنت. لتحسين دقة التعرف على الأحجار الكريمة فعلياً، ستحتاج نموذج تدريب مخصص للأحجار وصور عالية الجودة.",
            style = MaterialTheme.typography.bodySmall
        )
    }
}

private fun inferGemType(result: AnalysisResult): GemType {
    val all = result.predictions.joinToString(" ") { it.text.lowercase() }

    // Heuristics: ML Kit default labeler is generic, so we map common terms.
    return when {
        "diamond" in all || "gem" in all || "jewelry" in all || "jewellery" in all -> GemType.DIAMOND
        "ruby" in all -> GemType.RUBY
        "sapphire" in all -> GemType.SAPPHIRE
        "emerald" in all -> GemType.EMERALD
        "amethyst" in all -> GemType.AMETHYST
        "opal" in all -> GemType.OPAL
        "topaz" in all -> GemType.TOPAZ
        else -> GemType.UNKNOWN
    }
}

private fun formatMoney(v: Double): String = String.format(Locale.US, "%.2f", v)
