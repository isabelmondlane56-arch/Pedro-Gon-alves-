package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.localization.AppLanguage
import com.example.localization.LocalAppStrings
import com.example.model.ContentReport
import com.example.model.MarketProduct
import com.example.model.PayoutMethod
import com.example.model.PayoutProvider
import com.example.model.UiState
import com.example.ui.components.SectionHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    language: AppLanguage,
    payoutMethodsState: UiState<List<PayoutMethod>>,
    onBack: () -> Unit,
    onAddPayoutMethod: (PayoutProvider, String, String, String) -> Unit,
    onDeletePayoutMethod: (String) -> Unit
) {
    BackHandler(onBack = onBack)
    val strings = LocalAppStrings.current
    val methods = (payoutMethodsState as? UiState.Success)?.data ?: emptyList()

    var selectedProvider by remember { mutableStateOf(PayoutProvider.MPESA) }
    var accountLabel by remember { mutableStateOf("") }
    var maskedAccount by remember { mutableStateOf("") }
    var currency by remember { mutableStateOf("MZN") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.walletTitle) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.close)
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("wallet_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(strings.walletAvailableBalance, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("0.00 MZN  •  $0.00 USD", style = MaterialTheme.typography.headlineLarge)
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
                                Icon(Icons.Filled.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(strings.walletNotice, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }

            item {
                SectionHeader(title = strings.addPayoutMethod)
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(PayoutProvider.entries) { provider ->
                        FilterChip(
                            selected = selectedProvider == provider,
                            onClick = {
                                selectedProvider = provider
                                currency = if (provider == PayoutProvider.PAYPAL || provider == PayoutProvider.STRIPE) "USD" else "MZN"
                            },
                            label = { Text(provider.label) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = accountLabel,
                    onValueChange = { accountLabel = it.take(80) },
                    label = { Text(if (language == AppLanguage.PT) "Nome da Conta (ex: M-Pesa Pessoal)" else "Account Label") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = maskedAccount,
                    onValueChange = { maskedAccount = it.take(60) },
                    label = { Text(if (language == AppLanguage.PT) "Número / Identificador (ex: +258 84 *** 0000)" else "Masked Number / Email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = {
                        onAddPayoutMethod(selectedProvider, accountLabel, maskedAccount, currency)
                        accountLabel = ""
                        maskedAccount = ""
                    },
                    enabled = accountLabel.trim().length >= 2 && maskedAccount.trim().length >= 3,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text(strings.addPayoutMethod, fontWeight = FontWeight.Bold)
                }
            }

            item {
                SectionHeader(title = "${strings.payoutMethodsTitle} (${methods.size})")
            }

            items(methods, key = { it.id }) { method ->
                val prov = PayoutProvider.fromCode(method.provider)
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.AccountBalance, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(method.accountLabel, style = MaterialTheme.typography.titleMedium)
                            Text("${prov.label} • ${method.maskedAccount} (${method.currency})", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { onDeletePayoutMethod(method.id) }) {
                            Icon(Icons.Filled.Delete, contentDescription = strings.delete, tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumScreen(
    language: AppLanguage,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val strings = LocalAppStrings.current

    val freeFeatures = if (language == AppLanguage.PT) {
        listOf(
            "Acesso básico à Money Academy",
            "Modelos essenciais no Money Design",
            "Compra e venda no Money Market",
            "Utilização padrão do Money AI"
        )
    } else {
        listOf(
            "Basic access to Money Academy",
            "Essential templates in Money Design",
            "Buy & sell on Money Market",
            "Standard Money AI quota"
        )
    }

    val premiumFeatures = if (language == AppLanguage.PT) {
        listOf(
            "Todos os cursos avançados e certificados",
            "Modelos exclusivos e exportação avançada no Money Design",
            "Maior capacidade e prioridade no Money AI",
            "Comissões reduzidas no Money Market",
            "Experiência sem anúncios integrados"
        )
    } else {
        listOf(
            "All advanced courses & workshops",
            "Exclusive templates in Money Design",
            "Expanded Money AI priority quota",
            "Reduced seller fees on Money Market",
            "Ad-free experience"
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.premiumTitle) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.close)
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("premium_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(strings.premiumSubtitle, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            item {
                PlanTierCard(title = strings.freePlan, features = freeFeatures, highlighted = false)
            }
            item {
                PlanTierCard(title = strings.premiumPlan, features = premiumFeatures, highlighted = true)
            }
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                        Icon(Icons.Filled.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (language == AppLanguage.PT) {
                                "Assinatura via Google Play Billing e pagamentos locais será ativada na fase de faturação. Nenhuma cobrança está ativa neste momento."
                            } else {
                                "Google Play Billing & local subscriptions will activate in the billing release. No charges are active yet."
                            },
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlanTierCard(title: String, features: List<String>, highlighted: Boolean) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            width = if (highlighted) 2.dp else 1.dp,
            color = if (highlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
        )
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = if (highlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(10.dp))
            features.forEach { feat ->
                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = if (highlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(feat, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    isAdminUser: Boolean,
    reportsState: UiState<List<ContentReport>>,
    marketProductsState: UiState<List<MarketProduct>>,
    onBack: () -> Unit,
    onUpdateReportStatus: (reportId: String, status: String) -> Unit,
    onModerateProduct: (productId: String, status: String) -> Unit
) {
    BackHandler(onBack = onBack)
    val strings = LocalAppStrings.current
    val reports = (reportsState as? UiState.Success)?.data ?: emptyList()
    val products = (marketProductsState as? UiState.Success)?.data ?: emptyList()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.adminPanelTitle) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.close)
                    }
                }
            )
        }
    ) { innerPadding ->
        if (!isAdminUser) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Filled.Security, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Text("Acesso restrito a administradores verificados.", style = MaterialTheme.typography.titleMedium)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .testTag("admin_screen"),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.VerifiedUser, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Moderação & Segurança Ativa • ${products.size} produtos ativos • ${reports.size} denúncias",
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                    }
                }

                item { SectionHeader(title = "Denúncias & Moderação (${reports.size})") }
                if (reports.isEmpty()) {
                    item {
                        Text(strings.emptyStateTitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    items(reports, key = { it.id }) { rep ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("${rep.targetType} • ${rep.status}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                                Text(rep.reason, style = MaterialTheme.typography.bodyMedium)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(onClick = { onUpdateReportStatus(rep.id, "REVIEWED") }) {
                                        Text("Marcar Revista")
                                    }
                                    OutlinedButton(onClick = { onUpdateReportStatus(rep.id, "DISMISSED") }) {
                                        Text("Arquivar")
                                    }
                                }
                            }
                        }
                    }
                }

                item { SectionHeader(title = "Gestão de Produtos (${products.size})") }
                items(products, key = { it.id }) { prod ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(prod.title, style = MaterialTheme.typography.titleSmall)
                                Text("${prod.sellerName} • ${prod.status}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            OutlinedButton(onClick = { onModerateProduct(prod.id, "FLAGGED") }) {
                                Text("Suspender", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }
}
