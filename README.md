# Baseapp — Spring Boot REST API with JWT Authentication

A production-ready **Spring Boot base application** with full **JWT Authentication & Authorization** using Spring Security 7, JPA, and PostgreSQL. Designed to be used as a starting template for building secure REST APIs.

---

## 🧰 Tech Stack

| Teknologi | Versi | Keterangan |
|---|---|---|
| **Java** | 21 | Language utama |
| **Spring Boot** | 4.1.1 | Framework utama |
| **Spring Security** | 7.x | Autentikasi & Otorisasi |
| **Spring Data JPA** | — | ORM Layer |
| **Hibernate** | 7.4.5 | JPA Implementation |
| **PostgreSQL** | 16 | Database |
| **JJWT** | 0.12.5 | Library JWT |
| **Lombok** | — | Boilerplate reduction |
| **Maven** | — | Build tool |

---

## 📁 Struktur Project

```
src/main/java/com/alfaizunawebid/baseapp/
│
├── BaseappApplication.java          # Entry point aplikasi
│
├── config/
│   ├── ApplicationConfig.java       # Bean: PasswordEncoder, AuthManager, UserDetailsService
│   ├── JwtAuthenticationFilter.java # Filter: validasi JWT pada setiap HTTP request
│   └── SecurityConfiguration.java  # Konfigurasi SecurityFilterChain & whitelist route
│
├── controller/
│   ├── AuthenticationController.java # POST /register & POST /login
│   └── DemoController.java           # GET /hello & GET /admin (contoh protected endpoint)
│
├── dto/
│   ├── AuthenticationRequest.java    # Body request login { email, password }
│   ├── AuthenticationResponse.java   # Response { token }
│   └── RegisterRequest.java          # Body request register { name, email, password }
│
├── model/
│   ├── Role.java                     # Enum: USER, ADMIN
│   └── User.java                     # JPA Entity + implements UserDetails
│
├── repository/
│   └── UserRepository.java           # JPA Repository: findByEmail, existsByEmail
│
└── service/
    ├── AuthenticationService.java    # Logic register & login
    └── JwtService.java               # Generate, validasi & ekstrak data dari JWT
```

---

## ⚙️ Konfigurasi

### `src/main/resources/application.yaml`

```yaml
spring:
  application:
    name: baseapp
  datasource:
    url: jdbc:postgresql://localhost:5432/baseapp_db
    username: postgres
    password: ""
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: update   # Ganti ke 'validate' di production
    show-sql: true       # Set false di production

# Konfigurasi JWT custom
application:
  security:
    jwt:
      secret-key: ${JWT_SECRET:404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970}
      expiration: ${JWT_EXPIRATION:86400000}  # 24 jam (dalam milidetik)
```

### Environment Variables (Production)

| Variable | Keterangan |
|---|---|
| `JWT_SECRET` | Secret key untuk signing JWT (min. 32 karakter) |
| `JWT_EXPIRATION` | Masa berlaku token dalam milidetik |
| `SPRING_PROFILES_ACTIVE` | Set ke `prod` di server production |

---

## 🚀 Cara Menjalankan

### Prerequisites

- Java 21+
- PostgreSQL 16+
- Maven (atau gunakan `./mvnw`)

### 1. Setup Database

```bash
# Buat database
createdb baseapp_db

# Atau via psql
psql postgres -c "CREATE DATABASE baseapp_db;"
```

### 2. Jalankan Aplikasi

```bash
# Clone & masuk ke direktori project
git clone <repo-url>
cd baseapp

# Jalankan dengan Maven Wrapper
./mvnw spring-boot:run
```

Aplikasi akan berjalan di: **http://localhost:8080**

> Hibernate akan otomatis membuat tabel `_user` di database saat pertama kali dijalankan (karena `ddl-auto: update`).

---

## 📖 Apa itu JWT?

