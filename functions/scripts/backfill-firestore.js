#!/usr/bin/env node
/**
 * Одноразовая синхронизация: все пользователи Firebase Auth получают документ users/{uid},
 * для hr@mail.ru выставляются HR + APPROVED + isActive (как согласовано с приложением).
 *
 * Требуется ключ сервисного аккаунта:
 * Firebase Console → Project settings → Service accounts → Generate new private key → JSON.
 *
 * Запуск из каталога functions (где установлены зависимости):
 *   set GOOGLE_APPLICATION_CREDENTIALS=E:\\path\\to\\serviceAccount.json
 *   node scripts/backfill-firestore.js
 *
 * Или передайте путь первым аргументом:
 *   node scripts/backfill-firestore.js E:\\path\\to\\serviceAccount.json
 */

const fs = require("fs");
const path = require("path");

const admin = require("firebase-admin");

const HR_EMAIL = (process.env.TARGET_HR_EMAIL || "hr@mail.ru").trim().toLowerCase();

function loadServiceAccount() {
  const p =
    process.argv[2] ||
    process.env.GOOGLE_APPLICATION_CREDENTIALS ||
    process.env.FIREBASE_SERVICE_ACCOUNT_JSON;
  if (!p) {
    console.error(
      "Укажите JSON ключ: переменная GOOGLE_APPLICATION_CREDENTIALS или аргумент командной строки."
    );
    process.exit(1);
  }
  const resolved = path.resolve(p);
  if (!fs.existsSync(resolved)) {
    console.error("Файл не найден:", resolved);
    process.exit(1);
  }
  const raw = fs.readFileSync(resolved, "utf8");
  return JSON.parse(raw);
}

function displayNameFromEmail(email) {
  if (!email || !email.includes("@")) return "Новый сотрудник";
  const left = email.split("@")[0].replace(/\./g, " ").replace(/_/g, " ").trim();
  if (!left) return "Новый сотрудник";
  return left.charAt(0).toUpperCase() + left.slice(1);
}

function baseProfile(uid, emailLower, fullName) {
  const now = Date.now();
  return {
    id: uid,
    email: emailLower,
    fullName: fullName || displayNameFromEmail(emailLower),
    phone: null,
    avatarUrl: null,
    departmentId: null,
    position: "",
    gender: "MALE",
    militaryDocument: null,
    isMilitaryLiable: false,
    updatedAt: now,
    createdAt: now,
  };
}

async function main() {
  const sa = loadServiceAccount();
  admin.initializeApp({
    credential: admin.credential.cert(sa),
  });

  const db = admin.firestore();
  let nextPageToken;
  let total = 0;
  let updatedHr = 0;
  let created = 0;
  let merged = 0;

  do {
    const batch = await admin.auth().listUsers(1000, nextPageToken);
    for (const user of batch.users) {
      total++;
      const uid = user.uid;
      const emailLower = (user.email || "").toLowerCase();
      const fullName = user.displayName || displayNameFromEmail(emailLower);
      const ref = db.collection("users").doc(uid);
      const snap = await ref.get();
      const isHr = emailLower === HR_EMAIL;

      if (isHr) {
        const data = {
          ...baseProfile(uid, emailLower, fullName),
          role: "HR",
          approvalStatus: "APPROVED",
          isActive: true,
        };
        await ref.set(data, { merge: true });
        updatedHr++;
        console.log("HR обновлён:", emailLower, uid);
        continue;
      }

      if (!snap.exists) {
        const data = {
          ...baseProfile(uid, emailLower, fullName),
          role: "EMPLOYEE",
          approvalStatus: "PENDING",
          isActive: false,
        };
        await ref.set(data, { merge: true });
        created++;
        console.log("Создан профиль:", emailLower, uid);
      } else {
        const d = snap.data() || {};
        const patch = {};
        if (d.email == null && emailLower) patch.email = emailLower;
        if (d.id == null) patch.id = uid;
        if (d.fullName == null || d.fullName === "")
          patch.fullName = fullName;
        if (d.role == null) patch.role = "EMPLOYEE";
        if (d.approvalStatus == null) patch.approvalStatus = "PENDING";
        if (d.isActive == null) patch.isActive = false;
        if (d.updatedAt == null) patch.updatedAt = Date.now();
        if (Object.keys(patch).length > 0) {
          await ref.set(patch, { merge: true });
          merged++;
          console.log("Дополнен профиль:", emailLower, uid, Object.keys(patch).join(","));
        }
      }
    }
    nextPageToken = batch.pageToken;
  } while (nextPageToken);

  console.log("---");
  console.log("Всего пользователей Auth:", total);
  console.log("HR-профиль (merge):", updatedHr);
  console.log("Новых документов users:", created);
  console.log("Дополнено существующих:", merged);
}

main().catch((e) => {
  console.error(e);
  process.exit(1);
});
