import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)

// Data classes for interactive input with observable state
class HaulData {
    var itemsK by mutableStateOf("")
    var cashK by mutableStateOf("")
    var location by mutableStateOf("BEACH")
    var tab by mutableStateOf("1")
    var hadOrganizer by mutableStateOf(false)
    var caller by mutableStateOf("")
    var participants by mutableStateOf("")
    var isEditing by mutableStateOf(true) // New hauls start in edit mode
    var isConfirmed by mutableStateOf(false) // Track if haul was confirmed
}

class ContentData(val id: Int) {
    var organizer by mutableStateOf("")
    val hauls = mutableStateListOf<HaulData>()
    var isEditing by mutableStateOf(true)
    var isConfirmed by mutableStateOf(false)
}

class CtaData(val id: Int) {
    var caller by mutableStateOf("")
    var participants by mutableStateOf("")
    var isEditing by mutableStateOf(true)
    var isConfirmed by mutableStateOf(false)
}

class RecruitmentData {
    var recruiter by mutableStateOf("")
    var points by mutableStateOf("")
}

class InteractiveInputState {
    val contents = mutableStateListOf<ContentData>()
    val ctas = mutableStateListOf<CtaData>()
    val recruitments = mutableStateListOf<RecruitmentData>()
    val customLocations = mutableStateListOf<String>() // Custom locations added by user
    
    fun generateInputText(): String {
        val sb = StringBuilder()
        
        // Contents section - always required
        sb.appendLine("KONTENTY:")
        val validContents = contents.filter { it.hauls.isNotEmpty() }
        validContents.forEach { content ->
            sb.append(content.id)
            if (content.organizer.isNotBlank()) {
                sb.append(": ${content.organizer}")
            } else {
                sb.append(":")
            }
            sb.appendLine()
            
            content.hauls.forEachIndexed { index, haul ->
                sb.append("  ${index + 1}: ")
                sb.append("${haul.itemsK}, ${haul.cashK}, ")
                sb.append("${haul.location}, ${haul.tab}, ")
                sb.append("${if (haul.hadOrganizer) "TAK" else "NIE"}, ")
                sb.append("${haul.caller}, ")
                sb.appendLine(haul.participants)
            }
        }
        
        // CTA section - always add header, even if empty
        sb.appendLine()
        sb.appendLine("CTA:")
        ctas.forEach { cta ->
            sb.appendLine("${cta.id}: ${cta.caller} - ${cta.participants}")
        }
        
        // Recruitment section - always add header, even if empty
        sb.appendLine()
        sb.appendLine("REKRUTACJA:")
        recruitments.forEach { rec ->
            sb.appendLine("${rec.recruiter}: ${rec.points}")
        }
        
        return sb.toString()
    }
}

