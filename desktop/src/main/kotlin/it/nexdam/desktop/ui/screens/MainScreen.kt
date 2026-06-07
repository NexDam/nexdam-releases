package it.nexdam.desktop.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.nexdam.desktop.data.models.Project
import it.nexdam.desktop.ui.theme.*
import it.nexdam.desktop.ui.viewmodels.AppViewModel

@Composable
fun MainScreen(vm: AppViewModel, onLogout: () -> Unit) {
    val projects by vm.projects.collectAsState()
    val profile by vm.profile.collectAsState()
    val selectedProject by vm.selectedProject.collectAsState()
    val loading by vm.loading.collectAsState()
    val error by vm.error.collectAsState()
    var showProfile by remember { mutableStateOf(false) }
    var showBlog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { vm.loadData() }

    Row(Modifier.fillMaxSize().background(Background)) {

        // ── SIDEBAR ──────────────────────────────────────────────
        Column(
            modifier = Modifier
                .width(260.dp)
                .fillMaxHeight()
                .background(Surface)
        ) {
            // Logo header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(Primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("ND", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Primary)
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("NexDam", fontWeight = FontWeight.ExtraBold, color = OnBg, fontSize = 15.sp)
                    Text("Client Portal", color = Muted, fontSize = 11.sp)
                }
            }

            HorizontalDivider(color = Divider, thickness = 0.5.dp)

            // Blog entry
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (showBlog) Primary.copy(alpha = 0.08f) else androidx.compose.ui.graphics.Color.Transparent)
                    .clickable { showBlog = true; vm.clearSelectedProject() }
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Article, contentDescription = null, tint = if (showBlog) Primary else Muted, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(10.dp))
                Text("Blog", color = if (showBlog) Primary else OnBg, fontSize = 13.sp, fontWeight = if (showBlog) FontWeight.SemiBold else FontWeight.Normal)
            }

            HorizontalDivider(color = Divider, thickness = 0.5.dp)
            Spacer(Modifier.height(8.dp))

            // Projects section
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("PROGETTI", fontSize = 10.sp, color = Muted, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { vm.loadData() }, modifier = Modifier.size(20.dp)) {
                    Icon(Icons.Default.Refresh, contentDescription = "Ricarica", tint = Muted, modifier = Modifier.size(14.dp))
                }
            }

            if (loading) {
                Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Primary, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                }
            } else if (error != null) {
                Text(error!!, color = Danger, fontSize = 12.sp, modifier = Modifier.padding(16.dp))
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    if (projects.isEmpty()) {
                        item {
                            Text("Nessun progetto", color = Muted, fontSize = 13.sp, modifier = Modifier.padding(16.dp))
                        }
                    }
                    items(projects) { project ->
                        SidebarProjectItem(
                            project = project,
                            isSelected = selectedProject?.id == project.id,
                            onClick = { vm.selectProject(project); showBlog = false }
                        )
                    }
                }
            }

            // Bottom: Profile button
            HorizontalDivider(color = Divider, thickness = 0.5.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showProfile = !showProfile }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val initials = (profile?.fullName ?: profile?.username ?: "ND")
                    .split(" ").take(2).mapNotNull { it.firstOrNull()?.uppercaseChar() }.joinToString("").ifEmpty { "ND" }
                Box(
                    modifier = Modifier.size(34.dp).clip(CircleShape).background(Primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(initials, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Primary)
                }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(profile?.fullName ?: profile?.username ?: "Client", color = OnBg, fontSize = 13.sp, fontWeight = FontWeight.Medium, maxLines = 1)
                    profile?.company?.let { Text(it, color = Muted, fontSize = 11.sp, maxLines = 1) }
                }
                IconButton(
                    onClick = { vm.logout(); onLogout() },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(Icons.Default.ExitToApp, contentDescription = "Esci", tint = Danger, modifier = Modifier.size(16.dp))
                }
            }
        }

        // Vertical divider
        Box(Modifier.width(1.dp).fillMaxHeight().background(Divider))

        // ── MAIN CONTENT ─────────────────────────────────────────
        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
            if (showProfile) {
                ProfilePanel(profile = profile, onClose = { showProfile = false })
            } else if (showBlog) {
                BlogPanel(onClose = { showBlog = false })
            } else if (selectedProject != null) {
                ProjectPanel(
                    project = selectedProject!!,
                    vm = vm,
                    onClose = { vm.clearSelectedProject() }
                )
            } else {
                DashboardOverview(projects = projects, profile = profile, onProjectClick = { vm.selectProject(it) })
            }
        }
    }
}

