# Mini Bank Application

Mini Bank merupakan aplikasi simulasi perbankan sederhana yang dibangun menggunakan **Java 21, Spring Boot 4, dan PostgreSQL**.

## Fitur Utama

1. **Registrasi Customer**
   - Customer melakukan registrasi.
   - Sistem otomatis membuat User, Customer, Account, dan Account Balance.

2. **Login**
   - Customer yang sudah terdaftar dapat login menggunakan kredensial.

3. **Transfer**
   - Customer dapat melakukan transfer ke rekening nasabah lain.
   - Sistem melakukan validasi rekening dan saldo sebelum transaksi diproses.

## Arsitektur Aplikasi

Aplikasi ini menggunakan **Modular Monolith Architecture**.

Arsitektur ini dipilih untuk mempercepat proses pengembangan sekaligus menjaga pemisahan tanggung jawab setiap modul.

Modul aplikasi meliputi:
- Authentication
- Customer
- Account
- Transaction
- registrasi


Setiap modul memiliki tanggung jawab bisnis masing-masing sehingga lebih mudah dikembangkan dan dipelihara.

Sebagai contoh, modul Transaction dapat dikembangkan lebih lanjut untuk mendukung setor tunai, tarik tunai, transfer internal, dan transfer antarbank.

## Teknologi

- Java 21
- Spring Boot 3
- Spring Data JPA
- Spring Security & JWT
- PostgreSQL
- Flyway
- Docker & Docker Compose

## Cara Menjalankan Aplikasi

**Prerequisites:** Docker sudah terinstal di komputer.

1. Clone repository:

```bash
git clone https://github.com/AminTurmudiYusup/minibank.git
```

2. Masuk ke direktori project:

```bash
cd mini-bank
```


3. Build dan jalankan aplikasi:

```bash
docker compose up --build -d
```

Aplikasi dan database akan berjalan di dalam Docker container. Database migration akan dijalankan otomatis menggunakan Flyway.

Aplikasi dapat diakses melalui:

```text
{{baseUrl}}/api/registrations
{{baseUrl}}/api/auth/login
{{baseUrl}}/api/transfers
```

## Sample Registration Request

**Endpoint:** `POST /api/auth/register`

**Content-Type:** `application/json`

### Customer 1

```json
{
  "username": "amin",
  "email": "amin@test.com",
  "password": "Password123!",
  "fullName": "Amin Turmudzi",
  "phone": "081234567890",
  "address": "Jakarta"
}
```

### Customer 2

```json
{
  "username": "budi",
  "email": "budi@test.com",
  "password": "Password123!",
  "fullName": "Budi Turmudzi",
  "phone": "081234567891",
  "address": "Jakarta"
}
```

## Sample Login Request

**Endpoint:** `POST /api/auth/login`

**Content-Type:** `application/json`

```json
{
  "username": "amin",
  "password": "Password123!"
}
```


## Sample Fund Transfer Request

**Endpoint:** `POST /api/transfers`

**Headers:**

```http
Authorization: Bearer <YOUR_ACCESS_TOKEN>
Content-Type: application/json
Idempotency-Key: transfer-001
```

**Request Body:**

```json
{
  "toAccountNumber": "{{accountB}}",
  "amount": 500000,
  "description": "Transfer to Budi"
}
```

**Notes:**
- `Authorization`: JWT access token obtained from the login endpoint.
- `Idempotency-Key`: Unique key used to prevent duplicate transactions when the same request is submitted multiple times.
- `toAccountNumber`: Destination account number.
- `amount`: Transfer amount in IDR (Rupiah).
- `description`: Transfer description.

## Stop Application

To stop the application and remove the running containers, execute:

```bash
docker compose down
```