@Composable
fun InteractiveInputScreen(appState: AppState, inputState: InteractiveInputState, onCalculated: () -> Unit) {
    var selectedSection by remember { mutableStateOf(0) }
    val sections = listOf("Kontenty", "CTA", "Rekrutacja")
    
    Column(modifier = Modifier.fillMaxSize()) {
        // Section tabs
        TabRow(selectedTabIndex = selectedSection) {
            sections.forEachIndexed { index, title ->
                Tab(
                    selected = selectedSection == index,
                    onClick = { selectedSection = index },
                    text = { Text(title) }
                )
            }
        }
        
        // Content area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (selectedSection) {
                0 -> ContentsSection(inputState)
                1 -> CtaSection(inputState)
                2 -> RecruitmentSection(inputState)
            }
        }
        
        // Bottom action bar
        Surface(
            tonalElevation = 3.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Load example button
                if (inputState.contents.isEmpty() && inputState.ctas.isEmpty() && inputState.recruitments.isEmpty()) {
                    OutlinedButton(
                        onClick = {
                            // Load example data
                            val content = ContentData(id = 1)
                            content.organizer = ""
                            val haul = HaulData()
                            haul.itemsK = "3770"
                            haul.cashK = "0"
                            haul.location = "BEACH"
                            haul.tab = "1"
                            haul.hadOrganizer = false
                            haul.caller = ""
                            haul.participants = "orzech, yuegre, arkadius, koniu"
                            content.hauls.add(haul)
                            inputState.contents.add(content)
                            
                            val cta = CtaData(id = 1)
                            cta.caller = "sziba"
                            cta.participants = "karoll, han, golon, juri"
                            inputState.ctas.add(cta)
                            
                            val recruitment = RecruitmentData()
                            recruitment.recruiter = "kacper17"
                            recruitment.points = "6"
                            inputState.recruitments.add(recruitment)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Załaduj przykładowe dane")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            appState.inputFileContent = inputState.generateInputText()
                            appState.inputFilePath = "(Utworzono interaktywnie)"
                            appState.errorMessage = null
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Użyj tego wejścia")
                    }
                    
                    Button(
                        onClick = {
                            try {
                                // Validate input - at least one content with hauls is required
                                val validContents = inputState.contents.filter { it.hauls.isNotEmpty() }
                                if (validContents.isEmpty()) {
                                    appState.errorMessage = "Wymagany jest przynajmniej jeden kontent ze zwózką.\n\nCTA i Rekrutacja są opcjonalne."
                                    return@Button
                                }
                                
                                val generatedText = inputState.generateInputText()
                                if (generatedText.isBlank()) {
                                    appState.errorMessage = "Nie można wygenerować danych wejściowych. Sprawdź czy wszystkie pola są wypełnione."
                                    return@Button
                                }
                                
                                appState.inputFileContent = generatedText
                                appState.inputFilePath = "(Utworzono interaktywnie)"
                                
                                // Debug: print generated text
                                println("=== Generated Input ===")
                                println(generatedText)
                                println("======================")
                                
                                val lines = generatedText.lines()
                                val input = parsePayrollInput(lines)
                                val payroll = calculatePayroll(input)
                                appState.payrollResult = payroll
                                appState.errorMessage = null
                                onCalculated()
                            } catch (e: Exception) {
                                appState.errorMessage = "Błąd obliczania: ${e.message}\n\nSprawdź czy wszystkie pola są wypełnione poprawnie."
                                e.printStackTrace()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Oblicz")
                    }
                }
            }
        }
    }
}

@Composable
fun ContentsSection(state: InteractiveInputState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val hasEditingContent = state.contents.any { it.isEditing }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Kontenty", style = MaterialTheme.typography.headlineSmall)
            Button(
                onClick = {
                    state.contents.add(ContentData(id = state.contents.size + 1))
                },
                enabled = !hasEditingContent
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Dodaj Kontent")
            }
        }
        
        if (state.contents.isEmpty()) {
            Card {
                Box(
                    modifier = Modifier.padding(32.dp).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Brak kontentów. Kliknij 'Dodaj Kontent' aby rozpocząć.")
                }
            }
        }
        
        state.contents.forEachIndexed { index, content ->
            ContentCard(content, state, onDelete = { state.contents.removeAt(index) })
        }
    }
}

