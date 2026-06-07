package it.nexdam.desktop.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import it.nexdam.desktop.data.models.BlogPost
import it.nexdam.desktop.ui.theme.*
import it.nexdam.desktop.ui.viewmodels.BlogDetailState
import it.nexdam.desktop.ui.viewmodels.BlogListState
import it.nexdam.desktop.ui.viewmodels.BlogViewModel

private val CATEGORIES = listOf(
    null to "Tutti",
    "Linux & Server" to "Linux & Server",
    "Cloudflare & Sicurezza" to "Cloudflare & Sicurezza",
    "Web Development" to "Web Development"
)

private fun categoryEmoji(category: String?): String = when (category) {
    "Linux & Server" -> "🖥️"
    "Cloudflare & Sicurezza" -> "🛡️"
    "Web Development" -> "🌐"
    else -> "📄"
}

private fun formatDate(iso: String?): String {
    if (iso.isNullOrBlank()) return ""
    return runCatching { iso.substring(0, 10) }.getOrDefault("")
}

@Composable
fun BlogPanel(onClose: () -> Unit, vm: BlogViewModel = remember { BlogViewModel() }) {
    val listState by vm.listState.collectAsState()
    val detailState by vm.detailState.collectAsState()
    val selectedCategory by vm.selectedCategory.collectAsState()

    if (detailState != null) {
        BlogDetailContent(state = detailState!!, onBack = { vm.closePost() })
        return
    }

    Column(modifier = Modifier.fillMaxSize().background(Background)) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Article, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column {
                Text("Blog NexDam", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = OnBg)
                Text("Guide e novità dal mondo NexDam", fontSize = 13.sp, color = Muted)
            }
        }

        // Category filters
        LazyRow(
            contentPadding = PaddingValues(horizontal = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(CATEGORIES) { (value, label) ->
                val selected = selectedCategory == value
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (selected) Primary else SurfaceVar)
                        .clickable { vm.loadPosts(value) }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        label,
                        color = if (selected) Background else Muted,
                        fontSize = 13.sp,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        when (val s = listState) {
            is BlogListState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Primary, modifier = Modifier.size(28.dp), strokeWidth = 2.dp)
                }
            }
            is BlogListState.Error -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(s.message, color = Danger, fontSize = 14.sp)
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = { vm.loadPosts() },
                            colors = ButtonDefaults.buttonColors(containerColor = Primary)
                        ) { Text("Riprova") }
                    }
                }
            }
            is BlogListState.Success -> {
                if (s.posts.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Nessun articolo disponibile", color = Muted, fontSize = 14.sp)
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 32.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(s.posts) { post ->
                            BlogPostRow(post = post, onClick = { vm.openPost(post.slug) })
                        }
                        item { Spacer(Modifier.height(12.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
fun BlogPostRow(post: BlogPost, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Surface)
            .clickable(onClick = onClick)
            .padding(20.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Primary.copy(alpha = 0.10f)),
            contentAlignment = Alignment.Center
        ) {
            Text(categoryEmoji(post.category), fontSize = 24.sp)
        }

        Spacer(Modifier.width(16.dp))

        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                post.category?.let {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Primary.copy(alpha = 0.12f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(it, color = Primary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(8.dp))
                }
                Icon(Icons.Default.Schedule, contentDescription = null, tint = Muted, modifier = Modifier.size(13.dp))
                Spacer(Modifier.width(4.dp))
                Text("${post.readMinutes} min · ${formatDate(post.publishedAt)}", color = Muted, fontSize = 12.sp)
            }
            Spacer(Modifier.height(6.dp))
            Text(post.title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = OnBg, maxLines = 2, overflow = TextOverflow.Ellipsis)
            post.excerpt?.let {
                Spacer(Modifier.height(4.dp))
                Text(it, fontSize = 13.sp, color = Muted, maxLines = 2, overflow = TextOverflow.Ellipsis, lineHeight = 18.sp)
            }
        }

        Spacer(Modifier.width(12.dp))
        Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Muted, modifier = Modifier.size(18.dp).align(Alignment.CenterVertically))
    }
}

@Composable
fun BlogDetailContent(state: BlogDetailState, onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(Background)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Indietro", tint = Muted)
            }
            Spacer(Modifier.width(8.dp))
            Text("Articolo", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = OnBg)
        }

        when (state) {
            is BlogDetailState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Primary, modifier = Modifier.size(28.dp), strokeWidth = 2.dp)
                }
            }
            is BlogDetailState.Error -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(state.message, color = Danger, fontSize = 14.sp)
                }
            }
            is BlogDetailState.Success -> {
                val post = state.post
                LazyColumn(
                    modifier = Modifier.fillMaxSize().weight(1f),
                    contentPadding = PaddingValues(horizontal = 32.dp, vertical = 8.dp)
                ) {
                    item {
                        Box(modifier = Modifier.widthIn(max = 760.dp)) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    post.category?.let {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Primary.copy(alpha = 0.12f))
                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Text(it, color = Primary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Spacer(Modifier.width(10.dp))
                                    }
                                    Icon(Icons.Default.Schedule, contentDescription = null, tint = Muted, modifier = Modifier.size(13.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("${post.readMinutes} min di lettura · ${formatDate(post.publishedAt)}", color = Muted, fontSize = 12.sp)
                                }

                                Spacer(Modifier.height(14.dp))
                                Text(
                                    post.title,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = OnBg,
                                    lineHeight = 36.sp
                                )

                                Spacer(Modifier.height(20.dp))
                                HorizontalDivider(color = Divider, thickness = 0.5.dp)
                                Spacer(Modifier.height(20.dp))

                                val body = post.body ?: post.excerpt ?: ""
                                body.split("\n").forEach { paragraph ->
                                    if (paragraph.isNotBlank()) {
                                        val trimmed = paragraph.trim()
                                        val isHeading = trimmed.startsWith("**") && trimmed.endsWith("**")
                                        val cleaned = trimmed.removeSurrounding("**")
                                        Text(
                                            cleaned,
                                            fontSize = if (isHeading) 18.sp else 15.sp,
                                            fontWeight = if (isHeading) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isHeading) OnBg else Muted,
                                            lineHeight = 26.sp,
                                            modifier = Modifier.padding(bottom = 16.dp)
                                        )
                                    }
                                }
                                Spacer(Modifier.height(40.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
