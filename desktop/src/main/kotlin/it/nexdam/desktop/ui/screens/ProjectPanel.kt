package it.nexdam.desktop.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.nexdam.desktop.data.models.Invoice
import it.nexdam.desktop.data.models.Project
import it.nexdam.desktop.data.models.ProjectFile
import it.nexdam.desktop.data.models.ProjectMessage
import it.nexdam.desktop.ui.theme.*
import it.nexdam.desktop.ui.viewmodels.AppViewModel

@Composable
fun ProjectPanel(project: Project, vm: AppViewModel, onClose: () -> Unit) {
    val sending by vm.sendingMessage.collectAsState()
    var messageText by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Messaggi", "File", "Fatture")
    val statusColor = when (project.status) { "in_progress" -> Primary; "completed" -> Success; else -> Warning }
    val statusLabel = when (project.status) { "in_progress" -> "In corso"; "completed" -> "Completato"; else -> "In attesa" }

    Column(Modifier.fillMaxSize()) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth().background(Surface).padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Indietro", tint = Muted, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(project.title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = OnBg)
                project.description?.let { Text(it, color = Muted, fontSize = 12.sp) }
            }
            Box(
                modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(statusColor.copy(alpha = 0.12f)).padding(horizontal = 12.dp, vertical = 6.dp)
            ) { Text(statusLabel, color = statusColor, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
        }

        HorizontalDivider(color = Divider, thickness = 0.5.dp)

        // Quick stats
        Row(modifier = Modifier.fillMaxWidth().background(SurfaceVar).padding(horizontal = 24.dp, vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(32.dp)) {
            QuickStat("Messaggi", project.messages.size.toString(), Primary)
            QuickStat("File", project.files.size.toString(), OnBg)
            QuickStat("Fatture", project.invoices.size.toString(), if (project.invoices.any { it.status == "pending" }) Warning else Muted)
            val total = project.invoices.sumOf { it.amount }
            if (total > 0) QuickStat("Totale", "€${"%.0f".format(total)}", Muted)
        }

        HorizontalDivider(color = Divider, thickness = 0.5.dp)

        // Tabs
        TabRow(selectedTabIndex = selectedTab, containerColor = Surface, contentColor = Primary) {
            tabs.forEachIndexed { i, title ->
                Tab(selected = selectedTab == i, onClick = { selectedTab = i }, text = { Text(title, fontSize = 13.sp) })
            }
        }

        when (selectedTab) {
            0 -> MessagesPanel(
                messages = project.messages,
                messageText = messageText,
                onMessageChange = { messageText = it },
                onSend = { vm.sendMessage(project.id, messageText); messageText = "" },
                sending = sending
            )
            1 -> FilesPanel(files = project.files)
            2 -> InvoicesPanel(invoices = project.invoices)
        }
    }
}

@Composable
fun QuickStat(label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Column {
        Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = color)
        Text(label, fontSize = 11.sp, color = Muted)
    }
}

@Composable
fun MessagesPanel(messages: List<ProjectMessage>, messageText: String, onMessageChange: (String) -> Unit, onSend: () -> Unit, sending: Boolean) {
    val listState = rememberLazyListState()
    LaunchedEffect(messages.size) { if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1) }

    Column(Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).padding(horizontal = 24.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (messages.isEmpty()) {
                item { Text("Nessun messaggio ancora.", color = Muted, modifier = Modifier.padding(top = 8.dp)) }
            }
            items(messages) { msg ->
                val isClient = !msg.isAdmin
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = if (isClient) Arrangement.End else Arrangement.Start) {
                    Box(
                        modifier = Modifier
                            .widthIn(max = 480.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isClient) Primary.copy(alpha = 0.18f) else SurfaceVar)
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Column {
                            if (!isClient) Text("NexDam", color = Primary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(msg.body ?: "", color = OnBg, fontSize = 14.sp)
                            msg.createdAt?.take(16)?.replace("T", " ")?.let {
                                Text(it, color = Muted, fontSize = 10.sp, modifier = Modifier.padding(top = 4.dp))
                            }
                        }
                    }
                }
            }
        }

        HorizontalDivider(color = Divider, thickness = 0.5.dp)
        Row(
            modifier = Modifier.fillMaxWidth().background(Surface).padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = messageText,
                onValueChange = onMessageChange,
                placeholder = { Text("Scrivi un messaggio… (Invio per inviare)", color = Muted) },
                modifier = Modifier.weight(1f).onKeyEvent {
                    if (it.type == KeyEventType.KeyUp && it.key == Key.Enter && !it.isShiftPressed) {
                        if (messageText.isNotBlank()) onSend(); true
                    } else false
                },
                colors = fieldColors(),
                maxLines = 4,
                shape = RoundedCornerShape(10.dp)
            )
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = onSend,
                enabled = messageText.isNotBlank() && !sending,
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.height(50.dp)
            ) {
                if (sending) CircularProgressIndicator(color = Background, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                else Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Invia")
            }
        }
    }
}

@Composable
fun FilesPanel(files: List<ProjectFile>) {
    val uriHandler = LocalUriHandler.current
    if (files.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.FolderOpen, contentDescription = null, tint = Muted, modifier = Modifier.size(48.dp))
                Spacer(Modifier.height(8.dp))
                Text("Nessun file disponibile.", color = Muted)
            }
        }
        return
    }
    LazyColumn(contentPadding = PaddingValues(24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(files) { file ->
            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Surface).padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.InsertDriveFile, contentDescription = null, tint = Primary, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(file.name, color = OnBg, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    file.sizeLabel?.let { Text(it, color = Muted, fontSize = 12.sp) }
                }
                file.url?.let { url ->
                    OutlinedButton(
                        onClick = { uriHandler.openUri(url) },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary),
                        shape = RoundedCornerShape(8.dp)
                    ) { Text("Scarica", fontSize = 13.sp) }
                }
            }
        }
    }
}

@Composable
fun InvoicesPanel(invoices: List<Invoice>) {
    if (invoices.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Receipt, contentDescription = null, tint = Muted, modifier = Modifier.size(48.dp))
                Spacer(Modifier.height(8.dp))
                Text("Nessuna fattura.", color = Muted)
            }
        }
        return
    }
    LazyColumn(contentPadding = PaddingValues(24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(invoices) { inv ->
            val isPaid = inv.status == "paid"
            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Surface).padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Receipt, contentDescription = null, tint = if (isPaid) Success else Warning, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    inv.description?.let { Text(it, color = OnBg, fontSize = 14.sp, fontWeight = FontWeight.Medium) }
                    inv.dueDate?.let { Text("Scadenza: $it", color = Muted, fontSize = 12.sp) }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("${inv.currency} ${"%.2f".format(inv.amount)}", color = OnBg, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Box(
                        modifier = Modifier.clip(RoundedCornerShape(6.dp)).background((if (isPaid) Success else Warning).copy(alpha = 0.12f)).padding(horizontal = 8.dp, vertical = 3.dp)
                    ) { Text(if (isPaid) "Pagata" else "In attesa", color = if (isPaid) Success else Warning, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}
