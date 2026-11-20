package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.localization.AppLanguage
import com.example.localization.LocalAppStrings
import com.example.model.DesignPalettes
import com.example.model.MarketCategory
import com.example.model.MarketProduct
import com.example.model.ProductReview
import com.example.model.UiState

data class MarketPublishDraft(
    val title: String,
    val description: String,
    val paletteId: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketScreen(
    language: AppLanguage,
    currentUserId: String,
    productsState: UiState<List<MarketProduct>>,
    reviewsState: UiState<List<ProductReview>>,
    publishDraft: MarketPublishDraft?,
    onClearPublishDraft: () -> Unit,
    onSelectProduct: (String?) -> Unit,
    onPublishProduct: (
        title: String,
        description: String,
        category: MarketCategory,
        priceUsd: Double,
        priceMzn: Double,
        affiliatePercent: Long,
        paletteId: String
    ) -> Unit,
    onDeleteProduct: (String) -> Unit,
    onSubmitReview: (productId: String, rating: Long, comment: String) -> Unit,
    onReportContent: (targetType: String, targetId: String, reason: String) -> Unit
) {
    val strings = LocalAppStrings.current
    val context = LocalContext.current

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<MarketCategory?>(null) }
    var selectedProduct by remember { mutableStateOf<MarketProduct?>(null) }
    var showPublishSheet by remember { mutableStateOf(false) }

    var draftTitle by remember { mutableStateOf("") }
    var draftDesc by remember { mutableStateOf("") }
    var draftCategory by remember { mutableStateOf(MarketCategory.EBOOK) }
    var draftPriceUsd by remember { mutableStateOf("15.0") }
    var draftPriceMzn by remember { mutableStateOf("950") }
    var draftAffiliate by remember { mutableStateOf("25") }
    var draftPalette by remember { mutableStateOf("gold_navy") }

    var showCheckoutDialog by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    var reportReason by remember { mutableStateOf("") }

    LaunchedEffect(publishDraft) {
        if (publishDraft != null) {
            draftTitle = publishDraft.title
            draftDesc = publishDraft.description
            draftPalette = publishDraft.paletteId
            showPublishSheet = true
            onClearPublishDraft()
        }
    }

    val allProducts = (productsState as? UiState.Success)?.data ?: emptyList()
    val filteredProducts = allProducts.filter { prod ->
        val matchesCategory = selectedCategory == null || prod.category == selectedCategory?.code
        val q = searchQuery.trim()
        val matchesQuery = q.isEmpty() ||
            prod.title.contains(q, ignoreCase = true) ||
            prod.description.contains(q, ignoreCase = true) ||
            prod.sellerName.contains(q, ignoreCase = true)
        matchesCategory && matchesQuery
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showPublishSheet = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("market_publish_fab")
            ) {
                Icon(Icons.Filled.Add, contentDescription = strings.publishProductTitle)
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("market_screen"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(124.dp)
                        .clip(RoundedCornerShape(20.dp))
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_market_showcase),
                        contentDescription = strings.marketTitle,
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
                                        MaterialTheme.colorScheme.background.copy(alpha = 0.40f)
                                    )
                                )
                            )
                            .padding(16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Column {
                            Text(strings.marketTitle, style = MaterialTheme.typography.headlineMedium)
                            Text(
                                strings.marketSubtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    placeholder = { Text(strings.searchMarketHint) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("market_search_input")
                )
            }

            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = selectedCategory == null,
                            onClick = { selectedCategory = null },
                            label = { Text(strings.allCategories) }
                        )
                    }
                    items(MarketCategory.entries) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(if (language == AppLanguage.PT) cat.labelPt else cat.labelEn) }
                        )
                    }
                }
            }

            when (productsState) {
                is UiState.Loading -> {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                }
                is UiState.Error -> {
                    item {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.14f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = productsState.message,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
                is UiState.Success -> {
                    if (filteredProducts.isEmpty()) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = strings.emptyStateTitle,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(18.dp)
                                )
                            }
                        }
                    } else {
                        items(filteredProducts, key = { it.id }) { prod ->
                            MarketProductCard(
                                product = prod,
                                language = language,
                                onClick = {
                                    selectedProduct = prod
                                    onSelectProduct(prod.id)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Product Detail + Seller Profile + Reviews Modal
    if (selectedProduct != null) {
        val prod = allProducts.firstOrNull { it.id == selectedProduct!!.id } ?: selectedProduct!!
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        var reviewRating by remember { mutableLongStateOf(5L) }
        var reviewComment by remember { mutableStateOf("") }
        val reviews = (reviewsState as? UiState.Success)?.data ?: emptyList()

        ModalBottomSheet(
            onDismissRequest = {
                selectedProduct = null
                onSelectProduct(null)
            },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        val cat = MarketCategory.fromCode(prod.category)
                        Text(
                            text = if (language == AppLanguage.PT) cat.labelPt else cat.labelEn,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Row {
                        IconButton(onClick = { showReportDialog = true }) {
                            Icon(Icons.Filled.Flag, contentDescription = strings.reportContent, tint = MaterialTheme.colorScheme.error)
                        }
                        if (prod.sellerId == currentUserId) {
                            IconButton(onClick = {
                                onDeleteProduct(prod.id)
                                selectedProduct = null
                                onSelectProduct(null)
                            }) {
                                Icon(Icons.Filled.Delete, contentDescription = strings.delete, tint = MaterialTheme.colorScheme.error)
                            }
                        }
                        IconButton(onClick = {
                            selectedProduct = null
                            onSelectProduct(null)
                        }) {
                            Icon(Icons.Filled.Close, contentDescription = strings.close)
                        }
                    }
                }

                Text(prod.title, style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "$${String.format("%.2f", prod.priceUsd)} USD  •  ${prod.priceMzn.toInt()} MZN",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(prod.description, style = MaterialTheme.typography.bodyMedium)

                Spacer(modifier = Modifier.height(14.dp))

                // Seller Profile Card
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(strings.sellerProfileTitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${prod.sellerName} • ${prod.sellerCountry}", style = MaterialTheme.typography.titleSmall)
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "${prod.affiliatePercent}% Af.",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = { showCheckoutDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Icon(Icons.Filled.ShoppingBag, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (language == AppLanguage.PT) "Adquirir" else "Acquire")
                    }
                    OutlinedButton(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "Money Market: ${prod.title} — $${String.format("%.2f", prod.priceUsd)} (${prod.affiliatePercent}% affiliate ref: $currentUserId)"
                                )
                            }
                            context.startActivity(Intent.createChooser(shareIntent, strings.affiliateShare))
                        },
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(strings.affiliateShare, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = "${strings.reviewsTitle} (${reviews.size})",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Rating stars selector
                Row(verticalAlignment = Alignment.CenterVertically) {
                    (1L..5L).forEach { star ->
                        IconButton(onClick = { reviewRating = star }) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = "$star stars",
                                tint = if (star <= reviewRating) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = reviewComment,
                    onValueChange = { reviewComment = it.take(400) },
                    label = { Text(strings.writeReviewHint) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        if (reviewComment.trim().length >= 2) {
                            onSubmitReview(prod.id, reviewRating, reviewComment)
                            reviewComment = ""
                        }
                    },
                    enabled = reviewComment.trim().length >= 2,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(strings.submitReview)
                }

                Spacer(modifier = Modifier.height(12.dp))
                reviews.forEach { rev ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(rev.authorName, style = MaterialTheme.typography.titleSmall)
                                Text("★ ${rev.rating}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(rev.comment, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Publish Product Bottom Sheet
    if (showPublishSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showPublishSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(strings.publishProductTitle, style = MaterialTheme.typography.headlineSmall)

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(MarketCategory.entries) { cat ->
                        FilterChip(
                            selected = draftCategory == cat,
                            onClick = { draftCategory = cat },
                            label = { Text(if (language == AppLanguage.PT) cat.labelPt else cat.labelEn) }
                        )
                    }
                }

                OutlinedTextField(
                    value = draftTitle,
                    onValueChange = { draftTitle = it.take(100) },
                    label = { Text(if (language == AppLanguage.PT) "Título do Produto (min. 3)" else "Product Title (min 3)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("publish_title_input")
                )
                OutlinedTextField(
                    value = draftDesc,
                    onValueChange = { draftDesc = it.take(1000) },
                    label = { Text(if (language == AppLanguage.PT) "Descrição Comercial (min. 10)" else "Commercial Description (min 10)") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth().testTag("publish_desc_input")
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = draftPriceUsd,
                        onValueChange = { draftPriceUsd = it },
                        label = { Text("USD ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = draftPriceMzn,
                        onValueChange = { draftPriceMzn = it },
                        label = { Text("MZN") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = draftAffiliate,
                        onValueChange = { draftAffiliate = it },
                        label = { Text("Af. %") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(0.8f)
                    )
                }

                Button(
                    onClick = {
                        onPublishProduct(
                            draftTitle,
                            draftDesc,
                            draftCategory,
                            draftPriceUsd.toDoubleOrNull() ?: 0.0,
                            draftPriceMzn.toDoubleOrNull() ?: 0.0,
                            draftAffiliate.toLongOrNull() ?: 20L,
                            draftPalette
                        )
                        showPublishSheet = false
                        draftTitle = ""
                        draftDesc = ""
                    },
                    enabled = draftTitle.trim().length >= 3 && draftDesc.trim().length >= 10,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.fillMaxWidth().height(50.dp).testTag("publish_submit_button")
                ) {
                    Text(strings.publishButton, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showCheckoutDialog) {
        AlertDialog(
            onDismissRequest = { showCheckoutDialog = false },
            title = { Text(strings.checkoutGatewayTitle) },
            text = { Text(strings.checkoutGatewayNotice) },
            confirmButton = {
                TextButton(onClick = { showCheckoutDialog = false }) {
                    Text(strings.close)
                }
            }
        )
    }

    if (showReportDialog && selectedProduct != null) {
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = { Text(strings.reportContent) },
            text = {
                OutlinedTextField(
                    value = reportReason,
                    onValueChange = { reportReason = it.take(300) },
                    label = { Text(strings.reportReasonHint) },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (reportReason.trim().length >= 5) {
                            onReportContent("MARKET_PRODUCT", selectedProduct!!.id, reportReason)
                            reportReason = ""
                            showReportDialog = false
                        }
                    }
                ) { Text(strings.submitReport) }
            },
            dismissButton = {
                TextButton(onClick = { showReportDialog = false }) { Text(strings.cancel) }
            }
        )
    }
}

@Composable
private fun MarketProductCard(
    product: MarketProduct,
    language: AppLanguage,
    onClick: () -> Unit
) {
    val palette = DesignPalettes.firstOrNull { it.id == product.paletteId } ?: DesignPalettes.first()
    val category = MarketCategory.fromCode(product.category)

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Brush.linearGradient(listOf(palette.backgroundStart, palette.accent))),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = product.title.take(2).uppercase(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = palette.textColor
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (language == AppLanguage.PT) category.labelPt else category.labelEn,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "★ ${String.format("%.1f", product.ratingAvg)} (${product.reviewCount})",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = product.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${product.sellerName} • ${product.sellerCountry}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$${String.format("%.2f", product.priceUsd)} • ${product.priceMzn.toInt()} MZN",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "${product.affiliatePercent}% Af.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