@Composable
fun ContentCard(content: ContentData, state: InteractiveInputState, onDelete: () -> Unit) {
    var expanded by remember { mutableStateOf(true) }
    val hasEditingHaul = content.hauls.any { it.isEditing }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (content.isEditing) 
                MaterialTheme.colorScheme.surfaceVariant 
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Kontent ${content.id}" + if (!content.isEditing) " ✓" else "",
                    style = MaterialTheme.typography.titleMedium
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(
                            if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null
                        )
                    }
                    if (!content.isEditing) {
                        IconButton(onClick = { content.isEditing = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edytuj")
                        }
                    } else {
                        IconButton(
                            onClick = {
                                content.isEditing = false
                                content.isConfirmed = true
                            },
                            enabled = !hasEditingHaul // Can't save if hauls are being edited
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "Zapisz")
                        }
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Usuń")
                    }
                }
            }
            
            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = content.organizer,
                    onValueChange = { content.organizer = it },
                    label = { Text("Organizator (opcjonalnie)") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("np. smokq, fenix") },
                    enabled = content.isEditing,
                    readOnly = !content.isEditing
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Zwózki", style = MaterialTheme.typography.titleSmall)
                    Button(
                        onClick = { content.hauls.add(HaulData()) },
                        modifier = Modifier.height(32.dp),
                        enabled = content.isEditing && !hasEditingHaul // Can't add if editing another haul
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Dodaj Zwózkę", style = MaterialTheme.typography.bodySmall)
                    }
                }
                
                content.hauls.forEachIndexed { haulIndex, haul ->
                    Spacer(modifier = Modifier.height(8.dp))
                    HaulCard(haul, haulIndex + 1, state, onDelete = { content.hauls.removeAt(haulIndex) })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HaulCard(haul: HaulData, number: Int, state: InteractiveInputState, onDelete: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (haul.isEditing) 
                MaterialTheme.colorScheme.surfaceVariant 
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Zwózka $number" + if (!haul.isEditing) " ✓" else "",
                    style = MaterialTheme.typography.labelLarge
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (!haul.isEditing) {
                        IconButton(
                            onClick = { haul.isEditing = true },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edytuj", modifier = Modifier.size(16.dp))
                        }
                    } else {
                        IconButton(
                            onClick = {
                                // Save and add location to custom list if needed
                                if (haul.location.isNotBlank() && 
                                    !listOf("BEACH", "FORT STERLING", "LYMHURST").contains(haul.location) &&
                                    !state.customLocations.contains(haul.location)) {
                                    state.customLocations.add(haul.location)
                                }
                                haul.isEditing = false
                                haul.isConfirmed = true
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "Zapisz", modifier = Modifier.size(16.dp))
                        }
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Usuń", modifier = Modifier.size(16.dp))
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = haul.itemsK,
                    onValueChange = { haul.itemsK = it },
                    label = { Text("Przedmioty (k)") },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("np. 3770") },
                    enabled = haul.isEditing,
                    readOnly = !haul.isEditing
                )
                OutlinedTextField(
                    value = haul.cashK,
                    onValueChange = { haul.cashK = it },
                    label = { Text("Gotówka (k)") },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("np. 0") },
                    enabled = haul.isEditing,
                    readOnly = !haul.isEditing
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                var locationExpanded by remember { mutableStateOf(false) }
                val defaultLocations = listOf("BEACH", "FORT STERLING", "LYMHURST")
                val allLocations = defaultLocations + state.customLocations
                
                ExposedDropdownMenuBox(
                    expanded = locationExpanded && haul.isEditing,
                    onExpandedChange = { if (haul.isEditing) locationExpanded = it },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = haul.location,
                        onValueChange = { newValue ->
                            if (haul.isEditing) {
                                haul.location = newValue
                            }
                        },
                        label = { Text("Lokacja") },
                        trailingIcon = { 
                            if (haul.isEditing) {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = locationExpanded)
                            }
                        },
                        modifier = Modifier.menuAnchor(),
                        placeholder = { Text("Wybierz lub wpisz") },
                        enabled = haul.isEditing,
                        readOnly = !haul.isEditing
                    )
                    ExposedDropdownMenu(
                        expanded = locationExpanded,
                        onDismissRequest = { locationExpanded = false }
                    ) {
                        allLocations.forEach { location ->
                            DropdownMenuItem(
                                text = { Text(location) },
                                onClick = {
                                    haul.location = location
                                    locationExpanded = false
                                }
                            )
                        }
                    }
                }
                
                OutlinedTextField(
                    value = haul.tab,
                    onValueChange = { haul.tab = it },
                    label = { Text("Tab") },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("np. 1") },
                    enabled = haul.isEditing,
                    readOnly = !haul.isEditing
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = haul.hadOrganizer,
                    onCheckedChange = { if (haul.isEditing) haul.hadOrganizer = it },
                    enabled = haul.isEditing
                )
                Text(
                    "Organizator był obecny",
                    color = if (haul.isEditing) 
                        MaterialTheme.colorScheme.onSurface 
                    else 
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = haul.caller,
                onValueChange = { haul.caller = it },
                label = { Text("Caller (opcjonalnie)") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("np. karoll lub karoll(il)") },
                enabled = haul.isEditing,
                readOnly = !haul.isEditing
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = haul.participants,
                onValueChange = { haul.participants = it },
                label = { Text("Uczestnicy (oddzieleni przecinkami)") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("np. orzech, yuegre, arkadius, koniu") },
                minLines = 2,
                enabled = haul.isEditing,
                readOnly = !haul.isEditing
            )
        }
    }
}

