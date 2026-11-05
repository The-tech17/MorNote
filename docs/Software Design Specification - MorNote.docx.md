Software Design Specification for MorNote based on event-driven (function-oriented) technology

**Table of Contents**

**[1\. Introduction	3](#introduction)**

[1.1. Purpose	3](#purpose)

[1.2. Document Conventions	3](#document-conventions)

[1.3. Intended Audience and Reading Suggestions	3](#intended-audience-and-reading-suggestions)

[1.4. References	3](#references)

[1.5. Scope	3](#scope)

[**2\. Analysis Model	3**](#analysis-model)

[2.1. Methodology Used	3](#methodology-used)

[2.2. Use Case diagram	4](#use-case-diagram)

[2.3. ER Model	4](#er-model)

[2.4. Data Flow Diagram	5](#data-flow-diagram)

[2.4.1. Process Specifications (P-Specs)	5](#process-specifications-\(p-specs\))

[2.4.1.1. P-Spec 1.0: Authenticate User & Release Key	5](#p-spec-1.0:-authenticate-user-&-release-key)

[2.4.1.2. P-Spec 2.0: Authenticated Note Encrypt/Decrypt	5](#p-spec-2.0:-authenticated-note-encrypt/decrypt)

[2.4.1.3. P-Spec 3.0: Active Threat Mitigation (3-Strike Response)	5](#p-spec-3.0:-active-threat-mitigation-\(3-strike-response\))

[2.4.1.4. P-Spec 4.0: Zero-Knowledge Vault Export/Import	6](#p-spec-4.0:-zero-knowledge-vault-export/import)

[2.5. Control Flow Diagram	6](#control-flow-diagram)

[2.5.1. Control Specs (State-Decision Table)	7](#control-specs-\(state-decision-table\))

[2.6. State Transition Diagram	8](#state-transition-diagram)

[**3\. Design Model	9**](#design-model)

[3.1. Architectural Design	9](#architectural-design)

[3.1.1. System Architecture Diagram	9](#system-architecture-diagram)

[3.1.2. Diagram and description of Architectural Design	10](#diagram-and-description-of-architectural-design)

[3.2. Database Design	10](#database-design)

[3.2.1. Data Dictionary	10](#data-dictionary)

[3.2.2. Normalization	11](#normalization)

[3.3. Component Design	11](#component-design)

[3.3.1. Package Diagram	11](#package-diagram)

[3.3.2. Deployment Diagram	12](#deployment-diagram)

[3.4. Interface Design	12](#interface-design)

[3.4.1. Screenshots	12](#screenshots)

[**Appendix B1: Glossary	13**](#appendix-b1:-glossary)

[**Appendix B2: To be Determined List	13**](#appendix-b2:-to-be-determined-list)

1. # **Introduction** {#introduction}

   1. ## **Purpose** {#purpose}

   This SDS provides the detailed architectural, component-level, and object-oriented design for the MorNote secure diary application. 

   2. ## **Document Conventions** {#document-conventions}

   Follows standard UML 2.5 notation specifications.

   3. ## **Intended Audience and Reading Suggestions** {#intended-audience-and-reading-suggestions}

   Targeted at evaluators, academic project guides, Android software engineers, and digital forensics auditors. Recommended reading sequence: Section 1 $\rightarrow$ Section 2 $\rightarrow$ Section 3\.

   4. ## **References** {#references}

      1. MorNote Software Requirements Specification (SRS) v1.1.0 (August 2026).  
      2. Android Developers Architecture Guide: Modern Android Application Architecture (Google, 2026).

   5. ## **Scope** {#scope}

   MorNote provides an isolated, encrypted mobile storage environment for personal reflections and sensitive credentials, enforcing authenticated AES-256-GCM encryption, active tamper detection, and cross-device zero-knowledge data export. 

2. # **Analysis Model** {#analysis-model}

   1. ## **Methodology Used** {#methodology-used}

   MorNote is designed using **Object-Oriented Programming (OOP)** combined with **Functional Reactive Programming (FRP)** in Kotlin. The development follows the **Agile Iterative Process Model**, refining cryptographic verification modules through continuous automated testing. 

   

   

   

   

   

   

   

   2. ## **Use Case diagram** {#use-case-diagram}

   

   

   

   

   

   

   

   

   

   

   

   

   

   

   

   

   3. ## **ER Model** {#er-model}

   

   

   

   

   

   

   

   

   

   

   

   4. ## **Data Flow Diagram** {#data-flow-diagram}

   

   

   

   

   

   

   

   

   

   

   

   

   

   

      1. ### **Process Specifications (P-Specs)** {#process-specifications-(p-specs)}

         1. #### **P-Spec 1.0: Authenticate User & Release Key** {#p-spec-1.0:-authenticate-user-&-release-key}

* Input: Biometric Fingerprint Vector / 6-digit Master PIN.  
* Output: Decrypted in-memory Master Key (\$K\_{DB}\$) or Authentication Error Signal.  
* Logic: Prompt BiometricPrompt. If successful, fetch wrapped key from AndroidKeyStore, unwrap into volatile memory, and reset consecutive\_fails \= 0 in EncryptedSharedPreferences. If failed, route to P-Spec 3.0.

  2. #### **P-Spec 2.0: Authenticated Note Encrypt/Decrypt** {#p-spec-2.0:-authenticated-note-encrypt/decrypt}

* Input: Plaintext String (\$M\$) OR Ciphertext Base64 Blob (\$C \\mathbin{\\Vert} T\$).  
* Output: Stored Ciphertext Record OR Rendered Plaintext String.  
* Logic: Generate 12-byte secure random \$IV\$. Execute AES/GCM/NoPadding using \$K\_{DB}\$. Calculate 128-bit authentication tag \$T\$. On read, verify tag \$T\$; if verification fails, throw AEADBadTagException and display \[Decryption Error\].

  3. #### **P-Spec 3.0: Active Threat Mitigation (3-Strike Response)** {#p-spec-3.0:-active-threat-mitigation-(3-strike-response)}

* Input: Authentication Failure Event (\$N\_{fails}\$).  
* Output: Database Record Sanitization, KeyStore Purge, Outbound SMTP Alert.  
* Logic: Increment \$N\_{fails}\$. If \$N\_{fails} \< 3\$, prompt retry/Master PIN fallback. If \$N\_{fails} \\ge 3\$, invoke NoteDao.deleteById(), execute KeyStore.deleteEntry("MorNoteMasterKey"), construct alert JSON { note\_title, timestamp }, and invoke background worker to POST payload to out-of-band email relay.

  4. #### **P-Spec 4.0: Zero-Knowledge Vault Export/Import** {#p-spec-4.0:-zero-knowledge-vault-export/import}

* Input: User Backup Passphrase (\$P\_{backup}\$), File URI.  
* Output: Encrypted .mornote file container.  
* Logic: Generate 16-byte random salt. Derive 256-bit \$K\_{backup}\$ via Argon2id (or PBKDF2 with 100,000 iterations). Encrypt SQLite dump using AES-256-GCM. Save combined payload { salt, iv, ciphertext, tag } to user-selected SAF storage.

  5. ## **Control Flow Diagram** {#control-flow-diagram}

     1. ### **Control Specs (State-Decision Table)** {#control-specs-(state-decision-table)}

| Condition / Input Signal  | State: Normal  | State: Attempt 1 Fail  | State: Attempt 2 Fail  | State: Strike 3 Trigger  | Action / Control Output Issued |
| :---- | :---- | :---- | :---- | :---- | :---- |
| **AUTH\_SUCCESS** | True | True | True | N/A | Reset counter (Nfails \= 0), emit KEY\_RELEASE\_SIGNAL |
| **AUTH\_FAILURE (Nfails \< 3\)** | False | True | True | N/A | Increment counter, emit DISPLAY\_PIN\_FALLBACK\_SIGNAL |
| **AUTH\_FAILURE (Nfails\< \= 3\)** | N/A | N/A | N/A | True | Assert ACTIVE\_SELF\_DESTRUCT\_SIGNAL (Purge Key \+ Row \+ Alert) |
| **USER\_CANCEL** | False | False | False | False | Maintain counter unchanged, route to NoteListScreen |
| **ROOT\_DETECTED** | True | True | True | True | Assert TAMPER\_LOCKOUT\_ SIGNAL, block crypto operations |


  


  


  


  


  


  


  


  

  6. ## **State Transition Diagram** {#state-transition-diagram}

3. # **Design Model** {#design-model}

   1. ## **Architectural Design** {#architectural-design}

      1. ### **System Architecture Diagram** {#system-architecture-diagram}

   

   

   

   

   

   

   

   

   

   

   

   

   

   

   

   

   

   

   

   

   

   

   

   

   

   

   

      2. ### **Diagram and description of Architectural Design** {#diagram-and-description-of-architectural-design}

   ![][image1]

   2. ## **Database Design** {#database-design}

      1. ### **Data Dictionary** {#data-dictionary}

         * Table Name: **note\_table**  
           * Primary Key: **id (Integer, Auto-Increment)**

| Field Name | Data Type | Nullable | Content Description | Supplementary Information PDF |
| :---- | :---- | :---- | :---- | :---- |
| id | INTEGER | No | Unique record primary key | Auto-generated by Room |
| title | TEXT | No | User-entered note heading | Stored as plaintext string for list preview |
| contentMorse | TEXT | No | AES-256-GCM encrypted payload | Base64 encoded string containing IV \+ Ciphertext \+ Tag |
| timestamp | INTEGER | No | Unix creation timestamp | Milliseconds epoch representation |

   

      2. ### **Normalization** {#normalization}

   The SQLite schema is structured in Third Normal Form (3NF):

* 1NF: All attributes are atomic (no repeating groups or multi-valued fields).  
* 2NF: All non-key attributes (title, contentMorse, timestamp) depend fully on the primary key id.  
* 3NF: Zero transitive dependencies exist between non-key fields.

  3. ## **Component Design** {#component-design}

     1. ### **Package Diagram** {#package-diagram}

  Organizes components cleanly into ui.feed, ui.detail, ui.list, data.local, data.remote, and security packages.


  


  


  





     2. ### **Deployment Diagram** {#deployment-diagram}

  Demonstrates the runtime deployment of MorNote.apk inside the Android application sandbox, communicating with the hardware TEE enclave via KeyStore IPC and external cloud hosts over TLS.

![][image2]

4. ## **Interface Design** {#interface-design}

   1. ### **Screenshots** {#screenshots}

## 

   

   # 

   

   

   

   

    

   

   

   

   

   

   

   

   

   

   

   

   

   

   

   

   

   

   

   

   

   

   

   

   

   

   

# **Appendix B1: Glossary** {#appendix-b1:-glossary}

* AEAD: Authenticated Encryption with Associated Data.  
* GCM: Galois/Counter Mode.  
* KDF: Key Derivation Function (e.g., PBKDF2, Argon2id).  
* TEE: Trusted Execution Environment (Hardware-isolated security zone).  
* RASP: Runtime Application Self-Protection.

# **Appendix B2: To be Determined List** {#appendix-b2:-to-be-determined-list}

All functional requirements, cryptographic primitives (AES-256-GCM, Android KeyStore), and access-control mechanisms defined in this document have been implemented and verified. The TBD list above tracks architectural enhancements and research extensions planned for subsequent version revisions.

 

| Item ID | Component / Area | Description of Unresolved Item / Pending Decision | Responsible Entity | Target Milestone / Resolution Date |
| :---- | :---- | :---- | :---- | :---- |
| TBD-01 | Post-Quantum Cryptography (PQC) | Evaluate migration from standard AES key encapsulation to a hybrid Post-Quantum Key Encapsulation Mechanism (ML-KEM / Kyber-768) within the hardware KeyStore wrapper. | Lead Developer | Phase 2 / Major Revision v2.0 |
| TBD-02 | Searchable Symmetric Encryption (SSE) | Selection of a cryptographic tokenization indexing scheme (e.g., BlindBase / CryptDB dynamic index) to support full-text search across encrypted note contents without decrypting into runtime memory. | Lead Developer | Minor Revision v1.2 |
| TBD-03 | Secondary Out-of-Band Alert Relays | Integration of secondary fallback alert webhooks (e.g., Telegram Bot API / Pushbullet API) in addition to EmailJS for environments where outbound SMTP/REST relay is throttled. | Lead Developer | v1.1.5 Patch |
| TBD-04 | Hardware StrongBox Availability Check | Refinement of fallback behavior when running on legacy devices lacking dedicated hardware-isolated StrongBox Keymaster modules (graceful fallback to ARM TrustZone TEE vs. software emulated keys). | Lead Developer | End-Semester Testing Phase |

[image1]: <data:image/png;base64,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>

[image2]: <data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAoYAAAF8CAYAAABSa3qbAABvSklEQVR4XuydC7gVU//Hp9J7w6tyjpPOUafe5C0VSn/kllvkmm4k6SpKRCK6kPJWdA9JqRC6UOgql4qiKylCkkJJusi1EOv/+66Z2U3rnF3nsvc+a/b+fp7n98zsWWtmz8yey2evWbOW4xBCCCGEEEIIIYQQQgghhBBCCCGEEEIIIYQchGOOOaZuuXLlzmIwGAwGgxH/yMjIqG7eiwmxhrJly/4pcngRg8FgMBiM+IeI4W3mvZgQa8BBak4jhBBCSPwoW7bs/eY0QqzA+/fCYm1CCCEkQYgYDjanEWIFEEM5QOea0wkhhBASHzIyMg43pxFiBf6jZMghg8FgMBiMhMVf5j2ZkCKHdQwJIYSQIuEwcwIhRQ7FkBBCCCkSKIbEPiiGhBBCSJFAMST2QTEkhBBCigSKIbEPiiEhhBBSJFAMiX1QDAkhhJAigWJI7INiSAghhBQJFENiHxRDQgpEKW845ICpjtPPGyqJt4MJQn3jM6jsDZccMNVx9hqf3/WGf/eGWH40CtKjwlSJzxx3uV0linvjD3vpu7yhz87A+EOB8RyULVv2hYyMjDEMRrKFU3ixK+z8hMQeiiEhBSIohtO88ZckfvHGl0l8LFFToqLjita5EkdIXCgxxct3nzfc7A1f9Ia/STwu8S+J1hKTHVfAfDFs5rhSie+e7uxf3nMSXzqu3JWVeMGb/qTjftc4x5XAILg5QfQghi0cd94KjiuG33p5fBGF+E70AtuFdVrppeWKiOGraWlp5RiMZAuRw2vM4z2fUAyJfVAMCSkQEEPI0nCJdIlWEq9IbPTSMV7FccXqd4mTHFfm9kngnEMJ4F0Sn0iUlrjCnc05WmKo44rhexJ7HFfGfnbc0rtgieEpjrvMOx23NLKW467XeC/vKoksx5VKSCS6vhwpsVDifIn/SpzguMIKGYQY/uqNz5T4ynGFsYnEJscV0DcknpHwe2zAevnyGRWRw2rmNELCTrly5dLMafmEYkjsg2JISIHwSwyHeePtJWY4B4phI4kdjitoKKXzxRDCiHz3Oq6MtXH2l8ihRPFRxxXDRY5bAtnDcQXsAWe/GP5D4kyJqyRukrjBccUQ80MMn5BY7bilhqMdtwQQYni/xHpvvpMdV1gHOu73Y112O64YQkghhq9LrPDSsTysc13HXWfII/IfUgwzMjKwPoQkI4WRu8LMS0h8oBgSUiCO9IYo9YP4gTUSowLjELzTJFo67uPWiyX+7bgSuVyipONKHIQLIA2PjsH7Ek9L/NNxSwTxaBqPif/mpWP5xztuyV9T7zMea2F+jGPe4yT8Eg0IHh5Nd3TcdcGjbZ8F3nCW465DJ8ddd8zT3Uub5w19Fkrc7LiP0lGyeUjKlStXnsFItkBVCfNYzwcUQ2IfFENCihSU9MUDPI5OFOZLNrly7LHH3sFgJFuIGPp/ngoCxZDYR1AM09LSjkxPTz+CwWAkJv7xj39UNKfFIooXL97XnHaoCF4X8glKDgkh+YdiSOwjKIbyzydP//wJIclHRkaG39QOISQxUAyJfRhieF4wjRCSOsj539mcRgiJKxRDYh9BMSxXrtwJcnPwK88TQlKMjIwMtHdICEkMFENiH6YYZmZmoh214BuLhJAU4dhjj0XTN4SQxEAxJPZhiiGGcnNokZGRUYPBYKRWyLlfu1zhG+0lhOQNiiGxj9zEsGzZsmhIlxCSgkAOzWkkYaCtyryAHmz8xs6D9DEnGKCdS3wHesIJ0tD4TBIDxZDYB8WQEBKEYlikbHLcRszReHmGRDHvMwQCfVP7Qof+sA8PTEeg+8N3HLcqUAnHFUc0cl7JC3CU4/Z8g+4UMQ960EH0dNzvRPeMwe8h8YViSOyDYkgICUIxLFL+dFyxqykxxXG7XDxR4liJco7bDSJAH9Z+N4rouvBHiTmO20POjY7bIw7YLIF+qm9x3D66IYaYD8tDt427JJY4rhhCLCGSL0tcK3GhQ+INxZDYB8WQEBKEYlikoD9tiCDkbYA3zRdDlAD6MrhFoq3jPgLe5rhyiH6wUWKILtqecdwuErdLHOO486E7RIghumhEl469HLfkEUAMMR1dIl7p5W/vpZH4QTEk9kExJIQEoRgWKXUlTpX4j+P2p43+qyFsvhj6JYb+o+QXJHY4rhguddwSw78kfnbc/qy/c1wxRP/XmB9iiEfGaH0CYoh5IZkQw5MdVwwrOK5U5laHkcQWiiGxD4ohISQIxZCQhEExJPZBMSSEBKEYEpIwKIbEPiiGVoDHO9WNaXjcE6SU41YeB82d/XWN/GFVx61IboI3DlFHKRpYLsCjo2h8K7HInBgA6w+elxgaTAiANx1NenvDR53920aKmHiKYUZGRnNGbMLct7FCrv/3y/LbJWvIfe46c5uLEIohsQ+KoRVAkO6Q+ExinePKHsRwk+NWLEe9H8jd/7z8qBeENxQhfb5QPihxr0RXx32T8QPHberiE2d/ZXbUGcLbjk847huHvtABfM9aCfSX21dinMQVXhrEcLHjyh3W4SeJdyX+z3GX68ulL4a3SdwkscdxL3y/Ou68lzvum5YTHbcpDmw33qbEMlBhvrVEI4kVjrvu/3RIwomnGMq15WG5zuDmzChEyH5EncG49FAl8tQzPT39CHN6siD7brI5rQihGBL7oBhawQbHbToCYrhR4iHHFUNUDsd09F8LuXvdy/+DN0yXeN8bP09ikERTx22nDJKICutdHHfeu71AGiqkQxKDbZVBDPc6rhz6YrjGS4Pg4Q1FNGOx2vuM/AMdVwD95fhiiPWDBA6RqO/lgRi+KNHKcZf7jeOK4RuOuz4Qw32OW4keFeHRdlsVhySceIqhSMcYcxopGPI7NTanxQKIYTL/TmlpaXgj2xYohsQ+KIZWgBIztF8GMRzvuKV6EEOUFEKkIISQu/5e/u8lynjp6MkA3Oi4zVdAsvDGov8IGXKIIUoYn/PS0QYaShtO13O6QPRWOe464NEuxBACCNDkBWQO0c5xpQ4lf8MdtwmM/3r5RjuuyKK0D7KLkss6Xpr/KBmlnU877puTEENs7xeOK4ZY7seOu10Q1wu8eUgCiacYApEOvySaFALZj3grOeZADGVQXIZ4mzkpkW3DExYboBgS+6AYWgEe/z7s7BdD/1EyBAmlaDc7rtzhsStAe2VmHUPUOYJcQvwgWhBDPCr+w9n/KBmSBjlEQ7hYPgIgLSiGWA7EECV9AI+S75G4T+IX50AxxLy+9KG0Eo+ZIX9YFuSvnpeGPJBGzA+xxfwQw92OK5y+GKIuoy+GvnCSBBJvMZTljzWnkYIRj1JDTwxxH8AfxKQkXlJdACiGxD4ohikPBJKQCPEWQyDXmPESgxiFCxEc/DmLKb4YAjx2TdYIbnMRQjEk9kExTHkohuQAEiSGTURAajAKH+a+LSwZATEkcYdiSOyDYkgICZIIMczMzEQvG8RCKIYJhWJI7INiSAgJQjFMbSiGCYViSOyDYkgICUIxTG0ohgklrmIov+Vtcj9/nMHILczjJQLFkBAShGKY2lAME0pcxVDu5R/Jfb28OZ0QIMcHWkHJCcWQEBKEYpjaUAwTStzFUGKBOZ0QINfho81pGoohISQIxTC1oRgmlLiLodzXI/d4QvIExZAQEqQIxfBzZ3+D7UHMR2HoDQggL7qNjAYaeL/BnOiR2/e0MSccAvRTju/AOmB56DISQ7/h93e8fG97Q0w7VMPKvzu5r1vCyKMYojcm9MeOewYawi7puD0z+Y1io0emZxy3a050k+n3NHKuxFzH7bkJTe3cLlFJYqSXjn7WBztu4/toFB/da6IfeXTTOdVxm9fCd9zl5cM4GsTHZ4yjowAcW36j+7YTdzHEkI+TSTRyfZxMMSSEBClCMQQQgHmO263j+Y4rSTMkMh23n2+Iky+G/3TcGyu6boNYoPvHXwPTBzjuctCH+CmOK27Bdv+qOW4vP+iRBzKE3n/8nn0gOuiHHCKHnn8WS6Cydobj9gqU5rg9/FztuGyUmOyN+41O7/KGn3hDSCQkEb2VoEciXwAxvEZik+PKMdYVfYWj5x/IFnoUShh5FENI2CzH7bYSXWyiwWhsL8bbO65kb3Hc3wO9IaHXJgjcaRJ4fLVZ4jXH7R3pMcftdhPbif2/23F/S/yu+B3QGxL272+O+5viO9C7E/Y/lo0+1fHbYhn4PqwHjpkwkBAxlOEWie0MRm5hHjcUQ0LIAVgghiiB88UQ4nCq45YkQaDQj7gvhhCECx1XspAPJVgoiUKpEm64kAssZ4TEcolHnP3yBqmAGEJSIBbo2xtiCFCChS4aKztuaeUREh867vdD7M6TuNxxSwyXePP4YojlQgy3ep/Brd4Q64v1g+SW8KZhHbs7rvRCdvZ6+SBd6FbyUy9fwsijGEK2b5Ko77gSe5njim9px+12E3J/kuN21Ql5O8NxJQ/Sfr/j7kvsq/lefsggpPgcbxwgP35bSDl+R/S9jv2Mkkh0z4nfHscCShexH7E87DscB0udcJAQMZTf9CUzjZCoUAwJIUEsEUOU2kEMOzmuGF7ruHKIkrVgiSGAFELsMB3XMIgCZM4Xw7e89KAYAl8MkXe6s18Mz5aY5OQUQ8jb9c7BxRD4JYb+Z+QDEChfDIs5bn/g+AwxhEj6coTvQ4kc+i3/Wc+ZQPIohu85rtShxBCPorKc/WKIx7oQxaed/WIIoYaA4/eb5rgCB0H0xfAFx+07HY/ffTFE6S7EEHm/dlxxnuC4ol/PcUttMY7jFfPi96UYBqAYkgJBMSSEBLFEDPH49WLHLVnCo9WzHFcA8dkXQ4yPkvg/x32MCBFDHjz+xSPiso4rhpBMCEhQDDGvL4agnbNfDDEvSrhMMdwj0c/ZL4Z4POovzxdDLBdiiCFkB/j1CjEN4oSSNv8zgBji0ScEyq9jiOsyhAl1+VCCmTDyKIZ4XOuX8GEcpbR4VFzKS8d+g+w2dlyp04IiFHfc/QyRRN1BPMYf4+x/7I7Pm7xx0FDiTceVvZmO+1sgb7rjlihiHKWvC7xxyCMkFPstDFAMiX1QDAkhQYpYDMMGSq2iASnNKyjhQh26IicXMYR42RZf5TItWkBGbYViSOyDYkgICUIxzBcQj2igZDGv4HEzSkiLHFMMTzrppCfr1KkzPqxRqVKl0cHtsQyKIbEPiiEhJEiYxPDLL79UNsc555yzxlxn2wmKYbVq1f72ww8/qLBHcPssg2JI7INiSAgJEiYxNAXAtliwYIHNUpIrQTHMzs7+h7lNYYg777wzEvgc3D7LoBjmH9RlRf3SaJj7FPmjgZfAQLQ8B/ue5IViSAgJQjGMXWzatEnJdRXtEYaGsIvh/PnzVa9evdSyZct0YFpw+yzDlJiYkqRiiN8TLylF4wrjM/JHEzy0CoA6qNGOEbyglnpQDAkhQSiGsQuI4fvvvx/tpmMlYRfDV155RY0fP/6AacHtswyKYf55ynFbIUCzSHjrHW+hL5JY6biN0qP1gR6Ou2/RagFAyeAmx31rHS+FoX1QDCGGqCf8hOM2j4R50TMPmrdCW6JtHLeVhOsct2UDvymq5IZiSAgJkmxi2KBBA/Xcc88dMA2PeM18fixevFhlZWXlmO7H5MmT1cqVK3NMzy0ghhiGqdQw7GLYv39/1apVqwOmBbfPMiiG+Qe926DNTPyuEEO0dYp2LNHcU1vHLTH0m4Z62psHQAT7eUOIYTfHFUOA9kLR9ibmgxiiPU00ldTRcX8j9KyENLSrmfxQDAkhQZJNDC+++GI1YcIEdc8996jXXntNrVixQk2bNk0LIyTwiSeeUCNHjtTTkb9NmzZq2LBhauLEieqdd95R3bt3V1dffbXq2rWr2rlzpxZDPKI89dRT1eWXX67mzZuX4zv98MXwgw8+sFlODiDsYoh45plnIoHPwe2zDIph/kHJnt+maVAMJzpuW6OQPP83R5/dGEdj+Msct23NHxxXDJEXYoiG55HnOG8IMUQbpn933L7WMQ3tdfrfiWUmNxRDQkiQZBPD2rVrq+uuu07169dPiyHE7vXXX1eDBg1SX331lWrUqJF+/Ni5c2edv0uXLlooevTooT777DMtj2effbYWSaSj9BEyecIJJ6ixY8eqjRs35vhOPwYMGKBGjBihw1x3W0kGMXzppZcigc/B7bMMimH+QePmaBwdDcPDWdAAfl3HrUeILhq7Stwdye3mA0c7bi9KrR23P29IHxpmD+bBEH12+9PRjBSmoR4iurb08yU3FENCSJBkE0P/UTJEEGKIwKNkyODnn3+uXnzxRT1t9+7dOj9KFyGGM2fOVN9//71+LHnjjTeqHTt26HSIZcOGDbVIfvjhhzm+LxjvvfdeJMx1t5W8iuG2bdt0Cao5Pa+xfft2vQx/v0aL4HfgN8I85vRDRXD7LINiSOyDYkgICZJsYhjPOFhpoRnmuttKXsUQgcfzF1xwgRa2v/3tb3ra4YcfrmrWrKmaNGmip5cqVUo9+eST6ogjjlADBw6MzHv77bdr8b711lv1vG+//baqXr26rt/ZokULLYxYNpqckWNSL2v9+vV6XpTA9u7dW73wwgvqvvvu0/MPHz48x/r5Edw+y6AYEvugGBJCglAM8x7mSy0HC3PdbSWvYog6lxDDhx56SNezhMitXbtWl8IuWrRIP65fuHChlj+IISI4P0piUeoKMdy1a5d66623VJ8+fVT79u3V008/rSWySpUq+jvQBM2GDRsiYtizZ081ZMgQPT/mw3egXqi5jiHY9xRDYh8UQ0JIEIphfMJcd1vJqxgiIG3PP/+8fhyPz5BBDPGIHaI4adKkiBj6L+nghR4MUWKIUkCIIT5D8B599FH14IMPRt4a98UQvcjgsy+GKCWEGHbr1k1LJaaxxDAnQTGU8ffM9GRFtnWIOe1gyDUPL7EkHWlpaUea0/IExZAQEiQZxLB169ZaGm644QYtJmb6qFGjDmjSBG0NmnkQePu4atWqkc+rV6+OjH/33XdafIL5UdJlLsMPc91tpSBiiPqaeJFny5Yt6osvvtBiiMe8KOULiiEkDnU4MS9e8kE9Q4jh1q1b9aNkXwzxu6H00BdDzD9jxgwthhBOXwz9EltMY4lhTkwxlOHpZp4kBm0X5gmIocT/zOlhR37vsea0PEExJIQESQYxxKPMli1basGAfEyfPl3dddddasqUKTod4njRRRfpz99++62WDkwfPXq0Wrp0qS6hQlMz999/vzrzzDP1vEOHDlWPPfaYFp/NmzfrN5M7deqkJRPN0cyaNUsLjrkuIZCTA8iPGM6ZM0e/WLNmzRrd2wimYQhJxMs++IxSQezTjz/+WE2dOlW9+eabejqaA0I9QUgl5vHfAH/jjTd0A9UQSuxzfAfe7sY833zzjc4LycRjajy29r8TYmmunx/B7bOMRIvhs2aeZEWuY5Hj+FBADGX/oDHrpEK2qYE5LU9QDAkhQZJBDPHmaocOHbQYopQJbRmiTpyf/vDDD+umTFA/DgKDEsPrr79ejRkzRr377ru6eZrmzZurBx54QJdO3XbbbXpZKDGsXLmyFkKUGEII0cbhp59+qttCNNcjJHJyAPkRw2Cg3l/w86HeNk5kBLfPMhIqht608w7MlZzgMWq1atXQRuEh8R8ly366zUwLO/J7DzSnHRKKISEkSLKIIYaQObRhCOkLpkME8ZgYjyN9McTLDigFhBg2btxYl3ShzhzkEaWH+Ix6cP5buBDDOnXqqLlz5+rPs2fPzrEewTDX3VYKKoY2R3D7LKMoxBCNO6NdvqQP2Vb0XHJIAmJ4u5PLcsIcsm1nO/mFYkgICZIMYoieTcxpiQj/JYncwlx3W6EYJpSEi2EqIdu81pyWG8n68kmBoRgSQoIkgxiiEWpzWjwD9RQxRCmkmeaHue624ouh3AdGyXh1czvCGOY2WgTFMI5QDAsIxZAQEiQZxNDGMNfdVjwxLCn3gb9kfLO5HWEMcxstgmIYRyiGBYRiSAgJQjGMT5jrbgtyvU+XGC/xocSl/tucMt6Uj5LjDsUwjlAMCwjFkBASJAxiKNeq4zA0BcDmMLehKJFrfLbIwr0y7CPDe9LS0mr5aaxjmFAohnGEYlhAKIaEkCC2i6Hc5M6Xa9QuGY7+73//2yosYW5HopB9VU/iPlzXZZ91kev8f808QYJiCAYOHPjH4MGD94U17rvvvt+C22MZFMM4QjEsIBRDQkgQm8VQ1m2sRC+5Ru118tGzQSoi1/O6sp8WSdzvuAKC5isOiSmGZcqU+feRRx55dFgjr23ZFREUwzhCMSwgBxPDtLS0Y2W8NyP5wv/NCTGxUQxlnc6W4/YBiTr4LDe6tmaeVKd27dolZT9dUtYtHewr4zfKNb28me9QmGJI4koYxPA0iV4SzY3pJY3PQepJ3G1OFEqbEzyOcNz8psTXND7ni7KxFcN/SFQKfK4eGAfYRyDjgKn7uckbohHtf0pkSuBcu9Vx50X424/xPP2RiwsHE0NJO8lPCwFTJYJ1SdBQZdXA52aB8V8C4z5djc+tjc8AHVL7F9oSEpcE0gB+YEJCTTzFUJb9HIZ5EUPJe73c0B6U69KZZlqqU7ly5X/L9flq/MmT/fSgREsnRiWostwzzGkkboRBDO/whq9K7JL4UeJoic8dV/Rel/hBoq6z/x78oEQ/iR4SayQ+kfiPREOJeY4rm59JlPXmOVbiZ4kymFn408uPdf7Vce+3+SbGYrhRYqnEDonVzv5tvVPieG+6n+9wL32bxFsSmyT0byF87Lj7b5CXDlDH9zWJUd7nf0s8L7HHcfcJJHSTxCyJmRLtvfyoonKzO0sMSSIx/ELiS4lVjnuyYafj3wYM/3wvza9rsshxf8jvJFY47gGNHw3LwIHa2Us7S6KKxNPubA72z2USX0m8KLFM4mLH/R4sGwc2IaEmnmIoQvN3DA8lhnINGijXn/pOUf5rthS5yQ+V/bNBhijBQalNTISQFAlhEUPc3yCGuP+h5Ot6CZzD+BOB+yHuhThXcX7/y3HFEFKD7UO1j3GOKz3fSsyVQO8rIxx3eec6+8UQf3BOkajm5RsmgetAZacAxFgMF0h0cfaLIWQYoKDpYcddb4BCqAsdtyDpJ8fdLsicL4YQRojhDIm3vWkQQ8gwJA+lpwD7EyWI+GOM7+3muGIOyc5yXGHGPsz28seOJBLDsRIPOa7M4UBGiSEO2Pcl1jluMTgOVAAbh/CdLvGB4xo88mB7UcwLw28t8aHEQseVRRzg8x334MA4frB2jvuvqY/jwhJDEnriKYYApYa5iGFxuXktkWsOShQKVDqQbHhVeSZILMcjYq+uXEIk0L8XkLgTFjEEEEMcF5AXrDfk7TqJnY57XE5x3MetGIcYQo4gMRDDl71o5LjCh3xtJC53XLn0xRD3Y1x/UFqIkkgIJQpfCnRNiLEYorAIJXwQQqzrbsfdDgjaEIkajivGVzuu8KY5boEU8r7k7BfDRx1XDBc6rmsAv8TQp5zEH47rIxBDPNHEd82RGOC4TgJZh7fE/pqQRGLYwHHFEGZ9peOKIYp08YNADmHx+KHwbB87FgcfpqE4+13H3cFNJJ5y3H8/rSXWS+DRF+oM4ETAPwXkD4ohDpQ+jvvPnWJIQk+8xVBuUpWCYijXm7tRQug3QZPKyL6piPqTsj/uk9+hZ3p6ul8KkXDk+h+trhSJHWEUQ5SCnei4jzazJTZ40/CnDqVhkDiIIcRmtuPeh3EvxbwokEHhCoQHYgiBCj5KRkkkQOkZ8mPZKNBB6Vu+ibEYdnJcH5jmuC7wjTcd9QFxrkAMwdcSR0lscfaL4WZnvxj6j+Kxj6KJIfYJ0jGEfEMOMR9KVFs4bqkiPo/2Z4gpSSSGqPsHMUQRLyQNYtheAk0zoD4DirBxgOGxMYA84sdCfjy/hxhuctx/OfgxUWSLR9C4WfliiGf+fb3xJY4rhtMlHnHcfw251V0kJFTEWwyBXGMmyfcMkBvW53KdeRRiiM+pGrIfxso++ERivYxPlOEQM0+0MPdtrJB1eMqcRmJOGMTQX0e/xBolVwClY/7QLylEwQvAPRjjGCId8wLk8as/QCARyOfP61cd8ZeD+fzvyzcxFkOsI9YP+wPr5G+/X2Lnr7u5X7AtGPen+/sKy/Gn+Y/hffztxxD5/P2O/eV/Dz738cZjSxKJYX7xf8wCH3SEJCPHxlEMUVoo15cNmZmZqCOTkvUHZfvPlf2wWOLZrKws1HWOqxwUBjRwnaxhbmsREdffPkZiGFpiLIY24gtkbElhMSSE5EI8xBAvnchye8kNqofl7crFBdn2WrLt90r8T8Zv9sSYEIphHEkBMYwPKSiGMOwbJW6Q6OhNQ2VRv35gN8etf4jH0Ngfwef+hCQ9sRJDuY5ky7J6ynCXRSU08aaYXDfrQoDxmFe2/YasrCy/CQ5CTIpMDLt3776vR48eKlkitz+cBRXDqlWrHmsu3/Zo1aoV3n+IDSkohqj8CjFE20ptHLf+IeoZos4hyHbc+olLHPetoQHedEJSgliIodyIrpXryFwZHi7Lwx+vpEe2tZ1s62bZ7j5yLUWTHQV6k5KkFEUmhrt3787Rp3SYo3LlyjlK4QsqhrVq1ephLt/22LFjh9+uYuFJQTFs4LhiiBdGBjpuI9goHezvpU9wXDHE2814m+p/3nRCUoLCiOHRRx+dCTGSZfhvF4Li5cuXLx34nBTUq1fvMAgwHpHLNj8k0SSFSkZJbCgyMTTFIuwh25cd3D5QUDE899xzx5nLD0MEt6FQpKAY4u1liCFKBNGeEtpQektipOM2a4M3m9FmEBq+RtoYdzZCUoP8iqFcJ86Qa8b9GQfppk7S0dRDaMlwX5ppVdbtUnKQxKVmHkIKAMWwgPHtt98e8DnVxLBJkyaR8SVLluhhcBsKxaHEMDMz8/QkizPkIDhbTpTzZfvq47OMX4hxf1iyZMlhGEdaiRIl3s5lGaEP/zcnxCQ/Yih50azKg1lZWYd6Oy6UbyB7/Q+jORncWOt42+m3aEBIYaEYFjA+//zzAz6nmhhu3rw5Mp5wMfTTCCGpwaHEECIo14jBkCYz7WCg3p3NkeE2Lr0qw2tGJi0t7UgzD4ORW5jHej6gGBYwNmzYcMDnVBPDChUqRMYphoSQuBJNDOX60FDS+qM03UwLK8e6zcjcKte8vjLeD3UkzTyExBGKYQEj1UsMH3nkkcg4xZAQEldyEcPD5JrwFi6eaI/QSAslsj1NcNOQa95g9EnMl0ZIEUExLGCYb1Wnmhj27t07Mk4xJITEFU8Mi8t1YK6Mo1/OUNYP9JFt6CDbslGGPdGmYLLILUkKKIYxilQTw9wiuA2FgmJICAnwNzn/H5YL5dD09PTKZmJI+Ltsw3kSd8oNcZgMm1AGiaVQDGMUFEOKISEkduBRcRuJvjJeIpdHydZSunTpo2S9G0j0lvUeUZbNyJBwYZ0Yfv/992r69Ok68Hn48OH6se3gwYN1YHzcuHE6DZ+bN2+uRo0aFfmMQGPLW7Zs0S+IDB06VKedf/756r333lNvvPGG/jx69Gi1cOFC9fbbb6vnnntOT3vsscci64HvnTZtmh5fv369nm/Xrl16+eY6I+Iphvh+DOfOnas++OADXb8R6/Hdd9/p7cc2oPmYWbNm6XxXXHGFWrt2rXrqqaf05zZt2qgBAwZEloftwHDy5Ml6Odjnq1evVl999VUkz3XXXaeH/j54+umn9X6fOnWqnn/ixIk59kFwGwoFxZCQ1CUzMzNLLoqfyY2juj8tDGIo63u4XKce8t4ivi49Pf0Ih83IkPBhnRhu375dde/eXfXq1UtNmDBBLViwQC1btkw9/vjj6uOPP9Zy9+CDD+q8kJqmTZtqsVu+fLlas2aNWrRokerTp4/67LPP1NatW9WHH36o87Zs2VIPkR+yCCn85ptvtDydffbZOm3EiBF6iDRIUN26dfX6vPXWW6pFixbq2WefVevWrcuxzohEiGHNmjWVXDP1+kN6sb2+GJ5xxhmReo8QQ0ijL4b16tWLyCDCH8d+xP6BGDZq1EhvJ/YZ0nwxPPzww3U6XjapWrWqatasmVq5cqVq165djn0Q3IZCQTEkJPWQc728nOd3yQWxv/niha1iKNcjub9ltJP17uM9Ij7XzENIyLBKDNu3b69F7NVXX1XdunVTl112mZaSnTt36v54H3roIS0zvhhCUjBtyJAhurQPJYUQomrVqqmHH35Yvfnmm5ESvoEDB+ohZAmCg7wQKZR8rVixQqdh3pIlS6qePXvqZUImly5dqmXTL3nEdKyTue7xFkPsG8ibpOntf/HFF3Upny+G8+bN02nIX6NGDXX33XfrUtJOnTppoe3QoUNkeRBDbAdKFYPbA+FDqSTGTzjhBP2dWCZkHFKO/TZmzBg1c+ZMLd/mPghuQ6GgGBKSGqSlpZWTc7prxkF6KAE2iKEnrh1lXftBXm1YJ0LigFViiPDF8NFHH1WzZ8/WpXqQFZQeQmjw+NNvQxDSh5JFiBtKz3wxbNCggXrhhRe0+PjSg9K/Tz/9VOeHcAbFcNCgQToPSioxxGNmlJxBDOvUqaMWL16s7rzzTl2iZq6vH/EWQwwhhpdeeqkWQ//xL8Tw5Zdf1iV8Gzdu1PlQYnjvvfdGSgw7duwYKTlF+CWGH330kd5OLAsCCTG85JJLdJpfYggx3LRpk3r99dcPEEOIorkPgttQKCiGhCQ/cmO4Qi5+8+TcTjfTTIpSwmQ9a8g6viDrMA2PuQvZeDAhtmOtGJ533nlaiI4//ngtPBAzcQItfv/5z390XoghHgOnp6drgfTFEHXwIDsQSvlenRfT0Chzly5dtAwFxfCss87SedDNnayrTpM/sloML7jgAi2GmA/LQLq5zoh4iyG+NyiG2C7Ue4QYVq9eXc2ZM0dVqVJF54cYYj8eddRRej48Iofg+svzxTArK0tvJ/YVSgch3ePHj9dpRxxxhJ4XYogSW+znoBiiRNHcB8FtKBQUQ0KSEzmfL0SJG17QMNMORqLEUK4z50q8IdEdj4nz0K0eIcmGdWJYFFGqVKkc0/Ib8RTDRIUvy+Z0iOOpp56aY7oZwW0oFBRDQpKLzMzMk+VG0FOii5mWF+IlhqjLKNeWerL822U4SIZ3yDXnODMfISkExTBGkQxiWNgIbkOhoBgSkjzIudtH4uHy5cuXNtPySjzEUK4tDWW9PpZl3yQ3qWPYriAhGophjIJiSDEkhHjI+Xq3RDO5+B9uphWEwoohxFTW5R25njSX8fRq1ar9zcxDCNEkXAxl+IPEJFMqwh4UQ4ohISkPzl25oA2Qi/6VZlphyK8Yon6gcIVcN7pLPCbzn2PmIYTkSiLF8Iu0tLQjZfizxEOmVIQ9KIYUQ0JSEjk3b5B4PDMzs4qZFisOJoaSVkG+v6kMe8pwoAxrmXkIIXkmpmJ43HHHoUmqk+TcTEd1DYgh6vYGSwy9xuD5KDmAL4a1a9cuKfP8JmI43lx+GMLcrgJDMSQkHMh5OVnOyZpOjG8mJrmJodxMTinrNiPznFwnzsrKyipj5iGE5JuYnMtedY3iEECJ82W8pEQxiCHSWcfw4EAMIYXYjzLPLywxpBgSYjOHyUX9OjknR2UEuq2LJ74YpqWlVZFrAr77YYne8llfHwghMaNAYoimneT+fIacl5fJ+XpqpUqV0CRVjmVRDA8uhnJNO1b2zeVelZxjME2GT1IMKYaEWIdcnLrIeThSRoubafFAvq+GnP+3ynC4fO8gufFkmnkIITEnh8yZiLRUlfPyGhneXNZt7qmCmScaBxPDMWPG/ImeOZIlzK49QS5i+He5zp0CGSxTpsy//YlmHcMTTjihorl82+P222/fEtyGQkExJMQeMjMzj5bz75NEiZlcEBvJ962QGIjvRElEbo+SCSFxIaoYokQQdXlFYvpDZrwmqEqY+Q7GwcQQJWwyvWKyRHDbfIJiKOMNJBZJnOfXs/QxxVAoZi7f9qhWrVpZYxsKDsWQkKJFzrfLJGaWcxt7Lmamxwq5eLST71kj33MNHqFEa0aGYkhIwjhMzsc0OS/vk2gt5+iVODdj1QvQwcQw2UHJqmzzbhle7bh1LqOSiximNhRDQooG3BDkgtRLLtq3mWmxwHsrEdJ5t3zPWNQXdA5xgQQUQ0LiB85LSIucj/WPdeu29ZO40MwXC4JiKN/1mpmejOAPtmzvrbLtgyU2mOm5IfmvMaelNBRDQhKPXIjOlwv1PDnX0s20GIAmF+6U71glwwZyHmc4B3lkZUIxJCQ+yLnVwpPCM3Huo+qImSeWGCWGDcz0ZEP267XYv37PT2Vz1jHMFbTvaE5LaSiGhDiOUuo1ZSnmugbBBU0uhA/KOTshPT39LE80C/XCCsWQkMIhInaaDIrJPfVfOEc98citfmCe/7AVhKAYep9nH5gj3Mi16hzZpru9Otk59m9exRDIPhprTktZKIaEuGJ45513HiBkTZo0UT/++OMB08DevXtV/fr11cqVKyPTXnttv1cuWLBAnXnmmeqyyy5TnTt3VnPnzlV79uxRS5YsUS1atIjkA4sXLz7gc/PmzQ/4/O6772KAmQ6I33//veWIESOmy/L3jB8/fhymmdtUUCiGhBSIYiIXx8j5c73cO3vXq1cvL9KXlzwFxhRDlFgemCN8iGSjEe8WeCRvvkRikh8xRN1Oc1rKQjEkxBVDCN/o0aPV1KlT1S233KKl7tdff1UPPPCAmjlzphIBU9ddd50Ww1atWqmXX35ZLVq0SLVu3VrNmDFDPf3002rfvn1aDCGF11xzjVq9erUWvJYtW6rbbrtNj3fv3l2J2KkbbrhBLVy4UFWoUEGNHTtWzZo1S6d///33avr06Wrp0qXq7rvv1nkvuOAC3SQBuP3229X27dv1uiBvo0aN1NatWw9aspgfKIaE5A5kBKV/XkPI6SJc18l9879mvnyQUDH0pj22P4f9eKWtaM+1olybTjXTD0Z+xBBI/sfNaSkJxZAQVwyXLVumypcvr2UOpXwbN25Umzdv1vL27LPPqm7duqGtLC2GjzzyiBbDDRs2qJ49e2qRQxqAGDZr1kzP+9577+lpdevWVR07dtTjTzzxhP4OSB++p169eqp///4HiCE+Q/yGDx+u5XP+/PmqS5cuOn3dunVaDMHEiRO1jGJ9zG0qKBRDQnIi98VbRE5ul/viGY772LJQVTY8Ei6Gcn43lumfhii+kPX/TNb7s1zSDhUr9u+NQ4O6ibksI/RhbuchoRgS4oph27Zt1bx58/RjXzxCRkndhx9+qEsKX3zxRTVy5EgtiRDDN998MyKGKAHE+BdffKFlDWKI0kDw8MMPqyuvvFI/Iu7UqZNas2aNGjdunLr00ksVvu+NN95QNWvWVEOHDtVC+Oeff+rvHjNmjHr++edV37591bBhw1SvXr3UoEGD9DJ//vlnLYZIv//++/U0fJ+5TQWFYkiIvh+Wx+PF9PT0st4j4kuc2DcnlXAxDAnFUW9Q1v//zASSACiGhBz48smOHTv80Zhh1lXEo+V77rnngGk+P/30U2R8ypQpavDgwbrE0OeXX36JlBiipBKiirqL5jYVFIohSSXkvpeWmZl5Ml5kQFMnMmyZwL7AKYYeso5DPfkmRQ3FkJADxRD1CuMN5G/btm3mZA1KDX3++OMP/Yg5KIZ4RO2LIaTw+OOP1yWL5jYVFIohSQXkOK8lMULudy/IsJHXxElcRS0X4vp9sm2TMLRVDGW9qh999NG6l6dgF3WkiKEYEpI3jjrqqNJyIVsscb6Z5hOxtzjy1VdfmZO0TJrrUlAohiSZgHDIMX213NemSHSX87dS6dKljzLzFRFxFUMg21zNJjGU9RknrtH8GLd9VWIjFENCDo5Xv+hGOS+eQrtkZrqPpKfPmTPn9zVr1nworjY10WGuT0GhGJKwI+didlm3b9zuaN4kMzOzihP7+oGxIO5iKOfzy0UthuISNWU92qenp5+SnZ39DzOdWAbFkJDooM9SOR+meTeWXJGLbkXJ86jEVDyegiCaecIExZCEFdQXxGNhOSd7oc6g/5jSYuIuhnI9OrGoxBB1NeX7J8lv0RP1N53YvMlN4g3FkJADkeO/nsQMiWqOUcog58gpcpF9VtLuR/2YBFZSTxgUQxIC0Od3MTkPz5Vz8iq5V/0npN2axV0MQQLFEL/JA3iRxEnQtpE4QDEkxAXHPx49iRj1Ck7HG4syvRUqqkv0L1e4Bm2th2JIbAQl8XLu1Rfp+F92dnYpJ5cu0EJIQuRJ9t0EiWlxjJkSH8tvg/7fzTSG5WEeLxRDktKkp6dXluO9m1zQ+qH+oIz/n4zfKHI0WuJe1E8y50l2KIakiCnmtR14u5yTZ6GqBqaZmZKEhIhhrMHLO7hWSozEb2Wmk5BDMSSpihzvt8mxPkQiW471hjKcL1J0E+oTHqoPzmSGYkiKAhHAqyRqyGgJORfT5TzMMvMkIaETQ/ldjpbf5zL4QSnBTCdJAMWQpBpy8zldjvFReDQsw84ST8m0W818qQrFkCQC9DeMoZx/1VB9Q87Ba1NQNMIihsXld6ojv1NV+Z0ONxNJkkExJMmOXMiOkWP6UokFEq9InGvmIfuhGJIYU8JrPBolgY/I+fggj7EItophCUg6mpZBCWG9evVsXU8SDyiGJFnJyspC/cFH5Sb0vtyMhnnHN95mJAeBN20SC9DUk5x3XSCDcu7VlUnFLGpY2hasE65ybpM/T6L+tZlGUgSKIUkG5EJ2qtyEJkqMluP4Qhm+JHE+bk5mXnJwKIYkv8i5djlK42U4QKIiBTDPFLkYHnfcceXknL9ErpU1HbYzSADFkISVsm7vBnhE/Jhc2AagGRm5KV0on5+W+D8zP8kbFENyKPCYUc61GnhrH/Vz5ZjpyrdTC0RRimFJuU7ehWsnfztyABRDEjbkRnRBWbcB6sFyzJ6JmxQef2S4bWg1yGDl6EJBMSTR8P6M4U3+aXKctHBcsUmG9gSLiiIRQ/n9zoXUC+XNNEIohsRq5ObTWI7HNyX64I24YDMy6OlAps9ByYWTvO2cJRyKIRFKynl1lZxf4yR6O67A8DFj7EmIGMpv2Aalg2Xd3pwo8uTgUAyJZZTIcJuT6SyC8jyGZgkg3nCUaddJ2gve244khlAMUxOvrtnZct3P8B4Rn27mITEnbmKY4bbGcJn8ph2ysrJs7zOa2ATFkBQVaApBjrmz5HjrVtZtRqa1E+XfrFc6iH+8fc00ElsohskNSt1RH1fOpQZyXh0LEeQfrCIjZmKI6ynahpTz9xxUtzHTCckzFEOSaHABkwvX7XKcvSrHXEMZr+Qc5AIp6afJxe5lOR7/Y6aR2EMxTFoOw0sG8vteIufeDRBCJ8ofMZIwol738oP3m8523N+Tj/xJ4aAYkkSAx1RyXJ0n0VtE7yW5kF1t5jGRvNmS7w4ZPuSwDmHCoBgmDcXROLGcb9fKOdQGUoiSdzMTKVIKJYZZWVll5Lftg6cpDq+RJFYURAzxBmiYw9yeaJjzhTnMbYsnIhYVJC6W+J8cS1PkxnShmScasq7HyXzDUIfQTCOJgWIYSg7DOSfnW185d9pBGMwMxEryI4bFUVdQrpFoHuhsM5GQmFEQMaxfv/6yMIecVLXMbcoNc74wR8WKFUea2xdr8EgYj3wlBkicc/TRR+erwrPM3xZS6D3iIkUExTA8pKenHw8J9Orrnok/Vg5LjsJEXsWwGK6n+JPNNgdJ3MmvGEpagx9++EGFOe69995N5nblhjlfmGPVqlV/mttXGOQ4aCIXqde8R8OnF1TmZP7HJDrjLUgzjRQNFEP7OO644/4j59kVEhVxDca1WuJfZj4SOnIVQ/mdD0fvMagCkOgnPoTkWwzlgL3clI6wxX333fe1uV25Yc4X5vjggw/+Mrcvn5Qs577J2EyOgdESt1auXPnvZqa8Iss5ESWLssxTzDRStFAMraC4nGNyCT4G7XiiZ58x+ANmZiKh5wAxRJ1QT/j5UhApOiiG0THnC3MUQgyLyXHRVo6JVyXulBvUqbh4mZnygyxniBxHd3iPvYhlUAyLDvzZQh1BryS+ukQNlhglNRExlN98gpx716NagMM3i0lRQjGMjjlfmCMvYoh/qhA21BOUY6CjV5oXk3+ustzX5XhqyLci7YdimBjKum/pP4Vx/NmS/V7BoRCkDHJ9RSP9t+CxsZlGSJFCMYyOOV+YI5oYlilT5t8QQPnN75eYJeMNzTyFoBgef0lM9P4FkxBAMYwP3jmAcwLtcnaFFEhkm/lIUlNCrrH15RiYLr99AydKHUNCipRUEMOGDRuqq6++WsfTTz+dEmLoby/ixBNP1GLotWl2ofy+AyWmyvj55jYXFq/CNF5MmY5xM53YD8Ww0BQvJdSuXbuk7Mtach29WK6tZ5qZSPKDa6AcA+fINXGURCcz3aEYEhtJBTHcuXNnZHzy5MkpIYbBgBh37tz5L7lAjcXjKxHELHNbYwEeich3PCfHzRlOjB5Bk8RDMSwweEGrLq6fco6dLJ+LZWVl/dPMRFIDuR5WlGgv59MlB6knSjEk9kExjI65nDDFRx99pF555RXVrVs3VaNGDWx3ro+SYwH6XpXj4go5bmaaaSR8UAzzDnr08W76JeUcOAbtCqK/WjMfSRlKyDFwslwL78rjcUAxJPaR7GI4depU1a5du8jnZBTD1atXqylTpqhbbrlF3XPPPWrp0qXq5ptvjqQ3a9Ysah3DgoJSRxGILnI8DDfTSLixWQyVUmcXZaxYseLKP/744xyM79mz51x/urmeJHXAm+ReMzNXmml5gGJI7CMVxPDWW2+NfE4mMZw3b56W3uHDh6u33npLbdq0SU/funVrXMVQjouacow8JMdCdTONhB/LxdBKzPUkyY9cA9MlHvOq5hS06gzFkNhHsovh66+/jvp1kc9hFcNp06apNm3aqNGjR6t33nlHbdmyJUcePyCG/fv3j3yOhRjKMXGDxBC5COY4JkhyYbsY7tu3D8dzRMr+/PNP/ZIV/iSB3bt3R9LAV199pcaPH6/27t2r/yiCfv366fnWrVunP99xxx26pB3nyvXXX6++/PJLtWvXLp2GPL/99puejnyopoE/Yd9//72aOXOm/lNmridJPiCCuEeiya0Y9kVNMST2kexiiAhjHUOI3/Lly7UIXnvttbpU0MyT1yiMGJZzezsZIrJwo5lGkhObxXDx4sU5xHDRokXq8ssv1zKXG5deemlk3BfDcePGaYGE9EEIH330UfXtt9+qlStXqgsvvFC1b98+IoY4D/Edr776qp4f1xP8SWvatKnq1auXGjNmDMUwicELRHINrCP3vnvi0NICxZDYB8UwOuZyEhEohfjf//6nOnbsqCZMmKCWLVumtm3bliNffqKgYii/9TWoRyjHRXkzjSQvNoshqk+YYghhw7SbbropMi0IZA9iB3wxRMnf119/rcVw/vz56pRTTtF1dXfs2KH/iJli+Nxzz6nNmzerli1b6uvJRRddpE466ST1/vvvq5EjR1IMkxe8aZ5W2N6eDgLFkNhHsoshbgR4BOR/tkkMV61apXr06KG6du2qnn/+efXee+8dILEFDTxKxk3S/5wfMRQp6C+//51oasFMI6mBzWIIUTPFEI+E8Wi4b9+++nFxkAYNGqjLLrtMlwYCXA9QGv/iiy/qx8P+o+TGjRurH3/8UZ111lmqRYsWWgwBSg8hhpDEq666SqdjWXPmzNEvfOHP2/Tp0ymGycNhcv3ri3rUGDcT40AivoOQ/JEKYmjTyycbN27UdQRRBxBvES9ZsiRHnsJGQV4+ERk4VX7b6fKb1zXTSGphuxjGkj/++MOclAOI4cGAKJrrScIDHg9nZmZWkeO+ahF02UkxJPaR7GI4e/ZsdcMNN0Q+J1IM169frx9TdejQQXXv3l2tXbs2R554BMSwd+/ekc9RxLBYObcrvCkS5xlpJIWxXAyXIkToVu7du3eVHN8f79u3D+ampxdVmOtJ7AXNy+BFErmXDZNhMzM9wVAMiX0kuxh+9913+q1E/3MixHDu3Ln6UVSfPn3UjBkzIs3IJCrw2Oubb76JfDbFUH7Dw+U3niYC0CKGb9eRJMFmMcQjvuzs7FLodxgNq3vHb3EzHyG5Ifc4dAmKprZOQz/xZnoRQDEk9pHsYoiI98sneLvx008/1f0woz7jgw8+qN+QNPMVVfhiiN8Tv5/8vgssuSgSC7FMDIsh8GdGrk8NJeqjxMfMREg08FYxeqiR697DEn0cu/5IUAyJfaSCGI4dOzYSeDuxMGKI0ji8ifj444/rttNGjBihtm/fniNfUUdwmxEQQ/ntXkI9GnM7CQlSFGKI7sPk+vMvrzmQYnINukyimpmPkDxQXI6dbLne3SPDyWaiZVAMiX2kghiaURAxRDMyKBFs1aqVbiwXL43gMbW5bFvDf5Scx/47SQqTaDGU77tDrjl3y7XlAjwidtxSQkIKQjG5b50h97LmckydaCZaCMWQ2AfFMCeouyT74Ja77rpLPfvss2rFihW6pNBcTpgil5dPCMmVeImhLLeRnFeT5Bpyr5xjlePQWDBJMVDKLMfVTXhEDCE000MAxZDYB8XQpZzbiOnJ2HaJBXKxaWzOF+agGJK8EisxhPjJsipACPEGvAw7oL6XmY+Q/II60nj6Ifej6v59K6RQDIl9pKoYenXtiqenp5eV7UZdFFRMbuLtA/0oy5wvzEExJHmlsGIo858q59JdEo9KNMO5hpIdMx8hBUGOqVa4DznJIVXJsA0k2UhVMZRt3CPR2ty+IOZ8YQ6KIckreRVDNBWDx3eeBCLqZWZmZpn5CCkMcs85HX8s8DY63k4300MOxZDYR6qKIS4ysr3XmdsXxJwvzEExJHnlYGIIGZRrQA2vnmBZ+VzZzENIDNBPbeRe9BZKoM3EJIJiSOwjVcXQ3K7cMOcLc1AMSV7xxPCwtLS0cjJ+PcbNPITEEpQEen842h3j9lOcKvDcIvZBMYyOOV+Yg2JI8gIq9KOERq4Fx0nUlzirdu3aJc18hMQKvKQk95yBcm+5Rv6MHGumJzkUQ2IfFMPomPOFOSiGJBpe93InixBe7PUQ8X9mHkJiTAkca3I/aY+SQjMxhaAYEvuIpRjeeOONavny5Wrjxo1q8+bNatmyZWrp0qU6DUOkf/LJJ7oLua1bt+rpq1atgrToeXbs2KHzffvtt7o3EX/elStX6iHSO3TooEaPHq0/Yzno7g7fg76B165dqxudxnzr16/XjVKb64gorBiiTUOsN7YRbRyiyzl8P753w4YNasKECToftgH7A9OxruvWrVNffPGFWrNmjU7ftm2bHp522mlq1KhRehzb0qlTJz1+1VVXqXr16qmhQ4fqz+PGjdPD3r1763XA9uE7MA8+y79t3Uezub4IiiERSqC5GBHAXhLt5TyvY2YAB6tjSEhBQZ1UVE1As0VmWgpDMST2EWsxHD58uBafl156SQsMeguBDKELOaQvWLBASyDyQ+LuuOMOJRcKVbduXTVmzBjVr18/dc8996gWLVpoAYQ83X///Tp/bmL4+eefKzRE3bRpU1WzZk0tSU8++aRq06aNmjt3bo51RBRWDBGQWfS7fO+996ratWur5s2b6/Xv2rWr7hkFedB1XuvWrdWgQYO0wF566aXq1FNPVd27d9d9Kffq1UvnwzRfDF999VX12Wef6fH58+erGjVqqAsuuEBvq//dV155pR76Yolt/vrrr3U/zX369Mmxrt76UgxTE3Qvd56cY7Xk3D0Gj+1kWMnMFIRiSGIJjjs5pgbIcdgNLy+Z6SkOxZDYR6zF8KmnnlJbtmxRM2bM0KVZDRo0iJSgQf4w/Oijj/Swb9++Om+FChVUly5dtAz6y2nbtq0WvgEDBuhlYno0MUSpXM+ePbWcQZKQBpmEZJnriIiFGCIghig1xPeeeOKJej07d+4cWV+I4cyZM/U4xLBly5Z6WyHPKAWtWLGi7msZ+8gXQwTWvUmTJnocwochJPv555/X/TPffPPNatq0abqEFGnYZswDof74449zrCeCYpj8oNI+SgIlri5fvnxp9OAjk/PdBSLFkMSAw3D8iQhmmgnkACiGxD5iJYYQmFtuuUWLHoQQYohSMaTNmzdPSxyED6VcEDw8BkUePJINiiFKESFa5513ns6Dkkfkx3IgYXhsm5sYYvq5554bEcNHH31UP9Y11xNRWDHE90JEIYaffvppRAwxPTcxxPoHxXDhwoV6P5100klagJHXF8PBgwfrfpgxPmzYMD1844031KRJkyIloGeffbYeYp/jO7HN1157rRbDd999N8f6IiiGSclh6AECQ5G5Z+W8HRcLqYvFMkjqkZaWdqQcgw0klrNkMM9QDIl9xEoMIT4QoYsuukiLIfoYxnjjxo3VO++8oxo1aqTFEOKE/Kg/h3kwDIohHpvikezIkSP1I1NIVcOGDSPzTJw4MSKGWCbEDGKIR9AXX3xxRAxRshavEkMsG1IGMbzkkksiYoj1wfahLiC22xdDrFdQDPEZ643tuuaaa/QyUY8Q8+AxO0pFMa18+fJ6GrYbdTIxH6ZDpjHd3zf+43OIoV86awbFMHnw2hJEHcEhMrwCL4/IuZtm5isoFEOSX/AHRY7BK+V4PB/Ho5lOokIxJPYRKzE0A6WB5rR4BmTtueeeyzE9tyisGPrx4osv5piWW0AYzWnxCr+00gyKYThBkzFyPo4SWesv597piSiJoRiSQyHHyKlyPLbLzMysIh/ZnFHBoRgS+4iXGNocsRLDMAXFMBzIOfgvifIZboO/7eRcvLOUYOaLJxRDkht4ox2BR8YZqd3ETCyhGBL7oBhGx5wvzEExtI4SeFsTbwnjg8jYJSiFMTMVBRRDEgTHqdwTbjCnk5hAMST2QTGMjjlfmINiaA3F5Bw6Tc6xviJgPVACY2YoaiiGRI7Lmqi6UKlSJf3HhcQNiiGxD4phdMz5whwUw6LD6/IrW86n/8jHw2S8mpnHJiiGKU1x+f2flLhXxouZiSTmUAyJfVAMo2POF+agGMYf9DMs50yG3FRvlGHNypUr/90JYcV8imHKUAx9E8v1/haJbrF8s53kGYohsQ+KYXTM+cIcFMP4gYZ8IYF4RIzzA/0OQxLNfGGBYpgayHW+jfzWXW2p25qiUAyJfVAMo2POF+agGBYetM+WmZmZ5bUbeBteGEEJoZkv7FAMkw+85Q4JlGv7/WgCyaGQ2AJ/B2IfFMPomPOFOSiGBaaYyODJclMdJvGEf44kMxTD5AFCmJWVVVmGV3ltDhK7oBgS+6AYRsecL8xBMcwfIke1JKrK6GFyzF/rjacEFMPkQK7jrSUGyWhxM41YA8WQ2AfFMDrmfGEOimHupKWlVZFjujnqWclxf6kc83XRyLSZL5WgGIYTVGtANQc5psul+jEcIiiGxD4ohtEx5wtzUAwjFMeLISI/58ixPF2O9RfluD/LzJTKUAxDRQk0hyS/2TNyvT7DTCTWQzEk9kExjI45X5gjRcWweHZ2dik0xSExSY7dtmYGkhOKodUUQ8mg/EaN/V5zSKihGBL7oBhGx5wvzJFKYojSQIn+mZmZRyPkmK1o5iHRoRjaiVyf07Kyso6X63MDiXQznYQSiiGxD4phdMz5whzJLIZem4HosaGqxE1y/N6Vnp5e1sxH8gbF0CqK481itJOZlpZ2pJlIQg/FkNgHxTA65nxhjmQQQwggbpIyWlKGlXDsyrF6rpmPFA6KYZFzWO3atUvK7/Ckw27pkh2KIbEPimF0zPnCHGEWQ+9NS7w9PFRulpfgM8LMR2IDxbBokOP7GLkGN8TLUHyrOGWgGBL7oBhGx5wvzBEWMYTwyfE3SqK3Jyi8cCYYimHCKO6Vet99DN8oTlV4fSP2QTGMjjlfmMNWMczKyvonKtKLjNRiKaAdUAzjC6pDSFwo+/k5XE/NdJJSUAyJfVAMo2POF+awTQxRX9CrR9VfjqnbcOxBEs18JPFQDOMDjnlcU70XpJrwjxBxKIbERiiG0THnC3MUlRh6j4az5Wb4oAy7y7CRHGflzXzEHiiGsUOul6fLcT9I9mkHM40Qh2JIbIRiGB1zvjBHAsWwGB6VybF0nAz/JzfE4XI8XWZmIvZCMSw06Ju4RPny5UvLsf9/ZiIhASiGxD4ohtEx5wtzxEkM0WwM6ku1E5lYLDHAzEDCB8WwQKC9wfYSY2W8hJlISBQohsQ+KIbRMecLc8RSDPEo2HtEXE+OhyvlOKlbpkyZf5v5SDihGOYdORdOyczMzML5IOfCMWY6IYeAYkjsg2IYHXO+MEchxRCN7B4mN0DUlxolx8Dr3jSShFAM80RJORdukbjbTCAkH1AMiX1QDKNjzhfmyIcYFkPpX1m3P9ZJqCtoZiDJja1iqEKOuT2EOBRDYiMUw+iY84U5DiWG8lufkZmZWUVGS8px8K+srKwyjluJnqQYtorhN998o2bPnq26dOmiQ85jPfzzzz/VO++8o+Xru+++09N69+6tP/fo0UPt27dPj69fv14Pe/XqpYe///67zuuzYsWKyLjJtm3b1F9//aXHf/vttwPS7rjjDrVo0aIDppm8/vrrFEOSGxRDYh/5FcNSwrJly1SY4+STTx5vbldumPOFObp16/ZNcNtE8GtkZ2eXctxHwnwsTCLYKoZr1qzRkvXll1+qX375JSJdmzZtUm+//bYe37t3r9qzZ08krU+fPmr16tU6/8KFC/W01157TQ/lz5IeYnmTJk1SnTp1Uk2bNlW33nqreuGFF1Tbtm21RGL+Tz/9VK1du1YtWbJEbd++XTVu3Fh1795dz//www+rs88+Ww8vvvhiNWPGDHXXXXep5cuXq44dO6q+fftqmTS3hxCHYkhsJL9iCKpWrfpSmMPcnmiY84U58IKIuX2E5IatYnjEEUdERM4Xw6OOOkqX5F199dX6M0oPg2L4wAMPqB07duhxXwzT0tL0/IsXL9af161bp8Vw4sSJasqUKWrDhg0RMYQEDhs2TIvhhAkT1Lhx49T777+vhg4dqj766CM9P9ZrwYIFavfu3WrQoEGR9cN6QTJ9zO0hxKEYEhspiBgSQpIXW8UwWonhTz/9pEv2IIUgKIbgmWee0UNfDP3qFR9++KH+vHnzZjVq1Cgthk8++WREDFu0aKG/p3///loML7jgAtW6dWsthkOGDNGPrQFKCgGkEMvEI2rM99JLL6lmzZrpNGBuDyEOxZDYCMWQEBLEVjFE6R2A1P3xxx8R4QJvvfWWFkTgpw0ePFgLn//566+/Vt9//72e7uOPz5o1S40dO1bXBRw9erT6+eef1SuvvBKZd9euXVr6Vq1apcVv8uTJETnFI2Pw2GOPqV9//VWPo0Rx586d+rE1JBKli+b2EOJQDImNUAwJIUFsFUNtXDHELFmMBa1atTInaSCg5vYQ4lAMiY1QDAkhQWwVw3xSDI1OY+jw7XpiLxRDYh8UQ0JIkLCLYWZm5tHonk62o1d6evoRZjohFkExJPZBMSSEBAmpGOqSQZQShnT9SWpCMST2QTEkhAQJk1hlZGRUl+tVRxktYaYREgIohsQ+KIaEkCC2i6Fcn9Il6pQuXfqo8uXLlzbTCQkRFENiHxRDQkgQG8Wwdu3aJdGHt6zbJWYaISGGYkjsg2JICAlikRiWkHU5W65HvfFCiZlISBJAMST2QTEkhASxQQzxEolch/pKtM7KyvqnmU5IkkAxJPZBMSSEBCkKMZRrTgNPBJvK91cw0wlJUiiGxD4ohoSQIAkWw+LyfQMyMjJuNRMISQEohsQ+KIbER373kRJTGUkdrczf3STOYlhMlt9P4lkzgZAUhGJI7INiSHzkdx8Z/Fy5cuXuqRDBbU52ikoM5VqSIcjgmP/Idaa8mU5IikIxJPZBMSQ+phguXrz4ry+//FIlewS3OdkpCjEUIewi37vWnE4IoRgSC6EYEh9TDNetW/eXSgGC25zsJEIMMzMzq6Snp5c1pxNCckAxJPZBMSQ+FMPkJ45iWEJk8BRcO/zrCCHkkFAMiX1QDIkPxTD5iaUYSr6r8TZxVlZWpplGCMkTFENiHxRD4kMxTH5iIYZocDotLa2W5KslH4uZ6YSQPEMxJPZBMSQ+FMPkpzBiKPOem5GRUV1GD5Nrxb/MdEJIvqEYEvugGBIfimHykx8xLF++fOn09PQj5PMd2dnZpcx8hJBCQzEk9kExJD4Uw+QnL2Ioef4vIyNjqJz/Lc00QkhMoRgS+6AYEh+KYfITTQxFBNtKXCujh0V7lEwIiTkUQ2IfFEPiQzFMfoJimJ6efrx87ijn/T0ihRf40ymGhCQMiiGxD4oh8aEYJj++GGZmZl4g463T0tKOlI/Fg3kohoQkDIohsQ+KIfGhGCYv6enpZ4nwTc/IyOhhpplQDAlJGBRDYh8UQ+JDMUw6ilWuXPnvtWvXLim/bTomRKtjGIRiSEjCoBgS+6AYEh+KYVJQEm0Nym/ZtEyZMv82EymGhFgFxZDYB8WQ+FAMw4ucq2fI71cPbQ86Rp3BIBRDQqyCYkjsg2JIfCiG4UPO2TT53S7NyMi4Ki+9kVAMCbEKiiGxD4oh8Ykmhtdcc40WqMGDB6vdu3erkSNH6s+rV69WtWvXVnPnzo2kL1iwQP3111+qQ4cOavz48ZG8s2fPVmPGjFGLFi1Sb7zxhp72888/qx9++EEtXbpUzwu6du2qGjdurDp37qweffRRPW369Ok6/YknnlDLli1TQ4cO1dNjRXCbbUd+o/NE3HqJCJ5frVq1v5nph4JiSIhVUAyJfVAMiU80MYS4gW+++UZ98cUXatWqVfrzjBkztNxB5ObPn6927NihOnXqpB577DH15JNPqhNOOEEtXLhQ54UYYhzTkAdAApFvz549el7QunVr/XnevHlq5syZehwyCoG899571YABA9S2bdt03lgR3GabQdMy8hvdmZeSwWhQDAmxCoohsQ+KIfGJJoZHHXWUknEtUStXrlR79+5VGRkZ+nP9+vW18F1++eVa4CZPnqz+/PNPNXHiRC2Bv//+u843depULZFt27bVYrh+/Xr10EMPqdGjR+sSRszrg9JClCwC/3s/+eQTnQdiiCHmiRXBbbYJ2cdPyG8yPisr65/OQeoN5geKISFWQTEk9kExJD65iSEkEDz//PNaxiBqfokdZO2yyy5TI0aM0IL3xx9/qAceeEA/KvbF8KuvvtJ5UWIIfDG86KKL1B133BERQx+UGHbp0kWLIUont2zZooXyl19+UXPmzNFi+Ntvv6l9+/ZF5ikswW0ualBnUM67/8hvcbfE/5nphYViSIhVUAyJfVAMiU9uYojHuQD1DEuVKqVlTY4TPa169epqw4YNup4hRA0li7fffrsWR18MRTJ0XlMMixUrpksRUTp400036WWDjRs3qldffVWVLl1aZWVlaWlEoISyUqVKaty4cZG8sSK4zUWJ7P/Osr8mlilTJsuJUQmhCcWQEKugGBL7oBgSn9zE0JQoiKHJtGnTzElxAXIYy5JCn+A2JxIRsHNkn58oAlxGPpY00+MBxZAQq6AYEvugGBKfvIhhMhLc5gTwNxGvs3GOiejeZibGG4ohIVZBMST2QTEkPtHE8N1339X1CfGGsIlfPxB1EMGSJUsiaW+++WZkPC/gpZWCsHPnzgPG81uqGNzmeCD7tZ73AkkxMy3RUAwJsQqKIbEPiiHxiSaG27dvV6+99lqkSZkgeBEEnHTSSXrYs2fPSBpeTEkEH330UWT8qaeeitRnzCvBbY4xh8k+7ZaRkdHFiVOdwfxCMSTEKiiGxD4ohsQnmhiCzz77TDVt2lQ3Ol2rVi3dlmGzZs3Ud999p2Xwv//9r36j+Mwzz9Qvl+AlFDRHc/PNN+t2EPFWMdot9NswvPrqq9V7772n6tWrp6Vy7NixOk+TJk10u4i7du3S8wI0lt2+fXtdonjDDTfoN5r79++vTjvtNNWuXTs1aNAgLYd4sQUvsiDyQ3CbY0FaWtqxcu5kVK5c+e8IM70ooRgSYhUUQ2IfFEPiczAxfPnll9VPP/2km4yByKGksFWrVroXkoEDB6pzzz1XSxok8fHHH9ftG0L0wPHHH6+GDBmix/1Hz8i3YsUK1aBBAy2HN954o27zsGXLlmrSpEnq7bffVtdee63O37FjR3XWWWep5cuX63lPPfVULZ0QTQgqpLBGjRpaOmfNmqUefvhhd6XzSHCbC0KZMmX+jUanIVTZ2dmlzHSboBgSYhUUQ2IfFEPiE00M0T4huqJD/T3IF0rt8Hby999/r0v4evTooU4++WT9yBnCh5I75PHrDOYmhgDLgnD6Yrh582a1devWiBhCPoNiiKZssC5BMUR3fBBDlFQCtJvYr1+/yHfkheA25weUBso+u1vOm7McC+oP5gWKISFWQTEk9kExJD7RxNAXPIgfSg1//PFHXRqIbuow/PXXX3UapA7jEEg8CgaYZvZSgmUgD5aLZWEcy4L0QTbxkgukEI+p8SIJ0pAHy/n22291XUc0eo15Ef76+XnuuuuuA77vUAS3+VBkZGRUkkFJr4mZUMhgEIohIVZBMST2QTEkPtHE0O+eLp5AMPP7NnE00G1ffghuc26IDFaUfdNRhveINFUw08MExZAQq6AYEvugGBKfaGKIUrl4U9CmanLDLKE8FMFtNsHjYhHCSXJuHFetWrW/melhg2JIiFVQDIl9UAyJTzQxTHaC2ywS2Fz2w9ysrKzT8EJJMC0ZoBgSYhUUQ2IfFEPik8JiWFxkqJZI4XWyD5o4Iaw7mFcohoRYBcWQ2AfFkPikihiiLiNeYJkyZYp65plnVOnSpY8KbncyQzEkxCoohsQ+KIbEJxXEcO/evfpFl23btulxENzmZIdiSIhVUAyJfVAMiU8yiyGa00H7iHfffbeZRDE0oBgSkjAohsQ+KIbEJ5nEEI1lv//++7pdRL8/52gEtznZoRgSYhUUQ2IfFEPikyxiiG7y0H+y3yXfoQhuc7JDMSTEKiiGxD4ohsQnrGIICezevbvudSW/bRiC4DYnOxRDQqyCYkjsg2JIfMIkhug2D4FHxitWrDCT80Vwm5MdiiEhVkExJPZBMSQ+YRFDNDEzevRoLYax6DEluM3JDsWQEKugGBL7oBgSH1vFcOvWrTo2btyoHxfHmuA2JzsUQ0KsgmJI7INiSHxsEkO8Sfzmm2+qgQMHqpdffjkmJYPRCG5zskMxJMQqKIbEPiiGxMcUw5EjR+6bPHnyn4mKSZMm/dWrV6+/+vbtmyMtnhHc5mSHYkiIVVAMiX1QDImPKYZZWVnHJyIyMzPPl2PvTfn+3mZaIiK4zckOxZAQq6AYEvugGBIfUwzjDaRMjrGactydlZ6eXtZMJ7GHYkiIVVAMiX1QDIlPAsTwMBxj8j1t5Ng6w0wk8YdiSIhVUAyJfVAMiU+cxbBYdnb2P2RY3EwgiYNiSIhVUAyJfVAMiU8sxVCWlZ6VlfVPiTIZGRmHm+mkaKAYEmIVFENiHxRD4lNYMUSJoBxD9eW4qZ+ZmZllppOih2JIiFVQDIl9UAyJT2HEEKWCMn89GVZESaGZTuyAYkiIVVAMiX1QDIlPfsRQ8jYVgXhNRPB0imB4oBgSYhUUQ2IfFEPikxcxLF++fOnatWuXlLwdzTRiPxRDQqyCYkjsg2JIfHITQ5l2osR9EoPKlCnzbzOdhAuKISFWQTEk9kExJD5BMaxWrdrf5PNlmZmZZ6SlpR0bzEfCC8WQEKugGBL7oBgSH4hhRkZGdRGDDjL+uPz+Nc08JNxQDAmxCoohsQ+KYWqDZmVEBq/Jzs4uJb/7KDOdJBcUQ0KsgmJI7INimLrI79xEojdKCb3POeoYkuSCYkiIVVAMiX1QDFMHEcAr5Df+r4wW8+IAKIbJD8WQEKugGBL7yK8Y4pFjMoS5XYdC5vmHuQzbw1t1XHiKixTWgBgGt8mEYpj8UAwJsQqKIbGPfIphiTPOOGN5MkTFihUj250XqlWrttBchu2RlpZWKysrq7L8rv+STShhbpNJIsUQfSinp6efIt/ZQIT1SgwzMzNPl3Utb+YlsYNiSIhVUAyJfeRHDKtWrXrDDz/8oJIhHnzwwV+C23YozPltjE8++URt3rxZbdy4UW3ZskWddtppA8ztOBixFsPjjjuuXIUKFZ6sX7/+irlz5+5bvXr1H5s2bVI7duzIse7B2L59u0K+Dz744I85c+bs69Sp02eyrAVyPGaY30HyB8WQEKugGBL7yI8YVqtWra15Ew9rDBw48Nfgth0Kc36bYvHixWrAgAHq1VdfVbt27YpMP+OMMwaZ23EwYiWGspwT8d3t2rX7edmyZTnWtyAB0W3btu0vp59++kOy/Grmd8aK888/f3gyhGxKcXPbAMWQEKugGBL7oBjmDXP+oozly5erefPmqS+//PKgpW8JFsPi1atXv2327Nl/musRj5g5c+a+K664YlZaWlo5c0UKg/k9YY3mzZv/ZG4boBgSYhUUQ2IfFMO8Yc5fFIFHxS1btlQjRoxQn3/+eY50MxIphlWqVJkrwpoQKfRj/fr16sorr1xjrkthML8jrIE/Dea2AYohIVZBMST2QTHMG+b8iYyvvvpK1x3s0aNHnoTQj0SJYd26dYdAWs3vT1TUqVOnv7lOBcVcdpijfPnyOV6wohgSYhUUQ2IfqSKGzZs3j0SzZs2sFkOI4Isvvqg6deqk5s+fnyM9WgS3cevWrXEXQ7xZ/Mgjj+w116MoYvDgwXtEhEqb65hfzOWGOfAnwtw+iiEhVkExJPaRKmL4/fffR6JRo0bWiuELL7ygFi1apD766CO1c+fOHOkHi+A2JkIMzzzzzLfMdSjKqFOnzuvmOuYXc5lhD3P7KIaEWAXFkNhHKojh6tWrVf/+/SOfbRLDtWvXqlmzZqkVK1box8Vmel4D9Q6fffbZyOd4i+HJJ58821wHG0LW61lzXfODubywxksvvaTj4osv/i64fRRDQqyCYkjsIxXEcOnSpWrYsGGRz0Uthminb+HChXr866+/zpFekMDj8Y4dO0Y+x1sMX3nllX3mOtgQzz//fL5+VxNzeWEN/FFAdO/e/a/g9lEMCbEKiiGxj1QRQ7Tx538uSjHcsGGDeuqpp9TkyZNzpBUmEi2G5vfbFOa65gdzWWEPvCke3D6KISFWQTEk9pEqYogXOfzPiRRDNKny2GOP6aZmgo1PxzpuueUW1a9fv8jnVBbD7Ozsa8z1zSvmssIeFENCrIZiSOwjFcTQjESI4XfffafjlVdeyZGWiEhlMaxSpUqet8PEXFbYg2JIiNVQDIl9pIIYovmXOXPmRD7HQwzxJrAsE30wq2+++SZHerzjjTfeUO+++27kc5zFsJj5/TZFZmbmJHOF84q5rLAHxZAQq6EYEvtIBTHEo+SRI0dGPsdSDPF4GMtH6SCamDHTExVXXXWVuummmyKfU1kMs7OznzZXOK+Yywp7UAwJsRqKIbGPVBHDWL988uGHH6qxY8fqEsLPPvssx3cmOhL88kmxLVu25FgHGwKlwxUrVnzKXOG8Yi4v7EExJMRqKIbEPlJFDNu0aRP5XBAx/OKLL9Trr7+u3nzzzZg1MRPLuO2229T9998f+RxvMUTp6AcffJBjPYoy3n//ff2yD8Vwf1AMCbEaiiGxj1QQQ9T5Q0mS/zm/Yij7o/tdd92lFi9erIXLXL4NAVkNluLFWwzxHcG3oG2IAQMG6CHFcH9QDAmxGoohsY9UEEPEtGnTInEoMZQbYyPZB+Ozs7P/IR9LYJq5PBsjuI2JEEMEhPSee+5RS5YsybE+iYgdO3aoHj16HCDFFMP9QTEkxGoohsQ+UkUM8fKJH1OnTs1NDEvKdqdLDJQb441GWiiEIbiN27ZtS4gY+tGtWzc1bty4hJWoohQYb2I3b948RxrFcH9QDAmxGoohsY9UEUMzgmIo21kzMzPzaBlmCMcEt9nHnD8MkUgxRKC5nBtvvFFNnDgxR1os4+mnn1YdOnRQs2fP1m+Dm+kUw/1BMSTEaiiGxD5SVQz79u27B9t79NFHZwa3MRrm/GGIRIthMFBiuWzZMvRdrF+KwVvTqJM4ZcoUPf3LL7/MMQ/i22+/1S8LIZ/8Rqpp06bqzjvvVE888QQkRy/XnMcMiuH+oBgSYjUUQ2IfqSqGuTxKPijm/GGIohRDM9De4/z583X3gBA9PAK+9NJLtfhdc801enjFFVfo+p94LD1q1Ci1YMEC3XC4uaxDBcVwf1AMCbEaiiGxD4ph3jDnD0PYJIaJDIrh/qAYEmI1FENiHxTDvGHOH4agGOYfc1lhD4ohIVZDMST2QTHMG+b8YYhkEsP8NIdDMdwfFENCrIZiSOyDYpg3zPnDEEUphh9//LE68cQTVdWqVfVLJNu3b9cvodSoUUN3I4gXUJCvVatWuo/nK6+8UjdCvnDhQp2vVq1aavfu3TrPCy+8oOTYVLVr187xPblFosQQ3SJiOzH++eef6wbQMQ11KdFd4saNGyPbgGnolQXj77zzjq47iRdw8KKNvzz03IJ9gHmQF8vG+Nq1a9XmzZvVW2+9le/+uCmGhFgNxZDYB8Uwb5jzhyGKWgzRZR7eSm7fvr0WQ7xVDOGBGNatW1fnw9vKIiJqxowZavTo0erTTz9VvXr1Uhs2bIi8eHLttdeqSpUq5flFlESJYb169dStt96qZfbMM89U48ePVw0bNtQNbqMtyQYNGug05J00aZK6+eab9T7B9qEdxmeffVa/iOPLY5cuXXQbm5i+atUqvWy8hf3QQw+pdevW6ZdyfKHOa1AMCbEaiiGxj1iJIfoS9sdxQ3vyyScPSH/ttdfUJZdcot57770c8yJw48QQN0mUnJjpI0aMyDENJShYJsavu+46PfQ/Y3iwPo1jKYb4LpQUmdP9uO+++yLrFQy/C7e8hC8PZqDU6bnnntN9OAeBaIHLL79cp/v0799fzZs3T11//fWRafjt0Jd0NNBuoA+ajfnpp5/077tp0yY9DSVgPnv27NHrBTGEBM6cOVP/dhBDTJ8wYYIWQ3/9UWJYoUIFLUIoKYQ8YjreYvb7YsYbzHXq1Mmx7dEiUWLYtWtX/Yh7yJAhWn7RuDfkD0MIcf369dXq1at1XhzfLVq0iBzbvhgOHz488ttCitFI+AUXXKAlGPPi98U8yIPfFPvaXI+DBcWQEKuhGBL7iJUY4gaHIWQNjxBxk8NnNHrs50FJCh6p4WaKx27I689z++236/FPPvlES4s/joAkNG7cOCINb7/9th76bdrhpokGjzGOvGj4GOO+jOQWvhhWrlz578FtjIY5vxk1a9aMbA9KdfD4D23y7dy5U9122216vXzphfAsWrRIyyT2AwQW6ZjmPyrEsiBe6PINnzEvZMt/PInw87dt21bnQ7dw2F/Ig33tAwn5448/1C+//KL3Y6dOnXQeSBtAaRRYs2aN+vPPP9WPP/6ofz/sX6wrpGflypXq999/19KPdUUJGfIjHbL4119/6e/PTQyxb/BbID9KBZs0aaLlB/l8MezcubNeD5QUYv1eeeUVvd7IA7E+8sgj9bFj7vfcItFiiD89p5xySkQMBw0a9P/tnV+IVNcdx2mhb1KCdLshhs1WVsSHkDyF0qJG0doiJaQhlEIjAYsiEqiErNGtoFhBRFypghhICIKQgP+e/NP4FlpKUw0+xUqhtNCXxte20YRO7+fisTdHd3NHd66/M/v5wJeZ2bn3zrn3js5nfuecO/V7cMWKFXUFkGVp+8aNG3sXLlzgvVd3NSOGu3btuiuGBw8erJfnOc7n1q1b63PA8kiiYigydCiGEo+5EsO1a9fWH3B0py1ZsqS3ffv2+jFVoFQlTGL4yiuv1B9+VI8QkHfffbf+0GSZo0eP1rLw3nvv1ctxrbuzZ8/WFTeqWkghFbDma/PrFx999FF9PwkI9xGzvJ0piOHChQu/We3r6819nIl8/TzVsal/8QPRo50rV66suxI/+OCDWgw5Fly0+cUXX6z3hd8XpspJ9ezGjRv1WLMdO3b0Vq1aVY8n45iw3u7du+vts/769evrqhP7iyggTIghYsLyHAO2j2SzboJzgFRMTU3VYsi5QeS43xRDKl+IHpKP0NHlibBRsWP7COCrr75aiyHXHOQ4071J9ynHmtehmkh7ERraSTWSc5y6gTkGSGb6xRJEBwnk5+14zLJ8AWC5dGwRZ9rPeyI/7vfLg4hhJUPbuM23NVtoJ+eK+xwLjguiSDv5e/pSw/P8Lb0v2V8ec97TlxiCFKZxiIT3Ere8N/hCwbFMr9c2iqFIaBRDicdciSFVQipG3H/uuedq8UEy6EJL3WdJDKmUIHB0t/HhiehRFWEZKkrIFHJ57ty5umuNyQtUYhiYj/QgSdxnebpK+fBFwqgwcf/AgQN3u6bposzbShDD0dHRf1QfgpurrGvkhynVcfhRCmKSwgd7M2fOnKnbSpA3XpsLNfPzcFSVELDTp0/31q1bV0+koGKG+CDTyBaSwAWeWW/NmjW9ycnJ+vkNGzbUssnzPFedj1o4qTIhzUg4YsjxQ9w4lrQBkab7timGCNzFixfrttA9SWWQ9iFySQyROaQQEEdEhXOIsLJdKrnc5xgfOnSoFhbOA+1ADDm/vAbnkBw7dqwOF6tO4QLXhC8A7MeRI0fupvlbz7R/z5499bGgTWR6evpueP0UhDaFqtuiRYv+VJ2z7dX5fbORHaQ6rzvvZKqRX1XLf1jl9/n7pPQohiKhUQwlHnMlhgTBQNqSGCISiAtdojyfiyGhq5mKUBJDqiLICJMTEICmGNJ1mgbrp25kbpkRyn3khcoUs1xTm1KVJs+druRvVLIw1dzHmcjXz4PY0C7EiaoR3YDs01tvvVVX71iGdrGvyC23VAyRWSpHdD/S7Ux1DjFOy6fuc7aNZFGdQyIZ48fyHF+qiIhhWofXozrYFENEk/OBGH722Wd1RZDjDJw3jhPHkgoW7QP+RlUSgWQSBdug2xoxREx5noon3fjsA/dTxfBR5kEqhtX74Bq3+bZKj2IoEhrFUOIxl2LYdRCj/G9tM5eTT7oO3cXp/lzAuMAcBO9BSBNLnn322VockVIec/mZ5j5QbUSO6SpHULmMTXouDQtIOXXqVG9kZKSuuNJln/7OUITmcikPIoaJfFv3S9onxpEi03SH0z3OlxIq1OxP+i1ohJq2M3aSLww8z3J0CSP17MPy5ct7b7zxRr0dgthzS6WdY5i+QDEmNVXAqQ6nLw2zRTEUCY1iKPEoWQzTpUBmymzjsUoWw+YYy2ggmcgSEzGeeeaZuxL12GOP1ZXf1G6qjlRUqXAySSNVlQnd60g/Xek8ptJMtzMzqhnz2NxGfmzIoMUwzcBHVNkHus0ZG0rVlLGyjINF7rilC3xsbKye7MMkHoYYIHtUafmN6G3bttViSEWc6jJd6FRj9+3b11u9enVd4eU3o5mVzbJbtmypJZOKPDKZty2PYigSGsVQ4lGyGD5MShbDtun6OoZU0Khspdnge/fu/VLFkPGAdPen5en+ZhIK4y55nESPSRcMD+CyOjymW57JNlQSZ7rcUTODFkPCmFrGitI2HnMNQibQnD9/vh5ry/UZGSPJsWCGMftJNz1VQ6qlVPwYP8n+LFiwoB4fiQxz/JBCbhFjjiFDLxg3y9hRhldQOeT4MOwgb1cexVAkNIqhxEMxbEe+fgnpWgypEnKbxnUy8aYphnS3cj/NUGYGMhNSmIhCFS79HTFEItN2Gb/Jc1Qi6UpOrzNTBi2GTLhB8KgAIoZcggaxo/LHGFPEkHGZzLxn0hB/Z7YxYsjlfugmZswo4kcVlefZLmKMDCOGjAGlOopgIoSIIRVCxthyfOiCZ1t52/IohiKhUQwlHophO/L1S0jXYpjG1SFOVLSYdd0UQ7qCmWGdJgshRVTV6EpuThhKPy2XlmNGN7fpgtf3uwB6M4MWQ2Y+nzhxop4MheRxn7bRzUtFD8FL16Ok7UgvIkgXMMtyy3MIJbJIlZHHLMPz7B+3bBtRRIQZW4gsEpalepqu5zlbFEOR0CiGEg/FsB35+iWkazGk+td83PYn7FLShZ7bZqYLmA9aDEuKYigSGsVQ4qEYtiNfv4R0LYapOthVHtWs5JKiGIqERjGUeCiG7cjXLyFdi2GUKIb/j2IoEhrFUOKhGLYjX7+EKIb9k2+r9CiGIqFRDCUeimE78vVLiGLYP/m2So9iKBIaxVDioRi2I1+/hCiG/ZNvq/QohiKhUQwlHophO/L1S4hi2D/5tkqPYigSGsVQ4qEYtiNfv4Qohv2Tb6v0KIYioVEMJR6KYTvy9UuIYtg/+bZKj2IoEhrFUOKhGLYjX7+EKIb9k2+r9CiGIqFRDCUeimE78vVLiGLYP/m2So9iKBIaxVDioRi2I1+/hCiG/ZNvq/QohiKhUQwlHophO/L1S4hi2D/5tkqPYigSGsVQ4qEYtiNfv4QMUAxDH4+xsbEf5+1tS76t0qMYioRGMZR4KIbtyNcvIfNVDPO29kO+rdKjGIqERjGUeCiG7cjXLyGDFMOTJ0/+N3+9CDl+/PiXRKhf8u2VHsVQJDSKocRDMWxHvn4JGaQYLl269HD+ehHy9NNP783b2g/59kqPYigSGsVQ4qEYtiNfv4QMUgwrvn7t2rVQVcOrV68+VLUQ8m2WHsVQJDSKocRDMWxHvn4JGbAY1hw+fPg/+es+ivR7Pmci327pUQxFQqMYSjwUw3bk65eQLsRw2bJl68+cOfN5/tpd5v333/9iYmLiB3nbHoR826VHMRQJjWIo8VAM25GvX0K6EMPE+Pj49rfffvt23oZB5sqVK72XX375Yt6WhyF/jdKjGIqERjGUeCiG7cjXLyFdiiFUQvHzS5cufXHz5s172jKX+fTTT3vnz5//fOnSpZfyNjws+WuVHsVQJDSKocRDMWxHvn4J6VoMYXx8/GebN2/+ayWIt/L2zEUuXLhwa9OmTX+pXuen+WvPBZcvX+4NU9555x3FUCQuiqHEox8xHB0d/c7HH3/cG4Y8//zzf2ju21dx7ty5L/JtRM/ExMR38/2YjbkQwya8X5588slfvPTSS2enpqZunT59+vb169fvkb375ZNPPumdOnXq9s6dO29t3Ljx6pIlSyar9o3nryH9oRiKhEIxlHj0I4awbNmy3w5Dqn0cyfdtNioxmc63ET35PnwVcy2GTUZGRh6vhGPb4sWLf/fUU0/9bfXq1ddfe+21f+7fv/9f09PT/+Z2cnLy5gsvvPDnsbGxv1fLfViJ5S+r9+Bovi15cBRDkVAohhKPfsVQhpdBiqHEQDEUCYViKPFQDCWhGA4/iqFIKBRDiYdiKAnFcPhRDEVCoRhKPBRDSSiGw49iKBIKxVDioRhKQjEcfhRDkVAohhIPxVASiuHwoxiKhEIxlHgohpJQDIcfxVAkFIqhxEMxlIRiOPwohiKhUAwlHoqhJBTD4UcxFAmFYijxUAwloRgOP4qhSCgUQ4mHYigJxXD4UQxFQqEYSjwUQ0kohsOPYigSCsVQ4qEYSkIxHH4UQ5FQKIYSjxZiyBvXDF/uQTEcfhRDkVDc9/9ikUfKbGI4MjIywX0zfEnnvEn1999U74efmOHN44qhSCQUQ4nHt2cRQxGZfyiGIp2hGEo8FEMRaaIYinSGYijxUAxFpIliKNIZiqHEQzEUkSaKoUhnKIYSD8VQRJoohiKdoRhKPBRDEWmiGIp0hmIo8VAMRaSJYijSGYqhxEMxFJEmiqFIZyiGEg/FUESaKIYinaEYSjwUQxFpohiKdIZiKPFQDEWkiWIo0hmKocRDMRSRJoqhSGcohhIPxVBEmiiGIp2hGEo8FEMRaaIYinSGYijxUAxFpIliKNIZiqHEoxLD76X7lRC+fudWMRSZpyiGIp2hGEoZKIYi8xfFUKQzFEOJyRNPPPGtdL/6UPi1Yigyf1EMRTpDMZSYVGL4/cbDrymGIvMXxVCkMxRDicvo6OibKZUY/rH52Bgzf1L9+z+S/80YM5DsyD+LRUREREREREREREREREREREREREREIvE/aMoonJ65x0sAAAAASUVORK5CYII=>