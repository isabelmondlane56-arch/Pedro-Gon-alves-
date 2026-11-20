const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_uid_101";
const BOB_UID = "bob_uid_202";
const ADMIN_EMAIL = "isabelmondlane56@gmail.com";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated users are denied across all collections", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("market_products").where("status", "==", "ACTIVE").get());
  await assertFails(unauthDb.collection("content_reports").get());
});

test("User profile creation, RBAC protection, and cross-user isolation", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID, { email: "alice@example.com", email_verified: true }).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID, { email: "bob@example.com", email_verified: true }).firestore();
  const now = new Date(Date.now() - 2000);

  // Valid create
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Alice Mondlane",
      email: "alice@example.com",
      country: "Moçambique",
      language: "pt",
      themeMode: "dark",
      plan: "FREE",
      role: "USER",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Reject self-assigned ADMIN or PREMIUM on create
  await assertFails(
    bobDb.collection("users").doc(BOB_UID).set({
      userId: BOB_UID,
      displayName: "Bob",
      email: "bob@example.com",
      country: "Moçambique",
      language: "en",
      themeMode: "dark",
      plan: "PREMIUM",
      role: "ADMIN",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Reject cross-user read and update
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).update({ displayName: "Hacked", updatedAt: new Date(Date.now() - 1000) }));

  // Reject user upgrading their own plan or role
  await assertFails(aliceDb.collection("users").doc(ALICE_UID).update({ plan: "PREMIUM", updatedAt: new Date(Date.now() - 1000) }));
  await assertFails(aliceDb.collection("users").doc(ALICE_UID).update({ role: "ADMIN", updatedAt: new Date(Date.now() - 1000) }));

  // Valid partial update of profile preferences
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      displayName: "Alice M.",
      country: "Portugal",
      language: "en",
      themeMode: "light",
      bio: "Digital entrepreneur",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Reject future timestamp
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      displayName: "Future Alice",
      updatedAt: new Date(Date.now() + 60000),
    })
  );
});

test("User subcollections (academy_progress, design_projects, ai_generations, payout_methods)", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const now = new Date(Date.now() - 2000);

  // Academy progress
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("academy_progress").doc("course_digital_mkt").set({
      userId: ALICE_UID,
      courseId: "course_digital_mkt",
      completedLessonIds: ["l1", "l2"],
      progressPercent: 50,
      createdAt: now,
      updatedAt: now,
    })
  );
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("academy_progress").get());

  // Design project (NO id field)
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("design_projects").doc("proj_1").set({
      userId: ALICE_UID,
      templateType: "EBOOK_COVER",
      title: "Capa Ebook Vendas",
      headline: "Domine o Tráfego Pago",
      subheadline: "Guia prático para iniciantes",
      ctaText: "Baixar Agora",
      brandName: "Money Hub",
      paletteId: "gold_navy",
      createdAt: now,
      updatedAt: now,
    })
  );
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("design_projects").doc("proj_bad").set({
      id: "proj_bad",
      userId: ALICE_UID,
      templateType: "EBOOK_COVER",
      title: "Bad ID",
      headline: "H",
      subheadline: "S",
      ctaText: "C",
      brandName: "B",
      paletteId: "gold_navy",
      createdAt: now,
      updatedAt: now,
    })
  );

  // AI generation
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("ai_generations").doc("gen_1").set({
      userId: ALICE_UID,
      mode: "BUSINESS_IDEAS",
      prompt: "Ideias de negócio digital em Moçambique",
      response: "1. Loja de templates...",
      createdAt: now,
    })
  );

  // Payout method
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("payout_methods").doc("pm_1").set({
      userId: ALICE_UID,
      provider: "MPESA",
      accountLabel: "M-Pesa Principal",
      maskedAccount: "+258 84 *** 1234",
      currency: "MZN",
      createdAt: now,
      updatedAt: now,
    })
  );
});

test("Money Market products, reviews, query alignment, and moderation reports", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const spoofedAdminDb = testEnv.authenticatedContext("spoof_uid", { email: ADMIN_EMAIL, email_verified: false }).firestore();
  const realAdminDb = testEnv.authenticatedContext("real_admin_uid", { email: ADMIN_EMAIL, email_verified: true }).firestore();
  const now = new Date(Date.now() - 2000);

  // Alice creates a product
  await assertSucceeds(
    aliceDb.collection("market_products").doc("prod_1").set({
      sellerId: ALICE_UID,
      sellerName: "Alice Mondlane",
      sellerCountry: "Moçambique",
      title: "Guia Completo de Facebook Ads",
      description: "Aprenda a criar campanhas lucrativas do zero ao avançado.",
      category: "EBOOK",
      priceUsd: 12.5,
      priceMzn: 800,
      affiliatePercent: 25,
      ratingAvg: 0,
      reviewCount: 0,
      paletteId: "gold_navy",
      status: "ACTIVE",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Unfiltered query on market_products must fail; filtered ACTIVE query succeeds
  await assertFails(bobDb.collection("market_products").get());
  await assertSucceeds(bobDb.collection("market_products").where("status", "==", "ACTIVE").get());
  await assertSucceeds(aliceDb.collection("market_products").where("sellerId", "==", ALICE_UID).get());

  // Bob cannot hijack Alice's product title, but can submit a review + rating update
  await assertFails(bobDb.collection("market_products").doc("prod_1").update({ title: "Hijacked", updatedAt: new Date(Date.now() - 1000) }));
  await assertSucceeds(
    bobDb.collection("market_products").doc("prod_1").collection("reviews").doc("rev_1").set({
      authorId: BOB_UID,
      authorName: "Bob",
      rating: 5,
      comment: "Conteúdo muito prático!",
      createdAt: new Date(Date.now() - 1000),
    })
  );
  await assertSucceeds(
    bobDb.collection("market_products").doc("prod_1").update({
      ratingAvg: 5,
      reviewCount: 1,
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Bob files a report; spoofed admin cannot moderate; real verified admin can moderate
  await assertSucceeds(
    bobDb.collection("content_reports").doc("rep_1").set({
      reporterId: BOB_UID,
      targetType: "MARKET_PRODUCT",
      targetId: "prod_1",
      reason: "Verificação de direitos autorais solicitada",
      status: "OPEN",
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertFails(spoofedAdminDb.collection("content_reports").get());
  await assertFails(spoofedAdminDb.collection("content_reports").doc("rep_1").update({ status: "REVIEWED", updatedAt: new Date(Date.now() - 500) }));

  await assertSucceeds(realAdminDb.collection("content_reports").get());
  await assertSucceeds(realAdminDb.collection("content_reports").doc("rep_1").update({ status: "REVIEWED", updatedAt: new Date(Date.now() - 500) }));
  await assertSucceeds(realAdminDb.collection("market_products").doc("prod_1").update({ status: "FLAGGED", updatedAt: new Date(Date.now() - 500) }));
});
