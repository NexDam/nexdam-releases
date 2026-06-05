package it.nexdam.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import it.nexdam.app.data.models.Project
import it.nexdam.app.ui.theme.*
import it.nexdam.app.ui.viewmodels.DashboardUiState
import it.nexdam.app.ui.viewmodels.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onProjectClick: (String) -> Unit,
    onProfileClick: () -> Unit,
    vm: DashboardViewModel = viewModel()
) {
    val uiState by vm.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(7.dp))
                                .background(Primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("ND", fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = Primary)
                        }
                        Spacer(Modifier.width(8.dp))
                        Text("NexDam", color = OnBackground, fontWeight = FontWeight.Bold)
                    }
                },
                actions = {
                    IconButton(onClick = { vm.loadProjects() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Aggiorna", tint = Muted)
                    }
                    IconButton(onClick = onProfileClick) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = "Profilo", tint = Primary, modifier = Modifier.size(18.dp))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        },
        containerColor = Background
    ) { padding ->
        when (val s = uiState) {
            is DashboardUiState.Loading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Primary)
                        Spacer(Modifier.height(12.dp))
                        Text("Caricamento progetti…", color = Muted, fontSize = 13.sp)
                    }
                }
            }
            is DashboardUiState.Error -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                        Text("⚠️", fontSize = 40.sp)
                        Spacer(Modifier.height(12.dp))
                        Text(s.message, color = Error, textAlign = androidx.compose.ui.text.style.TextAlign.Center, fontSize = 14.sp)
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = { vm.loadProjects() },
                            colors = ButtonDefaults.buttonColors(containerColor = Primary)
                        ) { Text("Riprova") }
                    }
                }
            }
            is DashboardUiState.Success -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        // Greeting header
                        val displayName = s.profile?.fullName
                            ?: s.profile?.username
                            ?: "Client"
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Ciao, $displayName 👋", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = OnBackground)
                                Text(
                                    "${s.projects.size} progett${if (s.projects.size == 1) "o" else "i"} attiv${if (s.projects.size == 1) "o" else "i"}",
                                    fontSize = 13.sp,
                                    color = Muted
                                )
                            }
                        }
                    }

                    // Summary cards row
                    if (s.projects.isNotEmpty()) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                val inProgress = s.projects.count { it.status == "in_progress" }
                                val completed = s.projects.count { it.status == "completed" }
                                val totalMessages = s.projects.sumOf { it.messages.size }

                                SummaryCard("In corso", inProgress.toString(), Primary, Modifier.weight(1f))
                                SummaryCard("Completati", completed.toString(), Success, Modifier.weight(1f))
                                SummaryCard("Messaggi", totalMessages.toString(), Warning, Modifier.weight(1f))
                            }
                        }
                    }

                    if (s.projects.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 60.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("📋", fontSize = 48.sp)
                                    Spacer(Modifier.height(12.dp))
                                    Text("Nessun progetto", color = OnBackground, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                                    Text("I tuoi progetti appariranno qui", color = Muted, fontSize = 13.sp)
                                }
                            }
                        }
                    } else {
                        item {
                            Text(
                                "I TUOI PROGETTI",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Muted,
                                letterSpacing = 1.5.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                        items(s.projects) { project ->
                            ProjectCard(project = project, onClick = { onProjectClick(project.id) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryCard(label: String, value: String, color: androidx.compose.ui.graphics.Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.08f))
            .padding(12.dp)
    ) {
        Column {
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = color)
            Text(label, fontSize = 11.sp, color = color.copy(alpha = 0.8f))
        }
    }
}

@Composable
fun ProjectCard(project: Project, onClick: () -> Unit) {
    val unpaid = project.invoices.count { it.status == "pending" }
    val totalAmount = project.invoices.sumOf { it.amount }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Surface)
            .clickable(onClick = onClick)
    ) {
        // Left accent bar
        Box(
            modifier = Modifier
                .width(3.dp)
                .fillMaxHeight()
                .clip(RoundedCornerShape(topStart = 14.dp, bottomStart = 14.dp))
                .background(
                    when (project.status) {
                        "in_progress" -> Primary
                        "completed" -> Success
                        else -> Warning
                    }
                )
        )

        Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    project.title,
                    fontWeight = FontWeight.Bold,
                    color = OnBackground,
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                StatusBadge(project.status)
            }

            project.description?.let {
                Text(it, color = Muted, fontSize = 13.sp, modifier = Modifier.padding(top = 6.dp), maxLines = 2)
            }

            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = DividerColor, thickness = 0.5.dp)
            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                MetaStat(
                    label = "Messaggi",
                    value = project.messages.size.toString(),
                    valueColor = if (project.messages.isNotEmpty()) Primary else Muted
                )
                MetaStat(
                    label = "File",
                    value = project.files.size.toString(),
                    valueColor = if (project.files.isNotEmpty()) OnBackground else Muted
                )
                MetaStat(
                    label = "Fatture",
                    value = project.invoices.size.toString(),
                    valueColor = if (unpaid > 0) Warning else Muted
                )
                if (totalAmount > 0) {
                    val currency = project.invoices.firstOrNull()?.currency ?: "EUR"
                    MetaStat(
                        label = "Totale",
                        value = "€${"%.0f".format(totalAmount)}",
                        valueColor = Muted
                    )
                }
            }
        }
    }
}

@Composable
fun MetaStat(label: String, value: String, valueColor: androidx.compose.ui.graphics.Color) {
    Column {
        Text(value, fontWeight = FontWeight.Bold, color = valueColor, fontSize = 15.sp)
        Text(label, color = Muted, fontSize = 11.sp)
    }
}

@Composable
fun StatusBadge(status: String) {
    val (label, color) = when (status) {
        "in_progress" -> "In corso" to Primary
        "completed" -> "Completato" to Success
        else -> "In attesa" to Warning
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(label, color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}
