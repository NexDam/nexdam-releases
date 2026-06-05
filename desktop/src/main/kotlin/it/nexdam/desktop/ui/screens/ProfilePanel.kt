package it.nexdam.desktop.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.nexdam.desktop.data.models.Profile
import it.nexdam.desktop.data.supabase
import it.nexdam.desktop.ui.theme.*
import io.github.jan.supabase.auth.auth

@Composable
fun ProfilePanel(profile: Profile?, onClose: () -> Unit) {
    val authUser = supabase.auth.currentUserOrNull()
    val displayName = profile?.fullName ?: profile?.username ?: authUser?.email?.substringBefore("@") ?: "Client"
    val initials = displayName.split(" ").take(2).mapNotNull { it.firstOrNull()?.uppercaseChar() }.joinToString("").ifEmpty { "ND" }

    Column(Modifier.fillMaxSize()) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth().background(Surface).padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Muted, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(12.dp))
            Text("Profilo", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = OnBg)
        }

        HorizontalDivider(color = Divider, thickness = 0.5.dp)

        Row(modifier = Modifier.fillMaxSize().padding(40.dp), horizontalArrangement = Arrangement.spacedBy(40.dp)) {
            // Left: avatar + name
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(200.dp)) {
                Box(
                    modifier = Modifier.size(96.dp).clip(CircleShape).background(Primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) { Text(initials, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = Primary) }
                Spacer(Modifier.height(16.dp))
                Text(displayName, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = OnBg)
                Text(authUser?.email ?: "", fontSize = 12.sp, color = Muted)
                profile?.company?.let {
                    Spacer(Modifier.height(8.dp))
                    Box(
                        modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(Primary.copy(alpha = 0.1f)).padding(horizontal = 10.dp, vertical = 4.dp)
                    ) { Text(it, color = Primary, fontSize = 12.sp) }
                }
            }

            // Right: info table
            Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).background(Surface).padding(4.dp)) {
                Column {
                    InfoRow(Icons.Default.Email, "Email", authUser?.email ?: "—")
                    HorizontalDivider(color = Divider, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    profile?.fullName?.let {
                        InfoRow(Icons.Default.Person, "Nome completo", it)
                        HorizontalDivider(color = Divider, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    }
                    profile?.company?.let {
                        InfoRow(Icons.Default.Business, "Azienda", it)
                        HorizontalDivider(color = Divider, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    }
                    profile?.phone?.let {
                        InfoRow(Icons.Default.Phone, "Telefono", it)
                        HorizontalDivider(color = Divider, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    }
                    profile?.role?.let { InfoRow(Icons.Default.Badge, "Ruolo", it) }
                }
            }
        }
    }
}

@Composable
fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = Primary, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(14.dp))
        Column {
            Text(label, color = Muted, fontSize = 11.sp)
            Text(value, color = OnBg, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}