@Composable
fun SidebarProjectItem(project: Project, isSelected: Boolean, onClick: () -> Unit) {
    val statusColor = when (project.status) {
        "in_progress" -> Primary
        "completed" -> Success
        else -> Warning
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isSelected) Primary.copy(alpha = 0.08f) else androidx.compose.ui.graphics.Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(statusColor))
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(project.title, color = if (isSelected) Primary else OnBg, fontSize = 13.sp, fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                when (project.status) { "in_progress" -> "In corso" ; "completed" -> "Completato" ; else -> "In attesa" },
                color = Muted, fontSize = 11.sp
            )
        }
        if (project.messages.isNotEmpty()) {
            Box(
                modifier = Modifier.size(18.dp).clip(CircleShape).background(Primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(project.messages.size.toString(), color = Primary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun DashboardOverview(projects: List<Project>, profile: Any?, onProjectClick: (Project) -> Unit) {
    val inProgress = projects.count { it.status == "in_progress" }
    val completed = projects.count { it.status == "completed" }
    val totalMessages = projects.sumOf { it.messages.size }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(32.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Text("Panoramica", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = OnBg)
            Text("Tutti i tuoi progetti in un colpo d'occhio", fontSize = 14.sp, color = Muted)
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatCard("Progetti totali", projects.size.toString(), Icons.Default.Folder, OnBg, Modifier.weight(1f))
                StatCard("In corso", inProgress.toString(), Icons.Default.PlayArrow, Primary, Modifier.weight(1f))
                StatCard("Completati", completed.toString(), Icons.Default.CheckCircle, Success, Modifier.weight(1f))
                StatCard("Messaggi", totalMessages.toString(), Icons.Default.Message, Warning, Modifier.weight(1f))
            }
        }

        if (projects.isNotEmpty()) {
            item { Text("Progetti recenti", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = OnBg) }
            items(projects) { project ->
                ProjectRow(project = project, onClick = { onProjectClick(project) })
            }
        }
    }
}

@Composable
fun StatCard(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: androidx.compose.ui.graphics.Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Surface)
            .padding(20.dp)
    ) {
        Column {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
            Spacer(Modifier.height(12.dp))
            Text(value, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = color)
            Text(label, fontSize = 12.sp, color = Muted)
        }
    }
}

@Composable
fun ProjectRow(project: Project, onClick: () -> Unit) {
    val statusColor = when (project.status) { "in_progress" -> Primary; "completed" -> Success; else -> Warning }
    val statusLabel = when (project.status) { "in_progress" -> "In corso"; "completed" -> "Completato"; else -> "In attesa" }
    val unpaid = project.invoices.count { it.status == "pending" }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(statusColor))
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(project.title, color = OnBg, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            project.description?.let { Text(it, color = Muted, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) }
        }
        Spacer(Modifier.width(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp), verticalAlignment = Alignment.CenterVertically) {
            MiniStat(Icons.Default.Message, project.messages.size.toString())
            MiniStat(Icons.Default.AttachFile, project.files.size.toString())
            MiniStat(Icons.Default.Receipt, project.invoices.size.toString(), if (unpaid > 0) Warning else Muted)
        }
        Spacer(Modifier.width(16.dp))
        Box(
            modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(statusColor.copy(alpha = 0.12f)).padding(horizontal = 10.dp, vertical = 4.dp)
        ) { Text(statusLabel, color = statusColor, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
    }
}

@Composable
fun MiniStat(icon: androidx.compose.ui.graphics.vector.ImageVector, value: String, color: androidx.compose.ui.graphics.Color = Muted) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(4.dp))
        Text(value, color = color, fontSize = 12.sp)
    }
}