**JWT (JSON Web Token)** adalah standar terbuka ([RFC 7519](https://datatracker.ietf.org/doc/html/rfc7519)) yang mendefinisikan cara yang ringkas dan mandiri (*self-contained*) untuk mentransmisikan informasi secara aman antar pihak dalam format JSON.

Token JWT **ditandatangani secara digital** (bukan dienkripsi), sehingga:
- Siapapun bisa **membaca** isi token (jangan simpan data sensitif di dalamnya)
- Tidak ada yang bisa **memalsukan** token tanpa mengetahui secret key

---

## 🧬 Anatomi / Struktur Token JWT

Sebuah token JWT terdiri dari **3 bagian** yang dipisahkan oleh titik (`.`):

```
eyJhbGciOiJIUzUxMiJ9  .  eyJzdWIiOiJqb2huQGV4YW1wbGUuY29tIiwiaWF0IjoxNzg3ODIxNTAwLCJleHAiOjE3ODc5MDc5MDB9  .  _J2Su3q6mhcdkGfxtr...
      HEADER                                        PAYLOAD (CLAIMS)                                                    SIGNATURE
```

### 1. Header
Berisi metadata tentang jenis token dan algoritma kriptografi yang digunakan.

```json
{
  "alg": "HS512",
  "typ": "JWT"
}
```
- **`alg`**: Algoritma signing — project ini menggunakan `HS512` (HMAC-SHA512)
- **`typ`**: Selalu bernilai `JWT`

### 2. Payload (Claims)
Berisi data (*claims*) yang ingin dikirimkan. Ada 3 jenis claim:

| Jenis | Contoh | Keterangan |
|---|---|---|
| **Registered Claims** | `sub`, `iat`, `exp` | Claim standar yang sudah didefinisikan RFC 7519 |
| **Public Claims** | — | Claim yang didaftarkan secara publik (IANA) |
| **Private Claims** | `role`, `name` | Claim kustom yang disepakati kedua pihak |

Payload di project ini:
```json
{
  "sub": "john@example.com",   // subject: identifier user (email)
  "iat": 1787821500,           // issued at: waktu token dibuat (Unix timestamp)
  "exp": 1787907900            // expiration: waktu token kedaluwarsa
}
```

> ⚠️ **Penting**: Payload hanya di-encode Base64Url, **bukan dienkripsi**. Jangan menyimpan password atau data sensitif di dalam payload.

### 3. Signature (Tanda Tangan Digital)
Dibuat dengan menggabungkan header + payload yang sudah di-encode, lalu ditandatangani menggunakan secret key:

```
HMACSHA512(
  Base64Url(header) + "." + Base64Url(payload),
  secretKey
)
```

Signature inilah yang menjamin **integritas** token. Jika ada satu karakter pun yang diubah di header atau payload, signature akan tidak cocok dan token akan ditolak.

---

## 🔑 Algoritma Signing: HMAC-SHA512

Project ini menggunakan **HMAC-SHA512** (`HS512`) — algoritma *symmetric* (satu kunci):

```
[Server Secret Key] ──▶ dipakai untuk SIGN token saat login
[Server Secret Key] ──▶ dipakai untuk VERIFY token saat ada request
```

**Keuntungan**: Sederhana, performa tinggi, cocok untuk aplikasi monolitik.

**Kekurangan**: Secret key harus dijaga ketat di server. Jika bocor, siapapun bisa memalsukan token.

> Alternatif: **RSA (RS256)** menggunakan *private key* untuk sign dan *public key* untuk verify — cocok untuk arsitektur microservices di mana beberapa service perlu memverifikasi token tanpa bisa membuatnya.

---

## ⚖️ JWT vs Session: Stateless vs Stateful

| Aspek | JWT (Stateless) | Session (Stateful) |
|---|---|---|
| **Penyimpanan** | Di client (header/localStorage) | Di server (memory/database) |
| **Skalabilitas** | ✅ Mudah di-scale (tanpa shared state) | ❌ Butuh shared session store (Redis, dll.) |
| **Logout** | ❌ Sulit — token valid sampai expired | ✅ Mudah — hapus session di server |
| **Revokasi** | ❌ Perlu blacklist token | ✅ Langsung hapus session |
| **Overhead per Request** | Lebih besar (token dikirim di header) | Lebih kecil (hanya session ID) |
| **Cocok untuk** | REST API, Microservices, Mobile App | Web app tradisional dengan server-side rendering |

Project ini menggunakan JWT dengan session `STATELESS` di Spring Security, artinya **server tidak menyimpan state apapun** — setiap request harus membawa token yang valid.

---

## 🔐 Alur Autentikasi JWT

```
Client                          Server
  │                               │
  │── POST /api/v1/auth/register ─▶│ Simpan user (password di-hash BCrypt)
  │◀─────── { token } ────────────│ Return JWT Token
  │                               │
  │── POST /api/v1/auth/login ───▶│ Verifikasi email & password
  │◀─────── { token } ────────────│ Return JWT Token
  │                               │
  │── GET /api/v1/demo/hello ────▶│ JwtAuthenticationFilter validasi token
  │   Authorization: Bearer <token>│ Load user dari DB, set SecurityContext
  │◀─────── { data user } ────────│ Return response jika valid
```

---

## 📡 API Endpoints

### Auth (Public — tidak perlu token)

#### Register
```http
POST /api/v1/auth/register
Content-Type: application/json

{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "secret123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9..."
}
```

#### Login
```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "email": "john@example.com",
  "password": "secret123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9..."
}
```

---

### Demo (Protected — wajib token)

#### Hello (semua user yang login)
```http
GET /api/v1/demo/hello
Authorization: Bearer <token>
```

**Response:**
```json
{
  "message": "Hello, John Doe!",
  "email": "john@example.com",
  "role": "USER",
  "authorities": "[ROLE_USER]"
}
```

#### Admin Only (khusus role ADMIN)
```http
GET /api/v1/demo/admin
Authorization: Bearer <token>
```

**Response (jika ADMIN):** `200 OK`
**Response (jika USER biasa):** `403 Forbidden`

---

## 🧩 Cara Kerja Komponen Utama

### `JwtAuthenticationFilter`
Filter yang dieksekusi pada **setiap HTTP request** sebelum masuk ke controller. Alurnya:
1. Ambil header `Authorization: Bearer <token>`
2. Ekstrak username (email) dari token via `JwtService`
3. Load data user dari database
4. Validasi token (signature & expired)
5. Set `Authentication` ke `SecurityContextHolder` jika valid

### `JwtService`
Utility service untuk mengelola siklus hidup JWT:
- `generateToken(userDetails)` — buat token baru saat login/register
- `isTokenValid(token, userDetails)` — cek validitas token
- `extractUsername(token)` — ambil email dari payload token

### `SecurityConfiguration`
Mendefinisikan aturan akses route:
```java
.requestMatchers("/api/v1/auth/**").permitAll()         // Publik
.requestMatchers("/api/v1/demo/admin").hasRole("ADMIN") // Khusus ADMIN
.anyRequest().authenticated()                           // Lainnya wajib login
```

---

## 🔒 Aturan Akses

| Endpoint | Akses |
|---|---|
| `POST /api/v1/auth/register` | Public |
| `POST /api/v1/auth/login` | Public |
| `GET /api/v1/demo/hello` | Semua user yang login |
| `GET /api/v1/demo/admin` | Hanya ADMIN |
| Semua route lainnya | Wajib login |

---

## 🛡️ Catatan Keamanan untuk Production

- [ ] Ganti `ddl-auto: update` → `validate` dan gunakan **Flyway** untuk migrasi
- [ ] Set `show-sql: false`
- [ ] Simpan `JWT_SECRET` di environment variable, bukan hardcode
- [ ] Gunakan HTTPS
- [ ] Pertimbangkan implementasi **Refresh Token** untuk token yang expired
- [ ] Tambahkan **Rate Limiting** pada endpoint `/login` untuk mencegah brute force
