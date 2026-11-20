package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.DesignCopySuggestion
import com.example.localization.AppLanguage
import com.example.localization.LocalAppStrings
import com.example.model.DesignPalettes
import com.example.model.DesignProject
import com.example.model.DesignTemplateType
import com.example.model.UiState
import com.example.ui.components.DesignCanvasPreview
import com.example.ui.components.SectionHeader

@Composable
fun DesignScreen(
    language: AppLanguage,
    projectsState: UiState<List<DesignProject>>,
    aiCopySuggestion: DesignCopySuggestion?,
    isGeneratingCopy: Boolean,
    prefilledHeadline: String?,
    onClearPrefilledHeadline: () -> Unit,
    onClearAiCopy: () -> Unit,
    onRequestAiCopy: (topic: String, templateLabel: String) -> Unit,
    onSaveProject: (
        existingId: String?,
        templateType: DesignTemplateType,
        title: String,
        headline: String,
        subheadline: String,
        ctaText: String,
        brandName: String,
        paletteId: String
    ) -> Unit,
    onDeleteProject: (String) -> Unit,
    onPublishToMarket: (title: String, description: String, paletteId: String) -> Unit
) {
    val strings = LocalAppStrings.current
    val savedProjects = (projectsState as? UiState.Success)?.data ?: emptyList()

    var editingProjectId by remember { mutableStateOf<String?>(null) }
    var templateType by remember { mutableStateOf(DesignTemplateType.POSTER) }
    var projectTitle by remember { mutableStateOf("Campanha Money Hub") }
    var headline by remember { mutableStateOf("Transforma o teu conhecimento em rendimento") }
    var subheadline by remember { mutableStateOf("Método prático passo a passo para negócios digitais.") }
    var ctaText by remember { mutableStateOf("Começar Hoje") }
    var brandName by remember { mutableStateOf("MONEY HUB STUDIO") }
    var paletteId by remember { mutableStateOf("gold_navy") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> selectedImageUri = uri }
    )

    LaunchedEffect(aiCopySuggestion) {
        if (aiCopySuggestion != null) {
            headline = aiCopySuggestion.headline
            subheadline = aiCopySuggestion.subheadline
            ctaText = aiCopySuggestion.ctaText
            onClearAiCopy()
        }
    }

    LaunchedEffect(prefilledHeadline) {
        if (!prefilledHeadline.isNullOrBlank()) {
            headline = prefilledHeadline
            onClearPrefilledHeadline()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("design_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(20.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_design_studio),
                    contentDescription = strings.designTitle,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.background.copy(alpha = 0.90f),
                                    MaterialTheme.colorScheme.background.copy(alpha = 0.45f)
                                )
                            )
                        )
                        .padding(16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(strings.designTitle, style = MaterialTheme.typography.headlineMedium)
                            Text(
                                strings.designSubtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier
                                .testTag("design_new_button")
                                .clickable {
                                    editingProjectId = null
                                    projectTitle = if (language == AppLanguage.PT) "Novo Projeto" else "New Project"
                                    headline = ""
                                    subheadline = ""
                                    ctaText = if (language == AppLanguage.PT) "Saiba Mais" else "Learn More"
                                    selectedImageUri = null
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(strings.createNewDesign, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }

        // Live Studio Canvas Preview
        item {
            DesignCanvasPreview(
                templateType = templateType,
                headline = headline,
                subheadline = subheadline,
                ctaText = ctaText,
                brandName = brandName,
                paletteId = paletteId,
                language = language,
                imageUri = selectedImageUri
            )
        }

        // Format selector chips (7 formats)
        item {
            Text(strings.templateLabel, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(DesignTemplateType.entries) { type ->
                    FilterChip(
                        selected = templateType == type,
                        onClick = { templateType = type },
                        label = {
                            Text(if (language == AppLanguage.PT) type.labelPt else type.labelEn)
                        }
                    )
                }
            }
        }

        // Palette Swatches
        item {
            Text(strings.paletteLabel, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(DesignPalettes) { pal ->
                    val selected = pal.id == paletteId
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(
                                width = if (selected) 2.dp else 1.dp,
                                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable { paletteId = pal.id }
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(pal.backgroundStart, pal.accent)))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (language == AppLanguage.PT) pal.namePt else pal.nameEn,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }

        // Studio Form Controls
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = projectTitle,
                    onValueChange = { projectTitle = it.take(100) },
                    label = { Text(strings.projectTitleLabel) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("design_title_input")
                )
                OutlinedTextField(
                    value = headline,
                    onValueChange = { headline = it.take(120) },
                    label = { Text(strings.headlineLabel) },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth().testTag("design_headline_input")
                )
                OutlinedTextField(
                    value = subheadline,
                    onValueChange = { subheadline = it.take(240) },
                    label = { Text(strings.subheadlineLabel) },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = ctaText,
                        onValueChange = { ctaText = it.take(60) },
                        label = { Text(strings.ctaLabel) },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = brandName,
                        onValueChange = { brandName = it.take(60) },
                        label = { Text(strings.brandLabel) },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                // AI Copy + Zero-Permission Photo Picker Row
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Icon(Icons.Filled.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(strings.pickPhotoLabel, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }

                    OutlinedButton(
                        onClick = {
                            val topic = projectTitle.ifBlank { headline.ifBlank { "negócio digital" } }
                            val label = if (language == AppLanguage.PT) templateType.labelPt else templateType.labelEn
                            onRequestAiCopy(topic, label)
                        },
                        enabled = !isGeneratingCopy,
                        modifier = Modifier.weight(1f).height(48.dp).testTag("design_ai_copy_button")
                    ) {
                        if (isGeneratingCopy) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(strings.generateCopyWithAi, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = {
                            onSaveProject(
                                editingProjectId,
                                templateType,
                                projectTitle.ifBlank { "Projeto Money Design" },
                                headline,
                                subheadline,
                                ctaText,
                                brandName,
                                paletteId
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.weight(1f).height(50.dp).testTag("design_save_button")
                    ) {
                        Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(strings.saveProject, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            val title = projectTitle.ifBlank { headline }.take(100)
                            val desc = "$headline — $subheadline".trim(' ', '—')
                            onPublishToMarket(title, desc, paletteId)
                        },
                        modifier = Modifier.weight(1f).height(50.dp).testTag("design_publish_market_button")
                    ) {
                        Icon(Icons.Filled.Storefront, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(strings.publishToMarket, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }

        // Saved Projects List
        item {
            SectionHeader(title = "${strings.savedProjects} (${savedProjects.size})")
        }

        if (savedProjects.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = strings.emptyStateTitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(savedProjects, key = { it.id }) { proj ->
                val projType = DesignTemplateType.fromCode(proj.templateType)
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            editingProjectId = proj.id
                            templateType = projType
                            projectTitle = proj.title
                            headline = proj.headline
                            subheadline = proj.subheadline
                            ctaText = proj.ctaText
                            brandName = proj.brandName
                            paletteId = proj.paletteId
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (language == AppLanguage.PT) projType.labelPt else projType.labelEn,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(text = proj.title, style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = proj.headline,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        IconButton(onClick = { onDeleteProject(proj.id) }) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = strings.delete,
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}
