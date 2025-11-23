import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter
import kotlinx.coroutines.launch

// Shared state for the app
class AppState {
    var inputFilePath by mutableStateOf("")
    var inputFileContent by mutableStateOf("")
    var payrollResult by mutableStateOf<Payroll?>(null)
    var errorMessage by mutableStateOf<String?>(null)
    var isCalculating by mutableStateOf(false)
    val interactiveInputState = InteractiveInputState() // Hoist interactive input state here
}

@Composable
@Preview
fun App() {
    val appState = remember { AppState() }
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Kreator", "Plik", "Wyniki")

    MaterialTheme {
        Scaffold(
            topBar = {
                TabRow(selectedTabIndex = selectedTab) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title) }
                        )
                    }
                }
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding)) {
                when (selectedTab) {
                    0 -> InteractiveInputScreen(appState, appState.interactiveInputState) { selectedTab = 2 }
                    1 -> InputScreen(appState) { selectedTab = 2 }
                    2 -> ResultsScreen(appState)
                }
            }
        }
    }
}

@Composable
fun InputScreen(appState: AppState, onCalculated: () -> Unit) {
    val scope = rememberCoroutineScope()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Dane Wejściowe", style = MaterialTheme.typography.headlineMedium)
        
        // File selection
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Plik Wejściowy", style = MaterialTheme.typography.titleMedium)
                
                OutlinedTextField(
                    value = appState.inputFilePath,
                    onValueChange = { appState.inputFilePath = it },
                    label = { Text("Ścieżka Pliku") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    singleLine = true
                )
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            val fileChooser = JFileChooser()
                            fileChooser.fileFilter = FileNameExtensionFilter("Pliki tekstowe", "txt")
                            fileChooser.currentDirectory = File(".")
                            if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                                val file = fileChooser.selectedFile
                                appState.inputFilePath = file.absolutePath
                                appState.inputFileContent = file.readText()
                                appState.errorMessage = null
                            }
                        }
                    ) {
                        Text("Przeglądaj...")
                    }
                    
                    Button(
                        onClick = {
                            val defaultFile = File("wejscie.txt")
                            if (defaultFile.exists()) {
                                appState.inputFilePath = defaultFile.absolutePath
                                appState.inputFileContent = defaultFile.readText()
                                appState.errorMessage = null
                            } else {
                                appState.errorMessage = "Nie znaleziono pliku wejscie.txt w bieżącym katalogu"
                            }
                        }
                    ) {
                        Text("Wczytaj wejscie.txt")
                    }
                }
                
                if (appState.inputFileContent.isNotEmpty()) {
                    Text(
                        "✓ Plik wczytany (${appState.inputFileContent.lines().size} linii)",
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        
        // Error display
        if (appState.errorMessage != null) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    appState.errorMessage!!,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Calculate button
        Button(
            onClick = {
                scope.launch {
                    try {
                        appState.isCalculating = true
                        appState.errorMessage = null
                        
                        // Parse and calculate
                        val lines = appState.inputFileContent.lines()
                        val input = parsePayrollInput(lines)
                        val payroll = calculatePayroll(input)
                        
                        appState.payrollResult = payroll
                        appState.isCalculating = false
                        
                        // Switch to results tab
                        onCalculated()
                        
                    } catch (e: Exception) {
                        appState.errorMessage = "Błąd obliczania: ${e.message}"
                        appState.isCalculating = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = appState.inputFileContent.isNotEmpty() && !appState.isCalculating
        ) {
            if (appState.isCalculating) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(if (appState.isCalculating) "Obliczanie..." else "Oblicz Rozliczenie")
        }
    }
}

@Composable
fun ResultsScreen(appState: AppState) {
    val payroll = appState.payrollResult
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Wyniki Rozliczenia", style = MaterialTheme.typography.headlineMedium)
        
        if (payroll == null) {
            Card {
                Column(
                    modifier = Modifier.padding(32.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Brak wyników", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Wczytaj plik wejściowy i oblicz rozliczenie")
                }
            }
        } else {
            // Summary card
            Card {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Podsumowanie", style = MaterialTheme.typography.titleLarge)
                    Divider()
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Podatek w przedmiotach:")
                        Text(formatNumberWithSpacesAndK(payroll.itemsTaxTotal), style = MaterialTheme.typography.titleMedium)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Podatek w gotówce:")
                        Text(formatNumberWithSpacesAndK(payroll.cashTaxTotal), style = MaterialTheme.typography.titleMedium)
                    }
                    
                    Divider()
                    
                    val totalItems = payroll.participants.values.sumOf { p ->
                        p.itemsAfterTaxPerTabPerLocation.values.sumOf { it.values.sum() }
                    }
                    val totalCash = payroll.participants.values.sumOf { it.cashAfterTax }
                    val totalItemReturns = payroll.participants.values.sumOf { it.returnsFromItems }
                    val totalCashReturns = payroll.participants.values.sumOf { it.returnsFromCash }
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Suma wypłat w przedmiotach:")
                        Text(formatNumberWithSpacesAndK(totalItems))
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Suma wypłat w gotówce:")
                        Text(formatNumberWithSpacesAndK(totalCash))
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Suma zwrotów w przedmiotach:")
                        Text(formatNumberWithSpacesAndK(totalItemReturns))
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Suma zwrotów w gotówce:")
                        Text(formatNumberWithSpacesAndK(totalCashReturns))
                    }
                    
                    Divider()
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Uczestnicy:", style = MaterialTheme.typography.titleMedium)
                        Text("${payroll.participants.size}", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
            
            // Participants table
            Card {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Wypłaty Uczestników", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Table header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Nick", modifier = Modifier.weight(2f), style = MaterialTheme.typography.titleSmall)
                        Text("Gotówka", modifier = Modifier.weight(1.5f), style = MaterialTheme.typography.titleSmall)
                        Text("Zwroty", modifier = Modifier.weight(1.5f), style = MaterialTheme.typography.titleSmall)
                        Text("Punkty", modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleSmall)
                    }
                    
                    Divider()
                    
                    // Table rows
                    payroll.participants.values.sortedByDescending { 
                        it.cashAfterTax + it.returnsFromItems + it.returnsFromCash 
                    }.forEach { participant ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(participant.name, modifier = Modifier.weight(2f))
                            Text(formatNumberWithSpacesAndK(participant.cashAfterTax), modifier = Modifier.weight(1.5f))
                            Text(
                                formatNumberWithSpacesAndK(participant.returnsFromItems + participant.returnsFromCash),
                                modifier = Modifier.weight(1.5f)
                            )
                            Text(
                                String.format("%.2f", participant.returnPoints),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
            
            // Save button
            Button(
                onClick = {
                    try {
                        // TODO: Implement file saving
                        appState.errorMessage = "Zapisywanie plików nie zostało jeszcze zaimplementowane"
                    } catch (e: Exception) {
                        appState.errorMessage = "Błąd zapisu: ${e.message}"
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Zapisz Wyniki do Plików")
            }
        }
    }
}

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Albion Payroll Calculator v2.1.0",
        state = rememberWindowState(width = 1000.dp, height = 700.dp)
    ) {
        App()
    }
}
