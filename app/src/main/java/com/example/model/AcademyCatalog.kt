package com.example.model

data class AcademyLesson(
    val id: String,
    val titlePt: String,
    val titleEn: String,
    val durationMin: Int,
    val summaryPt: String,
    val summaryEn: String,
    val bodyPt: String,
    val bodyEn: String,
    val takeawaysPt: List<String>,
    val takeawaysEn: List<String>
)

data class AcademyCourse(
    val id: String,
    val categoryPt: String,
    val categoryEn: String,
    val titlePt: String,
    val titleEn: String,
    val descriptionPt: String,
    val descriptionEn: String,
    val levelPt: String,
    val levelEn: String,
    val premiumOnly: Boolean,
    val lessons: List<AcademyLesson>
)

data class BusinessOpportunity(
    val id: String,
    val titlePt: String,
    val titleEn: String,
    val subtitlePt: String,
    val subtitleEn: String,
    val tagPt: String,
    val tagEn: String,
    val aiMode: AiMode,
    val suggestedPromptPt: String,
    val suggestedPromptEn: String
)

object AcademyCatalog {
    val courses: List<AcademyCourse> = listOf(
        AcademyCourse(
            id = "course_digital_mkt",
            categoryPt = "Marketing Digital",
            categoryEn = "Digital Marketing",
            titlePt = "Fundamentos de Marketing Digital",
            titleEn = "Digital Marketing Foundations",
            descriptionPt = "Domina funis de aquisição, posicionamento e conversão de audiência em clientes.",
            descriptionEn = "Master acquisition funnels, positioning, and converting audiences into buyers.",
            levelPt = "Iniciante",
            levelEn = "Beginner",
            premiumOnly = false,
            lessons = listOf(
                AcademyLesson(
                    id = "dm_1",
                    titlePt = "O Mapa do Funil Digital",
                    titleEn = "The Digital Funnel Blueprint",
                    durationMin = 8,
                    summaryPt = "Como transformar desconhecidos em compradores recorrentes.",
                    summaryEn = "How to turn strangers into repeat digital buyers.",
                    bodyPt = "Todo negócio digital rentável segue três etapas claras: Atração (conteúdo curto e anúncios), Relacionamento (provas sociais e valor gratuito no WhatsApp, email ou comunidade) e Oferta (uma solução clara para uma dor urgente). Em mercados emergentes como Moçambique e CPLP, a confiança visual e respostas rápidas fazem toda a diferença.",
                    bodyEn = "Every profitable digital business follows three clear stages: Attraction (short-form content and targeted ads), Nurture (social proof and free value via WhatsApp, email, or community), and Offer (a clear solution to an urgent pain point). Strong visual trust and fast follow-up multiply conversions.",
                    takeawaysPt = listOf(
                        "Define 1 problema específico que o teu produto resolve",
                        "Cria 1 íman digital gratuito antes de vender",
                        "Usa provas reais e clareza no preço"
                    ),
                    takeawaysEn = listOf(
                        "Define 1 specific problem your product solves",
                        "Offer 1 free lead magnet before pitching",
                        "Use clear pricing and verifiable social proof"
                    )
                ),
                AcademyLesson(
                    id = "dm_2",
                    titlePt = "Copywriting que Converte",
                    titleEn = "High-Converting Copywriting",
                    durationMin = 10,
                    summaryPt = "Estrutura AIDA aplicada a páginas, WhatsApp e redes sociais.",
                    summaryEn = "AIDA framework applied to landing pages and social channels.",
                    bodyPt = "Escrever para vender não é usar palavras difíceis — é mostrar transformação. Começa pela Atenção (uma promessa específica), constrói Interesse (identificando o obstáculo atual), desperta Desejo (o antes e depois) e termina com Ação direta (botão ou mensagem). Usa o Money AI para gerar 3 variações e testar qual ressoa melhor.",
                    bodyEn = "Sales writing is about clarity and transformation, not complex jargon. Start with Attention (a sharp promise), build Interest (name the current bottleneck), spark Desire (paint the after-state), and finish with clear Action. Use Money AI to draft 3 variations and test engagement.",
                    takeawaysPt = listOf(
                        "Foca nos benefícios mensuráveis, não apenas características",
                        "Inclui sempre uma Chamada à Ação única",
                        "Testa títulos curtos com o Money Design"
                    ),
                    takeawaysEn = listOf(
                        "Focus on measurable outcomes over features",
                        "End every piece with one unmistakable CTA",
                        "Test bold headlines inside Money Design"
                    )
                )
            )
        ),
        AcademyCourse(
            id = "course_ebooks",
            categoryPt = "Ebooks",
            categoryEn = "Ebooks",
            titlePt = "Criação e Venda de Ebooks",
            titleEn = "Creating & Selling Ebooks",
            descriptionPt = "Do tema rentável à capa profissional e lançamento no Money Market.",
            descriptionEn = "From profitable topic selection to pro covers and Money Market launch.",
            levelPt = "Iniciante",
            levelEn = "Beginner",
            premiumOnly = false,
            lessons = listOf(
                AcademyLesson(
                    id = "eb_1",
                    titlePt = "Escolher um Tema com Procura Real",
                    titleEn = "Choosing a High-Demand Topic",
                    durationMin = 7,
                    summaryPt = "Os 3 nichos mais lucrativos: dinheiro, competências e produtividade.",
                    summaryEn = "The 3 evergreen ebook niches: wealth, skills, and productivity.",
                    bodyPt = "Os melhores ebooks resolvem um problema em 40 a 70 páginas práticas. Em vez de um livro genérico sobre 'como ter sucesso', cria algo específico como 'Guia Prático de Contabilidade para Pequenas Lojas' ou 'Receitas Económicas para Marmitas Lucrativas'.",
                    bodyEn = "Top-selling ebooks solve one concrete problem in 40 to 70 actionable pages. Instead of a broad topic like 'how to succeed', build something focused like 'Practical Bookkeeping for Small Shops' or '30 High-Margin Freelance Templates'.",
                    takeawaysPt = listOf(
                        "Valida 5 perguntas frequentes do teu público",
                        "Estrutura em capítulos curtos com checklists",
                        "Gera uma capa de alto impacto no Money Design"
                    ),
                    takeawaysEn = listOf(
                        "Validate 5 real questions your audience asks",
                        "Structure short chapters with actionable checklists",
                        "Build a bold cover inside Money Design"
                    )
                ),
                AcademyLesson(
                    id = "eb_2",
                    titlePt = "Precificação & Rede de Afiliados",
                    titleEn = "Pricing & Affiliate Leverage",
                    durationMin = 9,
                    summaryPt = "Como definir preço em MZN/USD e atrair divulgadores.",
                    summaryEn = "Setting accessible pricing and motivating affiliate partners.",
                    bodyPt = "Ebooks de entrada entre 5 USD e 19 USD (ou 300–1200 MZN) têm baixa fricção de compra. Ao ativares 20% a 40% de comissão para afiliados no Money Market, outros criadores passam a divulgar o teu ebook para as suas audiências.",
                    bodyEn = "Entry-level ebooks priced between $5 and $19 USD minimize buyer friction. Offering 20% to 40% affiliate commission on Money Market turns other creators into a motivated sales force for your catalog.",
                    takeawaysPt = listOf(
                        "Define preço acessível para primeira venda rápida",
                        "Oferece 20–35% de comissão de afiliado",
                        "Recolhe avaliações reais dos primeiros leitores"
                    ),
                    takeawaysEn = listOf(
                        "Use friction-free pricing for first-time buyers",
                        "Offer 20–35% affiliate commission",
                        "Collect verified reviews from early readers"
                    )
                )
            )
        ),
        AcademyCourse(
            id = "course_entrepreneurship",
            categoryPt = "Empreendedorismo",
            categoryEn = "Entrepreneurship",
            titlePt = "Empreendedorismo Digital do Zero",
            titleEn = "Digital Entrepreneurship from Zero",
            descriptionPt = "Validação enxuta, modelo de negócio e execução disciplinada.",
            descriptionEn = "Lean validation, business models, and disciplined execution.",
            levelPt = "Iniciante",
            levelEn = "Beginner",
            premiumOnly = false,
            lessons = listOf(
                AcademyLesson(
                    id = "en_1",
                    titlePt = "MVP: Começa Pequeno, Lança Rápido",
                    titleEn = "MVP: Start Lean, Launch Fast",
                    durationMin = 8,
                    summaryPt = "Como validar uma oferta em 72 horas sem gastar fortunas.",
                    summaryEn = "How to validate an offer in 72 hours with minimal budget.",
                    bodyPt = "O maior erro dos iniciantes é passar meses a construir em segredo. Define a versão mínima viável do teu serviço ou produto digital, cria um cartaz no Money Design, gera a descrição no Money AI e apresenta a 10 potenciais clientes reais.",
                    bodyEn = "The biggest beginner trap is building in secret for months. Define a minimum viable offer, design a promo poster in Money Design, refine your pitch with Money AI, and present it to 10 real prospective customers.",
                    takeawaysPt = listOf(
                        "Valida antes de investir capital pesado",
                        "Ouve objeções reais dos clientes",
                        "Melhora a oferta com base em feedback"
                    ),
                    takeawaysEn = listOf(
                        "Validate demand before heavy investment",
                        "Listen carefully to buyer objections",
                        "Iterate your offer based on real feedback"
                    )
                )
            )
        ),
        AcademyCourse(
            id = "course_paid_traffic",
            categoryPt = "Tráfego Pago",
            categoryEn = "Paid Traffic",
            titlePt = "Tráfego Pago & Métricas de ROI",
            titleEn = "Paid Traffic & ROI Metrics",
            descriptionPt = "Aprende CPC, CTR, CPA e como escalar campanhas sem desperdiçar orçamento.",
            descriptionEn = "Master CPC, CTR, CPA, and how to scale campaigns profitably.",
            levelPt = "Intermédio",
            levelEn = "Intermediate",
            premiumOnly = false,
            lessons = listOf(
                AcademyLesson(
                    id = "pt_1",
                    titlePt = "As 4 Métricas Essenciais do Anunciante",
                    titleEn = "The 4 Core Ad Metrics",
                    durationMin = 11,
                    summaryPt = "CTR, CPC, Taxa de Conversão e ROAS explicados de forma simples.",
                    summaryEn = "CTR, CPC, Conversion Rate, and ROAS demystified.",
                    bodyPt = "Se o teu CTR (taxa de cliques) estiver abaixo de 1%, o problema está no criativo ou no título — volta ao Money Design e melhora o gancho visual. Se houver muitos cliques mas zero vendas, o problema está na página de oferta ou no preço.",
                    bodyEn = "If your CTR (click-through rate) is below 1%, fix your creative hook or headline in Money Design. If clicks are high but sales stay flat, refine your product description, pricing, or checkout clarity.",
                    takeawaysPt = listOf(
                        "CTR mede a atratividade do anúncio",
                        "Testa sempre 2 a 3 criativos em paralelo",
                        "Acompanha o custo por resultado diariamente"
                    ),
                    takeawaysEn = listOf(
                        "CTR measures creative & hook strength",
                        "Always A/B test 2–3 visual creatives",
                        "Track cost per conversion daily"
                    )
                )
            )
        ),
        AcademyCourse(
            id = "course_meta_ads",
            categoryPt = "Facebook & Instagram Ads",
            categoryEn = "Facebook & Instagram Ads",
            titlePt = "Facebook Ads & Instagram Ads na Prática",
            titleEn = "Practical Facebook & Instagram Ads",
            descriptionPt = "Estrutura de campanhas, públicos locais e globais, e anúncios para WhatsApp e Lojas.",
            descriptionEn = "Campaign structure, local/global targeting, and WhatsApp/Store conversion ads.",
            levelPt = "Intermédio",
            levelEn = "Intermediate",
            premiumOnly = false,
            lessons = listOf(
                AcademyLesson(
                    id = "ma_1",
                    titlePt = "Estrutura Campanha > Conjunto > Anúncio",
                    titleEn = "Campaign > Ad Set > Ad Architecture",
                    durationMin = 12,
                    summaryPt = "Evita o botão 'Turbinar' e configura objetivos reais de conversão.",
                    summaryEn = "Skip generic boost buttons and configure real conversion objectives.",
                    bodyPt = "No Gestor de Anúncios da Meta, define o objetivo certo (Vendas ou Mensagens para WhatsApp Business). No Conjunto de Anúncios, escolhe localização precisa (ex: Maputo, Beira, Lisboa, Luanda ou países CPLP) e segmentação limpa. Nos Anúncios, combina vídeos curtos com cartazes nítidos.",
                    bodyEn = "Inside Meta Ads Manager, pick the exact objective (Sales or WhatsApp Messaging). At the Ad Set level, configure clean geographic targeting and broad-to-interest testing. At the Ad level, pair crisp posters with short vertical video hooks.",
                    takeawaysPt = listOf(
                        "Usa objetivo de Vendas ou Mensagens, não apenas Curtidas",
                        "Adapta o texto ao público local ou internacional",
                        "Renova criativos a cada 14 dias"
                    ),
                    takeawaysEn = listOf(
                        "Choose Sales or Messages objectives over Vanity Likes",
                        "Localize copy for regional or global audiences",
                        "Refresh creatives every 14 days to avoid fatigue"
                    )
                )
            )
        ),
        AcademyCourse(
            id = "course_affiliates",
            categoryPt = "Marketing de Afiliados",
            categoryEn = "Affiliate Marketing",
            titlePt = "Marketing de Afiliados Rentável",
            titleEn = "Profitable Affiliate Marketing",
            descriptionPt = "Como recomendar produtos digitais de alta qualidade e ganhar comissões éticas.",
            descriptionEn = "Recommend high-value digital products and earn recurring commissions ethically.",
            levelPt = "Iniciante",
            levelEn = "Beginner",
            premiumOnly = false,
            lessons = listOf(
                AcademyLesson(
                    id = "af_1",
                    titlePt = "Como Escolher Produtos Vencedores",
                    titleEn = "Selecting Winning Affiliate Products",
                    durationMin = 9,
                    summaryPt = "Critérios de qualidade, comissão e procura constante.",
                    summaryEn = "Evaluating product quality, commission rates, and evergreen demand.",
                    bodyPt = "Um bom afiliado nunca faz spam de links. Escolhe produtos no Money Market com boa avaliação, cria conteúdo educativo mostrando como o produto resolve um problema real (mini tutoriais, resumos ou estudos de caso) e partilha o teu código de afiliado com transparência.",
                    bodyEn = "Great affiliates never spam links. Pick well-reviewed products in Money Market, publish helpful tutorials or mini case studies demonstrating real value, and share your referral code transparently.",
                    takeawaysPt = listOf(
                        "Recomenda apenas produtos que compreendes",
                        "Cria conteúdo útil antes de partilhar o link",
                        "Prefere comissões entre 20% e 50%"
                    ),
                    takeawaysEn = listOf(
                        "Promote products you genuinely understand",
                        "Lead with educational content before linking",
                        "Look for sustainable 20–50% commissions"
                    )
                )
            )
        ),
        AcademyCourse(
            id = "course_online_business",
            categoryPt = "Negócios Online",
            categoryEn = "Online Business",
            titlePt = "Lojas Virtuais & Serviços Digitais",
            titleEn = "Online Stores & Digital Services",
            descriptionPt = "Construção de catálogos digitais, entrega automática e atendimento profissional.",
            descriptionEn = "Building digital catalogs, automated delivery, and high-trust customer service.",
            levelPt = "Intermédio",
            levelEn = "Intermediate",
            premiumOnly = true,
            lessons = listOf(
                AcademyLesson(
                    id = "ob_1",
                    titlePt = "Montar a Tua Montra Digital Internacional",
                    titleEn = "Launching Your Global Digital Storefront",
                    durationMin = 10,
                    summaryPt = "Organização de portfólio, preços multi-moeda e atendimento pós-venda.",
                    summaryEn = "Structuring your catalog, dual currency pricing, and post-sale trust.",
                    bodyPt = "Vender online em Moçambique e internacionalmente exige duas coisas: preços claros em moeda local e USD, e apresentação impecável. Usa o Money Design para uniformizar a identidade visual de todos os teus produtos.",
                    bodyEn = "Selling both locally and globally requires transparent multi-currency pricing and consistent visual branding. Use Money Design so every product card in your storefront feels cohesive and trustworthy.",
                    takeawaysPt = listOf(
                        "Padroniza cores e tipografia da tua marca",
                        "Oferece pacotes (bundles) para aumentar o ticket médio",
                        "Responde a dúvidas pré-compra em menos de 1 hora"
                    ),
                    takeawaysEn = listOf(
                        "Standardize brand colors and typography",
                        "Bundle complementary assets to raise order value",
                        "Maintain responsive pre-sale communication"
                    )
                )
            )
        ),
        AcademyCourse(
            id = "course_finance",
            categoryPt = "Gestão Financeira",
            categoryEn = "Financial Management",
            titlePt = "Gestão Financeira para Empreendedores",
            titleEn = "Financial Management for Creators",
            descriptionPt = "Separação de contas, margem de lucro, reinvestimento e reserva de emergência.",
            descriptionEn = "Separating personal & business funds, profit margins, and reinvestment.",
            levelPt = "Essencial",
            levelEn = "Essential",
            premiumOnly = false,
            lessons = listOf(
                AcademyLesson(
                    id = "fn_1",
                    titlePt = "Regra 50 / 30 / 20 para Negócios Digitais",
                    titleEn = "The 50 / 30 / 20 Creator Profit Rule",
                    durationMin = 9,
                    summaryPt = "Como nunca misturar dinheiro pessoal com capital de crescimento.",
                    summaryEn = "Never mix personal spending with business growth capital again.",
                    bodyPt = "Faturação não é lucro. Separa 50% do lucro líquido para reserva e custos operacionais, 30% para reinvestimento em tráfego pago e ferramentas, e 20% para a tua retirada pessoal. Regista cada receita e custo semanalmente.",
                    bodyEn = "Revenue is not profit. Allocate 50% of net profit to operating reserves, 30% to reinvesting in paid traffic and tools, and 20% to founder compensation. Review cash flow weekly.",
                    takeawaysPt = listOf(
                        "Separa carteira pessoal e carteira do negócio",
                        "Calcula sempre o lucro líquido após taxas e anúncios",
                        "Constrói fundo de reserva antes de escalar gastos"
                    ),
                    takeawaysEn = listOf(
                        "Separate personal and business accounts",
                        "Always measure net profit after fees & ad spend",
                        "Build an operating cushion before scaling fixed costs"
                    )
                )
            )
        )
    )

