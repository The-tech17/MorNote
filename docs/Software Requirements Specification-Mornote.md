# **Minor Project (Course Code: CB3PC05) Report**

Software Requirement Specification (SRS) for **MorNote** (according to the standard IEEE 830 format

## **1\. Introduction**

### *1.1 Purpose*

This document specifies the software requirements for MorNote, a secure mobile diary application. It details the functional, non-functional, security, and interface requirements for version 1.0.0.

### *1.2 Document Conventions*

* **Font Standard:** Standard formatting is used throughout. Bold formatting emphasizes code-level component keys or configuration states.  
* **Priority Definitions:** Requirements are categorized as High (essential for core release lifecycle), Medium (advanced functionality), or Low (aesthetic/future enhancements).

### *1.3 Intended Audience and Reading Suggestions*

This SRS is written for project supervisors, external examiners, and technology developers. Developers should focus on Section 3 (Specific Requirements), while evaluators should prioritize Section 2 (Overall Description) to understand project scope and system boundaries.

### *1.4 Product Scope*

MorNote is designed to address a real-world need for private digital journaling. It implements an immediate cryptographic mutation layer (ROT13 rotation cipher) on user text inputs before database storage. The app integrates secure remote live configurations via Firebase Remote Config and enforces zero-tolerance data confidentiality through local biometric-triggered device data sanitization (self-destruct) and external automated SMTP cloud alerting.

### *1.5 References*

* IEEE Std 830-1998, IEEE Recommended Practice for Software Requirement Specifications.  
* Firebase Authentication & Remote Config Standard Architecture Guides.  
* Android Jetpack Biometric API Framework Specifications.

## **2\. Overall Description**

### *2.1 Product Perspective*

MorNote is a standalone full-stack mobile application operating within the Android ecosystem. It leverages global cloud infrastructure singletons (Firebase Auth, Firebase Remote Config) for operations that do not store private user notes in the cloud, maintaining local device storage boundaries (Room SQLite) for data preservation.

### *2.2 Product Functions*

* **Secure Authentication:** Validates user identity using the Credential Manager API and Firebase Google Auth Provider.  
* **Secure Cryptographic Journaling:** Intercepts plain text inputs in the lifecycle and converts them to rotation cipher variants prior to saving.  
* **Asynchronous Reactive UI Wiping:** Swaps screen visibility values dynamically upon logout hooks.  
* **Dynamic Remote Config Parsing:** Pulls live parameter updates from the console dashboard without app redistributions.  
* **Confidentiality Guard (Self-Destruct Sequence):** Tracks sequential biometric verification failures, initiates database row sanitization, and dispatches external cloud-alerting payloads.

### *2.3 User Classes and Characteristics*

* **End User:** Requires a secure, private diary application with zero complex overhead. Expects an automated experience requiring no specialized technical expertise regarding encryption processes.

### *2.4 Operating Environment*

* **Platform:** Android OS.  
* **Minimum API Level:** API 29 (Android 10\) or higher.  
* **Hardware Dependencies:** Integrated biometric sensor hardware (Fingerprint scanner).  
* **Network Requirements:** Active internet connection required for initial authentication, remote config polling, and outbound email alerts. Note storage runs entirely offline.

### *2.5 Design and Implementation Constraints*

* **State Management:** Strict adherence to Jetpack Compose single-source-of-truth architectures (State Hoisting) is required.  
* **Memory Footprint Boundaries:** Zero static field allocations of instances holding system Context singletons to eliminate runtime memory leaks.  
* **Database Isolation Constraints:** room SQLite database files must remain fully isolated to internal sandboxed app directories. No plain text data may leak into device standard logs.

### *2.6 Assumptions and Dependencies*

* **Sensor Reliability:** Assumes the underlying device hardware provides reliable biometric verification signals.  
* **Cloud Endpoint Availability:** Assumes continuous operational availability of the Google Firebase Console infrastructure and external EmailJS API services.

## **3\. Specific Requirements**

### *3.1 External Interface Requirements*

#### 3.1.1 User Interfaces

* **UI-01 (Authentication Screen):** Minimal layout featuring branding headers and a clean "Sign in with Google" button asset.  
* **UI-02 (Notes Dashboard Screen):** Implements a side-navigation menu drawer (Hamburger Icon trigger) overlaying a Material 3 LazyColumn containing individual notes cards. Each note card displays the raw mutated cipher string and a "tap to unlock" trailing hint indicator.  
* **UI-03 (Note Detail Screen):** Two standard text inputs capturing Title and Content data properties, accompanied by a fixed execution button labeled "Save MorNote (Morphed)".

#### 3.1.2 Software Interfaces

* **Firebase Authentication SDK:** Handles user access token exchanges.  
* **Room Database Runtime:** Layer managing local persistence interfaces.  
* **Coil Image Loading Engine:** Handles asynchronous parsing of Google account avatar URL objects safely into UI shapes.  
* **OkHttp Client:** Manages network request calls for outbound JSON post payloads.

### *3.2 Functional Requirements*

#### 3.2.1 Component: Authentication Gate

* **FR-Auth-01 (Access Isolation):** The system shall intercept execution at MainActivity launch and block navigation to note screens if currentUser evaluates to null.  
* **FR-Auth-02 (State Sync):** Upon successful trigger of onLoginSuccess, property delegation observers shall instantly refresh the Compose hierarchy to display the system dashboard.  
* **FR-Auth-03 (Session Severance):** Triggering the logout interface button shall immediately clear local reference tokens, invoke the underlying Firebase sign-out method, and instantly render the authentication view.

#### 3.2.2 Component: Note Management & Cryptographic Layer

* **FR-Note-01 (Encryption Enforcement):** The system shall morph plain text inputs using the CipherEngine rotation mechanism prior to executing database Insert or Update SQL tasks. Plain text inputs shall never be stored directly in database files.  
* **FR-Note-02 (Data Mutation Lifecycle):** Database save routines must explicitly match existing primary keys during Update operations to modify existing rows rather than creating duplicate note objects.  
* **FR-Note-03 (Decryption Boundary):** Notes list cards shall display only the raw cipher code preview string. Plain text versions shall only be decrypted in memory on the NoteDetailScreen after successful biometric verification.

#### 3.2.3 Component: Remote Config Polling

* **FR-Config-01 (Leak-Safe Instance Management):** The remote config manager object shall access the singleton instance dynamically via property getters (get() \= Firebase.remoteConfig), avoiding long-lived static context references.  
* **FR-Config-02 (Live News Feed):** The system shall asynchronously poll the server dashboard using fetchAndActivate() methods on a background worker coroutine thread and expose string updates to the user's FeedScreen.

#### 3.2.4 Component: Self-Destruct Guard

* **FR-Guard-01 (Failure Tracking):** The system shall count consecutive BiometricPrompt verification mismatches within a given session. Successful validations shall reset the counter to zero.  
* **FR-Guard-02 (Data Sanitization):** Upon recording the third consecutive biometric mismatch, the app shall invoke an immediate database deletion command matching the target note ID.  
* **FR-Guard-03 (Outbound Threat Alerting):** Immediately following local file deletion, the system shall assemble an outbound HTTP POST payload containing the note title and the signed-in user's email address, dispatching it to the security email endpoint.  
* **FR-Guard-04 (UI Evacuation):** After completing the data sanitization process, the screen context shall immediately clear, show a confirmation message, and navigate back to the main note list screen.

### *3.3 Non-Functional Requirements*

#### 3.3.1 Performance Requirements

* **Crypto Latency:** Cryptographic cipher conversions (morphing/unmorphing) must execute in under 50 milliseconds, ensuring zero perceptible delay during save actions.  
* **Memory Leak Safety:** Zero static memory leaks are permitted across lifecycle updates.

#### 3.3.2 Security Requirements

* **Cryptographic Isolation:** Plain text strings must never touch the database disk layer.  
* **Outbound Data Leak Protection:** Outbound security alert payloads must never bundle or leak the actual contents of the protected diary notes.