package it.nexdam.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import it.nexdam.app.data.models.BlogPost
import it.nexdam.app.ui.theme.*
import it.nexdam.app.ui.viewmodels.BlogDetailState
import it.nexdam.app.ui.viewmodels.BlogListState
import it.nexdam.app.ui.viewmodels.BlogViewModel

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlogScreen(onBack: () -> Unit, vm: BlogViewModel = viewModel()) {
    val listState by vm.listState.collectAsState()
    val detailState by vm.detailState.collectAsState()
    val selectedCategory by vm.selectedCategory.collectAsState()

    if (detailState != null) {
        BlogDetailContent(state = detailState!!, onBack = { vm.closePost() })
        return
    }

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
                        Text("Blog", color = OnBackground, fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro", tint = Muted)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        },
        containerColor = Background
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            // Category filters
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(CATEGORIES) { (value, label) ->
                    val selected = selectedCategory == value
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (selected) Primary else SurfaceVariant)
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

            when (val s = listState) {
                is BlogListState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = Primary)
                            Spacer(Modifier.height(12.dp))
                            Text("Caricamento articoli…", color = Muted, fontSize = 13.sp)
                        }
                    }
                }
                is BlogListState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                            Text("⚠️", fontSize = 40.sp)
                            Spacer(Modifier.height(12.dp))
                            Text(s.message, color = Error, fontSize = 14.sp)
                            Spacer(Modifier.height(16.dp))
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
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("📋", fontSize = 48.sp)
                                Spacer(Modifier.height(12.dp))
                                Text("Nessun articolo disponibile", color = OnBackground, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            items(s.posts) { post ->
                                BlogPostCard(post = post, onClick = { vm.openPost(post.slug) })
                            }
                            item { Spacer(Modifier.height(8.dp)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BlogPostCard(post: BlogPost, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Surface)
            .clickable(onClick = onClick)
    ) {
        if (!post.coverUrl.isNullOrBlank()) {
            AsyncImage(
                model = post.coverUrl,
                contentDescription = post.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(Primary.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Text(categoryEmoji(post.category), fontSize = 36.sp)
            }
        }

        Column(modifier = Modifier.padding(16.dp)) {
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
                Text("${post.readMinutes} min", color = Muted, fontSize = 12.sp)
            }

            Spacer(Modifier.height(8.dp))
            Text(
                post.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = OnBackground,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            post.excerpt?.let {
                Spacer(Modifier.height(6.dp))
                Text(it, fontSize = 13.sp, color = Muted, maxLines = 3, overflow = TextOverflow.Ellipsis, lineHeight = 19.sp)
            }

            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Leggi l'articolo", color = Primary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.width(4.dp))
                Text("→", color = Primary, fontSize = 13.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlogDetailContent(state: BlogDetailState, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Articolo", color = OnBackground, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro", tint = Muted)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        },
        containerColor = Background
    ) { padding ->
        when (state) {
            is BlogDetailState.Loading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Primary)
                }
            }
            is BlogDetailState.Error -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("⚠️", fontSize = 40.sp)
                        Spacer(Modifier.height(12.dp))
                        Text(state.message, color = Error, fontSize = 14.sp)
                    }
                }
            }
            is BlogDetailState.Success -> {
                val post = state.post
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(20.dp)
                ) {
                    item {
                        if (!post.coverUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = post.coverUrl,
                                contentDescription = post.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(16.dp))
                            )
                            Spacer(Modifier.height(20.dp))
                        }

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
                            Text("${post.readMinutes} min di lettura", color = Muted, fontSize = 12.sp)
                            if (!post.publishedAt.isNullOrBlank()) {
                                Spacer(Modifier.width(10.dp))
                                Text("· ${formatDate(post.publishedAt)}", color = Muted, fontSize = 12.sp)
                            }
                        }

                        Spacer(Modifier.height(12.dp))
                        Text(
                            post.title,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = OnBackground,
                            lineHeight = 32.sp
                        )

                        Spacer(Modifier.height(20.dp))
                        HorizontalDivider(color = DividerColor, thickness = 0.5.dp)
                        Spacer(Modifier.height(20.dp))

                        val body = post.body ?: post.excerpt ?: ""
                        body.split("\n").forEach { paragraph ->
                            if (paragraph.isNotBlank()) {
                                val cleaned = paragraph.trim().removeSurrounding("**")
                                val isHeading = paragraph.trim().startsWith("**") && paragraph.trim().endsWith("**")
                                Text(
                                    cleaned,
                                    fontSize = if (isHeading) 17.sp else 15.sp,
                                    fontWeight = if (isHeading) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isHeading) OnBackground else Muted,
                                    lineHeight = 24.sp,
                                    modifier = Modifier.padding(bottom = 14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