    val opportunities: List<BusinessOpportunity> = listOf(
        BusinessOpportunity(
            id = "opp_ebook",
            titlePt = "Lançar Ebook Lucrativo",
            titleEn = "Launch a Profitable Ebook",
            subtitlePt = "Transforma conhecimento prático num ativo digital vendável 24h por dia.",
            subtitleEn = "Turn practical expertise into a 24/7 digital asset.",
            tagPt = "Baixo Custo",
            tagEn = "Low Startup Cost",
            aiMode = AiMode.EBOOK_IDEAS,
            suggestedPromptPt = "Cria uma estrutura de ebook rentável sobre finanças ou marketing digital para vender online",
            suggestedPromptEn = "Outline a high-demand ebook on digital skills or personal finance to sell online"
        ),
        BusinessOpportunity(
            id = "opp_traffic",
            titlePt = "Gestor de Tráfego Pago",
            titleEn = "Paid Traffic Specialist",
            subtitlePt = "Gere campanhas de Facebook e Instagram Ads para empresas locais e internacionais.",
            subtitleEn = "Run Facebook & Instagram Ads for local and international businesses.",
            tagPt = "Alta Procura",
            tagEn = "High Demand",
            aiMode = AiMode.MARKETING_STRATEGY,
            suggestedPromptPt = "Como conseguir os primeiros 3 clientes de gestão de tráfego pago e quanto cobrar?",
            suggestedPromptEn = "How to land my first 3 paid traffic clients and structure monthly retainers?"
        ),
        BusinessOpportunity(
            id = "opp_affiliate",
            titlePt = "Rede de Afiliados Digitais",
            titleEn = "Digital Affiliate Network",
            subtitlePt = "Promove cursos e ebooks do Money Market e recebe comissões por cada venda.",
            subtitleEn = "Promote Money Market courses and ebooks and earn per-sale commissions.",
            tagPt = "Sem Produto Próprio",
            tagEn = "No Product Needed",
            aiMode = AiMode.SOCIAL_CONTENT,
            suggestedPromptPt = "Cria 5 ideias de publicações educativas para promover um curso digital como afiliado",
            suggestedPromptEn = "Create 5 educational social posts to promote a digital course as an affiliate"
        ),
        BusinessOpportunity(
            id = "opp_store",
            titlePt = "Loja de Templates & Design",
            titleEn = "Templates & Design Store",
            subtitlePt = "Cria cartazes, capas e identidades no Money Design e vende pacotes prontos.",
            subtitleEn = "Craft posters, covers, and brand kits in Money Design and sell bundles.",
            tagPt = "Escalável",
            tagEn = "Scalable",
            aiMode = AiMode.PRODUCT_DESC,
            suggestedPromptPt = "Escreve uma descrição comercial atrativa para um pacote de 20 cartazes editáveis para negócios",
            suggestedPromptEn = "Write a compelling commercial description for a 20-poster social media template bundle"
        )
    )
}
