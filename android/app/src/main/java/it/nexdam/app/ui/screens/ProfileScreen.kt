package it.nexdam.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import it.nexdam.app.data.supabase
import it.nexdam.app.ui.theme.*
import it.nexdam.app.ui.viewmodels.ProfileViewModel
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    vm: ProfileViewModel = viewModel()
) {
    val scope = rememberCoroutineScope()
    val profile by vm.profile.collectAsState()
    val authUser = supabase.auth.currentUserOrNull()
    var showLogoutDialog by remember { mutableStateOf(false) }

    val displayName = profile?.fullName ?: profile?.username ?: authUser?.email?.substringBefore("@") ?: "Client"
    val initials = displayName.split(" ")
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .joinToString("")
        .ifEmpty { "ND" }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Esci dall'account", color = OnBackground, fontWeight = FontWeight.Bold) },
            text = { Text("Sei sicuro di voler uscire? Dovrai effettuare di nuovo il login.", color = Muted) },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        scope.launch {
                            supabase.auth.signOut()
                            onLogout()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Error)
                ) { Text("Esci", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Annulla", color = Muted)
                }
            },
            containerColor = Surface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profilo", color = OnBackground, fontWeight = FontWeight.Bold) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))

            // Avatar con iniziali
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(Primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(initials, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Primary)
            }

            Spacer(Modifier.height(12.dp))
            Text(displayName, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = OnBackground)
            Text(authUser?.email ?: "", fontSize = 13.sp, color = Muted)

            profile?.company?.let {
                Spacer(Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Primary.copy(alpha = 0.1f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(it, color = Primary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(Modifier.height(28.dp))

            // Info card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Surface)
                    .padding(4.dp)
            ) {
                Column {
                    ProfileInfoRow(icon = Icons.Default.Email, label = "Email", value = authUser?.email ?: "—")
                    HorizontalDivider(color = DividerColor, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    profile?.fullName?.let {
                        ProfileInfoRow(icon = Icons.Default.Person, label = "Nome completo", value = it)
                        HorizontalDivider(color = DividerColor, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    }
                    profile?.company?.let {
                        ProfileInfoRow(icon = Icons.Default.Business, label = "Azienda", value = it)
                        HorizontalDivider(color = DividerColor, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    }
                    profile?.phone?.let {
                        ProfileInfoRow(icon = Icons.Default.Phone, label = "Telefono", value = it)
                        HorizontalDivider(color = DividerColor, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    }
                    profile?.role?.let {
                        ProfileInfoRow(icon = Icons.Default.Badge, label = "Ruolo", value = it)
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            // Logout button
            Button(
                onClick = { showLogoutDialog = true },
                modifier = Modifier.fillMaxWidth().height(52.dp).padding(bottom = 4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Error.copy(alpha = 0.12f), contentColor = Error),
                shape = RoundedCornerShape(12.dp),
                elevation = ButtonDefaults.buttonElevation(0.dp)
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Esci dall'account", fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun ProfileInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Primary, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(label, color = Muted, fontSize = 11.sp)
            Text(value, color = OnBackground, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}