@Composable
fun CtaSection(state: InteractiveInputState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val hasEditingCta = state.ctas.any { it.isEditing }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("CTA", style = MaterialTheme.typography.headlineSmall)
            Button(
                onClick = {
                    state.ctas.add(CtaData(id = state.ctas.size + 1))
                },
                enabled = !hasEditingCta
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Dodaj CTA")
            }
        }
        
        if (state.ctas.isEmpty()) {
            Card {
                Box(
                    modifier = Modifier.padding(32.dp).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Brak CTA. Kliknij 'Dodaj CTA' aby rozpocząć.")
                }
            }
        }
        
        state.ctas.forEachIndexed { index, cta ->
            CtaCard(cta, onDelete = { state.ctas.removeAt(index) })
        }
    }
}

@Composable
fun CtaCard(cta: CtaData, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (cta.isEditing) 
                MaterialTheme.colorScheme.surfaceVariant 
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "CTA ${cta.id}" + if (!cta.isEditing) " ✓" else "",
                    style = MaterialTheme.typography.titleMedium
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (!cta.isEditing) {
                        IconButton(onClick = { cta.isEditing = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edytuj")
                        }
                    } else {
                        IconButton(
                            onClick = {
                                cta.isEditing = false
                                cta.isConfirmed = true
                            }
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "Zapisz")
                        }
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Usuń")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = cta.caller,
                onValueChange = { cta.caller = it },
                label = { Text("Caller") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("np. sziba") },
                enabled = cta.isEditing,
                readOnly = !cta.isEditing
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = cta.participants,
                onValueChange = { cta.participants = it },
                label = { Text("Uczestnicy (oddzieleni przecinkami)") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("np. karoll, han, golon, juri") },
                minLines = 2,
                enabled = cta.isEditing,
                readOnly = !cta.isEditing
            )
        }
    }
}

@Composable
fun RecruitmentSection(state: InteractiveInputState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Rekrutacja", style = MaterialTheme.typography.headlineSmall)
            Button(
                onClick = {
                    state.recruitments.add(RecruitmentData())
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Dodaj Rekrutację")
            }
        }
        
        if (state.recruitments.isEmpty()) {
            Card {
                Box(
                    modifier = Modifier.padding(32.dp).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Brak rekrutacji. Kliknij 'Dodaj Rekrutację' aby rozpocząć.")
                }
            }
        }
        
        state.recruitments.forEachIndexed { index, recruitment ->
            RecruitmentCard(recruitment, onDelete = { state.recruitments.removeAt(index) })
        }
    }
}

@Composable
fun RecruitmentCard(recruitment: RecruitmentData, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = recruitment.recruiter,
                onValueChange = { recruitment.recruiter = it },
                label = { Text("Rekruter") },
                modifier = Modifier.weight(2f),
                placeholder = { Text("np. kacper17") }
            )
            
            OutlinedTextField(
                value = recruitment.points,
                onValueChange = { recruitment.points = it },
                label = { Text("Punkty") },
                modifier = Modifier.weight(1f),
                placeholder = { Text("np. 6") }
            )
            
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = null)
            }
        }
    }
}
