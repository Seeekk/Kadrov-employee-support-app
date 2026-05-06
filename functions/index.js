const functions = require("firebase-functions");
const admin = require("firebase-admin");

admin.initializeApp();

/**
 * Автосоздание профиля в Firestore при появлении пользователя в Firebase Auth.
 * Документ хранится в users/{uid}, чтобы совпадать с request.auth.uid в rules.
 */
exports.createUserProfile = functions.auth.user().onCreate(async (user) => {
  const uid = user.uid;
  const email = (user.email || "").toLowerCase();
  const displayName = user.displayName || (email.includes("@")
    ? email.split("@")[0]
    : "Новый сотрудник");
  const now = Date.now();

  await admin.firestore().collection("users").doc(uid).set({
    id: uid,
    email,
    fullName: displayName,
    phone: null,
    avatarUrl: null,
    departmentId: null,
    position: "",
    gender: "MALE",
    militaryDocument: null,
    role: "EMPLOYEE",
    approvalStatus: "PENDING",
    isMilitaryLiable: false,
    isActive: false,
    updatedAt: now,
    createdAt: now
  }, { merge: true });
});

/**
 * Назначение роли через Firebase Custom Claims.
 * Вызывать только администратором (claim admin=true).
 *
 * payload: { email: string, role: "HR" | "EMPLOYEE" }
 */
exports.setUserRole = functions.https.onCall(async (data, context) => {
  if (!context.auth || context.auth.token.admin !== true) {
    throw new functions.https.HttpsError(
      "permission-denied",
      "Требуются права администратора"
    );
  }

  const email = typeof data?.email === "string" ? data.email.trim().toLowerCase() : "";
  const role = typeof data?.role === "string" ? data.role.trim().toUpperCase() : "";
  if (!email || (role !== "HR" && role !== "EMPLOYEE")) {
    throw new functions.https.HttpsError(
      "invalid-argument",
      "Передайте email и роль HR/EMPLOYEE"
    );
  }

  const user = await admin.auth().getUserByEmail(email);
  await admin.auth().setCustomUserClaims(user.uid, { role });

  const now = Date.now();
  const patch = {
    role,
    updatedAt: now,
  };
  if (role === "HR") {
    patch.approvalStatus = "APPROVED";
    patch.isActive = true;
  }
  await admin.firestore().collection("users").doc(user.uid).set(patch, { merge: true });

  return { ok: true, uid: user.uid, role };
});
