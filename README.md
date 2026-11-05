# MorNote: Your Secure Diary

> **A Zero-Knowledge, Hardware-Backed Mobile Diary Architecture with Active Anti-Forensic Defense and Zero-Leakage Recovery**

[![Platform](https://img.shields.io/badge/Platform-Android_10.0+_(API_29--36)-3DDC84.svg?style=flat&logo=android)](https://www.android.com)
[![Language](https://img.shields.io/badge/Language-Kotlin_2.0+-7F52FF.svg?style=flat&logo=kotlin)](https://kotlinlang.org)
[![UI Toolkit](https://img.shields.io/badge/UI-Jetpack_Compose_Material3-4285F4.svg?style=flat&logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![Cipher](https://img.shields.io/badge/Cipher-AES--256--GCM_(AEAD)-00599C.svg?style=flat)](https://csrc.nist.gov/publications/detail/sp/800-38d/final)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

---

## 📌 Overview

**MorNote** is an advanced, privacy-first mobile journaling and note-taking application designed to establish an absolute zero-knowledge security boundary on modern mobile devices. 

While mainstream and commercial note-taking applications (such as ClevNote and Samsung Notes) rely on cosmetic UI-only locks, static key derivation from APK bytecode, unauthenticated cipher modes (AES-CBC), and insecure logical backup channels, **MorNote** redesigns client-side personal data protection from the ground up.

MorNote couples authentication directly to data encryption, enforces hardware-isolated cryptographic key storage via the **Android KeyStore (TEE/StrongBox)**, actively defends against physical attacks via a **3-Strike Self-Destruct Protocol with Out-of-Band Alerting**, and introduces a cross-device **Zero-Knowledge Backup Recovery Protocol**.

---

## 🛡️ Core Security Architecture & Pillars

```
+─────────────────────────────────────────────────────────────────────────────+
|                                MorNote Core                                 |
|                                                                             |
|   +────────────────────────+            +───────────────────────────────+   |
|   │ Jetpack Compose UI     │            │ Biometric / Fallback Auth     │   |
|   │ (Ciphertext Previews)  │            │ (BiometricPrompt / RASP)      │   |
|   +───────────┬────────────+            +───────────────┬───────────────+   |
|               │                                         │                   |
|               ▼                                         ▼                   |
|   +─────────────────────────────────────────────────────────────────────+   |
|   │ NoteDetailViewModel (Lifecycle & Rate-Limiting Gate)                │   |
|   +───────────┬─────────────────────────────────────────┬───────────────+   |
|               │ (Plaintext Stream)                      │ (3-Fail Trigger)  |
|               ▼                                         ▼                   |
|   +────────────────────────+            +───────────────────────────────+   |
|   │ CipherEngine           │            │ Active Defense Coordinator    │   |
|   │ AES-256-GCM / KeyStore │            │ Local Wipe + Key Purge        │   |
|   +───────────┬────────────+            +───────────────┬───────────────+   |
|               │ (Encrypted Blob)                        │ (SMTP Alert)      |
|               ▼                                         ▼                   |
|   +────────────────────────+            +───────────────────────────────+   |
|   │ Room Database (SQLite) │            │ Outbound Security Endpoint    │   |
|   │ (contentMorse Column)  │            │ (EmailJS / REST API)          │   |
|   +────────────────────────+            +───────────────────────────────+   |
+─────────────────────────────────────────────────────────────────────────────+
```

### 1. Authenticated AEAD Encryption (AES-256-GCM)
* **Zero Plaintext at Rest:** Raw text and body fields are never written to disk in unencrypted format.
* **Galois/Counter Mode (GCM):** Deprecates vulnerable AES-CBC modes. Every record is encrypted using a unique, cryptographically secure 12-byte random Initialization Vector (IV) and protected by a 128-bit Galois Authentication Tag ($T$), rendering the database completely immune to bit-flipping, padding oracle, and offline tampering attacks.
* **Hardware-Backed Key Isolation:** The 256-bit symmetric master key is generated and isolated inside the device's hardware enclave (ARM TrustZone / TEE / StrongBox Keymaster) via the `AndroidKeyStore`.

### 2. Active Threat Mitigation (3-Strike Self-Destruct & Out-of-Band Alerts)
* **Continuous Rate Limiting:** Active runtime monitoring tracks failed biometric and credential unlock events.
* **Cryptographic Self-Destruct:** Upon reaching 3 consecutive unauthorized attempts, MorNote immediately purges the volatile key in RAM, deletes the affected database entry, and invokes `KeyStore.deleteEntry("MorNoteMasterKey")` to destroy the hardware key pointer.
* **Out-of-Band Incident Signaling:** An asynchronous worker immediately dispatches a TLS-encrypted alert payload containing incident metadata to the owner's pre-configured email address.
* **Runtime Application Self-Protection (RASP):** Environment integrity verification actively checks for superuser binaries (`/system/xbin/su`), dangerous build tags, and Magisk/Frida hooking frameworks.

### 3. Anti-Forensic Zero-Knowledge Backup Engine
* **Hardened Manifest:** Standard logical backups are completely disabled (`android:allowBackup="false"` and `android:fullBackupOnly="false"`), preventing unrooted forensic extraction via Android Debug Bridge (`adb backup`).
* **Cross-Device Portability:** Notes can be exported into portable `.mornote` containers encrypted with keys derived independently via **PBKDF2-HMAC-SHA256 / Argon2id** (with 100,000+ iterations and a 16-byte cryptographically secure salt). Decryption is decoupled from device-bound hardware identifiers, preventing the migration failure flaw present in Samsung Notes.

### 4. Client-Side Vault AI Assistant (Ephemeral Co-Pilot)
* Integrated with Google GenAI SDK (`gemini-1.5-flash` / `gemini-2.0-flash`) or Groq REST API.
* Runs on demand in volatile memory only after local decryption.
* Summarizes personal notes, provides structural suggestions, and evaluates passphrase strength without storing chat history in persistent storage.

---

## 🔬 Comparative Forensic Analysis

MorNote's design specifically remediates the published vulnerabilities identified in mobile forensic literature (e.g., Park et al., 2020):

| Security Feature | ClevNote (v2.21.0) | Samsung Notes (v4.1.03.1) | **MorNote (Proposed)** |
| :--- | :--- | :--- | :--- |
| **Cipher Algorithm** | AES-256-CBC (Unauthenticated) | AES-256-CBC (Unauthenticated) | **AES-256-GCM (AEAD)** |
| **Integrity Validation** | None (Vulnerable to bit-flipping) | None | **128-bit Galois Authentication Tag** |
| **Key Derivation Strategy** | Static keys hardcoded in bytecode (`FK1`, `FK2`) | Plaintext device identifiers in XML (`NotesDeviceInfo.xml`) | **Hardware Enclave KeyStore + Argon2id / PBKDF2** |
| **Access Control Coupling** | Decoupled (Cosmetic UI check only) | Decoupled (Password doesn't encrypt data) | **Strictly Coupled ($K_{\text{derived}} \implies K_{\text{DB}}$)** |
| **ADB Backup Exposure** | Vulnerable (`android:allowBackup="true"`) | Disabled (`allowBackup="false"`) | **Disabled + Zero-Knowledge Export Container** |
| **Device Migration** | Vulnerable via unencrypted backup | Broken (Omission of device identifiers) | **Seamless via Passphrase Derivation** |
| **Tamper Defense** | Passive (Unlimited attempts) | Passive (Basic UI lockout counter) | **Active: 3-Strike Self-Destruct + Outbound Alert** |
| **Plaintext Storage** | Plaintext SQLite columns | Unlocked notes stored in cleartext | **Zero Plaintext at Rest (Always Encrypted)** |

---

## 🛠️ Tech Stack & Architecture

* **Programming Language:** [Kotlin 2.0+](https://kotlinlang.org/)
* **UI Framework:** [Jetpack Compose (Material3)](https://developer.android.com/jetpack/compose)
* **Architecture Pattern:** Clean Architecture + MVVM (Model-View-ViewModel) + Single Source of Truth (SSOT)
* **Local Persistence:** [Room Persistence Library (SQLite)](https://developer.android.com/training/data-storage/room)
* **Asynchronous Runtime:** Kotlin Coroutines & Reactive `StateFlow`
* **Cryptography & Security:**
  * `AndroidKeyStore` (Hardware TEE / StrongBox Enclave)
  * `javax.crypto.Cipher` (`AES/GCM/NoPadding`)
  * `androidx.biometric.BiometricPrompt`
  * RootBeer Library (Root & Tamper Detection)
* **Network & Cloud (Free Tier):**
  * Firebase Remote Config (Bulletin news & configuration)
  * Google GenAI SDK / Groq API (Ephemeral on-device Vault AI)
  * EmailJS REST API (Out-of-band security alerting)

---

## 📁 Project Structure

```
com.sanjana.mornote/
├── data/
│   ├── local/              # Room Database, DAOs, and Note Entities
│   ├── remote/             # Remote Config & EmailJS API dispatchers
│   └── repository/         # Repository implementations
├── domain/
│   ├── model/              # Domain entities
│   └── repository/         # Repository interfaces
├── presentation/
│   ├── feed/               # FeedScreen & Bulletin timeline UI
│   ├── list/               # NoteListScreen (Ciphertext previews)
│   ├── detail/             # NoteDetailScreen (Plaintext reader & AI drawer)
│   └── theme/              # Material3 color, typography, and styling
├── security/
│   ├── CipherEngine.kt     # AES-256-GCM and AndroidKeyStore management
│   ├── AuthManager.kt      # Biometric gating & PBKDF2 Master PIN hashing
│   ├── SecurityGuard.kt    # Root detection and environment integrity checks
│   ├── BackupManager.kt    # Encrypted zero-knowledge container exporter
│   └── VaultAiAssistant.kt # Client-side Google GenAI / Groq integration
└── viewmodel/              # StateFlow-driven ViewModels
```

---

## 🚀 Getting Started

### Prerequisites
* Android Studio Ladybug (2024.2.1) or newer
* Android SDK 35 / 36 (Minimum SDK: 29)
* Java Development Kit (JDK) 17 or 21

### Installation & Build

1. **Clone the repository:**
   ```bash
   git clone https://github.com/your-username/MorNote.git
   cd MorNote
   ```

2. **Configure API Keys:**
   Create a `local.properties` file in the root directory (if not already present) and add your free Gemini API key:
   ```properties
   GEMINI_API_KEY=your_gemini_api_key_here
   ```

3. **Build the Project:**
   ```bash
   ./gradlew clean assembleDebug
   ```

4. **Run on Device / Emulator:**
   * Deploy via Android Studio or command line:
     ```bash
     ./gradlew installDebug
     ```
   * *Note:* Biometric features require a physical test device with fingerprint/face sensors or an emulator configured with fingerprint emulation via ADB.

---

## 🧪 Forensic & Security Verification

To verify MorNote's security boundaries locally:

```bash
# 1. Verify ADB Logical Backup Protection
adb backup -f backup.ab com.sanjana.mornote
# Result: Generates an empty 0-byte or 47-byte header file (Backup denied by manifest)

# 2. Inspect Raw Database for Plaintext Leaks
adb shell "su -c 'strings /data/data/com.sanjana.mornote/databases/morse_notes_db'"
# Result: Zero plaintext matches for note bodies; all entries are Base64 GCM payloads

# 3. Simulate Offline Bit Tampering (Integrity Check)
# Modifying any byte within the database row content raises:
# javax.crypto.AEADBadTagException: Tag mismatch! (Halts execution safely)
```

---

## 📄 Academic Research & Attribution

This project forms the core technical implementation of the academic research paper:
> **"MorNote: A Secure, Zero-Knowledge Client-Side Note-Taking Android Application with Active Forensic Threat Mitigation"**  
> *Sanjana Londhe, Department of Computer Science & Engineering, Medi-Caps University, Indore.*

If you use or build upon MorNote's architecture, please cite:
```bibtex
@article{mornote2026,
  author    = {Sanjana Londhe},
  title     = {MorNote: A Secure, Zero-Knowledge Client-Side Note-Taking Android Application with Active Forensic Threat Mitigation},
  journal   = {Technical Preprint, Medi-Caps University},
  year      = {2026}
}
```

---

## 📜 License

Distributed under the MIT License. See `LICENSE` for more information.