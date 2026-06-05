package it.nexdam.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import it.nexdam.app.data.models.Invoice
import it.nexdam.app.data.models.ProjectFile
import it.nexdam.app.data.models.ProjectMessage
import it.nexdam.app.ui.theme.*
import it.nexdam.app.ui.viewmodels.ProjectUiState
import it.nexdam.app.ui.viewmodels.ProjectViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectScreen(
    projectId: String,
    onBack: () -> Unit,
    vm: ProjectViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return ProjectViewModel(projectId) as T
        }
    })
) {
    val uiState by vm.uiState.collectAsState()
    val sending by vm.sendingMessage.collectAsState()
    var messageText by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Messaggi", "File", "Fatture")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val title = (uiState as? ProjectUiState.Success)?.project?.title ?: "Progetto"
                    Text(title, color = OnBackground, fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro", tint = OnBackground)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        },
        containerColor = Background
    ) { padding ->
        when (val s = uiState) {
            is ProjectUiState.Loading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Primary)
                }
            }
            is ProjectUiState.Error -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(s.message, color = Error)
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { vm.loadProject() }) { Text("Riprova") }
                    }
                }
            }
            is ProjectUiState.Success -> {
                Column(Modifier.fillMaxSize().padding(padding)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Surface)
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Stato", color = Muted, fontSize = 13.sp)
                        StatusBadge(s.project.status)
                    }

                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Surface,
                        contentColor = Primary
                    ) {
                        tabs.forEachIndexed { i, title ->
                            Tab(
                                selected = selectedTab == i,
                                onClick = { selectedTab = i },
                                text = { Text(title, fontSize = 13.sp) }
                            )
                        }
                    }

                    when (selectedTab) {
                        0 -> MessagesTab(
                            messages = s.project.messages,
                            messageText = messageText,
                            onMessageChange = { messageText = it },
                            onSend = {
                                vm.sendMessage(messageText)
                                messageText = ""
                            },
                            sending = sending
                        )
                        1 -> FilesTab(files = s.project.files)
                        2 -> InvoicesTab(invoices = s.project.invoices)
                    }
                }
            }
        }
    }
}

@Composable
fun MessagesTab(
    messages: List<ProjectMessage>,
    messageText: String,
    onMessageChange: (String) -> Unit,
    onSend: () -> Unit,
    sending: Boolean
) {
    val listState = rememberLazyListState()
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    Column(Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (messages.isEmpty()) {
                item {
                    Text("Nessun messaggio.", color = Muted, modifier = Modifier.padding(top = 16.dp))
                }
            }
            items(messages) { msg ->
                val isClient = !msg.isAdmin
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isClient) Arrangement.End else Arrangement.Start
                ) {
                    Box(
                        modifier = Modifier
                            .widthIn(max = 280.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isClient) Primary.copy(alpha = 0.2f) else Surface)
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Column {
                            if (!isClient) {
                                Text("NexDam", color = Primary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Text(msg.body ?: "", color = OnBackground, fontSize = 14.sp)
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Surface)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = messageText,
                onValueChange = onMessageChange,
                placeholder = { Text("Scrivi un messaggio…", color = Muted) },
                modifier = Modifier.weight(1f),
                colors = nexDamTextFieldColors(),
                maxLines = 4
            )
            Spacer(Modifier.width(8.dp))
            IconButton(
                onClick = onSend,
                enabled = messageText.isNotBlank() && !sending
            ) {
                if (sending) {
                    CircularProgressIndicator(color = Primary, modifier = Modifier.size(20.dp))
                } else {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Invia", tint = Primary)
                }
            }
        }
    }
}

@Composable
fun FilesTab(files: List<ProjectFile>) {
    val uriHandler = LocalUriHandler.current

    if (files.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Nessun file disponibile.", color = Muted)
        }
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(files) { file ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Surface)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.AttachFile, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(file.name, color = OnBackground, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    file.sizeLabel?.let {
                        Text(it, color = Muted, fontSize = 12.sp)
                    }
                }
                TextButton(onClick = { file.url?.let { uriHandler.openUri(it) } }) {
                    Text("Scarica", color = Primary, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun InvoicesTab(invoices: List<Invoice>) {
    if (invoices.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Nessuna fattura.", color = Muted)
        }
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(invoices) { invoice ->
            val isPaid = invoice.status == "paid"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Surface)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Receipt,
                    contentDescription = null,
                    tint = if (isPaid) Success else Warning,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    invoice.description?.let {
                        Text(it, color = OnBackground, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                    invoice.dueDate?.let {
                        Text("Scadenza: $it", color = Muted, fontSize = 12.sp)
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "${invoice.currency} ${invoice.amount}",
                        color = OnBackground,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        if (isPaid) "Pagata" else "In attesa",
                        color = if (isPaid) Success else Warning,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
