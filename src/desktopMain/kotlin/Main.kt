import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

@Composable
@Preview
fun App() {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Input", "Results", "History")

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
                    0 -> InputScreen()
                    1 -> ResultsScreen()
                    2 -> HistoryScreen()
                }
            }
        }
    }
}

@Composable
fun InputScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Payroll Input", style = MaterialTheme.typography.headlineMedium)
        
        OutlinedTextField(
            value = "",
            onValueChange = {},
            label = { Text("Input File Path") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Button(
            onClick = { /* TODO: Load file */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Load Input File")
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        Button(
            onClick = { /* TODO: Calculate */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Calculate Payroll")
        }
    }
}

@Composable
fun ResultsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Payroll Results", style = MaterialTheme.typography.headlineMedium)
        Text("Results will appear here after calculation")
    }
}

@Composable
fun HistoryScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Payroll History", style = MaterialTheme.typography.headlineMedium)
        Text("Previous payroll files will be listed here")
    }
}

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Albion Payroll Calculator v2.0.0",
        state = rememberWindowState(width = 1000.dp, height = 700.dp)
    ) {
        App()
    }
}
